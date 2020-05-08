 package com.intelix.digihdmi.app.actions;
 
 import com.intelix.digihdmi.app.DigiHdmiApp;
 import com.intelix.digihdmi.app.tasks.GetConnectorsTask;
 import com.intelix.digihdmi.app.tasks.GetInputsForSelectionTask;
 import com.intelix.digihdmi.app.tasks.GetInputsForCustomizationTask;
 import com.intelix.digihdmi.app.tasks.GetOutputsForCustomizationTask;
 import com.intelix.digihdmi.app.tasks.GetOutputsForSelectionTask;
 import com.intelix.digihdmi.app.tasks.LoadIconsForInputTask;
 import com.intelix.digihdmi.app.tasks.LoadIconsForOutputTask;
 import com.intelix.digihdmi.app.tasks.MakeConnectionTask;
 import com.intelix.digihdmi.app.tasks.SetConnectorNameTask;
 import com.intelix.digihdmi.app.views.dialogs.NameChangeDlg;
 import com.intelix.digihdmi.model.Connector;
 import com.intelix.digihdmi.util.TaskListenerAdapter;
 import java.awt.event.ActionEvent;
 import javax.swing.AbstractButton;
 import javax.swing.JOptionPane;
 import org.jdesktop.application.Action;
 import org.jdesktop.application.Application;
 import org.jdesktop.application.Task;
 import org.jdesktop.application.TaskEvent;
 
 public class ConnectorActions {
 
     DigiHdmiApp appInstance;
 
     public ConnectorActions() {
         appInstance = (DigiHdmiApp) Application.getInstance();
     }
 
     protected int getSelectedConnector(AbstractButton b)
     {
         int index = b.getName().lastIndexOf('_') + 1;
         return Integer.parseInt(b.getName().substring(index));
     }
 
     @Action (block=Task.BlockingScope.WINDOW)
     public Task showInputListForSelection(ActionEvent ev) {
         int outputNumber = getSelectedConnector((AbstractButton) ev.getSource());
         appInstance.getDevice().setSelectedOutput(outputNumber);
         appInstance.showInputSelectionView();
 
         Task t = new GetInputsForSelectionTask(appInstance);
         t.setInputBlocker(appInstance.new BusyInputBlocker(t));
         return t;
     }
 
     @Action (block=Task.BlockingScope.WINDOW)
     public Task showOutputListForCustomization() {
         appInstance.showOutputCustomizationView();
         Task t = new GetOutputsForCustomizationTask(appInstance);
         t.setInputBlocker(appInstance.new BusyInputBlocker(t));
         return t;
     }
 
     @Action (block=Task.BlockingScope.WINDOW)
     public Task showInputListForCustomization() {
         appInstance.showInputCustomizationView();
         Task t = new GetInputsForCustomizationTask(appInstance);
         t.setInputBlocker(appInstance.new BusyInputBlocker(t));
         return t;
     }
 
     @Action
     public void changeInput(ActionEvent ev)
     {
         int index = getSelectedConnector((AbstractButton) ev.getSource());
         appInstance.getDevice().setSelectedInput(index);
         appInstance.getDevice().setSelectedOutput(-1);
         appInstance.showInputChangeView();
     }
 
     @Action
     public void changeOutput(ActionEvent ev)
     {
         int index = getSelectedConnector((AbstractButton) ev.getSource());
         appInstance.getDevice().setSelectedOutput(index);
         appInstance.getDevice().setSelectedInput(-1);
         appInstance.showOutputChangeView();
     }
 
     @Action (block=Task.BlockingScope.WINDOW)
     public Task connectInputAndOutput(ActionEvent ev) {
         int index = getSelectedConnector((AbstractButton) ev.getSource());
 
         // avoid setting the selected input here because if the task fails, we
         // want the task to reset to the previously selected without another
         // round trip to the device.
         MakeConnectionTask t = new MakeConnectionTask(appInstance, index);
 
         t.setInputBlocker(appInstance.new BusyInputBlocker(t));
         return t;
     }
 
     @Action (block=Task.BlockingScope.WINDOW)
     public Task showOutputList() {
         Task t = new GetOutputsForSelectionTask(appInstance);
         t.setInputBlocker(appInstance.new BusyInputBlocker(t));
         return t;
     }
 
     @Action
     public Task assignNewName()
     {
         // show a dialog to capture a name and use the selected button to
         // assign new name to device's input
         
         Connector sI = appInstance.getDevice().getSelectedInput();
         Connector sO = appInstance.getDevice().getSelectedOutput();
         Connector selected = null;
         final Task onFinishTask;
 
         String insertText;
         if (sI != null)
         {
             insertText = "input " + sI.getIndex();
             selected = sI;
         } else
         {
             insertText = "output " + sO.getIndex();
             selected = sO;
         }
 
         NameChangeDlg dlg = new NameChangeDlg(appInstance.getMainFrame(),
                                             insertText, 
                                             appInstance.getDevice().getIONameLength()
                                             );
         dlg.setVisible(true);
         String newName = dlg.getTheName();
 
         if (sI != null) 
             onFinishTask = showInputListForCustomization();
         else 
             onFinishTask = showOutputListForCustomization();
 
        if (! dlg.isCancelled())
         {
            if (newName.isEmpty())
                newName = " ";
             Task t = new SetConnectorNameTask(appInstance, newName, selected);
             t.addTaskListener(new TaskListenerAdapter() {
                 @Override
                 public void finished(TaskEvent event) {
                     onFinishTask.setInputBlocker(appInstance.new BusyInputBlocker(onFinishTask));
                     appInstance.getContext().getTaskService().execute(onFinishTask);
                 }
             });
             return t;
         }
 
         onFinishTask.setInputBlocker(appInstance.new BusyInputBlocker(onFinishTask));
         return onFinishTask;
     }
 
     @Action
     public Task assignNewIcon()
     {
         Connector sI = appInstance.getDevice().getSelectedInput();
         Connector sO = appInstance.getDevice().getSelectedOutput();
         Task t;
 
         if (sI != null)
         {
             appInstance.showInputIconChoicePanel();
             t = new LoadIconsForInputTask(appInstance,sI);
         } else
         {
             appInstance.showOutputIconChoicePanel();
             t = new LoadIconsForOutputTask(appInstance,sO);
         }
 
         return t;
     }
 }
