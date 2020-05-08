 package controllers;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 
 import models.User;
 import play.Play;
 import play.libs.OpenID;
 import play.libs.OpenID.UserInfo;
 import play.mvc.Controller;
 import play.mvc.Router;
 import play.mvc.Http.Request;
 
 public class Auth extends Controller{
 	public static final String KEY_TIMESTAMP = "timestamp";
 	public static final String KEY_EMAIL = "email";
 	public static void logout() {
 		session.clear();
 		CloudPaster.index();
 	}
 	public static void yahoologin() throws UnsupportedEncodingException {
		String returnto = Router.getFullUrl("Auth.login");
 		if(flash.contains("return.to"))
 			returnto+="?return="+URLEncoder.encode(flash.get("return.to"), "utf8");
 		if (!OpenID.id("https://me.yahoo.com").required(KEY_EMAIL, "http://axschema.org/contact/email")
 				.returnTo(returnto)
 				.forRealm(Play.configuration.getProperty("auth.returnto", "Auth.login")).verify()) {
 			flash.error("无法连接Yahoo服务器");
 			login();
 		}
 	}
 	public static void googlelogin() throws UnsupportedEncodingException {
		String returnto = Router.getFullUrl("Auth.login");
 		if(flash.contains("return.to"))
 			returnto+="?return="+URLEncoder.encode(flash.get("return.to"), "utf8");
 		if (!OpenID.id("https://www.google.com/accounts/o8/id").required(KEY_EMAIL, "http://axschema.org/contact/email")
 				.returnTo(returnto)
 				.forRealm(Play.configuration.getProperty("auth.returnto", "Auth.login")).verify()) {
 			flash.error("无法连接Google服务器");
 			flash.keep();
 			login();
 		}
 	}
 	public static void login() {
 		flash.keep("return.to");
 		if(OpenID.isAuthenticationResponse()) {
 	        // Retrieve the verified id
 	        UserInfo userinfo = OpenID.getVerifiedID();
 	        if(userinfo == null) {
 	              flash.error("不妙，登陆错误。");
 	              login();
 	        } else {
               String email = userinfo.extensions.get(KEY_EMAIL);
               User user = User.createOrGet(email);
               session.put(KEY_USER, user.id);
               session.put(KEY_TIMESTAMP, System.currentTimeMillis());
               if(params._contains("return")) {
             	  redirect(params.get("return"));
               }
               CloudPaster.activity();
 	        }
 		}
 		render();
 	}
 	static final String KEY_USER = "user.key";
 	public static User getLoginUser() {
 		String userid = session.get(KEY_USER);
 		if(userid == null)
 			return null;
 		User user = User.findById(Long.parseLong(userid));
 		return user;
 	}
 
 }
