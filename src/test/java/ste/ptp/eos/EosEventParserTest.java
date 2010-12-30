/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ste.ptp.eos;

import java.io.ByteArrayInputStream;
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

    public void testParsePropValueChangedPictureStyleStandard()
    throws Exception {
        final byte[] BUF = {
          (byte)0x28, (byte)0x00, (byte)0x00, (byte)0x00,
          (byte)0x89, (byte)0xC1, (byte)0x00, (byte)0x00,
          (byte)0x50, (byte)0xD1, (byte)0x00, (byte)0x00,
          (byte)0x1C, (byte)0x00, (byte)0x00, (byte)0x00,
          (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
          (byte)0x03, (byte)0x00, (byte)0x00, (byte)0x00,
          (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
          (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
          (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
          (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00
        };

        EosEventParser parser = new EosEventParser(
                                    new ByteArrayInputStream(BUF)
                                );

        EosEvent e = parser.getNextEvent();

        assertEquals(EosEventConstants.EosEventPropValueChanged, e.getCode());
        assertEquals(5, e.getParamCount());
        assertEquals(EosEventConstants.EosPropPictureStyleStandard, e.getIntParam(1));
        //
        // TODO: check the other parameters
        //
    }

}
