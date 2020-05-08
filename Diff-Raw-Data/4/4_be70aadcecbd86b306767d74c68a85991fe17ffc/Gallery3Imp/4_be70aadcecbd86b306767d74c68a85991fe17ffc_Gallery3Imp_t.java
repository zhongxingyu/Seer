 /*
  * GalDroid - a webgallery frontend for android
  * Copyright (C) 2011  Raptor 2101 [raptor2101@gmx.de]
  *		
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  
  */
 
 package de.raptor2101.GalDroid.WebGallery.Gallery3;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.HttpMultipartMode;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.StringBody;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.AsyncTask.Status;
 import android.util.FloatMath;
 
 import de.raptor2101.GalDroid.WebGallery.GalleryStream;
 import de.raptor2101.GalDroid.WebGallery.Gallery3.JSON.AlbumEntity;
 import de.raptor2101.GalDroid.WebGallery.Gallery3.JSON.CommentEntity;
 import de.raptor2101.GalDroid.WebGallery.Gallery3.JSON.CommentEntity.CommentState;
 import de.raptor2101.GalDroid.WebGallery.Gallery3.JSON.Entity;
 import de.raptor2101.GalDroid.WebGallery.Gallery3.JSON.EntityFactory;
 import de.raptor2101.GalDroid.WebGallery.Gallery3.Tasks.JSONArrayLoaderTask;
 import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryDownloadObject;
 import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObject;
 import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObjectComment;
 import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryProgressListener;
 import de.raptor2101.GalDroid.WebGallery.Interfaces.WebGallery;
 
 public class Gallery3Imp implements WebGallery {
 	private HttpClient mHttpClient;
 	private String mSecurityToken;
 	private float mMaxImageDiag;
 	private final String mRootLink;
 	public final String LinkRest_LoadSecurityToken;
 	public final String LinkRest_LoadItem;
 	public final String LinkRest_LoadTag;
 	public final String LinkRest_LoadBunchItems;
 	public final String LinkRest_LoadBunchTags;
 	public final String LinkRest_LoadPicture;
 	public final String LinkRest_LoadComments;
 
 	private static int MAX_REQUEST_SIZE = 4000;
 	
 	public Gallery3Imp(String rootLink)
 	{
 		mRootLink = rootLink;
 		LinkRest_LoadSecurityToken = rootLink+"/index.php/rest";
 		LinkRest_LoadItem = rootLink+"/index.php/rest/item/%d";
 		LinkRest_LoadTag = rootLink+"/index.php/rest/tag/%s";
 		LinkRest_LoadBunchItems = rootLink +"/index.php/rest/items?urls=[%s]";
 		LinkRest_LoadBunchTags = rootLink +"/index.php/rest/tags?urls=[%s]";
 		LinkRest_LoadPicture = rootLink +"/index.php/rest/data/%s?size=%s";
 		LinkRest_LoadComments = rootLink + "/index.php/rest/item_comments/%d";
 	}
 	
 	public String getItemLink(int id){
 		return String.format(LinkRest_LoadItem, id);
 	}
 	
 	private RestCall buildRestCall(String url, long suggestedLength){
 		HttpGet httpRequest = new HttpGet(url);
         
 		httpRequest.addHeader("X-Gallery-Request-Method", "get");
 		httpRequest.addHeader("X-Gallery-Request-Key", mSecurityToken);
 		
 		return new RestCall(this, httpRequest, suggestedLength);
 	}
 	
 	private Entity loadGalleryEntity(String url) throws ClientProtocolException, IOException, JSONException
 	{
 		
 		RestCall restCall = buildRestCall(url, -1);
 		return EntityFactory.parseJSON(restCall.loadJSONObject(), this);
 	}	
 	
 	private List<JSONObject> loadJSONObjectsParallel(String bunchUrl,List<String> urls, int taskCount, GalleryProgressListener listener) {
 		int objectSize = urls.size();
 		ArrayList<JSONArrayLoaderTask> tasks = new ArrayList<JSONArrayLoaderTask>(taskCount);
 		
 		ArrayList<JSONObject> jsonObjects = new ArrayList<JSONObject>(objectSize);
 		
 		for(int i=0; i<objectSize; ) {
 			StringBuilder requstUrl = new StringBuilder(MAX_REQUEST_SIZE);
 			JSONArrayLoaderTask task = new JSONArrayLoaderTask();
 			tasks.add(task);
 			/*
 			 *  TODO Kommentar anpassen  
 			 *  Otherwise the max length of a GET-Request might be exceeded
 			 */
 			
 			for( ; i<objectSize; i++) {
 				String url = urls.get(i);
 				if(url.length()+requstUrl.length() > MAX_REQUEST_SIZE)
 				{
 					break;
 				}
 				requstUrl.append("%22" + url + "%22,");
 			}
 			
 			requstUrl.deleteCharAt(requstUrl.length() - 1);
 			String realUrl = String.format(bunchUrl, requstUrl);
 			
 			task.execute(buildRestCall(realUrl, -1));
 		}
 		
 		for(JSONArrayLoaderTask task:tasks) {
 			if(task.getStatus() != Status.FINISHED) {
 				try {
 					JSONArray jsonArray = task.get();
 					for(int i=0; i<jsonArray.length(); i++) {
 						try {
 							jsonObjects.add(jsonArray.getJSONObject(i));
							if(listener != null) {
								listener.handleProgress(jsonObjects.size(), objectSize);
							}
 						} catch (JSONException e) {
 							e.printStackTrace();
 						}
 					}
 				} catch (InterruptedException e) {
 					
 				} catch (ExecutionException e) {
 					
 				}
 			}
 		}
 		
 		return jsonObjects;
 	}
 
 	public GalleryObject getDisplayObject(String url) throws ClientProtocolException, IOException, JSONException {
 		return loadGalleryEntity(url);
 	}
 	
 	public List<GalleryObject> getDisplayObjects() {
 		return getDisplayObjects(String.format(LinkRest_LoadItem, 1), null);
 	}
 	
 	public List<GalleryObject> getDisplayObjects(String url) {
 		return getDisplayObjects(url, null);
 	}
 	
 	public List<GalleryObject> getDisplayObjects(GalleryProgressListener progressListener) {
 		return getDisplayObjects(String.format(LinkRest_LoadItem, 1), progressListener);
 	}
 	
 	public List<GalleryObject> getDisplayObjects(String url, GalleryProgressListener listener) {
 		ArrayList<GalleryObject> displayObjects;
 		try {
 			GalleryObject galleryObject = loadGalleryEntity(url);
 			if(galleryObject.hasChildren()) {
 				AlbumEntity album = (AlbumEntity) galleryObject;
 				List<String> members = album.getMembers();
 				
 				displayObjects = new ArrayList<GalleryObject>(members.size());
 				
 				List<JSONObject> jsonObjects = loadJSONObjectsParallel(LinkRest_LoadBunchItems,members,5,listener);
 				
 				for(JSONObject jsonObject:jsonObjects) {
 					if(jsonObject != null) {
 						displayObjects.add(EntityFactory.parseJSON(jsonObject, this));
 					}
 				}
 			}
 			else displayObjects = new ArrayList<GalleryObject>(0);
 				
 			
 		} catch (ClientProtocolException e) {
 			displayObjects = new ArrayList<GalleryObject>(0);
 			e.printStackTrace();
 		} catch (IOException e) {
 			displayObjects = new ArrayList<GalleryObject>(0);
 			e.printStackTrace();
 		} catch (JSONException e) {
 			displayObjects = new ArrayList<GalleryObject>(0);
 			e.printStackTrace();
 		}
 		
 		return displayObjects;
 	}
 	
 	public List<GalleryObject> getDisplayObjects(GalleryObject galleryObject) {
 		return getDisplayObjects(galleryObject, null);
 	}
 
 	public List<GalleryObject> getDisplayObjects(GalleryObject galleryObject, GalleryProgressListener progressListener) {
 		try {
 			Entity entity = (Entity) galleryObject;
 			if(!entity.hasChildren()) {
 				return null;
 			}
 			return getDisplayObjects(entity.getObjectLink(), progressListener);
 		} catch (ClassCastException e) {
 			return null;
 		}
 	}
 	
 	public List<String> getDisplayObjectTags(GalleryObject galleryObject, GalleryProgressListener listener) throws IOException {
 		try {
 			Entity entity = (Entity) galleryObject;
 			List<String> tagLinks = entity.getTagLinks();
 			int linkCount = tagLinks.size();
 			ArrayList<String> tags = new ArrayList<String>(linkCount);
 			ArrayList<JSONObject> jsonObjects = new ArrayList<JSONObject>(linkCount);
 			if (linkCount>0) {
 				for(int i=0; i<linkCount; i++ ) {
 					try {
 						RestCall restCall = buildRestCall(tagLinks.get(i),-1);
 						JSONObject jsonObject = restCall.loadJSONObject();
 						tags.add(EntityFactory.parseTag(jsonObject));
 					} catch (JSONException e) {
 						// Nothing to do here
 					} catch (IOException e) {
 						// Nothing to do here
 					}
 					
 				}
 			}
 			return tags;
 		} catch (ClassCastException e) {
 			throw new IOException("GalleryObject doesn't contain to Gallery3Implementation", e);
 		}
 		
 	}
 
 	public List<GalleryObjectComment> getDisplayObjectComments(GalleryObject galleryObject, GalleryProgressListener listener) throws IOException, ClientProtocolException, JSONException {
 		try {
 			Entity entity = (Entity) galleryObject;
 			String commentSource = String.format(LinkRest_LoadComments, entity.getId());
 			RestCall restCall = buildRestCall(commentSource, -1);
 			JSONObject jsonObject = restCall.loadJSONObject();
 			JSONArray jsonItemComments = jsonObject.getJSONArray("members");
 			int commentCount = jsonItemComments.length();
 			ArrayList<JSONObject> jsonObjects = new ArrayList<JSONObject>(commentCount);
 			ArrayList<GalleryObjectComment> comments = new ArrayList<GalleryObjectComment>(commentCount);
 			ArrayList<String> authors = new ArrayList<String>(commentCount);			
 			
 			for(int i=0; i<commentCount; i++ ) {
 				restCall = buildRestCall(jsonItemComments.getString(i),-1);
 				jsonObject = restCall.loadJSONObject();
 				
 				CommentEntity comment = EntityFactory.parseComment(jsonObject);
 				if(comment.getState() == CommentState.Published) {
 					comments.add(comment);
 					
 					if(!comment.isAuthorInformationLoaded()) {
 						authors.add(comment.getAuthorId());
 					}
 				}
 			}
 			
 			return comments;
 		} catch (ClassCastException e) {
 			throw new IOException("GalleryObject doesn't contain to Gallery3Implementation", e);
 		}
 	}
 
 	private GalleryStream getFileStream(String sourceLink, long suggestedLength) throws ClientProtocolException, IOException{
 		RestCall restCall = buildRestCall(sourceLink, suggestedLength);
 		return restCall.open();
 		
 	}
 	
 	public GalleryStream getFileStream(GalleryDownloadObject galleryDownloadObject) throws ClientProtocolException, IOException {
 		if(!(galleryDownloadObject instanceof DownloadObject)) {
 			throw new IOException("downloadObject don't belong to the Gallery3 Implementation");
 		}
 		DownloadObject downloadObject = (DownloadObject) galleryDownloadObject;
 		if(!(downloadObject.getRootLink().equals(mRootLink))){
 			throw new IOException("downloadObject don't belong to the this Host");
 		}
 		return getFileStream(downloadObject.getUniqueId(), downloadObject.getFileSize());
 	}
 	
 	public void setHttpClient(HttpClient httpClient) {
 		mHttpClient = httpClient;
 	}
 
 	public String getSecurityToken(String user, String password) throws SecurityException {
 		try {
 			HttpPost httpRequest = new HttpPost(LinkRest_LoadSecurityToken);
 	        
 			httpRequest.addHeader("X-Gallery-Request-Method", "post");
 			MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
 		
 			mpEntity.addPart("user", new StringBody(user));
 			mpEntity.addPart("password", new StringBody(password));
 			
 			httpRequest.setEntity(mpEntity);
 	        HttpResponse response;
 		
 			response = mHttpClient.execute(httpRequest);
 			InputStream  inputStream = response.getEntity().getContent();
 			InputStreamReader streamReader = new InputStreamReader(inputStream);
 			BufferedReader reader = new BufferedReader(streamReader);
 			String content = reader.readLine();
 			inputStream.close();
 			if(content.length()==0 || content.startsWith("[]"))
 			{
 				throw new SecurityException("Couldn't verify user-credentials");
 			}
 			
 			return content.trim().replace("\"", "");
 		} catch (Exception e) {
 			throw new SecurityException("Couldn't verify user-credentials", e);
 		}
 	}
 
 	public HttpClient getHttpClient() {
 		return mHttpClient;
 	}
 
 	public void setSecurityToken(String securityToken) {
 		mSecurityToken = securityToken;
 	}
 
 	public void setPreferedDimensions(int height, int width) {
 		mMaxImageDiag = FloatMath.sqrt(height*height+width*width);
 	}
 
 	public String getRootLink() {
 		return mRootLink;
 	}
 
 	public float getMaxImageDiag() {
 		return mMaxImageDiag;
 	}
 }
