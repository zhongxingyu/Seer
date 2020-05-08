 package com.comsysto.playground.service.impl;
 
 import com.comsysto.playground.repository.api.MovieRepository;
 import com.comsysto.playground.repository.model.Movie;
 import com.comsysto.playground.repository.query.MovieQuery;
 import com.comsysto.playground.service.api.MovieService;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBObject;
 import com.mongodb.util.JSON;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.List;
 import java.util.Random;
 
 /**
  * User: christian.kroemer@comsysto.com
  * Date: 5/29/13
  * Time: 4:37 PM
  */
 @Service
 public class MovieServiceImpl implements MovieService {
 
     // API Key
     private static final String API_KEY = "16a0036a641140ce7e23ddd423dfbf50";
 
     @Autowired
     MovieRepository movieRepository;
 
     @Override
     public long countAll() {
         return movieRepository.countAll();
     }
 
     @Override
     public long countForQuery(MovieQuery query) {
         return movieRepository.countForQuery(query);
     }
 
     @Override
     public List<Movie> findAll() {
         return movieRepository.findAll();
     }
 
     @Override
     public List<Movie> findByQuery(MovieQuery query) {
         return movieRepository.findByQuery(query);
     }
 
     @Override
     public void save(Movie object) {
         movieRepository.save(object);
     }
 
     @Override
     public void delete(Movie object) {
         movieRepository.delete(object);
     }
 
     @Override
     public void deleteAll() {
         movieRepository.removeAll();
     }
 
     @Override
     public void importMovies (int numberOfMovies) {
         try {
             Random rnd = new Random();
 
             int counter = 0;
 
             int numPages = numberOfMovies/20+1;
            for (int page=1; page<=numPages; page++) {
                 URL url = new URL("http://api.themoviedb.org/3/discover/movie?api_key="+API_KEY+"&page="+page);
                 InputStream is = url.openStream();
                 ByteArrayOutputStream os = new ByteArrayOutputStream();
                 int retVal;
                 while ((retVal = is.read()) != -1) {
                     os.write(retVal);
                 }
 
                 final String movieString = os.toString();
                 BasicDBObject parsedResponse = (BasicDBObject) JSON.parse(movieString);
                 List<DBObject> movieList = (List<DBObject>) parsedResponse.get("results");
                 if (movieList.isEmpty()) {
                     break;
                 }
 
                 for (DBObject movieObject : movieList) {
                    if (counter>=numberOfMovies) {
                        break;
                    }

                     String title = movieObject.get("title").toString();
                     String yearString = movieObject.get("release_date").toString().split("-")[0];
 
                     int year = 0;
                     if (yearString.length() >0) {
                         year = Integer.valueOf(yearString);
                     }
 
                     Movie.MovieBuilder movieBuilder = Movie.MovieBuilder.create(title)
                             .withYear(year);
 
                     if (rnd.nextBoolean()) {
                         movieBuilder.withAlreadyWatched(true);
                     }
                     else if (rnd.nextBoolean()) {
                         movieBuilder.withLikeToWatch(true);
                     }
 
                     Movie movie = movieBuilder.build();
                     movieRepository.save(movie);
                     counter++;
                 }
             }
 
         } catch (IOException e) {
             System.out.println("IOException in importMovies()");
             e.printStackTrace();
         }
 
     }
 
 }
