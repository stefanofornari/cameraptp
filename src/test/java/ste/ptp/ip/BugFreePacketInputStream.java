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
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;
import ste.ptp.Command;

/**
 *
 * @author ste
 */
public class BugFreePacketInputStream {

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
    public void read_init_command_request() throws Exception {
        final ByteArrayInputStream IS = new ByteArrayInputStream(
            new byte[] {
                (byte)0xab, (byte)0xad, (byte)0xa5, (byte)0xad, // ---
                (byte)0xa8, (byte)0xaa, (byte)0xa9, (byte)0xac, // 16 bytes GUID
                (byte)0xaf, (byte)0xa4, (byte)0xa0, (byte)0xab, //
                (byte)0xa6, (byte)0xa9, (byte)0xa5, (byte)0xae, // ---
                (byte)0x53, (byte)0x00, (byte)0x4d, (byte)0x00, // ---
                (byte)0x2d, (byte)0x00, (byte)0x4a, (byte)0x00, //
                (byte)0x33, (byte)0x00, (byte)0x32, (byte)0x00, // Hostname (SM-J320FN)
                (byte)0x30, (byte)0x00, (byte)0x46, (byte)0x00, //
                (byte)0x4e, (byte)0x00, (byte)0x00, (byte)0x00, // ---
                (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x00,  // version 1.0
                (byte)0xbb, (byte)0xbd, (byte)0xb5, (byte)0xbd, // ---
                (byte)0xba, (byte)0xba, (byte)0xb9, (byte)0xbc, // 16 bytes GUID
                (byte)0xbf, (byte)0xb4, (byte)0xb0, (byte)0xbb, //
                (byte)0xb6, (byte)0xb9, (byte)0xb5, (byte)0xbe, // ---
                (byte)0x53, (byte)0x00, (byte)0x4d, (byte)0x00, // ---
                (byte)0x2d, (byte)0x00, (byte)0x4b, (byte)0x00, //
                (byte)0x33, (byte)0x00, (byte)0x32, (byte)0x00, // Hostname (SM-J320FN)
                (byte)0x30, (byte)0x00, (byte)0x45, (byte)0x00, //
                (byte)0x4f, (byte)0x00, (byte)0x00, (byte)0x00, // ---
                (byte)0x01, (byte)0x00, (byte)0x01, (byte)0x00  // version 1.0
            }
        );

        PacketInputStream is = new PacketInputStream(IS);
        InitCommandRequest req = is.readInitCommandRequest();
        then(req.guid).isEqualTo(new byte[] {
            (byte)0xab, (byte)0xad, (byte)0xa5, (byte)0xad,
            (byte)0xa8, (byte)0xaa, (byte)0xa9, (byte)0xac,
            (byte)0xaf, (byte)0xa4, (byte)0xa0, (byte)0xab,
            (byte)0xa6, (byte)0xa9, (byte)0xa5, (byte)0xae
        });
        then(req.hostname).isEqualTo("SM-J320FN");
        then(req.version).isEqualTo("1.0");

        req = is.readInitCommandRequest();
        then(req.guid).isEqualTo(new byte[] {
            (byte)0xbb, (byte)0xbd, (byte)0xb5, (byte)0xbd, // ---
            (byte)0xba, (byte)0xba, (byte)0xb9, (byte)0xbc, // 16 bytes GUID
            (byte)0xbf, (byte)0xb4, (byte)0xb0, (byte)0xbb, //
            (byte)0xb6, (byte)0xb9, (byte)0xb5, (byte)0xbe
        });
        then(req.hostname).isEqualTo("SM-K320EO");
        then(req.version).isEqualTo("1.1");
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
        then(ack.sessionId).isEqualTo(1);
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
    public void read_init_event_request() throws Exception {
        final ByteArrayInputStream IS = new ByteArrayInputStream(
            new byte[] {
                (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, // --- session id 1
                (byte)0xaa, (byte)0xbb, (byte)0xcc, (byte)0xdd, // --- session id 2
                (byte)0x00, (byte)0x11                          // --- not enough data
            }
        );

        PacketInputStream is = new PacketInputStream(IS);
        then(is.readInitEventRequest().sessionId).isEqualTo(0x04030201);
        then(is.readInitEventRequest().sessionId).isEqualTo(0xddccbbaa);

        try {
            is.readInitEventRequest();
            fail("no io error");
        } catch (IOException x) {
            then(x).hasMessage("not enough bytes (2 missing)");
        }
    }

    @Test
    public void read_init_evet_acknowledge() throws Exception {
        //
        // the packet init_event_acknowledge is empty...
        //
        final ByteArrayInputStream IS = new ByteArrayInputStream(
            new byte[] { 0x01, 0x02, 0x03 }
        );

        PacketInputStream is = new PacketInputStream(IS);
        is.readInitEventAcknowledge();  // no errors is enough

        then(IS.read()).isEqualTo(0x01);
        then(IS.read()).isEqualTo(0x02);
        then(IS.read()).isEqualTo(0x03);
    }

    @Test
    public void read_init_error() throws Exception {
        final ByteArrayInputStream IS = new ByteArrayInputStream(
            new byte[] {
                (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, // --- error code 1
                (byte)0xaa, (byte)0xbb, (byte)0xcc, (byte)0xdd, // --- error code 2
                (byte)0x00                                      // --- not enough data
            }
        );

        PacketInputStream is = new PacketInputStream(IS);
        then(is.readInitError().error).isEqualTo(0x01020304);
        then(is.readInitError().error).isEqualTo(0xaabbccdd);

        try {
            is.readInitError();
            fail("no io error");
        } catch (IOException x) {
             then(x).hasMessage("not enough bytes (3 missing)");
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

    @Test
    public void read_operation_request_ok() throws Exception {
        final ByteArrayInputStream IS = new ByteArrayInputStream(
            new byte[] {
                //
                // 1st
                //
                (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, // --- data phase info
                (byte)0x01, (byte)0x10,                         // --- operation code
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, // --- transaction id
                //
                // 2nd
                //
                (byte)0x01, (byte)0x20, (byte)0x00, (byte)0x00, // --- data phase info
                (byte)0x02, (byte)0x20,                         // --- operation code
                (byte)0x01, (byte)0x00, (byte)0x02, (byte)0x00  // --- transaction id
            }
        );

        PacketInputStream is = new PacketInputStream(IS);

        OperationRequest or = is.readOperationRequest();
        then(or.code).isEqualTo(0x1001);
        then(or.dataPhaseInfo).isEqualTo(1);
        then(or.transaction).isEqualTo(0);

        or = is.readOperationRequest();
        then(or.code).isEqualTo(0x2002);
        then(or.dataPhaseInfo).isEqualTo(0x00002001);
        then(or.transaction).isEqualTo(0x00020001);

        try {
            is.readOperationRequest();
            fail("no io error");
        } catch (IOException x) {
            then(x).hasMessage("not enough bytes (4 missing)");
        }
    }

    @Test
    public void read_generic_packets() throws Exception {
        final ByteArrayOutputStream OS = new ByteArrayOutputStream();
        final InitCommandRequest CR = new InitCommandRequest(GUID1, "mypc1", "1.0");
        final InitCommandAcknowledge CA = new InitCommandAcknowledge(0x00010203, GUID2, "mypc2", "1.1");
        final InitEventAcknowledge EA = new InitEventAcknowledge();
        final InitError E = new InitError(0x0a0b);
        final OperationRequest OR = new OperationRequest(Command.GetDeviceInfo);

        PacketOutputStream out = new PacketOutputStream(OS);
        out.write(new PTPIPContainer(CR));
        out.write(new PTPIPContainer(CA));
        out.write(new PTPIPContainer(EA));
        out.write(new PTPIPContainer(E));
        out.write(new PTPIPContainer(OR));
        //
        // unknown package
        out.writeLEInt(0x0000001d); out.writeLEInt(0x00100010);
        out.write(new byte[] {(byte)0x00, (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x05});
        //

        out.flush();

        PacketInputStream is = new PacketInputStream(
            new ByteArrayInputStream(OS.toByteArray())
        );

        then(is.readPTPContainer().payload).isInstanceOf(InitCommandRequest.class);
        then(is.readPTPContainer().payload).isInstanceOf(InitCommandAcknowledge.class);
        then(is.readPTPContainer().payload).isInstanceOf(InitEventAcknowledge.class);
        then(is.readPTPContainer().payload).isInstanceOf(InitError.class);
        then(is.readPTPContainer().payload).isInstanceOf(OperationRequest.class);

        try {
            is.readPTPContainer();
            fail("missing unknown package error");
        } catch (IOException x) {
            then(x).hasMessage("unknown package 0x00100010 (size: 29 bytes)");
        }

    }
}
