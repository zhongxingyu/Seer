 
 package view.main;
 
 import java.util.Stack;
 
 import javafx.scene.layout.BorderPane;
 import view.home.HomePane;
 import view.util.Callbacks;
 import view.util.GenericPane;
 import view.util.MainPaneCallbacks;
 
 /**
  * Main JavaFX pane for holding the applications UI upon successful login.
  * 
  * @author Mohammad Juma
  * @version 11-11-2013
  */
 public class MainPane extends GenericPane<BorderPane> implements MainPaneCallbacks {
     
     /**
      * The top pane of this BorderPane.
      */
     private TopPane topPane;
     
     /**
      * The initial center pane for this BorderPane.
      */
     private HomePane homePane;
     
     private Stack<GenericPane<?>> stack;
     
     /**
      * Constructs a new MainPane pane that extends BorderPane and displays the main user
      * interface that all other panes are placed upon.
      */
     public MainPane(final Callbacks callbacks) {
         super(new BorderPane(), callbacks);
         stack = new Stack<GenericPane<?>>();
         
         topPane = new TopPane(this, callbacks);
         pane.setTop(topPane.getPane());
         homePane = new HomePane(callbacks, this, topPane);
         pushPane(homePane);
         pane.setCenter(homePane.getPane());
     }
     
     @Override
     public void pushPane(final GenericPane<?> pane) {
         stack.push(pane);
         this.pane.setCenter(pane.getPane());
         updateBackButton();
     }
     
     @Override
     public GenericPane<?> popPane() {
         GenericPane<?> pane = null;
         if (stack.peek()
                  .getClass() != HomePane.class) {
             pane = stack.pop();
         }
         else {
             pane = stack.peek();
         }
         updateBackButton();
         this.pane.setCenter(stack.peek()
                                  .getPane());
         return pane;
     }
     
     @Override
     public GenericPane<?> clearPanes() {
        GenericPane<?> pane = stack.peek();
         while (stack.size() > 1) {
             pane = popPane();
         }
         updateBackButton();
         this.pane.setCenter(pane.getPane());
         return pane;
     }
     
     public void updateBackButton() {
         topPane.enableBackButton(stack.size() > 1);
     }
     
 }
