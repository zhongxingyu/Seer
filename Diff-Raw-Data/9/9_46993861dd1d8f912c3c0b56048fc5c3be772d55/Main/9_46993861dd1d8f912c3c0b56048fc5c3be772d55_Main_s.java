 package depend;
 
 import java.io.IOException;
 import java.util.Properties;
 
 import com.ibm.wala.classLoader.IClass;
 import com.ibm.wala.classLoader.IMethod;
 import com.ibm.wala.types.ClassLoaderReference;
 import com.ibm.wala.types.Selector;
 import com.ibm.wala.types.TypeReference;
 import com.ibm.wala.util.CancelException;
 import com.ibm.wala.util.WalaException;
 import com.ibm.wala.util.io.CommandLine;
 import com.ibm.wala.util.warnings.Warnings;
 
 import depend.util.SimpleGraph;
 import depend.util.Util;
 
 public class Main {
 
 
   /**
    * example of use for this class
    * 
    * @param args currently hard-coded (modify appJar)
    * 
    * @throws IOException
    * @throws IllegalArgumentException
    * @throws WalaException
    * @throws CancelException
    * @throws InterruptedException 
    */
   public static void main(String[] args) throws IOException, IllegalArgumentException, WalaException, CancelException, InterruptedException {
     
     // reading and saving command-line properties
     Properties p = CommandLine.parse(args);
     Util.setProperties(p);
     
     // clearing warnings from WALA
     Warnings.clear();
     
     // performing dependency analysis
     MethodDependencyAnalysis an = new MethodDependencyAnalysis(p);
     
     // find informed class
     String strClass = Util.getStringProperty("targetClass");
     IClass clazz = an.cha.lookupClass(TypeReference.findOrCreate(ClassLoaderReference.Application, strClass));
     if (clazz == null) {
      throw new RuntimeException("could not find class!");
     }
 
     //  find informed method
     String strMethod = Util.getStringProperty("targetMethod");
     IMethod method = clazz.getMethod(Selector.make(strMethod));
     if (method == null) {
      throw new RuntimeException("could not find class!");
     }
     
     // obtain methods that can write data to this method
     SimpleGraph depGraph = an.getDependencies(method, false, false);
     
     // dump results in file    
     Util.dumpResults(depGraph);
 
   }
 
}
