 package javax.tools;
 
 import java.io.*;
 import java.util.*;
 import java.nio.charset.*;
 
 public class CompileTest {
     public static void main(String[] args) throws Exception {
         boolean useRun = args.length > 0 && "useRun".equals(args[0]);
       
         JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
         System.out.println("compiler: " + compiler.getClass());        
         File outputFolder = new File("build/tmp");
         if (!outputFolder.exists()) {
             outputFolder.mkdir();
         }
         
         if (useRun) {
             System.out.println(String.format("Using compiler.run() instead of compiler.getTask().  Output dir: %s", outputFolder.getAbsolutePath()));
            compiler.run(null, System.out, System.err, "-d", outputFolder.getAbsolutePath(), "src/main/java/Test.java");
         } else {
             try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null)) {                
                 fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(outputFolder));
                 
                 Iterable<? extends JavaFileObject> compilationUnits1 =
                            fileManager.getJavaFileObjectsFromFiles(Arrays.asList(new File("src/main/java/Test.java")));
         
                 JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, null, Arrays.asList("-encoding", "UTF-8", "-source", "1.7"), null, compilationUnits1);
                 System.out.println("task: " + task.getClass());               
                task.call();
             }
         }
     }
 }
