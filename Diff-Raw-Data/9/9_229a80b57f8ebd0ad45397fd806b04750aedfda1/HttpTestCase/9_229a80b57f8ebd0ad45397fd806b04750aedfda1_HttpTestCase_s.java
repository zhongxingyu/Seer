 package com.artcom.y60.http;
 
 import android.test.InstrumentationTestCase;
 
 import com.artcom.y60.TestHelper;
 
 public class HttpTestCase extends InstrumentationTestCase {
 
     private MockHttpServer mServer;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         mServer = new MockHttpServer();
         System.gc();
     }
 
     @Override
     protected void tearDown() throws Exception {
         mServer.quit();
         System.gc();
         super.tearDown();
     }
 
     protected MockHttpServer getServer() {
         return mServer;
     }
 
     protected void assertRequestIsDone(final AsyncHttpRequest pRequest) throws Exception {
         TestHelper.blockUntilTrue("request should have been performed by now", 3000,
                 new TestHelper.Condition() {
 
                     @Override
                     public boolean isSatisfied() throws Exception {
                        return pRequest.isDone();
                     }
                 });
     }
 
     protected void assertHeadersAvailable(final ResponseHandlerForTesting requestStatus)
             throws Exception {
         TestHelper.blockUntilTrue("headers hould be there", 2000, new TestHelper.Condition() {
 
             @Override
             public boolean isSatisfied() throws Exception {
                 return requestStatus.hasOnHeadersAvailableBeenCalled;
             }
         });
     }
 
 }
