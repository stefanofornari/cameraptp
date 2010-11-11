// Copyright 2000 by David Brownell <dbrownell@users.sourceforge.net>
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//

package com.ste.jphoto2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import java.util.Enumeration;
import java.util.Vector;

import com.ste.gnu.getopt.Getopt;
import com.ste.gnu.getopt.LongOpt;

import ch.ntb.usb.*;


/**
 * This is a command line tool, which currently supports
 * access only to PTP cameras.
 *
 * @version $Id: JPhoto.java,v 1.20 2001/05/30 19:35:13 dbrownell Exp $
 * @author David Brownell
 */
public class JPhoto
{
    /** No instances permitted */
    private JPhoto () { }

    // options
    static private File		directory;
    static private String	device;
    static private boolean	overwrite;
    static private int		storageId;

    static final private LongOpt	longOpts [] = new LongOpt [] {
	new LongOpt ("camera", LongOpt.REQUIRED_ARGUMENT, null, 'c'),
	new LongOpt ("directory", LongOpt.REQUIRED_ARGUMENT, null, 'd'),
	new LongOpt ("help", LongOpt.NO_ARGUMENT, null, 'h'),
	new LongOpt ("overwrite", LongOpt.NO_ARGUMENT, null, 'w'),
	new LongOpt ("storage ", LongOpt.REQUIRED_ARGUMENT, null, 's'),
    };

    private static void usage (int status)
    {
	System.err.println ("Usage: jphoto command [options]");

	System.err.println ("Key commands include:");
	System.err.println ("  cameras ... lists devices by portid");
	System.err.println ("  capture ... starts image/object capture");
	System.err.println ("  devinfo ... shows device info");
	System.err.println ("  devprops ... shows all device properties");
	System.err.println ("  format ... reformat a storage unit");
	System.err.println ("  images ... download images/videos to directory (default 'images')");
	System.err.println ("  reset ... reset request");
	System.err.println ("  storage ... shows storage info");
	System.err.println ("  tree ... lists storage contents");

	System.err.println ("Other commands include:");
	// System.err.println ("  delete filename ... deletes one object");
	System.err.println ("  devices ... (same as 'cameras')");
	System.err.println ("  getprop propname ... shows one device property");
	System.err.println ("  help ... shows this message");
	System.err.println ("  powerdown ... powers down device");
	// System.err.println ("  print ... prints according to DPOF order");
	System.err.println ("  put fileOrURL [...] ... copy object(s) to device");
	System.err.println ("  selftest ... runs basic selftest");
	// System.err.println ("  settime ... sets clock on device");
	System.err.println ("  status ... status summary");
	System.err.println ("  thumbs ... download thumbs to directory (default 'thumbs')");

	System.err.println ("Options include:");
	for (int i = 0; i < longOpts.length; i++) {
	    String	temp = " ?";

	    System.err.print ("  --");
	    System.err.print (longOpts [i].getName ());
	    switch (longOpts [i].getHasArg ()) {
		case LongOpt.REQUIRED_ARGUMENT: temp = " value"; break;
		case LongOpt.OPTIONAL_ARGUMENT: temp = " [value]"; break;
		case LongOpt.NO_ARGUMENT: temp = ""; break;
	    }
	    System.err.print (temp);
	    if (Character.isLetter ((char)longOpts [i].getVal ())) {
		System.err.print (" (or, -");
		System.err.print ((char)longOpts [i].getVal ());
		System.err.print (temp);
		System.err.print (")");
	    }
	    System.err.println ();
	}

	System.err.println ("Documentation and Copyright at:  "
		+ "http://jphoto.sourceforge.net/");
	System.err.println ("Licensed under the GNU General Public License.");
	System.err.println ("No Warranty.");
	System.err.println ();

	if (status != 0)
	    System.exit (status);
    }


