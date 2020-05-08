 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.intelix.digihdmi.app.actions;
 
 import com.intelix.digihdmi.app.DigiHdmiApp;
 import com.intelix.digihdmi.app.tasks.SetAdminPasswordTask;
 import com.intelix.digihdmi.app.tasks.SetPasswordTask;
 import com.intelix.digihdmi.app.tasks.SetUnlockPasswordTask;
 import com.intelix.digihdmi.app.tasks.ToggleAdminLockTask;
 import com.intelix.digihdmi.app.views.dialogs.PasswordSubmissionDlg;
 import com.intelix.digihdmi.app.views.dialogs.SetPasswordDialog;
 import com.intelix.digihdmi.model.Device;
 import com.intelix.digihdmi.util.TaskListenerAdapter;
 import javax.swing.JOptionPane;
 import org.jdesktop.application.Action;
 import org.jdesktop.application.Application;
 import org.jdesktop.application.Task;
 import org.jdesktop.application.TaskEvent;
 
 /**
  *
  * @author developer
  */
 public class AdminActions {
 
     DigiHdmiApp app;
     Device device;
 
     public AdminActions() {
         app = (DigiHdmiApp)Application.getInstance();
         device = app.getDevice();
     }
 
     @Action (block=Task.BlockingScope.WINDOW)
     public Task unlockUtilView() {
         PasswordSubmissionDlg dlg = new PasswordSubmissionDlg(
                     app.getMainFrame(), device.getPassLength());
         String password = "";
         final boolean needToUnlock = device.isAdminLocked();
         if (needToUnlock)
         {
             dlg.setVisible(true);
             password = dlg.getPassword();
         }
         // If the user didn't cancel the dialog and we need to unlock OR
         // if we don't need to unlock, then go forward with the unlock stuff
         if (!needToUnlock || !dlg.isCancelled())
         {
             Task t = new ToggleAdminLockTask(app,password);
             t.setInputBlocker(((DigiHdmiApp)Application.getInstance()).new BusyInputBlocker(t));
             t.addTaskListener(new TaskListenerAdapter() {
                 @Override
                 public void succeeded(TaskEvent event) {
                     if (needToUnlock)
                     {
                         // spec says that 0 (false) is Locked, 1 (true) is unlocked
                         if (!(Boolean)event.getValue())
                         {
                             JOptionPane.showMessageDialog(null, "Error!","Device is still locked.",
                                 JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    app.showUtilView();
                 }
             });
             return t;
         }
         return null;
     }
 
     @Action
     public Task setAdminPassword() {
         return setPassword(new SetAdminPasswordTask(Application.getInstance()));
     }
     
     @Action
     public Task setUnlockPassword() {
         return setPassword(new SetUnlockPasswordTask(Application.getInstance()));
     }
 
     private Task setPassword(SetPasswordTask t) {
         DigiHdmiApp appInstance = ((DigiHdmiApp)Application.getInstance());
         SetPasswordDialog d = new SetPasswordDialog(appInstance.getMainFrame(),
                 appInstance.getDevice().getPassLength());
         d.setLocationRelativeTo(null);
         d.setVisible(true);
 
         if (!d.isCancelled())
         {
             String password = d.getValidPass();
             t.setPwd(password);
             return t;
         }
 
         return null;
     }
 
     @Action
     public void defineInputs() {}
 
     @Action
     public void defineOutputs() {}
 
 }
