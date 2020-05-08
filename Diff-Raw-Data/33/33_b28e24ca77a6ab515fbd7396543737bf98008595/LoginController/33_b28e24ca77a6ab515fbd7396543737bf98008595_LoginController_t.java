 package com.poliformas.warehouse.web;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import javax.validation.Valid;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import com.poliformas.warehouse.web.forms.LoginForm;
 
 @Controller
 public class LoginController {
 
 	@RequestMapping(value = "/login", method = RequestMethod.GET)
 	public String login(ModelMap model) {
 		System.out.println("------------------------algo--");
		model.addAttribute("loginForm",new LoginForm());
 
 		return "/login";
 	}
 
 	@RequestMapping(value = "/login", method = RequestMethod.POST)
 	public String create(@Valid LoginForm loginForm, BindingResult result,
 			HttpServletRequest request) {
 		if (result.hasErrors()) {
 			System.out.println("---------------------+++++++");
 
 			return "/login";
 		}
 		System.out.println("la bombaaaaa");
 		return "forward:j_spring_security_check";
 	}
 
 	@RequestMapping(value = "/denied", method = RequestMethod.GET)
 	public String loginedrror(ModelMap model) {
 		System.out.println("----------------que mamada--------");
 		model.addAttribute("error", "true");
 		return "denied";
 
 	}
 
 	@RequestMapping(value = "/loginfailed", method = RequestMethod.GET)
 	public String loginerror(ModelMap model) {
 
 		model.addAttribute("error", "true");
 		return "login";
 
 	}
 
 	@RequestMapping(value = "/logout", method = RequestMethod.GET)
 	public String logout(ModelMap model) {
 
 		return "login";
 
 	}
 	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String inicio(ModelMap model) {
		System.out.println("------------------------algo--");
		model.addAttribute("loginForm",new LoginForm());

		return "/login";
	}
	@RequestMapping(value = "/", method = RequestMethod.POST)
	public String inicio(@Valid LoginForm loginForm, BindingResult result,
			HttpServletRequest request) {
		if (result.hasErrors()) {
			System.out.println("---------------------+++++++");

			return "/login";
		}
		System.out.println("la bombaaaaa");
		return "forward:j_spring_security_check";
 	}
 
 


 }
