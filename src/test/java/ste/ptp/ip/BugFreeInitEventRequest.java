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

import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;

/**
 *
 * @author ste
 */
public class BugFreeInitEventRequest {

    @Test
    public void init_event_request_payload_type() {
        then(new InitEventRequest(0x01).getType()).isEqualTo(Constants.INIT_EVENT_REQUEST);
    }

    @Test
    public void error_payload_size() {
        then(new InitEventRequest(0x01).getSize()).isEqualTo(4);
    }

    @Test
    public void error_payload_with_sessionid() {
        then(new InitEventRequest(0x01020304).sessionId).isEqualTo(0x01020304);
        then(new InitEventRequest(0x0a0b0c0d).sessionId).isEqualTo(0x0a0b0c0d);
    }

}
