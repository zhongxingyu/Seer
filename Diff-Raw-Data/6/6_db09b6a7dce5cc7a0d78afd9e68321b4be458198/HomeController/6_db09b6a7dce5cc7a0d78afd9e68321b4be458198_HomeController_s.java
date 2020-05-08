 /*
  * Copyright 2013 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.swadeshi.controllers;
 
 import java.security.Principal;
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.servlet.http.HttpServletRequest;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.context.SecurityContext;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.social.connect.ConnectionSignUp;
 import org.springframework.social.google.api.Google;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.servlet.ModelAndView;
 import org.swadeshi.entities.Appreciation;
 import org.swadeshi.entities.User;
 import org.swadeshi.entities.UserConnection;
 import org.swadeshi.exceptions.CustomException;
 import org.swadeshi.services.AppreciationService;
 import org.swadeshi.services.UserService;
 
 @Controller
 public class HomeController {
 	
 	private Google google;
 	
 	@Autowired
 	private AppreciationService appreciationService;
 
 	@Autowired
 	private UserService userService;
 	
 	@Inject
 	public HomeController(Google google){
 		this.google = google;
 	}
 	
 	
 	@RequestMapping("/home")
	public ModelAndView test(Principal currentUser){
 		
 		String userName="Guest";
 		if (google.isAuthorized())
 				userName = google.userOperations().getUserProfile().getName();
 		ModelAndView modelAndView =new ModelAndView("home");
 		
 		try {
 			List<Appreciation> appreciations = appreciationService.fetchAllAppreciations();
 			List<User> users = userService.fetchAllUsers();
 			
 			modelAndView.addObject("appreciations", appreciations);
 			modelAndView.addObject("users", users);
 			
 		} catch (CustomException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		modelAndView.addObject("username", userName);
 		return modelAndView;
 		
 	}
 
 	@RequestMapping("/signin")
 	public String home(Principal currentUser, Model model, HttpServletRequest request) {
 		String errorValue = request.getParameter("error");
 		return "index";
 	}
 	
 	
 }
