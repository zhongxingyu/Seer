 /*
  *      Copyright (c) 2004-2013 YAMJ Members
  *      http://code.google.com/p/moviejukebox/people/list
  *
  *      This file is part of the Yet Another Movie Jukebox (YAMJ).
  *
  *      The YAMJ is free software: you can redistribute it and/or modify
  *      it under the terms of the GNU General Public License as published by
  *      the Free Software Foundation, either version 3 of the License, or
  *      any later version.
  *
  *      YAMJ is distributed in the hope that it will be useful,
  *      but WITHOUT ANY WARRANTY; without even the implied warranty of
  *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *      GNU General Public License for more details.
  *
  *      You should have received a copy of the GNU General Public License
  *      along with the YAMJ.  If not, see <http://www.gnu.org/licenses/>.
  *
  *      Web: http://code.google.com/p/moviejukebox/
  *
  */
 package com.moviejukebox.allocine;
 
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class JSONAllocineAPIHelperTest {
 
     private static final Logger LOG = LoggerFactory.getLogger(JSONAllocineAPIHelperTest.class);
     private static AllocineAPIHelper api;
     private static final String PARTNER_KEY = "100043982026";
     private static final String SECRET_KEY = "29d185d98c984a359e6e6f26a0474269";
 
     @BeforeClass
     public static void setUpClass() throws Exception {
         api = new JSONAllocineAPIHelper(PARTNER_KEY, SECRET_KEY);
         TestLogger.Configure();
     }
 
     @Before
     public void setUp() {
     }
 
     @Test
     public void testSearchMovieInfos() throws Exception {
         LOG.info("testSearchMovieInfos");
         Search search = api.searchMovieInfos("avatar");
         assertEquals(10, search.getMovie().size());
     }
 
     @Test
     public void testSearchTvseriesInfos() throws Exception {
         LOG.info("testSearchTvseriesInfos");
         Search search = api.searchTvseriesInfos("glee");
         assertEquals(1, search.getTvseries().size());
     }
 
     @Test
     public void testGetMovieInfos() throws Exception {
         LOG.info("testGetMovieInfos");
         MovieInfos movieInfos = api.getMovieInfos("61282");
         assertEquals(61282, movieInfos.getCode());
         assertEquals("Avatar", movieInfos.getTitle());
         assertEquals("Avatar", movieInfos.getOriginalTitle());
         assertEquals("2009", movieInfos.getProductionYear());
         assertEquals(9720, movieInfos.getRuntime());
         assertNotNull(movieInfos.getRelease());
         assertEquals("2009-12-16", movieInfos.getRelease().getReleaseDate());
         assertNotNull(movieInfos.getRelease().getDistributor());
         assertEquals("Twentieth Century Fox France", movieInfos.getRelease().getDistributor().getName());
         assertNotNull(movieInfos.getSynopsis());
         assertEquals(2, movieInfos.getGenreList().size());
         assertEquals(1, movieInfos.getNationalityList().size());
         assertEquals(1, movieInfos.getDirectors().size());
         assertEquals(1, movieInfos.getWriters().size());
         assertEquals(42, movieInfos.getActors().size());
         assertEquals(83, movieInfos.getRating());
     }
 
     @Test
     public void testGetTvSeriesInfos() throws Exception {
         LOG.info("testGetTvSeriesInfos");
         TvSeriesInfos tvseriesInfos = api.getTvSeriesInfos("132");
         assertEquals(132, tvseriesInfos.getCode());
         assertEquals("Mon oncle Charlie", tvseriesInfos.getTitle());
         assertEquals("Two and a Half Men", tvseriesInfos.getOriginalTitle());
         assertEquals("2003", tvseriesInfos.getYearStart());
         assertNull(tvseriesInfos.getYearEnd());
         assertEquals("CBS", tvseriesInfos.getOriginalChannel());
         assertNull(tvseriesInfos.getRelease());
         assertNotNull(tvseriesInfos.getSynopsis());
         assertEquals(1, tvseriesInfos.getGenreList().size());
         assertEquals(1, tvseriesInfos.getNationalityList().size());
 //        assertEquals(1, tvseriesInfos.getDirectors().size());
 //        assertEquals(6, tvseriesInfos.getWriters().size());
        assertTrue(tvseriesInfos.getActors().size() >= 5);
         assertEquals(-1, tvseriesInfos.getRating());
        assertTrue(tvseriesInfos.getSeasonCount() > 10);
        assertTrue(tvseriesInfos.getSeasonList().size() > 10);
     }
 
     @Test
     public void testGetTvSeasonInfos() throws Exception {
         LOG.info("testGetTvSeasonInfos");
         TvSeasonInfos tvseasonInfos = api.getTvSeasonInfos(20976);
         assertEquals(20976, tvseasonInfos.getCode());
         assertEquals(10, tvseasonInfos.getSeasonNumber());
         assertEquals("2012", tvseasonInfos.getYearStart());
         assertEquals("2013", tvseasonInfos.getYearEnd());
         assertTrue(tvseasonInfos.getEpisodeList().size() > 19);
     }
 }
