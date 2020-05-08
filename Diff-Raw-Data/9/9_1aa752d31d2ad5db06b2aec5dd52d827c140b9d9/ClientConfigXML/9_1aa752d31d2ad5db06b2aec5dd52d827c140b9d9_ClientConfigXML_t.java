 package com.sri.straylight.client.model;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 
 import com.thoughtworks.xstream.XStream;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class ClientConfigXML.
  */
 public class ClientConfigXML {
 	
 	/** The Constant configFile_. */
 	private static final String configFile_ = "client-config.xml";
 	
 	/** The Constant VERSION. */
	public static final String VERSION = "Alpha 0.51";
 
 	/** The fmu file path. */
 	public String fmuFilePath = "";
 	
 	/** The window icon file name. */
 	public String windowIconFileName;
 	
 	/** The auto connect string. */
 	public String autoConnectString;
 	
 	/** The auto parse xml string. */
 	public String autoParseXMLString;
 	
 	/** The auto init string. */
 	public String autoInitString;
 	
 	
 	/**
 	 * Load.
 	 *
 	 * @return the client config
 	 */
 	public static ClientConfig load() {
 		
 		String theOs = System.getProperty("os.name");
 		System.out.println( "ClientConfig detected operating system : " + theOs);
 		
 		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
 		InputStream is = classLoader.getResourceAsStream(configFile_);
 		
 		XStream xStream = new XStream();
 		xStream.alias("config", com.sri.straylight.client.model.ClientConfigXML.class);
 		ClientConfigXML configXML;
 		
 		try {
 			
 			Object object = xStream.fromXML(is);
 					
 			configXML = (ClientConfigXML)object;
 			is.close();
 			
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		}
 		
 		ClientConfig clientConfig = new ClientConfig();
 		//clientConfig.connectTo = ConnectTo.connecTo_localhost;
 
 		clientConfig.windowIconUrl = classLoader.getResource(configXML.windowIconFileName);
 		clientConfig.autoConnectFlag = Boolean.valueOf(configXML.autoConnectString);
 		clientConfig.autoParseXMLFlag = Boolean.valueOf(configXML.autoParseXMLString);
 		clientConfig.autoInitFlag = Boolean.valueOf(configXML.autoInitString);
 		
 		
 		
 		return clientConfig;
 	}
 	
 
 	
 }
