 /*
  * $Id$
  * --------------------------------------------------------------------------------------
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 package org.mule.ibeans.flickr;
 
 import org.mule.api.transformer.DiscoverableTransformer;
 import org.mule.api.transformer.Transformer;
 import org.mule.ibeans.test.AbstractIBeansTestCase;
 
 import java.net.URL;
 
 public class FlickrTransformersTestCase extends AbstractIBeansTestCase
 {
     @Override
     protected void doSetUp() throws Exception
     {
         registerBeans(new FlickrTransformers());
     }
 
     public void testTransformers() throws Exception
     {
        Object t = iBeansContext.getConfig().get("FlickrTransformers.transformStringToURL");
         assertNotNull(t);
         assertTrue(t instanceof Transformer);
         assertTrue(t instanceof DiscoverableTransformer);
 
         //Test auto transform
         URL url = iBeansContext.transform("http://foo.com", URL.class);
         assertNotNull(url);
         assertEquals("http://foo.com", url.toString());
 
        t = iBeansContext.getConfig().get("FlickrTransformers.transformInputstreamToBufferedImage");
         assertNotNull(t);
         assertTrue(t instanceof Transformer);
         assertTrue(t instanceof DiscoverableTransformer);
     }
 }
