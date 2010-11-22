// Copyright 2000 by David Brownell <dbrownell@users.sourceforge.net>
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
//
package ste.ptp;

/**
 * Command messages start PTP transactions, and are sent from
 * initiator to responder.  They include an operation code,
 * either conform to chapter 10 of the PTP specification or
 * are vendor-specific commands.
 *
 * <p> Create these objects in helper routines which package
 * intelligence about a given Operation.  That is, it'll know
 * the command code, how many command and response parameters
 * may be used, particularly significant response code, and
 * whether the transaction has a data phase (and its direction). 
 *
 * @version $Id: Command.java,v 1.3 2001/04/12 23:13:00 dbrownell Exp $
 * @author David Brownell
 */
public class Command extends ParamVector {

    private Command(int nparams, int code, Session s) {
        super(new byte[HDR_LEN + (4 * nparams)], s.getFactory());
        putHeader(data.length, 1 /*OperationCode*/, code, s.getNextXID());
    }

    /**
     * This creates a zero-parameter command.
     * @param code as defined in section 10, table 18
     * @param s session this command is associated with
     */
    Command(int code, Session s) {
        this(0, code, s);
    }

    /**
     * This creates a one-parameter command.
     * @param code as defined in section 10, table 18
     * @param s session this command is associated with
     * @param param1 first operation parameter
     */
    Command(int code, Session s, int param1) {
        this(1, code, s);
        put32(param1);
    }

    /**
     * This creates a two-parameter command.
     * @param code as defined in section 10, table 18
     * @param s session this command is associated with
     * @param param1 first operation parameter
     * @param param2 second operation parameter
     */
    Command(int code, Session s, int param1, int param2) {
        this(2, code, s);
        put32(param1);
        put32(param2);
    }

    /**
     * This creates a three-parameter command.
     * @param code as defined in section 10, table 18
     * @param s session this command is associated with
     * @param param1 first operation parameter
     * @param param2 second operation parameter
     * @param param3 third operation parameter
     */
    Command(int code, Session s, int param1, int param2, int param3) {
        this(3, code, s);
        put32(param1);
        put32(param2);
        put32(param3);
    }
    // allegedly some commands could have up to five params
    public static final int GetDeviceInfo          = 0x1001;
    public static final int OpenSession            = 0x1002;
    public static final int CloseSession           = 0x1003;
    public static final int GetStorageIDs          = 0x1004;
    public static final int GetStorageInfo         = 0x1005;
    public static final int GetNumObjects          = 0x1006;
    public static final int GetObjectHandles       = 0x1007;
    public static final int GetObjectInfo          = 0x1008;
    public static final int GetObject              = 0x1009;
    public static final int GetThumb               = 0x100a;
    public static final int DeleteObject           = 0x100b;
    public static final int SendObjectInfo         = 0x100c;
    public static final int SendObject             = 0x100d;
    public static final int InitiateCapture        = 0x100e;
    public static final int FormatStore            = 0x100f;
    public static final int ResetDevice            = 0x1010;
    public static final int SelfTest               = 0x1011;
    public static final int SetObjectProtection    = 0x1012;
    public static final int PowerDown              = 0x1013;
    public static final int GetDevicePropDesc      = 0x1014;
    public static final int GetDevicePropValue     = 0x1015;
    public static final int SetDevicePropValue     = 0x1016;
    public static final int ResetDevicePropValue   = 0x1017;
    public static final int TerminateOpenCapture   = 0x1018;
    public static final int MoveObject             = 0x1019;
    public static final int CopyObject             = 0x101a;
    public static final int GetPartialObject       = 0x101b;
    public static final int InitiateOpenCapture    = 0x101c;
    public static final int DsGetStorageIds        = 0x9101;
    public static final int DsGetStorageInfo       = 0x9102;
    public static final int DsGetObjectInfo        = 0x9103;
    public static final int DsGetObject            = 0x9104;
    public static final int DsDeleteObject         = 0x9105;
    public static final int DsFormatStore          = 0x9106;
    public static final int DsGetPartialObject     = 0x9107;
    public static final int DsGetDeviceInfoEx      = 0x9108;
    public static final int DsGetObjectInfoEx      = 0x9109;
    public static final int DsGetThumbEx           = 0x910a;
    public static final int DsSendPartialObject    = 0x910b;
    public static final int DsSetObjectProperties  = 0x910c;
    public static final int DsGetObjectTime        = 0x910d;
    public static final int DsSetObjectTime        = 0x910e;
    public static final int DsRemoteRelease        = 0x910f;
    public static final int DsSetDevicePropValueEx = 0x9110;
    public static final int DsSendObjectEx         = 0x9111;
    public static final int DsCreageObject         = 0x9112;
    public static final int DsGetRemoteMode        = 0x9113;
    public static final int DsSetRemoteMode        = 0x9114;
    public static final int DsGetEvent             = 0x9116;
    public static final int DsGetTransferComplete  = 0x9117;
    public static final int DsCancelTransfer       = 0x9118;
    public static final int DsResetTransfer        = 0x9119;
    public static final int DsPCHDDCapacity        = 0x911a;
    public static final int DsSetUILock            = 0x911b;
    public static final int DsResetUILock          = 0x911c;
    public static final int DsKeepDeviceOn         = 0x911d;
    public static final int DsSetNullPacketmode    = 0x911e;
    public static final int DsUpdateFirmware       = 0x911f;
    public static final int DsFapiMessageTx        = 0x91fe;
    public static final int DsFapiMessageRx        = 0x91ff;

