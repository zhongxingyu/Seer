 import org.antlr.runtime.ANTLRStringStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 import org.antlr.runtime.TokenStream;
 import org.antlr.runtime.tree.CommonTree;
 import java.io.*;
 import org.antlr.runtime.*;
 import org.junit.Test;
 import org.junit.BeforeClass;
 import static org.junit.Assert.*;
 
 
 public class TandemTest
 {
     private static File currentDir = new File(".");
     private static String currentDirName;
     private static String testPath;
     private final static String whitespace = "misc/whitespace/";
     private final static String comments = "misc/comments/";
     private final static String expression = "expression/";
     private final static String statement = "statement/";
     private final static String failure = "failure/";
 
     public static void main(String args[])
     {
         try
         {
             currentDirName = currentDir.getCanonicalPath();
         }
         catch(Exception e)
         {
             e.printStackTrace();
         }
 
         testPath = currentDirName + "/test/";
     }
 
     @BeforeClass
     public static void oneTimeSetUp()
     {
         try
         {
             currentDirName = currentDir.getCanonicalPath();
         }
         catch(Exception e)
         {
             e.printStackTrace();
         }
 
         testPath = currentDirName + "/test/";
     }
 
     public static boolean parseFile(String filename)
     {
         boolean parsing_success = false;
 
         try
         {
             CharStream input = new ANTLRFileStream(filename);
             TanGLexer lexer = new TanGLexer(input);
 
             TokenStream ts = new CommonTokenStream(lexer);
             TanGParser parse = new TanGParser(ts);
             parse.tanG();
 
             int errorsCount = parse.getNumberOfSyntaxErrors();
 
             if(errorsCount == 0)
             {
                 parsing_success = true;
             }
         }
         catch(Exception t)
         {
             // System.out.println("Exception: "+t);
             // t.printStackTrace();
             parsing_success = false;
             return parsing_success;
         }
 
         return parsing_success;
 
     }
 
     public static File[] listTDFiles(File file)
     {
         File[] files = file.listFiles(new FilenameFilter() {
             @Override
             public boolean accept(File dir, String name)
             {
                 if(name.toLowerCase().endsWith(".td"))
                 {
                     return true;
                 }
                 else
                 {
                     return false;
                 }
             }
         });
 
         return files;
     }
 
     public static void run_success(File file)
     {
         File[] files = listTDFiles(file);
 
         for(File f : files)
         {
             if(f != null)
             {
                 // System.out.println(f.getAbsolutePath());
                 assertTrue("Failed to parse " + f.getName(), parseFile(f.getAbsolutePath()) );
             }
         }
     }
 
     public static void run_failure(File file)
     {
         File[] files = listTDFiles(file);
 
         for(File f : files)
         {
             if(f != null)
             {
                 // System.out.println(f.getAbsolutePath());
                 assertFalse("Should not have parsed " + f.getName(), parseFile(f.getAbsolutePath()) );
             }
         }
     }
 
     @Test
     public void test_whitespace()
     {
         // System.out.println(testPath + whitespace);
         File file = new File(testPath + whitespace);
         run_success(file);
     }
 
     @Test
     public void test_comments()
     {
         File file = new File(testPath + comments);
         run_success(file);
     }
 
     @Test
     public void test_expression()
     {
         File file = new File(testPath + expression);
         run_success(file);
     }
 
     @Test
     public void test_statement()
     {
         File file = new File(testPath + statement);
         run_success(file);
     }
 
     @Test
     public void test_failures()
     {
         File file = new File(testPath + failure);
         run_failure(file);
     }
 }
