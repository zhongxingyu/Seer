 package edu.rit.se.sse.rapdevx.gui.screens;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.MouseEvent;
 import java.awt.geom.Area;
 import java.util.ArrayList;
 
 import edu.rit.se.sse.rapdevx.clientmodels.Ship;
 import edu.rit.se.sse.rapdevx.clientstate.AttackState;
 import edu.rit.se.sse.rapdevx.clientstate.GameSession;
 import edu.rit.se.sse.rapdevx.events.StateEvent;
 import edu.rit.se.sse.rapdevx.events.StateListener;
 import edu.rit.se.sse.rapdevx.gui.Screen;
 import edu.rit.se.sse.rapdevx.gui.ScreenStack;
 import edu.rit.se.sse.rapdevx.gui.drawable.Camera;
 import edu.rit.se.sse.rapdevx.gui.drawable.DrawableMovePath;
 import edu.rit.se.sse.rapdevx.gui.drawable.DrawableShip;
 
 public class MoveScreen extends Screen implements StateListener {
 	
 	/** A reference to the map camera for positioning objects in world space */
 	private Camera camera;
 	
 	/** A list of ships currently on the field **/
 	private ArrayList<DrawableShip> shipList;
 	private DrawableShip selectedShip;
 	
 	/** A path */
 	private DrawableMovePath movePath;
 	
 	/** A stats screen that shows when a ship is moused over */
 	private StatsScreen statsScreen = null;
 	
 	
 	public MoveScreen(Camera camera, int width, int height) {
 		super(width, height);
 		this.camera = camera;
 		
 		GameSession.get().addStateListener(this);
 		
 		shipList = new ArrayList<DrawableShip>();
 		Ship ship = new Ship();
 		ship.setX(150);
 		ship.setY(150);
 		ship.setHp(50);
 		shipList.add(new DrawableShip(ship, new Color(48, 129, 233)));
 		
 		Ship ship2 = new Ship();
 		ship2.setX(200);
 		ship2.setY(300);
		ship.setHp(75);
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
 			movePath.draw( gPen, cameraBounds );
 		}
 		
 		// Draw all the ships on the map
 		for (DrawableShip ship : shipList) {
 			ship.draw(gPen, cameraBounds);
 		}
 		
 		// Change the drawing back to screen based coordinates
 		gPen.translate(cameraBounds.getX(), cameraBounds.getY());
 	}
 	
 	private void selectShip(DrawableShip ship) {
 		setShipSelected(ship, true);
 		
 		selectedShip = ship;
 		movePath = new DrawableMovePath( ship );
 	}
 	
 	private void deselectShip() {
 		setShipSelected(selectedShip, false);
 		
 		selectedShip = null;
 		movePath = null;
 	}
 	
 	private void setShipSelected(DrawableShip ship, boolean isSelected) {
 		ship.setSelected(isSelected);
 		
 		if (isSelected) {
 			if (statsScreen == null) {
 				statsScreen = new StatsScreen(/*300, 200,*/ screenWidth, screenHeight, ship.getShip());
 				ScreenStack.get().addScreenAfter(this, statsScreen);
 			} else if (statsScreen != null && statsScreen.getShip() != ship.getShip()) {
 				ScreenStack.get().removeScreen(statsScreen);
 				statsScreen = null;
 			}
 		} else {
 			// If the there is a stats screen for this ship, get rid of it
 			if (statsScreen != null && statsScreen.getShip() == ship.getShip()) {
 				ScreenStack.get().removeScreen(statsScreen);
 				statsScreen = null;
 			}
 		}
 	}
 	
 	public void mouseReleased(MouseEvent e) {
 		// if the screen was right-clicked
 		if ( e.getButton() == MouseEvent.BUTTON3 ) {
 			if ( movePath != null ) {
 				if ( movePath.isAcceptingInput() ) {
 					if ( movePath.hasInitialPath() ) {
 						deselectShip();
 					} else {
 						movePath.removeLastMove();
 					}
 				}
 			}
 		} else {
 			
 			boolean selectedShipWasNull;
 			if ( selectedShip == null )
 				selectedShipWasNull = true;
 			else
 				selectedShipWasNull = false;
 			
 			// Check to see if one of the ships was clicked
 			for (DrawableShip ship : shipList) {
 				if (new Area(ship.getBounds()).contains(e.getX() + camera.getX(), e.getY() + camera.getY())) {
 					// The ship is selected so deselect it
 					if (ship == selectedShip) {
 						deselectShip();
 					// The ship is not selected
 					} else {
 						// Another ship is selected, make sure this one has no selection circle
 						if (selectedShip != null) {
 							selectedShip.setSelected(false);
 						// No other ship is selected so select this one
 						} else {
 							selectShip(ship);
 						}
 					}
 				
 				}
 			}
 			
 			// If no ship is clicked, move any selected ship to the mouse coordinates
 			if ( selectedShip != null && !selectedShipWasNull && movePath != null ) {
 				// selectedShip.setCenter(e.getX() + camera.getX(), e.getY() + camera.getY());
 				
 				Point point = new Point( e.getX() + camera.getX(), e.getY() + camera.getY() );
 				
 				if ( movePath.hasPointCloseToPrevious( point, 5 ) ) {
 					movePath.stopInput();
 					selectedShip.setCenter( (int)movePath.getPath().getLastPoint().getX(), 
 							(int)movePath.getPath().getLastPoint().getY() );
 					// selectedShip.setSelected( false );
 					selectedShip = null;
 					movePath = null;
 				} else {
 					movePath.addMove( point );
 				}
 			}
 		
 		}
 		
 		e.consume();
 	}
 	
 	public void mouseMoved(MouseEvent e) {
 		// Check all ships to see if the mouse is hovered over it
 		boolean found = false;
 		for (DrawableShip ship : shipList) {
 			if (!found && new Area(ship.getBounds()).contains(e.getX() + camera.getX(), e.getY() + camera.getY())) {
 				setShipSelected(ship, true);
 				found = true;
 				continue;
 			}
 			
 			// Another ship is hovered
 			if (ship != selectedShip)
 				setShipSelected(ship, false);
 		}
 		
 		if ( movePath != null ) {
 			// update movePath to let know that the mouse has moved
 			movePath.setMouseLocation( new Point( e.getX() + camera.getX(), e.getY() + camera.getY()) );
 		}
 		
 		e.consume();
 	}
 	
 	public void stateChanged(StateEvent e) {
 		if (e.getNewState() instanceof AttackState) {
 			ScreenStack.get().addScreenAfter(this, new AttackScreen(camera, screenWidth, screenHeight));
 			ScreenStack.get().removeScreen(this);
 			GameSession.get().removeStateListener(this);
 		}
 	}
 
 }
