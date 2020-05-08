 package com.tngtech.leapdrone.entry;
 
 import com.google.inject.Inject;
 import com.tngtech.leapdrone.control.DroneInputController;
 import com.tngtech.leapdrone.drone.DroneController;
import com.tngtech.leapdrone.drone.data.Config;
 import com.tngtech.leapdrone.injection.Context;
 import com.tngtech.leapdrone.input.leapmotion.LeapMotionController;
 import com.tngtech.leapdrone.input.speech.SpeechDetector;
 import com.tngtech.leapdrone.ui.SwingWindow;
 
 import java.io.IOException;
 
 public class Main
 {
   private final SwingWindow swingWindow;
 
   private final DroneController droneController;
 
   private final SpeechDetector speechDetector;
 
   private final LeapMotionController leapMotionController;
 
   private final DroneInputController droneInputController;
 
   public static void main(String[] args)
   {
     Context.getBean(Main.class).start();
   }
 
   @Inject
   public Main(SwingWindow swingWindow, DroneController droneController,
               SpeechDetector speechDetector, LeapMotionController leapMotionController, DroneInputController droneInputController)
   {
     this.swingWindow = swingWindow;
     this.droneController = droneController;
     this.speechDetector = speechDetector;
     this.leapMotionController = leapMotionController;
     this.droneInputController = droneInputController;
   }
 
   private void start()
   {
     addEventListeners();
     startComponents();
 
     keepProcessBusy();
   }
 
   private void addEventListeners()
   {
     droneController.addNavDataListener(droneInputController);
     leapMotionController.addDetectionListener(droneInputController);
     leapMotionController.addGestureListener(droneInputController);
     //speechDetector.addSpeechListener(droneInputController);
   }
 
   private void startComponents()
   {
    droneController.startAsync(new Config("com.tngtech.internal.leap-drone", "myProfile"));
     swingWindow.createWindow();
 
     leapMotionController.connect();
     //speechDetector.start();
   }
 
   @SuppressWarnings("ResultOfMethodCallIgnored")
   private void keepProcessBusy()
   {
     try
     {
       System.in.read();
     } catch (IOException e)
     {
       e.printStackTrace();
     }
   }
 }
