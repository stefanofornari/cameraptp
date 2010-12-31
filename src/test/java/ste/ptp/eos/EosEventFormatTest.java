/* Copyright 2010 by Stefano Fornari
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package ste.ptp.eos;

import junit.framework.TestCase;

/**
 *
 * @author ste
 */
public class EosEventFormatTest extends TestCase {

    private EosEvent e;
    
    public EosEventFormatTest(String testName) {
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

    public void testFormat2Parameters() {
        EosEvent e = new EosEvent();
        
        e.setCode(EosEventConstants.EosEventPropValueChanged);
        e.setParam(1, EosEventConstants.EosPropAperture);
        e.setParam(2, 0x001D);

        String msg = EosEventFormat.format(e);

        assertTrue(msg.indexOf("EosEventPropValueChanged") >= 0);
        assertTrue(msg.indexOf("EosPropAperture") >= 0);
        assertTrue(msg.indexOf("29") >= 0);

        e.setCode(EosEventConstants.EosEventPropValueChanged);
        e.setParam(1, EosEventConstants.EosPropISOSpeed);
        e.setParam(2, 0x0068);

        msg = EosEventFormat.format(e);
        
        assertTrue(msg.indexOf("EosEventPropValueChanged") >= 0);
        assertTrue(msg.indexOf("EosPropISOSpeed") >= 0);
        assertTrue(msg.indexOf("104") >= 0);
    }

    public void testFormat0Prameters() {
        EosEvent e = new EosEvent();

        e.setCode(EosEventConstants.EosEventShutdownTimerUpdated);

        String msg = EosEventFormat.format(e);

        assertTrue(msg.indexOf("EosEventShutdownTimerUpdated") >= 0);
        assertTrue(msg.indexOf('[') < 0);
    }

    public void testPropPictureStyleNoBlackAndWhite() {
        EosEvent e = new EosEvent();

        e.setCode(EosEventConstants.EosEventPropValueChanged);
        e.setParam(2, Boolean.FALSE);
        e.setParam(3, 1);
        e.setParam(4, 2);
        e.setParam(5, 3);
        e.setParam(6, 4);

        for (
            int i = EosEventConstants.EosPropPictureStyleStandard;
            i < EosEventConstants.EosPropPictureStyleUserSet3;
            ++i
        ) {
            if (i == EosEventConstants.EosPropPictureStyleMonochrome) {
                continue;
            }
            e.setParam(1, i);

            String msg = EosEventFormat.format(e);
            System.out.println("msg: " + msg);

            assertTrue(msg.indexOf("Sharpness") >= 0);
            assertTrue(msg.indexOf("Contrast") >= 0);
            assertTrue(msg.indexOf("Saturation") >= 0);
            assertTrue(msg.indexOf("Color") >= 0);
            assertFalse(msg.indexOf("effect") >= 0);
        }
    }

    public void testPropPictureStyleBlackAndWhite() {
        EosEvent e = new EosEvent();

        e.setCode(EosEventConstants.EosEventPropValueChanged);
        e.setParam(1, EosEventConstants.EosPropPictureStyleMonochrome);
        e.setParam(2, Boolean.TRUE);
        e.setParam(3, 1);
        e.setParam(4, 2);
        e.setParam(5, 1);
        e.setParam(6, 2);

        String msg = EosEventFormat.format(e);

        assertTrue(msg.indexOf("Sharpness") >= 0);
        assertTrue(msg.indexOf("Contrast") >= 0);
        assertFalse(msg.indexOf("Saturation") >= 0);
        assertFalse(msg.indexOf("Color") >= 0);
        assertTrue(msg.indexOf("effect") >= 0);
    }

}
