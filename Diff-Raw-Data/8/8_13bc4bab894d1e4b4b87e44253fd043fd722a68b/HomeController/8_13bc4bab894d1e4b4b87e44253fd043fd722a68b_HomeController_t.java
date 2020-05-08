 package com.web.controller;
 
 import java.util.Map;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 @Controller
 public class HomeController {
	@RequestMapping(value = { "/", "/home", "/index" }, method = RequestMethod.GET)
 	public String showHomePage(Map<String, Object> model) {
 		model.put("message", "Home page");
 		return "home";
 	}

	@RequestMapping(value = "/staticPage", method = RequestMethod.GET)
	public String redirect() {
		return "redirect:/pages/final.htm";
	}
 }
