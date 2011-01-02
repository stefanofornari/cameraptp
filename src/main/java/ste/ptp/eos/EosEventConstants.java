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

/**
 * EOS event codes
 *
 * @author stefano fornari
 */
public interface EosEventConstants {
    /**
     * Events
     */
    public static final int EosEventRequestGetEvent         = 0xC101;
    public static final int EosEventObjectAddedEx           = 0xC181;
    public static final int EosEventObjectRemoved           = 0xC182;
    public static final int EosEventRequestGetObjectInfoEx  = 0xC183;
    public static final int EosEventStorageStatusChanged    = 0xC184;
    public static final int EosEventStorageInfoChanged      = 0xC185;
    public static final int EosEventRequestObjectTransfer   = 0xC186;
    public static final int EosEventObjectInfoChangedEx     = 0xC187;
    public static final int EosEventObjectContentChanged    = 0xC188;
    public static final int EosEventPropValueChanged        = 0xC189;
    public static final int EosEventAvailListChanged        = 0xC18A;
    public static final int EosEventCameraStatusChanged     = 0xC18B;
    public static final int EosEventWillSoonShutdown        = 0xC18D;
    public static final int EosEventShutdownTimerUpdated    = 0xC18E;
    public static final int EosEventRequestCancelTransfer   = 0xC18F;
    public static final int EosEventRequestObjectTransferDT = 0xC190;
    public static final int EosEventRequestCancelTransferDT = 0xC191;
    public static final int EosEventStoreAdded              = 0xC192;
    public static final int EosEventStoreRemoved            = 0xC193;
    public static final int EosEventBulbExposureTime        = 0xC194;
    public static final int EosEventRecordingTime           = 0xC195;
    public static final int EosEventRequestObjectTransferTS = 0xC1A2;
    public static final int EosEventAfResult                = 0xC1A3;

