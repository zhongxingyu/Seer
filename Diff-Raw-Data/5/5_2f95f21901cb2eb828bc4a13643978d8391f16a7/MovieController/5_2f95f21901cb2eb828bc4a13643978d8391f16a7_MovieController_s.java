 package za.co.imqs.example;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 import org.springframework.http.HttpStatus;
 
 import za.co.imqs.example.Movie;
 
 @Controller
 @RequestMapping("/movieDB")
 public class MovieController {
 
 	List<Movie> movies = new ArrayList<Movie>();
 	public MovieController(){
 		// Just add a movie so we can test if we get a list out
 		movies.add(new Movie("Alien", "pg16", "Ridley Scott"));
 	}
 
 	// Request of the form /movieDB/get/{name to retrieve}
 	@RequestMapping(value="/get/{name}", method = RequestMethod.GET)
 	public @ResponseBody Movie getMovie(@PathVariable("name") String name) {
 		Iterator<Movie> it = movies.iterator();
 		while (it.hasNext())
 		{
 			Movie m = it.next();
 			System.out.println(name+ " " + m.getName());
 			if (m.getName().equals(name)) {
 				return m;
 			}
 		}
		return new Movie("","","");
 	}
 	
 	// Request of the form "movieDB/add?name=Terminator2&rating=pg13&director=Someone&20Important"
 	// Note we need to specify the ReqestParam name as Java cannot infer it if it is compiled without debugging
 	// enabled. 
 	@RequestMapping(value="/add", method = RequestMethod.GET)
 	@ResponseStatus(HttpStatus.CREATED)
 	public void addMovie(@RequestParam("name") String name, @RequestParam("rating") String rating, @RequestParam("director") String director) {
 		movies.add(new Movie(name,rating,director));
 	}
 	
 	// Return a list of all movies in the database
 	@RequestMapping(value="/list", method = RequestMethod.GET) 
 	public @ResponseBody List<Movie> listMovies(){
 		return movies;
 	}	
 }
