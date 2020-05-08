 
 package edu.msoe.se2800.h4;
 
 import static org.testng.Assert.assertEquals;
 
 import com.google.common.io.Closeables;
 
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 
 public class ReadPathTest {
 
    private Path mPath = Path.INSTANCE;
 
     @BeforeMethod
     public void beforeMethod() {
         mPath.reset();
     }
 
     @Test(expectedExceptions = {
             NullPointerException.class
     }, description = "")//TODO @Andrew add a description
     public void testReadNull() throws Path.BadFormatException, FileNotFoundException {
         mPath.readFromFile(null);
     }
 
     @Test(expectedExceptions = {
             FileNotFoundException.class
     }, description = "")//TODO @Andrew add a description
     public void testNonExistentFile() throws Path.BadFormatException, FileNotFoundException {
         File f = new File("aaa");
         mPath.readFromFile(f);
     }
 
     @Test(expectedExceptions = {
             Path.BadFormatException.class
     }, description = "")//TODO @Andrew add a description
     public void testBadFormatCoordinates() throws Path.BadFormatException, FileNotFoundException {
         File f = new File("./resourcesTest/BadCoordinates.scrumbot");
         try {
             mPath.readFromFile(f);
         } catch (Path.BadFormatException e) {
             assertEquals(mPath.size(), 0);
             throw e;
         }
     }
 
     @Test(description = "")//TODO @Andrew add a description
     public void testEmptyPath() throws Path.BadFormatException, FileNotFoundException {
         File f = new File("./resourcesTest/ExamplePathFileNoCoordinates.scrumbot");
         mPath.readFromFile(f);
     }
 
     @Test(description = "")//TODO @Andrew add a description
     public void testMultipleCoordinates() throws Path.BadFormatException, IOException {
         File f = new File("./resourcesTest/ReadOnlyFile.scrumbot");
 
         mPath.readFromFile(f);
         FileReader f2 = new FileReader(f);
         BufferedReader br = new BufferedReader(f2);
         int lines = 0;
         try {
             while (br.readLine() != null) {
                 lines++;
             }
         } catch (IOException e) {
             e.printStackTrace(); // To change body of catch statement use File | Settings | File
                                  // Templates.
         } finally {
             Closeables.close(br, true);
         }
         assertEquals(mPath.size(), lines - 1);
     }
 
     @AfterClass
     public void resetPath() {
         mPath.reset();
     }
 
 }
