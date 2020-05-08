 package edu.mines.csci598.recycler.splashscreen.playerdetector;
 
 import edu.mines.csci598.recycler.backend.GameManager;
 import edu.mines.csci598.recycler.backend.InputStatus;
 
 public class DetectHand {
     long _waitTime;
     GameManager _manager;
     boolean _detected;
     long _startedDetecting;
 
     public DetectHand( long waitTime, GameManager manager){
         this._waitTime = waitTime;
         this._manager = manager;
         this._detected = false;
         this._startedDetecting = 0;
     }
 
     public boolean playerFound(){
         // if there exists a detected
         float[][] pointers = _manager.getSharedInputStatus().pointers;
        for( int person = 0; person < pointers.length; person++ ){
            for( int pointer = 0; pointer < pointers[person].length; pointer++ ){
                if( pointers[person][pointer] >= 0 ){
                     return true;
                 }
             }
         }
 
         return false;
     }
 
     public boolean startGame(){
 
         // If the player has been detected in the past is currently detected and the time since detection is greater
         // than the time to wait then return true
         if( startConditionsMet() ){
             return true;
         }
 
         // Make nessicary variable changes
         if( playerFound() ){
 
             // If this is the first frame the person is detected
             if( !_detected ){
                 _startedDetecting = System.currentTimeMillis();
             }
 
             _detected = true;
         }
         else{
             _detected = false;
         }
 
         return false;
     }
 
     private boolean startConditionsMet(){
         // If a player hasn't been detected yet
         if( !_detected ){
             return false;
         }
         // If the player is not currently found
         else if( !playerFound() ){
             return false;
         }
         // If the time with the player detected has not yet been met
         else if( (System.currentTimeMillis() - _startedDetecting) < _waitTime ){
             return false;
         }
         else{
             return true;
         }
     }
 
     public void setGameManager( GameManager man ){
         this._manager = man;
         resetCounters();
     }
 
     public void resetCounters(){
         this._detected = false;
         this._startedDetecting = 0;
     }
 }
