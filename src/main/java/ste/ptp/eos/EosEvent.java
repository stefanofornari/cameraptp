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
import java.util.List;



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
     * Params
     */
    private List params;

    /**
     * Creates a new parser with the given data
     *
     * @param data the events data - NOT NULL
     */
    public EosEvent() {
        params = new ArrayList();
    }

    public void setCode(int code) {
        if (code < 0) {
            throw new IllegalArgumentException("code cannot be < 0");
        }
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    /**
     * @param i the parameter index
     * @param param the param to set
     */
    public void setParam(int i, Object value) {
        if (i<0) {
            throw new IllegalArgumentException("param index cannot be < 0");
        }
        if (params.size() <= i) {
            ArrayList newParams = new ArrayList(i);
            newParams.addAll(params);
            params = newParams;
            for (int j=params.size(); j<i; ++j) {
                params.add(null);
            }
        }
        params.set(i-1, value);
    }

    public void setParam(int i, int value) {
        setParam(i, new Integer(value));
    }

    public int getIntParam(int i) {
        return ((Integer)params.get(i-1)).intValue();
    }

    /**
     *
     * @return the number of parameters in this event
     */
    public int getParamCount() {
        return params.size();
    }

}
