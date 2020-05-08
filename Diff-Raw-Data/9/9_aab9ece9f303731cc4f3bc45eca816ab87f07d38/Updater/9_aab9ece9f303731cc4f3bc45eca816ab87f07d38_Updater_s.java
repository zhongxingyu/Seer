 package src.edu.ucsb.cs56.games.towers_of_hanoi.utility;
 

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
 
 	HanoiTimer a_timer = new HanoTimer();
 	TimerCallback callback = new UpdateGUITimer();
 	a_timer.RegisterEveryNMilliseconds(n);
     }
     
     /** Starts up timer for updating for updating every second;
      *  The primary GUI code should be modified to call this at
      *  the start of whenever the timer should first appear
      *  (presumably as soon as the program starts)
      *  (no params)
      */
     public static void UpdateEverySecond() {
 
 	// In an hour, there are:
 	// 1000 milliseconds/second * 60 seconds/minute * 60 minutes per hour
 	// = 1000*60*60 milliseconds
	Updater.UpdateEverNMilliseconds(1000*60*60);
     }
 
     /** for potential testing
      */
     public void go() {
 
     }
     
     /** for potential testing
      */
     public static void main(String[] args) {
 	Updater a_updater = new Updater();
	a_updater.go()
     }

 }
