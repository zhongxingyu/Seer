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
	String getName() {
 		return mName;
 	}
 	
	String getSurname() {
 		return mSurname;
 	}
 	
	String getBio() {
 		return mBio;
 	}
 }
