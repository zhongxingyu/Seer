 package com.artcom.y60.http;
 
 import java.util.Arrays;
 import java.util.concurrent.TimeoutException;
 
 import android.content.Intent;
 import android.net.Uri;
 import android.test.ActivityUnitTestCase;
 
 import com.artcom.y60.BindingListener;
 import com.artcom.y60.HttpHelper;
 import com.artcom.y60.Logger;
 import com.artcom.y60.TestHelper;
 
 /**
  * Blackbox service testing through HttpProxyHelper (aidl and
  * inter-vm-communication).
  */
 public class HttpProxyTest extends ActivityUnitTestCase<HttpProxyTestActivity> {
 
     // Constants ---------------------------------------------------------
 
     public static final String LOG_TAG = "HttpProxyTest";
 
     // Instance Variables ------------------------------------------------
 
    private Intent             mStartIntent;
 
     // Constructors ------------------------------------------------------
 
     public HttpProxyTest() {
 
         super(HttpProxyTestActivity.class);
     }
 
     // Public Instance Methods -------------------------------------------
 
     public void testGetInitiallyReturnsNull() throws Exception {
 
         initializeActivity();
         HttpProxyHelper helper = createHelper();
         byte[] bytes = helper.get(TestUriHelper.createUri());
 
         assertNull("uncached content should be null initially", bytes);
     }
 
     public void testResourceIsAsynchronouslyUpdated() throws Exception {
 
         initializeActivity();
         final HttpProxyHelper helper = createHelper();
 
         Logger.v(LOG_TAG, "enough waiting, let's get to work");
 
         TestListener listener = new TestListener();
         final Uri uri = TestUriHelper.createUri();
         helper.addResourceChangeListener(uri, listener);
         helper.get(uri);
 
         long start = System.currentTimeMillis();
         TestHelper.blockUntilTrue("proxy should return the object", 4000,
                 new TestHelper.Condition() {
 
                     @Override
                     public boolean isSatisfied() {
                         return (helper.get(uri) != null);
                     }
 
                 });
 
         Logger.v(LOG_TAG, "now let's check results");
 
         assertTrue("update should have been called", listener.wasResourceChangeCalled());
         assertNotNull("get should return an object", helper.get(uri));
         // HttpProxyService.logCache();
 
         byte[] fromService = helper.fetchFromCache(uri);
         assertNotNull("content from cache was null", fromService);
 
         byte[] fromHttp = HttpHelper.getAsByteArray(Uri.parse(uri.toString()));
         assertTrue("content doesn't match", Arrays.equals(fromService, fromHttp));
     }
 
     public void testRemovingResourceFromCache() throws Exception {
 
         initializeActivity();
         HttpProxyHelper helper = createHelper();
         Uri uri = TestUriHelper.createUri();
 
         TestListener listener = new TestListener();
         helper.addResourceChangeListener(uri, listener);
 
         byte[] data = helper.get(uri);
         assertNull("uncached content should be null initially", data);
 
         long start = System.currentTimeMillis();
         while (!listener.wasResourceChangeCalled()) {
 
             if (System.currentTimeMillis() - start > 2000) {
                 throw new TimeoutException("took to long");
             }
 
             Thread.sleep(50);
         }
 
         assertTrue("update wasn't called", listener.wasResourceChangeCalled());
         data = helper.fetchFromCache(uri);
         assertNotNull("content from cache was null", data);
 
         helper.removeFromCache(uri.toString());
         data = helper.get(uri);
         assertNull("content should be null after removing form cache", data);
 
     }
 
     public void testResourceNotAvailableInCache() throws Exception {
         initializeActivity();
         HttpProxyHelper helper = createHelper();
         TestListener listener = new TestListener();
         Uri uri = TestUriHelper.createUri();
         helper.addResourceChangeListener(uri, listener);
 
         byte[] data = helper.get(uri);
         assertNull(data);
 
         blockUntilResourceAvailableWasCalled(listener, 4000);
 
         assertTrue("callback not succsessful", listener.wasResourceAvailableCalled());
         data = helper.get(uri);
         assertNotNull(data);
     }
 
     public void testResourceIsAvailableInCache() throws Exception {
         initializeActivity();
         HttpProxyHelper helper = createHelper();
 
         TestListener listener = new TestListener();
         Uri uri = TestUriHelper.createUri();
         helper.addResourceChangeListener(uri, listener);
 
         byte[] data = helper.get(uri);
         assertNull(data);
 
         blockUntilResourceAvailableWasCalled(listener, 4000);
 
         listener.reset();
         assertFalse(listener.wasResourceChangeCalled());
         assertFalse(listener.wasResourceAvailableCalled());
         helper.addResourceChangeListener(uri, listener);
 
         // this is minimal asynchronous
         blockUntilResourceAvailableWasCalled(listener, 200);
         assertTrue("callback not succsessful", listener.wasResourceAvailableCalled());
         data = helper.get(uri);
         assertNotNull(data);
     }
 
     // Protected Instance Methods ----------------------------------------
 
     protected void setUp() throws Exception {
 
         super.setUp();
 
         mStartIntent = new Intent(Intent.ACTION_MAIN);
     }
 
     protected void tearDown() throws Exception {
 
         super.tearDown();
     }
 
     // Private Instance Methods ------------------------------------------
 
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
 
     private HttpProxyHelper createHelper() {
 
         HttpProxyHelper helper = new HttpProxyHelper(getActivity(), new DummyListener());
 
         for (int i = 0; i < 200; i++) {
             try {
                 Thread.sleep(10);
             } catch (InterruptedException ix) {
                 Logger.v(LOG_TAG, "INTERRUPT!!1!");
             }
         }
 
         return helper;
     }
 
     private void initializeActivity() {
 
         startActivity(mStartIntent, null, null);
         assertNotNull(getActivity());
     }
 
     // Inner Classes -----------------------------------------------------
 
     class TestListener implements ResourceListener {
 
        private boolean mWasResourceChangedCalled   = false;
         private boolean mWasResourceAvailableCalled = false;
 
         public void onResourceChanged(Uri resourceUri) {
 
             mWasResourceChangedCalled = true;
         }
 
         @Override
         public void onResourceAvailable(Uri pResourceUri) {
             mWasResourceAvailableCalled = true;
         }
 
         public boolean wasResourceChangeCalled() {
 
             return mWasResourceChangedCalled;
         }
 
         public boolean wasResourceAvailableCalled() {
 
             return mWasResourceAvailableCalled;
         }
 
         public void reset() {
             mWasResourceChangedCalled = false;
             mWasResourceAvailableCalled = false;
         }
     }
 
     class DummyListener implements BindingListener<HttpProxyHelper> {
         public void bound(HttpProxyHelper helper) {
         }
 
         public void unbound(HttpProxyHelper helper) {
         }
     }
 }
