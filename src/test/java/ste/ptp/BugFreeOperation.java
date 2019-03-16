/* Copyright 2019 by Stefano Fornari
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
 * @author ste
 */
public class BugFreeOperation {

    @Test
    public void provide_operation_code() {
        then(new Operation(0x0102).code).isEqualTo(0x0102);
        then(new Operation(0x0201).code).isEqualTo(0x0201);
    }

    @Test
    public void no_parameters() {
        then(new Operation(0x0102).getParams()).isEmpty();
        then(new Operation(0x0201).getParams()).isEmpty();
    }

}
