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
import ste.ptp.Command;

/**
 *
 * @author ste
 */
public class BugFreeOperationRequest {

    @Test
    public void operaqtion_request_payload_type() {
        then(new OperationRequest(Command.GetDeviceInfo).getType()).isEqualTo(Constants.OPERATION_REQUEST);
    }

    @Test
    public void operation_payload_size() {
        then(new OperationRequest(0x01).getSize()).isEqualTo(14);
    }

    @Test
    public void operation_payload_constructor() {
        OperationRequest O = new OperationRequest(Command.GetDeviceInfo);

        then(O.code).isEqualTo(Command.GetDeviceInfo);
        then(O.transaction).isEqualTo(0);
        then(O.dataPhaseInfo).isEqualTo(0);

        O = new OperationRequest(Command.DeleteObject, 10);
        then(O.code).isEqualTo(Command.DeleteObject);
        then(O.transaction).isEqualTo(10);
        then(O.dataPhaseInfo).isEqualTo(0);

        O = new OperationRequest(Command.DeleteObject, 2, 1000);
        then(O.code).isEqualTo(Command.DeleteObject);
        then(O.transaction).isEqualTo(1000);
        then(O.dataPhaseInfo).isEqualTo(2);
    }

    @Test
    public void operation_paylot_with_code() {
        OperationRequest r = new OperationRequest(0x01020304);
        then(r.code).isEqualTo(0x01020304); then(r.transaction).isZero();

        r = new OperationRequest(0x0a0b0c0d);
        then(r.code).isEqualTo(0x0a0b0c0d); then(r.transaction).isZero();
    }

    @Test
    public void operation_payload_with_session() {
        OperationRequest r = new OperationRequest(0x01020304, 0x00110011);
        then(r.code).isEqualTo(0x01020304); then(r.transaction).isEqualTo(0x00110011);

        r = new OperationRequest(0x0a0b0c0d, 0x11001100);
        then(r.code).isEqualTo(0x0a0b0c0d); then(r.transaction).isEqualTo(0x11001100);
    }


}
