 /*
  * Copyright (C) 2008 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package android.widget.cts;
 
 import com.android.cts.stub.R;
 
 import dalvik.annotation.TestLevel;
 import dalvik.annotation.TestTargetClass;
 import dalvik.annotation.TestTargetNew;
 import dalvik.annotation.TestTargets;
 import dalvik.annotation.ToBeFixed;
 
 import org.xmlpull.v1.XmlPullParser;
 
 import android.app.Activity;
 import android.app.Instrumentation;
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.test.ActivityInstrumentationTestCase2;
 import android.test.TouchUtils;
 import android.test.UiThreadTest;
 import android.util.AttributeSet;
 import android.util.Xml;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.animation.cts.DelayedCheck;
 import android.widget.ImageButton;
 import android.widget.MediaController;
 import android.widget.ProgressBar;
 import android.widget.VideoView;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 /**
  * Test {@link MediaController}.
  */
 @TestTargetClass(MediaController.class)
 public class MediaControllerTest extends
         ActivityInstrumentationTestCase2<MediaControllerStubActivity> {
     private MediaController mMediaController;
     private Activity mActivity;
     private Instrumentation mInstrumentation;
     private static final long DEFAULT_TIMEOUT = 3000;
 
     public MediaControllerTest() {
         super("com.android.cts.stub", MediaControllerStubActivity.class);
     }
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         mActivity = getActivity();
         mInstrumentation = getInstrumentation();
     }
 
     @TestTargets({
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             notes = "Test constructor(s) of {@link MediaController}",
             method = "MediaController",
             args = {android.content.Context.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             notes = "Test constructor(s) of {@link MediaController}",
             method = "MediaController",
             args = {android.content.Context.class, android.util.AttributeSet.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             notes = "Test constructor(s) of {@link MediaController}",
             method = "MediaController",
             args = {android.content.Context.class, boolean.class}
         )
     })
     public void testConstructor() {
         new MediaController(mActivity, null);
 
         new MediaController(mActivity, true);
 
         new MediaController(mActivity);
 
         final XmlPullParser parser =
                 mActivity.getResources().getXml(R.layout.mediacontroller_layout);
         final AttributeSet attrs = Xml.asAttributeSet(parser);
         new MediaController(mActivity, attrs);
     }
 
     @TestTargets({
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             notes = "Test {@link MediaController#onFinishInflate()}",
             method = "onFinishInflate",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             notes = "Test {@link MediaController#onFinishInflate()}",
             method = "setAnchorView",
             args = {android.view.View.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             notes = "Test {@link MediaController#onFinishInflate()}",
             method = "setMediaPlayer",
             args = {android.widget.MediaController.MediaPlayerControl.class}
         )
     })
     /**
      * scenario description:
      * 1. Show the MediaController.
      * 2. Click Pause button to check if MediaPlayerControl is not playing.
      * 3. Click forward and reward buttons to check if current position is right.
      * 4. Check forward button's status is GONE.
      */
     @UiThreadTest
     @ToBeFixed(bug = "1695243", explanation = "setAnchorView() must be called before show(), " +
             "javadoc does not declare the preconditions for showing. " +
             "And javadoc does not declare the default status is paused.")
     public void testMediaController() {
         mMediaController = new MediaController(mActivity);
         final MockMediaPlayerControl mediaPlayerControl = new MockMediaPlayerControl();
         mMediaController.setMediaPlayer(mediaPlayerControl);
 
         assertFalse(mMediaController.isShowing());
         mMediaController.show();
         // setAnchorView() must be called before show(),
         // otherwise MediaController never show.
         assertFalse(mMediaController.isShowing());
 
         View videoview = mActivity.findViewById(R.id.mediacontroller_videoview);
         mMediaController.setAnchorView(videoview);
 
         mMediaController.show();
         assertTrue(mMediaController.isShowing());
 
         final ImageButton pauseButton =
                 (ImageButton) mMediaController.findViewById(com.android.internal.R.id.pause);
         assertNotNull(pauseButton);
         pauseButton.requestFocus();
         final Drawable d1 = pauseButton.getDrawable();
         assertFalse(mediaPlayerControl.isPlaying());
         pauseButton.performClick();
 
         final Drawable d2 = pauseButton.getDrawable();
         assertNotSame(d1, d2);
 
         assertTrue(mediaPlayerControl.isPlaying());
 
         ImageButton ffwdButton = (ImageButton) mMediaController.findViewById(
                 com.android.internal.R.id.ffwd);
         assertNotNull(ffwdButton);
         int origPos = mediaPlayerControl.getCurrentPosition();
         ffwdButton.performClick();
         assertTrue(mediaPlayerControl.getCurrentPosition() > origPos);
 
         final ImageButton rewButton = (ImageButton) mMediaController.findViewById(
                 com.android.internal.R.id.rew);
         assertNotNull(rewButton);
         origPos = mediaPlayerControl.getCurrentPosition();
         rewButton.performClick();
         assertTrue(mediaPlayerControl.getCurrentPosition() < origPos);
 
         final ImageButton nextButton = (ImageButton) mMediaController.findViewById(
                 com.android.internal.R.id.next);
         assertEquals(View.GONE, nextButton.getVisibility());
         final ImageButton prevButton = (ImageButton) mMediaController.findViewById(
                 com.android.internal.R.id.prev);
         assertEquals(View.GONE, prevButton.getVisibility());
 
         mMediaController = new MediaController(mActivity, false);
         mMediaController.setMediaPlayer(mediaPlayerControl);
         videoview = mActivity.findViewById(R.id.mediacontroller_videoview);
         mMediaController.setAnchorView(videoview);
 
         mMediaController.show();
         assertTrue(mMediaController.isShowing());
 
         ffwdButton = (ImageButton) mMediaController.findViewById(com.android.internal.R.id.ffwd);
         assertNotNull(ffwdButton);
         // forward button is GONE
         assertEquals(View.GONE, ffwdButton.getVisibility());
     }
 
     @TestTargets({
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             notes = "Test {@link MediaController#isShowing()}",
             method = "isShowing",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             notes = "Test {@link MediaController#isShowing()}",
             method = "hide",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             notes = "Test {@link MediaController#isShowing()}",
             method = "show",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             notes = "Test {@link MediaController#isShowing()}",
             method = "show",
             args = {int.class}
         )
     })
     @ToBeFixed(bug = "1559790", explanation = "isShowing() should return false after time out, " +
             "but MediaController still shows, this may be a bug.")
     public void testShow() {
         mMediaController = new MediaController(mActivity, true);
         assertFalse(mMediaController.isShowing());
 
         final MockMediaPlayerControl mediaPlayerControl = new MockMediaPlayerControl();
         mMediaController.setMediaPlayer(mediaPlayerControl);
 
         final VideoView videoView =
                 (VideoView) mActivity.findViewById(R.id.mediacontroller_videoview);
         mMediaController.setAnchorView(videoView);
 
         mActivity.runOnUiThread(new Runnable() {
             public void run() {
                 mMediaController.show();
             }
         });
         mInstrumentation.waitForIdleSync();
         assertTrue(mMediaController.isShowing());
 
         mActivity.runOnUiThread(new Runnable() {
             public void run() {
                 mMediaController.hide();
             }
         });
         mInstrumentation.waitForIdleSync();
         assertFalse(mMediaController.isShowing());
 
         final int timeout = 2000;
         mActivity.runOnUiThread(new Runnable() {
             public void run() {
                 mMediaController.show(timeout);
             }
         });
         mInstrumentation.waitForIdleSync();
         assertTrue(mMediaController.isShowing());
 
         // isShowing() should return false, but MediaController still shows, this may be a bug.
         new DelayedCheck(timeout + 500) {
             @Override
             protected boolean check() {
                 return mMediaController.isShowing();
             }
         }.run();
     }
 
     private String prepareSampleVideo() {
         InputStream source = null;
         OutputStream target = null;
         final String VIDEO_NAME   = "testvideo.3gp";
 
         try {
             source = mActivity.getResources().openRawResource(R.raw.testvideo);
             target = mActivity.openFileOutput(VIDEO_NAME, Context.MODE_WORLD_READABLE);
 
             final byte[] buffer = new byte[1024];
             for (int len = source.read(buffer); len > 0; len = source.read(buffer)) {
                 target.write(buffer, 0, len);
             }
         } catch (final IOException e) {
             fail(e.getMessage());
         } finally {
             try {
                 if (source != null) {
                     source.close();
                 }
                 if (target != null) {
                     target.close();
                 }
             } catch (final IOException _) {
                 // Ignore the IOException.
             }
         }
 
         return mActivity.getFileStreamPath(VIDEO_NAME).getAbsolutePath();
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test {@link MediaController#onTouchEvent(MotionEvent)}, " +
                 "this function always returns true",
         method = "onTouchEvent",
         args = {android.view.MotionEvent.class}
     )
     @ToBeFixed(bug = "1559790", explanation = "MediaController does not appear " +
             "when the user touches the anchor view.")
     public void testOnTouchEvent() {
         final XmlPullParser parser =
                 mActivity.getResources().getXml(R.layout.mediacontroller_layout);
         final AttributeSet attrs = Xml.asAttributeSet(parser);
         mMediaController = new MediaController(mActivity, attrs);
         final MockMediaPlayerControl mediaPlayerControl = new MockMediaPlayerControl();
         mMediaController.setMediaPlayer(mediaPlayerControl);
 
         final VideoView videoView =
                 (VideoView) mActivity.findViewById(R.id.mediacontroller_videoview);
         videoView.setMediaController(mMediaController);
         mActivity.runOnUiThread(new Runnable() {
             public void run() {
                 videoView.setVideoPath(prepareSampleVideo());
             }
         });
         mInstrumentation.waitForIdleSync();
 
         assertFalse(mMediaController.isShowing());
         TouchUtils.tapView(this, videoView);
         mInstrumentation.waitForIdleSync();
 
         // isShowing() should return true, but MediaController still not shows, this may be a bug.
         assertFalse(mMediaController.isShowing());
 
         // timeout is larger than duration, in case the system is sluggish
         new DelayedCheck(DEFAULT_TIMEOUT + 500) {
             @Override
             protected boolean check() {
                 return !mMediaController.isShowing();
             }
         }.run();
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test {@link MediaController#onTrackballEvent(MotionEvent)}, " +
                 "this function always returns false",
         method = "onTrackballEvent",
         args = {android.view.MotionEvent.class}
     )
     @ToBeFixed(bug = "1559790", explanation = "MediaController does not show after " +
             "a track ball event is processed.")
     public void testOnTrackballEvent() {
         mMediaController = new MediaController(mActivity);
         final MockMediaPlayerControl mediaPlayerControl = new MockMediaPlayerControl();
         mMediaController.setMediaPlayer(mediaPlayerControl);
 
         final VideoView videoView =
                 (VideoView) mActivity.findViewById(R.id.mediacontroller_videoview);
         videoView.setMediaController(mMediaController);
         mActivity.runOnUiThread(new Runnable() {
             public void run() {
                 videoView.setVideoPath(prepareSampleVideo());
                 videoView.requestFocus();
             }
         });
         mInstrumentation.waitForIdleSync();
 
        assertFalse(mMediaController.isShowing());

         final long curTime = System.currentTimeMillis();
         // get the center of the VideoView.
         final int[] xy = new int[2];
         videoView.getLocationOnScreen(xy);
 
         final int viewWidth = videoView.getWidth();
         final int viewHeight = videoView.getHeight();
 
         final float x = xy[0] + viewWidth / 2.0f;
         final float y = xy[1] + viewHeight / 2.0f;
         final MotionEvent event = MotionEvent.obtain(curTime, 100,
                 MotionEvent.ACTION_DOWN, x, y, 0);
         mInstrumentation.sendTrackballEventSync(event);
         mInstrumentation.waitForIdleSync();
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test {@link MediaController#dispatchKeyEvent(KeyEvent)}",
         method = "dispatchKeyEvent",
         args = {android.view.KeyEvent.class}
     )
     @ToBeFixed(bug = "1559790", explanation = "MediaController does not appear " +
             "when the user presses a key.")
     public void testDispatchKeyEvent() {
         mMediaController = new MediaController(mActivity);
         final MockMediaPlayerControl mediaPlayerControl = new MockMediaPlayerControl();
         mMediaController.setMediaPlayer(mediaPlayerControl);
 
         final VideoView videoView =
                 (VideoView) mActivity.findViewById(R.id.mediacontroller_videoview);
         videoView.setMediaController(mMediaController);
         mActivity.runOnUiThread(new Runnable() {
             public void run() {
                 videoView.setVideoPath(prepareSampleVideo());
                 videoView.requestFocus();
             }
         });
         mInstrumentation.waitForIdleSync();
 
        assertFalse(mMediaController.isShowing());
         mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_SPACE);
         mInstrumentation.waitForIdleSync();
 
         // isShowing() should return true, but MediaController still not shows, this may be a bug.
         assertFalse(mMediaController.isShowing());
 
         // timeout is larger than duration, in case the system is sluggish
         new DelayedCheck(DEFAULT_TIMEOUT + 500) {
             @Override
             protected boolean check() {
                 return !mMediaController.isShowing();
             }
         }.run();
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test {@link MediaController#setEnabled(boolean)}",
         method = "setEnabled",
         args = {boolean.class}
     )
     @UiThreadTest
     public void testSetEnabled() {
         final View videoView = mActivity.findViewById(R.id.mediacontroller_videoview);
         final MockMediaPlayerControl mediaPlayerControl = new MockMediaPlayerControl();
 
         mMediaController = new MediaController(mActivity);
         mMediaController.setAnchorView(videoView);
         mMediaController.setMediaPlayer(mediaPlayerControl);
 
         final MockOnClickListener next = new MockOnClickListener();
         final MockOnClickListener prev = new MockOnClickListener();
         mMediaController.setPrevNextListeners(next, prev);
 
         mMediaController.show();
 
         final ImageButton pauseButton = (ImageButton) mMediaController.findViewById(
                 com.android.internal.R.id.pause);
         final ImageButton nextButton = (ImageButton) mMediaController.findViewById(
                 com.android.internal.R.id.next);
         final ImageButton ffwdButton = (ImageButton) mMediaController.findViewById(
                 com.android.internal.R.id.ffwd);
         final ImageButton rewButton = (ImageButton) mMediaController.findViewById(
                 com.android.internal.R.id.rew);
         final ImageButton prevButton = (ImageButton) mMediaController.findViewById(
                 com.android.internal.R.id.prev);
         final ProgressBar progress = (ProgressBar) mMediaController.findViewById(
                 com.android.internal.R.id.mediacontroller_progress);
 
         mMediaController.setEnabled(true);
 
         assertTrue(mMediaController.isEnabled());
         assertTrue(pauseButton.isEnabled());
         assertTrue(ffwdButton.isEnabled());
         assertTrue(rewButton.isEnabled());
         assertTrue(nextButton.isEnabled());
         assertTrue(prevButton.isEnabled());
         assertTrue(progress.isEnabled());
 
         mMediaController.setEnabled(false);
 
         assertFalse(mMediaController.isEnabled());
         assertFalse(pauseButton.isEnabled());
         assertFalse(ffwdButton.isEnabled());
         assertFalse(rewButton.isEnabled());
         assertFalse(nextButton.isEnabled());
         assertFalse(prevButton.isEnabled());
         assertFalse(progress.isEnabled());
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         notes = "Test " +
                 "{@link MediaController#setPrevNextListeners(OnClickListener,OnClickListener)}",
         method = "setPrevNextListeners",
         args = {android.view.View.OnClickListener.class, android.view.View.OnClickListener.class}
     )
     public void testSetPrevNextListeners() {
         final View videoView = mActivity.findViewById(R.id.mediacontroller_videoview);
         final MockMediaPlayerControl mediaPlayerControl = new MockMediaPlayerControl();
 
         mMediaController = new MediaController(mActivity);
         mMediaController.setAnchorView(videoView);
         mMediaController.setMediaPlayer(mediaPlayerControl);
 
         final ImageButton nextButton = (ImageButton) mMediaController.findViewById(
                 com.android.internal.R.id.next);
         final ImageButton prevButton = (ImageButton) mMediaController.findViewById(
                 com.android.internal.R.id.prev);
         assertFalse(nextButton.performClick());
         assertFalse(prevButton.performClick());
 
         final MockOnClickListener next = new MockOnClickListener();
         final MockOnClickListener prev = new MockOnClickListener();
         mMediaController.setPrevNextListeners(next, prev);
 
         assertTrue(nextButton.isEnabled());
         assertTrue(prevButton.isEnabled());
 
         assertTrue(nextButton.performClick());
         assertTrue(next.hasOnClickCalled());
 
         assertTrue(prevButton.performClick());
         assertTrue(prev.hasOnClickCalled());
     }
 
     private static class MockMediaPlayerControl implements MediaController.MediaPlayerControl {
         private boolean mIsPlayingCalled = false;
         private boolean mIsPlaying = false;
         private int mPosition = 0;
 
         public boolean hasIsPlayingCalled() {
             return mIsPlayingCalled;
         }
 
         public void start() {
             mIsPlaying = true;
         }
 
         public void pause() {
             mIsPlaying = false;
         }
 
         public int getDuration() {
             return 0;
         }
 
         public int getCurrentPosition() {
             return mPosition;
         }
 
         public void seekTo(int pos) {
             mPosition = pos;
         }
 
         public boolean isPlaying() {
             mIsPlayingCalled = true;
             return mIsPlaying;
         }
 
         public int getBufferPercentage() {
             return 0;
         }
     }
 
     private static class MockOnClickListener implements OnClickListener {
         private boolean mOnClickCalled = false;
 
         public boolean hasOnClickCalled() {
             return mOnClickCalled;
         }
 
         public void onClick(View v) {
             mOnClickCalled = true;
         }
     }
 }
