package com.tommasocodella.androdmtp.opendmtp.client.gps;

import com.tommasocodella.androdmtp.opendmtp.util.Log;

public class GPSUtils {
	private static final String  LOG_NAME = "GPS";
	
	private static boolean stale = false;
	private static GPSUtils DMTP_GPSUtils = null;
	private static long lastSampleTime = 0L;
	private static long lastValidTime = 0L;
	
	public static GPSUtils init(){
        if (DMTP_GPSUtils == null) {
        	DMTP_GPSUtils = new GPSUtils();
        }
        return DMTP_GPSUtils;
    }
	
	public static GPSUtils getInstance(){
        if (DMTP_GPSUtils == null) {
            Log.error(LOG_NAME, "GPSReceiver uninitialized!");
        }
        return DMTP_GPSUtils;
    }
	
	public static boolean isGpsStale() {
		return GPSUtils.stale;
	}

	public static void setGpsStale(boolean stale) {
		GPSUtils.stale = stale;
	}

	public static long getLastSampleTime() {
		return lastSampleTime;
	}

	public static void setLastSampleTime(long lastSampleTime) {
		GPSUtils.lastSampleTime = lastSampleTime;
	}

	public static long getLastValidTime() {
		return lastValidTime;
	}

	public static void setLastValidTime(long lastValidTime) {
		GPSUtils.lastValidTime = lastValidTime;
	}
	
}
