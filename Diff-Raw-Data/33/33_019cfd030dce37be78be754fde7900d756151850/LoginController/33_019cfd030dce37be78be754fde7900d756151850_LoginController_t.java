 package com.lucho.controller;
 
 import com.lucho.service.TwitterMessageListener;
 import com.lucho.service.UserService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 /**
  * Created by IntelliJ IDEA.
  * User: lucianol
  * Date: 8/31/11
  * Time: 11:31 AM
  * To change this template use File | Settings | File Templates.
  */
 @Controller
 public class LoginController {
 
     public UserService getUserService() {
         return userService;
     }
 
     @Autowired
     public void setUserService(final UserService userService) {
         this.userService = userService;
     }
 
     private UserService userService;
 
     @RequestMapping(value = "/register", method = RequestMethod.POST)
     public
     @ResponseBody
     Boolean register(final String username, final String password) {
        return this.getUserService().addUser(username, password) != null;
     }
 
     @RequestMapping(value = "/exists", method = RequestMethod.GET)
     public
     @ResponseBody
     Boolean exists(final @RequestParam(value="name") String username) {
         return this.getUserService().userExists(username);
     }
 
 }
