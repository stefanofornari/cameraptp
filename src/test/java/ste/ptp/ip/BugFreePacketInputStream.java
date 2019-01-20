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
import java.io.IOException;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;

/**
 *
 * @author ste
 */
public class BugFreePacketInputStream {

    @Test
    public void from_input_stream() throws Exception  {
        final ByteArrayInputStream IS = new ByteArrayInputStream("hello".getBytes());

        PacketInputStream is = new PacketInputStream(IS);

        then(is.read()).isEqualTo('h');
        then(is.read()).isEqualTo('e');
        then(is.read()).isEqualTo('l');
        then(is.read()).isEqualTo('l');
        then(is.read()).isEqualTo('o');
    }

    @Test
    public void from_invalid_source() {
        try {
            new PacketInputStream(null);
            fail("missing sanity check");
        } catch (IllegalArgumentException x) {
            then(x).hasMessage("source can not be null");
        }
    }

    @Test
    public void read_big_endian_int_ok() throws Exception {
        final ByteArrayInputStream IS = new ByteArrayInputStream(
            new byte[] {0, 0, 0, 0, 0x0A, 0x0B, 0x0C, 0x0D}
        );

        PacketInputStream is = new PacketInputStream(IS);

        then(is.readBEInt()).isEqualTo(0);
        then(is.readBEInt()).isEqualTo(0x0A0B0C0D);
    }

    @Test
    public void read_big_endian_ko() throws Exception {
        final ByteArrayInputStream IS = new ByteArrayInputStream(
            new byte[] {0, 0, 0}
        );

        PacketInputStream is = new PacketInputStream(IS);

        try {
            is.readBEInt();
            fail("missing io error");
        } catch (IOException x) {
            then(x).hasMessage("not enough bytes (1 missing)");
        }

        try {
            is.readBEInt();
            fail("missing io error");
        } catch (IOException x) {
            then(x).hasMessage("not enough bytes (4 missing)");
        }
    }

    @Test
    public void read_little_endian_int_ok() throws Exception {
        final ByteArrayInputStream IS = new ByteArrayInputStream(
            new byte[] {0, 0, 0, 0, 0x0A, 0x0B, 0x0C, 0x0D}
        );

        PacketInputStream is = new PacketInputStream(IS);

        then(is.readLEInt()).isEqualTo(0);
        then(is.readLEInt()).isEqualTo(0x0D0C0B0A);
    }

    @Test
    public void read_little_endian_ko() throws Exception {
        final ByteArrayInputStream IS = new ByteArrayInputStream(
            new byte[] {0, 0, 0}
        );

        PacketInputStream is = new PacketInputStream(IS);

        try {
            is.readLEInt();
            fail("missing io error");
        } catch (IOException x) {
            then(x).hasMessage("not enough bytes (1 missing)");
        }

        try {
            is.readLEInt();
            fail("missing io error");
        } catch (IOException x) {
            then(x).hasMessage("not enough bytes (4 missing)");
        }
    }

