 package my.triviagame.xmcd;
 
 import java.io.File;
 import java.io.IOException;
 import org.apache.commons.io.FileUtils;
 
 /**
  * Utilities for xmcd tests.
  */
 public class TestUtilities {
 
     public static String get_0a0d7d14() throws IOException {
         return FileUtils.readFileToString(FileUtils.toFile(TestUtilities.class.getResource(
                 "resources/xmcd samples/0a0d7d14")));
     }
     
     public static String get_a10be40d() throws IOException {
         return FileUtils.readFileToString(FileUtils.toFile(TestUtilities.class.getResource(
                 "resources/xmcd samples/a10be40d")));
     }
     
     public static String get_9209840d() throws IOException {
         return FileUtils.readFileToString(FileUtils.toFile(TestUtilities.class.getResource(
                 "resources/xmcd samples/9209840d")));
     }
     
     public static String getStressFile() throws IOException {
         return FileUtils.readFileToString(FileUtils.toFile(TestUtilities.class.getResource(
                 "resources/xmcd samples/stress")));
     }
     
     public static File getUpdate_20120401_20120501() {
         return FileUtils.toFile(TestUtilities.class.getResource(
                 "resources/xmcd samples/freedb-update-20120401-20120501.tar.bz2"));
     }
     
     public static File getUpdate_20120501_20120601() {
         return FileUtils.toFile(TestUtilities.class.getResource(
                 "resources/xmcd samples/freedb-update-20120501-20120601.tar.bz2"));
     }
     
     /**
      * References a sample complete FreeDB archive.
      * The actual file is not under source control because it weighs ~750MB.
      * Don't expect this method to work unless you have the complete file in your local source tree.
      */
     public static File getComplete_20120601() {
        return FileUtils.toFile(TestUtilities.class.getResource(
                 "resources/xmcd samples/freedb-complete-20120601.tar.bz2"));
     }
 }
