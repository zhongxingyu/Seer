 /**
  *
  */
 package de.pentasys.k.domain;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Random;
 
 /**
  * @author <a href="mailto:martin.dilger@pentasys.de">Martin Dilger</a>
  * @since 21.07.2010
  */
 public class VideoStoreFacadeImpl implements VideoStoreFacade {
 
     private List<Movie> movieList;
 
     private Random random = new Random();
 
     public VideoStoreFacadeImpl() {
 	initMockData();
     }
 
     /*
      * (non-Javadoc)
      *
      * @see de.pentasys.k.domain.VideoStoreFacade#getMovies()
      */
     public List<Movie> getMovies() {
	return null;
     }
 
     /*
      * (non-Javadoc)
      *
      * @see
      * de.pentasys.k.domain.VideoStoreFacade#submitOrder(de.pentasys.k.domain
      * .Customer, java.util.List)
      */
     public void submitOrder(Customer customer, List<Movie> movies) {
     }
 
     /*
      * (non-Javadoc)
      *
      * @see de.pentasys.k.domain.VideoStoreFacade#getDailyOffers()
      */
     public Movie getDailyOffer() {
 	return movieList.get(random.nextInt() * (movieList.size()-1));
     }
 
     private void initMockData() {
 	movieList = new ArrayList<Movie>();
 
 	/*
 	 * Die Hard
 	 */
 	List<Actor> dieHardActors = new ArrayList<Actor>();
 	dieHardActors.add(new Actor("Bruce Willis", new Date(), 5));
 	Movie dieHard = new Movie("Die Hard 4.0", dieHardActors, new Date(),
 		5.0);
 
 	/*
 	 * Speed
 	 */
 	List<Actor> speedActors = new ArrayList<Actor>();
 	speedActors.add(new Actor("Keanu Reeves", new Date(), 4));
 	speedActors.add(new Actor("Sandra Bullock", new Date(), 3));
 	Movie speed = new Movie("Speed", speedActors, new Date(), 3.0);
 
 	/*
 	 * King of Queens - Season 1
 	 */
 	List<Actor> koqActors = new ArrayList<Actor>();
	speedActors.add(new Actor("Kevin ...", new Date(), 4));
	speedActors.add(new Actor("...", new Date(), 3));
	speedActors.add(new Actor("... Stiller", new Date(), 2));
 	Movie koq = new Movie("King of Queens Season 1", koqActors, new Date(),
 		5.0);
 
 	movieList.add(dieHard);
 	movieList.add(speed);
 	movieList.add(koq);
 
 
     }
 
 }
