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

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import ch.ntb.usb.*;

/**
 * This initiates interactions with USB devices, supporting only
 * mandatory PTP-over-USB operations; both
 * "push" and "pull" modes are supported.  Note that there are some
 * operations that are mandatory for "push" responders and not "pull"
 * ones, and vice versa.  A subclass adds additional standardized
 * operations, which some PTP devices won't support.  All low
 * level interactions with the device are done by this class,
 * including especially error recovery.
 *
 * <p> The basic sequence of operations for any PTP or ISO 15470
 * initiator (client) is:  acquire the device; wrap it with this
 * driver class (or a subclass); issue operations;
 * close device.  PTP has the notion
 * of a (single) session with the device, and until you have an open
 * session you may only invoke {@link #getDeviceInfo} and
 * {@link #openSession} operations.  Moreover, devices may be used
 * both for reading images (as from a camera) and writing them
 * (as to a digital picture frame), depending on mode support.
 *
 * <p> Note that many of the IOExceptions thrown here are actually
 * going to be <code>usb.core.USBException</code> values.  That may
 * help your application level recovery processing.  You should
 * assume that when any IOException is thrown, your current session
 * has been terminated.
 *
 * @see Initiator
 *
 * @version $Id: BaselineInitiator.java,v 1.17 2001/05/30 19:33:43 dbrownell Exp $
 * @author David Brownell
 */
public class BaselineInitiator extends NameFactory implements Runnable {
    // package private

    final static boolean DEBUG = false;
    final static boolean TRACE = false;
    
    private UsbDevice             dev;
    private UsbInterface          intf;
    private UsbEndpointDescriptor in;
    private int                   inMaxPS;
    private UsbEndpointDescriptor out;
    private UsbEndpointDescriptor intr;
    private Session               session = new Session();
    private DeviceInfo            info;

    // package private
    NameFactory factory;

    /**
     * This function returns a PTP interface for a device,
     * or null if the device has none.
     */
    public static UsbInterface getPtpInterface(UsbDevice dev) {
        UsbInterface intf = null;
        UsbDeviceDescriptor desc;

        if (dev == null) {
            return null;
        }
        desc = dev.getDescriptor();

        // from Still Image Capture Device class spec (usb-if)
        if (desc.getDeviceClass() != 0
                || desc.getDeviceSubClass() != 0
                || desc.getDeviceProtocol() != 0) {
            return null;
        }

        // some cameras predate final USB-IF code assignments
        if (desc.getVendorId() == 0x040a) {		// Kodak
            // DC-4800 (0x0160) or a DC-240 with
            // firmware prototype (0x0121)
            if (desc.getProductId() == 0x0160
                    || desc.getProductId() == 0x0121) {
                return dev.getConfig()[0].getInterface(0);
            }
        }

        for (UsbConfigDescriptor config : dev.getConfig()) {
            // class/subclass/protocol from SICD Class spec 1.0
            for (int i = 0;
                    intf == null && i < config.getNumInterfaces();
                    i++) {
                intf = config.getInterfaceByAlternateSetting(0);
                if (intf == null) {
                    continue;
                }

                if (!intf.belongsToClass((byte) 0x06)
                        || !intf.belongsToSubClass((byte) 0x01)
                        || !intf.supportsProtocol((byte) 0x01)) {
                    intf = null;
                    continue;
                }
                // stops at the first one
            }
        }

        return intf;
    }

    /**
     * Constructs a class driver object, if the device supports
     * operations according to Annex D of the PTP specification.
     *
     * @param dev the first PTP interface will be used
     * @exception IllegalArgumentException if the device has no
     *	Digital Still Imaging Class or PTP interfaces
     */
    public BaselineInitiator(UsbDevice dev)
            throws IOException {
        this(getPtpInterface(dev));
    }

