 package com.test.controller;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 @Controller
 public class MainController {
 
     @RequestMapping(value = "/app/", method = RequestMethod.GET)
     public String showMainPage(ModelMap model) {
         // params for JSP
         model.addAttribute("message", "Hello");
         return "main"; // name of JSP
     }
 
 }
