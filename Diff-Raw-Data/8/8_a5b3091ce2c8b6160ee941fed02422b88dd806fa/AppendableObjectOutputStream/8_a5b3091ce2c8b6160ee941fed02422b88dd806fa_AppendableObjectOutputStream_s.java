 package com.github.schali.util.io;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 
 public class AppendableObjectOutputStream extends ObjectOutputStream{
 
	public AppendableObjectOutputStream(OutputStream out) throws IOException {
 		super(out);
 	}
 	
 	@Override
 	protected void writeStreamHeader() throws IOException {
 		//omit header to avoid corrupting the stream/file
 		System.out.println("omitting header for apppending to file");
 	}
 	
 	public static ObjectOutputStream createObjectOutputStreamForFile(String filename) throws FileNotFoundException, IOException {
 		File file = new File(filename);
 		return createObjectOutputStreamForFile(file);
 	}
 	
 	public static ObjectOutputStream createObjectOutputStreamForFile(File file) throws FileNotFoundException, IOException {
 		if(file.exists() && file.length() > 0)
 			return new AppendableObjectOutputStream(new FileOutputStream(file, true));
 		else
 			return new ObjectOutputStream(new FileOutputStream(file));
 	}
 	
 }
