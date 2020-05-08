 package com.potatorental.controller;
 
 import com.potatorental.model.Movie;
 import com.potatorental.repository.MovieDao;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
 import org.springframework.web.servlet.ModelAndView;
 
 import java.util.List;
 
 
 @Controller
 @RequestMapping("/")
 @SessionAttributes("popular")
 public class HomeController {
 
     private MovieDao movieDao;
 
     @Autowired
     public HomeController(MovieDao movieDao) {
         this.movieDao = movieDao;
     }
 
     @RequestMapping(method = RequestMethod.GET)
     public ModelAndView printWelcome(ModelMap modelMap) {
         if (modelMap.get("popular") == null) {
             List<Movie> movies = movieDao.getPopularMovies(30);
             modelMap.addAttribute("popular", movies);
         }
 
         return new ModelAndView("home", modelMap);
     }
 
     @RequestMapping(value = "help", method = RequestMethod.GET)
     public String getHelp() {
         return "help";
     }
 
 }
