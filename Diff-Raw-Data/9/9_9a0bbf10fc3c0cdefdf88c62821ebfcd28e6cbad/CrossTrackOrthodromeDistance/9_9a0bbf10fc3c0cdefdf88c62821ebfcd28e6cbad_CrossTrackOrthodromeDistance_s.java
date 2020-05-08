 /*
  * Adapted from <a
  * href="http://www.movable-type.co.uk/scripts/latlong.html">JavaScript
  * version</a> created by <a href="http://www.movable-type.co.uk/">Chris
  * Veness<a/> under the <a
  * href="http://creativecommons.org/licenses/by/3.0/">Create Commons Attribution
  * 3.0</a> licencse.
  */
 package com.oschrenk.humangeo.calc;
 
 import com.oschrenk.humangeo.api.Distance;
 import com.oschrenk.humangeo.core.Segment;
 import com.oschrenk.humangeo.core.Sphere;
 import com.oschrenk.humangeo.cs.Geographic2dCoordinate;
 
 /**
  * Distance of a point to a line on a great-circle path
  * 
  * @author Oliver Schrenk <oliver.schrenk@gmail.com>
  */
 public class CrossTrackOrthodromeDistance implements
 		Distance<Geographic2dCoordinate, Segment<Geographic2dCoordinate>> {
 
 	private final Sphere sphere;
 
 	public CrossTrackOrthodromeDistance(final Sphere sphere) {
 		super();
 		this.sphere = sphere;
 	}
 
 	/*
 	 * @see com.oschrenk.humangeo.api.Distance#distance(java.lang.Object,
 	 * java.lang.Object)
 	 */
 	@Override
 	public double distance(final Geographic2dCoordinate point,
 			final Segment<Geographic2dCoordinate> segment) {
 
 		// dXt = Math.asin(Math.sin(d13/r)*Math.sin(b13-b12)) * r;
 
 		final double r = sphere.getRadius();
 		final double b12 = new OrthodromeBearing().bearing(segment.getFrom(),
 				segment.getTo());
 		final double b13 = new OrthodromeBearing().bearing(segment.getFrom(),
 				point);
 		final double d13 = new HaversineDistance(sphere).distance(
 				segment.getFrom(), point);
 
 		// @formatter:off
 		final double dt = //
 		Math.asin( //
		Math.sin(Math.toRadians(d13 / r)) //
 				* Math.sin(Math.toRadians(b13 - b12)) //
 		) * r; //
 		// @formatter:on
 
 		return dt;
 	}
 }
