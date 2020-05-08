 package simulation.core.view;
 
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Shell;
 
 import simulation.extensionpoint.simulationplugin.definition.AbstractPluginPageWrapper;
 import simulation.extensionpoint.simulationplugin.resources.IForegroundManager;
 
 /**
  * Manages which pluginPage Composite is shown on the application in foreground. 
  * @author S-lenovo
  */
 public class ForegroundManager implements IForegroundManager{
 
 	private Composite currentlyInBackgroundsParent;
 	private Composite pluginPane;	
 	private AbstractPluginPageWrapper currentlyInForegroundPage;
 	private TreeViewer viewer;
 
 	public ForegroundManager(Composite foregroundParent) {
 		pluginPane = foregroundParent;
 		currentlyInBackgroundsParent = new Composite(new Shell(), SWT.NONE);
 	}
 
 	@Override
 	public void setToForeground(AbstractPluginPageWrapper pageFactory) {
 		if(currentlyInForegroundPage != pageFactory){
 			if(currentlyInForegroundPage != null)
 				currentlyInForegroundPage.setParent(currentlyInBackgroundsParent);
 			pageFactory.setParent(pluginPane);
 			currentlyInForegroundPage = pageFactory;
 			IStructuredSelection selection = new StructuredSelection(pageFactory);
 			viewer.setSelection(selection, true);
 		}
		pluginPane.pack();
		pluginPane.setFocus();
 	}
 
 	@Override
 	public Shell getShell() {
 		return pluginPane.getShell();
 	}
 
 	public void setTreeViewer(TreeViewer viewer) {
 		this.viewer = viewer;
 	}
 	
 }
