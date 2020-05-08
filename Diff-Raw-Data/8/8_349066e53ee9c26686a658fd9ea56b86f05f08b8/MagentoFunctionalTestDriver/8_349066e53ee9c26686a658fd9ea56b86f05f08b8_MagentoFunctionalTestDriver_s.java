 /**
  * Mule Magento Cloud Connector
  *
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mulesoft.demo.magento;
 
 import org.mule.construct.SimpleFlowConstruct;
 import org.mule.tck.FunctionalTestCase;
 
 public class MagentoFunctionalTestDriver extends FunctionalTestCase
 {
 
     @Override
     protected String getConfigResources()
     {
         return "mule-config.xml";
     }
 
     /**
      * Creates some products for this test. Run this test only once
      */
     public void testCreateProductsFlow() throws Exception
     {
         lookupFlowConstruct("CreateProductsFlow").process(getTestEvent(""));
     }
     
     /**
      * Creates some products for this test. Run this test only 
      * once
      */
     public void ignoretestSetupFlow() throws Exception
     {
         lookupFlowConstruct("CreatePriceUpdatesFlow").process(getTestEvent(""));
     }
 
     /**
      * Creates an S3 bucket
      */
     public void testCreateS3BucketFlow() throws Exception
     {
         lookupFlowConstruct("CreateS3BucketFlow").process(getTestEvent(""));
     }
 
     /**
      * Uploads an image to S3. Run this test only once
      */
     public void testUploadS3ImageFlow() throws Exception
     {
         lookupFlowConstruct("UploadS3ImageFlow").process(getTestEvent(""));
     }
 
     private SimpleFlowConstruct lookupFlowConstruct(final String name)
     {
         return (SimpleFlowConstruct) muleContext.getRegistry().lookupFlowConstruct(name);
     }
 
 }
