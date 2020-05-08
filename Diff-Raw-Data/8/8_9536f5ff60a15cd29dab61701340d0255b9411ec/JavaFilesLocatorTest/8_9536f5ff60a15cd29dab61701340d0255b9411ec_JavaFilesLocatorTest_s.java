 /*
  *  The MIT License
  *
  *  Copyright 2011 Martin Vejmelka <martin.vejmelka@fel.cvut.cz>.
  *
  *  Permission is hereby granted, free of charge, to any person obtaining a copy
  *  of this software and associated documentation files (the "Software"), to deal
  *  in the Software without restriction, including without limitation the rights
  *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  *  copies of the Software, and to permit persons to whom the Software is
  *  furnished to do so, subject to the following conditions:
  *
  *  The above copyright notice and this permission notice shall be included in
  *  all copies or substantial portions of the Software.
  *
  *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  *  THE SOFTWARE.
  */
 
 package fel.cvut.cz.archval.input_processor;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.openide.util.Exceptions;
 
 /**
  *
  * @author Martin Vejmelka <martin.vejmelka@fel.cvut.cz>
  */
 public class JavaFilesLocatorTest {
 
    private String dirs[] = {
         "./testdir1",};
    private String files[] = {
         "./testdir1/TestFile.java",
         "./testdir1/SomeGarbage.txt",
         "./testdir1/SomeGarbage2.txt",
         "./testdir1/SomeGarbage3.java",
         "./testdir1/SomeGarbage4.java"
     };
    private String expectedResults[] = {
         "SomeGarbage4.java",
         "TestFile.java",
         "SomeGarbage3.java"
     };
 
     public JavaFilesLocatorTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
 
     @Before
     public void setUp() {
         for (String dir : dirs) {
             new File(dir).mkdir();
         }
         for (String file : files) {
             try {
                 new File(file).createNewFile();
             } catch (IOException ex) {
                 Exceptions.printStackTrace(ex);
             }
         }
     }
 
     @After
     public void tearDown() {
         for (String file : files) {
             new File(file).delete();
         }
         for (String dir : dirs) {
             new File(dir).delete();
         }
     }
 
     /**
      * Test of getProjectJavaFiles method, of class JavaFilesLocator.
      */
     @Test
     public void testGetProjectJavaFiles() {
         String directory = "./testdir1";
         JavaFilesLocator instance = new JavaFilesLocator();
         List<File> filesInDir = instance.getProjectJavaFiles(directory);
         if (filesInDir.size() != expectedResults.length) {
             Assert.fail("Expected count of found *.java files is different from real *.java files found.");
         }
         for (int i = 0; i < filesInDir.size(); i++) {
             Assert.assertEquals(filesInDir.get(i).getName(), expectedResults[i]);
         }
     }
 }
