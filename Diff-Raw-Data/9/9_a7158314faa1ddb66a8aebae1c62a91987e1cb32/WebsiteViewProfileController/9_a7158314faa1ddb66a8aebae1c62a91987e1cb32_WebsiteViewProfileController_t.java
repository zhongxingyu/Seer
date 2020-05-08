 package com.springapp.mvc.controller;
 
 import com.springapp.mvc.data.FriendRepository;
 import com.springapp.mvc.data.ImageRepository;
 import com.springapp.mvc.data.TweetRepository;
 import com.springapp.mvc.data.UserRepository;
 import com.springapp.mvc.data.ValidationChecks;
 import com.springapp.mvc.model.Tweet;
 import com.springapp.mvc.model.User;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.multipart.MultipartFile;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.util.HtmlUtils;
 import org.springframework.web.util.WebUtils;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.util.List;
 import java.util.Map;
 
 @Controller
 public class WebsiteViewProfileController {
 
     private final UserRepository userRepository;
     private final TweetRepository tweetRepository;
     private final FriendRepository friendRepository;
     private final ImageRepository imageRepository;
     private final ValidationChecks validationChecks;
 
     static Logger log = Logger.getLogger(UserRepository.class);
 
     @Autowired
     public WebsiteViewProfileController(UserRepository userRepository, TweetRepository tweetRepository, FriendRepository friendRepository, ImageRepository imageRepository, ValidationChecks validationChecks) {
         this.userRepository = userRepository;
         this.tweetRepository = tweetRepository;
         this.friendRepository = friendRepository;
         this.imageRepository = imageRepository;
         this.validationChecks = validationChecks;
     }
 
     @RequestMapping(value = "MiniTwitter/Website", method= RequestMethod.GET)
     @ResponseBody
     public ModelAndView getHomePage(HttpServletRequest httpServletRequest) {
         ModelAndView modelAndView = new ModelAndView("home");
         modelAndView = addLoggedInUser(modelAndView,httpServletRequest);
         String userName = httpServletRequest.getAttribute("currentUser").toString();
         User currentUser = userRepository.fetchUser(userName);
         modelAndView.addObject("info",currentUser);
         modelAndView = getUserInformation(userName,modelAndView);
         List<Tweet> timeline = tweetRepository.fetchHomeTimeline(userName,1);
         modelAndView.addObject("timeline",timeline);
         return modelAndView;
     }
 
     @RequestMapping(value = "MiniTwitter/Website/{id}", method = RequestMethod.GET)
     @ResponseBody
     public ModelAndView printWelcome(@PathVariable("id") String userName,HttpServletRequest httpServletRequest) {
         ModelAndView modelAndView = new ModelAndView("user_profile");
         modelAndView = addLoggedInUser(modelAndView,httpServletRequest);
         try {
             List<Tweet> tweets = tweetRepository.fetchTweets(userName);
             modelAndView.addObject("tweets", tweets);
             modelAndView.addObject("info",userRepository.fetchUser(userName));
             modelAndView = getUserInformation(userName,modelAndView);
             return modelAndView;
         }
         catch (Exception e) {
             log.error(e.toString());
             return new ModelAndView("errorPageTemplate");
         }
     }
 
     @RequestMapping(value = "MiniTwitter/Website/{id}/following", method = RequestMethod.GET)
     @ResponseBody
     public ModelAndView fetchFollowing(@PathVariable("id") String userName,HttpServletRequest httpServletRequest) {
         ModelAndView modelAndView = new ModelAndView("following");
         modelAndView = addLoggedInUser(modelAndView,httpServletRequest);
 //        List<Tweet> tweets = tweetRepository.fetchTweets(userName);
 //        List<User> following = friendRepository.fetchFollowing(userName);
         modelAndView.addObject("info",userRepository.fetchUser(userName));
         modelAndView = getUserInformation(userName,modelAndView);
         System.out.println("Returning basic user, following page requested.");
         return modelAndView;
     }
 
     @RequestMapping(value = "MiniTwitter/Website/{id}/followers", method = RequestMethod.GET)
     @ResponseBody
     public ModelAndView fetchFollowers(@PathVariable("id") String userName, HttpServletRequest httpServletRequest) {
         ModelAndView modelAndView = new ModelAndView("followers");
         modelAndView = addLoggedInUser(modelAndView,httpServletRequest);
         modelAndView.addObject("info",userRepository.fetchUser(userName));
         List<Tweet> tweets = tweetRepository.fetchTweets(userName);
         List<User> followers = friendRepository.fetchFollowers(userName);
         modelAndView = getUserInformation(userName,modelAndView);
         return modelAndView;
     }
 
     @RequestMapping(value = "MiniTwitter/Website/logout", method = RequestMethod.GET)
     public ModelAndView logoutCurrentUser(HttpServletResponse httpServletResponse) {
         log.info("Logging the user out");
         Cookie cookie = new Cookie("token",null);
         cookie.setMaxAge(0);
         cookie.setPath("/MiniTwitter");
         httpServletResponse.addCookie(cookie);
         return new ModelAndView("loginPage");
     }
 
     private ModelAndView addCurrentUser(String userName, ModelAndView modelAndView) {
         User currentUser = userRepository.fetchUser(userName);
         modelAndView.addObject("currentUser",currentUser);
         modelAndView.addObject("currentUserName",currentUser.getName());
         modelAndView.addObject("currentUserEmail",currentUser.getEmail());
         return modelAndView;
     }
 
     private ModelAndView addLoggedInUser(ModelAndView modelAndView,HttpServletRequest httpServletRequest) {
         Object user = httpServletRequest.getAttribute("currentLoggedUser");
         if(user==null) {
             log.info("User not verified");
             modelAndView.addObject("currentLoggedUser", "-1");
         }
         else {
             log.info("User at present is : " + user);
             modelAndView.addObject("currentLoggedUser",user.toString());
             modelAndView = addCurrentUser(user.toString(),modelAndView);
         }
         return modelAndView;
     }
 
     private ModelAndView getUserInformation(String userName, ModelAndView modelAndView) {
         List<Tweet> tweets = tweetRepository.fetchTweets(userName);
         List<User> followers = friendRepository.fetchFollowers(userName);
         List<User> following = friendRepository.fetchFollowing(userName);
         modelAndView.addObject("num_following", following.size());
         modelAndView.addObject("num_followers", followers.size());
         modelAndView.addObject("num_of_tweets", tweets.size());
         modelAndView.addObject("followers", followers);
         modelAndView.addObject("following",following);
         return modelAndView;
     }
 
     @RequestMapping(value = "MiniTwitter/Website/updateInfo", method = RequestMethod.POST)
     @ResponseBody
     public boolean updateUser(@RequestParam("file") MultipartFile file, @RequestParam("password") String password, @RequestParam("name") String name, @RequestParam("description") String description, HttpServletRequest httpServletRequest){
         System.out.println("In updateUser function..");
        System.out.println("FILE:"+ file);
         System.out.println("password: "+ password);
         System.out.println("description: "+ description);
         System.out.println("name: "+ name);
 
         String userName = httpServletRequest.getAttribute("currentUser").toString();
         try {
             FileOutputStream outputStream = null;
             String filePath = System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename();
             outputStream = new FileOutputStream(new File(filePath));
             outputStream.write(file.getBytes());
             outputStream.close();
            System.out.println("calling to imageRepo function");
            System.out.println(file.getBytes().length);
            System.out.println(file.getSize());
            System.out.println("calling to imageRepo function");
             imageRepository.setUserImage(userName, filePath);
         } catch (Exception e) {
             System.out.println("Error while saving file");
            System.out.println("Error:"+e);

         }
         System.out.println("Update settings request confirmation from user: "+userName);
         if (validationChecks.isNameValid(name) && validationChecks.isDescriptionValid(description)) {
             userRepository.updateNameByUsername(userName, HtmlUtils.htmlEscape(name));
             userRepository.updateDescriptionByUsername(userName, HtmlUtils.htmlEscape(description));
         }
 
         if(validationChecks.isPasswordValid(password)) {
             userRepository.updatePasswordByUsername(userName, password);
         }
         return true;
     }
 }
