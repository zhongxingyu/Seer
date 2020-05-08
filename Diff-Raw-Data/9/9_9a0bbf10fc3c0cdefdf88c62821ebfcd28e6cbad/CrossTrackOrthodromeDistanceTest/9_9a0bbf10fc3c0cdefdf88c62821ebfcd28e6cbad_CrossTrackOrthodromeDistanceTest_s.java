 package com.oschrenk.humangeo.calc;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 import com.oschrenk.humangeo.core.Segment;
 import com.oschrenk.humangeo.cs.Geographic2dCoordinate;
 import com.oschrenk.humangeo.ref.Spheres;
 
 /**
  * 
  * @author Oliver Schrenk <oliver.schrenk@gmail.com>
  */
 public class CrossTrackOrthodromeDistanceTest {
 
 	@Test
 	public void testPointOnLine() {
 		final Geographic2dCoordinate from = new Geographic2dCoordinate(0, 0);
 		final Geographic2dCoordinate to = new Geographic2dCoordinate(90, 0);
 		final Geographic2dCoordinate point = new Geographic2dCoordinate(45, 0);
 		final double distance = new CrossTrackOrthodromeDistance(Spheres.EARTH)
 				.distance(point, new Segment<Geographic2dCoordinate>(from, to));
 
 		assertEquals(0.0, distance, 0d);
 	}
 
 	@Test
 	public void testPointOnEquator() {
 		final Geographic2dCoordinate from = new Geographic2dCoordinate(0, 0);
 		final Geographic2dCoordinate to = new Geographic2dCoordinate(90, 0);
 		final Geographic2dCoordinate point = new Geographic2dCoordinate(0, 45);
 		final double distance = new CrossTrackOrthodromeDistance(Spheres.EARTH)
 				.distance(point, new Segment<Geographic2dCoordinate>(from, to));
 
 		final double circumferenceEarth = 2 * Math.PI
 				* Spheres.EARTH.getRadius();
 
		assertEquals(circumferenceEarth / 8, distance, 0d);
 	}
 }
