 /*
  * Created on 16-Oct-2004
  *
  */
 package org.tom.weather.comm;
 
 import java.io.IOException;
 
 import org.apache.log4j.Logger;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.FileSystemXmlApplicationContext;
 
 public class Main {
   private static final int MAX_ERRORS = 20;
   public static final Logger LOGGER = Logger.getLogger(Main.class);
   private Station station;
   private boolean checkArchive;
 
   /**
    * Main method
    * 
    * @param args
    * @since 1.0
    */
   public static void main(String[] args) {
 	int errorCount = 0;
     while (true) {
       try {
         ApplicationContext factory = new FileSystemXmlApplicationContext(
             "weather.xml");
         Main main = (Main) factory.getBean("weatherMonitor");
         main.monitorWeather();
       } catch (Exception e) {
         LOGGER.error(e);
           try {
             if (errorCount++ > MAX_ERRORS) {
               System.exit(1);
             }
             Thread.sleep(5000);
           } catch (InterruptedException ex) {
             LOGGER.error(ex);
           }
       }
     }
   }
 
   public void monitorWeather() throws Exception {
	int errorCount = 0;
     try {
         getStation().test();
     } catch (IOException ex) {
         LOGGER.error(ex);
     }
     while (true) {
       try {
         for (int i = 0; i < 15; i++) {
           getStation().readCurrentConditions();
         }
         if (isCheckArchive()) {
           getStation().readArchiveMemory();
         }
       } catch (Exception e) {
         LOGGER.error("exception - waiting 5s", e);
         try {
           Thread.sleep(5000);
           boolean ok = getStation().test();
           // show results of station test after the wait
           LOGGER.warn("station test:" + (ok ? "ok" : "not ok"));
         } catch (Exception e1) {
          if (errorCount++ > MAX_ERRORS) {
            System.exit(1);
          }
           LOGGER.error(e1);
           throw e1;
         }
       }
     }
   }
 
   public void setStation(Station station) {
     this.station = station;
   }
 
   public Station getStation() {
     return station;
   }
 
   public boolean isCheckArchive() {
       return checkArchive;
   }
 
   public void setCheckArchive(boolean checkArchive) {
       this.checkArchive = checkArchive;
   }
 }
