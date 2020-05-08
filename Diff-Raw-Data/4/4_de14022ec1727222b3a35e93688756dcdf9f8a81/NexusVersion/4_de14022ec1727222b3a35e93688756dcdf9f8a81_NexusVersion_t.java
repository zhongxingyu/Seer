 package com.nexus;
 
 
 public class NexusVersion{
 	
 	//This number is incremented every time we add a new major functionality, never reset
 	public static final int majorVersion = 4;
 	//This number is incremented every time we do some major api changes, resets every major version
 	public static final int minorVersion = 3;
 	//This number is incremented every time we throw out a release for some bugfixes/small additions, resets every minor version
 	public static final int revisionVersion = 3;
 	//This number is incremented every jenkins build. In the code this should be 0
 	public static final int buildVersion = 0;
 	
	public static final boolean IsDevelopmentVersion = (System.getProperty("nexus.version.development", "false").equalsIgnoreCase("true"));
	
 	public static String getVersion(){
 		return String.format("%d.%d.%d.%d", majorVersion, minorVersion, revisionVersion, buildVersion);
 	}
 }