    /**
     * Parameters are a command and any option parameters
     * such as <em><b>--camera</b> port-id</em>
     * specifying the the port id for a camera.
     * (See <em>usb.core.PortIdentifier</em> for information
     * about those identifiers.)
     * Such port ids may be omitted when there is only one
     * camera currently connected; list them
     * using the <em>cameras</em> commands.
     * PTP devices may support only some of these commands.
     *
     * <table border=1 cellpadding=3 cellspacing=0 width="80%">
     * <tr bgcolor="#ccccff" class="TableHeadingColor">
     *  <th>Command and Arguments</th>
     *  <th>Description</th>
     *  <th>Options</th>
     *  </tr>
     *
     * <tr valign=top>
     *	<td> <code>cameras</code> </td>
     *	<td> (same as "devices") </td>
     *	<td> <em>none</em> </td>
     * </tr>
     *
     * <tr valign=top>
     *	<td> <code>capture</code> </td>
     *	<td> starts capturing images or other objects, according
     *		to the current device properties. </td>
     *	<td> <em>--port-id</em> id <br />
     *	     <em>--storage</em> id
     *		</td>
     * </tr>
     *
     * <tr valign=top>
     *	<td> <code>devices</code> </td>
     *	<td> Lists PTP devices with their port identifiers </td>
     *	<td> <em>none</em> </td>
     * </tr>
     *
     * <tr valign=top>
     *	<td> <code>devinfo</code> </td>
     *	<td> Displays the DeviceInfo for a camera, including
     *		all the operations, events, device properties,
     *		and object formats supported. </td>
     *	<td> <em>--port-id</em> id </td>
     * </tr>
     *
     * <tr valign=top>
     *	<td> <code>devprops</code> </td>
     *	<td> shows all device properties, with types and values. </td>
     *	<td> <em>--port-id</em> id </td>
     * </tr>
     *
     * <tr valign=top>
     *	<td> <code>format</code> </td>
     *	<td> Reformats the specified storage unit (zero based).  </td>
     *	<td> <em>--port-id</em> id
     *		<br> <em>--storage</em> number
     *		</td>
     * </tr>
     *
     * <tr valign=top>
     *	<td> <code>getprop</code> <em>propname</em></td>
     *	<td> shows named device property, with type and value. </td>
     *	<td> <em>--port-id</em> id </td>
     * </tr>
     *
     * <tr valign=top>
     *	<td> <code>help</code> </td>
     *	<td> shows command summary</td>
     *	<td> <em>none</em> </td>
     * </tr>
     *
     * <tr valign=top>
     *	<td> <code>images</code> </td>
     *	<td> Downloads image files to directory </td>
     *	<td> <em>--port-id</em> id
     *		<br> <em>--overwrite</em>
     *		<br> <em>--directory</em> directory (default "images") </td>
     * </tr>
     *
     * <tr valign=top>
     *	<td> <code>powerdown</code> </td>
     *	<td> Causes the device to power down. </td>
     *	<td> <em>--port-id</em> id </td>
     * </tr>
     *
     * <tr valign=top>
     *	<td> <code>put</code> <em>file-or-URL [...]</em> </td>
     *	<td> Copies images or other objects to device.  </td>
     *	<td> <em>--port-id</em> id <br />
     *	     <em>--storage</em> id
     * </tr>
     *
     * <tr valign=top>
     *	<td> <code>reset</code> </td>
     *	<td> Issues a PTP level reset. </td>
     *	<td> <em>--port-id</em> id </td>
     * </tr>
     *
     * <tr valign=top>
     *	<td> <code>selftest</code> </td>
     *	<td> Runs a basic device self test. </td>
     *	<td> <em>--port-id</em> id </td>
     * </tr>
     *
     * <tr valign=top>
     *	<td> <code>status</code> </td>
     *	<td> Shows status summary for the device </td>
     *	<td> <em>--port-id</em> id </td>
     * </tr>
     *
     * <tr valign=top>
     *	<td> <code>storage</code> </td>
     *	<td> Displays the StorageInfo for the device's
     *		storage units, all or just the specified (zero base) store </td>
     *	<td> <em>--port-id</em> id
     *		<br> <em>--storage</em> number
     *		</td>
     * </tr>
     *
     * <tr valign=top>
     *	<td> <code>thumbs</code> </td>
     *	<td> Downloads image thumbnails to directory </td>
     *	<td> <em>--port-id</em> id
     *		<br> <em>--overwrite</em>
     *		<br> <em>--directory</em> directory (default "thumbs") </td>
     * </tr>
     *
     * <tr valign=top>
     *	<td> <code>tree</code> </td>
     *	<td> Lists contents of camera storage. </td>
     *	<td> <em>--port-id</em> id </td>
     * </tr>
     *
     * </table>
     */
    public static void main (String argv [])
    {
	if (argv.length == 0)
	    usage (-1);

	try {
	    Getopt	g;
	    int		c;

	    // get any options
	    g = new Getopt ("jphoto", argv, "c:d:h", longOpts);
	    while ((c = g.getopt ()) != -1) {
		switch (c) {
		    case 'c':		// camera or device
			device = g.getOptarg ();
			continue;
		    case 'd':		// directory
			directory = new File (g.getOptarg ());
			continue;
		    case 'h':		// help
			usage (0);
			System.exit (0);
		    case 's':		// storage unit
			storageId = Integer.parseInt (g.getOptarg ());
			if (storageId < 0) {
			    System.err.println ("--storage N ... "
				+ "parameter must be an integer");
			    usage (-1);
			}
			continue;
		    case 'w':		// overwrite files
			overwrite = true;
			continue;
		    default:
			usage (-1);
		}
	    }

	    // then the command
	    c = g.getOptind ();

	    if ("cameras".equals (argv [c]) || "devices".equals (argv [c]))
		cameras (argv, c);
	    else if ("capture".equals (argv [c]))
		capture (argv, c);
	    else if ("devinfo".equals (argv [c]))
		devinfo (argv, c);
	    else if ("devprops".equals (argv [c]))
		devprops (argv, c);
	    else if ("format".equals (argv [c]))
		format (argv, c);
	    else if ("getprop".equals (argv [c]))
		getprop (argv, c);
	    else if ("help".equals (argv [c]))
		usage (0);
	    else if ("images".equals (argv [c]))
		images (argv, c);
	    else if ("put".equals (argv [c]))
		put (argv, c);
	    else if ("powerdown".equals (argv [c]))
		powerdown (argv, c);
	    else if ("reset".equals (argv [c]))
		reset (argv, c);
	    else if ("selftest".equals (argv [c]))
		selftest (argv, c);
	    else if ("status".equals (argv [c]))
		status (argv, c);
	    else if ("storage".equals (argv [c]))
		storage (argv, c);
	    else if ("thumbs".equals (argv [c]))
		thumbs (argv, c);
	    else if ("tree".equals (argv [c]))
		tree (argv, c);
	    else
		usage (-1);

	} catch (BaselineInitiator.UnsupportedOpException e) {
	    System.err.println ("Device does not support " + e.getMessage ());
	    System.exit (1);

	} catch (IOException e) {
	    System.err.println ("I/O exception: " + e.getMessage ());
	    // e.printStackTrace ();
	    System.exit (1);

	} catch (SecurityException e) {
	    System.err.println (e.getMessage ());
	    System.exit (1);

	}
    }

