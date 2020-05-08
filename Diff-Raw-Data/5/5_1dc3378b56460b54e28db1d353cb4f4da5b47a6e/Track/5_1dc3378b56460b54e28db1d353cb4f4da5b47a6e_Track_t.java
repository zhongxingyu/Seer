 /*
  * Androzic - android navigation client that uses OziExplorer maps (ozf2, ozfx3).
  * Copyright (C) 2010-2013 Andrey Novikov <http://andreynovikov.info/>
  * 
  * This file is part of Androzic application.
  * 
  * Androzic is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Androzic is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Androzic. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.androzic.data;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.androzic.util.Geo;
 
 public class Track
 {
 	public String name;
 	public String description;
 	public boolean show;
 	public int color = -1;
 	public int width = 0;
 
 	public long maxPoints = 0;
 	public double distance;
 	public String filepath;
 	public boolean removed = false;
 	public boolean editing = false;
 	public int editingPos = -1;
 
 	private final List<TrackPoint> trackpoints = new ArrayList<TrackPoint>(0);
 	private TrackPoint lastTrackPoint;
 
 	public class TrackPoint
 	{
 		public boolean continous;
 		public double latitude;
 		public double longitude;
 		public double elevation;
 		public double speed;
 		public double bearing;
 		public double accuracy;
 		public long time;
 		// Map position cache fields
 		public boolean dirty = true;
 		public int x = 0;
 		public int y = 0;
 
 		public TrackPoint()
 		{
 			continous = false;
 			latitude = 0;
 			longitude = 0;
 			elevation = 0;
 			speed = 0;
 			bearing = 0;
 			accuracy = Double.MAX_VALUE;
 			time = 0;
 		}
 
 		public TrackPoint(boolean cont, double lat, double lon, double elev, double spd, double brn, double acc, long t)
 		{
 			continous = cont;
 			latitude = lat;
 			longitude = lon;
 			elevation = elev;
 			speed = spd;
 			bearing = brn;
 			accuracy = acc;
 			time = t;
 		}
 	};
 
 	public Track()
 	{
 		this("", "", false);
 	}
 
 	public Track(String pname, String pdescr, boolean pshow)
 	{
 		name = pname;
 		description = pdescr;
 		show = pshow;
 		distance = 0;
 	}
 
 	public Track(String pname, String pdescr, boolean pshow, long max)
 	{
 		this(pname, pdescr, pshow);
 		maxPoints = max;
 	}
 
 	public List<TrackPoint> getPoints()
 	{
 		return trackpoints;
 	}
 
 	public void addPoint(boolean continous, double lat, double lon, double elev, double speed, double bearing, double accuracy, long time)
 	{
 		if (lastTrackPoint != null)
 		{
 			distance += Geo.distance(lastTrackPoint.latitude, lastTrackPoint.longitude, lat, lon);
 		}
 		lastTrackPoint = new TrackPoint(continous, lat, lon, elev, speed, bearing, accuracy, time);
 		synchronized (trackpoints)
 		{
 			if (maxPoints > 0 && trackpoints.size() > maxPoints)
 			{
 				// TODO add correct cleaning if preferences changed
 				TrackPoint fp = trackpoints.get(0);
 				TrackPoint sp = trackpoints.get(1);
 				distance -= Geo.distance(fp.latitude, fp.longitude, sp.latitude, sp.longitude);
 				trackpoints.remove(0);
 			}
 			trackpoints.add(lastTrackPoint);
 		}
 	}
 
 	public void clear()
 	{
 		synchronized (trackpoints)
 		{
 			trackpoints.clear();
 		}
 		lastTrackPoint = null;
 		distance = 0;
 	}
 
 	public TrackPoint getPoint(int location) throws IndexOutOfBoundsException
 	{
 		return trackpoints.get(location);
 	}
 
 	public TrackPoint getLastPoint()
 	{
 		return lastTrackPoint;
 	}
 	
 	public void removePoint(int location) throws IndexOutOfBoundsException
 	{
 		synchronized (trackpoints)
 		{
 			boolean last = location == trackpoints.size() - 1;
 			TrackPoint pp = trackpoints.get(location - 1);
 			TrackPoint cp = trackpoints.get(location);
 			distance -= Geo.distance(pp.latitude, pp.longitude, cp.latitude, cp.longitude);
 			if (! last)
 			{
 				TrackPoint np = trackpoints.get(location + 1);
 				distance -= Geo.distance(cp.latitude, cp.longitude, np.latitude, np.longitude);
 				distance += Geo.distance(pp.latitude, pp.longitude, np.latitude, np.longitude);
 			}
 			trackpoints.remove(location);
 			if (last)
 				lastTrackPoint = pp;
 		}
 	}
 
 	public void cutAfter(int location)
 	{
 		synchronized (trackpoints)
 		{
 			List<TrackPoint> tps = new ArrayList<TrackPoint>(trackpoints.subList(0, location + 1));
 			trackpoints.clear();
 			trackpoints.addAll(tps);
			if (trackpoints.size() > 0)
				lastTrackPoint = trackpoints.get(trackpoints.size() - 1);
			else
				lastTrackPoint = null;
 		}
 	}
 
 	public void cutBefore(int location)
 	{
 		synchronized (trackpoints)
 		{
 			List<TrackPoint> tps = new ArrayList<TrackPoint>(trackpoints.subList(location, trackpoints.size()));
 			trackpoints.clear();
 			trackpoints.addAll(tps);
 		}
 	}
 
 }
