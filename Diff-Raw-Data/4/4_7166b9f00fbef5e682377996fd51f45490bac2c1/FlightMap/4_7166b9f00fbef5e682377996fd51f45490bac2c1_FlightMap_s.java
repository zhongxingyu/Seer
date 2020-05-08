 /*
  TSAFE Prototype: A decision support tool for air traffic controllers
  Copyright (C) 2003  Gregory D. Dennis
 
  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation; either version 2
  of the License, or (at your option) any later version.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 
 package tsafe.client.graphical_client;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.image.ImageObserver;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Vector;
 
 import javax.swing.JComponent;
 
 import tsafe.common_datastructures.Fix;
 import tsafe.common_datastructures.Flight;
 import tsafe.common_datastructures.LatLonBounds;
 import tsafe.common_datastructures.Point4D;
 import tsafe.common_datastructures.Route;
 import tsafe.common_datastructures.Trajectory;
 
 /**
  * Shows the map, flights, fixes, routes, etc
  */
 class FlightMap extends JComponent implements ImageObserver {
     
 	static final long serialVersionUID = 42L;
 	/**
      * Show options
      */
     public static final int SHOW_ALL        = 0;
     public static final int SHOW_SELECTED   = 1;
     public static final int SHOW_WITH_PLAN  = 2;
     public static final int SHOW_CONFORMING = 3;
     public static final int SHOW_BLUNDERING = 4;
     public static final int SHOW_NONE       = 5;
     
     /**
      * Drawing Constants
      */
     private static final Color FIX_COLOR      = Color.green;
     private static final Color NO_PLAN_COLOR  = Color.yellow;
     private static final Color BLUNDER_COLOR  = Color.red;
     private static final Color CONFORM_COLOR  = Color.white;
     private static final Color ROUTE_COLOR    = Color.blue;
     private static final Color TRAJ_COLOR     = Color.magenta;
     private static final int FLIGHT_RADIUS    = 5;
     private static final int FIX_RADIUS       = 3;
     private static final int ROUTE_FIX_RADIUS = 4;
 
     /**
      * Image of the background map
      */
     private Image mapImage;
 
     /**
      * Image of the fixes superimposed on the background map
      */
     private Image mapFixesImage;
 
     /**
      * Image produced by the last paintUpdate method call
      */
     private Image lastImage;
 
     /**
      * True if has already painted once
      */
     private boolean paintedOnce = false;
 
     /**
      * True if there needs to be an updated paint
      */
     private boolean needUpdate = true;
 
     /**
      * Set to true by the image observer if the image it was given is ready
      */
     private boolean imageReady;
 
     /**
      * The bounds outlining the visible region
      */
     private LatLonBounds bounds;
 
     /**
      * Data to draw to the screen
      */
     private Collection fixes;
     private Collection flights  = new LinkedList();
     private Collection blunders = new LinkedList();
     private Map flight2TrajMap = new HashMap(); 
 
     /**
      * Flags triggering the display of certain flight pane items
      */
     private int showFixes  = SHOW_ALL, showFlights      = SHOW_ALL,
                 showRoutes = SHOW_ALL, showTrajectories = SHOW_ALL;
 
     /**
      * The selected flights
      */
     private Collection selectedFlights = new Vector();
 
     /**
      * Construct a flight map with a given map image, bounds, and fixes
      */
     public FlightMap(Image mapImage, LatLonBounds bounds, Collection fixes) {
         super();
         this.mapImage = mapImage;
         this.bounds   = bounds;
         this.fixes    = fixes;
 
         // Prepare the map image
         this.imageReady = false;
         super.prepareImage(mapImage, this);
         while(!this.imageReady) {
        try {
          Thread.sleep(50);
           } catch (InterruptedException e) {
             e.printStackTrace();
           }
         }
 
         // Set the size of the pane to the size of the image
         int width = mapImage.getWidth(this);
         int height = mapImage.getHeight(this);
         super.setPreferredSize(new Dimension(width, height));
 
         // Flight pane manages all the necessary offscreen images itself
         super.setDoubleBuffered(false);
     }
 
     /**
      * Implementation of ImageObserver
      * Sets imageReady equal to true when image is ready;
      */
     public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
         if ((infoflags & ImageObserver.ALLBITS) != 0) {
             imageReady = true;
             return false;
         }
         return true;
     }
 
     /**
      * Set the selected flight
      */
     public void setSelectedFlights(Collection selectedFlights) {
         this.selectedFlights = selectedFlights;
     }
 
     /**
      * Tells the flight pane its stored flight data
      * has changed and must perform an updated repaint
      */
     public void updateNeeded() {
         this.needUpdate = true;
     }
     
     /** Sets the flights in the pane */
     public void setFlights(Collection flights) {
         this.flights = new LinkedList(flights);
     }
     
     /** Sets the blunders in the pane */
     public void setBlunders(Collection blunders) {
         this.blunders = new LinkedList(blunders);
     }
 
     /** Sets the flight trajectory map in the pane */
     public void setFlightTrajectoryMap(Map flight2TrajMap) {
         this.flight2TrajMap = new HashMap(flight2TrajMap);
     }
 
     /** Turns on or off the painting of fixes */
     public void setShowFixes(int showFixes) {
         this.showFixes = showFixes;
     }
 
      /** Turns on or off the painting of flights */
     public void setShowFlights(int showFlights) {
         this.showFlights = showFlights;
     }
 
      /** Turns on or off the painting of routes */
     public void setShowRoutes(int showRoutes) {
         this.showRoutes = showRoutes;
     }
 
      /** Turns on or off the painting of trajectories */
     public void setShowTrajectories(int showTrajectories) {
         this.showTrajectories = showTrajectories;
     }
 
     /**
      * Paints the Flight Pane
      */
     public void paintComponent(Graphics g) {
         super.paintComponent(g);
 
         // If the image of the fixes superimposed on
         // the map has not been created yet, create it
         if (!this.paintedOnce) {
             // Create the mapFixes image and copy the map to it
             this.mapFixesImage = super.createImage(super.getWidth(), super.getHeight());
             Graphics g2 = mapFixesImage.getGraphics();
             g2.drawImage(mapImage, 0, 0, this.getWidth(), this.getHeight(), this);
 
             // Paint the fixes onto mapFixes image
             Iterator fixIter = this.fixes.iterator();
             g2.setColor(FIX_COLOR);
             while(fixIter.hasNext()) {
                 drawFix(g2, (Fix)fixIter.next());
             }
 
             paintedOnce = true;
         }
 
         // If there is a need to update the painting, perform an updated
         // paint make a copy of what was painted, and set needUpdate to false 
         if (needUpdate) {
             paintUpdate(g);
             this.lastImage = super.createImage(super.getWidth(), super.getHeight());
             paintUpdate(lastImage.getGraphics());
             this.needUpdate = false;
         }
         // If there is no need to update
         else {
             g.drawImage(lastImage, 0, 0, this.getWidth(), this.getHeight(), this);
         }
     }
 
     private void paintUpdate(Graphics g) {
         /**
          * Draw fixes
          */
         if (showFixes == SHOW_ALL) {
             g.drawImage(mapFixesImage, 0, 0, this.getWidth(), this.getHeight(), this);
         } else /* if (showFixes == SHOW_NONE) */ {
             g.drawImage(mapImage, 0, 0, this.getWidth(), this.getHeight(), this);
         }
 
         /**
          * Draw Flights
          */
         Iterator flightIter = flights.iterator();
         while(flightIter.hasNext()) {
             
             Flight flight = (Flight)flightIter.next();
             boolean hasFlightPlan = flight.getFlightPlan() != null;
             boolean isBlundering = hasFlightPlan && blunders.contains(flight);
             boolean isConforming = hasFlightPlan && !isBlundering;
             boolean isSelected = selectedFlights.contains(flight);
 
             // Draw the flight if . . .
             if ((showFlights == SHOW_ALL) ||
                 (showFlights == SHOW_WITH_PLAN  && hasFlightPlan) ||
                 (showFlights == SHOW_CONFORMING && isConforming) ||
                 (showFlights == SHOW_BLUNDERING && isBlundering) ||
                 (showFlights == SHOW_SELECTED   && isSelected)) {
                 
                 /** Set the color of the flight and draw it */
                 if      (!hasFlightPlan) g.setColor(NO_PLAN_COLOR);
                 else if (isBlundering)   g.setColor(BLUNDER_COLOR);
                 else /*(isConforming)*/  g.setColor(CONFORM_COLOR);
                 drawFlight(g, flight);
     
                 // Draw the route if . . .
                 if (hasFlightPlan &&
                     ((showRoutes == SHOW_ALL) ||
                      (showRoutes == SHOW_CONFORMING && isConforming) ||
                      (showRoutes == SHOW_BLUNDERING && isBlundering) ||
                      (showRoutes == SHOW_SELECTED   && isSelected))) {
                     /** Set the color of the route and draw it */
                     g.setColor(ROUTE_COLOR);
                     drawRoute(g, flight.getFlightPlan().getRoute());
                 }
 
                 // Draw the trajectory if . . .
                 if ((showTrajectories == SHOW_ALL) ||
                     (showTrajectories == SHOW_WITH_PLAN  && hasFlightPlan) ||
                     (showTrajectories == SHOW_CONFORMING && isConforming)  ||
                     (showTrajectories == SHOW_BLUNDERING && isBlundering)  ||
                     (showTrajectories == SHOW_SELECTED   && isSelected)) {
                      /** Set the color of the trajectory and draw it */
                     g.setColor(TRAJ_COLOR);
                     drawTrajectory(g, (Trajectory)flight2TrajMap.get(flight));
                 }
             }
         }
     }
 
     // HELPER DRAW METHODS
 
     private void drawFix(Graphics g, Fix f) {
         Point gPoint = translateToGraphicPoint(f.getLatitude(), f.getLongitude());
         g.fillOval(gPoint.x - FIX_RADIUS, gPoint.y - FIX_RADIUS,
                    2 * FIX_RADIUS, 2 * FIX_RADIUS);
         // Don't show the fixes for now
 		/*fillOval(gPoint.x - FIX_RADIUS, gPoint.y - FIX_RADIUS,
 				   2 * FIX_RADIUS, 2 * FIX_RADIUS, g.getColor());*/
                   
     }
     
     private void drawFlight(Graphics g, Flight f) {
         Point gPoint = translateToGraphicPoint(f.getFlightTrack().getLatitude(), f.getFlightTrack().getLongitude());
         g.fillOval(gPoint.x - FLIGHT_RADIUS, gPoint.y - FLIGHT_RADIUS,
                    2 * FLIGHT_RADIUS, 2 * FLIGHT_RADIUS);
                          
         g.drawString(f.getAircraftId(), gPoint.x + FLIGHT_RADIUS, gPoint.y - FLIGHT_RADIUS);
     }
 
     private void drawRoute(Graphics g, Route r) {
         java.util.List fixList = r.fixList();
         Iterator fixIter = fixList.iterator();
         int numPoints = fixList.size();
         
         if (numPoints > 0) {
             int xPoints[] = new int[numPoints];
             int yPoints[] = new int[numPoints];
             
             for (int i = 0; i < numPoints; i++) {
                 Fix fix = (Fix)fixIter.next();
                 Point gPoint = translateToGraphicPoint(fix.getLatitude(), fix.getLongitude());
 
                 g.fillOval(gPoint.x - ROUTE_FIX_RADIUS, gPoint.y - ROUTE_FIX_RADIUS,
                            2 * ROUTE_FIX_RADIUS, 2 * ROUTE_FIX_RADIUS);
 
                 xPoints[i] = gPoint.x;
                 yPoints[i] = gPoint.y;
             }
 
             g.drawPolyline(xPoints, yPoints, numPoints);
         }
     }
     
     private void drawTrajectory(Graphics g, Trajectory t) {
         java.util.List pointList = t.pointList();
         Iterator pointIter = pointList.iterator();
         int numPoints = pointList.size();
         
         if (numPoints > 0) {
             int xPoints[] = new int[numPoints];
             int yPoints[] = new int[numPoints];
 
             for (int i = 0; i < numPoints; i++) {
                 Point4D point4d = (Point4D)pointIter.next();
                 Point point = translateToGraphicPoint(point4d.getLatitude(), point4d.getLongitude());
                 g.fillOval(point.x - FIX_RADIUS, point.y - FIX_RADIUS, 2 * FIX_RADIUS, 2 * FIX_RADIUS);
 
                 
                 xPoints[i] = point.x;
                 yPoints[i] = point.y;
             }
             g.drawPolyline(xPoints, yPoints, numPoints);
 
         }
     }
 
     private Point translateToGraphicPoint(double lat, double lon) {
     	double xScale = this.getWidth () / (this.bounds.maxLon - this.bounds.minLon);
         double yScale = this.getHeight() / (this.bounds.maxLat - this.bounds.minLat );
        
         return new Point((int)(xScale * (lon - this.bounds.minLon)),
                          (int)(yScale * (this.bounds.maxLat - lat)));
     }
 
 }
