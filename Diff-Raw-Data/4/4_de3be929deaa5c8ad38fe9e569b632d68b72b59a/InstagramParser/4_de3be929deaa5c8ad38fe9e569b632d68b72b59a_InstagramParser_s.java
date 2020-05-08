 package dal;
 
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 
 import com.google.gson.*;
 
 /**
  * A class that parses JSON-objects to Java-objects
  * @author Marius Vasshus
  */
 public class InstagramParser {
 
 	public List<PictureData> parse(InputStreamReader reader, String searchTag) {
 
 		ArrayList<PictureData> pictures = new ArrayList<PictureData>();
 		JsonParser JParser = new JsonParser();
 		JsonObject JObject = JParser.parse(reader).getAsJsonObject();
 		JsonArray jsonPictures = JObject.get("data").getAsJsonArray();
 
 
 		for(JsonElement j: jsonPictures){
 			PictureData picture = new PictureData();
 
 			JsonObject jsonPicture = j.getAsJsonObject();
 
 			//Checks if there is caption for picture
 			JsonElement caption = jsonPicture.get("caption");
 
 			if(caption.isJsonObject()){
 				if(jsonPicture.get("type").getAsString().equals("image")){
 
 					int time = caption.getAsJsonObject().get("created_time").getAsInt();
 					picture.setCreatedTime(time);
 
					String picID = caption.getAsJsonObject().get("id").getAsString();
					picture.setId(picID);
 
 					JsonElement images = jsonPicture.get("images");
 
 					//Gets url to Standard Resolution Picture
 					JsonElement imageStd = images.getAsJsonObject().get("standard_resolution");
 					String urlStd = imageStd.getAsJsonObject().get("url").getAsString();
 					picture.setUrlStd(urlStd);
 
 					//Gets url to Thumbnail Resolution Picture
 					JsonElement imageThumb = images.getAsJsonObject().get("thumbnail");
 					String urlThumb = imageThumb.getAsJsonObject().get("url").getAsString();
 					picture.setUrlThumb(urlThumb);
 
 					//Sets removeFlag for picture
 					picture.setRemoveFlag(false);
 
 					//Sets hashtag for picture
 					picture.addHashtag(new Hashtag(searchTag));
 					
 					pictures.add(picture);
 				}
 			}
 		}
 		return pictures;
 	}
 }