    /*--------------------------------------------------------------------*/

    private static void indent (PrintStream out, int depth)
    {
	while (depth >= 8) {
	    out.print ("\t");
	    depth -= 8;
	}
	while (depth != 0) {
	    out.print (" ");
	    depth--;
	}
    }

    private static Vector getCameras ()
    throws IOException
    {
	Vector	cameras = new Vector (2, 5);

        LibusbJava.usb_init();
        LibusbJava.usb_find_busses();
        LibusbJava.usb_find_devices();

	UsbBus bus = LibusbJava.usb_get_busses();
        UsbDevice usbDevice = null;

        while(bus != null) {
            usbDevice = bus.getDevices();
	    while (usbDevice != null) {
		if (Initiator.getPtpInterface (dev) == null)
		    continue;

		cameras.addElement (dev);
	    }
	}
	return cameras;
    }

    private static Device getDefaultCamera ()
    throws IOException
    {
	Vector	cameras = getCameras ();

	if (cameras.size () != 1)
	    return null;
	return (Device) cameras.elementAt (0);
    }

    private static Device getCamera (String portId)
    throws IOException
    {
	try {
	    return HostFactory.getHost ().getDevice (portId);
	} catch (IllegalArgumentException e) {
	    return null;
	}
    }

    private static Initiator startCamera ()
    throws IOException
    {
	return startCamera (true);
    }

