package com.irefire.android.imdriving.utils;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Root {

	private static final Logger l = LoggerFactory.getLogger(Root.class.getSimpleName());

	public static boolean isRooted() {
	    return findBinary("su");
	}



	public static boolean findBinary(String binaryName) {
	    boolean found = false;
	    if (!found) {
	        String[] places = {"/sbin/", "/system/bin/", "/system/xbin/", "/data/local/xbin/",
	                "/data/local/bin/", "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/"};
	        for (String where : places) {
	            if ( new File( where + binaryName ).exists() ) {
	                found = true;
	                break;
	            }
	        }
	    }
	    return found;
	}
}
