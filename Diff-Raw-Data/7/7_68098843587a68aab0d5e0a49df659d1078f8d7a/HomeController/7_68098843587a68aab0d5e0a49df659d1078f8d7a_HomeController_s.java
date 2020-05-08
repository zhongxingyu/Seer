 package com.rockontrol.yaogan.controller;
 
import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import com.rockontrol.yaogan.model.User;
 import com.rockontrol.yaogan.service.ISecurityManager;
 import com.rockontrol.yaogan.util.GlobalConfig;
 
 @Controller
 public class HomeController {
   private static final Logger log = Logger.getLogger(HomeController.class);
 
    @Autowired
    private ISecurityManager _secMng;
 
    @RequestMapping("/")
    public String home() {
       User user = _secMng.currentUser();
       if (user != null && user.getIsAdmin().booleanValue()) {
         log.info("user is admin");
          return "redirect:" + GlobalConfig.getProperties().getString("homepage.admin");
       } else {
         log.info("user is not admin");
          return "redirect:" + GlobalConfig.getProperties().getString("homepage.user");
       }
    }
 }