    private static Initiator startCamera (boolean session)
    throws IOException
    {
	Device		dev = null;
	Initiator	retval;

	if (device == null) {
	    dev = getDefaultCamera ();
	    if (dev != null)
		device = new PortIdentifier (dev).toString ();
	} else 
	    dev = getCamera (device);

	if (dev == null) {
	    if (device != null)
		System.err.println ("Nothing at port: " + device);
	    else if (getCameras ().size () != 0)
		System.err.println ("Specify a camera's port ID.");
	    else
		System.err.println ("No PTP cameras are available.");
	    System.exit (1);
	}
	retval = new Initiator (dev);

	if (Initiator.DEBUG)
	    // new Thread (retval, "Events/" + device).start ()
	    ;

	if (session)
	    retval.openSession ();
	System.out.print ("PTP device at ");
	System.out.println (device);

	return retval;
    }

    static void closeSession (Initiator dev)
    {
	try {
	    dev.closeSession ();
	} catch (Exception e) {
	    // ignore everything!
	}
    }

    /*--------------------------------------------------------------------*/

    private static void cameras (String argv [], int index)
    throws IOException
    {
	if (index != (argv.length - 1))
	    usage (-1);
	
	Enumeration	cameras = getCameras ().elements ();

	while (cameras.hasMoreElements ()) {
	    Device		dev = (Device) cameras.nextElement ();
	    DeviceDescriptor	desc = dev.getDeviceDescriptor ();
	    String		temp;

	    System.out.print (dev.getPortIdentifier ().toString ());
	    System.out.print ("\t");

	    if ((temp = desc.getProduct (0)) == null) {
		System.out.print ("0x");
		System.out.print (Integer.toHexString (desc.getVendorId ()));
		System.out.print ("/0x");
		System.out.print (Integer.toHexString (desc.getProductId ()));
		System.out.println ();
	    } else
		System.out.println (temp);
	}
    }


    private static void capture (String argv [], int index)
    throws IOException
    {
	if (index != (argv.length - 1))
	    usage (-1);

	Initiator	dev = startCamera ();
	int		status = dev.initiateCapture (storageId, 0);
	
	if (status != Response.OK) {
	    // FIXME -- gets replaced with the better scheme
	    NameFactory	factory = dev.getFactory ();

	    System.out.print   ("Can't initiate capture: ");
	    System.out.println (factory.getResponseString (status));
	}
    }


    private static void devinfo (String argv [], int index)
    throws IOException
    {
	if (index != (argv.length - 1))
	    usage (-1);

	Initiator	dev = startCamera (false);
	DeviceInfo	info = dev.getDeviceInfo ();

	info.dump (System.out);
	// no session to close!
    }


    private static void devprops (String argv [], int index)
    throws IOException
    {
	if (index != (argv.length - 1))
	    usage (-1);

	Initiator	dev = startCamera ();
	DeviceInfo	info;

	// FIXME -- gets replaced with the better scheme
	NameFactory	factory = dev.getFactory ();

	try {
	    info = dev.getDeviceInfo ();
	    for (int i = 0; i < info.propertiesSupported.length; i++) {
		DevicePropDesc	desc = new DevicePropDesc (factory);
		int		propcode = info.propertiesSupported [i];
		int		status;

		status = dev.getDevicePropDesc (propcode, desc);
		if (status == Response.OK)
		    desc.dump (System.out);
		else {
		    System.out.print ("... can't read ");
		    System.out.print (factory.getPropertyName (propcode));
		    System.out.print (", ");
		    System.out.println (factory.getResponseString (status));
		}
	    }
	} finally {
	    closeSession (dev);
	}
    }


