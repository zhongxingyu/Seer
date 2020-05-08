 package controllers;
 
 
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.math.BigInteger;
 import java.net.URLEncoder;
 import java.util.Date;
 import java.util.List;
 
 import models.Acceptance;
 import models.Offer;
 import models.User;
 import play.libs.WS;
 import play.libs.WS.HttpResponse;
 import play.mvc.Controller;
 import play.mvc.Http.StatusCode;
 import siena.Model;
 import siena.Query;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonElement;
 
 import dwolla.DwollaTransfer;
 
 public class Offers extends BaseController {
 
 	protected static Offer parseJSON(InputStream in) {
 		return new Gson().fromJson(new InputStreamReader(in), Offer.class);
 	}
 
 	public static void create() {
 		Offer offer = parseJSON(request.body);
 		String token = request.headers.get("authorization").values.get(0);
 		User owner = all().filter("token", token.replaceAll("\"", "")).get();
 		offer.owner = owner;
 		offer.insert();
 		renderJSON(offer);
 	}
 	
 	public static void getAll() {
 		List<Offer> offers = Model.all(Offer.class).fetch();
 		for (Offer offer : offers) {
 			offer.acceptedCount = getAcceptances(offer);
 			offer.eligible = getEligible(offer, getUser());
 		}
 		renderJSON(offers);
 	}
 
 	private static boolean getEligible(Offer offer, User user) {
 		int count = Model.all(Acceptance.class).filter("offer", offer).filter("acceptor", user).count();
 		return count == 0;
 	}
 
 	private static int getAcceptances(Offer offer) {
 		return Model.all(Acceptance.class).filter("offer", offer).count();
 	}
 	
 	public static void accept(Long id) throws Exception {
 		User user = getUser();
 		Offer offer = Model.all(Offer.class).filter("id", id).get();
 		if (!getEligible(offer, user)) {
 			response.status = StatusCode.BAD_REQUEST;
			renderJSON("You are ineligible for this offer.");
 		}
 		if (offer.cap <= getAcceptances(offer)) {
 			response.status = StatusCode.BAD_REQUEST;
			renderJSON("This offer has already reached its limit.");
 		}
 		String url = "https://api.singly.com/types/statuses?access_token="+ user.singlyAccessToken + "&to="+offer.type.toLowerCase()+"&body="+ URLEncoder.encode(offer.content,"UTF-8");
 		HttpResponse post = WS.url(url).post();
 		JsonElement twitter = post.getJson().getAsJsonObject().get("twitter");
 		if (twitter.getAsJsonObject().get("errors") != null) {
 			response.status = StatusCode.BAD_REQUEST;
 			renderJSON(twitter.getAsJsonObject().get("errors").toString());
 		} else {
 			String executionId = twitter.getAsJsonObject().get("id").getAsString();
 			Acceptance acceptance = new Acceptance();
 			acceptance.acceptor = user;
 			acceptance.offer = offer;
 			acceptance.executed = true;
 			acceptance.executionTime = new Date();
 			acceptance.executionId = executionId;
 			acceptance.save();
 			renderJSON(acceptance);
 		}
 	}
 	
 	public static void acceptances() {
 		renderJSON(Model.all(Acceptance.class).fetch());
 	}
 	
 
 
 	static Query<User> all() {
         return Model.all(User.class);
     }
 }
