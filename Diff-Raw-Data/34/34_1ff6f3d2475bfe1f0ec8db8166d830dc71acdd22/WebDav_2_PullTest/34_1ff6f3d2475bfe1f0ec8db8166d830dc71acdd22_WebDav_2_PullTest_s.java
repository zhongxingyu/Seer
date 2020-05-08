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
 
 import au.com.bytecode.opencsv.CSVReader;
 import org.apache.log4j.Logger;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Ignore;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.MalformedURLException;
 
 import static junit.framework.Assert.assertEquals;
 
 
 /** Force this to run after WebDav_1_PushTest - The file needs to be pushed before being downloaded **/
 
 public class WebDav_2_PullTest {
     private Logger logger = Logger.getLogger(WebDav_2_PullTest.class);
     private String oFile = "src/test/resources/webdav/output.csv";
     private String url = "http://localhost:8080/repository/default";
     private String fileName = "output.csv";
     private Pull pull;
 
     @Before
     public void setUp() throws MalformedURLException {
         pull = new Pull("admin", "admin", url, fileName, oFile);
         File oFile_delete = new File(oFile);
         oFile_delete.delete();
     }
 
     @Ignore
 	public void testDownload() throws IOException {
         pull.download();
         CSVReader fileE = new CSVReader(new FileReader(oFile), ',');
         String[] line;
 
         int count = 0;
         while((line = fileE.readNext()) != null) {
             switch (count) {
                 case 0:
                     logger.trace("Read - " + line[0]);
                     assertEquals("Header_1", line[0]);
                     break;
                 case 2:
                     logger.trace("Read - " + line[2]);
                     assertEquals("˚∆∫å∂¬ß^∆", new String(line[2].getBytes(), "UTF8"));
                     break;
             }
 
             ++count;
         }
 
         assertEquals(count, 5);
     }
 
     @After
     public void tearDown() {
         pull = null;
     }
 }
