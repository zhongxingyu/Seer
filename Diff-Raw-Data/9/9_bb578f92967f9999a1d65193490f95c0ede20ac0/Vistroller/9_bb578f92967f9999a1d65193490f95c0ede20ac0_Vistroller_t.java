 /*
  * Copyright (C) 2011 Ron Huang
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package org.ronhuang.vistroller;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.Map;
 import java.util.HashMap;
 import java.lang.reflect.Field;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.os.SystemClock;
 import android.view.IWindowManager;
 import android.os.ServiceManager;
 import android.content.res.Resources;
 import android.content.res.TypedArray;
 
 import com.qualcomm.QCAR.QCAR;
 
 
 public class Vistroller
 {
     private Activity mActivity;
     private List<VistrollerListener> mListeners;
 
     // Application status constants:
     private static final int STATUS_UNINITED         = -1;
     private static final int STATUS_INIT_ENGINE      = 1;
     private static final int STATUS_INIT_TRACKER     = 2;
     private static final int STATUS_INITED           = 3;
     private static final int STATUS_CAMERA_STOPPED   = 4;
     private static final int STATUS_CAMERA_RUNNING   = 5;
 
     // Name of the native dynamic libraries to load:
     private static final String NATIVE_LIB_VISTROLLER = "vistroller";
     private static final String NATIVE_LIB_QCAR = "QCAR";
 
     // The current application status
     private int mStatus = STATUS_UNINITED;
 
     // The async tasks to initialize the QCAR SDK
     private InitQCARTask mInitQCARTask;
     private LoadTrackerTask mLoadTrackerTask;
     private TrackingTask mTrackingTask;
 
     // QCAR initialization flags
     private int mQCARFlags = 0;
 
     // Log tag
     private static final String TAG = "Vistroller";
 
     /** Static initializer block to load native libraries on start-up. */
     static
     {
         loadLibrary(NATIVE_LIB_QCAR);
         loadLibrary(NATIVE_LIB_VISTROLLER);
     }
 
 
     /** Exposed states. */
     public enum State {
         ENGINE_INITIALIZED,
         TRACKER_INITIALIZED,
         SYSTEM_INITIALIZED
     }
 
 
     /** Constructor */
     public Vistroller(Activity nActivity)
     {
         mActivity = nActivity;
 
         // Query the QCAR initialization flags:
         mQCARFlags = QCAR.GL_20;
 
         mListeners = new ArrayList<VistrollerListener>();
     }
 
 
     /** Get flags. */
     public int getFlags()
     {
         return mQCARFlags;
     }
 
 
     /** Add listener to various events. */
     public void addListener(VistrollerListener listener)
     {
         mListeners.add(listener);
     }
 
 
     /** Trigger state changed event to listeners. */
     private void triggerStateChanged(State state)
     {
         for (int i = 0; i < mListeners.size(); i++) {
             mListeners.get(i).onVistrollerStateChanged(state);
         }
     }
 
 
     /** Request starting camera. */
     public void start()
     {
         Log.d(TAG, "Vistroller::start");
 
         updateApplicationStatus(STATUS_CAMERA_RUNNING);
     }
 
 
     /** Request starting camera. */
     public void stop()
     {
         Log.d(TAG, "Vistroller::stop");
 
         updateApplicationStatus(STATUS_CAMERA_STOPPED);
     }
 
 
     /** An async task to initialize QCAR asynchronously. */
     private class InitQCARTask extends AsyncTask<Void, Integer, Boolean>
     {
         // Initialize with invalid value
         private int mProgressValue = -1;
 
         protected Boolean doInBackground(Void... params)
         {
             QCAR.setInitParameters(mActivity, mQCARFlags);
 
             do
             {
                 // QCAR.init() blocks until an initialization step is complete,
                 // then it proceeds to the next step and reports progress in
                 // percents (0 ... 100%)
                 // If QCAR.init() returns -1, it indicates an error.
                 // Initialization is done when progress has reached 100%.
                 mProgressValue = QCAR.init();
 
                 // Publish the progress value:
                 publishProgress(mProgressValue);
 
                 // We check whether the task has been canceled in the meantime
                 // (by calling AsyncTask.cancel(true))
                 // and bail out if it has, thus stopping this thread.
                 // This is necessary as the AsyncTask will run to completion
                 // regardless of the status of the component that started is.
             } while (!isCancelled() && mProgressValue >= 0 && mProgressValue < 100);
 
             return (mProgressValue > 0);
         }
 
 
         protected void onProgressUpdate(Integer... values)
         {
             Log.d(TAG, "InitQCARTask::onProgressUpdate " + values[0].intValue());
 
             // Do something with the progress value "values[0]", e.g. update
             // splash screen, progress bar, etc.
             for (int i = 0; i < mListeners.size(); i++) {
                 mListeners.get(i).onVistrollerProgressUpdate(values[0].intValue() / 2);
             }
         }
 
 
         protected void onPostExecute(Boolean result)
         {
             // Done initializing QCAR, proceed to next application
             // initialization status:
             if (result)
             {
                 Log.d(TAG, "InitQCARTask::onPostExecute: QCAR initialization successful");
 
                 // Inform listeners.
                 triggerStateChanged(State.ENGINE_INITIALIZED);
 
                 // Done initialization
                 updateApplicationStatus(STATUS_INIT_TRACKER);
             }
             else
             {
             	// Create dialog box for display error:
             	AlertDialog dialogError = new AlertDialog.Builder(mActivity).create();
             	dialogError.setButton(
             		"Close",
             		new DialogInterface.OnClickListener()
             		{
             			public void onClick(DialogInterface dialog, int which)
             			{
             				// Exiting application
                             System.exit(1);
             			}
             		}
             	);
 
             	String logMessage;
 
                 // NOTE: Check if initialization failed because the device is
                 // not supported. At this point the user should be informed
                 // with a message.
                 if (mProgressValue == QCAR.INIT_DEVICE_NOT_SUPPORTED)
                 {
                 	logMessage = "Failed to initialize QCAR because this " +
                 	    "device is not supported.";
                 }
                 else if (mProgressValue ==
                             QCAR.INIT_CANNOT_DOWNLOAD_DEVICE_SETTINGS)
                 {
                 	logMessage =
                         "Network connection required to initialize camera " +
                         "settings. Please check your connection and restart " +
                         "the application. If you are still experiencing " +
                         "problems, then your device may not be currently " +
                         "supported.";
                 }
                 else
                 {
                 	logMessage = "Failed to initialize QCAR.";
                 }
 
                 // Log error:
                 Log.e(TAG, "InitQCARTask::onPostExecute: " + logMessage + " Exiting.");
 
                 // Show dialog box with error message:
                 dialogError.setMessage(logMessage);
                 dialogError.show();
             }
         }
     }
 
 
     /** An async task to load the tracker data asynchronously. */
     private class LoadTrackerTask extends AsyncTask<Void, Integer, Boolean>
     {
         protected Boolean doInBackground(Void... params)
         {
             // Initialize with invalid value
             int progressValue = -1;
 
             do
             {
                 progressValue = QCAR.load();
                 publishProgress(progressValue);
 
             } while (!isCancelled() && progressValue >= 0 &&
                         progressValue < 100);
 
             return (progressValue > 0);
         }
 
 
         protected void onProgressUpdate(Integer... values)
         {
             Log.d(TAG, "LoadTrackerTask::onProgressUpdate " + values[0].intValue());
 
             // Do something with the progress value "values[0]", e.g. update
             // splash screen, progress bar, etc.
             for (int i = 0; i < mListeners.size(); i++) {
                 mListeners.get(i).onVistrollerProgressUpdate(50 + values[0].intValue() / 2);
             }
         }
 
 
         protected void onPostExecute(Boolean result)
         {
             Log.d(TAG, "LoadTrackerTask::onPostExecute: execution " + (result ? "successful" : "failed"));
 
             triggerStateChanged(State.TRACKER_INITIALIZED);
 
             // Done loading the tracker, update application status:
             updateApplicationStatus(STATUS_INITED);
         }
     }
 
 
     /** Native methods for retrieving trackable. */
     private native void startTracking();
     private native void stopTracking();
     private native Marker getMarker();
 
 
     /** An async task to track and post trackables to UI thread. */
     private class TrackingTask extends AsyncTask<Void, KeyEvent, Boolean> {
         private int mKeyDownCount = 0;
         private short mPreviousId = -1;
         private Map<Short, Integer> mIdToCodeMap = null;
         private IWindowManager mWindowManager = null;
 
         // sleep time
         private final long kSleep = 50;
 
 
         public TrackingTask() {
             // Load from resource
             mIdToCodeMap = loadMapFromResource(R.array.vistroller_mapping);
 
             mWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
         }
 
 
         protected void onPreExecute() {
             startTracking();
         }
 
 
         private void injectKey(short id, int action, int count) {
            Integer keycode = mIdToCodeMap.get(id);

            if (null == keycode)
                // No corresponding keycode
                return;
 
             long eventTime = SystemClock.uptimeMillis();
             long downTime = eventTime; //FIXME: should be in C?
 
             KeyEvent event = new KeyEvent(downTime,
                                           eventTime,
                                           action,
                                          keycode.intValue(),
                                           count);
             mWindowManager.injectKeyEvent(event, true);
         }
 
 
         protected Boolean doInBackground(Void... params) {
             do {
                 // Retrieve markers
                 Marker marker = getMarker();
                 short id = marker.getId();
 
                 try {
                     Thread.currentThread().sleep(kSleep);
                 } catch (InterruptedException ie) {
                     // Do nothing.
                 }
 
                 if (marker.isValid() && (mPreviousId == -1 || mPreviousId == id)) {
                     injectKey(id, KeyEvent.ACTION_DOWN, mKeyDownCount);
                     mKeyDownCount++;
                     mPreviousId = id;
                 } else if (marker.isValid() && mPreviousId != id) {
                     // inject key up if different from previous key down.
                     injectKey(mPreviousId, KeyEvent.ACTION_UP, 0);
                     mKeyDownCount = 0;
                     mPreviousId = -1;
 
                     injectKey(id, KeyEvent.ACTION_DOWN, mKeyDownCount);
                     mKeyDownCount++;
                     mPreviousId = id;
                 } else if (!marker.isValid() && mPreviousId != -1) {
                     injectKey(mPreviousId, KeyEvent.ACTION_UP, 0);
                     mKeyDownCount = 0;
                     mPreviousId = -1;
                 }
             } while (!isCancelled());
 
             return true;
         }
 
 
         protected void onPostExecute(Boolean result) {
             stopTracking();
         }
 
 
         protected void onCancelled(Boolean result) {
             stopTracking();
         }
     }
 
 
     /** Called when the activity first starts or the user navigates back
      * to an activity. */
     public void onCreate()
     {
         Log.d(TAG, "Vistroller::onCreate");
 
         // Update the application status to start initializing application
         updateApplicationStatus(STATUS_INIT_ENGINE);
     }
 
 
    /** Called when the activity will start interacting with the user.*/
     public void onResume()
     {
         Log.d(TAG, "Vistroller::onResume");
 
         // QCAR-specific resume operation
         QCAR.onResume();
 
         // We may start the camera only if the QCAR SDK has already been
         // initialized
         if (mStatus == STATUS_CAMERA_STOPPED)
             updateApplicationStatus(STATUS_CAMERA_RUNNING);
     }
 
 
     /** Called when the system is about to start resuming a previous activity.*/
     public void onPause()
     {
         Log.d(TAG, "Vistroller::onPause");
 
         // QCAR-specific pause operation
         QCAR.onPause();
 
         if (mStatus == STATUS_CAMERA_RUNNING)
             updateApplicationStatus(STATUS_CAMERA_STOPPED);
     }
 
 
     /** The final call you receive before your activity is destroyed.*/
     public void onDestroy()
     {
         Log.d(TAG, "Vistroller::onDestroy");
 
         // Cancel potentially running tasks
         if (mInitQCARTask != null &&
             mInitQCARTask.getStatus() != InitQCARTask.Status.FINISHED)
         {
             mInitQCARTask.cancel(true);
             mInitQCARTask = null;
         }
 
         if (mLoadTrackerTask != null &&
             mLoadTrackerTask.getStatus() != LoadTrackerTask.Status.FINISHED)
         {
             mLoadTrackerTask.cancel(true);
             mLoadTrackerTask = null;
         }
 
         if (mTrackingTask != null &&
             mTrackingTask.getStatus() != TrackingTask.Status.FINISHED)
         {
             mTrackingTask.cancel(true);
             mTrackingTask = null;
         }
 
         // Deinitialize QCAR SDK
         QCAR.deinit();
 
         System.gc();
     }
 
 
     /** NOTE: this method is synchronized because of a potential concurrent
      * access by Vistroller::onResume() and InitQCARTask::onPostExecute(). */
     private synchronized void updateApplicationStatus(int appStatus)
     {
         Log.d(TAG, "Vistroller::updateApplicationStatus (from " + mStatus + " to " + appStatus + ")");
 
         // Exit if there is no change in status
         if (mStatus == appStatus)
             return;
 
         // Store new status value
         mStatus = appStatus;
 
         // Execute application state-specific actions
         switch (mStatus)
         {
             case STATUS_INIT_ENGINE:
                 // Initialize QCAR SDK asynchronously to avoid blocking the
                 // main (UI) thread.
                 // This task instance must be created and invoked on the UI
                 // thread and it can be executed only once!
                 try
                 {
                     mInitQCARTask = new InitQCARTask();
                     mInitQCARTask.execute();
                 }
                 catch (Exception e)
                 {
                     Log.e(TAG, "Initializing QCAR SDK failed");
                 }
                 break;
 
             case STATUS_INIT_TRACKER:
                 // Load the tracking data set
                 //
                 // This task instance must be created and invoked on the UI
                 // thread and it can be executed only once!
                 try
                 {
                     mLoadTrackerTask = new LoadTrackerTask();
                     mLoadTrackerTask.execute();
                 }
                 catch (Exception e)
                 {
                     Log.e(TAG, "Loading tracking data set failed");
                 }
                 break;
 
             case STATUS_INITED:
                 triggerStateChanged(State.SYSTEM_INITIALIZED);
 
                 // Hint to the virtual machine that it would be a good time to
                 // run the garbage collector.
                 //
                 // NOTE: This is only a hint. There is no guarantee that the
                 // garbage collector will actually be run.
                 System.gc();
                 break;
 
             case STATUS_CAMERA_STOPPED:
                 // Cancel existing tracking task if exist
                 if (null != mTrackingTask) {
                     if (TrackingTask.Status.FINISHED != mTrackingTask.getStatus())
                         mTrackingTask.cancel(true);
                     mTrackingTask = null;
                 }
                 break;
 
             case STATUS_CAMERA_RUNNING:
                 // Cancel existing tracking task if exist
                 if (null != mTrackingTask) {
                     if (TrackingTask.Status.FINISHED != mTrackingTask.getStatus())
                         mTrackingTask.cancel(true);
                     mTrackingTask = null;
                 }
 
                 // Start tracking task.
                 try {
                     mTrackingTask = new TrackingTask();
                     mTrackingTask.execute();
                 } catch (Exception e) {
                     Log.e(TAG, "TrackingTask failed to initialize");
                 }
                 break;
 
             default:
                 throw new RuntimeException("Invalid application state");
         }
     }
 
 
     /** A helper for loading native libraries stored in "libs/armeabi*". */
     private static boolean loadLibrary(String nLibName)
     {
         try
         {
             System.loadLibrary(nLibName);
             Log.i(TAG, "Native library lib" + nLibName + ".so loaded");
             return true;
         }
         catch (UnsatisfiedLinkError ulee)
         {
             Log.e(TAG, "The library lib" + nLibName + ".so could not be loaded");
         }
         catch (SecurityException se)
         {
             Log.e(TAG, "The library lib" + nLibName + ".so was not allowed to be loaded");
         }
 
         return false;
     }
 
 
     /** Load id-to-keycode map from resource. */
     private Map<Short, Integer> loadMapFromResource(int id) {
         Resources res = mActivity.getResources();
         TypedArray mappings = res.obtainTypedArray(id);
         Class cls = null;
         Map<Short, Integer> map = new HashMap<Short, Integer>();
 
         try {
             cls = Class.forName("android.view.KeyEvent");
         } catch (ClassNotFoundException cnfe) {
             return map;
         }
 
         for (int i = 0; i < mappings.length(); i++) {
             int aid = mappings.getResourceId(i, 0);
             String[] mapping = res.getStringArray(aid);
 
             short markerId = Short.parseShort(mapping[0]);
             int keycode = 0;
 
             try {
                 Field fld = cls.getDeclaredField(mapping[1]);
                 keycode = fld.getInt(null);
             } catch (NoSuchFieldException nsfe) {
                 continue;
             } catch (IllegalAccessException iae) {
                 continue;
             }
 
             Log.d(TAG, String.format("loadMapFromResource:%d:%s:%s:%d:%d",
                                      i,
                                      mapping[0], mapping[1],
                                      markerId, keycode));
 
             map.put(markerId, keycode);
         }
 
         return map;
     }
 }
