 /**
  * 
  */
 package server;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 
 import model.NamedPipeStream;
 
 /**
  * @author Derek Carr
  *
  */
 public class NamedPipe {
 	private RandomAccessFile pipe;
 	
 	public NamedPipe() throws FileNotFoundException {
 		try {
			Runtime.getRuntime().exec("rm pipe && mkfifo pipe");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		pipe = new RandomAccessFile("./pipe", "rw");		
 	}
 
 	public NamedPipeStream readPipe() throws IOException {
 		String next =  pipe.readLine();
 
 		NamedPipeStream stream;
 		if (next != null) {
 
 			stream = new NamedPipeStream(next);
 		} else {
 			next = null;
 			stream = null;
 		}
 		return stream;
 	}
 }
