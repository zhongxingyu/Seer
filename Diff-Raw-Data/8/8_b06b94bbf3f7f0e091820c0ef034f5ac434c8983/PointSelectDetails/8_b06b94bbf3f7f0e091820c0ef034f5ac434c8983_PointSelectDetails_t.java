 /*
  * $ Header: it.geosolutions.geogwt.gui.client.model.PointSelectDetails,v. 0.1 18-apr-2012 16.59.50 created by tobia di pisa <tobia.dipisa at geo-solutions.it> $
  * $ Revision: 0.1-SNAPSHOT $
  * $ Date: 18-apr-2012 16.59.50 $
  *
  * ====================================================================
  * GeoGWT 0.1-SNAPSHOT
  *
  * Copyright (C) 2011 GeoSolutions S.A.S.
  * http://www.geo-solutions.it
  *
  * GPLv3 + Classpath exception
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.
  *
  * ====================================================================
  *
  * This software consists of voluntary contributions made by developers
  * of GeoSolutions.  For more information on GeoSolutions, please see
  * <http://www.geo-solutions.it/>.
  *
  */
 package it.geosolutions.geogwt.gui.client.model;
 
 import com.extjs.gxt.ui.client.data.BeanModel;
 
 /**
  * The Class PointSelectDetails.
  * 
  * @author Tobia Di Pisa at tobia.dipisa@geo-solutions.it
  *
  */
 public class PointSelectDetails extends BeanModel{
 
     /** serialVersionUID */
     private static final long serialVersionUID = -3415285888475954468L;
 
     private double lon;
     
     private double lat;
     
     private int x;
     
     private int y;
     
     private double scale;
 
     /**
      * @param lon
      * @param lat
      * @param x
      * @param y
      * @param scale
      */
     public PointSelectDetails(double lon, double lat, int x, int y, double scale) {
         super();
         this.lon = lon;
         this.lat = lat;
         this.x = x;
         this.y = y;
         this.scale = scale;
     }
 
     /**
      * @return the lon
      */
     public double getLon() {
         return lon;
     }
 
     /**
      * @param lon the lon to set
      */
     public void setLon(double lon) {
         this.lon = lon;
     }
 
     /**
      * @return the lat
      */
     public double getLat() {
         return lat;
     }
 
     /**
      * @param lat the lat to set
      */
     public void setLat(double lat) {
         this.lat = lat;
     }
 
     /**
      * @return the x
      */
     public int getX() {
         return x;
     }
 
     /**
      * @param x the x to set
      */
     public void setX(int x) {
         this.x = x;
     }
 
     /**
      * @return the y
      */
     public int getY() {
         return y;
     }
 
     /**
      * @param y the y to set
      */
     public void setY(int y) {
         this.y = y;
     }
 
     /**
      * @return the scale
      */
     public double getScale() {
         return scale;
     }
 
     /**
      * @param scale the scale to set
      */
     public void setScale(double scale) {
         this.scale = scale;
     }
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		StringBuilder builder = new StringBuilder();
 		builder.append("PointSelectDetails [lon=");
 		builder.append(lon);
 		builder.append(", lat=");
 		builder.append(lat);
 		builder.append(", x=");
 		builder.append(x);
 		builder.append(", y=");
 		builder.append(y);
 		builder.append(", scale=");
 		builder.append(scale);
 		builder.append("]");
 		return builder.toString();
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		long temp;
		temp = (long) lat;
 		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = (long) lon;
 		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = (long) scale;
 		result = prime * result + (int) (temp ^ (temp >>> 32));
 		result = prime * result + x;
 		result = prime * result + y;
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 		if (!(obj instanceof PointSelectDetails)) {
 			return false;
 		}
 		PointSelectDetails other = (PointSelectDetails) obj;
 		if (Double.doubleToLongBits(lat) != Double.doubleToLongBits(other.lat)) {
 			return false;
 		}
 		if (Double.doubleToLongBits(lon) != Double.doubleToLongBits(other.lon)) {
 			return false;
 		}
 		if (Double.doubleToLongBits(scale) != Double
 				.doubleToLongBits(other.scale)) {
 			return false;
 		}
 		if (x != other.x) {
 			return false;
 		}
 		if (y != other.y) {
 			return false;
 		}
 		return true;
 	}
 
 }
