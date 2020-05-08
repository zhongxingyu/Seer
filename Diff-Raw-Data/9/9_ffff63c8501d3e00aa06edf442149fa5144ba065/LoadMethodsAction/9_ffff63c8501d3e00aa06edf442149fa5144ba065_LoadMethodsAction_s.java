 package org.mxunit.eclipseplugin.actions;
 
 import java.rmi.RemoteException;
 
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.widgets.TreeItem;
 import org.mxunit.eclipseplugin.MXUnitPluginLog;
 import org.mxunit.eclipseplugin.actions.bindings.RemoteFacade;
 import org.mxunit.eclipseplugin.actions.util.RemoteCallCreator;
 import org.mxunit.eclipseplugin.actions.util.TreeHelper;
 import org.mxunit.eclipseplugin.model.ITest;
 import org.mxunit.eclipseplugin.model.TestCase;
 import org.mxunit.eclipseplugin.model.TestMethod;
 import org.mxunit.eclipseplugin.views.MXUnitView;
 
 /**
  * loads all functions for given test cases into the tree
  * @author marc esher
  *
  */
 public final class LoadMethodsAction extends Action {
 
 	private MXUnitView view;
 
 	private TreeItem[] items;
 	private final RemoteCallCreator callCreator = new RemoteCallCreator();
 	private String facadeURL;
 	private TreeHelper treeHelper;
 	private ITest[] selectedTests;
 	public  boolean isFinished = false;
 	
 	private RemoteFacade facade;
 	
 	public LoadMethodsAction(MXUnitView view) {
 		this.view = view;
 		treeHelper = new TreeHelper(view.getTestsViewer());
 	}
 
 	public void run() {
 	    final long runID = System.currentTimeMillis();
 	    view.setRunID(runID);
 		view.disableActions();
 		view.resetCounts(0);
 		
 		
 		items = view.getTestsViewer().getTree().getSelection();
 		
 		if (items.length == 0) {
 			view.getTestsViewer().getTree().selectAll();
 			items = view.getTestsViewer().getTree().getSelection();
 		}
 
 		boolean runIt = verifyOKToRun();
 
 		if (runIt) {
 			selectedTests = treeHelper.getSelectedTestCases();
 			
 			
 			final Thread t = new Thread() {
 	            public void run(){              
 	                loadTests(runID);
 	            }
 	        };
 	        t.run();//run instead of start because we always want this to run synchronously!
 	        
 		}else{
 			view.resetCounts(0);
 			view.enableActions();
 		}
 		
 		view.getTestsViewer().expandAll();
 		
 	}
 
 	/**
 	 * lamest f'n method i've ever written. i hate looking at it it's so bad.
 	 * @param runID
 	 */
 	private void loadTests(long runID) {
 		for (int i = 0; i < selectedTests.length; i++) {
 		    if(view.getRunID()!=runID){
 		        break;
 		    }
 			ITest item = selectedTests[i];
 			view.writeToConsole("Loading methods for component " + item.getName());
 			
 			Object[] tmpresult;
 			try {					
 				tmpresult = (Object[])facade.getComponentMethods(item.getName());					
 				
 				TestCase tc = (TestCase) item;
 				tc.clearMethods();
 				tc.clearStatus();
 				for (int j = 0; j < tmpresult.length; j++) {
 					TestMethod tm = new TestMethod();
 					tm.setName((String) tmpresult[j]);
 					tc.addMethod(tm);
 				}
 				
 				updateTreeViewer(runID, item);
 				
 				// view.getTestsViewer().refresh( item.getParent() );
 			} catch (final RemoteException e) {	
 				MXUnitPluginLog.logError("RemoteException in LoadMethodsAction",e);
 				showErrorDialog(item, e);
 			}
 		}		
 
 		view.resetCounts(selectedTests.length);
 		view.enableActions();
 	}
 	
 	private void updateTreeViewer(long runID, final ITest item) {
 		if(view.getRunID()==runID){
 		    view.getTestsViewer().getTree().getDisplay().syncExec(new Runnable() {
 		        public void run() {
 		        	//update the tree item to reflect the changes we made to the model
 		        	view.getTestsViewer().refresh(item);		                	
 		            view.getTestsViewer().update(item.getParent(),null);  
 		            view.updateDetailsPanel();
 		        }
 		    });
 		}
 	}
 	
 	private void showErrorDialog(final ITest item, final RemoteException e) {			
 		view.getTestsViewer().getTree().getDisplay().asyncExec(new Runnable() {
 		        public void run() {
 		        	MessageDialog.openError(null, "MXUnit",
 							"ERROR getting methods for Component. Message is: \n\n"
 									+ e.getMessage() + "\n\n*Component*: "
 									+ item.getName() + "\n\n*URL*: "
 									+ facadeURL);
 		        }
 		    });
 	}
 
 	
 
 	/**
 	 * checks that: --the facade URL has been defined at either the preferences
 	 * or project properties level --the facade URL is accessible
 	 * 
 	 * @return true if it's OK to run the methods
 	 */
 	private boolean verifyOKToRun() {
 		if (items.length == 0) {
 			MessageDialog.openInformation(null, "No Test Cases",
 					"No Test Cases are present");
 
 			return false;
 		}
 		
 		facade = callCreator.createFacade((ITest) items[0].getData());
 		facadeURL = callCreator.getFacadeURL();
 
 		if (facadeURL == null || facadeURL.length() == 0) {
 			MessageDialog
 					.openInformation(
 							null,
 							"MXUnit Remote Facade not defined",
 							"You must define a remote facade URL either in the project's properties or in Window -- Preferences -- MXUnit");
 			return false;
 		}
 		// make sure we can connect to the remote url
 		boolean pingResult = callCreator.runPing();
 		if (!pingResult) {
 			MessageDialog.openInformation(null,
 					"Unable to run ping method at facade URL "
 							+ callCreator.getFacadeURL(),
 					"Could not connect to facade URL. \n\nTry running this in a browser: "
							+ callCreator.getFacadeURL() + "&method=ping");
 			view.writeToConsole("Exception message trying to connect to url "
 					+ callCreator.getFacadeURL() + " is: "
 					+ callCreator.getCurrentException().getMessage());
 			
 			return false;
 		}
 		return true;
 	}
 }
