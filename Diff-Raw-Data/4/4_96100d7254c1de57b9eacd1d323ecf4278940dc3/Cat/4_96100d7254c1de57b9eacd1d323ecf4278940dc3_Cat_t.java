 package job;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 
import os.Init;
 import os.Pipe;
 
 
 /**
  * job/Cat.java
  * <br><br>
  * Concatenate file(s), or standard input, to standard output.
  * 
  * @author Jan Masopust
  * 
  * @version 0.3
  * 
  * @team <i>OutOfMemory</i> for KIV/OS 2013
  * @teamLeader Radek Petruška  radekp25@students.zcu.cz
  * 
  */
 public class Cat extends Job{
 	/**
 	 * Constructor.
 	 * @param PID of the new process.
 	 * @param stdERR  where to write errors.
 	 */
 	public Cat(Integer PID, Pipe stdERR) {
 		super(PID, stdERR);
 	}
 
 	@Override
 	public String getManual() {
 		// TODO I'll make it better don't worry.
 		return "I'm cute little cat. Meow Meow =^.^=";
 	}
 
 	@Override
 	protected void getJobDone() throws InterruptedException {
 		if(this.arguments == null || this.arguments.length == 0){
 			noArguments();
 		}
 		else{
 			withArguments();
 		}
 	}
 	
 	/**
 	 * Cat concatenate standard input to standard output.
 	 * @throws InterruptedException
 	 */
 	private void noArguments() throws InterruptedException{
 		char [] chars = new char[BUF_SIZE];
 		int len = 0;
 		while(len > -1){
 			try {
 				len = getData(chars);
 			} catch (IOException e) {
 				pushError("Read error");
 				//e.printStackTrace();
 				break;
 			}
 			
 			if(len == -1){
 				return;
 			}
 			
 			try {
 				pushData(chars, 0, len);
 			} catch (IOException e) {
 				pushError("Write error");
 				//e.printStackTrace();
 				break;
 			} 
 			
 			//printChars(chars, len);			
 		}
 	}
 	
 	/**
 	 * Cat concentrate file(s) to standard output.
 	 * @throws InterruptedException
 	 */
 	private void withArguments() throws InterruptedException{
 		char [] chars = new char[BUF_SIZE];
 		for(int i = 0; i < this.arguments.length; i++){
			System.out.println(this.getPath() + Init.FS + this.arguments[i]);
 			try {
 				BufferedReader br;
 				if(new File(this.arguments[i]).isAbsolute())
 					br = new BufferedReader(new FileReader(this.arguments[i]));
 				else
 					br = new BufferedReader(new FileReader(this.getPath() + "/" + this.arguments[i]));
 				
 				int len = 0;
 				while(len >= 0){
 					len = br.read(chars, 0, BUF_SIZE);
 					//printChars(chars, len);
 					
 					if(len == -1)
 						continue;
 					
 					pushData(chars, 0, len);
 				}
 				br.close();
 				
 			} catch (FileNotFoundException e) {
 				pushError("File not found: " + this.arguments[i]);
 			} catch (IOException e) {
 				pushError("Read/Write error");
 			}
 		}
 	}
 	
 	/**
 	 * Just for testing.
 	 * @param chars
 	 * @param len
 	 */
 	@SuppressWarnings("unused")
 	private static void printChars(char[] chars, int len){
 		for(int i = 0; i < len; i++){
 			System.out.print(chars[i]);
 		}
 		System.out.println();
 	}
 }
 	
 
