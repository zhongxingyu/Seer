 package mum.compro.onlineapp;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.bind.support.SessionStatus;
 import org.springframework.web.servlet.ModelAndView;
 
 @Controller
 /* @SessionAttributes("user") */
 public class LoginLogoutController {
 	@Resource
 	private RegistrationService registrationService;
 
 	@RequestMapping(value = "/login", method = RequestMethod.GET)
 	public String login() {
 		return "login";
 	}
 
 	@RequestMapping(value = "/logout", method = RequestMethod.GET)
 	public String logout(HttpSession session) {
 		session.removeAttribute("user");
 		return "redirect:/";
 	}
 
 	@RequestMapping(value = "/login", method = RequestMethod.POST)
 	public String login(String email, String password, HttpSession session) {
 		User user = registrationService.getUserbyEmail(email);
 		int checkValidity = registrationService.checkUser(email, password);
 		if (checkValidity == 1 && user.getUserType() != null
 				&& user.getUserType().equals(UserType.Administrator)) {
 			ModelAndView modelAndView = new ModelAndView();
 			modelAndView.addObject("user", email);
 			session.setAttribute("user", user);
 			return "redirect:dashboard";
 		} else if (checkValidity == 1 && user.getUserType() != null
 				&& user.getUserType().equals(UserType.AdmissionStaff)) {
 			// session.setAttribute("user", user);
 			ModelAndView modelAndView = new ModelAndView();
 			modelAndView.addObject("user", email);
 			session.setAttribute("user", user);
 			return "redirect:dashboard";
 		} else if (checkValidity == 1) {
 			ModelAndView modelAndView = new ModelAndView();
 			modelAndView.addObject("user", email);
 			session.setAttribute("user", user);
 			return "redirect:application";
 		} else
 			return "redirect:/loginfail";
 	}
 }
