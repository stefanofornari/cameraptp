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

import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;

/**
 *
 * @author ste
 */
public class BugFreeEosEvent {

    @Test
    public void code() {
        EosEvent e = new EosEvent();

        e.setCode(0x10);
        then(e.getCode()).isEqualTo(0x10);

        e.setCode(-0x10);
        then(e.getCode()).isEqualTo(-0x10);
    }

    @Test
    public void param1() {
        EosEvent e = new EosEvent();

        e.setParam(1, (int)0x11);
        then(e.getIntParam(1)).isEqualTo(0x11);
    }

    @Test
    public void param2() {
        EosEvent e = new EosEvent();

        e.setParam(2, (int)0x12);
        then(e.getIntParam(2)).isEqualTo(0x12);
    }

    @Test
    public void invalid_parameter_index() {
        EosEvent e = new EosEvent();

        try {
            e.setParam(-1, 0);
            fail("param index cannot be < 0");
        } catch (IllegalArgumentException x) {
            //
            // OK
            //
        }

        e.setParam(1, 1);
        e.setParam(2, new Object());

        try {
            e.getParam(-1);
            fail("param index cannot be < 0");
        } catch (IllegalArgumentException x) {
            //
            // OK
            //
        }

        try {
            e.getParam(4);
            fail("param index cannot be > param #");
        } catch (IllegalArgumentException x) {
            //
            // OK
            //
        }
    }

    @Test
    public void get_param() {
        EosEvent e = new EosEvent();

        String s = new String();
        e.setParam(1, s);
        then(e.getParam(1)).isSameAs(s);
    }

    @Test
    public void testGetStringParam() {
        EosEvent e = new EosEvent();

        String s = "test";
        e.setParam(1, s);
        then(e.getStringParam(1)).isEqualTo(s);
    }
}
