/* Copyright 2010 by Stefano Fornari
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package ste.ptp.eos;

import java.io.IOException;
import java.io.InputStream;
import ste.ptp.PTPException;
import ste.ptp.PTPUnsupportedException;

/**
 * This class parses a stream of bytes as a sequence of events accordingly
 * to how Canon EOS returns events.
 *
 * The event information is returned in a standard PTP data packet as a number
 * of records followed by an empty record at the end of the packet. Each record
 * consists of multiple four-byte fields and always starts with record length
 * field. Further structure of the record depends on the device property code
 * which always goes in the third field. The empty record consists of the size
 * field and four byte empty field, which is always zero.
 *
 * @author stefano fornari
 */
public class EosEventParser {

    /**
     * The stream data are read from
     */
    private InputStream is;

    /**
     * Creates a new parser to parse the given input stream
     * 
     * @param is
     */
    public EosEventParser(InputStream is) {
        if (is == null) {
            throw new IllegalArgumentException("The input stream cannot be null");
        }
        
        this.is = is;
    }

    /**
     * Returns true is there are events in the stream (and the stream is still
     * open), false otherwise.
     *
     * @return true is there are events in the stream (and the stream is still
     * open), false otherwise.
     */
    public boolean hasEvents() {
        try {
            if (is.available() <= 0) {
                return false;
            }
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    /**
     * Returns the next event in the stream.
     *
     * @return the next event in the stream.
     * 
     * @throws PTPException in case of errors
     */
    public EosEvent getNextEvent() throws PTPException {
        EosEvent event = new EosEvent();

        try {
            int len = getNextS32(); // len
            if (len < 0x8) {
                throw new PTPUnsupportedException("Unsupported event (size<8 ???)");
            }
            event.setCode(getNextS32());

            parseParameters(event, len-8);
        } catch (IOException e) {
            throw new PTPException("Error reading event stream", e);
        }

        return event;
    }


    // --------------------------------------------------------- Private methods

    private void parseParameters(EosEvent event, int len)
    throws PTPException, IOException {
        int code = event.getCode();
        
        if (code == event.EosEventPropValueChanged) {
            event.setParam(1, getNextS32());
            event.setParam(2, getNextS32());
        } else if (code == event.EosEventShutdownTimerUpdated) {
            //
            // No parameters
            //
        } else {
            is.skip(len);
            throw new PTPUnsupportedException("Unsupported event");
        }
    }

    /**
     * Reads and return the next signed 32 bit integer read from the input
     * stream.
     *
     * @return the next signed 32 bit integer in the stream
     *
     * @throws IOException in case of IO errors
     */
    private  final int getNextS32 () throws IOException {
	int retval;

	retval  = (0xff & is.read()) ;
	retval |= (0xff & is.read()) << 8;
	retval |= (0xff & is.read()) << 16;
	retval |=         is.read()  << 24;

	return retval;
    }

}
