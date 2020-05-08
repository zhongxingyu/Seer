 /*
  * $Id$
  * --------------------------------------------------------------------------------------
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 package org.mule.example.gpswalker;
 
 import org.mule.api.transformer.TransformerException;
 import org.mule.ibeans.api.application.Receive;
 import org.mule.ibeans.api.application.Schedule;
 import org.mule.ibeans.api.application.Send;
 
 import java.util.HashMap;
 import java.util.Map;
 
 
 /**
  * Generates a random walk around a city
  */
 public class CityStroller
 {
 
     public static final GpsCoord SAN_FRANCISCO = new GpsCoord(37.789167f, -122.419281f);
     public static final GpsCoord LONDON = new GpsCoord(51.515259f, -0.11776f);
     public static final GpsCoord VALLETTA = new GpsCoord(35.897655f, 14.511631f);
 
 
     private volatile GpsCoord currentCoord = LONDON;
     private volatile boolean firstTime = true;
 
     @Schedule(interval = 3000, startDelay = 2000)
     @Send(uri = "ajax:///ibeans/services/gps")
     public Map generateNextCoord() throws TransformerException
     {
         if (firstTime)
         {
             firstTime = false;
         }
         else
         {
             //could use a better algorithm here or real test data for better results            
             double dist = Math.random() * 0.002;
             double angle = Math.random() * Math.PI;
             float lat = currentCoord.getLatitude() + (float) (dist * Math.sin(angle));
             float lng = currentCoord.getLongitude() + (float) (dist * Math.cos(angle));
 
             currentCoord = new GpsCoord(lat, lng);
         }
         Map m = new HashMap(2);
         m.put("latitude", currentCoord.getLatitude());
         m.put("longitude", currentCoord.getLongitude());
 
         return m;
     }
 
     @Receive(uri = "ajax:///ibeans/services/gps-city")
     public synchronized void changeCity(String city)
     {
         if (city.equalsIgnoreCase("London"))
         {
             setCurrentCoord(LONDON);
         }
         else if (city.equalsIgnoreCase("Valletta"))
         {
             setCurrentCoord(VALLETTA);
         }
         else
         {
             setCurrentCoord(SAN_FRANCISCO);
         }
     }
 
     public GpsCoord getCurrentCoord()
     {
         return currentCoord;
     }
 
     public void setCurrentCoord(GpsCoord currentCoord)
     {
         this.currentCoord = currentCoord;
         firstTime = true;
     }
 }
