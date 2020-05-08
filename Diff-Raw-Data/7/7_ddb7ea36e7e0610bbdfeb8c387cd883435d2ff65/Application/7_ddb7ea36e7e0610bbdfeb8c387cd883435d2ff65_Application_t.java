 package controllers;
 
 import models.User;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import play.i18n.Lang;
 import play.mvc.Before;
 import play.mvc.Controller;
 import play.mvc.With;
 
 import play.test.Fixtures;
 
 import java.util.HashMap;
 import java.util.Map;
 
 @With(Secure.class)
 public class Application extends Controller {
 
 	private static final String ADMIN_REDIRECT_VIEW = "/objects";
 	private static final String USER_REDIRECT_VIEW = "/patientView/list";
 	private static Logger logger = LoggerFactory.getLogger(Application.class);
 	
 	@Before
 	static void storeSession() {
         if(Security.isConnected()) {
             User user = User.find("byLogin", Security.connected()).first();
             session.put("user", user);
             session.put("isUserRole", user.isUserRole());
         }
 	}
 
     public static void index() {
     	User loggedUser = User.find("byLogin", Security.connected()).first();
     	boolean isAdmin = loggedUser.isAdminRole();
     	logger.info("loggedUser:" + loggedUser + "; isAdmin: " + isAdmin);
        if (isAdmin) {
           redirect(ADMIN_REDIRECT_VIEW); 
        } else {
            PatientView.patients();
        }
     }
     
     public static void changeLanguage(String lang, String currentLocation){
     	String currentLang = Lang.get();
     	if(!lang.equals(currentLang)) {
     		Lang.change(lang);
     	}
     	redirect(currentLocation);
     }
     
 }
