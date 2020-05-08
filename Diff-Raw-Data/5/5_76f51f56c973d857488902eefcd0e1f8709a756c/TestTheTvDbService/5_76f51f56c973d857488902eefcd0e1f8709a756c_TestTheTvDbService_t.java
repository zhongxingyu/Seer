 /*
  * Copyright (C) 2009 Jean Couteau
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.kootox.episodesmanager.services.databases;
 
 import org.junit.Before;
 import org.kootox.episodesmanager.entities.Episode;
 import org.kootox.episodesmanager.entities.Season;
 import org.kootox.episodesmanager.entities.Show;
 import java.util.Map;
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.UnknownHostException;
 import org.junit.After;
 import org.junit.Test;
 import org.kootox.episodesmanager.services.AbstractEpisodesManagerServiceTest;
 import org.kootox.episodesmanager.services.shows.EpisodesService;
 import org.kootox.episodesmanager.services.shows.SeasonsService;
 import org.kootox.episodesmanager.services.shows.ShowsService;
 
 import static org.junit.Assert.*;
 import static org.junit.Assume.assumeTrue;
 
 /**
  *
  * @author couteau
  */
 public class TestTheTvDbService extends AbstractEpisodesManagerServiceTest {
     
     TheTvDbService service;
 
     ShowsService showService = newService(ShowsService.class);
     SeasonsService seasonsService = newService(SeasonsService.class);
     EpisodesService episodesService = newService(EpisodesService.class);
 
     /**
      * Method to test the internet connection to skip tests if not present. Use
      * the services.tvrage.com to test the connection. It is the one used by
      * episodes-manager
      * @return true if connected to Internet, false otherwise
      */
     public Boolean testInternet() {
 
         try {
             //make a URL to a known source
             URL url = new URL("http://www.thetvdb.com");
 
             //open a connection to that source
             HttpURLConnection urlConnect = (HttpURLConnection) url.openConnection();
 
             //trying to retrieve data from the source. If there
             //is no connection, this line will fail
             urlConnect.getContent();
 
         } catch (UnknownHostException e) {
             System.out.println("Not connected to Internet, skip test");
             return false;
         }
         catch (IOException e) {
             System.out.println("Not connected to Internet, skip test");
             return false;
         }
         return true;
     }
 
     @Before
     public void setUpService() {
         service = newService(TheTvDbService.class);
     }
 
     @After
     public void shutdown() throws Exception{
 
         Show show = showService.getShowByName("Buffy the Vampire Slayer");
         if (show != null){
             showService.deleteShow(show);
         }
     }
 
     @Test
     public void testSearchFrench() {
 
         assumeTrue(testInternet());
 
         Map<Integer, String> searchResults = service.search("buffy", "fr");
         assertEquals(3, searchResults.size());
         assertTrue(searchResults.containsValue("Buffy contre les vampires"));
         assertTrue(searchResults.containsValue("Buffy the Vampire Slayer: The Motion Comic"));
         assertTrue(searchResults.containsValue("Buffy the Animated Series"));
     }
 
     @Test
     public void testSearchEnglish() {
 
         assumeTrue(testInternet());
 
         Map<Integer, String> searchResults = service.search("buffy", "en");
         assertEquals(3, searchResults.size());
         assertTrue(searchResults.containsValue("Buffy the Vampire Slayer"));
         assertTrue(searchResults.containsValue("Buffy the Vampire Slayer: The Motion Comic"));
         assertTrue(searchResults.containsValue("Buffy the Animated Series"));
     }
 
 	    @Test
     public void testSearchSpace() {
 
         assumeTrue(testInternet());
 
         Map<Integer, String> searchResults = service.search("family guy", "en");
         assertEquals(1, searchResults.size());
         assertTrue(searchResults.containsValue("Family Guy"));
     }
 
     @Test
     public void testAddShow() {
 
         assumeTrue(testInternet());
 
         service.createOrUpdate(70327, "fr");
 
         //check if show was created
         assertTrue(showService.showExists("Buffy contre les vampires"));
 
         Show show = showService.getShowByName("Buffy contre les vampires");
 
         assertEquals("The WB", show.getNetwork());
        assertEquals(new Integer(45), show.getRuntime());
         assertTrue(show.getOver());
         assertEquals("TV-PG", show.getContentRating());
         assertNotNull("Summary should not be null", show.getSummary());
         assertTrue(show.getActors().contains("Sarah Michelle Gellar"));
         assertTrue(show.getGenres().contains("Fantasy"));
         assertEquals("tt0118276", show.getImdbId());
         assertEquals("EP00213110", show.getZap2itId());
 
         //check if last season was created
         assertTrue(seasonsService.seasonExists(show, 7));
 
         Season season = seasonsService.getSeasonByNumber(show, 7);
 
         //Check if last episode was created
         assertTrue(episodesService.episodeExistsByNumber(season, 22));
         Episode episode = episodesService.getEpisodeByNumber(season, 22);
 
         //Check if last episode info are ok
         assertFalse(episode.getAcquired());
         assertFalse(episode.getViewed());
         assertEquals("La fin des temps (2)", episode.getTitle());
         assertEquals("2003-05-20 00:00:00.0", episode.getAiringDate().toString());
 
     }
 
 	/**
 	 * Actually, this test does nothing. It only checks that there is no error
 	 * thrown.
 	 */
     @Test
     public void testUpdateShows() {
         assumeTrue(testInternet());
         service.updateShows();
     }
 	
 	@Test
 	public void testUpdateShow() throws Exception{
 		assumeTrue(testInternet());
 
 		//Manually create show and assign it the Buffy Id
 		Show show = showService.createShow("Toto");
 		show.setThetvdbId(70327);
 		showService.updateShow(show);
 
 		service.createOrUpdate(70327,"fr");
 
 		//check if show was created
         assertTrue(showService.showExists("Buffy contre les vampires"));
 
         Show testedShow = showService.getShowByName("Buffy contre les vampires");
 
         assertEquals("The WB", testedShow.getNetwork());
        assertEquals(new Integer(45), testedShow.getRuntime());
         assertTrue(testedShow.getOver());
         assertEquals("TV-PG", testedShow.getContentRating());
         assertNotNull("Summary should not be null", testedShow.getSummary());
         assertTrue(testedShow.getActors().contains("Sarah Michelle Gellar"));
         assertTrue(testedShow.getGenres().contains("Fantasy"));
         assertEquals("tt0118276", testedShow.getImdbId());
         assertEquals("EP00213110", testedShow.getZap2itId());
 
         //check if last season was created
         assertTrue(seasonsService.seasonExists(testedShow, 7));
 
         Season season = seasonsService.getSeasonByNumber(testedShow, 7);
 
         //Check if last episode was created
         assertTrue(episodesService.episodeExistsByNumber(season, 22));
         Episode episode = episodesService.getEpisodeByNumber(season, 22);
 
         //Check if last episode info are ok
         assertFalse(episode.getAcquired());
         assertFalse(episode.getViewed());
         assertEquals("La fin des temps (2)", episode.getTitle());
         assertEquals("2003-05-20 00:00:00.0", episode.getAiringDate().toString());
 
 	}
     
 }