    /*
     * Properties
     */
    public static final int EosPropAperture                = 0xD101;
    public static final int EosPropShutterSpeed            = 0xD102;
    public static final int EosPropISOSpeed                = 0xD103;
    public static final int EosPropExpCompensation         = 0xD104;
    public static final int EosPropAutoExposureMode        = 0xD105;
    public static final int EosPropDriveMode               = 0xD106;
    public static final int EosPropMeteringMode            = 0xD107;
    public static final int EosPropFocusMode               = 0xD108;
    public static final int EosPropWhiteBalance            = 0xD109;
    public static final int EosPropColorTemperature        = 0xD10A;
    public static final int EosPropWhiteBalanceAdjustA     = 0xD10B;
    public static final int EosPropWhiteBalanceAdjustB     = 0xD10C;
    public static final int EosPropWhiteBalanceXA          = 0xD10D;
    public static final int EosPropWhiteBalanceXB          = 0xD10E;
    public static final int EosPropColorSpace              = 0xD10F;
    public static final int EosPropPictureStyle            = 0xD110;
    public static final int EosPropBatteryPower            = 0xD111;
    public static final int EosPropBatterySelect           = 0xD112;
    public static final int EosPropCameraTime              = 0xD113;
    public static final int EosPropOwner                   = 0xD115;
    public static final int EosPropModelID		   = 0xD116;
    public static final int EosPropPTPExtensionVersion     = 0xD119;
    public static final int EosPropDPOFVersion             = 0xD11A;
    public static final int EosPropAvailableShots          = 0xD11B;
    public static final int EosPropCaptureDestination      = 0xD11C;
    public static final int EosPropBracketMode             = 0xD11D;
    public static final int EosPropCurrentStorage          = 0xD11E;
    public static final int EosPropCurrentFolder           = 0xD11F;
    public static final int EosPropImageFormat             = 0xD120; /* file setting */
    public static final int EosPropImageFormatCF           = 0xD121; /* file setting CF */
    public static final int EosPropImageFormatSD           = 0xD122; /* file setting SD */
    public static final int EosPropImageFormatExtHD        = 0xD123; /* file setting exthd */
    public static final int EosPropCompressionS            = 0xD130;
    public static final int EosPropCompressionM1           = 0xD131;
    public static final int EosPropCompressionM2           = 0xD132;
    public static final int EosPropCompressionL            = 0xD133;
    public static final int EosPropPCWhiteBalance1         = 0xD140;
    public static final int EosPropPCWhiteBalance2         = 0xD141;
    public static final int EosPropPCWhiteBalance3         = 0xD142;
    public static final int EosPropPCWhiteBalance4         = 0xD143;
    public static final int EosPropPCWhiteBalance5         = 0xD144;
    public static final int EosPropMWhiteBalance           = 0xD145;
    public static final int EosPropPictureStyleStandard    = 0xD150;
    public static final int EosPropPictureStylePortrait    = 0xD151;
    public static final int EosPropPictureStyleLandscape   = 0xD152;
    public static final int EosPropPictureStyleNeutral     = 0xD153;
    public static final int EosPropPictureStyleFaithful    = 0xD154;
    public static final int EosPropPictureStyleMonochrome  = 0xD155;
    public static final int EosPropPictureStyleUserSet1    = 0xD160;
    public static final int EosPropPictureStyleUserSet2    = 0xD161;
    public static final int EosPropPictureStyleUserSet3    = 0xD162;
    public static final int EosPropPictureStyleParam1      = 0xD170;
    public static final int EosPropPictureStyleParam2      = 0xD171;
    public static final int EosPropPictureStyleParam3      = 0xD172;
    public static final int EosPropFlavorLUTParams         = 0xD17F;
    public static final int EosPropCustomFunc1             = 0xD180;
    public static final int EosPropCustomFunc2             = 0xD181;
    public static final int EosPropCustomFunc3             = 0xD182;
    public static final int EosPropCustomFunc4             = 0xD183;
    public static final int EosPropCustomFunc5             = 0xD184;
    public static final int EosPropCustomFunc6             = 0xD185;
    public static final int EosPropCustomFunc7             = 0xD186;
    public static final int EosPropCustomFunc8             = 0xD187;
    public static final int EosPropCustomFunc9             = 0xD188;
    public static final int EosPropCustomFunc10            = 0xD189;
    public static final int EosPropCustomFunc11            = 0xD18A;
    public static final int EosPropCustomFunc12            = 0xD18B;
    public static final int EosPropCustomFunc13            = 0xD18C;
    public static final int EosPropCustomFunc14            = 0xD18D;
    public static final int EosPropCustomFunc15            = 0xD18E;
    public static final int EosPropCustomFunc16            = 0xD18F;
    public static final int EosPropCustomFunc17            = 0xD190;
    public static final int EosPropCustomFunc18            = 0xD191;
    public static final int EosPropCustomFunc19            = 0xD192;
    public static final int EosPropCustomFuncEx            = 0xD1A0;
    public static final int EosPropMyMenu                  = 0xD1A1;
    public static final int EosPropMyMenuList              = 0xD1A2;
    public static final int EosPropWftStatus               = 0xD1A3;
    public static final int EosPropWftInputTransmission    = 0xD1A4;
    public static final int EosPropHDDirectoryStructure    = 0xD1A5;
    public static final int EosPropBatteryInfo             = 0xD1A6;
    public static final int EosPropAdapterInfo             = 0xD1A7;
    public static final int EosPropLensStatus              = 0xD1A8;
    public static final int EosPropQuickReviewTime         = 0xD1A9;
    public static final int EosPropCardExtension           = 0xD1AA;
    public static final int EosPropTempStatus              = 0xD1AB;
    public static final int EosPropShutterCounter          = 0xD1AC;
    public static final int EosPropSpecialOption           = 0xD1AD;
    public static final int EosPropPhotoStudioMode         = 0xD1AE;
    public static final int EosPropSerialNumber            = 0xD1AF;
    public static final int EosPropEVFOutputDevice         = 0xD1B0;
    public static final int EosPropEVFMode                 = 0xD1B1;
    public static final int EosPropDepthOfFieldPreview     = 0xD1B2;
    public static final int EosPropEVFSharpness            = 0xD1B3;
    public static final int EosPropEVFWBMode               = 0xD1B4;
    public static final int EosPropEVFClickWBCoeffs        = 0xD1B5;
    public static final int EosPropEVFColorTemp            = 0xD1B6;
    public static final int EosPropExposureSimMode         = 0xD1B7;
    public static final int EosPropEVFRecordStatus         = 0xD1B8;
    public static final int EosPropLvAfSystem              = 0xD1BA;
    public static final int EosPropMovSize                 = 0xD1BB;
    public static final int EosPropLvViewTypeSelect        = 0xD1BC;
    public static final int EosPropArtist                  = 0xD1D0;
    public static final int EosPropCopyright               = 0xD1D1;
    public static final int EosPropBracketValue            = 0xD1D2;
    public static final int EosPropFocusInfoEx             = 0xD1D3;
    public static final int EosPropDepthOfField            = 0xD1D4;
    public static final int EosPropBrightness              = 0xD1D5;
    public static final int EosPropLensAdjustParams        = 0xD1D6;
    public static final int EosPropEFComp                  = 0xD1D7;
    public static final int EosPropLensName                = 0xD1D8;
    public static final int EosPropAEB                     = 0xD1D9;
    public static final int EosPropStroboSetting           = 0xD1DA;
    public static final int EosPropStroboWirelessSetting   = 0xD1DB;
    public static final int EosPropStroboFiring            = 0xD1DC;
    public static final int EosPropLensID                  = 0xD1DD;

