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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;

/**
 *
 * @author ste
 */
public class BugFreePacketOutputStream {

    private static final byte[] GUID1 = new byte[] {
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
        (byte)0x00, (byte)0x01, (byte)0xf4, (byte)0xa9,
        (byte)0x97, (byte)0xfa, (byte)0x6a, (byte)0xac
    };
    private static final byte[] GUID2 = new byte[] {
        (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04,
        (byte)0x10, (byte)0x20, (byte)0x30, (byte)0x40,
        (byte)0x0a, (byte)0x0b, (byte)0x0c, (byte)0x0d,
        (byte)0xa0, (byte)0xb0, (byte)0xc0, (byte)0xd0
    };

    @Test
    public void from_output_stream() throws Exception  {
        final ByteArrayOutputStream OS = new ByteArrayOutputStream();

        PacketOutputStream os = new PacketOutputStream(OS);

        os.write('h');
        os.write('e');
        os.write('l');
        os.write('l');
        os.write('o');

        then(OS.toString()).isEqualTo("hello");
    }

    @Test
    public void from_invalid_destination() {
        try {
            new PacketOutputStream(null);
            fail("missing sanity check");
        } catch (IllegalArgumentException x) {
            then(x).hasMessage("destination can not be null");
        }
    }

    @Test
    public void write_init_cmd_request() throws Exception {
        final ByteArrayOutputStream OS = new ByteArrayOutputStream();
        final InitCommandRequest R = new InitCommandRequest(GUID1, "mypc1", "1.0");

        PacketOutputStream out = new PacketOutputStream(OS);
        out.write(R);

        PacketInputStream is = new PacketInputStream(
            new ByteArrayInputStream(OS.toByteArray())
        );

        InitCommandRequest r = is.readInitCommandRequest();

        then(r.guid).containsExactly(R.guid);
        then(r.hostname).isEqualTo(R.hostname);
        then(r.version).isEqualTo(R.version);
    }

    @Test
    public void write_init_cmd_request_io_error() throws Exception {
        final InitCommandRequest R = new InitCommandRequest(GUID1, "mypc1", "1.0");

        PacketOutputStream out = new PacketOutputStream(new BrokenOutputStream());
        try {
            out.write(R);
            fail("no io error thrown");
        } catch (IOException x) {
            then(x).hasMessage("simulate an error");
        }
    }

    @Test
    public void write_init_cmd_acknowledge() throws Exception {
        final ByteArrayOutputStream OS = new ByteArrayOutputStream();
        final InitCommandAcknowledge R = new InitCommandAcknowledge(0x00010203, GUID1, "mypc1", "1.0");

        PacketOutputStream out = new PacketOutputStream(OS);
        out.write(R);

        PacketInputStream is = new PacketInputStream(
            new ByteArrayInputStream(OS.toByteArray())
        );

        InitCommandAcknowledge ack = is.readInitCommandAcknowledge();

        then(ack.sessionId).isEqualTo(R.sessionId);
        then(ack.guid).containsExactly(R.guid);
        then(ack.hostname).isEqualTo(R.hostname);
        then(ack.version).isEqualTo(R.version);
    }

    @Test
    public void write_init_cmd_acknowledge_io_error() throws Exception {
        final InitCommandAcknowledge R = new InitCommandAcknowledge(0x00010203, GUID1, "mypc1", "1.0");

        PacketOutputStream out = new PacketOutputStream(new BrokenOutputStream());
        try {
            out.write(R);
            fail("no io error thrown");
        } catch (IOException x) {
            then(x).hasMessage("simulate an error");
        }
    }

    @Test
    public void write_generic_packet() throws Exception {
        final ByteArrayOutputStream OS = new ByteArrayOutputStream();
        final InitCommandRequest R = new InitCommandRequest(GUID1, "mypc1", "1.0");
        final InitCommandAcknowledge A = new InitCommandAcknowledge(0x00010203, GUID2, "mypc2", "1.1");

        PacketOutputStream out = new PacketOutputStream(OS);
        out.write(new PTPIPContainer(R)); out.write(new PTPIPContainer(A));

        PacketInputStream is = new PacketInputStream(
            new ByteArrayInputStream(OS.toByteArray())
        );

        InitCommandRequest req = is.readInitCommandRequest();
        InitCommandAcknowledge ack = is.readInitCommandAcknowledge();
    }

    // ------------------------------------------------------ BrokenOutputStream

    private class BrokenOutputStream extends OutputStream {
        @Override
        public void write(int b) throws IOException {
            throw new IOException("simulate an error");
        }
    }
}
