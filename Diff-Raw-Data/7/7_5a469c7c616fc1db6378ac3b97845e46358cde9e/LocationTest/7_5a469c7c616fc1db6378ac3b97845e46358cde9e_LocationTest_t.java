 package org.atlasapi.media.entity.simple;
 
 import static org.junit.Assert.*;
 
 import org.joda.time.DateTime;
 import org.junit.Test;
 
 import com.metabroadcast.common.time.DateTimeZones;
 
 
 public class LocationTest {
     
     private final Location location = new Location();
 
     @Test
     public void testIsAvailableIfInAvailabilityWindow() {
         
         DateTime now = new DateTime(DateTimeZones.UTC);
         
         location.setAvailabilityStart(now.minusHours(1).toDate());
         location.setActualAvailabilityStart(null);
         location.setAvailabilityEnd(now.plusHours(1).toDate());
         
         assertTrue(location.isAvailable());
         
     }
 
     @Test
     public void testIsUnvailableIfOutsideAvailabilityWindow() {
         
         DateTime now = new DateTime(DateTimeZones.UTC);
         
         location.setAvailabilityStart(now.plusHours(1).toDate());
         location.setActualAvailabilityStart(null);
         location.setAvailabilityEnd(now.plusHours(2).toDate());
         
         assertFalse(location.isAvailable());
         
     }
 
     @Test
     public void testIsUnvailableIfOutsideActualAvailabilityWindow() {
         
         DateTime now = new DateTime(DateTimeZones.UTC);
         
         location.setAvailabilityStart(now.minusHours(1).toDate());
         location.setActualAvailabilityStart(now.plusHours(1).toDate());
         location.setAvailabilityEnd(now.plusHours(2).toDate());
         
         assertFalse(location.isAvailable());
         
     }
 
     @Test
     public void testIsAvailableIfInActualAvailabilityWindow() {
         
         DateTime now = new DateTime(DateTimeZones.UTC);
         
         location.setAvailabilityStart(now.minusHours(2).toDate());
         location.setActualAvailabilityStart(now.minusHours(1).toDate());
         location.setAvailabilityEnd(now.plusHours(2).toDate());
         
         assertTrue(location.isAvailable());
         
     }
 
     @Test
     public void testIsAvailableIfExplicitlySetAvailable() {
         
         location.setAvailable(true);
         location.setAvailabilityStart(null);
         location.setActualAvailabilityStart(null);
         location.setAvailabilityEnd(null);
         
         assertTrue(location.isAvailable());
         
     }
 
     @Test
     public void testIsUnavailableIfExplicitlySetUnavailable() {
         
         location.setAvailable(false);
         location.setAvailabilityStart(null);
         location.setActualAvailabilityStart(null);
         location.setAvailabilityEnd(null);
         
         assertFalse(location.isAvailable());
         
     }
 
     @Test
     public void testIsUnavailableIfExplicitlySetUnavailableAndInsideAvailabilityWindow() {
         
         DateTime now = new DateTime(DateTimeZones.UTC);
         
         location.setAvailable(false);
         location.setAvailabilityStart(now.minusHours(2).toDate());
         location.setActualAvailabilityStart(now.minusHours(1).toDate());
         location.setAvailabilityEnd(now.plusHours(2).toDate());
         
         assertFalse(location.isAvailable());
         
     }
 
 }
