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
package ste.ptp.ip;

/**
 *
 * TODO: bugfree code
 */
public class Utils {
    public static byte[] littleEndian(int i) {
        byte[] buf = new byte[4];

        buf[0] = (byte)(i & 0x000000ff);
        buf[1] = (byte)((i>>8) & 0x000000ff);
        buf[2] = (byte)((i>>16) & 0x000000ff);
        buf[3] = (byte)((i>>24) & 0x000000ff);

        return buf;
    }
}
