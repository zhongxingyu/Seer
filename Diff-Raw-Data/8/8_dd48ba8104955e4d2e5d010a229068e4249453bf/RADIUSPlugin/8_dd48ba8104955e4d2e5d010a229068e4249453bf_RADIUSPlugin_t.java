 package com.adito.radius;
 
 import com.adito.extensions.ExtensionException;
 import com.adito.extensions.types.DefaultPlugin;
 import com.adito.policyframework.*;
 import com.adito.security.AuthenticationModuleManager;
 import net.sf.jradius.packet.attribute.AttributeFactory;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class RADIUSPlugin extends DefaultPlugin
 {
 	static final Log log = LogFactory.getLog(RADIUSPlugin.class);
	public static final String BUNDLE_ID = "radius";
 	public static final String MESSAGE_RESOURCES_KEY = "radius";
	public static final String LICENSE_NAME = "RADIUS Authentication";
 	@SuppressWarnings("unchecked")
 	public static final ResourceType RADIUS_AUTH_RESOURCE_TYPE = new DefaultResourceType(7654, "radius", "personal");
 
 	//RADIUSPlugin Constructor
 	public RADIUSPlugin()
 	{
		super("/WEB-INF/radius-tiles-defs.xml", true);
 	}
 
 	//Activate RADIUS Plugin
 	public void activatePlugin() throws ExtensionException
 	{
 		log.info("Initializing Avantage RADIUSPlugin...");
 
 		try
 		{
 		
 			if(log.isInfoEnabled())
 				log.info("Starting Avantage RADIUS Authentication plugin");
 					
 			try
 			{
 				PolicyDatabaseFactory.getInstance().registerResourceType(RADIUS_AUTH_RESOURCE_TYPE);
 			}
 			catch(Exception exception)
 			{
 				throw new ExtensionException(15001, "radius", exception);
 			}
 	
 			if(log.isInfoEnabled())
 				log.info("Loading RADIUSAuthenticationModule");
 	
 			//Start RADIUSAuthenticationModule
 			try
 			{
 				AuthenticationModuleManager.getInstance().registerModule("RADIUS", RADIUSAuthenticationModule.class, "radius", true, true, false);
 			} catch (Exception e)
 			{
 				if(log.isInfoEnabled())
 					log.info("AuthenticationModuleManager exception (RADIUSAuthenticationModule): " + e);
 				
 			}
 	
 			if(log.isInfoEnabled())
 				log.info("Loading AttributeDictionary (net.sf.jradius.dictionary.AttributeDictionary)");
 
 			//Load AttributeDictionary ( moved to /lib/jradius.jar )
 			try
 			{
 				AttributeFactory.loadAttributeDictionary("net.sf.jradius.dictionary.AttributeDictionaryImpl");
 			} 
 			catch (Exception e)
 			{
 				if(log.isInfoEnabled())
 					log.info("Dictionary exception: " + e);
 			}
 
 		} catch(Exception e)
 		{
 			if(log.isInfoEnabled())
 				log.info("Error while starting Avantage RADIUSPlugin: " + e);			
 			
 			throw new ExtensionException(ExtensionException.FAILED_TO_LAUNCH, e);
 		}
 	
 	}
 
 	//Stop RADIUS Plugin	
 	public void stopPlugin() throws ExtensionException
 	{
 		if(log.isInfoEnabled())
 			log.info("Stopping Avantage RADIUSPlugin...");
 
 		try
 		{
 			super.stopPlugin();
 		    AuthenticationModuleManager.getInstance().deregisterModule("RADIUS");
 
 			if(log.isInfoEnabled())
 				log.info("Avantage RADIUSPlugin stopped successfully");
 		    
 		} 
 		catch (Exception e)
 		{
 			if(log.isInfoEnabled())
 				log.info("Error while stopping Avantage RADIUSPlugin: " + e);			
 			
 			throw new ExtensionException(ExtensionException.INTERNAL_ERROR, e);
 		}
 		
 	}
 
 }
