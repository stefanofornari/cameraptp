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

import static ste.ptp.ip.Constants.OPERATION_REQUEST;

/**
 *
 *
 */
public class OperationRequest extends Payload {

    public int code;
    public int transaction;
    public int dataPhaseInfo;

    public OperationRequest(int code) {
        this(code, 0, 0);
    }

    public OperationRequest(int code, int transaction) {
        this(code, 0, transaction);
    }

    public OperationRequest(int code, int dataPhaseInfo, int transaction) {
        this.code = code;
        this.dataPhaseInfo = dataPhaseInfo;
        this.transaction = transaction;
    }

    public int getType() {
        return OPERATION_REQUEST;
    }

    public int getSize() {
        return 14;
    }
}
