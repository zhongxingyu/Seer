 package com.sk.impl2;
 
 import static com.sk.impl2.FacebookApiLoader.SITE_KEY;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import com.google.gson.JsonObject;
 import com.sk.parse.IndividualExtractor;
 import com.sk.parse.Parsers;
 import com.sk.util.DocNavigator;
 import com.sk.util.FieldBuilder;
 import com.sk.util.PersonalData;
 import com.sk.web.IOUtil;
import com.sk.web.OAuthRequest;
 import com.sk.web.Request;
 
 public class FacebookPersonLoader extends IndividualExtractor {
 
 	private static final String BASE_URL = "https://graph.facebook.com/%s";
 
	private final String url;
 
 	private JsonObject json;
 
	public FacebookPersonLoader(String id) {
		this.url = String.format(BASE_URL, IOUtil.urlEncode(id));
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
 		json = Parsers.parseJSON(data).getAsJsonObject();
 	}
 
 	@Override
 	protected PersonalData getResult() {
 		init();
 		FieldBuilder builder = new FieldBuilder();
 		PersonalData ret = new PersonalData(SITE_KEY);
 		for (DocNavigator navigator : navigators) {
 			navigator.navigate(json, builder);
 		}
 		builder.joinNames();
 		builder.addTo(ret);
 		return ret;
 	}
 
 	private static final DocNavigator[] navigators = { new DocNavigator("firstName", "first_name"),
 			new DocNavigator("lastName", "last_name"), new DocNavigator("gender", "gender"),
 			new DocNavigator("email", "email"), new DocNavigator("id", "id") };
 
 }
