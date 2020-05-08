 package com.web.server;
 
 import java.io.IOException;
 import java.net.URI;
 import java.util.Arrays;
 import java.util.Locale;
 import java.util.logging.Logger;
 
 import javax.tools.Diagnostic;
 import javax.tools.DiagnosticCollector;
 import javax.tools.JavaCompiler;
 import javax.tools.JavaCompiler.CompilationTask;
 import javax.tools.JavaFileObject;
 import javax.tools.SimpleJavaFileObject;
 import javax.tools.StandardJavaFileManager;
 import javax.tools.ToolProvider;
 
 /**
  * A test class to test dynamic compilation API.
  * 
  */
 public class JavaCompilerWebServer {
 	/**
 	 * Does the required object initialization and compilation.
 	 */
 	public void doCompilation (String outputDirectory,String codeToCompile,String pack,String classpath){
		System.setProperty("java.home", "C:\\Program Files\\Java\\jdk1.7.0_03");
 		System.out.println(System.getProperty("java.home"));
 		String oldClasspath=System.getProperty("java.class.path");
 		//System.out.println("oldClasspath:"+oldClasspath);
 		//System.out.println("newClasspath:"+classpath);
		//System.setProperty("java.class.path",oldClasspath+classpath);
 		
 		//System.out.println(System.getProperty("classpath"));
 		try {
 			Class.forName("com.sun.tools.javac.api.JavacTool");
 		} catch (ClassNotFoundException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		/*Creating dynamic java source code file object*/
 		SimpleJavaFileObject fileObject = new DynamicJavaSourceCodeObject (pack, codeToCompile) ;
 		JavaFileObject javaFileObjects[] = new JavaFileObject[]{fileObject} ;
 		
 		/*Instantiating the java compiler*/
 		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
 		//compiler.run();
 		
 		/**
 		 * Retrieving the standard file manager from compiler object, which is used to provide
 		 * basic building block for customizing how a compiler reads and writes to files.
 		 * 
 		 * The same file manager can be reopened for another compiler task. 
 		 * Thus we reduce the overhead of scanning through file system and jar files each time 
 		 */
 		StandardJavaFileManager stdFileManager = compiler.getStandardFileManager(null, Locale.getDefault(), null);
 		
 		/* Prepare a list of compilation units (java source code file objects) to input to compilation task*/
 		Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(javaFileObjects);
 		
 		/*Prepare any compilation options to be used during compilation*/
 		//In this example, we are asking the compiler to place the output files under bin folder.
 		String[] compileOptions = new String[]{"-d", outputDirectory} ;
 		Iterable<String> compilationOptionss = Arrays.asList(compileOptions);
 		
 		/*Create a diagnostic controller, which holds the compilation problems*/
 		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
 		
 		/*Create a compilation task from compiler by passing in the required input objects prepared above*/
 		CompilationTask compilerTask = compiler.getTask(null, stdFileManager, diagnostics, compilationOptionss, null, compilationUnits) ;
 		
 		//Perform the compilation by calling the call method on compilerTask object.
 		boolean status = compilerTask.call();
 		
 		if (!status){//If compilation error occurs
 			/*Iterate through each compilation problem and print it*/
 			for (Diagnostic diagnostic : diagnostics.getDiagnostics()){
 				System.out.format("Error on line %d in %s", diagnostic.getLineNumber(), diagnostic);
 			}
 		}
 		try {
 			stdFileManager.close() ;//Close the file manager
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		System.setProperty("java.class.path",oldClasspath);
 	}
 }
 
 /**
  * Creates a dynamic source code file object
  * 
  * This is an example of how we can prepare a dynamic java source code for compilation.
  * This class reads the java code from a string and prepares a JavaFileObject
  * 
  */
 class DynamicJavaSourceCodeObject extends SimpleJavaFileObject{
 	private String qualifiedName ;
 	private String sourceCode ;
 	
 	/**
 	 * Converts the name to an URI, as that is the format expected by JavaFileObject
 	 * 
 	 * 
 	 * @param fully qualified name given to the class file
 	 * @param code the source code string
 	 */
 	protected DynamicJavaSourceCodeObject(String name, String code) {
 		super(URI.create("string:///" +name.replace(".", "/") + Kind.SOURCE.extension), Kind.SOURCE);
 		this.qualifiedName = name ;
 		this.sourceCode = code ;
 	}
 	
 	@Override
 	public CharSequence getCharContent(boolean ignoreEncodingErrors)
 			throws IOException {
 		return sourceCode ;
 	}
 
 	public String getQualifiedName() {
 		return qualifiedName;
 	}
 
 	public void setQualifiedName(String qualifiedName) {
 		this.qualifiedName = qualifiedName;
 	}
 
 	public String getSourceCode() {
 		return sourceCode;
 	}
 
 	public void setSourceCode(String sourceCode) {
 		this.sourceCode = sourceCode;
 	}
 }
