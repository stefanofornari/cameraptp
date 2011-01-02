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
package ste.ptp;

import ch.ntb.usb.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

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
 * going to be <code>usb.core.PTPException</code> values.  That may
 * help your application level recovery processing.  You should
 * assume that when any IOException is thrown, your current session
 * has been terminated.
 *
 * @see Initiator
 *
 * @version $Id: BaselineInitiator.java,v 1.17 2001/05/30 19:33:43 dbrownell Exp $
 * @author David Brownell
 *
 * This class has been reworked by ste in order to make it compatible with
 * usbjava2. Also, this is more a derivative work than just an adaptation of the
 * original version. It has to serve the purposes of usbjava2 and cameracontrol.
 */
public class BaselineInitiator extends NameFactory implements Runnable {

    ///////////////////////////////////////////////////////////////////
    // USB Class-specific control requests; from Annex D.5.2
    private static final byte CLASS_CANCEL_REQ        = (byte) 0x64;
    private static final byte CLASS_GET_EVENT_DATA    = (byte) 0x65;
    private static final byte CLASS_DEVICE_RESET      = (byte) 0x66;
    private static final byte CLASS_GET_DEVICE_STATUS = (byte) 0x67;

    final static boolean DEBUG = false;
    final static boolean TRACE = false;
    
    protected Device                 device;
    protected UsbInterfaceDescriptor intf;
    protected UsbEndpointDescriptor  in;
    protected int                    inMaxPS;
    protected UsbEndpointDescriptor  out;
    protected UsbEndpointDescriptor  intr;
    protected Session                session;
    protected DeviceInfo             info;

    /**
     * Constructs a class driver object, if the device supports
     * operations according to Annex D of the PTP specification.
     *
     * @param device the first PTP interface will be used
     * @exception IllegalArgumentException if the device has no
     *	Digital Still Imaging Class or PTP interfaces
     */
    public BaselineInitiator(Device dev) throws PTPException {
        try {
            if (dev == null) {
                throw new IllegalArgumentException();
            }
            session = new Session();
            this.device = dev;
            intf = dev.getPTPInterface();

            UsbInterface usbInterface = intf.getUsbInterface();

            if (usbInterface == null) {
                throw new PTPException("No PTP interfaces associated to the device");
            }

            ArrayList<UsbEndpointDescriptor> endpoints = usbInterface.getAllEndpoints();
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
            endpointSanityCheck();

            UsbDevice usbDevice = dev.getUsbDevice();
            UsbConfigDescriptor[] descriptors = usbDevice.getConfig();

            if ((descriptors == null) || (descriptors.length < 1)) {
                throw new PTPException("Device with no descriptors!");
            }

            // we want exclusive access to this interface.
            dev.open(
                descriptors[0].getConfigurationValue(),
                intf.getInterface(),
                intf.getAlternateSetting()
            );

            // clear out any previous state
            reset();
            if (getClearStatus() != Response.OK
                    && getDeviceStatus(null) != Response.OK) {
                throw new PTPException("can't init");
            }

            // get info to sanity check later requests
            info = getDeviceInfoUncached();

            // set up to use vendor extensions, if any
            if (info.vendorExtensionId != 0) {
                info.factory = updateFactory(info.vendorExtensionId);
            }
            session.setFactory(this);
        } catch (USBBusyException e) {
            throw new PTPBusyException();
        } catch (USBException e) {
            throw new PTPException(
                "Error initializing the communication with the camera (" +
                e.getMessage()
                + ")" , e);
        }
    }

    /**
     * @return the device
     */
    public Device getDevice() {
        return device;
    }

