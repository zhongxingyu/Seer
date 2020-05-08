 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package lmp;
 
 /**
  *
  * @author T-CAP
  */
 public class Node {
     private int MAX = 1000;
     private int nextAttempt; //This field is the time to wait for next 
                             //attempt to transmit
     private int CollisionCount; //count number of collision
     private static final double BACKOFF_CONSTANT = 51.2; // μseconds! 
     
     /**
      * Instantiate a network node
      */
     public Node()
     {
         CollisionCount=0;
     }
     /**
      * 
      * @param initial - In case one would want a different
      *      initial value for k
      */
     public Node(int initial)
     {
         MAX=initial;
     }
     
     /**
      * 
      * @return 
      */
     private int backoff(int c)
     {
     	MAX += Math.random() % 2; // Randomly decides if k will be incremented or not
        return (int) MAX*BACKOFF_CONSTANT;
     }
     
     /**
      * 
      * @return 
      */
     public boolean transmit(int T) // What are we actually transmitting?
     {
         if (T!=0){
             nextAttempt = T;
             return true;
         }
         else {
             nextAttempt = T;
             return false;
         }
     }
     
     
     /**
      * 
      * @return 
      */
     public boolean clear()
     {
        k=0;
         return true;
     }
     public void Collide(){
         CollisionCount++;
         nextAttempt += 1 + backoff( CollisionCount);
     }
 }
