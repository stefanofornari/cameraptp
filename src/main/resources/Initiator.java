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



import java.io.IOException;

import ch.ntb.usb.*;


/**
 * This supports all standardized PTP-over-USB operations, including
 * operations (and modes) that are optional for all responders.
 * Filtering operations invoked on this class may be done on the device,
 * or may be emulated on the client side.
 * At this time, not all standardized operations are supported.
 *
 * @version $Id: Initiator.java,v 1.9 2001/04/12 23:13:00 dbrownell Exp $
 * @author David Brownell
 */
public class Initiator extends BaselineInitiator
{
    /**
     * This is essentially a class driver, following Annex D of
     * the PTP specification.
     */
    public Initiator (UsbDevice dev)
    throws IOException
    {
	this (getPtpInterface (dev));
    }


    /**
     * This is essentially a class driver, following Annex D of
     * the PTP specification.
     */
    public Initiator (UsbInterface intf)
    throws IOException
    {
	super (intf);
    }


    public int copyObject (int handle, int storageId, int parent)
    throws IOException
    {
	return transact3 (Command.CopyObject, null, handle, storageId, parent)
		    .getCode ();
    }

    // deleteAll () ... ~0, 0
    // deleteAllByType (int formats) ... ~0, formats

    // deleteObject (int handle) ... handle, 0 ... object or assoc/hierarchy

    public int deleteObject (int handle, int formats)
    throws IOException
    {
	return transact2 (Command.DeleteObject, null, handle, formats)
		    .getCode ();
    }


    /**
     * Reformats the specified storage unit.  All images (and other objects)
     * on the media will be erased; write-protected objects will be erased,
     * unlike invoking {@link #deleteObject}.
     *
     * @param storageId the storage unit to reformat
     * @param formatCode normally zero.  Otherwise, a value as defined
     *	in table 12 of the PTP specification, selecting among types of
     *	filesystem supported by the device (flat, hierarchical, DCF,
     *	vendor-defined, and so on).
     * @return response code
     */
    public int formatStore (int storageId, int formatCode)
    throws IOException
    {
	return transact2 (Command.FormatStore, null, storageId, formatCode)
		    .getCode ();
    }


    /**
     * Fills out the provided device property description.
     *
     * @param propcode code identifying the property of interest
     * @param desc description to be filled; it may be a subtype
     *	associated with with domain-specific methods
     * @return response code
     */
    public int getDevicePropDesc (int propcode, DevicePropDesc desc)
    throws IOException
    {
	return transact1 (Command.GetDevicePropDesc, desc, propcode)
			.getCode ();
    }


    /**
     * Gets the value of the specified device property.
     * The same value is also seen through getDevicePropDesc.
     *
     * @param propcode code identifying the property of interest
     * @param value the value to be filled
     * @return response code
     */
    public int getDevicePropValue (int propcode, DevicePropValue value)
    throws IOException
    {
	Response	response;
	int		code;

	response = transact1 (Command.GetDevicePropValue, value, propcode);
	return response.getCode ();
    }


    /**
     * Returns an array of image handles in the store (or stores)
     * specified; uses device-side filtering when it's available.
     * Device-side filtering can substantially reduce USB traffic.
     *
     * @param storageID either ~0 to indicate all media, or the
     *	ID for some particular store.
     * @param association either zero, or ~0 to specify the root
     *	of a store; or else an association handle.
     */
    public int [] getImageHandles (int storageID, int association)
    throws IOException
    {
	Data		data = new Data (factory);
	Response	response = transact3 (Command.GetObjectHandles, data,
					storageID, ~0, association);

	// Some devices may support this filtering directly.
	switch (response.getCode ()) {
	    case Response.OK:
		return data.nextS32Array ();
	    case Response.SpecificationByFormatUnsupported:
		break;
	    default:
		throw new IOException (response.toString ());
	}

	// FIXME:  client/server filter xparency for all format codes.

	// For those that don't, do it ourselves.
	int		handles [];
	int		retval [];
	int		count = 0;

	handles = getObjectHandles (storageID, 0, association);
	for (int i = 0; i < handles.length; i++) {
	    ObjectInfo	info = getObjectInfo (handles [i]);

	    if (!info.isImage ())
		handles [i] = 0;
	    else
		count++;
	}

	retval = new int [count];
	count = 0;
	for (int i = 0; i < handles.length; i++) {
	    if (handles [i] != 0)
		retval [count++] = handles [i];
	}
	return retval;
    }


