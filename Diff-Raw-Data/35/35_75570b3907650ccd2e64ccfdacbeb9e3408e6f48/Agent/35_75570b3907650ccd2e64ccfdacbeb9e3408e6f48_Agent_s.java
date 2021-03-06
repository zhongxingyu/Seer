 package org.deuce.transform.asm;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.lang.instrument.ClassFileTransformer;
 import java.lang.instrument.IllegalClassFormatException;
 import java.lang.instrument.Instrumentation;
 import java.security.ProtectionDomain;
 import java.util.jar.JarEntry;
 import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.deuce.reflection.UnsafeHolder;
 import org.deuce.transform.Exclude;
 import org.deuce.transform.util.IgnoreTree;
 
 /**
  * A java agent to dynamically instrument transactional supported classes/  
  * 
  * @author Guy Korland
  * @since 1.0
  */
 @Exclude
 public class Agent implements ClassFileTransformer {
 	private static final Logger logger = Logger.getLogger("org.deuce.agent");
 	final private static boolean VERBOSE = Boolean.getBoolean("org.deuce.verbose");
 	final private static boolean GLOBAL_TXN = Boolean.getBoolean("org.deuce.transaction.global");
 	final public static IgnoreTree IGNORE_TREE;
 	static
 	{
 		String property = System.getProperty("org.deuce.exclude");
 		if( property == null)
 			property = "java.*,sun.*,org.eclipse.*,org.junit.*,junit.*";
 		IGNORE_TREE = new IgnoreTree( property);
 	}
 
 	/*
 	 * @see java.lang.instrument.ClassFileTransformer#transform(java.lang.ClassLoader,
 	 *      java.lang.String, java.lang.Class, java.security.ProtectionDomain,
 	 *      byte[])
 	 */
 	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
 			ProtectionDomain protectionDomain, byte[] classfileBuffer)
 	throws IllegalClassFormatException {
 		try {
 			// Don't transform classes from the boot classLoader.
 			if (loader != null)
 				return transform(className, classfileBuffer);
 		}
 		catch(Exception e) {
 			logger.log( Level.SEVERE, "Fail on class transform: " + className, e);
 		}
 		return classfileBuffer;
 	}
 	
 	private byte[] transform(String className, byte[] classfileBuffer)
 	throws IllegalClassFormatException {
 
 		if (className.startsWith("$") || IGNORE_TREE.contains(className)) 
 			return classfileBuffer;
 		
 		if (logger.isLoggable(Level.FINER))
 			logger.finer("Transforming: Class=" + className);
 
 		classfileBuffer = addFrames(className, classfileBuffer);
 
 		ByteCodeVisitor cv;
 		if( GLOBAL_TXN)
 			cv = new org.deuce.transaction.global.ClassTransformer( className); 
 		else
 			cv = new org.deuce.transform.asm.ClassTransformer( className); 
 
 		byte[] bytecode = cv.visit(classfileBuffer);
 
 		if( VERBOSE){
 			try {
				verbose(className, bytecode);
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return bytecode;
 	}
 
 	/**
 	 * Reads the bytecode and calculate the frames, to support 1.5- code.
 	 * 
 	 * @param className class to manipulate 
 	 * @param classfileBuffer original byte code
 	 *  
 	 * @return bytecode with frames
 	 */
 	private byte[] addFrames(String className, byte[] classfileBuffer) {
 
 		try{
 			FramesCodeVisitor frameCompute = new FramesCodeVisitor( className);
 			return frameCompute.visit( classfileBuffer); // avoid adding frames to Java6
 		}
 		catch( FramesCodeVisitor.VersionException ex){
 			return classfileBuffer;
 		}
 	}
 
 	public static void premain(String agentArgs, Instrumentation inst) throws Exception{
 		UnsafeHolder.getUnsafe();
 		logger.fine("Starting Duece agent");
 		inst.addTransformer(new Agent());
 	}
 	
 	/**
 	 * Used for offline instrumentation.
 	 * @param args input jar & output jar
 	 * e.g.: "C:\Java\jdk1.5.0_13\jre\lib\rt.jar" "C:\rt.jar"
 	 * @throws Exception
 	 */
 	public static void main(String[] args) throws Exception{
 		UnsafeHolder.getUnsafe();
 		logger.fine("Starting Duece translator");
 		
 		// TODO check args
 		Agent agent = new Agent();
 		agent.transformJar(args[0], args[1]);
 	}
 	
 	private void transformJar( String inFileName, String outFilename) throws IOException, IllegalClassFormatException {
 		
 		final int size = 4096;
 		byte[] buffer = new byte[size];
 		ByteArrayOutputStream baos = new ByteArrayOutputStream(size);
 		
 		JarInputStream jarIS = new JarInputStream(new FileInputStream(inFileName));
		JarOutputStream jarOS = new JarOutputStream(new FileOutputStream(outFilename), jarIS.getManifest());
 		
 		String nextName = "";
 		try {
 			for (JarEntry nextJarEntry = jarIS.getNextJarEntry(); nextJarEntry != null;
 								nextJarEntry = jarIS.getNextJarEntry()) {
 				
 				baos.reset();
 				int read;
 				while ((read = jarIS.read(buffer, 0, size)) > 0) {
 					baos.write(buffer, 0, read);
 				}
				byte[] byteArray = baos.toByteArray();
 				
 				nextName = nextJarEntry.getName();
				if( nextName.endsWith(".class")){
 					if( logger.isLoggable(Level.FINE)){
 						logger.fine("Transalating " + nextName);
 					}
					JarEntry transformedEntry = new JarEntry(nextName);
					jarOS.putNextEntry( transformedEntry); 
					jarOS.write( transform( nextName, byteArray));
				}
				else{
					jarOS.putNextEntry( nextJarEntry);
					jarOS.write(byteArray);
 				}
 			}
 		}
 		catch(Exception e){
 			logger.log(Level.SEVERE, "Failed to translate " + nextName, e);
 		}
 		finally {
 			jarIS.close();
			jarOS.close();
 		}
 	}
 	
	private void verbose(String className, byte[] bytecode) throws FileNotFoundException,
 	IOException {
		File file = new File( "verbose");
 		file.mkdir();
 
 		String[] packages = className.split("/");
 		for( int i=0 ; i<packages.length-1 ; ++i){
 			file = new File( file, packages[i]);
 			file.mkdir();
 		}
 		file = new File( file, packages[packages.length -1]);
 		FileOutputStream fs = new FileOutputStream( file + ".class");
 		fs.write(bytecode);
 		fs.close();
 	}
 }
