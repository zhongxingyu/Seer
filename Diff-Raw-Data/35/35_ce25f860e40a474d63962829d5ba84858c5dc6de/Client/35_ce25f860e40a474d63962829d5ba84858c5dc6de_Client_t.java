 package ${groupId};
 
 import java.awt.EventQueue;
 
import grisu.control.ServiceInterface;
import grisu.frontend.view.swing.GrisuApplicationWindow;
import grisu.frontend.view.swing.jobcreation.JobCreationPanel;

import java.awt.EventQueue;
 
 public class Client extends GrisuApplicationWindow {
 
 	public static void main(String[] args) {
 
 		// creating the UI
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 
 					GrisuApplicationWindow appWindow = new Client();
 					appWindow.setVisible(true);
 
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 
 	}
 
 	// pretty much everything is done for us in the superclass
 	public Client() {
 		super();
 	}
 
 	@Override
 	public boolean displayAppSpecificMonitoringItems() {
 		// yes, we only want to see the jobs that were submitted with this
 		// client and not the "all jobs" menu item
 		return true;
 	}
 
 	@Override
 	public boolean displayBatchJobsCreationPane() {
 		// no, we only submit a single job
 		return false;
 	}
 
 	@Override
 	public boolean displaySingleJobsCreationPane() {
 		// yes
 		return true;
 	}
 
 	@Override
 	public JobCreationPanel[] getJobCreationPanels() {
 		// only one type of job submission in our case, you can have more though
 		// (e.g. a basic one and an advanced)
 		return new JobCreationPanel[] { new ExampleJobCreationPanel() };
 	}
 
 	@Override
 	public String getName() {
 		// if you leave it the way it is, the name of your artifact will be the
 		// title of the java frame of this application. You can hardcode
 		// something different if you like, though.
 		return "${artifactId}";
 	}
 
 	@Override
 	protected void initOptionalStuff(ServiceInterface si) {
 		// here you could initialize application-wide stuff which needs a
 		// serviceInterface object
 	}

}
