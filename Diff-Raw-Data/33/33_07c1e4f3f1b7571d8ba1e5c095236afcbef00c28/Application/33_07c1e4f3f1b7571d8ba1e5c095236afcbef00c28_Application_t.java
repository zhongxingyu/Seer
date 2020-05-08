 package com.orissa.application;
 
 /**
  * Represents the application and associated code base.
  */
 public final class Application
 {
 	/**
	 * Returns a string.
	 * 
	 * @return returns a string
	 */
	public native String getMyString();

	/**
 	 * Returns the major version number of the application.
 	 * 
 	 * @return the major version number of the application
 	 */
 	public native int getMajorVersion();
 
 	/**
 	 * Returns the minor version number of the application.
 	 * 
 	 * @return the minor version number of the application
 	 */
 	public native int getMinorVersion();
 
 	/**
 	 * Disable default constructor.
 	 */
	public Application()
 	{
 	}
 }
