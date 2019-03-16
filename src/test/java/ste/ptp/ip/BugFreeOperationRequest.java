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
import ste.ptp.OpenSessionOperation;
import ste.ptp.Operation;

/**
 *
 * @author ste
 */
public class BugFreeOperationRequest {

    @Test
    public void operaqtion_request_payload_type() {
        then(new OperationRequest(new Operation(Command.GetObject)).getType()).isEqualTo(Constants.OPERATION_REQUEST);
    }

    @Test
    public void operation_request_payload_size() {
        then(new OperationRequest(new Operation(Command.GetObject)).getSize()).isEqualTo(14);
    }

    @Test
    public void operation_request_payload_constructor() {
        OperationRequest O = new OperationRequest(new OpenSessionOperation());

        then(O.operation.code).isEqualTo((short)Command.OpenSession);
        then(O.transaction).isEqualTo(0);
        then(O.dataPhaseInfo).isEqualTo(0);

        O = new OperationRequest(new Operation(Command.DeleteObject), 01, 10);
        then(O.operation.code).isEqualTo((short)Command.DeleteObject);
        then(O.transaction).isEqualTo(10);
        then(O.dataPhaseInfo).isEqualTo(1);
    }

    @Test
    public void operation_request_payload_with_code() {
        OperationRequest r = new OperationRequest(
            new Operation(0x0102));
        then(r.operation.code).isEqualTo((short)0x0102); then(r.transaction).isZero();

        r = new OperationRequest(
            new Operation(0x0a0b));
        then(r.operation.code).isEqualTo((short)0x0a0b); then(r.transaction).isZero();
    }

    @Test
    public void operation_request_payload_with_session() {
        OperationRequest r = new OperationRequest(
            new Operation(0x0102), 0x01, 0x00110011
        );
        then(r.operation.code).isEqualTo((short)0x0102);
        then(r.transaction).isEqualTo(0x00110011);
        then(r.dataPhaseInfo).isEqualTo(0x01);

        r = new OperationRequest(
            new Operation(0x0a0b), 0x02, 0x11001100
        );
        then(r.getSize()).isEqualTo(10);
        then(r.operation.code).isEqualTo((short)0x0a0b);
        then(r.transaction).isEqualTo(0x11001100);
        then(r.dataPhaseInfo).isEqualTo(0x02);
    }

    @Test
    public void operation_payload_with_parameters() {
        OperationRequest r = new OperationRequest(
            new Operation(0x0102), 0x01, 0x00110011, 0x01
        );
        then(r.getSize()).isEqualTo(14);
        then(r.operation.code).isEqualTo((short)0x0102);
        then(r.transaction).isEqualTo(0x00110011);
        then(r.dataPhaseInfo).isEqualTo(0x01);
        then(r.params).containsExactly(0x01);

        r = new OperationRequest(
            new Operation(0x0201), 0x12, 0x00220022, 0x02, 0x04
        );
        then(r.getSize()).isEqualTo(18);
        then(r.operation.code).isEqualTo((short)0x0201);
        then(r.transaction).isEqualTo(0x00220022);
        then(r.dataPhaseInfo).isEqualTo(0x12);
        then(r.params).containsExactly(0x02, 0x04);
    }


}
