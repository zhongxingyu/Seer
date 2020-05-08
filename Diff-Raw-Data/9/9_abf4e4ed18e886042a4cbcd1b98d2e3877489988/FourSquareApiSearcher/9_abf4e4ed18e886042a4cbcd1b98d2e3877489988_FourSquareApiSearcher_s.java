 package com.sk.api.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.scribe.builder.api.Foursquare2Api;
 import org.scribe.model.OAuthRequest;
 import org.scribe.model.Response;
 import org.scribe.model.Verb;
 
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import com.sk.api.AbstractApiSearcher;
 import com.sk.api.ApiUtility;
 import com.sk.util.PersonalData;
 
 public class FourSquareApiSearcher extends AbstractApiSearcher {
 
 	public FourSquareApiSearcher() {
 		super(new ApiUtility(Foursquare2Api.class));
 		util.init("gtgrab");
 	}
 
 	private static final String URL = "https://api.foursquare.com/v2/users/search?v=20130710&oauth_token=%s&name=%s%%20%s";
 
 	@Override
 	public OAuthRequest getNameRequest(String first, String last) {
 		return new OAuthRequest(Verb.GET, String.format(URL, util.getAccessToken().getToken(), first, last));
 	}
 
 	@Override
 	public boolean parseResponse(Response resp) {
 		String body;
 		if (resp == null || (body = resp.getBody()) == null)
 			return false;
 		JsonObject obj = new JsonParser().parse(body).getAsJsonObject();
 		List<PersonalData> ret = new ArrayList<>();
 		for (JsonElement e : obj.get("response").getAsJsonObject().get("results").getAsJsonArray()) {
 			JsonObject user = e.getAsJsonObject();
 			PersonalData dat = new PersonalData("foursquare");
 			if (user.has("type"))
 				continue;
 			if (user.has("firstName"))
 				dat.put("first-name", user.get("firstName").getAsString());
 			if (user.has("lastName"))
 				dat.put("last-name", user.get("lastName").getAsString());
 			if (user.has("gender"))
 				dat.put("gender", user.get("gender").getAsString());
 			if (user.has("homeCity"))
 				dat.put("location", user.get("homeCity").getAsString());
 			if (dat.size() > 0)
 				ret.add(dat);
 		}
 		this.data.set(ret.toArray(new PersonalData[ret.size()]));
 		return true;
 	}
 
 }
