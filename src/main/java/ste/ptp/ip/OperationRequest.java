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

import ste.ptp.Operation;
import static ste.ptp.ip.Constants.OPERATION_REQUEST;

/**
 *
 *
 */
public class OperationRequest extends Payload {

    final public Operation operation;
    final public int transaction;
    final public int dataPhaseInfo;
    final public int[] params;

    public OperationRequest(Operation o) {
        this(o, 0, 0);
    }
    public OperationRequest(Operation o, int dataPassInfo, int transaction, int... params) {
        this.operation = o;
        this.transaction = transaction;
        this.dataPhaseInfo = dataPassInfo;
        this.params = params;
    }

    public int getType() {
        return OPERATION_REQUEST;
    }

    public int getSize() {
        return 8 + (2 + 4*operation.getParams().length) + 4*params.length;
    }
}
