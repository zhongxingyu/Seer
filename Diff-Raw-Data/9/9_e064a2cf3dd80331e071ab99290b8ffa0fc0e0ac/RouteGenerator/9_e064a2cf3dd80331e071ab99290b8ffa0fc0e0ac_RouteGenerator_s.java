 /*     Copyright (c) 2012 Johannes Wikner, Anton Lindgren, Victor Lindhe,
  *         Niklas Andreasson, John Hult
  *
  *     Licensed to the Apache Software Foundation (ASF) under one
  *     or more contributor license agreements.  See the NOTICE file
  *     distributed with this work for additional information
  *     regarding copyright ownership.  The ASF licenses this file
  *     to you under the Apache License, Version 2.0 (the
  *     "License"); you may not use this file except in compliance
  *     with the License.  You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *     Unless required by applicable law or agreed to in writing,
  *     software distributed under the License is distributed on an
  *     "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  *     KIND, either express or implied.  See the License for the
  *     specific language governing permissions and limitations
  *     under the License.
  */
 
 /**
  * RouteGenerator.java
  */
 package com.pifive.makemyrun.model;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import android.location.Location;
 
 import com.google.android.maps.GeoPoint;
 import com.pifive.makemyrun.geo.MMRLocation;
 
 /**
  *	RouteGenerator
  *	Class with static methods for route generation.
  */
 public abstract class RouteGenerator {
 	private static double micro = 1E6;
 	private static double minDistForDiffPoints = 100.0;
 	private static double oneFifth = 0.2;
 	private static int factorToWidenRoute = 10;
 	private static double minDiff = 0.0005;
	private static double maxDiff = 0.001;
 	private static double thirdOfCircle = 2.0/3.0;
 
 	/**
 	 * Private constructor to prevent from being instantiated
 	 */
 	private RouteGenerator() {
 		
 	}
 	
 	/**
 	 * Generates a route.
 	 * If distance between points < 100 meters, a circle route will be returned.
 	 * @param startLoc
 	 * @param endLoc
 	 * @return
 	 */
 	public static String generateRoute(final GeoPoint startLoc, final GeoPoint endLoc) {
 		Location sLoc = new Location("start location");
 		sLoc.setLatitude(startLoc.getLatitudeE6() / micro);
 		sLoc.setLongitude(startLoc.getLongitudeE6() / micro);
 		
 		Location eLoc = new Location("end location");
 		eLoc.setLatitude(endLoc.getLatitudeE6() / micro);
 		eLoc.setLongitude(endLoc.getLongitudeE6() / micro);
 		
		if(sLoc.distanceTo(eLoc) < 100.0) {
 			return generateCircle(new MMRLocation
 					(sLoc.getLatitude(), sLoc.getLongitude()));
 			
 		} else {
 			return generateLinear(new MMRLocation
 					(sLoc.getLatitude(), sLoc.getLongitude()),
 					new MMRLocation
 					(eLoc.getLatitude(), eLoc.getLongitude()));
 		}
 	}
 	
 	/**
 	 * Returns a string containing a google query with generated waypoints 
 	 * @param startEndLoc - Current location that the generated route will start and end at
 	 * @return a google query with which you can query google for more steps
 	 */
 	private static String generateCircle(final MMRLocation loc) {		
 		MMRLocation centerLocation = generateRandomLocation(loc);
 		return QueryGenerator.googleQuery(loc, loc, getCircle(centerLocation, loc));
 	}
 	
 	/**
 	 * Returns a string containing a google query with generated waypoints
 	 * @param aLoc Starting location
 	 * @param bLoc Finish location
 	 * @return a google query with which you can query google for more steps
 	 */
 	private static String generateLinear(final MMRLocation aLoc,
 										 final MMRLocation bLoc) {
 		
 		double longDiff = (bLoc.getLng() - aLoc.getLng());
 		double latDiff = (bLoc.getLat() - aLoc.getLat());
 		
 		List<MMRLocation> locations = 
 							new ArrayList<MMRLocation>();
 		
 		if (longDiff == 0.0) {
 			locations = generateStraightVertical(aLoc, bLoc);
 			
 		} else if (latDiff == 0.0) {
 			locations = generateStraightHorizontal(aLoc, bLoc);
 			
 		} else {
 			double northLat = aLoc.getLat() > bLoc.getLat() ? aLoc.getLat() : bLoc.getLat();
 			double southLat = aLoc.getLat() > bLoc.getLat() ? bLoc.getLat() : aLoc.getLat();
 			double westLong = aLoc.getLng() < bLoc.getLng() ? aLoc.getLng() : bLoc.getLng();
 			double eastLong = aLoc.getLng() < bLoc.getLng() ? bLoc.getLng() : aLoc.getLng();
 			
 			if ((northLat-southLat)/(eastLong-westLong) < oneFifth) {
 				locations = generateHorizontalThin(northLat, southLat, westLong, eastLong);
 				
 			} else if ((eastLong-westLong)/(northLat-southLat) < oneFifth) {
 				locations = generateVerticalThin(northLat, southLat, westLong, eastLong);
 				
 			} else {
 				locations = generateNormal(northLat, southLat, westLong, eastLong);
 				
 			}
 			
 		}
 		
 		return QueryGenerator.googleQuery(aLoc, bLoc, locations);
 		
 	}
 	
 	
 	
 	/**
 	 * Generates waypoints when the vertical sides are more than 5 times shorter than horizontal sides.
 	 * @param northLat Northest latitude
 	 * @param southLat Southest latitude
 	 * @param westLong Westest longitude
 	 * @param eastLong Eastest longitude
 	 * @return waypoints
 	 */
 	private static List<MMRLocation> generateHorizontalThin(
 			double northLat, double southLat, double westLong, double eastLong) {
 		
 		List<MMRLocation> locations = 
 				new ArrayList<MMRLocation>();
 		
 		double diff = (northLat-southLat);
 		for(int i=0; i<2; i++) {
 			double randLat = diff * (new Random()).nextDouble() + southLat + 
 					((new Random()).nextBoolean() ? -1.0 : 1.0) * factorToWidenRoute * diff;
 			double randLong = (eastLong - westLong) * (new Random()).nextDouble() + westLong;
 			locations.add(new MMRLocation(randLat, randLong));
 		}
 		
 		return locations;
 	}
 
 	/**
 	 * Generates waypoints when the horizontal sides are more than 5 times shorter than vertical sides.
 	 * @param northLat Northest latitude
 	 * @param southLat Southest latitude
 	 * @param westLong Westest longitude
 	 * @param eastLong Eastest longitude
 	 * @return waypoints
 	 */
 	private static List<MMRLocation> generateVerticalThin(
 			double northLat, double southLat, double westLong, double eastLong) {
 		
 		List<MMRLocation> locations = 
 				new ArrayList<MMRLocation>();
 		
 		double diff = (eastLong-westLong);
 		for(int i=0; i<2; i++) {
 			double randLong = diff * (new Random()).nextDouble() + westLong + 
 					((new Random()).nextBoolean() ? -1.0 : 1.0) * factorToWidenRoute * diff;
 			double randLat = (northLat - southLat) * (new Random()).nextDouble() + southLat;
 			locations.add(new MMRLocation(randLat, randLong));
 		}
 		
 		return locations;
 	}
 
 	/**
 	 * Generates waypoints when the circumstances are normal
 	 * @param northLat Northest latitude
 	 * @param southLat Southest latitude
 	 * @param westLong Westest longitude
 	 * @param eastLong Eastest longitude
 	 * @return waypoints
 	 */
 	private static List<MMRLocation> 
 						generateNormal(
 						final double northLat, final double southLat, 
 						final double westLong, final double eastLong) {
 		
 		List<MMRLocation> locations = 
 				new ArrayList<MMRLocation>();
 		
 		for(int i=0; i<2; i++) {
 			double randLat = (northLat - southLat) * (new Random()).nextDouble() + southLat;
 			double randLong = (eastLong - westLong) * (new Random()).nextDouble() + westLong;
 			locations.add(new MMRLocation(randLat, randLong));
 		}
 		
 		return locations;
 	}
 	
 	/**
 	 * Generates waypoints for situations where the longitudes are the same
 	 * @param aLoc starting location
 	 * @param bLoc finish location
 	 * @return waypoints 
 	 */
 	private static List<MMRLocation> 
 						generateStraightVertical(
 						final MMRLocation aLoc,
 			 			final MMRLocation bLoc) {
 		
 		List<MMRLocation> locations = 
 				new ArrayList<MMRLocation>();
 		
 		double southLat = aLoc.getLat() > bLoc.getLat() ? bLoc.getLat() : aLoc.getLat();
 		double westLong = aLoc.getLng();
 		double eastLong = aLoc.getLng();
 		
 		for(int i=0; i<2; i++) {
 			double randLat = ((new Random()).nextBoolean() ? -1.0 : 1.0) * minDiff 
 											* (new Random()).nextDouble() + southLat;
 			double randLong = (eastLong - westLong) * (new Random()).nextDouble() + westLong;
 			locations.add(new MMRLocation(randLat, randLong));
 		}
 		
 		return locations;
 	}
 	
 	
 	/**
 	 * Generates waypoints for situations where the latitudes are the same
 	 * @param aLoc starting location
 	 * @param bLoc finish location
 	 * @return waypoints 
 	 */
 	private static List<MMRLocation> 
 						generateStraightHorizontal(
 						final MMRLocation aLoc,
 			 			final MMRLocation bLoc) {
 		
 		List<MMRLocation> locations = 
 				new ArrayList<MMRLocation>();
 		
 		double northLat = aLoc.getLat();
 		double southLat = aLoc.getLat();
 		double westLong = aLoc.getLng() < bLoc.getLng() ? aLoc.getLng() : bLoc.getLng();
 		
 		for(int i=0; i<2; i++) {
 			double randLat = (northLat - southLat) * (new Random()).nextDouble() + southLat;
 			double randLong = ((new Random()).nextBoolean() ? -1.0 : 1.0) * minDiff 
 									            * (new Random()).nextDouble() + westLong;
 			
 			locations.add(new MMRLocation(randLat, randLong));
 		}
 		
 		return locations;
 	}
 	
 	
 	
 	
 	/**
 	 * Generates a location with coordinates 0.005 - 0.010 latitude and 
 	 * longitude from the passed location
 	 * @param location
 	 * @return
 	 */
 	private static MMRLocation generateRandomLocation(MMRLocation location) {
 		// create another location approx 0.005 - 0.010 from the current
 		Random random = new Random();
		double randomNumber = minDiff + random.nextDouble() * maxDiff;
 		double latSign = random.nextBoolean() ? 1.0 : -1.0;
 		double longSign = random.nextBoolean() ? 1.0 : -1.0;
 		
 		double centerLatitude = location.getLat() + latSign*randomNumber;
 		double centerLongitude = location.getLng() + longSign*randomNumber;
 		
 		return new MMRLocation(centerLatitude, centerLongitude);
 	}
 	
 	/**
 	 * Returns a circle-shaped run
 	 * @param center
 	 * @param start
 	 * @return
 	 */
 	private static List<MMRLocation> getCircle(MMRLocation center, 
 			MMRLocation start) {
 		
 		double angle = Math.PI * thirdOfCircle;
 		List<MMRLocation> locations = new ArrayList<MMRLocation>();
 		double longDiff = start.getLng() - center.getLng();
 		double latDiff = start.getLat() - center.getLat();
 		double radius = Math.sqrt(Math.pow(longDiff, 2) + Math.pow(latDiff, 2));
 		
 		locations.add(new MMRLocation(center.getLat() + Math.cos(angle)*radius, 
 									   center.getLng() + Math.sin(angle)*radius));
 		locations.add(new MMRLocation(center.getLat() + Math.cos(angle*2)*radius, 
 				   center.getLng() + Math.sin(angle*2)*radius));
 		
 		return locations;
 	}
 }
