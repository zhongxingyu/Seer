 package cz.velim.threadcontrol;
 
 public class ThreadControl {
 
 	public static void main(String args[]) {
 		NewThread ob1 = new NewThread("One");
 		NewThread ob2 = new NewThread("Two");
 		try {
 			Thread.sleep(1000);
 			ob1.mysuspend();
 			System.out.println("Suspending thread One");
 			Thread.sleep(1000);
 			ob1.myresume();
 			System.out.println("Resuming thread One");
 			ob2.mysuspend();
 			System.out.println("Suspending thread Two");
 			Thread.sleep(1000);
 			ob2.myresume();
 			System.out.println("Resuming thread Two");
 		} catch (InterruptedException e) {
 			System.out.println("Main thread Interrupted");
 		}
 		// wait for threads to finish
 		try {
 			System.out.println("Waiting for threads to finish.");
 			ob1.t.join();
 			ob2.t.join();
 		} catch (InterruptedException e) {
 			System.out.println("Main thread Interrupted");
 		}
 		System.out.println("Main thread exiting.");
 	}
 
 }
