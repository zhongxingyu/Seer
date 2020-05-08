 package com.java.gwt.libertycinema.shared;
 
 
 import java.io.Serializable;
 
 
 public class LoginInfo implements Serializable {
 
     private boolean loggedIn = false;
     private boolean adminUser = false;
     private String loginUrl;
     private String logoutUrl;
     private String emailAddress;
     private String nickname;
 
     public boolean isLoggedIn() {
	return loggedIn;
     }
 
     public void setLoggedIn(boolean loggedIn) {
	this.loggedIn = loggedIn;
     }
 
     public void setAdminUser(boolean adminUser) {
	this.adminUser = adminUser;
     }
 
     public boolean isAdminUser() {
	return adminUser;
     }
 
     public String getLoginUrl() {
	return loginUrl;
     }
 
     public void setLoginUrl(String loginUrl) {
	this.loginUrl = loginUrl;
     }
 
     public String getLogoutUrl() {
	return logoutUrl;
     }
 
     public void setLogoutUrl(String logoutUrl) {
	this.logoutUrl = logoutUrl;
     }
 
     public String getEmailAddress() {
	return emailAddress;
     }
 
     public void setEmailAddress(String emailAddress) {
	this.emailAddress = emailAddress;
     }
 
     public String getNickname() {
	return nickname;
     }
 
     public void setNickname(String nickname) {
	this.nickname = nickname;
     }
 }
