 package org.codehaus.xsite.website;
 
 import java.io.File;
 import java.io.IOException;
 
 import junit.framework.TestCase;
 
 import org.codehaus.xsite.Main;
 
 /**
  * @author Mauro Talevi
  */
 public class WebsiteTest extends TestCase {
     protected  String testSrcDir;
     
     public void setUp() throws Exception {
         setTestDir();
     }    
     
     protected void setTestDir() {
         testSrcDir =  System.getProperty("test.src.dir");
         if ( testSrcDir == null ){
            testSrcDir = "xsite-website/src/test/"; 
         } else if ( !testSrcDir.endsWith(File.separator) ){
             testSrcDir = testSrcDir + File.separator; 
         }        
     }
     
     public void testBuild() throws IOException{
        Main.main(new String[] {testSrcDir+"../content/website.xml", testSrcDir+"../templates/skin.html", "target/xsite"});
     }
 
 }
