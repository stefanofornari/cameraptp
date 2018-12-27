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

import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;

/**
 *
 * @author ste
 */
public class BugFreeEosEventFormat {

    private EosEvent e;

    @Test
    public void format_2_parameters() {
        EosEvent e = new EosEvent();

        e.setCode(EosEventConstants.EosEventPropValueChanged);
        e.setParam(1, EosEventConstants.EosPropAperture);
        e.setParam(2, 0x001D);

        String msg = EosEventFormat.format(e);

        then(msg).contains("EosEventPropValueChanged")
                 .contains("Aperture")
                 .contains("29");

        e.setCode(EosEventConstants.EosEventPropValueChanged);
        e.setParam(1, EosEventConstants.EosPropISOSpeed);
        e.setParam(2, 0x0068);

        msg = EosEventFormat.format(e);

        then(msg).contains("EosEventPropValueChanged")
                 .contains("ISOSpeed")
                 .contains("104");
    }

    @Test
    public void format_0_parameters() {
        EosEvent e = new EosEvent();

        e.setCode(EosEventConstants.EosEventShutdownTimerUpdated);

        String msg = EosEventFormat.format(e);

        then(msg).contains("EosEventShutdownTimerUpdated");
    }

    @Test
    public void prop_picture_style_no_black_and_white() {
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

            then(msg).contains("Sharpness")
                     .contains("Contrast")
                     .contains("Saturation")
                     .contains("Color")
                     .doesNotContain("effect");
        }
    }

    @Test
    public void prop_picture_style_black_and_white() {
        EosEvent e = new EosEvent();

        e.setCode(EosEventConstants.EosEventPropValueChanged);
        e.setParam(1, EosEventConstants.EosPropPictureStyleMonochrome);
        e.setParam(2, Boolean.TRUE);
        e.setParam(3, 1);
        e.setParam(4, 2);
        e.setParam(5, 1);
        e.setParam(6, 2);

        String msg = EosEventFormat.format(e);

        then(msg).contains("Sharpness")
                 .contains("Contrast")
                 .doesNotContain("Saturation")
                 .doesNotContain("Color")
                 .contains("effect");
    }

    @Test
    public void eos_EventObjectAddedEx() {
        EosEvent e = new EosEvent();

        e.setCode(EosEventConstants.EosEventObjectAddedEx);
        e.setParam(1, 1);
        e.setParam(2, 2);
        e.setParam(3, 3);
        e.setParam(4, EosEventConstants.ImageFormatCANON_CRW3);
        e.setParam(5, 5);
        e.setParam(6, "IMG_1979.CR2");

        String msg = EosEventFormat.format(e);

        then(msg).contains("EosEventObjectAddedEx")
                 .contains("0x00000001")
                 .contains("0x00000002")
                 .contains("0x00000003")
                 .contains("CANON_CRW3")
                 .contains("5")
                 .contains("IMG_1979.CR2");
    }

    @Test
    public void get_image_format_name() {
        final String[] NAMES = new String[] { "EXIF_JPEG", "CANON_CRW", "PNG", "Unknown" };
        final int[]    CODES = new int[]    { 0x3801, 0xB101, 0x380B, 0x0001 };

        for (int i=0; i<NAMES.length; ++i) {
            then(EosEventFormat.getImageFormatName(CODES[i])).isEqualTo(NAMES[i]);
        }
    }

}