    private static void format (String argv [], int index)
    throws IOException
    {
	if (index != (argv.length - 1))
	    usage (-1);

	Initiator	dev = startCamera ();

	try {
	    if (storageId == 0) {
		int	ids [] = dev.getStorageIDs ();
		if (ids.length != 1)
		    throw new IOException ("need to specify a storage ID");
		else
		    storageId = ids [0];
	    }
	    if (!dev.hasStore (storageId)) {
		System.out.println ("Store " + storageId + " is not present");
		return;
	    }

	    System.out.println ("reformatting storage unit " + storageId);

	    int status = dev.formatStore (storageId, 0);

	    if (status != Response.OK) {
		// FIXME -- gets replaced with the better scheme
		NameFactory	factory = dev.getFactory ();

		System.out.print ("... can't reformat: ");
		System.out.println (factory.getResponseString (status));
	    }

	} finally {
	    closeSession (dev);
	}
    }


    private static void getprop (String argv [], int index)
    throws IOException
    {
	if (index != (argv.length - 2))
	    usage (-1);

	int		propcode;
	Initiator	dev;
	DeviceInfo	info;

	propcode = DevicePropDesc.getPropertyCode (argv [index + 1]);
	if (propcode < 0) {
	    System.err.println ("unrecognized property name: "
		    + argv [index + 1]);
	    System.err.println ("'jphoto devinfo' lists device properties");
	    System.exit (1);
	}
	dev = startCamera ();

	// FIXME -- gets replaced with the better scheme
	// ... which should handle extension property names
	NameFactory	factory = dev.getFactory ();

	try {
	    info = dev.getDeviceInfo ();

	    if (!info.supportsProperty (propcode))
		System.err.println ("device does not support property: "
		    + factory.getPropertyName (propcode));
	    else {
		DevicePropDesc		desc;
		int			status;

		desc = new DevicePropDesc (factory);
		status = dev.getDevicePropDesc (propcode, desc);
		if (status == Response.OK)
		    desc.dump (System.out);
		else {
		    System.out.print ("... can't read ");
		    System.out.print (factory.getPropertyName (propcode));
		    System.out.print (", ");
		    System.out.println (factory.getResponseString (status));
		}
	    }
	} finally {
	    closeSession (dev);
	}
    }


    private static void maybeSaveObject (
	Initiator	dev,
	File		dir,
	int		handle,
	ObjectInfo	objinfo,
	int		depth
    ) throws IOException
    {
	String		fname = objinfo.filename.toLowerCase ();
	File		f = new File (dir, fname);

	indent (System.out, depth);
	System.out.print (f.toString ());

	if (!overwrite && f.exists ()) {
	    System.out.println (" ... EXISTS, not saved");
	
	} else {
	    // FIXME -- gets replaced with the better scheme
	    NameFactory	factory = dev.getFactory ();

	    FileOutputStream	fout = new FileOutputStream (f);
	    FileData		obj = new FileData (fout, factory);

	    try {
		dev.fillObject (handle, obj);

		System.out.print (" image saved, size ");
		System.out.println (obj.getLength ());
		obj = null;

	    } finally {
		fout.close ();
		if (obj != null && f.delete ())
		    System.out.println (" ... not saved");
	    }
	}
    }

    private static void images (
	Initiator	dev,
	File		dir,
	int		storageId,
	int		assoc,
	int		depth
    ) throws IOException
    {
	int		objs [] = dev.getObjectHandles (storageId, 0, assoc);

	for (int i = 0; i < objs.length; i++) {
	    ObjectInfo	info = dev.getObjectInfo (objs [i]);

	    // still images must have thumbnails
	    if (info.thumbFormat != 0 || info.isImage ())
		maybeSaveObject (dev, dir, objs [i], info, depth);

	    // moving images dount too
	    else if (info.isVideo ())
		maybeSaveObject (dev, dir, objs [i], info, depth);
	    
	    else switch (info.objectFormatCode) {
		case ObjectInfo.Association:
		    indent (System.out, depth);
		    info.line (System.out);
		    images (dev, dir, storageId, objs [i], depth + 4);
		    break;

		/*
		// Digital Print Order Form
		case ObjectInfo.DPOF:
		    maybeSaveObject (dev, dir, objs [i], info, depth);
		    break;
		*/

		default:
		    // indent (System.out, depth);
		    // System.out.println ("  ... IGNORED " + info.filename);
	    }
	}
    }

