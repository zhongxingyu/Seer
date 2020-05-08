 package edu.mines.csci598.recycler.splashscreen.playerdetector;
 
 import edu.mines.csci598.recycler.backend.GameManager;
 import edu.mines.csci598.recycler.backend.GameState;
 import edu.mines.csci598.recycler.backend.OpenNIHandTrackerInputDriver;
 
 public class TestDetectHand {
     public static void main(String[] args) {
         GameManager man = new GameManager( "TestPlayerDetector" );
         OpenNIHandTrackerInputDriver driver = new OpenNIHandTrackerInputDriver();
         driver.installInto( man );
         GameState state = man.getGameState();
 
         DetectHand dh = new DetectHand( 1000, man );
 
         while( !dh.startGame() ){
             // Display splash screen stuff
             System.out.println( dh.playerFound() );
            System.out.println( man.vcxtopx(man.getSharedInputStatus().pointers[0][0]) );
 
             // Update info from the kinect
             driver.pumpInput( man.getGameState() );
         }
 
         // Move on to the game run loop
     }
 }
