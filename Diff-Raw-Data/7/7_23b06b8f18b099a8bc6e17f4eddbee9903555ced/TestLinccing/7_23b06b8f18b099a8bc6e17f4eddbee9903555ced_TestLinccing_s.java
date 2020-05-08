 package com.hoccer.api;
 
 import static org.junit.Assert.*;
 
 import org.json.*;
 import org.junit.Test;
 
 public class TestLinccing {
 
     private ClientDescription createDescription() {
         return new ClientDescription("java-api unit test");
     }
 
     @Test(timeout = 8000)
     public void oneToOneSuccsess() throws Exception {
         final Linccer linccerA = new Linccer(createDescription());
         Linccer linccerB = new Linccer(createDescription());
 
        linccerA.onGpsChanged(22.011, 102.115, 130);
        linccerB.onGpsChanged(22.011, 102.11, 130);
 
         ThreadedShare threadedShare = new ThreadedShare(linccerA);
         threadedShare.start();
 
         JSONObject receivedPayload = linccerB.receive("1:1");
         assertNotNull("should have received something", receivedPayload);
         assertTrue("should have received a message", receivedPayload.has("message"));
         assertEquals("hello world", receivedPayload.get("message"));
 
         threadedShare.join();
         threadedShare.assertNoExceptionsOccured();
         assertNotNull("should have shared the message", threadedShare.getResult());
         assertEquals("hello world", threadedShare.getResult().get("message"));
     }
 
    @Test(timeout = 1000)
     public void oneToOneCollision() throws Exception {
         final Linccer linccerA = new Linccer(createDescription());
         final Linccer linccerB = new Linccer(createDescription());
         final Linccer linccerC = new Linccer(createDescription());
 
         linccerA.onGpsChanged(22.012, 102.113, 130);
         linccerB.onGpsChanged(22.011, 102.11, 1030);
         linccerC.onGpsChanged(22.009, 102.116, 2000);
 
         ThreadedShare threadedShare = new ThreadedShare(linccerA);
         threadedShare.start();
         ThreadedReceive threadedReceive = new ThreadedReceive(linccerB);
         threadedReceive.start();
 
         JSONObject receivedPayload = linccerC.receive("1:1");
         assertNotNull("should have received something", receivedPayload);
         assertTrue("should have received a message", receivedPayload.has("message"));
         assertEquals("hello world", receivedPayload.get("message"));
 
         threadedShare.join();
         threadedShare.assertNoExceptionsOccured();
         assertNotNull("should have got status object", threadedShare.getResult());
 
         threadedReceive.join();
         threadedReceive.assertNoExceptionsOccured();
         assertNotNull("should have got status object", threadedReceive.getResult());
     }
 
     class ThreadedLinccing extends Thread {
         protected Exception   mException;
         private final Linccer mLinccer;
         JSONObject            mResult;
 
         public ThreadedLinccing(Linccer linccer) {
             mLinccer = linccer;
         }
 
         public Linccer getLinccer() {
             return mLinccer;
         }
 
         public JSONObject getResult() {
             return mResult;
         }
 
         public void assertNoExceptionsOccured() throws Exception {
             if (mException != null) {
                 throw mException;
             }
         }
     }
 
     class ThreadedShare extends ThreadedLinccing {
 
         public ThreadedShare(Linccer linccer) {
             super(linccer);
         }
 
         @Override
         public void run() {
             try {
                 JSONObject payload = new JSONObject();
                 payload.put("message", "hello world");
                 mResult = getLinccer().share("1:1", payload);
             } catch (Exception e) {
                 mException = e;
             }
         };
     }
 
     class ThreadedReceive extends ThreadedLinccing {
 
         public ThreadedReceive(Linccer linccer) {
             super(linccer);
         }
 
         @Override
         public void run() {
             try {
                 mResult = getLinccer().receive("1:1");
             } catch (Exception e) {
                 mException = e;
             }
         };
     }
 }
