 package com.minitwitter.controller;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.minitwitter.service.TweetService;
 
 @Controller
 public class UserController {
 	@Autowired
 	TweetService tweetService;
 	@RequestMapping("/")
 	public ModelAndView index() {
 		ModelAndView mv = new ModelAndView("index");
 		return mv;
 	}
 
 	@RequestMapping("/{username}")
 	public ModelAndView showTweet(@PathVariable String username) {
 		List<Map<String, Object>> listTweet = listTweet(username);
 		return new ModelAndView("post", "tweets", listTweet);
 	}
 
 	@RequestMapping("/{username}/post")
 	public ModelAndView post(@PathVariable String username) {
 		List<Map<String, Object>> listTweet = listTweet(username);
 		return new ModelAndView("post", "tweets", listTweet);
 	}
 
 	@RequestMapping(value = "/{username}", method = RequestMethod.POST)
 	public ModelAndView post(@PathVariable String username, @RequestParam String message) {
 		ModelAndView mv = new ModelAndView("post");
 		if (message != null && message != "") {
 			tweetService.tweet(username, message);
 			System.out.println("post as: " + username);
 			System.out.println("with message: " + message);
 		} else {
 			System.out.println("post empty message as: " + username);
 		}
 		return mv;
 	}
 
 	@RequestMapping(value = "/{username}/delete/{tweet}", method = RequestMethod.POST)
 	public ModelAndView delete(@PathVariable String username, @PathVariable String tweet) {
 		ModelAndView mv = new ModelAndView("post");
 		tweetService.delete(username, tweet);
 		System.out.println("post as: " + username);
 		return mv;
 	}
 
 	private List<Map<String, Object>> listTweet(String username){
 		Set<TypedTuple<String>> tweets = tweetService.list(username, "all");
 		List<Map<String, Object>> listTweet = new ArrayList<Map<String,Object>>();
 		for (TypedTuple<String> tweet: tweets) {
 			Map<String, Object> mapTweet = new HashMap<String, Object>();
 			mapTweet.put("message", tweet.getValue());
 			mapTweet.put("time", new Date(tweet.getScore().longValue()));
 			listTweet.add(mapTweet);
 		}
 
 		return listTweet;
 	}
 }
