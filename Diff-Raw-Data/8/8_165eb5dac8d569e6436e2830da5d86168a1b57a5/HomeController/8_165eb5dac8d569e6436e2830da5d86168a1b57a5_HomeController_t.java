 package com.potatorental.controller;
 
 import com.potatorental.model.Movie;
 import com.potatorental.model.Person;
 import com.potatorental.repository.AccountDao;
 import com.potatorental.repository.ActorDao;
 import com.potatorental.repository.MovieDao;
 import com.potatorental.repository.PersonsDao;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.DataAccessException;
 import org.springframework.security.core.context.SecurityContext;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.bind.support.SessionStatus;
 import org.springframework.web.servlet.ModelAndView;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.security.Principal;
 import java.util.List;
 
 
 @Controller
 @RequestMapping("/")
 @SessionAttributes({"popular", "recommended"})
 public class HomeController {
 
     private final MovieDao movieDao;
     private final AccountDao accountDao;
     private final PersonsDao personsDao;
     private final ActorDao actorDao;
 
     @Autowired
     public HomeController(MovieDao movieDao, AccountDao accountDao, PersonsDao personsDao, ActorDao actorDao) {
         this.movieDao = movieDao;
         this.accountDao = accountDao;
         this.personsDao = personsDao;
         this.actorDao = actorDao;
     }
 
     @RequestMapping(method = RequestMethod.GET)
     public ModelAndView printWelcome(ModelMap modelMap, Principal principal) {
         if (modelMap.get("popular") == null) {
             List<Movie> movies = movieDao.getPopularMovies(30);
             modelMap.addAttribute("popular", movies);
         }
         if (principal != null) {
             Person person = personsDao.getPersonByEmail(principal.getName());
             List<Movie> recommended = accountDao.personalRecommendation(person.getSsn());
             modelMap.addAttribute("recommended", recommended);
         }
 
         return new ModelAndView("home", modelMap);
     }
 
     @RequestMapping(value = "help", method = RequestMethod.GET)
     public String getHelp() {
         return "help";
     }
 
     @RequestMapping(value = "documentation", method = RequestMethod.GET)
     public String getDocumentation() {
         return "documentation";
     }
     
     @RequestMapping(value = "tables", method = RequestMethod.GET)
     public String getTables() {
         return "tables";
     }
 
/*    @RequestMapping(value = "allactors")
     public String allActors() {
        *//*String path = "C:\\Users\\Milky\\IdeaProjects\\PotatoRental\\src\\main\\webapp\\resources\\images\\actors";*//*
         String path = "C:\\Users\\Milky\\IdeaProjects\\PotatoRental\\src\\main\\webapp\\resources\\images\\actors\\";
         File files = new File(path);
         File allFiles[] = files.listFiles();
 
         for (File file : allFiles) {
             String oldFile = file.getName().substring(0, file.getName().length()-4);
             try {
                 int img = actorDao.getActorId(oldFile);
                 file.renameTo(new File(path + img + ".jpg"));
             } catch (DataAccessException e) {
                 continue;
             }
         }
 
         return "actors";
    }*/
 }