    /**
     * Constructs a class driver from an interface asserted to
     * conform fully to Annex D of the PTP specification.
     *
     * @param intf_arg the PTP interface to be used
     * @exception IllegalArgumentException if the interface is
     *	null or doesn't provide the necessary endpoints.
     */
    public BaselineInitiator(UsbInterface intf_arg)
            throws IOException {
        if (intf_arg == null) {
            throw new IllegalArgumentException();
        }
        intf = intf_arg;

        ArrayList<UsbEndpointDescriptor> endpoints = intf.getAllEndpoints();
        for (UsbEndpointDescriptor ep: endpoints) {
            if (ep.isTypeBulk()) {
                if (ep.isInput()) {
                    inMaxPS = ep.getMaxPacketSize();
                    in = ep;
                } else {
                    out = ep;
                }
            } else if (ep.isTypeInterrupt() && ep.isInput()) {
                intr = ep;
            }
        }
        if (in == null || out == null || intr == null) {
            throw new IllegalArgumentException();
        }

        // we want exclusive access to this interface.
        this.dev = USB.getDevice(intf.);
        intf.claim();

        // clear out any previous state
        reset();
        if (getClearStatus() != Response.OK
                && getDeviceStatus(null) != Response.OK) {
            throw new IOException("can't init");
        }

        // get info to sanity check later requests
        factory = this;
        info = getDeviceInfoUncached();

        // set up to use vendor extensions, if any
        if (info.vendorExtensionId != 0) {
            factory = factory.updateFactory(info.vendorExtensionId);
            info.factory = factory;
        }
        session.setFactory(factory);
    }

    /**
     * Releases USB resources so that some other program can access
     * the interface this class previously interacted with.  You may
     * wish to separately close the device.
     */
    public void close()
            throws IOException {
        if (intf != null) {
            try {
                intf.release();
            } finally {
                intf = null;
                in = null;
                out = null;
                intr = null;
                dev = null;
                session = null;
                info = null;
            }
        }
    }

    /**
     * Tries to clean up if you forget to call close.
     */
    protected void finalize() {
        try {
            close();
        } catch (Exception e) {
        }
    }

    /**
     * Returns a port identifier string for this device; use these to
     * address devices, they correspond to the hub port to which
     * a device is attached.  Bus addresses change dynamically,
     * so applications shouldn't use them.
     */
    public String getPortIdentifier() {
        return dev.getPortIdentifier().toString();
    }

    ///////////////////////////////////////////////////////////////////
    /**
     * Returns true iff the associated responder supports "pull mode",
     * reading objects from the device.  Example "pull mode" devices
     * include digital cameras and other image capture devices, as well
     * as image archives updated in "push mode".
     */
    public boolean isPull() {
        return info.supportsOperation(Command.GetObject);
    }

    /**
     * Returns true iff the associated responder supports "push mode",
     * sending objects to the device.  Example "push mode" devices
     * include printers, picture frames, web servers (though not with
     * USB!), and other types of image archives.
     */
    public boolean isPush() {
        return info.supportsOperation(Command.SendObject);
    }

    /**
     * Returns the last cached copy of the device info, or returns
     * a newly cached copy.
     * @see #getDeviceInfoUncached
     */
    public DeviceInfo getDeviceInfo()
            throws IOException {
        if (info == null) {
            return getDeviceInfoUncached();
        }
        return info;
    }

    /**
     * Returns the ID of the PTP sesssion in use; initiator only keep
     * one open session at a time, or zero if there's no session.
     */
    public int getSessionId() {
        if (session.isActive()) {
            return session.getSessionId();
        } else {
            return 0;
        }
    }

    // package private, and deprecated (will be replaced)
    NameFactory getFactory() {
        return factory;
    }
    ///////////////////////////////////////////////////////////////////
    // USB Class-specific control requests; from Annex D.5.2
    private static final byte CLASS_CANCEL_REQ = (byte) 0x64;
    private static final byte CLASS_GET_EVENT_DATA = (byte) 0x65;
    private static final byte CLASS_DEVICE_RESET = (byte) 0x66;
    private static final byte CLASS_GET_DEVICE_STATUS = (byte) 0x67;

    // stalls endpoint used with current transfer
    void cancel(int xid)
            throws IOException {
        Buffer buf = new Buffer(new byte[6]);
        ControlMessage msg = new ControlMessage();

        buf.put16(0x4001);	// "cancel"
        buf.put32(xid);
        msg.setRequestType((byte) (msg.DIR_TO_DEVICE
                | msg.TYPE_CLASS
                | msg.RECIPIENT_INTERFACE));
        msg.setRequest(CLASS_CANCEL_REQ);
        msg.setBuffer(buf.data);
        if (TRACE) {
            System.err.println("cancel");
        }
        dev.control(msg);
    }

