 package sglr;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel;
 
 public class LegacySGLRInvoker implements IInvoker{
 	private volatile static String binaryPath = "";
 	
 	public static void setBaseBinaryPath(String basePath){
 		if(basePath == null){
 			binaryPath = null;
 			return;
 		}
 			
 		if(basePath.endsWith("/")) binaryPath = basePath;
 		else binaryPath = basePath+"/";
 	}
 	
 	public LegacySGLRInvoker(){
 		super();
		
		if(binaryPath == null){ // Crap code.
			LegacySGLRInvoker.setBaseBinaryPath(System.getProperty("rascal.base.binary.path", "."));
		}
 	}
 	
 	public synchronized byte[] parseFromString(String inputString, String parseTableName){
 		if(inputString == null) throw new IllegalArgumentException("InputString must not be null.");
 		if(parseTableName == null) throw new IllegalArgumentException("ParseTableName must not be null.");
 		
 		try{
 			return reallyParse(inputString.getBytes(), parseTableName);
 		}catch(IOException ioex){
 			throw new RuntimeException("Failed to parse.");
 		}
 	}
 	
 	private byte[] buffer = new byte[8192]; // Shared & locked.
 	
 	public synchronized byte[] parseFromStream(InputStream inputStringStream, String parseTableName) throws IOException{
 		if(inputStringStream == null) throw new IllegalArgumentException("InputStringStream must not be null.");
 		if(parseTableName == null) throw new IllegalArgumentException("ParseTableName must not be null.");
 		
 		ByteArrayOutputStream inputStringData = new ByteArrayOutputStream();
 		
 		int bytesRead;
 		while((bytesRead = inputStringStream.read(buffer)) != -1){
 			inputStringData.write(buffer, 0, bytesRead);
 		}
 		
 		return reallyParse(inputStringData.toByteArray(), parseTableName);
 	}
 	
 	public synchronized byte[] parseFromFile(File inputFile, String parseTableName) throws IOException{
 		if(inputFile == null) throw new IllegalArgumentException("InputFile must not be null.");
 		if(!inputFile.exists()) throw new IllegalArgumentException("InputFile "+inputFile+" does not exist.");
 		if(parseTableName == null) throw new IllegalArgumentException("ParseTableName must not be null.");
 		
 		return reallyParse(fillInputStringBufferFromFile(inputFile), parseTableName);
 	}
 	
 	private byte[] fillInputStringBufferFromFile(File inputFile) throws IOException{
 		ByteBuffer inputStringBuffer = ByteBuffer.allocate((int) inputFile.length());
 		FileInputStream fis = null;
 		try{
 			fis = new FileInputStream(inputFile);
 			FileChannel fc = fis.getChannel();
 			fc.read(inputStringBuffer);
 		}finally{
 			if(fis != null) fis.close();
 		}
 		inputStringBuffer.flip();
 		return inputStringBuffer.array();
 	}
 	
 	private byte[] reallyParse(byte[] data, String parseTable) throws IOException{
 		Process p = null;
 		try{
 			p = Runtime.getRuntime().exec(binaryPath+"sglr -t -p "+parseTable, null, new File(binaryPath));
 			
 			OutputReader outputReader = new OutputReader(p.getInputStream());
 			Thread outputReaderThread = new Thread(outputReader);
 			outputReaderThread.start();
 			
 			OutputStream inputWriter = p.getOutputStream();
 			inputWriter.write(data, 0, data.length);
 			inputWriter.close();
 			
 			if(p.waitFor() != 0) throw new RuntimeException("Parsing failed.");
 			
 			return outputReader.waitForResult();
 		}catch(InterruptedException irex){
 			throw new RuntimeException("Parsing failed.");
 		}finally{
 			if(p != null){
 				p.destroy();
 			}
 		}
 	}
 	
 	private static class OutputReader implements Runnable{
 		private final InputStream inputStream;
 		private volatile byte[] result = null;
 		private volatile boolean errorOccured = false;
 		
 		private final Object lock = new Object();
 		
 		public OutputReader(InputStream inputStream){
 			super();
 			
 			this.inputStream = inputStream;
 		}
 		
 		public void run(){
 			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
 			
 			try{
 				byte[] block = new byte[8192];
 				int bytesRead;
 				while((bytesRead = inputStream.read(block)) != -1){
 					buffer.write(block, 0, bytesRead);
 				}
 				
 				result = buffer.toByteArray();
 			}catch(IOException ioex){
 				errorOccured = true;
 				throw new RuntimeException(ioex);
 			}catch(Throwable t){
 				errorOccured = true;
 				throw new RuntimeException(t);
 			}finally{
 				synchronized(lock){
 					lock.notify();
 				}
 			}
 		}
 		
 		public byte[] waitForResult(){
 			synchronized(lock){
 				while(result == null){
 					if(errorOccured) throw new RuntimeException("Unable to get result, due to the occurrence of an exception");
 					
 					try{
 						lock.wait();
 					}catch(InterruptedException irex){
 						// Ignore.
 					}
 				}
 			}
 			
 			return result;
 		}
 	}
 }