    private static void
    imageStore (Initiator dev, File dir, int id, boolean many)
    throws IOException
    {
	if (dev.hasStore (id)) {
	    if (many)
		System.out.println ("STORE: " + id);
	    images (dev, dir, id, ~0, 0);
	} else
	    System.out.println ("Store " + id + " is not present");
    }

    private static void images (String argv [], int index)
    throws IOException
    {
	if (index != (argv.length - 1))
	    usage (-1);

	Initiator	dev = startCamera ();
	File		dir = directory;

	if (dir == null)
	    dir = new File ("images");
	try {
	    if (!dir.exists ())
		dir.mkdir ();
	    else if (!dir.isDirectory ())
		throw new IOException ("not a directory: " + dir.toString ());

	    // FIXME:  add a way to access the "aggregate across device" mode.
	    // FIXME:  if it's supported, use the "only show image handles" mode

	    if (storageId != 0)
		imageStore (dev, dir, storageId, false);
	    else {
		int ids [] = dev.getStorageIDs ();
		for (int i = 0; i < ids.length; i++)
		    imageStore (dev, dir, ids [i], ids.length != 1);
	    }

	} finally {
	    // users may interrupt ... shutdown nicely
	    closeSession (dev);
	}
    }


    private static void put (String argv [], int index)
    throws IOException
    {
	if (index == (argv.length - 1))
	    usage (-1);

	Initiator	dev = startCamera ();
	DeviceInfo	devInfo = dev.getDeviceInfo ();
	int		folder = 0;

	if (directory != null) {
	    // for now we only support the "camera chooses location"
	    // mode; eventually, do directory lookup

	    System.err.println ("ignoring directory spec: " + directory);
	}

	// FIXME -- gets replaced with the better scheme
	NameFactory	factory = dev.getFactory ();

	try {
	    for (int i = index + 1; i < argv.length; i++) {
		try {
		    // see if the argument works as a filename
		    File	file = new File (argv [i]);
		    String	temp;

		    if (!file.exists ())
			throw new Exception ("not a file");
		    if (file.isDirectory ()) {
			System.err.println ("... can't send directory yet: "
				+ argv [i]);
			continue;
		    }

		    temp = file.getAbsolutePath ();
		    if (File.separatorChar != '/')
			temp = temp.replace (File.separatorChar, '/');
		    if (!temp.startsWith ("/"))
			temp = "/" + temp;
		    argv [i] = "file:" + temp;

		} catch (Exception e) {
		    // otherwise it's a URL or an error
		}

		System.out.print ("put ");
		System.out.print (argv [i]);
		System.out.print (" ");

		try {
		    URL			url = new URL (argv [i]);

		    try {
			URLConnection	conn = url.openConnection ();
			ObjectInfo	info;
			Data		obj;
			Response	resp;

			info = new ObjectInfo (conn, devInfo, factory);
			obj = new FileSendData (conn, factory);

			System.out.print (factory.getFormatString (
						info.objectFormatCode));
			System.out.print (", size ");
			System.out.print (info.objectCompressedSize);

			resp = dev.sendObjectInfo (info, storageId, folder);
			if (resp.getCode () == Response.OK) {
			    // dev.sendObject (obj);
			    System.out.println (" ... NOT ENABLED");
			} else {
			    System.out.println ("\n\t... ERROR, code = "
				+ resp.getCodeString ());
			}
		    } catch (IOException e) {
			System.out.println ("\n\t... IOException, "
				+ e.getMessage ());
			throw e;
		    }
		} catch (MalformedURLException e) {
		    System.out.println ("\n\t... not a file or URL:  "
		    		+ argv [i]);
		} catch (IllegalArgumentException e) {
		    System.out.println ("\n\t... can't send, "
		    		+ e.getMessage ());
		}
	    }

	} finally {
	    closeSession (dev);
	}
    }


