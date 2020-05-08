 package org.hackystat.dailyprojectdata.test;
 
 import org.hackystat.dailyprojectdata.server.Server;
 import org.junit.BeforeClass;
 
 /**
  * Provides a helper class to facilitate JUnit testing. 
  * @author Philip Johnson
  */
 public class DailyProjectDataTestHelper {
 
  /** The Sensorbase server used in these tests. */
   private static org.hackystat.sensorbase.server.Server sensorbaseServer;
   private static Server server;
 
   /**
    * Constructor.
    */
   public DailyProjectDataTestHelper () {
     // Does nothing.
   }
   
   /**
    * Starts the server going for these tests. 
    * @throws Exception If problems occur setting up the server. 
    */
   @BeforeClass public static void setupServer() throws Exception {
     // Create a testing version of the Sensorbase.
     DailyProjectDataTestHelper.sensorbaseServer = 
      org.hackystat.sensorbase.server.Server.newTestInstance();
    DailyProjectDataTestHelper.server = Server.newTestInstance();
   }
 
   /**
    * Returns the hostname associated with this DPD test server. 
    * @return The host name, including the context root. 
    */
   protected String getDailyProjectDataHostName() {
     return DailyProjectDataTestHelper.server.getHostName();
   }
   
   /**
    * Returns the sensorbase hostname that this DPD server communicates with.
    * @return The host name, including the context root. 
    */
   protected String getSensorBaseHostName() {
     return DailyProjectDataTestHelper.sensorbaseServer
     .getServerProperties().getFullHost();
   }
 }
 
