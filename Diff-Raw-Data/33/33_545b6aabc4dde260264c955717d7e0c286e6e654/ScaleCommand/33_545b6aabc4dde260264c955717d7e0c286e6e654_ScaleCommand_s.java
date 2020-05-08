 package com.ribomation.droidAtScreen.cmd;
 
 import com.ribomation.droidAtScreen.Application;
 import com.ribomation.droidAtScreen.gui.DeviceFrame;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 /**
  * Set the device frame projection scale, as a percentage.
  */
 public class ScaleCommand extends CommandWithTarget<DeviceFrame> {
   public ScaleCommand(DeviceFrame deviceFrame) {
     super(deviceFrame);
     setIcon("scale");
     updateButton(deviceFrame);
   }
 
   @Override
   protected void doExecute(Application app, final DeviceFrame deviceFrame) {
     final JDialog dlg = PreferredScaleCommand.createScaleDialog(app,
         deviceFrame.getScale(),
         new PreferredScaleCommand.OnScaleUpdatedListener() {
           @Override
           public void onScaleUpdated(int value) {
             updateButton(deviceFrame);
             deviceFrame.setScale(value);
             deviceFrame.pack();
             deviceFrame.validate();
           }
         });
     dlg.setLocationRelativeTo(deviceFrame);
     dlg.setVisible(true);
   }
 
   @Override
   protected void updateButton(DeviceFrame deviceFrame) {
     setTooltip(String.format("Current scale (%d%%)", deviceFrame.getScale()));
   }
 }
