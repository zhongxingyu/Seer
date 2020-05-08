 package org.nnc.moviediary.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.nnc.moviediary.domain.entities.Genre;
 import org.nnc.moviediary.domain.entities.Movie;
 import org.nnc.moviediary.domain.model.MovieTitleModel;
 import org.nnc.moviediary.service.interfaces.GenreService;
 import org.nnc.moviediary.service.interfaces.MovieService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 @Controller
 public class GenreController {
 
 	@Autowired
 	private GenreService genreService;
 	@Autowired
 	private MovieService movieService;
 
 	@RequestMapping(value = "/genres", method = RequestMethod.GET)
 	public String getGenres(final Model model) {
 		List<Genre> genres = genreService.getAllGenres();
 		model.addAttribute("genres", genres);
 		return "genres";
 	}
 
 	@RequestMapping(value = "/add-genre", method = RequestMethod.GET)
 	public String addGenre(final Model model) {
 		model.addAttribute("movies", movieService.getAllMovies());
 		return "addGenre";
 	}
 
 	@RequestMapping(value = "/edit-genre", method = RequestMethod.GET)
 	public String editGenre(final Model model, @RequestParam("genreId") final String genreId) {
 		model.addAttribute("genre", genreService.getGenreById(genreId));
 		model.addAttribute("movies", movieService.getAllMovies());
 		return "addGenre";
 	}
 
 	@RequestMapping(value = "/save-genre", method = RequestMethod.POST)
 	public String saveGenre(final Model model, @RequestParam(value = "visible", defaultValue = "false") final Boolean visible,
 			@RequestParam("name") final String name, @RequestParam("description") final String description,
 			@RequestParam(value = "moviesIds", required = false) final String[] moviesIds) {
 		List<String> errors = genreService.saveGenre(visible, name, description, moviesIds);
 		if (errors.isEmpty()) {
 			return "redirect:genres";
 		} else {
 			model.addAttribute("errors", errors);
 			return "/save-genre";
 		}
 	}
 
 	@RequestMapping(value = "/update-genre", method = RequestMethod.POST)
 	public String updateGenre(final Model model, @RequestParam("genreId") final String genreId,
 			@RequestParam(value = "visible", defaultValue = "false") final Boolean visible, @RequestParam("name") final String name,
 			@RequestParam("description") final String description, @RequestParam(value = "moviesIds", required = false) final String[] moviesIds) {
 		List<String> errors = genreService.updateGenre(genreId, visible, name, description, moviesIds);
 		if (errors.isEmpty()) {
 			return "redirect:genres";
 		} else {
 			model.addAttribute("errors", errors);
 			return "/save-genre";
 		}
 	}
 
 	@RequestMapping(value = "/ajax-get-movies", method = RequestMethod.GET)
 	public @ResponseBody
 	List<MovieTitleModel> ajaxGetMovies(@RequestParam("term") final String prefix) {
 		List<Movie> movies = movieService.getMoviesByTitlePiece(prefix);
 		List<MovieTitleModel> titles = new ArrayList<>();
 		for (Movie movie : movies) {
 			titles.add(new MovieTitleModel(Long.toString(movie.getId()), movie.getOriginalTitle() + " (" +  movie.getYear() + ")"));
 		}
 		return titles;
 	}
 }
