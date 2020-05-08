 /**@author Yeison Rodriguez */
 package tumblib;
 
 
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import com.google.gson.*;
 
 
 public class PostDeserializer implements JsonDeserializer<Post> {
 
 	/**
 	 * @param JsonElement post is the post in its JSON format.
 	 * @param 
 	 * **/
 	public Post deserialize(JsonElement post, Type typeOfT,
 			JsonDeserializationContext context) throws JsonParseException {
 		
 		//jpo: JSON Post Object
 		JsonObject jpo =  post.getAsJsonObject();
 		
 		long id = id(jpo); String url = url(jpo); String urlWithSlug = urlWithSlug(jpo); 
 		long date = date(jpo); String format = format(jpo); int bookmarklets = bookmarklets(jpo); 
 		int mobiles = mobiles(jpo); String reblogKey = reblogKey(jpo); String slug = slug(jpo); 
 		String[] tags = tags(jpo);
 		
 
 		PostType postType = type(jpo);
 		
 		switch(postType){
 		case audio: 
 			String caption = AudioPost.captionFromJson(jpo);
 			String player = AudioPost.playerFromJson(jpo);
 			int plays = AudioPost.playsFromJson(jpo);
 			return new AudioPost(id, url, urlWithSlug, postType, date, format, bookmarklets, 
 					mobiles, reblogKey, slug, tags, caption, player, plays);
 		case conversation:
 			String convoTitle = ConvoPost.titleFromJson(jpo);
 			String convoText = ConvoPost.textFromJson(jpo);
 			return new ConvoPost(id, url, urlWithSlug, postType, date, format, bookmarklets, 
 					mobiles, reblogKey, slug, tags, convoTitle, convoText);
 		case link:
 			String linkText = LinkPost.textFromJson(jpo);
 			String linkUrl = LinkPost.urlFromJson(jpo);
 			String linkDescription = LinkPost.descriptionFromJson(jpo);
 			return new LinkPost(id, url, urlWithSlug, postType, date, format, bookmarklets, 
 					mobiles, reblogKey, slug, tags, linkText, linkUrl, linkDescription);
 		case photo:
 			String photoCaption = PhotoPost.captionFromJson(jpo);
 			String[] photoUrls = PhotoPost.urlsFromJson(jpo);
 			TumblrPhoto[] photoArray = PhotoPost.photosFromJson(jpo);
 			return new PhotoPost(id, url, urlWithSlug, postType, date, format, bookmarklets, 
 					mobiles, reblogKey, slug, tags, photoCaption, photoUrls, photoArray);
 		case quote:
 			String quoteText = QuotePost.textFromJson(jpo);
 			String quoteSource = QuotePost.sourceFromJson(jpo);
 			return new QuotePost(id, url, urlWithSlug, postType, date, format, bookmarklets, 
 					mobiles, reblogKey, slug, tags, quoteText, quoteSource);
 		case regular:
 			String title = RegularPost.titleFromJson(jpo);
 			String body = RegularPost.bodyFromJson(jpo);
 			return new RegularPost(id, url, urlWithSlug, postType, date, format, bookmarklets, 
 					mobiles, reblogKey, slug, tags, title, body);
 		case video: 
 			String videoSource = VideoPost.sourceFromJson(jpo);
 			String videoCaption = VideoPost.captionFromJson(jpo);
 			String videoPlayer = VideoPost.playerFromJson(jpo);
 			return new VideoPost(id, url, urlWithSlug, postType, date, format, bookmarklets, 
 					mobiles, reblogKey, slug, tags, videoSource, videoCaption, videoPlayer);
 		default: return new Post(id, url, urlWithSlug, postType, date, format, bookmarklets, mobiles, 
 					reblogKey, slug, tags);
 		}
 	}
 	
 	PostType typeCheck(String type){
 		return PostType.valueOf(type);
 	}
 	
 	/**@return The unique id number of the post as a long.**/
 	long id(JsonObject post){
 		return post.getAsJsonPrimitive("id").getAsLong();
 	}
 	
 	/**@return The URL of the post as a String.**/
 	String url(JsonObject post){
 		return post.getAsJsonPrimitive("url").getAsString();
 	}
 	
 	/**@return The URL of the post with its slug as a string.**/
 	String urlWithSlug(JsonObject post){
 		return post.getAsJsonPrimitive("url-with-slug").getAsString();
 	}
 	
 	/**@return The type of the post as a PostType object.**/
 	PostType type(JsonObject post){
 		String typeString = post.getAsJsonPrimitive("type").getAsString();
 		return typeCheck(typeString);
 	}
 	
 	/**@return The date as a long.  The long represents a unix-timestamp: milliseconds 
 	 * since January 1, 1970, 00:00:00 GMT**/
 	long date(JsonObject post){
 		//Dates from the tumblr read api are represented in seconds not millis.
 		return post.getAsJsonPrimitive("unix-timestamp").getAsLong()*1000;
 	}
 	
 	String format(JsonObject post){
 		return post.getAsJsonPrimitive("format").getAsString();
 	}
 	
 	/**@return The number of times this post has been bookmarklet as int. **/
 	int bookmarklets(JsonObject post){
 		return post.getAsJsonPrimitive("bookmarklet").getAsInt();
 	}
 	
 	/**@return **/
 	int mobiles(JsonObject post){
 		return post.getAsJsonPrimitive("mobile").getAsInt();
 	}
 	
 	/**@return The reblog-key of the post as a String.**/
 	String reblogKey(JsonObject post){
 		return post.getAsJsonPrimitive("reblog-key").getAsString();
 	}
 	
 	/**@return The slug for the url as string.**/
 	String slug(JsonObject post){
 		return post.getAsJsonPrimitive("slug").getAsString();
 	}
 	
 	/**@return The tags that this post has been categorized under, as an array of strings.**/
 	String [] tags(JsonObject post){
 		Iterator<JsonElement> tagIterator = null;
 		try{
 			tagIterator = post.getAsJsonArray("tags").iterator();
 		}catch(NullPointerException e){
			//System.err.println("  Post " + post.getAsJsonPrimitive("id") +" does not have tags.");
 			return null;
 		}
 		ArrayList<String> tags = new ArrayList<String>();
 		
 		while(tagIterator.hasNext()){
 			tags.add(tagIterator.next().getAsString());
 		}
 		
 		String[] tagsArray = {};
 		tagsArray = tags.toArray(tagsArray);
 		
 		return tagsArray;
 	}
 	
 }
