 package job;
 
 import java.io.File;
 import java.io.IOException;
 
import javax.swing.Box.Filler;

 import os.Pipe;
 
 /**
  * job/Ls.java
  * <br><br>
  * List directory contents.
  * 
  * @author Jan Masopust
  * 
  * @version 0.1
  * 
  * @team <i>OutOfMemory</i> for KIV/OS 2013
  * @teamLeader Radek Petruška  radekp25@students.zcu.cz
  * 
  */
 public class Ls extends Job{
 	/**
 	 * Constructor.
 	 * @param PID of the new process.
 	 * @param stdERR  where to write errors.
 	 */
 	public Ls(Integer PID, Pipe stdERR) {
 		super(PID, stdERR);
 	}
 
 	@Override
 	public String getManual() {
 		return "Ls is the best.";
 	}
 
 	@Override
 	protected void getJobDone() throws InterruptedException {
		if(currentDirectory != null){
 			if(arguments != null && arguments.length == 0){
 				lsCurrentDirectory();
 			}
 		}
 		else{
			pushError("Shell does not provide the current directory.");
 		}
 			
 			
 	}
 	
 	private void lsCurrentDirectory() throws InterruptedException{
 		File file;
		file =  new File(currentDirectory);
 
 		if ( !file.exists() || !file.canRead() ) {
 			pushError("Can't read " + file);
 			return;
 		}
 		if ( !file.isDirectory() )  {
			pushError("'"+ currentDirectory +"' is not a directory");
 		}
 
 		String [] files = file.list();
 		for(int i = 0; i < files.length; i++){
 			files[i] +=  "\n";
 			try {
 				pushData(files[i].toCharArray(), 0, files[i].length());
 			} catch (IOException e) {
 				pushError("Write error");
 			}
 		}
 	}
 
 }
