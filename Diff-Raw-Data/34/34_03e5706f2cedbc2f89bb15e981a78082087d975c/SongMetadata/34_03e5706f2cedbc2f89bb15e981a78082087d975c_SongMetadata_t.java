 package com.lastcrusade.fanclub.model;
 
 public class SongMetadata {
 
     private long id;
 
     private String title;
 
     private String artist;
 
     private String album;
 
    // macAddress of person who added song to playlist
    private String macAddress; 
     
     public SongMetadata() {}
 
     public SongMetadata(long aId, String aTitle, String aArtist, String aAlbum, String aMacAddress) {
     	//be careful setting the song ID
         this.id = aId;
     	this.title = aTitle;
     	this.artist = aArtist;
     	this.album = aAlbum;
     	this.macAddress = aMacAddress;
     }
 
     public long getId() {
         return id;
     }
 
     public void setId(long id) {
         this.id = id;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public String getArtist() {
         return artist;
     }
 
     public void setArtist(String artist) {
         this.artist = artist;
     }
 
     public String getAlbum() {
         return album;
     }
 
     public void setAlbum(String album) {
         this.album = album;
     }
 
     public String getMacAddress() {
         return macAddress;
     }
 
     public void setMacAddress(String macAddress) {
         this.macAddress = macAddress;
     }
 
 }
