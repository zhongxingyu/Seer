 package com.sk.api.impl;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.scribe.builder.api.FacebookApi;
 import org.scribe.model.OAuthRequest;
 import org.scribe.model.Response;
 import org.scribe.model.Verb;
 
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import com.sk.Driver;
 import com.sk.api.AbstractApiSearcher;
 import com.sk.api.ApiUtility;
 import com.sk.api.NameComparison;
 import com.sk.threading.TaskGroup;
 import com.sk.util.FieldBuilder;
 import com.sk.util.PersonalData;
 
 public class FacebookApiSearcher extends AbstractApiSearcher {
 
 	public FacebookApiSearcher() {
 		super(new ApiUtility(FacebookApi.class));
 		util.init("gtgrab");
 	}
 
 	private static final int STEP_AMOUNT = 50;
 	private static final String BASE = "https://graph.facebook.com/search?type=user&limit=" + STEP_AMOUNT
 			+ "&q=%s%%20%s&offset=%d";
 	private static final String USER = "https://graph.facebook.com/%s";
 	private static final JsonParser parser = new JsonParser();
 
 	@Override
 	public boolean lookForName(String first, String last) throws IOException {
 		List<PersonalData> data = new ArrayList<>();
 		this.data.remove();
 		for (int loc = 0; loc >= 0; loc = parse(util.send(getNameRequest(first, last, loc)), loc, data, first,
 				last))
 			;
 		if (data.isEmpty())
 			return false;
 		this.data.set(data.toArray(new PersonalData[data.size()]));
 		return true;
 	}
 
 	private OAuthRequest getNameRequest(String first, String last, int start) {
 		try {
 			return new OAuthRequest(Verb.GET, String.format(BASE, URLEncoder.encode(first, "UTF-8"),
 					URLEncoder.encode(last, "UTF-8"), start));
 		} catch (UnsupportedEncodingException e) {
 			return null;
 		}
 	}
 
 	private static final int BAD_THRESHOLD = 3;
 
 	private int parse(Response resp, int start, final List<PersonalData> data, String... names) {
 		if (resp == null || resp.getBody() == null || resp.getBody().length() == 0)
 			return -1;
 		JsonObject responseObject = parser.parse(resp.getBody()).getAsJsonObject();
 		TaskGroup tasks = new TaskGroup();
 		NameComparison nameUtil = NameComparison.get();
 		int badCount = 0;
 		for (JsonElement basicElement : responseObject.get("data").getAsJsonArray()) {
 			JsonObject base = basicElement.getAsJsonObject();
 			if (!nameUtil.isSameName(nameUtil.parseName(base.get("name").getAsString()), names)) {
 				if (badCount++ < BAD_THRESHOLD)
 					continue;
 				else
 					break;
 			}
 			final String id = base.get("id").getAsString();
 			try {
 				final OAuthRequest person = new OAuthRequest(Verb.GET, String.format(USER,
 						URLEncoder.encode(id, "UTF-8")));
 				tasks.add(new Runnable() {
 					@Override
 					public void run() {
 						Response resp = util.send(person);
 						if (resp == null || resp.getBody() == null)
 							return;
 						JsonObject response = parser.parse(resp.getBody()).getAsJsonObject();
 						FieldBuilder builder = new FieldBuilder();
 						builder.put(response, "first_name", "firstName");
 						builder.put(response, "last_name", "lastName");
 						builder.put(response, "gender");
 						builder.put(response, "email");
 						builder.put(response, "id");
 						PersonalData dat = new PersonalData("facebook");
 						builder.addTo(dat);
 						data.add(dat);
 					}
 				});
 			} catch (UnsupportedEncodingException e) {
 				continue;
 			}
 		}
 		tasks.submit(Driver.EXECUTOR);
 		if (!tasks.waitFor())
 			return -1;
		if (responseObject.has("paging") && badCount < BAD_THRESHOLD)
 			return start + STEP_AMOUNT;
 		else
 			return -1;
 	}
 }
