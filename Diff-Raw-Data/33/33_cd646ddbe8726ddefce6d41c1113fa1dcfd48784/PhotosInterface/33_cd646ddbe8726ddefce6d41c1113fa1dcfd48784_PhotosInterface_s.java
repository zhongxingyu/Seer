 /**
  * 
  */
 package com.github.yuyang226.j500px.photos;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.github.yuyang226.j500px.J500pxConstants;
 import com.github.yuyang226.j500px.J500pxException;
 import com.github.yuyang226.j500px.http.Parameter;
 import com.github.yuyang226.j500px.http.Response;
 import com.github.yuyang226.j500px.http.Transport;
 import com.github.yuyang226.j500px.oauth.OAuthInterface;
 import com.github.yuyang226.j500px.oauth.OAuthUtils;
 import com.github.yuyang226.j500px.users.UserList;
 import com.github.yuyang226.j500px.users.UserUtils;
 
 /**
  * @author yayu
  * 
  */
 public class PhotosInterface {
 
 	private String apiKey;
 	private String sharedSecret;
 	private Transport transportAPI;
 
 	/**
 	 * 
 	 */
 	public PhotosInterface(String apiKey, String sharedSecret,
 			Transport transportAPI) {
 		super();
 		this.apiKey = apiKey;
 		this.sharedSecret = sharedSecret;
 		this.transportAPI = transportAPI;
 	}
 
 	/**
 	 * @param photoId
 	 * @param imageSize
 	 * @param showComments
 	 *            Return comments to the photo in the response. Comments are
 	 *            returned in order of creation, 20 entries per page.
 	 * @param commentsPage
 	 *            Return the specified page from the comments listing. Page
 	 *            numbers are 1-based.
 	 * @return
 	 * @throws J500pxException
 	 * @throws JSONException
 	 * @throws IOException
 	 */
 	public Photo getPhotoDetail(int photoId, ImageSize imageSize,
 			boolean showComments, int commentsPage) throws J500pxException,
 			IOException, JSONException {
 		List<Parameter> parameters = new ArrayList<Parameter>();
 		boolean signed = OAuthUtils.hasSigned();
 
 		if (signed) {
 			OAuthUtils.addOAuthToken(parameters);
 			parameters.add(new Parameter(
 					OAuthInterface.PARAM_OAUTH_CONSUMER_KEY, apiKey));
 		} else {
 			parameters.add(new Parameter(OAuthInterface.PARAM_CONSUMER_KEY,
 					apiKey));
 		}
 
 		if (imageSize != null) {
 			parameters.add(new Parameter("image_size", imageSize.getSize()));
 		}
 
 		if (showComments) {
 			parameters.add(new Parameter("comments", 1));
 		}
 
 		if (commentsPage > 0) {
 			parameters.add(new Parameter("comments_page", commentsPage));
 		}
 
 		StringBuilder photoPath = new StringBuilder(J500pxConstants.PATH_PHOTOS);
 		photoPath.append("/");
 		photoPath.append(photoId);
 		Response response = signed ? transportAPI.getJSON(sharedSecret,
 				photoPath.toString(), parameters) : transportAPI.get(
 				photoPath.toString(), parameters);
 		if (response.isError()) {
 			throw new J500pxException(response.getErrorMessage());
 		}
 		System.out.println(response.getData());
 		Photo photo = PhotoUtils.parsePhoto(response.getData().getJSONObject("photo"));
 		JSONArray commentsObj = response.getData().optJSONArray("comments");
 		for (int i = 0; commentsObj != null && i < commentsObj.length(); i++) {
 			photo.getComments().add(parseComment(commentsObj.getJSONObject(i)));
 		}
 		return photo;
 	}
 
 	/**
 	 * 
 	 * @param photoId the photo id
 	 * @param pageNo a specific page in the comment listing. Page numbering is 1-based.
 	 * @throws IOException
 	 * @throws JSONException
 	 * @throws J500pxException
 	 */
 	public CommentList getPhotoComments(int photoId, int pageNum) throws IOException,
 			JSONException, J500pxException {
 		String path = String.format(Locale.US,
 				J500pxConstants.PATH_PHOTO_COMMENTS, photoId);
 		List<Parameter> parameters = new ArrayList<Parameter>();
 		parameters.add(new Parameter(OAuthInterface.PARAM_CONSUMER_KEY, apiKey));
 		if (pageNum > 0) {
 			parameters.add(new Parameter("page", pageNum));
 		}
 		Response response = transportAPI.getJSON(sharedSecret, path, parameters);
 		if (response.isError()) {
 			throw new J500pxException(response.getErrorMessage());
 		}
 		JSONArray commentsObj = response.getData().optJSONArray("comments");
 		CommentList commentList = new CommentList();
 		if (response.getData().has("media_type")) {
 			commentList.setMediaType(MediaType.valueOf(response.getData().getString("media_type").toUpperCase(Locale.US)));
 		}
 		for (int i = 0; commentsObj != null && i < commentsObj.length(); i++) {
 			commentList.add(parseComment(commentsObj.getJSONObject(i)));
 		}
 		return commentList;
 	}
 	
 	/**
 	 * @param photoId
 	 * @param pageNum Return a specific page in the photo stream. Page numbering is 1-based.
 	 * @param pageSize The number of results to return. Can not be over 100, default 20.
 	 * @return
 	 * @throws IOException
 	 * @throws JSONException
 	 * @throws J500pxException
 	 */
 	public UserList getPhotoVotedUsers(int photoId, int pageNum, int pageSize) throws IOException, JSONException, J500pxException {
 		if (!OAuthUtils.hasSigned()) {
 			throw new IllegalArgumentException("must be signed");
 		}
 		String path = String.format(Locale.US,
 				J500pxConstants.PATH_PHOTO_VOTES, photoId);
 		List<Parameter> parameters = new ArrayList<Parameter>();
 
 		OAuthUtils.addOAuthToken(parameters);
 		parameters.add(new Parameter(
 				OAuthInterface.PARAM_OAUTH_CONSUMER_KEY, apiKey));
 		
 		if (pageNum > 0) {
 			parameters.add(new Parameter("page", pageNum));
 		}
 
 		if (pageSize > 0) {
 			parameters.add(new Parameter("rpp", pageSize));
 		}
 		
 		Response response = transportAPI.getJSON(sharedSecret, path, parameters);
 		if (response.isError()) {
 			throw new J500pxException(response.getErrorMessage());
 		}
 
 		UserList userList = new UserList();
 		userList.setTotalPages(response.getData().getInt("total_pages"));
 		userList.setCurrentPage(response.getData().getInt("current_page"));
 		userList.setTotalItems(response.getData().getInt("total_items"));
 		JSONArray usersObj = response.getData().optJSONArray("users");
 		for (int i = 0; usersObj != null && i < usersObj.length(); i++) {
 			userList.add(UserUtils.parseUserObject(usersObj.getJSONObject(i)));
 		}
 		return userList;
 	}
 	
 	/**
 	 * Returns all users that had favorite that photo.
 	 * @param photoId
 	 * @param pageNum
 	 * @param pageSize
 	 * @return
 	 * @throws IOException
 	 * @throws JSONException
 	 * @throws J500pxException
 	 */
 	public UserList getPhotoFavedUsers(int photoId, int pageNum, int pageSize) throws IOException, JSONException, J500pxException {
 		if (!OAuthUtils.hasSigned()) {
 			throw new IllegalArgumentException("must be signed");
 		}
 		String path = String.format(Locale.US,
 				J500pxConstants.PATH_PHOTO_FAVORITES, photoId);
 		List<Parameter> parameters = new ArrayList<Parameter>();
 
 		OAuthUtils.addOAuthToken(parameters);
 		parameters.add(new Parameter(
 				OAuthInterface.PARAM_OAUTH_CONSUMER_KEY, apiKey));
 		
 		if (pageNum > 0) {
 			parameters.add(new Parameter("page", pageNum));
 		}
 
 		if (pageSize > 0) {
 			parameters.add(new Parameter("rpp", pageSize));
 		}
 		
 		Response response = transportAPI.getJSON(sharedSecret, path, parameters);
 		if (response.isError()) {
 			throw new J500pxException(response.getErrorMessage());
 		}
 
 		UserList userList = new UserList();
 		userList.setTotalPages(response.getData().getInt("total_pages"));
 		userList.setCurrentPage(response.getData().getInt("current_page"));
 		userList.setTotalItems(response.getData().getInt("total_items"));
 		JSONArray usersObj = response.getData().optJSONArray("users");
 		for (int i = 0; usersObj != null && i < usersObj.length(); i++) {
 			userList.add(UserUtils.parseUserObject(usersObj.getJSONObject(i)));
 		}
 		return userList;
 	}
 
 	public static Comment parseComment(JSONObject commentObj) throws JSONException {
 		Comment comment = new Comment();
 		
 		comment.setId(commentObj.getInt("id"));
 		comment.setComment(commentObj.getString("body"));
 		comment.setToWhomUserId(commentObj.optInt("to_whom_user_id", -1));
 		if (commentObj.has("user")) {
 			comment.setAuthor(UserUtils.parseUserObject(commentObj
 					.getJSONObject("user")));
 		}
 		comment.setUserId(commentObj.getInt("user_id"));
 		if (commentObj.has("created_at")) {
 			try {
 				comment.setCreatedAt(J500pxConstants.W3C_DATE_FORMATS.get()
 						.parse(commentObj.getString("created_at")));
 			} catch (ParseException e) {
 				// ignore
 			}
 		}
 
 		return comment;
 	}
 
 	public PhotoList getUserPhotos(GlobalFeatures userFeature, String userId,
 			String userName, SearchSort sort, PhotoCategory categoryOnly,
			PhotoCategory categoryExclude, ImageSize imageSize, int pageNum,
 			int pageSize) throws J500pxException, IOException, JSONException {
 		if (userId == null && userName == null) {
 			throw new IllegalArgumentException(
 					"Either userId or userName must be set");
 		}
 		if (userFeature != null && userFeature != GlobalFeatures.USER
 				&& userFeature != GlobalFeatures.USER_FRIENDS
 				&& userFeature != GlobalFeatures.USER_FAVORITES) {
 			throw new IllegalArgumentException(
 					"Must be one of the user features");
 		}
 		List<Parameter> extraParams = new ArrayList<Parameter>();
 		if (userId != null) {
 			extraParams.add(new Parameter("user_id", userId));
 		} else {
 			extraParams.add(new Parameter("username", userName));
 		}
 
 		return getPhotos(userFeature != null ? userFeature
 				: GlobalFeatures.USER, sort, categoryOnly, categoryExclude,
				new ImageSize[]{imageSize}, extraParams, false, false, false, pageNum, pageSize);
 	}
 
 	/**
 	 * @param feature
 	 * @param sort
 	 * @param categoryOnly
 	 * @param categoryExclude
 	 * @param imageSizes
 	 * @param extraParams
 	 * @param includeStore Whether to return market information about the photo.
 	 * @param includeStates Returns state of the photo for the currently logged in user and authenticated request.
 	 * @param showTags Whether to return an array of tags for the photo.
 	 * @param pageNum Return a specific page in the photo stream. Page numbering is 1-based.
 	 * @param pageSize The number of results to return. Can not be over 100, default 20.
 	 * @return
 	 * @throws J500pxException
 	 * @throws IOException
 	 * @throws JSONException
 	 */
 	public PhotoList getPhotos(GlobalFeatures feature, SearchSort sort,
 			PhotoCategory categoryOnly, PhotoCategory categoryExclude,
 			ImageSize[] imageSizes, List<Parameter> extraParams, boolean includeStore, 
 			boolean includeStates, boolean showTags, int pageNum, int pageSize) 
 					throws J500pxException, IOException, JSONException {
 		List<Parameter> parameters = new ArrayList<Parameter>();
 		boolean signed = OAuthUtils.hasSigned();
 		parameters.add(new Parameter("feature", feature.toString()));
 
 		if (signed) {
 			OAuthUtils.addOAuthToken(parameters);
 			parameters.add(new Parameter("include_states","1"));
 			parameters.add(new Parameter(
 					OAuthInterface.PARAM_OAUTH_CONSUMER_KEY, apiKey));
 		} else {
 			parameters.add(new Parameter(OAuthInterface.PARAM_CONSUMER_KEY,
 					apiKey));
 		}
 
 		if (categoryOnly != null) {
 			parameters
 					.add(new Parameter("only", categoryOnly.getCategoryName()));
 		}
 
 		if (categoryExclude != null) {
 			parameters.add(new Parameter("exclude", categoryExclude
 					.getCategoryName()));
 		}
 
 		if (extraParams != null) {
 			parameters.addAll(extraParams);
 		}
 
 		if (sort != null) {
 			parameters.add(new Parameter("sort", sort.toString()));
 		}
 
 		if (imageSizes != null) {
 			if (imageSizes.length == 1) {
 				parameters.add(new Parameter("image_size", imageSizes[0].getSize()));
 			} else {
 				//Format: '&image_size[]=3&image_size[]=4'
 				for (ImageSize imageSize : imageSizes) {
 					parameters.add(new Parameter("image_size[]", imageSize.getSize()));
 				}
 			}
 		}
 		
 		if (includeStore) {
 			parameters.add(new Parameter("include_store", 1));
 		}
 		
 		if (includeStates) {
 			parameters.add(new Parameter("include_states", 1));
 		}
 		
 		if (showTags) {
 			parameters.add(new Parameter("tags", 1));
 		}
 
 		if (pageNum > 0) {
 			parameters.add(new Parameter("page", pageNum));
 		}
 
 		if (pageSize > 0) {
 			parameters.add(new Parameter("rpp", pageSize));
 		}
 
 		Response response = signed ? transportAPI.getJSON(sharedSecret,
 				J500pxConstants.PATH_PHOTOS, parameters) : transportAPI.get(
 				J500pxConstants.PATH_PHOTOS, parameters);
 		if (response.isError()) {
 			throw new J500pxException(response.getErrorMessage());
 		}
 		return PhotoUtils.parsePhotoList(response.getData());
 	}
 
 	/**
 	 * @param term
 	 *            A keyword to search for
 	 * @param tag
 	 *            A complete tag string to search for
 	 * @param pageNum
 	 *            Return a specific page. Page numbering is 1-based.
 	 * @param pageSize
 	 *            The number of results to return. Can not be over 100, default
 	 *            20.
 	 * @return
 	 * @throws J500pxException
 	 * @throws IOException
 	 * @throws JSONException
 	 * @see http://developer.500px.com/docs/photos-search
 	 */
	public PhotoList searchPhotos(String term, String tag, boolean showTags, ImageSize imageSize, 
 			int pageNum, int pageSize) throws J500pxException, IOException, JSONException {
 		if (term == null && tag == null) {
 			throw new IllegalArgumentException("Both term and tag are null");
 		}
 		List<Parameter> parameters = new ArrayList<Parameter>();
 		boolean signed = OAuthUtils.hasSigned();
 
 		if (signed) {
 			parameters.add(new Parameter(
 					OAuthInterface.PARAM_OAUTH_CONSUMER_KEY, apiKey));
 			OAuthUtils.addOAuthToken(parameters);
 		} else {
 			parameters.add(new Parameter(OAuthInterface.PARAM_CONSUMER_KEY,
 					apiKey));
 		}
 
 		if (term != null) {
 			parameters.add(new Parameter("term", term));
 		}
 
 		if (tag != null) {
 			parameters.add(new Parameter("tag", tag));
 		}
 		
 		if (showTags) {
 			parameters.add(new Parameter("tags", 1));
 		}
 		
		if (imageSize != null) {
			parameters.add(new Parameter("image_size", imageSize.getSize()));
 		}
 
 		if (pageNum > 0) {
 			parameters.add(new Parameter("page", pageNum));
 		}
 
 		if (pageSize > 0) {
 			parameters.add(new Parameter("rpp", pageSize));
 		}
 
 		Response response = signed ? transportAPI.getJSON(sharedSecret,
 				J500pxConstants.PATH_PHOTOS_SEARCH, parameters) : transportAPI
 				.get(J500pxConstants.PATH_PHOTOS_SEARCH, parameters);
 		if (response.isError()) {
 			throw new J500pxException(response.getErrorMessage());
 		}
 		return PhotoUtils.parsePhotoList(response.getData());
 	}
 
 	/**
 	 * Create a new photo on behalf of the user, and receive an upload token in
 	 * exchange.
 	 * 
 	 * OAuth required.
 	 * 
 	 * @param name
 	 *            — Title of the photo
 	 * @param description
 	 *            — Description for the photo
 	 * @param category
 	 *            — The category of the photo
 	 * @param exif
 	 *            - Technical details of the photo
 	 * @param latitude
 	 *            — Latitude, in decimal format
 	 * @param longitude
 	 *            — Longitude, in decimal format
 	 * @param privacy
 	 *            — Whether to hide the photo from the user profile on the
 	 *            website. Otherwise, the photo is only available for use in
 	 *            Collections/Portfolio.
 	 * @throws J500pxException
 	 * @throws IOException
 	 * @throws JSONException
 	 */
 	public PhotoUpload createNewPhoto(String name, String description,
 			PhotoCategory category, PhotoExif exif, Double latitude,
 			Double longitude, Boolean privacy) throws J500pxException,
 			IOException, JSONException {
 		if (name == null) {
 			throw new IllegalArgumentException("Name must not be null");
 		}
 		boolean signed = OAuthUtils.hasSigned();
 		if (!signed) {
 			throw new J500pxException("must sign in first.");
 		}
 		List<Parameter> parameters = new ArrayList<Parameter>();
 		parameters.add(new Parameter(OAuthInterface.PARAM_OAUTH_CONSUMER_KEY,
 				apiKey));
 		OAuthUtils.addOAuthToken(parameters);
 		parameters.add(new Parameter("name", name));
 		if (description != null) {
 			parameters.add(new Parameter("description", description));
 		}
 		parameters.add(new Parameter("category", category != null ? category
 				.getCategoryId() : PhotoCategory.Uncategorized));
 		if (exif != null) {
 			if (exif.getShutterSpeed() != null) {
 				parameters.add(new Parameter("shutter_speed", exif
 						.getShutterSpeed()));
 			}
 			if (exif.getFocalLength() != null) {
 				parameters.add(new Parameter("focal_length", exif
 						.getFocalLength()));
 			}
 			if (exif.getAperture() != null) {
 				parameters.add(new Parameter("aperture", exif.getAperture()));
 			}
 			if (exif.getIso() != null) {
 				parameters.add(new Parameter("iso", exif.getIso()));
 			}
 			if (exif.getCamera() != null) {
 				parameters.add(new Parameter("camera", exif.getCamera()));
 			}
 			if (exif.getLens() != null) {
 				parameters.add(new Parameter("lens", exif.getLens()));
 			}
 		}
 
 		if (latitude != null) {
 			parameters.add(new Parameter("latitude", latitude));
 		}
 
 		if (longitude != null) {
 			parameters.add(new Parameter("longitude", longitude));
 		}
 
 		Response response = transportAPI.postJSON(sharedSecret,
 				J500pxConstants.PATH_PHOTOS, parameters);
 		if (response.isError()) {
 			throw new J500pxException(response.getErrorMessage());
 		}
 
 		PhotoUpload upload = new PhotoUpload();
 		upload.setUploadKey(response.getData().getString("upload_key"));
 		upload.setPhoto(PhotoUtils.parsePhoto(response.getData().getJSONObject("photo")));
 		return upload;
 	}
 
 	/**
 	 * @param photoId
 	 * @param vote
 	 *            false for 'dislike' or true for 'like'.
 	 * @throws J500pxException
 	 * @throws JSONException
 	 * @throws IOException
 	 */
 	public Photo votePhoto(int photoId, boolean vote) throws J500pxException,
 			IOException, JSONException {
 		boolean signed = OAuthUtils.hasSigned();
 		if (!signed) {
 			throw new J500pxException("must sign in first.");
 		}
 
 		List<Parameter> parameters = new ArrayList<Parameter>();
 		parameters.add(new Parameter(OAuthInterface.PARAM_OAUTH_CONSUMER_KEY,
 				apiKey));
 		OAuthUtils.addOAuthToken(parameters);
 		parameters.add(new Parameter("vote", vote ? "1" : "0"));
 
 		final String path = String.format(Locale.US,
 				J500pxConstants.PATH_PHOTO_VOTE, photoId);
 		Response response = transportAPI.postJSON(sharedSecret, path,
 				parameters);
 		if (response.isError()) {
 			throw new J500pxException(response.getErrorMessage());
 		}
 		return PhotoUtils.parsePhoto(response.getData().getJSONObject("photo"));
 	}
 
 	/**
 	 * @param photoId
 	 * @param fav
 	 * @return
 	 * @throws J500pxException
 	 * @throws IOException
 	 * @throws JSONException
 	 */
 	public void likePhoto(int photoId, boolean fav) throws J500pxException,
 			IOException, JSONException {
 		boolean signed = OAuthUtils.hasSigned();
 		if (!signed) {
 			throw new J500pxException("must sign in first.");
 		}
 
 		List<Parameter> parameters = new ArrayList<Parameter>();
 		parameters.add(new Parameter(OAuthInterface.PARAM_OAUTH_CONSUMER_KEY,
 				apiKey));
 		OAuthUtils.addOAuthToken(parameters);
 
 		final String path = String.format(Locale.US,
 				J500pxConstants.PATH_PHOTO_FAVORITE, photoId);
 		Response response = null;
 		if (fav) {
 			response = transportAPI.postJSON(sharedSecret, path, parameters);
 		} else {
 			// delete
 			response = transportAPI.deleteJSON(sharedSecret, path, parameters);
 		}
 		if (response.isError()) {
 			throw new J500pxException(response.getErrorMessage());
 		}
 	}
 
 	/**
 	 * Comments a given photo identified by <code>photoId</code>. This needs
 	 * auth.
 	 * 
 	 * @param photoId
 	 * @param comment
 	 * @throws J500pxException
 	 * @throws JSONException
 	 * @throws IOException
 	 */
 	public void commentPhoto(int photoId, String comment)
 			throws J500pxException, IOException, JSONException {
 		boolean signed = OAuthUtils.hasSigned();
 		if (!signed) {
 			throw new J500pxException("must sign in first.");
 		}
 
 		String path = String.format(Locale.US,
 				J500pxConstants.PATH_PHOTO_COMMENTS, photoId);
 		List<Parameter> parameters = new ArrayList<Parameter>();
 		parameters.add(new Parameter(OAuthInterface.PARAM_OAUTH_CONSUMER_KEY,
 				apiKey));
 		OAuthUtils.addOAuthToken(parameters);
 		parameters.add(new Parameter("body", comment));
 		Response response = transportAPI.postJSON(sharedSecret, path,
 				parameters);
 		if (response.isError()) {
 			throw new J500pxException(response.getErrorMessage());
 		}
 
 	}
 
 }
