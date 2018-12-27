/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ste.ptp.eos;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import static junit.framework.TestCase.fail;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;
import ste.ptp.PTPUnsupportedException;

/**
 *
 * @author ste
 */
public class BugFreeEosEventParser {

    private static final byte[] EOS_PROP_VALUE_CHANGED = {
      (byte)0x10, (byte)0x00, (byte)0x00, (byte)0x00,
      (byte)0x89, (byte)0xC1, (byte)0x00, (byte)0x00,
      (byte)0x02, (byte)0xD1, (byte)0x00, (byte)0x00,
      (byte)0x6D, (byte)0x00, (byte)0x00, (byte)0x00
    };

    private static final byte[] SHUTDOWN_TIMER_UPDATED = {
      (byte)0x08, (byte)0x00, (byte)0x00, (byte)0x00,
      (byte)0x8E, (byte)0xC1, (byte)0x00, (byte)0x00
    };

    private static final byte[] UNSUPPORTED_EVENT = {
      (byte)0x14, (byte)0x00, (byte)0x00, (byte)0x00,
      (byte)0x88, (byte)0x20, (byte)0x00, (byte)0x00,
      (byte)0x02, (byte)0xD1, (byte)0x00, (byte)0x00,
      (byte)0x6D, (byte)0x00, (byte)0x00, (byte)0x00,
      (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00
    };

    private static final byte[] UNSUP_SUP_EVENT = {
      (byte)0x04, (byte)0x00, (byte)0x00, (byte)0x00,

      (byte)0x10, (byte)0x00, (byte)0x00, (byte)0x00,
      (byte)0x89, (byte)0xC1, (byte)0x00, (byte)0x00,
      (byte)0x02, (byte)0xD1, (byte)0x00, (byte)0x00,
      (byte)0x6D, (byte)0x00, (byte)0x00, (byte)0x00
    };

    @Test
    public void constructor_with_valid_id() {
        new EosEventParser(new ByteArrayInputStream(new byte[0]));
    }

    @Test
    public void constructor_with_invalid_id() {
        try {
            new EosEventParser(null);
            fail("is not checked");
        } catch (IllegalArgumentException e) {
            //
            // OK
            //
        }
    }

    @Test
    public void invalid_initialization() throws Exception {
        EosEventParser parser = null;

        try {
            parser = new EosEventParser(null);
            fail("null parameter must be checked");
        } catch (IllegalArgumentException e) {
            //
            // OK!
            //
        }
    }

    @Test
    public void get_next_string() throws Exception {
        final byte[] BUF1 = new byte[] {
          // (empty string)
          (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00
        };

        final byte[] BUF2 = new byte[] {
          // IMG_1979.CR2
          (byte)0x49, (byte)0x4D, (byte)0x47, (byte)0x5F,
          (byte)0x31, (byte)0x39, (byte)0x37, (byte)0x39,
          (byte)0x2E, (byte)0x43, (byte)0x52, (byte)0x32,
          (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00
        };

        Method m = EosEventParser.class.getDeclaredMethod("getNextString");
        m.setAccessible(true);

        EosEventParser p = new EosEventParser(
                               new ByteArrayInputStream(BUF1)
                           );

        then(m.invoke(p)).isEqualTo("");

        p = new EosEventParser(
                new ByteArrayInputStream(BUF2)
            );
        then(m.invoke(p)).isEqualTo("IMG_1979.CR2");
    }

    @Test
    public void parser_with_events() throws Exception {
        EosEventParser parser = new EosEventParser(
                                    new ByteArrayInputStream(
                                        EOS_PROP_VALUE_CHANGED
                                    )
                                );

        then(parser.hasEvents()).isTrue();
    }

    @Test
    public void parser_without_events() throws Exception {
        EosEventParser parser = new EosEventParser(
                                    new ByteArrayInputStream(
                                        new byte[0]
                                    )
                                );

        then(parser.hasEvents()).isFalse();
    }

    @Test
    public void event_EosEventPropValueChanged() throws Exception {
        EosEventParser parser = new EosEventParser(
                                    new ByteArrayInputStream(
                                        EOS_PROP_VALUE_CHANGED
                                    )
                                );

        EosEvent e = parser.getNextEvent();

        then(e.getCode()).isEqualTo(EosEventConstants.EosEventPropValueChanged);
        then(e.getParamCount()).isEqualTo(2);
        then(e.getIntParam(1)).isEqualTo(EosEventConstants.EosPropShutterSpeed);
        then(e.getIntParam(2)).isEqualTo(0x006D);

    }

    @Test
    public void unsupported_event() throws Exception {
        EosEventParser parser = new EosEventParser(
                                    new ByteArrayInputStream(
                                        UNSUPPORTED_EVENT
                                    )
                                );

        try {
            parser.getNextEvent();
            fail("Unsopported event not cached");
        } catch (PTPUnsupportedException e) {
            //
            // OK!
            //
        }
    }

    @Test
    public void supported_after_unsupported() throws Exception {
        EosEventParser parser = new EosEventParser(
                                    new ByteArrayInputStream(
                                        UNSUP_SUP_EVENT
                                    )
                                );

        try {
            parser.getNextEvent();
            fail("Unsopported event not cached");
        } catch (PTPUnsupportedException e) {
            //
            // OK!
            //
        }

        EosEvent e = parser.getNextEvent();

        then(e.getCode()).isEqualTo(EosEventConstants.EosEventPropValueChanged);
    }

    @Test
    public void zero_parameters_event() throws Exception {
        EosEventParser parser = new EosEventParser(
                                    new ByteArrayInputStream(
                                        SHUTDOWN_TIMER_UPDATED
                                    )
                                );

        EosEvent e = parser.getNextEvent();

        then(e.getCode()).isEqualTo(EosEventConstants.EosEventShutdownTimerUpdated);
        then(e.getParamCount()).isZero();
    }

    @Test
    public void event_EosEventCameraStatusChanged() throws Exception {
        final byte[] EOS_CAMERA_STATUS_CHANGED = {
          (byte)0x0C, (byte)0x00, (byte)0x00, (byte)0x00,
          (byte)0x8B, (byte)0xC1, (byte)0x00, (byte)0x00,
          (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00
        };
        EosEventParser parser = new EosEventParser(
                                    new ByteArrayInputStream(
                                        EOS_CAMERA_STATUS_CHANGED
                                    )
                                );

        EosEvent e = parser.getNextEvent();

        then(e.getCode()).isEqualTo(EosEventConstants.EosEventCameraStatusChanged);
        then(e.getParamCount()).isEqualTo(1);
        then(e.getIntParam(1)).isEqualTo(0x01);
    }

    @Test
    public void prop_value_changed_picture_style_default()
    throws Exception {
        final byte[] BUF = {
          (byte)0x28, (byte)0x00, (byte)0x00, (byte)0x00,
          (byte)0x89, (byte)0xC1, (byte)0x00, (byte)0x00,
          (byte)0x50, (byte)0xD1, (byte)0x00, (byte)0x00,
          // length
          (byte)0x1C, (byte)0x00, (byte)0x00, (byte)0x00,
          // contrast (-4..+4)
          (byte)0x03, (byte)0x00, (byte)0x00, (byte)0x00,
          // sharpness (1..7)
          (byte)0x07, (byte)0x00, (byte)0x00, (byte)0x00,
          // saturation (-4..+4)
          (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF,
          // color tone (-4..+4)
          (byte)0x04, (byte)0x00, (byte)0x00, (byte)0x00,
          // filter effect (0:None, 1:Yellow, 2:Orange, 3:Red, 4:Green)
          (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
          // toning effect (0:None, 1:Sepia, 2:Blue, 3:Purple, 4:Green)
          (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00
        };

        //
        // Let's do Standard, Portrait and BN
        //


        //
        // Standard
        //
        EosEventParser parser = new EosEventParser(
                                    new ByteArrayInputStream(BUF)
                                );

        EosEvent e = parser.getNextEvent();

        then(e.getCode()).isEqualTo(EosEventConstants.EosEventPropValueChanged);
        then(e.getParamCount()).isEqualTo(6);
        then(e.getIntParam(1)).isEqualTo(EosEventConstants.EosPropPictureStyleStandard);
        then((Boolean)e.getParam(2)).isFalse();
        then(e.getIntParam(3)).isEqualTo(3);
        then(e.getIntParam(4)).isEqualTo(7);
        then(e.getIntParam(5)).isEqualTo(-1);
        then(e.getIntParam(6)).isEqualTo(4);

        //
        // Portrait
        //
        BUF[8] = 0x51; // EosPropPictureStylePortrait
        parser = new EosEventParser(
                                    new ByteArrayInputStream(BUF)
                                );

        e = parser.getNextEvent();

        then(e.getCode()).isEqualTo(EosEventConstants.EosEventPropValueChanged);
        then(e.getParamCount()).isEqualTo(6);
        then(e.getIntParam(1)).isEqualTo(EosEventConstants.EosPropPictureStylePortrait);
        then((Boolean)e.getParam(2)).isFalse();
        then(e.getIntParam(3)).isEqualTo(3);
        then(e.getIntParam(4)).isEqualTo(7);
        then(e.getIntParam(5)).isEqualTo(-1);
        then(e.getIntParam(6)).isEqualTo(4);

        //
        // B/N
        //
        BUF[8] = 0x55; // EosPropPictureStylePortrait
        BUF[32] = 0x03;
        BUF[36] = 0x01;
        parser = new EosEventParser(
                                    new ByteArrayInputStream(BUF)
                                );

        e = parser.getNextEvent();

        then(e.getCode()).isEqualTo(EosEventConstants.EosEventPropValueChanged);
        then(e.getParamCount()).isEqualTo(6);
        then(e.getIntParam(1)).isEqualTo(EosEventConstants.EosPropPictureStyleMonochrome);
        then((Boolean)e.getParam(2)).isTrue();
        then(e.getIntParam(3)).isEqualTo(3);
        then(e.getIntParam(4)).isEqualTo(7);
        then(e.getIntParam(5)).isEqualTo(3);
        then(e.getIntParam(6)).isEqualTo(1);

    }

    @Test
    public void prop_value_changed_picture_style_default_user()
    throws Exception {
        final byte[] BUF1 = {
          (byte)0x2C, (byte)0x00, (byte)0x00, (byte)0x00,
          (byte)0x89, (byte)0xC1, (byte)0x00, (byte)0x00,
          (byte)0x60, (byte)0xD1, (byte)0x00, (byte)0x00,
          // length
          (byte)0x20, (byte)0x00, (byte)0x00, (byte)0x00,
          // type (0x81:Standard, 0x82:Portrait, 0x83:Landscape, 0x84:Neutral, 0x85:Faithful, 0x86:Monochrome)
          (byte)0x81, (byte)0x00, (byte)0x00, (byte)0x00,
          // contrast (-4..+4)
          (byte)0x03, (byte)0x00, (byte)0x00, (byte)0x00,
          // sharpness (1..7)
          (byte)0x07, (byte)0x00, (byte)0x00, (byte)0x00,
          // saturation (-4..+4)
          (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF,
          // color tone (-4..+4)
          (byte)0x04, (byte)0x00, (byte)0x00, (byte)0x00,
          // filter effect (0:None, 1:Yellow, 2:Orange, 3:Red, 4:Green)
          (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
          // toning effect (0:None, 1:Sepia, 2:Blue, 3:Purple, 4:Green)
          (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00
        };

        final byte[] BUF2 = {
          (byte)0x2C, (byte)0x00, (byte)0x00, (byte)0x00,
          (byte)0x89, (byte)0xC1, (byte)0x00, (byte)0x00,
          (byte)0x61, (byte)0xD1, (byte)0x00, (byte)0x00,
          // length
          (byte)0x20, (byte)0x00, (byte)0x00, (byte)0x00,
          // type (0x81:Standard, 0x82:Portrait, 0x83:Landscape, 0x84:Neutral, 0x85:Faithful, 0x86:Monochrome)
          (byte)0x86, (byte)0x00, (byte)0x00, (byte)0x00,
          // contrast (-4..+4)
          (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
          // sharpness (1..7)
          (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
          // saturation (-4..+4)
          (byte)0x00, (byte)0x0, (byte)0x00, (byte)0x00,
          // color tone (-4..+4)
          (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
          // filter effect (0:None, 1:Yellow, 2:Orange, 3:Red, 4:Green)
          (byte)0x03, (byte)0x00, (byte)0x00, (byte)0x00,
          // toning effect (0:None, 1:Sepia, 2:Blue, 3:Purple, 4:Green)
          (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00
        };

        //
        // Let's do UserSet1/Standard and UserSet2/Monochromoe
        //

        //
        // UserSet1 - Standard
        //
        EosEventParser parser = new EosEventParser(
                                    new ByteArrayInputStream(BUF1)
                                );

        EosEvent e = parser.getNextEvent();

        then(e.getCode()).isEqualTo(EosEventConstants.EosEventPropValueChanged);
        then(e.getParamCount()).isEqualTo(6);
        then(e.getIntParam(1)).isEqualTo(EosEventConstants.EosPropPictureStyleUserSet1);
        then((Boolean)e.getParam(2)).isFalse();
        then(e.getIntParam(3)).isEqualTo(3);
        then(e.getIntParam(4)).isEqualTo(7);
        then(e.getIntParam(5)).isEqualTo(-1);
        then(e.getIntParam(6)).isEqualTo(4);

        //
        // UserSet2 - Monochrome
        //
        parser = new EosEventParser(
                                    new ByteArrayInputStream(BUF2)
                                );

        e = parser.getNextEvent();

        then(e.getCode()).isEqualTo(EosEventConstants.EosEventPropValueChanged);
        then(e.getParamCount()).isEqualTo(6);
        then(e.getIntParam(1)).isEqualTo(EosEventConstants.EosPropPictureStyleUserSet2);
        then((Boolean)e.getParam(2)).isTrue();
        then(e.getIntParam(3)).isEqualTo(0);
        then(e.getIntParam(4)).isEqualTo(0);
        then(e.getIntParam(5)).isEqualTo(3);
        then(e.getIntParam(6)).isEqualTo(1);
    }

    @Test
    public void event_EosEventObjectAddedEx() throws Exception {
        final byte[] BUF = {
          (byte)0x3C, (byte)0x00, (byte)0x00, (byte)0x00,
          (byte)0x81, (byte)0xC1, (byte)0x00, (byte)0x00,
          // object ID
          (byte)0xB1, (byte)0x7B, (byte)0x90, (byte)0x91,
          // storage ID
          (byte)0x01, (byte)0x00, (byte)0x02, (byte)0x00,
          // format
          (byte)0x03, (byte)0xB1,
          // unknown
          (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
          (byte)0x00, (byte)0x00, (byte)0x20, (byte)0x00,
          (byte)0x00, (byte)0x00,
          // object size
          (byte)0xE5, (byte)0x0B, (byte)0x88, (byte)0x00,
          // parent obejct id
          (byte)0x00, (byte)0x00, (byte)0x90, (byte)0x91,
          // unknown (it looks like an ID)
          (byte)0xB0, (byte)0x7B, (byte)0x90, (byte)0x91,
          // filename (IMG_1979.CR2)
          (byte)0x49, (byte)0x4D, (byte)0x47, (byte)0x5F,
          (byte)0x31, (byte)0x39, (byte)0x37, (byte)0x39,
          (byte)0x2E, (byte)0x43, (byte)0x52, (byte)0x32,
          (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
          // unknown
          (byte)0x2E, (byte)0x30, (byte)0x1E, (byte)0x4D
        };

        EosEventParser parser = new EosEventParser(
                                    new ByteArrayInputStream(BUF)
                                );

        EosEvent e = parser.getNextEvent();

        then(e.getCode()).isEqualTo(EosEventConstants.EosEventObjectAddedEx);
        then(e.getIntParam(1)   ).isEqualTo(0x91907BB1    ); // object ID
        then(e.getIntParam(2)   ).isEqualTo(0x020001      ); // storage ID
        then(e.getIntParam(3)   ).isEqualTo(0x91900000    ); // parent ID
        then(e.getIntParam(4)   ).isEqualTo(0xB103        ); // format
        then(e.getIntParam(5)   ).isEqualTo(0x00880BE5    ); // size
        then(e.getStringParam(6)).isEqualTo("IMG_1979.CR2"); // fiel name
    }

}