    /**
     * ISO speed
     */
    public static final int EosISOSPeedAuto  = 0x00;
    public static final int EosISOSPeed100   = 0x48;
    public static final int EosISOSPeed200   = 0x50;
    public static final int EosISOSPeed400   = 0x58;
    public static final int EosISOSPeed800   = 0x60;
    public static final int EosISOSPeed1600  = 0x68;

    /**
     * Aperture
     */
    public static final int EosAperture_4    = 0x28;
    public static final int EosAperture_4_5  = 0x2B;
    public static final int EosAperture_5    = 0x2D;
    public static final int EosAperture_5_6  = 0x30;
    public static final int EosAperture_6_3  = 0x33;
    public static final int EosAperture_7_1  = 0x35;
    public static final int EosAperture_8    = 0x38;
    public static final int EosAperture_9    = 0x3B;
    public static final int EosAperture_10   = 0x3D;
    public static final int EosAperture_11   = 0x40;
    public static final int EosAperture_13   = 0x43;
    public static final int EosAperture_14   = 0x45;
    public static final int EosAperture_16   = 0x48;
    public static final int EosAperture_18   = 0x4B;
    public static final int EosAperture_20   = 0x4D;
    public static final int EosAperture_22   = 0x50;
    public static final int EosAperture_25   = 0x53;
    public static final int EosAperture_29   = 0x55;
    public static final int EosAperture_32   = 0x58;

    /**
     * User picture style type
     */
    public static final int EosPropPictureStyleUserTypeStandard    = 0x81;
    public static final int EosPropPictureStyleUserTypePortrait    = 0x82;
    public static final int EosPropPictureStyleUserTypeLandscape   = 0x83;
    public static final int EosPropPictureStyleUserTypeNeutral     = 0x84;
    public static final int EosPropPictureStyleUserTypeFaithful    = 0x85;
    public static final int EosPropPictureStyleUserTypeMonochrome  = 0x86;

