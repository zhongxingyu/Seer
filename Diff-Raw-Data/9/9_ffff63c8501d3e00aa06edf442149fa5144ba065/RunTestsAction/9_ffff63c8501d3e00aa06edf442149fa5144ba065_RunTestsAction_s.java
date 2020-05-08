 package org.mxunit.eclipseplugin.actions;
 
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.collections.map.CaseInsensitiveMap;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.widgets.TreeItem;
 import org.mxunit.eclipseplugin.MXUnitPlugin;
 import org.mxunit.eclipseplugin.MXUnitPluginLog;
 import org.mxunit.eclipseplugin.actions.bindings.RemoteFacade;
 import org.mxunit.eclipseplugin.actions.util.RemoteCallCreator;
 import org.mxunit.eclipseplugin.actions.util.TreeHelper;
 import org.mxunit.eclipseplugin.model.ITest;
 import org.mxunit.eclipseplugin.model.TestHistory;
 import org.mxunit.eclipseplugin.model.TestMethod;
 import org.mxunit.eclipseplugin.model.TestStatus;
 import org.mxunit.eclipseplugin.model.TestSuite;
 import org.mxunit.eclipseplugin.views.MXUnitView;
 
 
 public class RunTestsAction extends Action {
 
     private MXUnitView view;     
     private TreeItem[] allTreeItems;   
     private ITest[] runnableMethods;
     private Map<ITest, TreeItem> testItemsToTreeItemsMap;    
     private List<TreeItem> updatedSelectedTreeItemsList; 
     
     private final RemoteCallCreator callCreator = new RemoteCallCreator();
     private RemoteFacade facade;
 
     private TreeHelper treeHelper;
     
     
     public RunTestsAction(MXUnitView view) {
         this.view = view;
         MXUnitPlugin.getDefault().getPluginPreferences();
         treeHelper = new TreeHelper(view.getTestsViewer());
     }
 
     public void run() {     
         final long viewRunID = System.currentTimeMillis();
         view.setRunID(viewRunID);
         
     	view.disableActions();
      	//our "model"
         runnableMethods = treeHelper.getRunnableMethods();
         
         boolean runIt = verifyOKToRun();
         
         if(runIt){
         	TestSuite currentSuite = (TestSuite) view.getTestsViewer().getInput();
         	currentSuite.setStartTime(System.currentTimeMillis());
         	TestHistory history = view.getTestHistory();
         	history.addSuite(currentSuite);
         	//first, get an array of all tree items so we can easily set the tree's 
             //selected elements
             allTreeItems = treeHelper.getAllTreeItems();
             
             //set up a map and list that will eventually be used for setting the input of the tree
             testItemsToTreeItemsMap = new HashMap<ITest, TreeItem>();
             updatedSelectedTreeItemsList = new ArrayList<TreeItem>();
                         
 	        //deselect all so that we can update the selection as the tests complete
 	        view.getTestsViewer().getTree().deselectAll();      
 	       
 	        //create the map of testitems to treeitems
 	        for (int i = 0; i < allTreeItems.length; i++) {
 	            testItemsToTreeItemsMap.put((ITest) allTreeItems[i].getData(), allTreeItems[i]);
 	        } 
 	       
 	       
 	        final Thread t = new Thread() {
 	            public void run(){              
 	                runTests(viewRunID);
 	            }
 	        };
 	        t.start();
         }else{
         	view.enableActions();
         }
        
     }
     
     /**
      * checks that:
      * --runnable methods are present
      * --the facade URL has been defined at either the preferences or project properties level
      * --the facade URL is accessible
      * 
      * @return true if it's OK to run the methods
      */
     private boolean verifyOKToRun(){
     	if(runnableMethods.length==0){
     		MessageDialog
             .openInformation(
                     null,
                     "No runnable methods",
                     "No runnable methods have been selected");
     		return false;
     	}
     	//set up the remote stuff
     	facade = callCreator.createFacade(runnableMethods[0]);
     	
         //ensure we have a URL to run
         if (callCreator.getFacadeURL() == null || callCreator.getFacadeURL().length() == 0) {
             MessageDialog
                     .openInformation(
                             null,
                             "MXUnit Remote Facade not defined",
                             "You must define a remote facade URL either in the project's properties or in Window -- Preferences -- MXUnit");
             return false;
         }
         
         //make sure we can connect to the remote url
         boolean pingResult = callCreator.runPing();    
     	if(!pingResult){
     		MessageDialog
             .openInformation(
                     null,
                     "Unable to run ping method at facade URL " + callCreator.getFacadeURL(),
                    "Could not connect to facade URL. \n\nTry running this in a browser: " + callCreator.getFacadeURL() + "&method=ping");
     		view.writeToConsole("Exception message trying to connect to url " + callCreator.getFacadeURL() + " is: " + callCreator.getCurrentException().getMessage());
     		return false;
     	} 
         
     	
     	return true;
     }
 
     /**
      * runs the runnable test methods
      */
     private void runTests(long viewRunID) {    
         
     	resetProgressBar();
     	String testRunKey = startTestRun();
         for (int i = 0; i < runnableMethods.length; i++) {
             if(view.getRunID()!=viewRunID){
                 view.writeToConsole(".... Stopped.");
                 break;
             }
             //currentMethod = "";
             ITest testItem = runnableMethods[i];
             System.out.println("item is " + testItem.getName());  
            
             testItem.clearStatus();  
             //do the deed
             runTestMethod(testItem, viewRunID, testRunKey);            
         }
         endTestRun(testRunKey);
         ((TestSuite) view.getTestsViewer().getInput()).setEndTime(System.currentTimeMillis());
         FilterFailuresAction filter = new FilterFailuresAction(view);
         filter.run();
         view.enableActions();
         
     }
     
     /**
      * resets the progress bar to 'empty' color and sets the number of elements that will be run
      */
     private void resetProgressBar(){
     	view.resetCounts(runnableMethods.length);    	
     }
     
     
     /**
      * runs a test method
      * @param testItem the testmethod to run
      */
     @SuppressWarnings("unchecked")
 	private void runTestMethod(final ITest testItem, long viewRunID, String testRunKey) {
         final TestMethod tm = (TestMethod) testItem;
         final String currentComponent = tm.getParent().getName();
         final String currentMethod = tm.getName();
         
         view.writeToConsole("Running method " + currentMethod + "...");        
         
         Map tmpresults;
         
         try {            
         	tmpresults = (Map) facade.executeTestCase(currentComponent, currentMethod, testRunKey);
             
             Map results = new CaseInsensitiveMap(tmpresults);
             //printResults(results);
             Map parent = (Map) results.get(currentComponent);
             Map keys = (Map) parent.get(currentMethod);
             if(keys.get("EXCEPTION")!=null){
                 tm.setException( (String) keys.get("EXCEPTION") + ": " + (String) keys.get("MESSAGE"));
             }
             if(keys.get("TAGCONTEXT")!=null){       
                 Object[] tagContextArray = (Object[]) keys.get("TAGCONTEXT");
                 Map[] tagContextMap = new HashMap[tagContextArray.length];
                 for (int i = 0; i < tagContextArray.length; i++) {
                     Map trace = (Map) tagContextArray[i];
                     formatTagContextMap(trace);
                     tagContextMap[i] = trace;
                 }
                 tm.setTagcontext(tagContextMap);
             }
             tm.setResult((String) keys.get("MESSAGE"));
             tm.setOutput((String) keys.get("OUTPUT"));            
             tm.setStatusFromString((String) keys.get("RESULT"));
             
         } catch (RemoteException e) {
             tm.setStatus(TestStatus.ERROR);
             tm.setResult(e.toString());
             tm.setException(e.toString());
             tm.setTagcontext(null);
             view.writeToConsole("RemoteException: " + e.toString());
             MXUnitPluginLog.logError("RemoteException in RunTestsAction",e);
         }
         view.writeToConsole("   finished.\n");
         
         updatedSelectedTreeItemsList.add( testItemsToTreeItemsMap.get(testItem) );
         
         
         //repaint the tree and the details panel
         if(view.getRunID()==viewRunID){
             view.getTestsViewer().getTree().getDisplay().asyncExec(new Runnable() {
                 public void run() {
                 	//update the tree item to reflect the changes we made to the model
                     view.getTestsViewer().update(testItem, null);
                     view.getTestsViewer().update(testItem.getParent(),null);                
                     //this has the effect of selecting all the currently run methods. This was the only way
                     //i could figure out to achieve the type of selecting that i wanted. you cannot do
                     //an additive selection on a tree. you either select one or select an array...you can't have 
                     //some tree elements selected, say tree.select(item), and have that item selected in addition
                     //to the already selected items.
                     TreeItem[] tmp = updatedSelectedTreeItemsList.toArray( new TreeItem[0] );  
                     view.getTestsViewer().getTree().setSelection( tmp );  
                     //ensure that the currently selected tree item is visible in the tree (i.e. ensure we scroll down to it)
                     view.getTestsViewer().getTree().showItem(tmp[tmp.length-1]);
                     view.updateDetailsPanel();
                     view.addTestResult(tm.getStatus());
                 }
             });
         }
         
     }
     
     private String startTestRun(){
     	String key = "";
     	try {
     		key = facade.startTestRun();
 		} catch (RemoteException e) {
 			MXUnitPluginLog.logError("RemoteException in RunTestsAction startTestRun",e);
 		}
 		System.out.println("key fetched is " + key);
 		return key;
     }
     
     private void endTestRun(String testRunKey){
     	System.out.println("Invoking endTestRun with key " + testRunKey);
     	try {
     		facade.endTestRun(testRunKey);
 		} catch (RemoteException e) {
 			MXUnitPluginLog.logError("RemoteException in RunTestsAction endTestRun",e);
 		}
     }
 
     private void printResults(Map results){
         System.out.println(results);
     }
     
     /**
      * hook for consistently formatting certain values. BlueDragon passes the LINE as a Double, while CF passes as an Integer. We don't want our model  
      * @param tagContextItem
      */
     private void formatTagContextMap(Map tagContextItem){
     	Number num = (Number)tagContextItem.get("LINE");
         tagContextItem.put("LINE", num.intValue());      
     }
 
 }
