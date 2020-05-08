 package org.hackystat.sensor.eclipse;
 
 import java.util.Map;
 
 import org.eclipse.jface.action.IStatusLineManager;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IViewReference;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.hackystat.sensorshell.SensorShell;
 import org.hackystat.sensorshell.SensorShellException;
 import org.hackystat.sensorshell.SensorShellProperties;
 
 /**
  * Provides the sensor shell wrapper class, which provides eclipse specific invocation such that 
  * the collected sensor data are shown on the status bar for monitoring. 
  * 
  * @author Hongbing Kou
  */
 public class SensorShellWrapper {
   /** The sensor shell instance. */
   private SensorShell shell;
 
   /**
    * Instantiates the eclipse specific sensor shell.
    * 
    * @param sensorShellProperties the SensorProperties instance.
    * 
    * @throws SensorShellException If error occurs in instantiating the sensorshell.
    */
   SensorShellWrapper(SensorShellProperties sensorShellProperties) throws SensorShellException {
     this.shell = new SensorShell(sensorShellProperties, false, "Eclipse");
   }
   
   /**
    * Add key-value pairs of metrics to sensorshell to be sent to the Hackystat server 
    * automatically.
    * 
    * @param keyValuePairs Key-value pairs of metrics.
    * @param message A message to appear on the Eclipse's status bar. 
    */
   public void add(Map<String, String> keyValuePairs, String message) {
     processStatusLine("Hackystat Sensor : " + message);
  
     try {
       this.shell.add(keyValuePairs);
     }
     catch (Exception e) {
       EclipseSensorPlugin plugin = EclipseSensorPlugin.getDefault();
       plugin.log(e);
       processStatusLine("Hackystat Sensor : Error occurred when sending data to server. ");
     }
   }
   
   /**
    * Sends all Hackystat data to the server. Do nothing if sensor shell instance is null.
    */
   public void send() {
     try {
       this.shell.send();
     }
     catch (SensorShellException e) {
       EclipseSensorPlugin plugin = EclipseSensorPlugin.getDefault();
       plugin.log(e);
       processStatusLine("Hackystat Sensor : Error occurred when sending data to server. ");
     }
   }
   
   /**
    * Quick sensorshell when Hackystat Eclipse Sensor is closed or stopped. 
    * 
    */
   public void quit() {
    this.shell.quit();
   }
 
   /**
    * Processes to display the message in the status line. Since EclipseSensor is executed
    * from a non-UI thread, such the application that wishs to call UI code from the non-UI thread
    * must provide a Runnable (anonymous class in this case) that calls the UI code. 
    * For more detail, try to search "Threading issues" in the "Help | Help Contents" of
    * the Eclipse IDE.
    * 
    * @param message the message to be displayed.
    */
   private void processStatusLine(final String message) {
     Display.getDefault().asyncExec(new Runnable() {
 
       public void run() {
         // Retrieves status line manager instance and sets the message into the instance.
         IWorkbench workbench = EclipseSensorPlugin.getInstance().getWorkbench();
         IWorkbenchWindow activeWindow = workbench.getActiveWorkbenchWindow();
         if (activeWindow != null) {
           IWorkbenchPage activePage = activeWindow.getActivePage();
           if (activePage != null) {
             showStatusMessage(activePage);
           }
         }
       }
       
       /**
        * Show the status message.
        *  
        * @param activePage The active workbench.
        */
       private void showStatusMessage(IWorkbenchPage activePage) {
         IViewReference[] viewReferences = activePage.getViewReferences();
         for (int i = 0; i < viewReferences.length; i++) {
           IViewPart viewPart = viewReferences[i].getView(true);
           if (viewPart != null) {
             IActionBars viewActionbars = viewPart.getViewSite().getActionBars();
             IStatusLineManager viewStatusManager = viewActionbars.getStatusLineManager();
             // Set status line associated with view part such as Package Explore, and etc.
             viewStatusManager.setMessage(message);
           }
         }
         IEditorPart editorPart = activePage.getActiveEditor();
         if (editorPart != null) {
           IActionBars partActionBars = editorPart.getEditorSite().getActionBars();
           IStatusLineManager partStatusManager = partActionBars.getStatusLineManager();
           // Set status line associated with editor part such as text editor.
           partStatusManager.setMessage(message);
         }
 
       }
     });
   }
 }
