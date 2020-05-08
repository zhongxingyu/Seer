 package com.gatech.faceme.endpoints;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.inject.Named;
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 
 import com.gatech.faceme.entity.ImageUploadURL;
 import com.gatech.faceme.entity.PairTableEntity;
 import com.gatech.faceme.entity.UserFaceEntity;
 import com.gatech.faceme.mediastore.PMF;
 import com.google.api.server.spi.config.Api;
 import com.google.api.server.spi.config.ApiMethod;
 import com.google.appengine.api.blobstore.BlobstoreService;
 import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
 
 @Api(name = "userfaceendpoint", description = "This entity represents a user face.", version = "v1")
 public class UserFaceEndpoint {
 	private BlobstoreService blobstoreService = BlobstoreServiceFactory
 			.getBlobstoreService();
 	
 	/**
 	 * Get a list of posters from PosterEntity in Google Datastore and return
 	 * List of this entity.
 	 * 
 	 * @return List<PosterEntity>
 	 */
 	@ApiMethod(httpMethod = "GET", name = "userface.list", path = "userface/list")
 	@SuppressWarnings({ "cast", "unchecked" })
 	public List<UserFaceEntity> listUserFace() {
 		PersistenceManager mgr = PMF.get().getPersistenceManager();
 		List<UserFaceEntity> result = new ArrayList<UserFaceEntity>();
 		try {
 			Query query = mgr.newQuery(UserFaceEntity.class);
 			for (Object obj : (List<Object>) query.execute()) {
 				result.add(((UserFaceEntity) obj));
 			}
 		} finally {
 			mgr.close();
 		}
 		return result;
 	}
 
 	/**
 	 * Return a specific UserFaceEntity according to the blobkey in GET method
 	 * 
 	 * @param BlobKey
 	 * @return PosterEntity
 	 */
 	@ApiMethod(httpMethod = "GET", name = "poster.get", path = "poster/get/{id}")
 	@SuppressWarnings({ "cast", "unchecked" })
 	public UserFaceEntity getOriginalPosterByID(@Named("id") long key) {
 		PersistenceManager mgr = getPersistenceManager();
 		UserFaceEntity posterEntity = null;
 		try {
 			posterEntity = mgr.getObjectById(UserFaceEntity.class, key);
 		} finally {
 			mgr.close();
 		}
 		return posterEntity;
 	}
 
 	/**
 	 * Generate an url for client side request to upload userface images.
 	 * 
 	 * @return ImageUploadURL
 	 */
 	@ApiMethod(httpMethod = "GET", name = "userface.url", path = "userface/url")
 	@SuppressWarnings({ "cast", "unchecked" })
 	public ImageUploadURL getUploadUrl() {
 		return new ImageUploadURL(
 				blobstoreService.createUploadUrl("/uploadUserFace"));
 	}
 	
 	@ApiMethod(httpMethod = "POST", name = "userface.insert",
 			path = "userface/insert")
	//curl -H 'Content-Type: application/json' -d '{"imageKey": imageKey, "userID": userID, "posterKey": poseterKey, "characterKey": characterKey}' http://localhost:8888/_ah/api/userfaceendpoint/v1/userface/insert
 	public UserFaceEntity addUserFace(UserFaceEntity userface) {
 		PersistenceManager pm = getPersistenceManager();
 		pm.makePersistent(userface);
 		pm.close();
 		return userface;
 	}
 	private static PersistenceManager getPersistenceManager() {
 		return PMF.get().getPersistenceManager();
 	}
 	
 }
