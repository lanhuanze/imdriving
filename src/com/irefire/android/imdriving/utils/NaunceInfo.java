package com.irefire.android.imdriving.utils;

public class NaunceInfo
{
    /**
     * The login parameters should be specified in the following manner:
     * 
     * public static final String SpeechKitServer = "ndev.server.name";
     * 
     * public static final int SpeechKitPort = 1000;
     * 
     * public static final String SpeechKitAppId = "ExampleSpeechKitSampleID";
     * 
     * public static final byte[] SpeechKitApplicationKey =
     * {
     *     (byte)0x38, (byte)0x32, (byte)0x0e, (byte)0x46, (byte)0x4e, (byte)0x46, (byte)0x12, (byte)0x5c, (byte)0x50, (byte)0x1d,
     *     (byte)0x4a, (byte)0x39, (byte)0x4f, (byte)0x12, (byte)0x48, (byte)0x53, (byte)0x3e, (byte)0x5b, (byte)0x31, (byte)0x22,
     *     (byte)0x5d, (byte)0x4b, (byte)0x22, (byte)0x09, (byte)0x13, (byte)0x46, (byte)0x61, (byte)0x19, (byte)0x1f, (byte)0x2d,
     *     (byte)0x13, (byte)0x47, (byte)0x3d, (byte)0x58, (byte)0x30, (byte)0x29, (byte)0x56, (byte)0x04, (byte)0x20, (byte)0x33,
     *     (byte)0x27, (byte)0x0f, (byte)0x57, (byte)0x45, (byte)0x61, (byte)0x5f, (byte)0x25, (byte)0x0d, (byte)0x48, (byte)0x21,
     *     (byte)0x2a, (byte)0x62, (byte)0x46, (byte)0x64, (byte)0x54, (byte)0x4a, (byte)0x10, (byte)0x36, (byte)0x4f, (byte)0x64
     * };
     * 
     * Please note that all the specified values are non-functional
     * and are provided solely as an illustrative example.
     * 
     */

    /* Please contact Nuance to receive the necessary connection and login parameters */
    public static final String SpeechKitServer = "sandbox.nmdp.nuancemobility.net"/* Enter your server here */;

    public static final int SpeechKitPort = 443/* Enter your port here */;
    
    public static final boolean SpeechKitSsl = false;

    public static final String SpeechKitAppId = "NMDPTRIAL_bjtulan20140623145303"/* Enter your ID here */;

    public static final byte[] SpeechKitApplicationKey = {
        /* Enter your application key here:
        (byte)0x00, (byte)0x01, ... (byte)0x00
        */
    	(byte)0x05, (byte)0xe7, (byte)0x3c, (byte)0x32, (byte)0x81, (byte)0xf3, (byte)0x1e, (byte)0x8f, (byte)0x2b,
    	(byte)0x98, (byte)0x92, (byte)0xbf, (byte)0xa8, (byte)0xfd, (byte)0xdb, (byte)0xbc, (byte)0xa2, (byte)0x8f,
    	(byte)0x47, (byte)0xac, (byte)0xe7, (byte)0x8a, (byte)0x87, (byte)0x6a, (byte)0x52, (byte)0xb4, (byte)0xe3,
    	(byte)0xda, (byte)0xf9, (byte)0xd8, (byte)0xac, (byte)0xff, (byte)0xbf, (byte)0xe9, (byte)0x31, (byte)0x41,
    	(byte)0xdc, (byte)0x4f, (byte)0xd0, (byte)0x07, (byte)0xe7, (byte)0x62, (byte)0xc6, (byte)0x90, (byte)0x5f,
    	(byte)0xa2, (byte)0x4c, (byte)0x01, (byte)0x31, (byte)0x94, (byte)0xad, (byte)0x19, (byte)0x1e, (byte)0x35,
    	(byte)0x83, (byte)0x70, (byte)0xe9, (byte)0x66, (byte)0x4a, (byte)0xe3, (byte)0x3f, (byte)0xf7, (byte)0xe2,
    	(byte)0xfe
    };
}
