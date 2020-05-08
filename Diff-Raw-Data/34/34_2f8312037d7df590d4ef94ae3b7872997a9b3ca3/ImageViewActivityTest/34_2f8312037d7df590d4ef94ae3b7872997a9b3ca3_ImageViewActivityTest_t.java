 package de.raptor2101.GalDroid.Testing.ComponentTest.Activities;
 
 import java.io.File;
import java.security.NoSuchAlgorithmException;
 import java.security.acl.LastOwnerException;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 import java.util.Random;
 import java.util.concurrent.ExecutionException;
 
 import android.app.ActionBar;
 import android.app.Instrumentation;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.PointF;
 import android.os.SystemClock;
 import android.test.ActivityInstrumentationTestCase2;
 import android.test.TouchUtils;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Gallery;
 import android.widget.TextView;
 import de.raptor2101.GalDroid.R;
 import de.raptor2101.GalDroid.Activities.GalDroidApp;
 import de.raptor2101.GalDroid.Activities.ImageViewActivity;
 import de.raptor2101.GalDroid.Activities.Helpers.ImageAdapter;
 import de.raptor2101.GalDroid.Activities.Views.GalleryImageView;
 import de.raptor2101.GalDroid.Activities.Views.ImageInformationView;
import de.raptor2101.GalDroid.Config.GalDroidPreference;
 import de.raptor2101.GalDroid.Testing.ComponentTest.Activities.TestImplementations.TestDownloadObject;
 import de.raptor2101.GalDroid.Testing.ComponentTest.Activities.TestImplementations.TestGalleryObject;
 import de.raptor2101.GalDroid.Testing.ComponentTest.Activities.TestImplementations.TestGalleryObjectComment;
 import de.raptor2101.GalDroid.Testing.ComponentTest.Activities.TestImplementations.TestWebGallery;
 import de.raptor2101.GalDroid.WebGallery.ImageCache;
 import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryDownloadObject;
 import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObject;
 import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObjectComment;
 import de.raptor2101.GalDroid.WebGallery.Tasks.GalleryLoaderTask;
 import de.raptor2101.GalDroid.WebGallery.Tasks.ImageInformationLoaderTask;
 import de.raptor2101.GalDroid.WebGallery.Tasks.ImageLoaderTask;
 import de.raptor2101.GalDroid.WebGallery.Tasks.TaskInterface.Status;
 
 public class ImageViewActivityTest extends ActivityInstrumentationTestCase2<ImageViewActivity> {
   private static final String CLASS_TAG = "ImageViewActivityTest";
 
   public ImageViewActivityTest() {
     super("de.raptor2101.GalDroid.Activities.ImageViewActivity", ImageViewActivity.class);
   }
 
   private final int IMAGE_ID = de.raptor2101.GalDroid.Testing.R.drawable.testpic_1;
   private final int GALLERY_SAMPLE_SIZE = 300;
 
   private ImageViewActivity mActivity;
   private TestWebGallery mWebGallery;
   private ImageCache mImageCache;
 
   private Instrumentation mInstrumentation;
 
   private TestGalleryObject mCurrentGallery;
   private TestGalleryObject mCurrentVisibleChild;
 
   private ImageInformationView mImageInformationView;
 
   private Gallery mGalleryFullscreen;
   private Gallery mGalleryThumbnails;
 
   private ImageLoaderTask mImageLoaderTask;
 
   @Override
   protected void setUp() throws Exception {
     Log.d("ImageViewActivityTest", "Setup Called");
     super.setUp();
     mInstrumentation = getInstrumentation();
    GalDroidPreference.Initialize(mInstrumentation.getTargetContext());
    
    deleteCachedFiles();

    Resources recources = mInstrumentation.getContext().getResources();
    mWebGallery = createTestWebGallery(recources);

    GalDroidApp appContext = (GalDroidApp) mInstrumentation.getTargetContext().getApplicationContext();
    appContext.setWebGallery(mWebGallery);
    mImageCache = appContext.getImageCache();
 
    
  }

  private void deleteCachedFiles() throws NoSuchAlgorithmException {
     mImageCache = new ImageCache(this.getInstrumentation().getTargetContext());
     for (File file : mImageCache.getCacheDir().listFiles()) {
       try {
         file.delete();
       } catch (Exception e) {
         // if something goes wrong... ignore it
       }
     }
     
    GalDroidPreference asyncAccess = GalDroidPreference.GetAsyncAccess();
    asyncAccess.clearCacheTable();
    asyncAccess.close();
   }
 
   @Override
   protected void tearDown() throws Exception {
     Log.d(CLASS_TAG,String.format("TearDown Called",mActivity));
     mInstrumentation.runOnMainSync(new Runnable() {
       
       @Override
       public void run() {
         mInstrumentation.callActivityOnStop(mActivity);
         mActivity.finish();
       }
     });
     
     mActivity = null;
     
     if (mImageCache != null) {
       mImageCache.clearCachedBitmaps(true);
     }
    deleteCachedFiles();
     
     super.tearDown();
   }
 
   public void setupActivity(boolean showImageInformation) {
     Log.d(CLASS_TAG, "Create Intent");
     Intent intent = new Intent();
     intent.putExtra(GalDroidApp.INTENT_EXTRA_DISPLAY_GALLERY, mCurrentGallery);
     intent.putExtra(GalDroidApp.INTENT_EXTRA_DISPLAY_OBJECT, mCurrentVisibleChild);
     intent.putExtra(GalDroidApp.INTENT_EXTRA_SHOW_IMAGE_INFO, showImageInformation);
     setActivityIntent(intent);
 
     Log.d(CLASS_TAG, "getActivity");
 
     mActivity = getActivity();
     System.gc();
 
     Log.d(CLASS_TAG, "extract Controls");
     mGalleryFullscreen = (Gallery) mActivity.findViewById(R.id.singleImageGallery);
     mGalleryThumbnails = (Gallery) mActivity.findViewById(R.id.thumbnailImageGallery);
 
     ImageAdapter adapter = (ImageAdapter) mGalleryFullscreen.getAdapter();
     mImageLoaderTask = adapter.getImageLoaderTask();
 
     mImageInformationView = (ImageInformationView) mActivity.findViewById(R.id.viewImageInformations);
   }
 
   public void testActivityStart() throws Exception {
     setupActivity(false);
     checkStartUp(View.GONE);
   }
 
   public void testActivityStartWithOpeningInformations() throws Exception {
     setupActivity(false);
     checkStartUp(View.GONE);
     GalleryImageView imageView = (GalleryImageView) mGalleryFullscreen.getSelectedView();
     TestGalleryObject galleryObject = (TestGalleryObject) imageView.getGalleryObject();
 
     List<GalleryObjectComment> comments = new ArrayList<GalleryObjectComment>(5);
     comments.add(new TestGalleryObjectComment("Some Author", "First Comment"));
     comments.add(new TestGalleryObjectComment("Some other Author", "Second Comment"));
     comments.add(new TestGalleryObjectComment("Some Author", "give some more comment"));
     comments.add(new TestGalleryObjectComment("administraor", "shut the fuck up"));
     comments.add(new TestGalleryObjectComment("Test", "Test"));
 
     mWebGallery.setupGetDisplayObjectCommentsCall(galleryObject, comments);
 
     List<String> tags = new ArrayList<String>(5);
     tags.add("Some");
     tags.add("realy");
     tags.add("incredible");
     tags.add("genius");
     tags.add("tags");
 
     mWebGallery.setupGetDisplayObjectTagsCall(galleryObject, tags);
 
     TouchUtils.clickView(this, mGalleryFullscreen);
     ActionBar actionBar = mActivity.getActionBar();
     assertEquals("The actionbar don't appear", true, actionBar.isShowing());
     View imageButton = mActivity.findViewById(R.id.item_additional_info_object);
     TouchUtils.clickView(this, imageButton);
     assertEquals("The ImageInformationPanel has wrong Visibility", View.VISIBLE, mImageInformationView.getVisibility());
 
     checkImageInformationIsLoadedCorrectly(galleryObject, comments, tags);
   }
 
   public void testActivityStartWithInformations() throws Exception {
     List<TestGalleryObject> children = mCurrentGallery.getChildren();
     TestGalleryObject galleryObject = children.get(0);
 
     List<GalleryObjectComment> comments = new ArrayList<GalleryObjectComment>(5);
     comments.add(new TestGalleryObjectComment("Some Author", "First Comment"));
     comments.add(new TestGalleryObjectComment("Some other Author", "Second Comment"));
     comments.add(new TestGalleryObjectComment("Some Author", "give some more comment"));
     comments.add(new TestGalleryObjectComment("administraor", "shut the fuck up"));
     comments.add(new TestGalleryObjectComment("Test", "Test"));
 
     mWebGallery.setupGetDisplayObjectCommentsCall(galleryObject, comments);
 
     List<String> tags = new ArrayList<String>(5);
     tags.add("Some");
     tags.add("realy");
     tags.add("incredible");
     tags.add("genius");
     tags.add("tags");
 
     mWebGallery.setupGetDisplayObjectTagsCall(galleryObject, tags);
 
     setupActivity(true);
     checkStartUp(View.VISIBLE);
 
     checkImageInformationIsLoadedCorrectly(galleryObject, comments, tags);
 
     checkForUneededDownloads();
   }
 
   private void checkForUneededDownloads() throws InterruptedException {
     // wait if some downloads are enqueued twice
     Thread.sleep(2000);
     List<TestDownloadObject> requestetObjects = mWebGallery.getRequestedDownloadObjects();
     int size = requestetObjects.size();
     if (size > 1) {
       for (int outerIndex = 0; outerIndex < size; outerIndex++) {
         TestDownloadObject outerObject = requestetObjects.get(outerIndex);
         Log.d(CLASS_TAG, String.format("Requested DownloadObject: %s", outerObject));
         for (int innerIndex = outerIndex + 1; innerIndex < size; innerIndex++) {
           TestDownloadObject innerObject = requestetObjects.get(innerIndex);
 
           assertFalse(String.format("%s is requested twice", outerIndex), outerObject.equals(innerObject));
         }
       }
     }
   }
 
   public void testAbortImageLoading() throws Exception {
     Log.d(CLASS_TAG, "Prepare TestEnvironment");
     List<TestGalleryObject> children = mCurrentGallery.getChildren();
     TestGalleryObject galleryObject = children.get(0);
 
     List<GalleryObjectComment> comments = new ArrayList<GalleryObjectComment>(1);
     List<String> tags = new ArrayList<String>(2);
 
     comments.add(new TestGalleryObjectComment("Some Author", String.format("Comment %d", 0)));
 
     tags.add("Some");
     tags.add(String.format("Comment %d", 0));
 
     Log.d(CLASS_TAG, "activate DownloadWait Handle");
     mWebGallery.activateDownloadWaitHandle();
 
     Log.d(CLASS_TAG, "Setup GalleryCalls");
     mWebGallery.setupGetDisplayObjectCommentsCall(galleryObject, comments);
     mWebGallery.setupGetDisplayObjectTagsCall(galleryObject, tags);
 
     Log.d(CLASS_TAG, "start Activity");
     setupActivity(true);
 
     Log.d(CLASS_TAG, "initial Check");
     // Begin Check Startup
     assertEquals("The FullscreenGallery has wrong Visibility", View.VISIBLE, mGalleryFullscreen.getVisibility());
     assertEquals("The ThumbnailGallery has wrong Visibility", View.GONE, mGalleryThumbnails.getVisibility());
     assertEquals("The ImageInformationPanel has wrong Visibility", View.VISIBLE, mImageInformationView.getVisibility());
 
     Log.d(CLASS_TAG, "areGalleryObjectsAvailable");
 
     if (!mActivity.areGalleryObjectsAvailable()) {
       Log.d(CLASS_TAG, "Checking LoaderTask");
       GalleryLoaderTask loaderTask = mActivity.getDownloadTask();
       assertNotNull("No DownloadTask created although no gallery object could be loaded from the cache", loaderTask);
 
       loaderTask.get();
     }
 
     checkImageAdapterIsLoaded();
     checkSelectedViewIsNotNull();
 
     GalleryImageView selectedView = checkSelectedView(mCurrentVisibleChild);
 
     checkImageInformationIsntLoadedBeforImage();
 
     Log.d(CLASS_TAG, "Check ImageLoaderTask and ImageInformationLoaderTask");
     assertFalse("ImageView is loaded without loading an image", selectedView.isLoaded());
     galleryObject = (TestGalleryObject) selectedView.getGalleryObject();
     final GalleryDownloadObject downloadObject = galleryObject.getImage();
     assertTrue(String.format("The DownloadObject isn't enqueued for the gallerImageView %s", selectedView.getGalleryObject().getObjectId()), mImageLoaderTask.isDownloading(downloadObject));
 
     Log.d(CLASS_TAG, "Checking ExtractorTask");
     ImageInformationLoaderTask task = mImageInformationView.getImageInformationLoaderTask();
     assertFalse(String.format("Information for GalleryObject %s is loaded befor Image is downloaded", galleryObject), task.isLoading(galleryObject));
 
     Log.d(CLASS_TAG, "Abort image loading");
 
     ImageAdapter adapter = (ImageAdapter) mGalleryFullscreen.getAdapter();
     selectedView = (GalleryImageView) adapter.getView(1, selectedView, mGalleryFullscreen);
 
     Log.d(CLASS_TAG, "Releasing the FileStream");
     mWebGallery.releaseGetFileStream((TestDownloadObject) selectedView.getGalleryObject().getImage());
 
     checkImageLoaderTask(selectedView);
   }
 
   public void testAbortImageInformationLoading() throws Exception {
     Log.d(CLASS_TAG, "Prepare TestEnvironment");
     List<TestGalleryObject> children = mCurrentGallery.getChildren();
     TestGalleryObject galleryObject = children.get(0);
 
     List<GalleryObjectComment> comments = new ArrayList<GalleryObjectComment>(1);
     List<String> tags = new ArrayList<String>(2);
 
     comments.add(new TestGalleryObjectComment("Some Author", String.format("Comment %d", 0)));
 
     tags.add("Some");
     tags.add(String.format("Comment %d", 0));
 
     Log.d(CLASS_TAG, "activate DownloadWait Handle");
     mWebGallery.activateDownloadWaitHandle();
 
     Log.d(CLASS_TAG, "Setup GalleryCalls");
     mWebGallery.setupGetDisplayObjectCommentsCall(galleryObject, comments);
     mWebGallery.setupGetDisplayObjectTagsCall(galleryObject, tags);
 
     Log.d(CLASS_TAG, "start Activity");
     setupActivity(true);
 
     Log.d(CLASS_TAG, "initial Check");
     // Begin Check Startup
     assertEquals("The FullscreenGallery has wrong Visibility", View.VISIBLE, mGalleryFullscreen.getVisibility());
     assertEquals("The ThumbnailGallery has wrong Visibility", View.GONE, mGalleryThumbnails.getVisibility());
     assertEquals("The ImageInformationPanel has wrong Visibility", View.VISIBLE, mImageInformationView.getVisibility());
 
     Log.d(CLASS_TAG, "areGalleryObjectsAvailable");
 
     if (!mActivity.areGalleryObjectsAvailable()) {
       Log.d(CLASS_TAG, "Checking LoaderTask");
       GalleryLoaderTask loaderTask = mActivity.getDownloadTask();
       assertNotNull("No DownloadTask created although no gallery object could be loaded from the cache", loaderTask);
 
       loaderTask.get();
     }
 
     checkImageAdapterIsLoaded();
     checkSelectedViewIsNotNull();
 
     GalleryImageView selectedView = checkSelectedView(mCurrentVisibleChild);
 
     checkImageInformationIsntLoadedBeforImage();
     checkImageLoaderTask(selectedView);
 
     Log.d(CLASS_TAG, "Check that the ImageInformationLoader tries to download the Tags&Comments");
     ImageInformationLoaderTask task = mImageInformationView.getImageInformationLoaderTask();
     assertTrue(String.format("%s isn't enqueued for loading ImageINformation altough the ImageLoaderTask is finished", galleryObject), task.isLoading(galleryObject));
 
     Log.d(CLASS_TAG, "Do a image-change before the task finished");
     PointF dragFrom = new PointF(1000, 358);
     PointF dragTo = new PointF(400, 358);
     flingGalleryAndCheckImageChange(dragFrom, dragTo);
 
     TestGalleryObject currentObject = children.get(1);
 
     Log.d(CLASS_TAG, "Setup GalleryCalls");
     mWebGallery.setupGetDisplayObjectCommentsCall(currentObject, comments);
     mWebGallery.setupGetDisplayObjectTagsCall(currentObject, tags);
 
     GalleryImageView imageView = (GalleryImageView) mGalleryFullscreen.getSelectedView();
     checkSelectedView(currentObject);
 
     TaskHelper helper = new TaskHelper() {
 
       @Override
       protected boolean checkCondition(long timeElapsed) {
         return mImageInformationView.findViewById(R.id.progressBarTags).getVisibility() == View.GONE;
       }
     };
 
     helper.waitForExecution("ImageInformationView isn't reseted in the given time");
 
     checkImageInformationIsntLoadedBeforImage();
     checkImageLoaderTask(imageView);
 
     checkImageInformationIsLoadedCorrectly(currentObject, comments, tags);
 
     checkForUneededDownloads();
   }
 
   public void testScrollTrough() throws Exception {
     setupActivity(false);
     checkStartUp(View.GONE);
 
     List<TestGalleryObject> children = mCurrentGallery.getChildren();
 
     // fastForward ... to the last image...
     PointF dragFrom = new PointF(1000, 358);
     PointF dragTo = new PointF(500, 358);
 
     for (int pos = 1; pos < 5; pos++) {
       flingGalleryAndCheckImageChange(dragFrom, dragTo);
 
       GalleryImageView imageView = (GalleryImageView) mGalleryFullscreen.getSelectedView();
       checkSelectedView(children.get(pos));
       checkImageLoaderTask(imageView);
     }
 
     checkForUneededDownloads();
   }
 
   public void testScrollTroughWithInformations() throws Exception {
     List<TestGalleryObject> children = mCurrentGallery.getChildren();
     TestGalleryObject galleryObject = children.get(0);
 
     List<GalleryObjectComment> comments = new ArrayList<GalleryObjectComment>(1);
     List<String> tags = new ArrayList<String>(2);
 
     comments.add(new TestGalleryObjectComment("Some Author", String.format("Comment %d", 0)));
 
     tags.add("Some");
     tags.add(String.format("Comment %d", 0));
 
     mWebGallery.setupGetDisplayObjectCommentsCall(galleryObject, comments);
     mWebGallery.setupGetDisplayObjectTagsCall(galleryObject, tags);
 
     setupActivity(true);
     checkStartUp(View.VISIBLE);
 
     checkImageInformationIsLoadedCorrectly(galleryObject, comments, tags);
 
     for (int pos = 1; pos < 5; pos++) {
       galleryObject = children.get(pos);
       Log.d(CLASS_TAG, String.format("Simulating switching to %s", galleryObject));
 
       comments.add(new TestGalleryObjectComment("Some Author", String.format("Comment %d", pos)));
 
       tags.clear();
       tags.add("Some");
       tags.add(String.format("Comment %d", pos));
 
       mWebGallery.setupGetDisplayObjectCommentsCall(galleryObject, comments);
       mWebGallery.setupGetDisplayObjectTagsCall(galleryObject, tags);
       
       selectImage(mGalleryFullscreen, pos);
       
       GalleryImageView imageView = (GalleryImageView) mGalleryFullscreen.getSelectedView();
       checkSelectedView(children.get(pos));
       checkImageLoaderTask(imageView);
 
       checkImageInformationIsLoadedCorrectly(galleryObject, comments, tags);
     }
 
     checkForUneededDownloads();
   }
 
   public void testScrollTroughWithInformationsAndDeferedLoading() throws Exception {
     Log.d(CLASS_TAG, "Prepare TestEnvironment");
     List<TestGalleryObject> children = mCurrentGallery.getChildren();
     TestGalleryObject galleryObject = children.get(0);
 
     List<GalleryObjectComment> comments = new ArrayList<GalleryObjectComment>(1);
     List<String> tags = new ArrayList<String>(2);
 
     comments.add(new TestGalleryObjectComment("Some Author", String.format("Comment %d", 0)));
 
     tags.add("Some");
     tags.add(String.format("Comment %d", 0));
 
     Log.d(CLASS_TAG, "activate DownloadWait Handle");
     mWebGallery.activateDownloadWaitHandle();
 
     Log.d(CLASS_TAG, "Setup GalleryCalls");
     mWebGallery.setupGetDisplayObjectCommentsCall(galleryObject, comments);
     mWebGallery.setupGetDisplayObjectTagsCall(galleryObject, tags);
 
     Log.d(CLASS_TAG, "start Activity");
     setupActivity(true);
 
     Log.d(CLASS_TAG, "initial Check");
 
     // Begin Check Startup
     assertEquals("The FullscreenGallery has wrong Visibility", View.VISIBLE, mGalleryFullscreen.getVisibility());
     assertEquals("The ThumbnailGallery has wrong Visibility", View.GONE, mGalleryThumbnails.getVisibility());
     assertEquals("The ImageInformationPanel has wrong Visibility", View.VISIBLE, mImageInformationView.getVisibility());
 
     Log.d(CLASS_TAG, "areGalleryObjectsAvailable");
 
     if (!mActivity.areGalleryObjectsAvailable()) {
       Log.d(CLASS_TAG, "Checking LoaderTask");
       GalleryLoaderTask loaderTask = mActivity.getDownloadTask();
       assertNotNull("No DownloadTask created although no gallery object could be loaded from the cache", loaderTask);
 
       loaderTask.get();
     }
 
     checkImageAdapterIsLoaded();
     checkSelectedViewIsNotNull();
 
     GalleryImageView selectedView = checkSelectedView(mCurrentVisibleChild);
 
     checkImageInformationIsntLoadedBeforImage();
 
     checkImageLoaderTask(selectedView);
     checkPreLoading(selectedView, 2);
     // End Check Startup
 
     checkImageInformationIsLoadedCorrectly(galleryObject, comments, tags);
 
     for (int pos = 1; pos < 5; pos++) {
       galleryObject = children.get(pos);
       Log.d(CLASS_TAG, String.format("Simulating switching to %s", galleryObject));
 
       comments.add(new TestGalleryObjectComment("Some Author", String.format("Comment %d", pos)));
 
       tags.add("Some");
       tags.add(String.format("Comment %d", pos));
 
       mWebGallery.setupGetDisplayObjectCommentsCall(galleryObject, comments);
       mWebGallery.setupGetDisplayObjectTagsCall(galleryObject, tags);
 
       selectImage(mGalleryFullscreen, pos);
 
       GalleryImageView imageView = (GalleryImageView) mGalleryFullscreen.getSelectedView();
       checkSelectedView(children.get(pos));
       checkImageInformationIsntLoadedBeforImage();
       checkImageLoaderTask(imageView);
 
       checkImageInformationIsLoadedCorrectly(galleryObject, comments, tags);
     }
 
     checkForUneededDownloads();
   }
 
   public void testScrollTroughWithInformationsAndAbortions() throws Exception {
     Log.d(CLASS_TAG, "Prepare TestEnvironment");
     List<TestGalleryObject> children = mCurrentGallery.getChildren();
     TestGalleryObject galleryObject = children.get(0);
     
     List<GalleryObjectComment> comments = new ArrayList<GalleryObjectComment>(1);
     List<String> tags = new ArrayList<String>(2);
 
     comments.add(new TestGalleryObjectComment("Some Author", String.format("Comment %d", 0)));
 
     tags.add("Some");
     tags.add(String.format("Comment %d", 0));
 
     Log.d(CLASS_TAG, "activate DownloadWait Handle");
     mWebGallery.activateDownloadWaitHandle();
 
     Log.d(CLASS_TAG, "Setup GalleryCalls");
     mWebGallery.setupGetDisplayObjectCommentsCall(galleryObject, comments);
     mWebGallery.setupGetDisplayObjectTagsCall(galleryObject, tags);
 
     Log.d(CLASS_TAG, "start Activity");
     setupActivity(true);
 
     Log.d(CLASS_TAG, "initial Check");
     // Begin Check Startup
     assertEquals("The FullscreenGallery has wrong Visibility", View.VISIBLE, mGalleryFullscreen.getVisibility());
     assertEquals("The ThumbnailGallery has wrong Visibility", View.GONE, mGalleryThumbnails.getVisibility());
     assertEquals("The ImageInformationPanel has wrong Visibility", View.VISIBLE, mImageInformationView.getVisibility());
 
     Log.d(CLASS_TAG, "areGalleryObjectsAvailable");
 
     if (!mActivity.areGalleryObjectsAvailable()) {
       Log.d(CLASS_TAG, "Checking LoaderTask");
       GalleryLoaderTask loaderTask = mActivity.getDownloadTask();
       assertNotNull("No DownloadTask created although no gallery object could be loaded from the cache", loaderTask);
 
       loaderTask.get();
     }
 
     checkImageAdapterIsLoaded();
     checkSelectedViewIsNotNull();
 
     GalleryImageView selectedView = checkSelectedView(mCurrentVisibleChild);
 
     checkImageInformationIsntLoadedBeforImage();
 
     checkImageLoaderTask(selectedView);
     checkPreLoading(selectedView, 2);
     // End Check Startup
 
     checkImageInformationIsLoadedCorrectly(galleryObject, comments, tags);
 
     for (int pos = 1; pos < 30; pos++) {
       galleryObject = children.get(pos);
       final GalleryDownloadObject downloadObject = galleryObject.getImage();
       Log.d(CLASS_TAG, String.format("Simulating switching to %s", galleryObject));
 
       if (pos % 5 == 0) {
         comments.add(new TestGalleryObjectComment("Some Author", String.format("Comment %d", pos)));
 
         tags.add("Some");
         tags.add(String.format("Comment %d", pos));
 
         mWebGallery.setupGetDisplayObjectCommentsCall(galleryObject, comments);
         mWebGallery.setupGetDisplayObjectTagsCall(galleryObject, tags);
       }
 
       selectImage(mGalleryFullscreen, pos);
 
       assertEquals("the gallery doesn't select the correct image", pos, mGalleryFullscreen.getSelectedItemPosition());
 
       GalleryImageView imageView = (GalleryImageView) mGalleryFullscreen.getSelectedView();
       checkSelectedView(children.get(pos));
       checkImageInformationIsntLoadedBeforImage();
       if (pos % 5 == 0) {
         TaskHelper taskHelper = new TaskHelper() {
           protected boolean checkCondition(long timeElapsed) {
             return mImageLoaderTask.isDownloading(downloadObject);
           }
         };
         taskHelper.waitForExecution("ImageDownload isn'T enqueued");
         
         while(mImageLoaderTask.getActiveDownload() == null || !downloadObject.equals(mImageLoaderTask.getActiveDownload().getDownloadObject())) {
           if(mImageLoaderTask.getActiveDownload()!=null) {
             mImageLoaderTask.cancelActiveDownload(true);
           }
         }
         
         
         checkImageLoaderTask(imageView);
         checkImageInformationIsLoadedCorrectly(galleryObject, comments, tags);
       }
     }
 
     checkForUneededDownloads();
   }
 
   private void selectImage(final Gallery gallery,final int pos) throws Exception {
     final GalleryObject targetObject = mCurrentGallery.getChildren().get(pos);
     mInstrumentation.runOnMainSync(new Runnable() {
       
       @Override
       public void run() {
         gallery.setSelection(pos);
       }
     });
     
     TaskHelper taskHelper = new TaskHelper() {
       protected boolean checkCondition(long timeElapsed) {
         GalleryImageView view = (GalleryImageView) gallery.getSelectedView();
         if(view == null) {
           return false;
         }
         return view.getGalleryObject().equals(targetObject);
       }
     };
     taskHelper.waitForExecution("Selecting an Image takes to long.");
   }
 
   private void flingGalleryAndCheckImageChange(final PointF dragFrom, final PointF dragTo) throws InterruptedException {
     final int prePos = mGalleryFullscreen.getSelectedItemPosition();
 
     // waiting till the Gallery received the "switch" event
     TaskHelper taskHelper = new TaskHelper() {
       @Override
       protected boolean checkCondition(long diffTime) {
 
         if (diffTime % 1000 < 100) {
           simulateFlingGallery(mGalleryFullscreen, dragFrom, dragTo, 500);
         }
         int currentPos = mGalleryFullscreen.getSelectedItemPosition();
         Log.d(CLASS_TAG, String.format("FlingGallery currentPos: %d lastPos: %d", currentPos, prePos));
         return currentPos > prePos;
       }
     };
     taskHelper.waitForExecution("Switching of an image takes to long");
   }
 
   private void checkStartUp(int ImageInformationPanel) throws Exception {
     assertEquals("The FullscreenGallery has wrong Visibility", View.VISIBLE, mGalleryFullscreen.getVisibility());
     assertEquals("The ThumbnailGallery has wrong Visibility", View.GONE, mGalleryThumbnails.getVisibility());
     assertEquals("The ImageInformationPanel has wrong Visibility", ImageInformationPanel, mImageInformationView.getVisibility());
 
     Log.d(CLASS_TAG, "Activity is set up correctly, waiting that the gallery is loaded...");
     if (!mActivity.areGalleryObjectsAvailable()) {
       GalleryLoaderTask task = mActivity.getDownloadTask();
       assertNotNull("No DownloadTask created although no gallery object could be loaded from the cache", task);
 
       task.get();
     }
 
     checkImageAdapterIsLoaded();
     checkSelectedViewIsNotNull();
 
     Log.d(CLASS_TAG, "Gallery has requested an Image, check the image...");
     GalleryImageView selectedView = checkSelectedView(mCurrentVisibleChild);
     checkImageLoaderTask(selectedView);
     checkPreLoading(selectedView, 2);
   }
 
   private void checkSelectedViewIsNotNull() throws InterruptedException {
     Log.d(CLASS_TAG, "Image adapter is loaded, wait that the Gallery requestes the first image...");
     TaskHelper helper = new TaskHelper() {
 
       @Override
       protected boolean checkCondition(long diffTime) {
         return mGalleryFullscreen.getSelectedView() != null;
       }
     };
     helper.waitForExecution("The Gallery don't select the first View in the given time");
   }
 
   private void checkImageAdapterIsLoaded() throws InterruptedException {
     Log.d(CLASS_TAG, "Gallery is loaded, wait that the ImageAdapter get's loaded");
     final ImageAdapter adapter = (ImageAdapter) mGalleryFullscreen.getAdapter();
 
     TaskHelper helper = new TaskHelper() {
 
       @Override
       protected boolean checkCondition(long diffTime) {
         return adapter.isLoaded();
       }
     };
     helper.waitForExecution("Loading of the GalleryImageAdapter takes to long");
   }
 
   private GalleryImageView checkSelectedView(GalleryObject galleryObject) {
     GalleryImageView selectedView = (GalleryImageView) mGalleryFullscreen.getSelectedView();
 
     assertNotNull("No view selected", selectedView);
     assertEquals("The Gallery don't shows the inteted image", galleryObject, selectedView.getGalleryObject());
     return selectedView;
   }
 
   private void checkPreLoading(GalleryImageView currentView, int minimalLookAhead) {
     GalleryObject currentObject = currentView.getGalleryObject();
     GalleryDownloadObject downloadObject = currentObject.getImage();
     List<TestDownloadObject> requestedObject = mWebGallery.getRequestedDownloadObjects();
 
     int currentIndex = requestedObject.indexOf(downloadObject);
     assertTrue("Current GalleryObject was never requested", currentIndex > -1);
     assertTrue("No preloading requestes", currentIndex >= requestedObject.size() - 1);
     assertTrue("Not enough preloading requestes", currentIndex + minimalLookAhead >= requestedObject.size() - 1);
   }
 
   private void checkImageLoaderTask(final GalleryImageView galleryImageView) throws Exception {
     if (!galleryImageView.isLoaded()) {
       GalleryObject galleryObject = galleryImageView.getGalleryObject();
       final GalleryDownloadObject downloadObject = galleryObject.getImage();
       assertTrue(String.format("The DownloadObject isn't enqueued for the gallerImageView %s", galleryImageView.getGalleryObject().getObjectId()), mImageLoaderTask.isDownloading(downloadObject));
       if (mWebGallery.isDownloadWaitHandleActive()) {
 
         Log.d(CLASS_TAG, "Checking ExtractorTask");
         ImageInformationLoaderTask task = mImageInformationView.getImageInformationLoaderTask();
         assertFalse(String.format("Information for GalleryObject %s is loaded befor Image is downloaded", galleryObject), task.isLoading(galleryObject));
 
         Log.d(CLASS_TAG, "Release LoaderTask");
         mWebGallery.releaseGetFileStream((TestDownloadObject) galleryObject.getImage());
       }
 
       TaskHelper taskHelper = new TaskHelper() {
 
         @Override
         protected boolean checkCondition(long diffTime) {
           return !mImageLoaderTask.isDownloading(downloadObject);
         }
       };
       taskHelper.waitForExecution(String.format("The Download for the gallerImageView %s isn't finished within the given time", galleryImageView.getGalleryObject().getObjectId()));
 
       taskHelper = new TaskHelper() {
 
         @Override
         protected boolean checkCondition(long diffTime) {
           return galleryImageView.isLoaded();
         }
       };
       taskHelper.waitForExecution(String.format("The GalleryImageView %s is not loaded but the imageLoaderTask ist finished", galleryImageView.getGalleryObject().getObjectId()));
     }
   }
 
   private void checkImageInformationIsntLoadedBeforImage() throws InterruptedException {
     Log.d(CLASS_TAG, "Checking that the ImageInformationView is reseted correctly");
     TaskHelper taskHelper = new TaskHelper() {
 
       @Override
       protected boolean checkCondition(long timeElapsed) {
         return !mImageInformationView.isLoaded();
       }
     };
     taskHelper.waitForExecution("ImageInformation - ImageInformationView is loaded before ImageLoaderTask finished");
 
     assertEquals("ImageInformation - The TagLoader-ProgressBar is visible before ImageLoaderTask finished", View.GONE, mImageInformationView.findViewById(R.id.progressBarTags).getVisibility());
     assertEquals("ImageInformation - The CommentLoader-ProgressBar is visible before ImageLoaderTask finished", View.GONE, mImageInformationView.findViewById(R.id.progressBarComments).getVisibility());
 
     TextView textView = (TextView) mImageInformationView.findViewById(R.id.textTitle);
     assertEquals("ImageInformation - There is a Title setted before ImageLoaderTask finished", "", textView.getText());
 
     textView = (TextView) mImageInformationView.findViewById(R.id.textExifExposure);
     assertEquals("ImageInformation - There is are ExifInformation setted before ImageLoaderTask finished", "", textView.getText());
 
     textView = (TextView) mImageInformationView.findViewById(R.id.textTags);
     assertEquals("ImageInformation - There is are Tags setted before ImageLoaderTask finished", "", textView.getText());
 
     ViewGroup rootView = (ViewGroup) mImageInformationView.findViewById(R.id.layoutComments);
     assertEquals("ImageInformation - There are comments displayed before ImageLoaderTask finished", 0, rootView.getChildCount());
   }
 
   private void checkImageInformationIsLoadedCorrectly(final TestGalleryObject galleryObject, List<GalleryObjectComment> comments, List<String> tags) throws InterruptedException, ExecutionException, Exception {
     if (mWebGallery.isDownloadWaitHandleActive()) {
       final ImageInformationLoaderTask task = mImageInformationView.getImageInformationLoaderTask();
       TaskHelper taskHelper = new TaskHelper() {
         protected boolean checkCondition(long diffTime) {
           return task.isLoading(galleryObject);
         }
       };
       taskHelper.waitForExecution(String.format("%s isn't enqueued for loading ImageInformation in the given time, altough the ImageLoaderTask is finished", galleryObject));
 
       taskHelper = new TaskHelper() {
         protected boolean checkCondition(long diffTime) {
           return mImageInformationView.areImageInformationsLoaded();
         }
       };
       taskHelper.waitForExecution(String.format("The loading of the ImageInformations for GalleryObject %s isn't finished within the given time", galleryObject.getObjectId()));
 
       Log.d(CLASS_TAG, String.format("Checks that the ImageInformation of %s are loaded correctly", galleryObject));
       assertEquals("ImageInformation - TagLoader-ProgressBar wrong Visibility", View.VISIBLE, mImageInformationView.findViewById(R.id.progressBarTags).getVisibility());
       assertEquals("ImageInformation - CommentLoader-ProgressBar wrong Visibility", View.VISIBLE, mImageInformationView.findViewById(R.id.progressBarComments).getVisibility());
       checkImageInformationIsLoaded(galleryObject);
 
       TextView textView = (TextView) mImageInformationView.findViewById(R.id.textTags);
       assertEquals("ImageInformation - Tags are not reseted correctly", "", textView.getText().toString());
 
       mWebGallery.releaseGetGetDisplayObjectTags(galleryObject);
       taskHelper = new TaskHelper() {
 
         @Override
         protected boolean checkCondition(long diffTime) {
           return mImageInformationView.areTagsLoaded();
         }
       };
       taskHelper.waitForExecution(String.format("The loading of the Tags for GalleryObject %s isn't finished within the given time", galleryObject.getObjectId()));
 
       assertEquals("ImageInformation - TagLoader-ProgressBar wrong Visibility", View.GONE, mImageInformationView.findViewById(R.id.progressBarTags).getVisibility());
       assertEquals("ImageInformation - CommentLoader-ProgressBar wrong Visibility", View.VISIBLE, mImageInformationView.findViewById(R.id.progressBarComments).getVisibility());
 
       checkTagAreLoaded(tags);
 
       mWebGallery.releaseGetDisplayObjectComments(galleryObject);
 
       taskHelper = new TaskHelper() {
 
         @Override
         protected boolean checkCondition(long diffTime) {
           return mImageInformationView.areCommentsLoaded();
         }
       };
       taskHelper.waitForExecution(String.format("The loading of the Comments for GalleryObject %s isn't finished within the given time", galleryObject.getObjectId()));
 
       assertEquals("ImageInformation - TagLoader-ProgressBar wrong Visibility", View.GONE, mImageInformationView.findViewById(R.id.progressBarTags).getVisibility());
       assertEquals("ImageInformation - CommentLoader-ProgressBar wrong Visibility", View.GONE, mImageInformationView.findViewById(R.id.progressBarComments).getVisibility());
 
       checkCommentsAreLoaded(comments);
     } else {
       if (!mImageInformationView.isLoaded()) {
         Log.d(CLASS_TAG, String.format("Wait for the ImageInformationLoaderTask to load %s", galleryObject));
         TaskHelper taskHelper = new TaskHelper() {
 
           @Override
           protected boolean checkCondition(long diffTime) {
             return mImageInformationView.isLoaded();
           }
         };
         taskHelper.waitForExecution(String.format("The loading of Imageinformation for the GalleryObject %s isn't finished within the given time", galleryObject.getObjectId()));
       }
 
       Log.d(CLASS_TAG, String.format("Checks that the ImageInformation of %s are loaded correctly", galleryObject));
       assertEquals("ImageInformation - The TagLoader-ProgressBar is visible although ImageLoaderTask finished", View.GONE, mImageInformationView.findViewById(R.id.progressBarTags).getVisibility());
       assertEquals("ImageInformation - The CommentLoader-ProgressBar is visible although ImageLoaderTask finished", View.GONE, mImageInformationView.findViewById(R.id.progressBarComments)
           .getVisibility());
 
       checkImageInformationIsLoaded(galleryObject);
       checkTagAreLoaded(tags);
       checkCommentsAreLoaded(comments);
     }
     assertNull("ImageInformation - The ImageInformationView still listening to changes altough it is finished loading", mImageInformationView.getCurrentListenedImageView());
     assertNull("ImageInformation - The ImageInformationView still remember it last LoadingItam altough it is finished loading", mImageInformationView.getCurrentLoadingObject());
   }
 
   private void checkImageInformationIsLoaded(TestGalleryObject galleryObject) throws Exception {
     checkEmbededInformationIsLoaded(galleryObject);
     checkExifInformationIsLoaded();
   }
 
   private void checkEmbededInformationIsLoaded(TestGalleryObject galleryObject) {
     TextView textView = (TextView) mImageInformationView.findViewById(R.id.textTitle);
     assertEquals("ImageInformation - Title is set wrong", galleryObject.getTitle(), textView.getText().toString());
 
     textView = (TextView) mImageInformationView.findViewById(R.id.textUploadDate);
     Log.d(CLASS_TAG, String.format("%s - %s", galleryObject.getDateUploaded().toLocaleString(), textView.getText().toString()));
     assertEquals("ImageInformation - UploadDate is set wrong", galleryObject.getDateUploaded().toLocaleString(), textView.getText().toString());
   }
 
   private void checkExifInformationIsLoaded() {
     TextView textField;
     textField = (TextView) mImageInformationView.findViewById(R.id.textExifCreateDate);
     textField = (TextView) mImageInformationView.findViewById(R.id.textExifAperture);
     textField = (TextView) mImageInformationView.findViewById(R.id.textExifExposure);
     textField = (TextView) mImageInformationView.findViewById(R.id.textExifFlash);
     textField = (TextView) mImageInformationView.findViewById(R.id.textExifISO);
     textField = (TextView) mImageInformationView.findViewById(R.id.textExifModel);
     textField = (TextView) mImageInformationView.findViewById(R.id.textExifModel);
     textField = (TextView) mImageInformationView.findViewById(R.id.textExifMake);
     textField = (TextView) mImageInformationView.findViewById(R.id.textExifFocalLength);
     textField = (TextView) mImageInformationView.findViewById(R.id.textExifWhiteBalance);
     textField = (TextView) mImageInformationView.findViewById(R.id.textGeoLat);
     textField = (TextView) mImageInformationView.findViewById(R.id.textGeoLong);
     textField = (TextView) mImageInformationView.findViewById(R.id.textGeoHeight);
   }
 
   private void checkCommentsAreLoaded(List<GalleryObjectComment> comments) throws Exception {
     ViewGroup rootView = (ViewGroup) mImageInformationView.findViewById(R.id.layoutComments);
     assertEquals("ImageInformation - Commentscount isn't correct.", comments.size(), rootView.getChildCount());
     DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
     for (int count = 0; count < rootView.getChildCount(); count++) {
       View commentView = rootView.getChildAt(count);
       GalleryObjectComment comment = comments.get(count);
 
       TextView textAuthor = (TextView) commentView.findViewById(R.id.textCommentAuthor);
       TextView textDate = (TextView) commentView.findViewById(R.id.textCommentPosted);
       TextView textMessage = (TextView) commentView.findViewById(R.id.textCommentMessage);
 
       assertEquals("ImageInformation - Comment-Author is set wrong", comment.getAuthorName(), textAuthor.getText().toString());
       assertEquals("ImageInformation - Comment-Date is set wrong", dateFormat.format(comment.getCreateDate()), textDate.getText().toString());
       assertEquals("ImageInformation - Comment-Message is set wrong", comment.getMessage(), textMessage.getText().toString());
     }
   }
 
   private void checkTagAreLoaded(List<String> tags) throws Exception {
 
     StringBuilder stringBuilder = new StringBuilder(tags.size() * 10);
     for (String tag : tags) {
       stringBuilder.append(String.format("%s, ", tag));
     }
     int length = stringBuilder.length();
     if (length > 0) {
       stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
     }
 
     TextView textView = (TextView) mImageInformationView.findViewById(R.id.textTags);
     assertEquals("ImageInformation - Tags are set wrong", stringBuilder.toString(), textView.getText().toString());
   }
 
   private TestWebGallery createTestWebGallery(Resources resources) {
     TestWebGallery webGallery = new TestWebGallery(resources);
 
     List<TestGalleryObject> children = new ArrayList<TestGalleryObject>(GALLERY_SAMPLE_SIZE);
     Random random = new Random();
     for (int i = 0; i < GALLERY_SAMPLE_SIZE; i++) {
       String objectId = String.format("TestGalObject %d-%d", i, random.nextInt(10000));
       Date currentDate = new Date(System.currentTimeMillis());
 
       children.add(new TestGalleryObject(objectId, objectId, currentDate, IMAGE_ID, null));
     }
     String objectId = "ParentObject";
     Date currentDate = new Date(System.currentTimeMillis());
     List<TestGalleryObject> parents = new ArrayList<TestGalleryObject>(1);
     parents.add(new TestGalleryObject(objectId, objectId, currentDate, IMAGE_ID, children));
 
     webGallery.setTestGalleryObjects(parents);
 
     mCurrentGallery = parents.get(0);
     mCurrentVisibleChild = children.get(0);
     return webGallery;
   }
 
   private void simulateDragGallery(PointF from, PointF to) {
     long startTime = SystemClock.uptimeMillis() - 1000;
     MotionEvent startEvent = MotionEvent.obtain(startTime, startTime, MotionEvent.ACTION_DOWN, from.x, from.y, 0);
     MotionEvent stopEvent = MotionEvent.obtain(startTime, SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, to.x, to.y, 0);
     mInstrumentation.sendPointerSync(startEvent);
     mInstrumentation.sendPointerSync(stopEvent);
   }
 
   private void simulateFlingGallery(Gallery gallery, PointF from, PointF to, int time) {
     long startTime = SystemClock.uptimeMillis() - time;
     MotionEvent startEvent = MotionEvent.obtain(startTime, startTime, MotionEvent.ACTION_DOWN, from.x, from.y, 0);
     MotionEvent stopEvent = MotionEvent.obtain(startTime, SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, to.x, to.y, 0);
     float seconds = time / 1000f;
     float velocityX = (to.x - from.x) / seconds;
     float velocityY = (to.y - from.y) / seconds;
     gallery.onFling(startEvent, stopEvent, velocityX, velocityY);
   }
 }
