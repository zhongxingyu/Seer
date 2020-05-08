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
 
 import org.mule.api.MuleMessage;
 import org.mule.module.client.MuleClient;
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
 }
