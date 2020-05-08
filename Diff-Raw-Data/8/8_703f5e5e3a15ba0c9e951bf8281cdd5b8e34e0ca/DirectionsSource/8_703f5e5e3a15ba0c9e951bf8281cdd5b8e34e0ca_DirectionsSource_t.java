 /**
  * @author David S Anderson
  *
  *
  * Copyright (C) 2012 David S Anderson
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.dsanderson.xctrailreport;
 
 import org.dsanderson.xctrailreport.core.IAbstractFactory;
 import org.dsanderson.xctrailreport.core.IDirectionsSource;
 import org.dsanderson.xctrailreport.core.INetConnection;
 import org.json.JSONObject;
 
 /**
  * 
  */
 public class DirectionsSource implements IDirectionsSource {
 	IAbstractFactory factory;
 
 	double distance = 0;
 	double duration = 0;
 
 	public DirectionsSource(IAbstractFactory factory) {
 		this.factory = factory;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.dsanderson.xctrailreport.core.IDirectionsSource#updateDirections(
 	 * java.lang.String, java.lang.String)
 	 */
 	public boolean updateDirections(String src, String dest) {
 
 		if (src.length() == 0 || dest.length() == 0)
 			return false;
 
 		boolean successful = false;
 
 		// / example url:
 		// https://maps.googleapis.com/maps/api/directions/json?origin=44.972691,-93.232541&destination=45.1335,-93.441&sensor=false
 		// TODO Auto-generated method stub
 		String url = "https://maps.googleapis.com/maps/api/directions/json?origin="
 				+ src + "&destination=" + dest + "&sensor=false";
 
 		INetConnection netConnection = factory.getNetConnection();
 
 		if (netConnection.connect(url)) {
 			String directionsString = netConnection.getString();
 
 			try {
 				JSONObject json = new JSONObject(directionsString);
 
 				JSONObject routesObject = json.getJSONArray("routes")
 						.getJSONObject(0);
 
 				JSONObject legsObject = routesObject.getJSONArray("legs")
 						.getJSONObject(0);
 
 				JSONObject distanceObject = legsObject
 						.getJSONObject("distance");
 				int distanceMeters = distanceObject.getInt("value");
 
 				distance = (double) distanceMeters / 1609.344;
 
 				JSONObject durationObject = legsObject
 						.getJSONObject("duration");
 				int durationSeconds = durationObject.getInt("value");
 				duration = (double) durationSeconds / 60;
 				successful = true;
 
 			} catch (Exception e) {
				System.err.println(e);
 				successful = false;
 			}
 
 		}
 
 		return successful;
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.dsanderson.xctrailreport.core.IDirectionsSource#getDistance()
 	 */
 	public double getDistance() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.dsanderson.xctrailreport.core.IDirectionsSource#getDriveTime()
 	 */
 	public double getDriveTime() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 }
