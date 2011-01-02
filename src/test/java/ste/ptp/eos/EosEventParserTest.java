/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ste.ptp.eos;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import junit.framework.TestCase;
import ste.ptp.PTPUnsupportedException;

/**
 *
 * @author ste
 */
public class EosEventParserTest extends TestCase {

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
    
    public EosEventParserTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testConstructorValidIS() {
        new EosEventParser(new ByteArrayInputStream(new byte[0]));
    }

    public void testConstructorInValidIS() {
        try {
            new EosEventParser(null);
            fail("is not checked");
        } catch (IllegalArgumentException e) {
            //
            // OK
            //
        }
    }

    public void testInvalidInitialization() throws Exception {
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

    public void testGetNextString() throws Exception {
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

        assertEquals("", m.invoke(p));

        p = new EosEventParser(
                new ByteArrayInputStream(BUF2)
            );
        assertEquals("IMG_1979.CR2", m.invoke(p));
    }

    public void testHasEvents() throws Exception {
        EosEventParser parser = new EosEventParser(
                                    new ByteArrayInputStream(
                                        EOS_PROP_VALUE_CHANGED
                                    )
                                );

        assertTrue(parser.hasEvents());
    }

    public void testHasNotEvents() throws Exception {
        EosEventParser parser = new EosEventParser(
                                    new ByteArrayInputStream(
                                        new byte[0]
                                    )
                                );

        assertFalse(parser.hasEvents());
    }

    public void testParseEventPropValueChanged() throws Exception {
        EosEventParser parser = new EosEventParser(
                                    new ByteArrayInputStream(
                                        EOS_PROP_VALUE_CHANGED
                                    )
                                );

        EosEvent e = parser.getNextEvent();

        assertEquals(EosEventConstants.EosEventPropValueChanged, e.getCode());
        assertEquals(2, e.getParamCount());
        assertEquals(EosEventConstants.EosPropShutterSpeed, e.getIntParam(1));
        assertEquals(0x006D, e.getIntParam(2));

    }

    public void testUnsupportedEvent() throws Exception {
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

    public void testSupportedAfterUnsupportedEvent() throws Exception {
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

        assertEquals(EosEventConstants.EosEventPropValueChanged, e.getCode());
    }

    public void testZeroParametersEvents() throws Exception {
        EosEventParser parser = new EosEventParser(
                                    new ByteArrayInputStream(
                                        SHUTDOWN_TIMER_UPDATED
                                    )
                                );

        EosEvent e = parser.getNextEvent();

        assertEquals(EosEventConstants.EosEventShutdownTimerUpdated, e.getCode());
        assertEquals(0, e.getParamCount());
    }

    public void testCameraStatusChanged() throws Exception {
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

        assertEquals(EosEventConstants.EosEventCameraStatusChanged, e.getCode());
        assertEquals(1, e.getParamCount());
        assertEquals(0x01, e.getIntParam(1));
    }

    public void testParsePropValueChangedPictureStyleDefault()
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

        assertEquals(EosEventConstants.EosEventPropValueChanged, e.getCode());
        assertEquals(6, e.getParamCount());
        assertEquals(EosEventConstants.EosPropPictureStyleStandard, e.getIntParam(1));
        assertFalse((Boolean)e.getParam(2));
        assertEquals( 3, e.getIntParam(3));
        assertEquals( 7, e.getIntParam(4));
        assertEquals(-1, e.getIntParam(5));
        assertEquals( 4, e.getIntParam(6));

        //
        // Portrait
        //
        BUF[8] = 0x51; // EosPropPictureStylePortrait
        parser = new EosEventParser(
                                    new ByteArrayInputStream(BUF)
                                );

        e = parser.getNextEvent();

        assertEquals(EosEventConstants.EosEventPropValueChanged, e.getCode());
        assertEquals(6, e.getParamCount());
        assertEquals(EosEventConstants.EosPropPictureStylePortrait, e.getIntParam(1));
        assertFalse((Boolean)e.getParam(2));
        assertEquals(3, e.getIntParam(3));
        assertEquals(7, e.getIntParam(4));
        assertEquals(-1, e.getIntParam(5));
        assertEquals(4, e.getIntParam(6));

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

        assertEquals(EosEventConstants.EosEventPropValueChanged, e.getCode());
        assertEquals(6, e.getParamCount());
        assertEquals(EosEventConstants.EosPropPictureStyleMonochrome, e.getIntParam(1));
        assertTrue((Boolean)e.getParam(2));
        assertEquals(3, e.getIntParam(3));
        assertEquals(7, e.getIntParam(4));
        assertEquals(3, e.getIntParam(5));
        assertEquals(1, e.getIntParam(6));
    }

    public void testParsePropValueChangedPictureStyleUser()
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

        assertEquals(EosEventConstants.EosEventPropValueChanged, e.getCode());
        assertEquals(6, e.getParamCount());
        assertEquals(EosEventConstants.EosPropPictureStyleUserSet1, e.getIntParam(1));
        assertFalse((Boolean)e.getParam(2));
        assertEquals( 3, e.getIntParam(3));
        assertEquals( 7, e.getIntParam(4));
        assertEquals(-1, e.getIntParam(5));
        assertEquals( 4, e.getIntParam(6));

        //
        // UserSet2 - Monochrome
        //
        parser = new EosEventParser(
                                    new ByteArrayInputStream(BUF2)
                                );

        e = parser.getNextEvent();

        assertEquals(EosEventConstants.EosEventPropValueChanged, e.getCode());
        assertEquals(6, e.getParamCount());
        assertEquals(EosEventConstants.EosPropPictureStyleUserSet2, e.getIntParam(1));
        assertTrue((Boolean)e.getParam(2));
        assertEquals(0, e.getIntParam(3));
        assertEquals(0, e.getIntParam(4));
        assertEquals(3, e.getIntParam(5));
        assertEquals(1, e.getIntParam(6));
    }

