 package com.mtgi.io;
 
 import java.io.EOFException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 
 /**
  * A custom data transfer class that serializes data from a local file to an ObjectOutputStream,
  * which is then stored in a temporary file (rather than memory) on read.  Useful for returning large
  * files from RMI methods (e.g. JMX operations).
  */
 public class RelocatableFile implements Serializable {
 
 	private static final long serialVersionUID = -8888877891952135171L;
 	private File localPath;
 	
 	public RelocatableFile(File localPath) {
 		this.localPath = localPath;
 	}
 	
 	/** get the local path where the file data is stored */
 	public File getLocalFile() {
 		return localPath;
 	}
 
 	/** return the absolute path of the local data file */
 	@Override
 	public String toString() {
 		return localPath.getAbsolutePath();
 	}
 	
 	private void writeObject(ObjectOutputStream out) 
 		throws IOException 
 	{
		out.writeObject(localPath.getName());
 		out.writeLong(localPath.length());
 		
 		byte[] xfer = new byte[4096];
 		FileInputStream fis = new FileInputStream(localPath);
 		try {
 			for (int read = fis.read(xfer); read >= 0; read = fis.read(xfer))
 				out.write(xfer, 0, read);
 		} finally {
 			fis.close();
 		}
 	}
 	
 	private void readObject(ObjectInputStream in) 
 		throws IOException, ClassNotFoundException 
 	{
		String remote = (String)in.readObject();
 		String ext = ".data";
 		int dot = remote.lastIndexOf('.');
 		if (dot > 0) {
 			ext = remote.substring(dot);
 			remote = remote.substring(0, dot);
 		}
 		localPath = File.createTempFile(remote, ext);
 		
 		FileOutputStream fos = new FileOutputStream(localPath);
 		try {
 			long length = in.readLong();
 			byte[] xfer = new byte[4096];
 			while (length > 0) {
 				int read = in.read(xfer, 0, (int)Math.min(xfer.length, length));
 				if (read < 0)
 					throw new EOFException("Unexpected EOF (" + length + " bytes remaining)");
 				fos.write(xfer, 0, read);
 				length -= read;
 			}
 		} finally {
 			fos.close();
 		}
 	}
 }
