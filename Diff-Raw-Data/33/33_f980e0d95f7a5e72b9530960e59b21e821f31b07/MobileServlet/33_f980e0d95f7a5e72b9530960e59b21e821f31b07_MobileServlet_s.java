 /**
  * 
  */
 package bzb.gae.summer;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import bzb.gae.Utility;
 import bzb.gae.exceptions.BadArrivalTimeException;
 import bzb.gae.exceptions.TooFewArgumentsException;
 import bzb.gae.exceptions.TooManyArgumentsException;
 import bzb.gae.exceptions.UserAlreadyExistsException;
 import bzb.gae.exceptions.UserNotFoundException;
 import bzb.gae.summer.jdo.Group;
 import bzb.gae.summer.jdo.User;
 import bzb.gae.ws.FacebookCookie;
 
 /**
  * @author bzb
  * 
  */
 @SuppressWarnings("serial")
 public class MobileServlet extends HttpServlet {
 	
 	private static final String CLIENT_ID = "123839414321448";
 	private static final String CLIENT_SECRET = "9f8b40b75c06e0db09c53452e5a674fe";
 		
 	public void doGet(HttpServletRequest request, HttpServletResponse response)
 			throws IOException {
 
 		if (request.getParameterMap().isEmpty()) {
 			response.setContentType("text/html");
 			
 			FacebookCookie userCookie = null;
 			
 			Cookie[] cookies = request.getCookies();
 			if (cookies != null) {
 				for (int i = 0; i < cookies.length; i++) {
 					if (cookies[i].getName().equals("fbs_" + CLIENT_ID)) {
 						userCookie = new FacebookCookie(cookies[i]);
 						break;
 					}
 				}
 			}
 			String html = "<body><h1>DTC Summer School</h1><div class=\"bodybox\">";
 			
 			if (userCookie != null) {
 				html += "<p>Hi " + userCookie.getFirstName() + ". ";
 				User user;
 				if ((user = User.getUser(userCookie.getProfileURL())) != null) {
 					Group thisGroup = Group.getGroup(user.getGroupKey());
 					
 					if (Utility.getMinutesUntil(thisGroup.getArrivalTime()) < EndOfJourneyChecker.MINUTES_BEFORE_ARRIVAL) {
 						html = Utility.headWithTitle("DTC Summer School App") + html + "Your group is about to arrive, so no more travellers will join it.</p><p>";
 					} else {
 						html = Utility.headWithRefreshAndTitle("DTC Summer School App") + html;
 					}
 					
 					List<User> groupMembers = thisGroup.getUsers(user.getUsername());
 					if (groupMembers.size() > 0) {
 						html += "Your group contains ...</p>" +
 								"<ul>";
 						Iterator<User> it = groupMembers.iterator();
 						while (it.hasNext()) {
 							User thisUser = it.next();
 							html += "<li>" + thisUser.getUsername() + " (";
 							if (Utility.isValidPhone(thisUser.getContact())) {
 								html += "Tel: " + thisUser.getContact();
 							} else if (Utility.isValidTwitter(thisUser.getContact())) {
								html += "Twitter: <a href=\"http://www.twitter.com/" + thisUser.getContact().substring(1) + "\" target=\"_blank\">" + thisUser.getContact() + "</a> (" + thisUser.getUsername() + ")";
 							} else if (Utility.isValidFacebookURL(thisUser.getContact())){
 								html += "Facebook: <a href=\"" + thisUser.getContact() + "\" target=\"_blank\">" + thisUser.getContact() + "</a>";
 							}
 							if (thisUser.getOptionalContact() != null && Utility.isValidPhone(thisUser.getOptionalContact())) {
 								html += "; tel: " + thisUser.getOptionalContact();
 							}
 							html += ")</li>";
 						}
 						html += "</ul>";
 					} else {
 						html += "There is no-one else in your group yet.</p>";
 					}
 					if (!(Utility.getMinutesUntil(thisGroup.getArrivalTime()) < EndOfJourneyChecker.MINUTES_BEFORE_ARRIVAL)) {
 						html += "<p>This page will automatically update itself every " + Utility.REFRESH_PERIOD + " seconds to allow you to see when new travellers join your group.</p>";
 					}
 				} else {
 					html = Utility.headWithTitle("DTC Summer School App") + html + "Click <a href=\"?action=register\">here</a> to join a travelling group.</p>";
 				}
 				
 			} else {
 				response.sendRedirect("https://graph.facebook.com/oauth/authorize?" +
 					    "client_id=" + CLIENT_ID + "&" +
 					    "redirect_uri=http://carsharing-sms.appspot.com/summer/mobile&" +
 					    "display=touch");
 			}
 
 			html +=	"</div></body>";
 			
 			response.getWriter().println(html);
 		} else if (request.getParameter("action") != null && request.getParameter("action").equals("register")) {
 			response.setContentType("text/html");
 			
 			String html = Utility.headWithTitle("DTC Summer School App");
 			FacebookCookie userCookie = null;
 			
 			Cookie[] cookies = request.getCookies();
 			for (int i = 0; i < cookies.length; i++) {
 				if (cookies[i].getName().equals("fbs_" + CLIENT_ID)) {
 					userCookie = new FacebookCookie(cookies[i]);
 					break;
 				}
 			}
 			
 			html += "<body><h1>Registration</h1><div class=\"bodybox\">";
 			
 			if (userCookie != null) {
 				html += "<form name=\"input\" action=\"http://carsharing-sms.appspot.com/summer/mobile\" method=\"get\">" +
 						"<input type=\"hidden\" name=\"action\" value=\"register2\" /><br />" +
 						"Username:" +
 						"<input type=\"text\" name=\"user\" size=\"20\" value=\"" + userCookie.getFirstName() + " " + userCookie.getLastName() + "\" /><br />" +
 						"Scheduled arrival time:" +
 						"<input type=\"text\" name=\"arrival\" size=\"4\" value=\"" + new SimpleDateFormat("kkmm").format(new Date(System.currentTimeMillis() + 1000*60*60)) + "\" />" +
 						"<p>Once you have registered you can see who has joined your travelling group by checking this web-site; if you want to receive important notifications during your journey by SMS to ensure that you don't miss out, enter your mobile number below.</p>" +
 						"Mobile phone number:" +
 						"<input type=\"text\" name=\"phone\" size=\"14\" /> (optional)<br />" +
 						"<input type=\"submit\" value=\"Submit\" />" +
 						"</form>";
 			}
 			
 			html +=	"</div></body>";
 			
 			response.getWriter().println(html);
 		} else if (request.getParameter("action") != null && request.getParameter("action").equals("register2")) {
 			response.setContentType("text/html");
 			
 			String html = Utility.headWithTitle("DTC Summer School App");
 			FacebookCookie userCookie = null;
 			
 			Cookie[] cookies = request.getCookies();
 			for (int i = 0; i < cookies.length; i++) {
 				if (cookies[i].getName().equals("fbs_" + CLIENT_ID)) {
 					userCookie = new FacebookCookie(cookies[i]);
 					break;
 				}
 			}
 			
 			html += "<body><h1>Registration</h1><div class=\"bodybox\">";
 			
 			if (userCookie != null) {
 				if (request.getParameter("user") != null && request.getParameter("arrival") != null) {
 					try {
						String mobile = null;
						if (request.getParameter("mobile").length() > 0) {
							mobile = request.getParameter("mobile");
 						}
						new SummerSMS(userCookie.getProfileURL(), new String[]{"summer", request.getParameter("user"), request.getParameter("arrival"), mobile});
 						html += "<p>Thanks " + userCookie.getFirstName() + ". Now click <a href=\"http://carsharing-sms.appspot.com/summer/mobile\">here</a> to watch for other travellers joining your group.</p>";
 					} catch (UserAlreadyExistsException e) {
 						html += "<p>Thanks " + userCookie.getFirstName() + ". Your details have been updated. Now click <a href=\"http://carsharing-sms.appspot.com/summer/mobile\">here</a> to watch for other travellers joining your group.</p>";
 						e.printStackTrace();
 					} catch (BadArrivalTimeException e) {
 						html += "<p>Thanks " + userCookie.getFirstName() + ", but we couldn't understand that arrival time. Click <a href=\"http://carsharing-sms.appspot.com/summer/mobile?action=register\">here</a> to try again.</p>";
 						e.printStackTrace();
 					} catch (TooManyArgumentsException e) {
 						e.printStackTrace();
 					} catch (TooFewArgumentsException e) {
 						e.printStackTrace();
 					} catch (UserNotFoundException e) {
 						e.printStackTrace();
 					}
 				}				
 			}
 			
 			html +=	"</div></body>";
 			
 			response.getWriter().println(html);
 			
 		} else if (request.getParameter("code") != null) {
 			String token = Utility.makeGetRequest("https://graph.facebook.com/oauth/access_token?" +
 			    "client_id=" + CLIENT_ID + "&" +
 			    "redirect_uri=http://carsharing-sms.appspot.com/summer/mobile&" +
 			    "client_secret=" + CLIENT_SECRET + "&" +
 			    "code=" + request.getParameter("code"));
 			try {
 				String graph = Utility.makeGetRequest("https://graph.facebook.com/me?" + token);
 				if (graph != null) {
 					JSONObject thisUser = new JSONObject(graph);
 					response.addCookie(new Cookie("fbs_" + CLIENT_ID, "access_token=" + token + "&first_name=" + thisUser.getString("first_name") + "&last_name=" + thisUser.getString("last_name") + "&profile_url=" + thisUser.getString("id")));
 					response.sendRedirect("http://carsharing-sms.appspot.com/summer/mobile");
 				}
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void doPost(HttpServletRequest request, HttpServletResponse response)
 			throws IOException {
 		doGet(request, response);
 	}
 }
