 package job;
 
 import java.io.IOException;
 
 import os.Pipe;
 
 /**
  * 1) Jobs name will be THE SAME as the program. (Cat will be Cat.java).
  * 
  * 2) Jobs MUST NOT WRITE TO SYS.IN / OUT / ERR no matter what. I believe it is
  * just for testing :-D (You CAN redirect Pipe to System.out, Try it for me please).
  * 		RE: Redirect Pipe to System.out isn't possible with FileWriter but it could be possible with BufferedWritter 
  * 
  * 
  * 4) if you want to add some more methods which job may wants from OS, tell me
  * in Init in comment like I did here or find me in real life and punch me in 
  * da face.
  *
  */
 public class Cat extends Job{
 
	public Cat(Integer PID, Pipe stdERR) {
 		super(PID, stdERR);
 	}
 
 	@Override
 	public String getManual() {
 		// TODO I'll make it better don't worry.
 		return "I'm cute little cat. Meow Meow =^.^=";
 	}
 
 	@Override
 	protected void getJobDone() {
		if(this.arguments == null || this.arguments.length == 0){
 			noArguments();
 		}
 	}
 	
 	private void noArguments(){
 		char [] chars = new char[Pipe.BUF_SIZE];
 		while(getStatus().equals(JobStatus.RUNNING.toString())){
 			int len = 0;
 			try {
 				len = getData(chars);
 			} catch (IOException e) {
 				pushError("Read error");
 				//e.printStackTrace();
 				break;
 			} catch (InterruptedException e) {
 				pushError("Interrupted");
 				//e.printStackTrace();
 				break;
 			}
 			
 			
 			try {
 				pushData(chars, 0, len);
 			} catch (IOException e) {
 				pushError("Write error");
 				//e.printStackTrace();
 				break;
 			} catch (InterruptedException e) {
 				pushError("Interrupted");
 				//e.printStackTrace();
 				break;
 			}
 			
 
 			if(len == -1){
 				break;
 			}
 			
 		}
 	}
 	
 
 }
