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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import static ste.ptp.ip.Utils.bigEndian;
import static ste.ptp.ip.Utils.littleEndian;

/**
 *
 */
public class PacketOutputStream extends BufferedOutputStream {

    public PacketOutputStream(OutputStream destination) {
        super(destination);

        if (destination == null) {
            throw new IllegalArgumentException("destination can not be null");
        }
    }

    public int write(InitCommandRequest payload) throws IOException {
        byte[] hostnameBuf = payload.hostname.getBytes("UTF_16LE");

        write(payload.guid);
        write(hostnameBuf); write(0); write(0);

        int pos = payload.version.indexOf('.');
        int major = Integer.parseInt(payload.version.substring(0, pos));
        int minor = Integer.parseInt(payload.version.substring(pos+1));

        write(minor & 0x00ff); write((minor & 0xff00) >> 8);
        write(major & 0x00ff); write((major & 0xff00) >> 8);

        return 16 + hostnameBuf.length + 2 + 4;
    }

    public int write(InitCommandAcknowledge payload) throws IOException {
        byte[] hostnameBuf = payload.hostname.getBytes("UTF_16LE");

        writeLEInt(payload.sessionId);

        write(payload.guid);
        write(hostnameBuf); write(0); write(0);

        int pos = payload.version.indexOf('.');
        int major = Integer.parseInt(payload.version.substring(0, pos));
        int minor = Integer.parseInt(payload.version.substring(pos+1));

        write(minor & 0x00ff); write((minor & 0xff00) >> 8);
        write(major & 0x00ff); write((major & 0xff00) >> 8);

        return 20 + hostnameBuf.length + 2 + 4;
    }

    public int write(InitEventRequest payload) throws IOException {
        return writeLEInt(payload.sessionId);
    }

    public int write(InitError payload) throws IOException {
        return writeBEInt(payload.error);
    }

    public int write(OperationRequest payload) throws IOException {
        return
            writeLEInt(payload.dataPhaseInfo) +
            writeLEShort((short)payload.code) +
            writeLEInt(payload.transaction);
    }

    public int write(PTPIPContainer container) {
        try {
            writeLEInt(container.getSize());
            writeLEInt(container.payload.getType());
            write(container.payload);
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
        } else if (payload instanceof InitEventRequest) {
            return write((InitEventRequest)payload);
        } else if (payload instanceof InitEventAcknowledge) {
            return 0;
        } else if (payload instanceof InitError) {
            return write((InitError)payload);
        } else if (payload instanceof OperationRequest) {
            return write((OperationRequest)payload);
        }

        throw new IOException("unsupported payload " + payload.getClass());
    }

    public int writeBEInt(int i) throws IOException {
        write(bigEndian(i)); return 4;
    }

    public int writeLEInt(int i) throws IOException {
        write(littleEndian(i)); return 4;
    }

    public int writeLEShort(short s) throws IOException {
        write(littleEndian(s)); return 2;
    }
}
