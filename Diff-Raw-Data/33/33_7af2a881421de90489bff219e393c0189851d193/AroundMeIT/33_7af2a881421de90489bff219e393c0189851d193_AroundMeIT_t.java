 package com.ninedemons.leaderboard.redis.pipelined;
 
 import com.ninedemons.leaderboard.api.Entry;
 import junit.framework.Assert;
 import org.testng.annotations.*;
 import redis.clients.jedis.Jedis;
 import redis.clients.jedis.JedisPool;
 
 import java.util.List;
 
 import static junit.framework.Assert.*;
 
 /**
  * Created by Jon Barber
  */
 public class AroundMeIT {
 
     private static String HIGHEST_RANKED_USER;
     private static String LOWEST_RANKED_USER;
     private static String MID_RANKED_USER;
 
     JedisPool pool;
     String leaderboardName;
     PipelinedLeaderboard underTest;
 
     @Parameters({"redis.host", "redis.port"})
     @BeforeSuite(alwaysRun = true)
     public void setupRedis(@Optional("localhost") String hostname, @Optional("6379") int port) {
         pool = new JedisPool(hostname, port);
         checkRedisReachable();
     }
 
     @BeforeTest
     public void beforeTest() {
         underTest = new PipelinedLeaderboard();
         underTest.setJedisPool(pool);
         underTest.setPageSize(PipelinedLeaderboard.DEFAULT_PAGE_SIZE);
         underTest.setUseZeroIndexForRank(true);
         populateTestData();
     }
 
     @AfterTest
     public void afterTest() {
         rollbackRedisChanges();
     }
 
    @Test
    public void testNoSuchUser() {
        List<Entry> found = underTest.aroundMe(leaderboardName, "no_such_user");
        assertNotNull("Should have returned an empty list",found);
        assertEquals("Should have returned an empty list",0,found.size());

    }

 
     @Test
     public void testWhenPageSizeChanged() {
         int newPageSize = PipelinedLeaderboard.DEFAULT_PAGE_SIZE / 2;
         assertNotSame(newPageSize,PipelinedLeaderboard.DEFAULT_PAGE_SIZE);
 
         underTest.setPageSize(newPageSize);
         List<Entry> found = underTest.aroundMe(leaderboardName, MID_RANKED_USER);
         Assert.assertNotNull("Should have returned a list of users", found);
         assertEquals("Should have the modified page size of users", newPageSize, found.size());
         testResultsAreOrdered(found);
     }
 
     @Test
     public void testAroundMeHappyPath() {
         List<Entry> found = underTest.aroundMe(leaderboardName, MID_RANKED_USER);
         Assert.assertNotNull("Should have returned a list of users", found);
         assertEquals("Should have the full page size of users", underTest.getPageSize(), found.size());
 
         Entry middleEntry = found.get(found.size() / 2);
         assertEquals("User_50 should be the first entry", middleEntry.getUserId(), MID_RANKED_USER);
         testResultsAreOrdered(found);
     }
 
     @Test
     public void testForTopRankedUser() {
         List<Entry> found = underTest.aroundMe(leaderboardName, HIGHEST_RANKED_USER);
         assertEquals("Should have the full page size of users", underTest.getPageSize(), found.size());
 
         Entry first = found.get(0);
         assertEquals("User_0 should be the first entry", first.getUserId(), HIGHEST_RANKED_USER);
         testResultsAreOrdered(found);
     }
 
     @Test
     public void testForBottomRankedUser() {
         List<Entry> found = underTest.aroundMe(leaderboardName, LOWEST_RANKED_USER);
         assertEquals("Should have the full page size of users", underTest.getPageSize(), found.size());
 
         Entry last = found.get(found.size() - 1);
         assertEquals("User_99 should be the first entry", last.getUserId(), LOWEST_RANKED_USER);
         testResultsAreOrdered(found);
     }
 
     @Test
     public void testZeroBasedIndex() {
         underTest.setUseZeroIndexForRank(true);
         assertTrue("Zero index for rank has been enabled",underTest.isUseZeroIndexForRank());
         List<Entry> found = underTest.aroundMe(leaderboardName, HIGHEST_RANKED_USER);
         assertEquals("Should have the full page size of users", underTest.getPageSize(), found.size());
 
         Entry first = found.get(0);
         assertEquals("First user should have a rank of 0", new Long(0), first.getRank());
         testResultsAreOrdered(found);
     }
 
     @Test
     public void testOneBasedIndex() {
         underTest.setUseZeroIndexForRank(false);
         assertFalse("Zero index for rank has been disabled",underTest.isUseZeroIndexForRank());
         List<Entry> found = underTest.aroundMe(leaderboardName, HIGHEST_RANKED_USER);
         assertEquals("Should have the full page size of users", underTest.getPageSize(), found.size());
 
         Entry first = found.get(0);
         assertEquals("First user should have a rank of 1", new Long(1), first.getRank());
         testResultsAreOrdered(found);
     }
 
     public void testResultsAreOrdered(List<Entry> results) {
 
         if (results.size() == 0) {
             return;
         }
 
         Entry previousEntry = null;
         for (Entry entry : results) {
 
             if (previousEntry != null) {
 
                 assertTrue("Results should be ordered in descending order of score", !(entry.getScore().compareTo(previousEntry.getScore()) > 0));
                 assertTrue("Results should be ordered in ascending order of rank", entry.getRank().compareTo(previousEntry.getRank()) > 0);
             }
             previousEntry = entry;
         }
 
     }
 
     private void rollbackRedisChanges() {
         Jedis jedis = pool.getResource();
         jedis.del(leaderboardName);
         pool.returnResource(jedis);
     }
 
     private void populateTestData() {
         Jedis jedis = pool.getResource();
         leaderboardName = "test_leaderboard_" + Long.toString(jedis.incr("test_loaderboard_name"));
 
         for (int userNumber = 0; userNumber < 100; userNumber++) {
             int score = 1000 - userNumber;
             jedis.zadd(leaderboardName, score, "user_" + userNumber);
         }
         HIGHEST_RANKED_USER = "user_0";
         MID_RANKED_USER = "user_50";
         LOWEST_RANKED_USER = "user_99";
 
         pool.returnResource(jedis);
     }
 
     public void checkRedisReachable() {
 
         Jedis jedis = pool.getResource();
         String pong = jedis.ping();
         assertEquals("Unable to contact Redis", "PONG", pong);
         pool.returnResource(jedis);
     }
 
 }
