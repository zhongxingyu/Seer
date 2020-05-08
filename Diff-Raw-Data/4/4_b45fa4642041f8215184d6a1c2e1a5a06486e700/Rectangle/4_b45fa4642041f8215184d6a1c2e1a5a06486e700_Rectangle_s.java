 package net.novacodex.hibernate.search.spatial;
 
import sun.plugin.dom.css.Rect;

import java.lang.ref.Reference;

 public class Rectangle {
 	private Point lowerLeft;
 	private Point upperRight;
 
 	public void setUpperRight( Point upperRight ) {
 		this.upperRight = upperRight;
 	}
 
 	public void setLowerLeft( Point lowerLeft ) {
 		this.lowerLeft = lowerLeft;
 	}
 
 	public Point getLowerLeft() {
 		return lowerLeft;
 	}
 
 	public Point getUpperRight() {
 		return upperRight;
 	}
 
 	public Rectangle() {
 		this.setLowerLeft(new Point());
 		this.setUpperRight(new Point());
 	}
 
 	public Rectangle(Point lowerLeft, Point upperRight) {
 		this.setLowerLeft(lowerLeft);
 		this.setUpperRight(upperRight);
 	}
 
 	public Rectangle(Rectangle rectangle) {
 		this.setLowerLeft(rectangle.getLowerLeft());
 		this.setUpperRight(rectangle.getUpperRight());
 	}
 
 	public Rectangle(Point center,double radius) {
 		double minimumLatitude, maximumLatitude;
 		double minimumLongitude, maximumLongitude;
 
 		if(radius > center.distanceToPoint(new Point(GeometricConstants.LATITUDE_DEGREE_MAX,0))) {
 			maximumLatitude= GeometricConstants.LATITUDE_DEGREE_MAX;
 		} else {
 			maximumLatitude= center.computeDestination(radius,GeometricConstants.HEADING_NORTH).getLatitude();
 		}
 
 		if(radius > center.distanceToPoint(new Point(GeometricConstants.LATITUDE_DEGREE_MIN,0))) {
 			minimumLatitude= GeometricConstants.LATITUDE_DEGREE_MIN;
 		} else {
 			minimumLatitude= center.computeDestination(radius,GeometricConstants.HEADING_SOUTH).getLatitude();
 		}
 
 		if((radius > 2 * Math.PI * GeometricConstants.EARTH_MEAN_RADIUS_KM * Math.cos(Math.toRadians(minimumLatitude))) ||
        (radius > 2 * Math.PI * GeometricConstants.EARTH_MEAN_RADIUS_KM * Math.cos(Math.toRadians(maximumLatitude)))) {
 			maximumLongitude = GeometricConstants.LONGITUDE_DEGREE_MAX;
 			minimumLongitude = GeometricConstants.LONGITUDE_DEGREE_MIN;
 		} else {
 			Point referencePoint= new Point(Math.max(Math.abs(minimumLatitude), Math.abs(maximumLatitude)), center.getLongitude());
 			maximumLongitude = referencePoint.computeDestination( radius, GeometricConstants.HEADING_EAST ).getLongitude();
 			minimumLongitude = referencePoint.computeDestination( radius, GeometricConstants.HEADING_WEST ).getLongitude();
 		}
 
 		this.setLowerLeft(new Point(minimumLatitude,minimumLongitude));
 		this.setUpperRight(new Point(maximumLatitude,maximumLongitude));
 	}
 }
