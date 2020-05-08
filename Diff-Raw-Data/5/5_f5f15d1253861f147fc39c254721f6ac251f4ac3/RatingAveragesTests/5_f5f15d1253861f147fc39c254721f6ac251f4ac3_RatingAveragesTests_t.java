 package com.thoughtworks.thoughtferret.unittests;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 
 import org.junit.Test;
 
 import com.thoughtworks.thoughtferret.model.mood.MoodRating;
 import com.thoughtworks.thoughtferret.model.mood.MoodRatings;
 import com.thoughtworks.thoughtferret.view.moodgraph.RatingAverages;
 
 public class RatingAveragesTests {
 
 	@Test
 	public void shouldCalculateAveragesWhenAllPeriodsHaveRatings() {
 		MoodRatings ratings = new MoodRatings(
 				new MoodRating("04-06-2010 09:45", 3),
 				new MoodRating("22-07-2010 16:28", 3),
 				new MoodRating("17-08-2010 16:03", 1),
 				new MoodRating("09-09-2010 18:16", 2));
 		RatingAverages averages = new RatingAverages(ratings, 30);
		assertEquals(5, averages.getAverages().size());
 	}
 
 	@Test
 	public void shouldCalculateAveragesWhenAPeriodHasNoRatings() {
 		MoodRatings ratings = new MoodRatings(
 				new MoodRating("04-06-2010 09:45", 3),
 				new MoodRating("17-08-2010 16:03", 1),
 				new MoodRating("09-09-2010 18:16", 2));
 		RatingAverages averages = new RatingAverages(ratings, 30);
		assertEquals(5, averages.getAverages().size());
 		assertFalse(averages.getAverages().get(1).hasRatings());
 	}
 
 	
 }
