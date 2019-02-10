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
public class BugFreeInitEventAcknowledge {

    @Test
    public void init_event_ack_payload_type() {
        then(new InitEventAcknowledge().getType()).isEqualTo(Constants.INIT_EVENT_ACK);
    }

    @Test
    public void init_event_ack_payload_size() {
        then(new InitEventAcknowledge().getSize()).isZero();
    }

    @Test
    public void error_payload_with_code() {
        then(new InitError(0x00112233).error).isEqualTo(0x00112233);
        then(new InitError(0x0a0b0c0d).error).isEqualTo(0x0a0b0c0d);
    }

}
