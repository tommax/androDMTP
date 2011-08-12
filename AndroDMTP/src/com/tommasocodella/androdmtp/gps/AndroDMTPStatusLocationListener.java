package com.tommasocodella.androdmtp.gps;


import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.TextView;

public class AndroDMTPStatusLocationListener  implements LocationListener {
	private int TIME_INTERVAL = 1000 * 60 * 1;
	private boolean isNewLocation;
	private Location currentBestLocation;
	private TextView latitudeNow;
	private TextView longitudeNow;
	private TextView altitudeNow;
	private TextView accuracyNow;
	private TextView speedNow;


	public AndroDMTPStatusLocationListener(){
		super();
		currentBestLocation = null;
		isNewLocation = true;
	}



	public void onLocationChanged(Location location) {
		if (currentBestLocation == null) {
			currentBestLocation = location;
			isNewLocation = true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TIME_INTERVAL;
		boolean isSignificantlyOlder = timeDelta < -TIME_INTERVAL;
		boolean isNewer = timeDelta > 0;
		
		// If it's been more than two minutes since the current location, use the new location
		// because the user has likely moved
		
		if (isSignificantlyNewer) {
			currentBestLocation = location;
			isNewLocation = true;
			// If the new location is more than two minutes older, it must be worse
		} else if (isSignificantlyOlder) {}
		
		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;
		
		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(),currentBestLocation.getProvider());
		
		// Determine location quality using a combination of timeliness and accuracy
		if (isMoreAccurate) {
			currentBestLocation = location;
			isNewLocation = true;
		} else if (isNewer && !isLessAccurate) {
			currentBestLocation = location;
			isNewLocation = true;
		} else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
			currentBestLocation = location;
			isNewLocation = true;
		}
		
		if(latitudeNow!=null){
			latitudeNow.setText("" + currentBestLocation.getLatitude());
		}
		
		if(longitudeNow!=null){
			longitudeNow.setText("" + currentBestLocation.getLongitude());
		}
		
		if(altitudeNow!=null){
			altitudeNow.setText("" + currentBestLocation.getAltitude());
		}
		
		if(accuracyNow!=null){
			accuracyNow.setText("" + currentBestLocation.getAccuracy());
		}
		
		if(speedNow!=null){
			speedNow.setText("" + currentBestLocation.getSpeed());
		}
		
	}
	
	public boolean isLocationPresent(){
		if(currentBestLocation == null)
			return false;
		
		return true;
	}
	
	public void onProviderDisabled(String provider) {
	}
	
	public void onProviderEnabled(String provider) {
	}
	
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}
	
	public double getLatitude(){
		return currentBestLocation.getLatitude();
	}
	
	public double getLongitude(){
		return currentBestLocation.getLongitude();
	}
	
	public double getAccuracy(){
		return currentBestLocation.getAccuracy();
	}
	
	public float getSpeed(){
		return currentBestLocation.getSpeed();
	}
	
	public double getHeading(){
		return currentBestLocation.getBearing();
	}
	
	public double getAltitude(){
		return currentBestLocation.getAltitude();
	}
	
	public long getTimestamp(){
		return currentBestLocation.getTime();
	}

	public void setLatitudeNow(TextView latitudeNow) {
		this.latitudeNow = latitudeNow;
	}

	public void setNewLocation(boolean isNewLocation) {
		this.isNewLocation = isNewLocation;
	}

	public void setCurrentBestLocation(Location currentBestLocation) {
		this.currentBestLocation = currentBestLocation;
	}

	public void setLongitudeNow(TextView longitudeNow) {
		this.longitudeNow = longitudeNow;
	}

	public void setAltitudeNow(TextView altitudeNow) {
		this.altitudeNow = altitudeNow;
	}

	public void setAccuracyNow(TextView accuracyNow) {
		this.accuracyNow = accuracyNow;
	}

	public void setSpeedNow(TextView speedNow) {
		this.speedNow = speedNow;
	}
	
}