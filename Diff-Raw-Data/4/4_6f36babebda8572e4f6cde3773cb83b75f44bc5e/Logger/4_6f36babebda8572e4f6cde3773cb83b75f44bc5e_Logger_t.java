 package training.concurrency;
 
 import java.util.LinkedList;
 import java.util.Queue;
 
 public class Logger implements Runnable {
 
 	private final Queue<String> messages = new LinkedList<String>();
 
 	public void log(String message) {
 		messages.add(message);
 	}
 
 	@Override
 	public void run() {
 		while (true) {
			if (messages.isEmpty()) {
				Thread.yield();
			} else {
 				System.out.println(messages.remove());
 			}
 		}
 	}
 
 }
