 package de.enwida.web.controller;
 
 import java.security.Principal;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.mail.MessagingException;
 import javax.mail.internet.AddressException;
 import javax.servlet.http.HttpServletRequest;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.datasource.DriverManagerDataSource;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import de.enwida.web.dao.implementation.UserDao;
 import de.enwida.web.model.User;
 import de.enwida.web.service.implementation.Mail;
 import de.enwida.web.service.interfaces.UserService;
 
 /**
  * Handles requests for the user service.
  */
 @Controller
 @RequestMapping("/user")
 public class UserController {
 	
 	@Autowired
 	private UserService userService;
 	
 	@Autowired
 	private DriverManagerDataSource datasource;
 
 	@Autowired
 	UserDao userDao;
 	
 	@RequestMapping(value="/user", method = RequestMethod.GET)
 	public String displayDashboard(Model model, Locale locale) {
 		
 		User u = userService.getUser(new Long(0));
 		model.addAttribute("user", u);
 		
 		return "user";
 	}
 
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public String home(ModelMap model, Principal principal) {
 		String name,userStatus,userStatusURL;
 		
 		if(principal!=null){
 			name = principal.getName();
 			userStatus="logout";
 			userStatusURL="../j_spring_security_logout";
 		}else{
 			name="anonymous";
 			userStatusURL=userStatus="login";
 		}
 		model.addAttribute("username", name);
 		model.addAttribute("userStatus", userStatus);
 		model.addAttribute("userStatusURL", userStatusURL);
 		return "user/index";
 	}
 	
 	@RequestMapping(value = "/test", method = RequestMethod.GET)
 	public String test(ModelMap model, Principal principal) {
 		String name,userStatus,userStatusURL;
 		
 		if(principal!=null){
 			name = principal.getName();
 			userStatus="logout";
 			userStatusURL="../j_spring_security_logout";
 		}else{
 			name="anonymous";
 			userStatusURL=userStatus="login";
 		}
 		model.addAttribute("username", name);
 		model.addAttribute("userStatus", userStatus);
 		model.addAttribute("userStatusURL", userStatusURL);
 		return "user/test";
 	}
 	
 	@RequestMapping(value="/login", method = RequestMethod.GET)
 	public String login(ModelMap model) {
 		return "user/login";
 	}
 	
 	@RequestMapping(value="/logout", method = RequestMethod.GET)
 	public String logout(ModelMap model) {
 		return "logout";
 	}
 	
 	
 	@RequestMapping(value="/download", method = RequestMethod.GET)
 	public String download(ModelMap model) {
 		return "user/download";
 	}
 	
 	
 	@RequestMapping(value="/register",method=RequestMethod.POST)
 	public String processForm(@ModelAttribute(value="USER") User user,BindingResult result){
 	    if(result.hasErrors()){
 	        return "user/register";
 	    }else{
 	        System.out.println("User values is : " + user);
 	        return "user/register";
 	    }
 	}
 	
 	@RequestMapping(value="/forgotPassword",method=RequestMethod.GET)
     public String showForgotPassForm(ModelMap model){
 		return "user/forgotPassword";
     }
 	
 	@RequestMapping(value="/forgotPassword",method=RequestMethod.POST)
 	public String forgotPassword(ModelMap model,String email){
 		String password=userDao.getPassword(email);
 		if(password==null){
 			model.addAttribute("error", "User is not found");
 		}else{
 			try {
 				Mail.SendEmail(email,"Your Password:",password);
 			} catch (Exception e) {
 				model.addAttribute("error", "Mailling Error");
 			}
 		}
 		return "user/login";
 	}
 	
 	@RequestMapping(value="/admin", method = RequestMethod.GET)
 	public String manageUsers(ModelMap model) {
 		
 		List<User> users= userDao.findAllUsersWithPermissions();
 
 		model.addAttribute("users", users);
 		return "user/admin";
 	}
 	
 	@RequestMapping(value="/updateRole", method = RequestMethod.GET)
 	@ResponseBody
 	public String updateRole(HttpServletRequest request) {
 		String state="";
 		int userID=0;
 		int roleID=0;
 		Map pMap=request.getParameterMap();
 		if (pMap.containsKey("state")){
 			state=((String[]) pMap.get("state"))[0];
 		}
 		if (pMap.containsKey("userID")){
 			userID=Integer.parseInt(((String[]) pMap.get("userID"))[0]);
 		}
 		if (pMap.containsKey("roleID")){
 			roleID=Integer.parseInt(((String[]) pMap.get("roleID"))[0]);
 		}
 		
 		if (state.equals("true")){
 			userDao.addPermission(userID,roleID);
 		}
 		else{
 			userDao.removePermission(userID,roleID);
 		}
 		return "ok";
 	}
 }
