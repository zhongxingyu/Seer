 package btwmod.centralchat;
 
 public class ResourceConfig {
 	
 	public final ClientType clientType;
 	public final String id;
 	public final String key;
 	
 	protected ResourceConfig(ClientType clientType, String id, String key) {
 		this.clientType = clientType;
 		this.id = id;
 		this.key = key;
 	}
 	
 	public static ResourceConfig parse(String resourceDescriptor) {
 		String[] pathParts = resourceDescriptor == null ? new String[0] : resourceDescriptor.split("\\?")[0].replaceFirst("^/", "").split("/");
 		
 		return new ResourceConfig(
 			pathParts.length >= 1 ? ClientType.get(pathParts[0]) : null,
			pathParts.length >= 1 ? pathParts[0] : null,
			pathParts.length >= 1 ? pathParts[0] : null);
 	}
 }
