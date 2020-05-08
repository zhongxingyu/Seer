 package com.app.web;
 
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 /**
  * Handles requests for the application home page.
  */
 @Controller
 public class HomeController {
 	
 	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
 	
 	/**
 	 * Simply selects the home view to render by returning its name.
 	 */
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public String home(Locale locale, Model model) {
 		logger.info("Welcome home! The client locale is {}.", locale);
 		
 		Date date = new Date();
 		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
 		
 		String formattedDate = dateFormat.format(date);
 		
 		model.addAttribute("serverTime", formattedDate );
 		
 		return "home";
 	}
 	
 	@RequestMapping(value = "/login", method = RequestMethod.GET)
 	public String login(Model model) {
 		
 		return "login";
 	}
 	
 	@RequestMapping(value="/loginfailed", method = RequestMethod.GET)
 	public String loginerror(Model model) {
  
 		model.addAttribute("error", "true");
 		return "login";
 	}
  
 	@RequestMapping(value="/logout", method = RequestMethod.GET)
 	public String logout(Model model) {
  
 		return "login";
 	}
 	
 	@RequestMapping(value="/register", method = RequestMethod.GET)
 	public String register(Model model) {
  
 		return "register";
 	}
	
	@RequestMapping(value="/error", method = RequestMethod.GET)
	public String error(Model model) {
		return "error";
	}
	
 }
