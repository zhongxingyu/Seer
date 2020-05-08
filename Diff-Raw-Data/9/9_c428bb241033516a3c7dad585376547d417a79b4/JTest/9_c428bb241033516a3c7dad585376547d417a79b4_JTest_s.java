 import java.util.*;
 import java.util.concurrent.locks.*;
 
 public class JTest {
     public static void main(String ... args) {
 	Collection<String> cs = new ArrayList<String>();
 	cs.add("abc");
 	//String[] b = (String[]) cs.toArray();
 	Thread myThread = new Thread(new MyTask(0));
 	Thread otherThread = new Thread(new MyTask(1));
 	myThread.start();
 	otherThread.start();
     }
 }
 
 class MyTask implements Runnable {
     public static int i = 0;
     private int j = 0;
     private Lock myLock = new ReentrantLock();
 
     public MyTask(int j) {
 	this.j = j;
     }
 
     public void run() {
 	while(true) {
 	    myLock.lock();
	    i = j;
	    System.out.println("j:" + j + " i:" + i);
	    myLock.unlock();
 	}
     }
 }
