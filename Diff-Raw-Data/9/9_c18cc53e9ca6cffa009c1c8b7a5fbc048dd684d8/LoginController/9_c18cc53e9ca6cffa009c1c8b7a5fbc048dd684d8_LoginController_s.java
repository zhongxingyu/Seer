 package cn.edu.sjtu.se.dclab.haiercloud.web.controller;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.shiro.SecurityUtils;
 import org.apache.shiro.authc.AuthenticationException;
 import org.apache.shiro.authc.UsernamePasswordToken;
 import org.apache.shiro.subject.Subject;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import cn.edu.sjtu.se.dclab.haiercloud.web.auth.CommonVariableModel;
 import cn.edu.sjtu.se.dclab.haiercloud.web.entity.User;
 import cn.edu.sjtu.se.dclab.haiercloud.web.service.IAuthService;
 
 @Controller
 public class LoginController {
 
 	@Resource(name = "authService")
 	IAuthService authService;
 
 	@RequestMapping(value = "/logout", method = RequestMethod.GET)
 	public String logout() {
 
 		Subject user = SecurityUtils.getSubject();
 		user.logout();
 
 		return "redirect:/login";
 	}
 
 	@RequestMapping(value = "/unauthorized", method = RequestMethod.GET)
 	public ModelAndView unauthorizedPage() {
 		// set view
 		ModelAndView mav = new ModelAndView();
 		mav.setViewName("/unauthorized");
 
 		return mav;
 	}
 
 	@RequestMapping(value = "/login", method = RequestMethod.GET)
 	public ModelAndView getLoginPage() {
 		// set view
 		ModelAndView mav = new ModelAndView();
 		mav.setViewName("/login");
 
 		return mav;
 	}
 
 	@RequestMapping(value = "/register", method = RequestMethod.GET)
 	public ModelAndView registerPage() {
 		// set view
 		ModelAndView mav = new ModelAndView();
 		mav.setViewName("/register");
 
 		return mav;
 	}
 
 	@RequestMapping(value = "/register/submit", method = RequestMethod.POST)
 	public String register(@RequestParam String username,
 			@RequestParam String password, @RequestParam String email) {
 
 		User user = new User();
 		user.setEmail(email);
 		user.setUsername(username);
 		user.setPassword(password);
 
 		if (authService.register(user)) {
 			return "redirect:/";
 		} else {
 			return "/register";
 		}
 	}
 
 	@RequestMapping(value="/login/submit", method = RequestMethod.POST)
     public String login(HttpServletRequest request, @RequestParam String username, @RequestParam String password) {
     	
     	Subject user = SecurityUtils.getSubject();
     	UsernamePasswordToken token = new UsernamePasswordToken(username, password);
     	
     	try {
     		user.login(token);
    		request.getSession().setAttribute("user", user);
     		return "redirect:/home";
     	} catch (AuthenticationException ae) {
     		System.out.println("登陆信息错误：" + ae);
     		token.clear();
     		return "redirect:/login";
     	}
     }
 }
