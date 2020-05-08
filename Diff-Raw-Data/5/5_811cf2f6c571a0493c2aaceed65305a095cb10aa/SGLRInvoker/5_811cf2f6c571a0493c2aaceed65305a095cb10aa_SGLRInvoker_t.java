 package sglr;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel;
 
 public class SGLRInvoker implements Runnable{
 	private final static String NO_INPUT_PATH = "_noPath_";
 	
 	private volatile static SGLRInvoker instance = null;
 	
 	private volatile static String baseLibraryPath = null;
 	
 	private final NotifiableLock parserLock = new NotifiableLock();
 	private final NotifiableLock parserDoneLock = new NotifiableLock();
 	private volatile boolean running;
 	
 	private ByteBuffer inputString;
 	private int inputStringLength;
 	private String inputPath;
 	private String parseTableName;
 	
 	private byte[] result;
 	
 	private SGLRInvoker(){
 		super();
 		
 		running = true;
 	}
 	
 	public static void setBaseLibraryPath(String basePath){
 		if(basePath == null){
 			baseLibraryPath = null;
 			return;
 		}
 			
 		if(basePath.endsWith("/")) baseLibraryPath = basePath;
 		else baseLibraryPath = basePath+"/";
 	}
 	
 	public static void loadLibraries(){
 		if(baseLibraryPath == null){ // Look in the library path to find the libraries.
 			try{
 		        System.loadLibrary("ATerm");
 		        System.loadLibrary("ConfigAPI");
 		        System.loadLibrary("ErrorAPI");
 		        System.loadLibrary("LocationAPI");
 		        System.loadLibrary("ATB");
 		        System.loadLibrary("mept");
 		        System.loadLibrary("PTMEPT");
 		        System.loadLibrary("ptable");
 		        System.loadLibrary("logging");
 		        System.loadLibrary("statistics");
 		        System.loadLibrary("sglr");
 		        System.loadLibrary("SGLRInvoker");
 		    }catch(UnsatisfiedLinkError ule){
 		        throw new RuntimeException(ule);
 		    }
 		}else{ // Look in the given absolute path (OS specific).
 			String osName = System.getProperty("os.name");
 			
			if(osName.indexOf("Linux") != -1){ // Linux.
 				try{
 			        System.load(baseLibraryPath+"libCompleteSGLR.so");
 			    }catch(UnsatisfiedLinkError ule){
 			        throw new RuntimeException(ule);
 			    }
			}else if(osName.indexOf("Mac") != -1 || osName.indexOf("Darwin") != -1){ // Mac.
 				try{
 			        System.load(baseLibraryPath+"libCompleteSGLR.dylib");
 			    }catch(UnsatisfiedLinkError ule){
 			        throw new RuntimeException(ule);
 			    }
 			}else if(osName.startsWith("Win")){ // Windows.
 				try{
 			        System.load(baseLibraryPath+"CompleteSGLR.dll");
 			    }catch(UnsatisfiedLinkError ule){
 			        throw new RuntimeException(ule);
 			    }
 			}else{
 				throw new RuntimeException("Unknown OS. Unable to load libraries for SGLR Invoker.");
 			}
 		}
 	}
 	
 	public static SGLRInvoker getInstance(){
 		if(instance == null){
 			synchronized(SGLRInvoker.class){
 				if(instance == null){ // Yes DCL works on volatiles.
 					loadLibraries();
 					
 					instance = new SGLRInvoker();
 					
 					Thread parserThread = new Thread(instance);
 					parserThread.setDaemon(true);
 					parserThread.start();
 				}
 			}
 		}
 		
 		return instance;
 	}
 	
 	public void stop(){
 		running = false;
 		
 		synchronized(parserLock){
 			parserLock.notified = true;
 			parserLock.notify();
 		}
 	}
 	
 	public void run(){
 		initialize();
 		
 		while(running){
 			synchronized(parserLock){
 				while(!parserLock.notified){
 					try{
 						parserLock.wait();
 					}catch(InterruptedException irex){
 						// Ignore this; we don't want to know.
 					}
 					
 					if(!running) return;
 				}
 				parserLock.notified = false;
 			}
 			
 			// Invoke the parser.
 			ByteBuffer resultBuffer = parse();
 			
 			// Construct the result.
 			byte[] data = new byte[resultBuffer.capacity()];
 			resultBuffer.get(data);
 			result = data;
 			
 			// Cleanup.
 			inputString = null;
 			parseTableName = null;
 			
 			synchronized(parserDoneLock){
 				parserDoneLock.notified = true;
 				parserDoneLock.notify();
 			}
 		}
 	}
 	
 	public synchronized byte[] parseFromString(String inputString, String parseTableName){
 		if(inputString == null) throw new IllegalArgumentException("InputString must not be null.");
 		if(parseTableName == null) throw new IllegalArgumentException("ParseTableName must not be null.");
 		
 		return reallyParse(fillInputStringBufferFromBytes(inputString.getBytes()), NO_INPUT_PATH, parseTableName);
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
 		
 		return reallyParse(fillInputStringBufferFromBytes(inputStringData.toByteArray()), NO_INPUT_PATH, parseTableName);
 	}
 	
 	public synchronized byte[] parseFromFile(File inputFile, String parseTableName) throws IOException{
 		if(inputFile == null) throw new IllegalArgumentException("InputFile must not be null.");
 		if(!inputFile.exists()) throw new IllegalArgumentException("InputFile "+inputFile+" does not exist.");
 		if(parseTableName == null) throw new IllegalArgumentException("ParseTableName must not be null.");
 		
 		return reallyParse(fillInputStringBufferFromFile(inputFile), inputFile.getAbsolutePath(), parseTableName);
 	}
 	
 	private ByteBuffer cachedInputStringBuffer = ByteBuffer.allocateDirect(65536);
 	
 	private ByteBuffer getInputStringBuffer(int requiredSize){
 		int cachedBufferCapacity = cachedInputStringBuffer.capacity();
 		if(requiredSize > cachedBufferCapacity){
 			cachedInputStringBuffer = ByteBuffer.allocateDirect(requiredSize);
 		}
 		
 		cachedInputStringBuffer.clear();
 		
 		return cachedInputStringBuffer;
 	}
 	
 	private ByteBuffer fillInputStringBufferFromBytes(byte[] inputStringData){
 		ByteBuffer inputStringBuffer = getInputStringBuffer(inputStringData.length);
 		inputStringBuffer.put(inputStringData);
 		inputStringBuffer.flip();
 		
 		return inputStringBuffer;
 	}
 	
 	private ByteBuffer fillInputStringBufferFromFile(File inputFile) throws IOException{
 		ByteBuffer inputStringBuffer = getInputStringBuffer((int) inputFile.length()); // If you want to parse something larger then 2GB it won't work anyway.
 		FileInputStream fis = null;
 		try{
 			fis = new FileInputStream(inputFile);
 			FileChannel fc = fis.getChannel();
 			fc.read(inputStringBuffer);
 		}finally{
 			if(fis != null) fis.close();
 		}
 		inputStringBuffer.flip();
 		return inputStringBuffer;
 	}
 	
 	private byte[] reallyParse(ByteBuffer inputStringBuffer, String inputPath, String parseTableName){
 		synchronized(parserDoneLock){
 			this.inputString = inputStringBuffer;
 			this.inputStringLength = inputStringBuffer.limit();
 			this.inputPath = inputPath;
 			this.parseTableName = parseTableName;
 			
 			synchronized(parserLock){
 				parserLock.notified = true;
 				parserLock.notify();
 			}
 		
 			/* Wait for it. */
 			while(!parserDoneLock.notified){
 				try{
 					parserDoneLock.wait();
 				}catch(InterruptedException irex){
 					// Ignore this; we don't want to know.
 				}
 			}
 			parserDoneLock.notified = false;
 		}
 		
 		byte[] parseTree = result;
 		
 		result = null; // Cleanup.
 		
 		return parseTree;
 	}
 	
 	private ByteBuffer getInputString(){
 		return inputString;
 	}
 	
 	private int getInputStringLength(){
 		return inputStringLength;
 	}
 	
 	private String getInputPath(){
 		return inputPath;
 	}
 	
 	private String getParseTableName(){
 		return parseTableName;
 	}
 	
 	private native void initialize();
 	
 	private native ByteBuffer parse();
 	
 	private static class NotifiableLock{
 		public boolean notified = false;
 	}
 }
