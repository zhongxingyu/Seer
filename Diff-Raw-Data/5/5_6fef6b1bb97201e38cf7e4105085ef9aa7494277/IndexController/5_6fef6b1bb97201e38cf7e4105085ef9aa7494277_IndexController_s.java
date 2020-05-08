 package com.shiluoren.controller;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 @Controller
@RequestMapping(value = "index")
 public class IndexController {
 
 	@RequestMapping(method = RequestMethod.GET)
 	public String index(Model model, HttpServletRequest request) {
		return "/index/index";
 	}
 }
