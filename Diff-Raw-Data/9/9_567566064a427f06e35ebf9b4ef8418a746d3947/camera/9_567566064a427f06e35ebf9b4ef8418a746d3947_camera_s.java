 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.wpi.first.wpilibj.templates;
 
 /**
  *
  * @author 01965
  */
 public class camera implements IRobot {
     
     public void getCamera()
     {
            getInstance.AxisCamera().writeCompression(0);
         AxisCamera.getInstance().writeResolution(AxisCamera.ResolutionT.k320x240);
         AxisCamera.getInstance().writeBrightness(10);
         DriverStationLCD.getInstance().updateLCD();
     }
 }
