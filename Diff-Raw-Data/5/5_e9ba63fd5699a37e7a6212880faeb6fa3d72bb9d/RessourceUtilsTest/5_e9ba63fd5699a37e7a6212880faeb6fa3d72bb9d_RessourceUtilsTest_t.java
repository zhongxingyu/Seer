 package org.charless.qxmaven.mojo.qooxdoo.utils;
 
 import junit.framework.TestCase;
 
 public class RessourceUtilsTest extends TestCase {
 	
 	public void testGetRelativePathsUnix() throws Exception {
 	    assertEquals("stuff/xyz.dat", ResourceUtils.getRelativePath("/var/data/stuff/xyz.dat", "/var/data/", "/"));
 	    assertEquals("../../b/c", ResourceUtils.getRelativePath("/a/b/c", "/a/x/y/", "/"));
 	    assertEquals("../../b/c", ResourceUtils.getRelativePath("/m/n/o/a/b/c", "/m/n/o/a/x/y/", "/"));
 	}
 
 	public void testGetRelativePathFileToFile() throws Exception {
 	    String target = "C:\\Windows\\Boot\\Fonts\\chs_boot.ttf";
 	    String base = "C:\\Windows\\Speech\\Common\\sapisvr.exe";
 
 	    String relPath = ResourceUtils.getRelativePath(target, base, "\\");
 	    assertEquals("..\\..\\Boot\\Fonts\\chs_boot.ttf", relPath);
 	}
 
 	public void testGetRelativePathDirectoryToFile() throws Exception {
 	    String target = "C:\\Windows\\Boot\\Fonts\\chs_boot.ttf";
 	    String base = "C:\\Windows\\Speech\\Common\\";
 
 	    String relPath = ResourceUtils.getRelativePath(target, base, "\\");
 	    assertEquals("..\\..\\Boot\\Fonts\\chs_boot.ttf", relPath);
 	}
 
 	public void testGetRelativePathFileToDirectory() throws Exception{
 	    String target = "C:\\Windows\\Boot\\Fonts";
 	    String base = "C:\\Windows\\Speech\\Common\\foo.txt";
 
 	    String relPath = ResourceUtils.getRelativePath(target, base, "\\");
 	    assertEquals("..\\..\\Boot\\Fonts", relPath);
 	}
 
 	public void testGetRelativePathDirectoryToDirectory() throws Exception{
 	    String target = "C:\\Windows\\Boot\\";
 	    String base = "C:\\Windows\\Speech\\Common\\";
 	    String expected = "..\\..\\Boot";
 
 	    String relPath = ResourceUtils.getRelativePath(target, base, "\\");
 	    assertEquals(expected, relPath);
 	}
 
 	public void testGetRelativePathDifferentDriveLetters() throws Exception{
 	    String target = "D:\\sources\\recovery\\RecEnv.exe";
 	    String base = "C:\\Java\\workspace\\AcceptanceTests\\Standard test data\\geo\\";
 
 	    try {
 	        ResourceUtils.getRelativePath(target, base, "\\");
 	        fail();
 
 	    } catch (PathResolutionException ex) {
 	        // expected exception
 	    }
 	}
 	
 	public void testRealExampleWin() throws Exception {
	   /* assertEquals("../..", 
 	    		ResourceUtils.getRelativePath(
 	    				"C:\\Documents and Settings\\Administrateur\\git\\qooxdoo-maven-plugin\\target\\test-target",
 	    				"C:\\Documents and Settings\\Administrateur\\git\\qooxdoo-maven-plugin\\target\\test-target\\qooxdoo\\resources_app", 
 	    				"/"));
 	    
 	    assertEquals("../..", 
 			    ResourceUtils.getRelativePath(
 						"C:\\Documents and Settings\\Administrateur\\git\\qooxdoo-maven-plugin\\target\\it\\compile\\simple\\target",
 						"C:\\Documents and Settings\\Administrateur\\git\\qooxdoo-maven-plugin\\target\\it\\compile\\simple\\target\\qooxdoo\\test", 
 						"/",false));
*/
 	}
 
 }
 
