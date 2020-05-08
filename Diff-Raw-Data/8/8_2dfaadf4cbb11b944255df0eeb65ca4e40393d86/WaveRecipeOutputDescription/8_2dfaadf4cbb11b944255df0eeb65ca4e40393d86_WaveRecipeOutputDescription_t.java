 // 
 //  WaveRecipeOutputDescription.java
 //  AndroidWaveProject
 //  
 //  Created by Philip Kuryloski on 2011-02-22.
 //  Copyright 2011 Philip Kuryloski. All rights reserved.
 // 
 
 package edu.berkeley.androidwave.waveclient;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 import java.util.Vector;
 
 /**
  * WaveRecipeOutputDescription
  * 
  * General representation of one output from a WaveRecipe
  */
 public class WaveRecipeOutputDescription implements Parcelable {
     
     protected String name;
     protected String units;
     
    protected Vector<WaveRecipeOutputChannelDescription> channels;
     
     public WaveRecipeOutputDescription(String name, String units) {
         this.name = name;
         this.units = units;
         
        channels = new Vector<WaveRecipeOutputChannelDescription>();
     }
     
     /**
      * getName
      */
     public String getName() {
         return name;
     }
     
     /**
      * getUnits
      */
     public String getUnits() {
         return units;
     }
     
     /**
      * getChannels
      */
     public WaveRecipeOutputChannelDescription[] getChannels() {
        return channels.toArray(new WaveRecipeOutputChannelDescription[0]);
     }
 
     /**
      * addChannel
      */
     public boolean addChannel(WaveRecipeOutputChannelDescription c) {
         return channels.add(c);
     }
     
     /**
      * Parcelable Methods
      */
     public int describeContents() {
         return 0;
     }
     
     public void writeToParcel(Parcel dest, int flags) {
         
     }
     
     public static final Parcelable.Creator<WaveRecipeOutputDescription> CREATOR = new Parcelable.Creator<WaveRecipeOutputDescription>() {
         public WaveRecipeOutputDescription createFromParcel(Parcel in) {
             return new WaveRecipeOutputDescription(in);
         }
         
         public WaveRecipeOutputDescription[] newArray(int size) {
             return new WaveRecipeOutputDescription[size];
         }
     };
     
     private WaveRecipeOutputDescription(Parcel in) {
         
     }
 }
