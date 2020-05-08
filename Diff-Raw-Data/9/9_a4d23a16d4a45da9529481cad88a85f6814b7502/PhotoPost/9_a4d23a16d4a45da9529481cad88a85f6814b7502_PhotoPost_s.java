 package tumblib;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 
 public class PhotoPost extends Post {
 	TumblrPhoto mainPhoto;
 	TumblrPhoto[] photoArray;
 
 	public PhotoPost(long id, String url, String urlWithSlug, PostType type, long date, String format,
 			int bookmarklets, int mobiles, String reblogKey, String slug, String[] tags, 
 			String photoCaption, String[] photoUrls, TumblrPhoto[] photoArray){
 
 		super(id, url, urlWithSlug, type, date, format, bookmarklets, mobiles, reblogKey, slug, tags);
 		mainPhoto = new TumblrPhoto(photoUrls, photoCaption);
 	}
 
 	public String getContent(){
 		return getPhotoUrl(1280) + "\n" + getCaption();
 	}
 
 	public String getCaption(){
 		return mainPhoto.getCaption();
 	}
 
 	void setPhotoCaption(String photoCaption) {
 		mainPhoto.setPhotoCaption(photoCaption);	
 	}
 
 	public URL getPhotoUrl(int resolution){
 		return mainPhoto.getPhotoUrl(resolution);
 	}
 
 	void setUrls(String[] photoUrls){
 		mainPhoto.setUrls(photoUrls);
 	}
 
 	/******Static methods to be used in deserializing PhotoPosts from JSON.******/
 	static String[] urlsFromJson(JsonObject post){
 		ArrayList<String> urlList = new ArrayList<String>();
 			urlList.add(post.getAsJsonPrimitive("photo-url-500").getAsString());
 			urlList.add(post.getAsJsonPrimitive("photo-url-400").getAsString());
 			urlList.add(post.getAsJsonPrimitive("photo-url-250").getAsString());
 			urlList.add(post.getAsJsonPrimitive("photo-url-100").getAsString());
 			urlList.add(post.getAsJsonPrimitive("photo-url-75").getAsString());
 		try{
 			urlList.add(post.getAsJsonPrimitive("photo-url-1280").getAsString());	
 		}catch(NullPointerException e){
			System.err.println("  The 1280 resolution does not exist for photo.");
 		}
 		
 		String[] urls = {};
 		urls = urlList.toArray(urls);
 		return urls;
 
 	}
 
 	static String captionFromJson(JsonObject post){
 		return post.getAsJsonPrimitive("photo-caption").getAsString();
 	}
 
 	static TumblrPhoto[] photosFromJson(JsonObject post){
 		Iterator<JsonElement> photoIterator = null;
 		if(post.getAsJsonArray("photos").size() > 0)
 			photoIterator = post.getAsJsonArray("photos").iterator();
 		else
 			return null;
 		
 		ArrayList<TumblrPhoto> photos = new ArrayList<TumblrPhoto>();
 		while(photoIterator.hasNext()){
 			JsonObject jsonPhotoObject;
 			jsonPhotoObject =  photoIterator.next().getAsJsonObject();
 			String caption = jsonPhotoObject.getAsJsonPrimitive("caption").getAsString();
 			String[] urls = urlsFromJson(jsonPhotoObject);
 			photos.add(new TumblrPhoto(urls, caption));
 		}
 		
 		TumblrPhoto[] photoArray = {};
 		photoArray = photos.toArray(photoArray);
 		return photoArray;
 
 	}
 
 }
