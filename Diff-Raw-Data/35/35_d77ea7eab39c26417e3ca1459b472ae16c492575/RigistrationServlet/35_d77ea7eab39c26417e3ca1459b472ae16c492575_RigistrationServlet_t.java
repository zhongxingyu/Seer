 package com.algo.webshop.client.controller;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.algo.webshop.client.authentication.Response;
 
 @Controller
 public class RigistrationServlet {
 
 	@RequestMapping("/signin")
 	public String signIn() {
 		return "signin";
 	}
 	
	@RequestMapping(value = "/inputLog", method = RequestMethod.GET)
 	public @ResponseBody Response respInputLog(@RequestParam String log) {
 		Response result = new Response();
 		result.setLogin("Teyur");
 		result.setPass("hjkfsdhk");
 		
 		return result;
 	}
 
 	@RequestMapping("/forgotpass")
 	public String forgotPass() {
 		return "forgotpass";
 	}
 }
