 package code.gui.railboard;
 
 import code.controllers.VRailBoardController;
 import code.gui.VAbstractPanel;
 import java.awt.BorderLayout;
 
 /**
  *
  * @author Jose Carlos
  */
 public class VRailBoard extends VAbstractPanel
 {
     /** UI Components */
     private VRail rail;
     public VInstrumentPanel instrumentPanel;
     
     /** Controller */
     private VRailBoardController controller;
 
     public VRailBoardController getController() 
     {
         return controller;
     }
 
     public VRail getRail() 
     {
         return rail;
     }
 
     public VInstrumentPanel getInstrumentPanel() 
     {
         return instrumentPanel;
     }
     
     @Override
     protected void createComponents()
     {
         this.rail = new VRail();
         this.instrumentPanel = new VInstrumentPanel();
     }
     
     @Override
     protected void configureComponents()
     {
         setLayout(new BorderLayout());
         add(this.rail, BorderLayout.CENTER);
         add(this.instrumentPanel, BorderLayout.SOUTH);
     }
     
     @Override
     protected void configureControllers() 
     {
         this.controller = new VRailBoardController(this);
         this.instrumentPanel.addKeyListener(this.controller);
     }
 }
