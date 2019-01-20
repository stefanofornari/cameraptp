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

import java.io.IOException;
import java.io.OutputStream;
import static ste.ptp.ip.Utils.littleEndian;

/**
 *
 * @author ste
 */
public class PayloadWriter {

    public int write(OutputStream out, InitCommandRequest payload) throws IOException {
        byte[] hostnameBuf = payload.hostname.getBytes("UTF_16LE");

        out.write(payload.guid);
        out.write(hostnameBuf); out.write(0); out.write(0);

        int pos = payload.version.indexOf('.');
        int major = Integer.parseInt(payload.version.substring(0, pos-1));
        int minor = Integer.parseInt(payload.version.substring(pos));

        out.write((major & 0xff00) >> 8); out.write(major & 0x00ff);
        out.write((minor & 0xff00) >> 8); out.write(minor & 0x00ff);

        return 4 + hostnameBuf.length + 2 + 4;
    }

    public int write(OutputStream out, PTPIPContainer container) {
        try {
            out.write(littleEndian(container.getSize()));
            out.write(littleEndian(container.payload.getType()));
            write(out, container.payload);
        } catch (IOException x) {
        }

        return container.getSize();
    }

    public int write(OutputStream out, Payload payload) throws IOException {
        if (payload instanceof InitCommandRequest) {
            return write(out, (InitCommandRequest)payload);
        }

        return payload.getSize();
    }
}
