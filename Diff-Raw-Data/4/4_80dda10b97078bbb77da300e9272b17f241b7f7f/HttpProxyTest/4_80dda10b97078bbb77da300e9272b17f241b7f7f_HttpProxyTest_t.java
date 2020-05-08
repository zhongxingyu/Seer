 package com.artcom.y60.http;
 
 import java.util.Arrays;
 import java.util.concurrent.TimeoutException;
 
 import android.content.Intent;
 import android.net.Uri;
 import android.test.ActivityUnitTestCase;
 import android.test.suitebuilder.annotation.Suppress;
 
 import com.artcom.y60.BindingListener;
 import com.artcom.y60.Logger;
 import com.artcom.y60.TestHelper;
 
 /**
  * Blackbox service testing through HttpProxyHelper (aidl and inter-vm-communication).
  */
 public class HttpProxyTest extends ActivityUnitTestCase<HttpProxyTestActivity> {
 
     public static final String LOG_TAG = "HttpProxyTest";
 
     private Intent             mStartIntent;
 
     public HttpProxyTest() {
         super(HttpProxyTestActivity.class);
     }
 
     public void testResourceIsAsynchronouslyUpdated() throws Exception {
         initializeActivity();
         final HttpProxyHelper helper = createHelper();
 
         Logger.v(LOG_TAG, "enough waiting, let's get to work");
 
         final TestListener listener = new TestListener();
         final Uri uri = TestUriHelper.createUri();
         helper.addResourceChangeListenerAndReport(uri, listener);
         helper.requestDownload(uri);
 
         TestHelper.blockUntilTrue("proxy call the listener", 1000, new TestHelper.Condition() {
             @Override
             public boolean isSatisfied() {
                 return listener.wasResourceAvailableCalled();
             }
         });
 
         Logger.v(LOG_TAG, "now let's check results");
 
         assertTrue("update should have been called", listener.wasResourceAvailableCalled());
         assertNotNull("get should return an object", helper.fetchFromCache(uri));
 
         byte[] fromService = helper.fetchFromCache(uri);
         assertNotNull("content from cache was null", fromService);
 
        byte[] fromHttp = HttpHelper.getAsByteArray(uri.toString());
         assertTrue("content doesn't match", Arrays.equals(fromService, fromHttp));
     }
 
     public void testRemovingResourceFromCache() throws Exception {
         initializeActivity();
         HttpProxyHelper helper = createHelper();
         Uri uri = TestUriHelper.createUri();
 
         TestListener listener = new TestListener();
         helper.addResourceChangeListenerAndReport(uri, listener);
 
         helper.requestResource(uri.toString());
 
         long start = System.currentTimeMillis();
         while (!listener.wasResourceAvailableCalled()) {
             if (System.currentTimeMillis() - start > 2000) {
                 throw new TimeoutException("took to long");
             }
             Thread.sleep(50);
         }
 
         assertTrue("update wasn't called", listener.wasResourceAvailableCalled());
         byte[] data = helper.fetchFromCache(uri);
         assertNotNull("content from cache was null", data);
 
         helper.removeFromCache(uri.toString());
         assertFalse("uri shouldnt be in cache anymore", helper.isInCache(uri.toString()));
 
     }
 
     public void testRequestingDownload() throws Exception {
         initializeActivity();
         final HttpProxyHelper helper = createHelper();
         final Uri uri = TestUriHelper.createUri();
 
         TestListener listener = new TestListener();
         helper.addResourceChangeListenerAndReport(uri, listener);
         assertFalse(helper.isInCache(uri.toString()));
 
         helper.requestDownload(uri);
         assertFalse("the uri should not be in cache yet", helper.isInCache(uri.toString()));
 
         TestHelper.blockUntilTrue("cache should load uri into cache", 2000,
                 new TestHelper.Condition() {
                     @Override
                     public boolean isSatisfied() {
                         return helper.isInCache(uri.toString());
                     }
                 });
     }
 
     public void testResourceNotAvailableInCache() throws Exception {
         initializeActivity();
         HttpProxyHelper helper = createHelper();
         TestListener listener = new TestListener();
         Uri uri = TestUriHelper.createUri();
         helper.addResourceChangeListenerAndReport(uri, listener);
 
         helper.requestResource(uri.toString());
 
         blockUntilResourceAvailableWasCalled(listener, 8000);
 
         assertTrue("callback not succsessful", listener.wasResourceAvailableCalled());
         byte[] data = helper.fetchFromCache(uri);
         assertNotNull(data);
     }
 
     @Suppress
     public void testGettingNonexistentResource() throws Exception {
         initializeActivity();
         HttpProxyHelper helper = createHelper();
         final TestListener listener = new TestListener();
         Uri uri = Uri.parse("http://www.artcom.de/doesnotexist");
         helper.addResourceChangeListenerAndReport(uri, listener);
         helper.requestDownload(uri);
 
         try {
         	helper.requestDownload(uri);
         } catch (Exception ex) {
         	Logger.v(LOG_TAG, "Uri not found!!! ", ex.getMessage());
             assertTrue("expected a 404 exception in the service", ex.getMessage().contains("404"));
         }
         
         TestHelper.blockUntilTrue("not available callback should have been called", 5000,
                 new TestHelper.Condition() {
                     @Override
                     public boolean isSatisfied() throws Exception {
                         return listener.wasResourceNotAvailableCalled();
                     }
                 });
     }
 
     public void testResourceIsAvailableInCache() throws Exception {
         initializeActivity();
         HttpProxyHelper helper = createHelper();
 
         TestListener listener = new TestListener();
         Uri uri = TestUriHelper.createUri();
         helper.addResourceChangeListenerAndReport(uri, listener);
 
         helper.requestResource(uri.toString());
 
         blockUntilResourceAvailableWasCalled(listener, 4000);
 
         listener.reset();
         assertFalse(listener.wasResourceAvailableCalled());
         helper.addResourceChangeListenerAndReport(uri, listener);
 
         // this is minimal asynchronous
         blockUntilResourceAvailableWasCalled(listener, 200);
         assertTrue("callback not succsessful", listener.wasResourceAvailableCalled());
         byte[] data = helper.fetchFromCache(uri);
         assertNotNull(data);
     }
 
     // the next four test can only be tested if the constructor lines that
     // create HashMap and LinkedList in Cache.java are commented out
     @Suppress
     public void testGetException() throws Exception {
         initializeActivity();
         HttpProxyHelper helper = createHelper();
         helper.requestResource("http://bla");
     }
 
     @Suppress
     public void testRemoveException() throws Exception {
         initializeActivity();
         HttpProxyHelper helper = createHelper();
         helper.removeFromCache(Uri.parse("http://bla"));
     }
 
     @Suppress
     public void testFetchInCacheException() throws Exception {
         initializeActivity();
         HttpProxyHelper helper = createHelper();
         helper.fetchFromCache(Uri.parse("http://bla"));
     }
 
     @Suppress
     public void testIsInCacheException() throws Exception {
         initializeActivity();
         HttpProxyHelper helper = createHelper();
         helper.isInCache("http://bla");
     }
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         mStartIntent = new Intent(Intent.ACTION_MAIN);
     }
 
     @Override
     protected void tearDown() throws Exception {
         super.tearDown();
     }
 
     private void blockUntilResourceAvailableWasCalled(TestListener pListener, long pTimeout)
             throws TimeoutException, InterruptedException {
         long start = System.currentTimeMillis();
         while (!pListener.wasResourceAvailableCalled()) {
             if (System.currentTimeMillis() - start > pTimeout) {
                 throw new TimeoutException("took to long");
             }
             Thread.sleep(50);
         }
     }
 
     private HttpProxyHelper createHelper() throws Exception {
         final DummyListener lsner = new DummyListener();
         HttpProxyHelper helper = new HttpProxyHelper(getActivity(), lsner);
         TestHelper.blockUntilTrue("HTTP helper not bound", 5000, new TestHelper.Condition() {
             @Override
             public boolean isSatisfied() {
                 return lsner.isBound();
             }
         });
         return helper;
     }
 
     private void initializeActivity() {
         startActivity(mStartIntent, null, null);
         assertNotNull(getActivity());
     }
 
     class TestListener implements ResourceListener {
         private boolean mWasResourceAvailableCalled    = false;
         private boolean mWasResourceNotAvailableCalled = false;
 
         @Override
         public void onResourceAvailable(Uri pResourceUri) {
             mWasResourceAvailableCalled = true;
         }
 
         public boolean wasResourceAvailableCalled() {
             return mWasResourceAvailableCalled;
         }
 
         public boolean wasResourceNotAvailableCalled() {
             return mWasResourceNotAvailableCalled;
         }
 
         public void reset() {
             mWasResourceNotAvailableCalled = false;
             mWasResourceAvailableCalled = false;
         }
 
         @Override
         public void onResourceNotAvailable(Uri pResourceUri) {
             mWasResourceNotAvailableCalled = true;
         }
     }
 
     class DummyListener implements BindingListener<HttpProxyHelper> {
         private boolean mBound = false;
 
         public void bound(HttpProxyHelper helper) {
             mBound = true;
         }
 
         public void unbound(HttpProxyHelper helper) {
             mBound = false;
         }
 
         public boolean isBound() {
             return mBound;
         }
     }
 }