    public void testEosEventObjectAddedEx() throws Exception {
        final byte[] BUF = {
          (byte)0x3C, (byte)0x00, (byte)0x00, (byte)0x00,
          (byte)0x81, (byte)0xC1, (byte)0x00, (byte)0x00,
          // object ID
          (byte)0xB1, (byte)0x7B, (byte)0x90, (byte)0x91,
          // storage ID
          (byte)0x01, (byte)0x00,
          // parent ID
          (byte)0x02, (byte)0x00,
          // format
          (byte)0x03, (byte)0xB1, (byte)0x00, (byte)0x00,
          // unknown
          (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
          // length
          (byte)0x20, (byte)0x00, (byte)0x00, (byte)0x00,
          // object size (?)
          (byte)0xE5, (byte)0x0B, (byte)0x88, (byte)0x00,
          // unknown
          (byte)0x00, (byte)0x00, (byte)0x90, (byte)0x91,
          (byte)0xB0, (byte)0x7B, (byte)0x90, (byte)0x91,
          // filename (IMG_1979.CR2)
          (byte)0x49, (byte)0x4D, (byte)0x47, (byte)0x5F,
          (byte)0x31, (byte)0x39, (byte)0x37, (byte)0x39,
          (byte)0x2E, (byte)0x43, (byte)0x52, (byte)0x32,
          (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
          (byte)0x2E, (byte)0x30, (byte)0x1E, (byte)0x4D
        };

        EosEventParser parser = new EosEventParser(
                                    new ByteArrayInputStream(BUF)
                                );

        EosEvent e = parser.getNextEvent();

        assertEquals(EosEventConstants.EosEventObjectAddedEx, e.getCode());
        assertEquals(0x91907BB1,     e.getIntParam(1)   ); // object ID
        assertEquals(0x01,           e.getIntParam(2)   ); // storage ID
        assertEquals(0x02,           e.getIntParam(3)   ); // parent ID
        assertEquals(0xB103,         e.getIntParam(4)   ); // format
        assertEquals(0x00880BE5,     e.getIntParam(5)   ); // size
        assertEquals("IMG_1979.CR2", e.getStringParam(6)); // fiel name
    }

}
