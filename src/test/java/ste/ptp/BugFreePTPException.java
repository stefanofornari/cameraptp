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
package ste.ptp;

import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;

/**
 *
 */
public class BugFreePTPException {

    @Test
    public void constructors() {
        final String MSG = "MSG";
        final Throwable T = new Exception();
        final int CODE = Response.AccessDenied;

        PTPException e = new PTPException();

        then(e).hasNoCause();
        then(e).hasMessage("0x00002000 ");
        then(e.getErrorCode()).isEqualTo(Response.Undefined);

        e = new PTPException(MSG);
        then(e).hasNoCause();
        then(e).hasMessage("0x00002000 " + MSG);
        then(e.getErrorCode()).isEqualTo(Response.Undefined);

        e = new PTPException(MSG, T);
        then(e).hasCause(T);
        then(e).hasMessage("0x00002000 " + MSG);
        then(e.getErrorCode()).isEqualTo(Response.Undefined);

        e = new PTPException(CODE);
        then(e).hasNoCause();
        then(e).hasMessage("0x0000200f ");
        then(e.getErrorCode()).isEqualTo(CODE);

        e = new PTPException(MSG, CODE);
        then(e).hasNoCause();
        then(e).hasMessage("0x0000200f " + MSG);
        then(e.getErrorCode()).isEqualTo(CODE);
    }

}
