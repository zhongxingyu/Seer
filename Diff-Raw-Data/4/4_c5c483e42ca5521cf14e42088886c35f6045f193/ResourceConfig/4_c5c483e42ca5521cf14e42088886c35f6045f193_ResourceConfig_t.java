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
			pathParts.length >= 2 ? pathParts[1] : null,
			pathParts.length >= 3 ? pathParts[2] : null);
 	}
 	
 	public boolean isSameClient(String resourceDescriptor) {
 		return isSameClient(ResourceConfig.parse(resourceDescriptor));
 	}
 	
 	public boolean isSameClient(ResourceConfig config) {
 		if (config == null)
 			return false;
 		
 		return clientType == config.clientType && id.equalsIgnoreCase(config.id);
 	}
 
 	@Override
 	public String toString() {
 		return "ResourceConfig [clientType=" + clientType + ", id=" + id + ", key=" + key + "]";
 	}
 }
