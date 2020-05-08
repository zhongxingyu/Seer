 package com.algo.webshop.client.controller;
 
import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.algo.webshop.client.authentication.Response;
import com.algo.webshop.client.authentication.SignupForm;
import com.algo.webshop.client.authentication.SignupValidator;
 
 @Controller
 public class RigistrationServlet {
 
 	@RequestMapping("/signin")
 	public String signIn() {
 		return "signin";
 	}
 	
	@RequestMapping(value = "/inputLog", method = RequestMethod.POST)
 	public @ResponseBody Response respInputLog(@RequestParam String log) {
 		Response result = new Response();
 		result.setLogin("Teyur");
 		result.setPass("hjkfsdhk");
 		
 		return result;
 	}
	
	@RequestMapping("/signinDesign")
	public String signInDesign() {
		return "return text";
	}

	@Autowired
	private SignupValidator signupValidator;

	@RequestMapping(value = { "/signup" }, method = RequestMethod.GET)
	public String signup(ModelMap model) {
		SignupForm signupForm = new SignupForm();
		model.put("signupForm", signupForm);
		return "signup";
	}

	@RequestMapping(value = { "/signup" }, method = RequestMethod.POST)
	public String processSignup(SignupForm signupForm, BindingResult result) {
		signupValidator.validate(signupForm, result);
		if (result.hasErrors()) {
			return "signup";
		}
		return "signup-success";
	}
 
 	@RequestMapping("/forgotpass")
 	public String forgotPass() {
 		return "forgotpass";
 	}
 }
