 package com.golddigger.model;
 /**
  * Simple 2 Dimensional Vector class
  * Mainly used when both x and y coordinates are needed to be return.
  * @author Brett Wandel
  *
  */
 public class Point2D {
 		public int lat, lng;
 		public Point2D(int lat, int lng){
 			this.lat = lat;
 			this.lng = lng;
 		}
 		
 		@Override
 		public boolean equals(Object object){
 			if (object instanceof Point2D) {
 				Point2D tmp = (Point2D) object;
 				return this.lat == tmp.lat && this.lng == tmp.lng;
 			} else {
 				return false;
 			}
 		}
 		
 		@Override
 		public String toString(){
 			return "("+lat+","+lng+")";
 		}
 
 		public Point2D add(Point2D p) {
 			return add(p.lat, p.lng);
 		}
 		
 		public Point2D add(int lat, int lng){
 			return new Point2D(this.lat + lat, this.lng + lng);
 		}
 
		public Point2D sub(Point2D p) {
			return add(-p.lat, -p.lng);
 		}
 		
 		public Point2D sub(int lat, int lng){
 			return add(-lat,-lng);
 		}
 		
 		public Point2D inverse(){
 			return new Point2D(-lat, -lng);
 		}
 
 		public int getX() {
 			return lat;
 		}
 		public int getY() {
 			return lng;
 		}
	}
