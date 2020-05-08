 package com.sk.api.impl;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.scribe.builder.api.TwitterApi;
 import org.scribe.model.OAuthRequest;
 import org.scribe.model.Response;
 import org.scribe.model.Verb;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import com.sk.api.AbstractApiSearcher;
 import com.sk.api.ApiUtility;
 import com.sk.util.PersonalData;
 
 public class TwitterApiSearcher extends AbstractApiSearcher {
 
 	public TwitterApiSearcher() {
 		super(new ApiUtility(TwitterApi.class));
 		util.init("gtgrab");
 	}
 
 	@Override
 	public OAuthRequest getNameRequest(String first, String last) {
 		String query;
 		try {
 			query = URLEncoder.encode(first + " " + last, "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			query = "";
 		}
 		return new OAuthRequest(Verb.GET, "https://api.twitter.com/1.1/users/search.json?q=" + query);
 	}
 
 	@Override
 	public boolean parseResponse(Response resp) {
 		String body = resp.getBody();
 		if (body == null || body.length() == 0)
 			return false;
 		JsonElement parsed = new JsonParser().parse(resp.getBody());
 		if (!parsed.isJsonArray())
 			return false;
 		JsonArray users = parsed.getAsJsonArray();
 		List<PersonalData> found = new ArrayList<>();
 		for (JsonElement e : users) {
 			if (!e.isJsonObject())
 				continue;
 			JsonObject user = e.getAsJsonObject();
 			PersonalData add = new PersonalData("twitter");
 			put(user, add, "id_str", "id");
 			put(user, add, "location");
 			put(user, add, "name");
			put(user, add, "screen_name", "username");
 			put(user, add, "url", "home-page");
 			put(user, add, "profile_image_url", "profile-picture-url");
 			found.add(add);
 		}
 		data.set(found.toArray(new PersonalData[found.size()]));
 		return true;
 	}
 
 	private void put(JsonObject src, PersonalData dest, String key) {
 		put(src, dest, key, key);
 	}
 
 	private void put(JsonObject src, PersonalData dest, String skey, String dkey) {
 		if (src.has(skey) && src.get(skey).isJsonPrimitive()) {
 			dest.put(dkey, src.get(skey).getAsString());
 		}
 	}
 
 }
