 package noyd.compile;
 
 import java.util.Arrays;
 import java.util.Locale;
 
 import javax.tools.Diagnostic;
 import javax.tools.DiagnosticCollector;
 import javax.tools.JavaCompiler;
 import javax.tools.JavaFileObject;
 import javax.tools.StandardJavaFileManager;
 import javax.tools.ToolProvider;
 
 public class Compiler {
 
        public static String[] compileErrors(String file) {
 
         //
         // Compile and collect Diagnostic
         // http://stackoverflow.com/questions/11298856/syntax-checking-in-java
         //
 
         JavaCompiler compiler
             = ToolProvider.getSystemJavaCompiler();
 
         StandardJavaFileManager fileManager
             = compiler.getStandardFileManager(null, null, null);
 
         Iterable<? extends JavaFileObject> compilationUnits
             = fileManager.getJavaFileObjectsFromStrings(Arrays.asList(file));
 
         DiagnosticCollector<JavaFileObject> diagnostics
             = new DiagnosticCollector<JavaFileObject>();
 
         compiler.getTask(
 
             null, 
             fileManager, 
             diagnostics, 
             null, 
             null, 
             compilationUnits
 
         ).call();
 
         //
         // Assemble String[] array of errors
         //
 
         int i = 0;
         String[] errors = new String[diagnostics.getDiagnostics().size()];
 
         for (Diagnostic diagnostic : diagnostics.getDiagnostics())
 
             errors[i++] = String.format(
 
                "%s\tLine[%s,%s]\t%s", 
                 diagnostic.getKind(),
                 diagnostic.getLineNumber(),
                 diagnostic.getPosition(),
                 diagnostic.getMessage(Locale.ROOT)
 
             );
                 
         return errors;
 
     }
 
 }
