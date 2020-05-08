 package net.iteach.web.security;
 
 import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 @Controller
 public class LoginController {
 
 	@RequestMapping("/login")
 	public String login() {
 		return "login";
 	}
 	
	@RequestMapping("/login_error")
	public String login_error (Model model) {
		model.addAttribute("error", Boolean.TRUE);
		return "login";
	}
	
 }
