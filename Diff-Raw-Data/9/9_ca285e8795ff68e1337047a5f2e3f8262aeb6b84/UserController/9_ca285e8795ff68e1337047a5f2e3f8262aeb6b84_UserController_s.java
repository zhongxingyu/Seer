 package com.springapp.mvc.controller;
 
 import com.springapp.mvc.data.FriendRepository;
 import com.springapp.mvc.data.ImageRepository;
 import com.springapp.mvc.data.TweetRepository;
 import com.springapp.mvc.model.Tweet;
 import com.springapp.mvc.model.User;
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.log4j.Logger;
 import org.codehaus.jackson.map.util.JSONPObject;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.*;
 import com.springapp.mvc.data.UserRepository;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 
 @Controller
 public class UserController {
 
     private final UserRepository userRepository;
     private final TweetRepository tweetRepository;
    private final FriendRepository friendRepository;
    private final ImageRepository imageRepository;
 
     static Logger log = Logger.getLogger(UserRepository.class);
 
     @Autowired
    public UserController(UserRepository userRepository,TweetRepository tweetRepository,
                          FriendRepository friendRepository,ImageRepository imageRepository) {
         this.userRepository = userRepository;
         this.tweetRepository = tweetRepository;
        this.friendRepository = friendRepository;
        this.imageRepository = imageRepository;
     }
 
     @RequestMapping("MiniTwitter/API/users/{id}")
     @ResponseBody
     public HashMap fetchUser(@PathVariable("id") String userName) {
         HashMap userDetails = new HashMap();
         System.out.println("Fetching User Details for: " + userName);
         userDetails.put("tweets", tweetRepository.fetchTweets(userName));
         userDetails.put("info",userRepository.fetchUser(userName));
         return userDetails;
     }
 
     @RequestMapping(value = "MiniTwitter/API/users", method = RequestMethod.POST)
     @ResponseBody
     public void add(@RequestBody Map<String, String> user) {
         System.out.println("Creating new user: " + user.get("username") + " " + user.get("password"));
         if ( userRepository.isUserPresent(user.get("username")) ) {
             return;
         }
         userRepository.addUser(user.get("username"), encodePassword(user.get("password")), user.get("name"), user.get("email"));
     }
 
     @RequestMapping(value = "MiniTwitter/API/account/settings", method = RequestMethod.POST)
     @ResponseBody
     public void modifyUser(@RequestBody Map<String, String> values,HttpServletRequest httpServletRequest) {
         String userName = httpServletRequest.getAttribute("currentUser").toString();
         System.out.println("for user: " + userName + " Modifying value: " + values.get("name"));
         userRepository.modifyUser(userName, values.get("name"), values.get("email"), values.get("Password"));
     }
 
     @RequestMapping(value = "MiniTwitter/API/users/search", method = RequestMethod.GET)
     @ResponseBody
     public List<User> add(@RequestParam("query") String query) {
         log.info("Searching for user "+query);
         return userRepository.searchForUsers(query);
     }
 
     public String encodePassword(String password) {
         return DigestUtils.sha256Hex(password);
     }
 
 }
