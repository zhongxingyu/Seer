 /**
  * SAHARA Rig Client
  * 
  * Software abstraction of physical rig to provide rig session control
  * and rig device control. Automatically tests rig hardware and reports
  * the rig status to ensure rig goodness.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2009, University of Technology, Sydney
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, 
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright 
  *    notice, this list of conditions and the following disclaimer in the 
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of Technology, Sydney nor the names 
  *    of its contributors may be used to endorse or promote products derived from 
  *    this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * @author Michael Diponio (mdiponio)
  * @date 10th December 2009
  *
  * Changelog:
  * - 10/12/2009 - mdiponio - Initial file creation.
  */
 package au.edu.uts.eng.remotelabs.rigclient.protocol.types.tests;
 
 import java.io.ByteArrayInputStream;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.xml.namespace.QName;
 
 import junit.framework.TestCase;
 
 import org.apache.axiom.om.OMAbstractFactory;
 import org.apache.axiom.om.OMElement;
 import org.apache.axiom.om.util.StAXUtils;
 import org.junit.Test;
 
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.BatchState;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.BatchStatusResponseType;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.GetBatchControlStatusResponse;
 
 
 /**
  * Tests the {@link GetBatchControlStatusResponse} class.
  */
 public class GetBatchControlStatusResponseTester extends TestCase
 {
     @Test
     public void testParse() throws Exception
     {
        String str = "<ns1:getBatchControlStatusResponse xmlns:ns1=\"http://remotelabs.eng.uts.edu.au/rigclient/protocol\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"ns1:BatchStatusResponseType\">\n" +
                 "            <state>COMPLETE</state>\n" +
         		"            <progress>10</progress>\n" + 
         		"            <resultFilePath>C:\\results\\file1</resultFilePath>\n" +
         		"            <resultFilePath>C:\\results\\file2</resultFilePath>\n" +
        		"         </ns1:getBatchControlStatusResponse>";
         
         GetBatchControlStatusResponse resp  = GetBatchControlStatusResponse.Factory.parse(
                 StAXUtils.createXMLStreamReader(new ByteArrayInputStream(str.getBytes())));
         
         BatchStatusResponseType obj = resp.getGetBatchControlStatusResponse();
         assertEquals("10", obj.getProgress());
         assertEquals(BatchState.COMPLETE, obj.getState());
         
         String files[] = obj.getResultFilePath();
         List<String> fileList = Arrays.asList(files);
         assertTrue(fileList.contains("C:\\results\\file1"));        
         assertTrue(fileList.contains("C:\\results\\file2"));
     }
     
     @Test
     public void testSerialize() throws Exception
     {
         GetBatchControlStatusResponse resp = new GetBatchControlStatusResponse();
         BatchStatusResponseType obj = new BatchStatusResponseType();
         obj.addResultFilePath("C:\\FOO\\BAR");
         obj.addResultFilePath("C:\\BAR\\FOO");
         obj.setProgress("100");
         obj.setState(BatchState.FAILED);
         resp.setGetBatchControlStatusResponse(obj);
         
         OMElement ele = resp.getOMElement(new QName("", "batchStatus"), OMAbstractFactory.getOMFactory());
         String str = ele.toStringWithConsume();
         assertTrue(str.contains("<state>FAILED</state>"));
         assertTrue(str.contains("<progress>100</progress>"));
         assertTrue(str.contains("<resultFilePath>C:\\FOO\\BAR</resultFilePath>"));
         assertTrue(str.contains("<resultFilePath>C:\\BAR\\FOO</resultFilePath>"));
     }
 }
