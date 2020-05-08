 package cs444.acceptance;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Test;
 
 import cs444.Compiler;
 
 public class Asst1 {
 
     @Test
     public void testValidJoosCode() throws IOException, InterruptedException {
         String path = "JoosPrograms/SyntacticallyValidPrograms/";
 
         assertReturnCodeForFiles(path, 0, true);
     }
 
     @Test
     public void testInvalidJoosCode() throws IOException, InterruptedException {
         String path = "JoosPrograms/SyntacticallyInvalidPrograms/";
 
         assertReturnCodeForFiles(path, 42, false);
     }
 
     @Test
     public void testMarmosetTestCases() throws IOException, InterruptedException {
         String path = "JoosPrograms/MarmosetPrograms/a1/valid/";
 
        assertReturnCodeForFiles(path, 0, false);
         
         path = "JoosPrograms/MarmosetPrograms/a1/invalid/";
         
         assertReturnCodeForFiles(path, 42, false);
     }
 
     private void assertReturnCodeForFiles(String path, int expectedReturnCode, boolean printErrors) throws IOException,
                                                                                                            InterruptedException {
         File folder = new File(path);
 
         int totalTests = 0;
         int filesSkipped = 0;
         List<String> failFiles = new ArrayList<String>();
         for (File file : folder.listFiles()) {
             String fileName = file.getName();
 
             // Use this line to test a single file
            //if (!fileName.equals("FinalClasses.java")) continue;
 
             if (file.isFile() && fileName.toLowerCase().endsWith(".java")){
                 if (compileFile(path + fileName, printErrors) == expectedReturnCode) {
                     System.out.print(".");
                 }else{
                     System.out.print("F");
                     failFiles.add(path + fileName);
                 }
                 totalTests++;
             }else{
                 System.out.print("*"); // skip file
                 filesSkipped++;
             }
         }
 
         printSummary(totalTests, filesSkipped, failFiles);
         int failures = failFiles.size();
         assertEquals("Compiler return unexpected return code compiling " + failures + " files. Expected return code was: " + expectedReturnCode, 0, failures);
     }
 
     private void printSummary(int totalTests, int filesSkipped, List<String> failFiles) {
         System.out.println("\nNumber of tests: " + totalTests);
         if(filesSkipped > 0) System.out.println("Number of files skipped: " + filesSkipped);
         if (failFiles.size() != 0){
             System.out.println("Unexpected return code compiling these files: ");
             for (String fileName: failFiles) {
                 System.out.println("\t" + fileName);
             }
         }
     }
 
     private int compileFile(String filePath, boolean printErrors) throws IOException,
                                                                          InterruptedException {
         String[] files = new String[1];
         files[0] = filePath;
        return Compiler.compile(files, false);
     }
 }