        /**
     * Returns the last cached copy of the device info, or returns
     * a newly cached copy.
     * @see #getDeviceInfoUncached
     */
    public DeviceInfo getDeviceInfo() throws PTPException {
        if (info == null) {
            return getDeviceInfoUncached();
        }
        return info;
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
    public void reset() throws PTPException {
        try {
            device.controlMsg(
                (byte) (ControlMessage.DIR_TO_DEVICE      |
                        ControlMessage.TYPE_CLASS         |
                        ControlMessage.RECIPIENT_INTERFACE),
                CLASS_DEVICE_RESET,
                0,
                0,
                new byte[0],
                0,
                Device.DEFAULT_TIMEOUT,
                false
            );

            session.close();
        } catch (USBException e) {
            throw new PTPException(
                "Error initializing the communication with the camera (" +
                e.getMessage()
                + ")" , e);
        }
    }

    /**
     * Issues an OpenSession command to the device; may be used
     * with all responders.  PTP-over-USB doesn't seem to support
     * multisession operations; you must close a session before
     * opening a new one.
     */
    public void openSession() throws PTPException {
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
                    throw new PTPException(response.toString());
            }
        }
    }

    /**
     * Issues a CloseSession command to the device; may be used
     * with all responders.
     */
    public void closeSession() throws PTPException {
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
                    throw new PTPException(response.toString());
            }
        }
    }

    /**
     * @return true if the current session is active, false otherwise
     */
    public boolean isSessionActive() {
        synchronized (session) {
            return session.isActive();
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    // mandatory for all responders:  generating events
    /**
     * Makes the invoking Thread read and report events reported
     * by the PTP responder, until the Initiator is closed.
     */
    @Override
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

                    event = new Event(intr.recvInterrupt(), this);
                    if (TRACE) {
                        System.err.println("EVENT: " + event.toString());
                    }

                } catch (Exception e) {
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

    // ------------------------------------------------------- Protected methods

    ///////////////////////////////////////////////////////////////////
    /**
     * Performs a PTP transaction, passing zero command parameters.
     * @param code the command code
     * @param data data to be sent or received; or null
     * @return response; check for code Response.OK before using
     *	any positional response parameters
     */
    protected Response transact0(int code, Data data)
    throws PTPException {
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
    throws PTPException {
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
            throws PTPException {
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
            throws PTPException {
        synchronized (session) {
            Command command = new Command(code, session, p1, p2, p3);
            return transactUnsync(command, data);
        }
    }

    // --------------------------------------------------------- Private methods

        // like getDeviceStatus(),
    // but clears stalled endpoints before returning
    // (except when exceptions are thrown)
    // returns -1 iff device wouldn't return OK status
    private int getClearStatus() throws PTPException {
        Buffer buf = new Buffer(null, 0);
        int retval = getDeviceStatus(buf);

        // any halted endpoints to clear?  (always both)
        if (buf.length != 4) {
            while ((buf.offset + 4) <= buf.length) {
                int ep = buf.nextS32();

                if (in.getEndpointAddress() == ep) {
                    if (TRACE) {
                        System.err.println("clearHalt in");
                    }
                    clearHalt(in);
                } else if (out.getEndpointAddress() == ep) {
                    if (TRACE) {
                        System.err.println("clearHalt out");
                    }
                    clearHalt(out);
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
                } catch (PTPException x) {
                    if (DEBUG) {
                        x.printStackTrace();
                    }
                }
                if (status == Response.OK) {
                    break;
                }
                if (TRACE) {
                    System.err.println("sleep; status = "
                            + getResponseString(status));
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

    // returns Response.OK, Response.DeviceBusy, etc
    // per fig D.6, response may hold stalled endpoint numbers
    private int getDeviceStatus(Buffer buf)
    throws PTPException {
        try {
            byte[] data = new byte[33];
            device.controlMsg(
                (byte) (ControlMessage.DIR_TO_HOST        |
                        ControlMessage.TYPE_CLASS         |
                        ControlMessage.RECIPIENT_INTERFACE),
                CLASS_GET_DEVICE_STATUS,
                0,
                0,
                data,
                data.length, // force short reads
                Device.DEFAULT_TIMEOUT,
                false
            );

            if (buf == null) {
                buf = new Buffer(data);
            } else {
                buf.data = data;
            }
            buf.offset = 4;
            buf.length = buf.getU16(0);
            if (buf.length != buf.data.length) {
                throw new RuntimeException();
            }

            return buf.getU16(2);
        }  catch (USBException e) {
            throw new PTPException(
                "Error initializing the communication with the camera (" +
                e.getMessage()
                + ")" , e);
        }
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
    private DeviceInfo getDeviceInfoUncached()
    throws PTPException {
        DeviceInfo data = new DeviceInfo(this);
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
                throw new PTPException(response.toString());
        }
    }

    ///////////////////////////////////////////////////////////////////
    // INVARIANTS:
    // - caller is synchronized on session
    // - on return, device is always in idle/"command ready" state
    // - on return, session was only closed by CloseSession
    // - on PTPException, device (and session!) has been reset
    private Response transactUnsync(Command command, Data data)
    throws PTPException {
        if (!"command".equals(command.getBlockTypeName(command.getBlockType()))) {
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
            throw new UnsupportedOperationException(command.getCodeName(opcode));
        }

        // ok, then we'll really talk to the device
        Response response;
        boolean abort = true;

        try {
            OutputStream stream = device.getOutputStream(out);

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
                            throw new PTPException("eh? " + len);
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
                                throw new PTPException("short: " + len);
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
                    int len = device.getInputStream(in).read(buf1);

                    // Get the first bulk packet(s), check header for length
                    data.data = buf1;
                    data.length = len;
                    if (TRACE) {
                        System.err.println(data.toString());
                    }
                    if (!"data".equals(data.getBlockTypeName(data.getBlockType()))
                            || data.getCode() != command.getCode()
                            || data.getXID() != command.getXID()) {
                        throw new PTPException("protocol err 1, " + data);
                    }

                    // get the rest of it
                    int expected = data.getLength();

                    // Special handling for the write-to-N-mbytes-file case
                    if (data instanceof FileData) {
                        FileData fd = (FileData) data;

                        fd.write(buf1, Data.HDR_LEN, len - Data.HDR_LEN);
                        if (len == inMaxPS && expected != inMaxPS) {
                            InputStream is = device.getInputStream(in);

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
                        data.length += device.getInputStream(in).read(buf1, len, expected - len);
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
            int len = device.getInputStream(in).read(buf);

            // ZLP terminated previous data?
            if (len == 0) {
                len = device.getInputStream(in).read(buf);
            }

            response = new Response(buf, len, this);
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
                } catch (PTPException x) {
                    if (DEBUG) {
                        x.printStackTrace();
                    }
                }

                // something's very broken
                if (status == Response.OK || status == -1) {
                    throw new PTPException(e.getMessage(), e);
                }

                // treat status code as the device's response
                response = new Response(new byte[Response.HDR_LEN], this);
                response.putHeader(Response.HDR_LEN, 3 /*response*/,
                        status, command.getXID());
                if (TRACE) {
                    System.err.println("STALLED: " + response.toString());
                }

                abort = false;
                return response;
            }
            throw new PTPException(e.getMessage(), e);

        } catch (IOException e) {
            throw new PTPException(e.getMessage(), e);

        } finally {
            if (abort) {
                // not an error we know how to recover;
                // bye bye session!
                reset();
            }
        }
    }

    private void endpointSanityCheck() throws PTPException {
        if (in == null) {
            throw new PTPException("No input end-point found!");
        }

        if (out == null) {
            throw new PTPException("No output end-point found!");
        }

        if (intr == null) {
            throw new PTPException("No input interrupt end-point found!");
        }
    }

    private void clearHalt(UsbEndpointDescriptor e) {
        //
        // TODO: implement clearHalt of an endpoint
        //
    }

}
