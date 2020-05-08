 import java.util.concurrent.Semaphore;
 import java.io.*;
 
 
 public class Sem {
 	int Count;
	Semaphore s = new Semaphore(Count);
 	
 	
 	public Sem(int count) {
 		this.Count = count;
 	}
 
 	public void Wait() throws InterruptedException{
 		s.acquire();
 	}
 	
 	public void Signal() throws InterruptedException{
 		s.release();
 	}
 }
