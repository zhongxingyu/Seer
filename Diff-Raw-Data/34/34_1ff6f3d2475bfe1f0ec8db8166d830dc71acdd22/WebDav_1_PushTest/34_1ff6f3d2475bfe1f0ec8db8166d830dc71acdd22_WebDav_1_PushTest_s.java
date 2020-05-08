 /*
 Copyright (c) 2011, 2012 Parjanya Mudunuri
 
 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 associated documentation files (the "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to
 the following conditions:
 
 The above copyright notice and this permission notice shall be included in all copies or substantial
 portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 
 http://opensource.org/licenses/mit-license.php
  */
 
 package uk.co.firstzero.webdav;
 
 import org.apache.log4j.Logger;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Ignore;
 
 import java.io.File;
 import java.io.IOException;
 
 import static junit.framework.Assert.assertTrue;
 
 public class WebDav_1_PushTest {
     private Logger logger = Logger.getLogger(WebDav_1_PushTest.class);
     private String _iFile = "src/test/resources/webdav/output.csv";
     private String url = "http://localhost:8080/repository/default";
     private File iFile;
     private String fullUrl;
     private Push push;
 
     @Before
     public void setUp() throws IOException {
         push = new Push("admin", "admin", "http://localhost:8080/repository/default");
         push.setOverwrite(true);
 
         iFile = new File(_iFile);
         fullUrl = url + "/" + iFile.getName();
 
         logger.debug("fullUrl - " + fullUrl);
     }
 
     @Ignore
	public void testDownload() throws IOException {
         boolean upload = push.uploadFile(iFile, "output.csv");
         logger.debug("upload - " + upload);
         assertTrue(upload);
 
         boolean status = push.fileExists(fullUrl);
         logger.debug("fileExists - status - " + status);
 
         assertTrue(status);
     }
 
     @After
     public void tearDown() {
         push = null;
         iFile = null;
     }
 }
