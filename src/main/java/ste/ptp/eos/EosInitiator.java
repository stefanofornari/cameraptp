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
package ste.ptp.eos;

import ch.ntb.usb.*;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import ste.ptp.BaselineInitiator;
import ste.ptp.Command;
import ste.ptp.Data;
import ste.ptp.DevicePropDesc;
import ste.ptp.Event;
import ste.ptp.PTPException;
import ste.ptp.PTPUnsupportedException;
import ste.ptp.Response;

/**
 * This supports all standardized PTP-over-USB operations, including
 * operations (and modes) that are optional for all responders.
 * Filtering operations invoked on this class may be done on the device,
 * or may be emulated on the client side.
 * At this time, not all standardized operations are supported.
 *
 * @version $Id: EosInitiator.java,v 1.9 2001/04/12 23:13:00 dbrownell Exp $
 * @author David Brownell
 */
public class EosInitiator extends BaselineInitiator {

    /**
     * This is essentially a class driver, following Annex D of
     * the PTP specification.
     */
    public EosInitiator(Device dev) throws PTPException {
        super(dev);
    }

    /**
     * Fills out the provided device property description.
     *
     * @param propcode code identifying the property of interest
     * @param desc description to be filled; it may be a subtype
     *	associated with with domain-specific methods
     * @return response code
     */
    public int getDevicePropDesc(int propcode, DevicePropDesc desc)
            throws PTPException {
        return transact1(Command.GetDevicePropDesc, desc, propcode).getCode();
    }

    /**
     * Checks if there is any event available. If there is any, an ArrayList
     * of Event object is returned.
     *
     * @return the list of available events
     *
     * @throws PTPException in case of errors
     */
    public ArrayList<Event> checkEvents()
    throws PTPException {
        //EosEvent data = new EosEvent();
        Data data = new Data(this);
        Response res = transact0(Command.EosGetEvent, data);
        if (res.getCode() != Response.OK) {
            throw new PTPException(String.format("Failed getting events from the camera (%1$04X)", res.getCode()));
        }

        //
        // We need to discard the initial 12 USB header bytes
        //
        byte[] buf = new byte[data.getLength()-12];
        System.arraycopy(data.getData(), 12, buf, 0, buf.length);

        EosEventParser parser = new EosEventParser(new ByteArrayInputStream(buf));

        while (parser.hasEvents()) {
            try {
                System.out.println(EosEventFormat.format(parser.getNextEvent()));
            } catch (PTPUnsupportedException e) {
                System.out.println("Skipping undupported event");
            }
        }

        data.dump();

        //return new EosEventParser(res.getData()).getEvents();

        return new ArrayList<Event>();
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
    public int initiateCapture(int storageId, int formatCode)
    throws PTPException {
        //
        // Special initialization for EOS cameras
        //
        if (!info.supportsOperation(Command.EosRemoteRelease)) {
            throw new PTPException("The camera does not support EOS capture");
        }

        int ret = transact1(Command.EosSetRemoteMode, null, 1).getCode();
        if (ret != Response.OK) {
            return ret;
        }

        ret = transact1(Command.EosSetEventMode, null, 1).getCode();
        if (ret != Response.OK) {
            return ret;
        }

        //
        // TODO: cover the case where initialization has already been done
        //

        checkEvents();
        
        
        //
        // Generic intitialization
        //
        /*
        ret = transact2(Command.InitiateCapture, null, storageId, formatCode).getCode();
        if (ret != Response.OK) {
            return ret;
        }
         * 
         */

        return Response.OK;
    }
}
