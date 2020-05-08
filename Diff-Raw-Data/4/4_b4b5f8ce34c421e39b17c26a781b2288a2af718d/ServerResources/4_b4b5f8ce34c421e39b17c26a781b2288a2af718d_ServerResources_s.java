 package il.technion.ewolf.server;
 
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.activation.FileTypeMap;
 import javax.activation.MimetypesFileTypeMap;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.PropertiesConfiguration;
 
 public class ServerResources {	
 	private static final String EWOLF_CONFIG = "ewolf.config.properties";
 	private static final String MIME_TYPES = "/mime.types";
 	
 	public static class EwolfConfigurations {
 		public String username;
 		public String password;
 		public String name;
 		public List<URI> kbrURIs = new ArrayList<URI>();
 	}
 	
	//FIXME infinite loop!
 	static public URL getResource(String name) {
		return ServerResources.getResource(name);
 	}
 	
 	public static EwolfConfigurations getConfigurations() {
 		EwolfConfigurations configurations = new EwolfConfigurations();
 		
 		try {
 			PropertiesConfiguration config = new PropertiesConfiguration(EWOLF_CONFIG);
 			configurations.username = config.getString("username");
 			configurations.password = config.getString("password");
 			configurations.name = config.getString("name");
 			
 			for (Object o: config.getList("kbr.urls")) {
 				configurations.kbrURIs.add(new URI((String)o));
 			}
 			if (configurations.username == null) {
 				//TODO get username/password from user, store to EWOLF_CONFIG and continue
 			}
 		} catch (ConfigurationException e2) {
 			// TODO Auto-generated catch block
 			e2.printStackTrace();
 			System.out.println("Cant' read configuration file");
 			return null;
 		} catch (URISyntaxException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			System.out.println("String from configuration file could not be parsed as a URI");
 			return null;
 		}
 		
 		return configurations;
 	}
 	
 	public static FileTypeMap getFileTypeMap() {
 		MimetypesFileTypeMap map;
 		try {
 			URL mime = EwolfServer.class.getResource(MIME_TYPES);			
 			map = new MimetypesFileTypeMap(mime.openStream());
 		} catch (IOException e1) {
 			map = new MimetypesFileTypeMap();
 		}
 		return map;
 	}
 }
