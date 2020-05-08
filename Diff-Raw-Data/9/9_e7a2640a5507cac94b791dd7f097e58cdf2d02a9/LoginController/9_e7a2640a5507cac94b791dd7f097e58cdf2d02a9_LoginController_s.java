 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.exadel.borsch.web.controllers;
 
import java.security.Principal;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 /**
  *
  * @author Vlad
  */
 @Controller
 public class LoginController {
     @RequestMapping("/login")
     public String processPageRequest(HttpServletRequest req, HttpServletResponse res) {
         return ViewURLs.LOGIN_PAGE;
     }
     @RequestMapping("/login/err")
     public String error(ModelMap model) {
         model.addAttribute("error", true);
         return ViewURLs.LOGIN_PAGE;
     }
    @RequestMapping("/")
    public String welcome(ModelMap model, Principal principal) {
        String username = principal.getName();
        System.out.println(username);
        model.addAttribute("name", username); // get user from db...
        return ViewURLs.HOME_PAGE;
    }
 }
