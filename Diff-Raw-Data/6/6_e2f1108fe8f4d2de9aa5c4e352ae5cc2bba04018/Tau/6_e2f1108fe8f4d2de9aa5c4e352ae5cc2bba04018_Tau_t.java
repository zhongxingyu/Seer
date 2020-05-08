 package com.taugame.tau.client;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.ui.RootPanel;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class Tau implements EntryPoint {
     /**
      * The message displayed to the user when the server cannot be reached or
      * returns an error.
      */
     @SuppressWarnings("unused")
     private static final String SERVER_ERROR = "An error occurred while "
             + "attempting to contact the server. Please check your network "
             + "connection and try again.";
 
     /**
      * Create a remote service proxy to talk to the server-side Tau service.
      */
 
     GameModel gameModel;
 
     public void onModuleLoad() {
         CometMessageHandler.exportSendBoardUpdate();
 
         new Timer() {
             @Override
             public void run() {
                startListening();
                 initialize();
             }
         }.schedule(1000);
     }
 
     private void initialize() {
         StateController stateController = new StateController(RootPanel.get("game"));
         stateController.changeState(StateController.State.NONE, StateController.State.START);
     }

    private native void startListening() /*-{
        $wnd.l();
    }-*/;
 }
