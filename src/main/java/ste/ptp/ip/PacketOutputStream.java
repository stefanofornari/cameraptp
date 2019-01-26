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
 */
public class PacketOutputStream extends OutputStream {

    private final OutputStream destination;

    public PacketOutputStream(OutputStream destination) {
        if (destination == null) {
            throw new IllegalArgumentException("destination can not be null");
        }
        this.destination = destination;
    }

    public int write(InitCommandRequest payload) throws IOException {
        byte[] hostnameBuf = payload.hostname.getBytes("UTF_16LE");

        destination.write(payload.guid);
        destination.write(hostnameBuf); destination.write(0); destination.write(0);

        int pos = payload.version.indexOf('.');
        int major = Integer.parseInt(payload.version.substring(0, pos));
        int minor = Integer.parseInt(payload.version.substring(pos+1));

        destination.write(minor & 0x00ff); destination.write((minor & 0xff00) >> 8);
        destination.write(major & 0x00ff); destination.write((major & 0xff00) >> 8);

        return 16 + hostnameBuf.length + 2 + 4;
    }

    public int write(InitCommandAcknowledge payload) throws IOException {
        byte[] hostnameBuf = payload.hostname.getBytes("UTF_16LE");

        destination.write((payload.sessionId & 0xff000000) >> 24);
        destination.write((payload.sessionId & 0x00ff0000) >> 16);
        destination.write((payload.sessionId & 0x0000ff00) >> 8 );
        destination.write((payload.sessionId & 0x000000ff)      );

        destination.write(payload.guid);
        destination.write(hostnameBuf); destination.write(0); destination.write(0);

        int pos = payload.version.indexOf('.');
        int major = Integer.parseInt(payload.version.substring(0, pos));
        int minor = Integer.parseInt(payload.version.substring(pos+1));

        destination.write(minor & 0x00ff); destination.write((minor & 0xff00) >> 8);
        destination.write(major & 0x00ff); destination.write((major & 0xff00) >> 8);

        return 20 + hostnameBuf.length + 2 + 4;
    }

    public int write(PTPIPContainer container) {
        try {
            destination.write(littleEndian(container.getSize()));
            destination.write(littleEndian(container.payload.getType()));
            write(container.payload);
            destination.flush();
        } catch (IOException x) {
            x.printStackTrace();
        }

        return container.getSize();
    }

    public int write(Payload payload) throws IOException {
        if (payload instanceof InitCommandAcknowledge) {
            return write((InitCommandAcknowledge)payload);
        } else if (payload instanceof InitCommandRequest) {
            return write((InitCommandRequest)payload);
        }

        return payload.getSize();
    }

    @Override
    public void write(int b) throws IOException {
        destination.write(b);
    }
}
