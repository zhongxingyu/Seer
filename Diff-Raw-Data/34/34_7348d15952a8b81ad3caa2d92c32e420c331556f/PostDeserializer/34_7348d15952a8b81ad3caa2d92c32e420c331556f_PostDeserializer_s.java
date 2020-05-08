 package tumblib;
 
 import java.lang.reflect.Type;
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
 		
 		// TODO Auto-generated method stub
		return null;
 	}
 	
 	/**@return The unique id number of the post as a long.**/
 	long id(JsonObject post){
 		return post.getAsJsonPrimitive("id").getAsLong();
 	}
 	
 	/**@return The URL of the post as a String.**/
 	String url(JsonObject post){
 		return post.getAsJsonPrimitive("url").getAsString();
 	}
 	
 	/**@return The type of the post as a PostType object.**/
 	PostType type(JsonObject post){
 		String typeString = post.getAsJsonPrimitive("type").getAsString();
 		return typeCheck(typeString);
 	}
 	
 	/**@return The date as a long.  The long represents a unix-timestamp: milliseconds 
 	 * since January 1, 1970, 00:00:00 GMT**/
 	long date(JsonObject post){
 		return post.getAsJsonPrimitive("unix-timestamp").getAsLong();
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
 		Iterator<JsonElement> tagIterator = post.getAsJsonArray("tags").iterator();
		String [] tags = {};
 		
		int i = 0;
 		while(tagIterator.hasNext()){
			tags[i] = tagIterator.next().toString();
 		}
 		
		return tags;
 	}
 	
 	
 	PostType typeCheck(String type){
 		if(type == PostType.audio.toString())
 			return PostType.audio;
 		if(type == PostType.conversation.toString())
 			return PostType.conversation;
 		if(type == PostType.link.toString())
 			return PostType.link;
 		if(type == PostType.photo.toString())
 			return PostType.photo;
 		if(type == PostType.quote.toString())
 			return PostType.quote;
 		if(type == PostType.regular.toString())
 			return PostType.regular;
 		if(type == PostType.video.toString())
 			return PostType.video;
 		else
 			return PostType.unknown;
 	}
 	
 	//TODO: Remove main after testing
 	public static void main(String[] args){
 //		if("audio" == PostType.audio.toString())
 //			System.out.println("True");
 //		else
 //			System.out.println("False");
 	}
 
 }
