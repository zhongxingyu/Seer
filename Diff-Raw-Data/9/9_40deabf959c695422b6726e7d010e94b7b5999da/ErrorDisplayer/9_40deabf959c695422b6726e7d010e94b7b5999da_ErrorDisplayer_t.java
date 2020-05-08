 /*
  ** Licensed under the Apache License, Version 2.0 (the "License");
  ** you may not use this file except in compliance with the License.
  ** You may obtain a copy of the License at
  **
  **     http://www.apache.org/licenses/LICENSE-2.0
  **
  ** Unless required by applicable law or agreed to in writing, software
  ** distributed under the License is distributed on an "AS IS" BASIS,
  ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ** See the License for the specific language governing permissions and
  ** limitations under the License.
  */
 
 package com.google.code.geobeagle.ui;
 
 import com.google.code.geobeagle.Util;
 
 import android.app.Activity;
 import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
 
 // TODO: this class needs tests.
 public class ErrorDisplayer {
     private final Activity mActivity;
     private Builder mAlertDialogBuilder;
 
     public ErrorDisplayer(Activity activity) {
         mActivity = activity;
     }
 
     private class DisplayErrorRunnable implements Runnable {
         private DisplayErrorRunnable() {
         }
 
         public void run() {
             mAlertDialogBuilder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int which) {
                 }
             });
             mAlertDialogBuilder.create().show();
         }
     }
 
     public void displayError(int resourceId) {
         mAlertDialogBuilder = new Builder(mActivity);
         mAlertDialogBuilder.setMessage(resourceId);
         mActivity.runOnUiThread(new DisplayErrorRunnable());
     }
 
     public void displayError(String string) {
         mAlertDialogBuilder = new Builder(mActivity);
         mAlertDialogBuilder.setMessage(string);
         mActivity.runOnUiThread(new DisplayErrorRunnable());
     }
 
     public void displayErrorAndStack(Exception e) {
         displayErrorAndStack("", e);
     }
 
     public void displayErrorAndStack(String msg, Exception e) {
         mAlertDialogBuilder = new Builder(mActivity);
         mAlertDialogBuilder.setMessage(("Error " + msg + ":" + e.toString() + "\n\n" + Util
                 .getStackTrace(e)));
         mActivity.runOnUiThread(new DisplayErrorRunnable());
     }
 
     public void displayError(int resId, Object... args) {
        displayError(String.format((String)mActivity.getText(resId), args));
     }
 }
