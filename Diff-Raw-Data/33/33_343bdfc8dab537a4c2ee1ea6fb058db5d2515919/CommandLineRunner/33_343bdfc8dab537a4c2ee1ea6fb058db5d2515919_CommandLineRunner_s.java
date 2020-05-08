 package jsclassloader.cli;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.PrintStream;
 import java.util.List;
 
 import jsclassloader.Bundler;
 import jsclassloader.Config;
 import jsclassloader.dependency.ClassNode;
 
 
 public class CommandLineRunner {
 
 	static boolean usePropertiesFile;
 	static String propFileName;
 	static List<String> seedClassesFromCmdLine;
 	static String [] sourceFolders;
 	
 	public static void main(String[] args) throws Exception {
 		ArgumentParser parser = new ArgumentParser();
 		Config config = parser.parseArgs(args);
 		
 		Bundler bundler = new Bundler(config);
 		
 		PrintStream out = System.out;
 		
 		if (parser.getOutpuTotFile() != null) {
 			out = new PrintStream(new FileOutputStream(new File(parser.getOutpuTotFile())));
 		}
 		
 		if (parser.isScriptTagMode()) {
 			bundler.writeScriptTags(out, config);
 		}
 		else if (parser.isListMode()) {
 			for (ClassNode item : bundler.getClassList()) {
 				out.println(item.getValue());
 			}
 		}
 		else {
 			bundler.write(out);
 		}
 	}
 }
