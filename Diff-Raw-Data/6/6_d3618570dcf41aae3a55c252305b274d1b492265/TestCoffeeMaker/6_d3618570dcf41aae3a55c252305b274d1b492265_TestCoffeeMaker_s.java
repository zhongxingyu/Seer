 package coffeemaker;
 
 import japa.parser.ParseException;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Properties;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import com.ibm.wala.classLoader.IClass;
 import com.ibm.wala.classLoader.IMethod;
 import com.ibm.wala.shrikeCT.InvalidClassFileException;
 import com.ibm.wala.util.CancelException;
 import com.ibm.wala.util.WalaException;
 import com.ibm.wala.util.io.CommandLine;
 import com.ibm.wala.util.warnings.Warnings;
 
 import core.Helper;
 import depend.MethodDependencyAnalysis;
 import depend.util.Util;
 import depend.util.graph.SimpleGraph;
 
 public class TestCoffeeMaker {
   
   String USER_DIR = System.getProperty("user.dir");
   String SEP = System.getProperty("file.separator");
 
   @Test
   public void test0() throws IOException, WalaException, CancelException, ParseException, InvalidClassFileException {
 
     String strCompUnit = USER_DIR + SEP + "src-examples/coffeemaker/CoffeeMaker.java";
     
     Assert.assertTrue((new File(strCompUnit)).exists());
     
     String line = "if(addRecipe(newRecipe)) {";
     
     String coffeejar = USER_DIR + SEP + "coffee.jar";
     
     // check
     Assert.assertTrue((new File(strCompUnit)).exists());
 
     SimpleGraph sg = depend.Main.analyze(coffeejar, "coffee", strCompUnit, line);
         
     String expectedResultFile = USER_DIR + SEP + "src-tests/coffeemaker/TestCoffeeMaker.test0.data";
     
     // check
     Assert.assertEquals(Helper.readFile(expectedResultFile), sg.toDotString());
   }
 
   @Test
   public void testAnalysisWithLineContents() throws Exception {
     String strCompUnit = USER_DIR + SEP + "src-examples/coffeemaker/CoffeeMaker.java";
     String USER_DIR1 = System.getProperty("user.dir");
     String SEP1 = System.getProperty("file.separator");
     // default values
     String exclusionFile = USER_DIR1 + SEP1 + "dat" + SEP1
         + "ExclusionAllJava.txt";
     String exclusionFileForCallGraph = USER_DIR1 + SEP1 + "dat" + SEP1
         + "exclusionFileForCallGraph";
     String dotPath = "/usr/bin/dot";
 
     Assert.assertTrue((new File(strCompUnit)).exists());
 
     String targetClass = "Lcoffeemaker/CoffeeMaker";
     String targetMethod = "editRecipe(Lcoffeemaker/Recipe;Lcoffeemaker/Recipe;)Z";
 
 
     String[] args = new String[] { "-appJar=" + "coffee.jar",
         "-printWalaWarnings=" + false, "-exclusionFile=" + exclusionFile,
         "-exclusionFileForCallGraph=" + exclusionFileForCallGraph,
         "-dotPath=" + dotPath, "-appPrefix=" + "coffee",
         "-listAppClasses=" + false, "-listAllClasses=" + false,
         "-listAppMethods=" + false, "-genCallGraph=" + false,
         "-measureTime=" + false, "-reportType=" + "dot",
         "-targetClass=" + targetClass, "-targetMethod=" + targetMethod,
         "-targetLine=99"};
     // reading and saving command-line properties
     Properties p = CommandLine.parse(args);
     Util.setProperties(p);
 
     // clearing warnings from WALA
     Warnings.clear();
 
     MethodDependencyAnalysis mda = new MethodDependencyAnalysis(p);
 
     // find informed class    
     IClass clazz = depend.Main.findClass(mda);
     //  find informed method
     IMethod method = depend.Main.findMethod(clazz);
     SimpleGraph sg = depend.Main.run(mda, method);
     
     String expectedResultFile = USER_DIR + SEP + "src-tests/coffeemaker/TestCoffeeMaker.testAnalysisWithLineContents.data";
     
     Assert.assertEquals(Helper.readFile(expectedResultFile), sg.toDotString());
   } 
 }
