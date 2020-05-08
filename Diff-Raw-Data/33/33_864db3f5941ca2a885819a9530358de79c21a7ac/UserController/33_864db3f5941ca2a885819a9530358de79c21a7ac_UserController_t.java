 package ru.dimafeng.springresttest.controller;
 
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
 
    @RequestMapping(value = "/", method = RequestMethod.GET)
     @ResponseBody
     public List<User> user() {
         return userService.getUsers();
     }
 
    @RequestMapping(value = "/", method = RequestMethod.POST)
     @ResponseBody
     public User addUser(@RequestBody User newUser) {
         User result = userService.addUser(newUser);
         return result;
     }
 
    @RequestMapping(value = "/{userId}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void addUser(@PathVariable(value = "userId") long userId) {
        userService.removeUser(userId);
    }

     @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
     @ResponseBody
     public String handleMyException(Exception exception) {
         return exception.getMessage();
     }
 }
