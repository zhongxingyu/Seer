 package com.grikly;
 
 public enum URL {
 
 	ACCOUNT_REGISTER ("http://api.grik.ly/v1/Account/Register"),
 	ACCOUNT_EMAIL_CHECK ("http://api.grik.ly/v1/Account/EmailExist"),
 	ACCOUNT_LOGIN("http://api.grik.ly/v1/Account/Login"),
 	USER ("http://api.grik.ly/v1/Users/%d"),
	CARD_BASE ("http://api.grik.ly/v1/Users/"),
 	CARD ("http://api.grik.ly/v1/Cards/%d");
 	
 	private final String url;
 	
 	private URL(String url)
 	{
 		this.url = url;
 	}
 	
 	@Override
 	public String toString ()
 	{
 		return url;
 	}
 }//end URL enum
