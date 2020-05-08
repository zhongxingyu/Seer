 package com.potatorental.controller;
 
 import com.potatorental.model.Account;
 import com.potatorental.model.Actor;
 import com.potatorental.model.Customer;
 import com.potatorental.model.Movie;
 import com.potatorental.repository.AccountDao;
 import com.potatorental.repository.MovieDao;
 import com.potatorental.repository.PersonsDao;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 import javax.servlet.http.HttpServletRequest;
 import java.security.Principal;
 import java.util.List;
 
 /**
  * User: milky
  * Date: 5/6/13
  * Time: 8:04 PM
  */
 @Controller
 @RequestMapping("/movies")
 @SessionAttributes({"movies", "movieactors"})
 public class MovieController {
 
     private MovieDao movieDao;
     private AccountDao accountDao;
     private PersonsDao personsDao;
 
     @Autowired
     public MovieController(MovieDao movieDao, AccountDao accountDao, PersonsDao personsDao) {
         this.movieDao = movieDao;
         this.accountDao = accountDao;
         this.personsDao = personsDao;
     }
 
     @ModelAttribute("movieId")
     public Movie newMovie() {
         return new Movie();
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
 
     @RequestMapping(value = "{movieid}", method = RequestMethod.GET)
     public ModelAndView getMovie(@PathVariable int movieid, ModelMap modelMap,
                                  HttpServletRequest request, Principal principal) {
         Movie movie = movieDao.getMovieById(movieid);
         List<Actor> actors = movieDao.getMovieActors(movie);
 
         modelMap.addAttribute("movie", movie);
         modelMap.addAttribute("movieactors", actors);
 
         return new ModelAndView("movie", modelMap);
     }
 
     @RequestMapping(value = "{movieid}/edit", method = RequestMethod.GET)
     public String getMovieForm(@PathVariable Integer movieid) {
         return "movieform";
     }
 
     @RequestMapping(value = "{movieid}/done", method = RequestMethod.POST)
     public String submitMovieForm(@ModelAttribute Movie movie, HttpServletRequest request,
                                   @PathVariable Integer movieid, RedirectAttributes redirectAttributes) {
         movie.setId(movieid);
         movieDao.updateMovie(movie);
         return "redirect:/movies/{movieid}";
     }
 
 
     @RequestMapping(value = "genres", method = RequestMethod.GET)
     public String getGenres(ModelMap modelMap) {
         if (modelMap.get("movies") != null)
             return "genres";
 
         modelMap.addAttribute("movies", movieDao.getAllMovies());
         return "genres";
     }
 
     @RequestMapping(value = "popular", method = RequestMethod.GET)
     public String getPopular(ModelMap modelMap) {
 
         modelMap.addAttribute("popular", movieDao.getPopularMovies(50));
         return "popular";
     }
 
     @RequestMapping(value = "movielist", method = RequestMethod.GET)
     public String getMovielist(ModelMap modelMap) {
         if (modelMap.get("movies") != null)
             return "movielist";
 
         modelMap.addAttribute("movies", movieDao.getAllMovies());
         return "movielist";
     }
 
     @RequestMapping(value = "recommendation", method = RequestMethod.GET)
     public String getRecomendations(ModelMap modelMap, Principal principal) {
        if (principal == null)
             return "recommendations";
 
        modelMap.addAttribute("recommendations", accountDao.personalRecommendation(
                 personsDao.getPersonByEmail(principal.getName()).getSsn()));
         return "recommendations";
     }
 
     @RequestMapping(value = "search", method = RequestMethod.GET)
     public String getSearch() {
         return "search";
     }
 
 
 
 /*
     @RequestMapping(value = "insert", method = RequestMethod.GET)
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
