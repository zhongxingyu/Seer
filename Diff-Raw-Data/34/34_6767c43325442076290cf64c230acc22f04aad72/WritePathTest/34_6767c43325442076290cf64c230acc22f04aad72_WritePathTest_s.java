 
 package edu.msoe.se2800.h4;
 
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertFalse;
 import static org.testng.Assert.assertTrue;
 import edu.msoe.se2800.h4.jplot.JPoint;
 
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 public class WritePathTest {
 
    private Path mPath = Path.INSTANCE;
 
     @BeforeClass
     public void setupPath() {
 
         // Ensure we have a fresh start
         mPath.reset();
 
         // add 10 points along a straight line
         for (int i = 0; i < 10; i++) {
             mPath.add(new JPoint(i * 10, i * 10));
         }
     }
 
     @Test(expectedExceptions = {
             NullPointerException.class
     }, description = "Tests passing a null parameter. Expects a NullPointerException")
     public void writeToFileNullParameter() {
         mPath.writeToFile(null);
     }
 
     @Test(description = "This is a test writing under 'normal' conditions")
     public void writeAsExpected() throws IOException {
         File file = new File("TestFile.scrumbot");
         file.deleteOnExit();
         assertTrue(mPath.writeToFile(file));
 
         // Make sure we have the number of lines we expect
         int count = lineCount(file);
         int expectedCount = mPath.size() + 1;
         assertEquals(count, expectedCount, "Test the line counts");
     }
 
     @Test(enabled = false)
     public void writeToReadOnlyFile() {
         // TODO @Marius: test writing to read only files
     }
 
     @Test(description = "Tries to write to a non-existent file")
     public void writeToNonExistentFile() {
         assertFalse(mPath.writeToFile(new File("")));
     }
 
     @AfterClass
     public void resetPath() {
         mPath.reset();
     }
 
     /*
      * This was taken from StackTrace & slightly modified http://stackoverflow.com
      * /questions/453018/number-of-lines-in-a-file-in-java
      */
     private int lineCount(File file) throws IOException {
         InputStream is = new BufferedInputStream(new FileInputStream(file));
         try {
             byte[] c = new byte[1024];
             int count = 0;
             int readChars;
             boolean empty = true;
             while ((readChars = is.read(c)) != -1) {
                 empty = false;
                 for (int i = 0; i < readChars; ++i) {
                     if (c[i] == '\n')
                         ++count;
                 }
             }
             return (count == 0 && !empty) ? 1 : count;
         } finally {
             is.close();
         }
     }
 
 }
