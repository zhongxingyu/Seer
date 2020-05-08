 package ch.hszt.mdp.web;
 
 import java.sql.Time;
 import java.util.Date;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 @Controller
 @RequestMapping("/welcome")
 public class WelcomeController {
 	@RequestMapping(method = RequestMethod.GET)
 	public String welcome(Model model) {
 		
 		//how to get data into the jsp
		Time today = new Time(System.currentTimeMillis());
 		model.addAttribute("today", today);
 		
 		return "welcome";
 	}
 }
