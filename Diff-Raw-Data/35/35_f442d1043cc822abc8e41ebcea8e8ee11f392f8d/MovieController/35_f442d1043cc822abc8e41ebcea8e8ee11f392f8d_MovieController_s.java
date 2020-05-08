 package org.nnc.moviediary.controller;
 
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.nnc.moviediary.domain.entities.Movie;
 import org.nnc.moviediary.service.interfaces.CelebrityService;
 import org.nnc.moviediary.service.interfaces.GenreService;
 import org.nnc.moviediary.service.interfaces.MovieService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 @Controller
 public class MovieController {
 
 	@Autowired
 	private MovieService movieService;
 	@Autowired
 	private CelebrityService celebrityService;
 	@Autowired
 	private GenreService genreService;
 
 	@RequestMapping(value = "/add-movie", method = RequestMethod.GET)
 	public String addMovie(final Model model) {
 		model.addAttribute("celebrities", celebrityService.getAllCelebrities());
 		model.addAttribute("genres", genreService.getAllGenres());
 		return "addMovie";
 	}
 
 	@RequestMapping(value = "/save-movie", method = RequestMethod.POST)
	public String saveMovie(final Model model, @RequestParam("movieId") final String movieId, @RequestParam(value = "visible", defaultValue = "false") final Boolean visible,
			@RequestParam("originalTitle") final String originalTitle, @RequestParam("englishTitle") final String englishTitle,
			@RequestParam("hungarianTitle") final String hungarianTitle, @RequestParam("year") final String year,
			@RequestParam("runningTime") final String runningTime, @RequestParam("directorId") final String directorId,
			@RequestParam("imdbLink") final String imdbLink, @RequestParam("rating") final String rating, @RequestParam("language") final String language,
			@RequestParam(value = "genres", required = false) final String[] genres, @RequestParam(value = "actors", required = false) final String[] actors) {
 		List<String> errors = movieService.saveMovie(movieId, visible, originalTitle, englishTitle, hungarianTitle, year, runningTime, directorId, rating,
 				language, genres, actors, imdbLink);
 		if (errors.isEmpty()) {
 			return "redirect:/movies";
 		} else {
 			model.addAttribute("errors", errors);
 			return "/add-movie";
 		}
 	}
 
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public String index(final Model model) {
 		return getAllMovie(model);
 	}
	
 	@RequestMapping(value = "/movies", method = RequestMethod.GET)
 	public String getAllMovie(final Model model) {
 		List<Movie> movies = movieService.getAllMovies();
 		model.addAttribute("movies", movies);
 		return "movies";
 	}
 
 	@RequestMapping(value = "/movie/**", method = RequestMethod.GET)
 	public String getMovie(final HttpServletRequest request, final ModelMap model) {
 		try {
 			String movieUrl = request.getRequestURI();
 			int start = movieUrl.indexOf("/movie/") + "/movie/".length();
 			int end = movieUrl.indexOf("/", start);
 			String movieIdString = movieUrl.substring(start, end);
 			Long movieId = Long.parseLong(movieIdString);
 			model.addAttribute("movie", movieService.getMovie(movieId));
 			return "movie";
 		} catch (NumberFormatException e) {
 			System.out.println("Nincs ilyen oldal.");
 			return "redirect:/movies";
 		}
 	}
 }
