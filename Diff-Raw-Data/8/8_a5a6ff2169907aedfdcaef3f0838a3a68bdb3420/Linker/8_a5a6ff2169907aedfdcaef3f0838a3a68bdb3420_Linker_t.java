 package uk.ac.ids.linker;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.restlet.data.Reference;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import com.google.appengine.api.datastore.PreparedQuery;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.Query.FilterOperator;
 
 public abstract class Linker {
 	// Logger instance
 	protected static final Logger logger = Logger.getLogger(Linker.class.getName());
 
 	// Entity type in the AppEngine data store
 	private final static String DS_ENTITY = "ExternalResource";
 
 	// Property for the mapped resources
 	private final static String RESOURCE_PROPERTY = "Resources";
 
 	// Property for the parameters
 	private final static String PARAMETER_PROPERTY = "Parameters";
 
 	/**
 	 * @param countryName
 	 * @param countryCode
 	 * @return
 	 */
 	public List<Reference> getResource(LinkerParameters parameters) {
 		logger.info("Get " + parameters);
 		try {
 			// Try to return the result from the cache
 			return getFromCache(parameters);
 		} catch (EntityNotFoundException e) {
 			// Try to get it from geoname
 			List<Reference> uri = getFromService(parameters);
 
 			// If successful, save it
 			if (uri != null)
 				saveToCache(parameters, uri);
 
 			return uri;
 		}
 
 	}
 
 	/**
 	 * @param parameters
 	 * @return
 	 */
 	protected abstract List<Reference> getFromService(LinkerParameters parameters);
 
 	/**
 	 * @param parameters
 	 * @return
 	 * @throws EntityNotFoundException
 	 */
 	private List<Reference> getFromCache(LinkerParameters parameters) throws EntityNotFoundException {
 		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 
 		// Get the data
 		Query q = new Query(DS_ENTITY);
 		q.addFilter(PARAMETER_PROPERTY, FilterOperator.EQUAL, parameters.toKey());
 		PreparedQuery pq = datastore.prepare(q);
 		Entity entity = pq.asSingleEntity();
 		if (entity == null)
 			throw new EntityNotFoundException(null);
 
 		// De-serialize the data
		List<Reference> results = new ArrayList<Reference>();
 		@SuppressWarnings("unchecked")
 		List<String> uris = (List<String>) entity.getProperty(RESOURCE_PROPERTY);
		if (uris != null)
			for (String uri : uris)
				results.add(new Reference(uri));
 		return results;
 	}
 
 	/**
 	 * @param parameters
 	 * @param uri
 	 */
 	private void saveToCache(LinkerParameters parameters, List<Reference> uris) {
 		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 
 		// Serialize the uris
 		List<String> results = new ArrayList<String>();
 		for (Reference uri : uris)
 			results.add(uri.toString());
 
 		// Persist the data
 		Entity entity = new Entity(DS_ENTITY);
 		entity.setProperty(PARAMETER_PROPERTY, parameters.toKey());
 		entity.setProperty(RESOURCE_PROPERTY, results);
 		datastore.put(entity);
 	}
 
 }
