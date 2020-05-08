 package com.trugertech.quickbart;
 

 public class BartAPI_URIGenerator {
 	protected static final String API_KEY = "YRSI-ZII8-QHUQ-JXYX";
 	protected static final String URI_ROOT = "http://api.bart.gov/api/";
 	
 	public static final String getCmd_Sched(String orig, String dest){
		return URI_ROOT + "sched.aspx?cmd=depart&orig=" + orig + "&dest=" + dest + "&a=4&b=1&key=" + API_KEY;
 	}
 	
 	public static final String getCmd_Stn(){
 		return URI_ROOT + "stn.aspx?cmd=stns&key=" + API_KEY;
 	}
 
 }
