/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ste.ptp.usb;

import javax.usb.UsbConst;

/**
 *
 * @author ste
 */
public class USBUtils {

    public static boolean isBulkType(byte type) {
        return ((type & UsbConst.ENDPOINT_TYPE_BULK) > 0);
    }

    public static boolean isInterruptType(byte type) {
        return ((type & UsbConst.ENDPOINT_TYPE_INTERRUPT) > 0);
    }

    public static boolean isInputDirection(byte direction) {
        return ((direction & UsbConst.ENDPOINT_DIRECTION_IN) >0);
    }

}