    /**
     * Returns the number of images in the specified association on
     * the specified store; uses device-side filtering when it's available.
     *
     * @param storageID either ~0 to indicate all media, or the
     *	ID for some particular store.
     * @param association 0, or the object handle for some association
     */
    public int getNumImages (int storageID, int association)
    throws IOException
    {
	Data		data = new Data (factory);
	Response	response = transact3 (Command.GetNumObjects, data,
					storageID, ~0, association);

	switch (response.getCode ()) {
	    case Response.OK:
		return response.getParam1 ();
	    case Response.SpecificationByFormatUnsupported:
		break;
	    default:
		throw new IOException (response.toString ());
	}

	return getImageHandles (storageID, association).length;
    }


    public Response getPartialObject (int handle, int offset, int count)
    throws IOException
    {
	return transact3 (Command.GetPartialObject, null,
		    handle, offset, count);
    }


    /**
     * Starts the capture of one (or more) new
     * data objects, according to current device properties.
     * The capture will complete without issuing further commands.
     *
     * @see #initiateOpenCapture
     *
     * @param storageId Where to store the object(s), or zero to
     *	let the device choose.
     * @param formatCode Type of object(s) to capture, or zero to
     *	use the device default.
     *
     * @return status code indicating whether capture started;
     *	CaptureComplete events provide capture status, and
     *	ObjectAdded events provide per-object status.
     */
    public int initiateCapture (int storageId, int formatCode)
    throws IOException
    {
	return transact2 (Command.InitiateCapture, null, storageId, formatCode)
			.getCode ();
    }


    /**
     * Starts an open-ended capture of new data objects, according to
     * current device properties.  Capturing
     * will continue until {@link #terminateOpenCapture} is invoked.
     * Intended applications include "manually" controlled exposures,
     * and capture of time-based data such as audio or video.
     *
     * @see #initiateCapture
     *
     * @param storageId Where to store the object(s), or zero to
     *	let the device choose.
     * @param formatCode Type of object(s) to capture, or zero to
     *	use the device default.
     *
     * @return status code indicating whether capture started;
     *	CaptureComplete events provide capture status, and
     *	ObjectAdded events provide per-object status.
     */
    public int initiateOpenCapture (int storageId, int formatCode)
    throws IOException
    {
	return transact2 (Command.InitiateOpenCapture, null,
				storageId, formatCode)
			.getCode ();
    }


    public int moveObject (int handle, int storageId, int parent)
    throws IOException
    {
	return transact3 (Command.MoveObject, null, handle, storageId, parent)
		    .getCode ();
    }


    /**
     * Sends a PowerDown command to the device, causing it to power
     * down and close all currently open sessions.
     *
     * @return response code
     */
    public int powerDown ()
    throws IOException
    {
	Response	response = transact0 (Command.PowerDown, null);
	int		code;

	code = response.getCode ();
	if (code == Response.OK) {
	    // FIXME:  mark session as closed
	}
	return code;
    }
  

    /**
     * Sends a ResetDevice command to the device, putting it into a
     * default state and closing all open sessions.
     */
    public int resetDevice ()
    throws IOException
    {
	Response	response = transact0 (Command.ResetDevice, null);
	int		code;

// FIXME:  how is this different from class-specific reset() ??
// This issue should get addressed in v1.1 of the PTP spec.  Ideally,
// when PTP runs over USB resetDevice is mapped to the class request.

	code = response.getCode ();
	if (code == Response.OK) {
	    // FIXME:  mark session as closed
	}
	return code;
    }


    /**
     * Resets the value of the specified device property to
     * the factory default.
     *
     * @param propcode code identifying the property of interest
     * @return response code
     */
    public int resetDevicePropValue (int propcode)
    throws IOException
    {
	return transact1 (Command.SetDevicePropValue, null, propcode)
			.getCode ();
    }


    /**
     * Sends a SelfTest command to the device.
     *
     * @param type typically zero, or a vendor-defined code
     * @return response code
     */
    public int selfTest (int type)
    throws IOException
    {
	return transact1 (Command.SelfTest, null, type)
			.getCode ();
    }


    /**
     * Sets the value of the specified device property.
     * The same value is also seen through getDevicePropDesc.
     *
     * @param propcode code identifying the property of interest
     * @param value the value to be assigned
     * @return response code
     */
    public int setDevicePropValue (int propcode, DevicePropValue value)
    throws IOException
    {
	return transact1 (Command.SetDevicePropValue, value, propcode)
			.getCode ();
    }


    public int setObjectProtection (int handle, int status)
    throws IOException
    {
	return transact2 (Command.SetObjectProtection, null, handle, status)
		    .getCode ();
    }


    /**
     * Closes the current session's open capture operation.
     * @see #initiateOpenCapture
     */
    public int terminateOpenCapture ()
    throws IOException
    {
	return transact1 (Command.TerminateOpenCapture, null, getSessionId ())
		    .getCode ();
    }
}
