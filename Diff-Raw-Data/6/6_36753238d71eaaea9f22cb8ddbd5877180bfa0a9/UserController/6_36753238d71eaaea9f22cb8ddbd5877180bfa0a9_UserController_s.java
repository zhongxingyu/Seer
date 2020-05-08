 package com.jglitter.web;
 
 import com.jglitter.domain.User;
 import com.jglitter.domain.Users;
 import com.jglitter.services.UserService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 @Controller
 public class UserController {
 
     @Autowired
     private UserService userService;
 
     @RequestMapping(value = "/user", method = RequestMethod.POST)
     public User createUser(@RequestBody User user) {
         return userService.createUser(user);
     }
 
     @RequestMapping(value = "/user", method = RequestMethod.GET)
     public Users getUsers() {
         return userService.findAllUsers();
     }
 
     @RequestMapping(value = "/followers/{followerId}/{userToFollowId}", method = RequestMethod.POST)
     public @ResponseBody
     void addUserToFollow(@PathVariable String followerId, @PathVariable String userToFollowId) {
 
     }
 
     @RequestMapping(value = "/followees/{followerId}", method = RequestMethod.GET)
     public Users getFollowees(@PathVariable String followerId) {
        return new Users();
     }
 
 
 }
