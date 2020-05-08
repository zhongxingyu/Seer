 package com.uvs.coffeejob;
 
 public class User {
 	private String mName;
 	private String mSurname;
 	private String mBio;
 	
 	// setters
 	public void setName(String name) {
 		mName = name;
 	}
 	
 	public void setSurname(String surname) {
 		mSurname = surname;
 	}
 	
 	public void setBio(String bio) {
 		mBio = bio;
 	}
 	
 	// getters
	public String getName() {
 		return mName;
 	}
 	
	public String getSurname() {
 		return mSurname;
 	}
 	
	public String getBio() {
 		return mBio;
 	}
 }
