 package job;
 
 import java.io.IOException;
 
 import os.Pipe;
 
 public class Endless extends Job{
 
 	public Endless(Integer PID, Pipe stdERR) {
 		super(PID, stdERR);
 	}
 
 	@Override
 	public String getManual() {
 		return "\nI'm endless process.\n\nExample:\n\t endless";
 	}
 
 	@Override
 	protected void getJobDone() throws InterruptedException {
 		
 		String str = "Endless";
 		
 			while(true){
 				sleep(1000);
 				try {
 					pushData(str.toCharArray(), 0, str.length());
 				} catch (IOException e) {
					break;
 				}
 			}
 		}
 	
 
 }
