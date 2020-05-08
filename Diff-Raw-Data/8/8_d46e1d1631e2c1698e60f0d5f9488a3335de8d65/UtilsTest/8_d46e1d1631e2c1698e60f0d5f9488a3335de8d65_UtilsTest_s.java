 /**
  * 
  */
 package net.frontlinesms;
 
 import java.io.File;
 
 import net.frontlinesms.junit.BaseTestCase;
 
 /**
  * Unit tests for {@link Utils} class.
  * @author Alex Anderson <alex@frontlinesms.com>
  */
 public class UtilsTest extends BaseTestCase {
 	public void testGetFilenameWithoutExtension() {
 		testGetFilenameWithoutExtension("a.b", "a");
 		testGetFilenameWithoutExtension("/a/b/c/whatever.text", "whatever");
 		testGetFilenameWithoutExtension("/a/b/c/whatever.text etc", "whatever");
 		testGetFilenameWithoutExtension("C:/Program Files/My Program/filename.ext", "filename");
 		testGetFilenameWithoutExtension("C:/Program Files/My Program/filename with space.ext", "filename with space");
 	}
 	
 	private void testGetFilenameWithoutExtension(String path, String expectedNameWithoutExtension) {
 		File file = new File(path);
 		assertEquals(expectedNameWithoutExtension, Utils.getFilenameWithoutExtension(file));
 	}
 }
