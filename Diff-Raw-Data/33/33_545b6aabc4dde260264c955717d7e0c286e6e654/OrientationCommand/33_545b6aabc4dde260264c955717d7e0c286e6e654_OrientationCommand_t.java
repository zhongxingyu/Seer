 package com.ribomation.droidAtScreen.cmd;
 
 import com.ribomation.droidAtScreen.Application;
 import com.ribomation.droidAtScreen.cmd.CommandWithTarget;
 import com.ribomation.droidAtScreen.gui.DeviceFrame;
 
 import javax.swing.*;
 
 /**
  * Flips the device-frame 90 degrees.
  */
 public class OrientationCommand extends CommandWithTarget<DeviceFrame> {
   public OrientationCommand(DeviceFrame deviceFrame) {
     super(deviceFrame);
     updateButton(deviceFrame);
   }
 
   @Override
   protected void doExecute(Application app, DeviceFrame deviceFrame) {
     deviceFrame.setLandscapeMode(!deviceFrame.isLandscapeMode());
     updateButton(deviceFrame);
     deviceFrame.pack();
    deviceFrame.invalidate();
     deviceFrame.validate();
    deviceFrame.repaint();
   }
 
   protected void updateButton(DeviceFrame deviceFrame) {
     String orientation = deviceFrame.isLandscapeMode() ? "Landscape" : "Portrait";
     setTooltip(String.format("Flip the orientation (%s)", orientation));
     setIcon("orientation-" + orientation.toLowerCase().trim());
   }
 }
