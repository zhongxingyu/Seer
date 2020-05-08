 package com.sk.parse.impl;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.sk.parse.util.PagingLoader;
 import com.sk.parse.util.Parsers;
 import com.sk.util.ApiUtility;
 import com.sk.util.NameComparison;
 import com.sk.util.data.FieldBuilder;
 import com.sk.util.data.PersonalData;
 import com.sk.util.navigate.FieldNavigator;
 import com.sk.web.IOUtil;
 import com.sk.web.OAuthRequest;
 import com.sk.web.Request;
 
 public class TwitterApiLoader extends PagingLoader {
 
 	private static final int NUM_RESULTS = 1000, RESULTS_PER_PAGE = 20,
 			NUM_PAGES = NUM_RESULTS / RESULTS_PER_PAGE;
 	private static final String BASE_URL = "https://api.twitter.com/1.1/users/search.json?include_entities=false&count=%d&page=%d&q=%s%%20%s";
 	static final String SITE_KEY = "twitter";
 
 	private final int page;
 	private final String[] names;
 	private final String url;
 
 	private JsonArray users;
 
 	public TwitterApiLoader(String first, String last) {
 		this(new String[] { first, last }, 0);
 	}
 
 	private TwitterApiLoader(String[] names, int page) {
 		this.page = page;
 		this.names = names;
 		this.url = String.format(BASE_URL, RESULTS_PER_PAGE, page, IOUtil.urlEncode(names[0]),
 				IOUtil.urlEncode(names[1]));
 	}
 
 	@Override
 	protected PagingLoader createNextPage() {
 		int nextPage = page + 1;
 		if (nextPage >= NUM_PAGES)
 			return null;
 		if (stopPaging.get())
 			return null;
 		return new TwitterApiLoader(names, nextPage);
 	}
 
 	@Override
 	protected Request getRequest() {
 		try {
 			OAuthRequest request = new OAuthRequest(url, "GET");
 			request.signOAuth(ApiUtility.getConsumerToken(SITE_KEY), ApiUtility.getOAuthToken(SITE_KEY));
 			return request;
 		} catch (MalformedURLException ignored) {
 			return null;
 		}
 	}
 
 	@Override
 	protected void parse(URL source, String data) {
 		users = Parsers.parseJSON(data).getAsJsonArray();
 	}
 
 	@Override
 	protected boolean loadStopPaging() {
 		init();
 		for (JsonElement userElement : users) {
 			if (!checkName(userElement))
 				return true;
 		}
		return false;
 	}
 
 	private boolean checkName(JsonElement userElement) {
 		NameComparison nameUtil = NameComparison.get();
 		String name = getName(userElement);
 		String[] parsed = nameUtil.parseName(name);
 		return nameUtil.isSameFullName(parsed, names);
 	}
 
 	private String getName(JsonElement userElement) {
 		JsonObject user = userElement.getAsJsonObject();
 		if (user.has("name"))
 			return user.get("name").getAsString();
 		else
 			return "";
 	}
 
 	@Override
 	protected List<PersonalData> loadOwnResults() {
 		init();
 		List<PersonalData> ret = new ArrayList<>();
 		boolean stop = false;
 		for (JsonElement userElement : users) {
 			PersonalData data = getData(userElement);
 			if (data == null) {
 				stop = true;
 				continue;
 			}
 			ret.add(data);
 		}
		stopPaging.set(stop);
 		return ret;
 	}
 
 	private PersonalData getData(JsonElement userElement) {
 		if (!checkName(userElement))
 			return null;
 		FieldBuilder builder = new FieldBuilder();
 		for (FieldNavigator navigator : navigators) {
 			navigator.navigate(userElement, builder);
 		}
 		PersonalData ret = new PersonalData(SITE_KEY);
 		builder.addTo(ret);
 		return ret;
 	}
 
 	private static final FieldNavigator[] navigators = { new FieldNavigator("id", "id_str"),
 			new FieldNavigator("location", "location"), new FieldNavigator("username", "screen_name"),
 			new FieldNavigator("homePage", "url"), new FieldNavigator("profilePictureUrl", "profile_image_url"),
 			new FieldNavigator("blob", "description"), new FieldNavigator("twitter", "screen_name") };
 
 }
