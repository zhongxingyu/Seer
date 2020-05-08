 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.utgdev.trackergame;
 
 import java.util.Scanner;
 
 /**
  *
  * @author Nicklas
  */
 public class ConsoleInterface implements TrackController {
 
     private World world;
     private Scanner scanner;
 
     public ConsoleInterface() {
         this.world = new World(30, 15, 5, this);
         this.scanner = new Scanner(System.in);
     }
 
    public int move(boolean[] shadows) {
         printWorld();
         return scanner.nextInt();
     }
 
     private void printWorld() {
         //Printing world
         for (int i = world.getHeight() - 1; i >= 1; i--) {
             if (i == world.getObjectHeight()) {
                 System.out.println(boolToString(world.getObjectRow()));
             } else {
                 System.out.println("");
             }
         }
         //Printing track
         System.out.println(boolToString(world.getTrackerRow()));
     }
 
     private String boolToString(boolean[] b) {
         StringBuilder sb = new StringBuilder();
         for (boolean bb : b) {
             if (bb) {
                 sb.append("X");
             } else {
                 sb.append(" ");
             }
         }
         return sb.toString();
     }
 
     public void objectDone(double overlap, int objectSize) {
         System.out.println("Overlap: "+overlap+" objectSize: "+objectSize);
     }
 
     public static void main(String[] args) {
         ConsoleInterface ci = new ConsoleInterface();
         while (true) {
             ci.world.update();
         }
     }
 }
