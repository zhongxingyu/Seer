 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package model;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Ignore;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import java.util.Calendar;
 
 /**
  *
  * @author liufeng
  */
 public class SongSchedulerTest {
 
     public SongSchedulerTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
 
     @Before
     public void setUp() {
     }
 
     @After
     public void tearDown() {
     }
 
     /**
      * Test of generateOneHour method, of class SongScheduler.
      */
     @Test
    @Ignore
     public void testGenerateOneHour() {
         System.out.println("generateOneHour");
         Time startTime = new Time();
         SongScheduler instance = new SongScheduler(startTime);
         instance.generateOneHour(startTime);
         Schedule result = instance.getSchedule(startTime);
 
         // check the generated schedule is longer than 43 minutes
         assertTrue(result.getDuration() >= 2580000);
         // check the generated schedule is shorter than 48 minutes
         assertTrue(result.getDuration() <= 2880000);
     }
 
     /**
      * Tests that updating of song popularity works.
      */
     @Test
    @Ignore
     public void testUpdatePopularity(){
         double popChange = doUpdatePopularityTest(2, 3);
         assertTrue(popChange == 40);
 
         popChange = doUpdatePopularityTest(0, 1);
         assertTrue(popChange > -.1 && popChange < 0);
 
         popChange = doUpdatePopularityTest(1, 2);
         assertTrue(popChange == 30);
     }
 
     /**
      * Performs a series of requests on a test song in a set period of time,
      * return the change in test song's popularity.
      * @param timesToRequest
      * @param secondsToWait
      * @return
      */
     private double doUpdatePopularityTest(int timesToRequest, int secondsToWait){
         long startTime = Calendar.getInstance().getTimeInMillis();
        
        Song testSong = new Song("asdf", "a performer", "some title", "LP", "2009", 25832, 25);
         double startPop = testSong.getPopularity();
 
         for(int i=0; i<timesToRequest; i++){
             testSong.makeRequest();
         }
 
         System.out.print("Waiting " + secondsToWait + " seconds...");
         while(Calendar.getInstance().getTimeInMillis() < startTime + secondsToWait*1000){
         }
         System.out.print("Done.\n");
 
         testSong.updatePopularity();
 
         return testSong.getPopularity() - startPop;
     }
 
     /**
      * Test of generateMultipleHours method, of class SongScheduler.
      *
      * still has problem since we generate one-hour-schedules not a whole one.
      */
     @Test
     @Ignore
     public void testGenerateMultipleHours() {
         System.out.println("generateMultipleHours");
         Time startTime = new Time();
         SongScheduler instance = new SongScheduler(startTime);
         Schedule result;
 
         for (int hours = 0; hours < 24; hours++) {
             instance.generateMultipleHours(startTime, hours);
             result = instance.getSchedule(startTime);
 
             int minLength = hours * 43 * 60 * 1000;
             int maxLength = hours * 48 * 60 * 1000;
             System.out.println(result.getDuration());
             assertTrue(result.getDuration() >= minLength);
             assertTrue(result.getDuration() <= maxLength);
         }
     }
     
     /**
      * Test of generateDays method, of class SongScheduler.
      *
      * still has problem since we generate one-hour-schedules not a whole one.
      */
     @Test
     @Ignore
     public void testGenerateDays() {
         System.out.println("generateDays");
         Time startTime = new Time(2009, 6, 20, 0, 0, 0);
         SongScheduler instance = new SongScheduler(startTime);
         Schedule result;
 
         for (int days = 0; days < 7; days++) {
             instance.generateDays(startTime, days);
             result = instance.getSchedule(startTime);
 
             int minLength = days * 24 * 43 * 60 * 1000;
             int maxLength = days * 24 * 48 * 60 * 1000;
             assertTrue(result.getDuration() >= minLength);
             assertTrue(result.getDuration() <= maxLength);
         }
     }
 }
