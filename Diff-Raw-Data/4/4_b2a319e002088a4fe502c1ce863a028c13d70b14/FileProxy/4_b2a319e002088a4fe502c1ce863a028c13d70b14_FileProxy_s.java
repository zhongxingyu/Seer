 package webfs.model;
 
 import java.io.File;
 
 public class FileProxy {
	private File file;	
 	
 	public FileProxy(File file) {
 		this.file = file;
 	}
 
 	public String getName() {
 		return file.getName();
 	}
 	
 	public boolean isDirectory() {
 		return file.isDirectory();
 	}
 	
 	public long getSize() {
 		return file.length();
 	}
 }
