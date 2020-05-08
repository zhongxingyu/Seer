 package com.vance.quest2012;
 
 import junit.framework.Assert;
 import org.junit.Test;
 
 /**
  * Unit test for {@link WebRetriever}
  *
  * @author srvance
  */
 public class WebRetrieverTest {
 
     public static final String SIMPLE_HOST_STRING = "localhost";
     public static final String SIMPLE_LOCALHOST_TARGET = WebRetriever.HTTP_PROTOCOL_STRING + WebRetriever.PROTOCOL_HOST_SEPARATOR + SIMPLE_HOST_STRING;
 
     @Test
     public void testWebRetriever() {
         String expectedProtocol = WebRetriever.HTTP_PROTOCOL_STRING;
         String expectedHost = SIMPLE_HOST_STRING;
         String expectedTarget = SIMPLE_LOCALHOST_TARGET;
 
         WebRetriever sut = new WebRetriever(expectedTarget);
 
         Assert.assertNotNull(sut);
         Assert.assertEquals(expectedTarget, sut.getTarget());
         Assert.assertEquals(expectedProtocol, sut.getProtocol());
         Assert.assertEquals(expectedHost, sut.getHost());
     }
 
     @Test
     public void testParseTarget() {
         WebRetriever sut = new WebRetriever();
 
         sut.parseTarget(SIMPLE_LOCALHOST_TARGET);
 
         Assert.assertEquals(WebRetriever.HTTP_PROTOCOL_STRING, sut.getProtocol());
         Assert.assertEquals(SIMPLE_HOST_STRING, sut.getHost());
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testParseTarget_NullTarget() {
         WebRetriever sut = new WebRetriever();
 
         sut.parseTarget(null);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testParseTarget_EmptyTarget() {
         WebRetriever sut = new WebRetriever();
 
         sut.parseTarget("");
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testParseTarget_WhitespaceTarget() {
         WebRetriever sut = new WebRetriever();
 
         sut.parseTarget(" ");
     }
 
     @Test
     public void testParseTarget_NoProtocolHostSeparator() {
         WebRetriever sut = new WebRetriever();
 
         sut.parseTarget(SIMPLE_HOST_STRING);
        Assert.assertEquals(WebRetriever.HTTP_PROTOCOL_STRING, sut.getProtocol());
        Assert.assertEquals(SIMPLE_HOST_STRING, sut.getHost());
     }
 }
