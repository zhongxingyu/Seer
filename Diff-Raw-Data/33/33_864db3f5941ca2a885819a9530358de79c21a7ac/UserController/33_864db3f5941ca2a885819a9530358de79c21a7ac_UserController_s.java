 package ru.dimafeng.springresttest.controller;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 import ru.dimafeng.springresttest.model.User;
 import ru.dimafeng.springresttest.service.UserService;
 
 import java.util.List;
 
 @Controller
 @RequestMapping("/api/users")
 public class UserController {
 
     @Autowired
     private UserService userService;
 
    @RequestMapping(value = "getUsers")
     @ResponseBody
     public List<User> user() {
         return userService.getUsers();
     }
 
    @RequestMapping(value = "addUser", method = RequestMethod.POST)
     @ResponseBody
     public User addUser(@RequestBody User newUser) {
         User result = userService.addUser(newUser);
         return result;
     }
 
     @ExceptionHandler(Exception.class)
     @ResponseBody
     public String handleMyException(Exception exception) {
         return exception.getMessage();
     }
 }
