 /*
  * $Id:$
  * --------------------------------------------------------------------------------------
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 package org.mule.module.filepack;
 
 import org.mule.DefaultMuleMessage;
 import org.mule.api.MuleException;
 import org.mule.api.MuleMessage;
 import org.mule.module.client.MuleClient;
 import org.mule.module.datapack.DelimitedOutputTransformer;
 import org.mule.tck.FunctionalTestCase;
 import org.mule.transport.NullPayload;
 
 public class DataPackDelimitedOutputTestCase extends FunctionalTestCase
 {
     @Override
     protected String getConfigResources()
     {
         return "datapack-delimited-test-config.xml";
     }
 
     public void testDelimitedOutputWithPipes() throws Exception
     {
         MuleClient client = new MuleClient(muleContext);
         MuleMessage result = client.send("vm://delimitedOutputWithPipes.in", "data", null);
         assertNotNull(result);
         assertNull(result.getExceptionPayload());
         assertFalse(result.getPayload() instanceof NullPayload);
 
         assertEquals("data|data\n", result.getPayloadAsString());
     }
 
     public void testDelimitedOutputWithTransformers() throws Exception
     {
         MuleClient client = new MuleClient(muleContext);
         MuleMessage result = client.send("vm://delimitedOutPutWithTransformers.in", "data2", null);
 
         assertNotNull(result);
         assertNull(result.getExceptionPayload());
         assertFalse(result.getPayload() instanceof NullPayload);
 
         assertEquals("transformdata\n", result.getPayload());
     }
 
     public void testDelimitedOutputWithDefaultValues() throws Exception
     {
         MuleClient client = new MuleClient(muleContext);
         MuleMessage result = client.send("vm://delimitedOutputWithDefaultValue.in", "data", null);
 
         assertNotNull(result);
         assertNull(result.getExceptionPayload());
         assertFalse(result.getPayload() instanceof NullPayload);
 
         assertEquals("data\tdata\n", result.getPayload());
     }
     
     public void testSetTabSeparator() {
     	DelimitedOutputTransformer transformer = new DelimitedOutputTransformer();
     	transformer.setDelimiterChar("\t");
     	assertTrue("Unexpected delimiter found while expecting \\t: "
     			+ transformer.getDelimiterChar(), transformer.getDelimiterChar().equals("\t"));
     	
     	transformer.setDelimiterChar("\\t");
     	
     	assertTrue("Unexpected delimiter found while expecting \\t: "
     			+ transformer.getDelimiterChar(), transformer.getDelimiterChar().equals("\t"));
     }
     
     public void testSetHeaders() throws MuleException {
     	MuleClient client = new MuleClient(muleContext);
         MuleMessage result = client.send("vm://delimitedOutputWithHeaders.in", "", null);
         
         String header = result.getInboundProperty("header");
         assertNotNull(header);
         assertEquals("Field1,Field2", header);
     }
     
     public void testTarget() throws Exception {
     	MuleClient client = new MuleClient(muleContext);
     	MuleMessage msg = new DefaultMuleMessage("payload", muleContext);
     	
         MuleMessage result = client.send("vm://customTarget.in", msg);
         
         String transformation = result.getInboundProperty("csv");
         assertNotNull(transformation);
         assertEquals(result.getPayloadAsString(), "payload");
         assertEquals("1,2\n", transformation);
     }
     
     public void testFillLength() throws Exception {
     	MuleClient client = new MuleClient(muleContext);
     	MuleMessage msg = new DefaultMuleMessage("payload", muleContext);
     	
         MuleMessage result = client.send("vm://fillLength.in", msg);
         
         String transformation = result.getPayload().toString();
         assertNotNull(transformation);
         
         String[] values = transformation.split(",");
        assertEquals(values.length, 2);
         
        assertEquals(values[0].length(), 5);
        assertEquals(values[1].length(), 6); // plus the newline char
        assertEquals(values[0].substring(1), "0000");
        assertEquals(values[1].substring(1, values[1].length() -1), "0000");
     }
     
     public void testPrefixLength() throws Exception {
     	MuleClient client = new MuleClient(muleContext);
     	MuleMessage msg = new DefaultMuleMessage("payload", muleContext);
     	
         MuleMessage result = client.send("vm://prefixLength.in", msg);
         
         String transformation = result.getPayload().toString();
         assertNotNull(transformation);
         
         String[] values = transformation.split(",");
        assertEquals(values.length, 2);
         
        assertEquals(values[0].length(), 5);
        assertEquals(values[1].length(), 6); // plus the newline char
        assertEquals(values[0].substring(0, values[0].length() -1), "0000", "0000");
        assertEquals(values[1].substring(0, values[1].length() -2), "0000", "0000");
     }
 }
