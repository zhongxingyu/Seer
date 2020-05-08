 package fi.helsinki.cs.tmc.testscanner;
 
 import com.google.gson.Gson;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
import java.util.Collections;
 import java.util.List;
 import javax.annotation.processing.Processor;
 import javax.tools.JavaCompiler;
 import javax.tools.StandardJavaFileManager;
 import javax.tools.ToolProvider;
 
 public class TestScanner {
 	
 	public static void main(String[] args) {
 		if (args.length == 0) {
 			showHelp();
 			System.exit(1);
 		}
 		
 		TestScanner scanner = new TestScanner();
 		for (String arg : args) {
 			if (arg.equals("-h") || arg.equals("--help")) {
 				showHelp();
 				System.exit(0);
 			}
 			scanner.addSource(new File(arg));
 		}
 		
 		List<TestMethod> tests = scanner.findTests();
 		System.out.println(new Gson().toJson(tests));
 	}
 	
 	private static void showHelp() {
 		System.out.println("Usage: java ... " + TestScanner.class + " files-or-dirs");
 		
 	}
 	
 	private ArrayList<File> sourceFiles;
 	private JavaCompiler compiler;
 	private StandardJavaFileManager fileMan;
 
 	public TestScanner() {
 		sourceFiles = new ArrayList<File>();
 		compiler = ToolProvider.getSystemJavaCompiler();
 		fileMan = ToolProvider.getSystemJavaCompiler().getStandardFileManager(null, null, null);
 	}
 	
 	public void addSource(File fileOrDir) {
 		if (fileOrDir.isDirectory()) {
 			for (File entry : fileOrDir.listFiles()) {
 				addSource(entry);
 			}
 		} else {
 			if (fileOrDir.getPath().endsWith(".java")) {
 				sourceFiles.add(fileOrDir);
 			}
 		}
 	}
 	
 	public void clearSources() {
 		sourceFiles.clear();
 	}
 	
 	public List<TestMethod> findTests() {
		if (sourceFiles.isEmpty()) {
			return Collections.emptyList();
		}
		
 		JavaCompiler.CompilationTask task = compiler.getTask(
 				null,
 				null,
 				null,
 				Arrays.asList(new String[] {"-proc:only"}),
 				null,
 				fileMan.getJavaFileObjectsFromFiles(sourceFiles)
 				);
 		
 		TestMethodAnnotationProcessor processor = new TestMethodAnnotationProcessor();
 		task.setProcessors(Arrays.asList(new Processor[] {processor}));
 		if (!task.call()) {
 			throw new RuntimeException("Compilation failed");
 		}
 		return processor.getTestMethods();
 	}
 }
