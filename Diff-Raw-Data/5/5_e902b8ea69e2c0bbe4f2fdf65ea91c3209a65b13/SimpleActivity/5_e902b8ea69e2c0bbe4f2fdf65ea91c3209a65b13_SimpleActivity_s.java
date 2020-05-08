 package com.lge.app.qslide.example;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.*;
 import com.lge.app.floating.FloatableActivity;
 import com.lge.app.floating.FloatingWindow;
 
 /**
  * This class represents a ordinary activity with several buttons, dialogs, edit
  * texts. It also shows how to customize the look & feel and behavior of a
  * floating window.
  */
 public class SimpleActivity extends FloatableActivity implements OnClickListener {
     private final static int CUSTOM_DIALOG = 1;
     private final static int CONFIRM_SWITCH_DIALOG = 2;
     private final static int CONFIRM_CLOSE_DIALOG = 3;
     private Button mShowDialogButton;
     private Button mStartActivityButton;
     private Button mHideImeButton;
 
     private OptionsItemHandler mOptionsItemHandler;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.simple);
 
         mShowDialogButton = (Button)findViewById(R.id.showDialogButton);
         mShowDialogButton.setOnClickListener(this);
 
         mStartActivityButton = (Button)findViewById(R.id.startActivityButton);
         mStartActivityButton.setOnClickListener(this);
 
         mHideImeButton = (Button)findViewById(R.id.hideImeButton);
         mHideImeButton.setOnClickListener(this);
 
         mOptionsItemHandler = new OptionsItemHandler(this);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // handle the options menu item.
         return mOptionsItemHandler.handleOptionsItem(item);
     }
 
     @SuppressWarnings("deprecation")
     @Override
     public void onClick(View v) {
         if (v == mShowDialogButton) {
             // Dialog can be opened when the activity is in the floating mode.
             showDialog(CUSTOM_DIALOG);
         } else if (v == mStartActivityButton) {
             // The new activity will be always shown as a full screen beneath the current
             // floating window: NOT on top of the floating window.
             // If you want to change the UI of this activity, use Fragment class.
             // Please refer to the FragmentActivity class in this demo.
             Intent i = new Intent();
             i.setClass(getApplicationContext(), WebActivity.class);
             SimpleActivity.this.startActivityForResult(i, 33);
         } else if (v == mHideImeButton) {
             // This is just to test what happens when IME becomes hidden.
             InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
             imm.hideSoftInputFromWindow(v.getRootView().getWindowToken(), 0);
         }
     }
 
     @Override
     protected void onPause() {
         Toast.makeText(this, "on pause", Toast.LENGTH_SHORT).show();
     }
 
     @Override
     protected void onStop() {
         Toast.makeText(this, "on stop", Toast.LENGTH_SHORT).show();
     }
 
     @Override
     protected void onDestroy() {
         Toast.makeText(this, "on destroy", Toast.LENGTH_SHORT).show();
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         // In order to be notified of the activity result while in floating mode,
         // the activity should not be finished. Therefore, setDontFinishAtFloatingMode(true)
         // should be invoked before switching into the floating mode.
         Toast.makeText(this, "requestCode=" + requestCode, Toast.LENGTH_SHORT).show();
         super.onActivityResult(requestCode, resultCode, data);
     }
 
     @Override
     protected Dialog onCreateDialog(int id, Bundle args) {
         Context dialogContext = getApplicationContext();
         if (id == CUSTOM_DIALOG) {
             // This is to show how to specify a custom theme for a dialog
             int themeId = getResources().getIdentifier("Theme.LGE.Default.Dialog", "style", "com.lge.internal");
             if (themeId == 0) {
                 themeId = android.R.style.Theme_Holo_Dialog;
             }
             Dialog d = new Dialog(dialogContext, themeId);
             View view = getLayoutInflater().inflate(R.layout.dialog, null);
             ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(500, 400);
             d.addContentView(view, lp);
             return d;
         } else if (id == CONFIRM_SWITCH_DIALOG) {
             return new AlertDialog.Builder(dialogContext).setMessage(R.string.confirm_switch_to_full)
                     .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             // Switch to the full mode if user has confirmed.
                             getFloatingWindow().close(true);
                         }
                     }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
 
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             dialog.dismiss();
                         }
                     }).create();
         } else if (id == CONFIRM_CLOSE_DIALOG) {
             return new AlertDialog.Builder(dialogContext).setMessage(R.string.confirm_close)
                     .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             // Close the floating window if user has confirmed.
                             getFloatingWindow().close();
                         }
                     }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
 
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             dialog.dismiss();
                         }
                     }).create();
         } else {
             return null;
         }
     }
 
     @Override
     protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
         // When a dialog is about to be shown while in floating mode,
         // its window type should be set above TYPE_PHONE, which is the window type
         // that the floating window uses. Unless, the dialog will shown
         // BENEATH the floating window.
         if (dialog != null) {
             final int type = dialog.getWindow().getAttributes().type;
             final int newType = isInFloatingMode() ? WindowManager.LayoutParams.TYPE_PHONE
                     : WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
             if (type != newType) {
                 dialog.getWindow().setType(newType);
             }
         }
     }
 
     @Override
     public void onAttachedToFloatingWindow(FloatingWindow w) {
         // hide the action bar in floating mode, so that user cannot press the same menu item
         // in floating mode, which is unnecessary.
         getActionBar().hide();
 
         // You can be notified when user presses the switch to full screen button
         // or close button. By returning false in the callback methods, you can
         // cancel the normal operation and perform your own task such as
         // opening a confirmation dialog as shown below.
         w.setOnUpdateListener(new FloatingWindow.DefaultOnUpdateListener() {
             @SuppressWarnings("deprecation")
             @Override
             public boolean onSwitchFullRequested(FloatingWindow window) {
                 showDialog(CONFIRM_SWITCH_DIALOG);
                 return false;
             }
 
             @SuppressWarnings("deprecation")
             @Override
             public boolean onCloseRequested(FloatingWindow window) {
                 showDialog(CONFIRM_CLOSE_DIALOG);
                 return false;
             }
         });
     }
 
     @Override
     public boolean onDetachedFromFloatingWindow(FloatingWindow w, boolean isReturningToFullScreen) {
         // when returning from full screen, un-hide the action bar
         if (isReturningToFullScreen) {
             getActionBar().show();
         }
         return super.onDetachedFromFloatingWindow(w, isReturningToFullScreen);
     }
 }
