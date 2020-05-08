 /**
  * This file is part of Plingnote.
  * Copyright (C) 2012 Barnabas sapan
  * 
  * Plingnote is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.plingnote;
 
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Point;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 
 /**
  * Displays an accuracy circle as an overlay in a map.
  * @author Barnabas Sapan
  */
 public class MapOverlayGPSAccuracy extends Overlay implements UpdatableOverlay {
 	private GeoPoint point;
 	private Paint outerCircle, innerCircle;
 	private float radius = 0;
 
 	public MapOverlayGPSAccuracy(GeoPoint point, android.location.Location location) {
 		this.point = point;
 		if(location != null)
 			this.radius = location.getAccuracy();
 	

 		this.outerCircle = new Paint();
 		this.outerCircle.setARGB(20, 0, 0, 255);
 		this.outerCircle.setStrokeWidth(2);
 		this.outerCircle.setStrokeCap(Paint.Cap.ROUND);
 		this.outerCircle.setAntiAlias(true);
 		this.outerCircle.setDither(false);
 		this.outerCircle.setStyle(Paint.Style.STROKE);
 
 		this.innerCircle = new Paint();
 		this.innerCircle.setARGB(5, 0, 0, 255);  
 	}
 	
 	@Override
 	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
 		if(radius != 0) {
 			Point pt = mapView.getProjection().toPixels(this.point, null);
 			float projectedRadius = mapView.getProjection().metersToEquatorPixels(this.radius);
 	
 			canvas.drawCircle(pt.x, pt.y, projectedRadius, this.innerCircle);
 			canvas.drawCircle(pt.x, pt.y, projectedRadius, this.outerCircle);
 		}
 	}
 
 	public void update(android.location.Location location) {
 		this.radius = location.getAccuracy();
 	}
 }
