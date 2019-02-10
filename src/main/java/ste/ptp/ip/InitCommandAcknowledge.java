/* Copyright 2018 by Stefano Fornari
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
*/

package ste.ptp.ip;

import static ste.ptp.ip.Constants.INIT_COMMAND_ACK;

/**
 *
 *
 */
public class InitCommandAcknowledge extends InitCommandRequest {

    public int sessionId;

    /**
     * TODO: sanity check on guid
     * @return
     */
    public InitCommandAcknowledge(int sessionId, byte[] guid, String hostname, String version) {
        super(guid, hostname, version);
        this.sessionId = sessionId;
    }

    public int getType() {
        return INIT_COMMAND_ACK;
    }

    public int getSize() {
        return 4 + guid.length + (hostname.length()+1)*2 + 4;
    }
}