    private static void powerdown (String argv [], int index)
    throws IOException
    {
	if (index != (argv.length - 1))
	    usage (-1);

	Initiator	dev = startCamera ();
	int 		status = -1;

	// FIXME -- gets replaced with the better scheme
	NameFactory	factory = dev.getFactory ();

	try {
	    status = dev.powerDown ();
	    System.out.print ("PowerDown --> ");
	    System.out.println (factory.getResponseString (status));
	} finally {
	    if (status != Response.OK)
		closeSession (dev);
	    // session implicitly closed on successful powerdown
	}
    }


    private static void reset (String argv [], int index)
    throws IOException
    {
	if (index != (argv.length - 1))
	    usage (-1);

	startCamera (false).reset ();
	System.out.println ("Device was reset.");
    }


    private static void selftest (String argv [], int index)
    throws IOException
    {
	if (index != (argv.length - 1))
	    usage (-1);

	Initiator	dev = startCamera ();
	int 		status;

	// FIXME -- gets replaced with the better scheme
	NameFactory	factory = dev.getFactory ();

	try {
	    status = dev.selfTest (0);

	    System.out.print ("selftest --> ");
	    System.out.println (factory.getResponseString (status));
	} finally {
	    closeSession (dev);
	}
    }


    private static void status (String argv [], int index)
    throws IOException
    {
	if (index != (argv.length - 1))
	    usage (-1);

	Initiator	dev = startCamera ();
	DeviceInfo	info;
	int		ids [];

	try {
	    info = dev.getDeviceInfo ();
	    ids = dev.getStorageIDs ();

	    info.lines (System.out);

	    System.out.print ("Operations: ");
	    System.out.println (info.operationsSupported.length);

	    System.out.print ("Modes:");
	    if (dev.isPull ())
		System.out.print (" Pull");
	    if (dev.isPush ())
		System.out.print (" Push");
	    System.out.println ();

	    // one mandatory object counting mode:  aggregate across device
	    if (info.supportsOperation (Command.GetNumObjects)) {
		try {
		    System.out.println ("Object count: "
			+ dev.getNumObjects (~0, 0, 0));
		} catch (IOException e) {
		    // maybe has a single storage unit, that's not present
		}
	    }

	    for (int i = 0; i < ids.length; i++) {
		if (!dev.hasStore (ids [i])) {
		    System.out.println ("Store " + ids [i] + " is not present");
		    continue;
		}
		StorageInfo	sinfo = dev.getStorageInfo (ids [i]);

		if (ids.length != 1) {
		    System.out.print ("Store #" + ids [i] + ": ");
		    // FIXME: how many objects?
		}
		sinfo.line (System.out);
		// FIXME: size in MBytes

	    }
	} finally {
	    closeSession (dev);
	}
    }


    private static void
    storageStore (Initiator dev, int id)
    throws IOException
    {
	System.out.println ("STORE: " + id);
	if (dev.hasStore (id))
	    dev.getStorageInfo (id).dump (System.out);
	else
	    System.out.println ("... not present");
    }

    private static void storage (String argv [], int index)
    throws IOException
    {
	if (index != (argv.length - 1))
	    usage (-1);

	Initiator	dev = startCamera ();

	try {
	    if (storageId != 0)
		storageStore (dev, storageId);
	    else {
		int ids [] = dev.getStorageIDs ();
		System.out.println ("Stores: " + ids.length);
		for (int i = 0; i < ids.length; i++)
		    storageStore (dev, ids [i]);
	    }
	} finally {
	    closeSession (dev);
	}
    }


    private static void maybeSaveThumb (
	Initiator	dev,
	File		dir,
	int		handle,
	ObjectInfo	info,
	int		depth
    ) throws IOException
    {
	String		fname = info.filename.toLowerCase ();
	File		f;

	fname = info.filename.toLowerCase ();
	if (info.thumbFormat != ObjectInfo.JFIF
		&& info.thumbFormat != ObjectInfo.TIFF_EP
		&& info.thumbFormat != ObjectInfo.EXIF_JPEG) {
	    // FIXME -- gets replaced with the better scheme
	    NameFactory	factory = dev.getFactory ();

	    throw new IOException (
		      "illegal thumbnail format "
		    + factory.getFormatString (info.thumbFormat)
		    + " for image "
		    + info.filename
		    );
	}
	f = new File (dir, fname);

	indent (System.out, depth);
	System.out.print (f.toString ());

	if (!overwrite && f.exists ()) {
	    System.out.println (" ... EXISTS, not saved");
	} else {
	    FileOutputStream	fout = new FileOutputStream (f);
	    Data		obj = null;

	    try {
		obj = dev.getThumb (handle);
		fout.write (obj.data, obj.offset, obj.length - obj.offset);

		System.out.print (" thumbnail saved, size ");
		System.out.println (obj.length - obj.offset);
	    } finally {
		fout.close ();
		if (obj == null && f.delete ())
		    System.out.println (" ... not saved");
	    }
	}
    }

