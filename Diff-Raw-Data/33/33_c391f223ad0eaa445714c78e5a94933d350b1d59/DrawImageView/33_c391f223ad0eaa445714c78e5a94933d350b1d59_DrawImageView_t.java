 /*--------------------------------------------------------------------
 
 Copyright Jonathan Cozzo and Patrick Rannou (22/03/2013)
 
 This software is an Android application whose purpose is to select 
 and characterize zones on a photography (type, material, color...).
 
 This software is governed by the CeCILL license under French law and
 abiding by the rules of distribution of free software.  You can  use, 
 modify and/ or redistribute the software under the terms of the CeCILL
 license as circulated by CEA, CNRS and INRIA at the following URL
 "http://www.cecill.info". 
 
 As a counterpart to the access to the source code and  rights to copy,
 modify and redistribute granted by the license, users are provided only
 with a limited warranty  and the software's author,  the holder of the
 economic rights,  and the successive licensors  have only  limited
 liability. 
 
 In this respect, the user's attention is drawn to the risks associated
 with loading,  using,  modifying and/or developing or reproducing the
 software by the user in light of its specific status of free software,
 that may mean  that it is complicated to manipulate,  and  that  also
 therefore means  that it is reserved for developers  and  experienced
 professionals having in-depth computer knowledge. Users are therefore
 encouraged to load and test the software's suitability as regards their
 requirements in conditions enabling the security of their systems and/or 
 data to be ensured and,  more generally, to use and operate it in the 
 same conditions as regards security. 
 
 The fact that you are presently reading this means that you have had
 knowledge of the CeCILL license and that you accept its terms.
 
 -----------------------------------------------------------------------*/
 
 package com.ecn.urbapp.utils;
 
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.ColorFilter;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.graphics.Path;
 import android.graphics.drawable.Drawable;
 
 import com.ecn.urbapp.activities.MainActivity;
 import com.ecn.urbapp.db.Element;
 import com.ecn.urbapp.db.PixelGeom;
 import com.ecn.urbapp.zones.UtilCharacteristicsZone;
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.Polygon;
 import com.vividsolutions.jts.io.ParseException;
 import com.vividsolutions.jts.io.WKTReader;
 
 /**
  * This class is used to draw the zones on the image
  * 
  * @author patrick
  * 
  */
 public class DrawImageView extends Drawable {
 
 	/**
 	 * Constructor of this class
 	 * 
 	 * @param zones
 	 *            the set of zones the need to be drawn
 	 */
 	public DrawImageView() {
 		super();
 	}
 
 	/**
 	 * This method is called to draw the zones
 	 */
 	@Override
 	public void draw(Canvas canvas) {
 
 		Paint finishedPaint = new Paint();
 		finishedPaint.setColor(Color.RED);
 		finishedPaint.setStyle(Paint.Style.STROKE);
 
 		Paint fillPaint = new Paint();
 		fillPaint.setColor(Color.RED);
 		fillPaint.setStyle(Style.FILL);
 		fillPaint.setAlpha(50);
 
 		// For all the zones
 		for (int i = 0; i < MainActivity.pixelGeom.size(); i++) {
 			// If the zone is not selected, only draw the lines
 			try {
 				Element el = null;
 				for (Element e : MainActivity.element) {
 					if (e.getPixelGeom_id() == MainActivity.pixelGeom.get(i)
 							.getPixelGeomId()) {
 						el = e;
 						break;
 					}
 				}
 				WKTReader wktr = new WKTReader();
 				for (PixelGeom pg : UtilCharacteristicsZone
								.getPixelGeomsFromGeom(wktr.read(MainActivity.pixelGeom
 								.get(i).getPixelGeom_the_geom()), false)) {
					Polygon poly = (Polygon) wktr.read(pg.getPixelGeom_the_geom());
					Coordinate[] points2 = poly.getExteriorRing().getCoordinates();
 
					if (el.getElement_color() != null
 							&& Integer.parseInt(el.getElement_color()) != 0) {
 						finishedPaint.setColor(Integer.parseInt(el
 								.getElement_color()));
 					} else {
 						finishedPaint.setColor(Color.RED);
					}
 
 					// Create a closed path for the polygon
 					Path polyPath = new Path();
 					polyPath.moveTo((int) points2[0].x, (int) points2[0].y);
 					for (int j = 0; j < points2.length; j++) {
 						polyPath.lineTo((int) points2[j].x, (int) points2[j].y);
 						if (j != points2.length - 1) {
 							canvas.drawLine((int) points2[j].x,
 									(int) points2[j].y, (int) points2[j + 1].x,
 									(int) points2[j + 1].y, finishedPaint);
 						}
 					}
 					for (int k = 0; k < poly.getNumInteriorRing(); k++) {
 						polyPath.close();
 						points2 = poly.getInteriorRingN(k).getCoordinates();
 						for (int j = 0; j < points2.length; j++) {
 							polyPath.lineTo((int) points2[j].x,
 									(int) points2[j].y);
 							if (j != points2.length - 1) {
 								canvas.drawLine((int) points2[j].x,
 										(int) points2[j].y,
 										(int) points2[j + 1].x,
 										(int) points2[j + 1].y, finishedPaint);
 							}
 						}
 					}
 
 					if (el.getElement_color() != null
 							&& Integer.parseInt(el.getElement_color()) != 0) {
 						fillPaint.setColor(Integer.parseInt(el
 								.getElement_color()));
 					} else {
 						fillPaint.setColor(Color.RED);
 					}
 
 					if (!MainActivity.pixelGeom.get(i).selected) {
 						fillPaint.setAlpha(50);
 					} else {
 						fillPaint.setAlpha(150);
 					}
 					// Draw the polygon
 					canvas.drawPath(polyPath, fillPaint);
 				}
 			} catch (ParseException e) {
 			}
 		}
 	}
 
 	@Override
 	public void setAlpha(int arg0) {
 	}
 
 	@Override
 	public void setColorFilter(ColorFilter arg0) {
 	}
 
 	// TODO Adddescription for javadoc
 	@Override
 	public int getOpacity() {
 		return 0;
 	}
 }
