 package cms.controller.admin;
 
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 import org.slim3.controller.Controller;
 import org.slim3.controller.Navigation;
 
 public abstract class AdminController extends Controller {
 	
 	@Override
 	protected Navigation setUp() {
 		UserService userService = UserServiceFactory.getUserService();
 
 		String thisURL = request.getRequestURI();
 		if (userService.isUserLoggedIn()) {
 			requestScope("username", userService.getCurrentUser().getNickname());
 			requestScope("logoutUrl", userService.createLogoutURL(thisURL));
 		} else {
			return new Navigation(userService.createLoginURL(thisURL), false);
 		}
 
 		return null;
 	}
 }
