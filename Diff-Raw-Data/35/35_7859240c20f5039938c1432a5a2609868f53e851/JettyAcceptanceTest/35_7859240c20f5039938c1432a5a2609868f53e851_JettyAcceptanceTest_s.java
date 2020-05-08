 package com.wideplay.warp.widgets.acceptance;
 
 import com.wideplay.warp.widgets.acceptance.util.Jetty;
 import org.testng.annotations.AfterSuite;
 import org.testng.annotations.BeforeSuite;
 
 /**
  * @author Tom Wilson (tom@tomwilson.name)
  */
 public class JettyAcceptanceTest {
     private Jetty server;
 
     public JettyAcceptanceTest() {
         server = new Jetty();
     }
 
     @BeforeSuite
     public void start() throws Exception {
         server.start();
     }
 
     @AfterSuite
     public void stop() throws Exception {
         server.stop();
     }
 }
