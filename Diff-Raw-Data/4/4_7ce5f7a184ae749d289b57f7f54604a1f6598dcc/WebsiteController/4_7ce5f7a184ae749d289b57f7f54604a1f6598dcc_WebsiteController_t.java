 package com.springapp.mvc.controller;
 
 import com.springapp.mvc.data.TokenRepository;
 import com.springapp.mvc.data.TweetRepository;
 import com.springapp.mvc.data.UserRepository;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletResponse;
 import java.util.UUID;
 
 /**
  * Created with IntelliJ IDEA.
  * User: mayday
  * Date: 22/7/13
  * Time: 3:00 PM
  * To change this template use File | Settings | File Templates.
  */
 
 @Controller
 @RequestMapping("MiniTwitter/Website")
 public class WebsiteController {
 
     private final UserRepository userRepository;
     private final TweetRepository tweetRepository;
     private final TokenRepository tokenRepository;
 
     static Logger log = Logger.getLogger(UserRepository.class);
 
     @Autowired
     public WebsiteController(UserRepository userRepository,TweetRepository tweetRepository, TokenRepository tokenRepository) {
         this.userRepository = userRepository;
         this.tweetRepository = tweetRepository;
         this.tokenRepository = tokenRepository;
     }
 
     @RequestMapping(method= RequestMethod.GET)
     @ResponseBody
     public ModelAndView loginPage() {
         ModelAndView modelAndView = new ModelAndView("loginPage");
         return modelAndView;
     }
 
     @RequestMapping(method= RequestMethod.POST)
     public String verifyUser(@RequestParam("username") String username, @RequestParam("password") String password,
                              ModelMap model, HttpServletResponse httpServletResponse) {
         System.out.println("Verifying user");
         log.debug("Verifying user");
         if (userRepository.isUserValid(username, password)){
             String token = UUID.randomUUID().toString();
             Cookie cookie = new Cookie("token",token+"|"+username);
             cookie.setMaxAge(3600);
             cookie.setPath("/MiniTwitter");
             tokenRepository.setToken(token,username);
             httpServletResponse.addCookie(cookie);
             String redirectUrl = "/MiniTwitter/Website/home";
             return "redirect:" + redirectUrl;
         }
        model.addAttribute("heading", "Wrong Credentials.");
        model.addAttribute("message", "You can try again, or Sign Up now!");
        model.addAttribute("errorCode", "401");
         return "errorPageTemplate";
     }
 }
