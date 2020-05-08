 package com.histograph.server.controllers;
 
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.appengine.api.blobstore.BlobKey;
 import com.google.appengine.api.blobstore.BlobstoreService;
 import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
 import com.histograph.server.entities.Image;
 import com.histograph.server.persistence.ImagePersistence;
 /**
  * 
  * @author alandonohoe
  * Callback servlet, called by the blobstore when it's completed asynch'ing saving the img to blobstore
  *  
  */
 public class PhoneImgBlobsCallback extends HttpServlet{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	private static Logger log = Logger.getLogger(PhoneImgBlobsCallback.class
 			.getName());
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 
 		log.info("Entering PhoneImgBlobsCallback::doGet");
 		
 		doPost(req, resp);
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 	
 		log.info("Entering PhoneImgBlobsCallback::doPost");
 		
		String paramName, paramValue,keyString, urlParameters = "";
		List<BlobKey> keys;
		Map<String, List<BlobKey>> blobs;
		BlobstoreService blobstoreService;
 		Image image = new Image();
 		ImagePersistence imagePersistence = new ImagePersistence();
 		
 		//Check params
 		Enumeration<String> paramNames = req.getParameterNames();
 		while (paramNames.hasMoreElements()) {
 
 			// check that we're not at the first param, if not - then need to
 			// add & to the string
 			if (!urlParameters.isEmpty())
 				urlParameters += "&";
 
 			paramName = (String) paramNames.nextElement();
 			urlParameters += paramName;
 			urlParameters += "=";
 
 			paramValue = req.getParameter(paramName);
 			urlParameters += paramValue;
 		}
 
 		log.info("urlParameters = " + urlParameters);
 
 		try {
 
 			// Get image's blobkey from blobstore
 			blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
 			blobs = blobstoreService.getUploads(req);
			keys = blobs.get("imgUpload"); // this is set in the JS script, as the param with a value = img file name.
 			keyString = keys.get(0).getKeyString();
 
 			log.info("keyString = " + keyString);
 		} catch (Exception e) {
 			log.warning(e.getMessage());
 		}
 		
 		
 		
 		
 
 		//TODO:
 		// pull the user's phone's unique ID out at this stage and save that with this img's blobkey
 		// pull location of image, in GPS and possible compass if avail?
 		
 		// construct img:
 //		image.setBlobKey(keys.get(0));
 //		image.setDirection(Double.valueOf(direction));
 //		image.setLatitude(Double.valueOf(latitude));
 //		image.setLongitude(Double.valueOf(longitude));
 //		image.setDescription(description);
 //		image.setUserId(Long.valueOf(userNo).longValue());
 //		image.setYear(Integer.valueOf(year));
 //
 //		//3. Persist the new image:
 //		imagePersistence.SaveImage(image);
 		
 		
 		
 	}
 	
 
 	
 	
 
 }
