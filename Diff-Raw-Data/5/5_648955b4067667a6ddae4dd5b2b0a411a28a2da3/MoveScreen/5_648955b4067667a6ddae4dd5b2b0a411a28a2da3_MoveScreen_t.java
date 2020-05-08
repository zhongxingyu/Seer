 package edu.rit.se.sse.rapdevx.gui;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.MouseEvent;
 import java.awt.geom.Area;
 import java.util.ArrayList;
 
 import edu.rit.se.sse.rapdevx.clientmodels.Ship;
 import edu.rit.se.sse.rapdevx.events.StateEvent;
 import edu.rit.se.sse.rapdevx.events.StateListener;
 
 public class MoveScreen extends Screen implements StateListener {
 	
 	/** A reference to the map camera for positioning objects in world space */
 	private Camera camera;
 	
 	/** A list of ships currently on the field **/
 	private ArrayList<DrawableShip> shipList;
 	private DrawableShip selectedShip;
 	
 	/** A path */
 	private DrawableMovePath movePath;
 	
 	
 	public MoveScreen(Camera camera, int width, int height) {
 		super(width, height);
 		this.camera = camera;
 		
 		shipList = new ArrayList<DrawableShip>();
 		Ship ship = new Ship();
 		ship.setX(150);
 		ship.setY(150);
 		shipList.add(new DrawableShip(ship, new Color(48, 129, 233)));
 		
 		Ship ship2 = new Ship();
 		ship2.setX(200);
 		ship2.setY(300);
 		shipList.add(new DrawableShip(ship2, new Color(108, 200, 47)));
 	}
 
 	public void update(boolean hasFocus, boolean isVisible) {
 		if (selectedShip != null)
 			selectedShip.setSelected(true);
 		
 		for (DrawableShip ship : shipList) {
 			ship.update();
 		}
 	}
 	
 	public void draw(Graphics2D gPen) {
 		// Translate the coordinates based on the camera
 		Rectangle cameraBounds = camera.getBounds();
 		gPen.translate(-cameraBounds.getX(), -cameraBounds.getY());
 		
 		/** Draw things based on the camera here **/
 		
 		// draw the move path if there is one
 		if ( movePath != null ) {
 			movePath.draw( gPen );
 		}
 		
 		// Draw all the ships on the map
 		for (DrawableShip ship : shipList) {
 			ship.draw(gPen, cameraBounds);
 		}
 		
 		// Change the drawing back to screen based coordinates
 		gPen.translate(cameraBounds.getX(), cameraBounds.getY());
 	}
 	
 	public void mouseReleased(MouseEvent e) {
 		// if the screen was right-clicked
 		if ( e.getButton() == MouseEvent.BUTTON3 ) {
 			if ( movePath != null ) {
 				if ( movePath.isAcceptingInput() ) {
 					if ( movePath.hasInitialPath() )
 						movePath = null;
 					else
 						movePath.removeLastMove();
 				}
 			}
 		} else {
 			
 			// Check to see if one of the ships was clicked
 			for (DrawableShip ship : shipList) {
 				if (new Area(ship.getBounds()).contains(e.getX() + camera.getX(), e.getY() + camera.getY())) {
 					// Select a non-selected ship and deselect a selected ship
 					if (ship == selectedShip) {
 						selectedShip.setSelected(false);
 						selectedShip = null;
 						movePath = null;
 					} else {
 						if (selectedShip != null)
 							selectedShip.setSelected(false);
 						
 						ship.setSelected(true);
 						selectedShip = ship;
 						movePath = new DrawableMovePath( ship );
 					}
 				// If no ship is clicked, move any selected ship to the mouse coordinates
 				} else if (selectedShip != null) {
 					// selectedShip.setCenter(e.getX() + camera.getX(), e.getY() + camera.getY());
 					
 					Point point = new Point( e.getX() + camera.getX(), e.getY() + camera.getY() );
 					
 					if ( movePath.hasPointCloseToPrevious( point, 5 ) ) {
 						movePath.stopInput();
 					} else {
 						movePath.addMove( point );
 					}
 				}
 			}
 		
 		}
 		
 		e.consume();
 	}
 	
 	public void mouseMoved(MouseEvent e) {
 		// Check all ships to see if the mouse is hovered over it
 		for (DrawableShip ship : shipList) {
 			//TODO potential bug when a null ship is in the list and selected ship is null?
 			if (ship != selectedShip) {
 				ship.setSelected(false);
 			}
 			
 			if (new Area(ship.getBounds()).contains(e.getX() + camera.getX(), e.getY() + camera.getY())) {
 				ship.setSelected(true);
 			}
 		}
 		
		if ( movePath != null )
			// update movePath to let know that the mouse has moved
			movePath.setMouseLocation( new Point( e.getX() + camera.getX(), e.getY() + camera.getY()) );
 		
 		e.consume();
 	}
 	
 	public void stateChanged(StateEvent e) {
 		//TODO Switch to move phase, etc
 	}
 
 }