    /**
     * Image formats
     */
    public static final int ImageFormatEXIF_JPEG                       = 0x3801;
    public static final int ImageFormatTIFF_EP                         = 0x3802;
    public static final int ImageFormatFlashPix                        = 0x3803;
    public static final int ImageFormatBMP                             = 0x3804;
    public static final int ImageFormatCIFF                            = 0x3805;
    public static final int ImageFormatUndefined_0x3806                = 0x3806;
    public static final int ImageFormatGIF                             = 0x3807;
    public static final int ImageFormatJFIF                            = 0x3808;
    public static final int ImageFormatPCD                             = 0x3809;
    public static final int ImageFormatPICT                            = 0x380A;
    public static final int ImageFormatPNG                             = 0x380B;
    public static final int ImageFormatUndefined_0x380C                = 0x380C;
    public static final int ImageFormatTIFF                            = 0x380D;
    public static final int ImageFormatTIFF_IT                         = 0x380E;
    public static final int ImageFormatJP2                             = 0x380F;
    public static final int ImageFormatJPX                             = 0x3810;
    /* ptp v1.1 has only DNG new */
    public static final int ImageFormatDNG                             = 0x3811;
    /* Eastman Kodak extension ancillary format */
    public static final int ImageFormatEK_M3U                          = 0xB002;
    /* Canon extension */
    public static final int ImageFormatCANON_CRW                       = 0xB101;
    public static final int ImageFormatCANON_CRW3                      = 0xB103;
    public static final int ImageFormatCANON_MOV                       = 0xB104;
    /* CHDK specific raw mode */
    public static final int ImageFormatCANON_CHDK_CRW                  = 0xB1FF;
    /* MTP extensions */
    public static final int ImageFormatMTP_MediaCard                   = 0xB211;
    public static final int ImageFormatMTP_MediaCardGroup              = 0xb212;
    public static final int ImageFormatMTP_Encounter                   = 0xb213;
    public static final int ImageFormatMTP_EncounterBox                = 0xb214;
    public static final int ImageFormatMTP_M4A                         = 0xb215;
    public static final int ImageFormatMTP_ZUNEUNDEFINED               = 0xb217;
    public static final int ImageFormatMTP_Firmware                    = 0xb802;
    public static final int ImageFormatMTP_WindowsImageFormat          = 0xb881;
    public static final int ImageFormatMTP_UndefinedAudio              = 0xb900;
    public static final int ImageFormatMTP_WMA                         = 0xb901;
    public static final int ImageFormatMTP_OGG                         = 0xb902;
    public static final int ImageFormatMTP_AAC                         = 0xb903;
    public static final int ImageFormatMTP_AudibleCodec                = 0xb904;
    public static final int ImageFormatMTP_FLAC                        = 0xb906;
    public static final int ImageFormatMTP_SamsungPlaylist             = 0xb909;
    public static final int ImageFormatMTP_UndefinedVideo              = 0xb980;
    public static final int ImageFormatMTP_WMV                         = 0xb981;
    public static final int ImageFormatMTP_MP4                         = 0xb982;
    public static final int ImageFormatMTP_MP2                         = 0xb983;
    public static final int ImageFormatMTP_3GP                         = 0xb984;
    public static final int ImageFormatMTP_UndefinedCollection         = 0xba00;
    public static final int ImageFormatMTP_AbstractMultimediaAlbum     = 0xba01;
    public static final int ImageFormatMTP_AbstractImageAlbum          = 0xba02;
    public static final int ImageFormatMTP_AbstractAudioAlbum          = 0xba03;
    public static final int ImageFormatMTP_AbstractVideoAlbum          = 0xba04;
    public static final int ImageFormatMTP_AbstractAudioVideoPlaylist  = 0xba05;
    public static final int ImageFormatMTP_AbstractContactGroup        = 0xba06;
    public static final int ImageFormatMTP_AbstractMessageFolder       = 0xba07;
    public static final int ImageFormatMTP_AbstractChapteredProduction = 0xba08;
    public static final int ImageFormatMTP_AbstractAudioPlaylist       = 0xba09;
    public static final int ImageFormatMTP_AbstractVideoPlaylist       = 0xba0a;
    public static final int ImageFormatMTP_AbstractMediacast           = 0xba0b;
    public static final int ImageFormatMTP_WPLPlaylist                 = 0xba10;
    public static final int ImageFormatMTP_M3UPlaylist                 = 0xba11;
    public static final int ImageFormatMTP_MPLPlaylist                 = 0xba12;
    public static final int ImageFormatMTP_ASXPlaylist                 = 0xba13;
    public static final int ImageFormatMTP_PLSPlaylist                 = 0xba14;
    public static final int ImageFormatMTP_UndefinedDocument           = 0xba80;
    public static final int ImageFormatMTP_AbstractDocument            = 0xba81;
    public static final int ImageFormatMTP_XMLDocument                 = 0xba82;
    public static final int ImageFormatMTP_MSWordDocument              = 0xba83;
    public static final int ImageFormatMTP_MHTCompiledHTMLDocument     = 0xba84;
    public static final int ImageFormatMTP_MSExcelSpreadsheetXLS       = 0xba85;
    public static final int ImageFormatMTP_MSPowerpointPresentationPPT = 0xba86;
    public static final int ImageFormatMTP_UndefinedMessage            = 0xbb00;
    public static final int ImageFormatMTP_AbstractMessage             = 0xbb01;
    public static final int ImageFormatMTP_UndefinedContact            = 0xbb80;
    public static final int ImageFormatMTP_AbstractContact             = 0xbb81;
    public static final int ImageFormatMTP_vCard2                      = 0xbb82;
    public static final int ImageFormatMTP_vCard3                      = 0xbb83;
    public static final int ImageFormatMTP_UndefinedCalendarItem       = 0xbe00;
    public static final int ImageFormatMTP_AbstractCalendarItem        = 0xbe01;
    public static final int ImageFormatMTP_vCalendar1                  = 0xbe02;
    public static final int ImageFormatMTP_vCalendar2                  = 0xbe03;
    public static final int ImageFormatMTP_UndefinedWindowsExecutable  = 0xbe80;
    public static final int ImageFormatMTP_MediaCast                   = 0xbe81;
    public static final int ImageFormatMTP_Section                     = 0xbe82;
}
