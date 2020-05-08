 package com.tsekhan.rssreader.web;
 
 import com.tsekhan.rssreader.services.PropertiesService;
 import java.util.Locale;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.MessageSource;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
 
 /**
  * Handles requests directed to the login page.
  *
  * @author Mikola Tsekhan <tsekhan@gmail.com>
  */
 @Controller
 @RequestMapping("/login")
 public class LoginController {
 
     @Autowired
     MessageSource messageSource;
     @Autowired
     PropertiesService propertiesService;
 
     private PageVariables getMainVariables(Locale locale) {
         PageVariables var = new PageVariables();
         var.setRoot(propertiesService.getProperty("application.root"));
         var.setTitle(messageSource.getMessage("page.login.title", null, locale));
         var.setSelfAddress("login/");
         var.setResource("login");
         var.setHiddenLeft(true);
         return var;
     }
 
     @RequestMapping
     public String renderPage(Model model, Locale locale) {
         model.addAttribute("page", getMainVariables(locale));
         model.addAttribute("loginFailed", false);
         return "login";
     }
 
     @RequestMapping(method = {RequestMethod.GET}, params = {"failed"})
     public String renderPage(@RequestParam("failed") boolean failed,
             Model model, Locale locale) {
         model.addAttribute("page", getMainVariables(locale));
         model.addAttribute("loginFailed", true);
         return "login";
     }
 }
