 /**
  * Copyright (C) 2009-2011 Kenneth Prugh
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  */
 
 package config;
 
 import java.io.FileInputStream;
 import java.util.Properties;
 
 public class Config {
     private static Config _instance;
 
 	private Properties config;
 	private FileInputStream in;
 
 	private final String network;
 	private final String port;
 	private final String ident;
 	private final String identpassword;
 	private final String dbpath, dbuser, dbpass;
 	private final String channels;
 	private final String OS, Contact; /* CTCP */
 
     /* Perhaps specify the file in the constructor to allow multiple configs */
 	private Config() {
 		config = new Properties();
 		try {
 			in = new FileInputStream(".settings");
 			config.load(in);
 			in.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 
         /* Setup */
         network = config.getProperty("network");
         port = config.getProperty("port");
         ident = config.getProperty("ident");
         identpassword = config.getProperty("identpassword");
         dbpath = config.getProperty("dbpath");
         dbuser = config.getProperty("dbuser");
         dbpass = config.getProperty("dbpass");
         channels = config.getProperty("channels");
         OS = config.getProperty("OS");
         Contact = config.getProperty("Contact");
 	}
 
 	/**
      * @return the config
      */
     public static synchronized Config getInstance() {
         if (_instance == null) {
             _instance = new Config();
         }
         else {
             return _instance;
         }
     }
 
 	/**
 	 * @return the network
 	 */
 	public String getNetwork() {
 		return network;
 	}
 
 	/**
 	 * @return the port
 	 */
 	public String getPort() {
 		return port;
 	}
 
 	/**
 	 * @return the ident
 	 */
 	public String getIdent() {
 		return ident;
 	}
 
 	/**
 	 * @return the identpassword
 	 */
 	public String getIdentpassword() {
 		return identpassword;
 	}
 
 	/**
 	 * @return the dbpath
 	 */
 	public String getDbpath() {
 		return dbpath;
 	}
 
 	/**
 	 * @return the dbuser
 	 */
 	public String getDbuser() {
 		return dbuser;
 	}
 
 	/**
 	 * @return the dbpass
 	 */
 	public String getDbpass() {
 		return dbpass;
 	}
 
 	/**
 	 * @return the channels
 	 */
 	public String getChannels() {
 		return channels;
 	}
 
 	/**
 	 * @return the oS
 	 */
 	public String getOS() {
 		return OS;
 	}
 
 	/**
 	 * @return the contact
 	 */
 	public String getContact() {
 		return Contact;
 	}
 }
