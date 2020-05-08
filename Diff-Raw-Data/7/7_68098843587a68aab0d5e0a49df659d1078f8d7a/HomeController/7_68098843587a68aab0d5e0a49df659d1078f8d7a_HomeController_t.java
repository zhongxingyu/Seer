 package com.rockontrol.yaogan.controller;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import com.rockontrol.yaogan.model.User;
 import com.rockontrol.yaogan.service.ISecurityManager;
 import com.rockontrol.yaogan.util.GlobalConfig;
 
 @Controller
 public class HomeController {
 
    @Autowired
    private ISecurityManager _secMng;
 
    @RequestMapping("/")
    public String home() {
       User user = _secMng.currentUser();
       if (user != null && user.getIsAdmin().booleanValue()) {
          return "redirect:" + GlobalConfig.getProperties().getString("homepage.admin");
       } else {
          return "redirect:" + GlobalConfig.getProperties().getString("homepage.user");
       }
    }
 }
