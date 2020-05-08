 package org.mxunit.eclipseplugin.utils;
 
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.Collection;
 
 import junit.framework.TestCase;
 
 public class PathUtilsTest extends TestCase {
     
     public void setUp(){
         System.out.println("inside setup");
     }
 	
 	
 	public void testDeriveComponentPath(){
 		String root = "c:\\cfusionmx7\\wwwroot";
 		String component = "C:\\cfusionmx7\\WWWroot\\sandbox\\unittests\\TestMyCFC.cfc";
 		
 		File rootFile = new File(root);
 		File componentFile = new File(component);		
 		
 		String componentPath = PathUtils.deriveComponentPath(rootFile.getAbsolutePath(), componentFile.getAbsolutePath());
 		assertEquals("sandbox.unittests.TestMyCFC",componentPath);
 		
 		componentPath = PathUtils.deriveComponentPath(root, component);
 		assertEquals("sandbox.unittests.TestMyCFC",componentPath);
 		
 		root += "\\";
 		componentPath = PathUtils.deriveComponentPath(root, component);
 		assertEquals("sandbox.unittests.TestMyCFC",componentPath);
 		
 		
 		//test unix style
 		root = "/usr/inetpub/wwwroot";
 		component = "/usr/inetpub/wwwroot/com/my/path/to_file/MyCFC.cfc";
 		componentPath = PathUtils.deriveComponentPath(root, component, "/");
 		assertEquals("com.my.path.to_file.MyCFC",componentPath);
 		
 		//test non-local development style
 		root = "\\\\My-Nas(alpha)";
 		component="\\\\My-Nas(alpha)\\com\\my\\path\\to-my-files\\MyCFC.cfc";
 		componentPath = PathUtils.deriveComponentPath(root, component);
 		assertEquals("com.my.path.to-my-files.MyCFC",componentPath);
 		
 	}
 	
	public void testFredLeeBugChoppingOffT(){
		/*
		 * Sure was. I've re-arranged my folder structure to be:

		/users/fredlee/projects/wwwroot/waythrough
		/users/fredlee/projects/wwwroot/mxunit
		/users/fredlee/projects/wwwroot/tests
		
		(Apache re-configured to point at the new wwwroot location)
		
		Mxunit preferences: (both resolve ok)
		
		http://localhost/mxunit/framework/RemoteFacade.cfc
		/Users/fredleefarr/projects/wwwroot
		
		Project preferences:
		
		Path: /WTF
		Type: Project
		Location: /users/frelee/projects/wwwroot/tests
		 */
		String webroot = "/Users/fredlee/projects/wwwroot ";
		String component = "/Users/fredlee/projects/wwwroot/tests/MyTest.cfc";
		String componentPath = PathUtils.deriveComponentPath(webroot, component,"/");
		System.out.println(componentPath);
		assertEquals("","tests.MyTest",componentPath);
	}
	
 	public void testDeriveComponentPathWithCFCInDirectory(){
 		String root = "c:\\cfusionmx7\\wwwroot";
 		String component = "C:\\cfusionmx7\\WWWroot\\sandbox\\cfc\\TestMyCFC.cfc";
 		File rootFile = new File(root);
 		File componentFile = new File(component);	
 		String componentPath = PathUtils.deriveComponentPath(rootFile.getAbsolutePath(), componentFile.getAbsolutePath());
 		assertEquals("sandbox.cfc.TestMyCFC",componentPath);
 	}
 	
 	public void testDeriveComponentPathFromComponentRoot(){
 	    String root = "nonwebrootcfc";
 	    String projectDir = "c:\\coldfusion8\\extensions\\nonwebrootcomponents";
 	    String filePath = projectDir + "\\TestSomething.cfc";
 	    String componentPath = PathUtils.deriveComponentPathFromComponentRoot(root, projectDir, filePath);
 	    assertEquals("nonwebrootcfc.TestSomething",componentPath);
 	    
 	    filePath = projectDir + "\\app2\\TestSomething.cfc";
 	    componentPath = PathUtils.deriveComponentPathFromComponentRoot(root, projectDir, filePath);
         assertEquals("nonwebrootcfc.app2.TestSomething",componentPath);
 	}
 	
 	public void testDeriveComponentPathFromComponentRoot2(){
         String root = "nonwebrootcfc";
         String projectDir = "/coldfusion8/extensions/nonwebrootcomponents";
         String filePath = projectDir + "/TestSomething.cfc";
         String componentPath = PathUtils.deriveComponentPathFromComponentRoot(root, projectDir, filePath);
         assertEquals("nonwebrootcfc.TestSomething",componentPath);
         
         filePath = projectDir + "/app2/TestSomething.cfc";
         componentPath = PathUtils.deriveComponentPathFromComponentRoot(root, projectDir, filePath);
         assertEquals("nonwebrootcfc.app2.TestSomething",componentPath);
     }
 	
 	public void testDeriveComponentPathFromComponentRootWithCFCInName(){
 		String root = "cfc.nonwebrootcfc.cfc";
         String projectDir = "/coldfusion8/extensions/cfc/nonwebrootcomponents/cfc/";
         String filePath = projectDir + "/TestSomething.cfc";
         String componentPath = PathUtils.deriveComponentPathFromComponentRoot(root, projectDir, filePath);
         assertEquals("cfc.nonwebrootcfc.cfc.TestSomething",componentPath);
         
 	   
 	}
 	
 	public void testGetTestComponents() throws FileNotFoundException{
 		String sandbox = "C:\\inetpub\\WWWroot\\mxunit\\PluginDemoTests";
 		File sandboxDir = new File(sandbox);
 		
 		Collection<File> fileList = PathUtils.getTestComponents(sandboxDir);
 		System.out.println(fileList);
 		assertTrue(fileList.size()>0);
 		for (File file : fileList) {
 			assertTrue(file.getAbsolutePath().endsWith(".cfc"));
 		}
 	}
 	
 	public void testDeriveComponentPathRelativeToProject(){
 		String component = "PluginDemoTests/HodgePodgeTest.cfc";
 		String root = "components";
 		String ret = PathUtils.deriveComponentPathRelativeToProject(root, component);
 		assertEquals("components.PluginDemoTests.HodgePodgeTest",ret);
 	}
 	public void testDeriveComponentPathRelativeToProjectWithCFCInName(){
 		String component = "cfc/PluginDemoTests/HodgePodgeTest.cfc";
 		String root = "components";
 		String ret = PathUtils.deriveComponentPathRelativeToProject(root, component);
 		assertEquals("components.cfc.PluginDemoTests.HodgePodgeTest",ret);
 	}
 	
 	
 
 }
