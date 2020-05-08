 package com.tantaman.ferox.webfinger.example.standalone;
 
 import java.io.IOException;
 import java.util.Dictionary;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.service.cm.Configuration;
 import org.osgi.service.cm.ConfigurationAdmin;
 
 public class ExampleServer {
 	private static BundleContext context;
 
 	static BundleContext getContext() {
 		return context;
 	}
 	
 	public void setConfigAdmin(ConfigurationAdmin configAdmin) {
		System.out.println("Config admin set.");
 		Configuration config;
 		try {
 			config = configAdmin.createFactoryConfiguration("ferox.SimpleFilesystemResourceProvider");
 			Dictionary<String, Object> dict = config.getProperties();
 			
 			dict.put("contentType", "application/json");
 			dict.put("metaPath", "resources/webfinger/meta.json");
 			dict.put("identityRoot", "resources/webfinger/identities");
 			
 			config.update(dict);
			System.out.println("Configuration updated");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void activate() {
 		
 	}
 }
