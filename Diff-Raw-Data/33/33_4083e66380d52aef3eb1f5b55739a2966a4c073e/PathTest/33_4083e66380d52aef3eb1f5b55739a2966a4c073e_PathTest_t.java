 
 package edu.msoe.se2800.h4;
 
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertFalse;
 import static org.testng.Assert.assertTrue;
 
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import java.awt.Point;
 import java.io.*;
 
 public class PathTest {
 
     private Path mPath = Path.INSTANCE;
 
     @BeforeClass
     public void setupPath() {
         
         //Ensure we have a fresh start
         mPath.reset();
         
         // add 10 points along a straight line
         for (int i = 0; i < 10; i++) {
             mPath.add(new Point(i*10, i*10));
         }
     }
 
     @Test(expectedExceptions = {
             NullPointerException.class
     })
     public void writeToFileNullParameter() {
         mPath.writeToFile(null);
     }
 
     @Test
     public void writeAsExpected() throws IOException {
         File file = new File("TestFile.scrumbot");
         file.deleteOnExit();
         assertTrue(mPath.writeToFile(file));
 
         // Make sure we have the number of lines we expect
         int count = lineCount(file);
         int expectedCount = mPath.size() + 1;
         assertEquals(count, expectedCount, "Test the line counts");
     }
 
     @Test(enabled=false)
     public void writeToReadOnlyFile() {
         // TODO Marius: test writing to read only files
     }
 
     @Test
     public void writeToNonExistentFile() {
         assertFalse(mPath.writeToFile(new File("")));
     }
 
 
     @Test(expectedExceptions = {
         NullPointerException.class
     })
     public void testReadNull() throws UnsupportedEncodingException, FileNotFoundException {
             mPath.readFromFile(null);
     }
 
     @Test(expectedExceptions = {
             FileNotFoundException.class
     })
     public void testNonExistentFile() throws UnsupportedEncodingException, FileNotFoundException {
         File f = new File("aaa");
         mPath.readFromFile(f);
     }
 //
 //    @Test(expectedExceptions = {
 //            UnsupportedEncodingException.class
 //    })
 //    public void testBadFormatHeaderLine(){
 //
 //    }
 //
    @Test(expectedExceptions = {
            UnsupportedEncodingException.class
    })
    public void testBadFormatCoordinates() throws UnsupportedEncodingException, FileNotFoundException {
        File f = new File("./resourcesTest/BadCoordinates.scrumbot");
        try {
            mPath.readFromFile(f);
        } catch (UnsupportedEncodingException e) {
            //verify that the path was not modified
            assertEquals(mPath.size(), 0);
            throw e;
        } catch (FileNotFoundException e) {
            throw e;
        }
    }
 
     @Test
     public void testEmptyPath(){
 
     }
 
     @Test
     public void testMultipleCoordinates(){
 
     }
 
     /*
      * This was taken from StackTrace & slightly modified
      * http://stackoverflow.com
      * /questions/453018/number-of-lines-in-a-file-in-java
      */
     private int lineCount(File file) throws IOException {
         InputStream is = new BufferedInputStream(new FileInputStream(file));
         try {
             byte[] c = new byte[1024];
             int count = 0;
             int readChars = 0;
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
 
 
     @AfterClass
     public void resetPath() {
         mPath.reset();
     }
 
 }
