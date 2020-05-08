 package itspring.controllers;
 
 import itspring.domain.User;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionRegistry;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 /**
  * @author Andrey Volkov
  */
 @Controller
 @RequestMapping("/profile")
 public class ProfileController {
 
     @RequestMapping(value = "", method = RequestMethod.GET)
     public String adminHome(User user, Model model) {
        model.addAttribute("user", user);
         return "user/profile";
     }
 }
