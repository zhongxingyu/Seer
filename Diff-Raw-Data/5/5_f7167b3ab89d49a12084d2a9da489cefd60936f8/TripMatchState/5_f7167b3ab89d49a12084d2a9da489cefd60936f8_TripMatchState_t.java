 package models;
 
 import com.avaje.ebean.annotation.EnumValue;
 
 public enum TripMatchState {
    @EnumValue("MATCHED") MATCHED, 
    @EnumValue("OPEN") OPEN
 }
