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

import java.lang.reflect.Field;

/**
 * This class formats an EosEvent to a string
 *
 * @author stefano fornari
 */
public class EosEventFormat implements EosEventConstants {
    public static String format(EosEvent e) {
        StringBuilder sb = new StringBuilder();

        sb.append(getEventName(e.getCode()))
          .append(" [")
          .append(getPropertyName(e.getIntParam(1)))
          .append(": ")
          .append(e.getIntParam(2))
          .append("]");
          ;

        return sb.toString();
    }

    /**
     * Returns the printable name of the given event
     *
     * @param code event code
     *
     * @return the printable name of the given event
     */
    public static String getEventName(int code) {
        Field[] fields = EosEventConstants.class.getDeclaredFields();

        for (Field f: fields) {
            String name = f.getName();
            if (name.startsWith("EosEvent")) {
                try {
                    if (f.getInt(null) == code) {
                        return name;
                    }
                } catch (Exception e) {
                    //
                    // Nothing to do
                    //
                }
            }
        }
        return "Unknown";
    }

    /**
     * Returns the printable name of the given property
     *
     * @param code property code
     *
     * @return the printable name of the given property
     */
    public static String getPropertyName(int code) {
        Field[] fields = EosEventConstants.class.getDeclaredFields();

        for (Field f: fields) {
            String name = f.getName();
            if (name.startsWith("EosProp")) {
                try {
                    if (f.getInt(null) == code) {
                        return name;
                    }
                } catch (Exception e) {
                    //
                    // Nothing to do
                    //
                }
            }
        }
        return "Unknown";
    }
            
}
