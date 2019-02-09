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
import java.io.InputStream;

/**
 *
 * @author ste
 */
public class PacketInputStream extends InputStream {

    private final InputStream source;

    public PacketInputStream(InputStream source) {
        if (source == null) {
            throw new IllegalArgumentException("source can not be null");
        }
        this.source = source;
    }

    @Override
    public int read() throws IOException {
        return source.read();
    }

    /**
     * Reads 4 bytes and tuns them into a int following big endian convention.
     * if not enough bytes are available an IOExceptionException is thrown.
     *
     * @return the int (big endian)
     *
     * @throws IOException if not enough bytes are available or in case of
     *                     errors in the source stream.
     */
    public int readBEInt() throws IOException {
        int next = source.read();
        if (next < 0) {
            throw new IOException("not enough bytes (4 missing)");
        }
        int ret = next << 24;

        next = source.read();
        if (next < 0) {
            throw new IOException("not enough bytes (3 missing)");
        }
        ret |= (next << 16);

        next = source.read();
        if (next < 0) {
            throw new IOException("not enough bytes (2 missing)");
        }
        ret |= (next << 8);

        next = source.read();
        if (next < 0) {
            throw new IOException("not enough bytes (1 missing)");
        }
        ret |= next;

        return ret;
    }

    /**
     * Reads 4 bytes into a little endian int.
     * if not enough bytes are available an IOExceptionException is thrown.
     *
     * @return the next int (32 bits) (little endian)
     *
     * @throws IOException if not enough bytes are available or in case of
     *                     errors in the source stream.
     */
    public int readLEInt() throws IOException {
        int next = source.read();
        if (next < 0) {
            throw new IOException("not enough bytes (4 missing)");
        }
        int ret = next;

        next = source.read();
        if (next < 0) {
            throw new IOException("not enough bytes (3 missing)");
        }
        ret |= (next << 8);

        next = source.read();
        if (next < 0) {
            throw new IOException("not enough bytes (2 missing)");
        }
        ret |= (next << 16);

        next = source.read();
        if (next < 0) {
            throw new IOException("not enough bytes (1 missing)");
        }
        ret |= (next << 24);

        return ret;
    }

    /**
     * Reads 2 bytes into a little endian short.
     * if not enough bytes are available an IOExceptionException is thrown.
     *
     * @return the next short (16 bits) in the stream (little endian)
     *
     * @throws IOException if not enough bytes are available or in case of
     *                     errors in the source stream.
     */
    public short readLEShort() throws IOException {
        int next = source.read();
        if (next < 0) {
            throw new IOException("not enough bytes (2 missing)");
        }
        short ret = (short)next;

        next = source.read();
        if (next < 0) {
            throw new IOException("not enough bytes (1 missing)");
        }
        ret |= (next << 8);

        return ret;
    }

    public byte[] readBytes(int howMany) throws IOException {
        if (howMany < 0) {
            throw new IllegalArgumentException("howMany can not be lesser than 0");
        }

        byte[] ret = new byte[howMany];

        if (howMany > 0) {
            int read = source.read(ret);
            if (read < 0) {
                read = 0;
            }
            if (read < howMany) {
                throw new IOException("not enough bytes (" + (howMany-read) + " missing)");
            };
        }

        return ret;
    }

    public String readString() throws IOException {
        StringBuilder sb = new StringBuilder();

        int c = 0;
        do {
            c = read() | (read()<<8);
            if (c > 0) {
                sb.append((char)c);
            }
        } while (c > 0);

        if (c < 0) {
            throw new IOException("string not terminated (eof)");
        }

        return sb.toString();
    }

    public String readVersion() throws IOException {
        int minor = read() | (read() << 8);
        int major = read() | (read() << 8);

        if (major < 0) {
            throw new IOException("not enough bytes");
        }

        return major + "." + minor;
    }

    public InitCommandAcknowledge readInitCommandAcknowledge() throws IOException {
        return new InitCommandAcknowledge(
            readLEInt(),
            readBytes(16),
            readString(),
            readVersion()
        );
    }

    public InitCommandRequest readInitCommandRequest() throws IOException {
        return new InitCommandRequest(
            readBytes(16),
            readString(),
            readVersion()
        );
    }

    public InitEventRequest readInitEventRequest() throws IOException {
        return new InitEventRequest(readLEInt());
    }

    public InitEventAcknowledge readInitEventAcknowledge() throws IOException {
        return new InitEventAcknowledge();
    }

    public InitError readInitError() throws IOException {
        return new InitError(readBEInt());
    }

    public OperationRequest readOperationRequest() throws IOException {
        int dataPhaseInfo = readLEInt();
        short code = readLEShort();
        int transaction = readLEInt();

        return new OperationRequest(code, dataPhaseInfo, transaction);
    }

    @Override
    public void close() {
        System.out.println("CLOSING " + this);
    }
}