    @Test
    public void read_init_command_acknowledge() throws Exception {
        final ByteArrayInputStream IS = new ByteArrayInputStream(
            new byte[] {
                (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, // 4 bytes session id
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, // ---
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, // 16 bytes GUID
                (byte)0x00, (byte)0x01, (byte)0xf4, (byte)0xa9, //
                (byte)0x97, (byte)0xfa, (byte)0x6a, (byte)0xac, // ---
                (byte)0x45, (byte)0x00, (byte)0x4f, (byte)0x00, // ---
                (byte)0x53, (byte)0x00, (byte)0x34, (byte)0x00, //
                (byte)0x30, (byte)0x00, (byte)0x30, (byte)0x00, // Host name (EOS4000D)
                (byte)0x30, (byte)0x00, (byte)0x44, (byte)0x00, //
                (byte)0x00, (byte)0x00,                         // ---
                (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x00   // version 1.0
            }
        );

        PacketInputStream is = new PacketInputStream(IS);
        InitCommandAcknowledge ack = is.readInitCommandAcknowledge();
        then(ack.sessionId).isEqualTo(16777216);
        then(ack.guid).isEqualTo(new byte[] {
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x01, (byte)0xf4, (byte)0xa9,
            (byte)0x97, (byte)0xfa, (byte)0x6a, (byte)0xac
        });
        then(ack.hostname).isEqualTo("EOS4000D");
        then(ack.version).isEqualTo("1.0");
    }

    @Test
    public void read_init_command_acknowledge_not_enough_bytes() throws Exception {
        final ByteArrayInputStream IS = new ByteArrayInputStream(
            new byte[] {
                (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, // 4 bytes session id
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, // ---
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00 // 16 bytes GUID
            }
        );

        PacketInputStream is = new PacketInputStream(IS);
        try {
            is.readInitCommandAcknowledge();
            fail("missing io error");
        } catch (IOException x) {
            then(x).hasMessage("not enough bytes (8 missing)");
        }
    }

    @Test
    public void read_bytes_with_enough_data() throws Exception {
        final ByteArrayInputStream IS = new ByteArrayInputStream(
            new byte[] {0x0A, 0x0B, 0x0C, 0x0D}
        );

        PacketInputStream is = new PacketInputStream(IS);

        then(is.readBytes(0)).isEmpty();
        then(is.readBytes(1)).containsExactly((byte)0x0A);
        then(is.readBytes(3)).containsExactly((byte)0x0B, (byte)0x0C, (byte)0x0D);
    }

    @Test
    public void read_bytes_with_not_enough_data() throws Exception {
        PacketInputStream is = new PacketInputStream(new ByteArrayInputStream(
            new byte[] {}
        ));

        try {
            is.readBytes(10);
            fail("missing io error");
        } catch (IOException x) {
            then(x).hasMessage("not enough bytes (10 missing)");
        }

        is = new PacketInputStream(new ByteArrayInputStream(
            new byte[] { (byte)0x0A, (byte)0x0B }
        ));
        try {
            is.readBytes(100);
            fail("missing io error");
        } catch (IOException x) {
            then(x).hasMessage("not enough bytes (98 missing)");
        }
    }

    @Test
    public void read_bytes_with_invalid_argument() throws Exception {
        PacketInputStream is = new PacketInputStream(new ByteArrayInputStream(
            new byte[] {}
        ));

        try {
            is.readBytes(-10);
            fail("missing sanity check");
        } catch (IllegalArgumentException x) {
            then(x).hasMessage("howMany can not be lesser than 0");
        }
    }

    @Test
    //
    // strings are 0-terminated
    //
    public void read_string_with_enough_data() throws Exception {
        PacketInputStream is = new PacketInputStream(
            new ByteArrayInputStream(
                new byte[] {
                    (byte)0x45, (byte)0x00, (byte)0x4f, (byte)0x00, // ---
                    (byte)0x53, (byte)0x00, (byte)0x34, (byte)0x00, //
                    (byte)0x30, (byte)0x00, (byte)0x30, (byte)0x00, // Host name (EOS4000D)
                    (byte)0x30, (byte)0x00, (byte)0x44, (byte)0x00, //
                    (byte)0x00, (byte)0x00,
                    (byte)0x45, (byte)0x00, (byte)0x4f, (byte)0x00, // ---
                    (byte)0x53, (byte)0x00, (byte)0x31, (byte)0x00, //
                    (byte)0x30, (byte)0x00, (byte)0x30, (byte)0x00, // Host name (EOS1000D)
                    (byte)0x30, (byte)0x00, (byte)0x44, (byte)0x00, //
                    (byte)0x00, (byte)0x00,
                }
            )
        );

        then(is.readString()).isEqualTo("EOS4000D");
        then(is.readString()).isEqualTo("EOS1000D");
    }

    @Test
    public void read_string_with_no_enough_data() throws Exception {
        PacketInputStream is = new PacketInputStream(
            new ByteArrayInputStream(
                new byte[] {
                    (byte)0x45, (byte)0x00, (byte)0x4f, (byte)0x00, // ---
                    (byte)0x53, (byte)0x00, (byte)0x31, (byte)0x00, //
                    (byte)0x30, (byte)0x00, (byte)0x30, (byte)0x00, // Host name (EOS1000D)
                    (byte)0x30, (byte)0x00, (byte)0x44, (byte)0x00  // (missing terminator)
                }
            )
        );

        try {
            is.readString();
            fail("missing io error");
        } catch (IOException x) {
            then(x).hasMessage("string not terminated (eof)");
        }
    }

    @Test
    public void read_version_ok() throws Exception {
        PacketInputStream is = new PacketInputStream(
            new ByteArrayInputStream(
                new byte[] {
                    (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x00, // 1.0
                    (byte)0x01, (byte)0x00, (byte)0x02, (byte)0x00  // 2.1
                }
            )
        );

        then(is.readVersion()).isEqualTo("1.0");
        then(is.readVersion()).isEqualTo("2.1");
    }

    @Test
    public void read_version_not_enout_data() throws Exception {
        PacketInputStream is = new PacketInputStream(
            new ByteArrayInputStream(
                new byte[] {
                    (byte)0x00, (byte)0x00, (byte)0x01
                }
            )
        );

        try {
            is.readVersion();
            fail("mising io error");
        } catch (IOException x) {
            then(x).hasMessage("not enough bytes");
        }

        is = new PacketInputStream(
            new ByteArrayInputStream(
                new byte[] {
                    (byte)0x00
                }
            )
        );

        try {
            is.readVersion();
            fail("mising io error");
        } catch (IOException x) {
            then(x).hasMessage("not enough bytes");
        }
    }
}
