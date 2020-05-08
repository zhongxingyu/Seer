 package controllers;
 
 import play.*;
 import play.cache.Cache;
 import play.data.validation.*;
 import play.libs.Codec;
 import play.libs.F.IndexedEvent;
 import play.libs.F.Promise;
 import play.libs.WS.HttpResponse;
 import play.libs.Images;
 import play.libs.WS;
 import play.mvc.*;
 import securesocial.provider.ProviderType;
 import securesocial.provider.SocialUser;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.*;
 import java.util.concurrent.ExecutionException;
 
 import javax.xml.namespace.QName;
 import javax.xml.transform.TransformerException;
 
 import org.w3c.dom.Document;
 
 import com.google.gson.JsonObject;
 import com.google.gson.reflect.TypeToken;
 
 import controllers.Authentifier;
 import controllers.WebService;
 import controllers.securesocial.SecureSocial;
 
 import models.*;
 import models.eventstream.Event;
 import models.eventstream.EventTopic;
 
 /**
  * The Application controller is the main controller in charge of all basic
  * tasks and page rendering.
  * 
  * @author Alexandre Bourdin
  * 
  */
 public class Application extends Controller {
 
 	/**
 	 * Action to call before each action requiring the user to be connected
 	 */
 	@Before(only = { "index", "historicalEvents", "waitEvents", "settings", "updateSettings", "sendEvent", "subscribe", "unsubscribe", "getTopics",
 			"patternQuery", "processPatternQuery", "historicalByTopic", "searchTopics" })
 	private static void checkAuthentification() {
 		if (session.get("socialauth") != null) {
 			session.remove("socialauth");
 			register();
 			return;
 		}
 		String uid = session.get("userid");
 		if (uid == null) {
 			login();
 			return;
 		}
 		User user = ModelManager.get().getUserById(Long.parseLong(uid));
 		if (user == null) {
 			logout();
 			return;
 		}
 		user.lastRequest = new Date();
 		request.args.put("user", user);
 	}
 
 	@Before(only = { "register", "processRegistration", "login", "processLogin" })
 	private static void checkIsConnected() {
 		if (session.get("userid") != null) {
 			index();
 		}
 	}
 
 	/**
 	 * Index action, renders the main page of the web application
 	 */
 	public static void index() {
 		User u = (User) request.args.get("user");
 		if (u == null) {
 			logout();
 		}
 		ArrayList<EventTopic> topics = new ArrayList<EventTopic>();
 		topics.addAll(ModelManager.get().getTopics());
 		ArrayList<EventTopic> userTopics = u.getTopics();
 		for (int i = 0; i < userTopics.size(); i++) {
 			topics.remove(userTopics.get(i));
 		}
 		SocialUser su = SecureSocial.getCurrentUser();
 		render(u, topics, userTopics, su);
 	}
 
 	/**
 	 * Historical events
 	 */
 	public static void historicalEvents() {
 		User u = (User) request.args.get("user");
 		if (u == null) {
 			logout();
 		}
 		ArrayList<EventTopic> userTopics = u.getTopics();
 		render(userTopics);
 	}
 
 	public static void historicalByTopic(String topicId) {
 		EventTopic et = ModelManager.get().getTopicById(topicId);
 		ArrayList<Event> events = WebService.getHistorical(et);
 		renderJSON(events, new TypeToken<ArrayList<Event>>() {
 		}.getType());
 	}
 
 	/**
 	 * Pattern Queries
 	 */
 
 	public static void patternQuery() {
 		render();
 	}
 
	public static void processTokenPatternQuery(String token, String eventtopic) {
 		if (token != null && token != "") {
			Boolean result = WebService.sendTokenPatternQuery(token, eventtopic);
 			if (!result) {
 				flash.error("The operation encoutered an error.");
 			}
 		}
 		patternQuery();
 	}
 
 	public static void processComposedPatternQuery(String text) {
 		patternQuery();
 	}
 
 	public static void processFullPatternQuery(String text, String eventtopic) {
 		if (text != null && text != "") {
 			try{
 			Boolean result = WebService.sendFullPatternQuery(text, eventtopic);
 			} catch (Exception e) {
 				flash.error(e.getMessage());
 			}
 		}
 		patternQuery();
 	}
 
 	/**
 	 * Login action, renders the login page
 	 */
 	public static void login() {
 		render();
 	}
 
 	/**
 	 * Logout action
 	 */
 	public static void logout() {
 		if (session.get("userid") != null) {
 			User u = ModelManager.get().getUserById(Long.parseLong(session.get("userid")));
 			if (u != null) {
 				ModelManager.get().disconnect(u);
 			}
 		}
 		session.clear();
 		login();
 	}
 
 	/**
 	 * Login processing action, receives the user's input sent by the login form
 	 * 
 	 * @param login
 	 * @param password
 	 */
 	public static void processLogin(@Required String email, @Required String password) {
 		if (validation.hasErrors()) {
 			flash.error("Please enter your email and password.");
 			login();
 		}
 		User u = ModelManager.get().connect(email, PasswordEncrypt.encrypt(password));
 		if (u != null) {
 			Logger.info("User connected with standard login : " + u);
 			session.put("userid", u.id);
 			index();
 		} else {
 			flash.error("Invalid indentifiers, please try again.");
 			login();
 		}
 	}
 
 	/**
 	 * Register action, renders the registration page
 	 * 
 	 * @param accessToken
 	 */
 	public static void register() {
 		String randomID = Codec.UUID();
 		SocialUser su = Cache.get("su" + session.getId(), SocialUser.class);
 		if (su == null) {
 			su = SecureSocial.getCurrentUser();
 		}
 		render(randomID, su);
 	}
 
 	public static void processRegistration(@Required(message = "Email is required") @Email(message = "Invalid email") @Equals("emailconf") String email,
 			@Required(message = "Email confirmation is required") @Email String emailconf,
 			@Required(message = "Password is required") @Equals("passwordconf") String password,
 			@Required(message = "Passsword confirmation is required") String passwordconf, @Required(message = "Full name is required") String name,
 			@Required(message = "Gender is required") String gender, @Required(message = "Mail notification choice is required") String mailnotif, String code,
 			String randomID) {
 		SocialUser su = SecureSocial.getCurrentUser();
 		if (su == null) {
 			validation.equals(code, Cache.get(randomID)).message("Invalid code. Please type it again");
 			validation.isTrue(User.find("byEmail", email).fetch().size() == 0).message("Email already in use");
 		}
 		if (validation.hasErrors()) {
 			ArrayList<String> errorMsg = new ArrayList<String>();
 //			for (Error error : validation.errors()) {
 //				errorMsg.add(error.message());
 //			}
 			flash.put("error", errorMsg);
 			renderTemplate("Application/register.html", email, emailconf, name, gender, mailnotif, randomID, su);
 		}
 		Cache.delete(randomID);
 		String pwdEncrypt = PasswordEncrypt.encrypt(password);
 		User u = null;
 		if (su != null) {
 			u = User.find("byEmail", su.email).first();
 		}
 		if (u == null) {
 			u = new User(email, pwdEncrypt, name, gender, mailnotif);
 		} else {
 			u.password = pwdEncrypt;
 			u.name = name;
 			u.gender = gender;
 			u.mailnotif = mailnotif;
 		}
 		if (su != null) {
 			if (su.id.provider == ProviderType.facebook) {
 				u.facebookId = su.id.id;
 			} else if (su.id.provider == ProviderType.google) {
 				u.googleId = su.id.id;
 			} else if (su.id.provider == ProviderType.twitter) {
 				u.twitterId = su.id.id;
 			}
 			u.avatarUrl = su.avatarUrl;
 		}
 		u.save();
 		// Connect
 		User uc = ModelManager.get().connect(u.email, pwdEncrypt);
 		if (uc != null) {
 			Logger.info("User registered : " + uc);
 			session.put("userid", uc.id);
 		}
 		index();
 	}
 
 	/**
 	 * Events handlers
 	 */
 
 	public static void sendEvent(@Required String title, @Required String content, @Required String topic) {
 		ModelManager.get().getTopicById(topic).multicast(new models.eventstream.Event(title, content));
 	}
 
 	/**
 	 * Long polling action called by the frontend page via AJAX Returns events
 	 * to the page in a JSON list of events
 	 * 
 	 * @param lastReceived
 	 * @throws InterruptedException
 	 * @throws ExecutionException
 	 */
 	public static void waitEvents(@Required Long lastReceived) throws InterruptedException, ExecutionException {
 		User u = (User) request.args.get("user");
 		if (u == null) {
 			renderJSON("{\"error\":\"disconnected\"}");
 		}
 		List events = await(u.getEventBuffer().nextEvents(lastReceived));
 		renderJSON(events, new TypeToken<List<IndexedEvent<Event>>>() {
 		}.getType());
 	}
 
 	public static void searchTopics(String search, String title, String desc) {
 		User u = (User) request.args.get("user");
 		ArrayList<EventTopic> topics = ModelManager.get().getTopics();
 		boolean searchTitle = Boolean.parseBoolean(title);
 		boolean searchContent = Boolean.parseBoolean(desc);
 		ArrayList<EventTopic> result = new ArrayList<EventTopic>();
 		ArrayList<EventTopic> userTopics = u.getTopics();
 		for (int i = 0; i < topics.size(); i++) {
 			EventTopic currentTopic = topics.get(i);
 			if (!userTopics.contains(currentTopic)) {
 				if (search.equals("")) {
 					result.add(topics.get(i));
 					continue;
 				} else {
 					if (searchTitle) {
 						if (BoyerMoore.match(search.toLowerCase(), currentTopic.getId().toLowerCase()).size() > 0) {
 							result.add(topics.get(i));
 							continue;
 						}
 					}
 					if (searchContent) {
 						if (BoyerMoore.match(search.toLowerCase(), currentTopic.content.toLowerCase()).size() > 0) {
 							result.add(topics.get(i));
 							continue;
 						}
 					}
 				}
 			}
 		}
 		render(result);
 	}
 
 	/**
 	 * Subscription action
 	 * 
 	 * @param topicId
 	 */
 	public static void subscribe(@Required String topicId) {
 		User u = (User) request.args.get("user");
 		String result = "{\"id\":\"-1\"}";
 		if (u != null) {
 			EventTopic et = ModelManager.get().getTopicById(topicId);
 			if (et != null) {
 				if (et.subscribersCount < 1) {
 					if (WebService.subscribe(et) == 1) {
 						if (u.subscribe(et)) {
 							result = "{\"id\":\"" + et.getId() + "\",\"title\":\"" + et.title + "\",\"icon\":\"" + et.icon + "\",\"content\":\"" + et.content
 									+ "\",\"path\":\"" + et.path + "\"}";
 						}
 					}
 				} else {
 					if (u.subscribe(et)) {
 						result = "{\"id\":\"" + et.getId() + "\",\"title\":\"" + et.title + "\",\"icon\":\"" + et.icon + "\",\"content\":\"" + et.content
 								+ "\",\"path\":\"" + et.path + "\"}";
 					}
 				}
 			}
 		}
 		renderJSON(result);
 	}
 
 	/**
 	 * Unsubscription action
 	 * 
 	 * @param topicId
 	 */
 	public static void unsubscribe(@Required String topicId) {
 		User u = (User) request.args.get("user");
 		String result = "{\"id\":\"-1\"}";
 		if (u != null) {
 			EventTopic et = ModelManager.get().getTopicById(topicId);
 			if (et != null) {
 				if (u.unsubscribe(et)) {
 					if (et.subscribersCount < 1) {
 						WebService.unsubscribe(et);
 					}
 					result = "{\"id\":\"" + et.getId() + "\",\"title\":\"" + et.title + "\",\"icon\":\"" + et.icon + "\",\"content\":\"" + et.content
 							+ "\",\"path\":\"" + et.path + "\"}";
 				}
 			}
 		}
 		renderJSON(result);
 	}
 
 	/**
 	 * Captcha generator
 	 */
 	public static void captcha(String id) {
 		Images.Captcha captcha = Images.captcha();
 		String code = captcha.getText("#33FF6F");
 		Cache.set(id, code, "10mn");
 		renderBinary(captcha);
 	}
 
 	/**
 	 * Settings page
 	 */
 	public static void settings() {
 		Long id = Long.parseLong(session.get("userid"));
 		User u = ModelManager.get().getUserById(id);
 		render(u);
 	}
 
 	/**
 	 * Update settings
 	 */
 	public static void updateSettings(String password, String newpassword, String newpasswordconf, @Required(message = "Name is required") String name,
 			@Required(message = "Gender is required") String gender, @Required(message = "Mail notification choice is required") String mailnotif) {
 		Long id = Long.parseLong(session.get("userid"));
 		User u = ModelManager.get().getUserById(id);
 		if (!newpassword.equals("")) {
 			validation.equals(password, u.password).message("Wrong password");
 			validation.equals(newpassword, newpasswordconf).message("New password and confirmation don't match.");
 			if (!validation.hasErrors()) {
 				u.password = newpassword;
 			}
 		}
 		if (validation.hasErrors()) {
 			ArrayList<String> errorMsg = new ArrayList<String>();
 //			for (Error error : validation.errors()) {
 //				errorMsg.add(error.message());
 //			}
 			flash.put("error", errorMsg);
 			settings();
 		}
 		u.name = name;
 		u.gender = gender;
 		u.mailnotif = mailnotif;
 		u.update();
 		settings();
 	}
 
 	/**
 	 * FAQ page
 	 */
 	public static void faq() {
 		render();
 	}
 
 	private static String fullURL() {
 		String url = "Application." + Thread.currentThread().getStackTrace()[2].getMethodName();
 		return play.mvc.Router.getFullUrl(url);
 	}
 
 	private static String fullURL(String url) {
 		return play.mvc.Router.getFullUrl(url);
 	}
 }
