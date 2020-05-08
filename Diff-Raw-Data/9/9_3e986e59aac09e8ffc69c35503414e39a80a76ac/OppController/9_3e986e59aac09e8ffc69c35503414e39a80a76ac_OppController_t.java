 package demo.client.local.game.controllers;
 
 import java.util.LinkedList;
 import java.util.Queue;
 
 import demo.client.local.game.gui.Block;
 import demo.client.local.game.gui.ControllableBoardDisplay;
 import demo.client.local.game.tools.DummyBus;
 import demo.client.shared.model.BoardModel;
 
 public class OppController extends BoardController {
 
   private Queue<BoardModel> stateQueue = new LinkedList<BoardModel>();
   private boolean active;
 
   public OppController(ControllableBoardDisplay boardDisplay) {
     // Dummy objects do nothing, to prevent this controller from updating the displayed score or
     // sending messages through the bus.
     super(boardDisplay, new DummyController(), new DummyBus());
   }
 
   public void addState(BoardModel state) {
     synchronized (stateQueue) {
      if (!active) {
        stateQueue.clear();
       }
      stateQueue.add(state);
     }
   }
 
   void update() {
     if (active) {
       synchronized (stateQueue) {
         // Deal with row clearing animation before entering new state.
         int numFullRows = model.numFullRows();
         if (numFullRows > 0) {
           clearRows(numFullRows);
         }
         else if (!stateQueue.isEmpty()) {
           model = stateQueue.poll();
           reset();
           redraw();
         }
       }
     }
   }
 
   private void redraw() {
     boardDisplay.clearBoard();
     Block allSquares = new Block(model.getAllSquares(), boardDisplay.getSizeCategory());
     boardDisplay.drawBlock(0, 0, allSquares);
   }
 
   public boolean isActive() {
     return active;
   }
 
   public void setActive(boolean active) {
     this.active = active;
     if (!active) {
       stateQueue.clear();
     }
   }
 
 }
