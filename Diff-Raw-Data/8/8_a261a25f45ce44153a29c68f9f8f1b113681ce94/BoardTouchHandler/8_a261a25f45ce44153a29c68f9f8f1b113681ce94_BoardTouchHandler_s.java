 package demo.client.local.game.handlers;
 
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.Touch;
 import com.google.gwt.event.dom.client.TouchEndEvent;
 import com.google.gwt.event.dom.client.TouchEndHandler;
 import com.google.gwt.event.dom.client.TouchEvent;
 import com.google.gwt.event.dom.client.TouchMoveEvent;
 import com.google.gwt.event.dom.client.TouchMoveHandler;
 import com.google.gwt.event.dom.client.TouchStartEvent;
 import com.google.gwt.event.dom.client.TouchStartHandler;
 import com.google.gwt.user.client.Timer;
 
 import demo.client.local.game.controllers.BoardController;
 
 /**
  * A touch input handler for the local player's game. This handler asynchronously sets values in an
  * associated {@link BoardController BoardController} which are handled by the
  * {@link BoardController BoardController} in it's game loop.
  * 
  * @author mbarkley <mbarkley@redhat.com>
  * 
  */
 public class BoardTouchHandler extends BoardInputHandler implements TouchStartHandler, TouchMoveHandler,
         TouchEndHandler {
 
   private Timer timer;
   private boolean tapped;
   private static final int timeout = 500;
 
   /**
    * Create a BoardTouchHandler instance.
    * 
    * @param controller
    *          The BoardController to be manipulated.
    * @param canvas
    *          An HTML5 canvas. Coordinates of events will be relative to this element.
    */
   public BoardTouchHandler(BoardController controller, Element canvas) {
     super(controller, canvas);
     timer = new Timer() {
 
       @Override
       public void run() {
         tapped = false;
       }
     };
   }
 
   @Override
   public void onTouchEnd(TouchEndEvent event) {
     if (event.getTouches().length() == 0) {
       endMove();
       event.preventDefault();
     }
   }
 
   @Override
   public void onTouchMove(TouchMoveEvent event) {
     if (isSingleFinger(event)) {
       Touch touch = event.getTouches().get(0);
       maybeContinueMove(touch.getRelativeX(canvas), touch.getRelativeY(canvas));
       event.preventDefault();
     }
   }
 
   @Override
   public void onTouchStart(TouchStartEvent event) {
     if (isSingleFinger(event)) {
       if (tapped) {
         drop();
       }
       else {
         Touch touch = event.getTouches().get(0);
         startMove(touch.getRelativeX(canvas), touch.getRelativeY(canvas));
         event.preventDefault();
 
         // Start timer to handle double-tap
         tapped = true;
         timer.schedule(timeout);
       }
     }
     else if (isTwoFingers(event)) {
       rotateOnce();
     }
   }
 
   private boolean isSingleFinger(TouchEvent<?> event) {
     return event.getTouches().length() == 1;
   }
 
   private boolean isTwoFingers(TouchEvent<?> event) {
     return event.getTouches().length() == 2;
   }
 
 }
