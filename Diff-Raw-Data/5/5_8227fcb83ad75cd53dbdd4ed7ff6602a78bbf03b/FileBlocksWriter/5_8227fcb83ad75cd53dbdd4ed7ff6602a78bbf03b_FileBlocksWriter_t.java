 package com.JTFTP;
 
 import java.io.*;
 
 /**
  * This class write blocks in a file.
  */
 public class FileBlocksWriter {
 	private int index;
 	private FileOutputStream output;
 	private int blockLength;
 	private boolean end;
 
 	/**
 	 * Creates a FileBlocksWriter of file specified in filename and writes with blocks of length blockLength
 	 * @param filename is the name of the file.
 	 * @param overwrite indicates wheter overwrite the file if it's exists.
 	 * @param blockLength is the length of blocks
 	 * @throws FileFoundException if file exists and overwrite is false.
 	 * @throws FileNotFoundException if file cannot be opened for any reason.
 	 * @throws SecurityException if can write or create file specified in filename.
 	 */
 	public FileBlocksWriter(String filename, boolean overwrite, int blockLength) throws FileFoundException, FileNotFoundException, SecurityException {
 		File file = new File(filename);
 		
 		if(file.exists() && !overwrite) {
 			throw new FileFoundException("File " + filename + "already exists.");
 		}
 		if(!file.exists()) {
 			try {
 				file.createNewFile();
 			}catch(IOException e) {
				throw new SecurityException("Can't create file " + filename);
 			}
 		}
 		if(!file.canWrite()) {
			throw new SecurityException("Can't write to file " + filename);
 		}
 		this.blockLength = blockLength;
 		index = 0;
 		output = new FileOutputStream(file);
 		end = false;
 	}
 
 	/**
 	 * Free all resources used.
 	 * @throws IOException if there are any problem.
 	 */
 	public void close() throws IOException {
 		output.close();
 	}
 
 	/**
 	 * Tells if the last block already was writted.
 	 * @return if the last block already was writted.
 	 */
 	public boolean hasNext() {
 		return !end;
 	}
 
 	/**
 	 * Return the next bloc index.
 	 * @return the next index.
 	 */
 	public int nextIndex() {
 		return index;
 	}
 
 	/**
 	 * Write the block b to the file.
 	 * @param b is the block to write.
 	 * @throws EOFException if the last block of file already was writted.
 	 * @throws IOException if a general error ocurred while writting.
 	 */
 	public void write(byte[] b) throws IOException {
 		if(end) {
 			throw new EOFException("The last block of file already was writted.");
 		}
 		if(b.length != blockLength) {
 			end = true;
 			if(b.length == 0) {
 				return;
 			}
 		}
 		output.write(b);
 		index++;
 	}
 
 }
