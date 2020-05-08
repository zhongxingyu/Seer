 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.gatech.statics.modes.fbd.ui;
 
 import com.jmex.bui.BButton;
 import com.jmex.bui.BContainer;
 import com.jmex.bui.event.ActionEvent;
 import com.jmex.bui.event.ActionListener;
 import com.jmex.bui.layout.BorderLayout;
 import com.jmex.bui.layout.GroupLayout;
 import edu.gatech.statics.exercise.Exercise;
 import edu.gatech.statics.modes.fbd.FBDChecker;
 import edu.gatech.statics.modes.fbd.FreeBodyDiagram;
 import edu.gatech.statics.ui.applicationbar.ApplicationModePanel;
 import edu.gatech.statics.ui.applicationbar.ApplicationTab;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class FBDModePanel extends ApplicationModePanel {
 
     public static final String panelName = "addLoads";
     private FBDTools tools;
     private BContainer checkContainer;
     private BButton checkButton,  resetButton;
 
     @Override
     public String getPanelName() {
         return panelName;
     }
 
     public FBDModePanel() {
         super();
 
         tools = makeTools();//new BContainer(GroupLayout.makeHoriz(GroupLayout.CENTER));
         checkContainer = new BContainer(GroupLayout.makeVert(GroupLayout.CENTER));
 
         CheckListener listener = new CheckListener();
 
         checkButton = new BButton("Check", listener, "check");
         resetButton = new BButton("Reset", listener, "reset");
         checkButton.setStyleClass("circle_button");
         resetButton.setStyleClass("circle_button");
         checkContainer.add(checkButton);
         checkContainer.add(resetButton);
 
         add(tools, BorderLayout.CENTER);
         add(checkContainer, BorderLayout.EAST);
     }
 
     /**
      * By default we make FBDTools2D. Later this may need to be changed to
      * allow the tools to be dependent on the exercise.
      * @return
      */
     protected FBDTools makeTools() {
         return new FBDTools2D(this);
     }
 
     @Override
     protected ApplicationTab createTab() {
         return new ApplicationTab("Add Loads");
     }
 
     @Override
     public void activate() {
         // need to have list of bodies here...
         FreeBodyDiagram diagram = (FreeBodyDiagram) getDiagram();
         getTitleLabel().setText("My Diagram: " + diagram.getBodySubset());
         Exercise.getExercise().enableTabs(diagram.getBodySubset());
 
         if (diagram.isSolved()) {
             checkButton.setEnabled(false);
             resetButton.setEnabled(false);
             tools.setEnabled(false);
         }
     }
 
     /**
      * This class handles what happens when the user presses the "check" or "reset" buttons.
      * if the check passes, the diagram is set as solved.
      */
     private class CheckListener implements ActionListener {
 
         public void actionPerformed(ActionEvent event) {
             FreeBodyDiagram diagram = (FreeBodyDiagram) getDiagram();
 
             if (event.getAction().equals("reset")) {
                 ResetPopup popup = new ResetPopup(diagram);
                 popup.popup(0, 0, true);
                 popup.center();
             } else if (event.getAction().equals("check")) {
                 FBDChecker checker = diagram.getChecker();//new FBDChecker(diagram);
                 if (checker.checkDiagram()) {
                     diagram.setSolved(true);
                     
                     resetButton.setEnabled(false);
                     tools.setEnabled(false);
 
                     //System.out.println("woo!");
 
                     // let the diagram to know to advance past the FBD stage.
                     diagram.postSolve();
                 }
             }
         }
     }
 }
