 //
// $Id: SimulatorFrame.java,v 1.2 2002/01/16 02:59:08 mdb Exp $
 
 package com.threerings.micasa.simulator.client;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import com.samskivert.swing.Controller;
 import com.samskivert.swing.ControllerProvider;
 
 /**
  * Contains the user interface for the Simulator client application.
  */
 public interface SimulatorFrame extends ControllerProvider
 {
     /**
      * Returns a reference to the top-level frame that the simulator will
      * use to display everything.
      */
     public JFrame getFrame ();
 
     /**
      * Sets the panel that makes up the entire client display.
      */
     public void setPanel (JPanel panel);
 
     /**
      * Sets the controller for the outermost scope. This controller will
     * handle all actions that aren't handled by controllers of tigher
      * scope.
      */
     public void setController (Controller controller);
 }
