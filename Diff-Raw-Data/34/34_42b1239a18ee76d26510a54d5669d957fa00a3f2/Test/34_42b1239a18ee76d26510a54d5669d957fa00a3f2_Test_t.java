 /*
  *      Copyright (c) 2004-2011 YAMJ Members
  *      http://code.google.com/p/moviejukebox/people/list 
  *  
  *      Web: http://code.google.com/p/moviejukebox/
  *  
  *      This software is licensed under a Creative Commons License
  *      See this page: http://code.google.com/p/moviejukebox/wiki/License
  *  
  *      For any reuse or distribution, you must make clear to others the 
  *      license terms of this work.  
  */
 package com.moviejukebox.rottentomatoes;
 
import java.util.Set;
 
 import junit.framework.TestCase;
 
 import com.moviejukebox.rottentomatoes.model.Cast;
 import com.moviejukebox.rottentomatoes.model.Link;
 import com.moviejukebox.rottentomatoes.model.Movie;
 import com.moviejukebox.rottentomatoes.model.Review;
 
 public class Test extends TestCase {
     public RottenTomatoes rt = new RottenTomatoes("rnt8xak564a8sxkts5xkqj5z");
 
     public Test(String name) {
         super(name);
     }
     
     protected void setUp() throws Exception {
         super.setUp();
     }
 
     protected void tearDown() throws Exception {
         super.tearDown();
     }
 
     public void testGetLists() {
        Set<Link> response = rt.getLists();
         assertEquals(2, response.size());
     }
     
     public void testMoviesSearch() {
        Set<Movie> response = rt.moviesSearch("Blade Runner");
         assertEquals(3, response.size());
     }
 
     public void NOtestListsDirectory() {
        Set<Link> response = rt.listsDirectory();
         assertEquals(2, response.size());
     }
     
     public void testMovieListsDirectory() {
        Set<Link> response = rt.movieListsDirectory();
         assertEquals(4, response.size());
     }
     
     public void testDvdListsDirectory() {
        Set<Link> response = rt.dvdListsDirectory();
         assertTrue(response.size() > 0);
     }
 
     public void testOpeningMovies() {
         int maxMovies = 10;
        Set<Movie> response = rt.openingMovies(maxMovies);
         assertTrue(response.size() <= maxMovies);
     }
 
     public void testUpcomingMovies() {
         int maxMovies = 10;
        Set<Movie> response = rt.upcomingMovies(maxMovies);
         assertTrue((response.size() > 0) && (response.size() <= maxMovies));
     }
 
     public void testNewReleaseDvds() {
         int maxMovies = 10;
        Set<Movie> response = rt.newReleaseDvds(maxMovies);
         assertTrue((response.size() > 0) && (response.size() <= maxMovies));
     }
 
     public void testMovieInfo() {
         Movie movie = rt.movieInfo(12886);
         assertEquals("Blade Runner", movie.getTitle());
     }
     
     public void testMovieCast() {
        Set<Cast> castList = rt.movieCast(12886);
         assertTrue(castList.size() > 0);
     }
 
     public void testMovieReviews() {
        Set<Review> reviewList = rt.movieReviews(12886, Review.ReviewType.all);
         assertTrue(reviewList.size() > 0);
     }
     
 }
