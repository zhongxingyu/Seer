 package com.mda.coordinatetracker.geocoder.dto;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 import com.google.gson.annotations.SerializedName;
 
 public class Location implements Parcelable {
 
     @SerializedName("lat")
     private Double lat;
     @SerializedName("lng")
     private Double lng;
 
    public Double getLat() {
         return lat;
     }
 
     public void setLat(double lat) {
         this.lat = lat;
     }
 
    public Double getLng() {
         return lng;
     }
 
     public void setLng(double lng) {
         this.lng = lng;
     }
 
     @Override
     public int describeContents() {
         return 0;
     }
 
     @Override
     public void writeToParcel(Parcel parcel, int flags) {
         parcel.writeDouble(lat);
         parcel.writeDouble(lng);
     }
 
     public static final Parcelable.Creator<Location> CREATOR =
             new Parcelable.Creator<Location>() {
                 @Override
                 public Location createFromParcel(Parcel in) {
                     Location l = new Location();
                     l.lat = in.readDouble();
                     l.lng = in.readDouble();
                     return l;
                 }
 
                 @Override
                 public Location[] newArray(int size) {
                     return new Location[size];
                 }
             };
 
 }
