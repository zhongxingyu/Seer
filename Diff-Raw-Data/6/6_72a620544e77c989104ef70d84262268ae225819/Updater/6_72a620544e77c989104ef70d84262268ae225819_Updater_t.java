 package src.edu.ucsb.cs56.games.towers_of_hanoi.utility;
 
 import  javax.swing.*;
 import java.awt.event.*;
 
 /** Class that main GUI can call to start up timer
  *  at the beginning of the game
  *  @author Shanen Cross
  */
 
 public class Updater {
 
     // This assumes that we have a Timer called HanoiTimer with the methods indicated,
     // but as of now no one has implemented one yet
 
     /** Starts up timer, makes a listener (which has the callback),
      *  and passes it to the timer, which invokes the callback
      *  every n milliseconds
      *  @param n  number of milliseconds; callback to be invoked
      */
     public static void UpdateEveryNMilliseconds(int n) {
 	//	SwingHanoiJFrame.container().add
 
 	// Register our callback with the timer (the event source),
 	// Specifying to the timer that it is to be invoked
 	// every n milliseconds
 
 	// (Here I assume our HanoiTimer class has a member method
 	// RegisterEveryNMilliseconds which registers a listener
 	// and schedules itself to invoke it every n milliseconds)
 	a_timer.EveryNMilliseconds(n);
     }
     
     /** Starts up timer for updating for updating every second;
      *  The primary GUI code should be modified to call this at
      *  the start of whenever the timer should first appear
      *  (presumably as soon as the program starts)
      *  (no params)
      */
     public static void UpdateEverySecond() {
 
	// In an second, there are 1000 milliseconds
	Updater.UpdateEveryNMilliseconds(1000);
     }
 
     /** for potential testing
      */
     public void go() {
 
     }
     
     /** for potential testing
      */
     public static void main(String[] args) {
 	Updater a_updater = new Updater();
 	a_updater.go();
     }
 }
