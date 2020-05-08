 package com.springapp.mvc.controller;
 
 import com.springapp.mvc.data.FriendRepository;
 import com.springapp.mvc.data.TweetRepository;
 import com.springapp.mvc.data.UserRepository;
 import com.springapp.mvc.model.Tweet;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 import java.util.List;
 
 @Controller
 public class WebsiteViewProfileController {
 
     private final UserRepository userRepository;
     private final TweetRepository tweetRepository;
     private final FriendRepository friendRepository;
 
     static Logger log = Logger.getLogger(UserRepository.class);
 
     @Autowired
     public WebsiteViewProfileController(UserRepository userRepository, TweetRepository tweetRepository, FriendRepository friendRepository) {
         this.userRepository = userRepository;
         this.tweetRepository = tweetRepository;
         this.friendRepository = friendRepository;
     }
 
     @RequestMapping(value = "MiniTwitter/Website/{id}", method = RequestMethod.GET)
     @ResponseBody
     public ModelAndView printWelcome(@PathVariable("id") String userName) {
        ModelAndView modelAndView = new ModelAndView("user_profile");
         List<Tweet> tweets = tweetRepository.fetchTweets(userName);
         modelAndView.addObject("tweets", tweets);
         modelAndView.addObject("info",userRepository.fetchUser(userName));
         modelAndView.addObject("num_followers", friendRepository.fetchFollowers(userName).size());
         modelAndView.addObject("num_following", friendRepository.fetchFollowing(userName).size());
         modelAndView.addObject("num_of_tweets", tweets.size());
         return modelAndView;
     }
 }
