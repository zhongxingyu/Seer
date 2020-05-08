 /*
  * ParkingIQ Project.
  *
  * (c) 2011 Akash Agrawal
  * This code may be freely used and modified for any purpose. 
  */
 package edu.colorado.piq.service.impl;
 
 import edu.colorado.piq.model.GeoPoint;
 import edu.colorado.piq.model.Zone;
 import edu.colorado.piq.service.ZoneDetectionService;
 
 /**
  * Implementation of ZoneDetectionService interface.
  */
 public class ZoneDetectionServiceImpl implements ZoneDetectionService {
 	
 	/* (non-Javadoc)
 	 * @see edu.colorado.piq.service.ZoneDetectionService#identifyZone(edu.colorado.piq.model.GeoPoint)
 	 */
 	public Zone identifyZone(GeoPoint currentCoord) {
 		String zoneId = null;
 
 		if (currentCoord.isValid()) {
            int lat = (int)(currentCoord.getLatitude()  * 1000.0);
            int lng = (int)(currentCoord.getLongitude() * 1000.0);
			zoneId = String.format("%1d_%2d", lat, lng);
 		}
 		return new Zone(zoneId); // 40001_-105263(>1) 40005_-105270(=1)
 	}
 }
