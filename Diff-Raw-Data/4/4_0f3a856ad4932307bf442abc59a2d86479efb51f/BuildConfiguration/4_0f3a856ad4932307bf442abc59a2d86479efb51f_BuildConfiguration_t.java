 package com.xaf;
 
 public class BuildConfiguration
 {
 	public static final String productName = "SparX";
 	public static final String productId   = "xaf";
 
 	public static final int releaseNumber = 1;
 	public static final int versionMajor = 2;
	public static final int versionMinor = 3;
	public static final int buildNumber = 15;
 
 	static public final String getBuildPathPrefix()
 	{
 		return productId + "-" + releaseNumber + "_" + versionMajor + "_" + versionMinor + "-" + buildNumber;
 	}
 
 	static public final String getBuildFilePrefix()
 	{
 		return productId + "-" + releaseNumber + "_" + versionMajor + "_" + versionMinor;
 	}
 
 	static public final String getVersion()
 	{
 		return releaseNumber + "." + versionMajor + "." + versionMinor;
 	}
 
 	static public final String getVersionAndBuild()
 	{
 		return "Version " + getVersion() + " Build " + buildNumber;
 	}
 
 	static public final String getProductBuild()
 	{
 		return productName + " Version " + getVersion() + " Build " + buildNumber;
 	}
 }
