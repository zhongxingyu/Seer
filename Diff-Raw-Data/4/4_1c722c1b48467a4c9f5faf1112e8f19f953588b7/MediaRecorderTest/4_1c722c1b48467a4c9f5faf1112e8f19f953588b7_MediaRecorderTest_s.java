 /*
  * Copyright (C) 2009 The Android Open Source Project
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
 package android.media.cts;
 
 import dalvik.annotation.BrokenTest;
 import dalvik.annotation.TestLevel;
 import dalvik.annotation.TestTargetClass;
 import dalvik.annotation.TestTargetNew;
 import dalvik.annotation.TestTargets;
 
 import android.hardware.Camera;
 import android.media.MediaRecorder;
 import android.media.MediaRecorder.OnErrorListener;
 import android.media.MediaRecorder.OnInfoListener;
 import android.os.Environment;
 import android.test.ActivityInstrumentationTestCase2;
 import android.view.Surface;
 
 import java.io.File;
 import java.io.FileDescriptor;
 import java.io.FileOutputStream;
 
 @TestTargetClass(MediaRecorder.class)
 public class MediaRecorderTest extends ActivityInstrumentationTestCase2<MediaStubActivity> {
 
     private final String OUTPUT_PATH;
     private static final int RECORD_TIME = 3000;
     private static final int VIDEO_WIDTH = 320;
     private static final int VIDEO_HEIGHT = 240;
     private static final int FRAME_RATE = 15;
     private static final long MAX_FILE_SIZE = 5000;
     private static final int MAX_DURATION_MSEC = 200;
     private boolean mOnInfoCalled;
     private boolean mOnErrorCalled;
     private File mOutFile;
     private Camera mCamera;
 
     /*
      * InstrumentationTestRunner.onStart() calls Looper.prepare(), which creates a looper
      * for the current thread. However, since we don't actually call loop() in the test,
      * any messages queued with that looper will never be consumed. We instantiate the recorder
      * in the constructor, before setUp(), so that its constructor does not see the
      * nonfunctional Looper.
      */
     private MediaRecorder mMediaRecorder = new MediaRecorder();
 
     public MediaRecorderTest() {
         super("com.android.cts.stub", MediaStubActivity.class);
         OUTPUT_PATH = new File(Environment.getExternalStorageDirectory(),
                 "record.out").getAbsolutePath();
     }
 
     @Override
     protected void setUp() throws Exception {
         mOutFile = new File(OUTPUT_PATH);
         mMediaRecorder.reset();
         mMediaRecorder.setOutputFile(OUTPUT_PATH);
         mMediaRecorder.setOnInfoListener(new OnInfoListener() {
             public void onInfo(MediaRecorder mr, int what, int extra) {
                 mOnInfoCalled = true;
             }
         });
         mMediaRecorder.setOnErrorListener(new OnErrorListener() {
             public void onError(MediaRecorder mr, int what, int extra) {
                 mOnErrorCalled = true;
             }
         });
         super.setUp();
     }
 
     @Override
     protected void tearDown() throws Exception {
         mMediaRecorder.release();
         if (mOutFile != null && mOutFile.exists()) {
             mOutFile.delete();
         }
         if (mCamera != null)  {
             mCamera.release();
         }
         super.tearDown();
     }
 
     @TestTargets({
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "MediaRecorder",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "release",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "setOutputFormat",
             args = {int.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "setVideoEncoder",
             args = {int.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "setVideoFrameRate",
             args = {int.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "setVideoSize",
             args = {int.class, int.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "setVideoSource",
             args = {int.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "setOutputFile",
             args = {String.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "reset",
             args = {}
         )
     })
     public void testRecorderCamera() throws Exception {
         mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
         mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
         mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
         mMediaRecorder.setVideoFrameRate(FRAME_RATE);
         mMediaRecorder.setVideoSize(VIDEO_WIDTH, VIDEO_HEIGHT);
         mMediaRecorder.setPreviewDisplay(getActivity().getSurfaceHolder().getSurface());
         mMediaRecorder.prepare();
         mMediaRecorder.start();
         Thread.sleep(1000);
         mMediaRecorder.stop();
         checkOutputExist();
     }
 
     @TestTargetNew(
         level = TestLevel.COMPLETE,
         method = "setCamera",
         args = {Camera.class}
     )
     @BrokenTest(value="No longer works in Donut. CameraService reports: " +
             "Attempt to use locked camera from different process")
     public void testSetCamera() throws Exception {
         mCamera = Camera.open();
         mMediaRecorder.setCamera(mCamera);
         mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
         mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
         mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
         mMediaRecorder.setVideoFrameRate(FRAME_RATE);
         mMediaRecorder.setVideoSize(VIDEO_WIDTH, VIDEO_HEIGHT);
         mMediaRecorder.setPreviewDisplay(getActivity().getSurfaceHolder().getSurface());
         mMediaRecorder.setOutputFile(OUTPUT_PATH);
 
         mMediaRecorder.prepare();
         mMediaRecorder.start();
         Thread.sleep(1000);
         mMediaRecorder.stop();
         assertTrue(mOutFile.exists());
     }
 
     private void checkOutputExist() {
         assertTrue(mOutFile.exists());
         assertTrue(mOutFile.length() > 0);
         assertTrue(mOutFile.delete());
     }
 
     @TestTargets({
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "MediaRecorder",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "prepare",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "setOutputFile",
             args = {FileDescriptor.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "setOutputFormat",
             args = {int.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "setPreviewDisplay",
             args = {Surface.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "setVideoEncoder",
             args = {int.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "setVideoFrameRate",
             args = {int.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "setVideoSize",
             args = {int.class, int.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "setVideoSource",
             args = {int.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "start",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "stop",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "setMaxFileSize",
             args = {long.class}
         )
     })
     public void testRecorderVideo() throws Exception {
         mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
         mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
         mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
         mMediaRecorder.setPreviewDisplay(getActivity().getSurfaceHolder().getSurface());
         mMediaRecorder.setVideoFrameRate(FRAME_RATE);
         mMediaRecorder.setVideoSize(VIDEO_WIDTH, VIDEO_HEIGHT);
         FileOutputStream fos = new FileOutputStream(OUTPUT_PATH);
         FileDescriptor fd = fos.getFD();
         mMediaRecorder.setOutputFile(fd);
         long maxFileSize = MAX_FILE_SIZE * 10;
         recordMedia(maxFileSize);
     }
 
     @TestTargets({
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "MediaRecorder",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "getMaxAmplitude",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "prepare",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "setAudioEncoder",
             args = {int.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "setAudioSource",
             args = {int.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "setOutputFile",
             args = {String.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "setOutputFormat",
             args = {int.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "start",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "stop",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "setMaxFileSize",
             args = {long.class}
         )
     })
     public void testRecorderAudio() throws Exception {
         mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
         assertEquals(0, mMediaRecorder.getMaxAmplitude());
         mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
         mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
         recordMedia(MAX_FILE_SIZE);
     }
 
     @TestTargets({
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "MediaRecorder",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "prepare",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "setAudioEncoder",
             args = {int.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "setAudioSource",
             args = {int.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "setOutputFormat",
             args = {int.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "start",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "setMaxDuration",
             args = {int.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "setOnInfoListener",
             args = {OnInfoListener.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "setOnErrorListener",
             args = {OnErrorListener.class}
         )
     })
     public void testOnInfoListener() throws Exception {
         mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
         mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
         mMediaRecorder.setMaxDuration(MAX_DURATION_MSEC);
         mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
         mMediaRecorder.prepare();
         mMediaRecorder.start();
         Thread.sleep(RECORD_TIME);
         assertTrue(mOnInfoCalled);
     }
 
     @TestTargets({
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "MediaRecorder",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "prepare",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "setAudioEncoder",
             args = {int.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "setAudioSource",
             args = {int.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "setOutputFormat",
             args = {int.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "start",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "stop",
             args = {}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "setOnErrorListener",
             args = {OnErrorListener.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             method = "setMaxFileSize",
             args = {long.class}
         )
     })
     public void testOnErrorListener() throws Exception {
         mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
         mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
         mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
 
         recordMedia(MAX_FILE_SIZE);
         // TODO: how can we trigger a recording error?
         assertFalse(mOnErrorCalled);
     }
 
     private void recordMedia(long maxFileSize) throws Exception {
         mMediaRecorder.setMaxFileSize(maxFileSize);
         mMediaRecorder.prepare();
         mMediaRecorder.start();
         Thread.sleep(RECORD_TIME);
         mMediaRecorder.stop();
         assertTrue(mOutFile.exists());
        assertTrue(mOutFile.length() < maxFileSize);
         assertTrue(mOutFile.length() > 0);
     }
 
 }
