 package com.github.aprestaux.funreco.api;
 
 import java.util.Date;
 
 public class Action {
 	private Object object;
 
     private Date date;
 
     private Profile profile;
 
     public Action() {
     }
 
    public Action(Object object, Profile profile) {
         this.object = object;
         this.date = new Date();
        this.profile = profile;
     }
 
 	public Object getObject() {
 		return object;
 	}
 
 	public void setObject(Object object) {
 		this.object = object;
 	}
 
 	public Date getDate() {
 		return date;
 	}
 
 	public void setDate(Date date) {
 		this.date = date;
 	}
 
     public Profile getProfile() {
         return profile;
     }
 
     public void setProfile(Profile profile) {
         this.profile = profile;
     }
 }