    // potentially used with some vendors' extension events
    void getExtendedEventData(Buffer buf)
            throws IOException {
        ControlMessage msg = new ControlMessage();

        msg.setRequestType((byte) (msg.DIR_TO_HOST
                | msg.TYPE_CLASS
                | msg.RECIPIENT_INTERFACE));
        msg.setRequest(CLASS_GET_EVENT_DATA);
        msg.setLength(buf.data.length);

        if (TRACE) {
            System.err.println("getExtendedEventData");
        }
        dev.control(msg);

        buf.data = msg.getBuffer();
        buf.offset = 0;
        buf.length = buf.data.length;
    }

    /**
     * Sends a USB level CLASS_DEVICE_RESET control message.
     * All PTP-over-USB devices support this operation.
     * This is documented to clear stalls and camera-specific suspends,
     * flush buffers, and close the current session.
     *
     * <p> <em>TO BE DETERMINED:</em> How does this differ from a bulk
     * protocol {@link Initiator#resetDevice ResetDevice} command?  That
     * command is documented as very similar to this class operation.
     * Ideally, only this control request will ever be used, since it
     * works even when the bulk channels are halted.
     */
    public void reset()
            throws IOException {
        ControlMessage msg = new ControlMessage();

        msg.setRequestType((byte) (msg.DIR_TO_DEVICE
                | msg.TYPE_CLASS
                | msg.RECIPIENT_INTERFACE));
        msg.setRequest(CLASS_DEVICE_RESET);
        msg.setBuffer(new byte[0]);

        if (TRACE) {
            System.err.println("reset " + getPortIdentifier());
        }
        dev.control(msg);

        session.close();
    }

    // returns Response.OK, Response.DeviceBusy, etc
    // per fig D.6, response may hold stalled endpoint numbers
    int getDeviceStatus(Buffer buf)
            throws IOException {
        ControlMessage msg = new ControlMessage();

        msg.setRequestType((byte) (msg.DIR_TO_HOST
                | msg.TYPE_CLASS
                | msg.RECIPIENT_INTERFACE));
        msg.setRequest(CLASS_GET_DEVICE_STATUS);
        msg.setLength(33);	// force short reads

        if (TRACE) {
            System.err.println("devstatus");
        }
        dev.control(msg);

        if (buf == null) {
            buf = new Buffer(msg.getBuffer());
        } else {
            buf.data = msg.getBuffer();
        }
        buf.offset = 4;
        buf.length = buf.getU16(0);
        if (buf.length != buf.data.length) {
            throw new RuntimeException();
        }

        return buf.getU16(2);
    }

    // like getDeviceStatus(),
    // but clears stalled endpoints before returning
    // (except when exceptions are thrown)
    // returns -1 iff device wouldn't return OK status
    int getClearStatus()
            throws IOException {
        Buffer buf = new Buffer(null, 0);
        int retval = getDeviceStatus(buf);

        // any halted endpoints to clear?  (always both)
        if (buf.length != 4) {
            while ((buf.offset + 4) <= buf.length) {
                int ep = buf.nextS32();

                if (in.getEndpoint() == ep) {
                    if (TRACE) {
                        System.err.println("clearHalt in");
                    }
                    in.clearHalt();
                } else if (out.getEndpoint() == ep) {
                    if (TRACE) {
                        System.err.println("clearHalt out");
                    }
                    out.clearHalt();
                } else {
                    if (DEBUG || TRACE) {
                        System.err.println("?? halted EP: " + ep);
                    }
                }
            }

            // device must say it's ready
            int status = Response.Undefined;

            for (int i = 0; i < 10; i++) {
                try {
                    status = getDeviceStatus(null);
                } catch (USBException x) {
                    if (DEBUG) {
                        x.printStackTrace();
                    }
                }
                if (status == Response.OK) {
                    break;
                }
                if (TRACE) {
                    System.err.println("sleep; status = "
                            + factory.getResponseString(status));
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException x) {
                }
            }
            if (status != Response.OK) {
                retval = -1;
            }
        } else {
            if (TRACE) {
                System.err.println("no endpoints halted");
            }
        }
        return retval;
    }

