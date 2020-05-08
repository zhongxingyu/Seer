 package nachos.threads;
 
 import nachos.machine.*;
 
 /**
  * A <i>communicator</i> allows threads to synchronously exchange 32-bit
  * messages. Multiple threads can be waiting to <i>speak</i>,
  * and multiple threads can be waiting to <i>listen</i>. But there should never
  * be a time when both a speaker and a listener are waiting, because the two
  * threads can be paired off at this point.
  */
 public class Communicator {
 	Lock lock;
 	Condition2 waitingReceivers;
 	Condition2 waitingSenders;
 	int liveSender;
 	int liveReceiver;
 	int value;
     /**
      * Allocate a new communicator.
      */
     public Communicator() {
     	lock = new Lock();
     	waitingReceivers = new Condition2(lock);
     	waitingSenders = new Condition2(lock);
     	liveReceiver = 0;
     	liveSender = 0;
     	value = 0;
     }
 
     /**
      * Wait for a thread to listen through this communicator, and then transfer
      * <i>word</i> to the listener.
      *
      * <p>
      * Does not return until this thread is paired up with a listening thread.
      * Exactly one listener should receive <i>word</i>.
      *
      * @param	word	the integer to transfer.
      */
     public void speak(int word) {
     	lock.acquire();
     	
     	while(liveSpeaker == 1) {
     		waitingSenders.sleep();
     	}
     	
     	liveSpeaker = 1;
     	value = word;
     	
     	while(liveReceiver == 0) {
     		waitingReceivers.wake();
     		waitingSenders.sleep();
     	}
     	
     	liveReceiver = 0;
     	
     	waitingSenders.wake();
     	waitingReceivers.wake();
     	
     	lock.release();
     }
 
     /**
      * Wait for a thread to speak through this communicator, and then return
      * the <i>word</i> that thread passed to <tt>speak()</tt>.
      *
      * @return	the integer transferred.
      */    
     public int listen() {
     	lock.acquire();
     	
     	while(liveListener == 1){
     		waitingReceivers.sleep();
     	}
     	
     	liveListener = 1;
     	
     	while(liveSpeaker == 0){
     		waitingSenders.wake();
     		waitingReceivers.sleep();
     	}
     	
     	waitingSenders.wake();
     	waitingReceivers.wake();
     	
     	lock.release();
     	liveSpeaker = 0;
     	
    	return value;
     }
 }
