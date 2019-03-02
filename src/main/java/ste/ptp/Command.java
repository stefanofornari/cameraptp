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
    //
    // see https://fossies.org/linux/libgphoto2/camlibs/ptp2/ptp.h
    // last update: 20190224
    //
    /* PTP v1.0 operation codes */
    public static final int Undefined                                  = 0x1000;
    public static final int GetDeviceInfo                              = 0x1001;
    public static final int OpenSession                                = 0x1002;
    public static final int CloseSession                               = 0x1003;
    public static final int GetStorageIDs                              = 0x1004;
    public static final int GetStorageInfo                             = 0x1005;
    public static final int GetNumObjects                              = 0x1006;
    public static final int GetObjectHandles                           = 0x1007;
    public static final int GetObjectInfo                              = 0x1008;
    public static final int GetObject                                  = 0x1009;
    public static final int GetThumb                                   = 0x100a;
    public static final int DeleteObject                               = 0x100b;
    public static final int SendObjectInfo                             = 0x100c;
    public static final int SendObject                                 = 0x100d;
    public static final int InitiateCapture                            = 0x100e;
    public static final int FormatStore                                = 0x100f;
    public static final int ResetDevice                                = 0x1010;
    public static final int SelfTest                                   = 0x1011;
    public static final int SetObjectProtection                        = 0x1012;
    public static final int PowerDown                                  = 0x1013;
    public static final int GetDevicePropDesc                          = 0x1014;
    public static final int GetDevicePropValue                         = 0x1015;
    public static final int SetDevicePropValue                         = 0x1016;
    public static final int ResetDevicePropValue                       = 0x1017;
    public static final int TerminateOpenCapture                       = 0x1018;
    public static final int MoveObject                                 = 0x1019;
    public static final int CopyObject                                 = 0x101a;
    public static final int GetPartialObject                           = 0x101b;
    public static final int InitiateOpenCapture                        = 0x101c;

    /* PTP v1.1 operation codes */
    public static final int StartEnumHandles                           = 0x101d;
    public static final int EnumHandles                                = 0x101e;
    public static final int StopEnumHandles                            = 0x101f;
    public static final int GetVendorExtensionMaps                     = 0x1020;
    public static final int GetVendorDeviceInfo                        = 0x1021;
    public static final int GetResizedImageObject                      = 0x1022;
    public static final int GetFilesystemManifest                      = 0x1023;
    public static final int GetStreamInfo                              = 0x1024;
    public static final int GetStream                                  = 0x1025;

    /* Canon extension Operation Codes */
    public static final int CanonGetPartialObjectInfo                  = 0x9001;
    public static final int CanonSetObjectArchive                      = 0x9002;  // 9002 - sends 2 uint32, nothing back
    public static final int CanonKeepDeviceOn                          = 0x9003;
    public static final int CanonLockDeviceUI                          = 0x9004;
    public static final int CanonUnlockDeviceUI                        = 0x9005;
    public static final int CanonGetObjectHandleByName                 = 0x9006;
    /* no 9007 observed yet */
    public static final int CanonInitiateReleaseControl                = 0x9008;
    public static final int CanonTerminateReleaseControl               = 0x9009;
    public static final int CanonTerminatePlaybackMode                 = 0x900a;
    public static final int CanonViewfinderOn                          = 0x900b;
    public static final int CanonViewfinderOoff                        = 0x900c;
    public static final int CanonDoAeAfAwb                             = 0x900d;
    public static final int CanonGetCustomizeSpec                      = 0x900e;  // 900e - send nothing, gets 5 uint16t in 32bit entities back in 20byte datablob
    public static final int CanonGetCustomizeItemInfo                  = 0x900f;
    public static final int CanonGetCustomizeData                      = 0x9010;
    public static final int CanonSetCustomizeData                      = 0x9011;
    public static final int CanonGetCaptureStatus                      = 0x9012;
    public static final int CanonCheckEvent                            = 0x9013;
    public static final int CanonFocusLock                             = 0x9014;
    public static final int CanonFocusUnlock                           = 0x9015;
    public static final int CanonGetLocalReleaseParam                  = 0x9016;
    public static final int CanonSetLocalReleaseParam                  = 0x9017;
    public static final int CanonAskAboutPcEvf                         = 0x9018;
    public static final int CanonSendPartialObject                     = 0x9019;
    public static final int CanonInitiateCaptureInMemory               = 0x901a;
    public static final int CanonGetPartialObjectEx                    = 0x901b;
    public static final int CanonSetObjectTime                         = 0x901c;
    public static final int CanonGetViewfinderImage                    = 0x901d;
    public static final int CanonGetObjectAttributes                   = 0x901e;
    public static final int CanonChangeUSBProtocol                     = 0x901f;
    public static final int CanonGetChanges                            = 0x9020;
    public static final int CanonGetObjectInfoEx                       = 0x9021;
    public static final int CanonInitiateDirectTransfer                = 0x9022;
    public static final int CanonTerminateDirectTransfer               = 0x9023;
    public static final int CanonSendObjectInfoByPath                  = 0x9024;
    public static final int CanonSendObjectByPath                      = 0x9025;
    public static final int CanonInitiateDirectTansferEx               = 0x9026;
    public static final int CanonGetAncillaryObjectHandles             = 0x9027;
    public static final int CanonGetTreeInfo                           = 0x9028;
    public static final int CanonGetTreeSize                           = 0x9029;
    public static final int CanonNotifyProgress                        = 0x902a;
    public static final int CanonNotifyCancelAccepted                  = 0x902b;
    public static final int Canon902c                                  = 0x902c; // no parms, read 3 uint32 in data, no response parms
    public static final int CanonGetDirectory                          = 0x902d;
    public static final int Canon902e                                  = 0x902e;
    public static final int Canon902f                                  = 0x902f;
    public static final int CanonSetPairingInfo                        = 0x9030;
    public static final int CanonGetPairingInfo                        = 0x9031;
    public static final int CanonDeletePairingInfo                     = 0x9032;
    public static final int CanonGetMACAddress                         = 0x9033; // no args
    public static final int CanonSetDisplayMonitor                     = 0x9034;
    public static final int CanonPairingComplete                       = 0x9035;
    public static final int CanonGetWirelessMAXChannel                 = 0x9036;

    public static final int CanonGetWebServiceSpec                     = 0x9068; // no args
    public static final int CanonGetWebServiceData                     = 0x906b;
    public static final int CanonGetRootCertificateSpec                = 0x906c; // no args
    public static final int CanonGetRootCertificateData                = 0x906d; // no args
    public static final int CanonSetRootCertificateData                = 0x906f;

    public static final int CanonGetGpsMobilelinkObjectInfo            = 0x9075; // 2 args: utcstart, utcend
    public static final int CanonSendGpsTagInfo                        = 0x9076; // 1 arg: oid?
    public static final int CanonGetTrancecodeApproxSize               = 0x9077; // 1 arg: oid?
    public static final int CanonRequestTrancecodeStart                = 0x9078; // 1 arg: oid?
    public static final int CanonRequestTrancecodeCancel               = 0x9079; // 1 arg: oid?


    public static final int EosGetStorageIds                           = 0x9101; // no args, 8 byte data (01 00 00 00 00 00 00 00), no resp data.
    public static final int EosGetStorageInfo                          = 0x9102;
    public static final int EosGetObjectInfo                           = 0x9103;
    public static final int EosGetObject                               = 0x9104;
    public static final int EosDeleteObject                            = 0x9105;
    public static final int EosFormatStore                             = 0x9106;
    public static final int EosGetPartialObject                        = 0x9107;
    public static final int EosGetDeviceInfoEx                         = 0x9108;
    public static final int EosGetObjectInfoEx                         = 0x9109;
    public static final int EosGetThumbEx                              = 0x910a;
    public static final int EosSendPartialObject                       = 0x910b;
    public static final int EosSetObjectProperties                     = 0x910c;
    public static final int EosGetObjectTime                           = 0x910d;
    public static final int EosSetObjectTime                           = 0x910e;
    public static final int EosRemoteRelease                           = 0x910f; // no args, no data, 1 response arg (0).
    public static final int EosSetDevicePropValueEx                    = 0x9110;
    public static final int EosSendObjectEx                            = 0x9111;
    public static final int EosCreageObject                            = 0x9112;
    public static final int EosGetRemoteMode                           = 0x9113;
    public static final int EosSetRemoteMode                           = 0x9114; // 1 arg (0x1), no data, no resp data.
    public static final int EosSetEventMode                            = 0x9115;
    public static final int EosGetEvent                                = 0x9116; // no args, data phase, no resp data.
    public static final int EosTransferComplete                        = 0x9117;
    public static final int EosCancelTransfer                          = 0x9118;
    public static final int EosResetTransfer                           = 0x9119;
    public static final int EosPCHDDCapacity                           = 0x911a; // 3 args (0xfffffff7, 0x00001000, 0x00000001), no data, no resp data.
    public static final int EosSetUILock                               = 0x911b; // no cmd args, no data, no resp args
    public static final int EosResetUILock                             = 0x911c; // no cmd args, no data, no resp args
    public static final int EosKeepDeviceOn                            = 0x911d; // no arg
    public static final int EosSetNullPacketmode                       = 0x911e; // 1 param
    public static final int EosUpdateFirmware                          = 0x911f;
    public static final int EosUpdateTransferCompleteDt                = 0x9120;
    public static final int EosCancelTransferDt                        = 0x9121;
    public static final int EosSetFWTProfile                           = 0x9122;
    public static final int EosGetFWTProfile                           = 0x9123; // 2 args: setnum, configid
    public static final int EosSetProfileToWTF                         = 0x9124;
    public static final int EosBulbStart                               = 0x9125;
    public static final int EosBulbEnd                                 = 0x9126;
    public static final int EosRequestDevicePropValue                  = 0x9127;
    public static final int EosRemoeReleaseOn                          = 0x9128; // args (0x1/0x2, 0x0), no data, no resp args
    public static final int EosRemoeReleaseOff                         = 0x9129; // args (0x1/0x2), no data, no resp args
    public static final int EosRegistBackgroundImage                   = 0x912a;
    public static final int EosChangePhotoStadIOMode                   = 0x912b;
    public static final int EosGetPartialObjectEx                      = 0x912c;
    public static final int EosResetMirrorLockupState                  = 0x9130; // no args
    public static final int EosPopupBuiltinFlash                       = 0x9131;
    public static final int EosEndGetPartialObjectEx                   = 0x9132;
    public static final int EosMovieSelectSWOn                         = 0x9133; // no args
    public static final int EosMovieSelectSWOff                        = 0x9134; // no args
    public static final int EosGetCTGInfo                              = 0x9135;
    public static final int EosgetLensAdjust                           = 0x9136;
    public static final int EosSetLensAdjust                           = 0x9137;
    public static final int EosGetMusicInfo                            = 0x9138;
    public static final int EosCreateHandle                            = 0x9139; // 3 paramaeters, no data, OFC, size, unknown
    public static final int EosSendPartialObjectEx                     = 0x913a;
    public static final int EosEndSendPartialObjectEx                  = 0x913b;
    public static final int EosSetCTGInfo                              = 0x913c;
    public static final int EosSetRequestOLCInfoGroup                  = 0x913d;
    public static final int EosSetRequestRollingPitchingLevel          = 0x913e; // 1 arg: onoff?
    public static final int EosGetCameraSupport                        = 0x913F; // 3 args, 0x21201020, 0x110, 0x1000000 (potentially reverse order)
    public static final int EosSetRating                               = 0x9140; // 2 args, objectid, rating?
    public static final int EosRequestInnerDevelopStart                = 0x9141; // 2 args: 1 type, 1 object?
    public static final int EosRequestInnerDevelopParamChange          = 0x9142;
    public static final int EosRequestInnerDevelopEnd                  = 0x9143;
    public static final int EosGpsLoggingDataMode                      = 0x9144; // 1 arg
    public static final int EosGetGpsLogCurrentHandle                  = 0x9145;
    public static final int EosSetImageRecoveryData                    = 0x9146; // sends data?
    public static final int EosGetImageRecoveryList                    = 0x9147;
    public static final int EosFormatImageRecoveryData                 = 0x9148;
    public static final int EosGetPresetLensAdjustParam                = 0x9149; // no arg
    public static final int EosGetRawDispImage                         = 0x914a; // ? 2 args ?
    public static final int EosSaveImageRecoveryData                   = 0x914b;
    public static final int EosRequestBLE                              = 0x914c; // ? 2 args ?
    public static final int EosDrivePowerZoom                          = 0x914d; // 1 arg
    public static final int EosGetIptcData                             = 0x914f;
    public static final int EosSetIptcData                             = 0x9150; // sends data?
    public static final int EosInitiateViewFinder                      = 0x9151; // no arg
    public static final int EosTerminateViewFinder                     = 0x9152;
    public static final int EosGetViewFinderData                       = 0x9153;
    public static final int EosDoAF                                    = 0x9154;
    public static final int EosDriveLens                               = 0x9155;
    public static final int EosDepthOfFieldPreview                     = 0x9156; // 1 arg
    public static final int EosClickWB                                 = 0x9157; // 2 args: x,y
    public static final int EosZoom                                    = 0x9158; // 1 arg: zoom
    public static final int EosZoomPosition                            = 0x9159; // 2 args: x,y
    public static final int EosSetLiveAFFrame                          = 0x915a; // sends data?
    public static final int EosTouchAfPosition                         = 0x915b; // 3 args: type,x,y
    public static final int EosLvPcFlavoreditMode                      = 0x915c; // 1 arg
    public static final int EosSetLvPcFlavoreditParam                  = 0x915d; // 1 arg

    public static final int EosAFCancel                                = 0x9160;

    public static final int EosGetObjectInfo64                         = 0x9170; // 1 arg: oid
    public static final int EosGetObject64                             = 0x9171; // 1 arg: oid
    public static final int EosGetPartialObject64                      = 0x9172; // args: oid, offset, maxbyte
    public static final int EosGetObjectInfoEx64                       = 0x9173; // 2 args: storageid, oid  ?
    public static final int EosGetPartialObjectEX64                    = 0x9174; // args: oid, offset 64bit, maxbyte
    public static final int EosCreateHandle64                          = 0x9175;

    public static final int EosNotifyEstimateNumberofImport            = 0x9182; // 1 arg: importnumber
    public static final int EosNotifyNumberofImported                  = 0x9183; // 1 arg: importnumber
    public static final int EosNotifySizeOfPartialDataTransfer         = 0x9184; // 4 args: filesizelow, filesizehigh, downloadsizelow, downloadsizehigh
    public static final int EosNotifyFinish                            = 0x9185; // 1 arg: reason

    public static final int EosSetDefaultCameraSetting                 = 0x91be; // 1 arg: reason
    public static final int EosSetGetAEData                            = 0x91bf;

    public static final int EosNotifyNetworkError                      = 0x91e8; // 1 arg: errorcode
    public static final int EosAdapterTransferProgress                 = 0x91e9;
    public static final int EosTransferComplete2                       = 0x91f0;
    public static final int EosCancelTransfer2                         = 0x91f1;
    public static final int EosFapiMessageTx                           = 0x91fe;
    public static final int EosFapiMessageRx                           = 0x91ff;

    public static final int MtpGetObjectPropsSupported                 = 0x9801;
    public static final int MtpGetObjectPropDesc                       = 0x9802;
    public static final int MtpGetObjectPropValue                      = 0x9803;
    public static final int MtpSetObjectPropValue                      = 0x9804;
    public static final int MtpGetObjPropList                          = 0x9805;

    public String getCodeName(int code) {
        return factory.getOpcodeString(code);
    }

    static String _getOpcodeString(int code) {
        switch (code) {
            case GetDeviceInfo:               return "GetDeviceInfo";
            case OpenSession:                 return "OpenSession";
            case CloseSession:                return "CloseSession";
            case GetStorageIDs:               return "GetStorageIDs";
            case GetStorageInfo:              return "GetStorageInfo";
            case GetNumObjects:               return "GetNumObjects";
            case GetObjectHandles:            return "GetObjectHandles";
            case GetObjectInfo:               return "GetObjectInfo";
            case GetObject:                   return "GetObject";
            case GetThumb:                    return "GetThumb";
            case DeleteObject:                return "DeleteObject";
            case SendObjectInfo:              return "SendObjectInfo";
            case SendObject:                  return "SendObject";
            case InitiateCapture:             return "InitiateCapture";
            case FormatStore:                 return "FormatStore";
            case ResetDevice:                 return "ResetDevice";
            case SelfTest:                    return "SelfTest";
            case SetObjectProtection:         return "SetObjectProtection";
            case PowerDown:                   return "PowerDown";
            case GetDevicePropDesc:           return "GetDevicePropDesc";
            case GetDevicePropValue:          return "GetDevicePropValue";
            case SetDevicePropValue:          return "SetDevicePropValue";
            case ResetDevicePropValue:        return "ResetDevicePropValue";
            case TerminateOpenCapture:        return "TerminateOpenCapture";
            case MoveObject:                  return "MoveObject";
            case CopyObject:                  return "CopyObject";
            case GetPartialObject:            return "GetPartialObject";
            case InitiateOpenCapture:         return "InitiateOpenCapture";
            case EosSetDevicePropValueEx:     return "EosSetDevicePropValueEx";
            case EosCancelTransfer:           return "EosCancelTransfer";
            case EosCreageObject:             return "EosCreageObject";
            case EosDeleteObject:             return "EosDeleteObject";
            case EosFapiMessageRx:            return "EosFapiMessageRx";
            case EosFapiMessageTx:            return "EosFapiMessageTx";
            case EosFormatStore:              return "EosFormatStore";
            case EosGetDeviceInfoEx:          return "EosGetDeviceInfoEx";
            case EosGetEvent:                 return "EosGetEvent";
            case EosGetObject:                return "EosGetObject";
            case EosGetObjectInfo:            return "EosGetObjectInfo";
            case EosGetObjectInfoEx:          return "EosGetObjectInfoEx";
            case EosGetObjectTime:            return "EosGetObjectTime";
            case EosGetPartialObject:         return "EosGetPartialObject";
            case EosGetRemoteMode:            return "EosGetRemoteMode";
            case EosGetStorageIds:            return "EosGetStorageIds";
            case EosGetStorageInfo:           return "EosGetStorageInfo";
            case EosGetThumbEx:               return "EosGetThumbEx";
            case EosKeepDeviceOn:             return "EosKeepDeviceOn";
            case EosPCHDDCapacity:            return "EosPCHDDCapacity";
            case EosRemoteRelease:            return "EosRemoteRelease";
            case EosResetUILock:              return "EosResetUILock";
            case EosResetTransfer:            return "EosResetTransfer";
            case EosSendObjectEx:             return "EosSendObjectEx";
            case EosSendPartialObject:        return "EosSendPartialObject";
            case EosSetNullPacketmode:        return "EosSetNullPacketmode";
            case EosSetObjectProperties:      return "EosSetObjectProperties";
            case EosSetObjectTime:            return "EosSetObjectTime";
            case EosSetRemoteMode:            return "EosSetRemoteMode";
            case EosSetUILock:                return "EosSetUILock";
            case EosTransferComplete:      return "EosGetTransferComplete";
            case EosUpdateFirmware:           return "EosUpdateFirmware";
            case EosRequestDevicePropValue:   return "EosRequestDevicePropValue";
            case EosUpdateTransferCompleteDt: return "EosUpdateTransferCompleteDt";
            case EosCancelTransferDt:         return "EosCancelTransferDt";
            case EosInitiateViewFinder:       return "EosInitiateViewFinder";
            case EosTerminateViewFinder:      return "EosTerminateViewFinder";
            case EosGetViewFinderData:        return "EosgetViewFinderData";
            case EosDriveLens:                return "EosDriveLens";
            case EosDepthOfFieldPreview:      return "EosDepthOfFieldPreview";
            case EosClickWB:                  return "EosClickWB";
            case EosSetLiveAFFrame:           return "EosSetLiveAFFrame";
            case EosZoom:                     return "EosZoom";
            case EosZoomPosition:             return "EosZoomPostion";
            case EosGetFWTProfile:            return "EosGetFWTProfile";
            case EosSetFWTProfile:            return "EosSetFWTProfile";
            case EosSetProfileToWTF:          return "EosSetProfileToWTF";
            case EosBulbStart:                return "EosBulbStart";
            case EosBulbEnd:                  return "EosBulbEnd";
            case EosRemoeReleaseOn:           return "EosRemoeReleaseOn";
            case EosRemoeReleaseOff:          return "EosRemoeReleaseOff";
            case EosDoAF:                     return "EosDoAF";
            case EosAFCancel:                 return "EosAFCancel";
            case EosRegistBackgroundImage:    return "EosRegistBackgroundImage";
            case EosChangePhotoStadIOMode:    return "EosChangePhotoStadIOMode";
            case EosGetPartialObjectEx:       return "EosGetPartialObjectEx";
            case EosResetMirrorLockupState:   return "EosResetMirrorLockupState";
            case EosPopupBuiltinFlash:        return "EosPopupBuiltinFlash";
            case EosEndGetPartialObjectEx:    return "EosEndGetPartialObjectEx";
            case EosMovieSelectSWOn:          return "EosMovieSelectSWOn";
            case EosMovieSelectSWOff:         return "EosMovieSelectSWOff";
            case EosSetEventMode:             return "EosSetEventMode";
            case MtpGetObjectPropsSupported:  return "MtpGetObjectPropsSupported";
            case MtpGetObjectPropDesc:        return "MtpGetObjectPropDesc";
            case MtpGetObjectPropValue:       return "MtpGetObjectPropValue";
            case MtpSetObjectPropValue:       return "MtpSetObjectPropValue";
            case MtpGetObjPropList:           return "GetObjPropList";
        }

        return Container.getCodeString(code);
    }
}