    private static void thumbs (
	Initiator	dev,
	File		dir,
	int		storageId,
	int		assoc,
	int		depth
    ) throws IOException
    {
	int		objs [] = dev.getObjectHandles (storageId, 0, assoc);

	for (int i = 0; i < objs.length; i++) {
	    ObjectInfo	info = dev.getObjectInfo (objs [i]);

	    // all images have thumbnails
	    if (info.thumbFormat != 0)
		maybeSaveThumb (dev, dir, objs [i], info, depth);

	    else switch (info.objectFormatCode) {
		case ObjectInfo.Association:
		    indent (System.out, depth);
		    info.line (System.out);
		    thumbs (dev, dir, storageId, objs [i], depth + 4);
		    break;

		default:
		    // ignore ancillary data
	    }
	}
    }

    private static void
    thumbStore (Initiator dev, File dir, int id, boolean many)
    throws IOException
    {
	if (dev.hasStore (id)) {
	    if (many)
		System.out.println ("STORE: " + id);
	    thumbs (dev, dir, id, ~0, 0);
	} else
	    System.out.println ("Store " + id + " is not present");
    }

    private static void thumbs (String argv [], int index)
    throws IOException
    {
	if (index != (argv.length - 1))
	    usage (-1);

	Initiator	dev = startCamera ();
	File		dir = directory;

	if (dir == null)
	    dir = new File ("thumbs");
	try {
	    if (!dir.exists ())
		dir.mkdir ();
	    else if (!dir.isDirectory ())
		throw new IOException ("not a directory: " + dir.toString ());

	    // FIXME:  add a way to access the "aggregate across device" mode.
	    // FIXME:  if it's supported, use the "only show image handles" mode

	    if (storageId != 0)
		thumbStore (dev, dir, storageId, false);
	    else {
		int ids [] = dev.getStorageIDs ();
		for (int i = 0; i < ids.length; i++)
		    thumbStore (dev, dir, ids [i], ids.length != 1);
	    }

	} finally {
	    // users may interrupt ... shutdown nicely
	    closeSession (dev);
	}
    }

    private static void tree (
	Initiator	dev,
	int		storageId,
	int		assoc,
	int		depth
    ) throws IOException
    {
	int		objs [] = dev.getObjectHandles (storageId, 0, assoc);

	for (int i = 0; i < objs.length; i++) {
	    ObjectInfo	info = dev.getObjectInfo (objs [i]);

	    indent (System.out, depth);
	    info.line (System.out);
	    if (ObjectInfo.Association == info.objectFormatCode)
		tree (dev, storageId, objs [i], depth + 4);
	}
    }

    private static void treeStore (Initiator dev, int id, boolean many)
    throws IOException
    {
	if (dev.hasStore (id)) {
	    if (many)
		System.out.println ("STORE: " + id);
	    tree (dev, id, ~0, 0);
	} else
	    System.out.println ("Store " + id + " is not present");
    }

    private static void tree (String argv [], int index)
    throws IOException
    {
	if (index != (argv.length - 1))
	    usage (-1);

	Initiator	dev = startCamera ();

	try {
	    if (storageId != 0)
		treeStore (dev, storageId, false);
	    else {
		int ids [] = dev.getStorageIDs ();
		for (int i = 0; i < ids.length; i++)
		    treeStore (dev, ids [i], ids.length != 1);
	    }
	} finally {
	    closeSession (dev);
	}
    }
}
