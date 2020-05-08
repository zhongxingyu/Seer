 package test;
 
 import java.io.FileWriter;
 import java.io.IOException;
 
 import job.*;
 import os.Pipe;
 
 public class JobTesting {
 	
 	public static void main(String[] args) throws IOException{
 		
 		/**Cat testing
 		Pipe pipeIn = new Pipe(new FileReader("testData/data02.txt"), null);	
 
 		Pipe pipeErr = new Pipe(null, new FileWriter("testData/data02err.txt"));
 		
 		Pipe pipeOut = new Pipe(null, new FileWriter("testData/data02out.txt"));
 				
 		
 		Job cat = new Cat(Integer.valueOf(333), pipeErr);
 		//cat.setStdIn(pipeIn);
 		cat.setArguments(new String[]{"testData/shortData01.txt", "testData/fakeData.txt","testData/shortData02.txt", "Bum Bum"});
 		cat.setStdOut(pipeOut);
 		
 		cat.start();
 		**/
 		
 		/** Echo testing
 		Pipe pipeErr = new Pipe(null, null);
 		Pipe pipeOut = new Pipe(null, new FileWriter("testData/data02out.txt"));
 
 		Job echo = new Echo(Integer.valueOf(333), pipeErr);
 		echo.setStdOut(pipeOut);
 		
 		echo.setArguments(new String[]{"Bem", "Bam", "Bum Bum", "-1", "I am a long text without a meaning."});
 		//echo.setArguments(new String[]{"Bem"});
 		echo.start();**/
 		
 		//Ls testing
 		Pipe pipeErr = new Pipe(null, new FileWriter("testData/data02err.txt"));
 		Pipe pipeOut = new Pipe(null, new FileWriter("testData/data02out.txt"));
 
 		Job ls = new Ls(Integer.valueOf(333), pipeErr);
 		ls.setStdOut(pipeOut);
		ls.setArguments(new String[]{"testData/"});
 		
 		ls.start();
 		
 	}
 }
