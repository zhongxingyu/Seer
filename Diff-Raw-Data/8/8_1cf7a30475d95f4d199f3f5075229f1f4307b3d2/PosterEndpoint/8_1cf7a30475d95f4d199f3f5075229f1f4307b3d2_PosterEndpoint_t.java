 package com.gatech.faceme.endpoints;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.inject.Named;
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 
 import com.gatech.faceme.entity.ImageUploadURL;
 import com.gatech.faceme.entity.OriginalPoster;
 import com.gatech.faceme.entity.PosterEntity;
 import com.gatech.faceme.mediastore.PMF;
 import com.google.api.server.spi.config.Api;
 import com.google.api.server.spi.config.ApiMethod;
 import com.google.appengine.api.blobstore.BlobstoreService;
 import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
 
 @Api(name = "posterendpoint", description = "This entity represents an poster.", version = "v1")
 public class PosterEndpoint {
 	private BlobstoreService blobstoreService = BlobstoreServiceFactory
 			.getBlobstoreService();
 
 	/**
 	 * Get a list of posters from PosterEntity in Google Datastore and return
 	 * List of this entity.
 	 * 
 	 * @return List<PosterEntity>
 	 */
 	@ApiMethod(httpMethod = "GET", name = "poster.list", path = "poster/list")
 	@SuppressWarnings({ "cast", "unchecked" })
 	public List<PosterEntity> listOriginalPoster() {
 		PersistenceManager mgr = PMF.get().getPersistenceManager();
 		List<PosterEntity> result = new ArrayList<PosterEntity>();
 		try {
 			Query query = mgr.newQuery(PosterEntity.class);
 			for (Object obj : (List<Object>) query.execute()) {
 				result.add(((PosterEntity) obj));
 			}
 		} finally {
 			mgr.close();
 		}
 		return result;
 	}
 	/**
 	 * Get a list with fixed number of posters from PosterEntity in Google Datastore and return
 	 * List of this entity.
 	 * 
 	 * @return List<PosterEntity>
 	 */
 	@ApiMethod(httpMethod = "GET", name = "poster.#list", path = "poster/list/{startPoint}/{quantity}")
 	@SuppressWarnings({ "cast", "unchecked" })
 	public List<PosterEntity> listPosterWithGivenQuanlity(@Named("startPoint") int start, 
 					@Named("quantity") int number) {
 		PersistenceManager mgr = PMF.get().getPersistenceManager();
 		List<PosterEntity> result = new ArrayList<PosterEntity>();
		if(start<=0||number==0) return result;

 		try {
 			Query query = mgr.newQuery(PosterEntity.class);
 			int count=0;
 			for (Object obj : (List<Object>) query.execute()) {
 				if(count<start-1) continue;
 				result.add(((PosterEntity) obj));
 				count++;
 				if(count==(start+number-1)) break;
 			}
 		} finally {
 			mgr.close();
 		}
 		return result;
 	}
 	
 	/**
 	 * Return a specific PosterEntity according to the blobkey in GET method
 	 * 
 	 * @param BlobKey
 	 * @return PosterEntity
 	 */
 	@ApiMethod(httpMethod = "GET", name = "poster.get", path = "poster/get/{id}")
 	@SuppressWarnings({ "cast", "unchecked" })
 	public PosterEntity getOriginalPosterByID(@Named("id") long key) {
 		PersistenceManager mgr = getPersistenceManager();
 		PosterEntity posterEntity = null;
 		try {
 			posterEntity = mgr.getObjectById(PosterEntity.class, key);
 		} finally {
 			mgr.close();
 		}
 		return posterEntity;
 	}
 
 	/**
 	 * Generate an url for client side request to upload images.
 	 * 
 	 * @return ImageUploadURL
 	 */
 	@ApiMethod(httpMethod = "GET", name = "poster.url", path = "poster/url")
 	@SuppressWarnings({ "cast", "unchecked" })
 	public ImageUploadURL getUploadUrl() {
 		return new ImageUploadURL(
 				blobstoreService.createUploadUrl("/uploadImage"));
 	}
 
 	/**
 	 * 
 	 * @param PosterEntity
 	 * @return
 	 */
 	/*
 	@ApiMethod(httpMethod = "POST", name = "poster.insert", path = "poster/insert")
 	public PosterEntity insertOriginalPoster(PosterEntity posterEntity) {
 		PersistenceManager pm = getPersistenceManager();
 		pm.makePersistent(posterEntity);
 		pm.close();
 		
 				
 		return posterEntity;
 	}*/
 	
 	
 	private static PersistenceManager getPersistenceManager() {
 		return PMF.get().getPersistenceManager();
 	}
 
 }
