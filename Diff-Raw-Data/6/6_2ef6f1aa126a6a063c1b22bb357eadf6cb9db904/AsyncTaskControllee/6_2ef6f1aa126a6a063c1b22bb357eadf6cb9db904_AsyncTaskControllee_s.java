 package com.fizzbuzz.android.util;
 
 /*
  * This interface represents the AsyncTaskHelper side of the contract between an AsyncTaskManager and an AsyncTaskHelper
  */
 public interface AsyncTaskControllee {
 
     /**
      * Assigns an AsyncTaskController to an AsyncTaskControllee
      * 
     * @param manager
      */
     public void setController(AsyncTaskController controller);
 
     /**
      * Assigns an ProgressListener to an AsyncTaskHelper
      * 
      * @param listener
      */
     public void setProgressListener(ProgressListener listener);
 
     public ProgressListener getProgressListener();
 
     /**
      * Indicates whether the background job of an AsyncTaskHelper is currently executing
      * 
     * @param task the AsyncTaskHelper to be checked
      * @return <code>true</code> if the task is currently executing, <code>false</code> otherwise.
      */
     public boolean isRunning();
 
     /**
      * Cancels an AsyncTaskHelper's execution
      * 
     * @param task the AsyncTaskHelper to be canceled
      */
     public void cancelTask();
 
     public void onUiPause();
 
     public void onUiResume();
 
 }
