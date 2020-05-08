 package com.gamalinda.java;
 
 import com.gamalinda.java.util.Log;
 
 import javax.swing.*;
 import java.awt.*;
 import java.util.Arrays;
 import java.util.List;
 
 public class MainApp {
     private static final String TAG = MainApp.class.getSimpleName();
 
    private static MainApp mainAppInstace = new MainApp();
 
     private static List<String> argsList;
 
     private static double SCREEN_WIDTH;
     private static double SCREEN_HEIGHT;
 
     private MainApp() {
     }
 
     public static void main(String[] args) {
 
         argsList = Arrays.asList(args);
         getInstance().run();
     }
 
     public static MainApp getInstance() {
        return mainAppInstace;
     }
 
     private void run() {
         printHelloWorld();
         printArgs();
         respondToArgs();
         writeOnScreen();
     }
 
     private void printHelloWorld() {
         System.out.println("Hello World");
         Log.i(TAG, "Hello World");
     }
 
     private void printArgs() {
         for (String arg : argsList) {
             System.out.println(arg);
         }
     }
 
     private void respondToArgs() {
         for (String arg : argsList) {
 
             if (arg.equals("-a")) {
                 System.out.println("Responding to A");
             } else if (arg.equals("-b")) {
                 System.out.println("Responding to B");
             } else if (arg.equals("-c")) {
                 System.out.println("Responding to C");
             } else if (arg.equals("-d")) {
                 System.out.println("Responding to D");
             } else {
                 System.out.println("Responding to Default");
             }
         }
     }
 
     private void writeOnScreen() {
         JWindow w = new JWindow();
         w.add(new JLabel("Hello World!"));
 
         getScreenDimensions();
         int centerHorizontal = (int) (SCREEN_WIDTH / 2);
         int centerVertical = (int) (SCREEN_HEIGHT / 2);
 
         w.setLocation(centerHorizontal, centerVertical);
         w.pack();
         w.setVisible(true);
     }
 
     private void getScreenDimensions() {
         Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         SCREEN_WIDTH = screenSize.getWidth();
         SCREEN_HEIGHT = screenSize.getHeight();
     }
 }
