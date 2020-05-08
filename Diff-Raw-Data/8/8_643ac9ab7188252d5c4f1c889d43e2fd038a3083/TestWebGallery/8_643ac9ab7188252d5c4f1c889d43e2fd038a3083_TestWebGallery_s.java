 package de.raptor2101.GalDroid.Testing.ComponentTest.Activities.TestImplementations;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Semaphore;
 
 import junit.framework.Assert;
 
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.json.JSONException;
 
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.drawable.BitmapDrawable;
 import android.util.Log;
 
 import de.raptor2101.GalDroid.WebGallery.Stream;
 import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryDownloadObject;
 import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObject;
 import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObjectComment;
 import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryProgressListener;
 import de.raptor2101.GalDroid.WebGallery.Interfaces.WebGallery;
 
 public class TestWebGallery implements WebGallery {
     private static final String CLASS_TAG = "TestWebGallery";
     private List<TestGalleryObject> mTestGalleryObject;
     private List<TestDownloadObject> mRequestedObjects;
 
     private final Resources mResources;
     private boolean mDownloadWaitHandle;
 
     public TestWebGallery(Resources resources) {
 	mDownloadWaitHandle = false;
 	mResources = resources;
 	mRequestedObjects = new ArrayList<TestDownloadObject>(0);
     }
 
     @Override
     public GalleryObject getDisplayObject(String path) throws ClientProtocolException, IOException, JSONException {
 	Assert.fail("Call not implemented TestMethod");
 	return null;
     }
 
     @Override
     public List<GalleryObject> getDisplayObjects() {
 	Assert.fail("Call not implemented TestMethod");
 	return null;
     }
 
     @Override
     public List<GalleryObject> getDisplayObjects(GalleryProgressListener progressListener) {
 	Assert.fail("Call not implemented TestMethod");
 	return null;
     }
 
     @Override
     public List<GalleryObject> getDisplayObjects(String path) {
 	Assert.fail("Call not implemented TestMethod");
 	return null;
     }
 
     @Override
     public List<GalleryObject> getDisplayObjects(String path, GalleryProgressListener progressListener) {
 	Assert.fail("Call not implemented TestMethod");
 	return null;
     }
 
     @Override
     public List<GalleryObject> getDisplayObjects(GalleryObject galleryObject) {
 	Assert.fail("Call not implemented TestMethod");
 	return null;
     }
 
     @Override
     public List<GalleryObject> getDisplayObjects(GalleryObject galleryObject, GalleryProgressListener progressListener) {
 	if (galleryObject instanceof TestGalleryObject) {
 	    TestGalleryObject testGalleryObject = (TestGalleryObject) galleryObject;
 
 	    Assert.assertTrue("The requestes GalleryObject has no childen", testGalleryObject.hasChildren());
 
 	    List<TestGalleryObject> children = testGalleryObject.getChildren();
 	    if (progressListener != null) {
 
 		progressListener.handleProgress(children.size(), children.size());
 	    }
 	    List<GalleryObject> castedChildren = new ArrayList<GalleryObject>(children.size());
 	    for (TestGalleryObject child : children) {
 		castedChildren.add(child);
 	    }
 	    return castedChildren;
 
 	} else {
 	    Assert.fail("Someone tries to use non-testing GalleryObjects on testing WebGallery");
 	    return new ArrayList<GalleryObject>(0);
 	}
     }
 
     private Map<GalleryObject, List<String>> expectedGetDisplayObjectTagsCalls = new HashMap<GalleryObject, List<String>>(10);
 
     public void setupGetDisplayObjectTagsCall(GalleryObject galleryObject, List<String> returnValue) {
 	expectedGetDisplayObjectTagsCalls.put(galleryObject, returnValue);
     }
 
     private Map<GalleryObject, Semaphore> mMapBlockedGetDisplayObjectTags = new HashMap<GalleryObject, Semaphore>(10);
 
     @Override
     public List<String> getDisplayObjectTags(GalleryObject galleryObject, GalleryProgressListener progressListener) throws IOException {
 	if (expectedGetDisplayObjectTagsCalls.containsKey(galleryObject)) {
 	    AquireWaitHandle("getDisplayObjectTags", galleryObject, mMapBlockedGetDisplayObjectTags);
 	    return expectedGetDisplayObjectTagsCalls.get(galleryObject);
 	} else {
 	    Assert.fail(String.format("No setup for %s - getDisplayObjectTags-Call", galleryObject));
 	    return null;
 	}
 
     }
 
     public void releaseGetGetDisplayObjectTags(GalleryObject galleryObject) {
 	ReleaseWaitHandle("getDisplayObjectTags", galleryObject, mMapBlockedGetDisplayObjectTags);
     }
 
     private Map<GalleryObject, List<GalleryObjectComment>> expectedGetDisplayObjectCommentsCalls = new HashMap<GalleryObject, List<GalleryObjectComment>>(10);
 
     public void setupGetDisplayObjectCommentsCall(GalleryObject galleryObject, List<GalleryObjectComment> returnValue) {
 	expectedGetDisplayObjectCommentsCalls.put(galleryObject, returnValue);
     }
 
     private Map<GalleryObject, Semaphore> mMapBlockedGetDisplayObjectComments = new HashMap<GalleryObject, Semaphore>(10);
 
     @Override
     public List<GalleryObjectComment> getDisplayObjectComments(GalleryObject galleryObject, GalleryProgressListener progressListener) throws IOException, ClientProtocolException, JSONException {
 	if (expectedGetDisplayObjectCommentsCalls.containsKey(galleryObject)) {
 	    AquireWaitHandle("getDisplayObjectComments", galleryObject, mMapBlockedGetDisplayObjectComments);
 
 	    return expectedGetDisplayObjectCommentsCalls.get(galleryObject);
 	} else {
 	    Assert.fail(String.format("No setup for %s - getDisplayObjectComments-Call", galleryObject));
 	    return null;
 	}
     }
 
     public void releaseGetDisplayObjectComments(GalleryObject galleryObject) {
 	ReleaseWaitHandle("getDisplayObjectComments", galleryObject, mMapBlockedGetDisplayObjectComments);
     }
 
     @Override
     public void setPreferedDimensions(int height, int width) {
 	// TODO currently not tracked for testing
     }
 
     @Override
     public String getSecurityToken(String user, String password) throws SecurityException {
 	Assert.fail("Call not implemented TestMethod");
 	return null;
     }
 
     private Map<TestDownloadObject, Semaphore> mMapBlockedGetFileStream = new HashMap<TestDownloadObject, Semaphore>(10);
 
     @Override
     public Stream getFileStream(GalleryDownloadObject downloadObject) throws IOException, ClientProtocolException {
 	if (downloadObject instanceof TestDownloadObject) {
 	    TestDownloadObject testDownloadObject = (TestDownloadObject) downloadObject;
 	    Assert.assertEquals("Wrong image size requested", TestDownloadObject.ImageSize.Image, testDownloadObject.getRequestedSize());
 	    synchronized (mRequestedObjects) {
 		mRequestedObjects.add(testDownloadObject);
 	    }
 
 	    if (mDownloadWaitHandle) {
 		Semaphore semaphore;
 		synchronized (mMapBlockedGetFileStream) {
 		    if (mMapBlockedGetFileStream.containsKey(testDownloadObject)) {
 			Log.d(CLASS_TAG, String.format("getFileStream - Aquire existing semaphore for %s", testDownloadObject));
 			semaphore = mMapBlockedGetFileStream.get(testDownloadObject);
 		    } else {
 			Log.d(CLASS_TAG, String.format("getFileStream - Creating and aquire existing semaphore for %s", testDownloadObject));
 			semaphore = new Semaphore(0);
 			mMapBlockedGetFileStream.put((TestDownloadObject) downloadObject, semaphore);
 		    }
 		}
 		try {
 		    semaphore.acquire();
 		} catch (InterruptedException e) {
		    Assert.fail(e.getMessage());
 		}
 
 		Log.d(CLASS_TAG, String.format("getFileStream - semaphore released for %s", testDownloadObject));
 	    }
 
 	    BitmapDrawable drawable = (BitmapDrawable) mResources.getDrawable(testDownloadObject.getResourceId());
 	    Bitmap bitmap = (Bitmap) ((BitmapDrawable) drawable).getBitmap();
 	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
 	    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
 	    byte[] byteArray = outputStream.toByteArray();
 	    InputStream inputStream = new ByteArrayInputStream(byteArray);
 	    return new Stream(inputStream, byteArray.length);
 	} else {
 	    Assert.fail("Someone tries to use non-testing GalleryObjects on testing WebGallery");
 	    return null;
 	}
 
     }
 
     public void releaseGetFileStream(TestDownloadObject downloadObject) {
 	Semaphore semaphore;
 	synchronized (mMapBlockedGetFileStream) {
 	    if (mMapBlockedGetFileStream.containsKey(downloadObject)) {
 		Log.d(CLASS_TAG, String.format("getFileStream - Release existing semaphore for %s", downloadObject));
 		semaphore = mMapBlockedGetFileStream.get(downloadObject);
 
 	    } else {
 		Log.d(CLASS_TAG, String.format("getFileStream - Creating and release existing semaphore for %s", downloadObject));
 		semaphore = new Semaphore(0);
 		mMapBlockedGetFileStream.put(downloadObject, semaphore);
 	    }
 	}
 	semaphore.release();
     }
 
     @Override
     public void setSecurityToken(String token) {
 	Assert.fail("Call not implemented TestMethod");
     }
 
     @Override
     public void setHttpClient(HttpClient httpClient) {
 	Assert.fail("Call not implemented TestMethod");
     }
 
     public void setTestGalleryObjects(List<TestGalleryObject> testGalleryObjects) {
 	mTestGalleryObject = testGalleryObjects;
 	synchronized (mRequestedObjects) {
 	    mRequestedObjects = new ArrayList<TestDownloadObject>(testGalleryObjects.size());
 	}
     }
 
     public List<TestGalleryObject> getTestGalleryObjects() {
 	return mTestGalleryObject;
     }
 
     public void resetRequestedDownloadObjects() {
 	synchronized (mRequestedObjects) {
 	    mRequestedObjects.clear();
 	}
     }
 
     public List<TestDownloadObject> getRequestedDownloadObjects() {
 	synchronized (mRequestedObjects) {
 	    return Collections.unmodifiableList(mRequestedObjects);
 	}
     }
 
     public boolean isDownloadWaitHandleActive() {
 	return mDownloadWaitHandle;
     }
 
     public void activateDownloadWaitHandle() {
 	mDownloadWaitHandle = true;
     }
 
     public void deactivateDownloadWaitHandle() {
 	mDownloadWaitHandle = false;
     }
 
    private void AquireWaitHandle(String source, GalleryObject galleryObject, Map<GalleryObject, Semaphore> mapHandles) {
 	if (mDownloadWaitHandle) {
 	    Semaphore semaphore;
 	    synchronized (mapHandles) {
 		if (mapHandles.containsKey(galleryObject)) {
 		    Log.d(CLASS_TAG, String.format("%s - Aquire existing semaphore for %s", source, galleryObject));
 		    semaphore = mapHandles.get(galleryObject);
 		} else {
 		    Log.d(CLASS_TAG, String.format("%s - Creating and aquire existing semaphore for %s", source, galleryObject));
 		    semaphore = new Semaphore(0);
 		    mapHandles.put(galleryObject, semaphore);
 		}
 	    }
 	    try {
 		semaphore.acquire();
 	    } catch (InterruptedException e) {
		Assert.fail(e.getMessage());
 	    }
 	    Log.d(CLASS_TAG, String.format("%s - semaphore released for %s", source, galleryObject));
 	}
     }
 
     private void ReleaseWaitHandle(String source, GalleryObject galleryObject, Map<GalleryObject, Semaphore> mapHandles) {
 	Semaphore semaphore;
 	synchronized (mapHandles) {
 	    if (mapHandles.containsKey(galleryObject)) {
 		Log.d(CLASS_TAG, String.format("%s - Release existing semaphore for %s", source, galleryObject));
 		semaphore = mapHandles.get(galleryObject);
 
 	    } else {
 		Log.d(CLASS_TAG, String.format("%s - Creating and release existing semaphore for %s", source, galleryObject));
 		semaphore = new Semaphore(0);
 		mapHandles.put(galleryObject, semaphore);
 	    }
 	}
 	semaphore.release();
     }
 }
