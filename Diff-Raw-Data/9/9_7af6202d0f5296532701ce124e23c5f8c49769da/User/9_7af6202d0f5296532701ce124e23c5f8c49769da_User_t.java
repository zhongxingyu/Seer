 package ex2;
 
 import java.util.ArrayList;
 
 import ex1.RateableMovie;
 import ex1.UserInterface;
 import ex1.Watchable;
 
 public class User implements UserInterface
 {
 	private final int id;
	private ArrayList<RateableMovie> userRatedMovies;
 	
 	public User(int id)
 	{
 		super();
 		this.id = id;
 		this.userRatedMovies = new ArrayList<UserRatedMovie>();
 	}
 	
 	@Override
 	public ArrayList<RateableMovie> getUserRatedMovies()
 	{
 		return userRatedMovies;
 	}
 
 	@Override
 	public int getId()
 	{
 		return id;
 	}
 	
 	// TODO: check for formula: sim(user4,user4) = 0 ??
 	/** returns the average rating of the user
 	 * @return double: rating
 	 */
 	@Override
 	public double averageRating()
 	{
 		double d = 0d;
 		int count = 0;
 		for (RateableMovie m : this.getUserRatedMovies())
 		{
 			count++;
 			d += Double.valueOf(m.getRating());
 		}
 		return d/count;
 	}
 	
 	@Override
 	public int getMovieRating(Watchable movie)
 	{
		for (RateableMovie m : userRatedMovies)
 		{
 			if (movie.getItemID() == m.getMovie().getItemID()) return m.getRating();
 		}
 		return 0;
 	}
 	
 	/**
 	 * adds an userRatedMovie to the user's record
 	 * @param movie Movie object to be added to user's record
 	 * @param rating the user's rating of that movie
 	 */
 	@Override
 	public void addMovieWithRating(Watchable movie, int rating)
 	{
		//TODO: check if cast works properly
		this.userRatedMovies.add(new UserRatedMovie((Movie) movie, rating));
 	}
 	
 	/** checks wether user has already rated a given movie
 	 * @param user user to be checked
 	 * @param movie movie to be searched for
 	 * @return true if user already rated this movie, else false
 	 */
 	@Override
 	public boolean containsMovie(Watchable movie)
 	{
 		for (RateableMovie urMovie : this.getUserRatedMovies())
 		{
 			if (urMovie.getMovie().getItemID() == movie.getItemID()) return true;
 		}
 		return false;
 	}
 }
