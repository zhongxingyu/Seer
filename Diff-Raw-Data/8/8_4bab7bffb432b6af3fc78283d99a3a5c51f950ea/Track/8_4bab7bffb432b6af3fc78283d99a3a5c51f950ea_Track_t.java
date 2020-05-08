 package com.mattwaqar.audioguide.models;
 
 import com.google.android.gms.maps.model.LatLng;
 
 public interface Track {
 	
 	public String getId();
 	
 	public String getTitle();
 	public void setTitle(String title);
 	
 	public String getDescription();
 	public void setDescription(String description);
 	
 	public String getAudioUri();
 	public void saveAudio(byte[] audio);
 	public void deleteAudio();
 	
 	public LatLng getLatLng();
 	public void setLatLng(LatLng latLng);
 	
 	public void saveInBackground();
 	public void deleteInBackground();
 }
