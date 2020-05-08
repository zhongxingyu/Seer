 package ${package}.internal;
 
 
 import org.cytoscape.task.AbstractNetworkTask;
 import org.cytoscape.model.CyNetwork;
 import org.cytoscape.work.TaskMonitor;
 import org.cytoscape.work.Tunable;
 import org.cytoscape.model.CyNode;
 
 /**
  * This is an example task that demonstrates several features
  * of the {@link Task} and {@link Tunable} system.
  * <br/>
  * It uses a Tunable annotated field that demonstrates how users
  * can supply configuration information for a task.
  * <br/>
  * It inherits from an abstract class provided in the core
  * task API that provides pre-built support for many specific types
  * of tasks (this one is network related).
  * <br/>
  * It demonstrates how to update the {@link TaskMonitor} when 
  * running a Task.
  * <br/>
  * It demonstrates how to cancel a task when requested to do
  * so by a user.
  */
 public class SampleTask extends AbstractNetworkTask {
 
 	/**
 	 * The number of nodes you'd like to select.  By annotating
 	 * this field as "Tunable" you are indicating that the field
 	 * should be set by a Tunable handler and thus the user. 
 	 * This will happen before the run() method is called.  You 
 	 * don't need to write any code to set this value!  
 	 * <br/>
 	 * Tunable fields are appropriate for information that only the
 	 * user can provide, such as the name of a file to load.  On the
 	 * other hand, things like a reference to a {@link CyNetwork}, 
 	 * or a {@link CyNode} that only Cytoscape can provide, should be
 	 * passed in as arguments to the constructor.
 	 */
 	@Tunable(description="Number of nodes to select")
 	public int numNodesToSelect;
 
 	// Used to trigger the task dialog. 
 	// You should delete the following line from your code!
 	int sleepTime = 1000;
 
 	/**
 	 * The constructor. Any necessary data that is <i>not</i> provided by 
 	 * the user should be provided as arguments to the constructor.  
 	 */
 	public SampleTask(final CyNetwork n) {
 		// Will set a CyNetwork field called "net".
 		super(n);
 	}
 
 	/** 
 	 * This is where the actual work of the Task gets accomplished. All data
 	 * needed to execute the Task should have been provided as arguments in
 	 * the constructor OR as Tunable annotated fields.
 	 */
 	public void run(final TaskMonitor taskMonitor) {
 
 		// Validate the input parameter.  
 		// Tunables can also do some validation for themselves.  
 		// See the Tunables documentation for more information.
 		if ( numNodesToSelect < 0 )
 			throw new IllegalArgumentException("The number of nodes to select is less than 0");
 
 		if ( numNodesToSelect > network.getNodeCount() )
 			throw new IllegalArgumentException("The number of nodes to select " + 
 			                                   "is greater than the number of available nodes (" + 
 											   network.getNodeCount() + ")");
 
 		// Give the task a title.
 		taskMonitor.setTitle("Selecting " + numNodesToSelect + " nodes.");
 
 		int alreadySelected = 0;
 
 		// Loop over all nodes in the network.
 		for ( CyNode node : network.getNodeList() ) {
 
 			// cancelled is inherited from AbstractTask and is
 			// set when the cancel() method is called.
 			// Also consider calling a cleanup method to 
 			// undo anyting already accomplished!
 			if ( cancelled )
 				break;
 
 			// Pay attention to how many nodes we've already 
 			// selected and stop selecting when we reach the limit.
 			if ( ++alreadySelected > numNodesToSelect )
 				break;
 
 
 			// DO THE ACTUAL WORK!!!
 			// Set the selected attribute to true for the given node.
			node.getCyRow().set("selected",true);
 
 
 			// Update the progress bar with the percent complete
 			// and a status message.
 			double percent = (double) alreadySelected/ (double) numNodesToSelect;
 			taskMonitor.setProgress( percent );
 			taskMonitor.setStatusMessage("We've selected " + alreadySelected + " nodes.");
 
 			// This just exists so that you will actually see the task dialog.  
 			// You should delete the following 3 lines from your code!
 			try {
 				Thread.sleep(sleepTime);
 			} catch (InterruptedException e) {}
 		}
 	}
 }
