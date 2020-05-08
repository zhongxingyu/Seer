 package vooga.rts.controller;
 
 import java.awt.geom.Rectangle2D;
 import util.input.*;
 import vooga.rts.commands.Command;
 import vooga.rts.commands.DragCommand;
 import vooga.rts.state.State;
 import vooga.rts.util.Camera;
 import vooga.rts.util.Location;
 import vooga.rts.util.Location3D;
 
 
 /**
  * After much thought, I've decided to only have one InputController. This controller
  * sends the formatted inputs to the main state, which relays them to the appropriate
  * state.
  * 
  * The Input Controller is responsible for routing Input Events to the correct place.
  * It turns input events into Commands that can be processed by the respective states
  * in order to turn them into actions.
  * 
  * It also manages the dragging input of a mouse. This will need to be passed in and
  * painted by the game state.
  * 
  * 
  * @author Challen Herzberg-Brovold
  * @author Jonathan Schmidt
  * 
  */
 @InputClassTarget
 public class InputController implements Controller {
 
     private State myState;
 
     private Location myLeftMouse;
 
     private Rectangle2D myDrag;
 
     public InputController (State state) {
         myState = state;
     }
 
     @Override
     public void sendCommand (Command command) {
         myState.receiveCommand(command);
     }
 
     @InputMethodTarget(name = "onLeftMouseDown")
     public void onLeftMouseDown (PositionObject o) {
         myLeftMouse = new Location(o.getPoint2D());
     }
 
     @InputMethodTarget(name = "onLeftMouseUp")
     public void onLeftMouseUp (PositionObject o) {
         if (myDrag == null) {
             sendCommand(new PositionCommand("leftclick", o));
         }
         else {
             myLeftMouse = null;
             myDrag = null;
            if (myDrag == null) {
                sendCommand(new DragCommand("drag", null, null));
            }
         }
     }
 
     @InputMethodTarget(name = "onRightMouseUp")
     public void onRightMouseUp (PositionObject o) {
         sendCommand(new PositionCommand("rightclick", o));
     }
 
     @InputMethodTarget(name = "onMouseMove")
     public void mouseMove (PositionObject o) {
         sendCommand(new PositionCommand("move", o));
     }
 
     @InputMethodTarget(name = "onMouseDrag")
     public void onMouseDrag (PositionObject o) {
         if (!myLeftMouse.equals(null)) {
             double uX = o.getX() > myLeftMouse.getX() ? myLeftMouse.getX() : o.getX();
             double uY = o.getY() > myLeftMouse.getY() ? myLeftMouse.getY() : o.getY();
             double width = Math.abs(o.getX() - myLeftMouse.getX());
             double height = Math.abs(o.getY() - myLeftMouse.getY());
             myDrag = new Rectangle2D.Double(uX, uY, width, height);
             sendCommand(new DragCommand("drag", Camera.instance().viewtoWorld(myDrag), myDrag));
         }
     }
    
    
 }
