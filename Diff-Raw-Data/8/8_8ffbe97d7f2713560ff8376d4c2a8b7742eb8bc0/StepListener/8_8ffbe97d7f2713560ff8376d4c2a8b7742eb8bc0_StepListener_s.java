 package view.listeners;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import view.JGrid;
 import view.ViewManager;
 
 public class StepListener implements ActionListener {
 
     @Override
     public void actionPerformed(ActionEvent arg) {
 
	JGrid grid = ViewManager.getManager().getGrid();
 
 	if (grid.isFirstRun()) {
 	    grid.removeGridListeners();
 	    grid.setInitialConfiguration();
 	}
 
 	grid.runEpoch();
	ViewManager.getManager().updateLabel();
 
     }
 
 }
