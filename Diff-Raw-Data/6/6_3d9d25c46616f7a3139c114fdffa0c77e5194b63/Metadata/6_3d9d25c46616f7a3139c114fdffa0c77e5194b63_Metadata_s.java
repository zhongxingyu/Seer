 package interdroid.vdb.content.metadata;
 
 import interdroid.vdb.content.EntityUriMatcher.UriMatch;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public abstract class Metadata {
 	private static final Logger logger = LoggerFactory.getLogger(Metadata.class);
 
 	// The entities that live inside this database
 	protected Map<String, EntityInfo> entities = new HashMap<String, EntityInfo>();
 	protected Map<String, String> namespaces = new HashMap<String, String>();
 
 	// The name of the database.
 	public final String namespace_;
 
 	protected Metadata(String namespace) {
 		logger.debug("Constructed metadata namespace: " + namespace);
 		namespace_ = namespace;
 		namespaces.put(namespace_, namespace_);
 	}
 
 	public Collection<EntityInfo> getEntities() {
 		return entities.values();
 	}
 
 	public EntityInfo getEntity(String name) {
 		if (logger.isDebugEnabled()) {
 			logger.debug("Checking for entity: " + name);
 //			for (String key : entities.keySet()) {
 //				logger.debug("Key: " + key);
 //			}
 		}
 
 		EntityInfo result = entities.get(name);
 
 		// Check the namespaces then if we are lacking a match
		if (result == null && !name.startsWith(namespace_)) {
 			for (String namespace : namespaces.keySet()) {
 				if (logger.isDebugEnabled())
 					logger.debug("Checking for entity: " + namespace + "." + name);
 				result = entities.get(namespace + "." + name);
 				if (result != null) {
 					break;
 				}
 			}
 		}
 
 		if (result == null && logger.isDebugEnabled()) {
 			logger.debug("Not found...");
 			for (String key : entities.keySet()) {
 				logger.debug("Existing Entity: " + key);
 			}
 		}
 
 		return result;
 	}
 
 	public EntityInfo getEntity(UriMatch uriMatch) {
 		if (logger.isDebugEnabled())
 			logger.debug("Checking for entity: " + uriMatch.entityName);
 
 		EntityInfo result = getEntity(uriMatch.entityName);
 
 		// Check if this is something inside the parent type(s).
 		if (uriMatch.parentEntityNames != null) {
 			String name = uriMatch.entityName;
 			for (String parent : uriMatch.parentEntityNames){
 				name = parent + "_" + name;
 			}
 			result = getEntity(name);
 		}
 
 		return result;
 	}
 
 	public void put(EntityInfo entityInfo) {
 		if (getEntity(entityInfo.getFullName()) == null) {
 			if (logger.isDebugEnabled())
 				logger.debug("Adding entity: " + entityInfo.getFullName());
 			entities.put(entityInfo.getFullName(), entityInfo);
 			if (!namespaces.containsKey(entityInfo.namespace())) {
 				namespaces.put(entityInfo.namespace(), entityInfo.namespace());
 			}
 		}
 	}
}
