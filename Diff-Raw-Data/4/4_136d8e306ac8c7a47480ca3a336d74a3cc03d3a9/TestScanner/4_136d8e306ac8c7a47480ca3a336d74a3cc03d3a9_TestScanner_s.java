 package fi.helsinki.cs.tmc.testscanner;
 
 import com.google.gson.Gson;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import javax.annotation.processing.Processor;
 import javax.tools.JavaCompiler;
 import javax.tools.StandardJavaFileManager;
 import javax.tools.ToolProvider;
 
 public class TestScanner {
 
     enum OutputFormat { JSON, TEST_RUNNER }
     
     public static void main(String[] args) {
         if (args.length == 0) {
             showHelp();
             System.exit(1);
         }
         
         OutputFormat outputFormat = OutputFormat.JSON;
 
         TestScanner scanner = new TestScanner();
         for (String arg : args) {
             if (arg.equals("-h") || arg.equals("--help")) {
                 showHelp();
                 System.exit(0);
             } else if (arg.equals("--test-runner-format")) {
                 outputFormat = OutputFormat.TEST_RUNNER;
             }
            scanner.addSource(new File(arg));
         }
 
         List<TestMethod> tests = scanner.findTests();
         if (outputFormat == OutputFormat.JSON) {
             System.out.println(new Gson().toJson(tests));
         } else if (outputFormat == OutputFormat.TEST_RUNNER) {
             System.out.println(toTestRunnerFormat(tests));
         }
     }
 
     private static void showHelp() {
         System.out.println("Usage: java [...] " + TestScanner.class.getName() + " files-or-dirs");
     }
     
     private static String toTestRunnerFormat(List<TestMethod> tests) {
         StringBuilder sb = new StringBuilder();
         for (TestMethod test : tests) {
             sb.append(test.className).append('.').append(test.methodName).append('{');
             commaSeparated(sb, test.points);
             sb.append("}\n");
         }
         return sb.toString();
     }
     
     private static void commaSeparated(StringBuilder sb, String[] elements) {
         if (elements.length > 0) {
             for (int i = 0; i < elements.length - 1; ++i) {
                 sb.append(elements[i]).append(',');
             }
             sb.append(elements[elements.length - 1]);
         }
     }
     
     private ArrayList<File> sourceFiles;
     private JavaCompiler compiler;
     private StandardJavaFileManager fileMan;
     private String classPath;
 
     public TestScanner() {
         sourceFiles = new ArrayList<File>();
         compiler = ToolProvider.getSystemJavaCompiler();
         fileMan = ToolProvider.getSystemJavaCompiler().getStandardFileManager(null, null, null);
         classPath = null;
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
 
     public void setClassPath(String classPath) {
         this.classPath = classPath;
     }
 
     public void clearSources() {
         sourceFiles.clear();
     }
 
     public List<TestMethod> findTests() {
         if (sourceFiles.isEmpty()) {
             return Collections.emptyList();
         }
         
         ArrayList<String> args = new ArrayList<String>();
         args.add("-proc:only");
         
         if (classPath != null) {
             args.add("-cp");
             args.add(classPath);
         }
 
         List<String> options = new ArrayList<String>();
         if (classPath != null) {
             options.add("-classpath");
             options.add(classPath);
         }
         options.add("-proc:only");
         
         JavaCompiler.CompilationTask task = compiler.getTask(
                 null,
                 null,
                 null,
                 options,
                 null,
                 fileMan.getJavaFileObjectsFromFiles(sourceFiles));
 
         TestMethodAnnotationProcessor processor = new TestMethodAnnotationProcessor();
         task.setProcessors(Arrays.asList(new Processor[]{processor}));
         if (!task.call()) {
             throw new RuntimeException("Compilation failed");
         }
         return stableSortedByClassName(processor.getTestMethods());
     }
     
     private List<TestMethod> stableSortedByClassName(List<TestMethod> unsorted) {
         ArrayList<TestMethod> methods = new ArrayList<TestMethod>(unsorted);
         Collections.sort(methods, new Comparator<TestMethod>() {
             public int compare(TestMethod m1, TestMethod m2) {
                 return m1.className.compareTo(m2.className);
             }
         });
         return Collections.unmodifiableList(methods);
     }
 }
