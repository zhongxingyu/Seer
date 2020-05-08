 package tdanford.json.schema;
 
 import java.util.*;
 
public JSONObjectType implements JSONType { 
 
 	private SchemaEnv env;
 	private String name;
 	private String description;
 	private Map<String,JSONType> properties;
 
 	public JSONObjectType(SchemaEnv env, JSONObject schema) { 
 		this.env = env;
 		this.name = null;
 		this.description = null;
 		properties = new TreeMap<String,JSONType>();
 	}
 	
 	public boolean contains(Object obj) { 
 		if(obj == null || !(obj instanceof JSONObject)) { 
 			return false; 
 		}
 
 		return true;
 	}
 }