    public String getCodeName(int code) {
        return factory.getOpcodeString(code);
    }

    static String _getOpcodeString(int code) {
        switch (code) {
            case GetDeviceInfo:          return "GetDeviceInfo";
            case OpenSession:            return "OpenSession";
            case CloseSession:           return "CloseSession";
            case GetStorageIDs:          return "GetStorageIDs";
            case GetStorageInfo:         return "GetStorageInfo";
            case GetNumObjects:          return "GetNumObjects";
            case GetObjectHandles:       return "GetObjectHandles";
            case GetObjectInfo:          return "GetObjectInfo";
            case GetObject:              return "GetObject";
            case GetThumb:               return "GetThumb";
            case DeleteObject:           return "DeleteObject";
            case SendObjectInfo:         return "SendObjectInfo";
            case SendObject:             return "SendObject";
            case InitiateCapture:        return "InitiateCapture";
            case FormatStore:            return "FormatStore";
            case ResetDevice:            return "ResetDevice";
            case SelfTest:               return "SelfTest";
            case SetObjectProtection:    return "SetObjectProtection";
            case PowerDown:              return "PowerDown";
            case GetDevicePropDesc:      return "GetDevicePropDesc";
            case GetDevicePropValue:     return "GetDevicePropValue";
            case SetDevicePropValue:     return "SetDevicePropValue";
            case ResetDevicePropValue:   return "ResetDevicePropValue";
            case TerminateOpenCapture:   return "TerminateOpenCapture";
            case MoveObject:             return "MoveObject";
            case CopyObject:             return "CopyObject";
            case GetPartialObject:       return "GetPartialObject";
            case InitiateOpenCapture:    return "InitiateOpenCapture";
            case DsSetDevicePropValueEx: return "DsSetDevicePropValueEx";
            case DsCancelTransfer:       return "DsCancelTransfer";
            case DsCreageObject:         return "DsCreageObject";
            case DsDeleteObject:         return "DsDeleteObject";
            case DsFapiMessageRx:        return "DsFapiMessageRx";
            case DsFapiMessageTx:        return "DsFapiMessageTx";
            case DsFormatStore:          return "DsFormatStore";
            case DsGetDeviceInfoEx:      return "DsGetDeviceInfoEx";
            case DsGetEvent:             return "DsGetEvent";
            case DsGetObject:            return "DsGetObject";
            case DsGetObjectInfo:        return "DsGetObjectInfo";
            case DsGetObjectInfoEx:      return "DsGetObjectInfoEx";
            case DsGetObjectTime:        return "DsGetObjectTime";
            case DsGetPartialObject:     return "DsGetPartialObject";
            case DsGetRemoteMode:        return "DsGetRemoteMode";
            case DsGetStorageIds:        return "DsGetStorageIds";
            case DsGetStorageInfo:       return "DsGetStorageInfo";
            case DsGetThumbEx:           return "DsGetThumbEx";
            case DsKeepDeviceOn:         return "DsKeepDeviceOn";
            case DsPCHDDCapacity:        return "DsPCHDDCapacity";
            case DsRemoteRelease:        return "DsRemoteRelease";
            case DsResetUILock:          return "DsResetUILock";
            case DsResetTransfer:        return "DsResetTransfer";
            case DsSendObjectEx:         return "DsSendObjectEx";
            case DsSendPartialObject:    return "DsSendPartialObject";
            case DsSetNullPacketmode:    return "DsSetNullPacketmode";
            case DsSetObjectProperties:  return "DsSetObjectProperties";
            case DsSetObjectTime:        return "DsSetObjectTime";
            case DsSetRemoteMode:        return "DsSetRemoteMode";
            case DsSetUILock:            return "DsSetUILock";
            case DsGetTransferComplete:  return "DsGetTransferComplete";
            case DsUpdateFirmware:       return "DsUpdateFirmware";
        }

        return Container.getCodeString(code);
    }
}
