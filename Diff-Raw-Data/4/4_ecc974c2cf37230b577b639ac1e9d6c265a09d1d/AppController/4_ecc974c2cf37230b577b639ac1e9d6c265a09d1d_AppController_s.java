 package app.web.SpringWebApp;
 
 import java.util.Date;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import app.web.SpringWebApp.user.User;
 import app.web.SpringWebApp.user.UserDAO;
 
 @Controller
 public class AppController extends AbstractController
 {
 	@Autowired
 	private UserDAO userDAO;
 
 	@RequestMapping(value = "/login")
 	public String login()
 	{
 		return "app/login";
 	}
 
 	@RequestMapping(value = "/loginError")
 	public String loginError(ModelMap model)
 	{
 		model.addAttribute("error", "true");
 		return "app/login";
 	}
 
 	@RequestMapping(value = "user/{userId}", method = RequestMethod.POST)
 	public String userUpdate(@PathVariable String userId, Model model,
 			HttpServletRequest request)
 	{
 		User userLoggedIn = AppHelper.getUserLoggedIn();
 
 		if (userLoggedIn != null
 				&& userLoggedIn.getId().equals(new Integer(userId)))
 		{
 			User user = AppHelper.getUserById(userId);
 			bindUser(request, user);
 
 			userDAO.update(user);
 
 			AppHelper.setUserLoggedIn(user);
 
 			model.addAttribute("saved", true);
 		}
 		return "app/user/userHome";
 	}
 
 	@RequestMapping(value = "user/")
 	public String userHome(Model model)
 	{
 		return "app/user/userHome";
 	}
 
 	@RequestMapping(value = "admin/")
 	public String adminHome(Model model)
 	{
 		return "app/admin/adminHome";
 	}
 
 	@RequestMapping(value = "admin/users")
 	public String adminUsrMgt(Model model)
 	{
 		model.addAttribute("users", userDAO.getAll());
 		return "app/admin/adminUsrMgt";
 	}
 
 	@RequestMapping(value = "admin/users/{userId}", method = RequestMethod.GET)
 	public String adminUsrLoad(@PathVariable String userId, Model model)
 	{
 		User user = userDAO.getById(userId);
 		model.addAttribute("user", user);
		return "app/admin/ajaxUserDetails";
 	}
 
 	@RequestMapping(value = "admin/users/{userId}", method = RequestMethod.POST)
 	public String adminUsrSave(@PathVariable String userId, Model model,
 			HttpServletRequest request)
 	{
 		User user = userDAO.getById(userId);
 
 		bindUser(request, user);
 		user.setAccountNonExpired(request.getParameter("accountNonExpired") == null);
 		user.setAccountNonLocked(request.getParameter("accountNonLocked") == null);
 		user.setCredentialsNonExpired(request
 				.getParameter("credentialsNonExpired") == null);
 		user.setEnabled(request.getParameter("enabled") != null);
 
 		userDAO.update(user);
 
 		System.out.println(user);
 		model.addAttribute("userUpdated", true);
 		return adminUsrMgt(model);
 	}
 
 	@RequestMapping(value = "/registerUser", method = RequestMethod.GET)
 	public String registerUserView()
 	{
 		return "app/registerUser";
 	}
 
 	@RequestMapping(value = "/testRedirect")
 	public String testRedirect()
 	{
 		return "redirect:/app/user/";
 	}
 
 	@RequestMapping(value = "/registerUser", method = RequestMethod.POST)
 	public String registerUserSave(HttpServletRequest request)
 	{
 		User user = new User();
 		user.setCreateDate(new Date());
 		bindUser(request, user);
 
 		userDAO.create(user);
 
 		user.getRoles().add(AppHelper.getRole(AppHelper.ROLE_USER));
 
 		userDAO.update(user);
 
 		AppHelper.setUserLoggedIn(user);
 
 		return "redirect:/app/user/";
 	}
 
 	@RequestMapping(value = "/checkUsername", method = RequestMethod.GET)
 	@ResponseBody
 	public String checkUsername(@RequestParam("username") String username)
 	{
 		boolean ok = false;
 
 		if (username != null)
 		{
 			User userLoggedIn = AppHelper.getUserLoggedIn();
 
 			if (userLoggedIn == null)
 			{
 				ok = AppHelper.getUserByUsername(username) == null;
 			}
 			else
 			{
 				ok = getCurrentSession()
 						.createQuery(
 								"from User u where u.id != ? and u.username=?")
 						.setParameter(0, userLoggedIn.getId())
 						.setParameter(1, username).list().size() == 0;
 			}
 		}
 
 		System.out.println("checkUsername: " + username
 				+ (ok ? " [ok]" : " [not ok]"));
 
 		return String.valueOf(ok);
 	}
 
 	@RequestMapping(value = "/checkUsername/{userId}", method = RequestMethod.GET)
 	@ResponseBody
 	public String checkUsername(@PathVariable String userId,
 			@RequestParam("username") String username)
 	{
 		boolean ok = false;
 
 		if (username != null)
 		{
 			User user = userDAO.getById(userId);
 
 			if (user == null)
 			{
 				ok = AppHelper.getUserByUsername(username) == null;
 			}
 			else
 			{
 				ok = getCurrentSession()
 						.createQuery(
 								"from User u where u.id != ? and u.username=?")
 						.setParameter(0, user.getId())
 						.setParameter(1, username).list().size() == 0;
 			}
 		}
 
 		System.out.println("checkUsername: " + username
 				+ (ok ? " [ok]" : " [not ok]"));
 
 		return String.valueOf(ok);
 	}
 
 	@RequestMapping(value = "/sessionExpired")
 	public String sessionExpired()
 	{
 		System.out.println("session expired!");
 		return "redirect:/app/login";
 	}
 
 	private void bindUser(HttpServletRequest request, User user)
 	{
 		user.setFirstName(request.getParameter("firstName"));
 		user.setLastName(request.getParameter("lastName"));
 		user.setEmail(request.getParameter("email"));
 		user.setUsername(request.getParameter("username"));
 
 		String password = request.getParameter("password");
 		if (!password.equals(request.getParameter("confirmPassword")))
 		{
 			throw new IllegalArgumentException("password mismatch!");
 		}
 		user.setPassword(password);
 	}
 
 }
