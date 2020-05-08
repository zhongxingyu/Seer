 package com.dedaulus.cinematty.framework.tools;
 
 import android.util.Pair;
 import com.dedaulus.cinematty.framework.Cinema;
 import com.dedaulus.cinematty.framework.Movie;
 
 import java.util.*;
 
 /**
  * User: Dedaulus
  * Date: 21.08.11
  * Time: 2:18
  */
 public class MovieComparator implements Comparator<Movie> {
     private MovieSortOrder sortOrder;
     private int day;
     private Map<String, Pair<Movie, List<Calendar>>> cinemaShowTimes;
     private Calendar now;
 
     public MovieComparator(MovieSortOrder sortOrder) {
         this.sortOrder = sortOrder;
     }
 
     public MovieComparator(MovieSortOrder sortOrder, int day) {
         this.sortOrder = sortOrder;
         this.day = day;
     }
 
     public MovieComparator(MovieSortOrder sortOrder, Map<String, Pair<Movie, List<Calendar>>> cinemaShowTimes) {
         this.sortOrder = sortOrder;
         this.cinemaShowTimes = cinemaShowTimes;
         now = Calendar.getInstance();
     }
 
     public MovieComparator(MovieSortOrder sortOrder, int day, Map<String, Pair<Movie, List<Calendar>>> cinemaShowTimes) {
         this.sortOrder = sortOrder;
         this.day = day;
         this.cinemaShowTimes = cinemaShowTimes;
         now = Calendar.getInstance();
     }
 
     public int compare(Movie o1, Movie o2) {
         switch (sortOrder) {
         case BY_CAPTION:
             return o1.getName().compareTo(o2.getName());
 
         case BY_POPULAR:
             Collection<Cinema> l1 = o1.getCinemas(day).values();
             Collection<Cinema> l2 = o2.getCinemas(day).values();
             int size1 = l1 != null ? l1.size() : 0;
             int size2 = l2 != null ? l2.size() : 0;
             if (size1 == size2) return 0;
             return (size1 < size2) ? 1 : -1;
 
         case BY_IMDB:
             float imdb1 = o1.getImdb();
             float imdb2 = o2.getImdb();
             if (imdb1 == imdb2) return 0;
             return imdb1 < imdb2 ? 1 : -1;
 
         case BY_KP:
             float kp1 = o1.getKp();
             float kp2 = o2.getKp();
             if (kp1 == kp2) return 0;
             return kp1 < kp2 ? 1 : -1;
 
         case BY_TIME_LEFT:
             List<Calendar> showTimes1 = cinemaShowTimes.get(o1.getName()).second;
             Calendar showTime1 = DataConverter.getClosestTime(showTimes1, now);
 
             List<Calendar> showTimes2 = cinemaShowTimes.get(o2.getName()).second;
             Calendar showTime2 = DataConverter.getClosestTime(showTimes2, now);
 
             if (showTime1 == showTime2) {
                 return 0;
             }
             if (showTime1 == null) {
                 return 1;
             }
             if (showTime2 == null) {
                 return -1;
             }
             return showTime1.compareTo(showTime2);
 
         default:
            throw new RuntimeException("Sort order not implemented!");
         }
     }
 }
