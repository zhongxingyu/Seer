 package it.sevenbits.space.controller;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 /**
  * Controller for main page (index.html).
  */
 @Controller
 public class WelcomeController {
 
    @RequestMapping(value = "/index.html", method = RequestMethod.GET)
    public String index(Model model) {
         model.addAttribute("message", "Hello Spring MVC Framework!");
        return "WEB-INF/jsp/index.jsp";
     }
 }
