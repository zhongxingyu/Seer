 /**
  * Fahrgemeinschaft / Ridesharing App
  * Copyright (c) 2013 by it's authors.
  * Some rights reserved. See LICENSE..
  *
  */
 
 package de.fahrgemeinschaft;
 
 import java.util.Date;
 import java.util.HashMap;
 
 import junit.framework.TestCase;
 
 import org.teleportr.Place;
 import org.teleportr.Ride;
 
 public class Tests extends TestCase {
 
     private FahrgemeinschaftConnector con;
 
     @Override
     protected void setUp() throws Exception {
         con = new FahrgemeinschaftConnector();
         con.settings = new HashMap<String, String>();
         con.settings.put("radius_from", "15");
         con.settings.put("radius_to", "25");
         con.settings.put("username", "blablamail@gmx.net");
         con.settings.put("password", "blabla");
         con.endpoint =  "http://test.service.fahrgemeinschaft.de";
         super.setUp();
     }
 
     public void testAuth() {
         assertNotNull(con.getAuth());
         assertEquals("f9bb1cab-9793-8534-5d01-58c3d27d50fc", con.get("user"));
     }
 
     Place berlin = new Place(52.519171, 13.406092).address("Berlin, Deutschland");
     Place munich = new Place(48.1671, 11.6094).address("München, Deutschland");
     Place leipzig = new Place("u30u1d1g3sc").address("Leipzig, Deutschland");
     Place nürnberg = new Place("u0zck43yfyx").address("Nürnberg, Deutschland");
     
     Place stuttgart = new Place(48.775417, 9.181758).address("Stuttgart, Deutschland");
     Place muc_flughafen_nordallee = new Place(48.356820, 11.762299);
     
     public void testSearchRides() {
 //        connector.getRides(munich, berlin, new Date(), null);
         con.search(leipzig, berlin,
                 new Date(System.currentTimeMillis() + 24*3600000), null);
         con.printResults();
     }
     
     public void testPublishRide() throws Exception {
         con.publish(new Ride().from(stuttgart).to(berlin));
         // go to test.fahrgemeinschaft and assert published
     }
 
     public void testPublishSubRides() throws Exception {
         Ride offer = new Ride()
                .from(stuttgart).via(munich).to(berlin)
                 .dep(new Date());
         con.publish(offer);
         // go to test.fahrgemeinschaft and assert published
     }
 }
