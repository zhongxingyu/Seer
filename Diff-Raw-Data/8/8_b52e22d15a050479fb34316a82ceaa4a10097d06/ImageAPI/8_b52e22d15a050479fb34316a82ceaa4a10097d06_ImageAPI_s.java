 package com.github.jkschoen.jsma;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.jkschoen.jsma.misc.SmugMugException;
 import com.github.jkschoen.jsma.model.Album;
 import com.github.jkschoen.jsma.model.Comment;
 import com.github.jkschoen.jsma.model.Image;
 import com.github.jkschoen.jsma.model.ImageEXIF;
 import com.github.jkschoen.jsma.model.ImageStats;
 import com.github.jkschoen.jsma.response.AlbumCommentResponse;
 import com.github.jkschoen.jsma.response.AlbumResponse;
 import com.github.jkschoen.jsma.response.ImageCommentsResponse;
 import com.github.jkschoen.jsma.response.ImageEXIFResponse;
 import com.github.jkschoen.jsma.response.ImageResponse;
 import com.github.jkschoen.jsma.response.ImageStatsResponse;
 import com.github.jkschoen.jsma.response.SMResponse;
 
 public class ImageAPI {
 	static final Logger logger = LoggerFactory.getLogger(ImageAPI.class);
 	
 	private SmugMugAPI smugmug;
 	
 	protected ImageAPI(SmugMugAPI smugmug){
 		this.smugmug = smugmug;
 	}
 	
 	/**
 	 * Applies a watermark to the image.
 	 * 
 	 * @param imageId (required) The id for a specific album.
 	 * @param watermarkId (required) The id for a specific watermark.
 	 * @param pretty return formatted JSON that is easier to read
 	 * @param strict Enable strict error handling.
 	 * @return returns true if watermark was set
 	 * @throws SmugMugException
 	 */
 	public boolean applyWatermark(int imageId, int watermarkId, boolean pretty, boolean strict) throws SmugMugException {
 		logger.debug("applyWatermark() called");
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("ImageID", Integer.toString(imageId));
 		params.put("WatermarkID", Integer.toString(watermarkId));
 		SMResponse requestToken = SMResponse.callMethod(this.smugmug,SMResponse.class, "smugmug.images.applyWatermark", params, null, pretty, false, strict, false);
 		logger.debug("applyWatermark() result: "+(requestToken == null ? "null" : requestToken.toString()));
 		//if it does not throw an exception than it worked, so return true
 		return true;
 	}
 	
 	/**
 	 * Change the position of an image within an album.
 	 * 
 	 * @param imageId (required) The id for a specific album.
 	 * @param position (required) The position of the image (or video) within the album.
 	 * @param pretty return formatted JSON that is easier to read
 	 * @param strict Enable strict error handling.
 	 * @return true id the position is changed
 	 * @throws SmugMugException
 	 */
 	public boolean changePosition(int imageId, int position, boolean pretty, boolean strict) throws SmugMugException {
 		logger.debug("changePosition() called");
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("ImageID", Integer.toString(imageId));
 		params.put("Position", Integer.toString(position));
 		SMResponse requestToken = SMResponse.callMethod(this.smugmug,SMResponse.class, "smugmug.images.changePosition", params, null, pretty, false, strict, false);
 		logger.debug("changePosition() result: "+(requestToken == null ? "null" : requestToken.toString()));
 		//if it does not throw an exception than it worked, so return true
 		return true;
 	}
 	
 	/**
 	 * Change the settings of an image.
 	 * 
 	 * Changable settings:
 	 * <ul>
 	 * 	<li>Album</li>
 	 * 	<li>Altitude</li>
 	 * 	<li>Caption</li>
 	 * 	<li>FileName</li>
 	 * 	<li>Hidden</li>
 	 * 	<li>Keywords</li>
 	 * 	<li>Latitude</li>
 	 * 	<li>Longitude</li>
 	 * </ul>
 	 * 
 	 * @param image the new image settings.
 	 * @param extras A comma separated string of additional attributes to return in the response. 
 	 *               NOTE: I do not think this does anything at all.
 	 * @param pretty return formatted JSON that is easier to read
 	 * @param sandboxed Forces URLs to a location with a crossdomain.xml file.
 	 * @param strict Enable strict error handling.
 	 * @return true if the settngs are successfully changed.
 	 * @throws SmugMugException
 	 */
	public boolean changeSettings(Image image, String[] extras, boolean pretty, boolean sandboxed, boolean strict) throws SmugMugException{
 		logger.debug("changeSettings() called");
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("ImageID", Integer.toString(image.getId()));
 		if(image.getAlbum() != null){
 			params.put("AlbumID", image.getAlbum().getId().toString());
 		}
 		if(image.getAltitude() != null){
 			params.put("Altitude", image.getAltitude().toString());
 		}
 		if(image.getCaption() != null){
 			params.put("Caption", image.getCaption());
 		}
 		if(image.getFileName() != null){
 			params.put("FileName", image.getFileName());
 		}
 		if(image.getHidden() != null){
 			params.put("Hidden", (image.getHidden() ? "true" : "false"));
 		}
 		if(image.getKeywords() != null){
 			params.put("Keywords", image.getKeywords());
 		}
 		if(image.getLatitude() != null){
 			params.put("Latitude", image.getLatitude().toString());
 		}
 		if(image.getLongitude() != null){
 			params.put("Longitude", image.getLongitude().toString());
 		}
 		
		SMResponse requestToken = SMResponse.callMethod(this.smugmug,SMResponse.class, "smugmug.images.changeSettings", params, extras, pretty, sandboxed, strict, false);
 		logger.debug("changeSettings() result: "+(requestToken == null ? "null" : requestToken.toString()));
 		//if it does not throw an exception than it worked, so return true
		return true;
 	}
 	
 	/**
 	 * Collect an image into an album.
 	 * 
 	 * @param albumId The id for a specific album.
 	 * @param imageId The id for a specific image.
 	 * @param imageKey The key for a specific image.
 	 * @param extras A comma separated string of additional attributes to return in the response.
 	 * @param pretty return formatted JSON that is easier to read
 	 * @param strict Enable strict error handling.
 	 * @return return the collected image
 	 * @throws SmugMugException
 	 */
 	public Image collect(int albumId, int imageId, String imageKey, String[] extras, boolean pretty, boolean strict) throws SmugMugException {
 		logger.debug("collect() called");
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("AlbumID", Integer.toString(albumId));
 		params.put("ImageID", Integer.toString(imageId));
 		params.put("ImageKey", imageKey);
 		ImageResponse requestToken = SMResponse.callMethod(this.smugmug,ImageResponse.class, "smugmug.images.collect", params, extras, pretty, false, strict, false);
 		logger.debug("collect() result: "+(requestToken == null ? "null" : requestToken.toString()));
 		return requestToken.getImage();
 	}
 	
 	/**
 	 * Add a comment to an image.
 	 * 
 	 * @param comment The comment to be added.
 	 * @param imageId The id for a specific image.
 	 * @param imageKey The key for a specific image.
 	 * @param extras array of extra fields to be populated.
 	 * @param pretty return formatted JSON that is easier to read
 	 * @param strict Enable strict error handling.
 	 * @return the added comment
 	 */
 	public Comment commentAdd(Comment comment, int imageId, String imageKey, String[] extras, boolean pretty, boolean strict) throws SmugMugException{
 		logger.debug("commentAdd() called");
 		
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("ImageID" , Integer.toString(imageId));
 		params.put("ImageKey" , imageKey);
 		params.put("Text", comment.getText());
 		if(comment.getRating() != null){
 			params.put("Rating", comment.getRating().toString());
 		}
 		
 		AlbumCommentResponse requestToken = SMResponse.callMethod(this.smugmug,AlbumCommentResponse.class, "smugmug.images.comment.add", params, extras, pretty, false, strict, false);
 		logger.debug("commentAdd() result: "+(requestToken == null ? "null" : requestToken.toString()));
 		comment.setId(requestToken.getComment().getId());
 		return comment;
 	}
 	
 	/**
 	 * Retrieve a list of comments for an image.
 	 * 
 	 * @param imageId The id for a specific album.
 	 * @param imageKey The key for a specific album.
 	 * @param password The password for the album.
 	 * @param sitePassword The site password for a specific user.
 	 * @param lastUpdated Return results where LastUpdated is after
 	 * @param pretty return formatted JSON that is easier to read
 	 * @param strict Enable strict error handling.
 	 * @return
 	 * @throws SmugMugException
 	 */
 	public List<Comment> commentGet(int imageId, String imageKey, String password, String sitePassword, Date lastUpdated, boolean pretty, boolean strict) throws SmugMugException{
 		logger.debug("commentGet() called");
 		
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("ImageID" , Integer.toString(imageId));
 		params.put("ImageKey" , imageKey);
 		
 		if(password != null){
 			params.put("Password", password);
 		}
 		if(sitePassword != null){
 			params.put("SitePassword", sitePassword);
 		}
 		if(lastUpdated != null){
 			params.put("LastUpdated", Long.toString(lastUpdated.getTime()));
 		}
 		
 		ImageCommentsResponse requestToken = SMResponse.callMethod(this.smugmug,ImageCommentsResponse.class, "smugmug.images.comments.get", params, null, pretty, false, strict, false);
 		logger.debug("commentGet() result: "+(requestToken == null ? "null" : requestToken.toString()));
 		return requestToken.getComments();
 	}
 
 	/**
 	 * Crop an image.
 	 * 
 	 * @param imageId (required) The id for a specific image.
 	 * @param height (required) The height of the crop.
 	 * @param width (required) The width of the crop.
 	 * @param x The x coordinate of the starting point.
 	 * @param y The y coordinate of the starting point.
 	 * @param pretty Return a more human friendly response.
 	 * @param strict Enable strict error handling.
 	 * @return
 	 * @throws SmugMugException  
 	 */
 	public boolean crop(int imageId, int height, int width, Integer x, Integer y, boolean pretty, boolean strict) throws SmugMugException {
 		logger.debug("crop() called");
 		
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("ImageID" , Integer.toString(imageId));
 		params.put("Height" , Integer.toString(height));
 		params.put("Width" , Integer.toString(width));
 		
 		if(x != null){
 			params.put("X", x.toString());
 		}
 		if(y != null){
 			params.put("Y", y.toString());
 		}
 		
 		SMResponse requestToken = SMResponse.callMethod(this.smugmug,SMResponse.class, "smugmug.images.crop", params, null, pretty, false, strict, false);
 		logger.debug("crop() result: "+(requestToken == null ? "null" : requestToken.toString()));
 		return true;
 	}
 
 	/**
 	 * Delete an image.
 	 * 
 	 * @param imageId (required) The id for a specific image.
 	 * @param albumId The id for a specific album.
 	 * @param pretty Return a more human friendly response.
 	 * @param strict Enable strict error handling.
 	 * @return
 	 * @throws SmugMugException
 	 */
 	public boolean delete(int imageId, Integer albumId, boolean pretty, boolean strict) throws SmugMugException {
 		logger.debug("delete() called");
 		
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("ImageID" , Integer.toString(imageId));
 		
 		if(albumId != null){
 			params.put("AlbumID ", albumId.toString());
 		}
 		
 		SMResponse requestToken = SMResponse.callMethod(this.smugmug,SMResponse.class, "smugmug.images.delete", params, null, pretty, false, strict, false);
 		logger.debug("delete() result: "+(requestToken == null ? "null" : requestToken.toString()));
 		return true;
 	}
 
 	/**
 	 * Retrieve a list of images for an album.
 	 * 
 	 * @param albumId The id for a specific album.
 	 * @param albumKey The key for a specific album.
 	 * @param customSize A custom size image to return.
 	 * @param password The password for the album.
 	 * @param sitePassword The site password for a specific user.
 	 * @param lastUpdated return results that where last updated after given time
 	 * @param extras array of extra fields to be populated.
 	 * @param pretty return formatted JSON that is easier to read
 	 * @param sandboxed Forces URLs to a location with a crossdomain.xml file.
 	 * @param strict Enable strict error handling.
 	 * @param heavy Returns a heavy response for this method.
 	 * @return
 	 * @throws SmugMugException
 	 */
 	public Album get(int albumId, String albumKey, String customSize, String password, String sitePassword, Date lastUpdated, String[] extras, boolean pretty, boolean sandboxed, boolean strict, boolean heavy) throws SmugMugException {
 		logger.debug("get() called");
 		
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("AlbumID" , Integer.toString(albumId));
 		params.put("AlbumKey" , albumKey);
 		if(customSize != null){
 			params.put("CustomSize ", customSize);
 		}
 		if(password != null){
 			params.put("Password ", password);
 		}
 		if(sitePassword != null){
 			params.put("SitePassword ", sitePassword);
 		}
 		if(lastUpdated != null){
 			params.put("LastUpdated ", Long.toString(lastUpdated.getTime()));
 		}
 		
 		AlbumResponse requestToken = SMResponse.callMethod(this.smugmug,AlbumResponse.class, "smugmug.images.get", params, extras, pretty, sandboxed, strict, heavy);
 		logger.debug("get() result: "+(requestToken == null ? "null" : requestToken.toString()));
 		return requestToken.getAlbum();
 	}
 	
 	/**
 	 * Retrieve the EXIF data for an image.
 	 * 
 	 * @param imageId (required) The id for a specific image.
 	 * @param imageKey (required) The key for a specific image.
 	 * @param password The password for the album.
 	 * @param sitePassword The site password for a specific user.
 	 * @param pretty return formatted JSON that is easier to read
 	 * @param strict Enable strict error handling.
 	 * @return
 	 * @throws SmugMugException
 	 */
 	public ImageEXIF getEXIF(int imageId, String imageKey, String password, String sitePassword, boolean pretty, boolean strict) throws SmugMugException {
 		logger.debug("getEXIF() called");
 		
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("ImageID" , Integer.toString(imageId));
 		params.put("ImageKey" , imageKey);
 		if(password != null){
 			params.put("Password ", password);
 		}
 		if(sitePassword != null){
 			params.put("SitePassword ", sitePassword);
 		}
 		
 		ImageEXIFResponse requestToken = SMResponse.callMethod(this.smugmug,ImageEXIFResponse.class, "smugmug.images.getEXIF", params, null, pretty, false, strict, false);
 		logger.debug("getEXIF() result: "+(requestToken == null ? "null" : requestToken.toString()));
 		return requestToken.getImage();
 	}
 	
 	/**
 	 * Retrieve the information for an image.
 	 * 
 	 * @param imageId (required) The id for a specific image.
 	 * @param imageKey (required) The key for a specific image.
 	 * @param customSize A custom size image to return.
 	 * @param password The password for the album.
 	 * @param sitePassword The site password for a specific user.
 	 * @param pretty return formatted JSON that is easier to read
 	 * @param sandboxed Forces URLs to a location with a crossdomain.xml file.
 	 * @param strict Enable strict error handling.
 	 * @return
 	 * @throws SmugMugException
 	 */
 	public Image getInfo(int imageId, String imageKey, String customSize, String password, String sitePassword, boolean pretty, boolean sandboxed, boolean strict) throws SmugMugException {
 		logger.debug("getInfo() called");
 		
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("ImageID" , Integer.toString(imageId));
 		params.put("ImageKey" , imageKey);
 		if(customSize != null){
 			params.put("CustomSize ", customSize);
 		}
 		if(password != null){
 			params.put("Password ", password);
 		}
 		if(sitePassword != null){
 			params.put("SitePassword ", sitePassword);
 		}
 		
 		ImageResponse requestToken = SMResponse.callMethod(this.smugmug,ImageResponse.class, "smugmug.images.getInfo", params, null, pretty, sandboxed, strict, false);
 		logger.debug("getInfo() result: "+(requestToken == null ? "null" : requestToken.toString()));
 		return requestToken.getImage();
 	}
 
 	/**
 	 * Retrieve the statistics for an image.
 	 * 
 	 * @param imageId The id for a specific image.
 	 * @param month The month to retrieve statistics for.
 	 * @param year The year to retrieve statistics for.
 	 * @param pretty return formatted JSON that is easier to read
 	 * @param strict Enable strict error handling.
 	 * @return
 	 * @throws SmugMugException
 	 */
 	public ImageStats getStats(int imageId, int month, int year, boolean pretty, boolean strict) throws SmugMugException {
 		logger.debug("getStats() called");
 		
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("ImageID", Integer.toString(imageId));
 		params.put("Month", Integer.toString(month));
 		params.put("Year", Integer.toString(year));
 		
 		ImageStatsResponse requestToken = SMResponse.callMethod(this.smugmug,ImageStatsResponse.class, "smugmug.images.getStats", params, null, pretty, false, strict, false);
 		logger.debug("getStats() result: "+(requestToken == null ? "null" : requestToken.toString()));
 		return requestToken.getStats();
 	}
 	
 	/**
 	 * Retrieve the URLs for an image.
 	 * 
 	 * @param imageId (required) The id for a specific image.
 	 * @param imageKey (required) The key for a specific image.
 	 * @param customSize A custom size image to return.
 	 * @param password The password for the album.
 	 * @param sitePassword The site password for a specific user.
 	 * @param pretty return formatted JSON that is easier to read
 	 * @param sandboxed Forces URLs to a location with a crossdomain.xml file.
 	 * @param strict Enable strict error handling.
 	 * @return
 	 * @throws SmugMugException
 	 */
 	public Image getURLs(int imageId, String imageKey, String customSize, String password, String sitePassword, boolean pretty, boolean sandboxed, boolean strict) throws SmugMugException {
 		logger.debug("getURLs() called");
 		
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("ImageID" , Integer.toString(imageId));
 		params.put("ImageKey" , imageKey);
 		if(customSize != null){
 			params.put("CustomSize ", customSize);
 		}
 		if(password != null){
 			params.put("Password ", password);
 		}
 		if(sitePassword != null){
 			params.put("SitePassword ", sitePassword);
 		}
 		
 		ImageResponse requestToken = SMResponse.callMethod(this.smugmug,ImageResponse.class, "smugmug.images.getURLs", params, null, pretty, sandboxed, strict, false);
 		logger.debug("getURLs() result: "+(requestToken == null ? "null" : requestToken.toString()));
 		return requestToken.getImage();
 	}
 
 	/**
 	 * Remove a watermark from an image.
 	 * 
 	 * @param imageId The id for a specific album.
 	 * @param pretty return formatted JSON that is easier to read
 	 * @param strict Enable strict error handling.
 	 * @return true if the watermark is removed
 	 * @throws SmugMugException
 	 */
 	public boolean removeWatermark(int imageId, boolean pretty, boolean strict) throws SmugMugException {
 		logger.debug("removeWatermark() called");
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("ImageID", Integer.toString(imageId));
 		SMResponse requestToken = SMResponse.callMethod(this.smugmug,SMResponse.class, "smugmug.images.removeWatermark", params, null, pretty, false, strict, false);
 		logger.debug("removeWatermark() result: "+(requestToken == null ? "null" : requestToken.toString()));
 		//if it does not throw an exception than it worked, so return true
 		return true;
 	}
 	
 	/**
 	 * Rotates an image.
 	 * 
 	 * @param imageId The id for a specific image.
 	 * @param degrees The degrees of rotation.
 	 *        Values:
 	 *        90 - Left
 	 *        180 - Down
 	 *        270 - Right
 	 * @param flip Mirror the image in the horizontal direction.
 	 * @param pretty return formatted JSON that is easier to read
 	 * @param strict Enable strict error handling.
 	 * @return
 	 * @throws SmugMugException
 	 */
 	public boolean rotate(int imageId, Integer degrees, Boolean flip, boolean pretty, boolean strict) throws SmugMugException {
 		logger.debug("rotate() called");
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("ImageID", Integer.toString(imageId));
 		if(degrees != null){
 			params.put("Degrees", degrees.toString());
 		}
 		if (flip != null){
 			params.put("Flip", flip ? "true" : "false");
 		}
 		SMResponse requestToken = SMResponse.callMethod(this.smugmug,SMResponse.class, "smugmug.images.removeWatermark", params, null, pretty, false, strict, false);
 		logger.debug("rotate() result: "+(requestToken == null ? "null" : requestToken.toString()));
 		//if it does not throw an exception than it worked, so return true
 		return true;
 	}
 	
 	/**
 	 * Upload an image from a URL to an album.
 	 * 
 	 * @param albumId (required) The id for a specific album.
 	 * @param downloadURL (required) The URL for the image.
 	 * @param imageSettings various settings that can be set for the image
 	 *        Changable settings:
 	 *        <ul>
 	 *        	<li>Altitude</li>
 	 *          <li>ByteCount</li>
 	 *        	<li>Caption</li>
 	 *        	<li>FileName</li>
 	 *        	<li>Hidden</li>
 	 *        	<li>Keywords</li>
 	 *        	<li>Latitude</li>
 	 *        	<li>Longitude</li>
 	 *          <li>MD5Sum</li>
 	 *        </ul>
 	 * @param extras array of extra fields to be populated.
 	 * @param pretty return formatted JSON that is easier to read
 	 * @param strict Enable strict error handling.
 	 * @return
 	 * @throws SmugMugException
 	 */
 	public Image uploadFromURL(int albumId, String downloadURL, Image imageSettings, String[] extras, boolean pretty, boolean strict) throws SmugMugException {
 		logger.debug("uploadFromURL() called");
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("ImageID", Integer.toString(albumId));
 		params.put("URL", downloadURL);
 		if (imageSettings != null){
 			if(imageSettings.getAltitude() != null){
 				params.put("Altitude", imageSettings.getAltitude().toString());
 			}
 			if (imageSettings.getSize() != null){
 				params.put("ByteCount", imageSettings.getSize().toString());
 			}
 			if (imageSettings.getCaption() != null){
 				params.put("Caption", imageSettings.getCaption());
 			}
 			if (imageSettings.getFileName() != null){
 				params.put("FileName", imageSettings.getFileName());
 			}
 			if (imageSettings.getHidden() != null){
 				params.put("Hidden", imageSettings.getHidden() ? "true" : "false");
 			}
 			if (imageSettings.getKeywords() != null){
 				params.put("Keywords", imageSettings.getKeywords());
 			}
 			if (imageSettings.getLatitude() != null){
 				params.put("Latitude", imageSettings.getLatitude().toString());
 			}
 			if (imageSettings.getLongitude() != null){
 				params.put("Longitude", imageSettings.getLongitude().toString());
 			}
 			if (imageSettings.getMd5Sum() != null){
 				params.put("MD5Sum", imageSettings.getMd5Sum());
 			}
 		}
 		ImageResponse requestToken = SMResponse.callMethod(this.smugmug,ImageResponse.class, "smugmug.images.uploadFromURL", params, null, pretty, false, strict, false);
 		logger.debug("uploadFromURL() result: "+(requestToken == null ? "null" : requestToken.toString()));
 		return requestToken.getImage();
 	}
 
 	/**
 	 * Crop the thumbnail of an image.
 	 * 
 	 * @param imageId The id for a specific image.
 	 * @param height The height of the crop.
 	 * @param width The width of the crop.
 	 * @param x The x coordinate of the starting point.
 	 * @param y The y coordinate of the starting point.
 	 * @param pretty return formatted JSON that is easier to read
 	 * @param strict Enable strict error handling.
 	 * @return
 	 * @throws SmugMugException
 	 */
 	public boolean zoomThumbnail(int imageId, int height, int width, Integer x, Integer y, boolean pretty, boolean strict) throws SmugMugException{
 		logger.debug("zoomThumbnail() called");
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("ImageID", Integer.toString(imageId));
 		params.put("Height", Integer.toString(height));
 		params.put("Width", Integer.toString(width));
 		if(x != null){
 			params.put("X", x.toString());
 		}
 		if (y != null){
 			params.put("Y", y.toString());
 		}
 		SMResponse requestToken = SMResponse.callMethod(this.smugmug,SMResponse.class, "smugmug.images.zoomThumbnail", params, null, pretty, false, strict, false);
 		logger.debug("zoomThumbnail() result: "+(requestToken == null ? "null" : requestToken.toString()));
 		//if it does not throw an exception than it worked, so return true
 		return true;
 	}
 
 
 }
