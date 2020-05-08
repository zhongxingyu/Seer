 package com.rtnotifier.controller;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 @Controller
@RequestMapping("/api")
 public class IndexController {
 
	@RequestMapping(method = RequestMethod.GET)
 
 	public String printWelcome(ModelMap model) {
 		model.addAttribute("message", "RT notifier!");
 		return "index";
 	}
 }
