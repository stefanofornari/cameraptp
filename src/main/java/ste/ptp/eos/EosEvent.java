/* Copyright 2010 by Stefano Fornari
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

package ste.ptp.eos;

import java.util.ArrayList;
import ste.ptp.Data;
import ste.ptp.Event;
import ste.ptp.NameFactory;



/**
 *
 * @author ste
 */
public class EosEvent implements EosEventConstants {

    /**
     * Event code
     */
    private int code;

    /**
     * Param 1
     */
    private int param1;

    /**
     * Param 2
     */
    private int param2;

    /**
     * Creates a new parser with the given data
     *
     * @param data the events data - NOT NULL
     */
    public EosEvent() {
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public int getParam1() {
        return param1;
    }

    public int getParam2() {
        return param2;
    }

    /**
     * @param param1 the param1 to set
     */
    public void setParam1(int param1) {
        this.param1 = param1;
    }

    /**
     * @param param2 the param2 to set
     */
    public void setParam2(int param2) {
        this.param2 = param2;
    }

}
