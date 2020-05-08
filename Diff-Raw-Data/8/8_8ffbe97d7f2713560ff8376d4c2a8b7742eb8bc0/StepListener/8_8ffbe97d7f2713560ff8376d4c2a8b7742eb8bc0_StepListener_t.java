 package view.listeners;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import view.JGrid;
 import view.ViewManager;
 
 public class StepListener implements ActionListener {
 
     @Override
     public void actionPerformed(ActionEvent arg) {
 
	ViewManager viewManager = ViewManager.getManager();
	JGrid grid = viewManager.getGrid();
 
 	if (grid.isFirstRun()) {
 	    grid.removeGridListeners();
 	    grid.setInitialConfiguration();
	    viewManager.disableSamples();
 	}
 
 	grid.runEpoch();
	viewManager.updateLabel();
 
     }
 
 }
