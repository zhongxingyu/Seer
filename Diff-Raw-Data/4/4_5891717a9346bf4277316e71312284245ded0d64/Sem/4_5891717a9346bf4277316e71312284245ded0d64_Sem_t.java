 import java.util.concurrent.Semaphore;
 import java.io.*;
 
 
 public class Sem {
 	int Count;
	Semaphore s;
 	
 	
 	public Sem(int count) {
 		this.Count = count;
                s = new Semaphore(Count);
 	}
 
 	public void Wait() throws InterruptedException{
 		s.acquire();
 	}
 	
 	public void Signal() throws InterruptedException{
 		s.release();
 	}
 }
