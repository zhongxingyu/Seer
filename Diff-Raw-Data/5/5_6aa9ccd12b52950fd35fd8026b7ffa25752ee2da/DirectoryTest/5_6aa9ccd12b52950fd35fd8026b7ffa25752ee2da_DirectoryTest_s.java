 package com.callcenter.external.model;
 
 import mockit.Expectations;
 import mockit.NonStrict;
 import mockit.NonStrictExpectations;
 import mockit.Verifications;
 import org.junit.Test;
 
 import java.io.File;
 import java.util.List;
 
 import static junit.framework.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 /**
  * Created by IntelliJ IDEA.
  *
  * @author deep
  */
 public class DirectoryTest {
 
     @Test
     public void shouldReturnTheAbsolutePathOfTheFile(@NonStrict final File file) {
         new NonStrictExpectations() {
             {
                 new File(".");
                 file.getAbsolutePath(); returns("HelloAbsolutePath");
             }
         };
         assertEquals("HelloAbsolutePath", new Directory(".").getAbsolutePath());
 
         new Verifications() {
             {
                 file.getAbsolutePath();
             }
         };
     }
 
     @Test
     public void shouldReturnTrueIfPathPointsToADirectory(@NonStrict final File file) {
         new NonStrictExpectations() {
             {
                 new File(".");
                 file.isDirectory(); returns(true);
             }
         };
         assertTrue(new Directory(".").isValid());
     }
 
     @Test
     public void shouldReturnFalseIfPathDoesNotPointsToADirectory(@NonStrict final File file) {
         new NonStrictExpectations() {
             {
                 new File(".");
                 file.isDirectory(); returns(false);
             }
         };
         assertFalse(new Directory(".").isValid());
     }
 
     @Test
     public void shouldReturnAListOfFilesInTheDirectoryPointedByTheWaveFileDirectory(@NonStrict final File file) {
         new Expectations() {
             {
                 new File("hellowavefiledirectory");
                file.list(); returns(new String[] {"file1", "file2"});
                file.getPath(); returns("c:\\testing\\file\\path");
                new File("c:\\testing\\file\\path\\file1");
                new File("c:\\testing\\file\\path\\file2");
             }
         };
         final List<File> files = new Directory("hellowavefiledirectory").list();
         assertEquals(2, files.size());
     }
 }