    ///////////////////////////////////////////////////////////////////
    // mandatory for all responders:  generating events
    /**
     * Makes the invoking Thread read and report events reported
     * by the PTP responder, until the Initiator is closed.
     */
    public void run() {
        try {

            System.err.println("START event thread");
            for (;;) {
                try {
                    byte buf[];
                    Event event;

                    // FIXME:  this seemed to stop other threads ...
                    // likely usbdevfs can't handle concurrent I/O
                    // requests on its file descriptors!

                    event = new Event(intr.recvInterrupt(), factory);
                    if (TRACE) {
                        System.err.println("EVENT: " + event.toString());
                    }

                } catch (USBException e) {
                    // if it's a timeout, fine ...

                    if (DEBUG) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }
        System.err.println("EXIT event thread");
    }

    // add event listener
    // rm event listener
    ///////////////////////////////////////////////////////////////////
    // mandatory for all responders
    /**
     * Issues a GetDeviceInfo command to the device; may be used
     * with all responders.  This is the only generic PTP command
     * that may be issued both inside or outside of a session.
     */
    public DeviceInfo getDeviceInfoUncached()
            throws IOException {
        DeviceInfo data = new DeviceInfo(factory);
        Response response;

        synchronized (session) {
            Command command;
            command = new Command(Command.GetDeviceInfo, session);
            response = transactUnsync(command, data);
        }

        switch (response.getCode()) {
            case Response.OK:
                info = data;
                return data;
            default:
                throw new IOException(response.toString());
        }
    }

    /**
     * Issues an OpenSession command to the device; may be used
     * with all responders.  PTP-over-USB doesn't seem to support
     * multisession operations; you must close a session before
     * opening a new one.
     */
    public void openSession()
            throws IOException {
        Command command;
        Response response;

        synchronized (session) {
            command = new Command(Command.OpenSession, session,
                    session.getNextSessionID());
            response = transactUnsync(command, null);
            switch (response.getCode()) {
                case Response.OK:
                    session.open();
                    return;
                default:
                    throw new IOException(response.toString());
            }
        }
    }

    /**
     * Issues a CloseSession command to the device; may be used
     * with all responders.
     */
    public void closeSession()
            throws IOException {
        Response response;

        synchronized (session) {
            // checks for session already open
            response = transact0(Command.CloseSession, null);
            switch (response.getCode()) {
                case Response.SessionNotOpen:
                    if (DEBUG) {
                        System.err.println("close unopen session?");
                    }
                // FALLTHROUGH
                case Response.OK:
                    session.close();
                    return;
                default:
                    throw new IOException(response.toString());
            }
        }
    }

    /**
     * Returns an array of storage ids for the device; may be used with
     * all responders.
     */
    public int[] getStorageIDs()
            throws IOException {
        Data data = new Data(factory);
        Response response = transact0(Command.GetStorageIDs, data);

        switch (response.getCode()) {
            case Response.OK:
                return data.nextS32Array();
            default:
                throw new IOException(response.toString());
        }
    }

    /**
     * Returns true iff the store is present.  For example, returns false
     * for a memory card that's removed, and reports an exception for
     * invalid store IDs.
     *
     * @param store either ~0 to indicate all media, or the
     *	ID for some particular store.
     */
    public boolean hasStore(int store)
            throws IOException {
        StorageInfo data = new StorageInfo(factory);
        Response response = transact1(Command.GetStorageInfo, data,
                store);

        switch (response.getCode()) {
            case Response.OK:
                return true;
            case Response.StoreNotAvailable:
                return false;
            default:
                throw new IOException(response.toString());
        }
    }

    /**
     * Issues a GetStorageInfo command to the device; may be used
     * with all responders.
     *
     * @param store either ~0 to indicate all media, or the
     *	ID for some particular store.
     */
    public StorageInfo getStorageInfo(int store)
            throws IOException {
        StorageInfo data = new StorageInfo(factory);
        Response response = transact1(Command.GetStorageInfo, data,
                store);

        switch (response.getCode()) {
            case Response.OK:
                return data;
            default:
                throw new IOException(response.toString());
        }
    }

    ///////////////////////////////////////////////////////////////////
    // mandatory for pull-mode responders
    /**
     * Returns the number of objects in the specified association on
     * the specified store.
     * Push-only responders don't need to support this operation.
     *
     * @param storageID either ~0 to indicate all media, or the
     *	ID for some particular store.
     * @param typeFilter either ~0 to indicate all image objects, zero
     *	to indicate all objects, or some specific object format code.
     *	Not all responders support such filtering.
     * @param association zero, or the object handle for some association
     */
    public int getNumObjects(int storageID, int typeFilter, int association)
            throws IOException {
        Response response;

        response = transact3(Command.GetNumObjects, null,
                storageID, typeFilter, association);

        switch (response.getCode()) {
            case Response.OK:
                return response.getParam1();
            default:
                throw new IOException(response.toString());
        }
    }

    /**
     * Returns an array of object handles in the store (or stores)
     * specified.
     * Push-only responders don't need to support this operation.
     *
     * @param store identifies the storage ID; or ~0 to identify
     *	all accessible storage.
     * @param code either zero, or ~0 to request only images be
     *	returned, or else some format code to be used to filter
     *	the handles before they are returned.
     * @param association either zero, or ~0 to specify the root
     *	of a store; or else an association handle.
     */
    public int[] getObjectHandles(int store, int code, int association)
            throws IOException {
        Data data = new Data(factory);
        Response response = transact3(Command.GetObjectHandles, data,
                store, code, association);

        switch (response.getCode()) {
            case Response.OK:
                return data.nextS32Array();
            default:
                throw new IOException(response.toString());
        }
    }

    /**
     * Returns information about the object identified by a given handle.
     * Push-only responders don't need to support this operation.
     *
     * @param handle object handle from the current session
     */
    public ObjectInfo getObjectInfo(int handle)
            throws IOException {
        ObjectInfo data = new ObjectInfo(handle, factory);
        Response response;

        response = transact1(Command.GetObjectInfo, data, handle);
        switch (response.getCode()) {
            case Response.OK:
                return data;
            default:
                throw new IOException(response.toString());
        }
    }

    /**
     * Returns the data for the object identified by a given handle.
     * Push-only responders don't need to support this operation.
     *
     * @param handle object handle from the current session
     *
     * @see #fillObject
     */
    public Data getObject(int handle)
            throws IOException {
        Data data = new Data(factory);

        fillObject(handle, data);
        return data;
    }

    /**
     * Fills the data object using the object identified by a given handle.
     * Push-only responders don't need to support this operation.
     *
     * <p> This differs from getObject in that the data object is supplied
     * by the caller.  Such objects may specialized behaviors, including in
     * particular reducing in-memory buffering for large objects.
     *
     * @param handle object handle from the current session
     */
    public void fillObject(int handle, Data data)
            throws IOException {
        Response response = transact1(Command.GetObject, data, handle);

        switch (response.getCode()) {
            case Response.OK:
                return;
            default:
                throw new IOException(response.toString());
        }
    }

    /**
     * Returns the thumbnail for the image identified by a given handle.
     * Push-only responders don't need to support this operation.
     *
     * @param handle object handle from the current session
     */
    public Data getThumb(int handle)
            throws IOException {
        Data data = new Data(factory);
        Response response = transact1(Command.GetThumb, data, handle);

        switch (response.getCode()) {
            case Response.OK:
                return data;
            default:
                throw new IOException(response.toString());
        }
    }

    ///////////////////////////////////////////////////////////////////
    // mandatory for push-mode responders
    /**
     * Sends object metadata, in preparation for sending the object
     * described by this data.
     * Pull-only responders don't need to support this operation.
     *
     * @param info information about the object; fields in this
     *	dataset are interpreted with respect to the initiator
     *	(for example, initiator's storage id)
     * @param storage desired responder storage id for the object;
     *	or zero indicating the responder should choose one
     * @param parent responder object handle for object's parent;
     *	~0 indicating the root of that object store;
     *	or zero indicating the responder should choose.
     *
     * @return response must be examined by caller; getCode() indicates
     *	command status, and if it's "Response.OK" then the positional
     *	parameters hold data used when transferring hierarchies.
     *
     * @see #sendObject
     */
    public Response sendObjectInfo(ObjectInfo info, int storage, int parent)
            throws IOException {
        return transact2(Command.SendObjectInfo, info, storage, parent);
    }

    /**
     * Sends the object identified in a successful preceding
     * sendObjectInfo call.
     * Pull-only responders don't need to support this operation.
     *
     * @see #sendObjectInfo
     */
    public void sendObject(Data obj)
            throws IOException {
        Response response = transact0(Command.SendObject, obj);

        switch (response.getCode()) {
            case Response.OK:
                return;
            default:
                throw new IOException(response.toString());
        }
    }

    ///////////////////////////////////////////////////////////////////
    /**
     * Performs a PTP transaction, passing zero command parameters.
     * @param code the command code
     * @param data data to be sent or received; or null
     * @return response; check for code Response.OK before using
     *	any positional response parameters
     */
    protected Response transact0(int code, Data data)
            throws IOException {
        synchronized (session) {
            Command command = new Command(code, session);
            return transactUnsync(command, data);
        }
    }

    /**
     * Performs a PTP transaction, passing one command parameter.
     * @param code the command code
     * @param data data to be sent or received; or null
     * @param p1 the first positional parameter
     * @return response; check for code Response.OK before using
     *	any positional response parameters
     */
    protected Response transact1(int code, Data data, int p1)
            throws IOException {
        synchronized (session) {
            Command command = new Command(code, session, p1);
            return transactUnsync(command, data);
        }
    }

    /**
     * Performs a PTP transaction, passing two command parameters.
     * @param code the command code
     * @param data data to be sent or received; or null
     * @param p1 the first positional parameter
     * @param p2 the second positional parameter
     * @return response; check for code Response.OK before using
     *	any positional response parameters
     */
    protected Response transact2(int code, Data data, int p1, int p2)
            throws IOException {
        synchronized (session) {
            Command command = new Command(code, session, p1, p2);
            return transactUnsync(command, data);
        }
    }

    /**
     * Performs a PTP transaction, passing three command parameters.
     * @param code the command code
     * @param data data to be sent or received; or null
     * @param p1 the first positional parameter
     * @param p2 the second positional parameter
     * @param p3 the third positional parameter
     * @return response; check for code Response.OK before using
     *	any positional response parameters
     */
    protected Response transact3(int code, Data data, int p1, int p2, int p3)
            throws IOException {
        synchronized (session) {
            Command command = new Command(code, session, p1, p2, p3);
            return transactUnsync(command, data);
        }
    }

    /**
     * Performs a PTP transaction.  This is normally used only to ensure
     * that vendor-specific command names show up nicely in diagnostics,
     * by providing a subclassed Command (and likely Data as well).  
     *
     * @param command the command block
     * @param data data to be sent or received; or null
     * @return response; check for code Response.OK before using
     *	any positional response parameters
     */
    protected Response transact(Command command, Data data)
            throws IOException {
        synchronized (session) {
            return transactUnsync(command, data);
        }
    }

    ///////////////////////////////////////////////////////////////////
    // INVARIANTS:
    // - caller is synchronized on session
    // - on return, device is always in idle/"command ready" state
    // - on return, session was only closed by CloseSession
    // - on IOException, device (and session!) has been reset
    private Response transactUnsync(Command command, Data data)
            throws IOException {
        if (command.getBlockTypeName(command.getBlockType()) != "command") {
            throw new IllegalArgumentException(command.toString());
        }

        // sanity checking
        int opcode = command.getCode();

        if (session.isActive()) {
            if (Command.OpenSession == opcode) {
                throw new IllegalStateException("session already open");
            }
        } else {
            if (Command.GetDeviceInfo != opcode
                    && Command.OpenSession != opcode) {
                throw new IllegalStateException("no session");
            }
        }

        // this would be UnsupportedOperationException ...
        // except that it's not available on jdk 1.1
        if (info != null && !info.supportsOperation(opcode)) {
            throw new UnsupportedOpException(command.getCodeName(opcode));
        }

        // ok, then we'll really talk to the device
        Response response;
        boolean abort = true;

        try {
            OutputStream stream = out.getOutputStream();

            // issue command
            // rejected commands will stall both EPs
            if (TRACE) {
                System.err.println(command.toString());
            }
            stream.write(command.data, 0, command.length);

            // may need to terminate request with zero length packet
            if ((command.length % out.getMaxPacketSize()) == 0) {
                stream.write(command.data, 0, 0);
            }

            // data exchanged?
            // errors or cancel (another thread) will stall both EPs
            if (data != null) {

                // write data?
                if (!data.isIn()) {
                    data.offset = 0;
                    data.putHeader(data.getLength(), 2 /*Data*/, opcode,
                            command.getXID());

                    if (TRACE) {
                        System.err.println(data.toString());
                    }

                    // Special handling for the read-from-N-mbytes-file case
                    if (data instanceof FileSendData) {
                        FileSendData fd = (FileSendData) data;
                        int len = fd.data.length - fd.offset;
                        int temp;

                        // fill up the rest of the first buffer
                        len = fd.read(fd.data, fd.offset, len);
                        if (len < 0) {
                            throw new IOException("eh? " + len);
                        }
                        len += fd.offset;

                        for (;;) {
                            // write data or terminating packet
                            stream.write(fd.data, 0, len);
                            if (len != fd.data.length) {
                                break;
                            }

                            len = fd.read(fd.data, 0, fd.data.length);
                            if (len < 0) {
                                throw new IOException("short: " + len);
                            }
                        }

                    } else {
                        // write data and maybe terminating packet
                        stream.write(data.data, 0, data.length);
                        if ((data.length % out.getMaxPacketSize()) == 0) {
                            stream.write(data.data, 0, 0);
                        }
                    }

                    // read data?
                } else {
                    byte buf1[] = new byte[inMaxPS];
                    int len = in.getInputStream().read(buf1);

                    // Get the first bulk packet(s), check header for length
                    data.data = buf1;
                    data.length = len;
                    if (TRACE) {
                        System.err.println(data.toString());
                    }
                    if (data.getBlockTypeName(data.getBlockType()) != "data"
                            || data.getCode() != command.getCode()
                            || data.getXID() != command.getXID()) {
                        throw new IOException("protocol err 1, " + data);
                    }

                    // get the rest of it
                    int expected = data.getLength();

                    // Special handling for the write-to-N-mbytes-file case
                    if (data instanceof FileData) {
                        FileData fd = (FileData) data;

                        fd.write(buf1, Data.HDR_LEN, len - Data.HDR_LEN);
                        if (len == inMaxPS && expected != inMaxPS) {
                            InputStream is = in.getInputStream();

                            // at max usb data rate, 128K ~= 0.11 seconds
                            // typically it's more time than that
                            buf1 = new byte[128 * 1024];
                            do {
                                len = is.read(buf1);
                                fd.write(buf1, 0, len);
                            } while (len == buf1.length);
                        }

                    } else if (len == inMaxPS && expected != inMaxPS) {
                        buf1 = new byte[expected];
                        System.arraycopy(data.data, 0, buf1, 0, len);
                        data.data = buf1;
                        data.length += in.getInputStream().read(buf1, len, expected - len);
                    }

                    // if ((expected % inMaxPS) == 0)
                    //	... next packet will be zero length

                    // and do whatever parsing needs to be done
                    data.parse();
                }
            }

            // (short) read the response
            // this won't stall anything
            byte buf[] = new byte[Response.MAX_LEN];
            int len = in.getInputStream().read(buf);

            if (len == 0) // ZLP terminated previous data?
            {
                len = in.getInputStream().read(buf);
            }

            response = new Response(buf, len, factory);
            if (TRACE) {
                System.err.println(response.toString());
            }

            abort = false;
            return response;

        } catch (USBException e) {
            if (DEBUG) {
                e.printStackTrace();
            }

            // PTP devices will stall bulk EPs on error ... recover.
            if (e.isStalled()) {
                int status = -1;

                try {
                    // NOTE:  this is the request's response code!  It can't
                    // be gotten otherwise; despite current specs, this is a
                    // "control-and-bulk" protocol, NOT "bulk-only"; or more
                    // structurally, the protocol handles certain operations
                    // concurrently.
                    status = getClearStatus();
                } catch (USBException x) {
                    if (DEBUG) {
                        x.printStackTrace();
                    }
                }

                // something's very broken
                if (status == Response.OK || status == -1) {
                    throw e;
                }

                // treat status code as the device's response
                response = new Response(new byte[Response.HDR_LEN],
                        factory);
                response.putHeader(Response.HDR_LEN, 3 /*response*/,
                        status, command.getXID());
                if (TRACE) {
                    System.err.println("STALLED: " + response.toString());
                }

                abort = false;
                return response;
            }
            throw e;

        } finally {
            if (abort) {
                // not an error we know how to recover;
                // bye bye session!
                try {
                    reset();
                } catch (IOException e) {
                    if (DEBUG) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * This is just like
     * <em>java&#046;lang&#046;UnsupportedOperationException</em>
     * except that it can be used on JDK 1&#046;1 JVMs, which don't
     * have that class.
     */
    public static class UnsupportedOpException extends RuntimeException {

        UnsupportedOpException(String s) {
            super(s);
        }
    }
}
