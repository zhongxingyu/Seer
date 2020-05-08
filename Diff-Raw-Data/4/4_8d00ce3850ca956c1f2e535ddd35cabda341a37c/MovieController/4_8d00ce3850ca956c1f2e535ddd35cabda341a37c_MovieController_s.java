 package com.potatorental.controller;
 
 import com.potatorental.model.Movie;
 import com.potatorental.repository.MovieDao;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.servlet.ModelAndView;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 /**
  * User: milky
  * Date: 5/6/13
  * Time: 8:04 PM
  */
 @Controller
 @RequestMapping("/movies")
 @SessionAttributes("movies")
 public class MovieController {
 
     public MovieDao movieDao;
 
     @Autowired
     public MovieController(MovieDao movieDao) {
         this.movieDao = movieDao;
     }
 
     @RequestMapping(method = RequestMethod.GET)
     public ModelAndView getMovies(ModelMap modelMap) {
         if (modelMap.get("movies") != null)
             return new ModelAndView("movies", modelMap);
 
         modelMap.addAttribute("movies", movieDao.getAllMovies());
 /*        if (num == null)
             modelMap.addAttribute("movies", movieDao.getAllMovies());
         else
             modelMap.addAttribute("movies", movieDao.getNumMovies(num));*/
 
         return new ModelAndView("movies", modelMap);
     }

    /*@RequestMapping(value = "insert", method = RequestMethod.GET)
     public ModelAndView insertMovies(ModelMap modelMap) {
         BufferedReader reader = null;
         List<Movie> movies = new ArrayList<>();
 
         try {
             reader = new BufferedReader(new FileReader("C:\\Users\\Milky\\IdeaProjects\\" +
                     "PotatoRental\\src\\main\\webapp\\resources\\movies.txt"));
 
             Random ran = new Random();
             String title = null;
             String genre = null;
             do {
                 title = reader.readLine();
                 if (title == null)
                    break;
                 genre = reader.readLine();
 
                 Movie movie = new Movie();
                 movie.setName(title);
                 movie.setType(genre);
                 movie.setNumCopies(ran.nextInt(25));
                 movie.setRating(ran.nextInt(100) < 15 ? ran.nextInt(3) + 2 : ran.nextInt(2) + 4);
                 movie.setDistrFee(ran.nextFloat() * (ran.nextInt(2) * 10000 + 1000) + ran.nextInt(100) * 100);
 
                 movies.add(movie);
 
                 movieDao.insertMovie(movie);
 
             } while (title != null);
 
 
         } catch (IOException e) {
             System.err.println("File not found");
         }
 
         modelMap.addAttribute("movies", movies);
 
         return new ModelAndView("movies", modelMap);
     }*/
 }
