 package com.github.sgdesmet.android.utils.dialogs;
 
 import android.R;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.view.View;
 
 /**
  * TODO description
  * <p/>
  * Date: 28/09/12
  * Time: 15:20
  *
  * @author: sgdesmet
  */
 public class DefaultDialogs implements IDefaultDialogs {
 
     private static final String YESNO = "yesno";
     private static final String OKAY = "okay";
     BaseDialogFragment dialogFragment;
     Context applicationContext;
 
     private static final String TAG = DefaultDialogs.class.getSimpleName();
 
     private static class SingletonHolder{
         public static final DefaultDialogs INSTANCE= new DefaultDialogs();
     }
 
     //TODO different implementations based on sherlock/support/normal
     public static DefaultDialogs getInstance(){
 
         return SingletonHolder.INSTANCE;
     }
 
     protected DefaultDialogs(){
 
     }
 
     public static void init(Context context){
         getInstance().applicationContext = context;
     }
 
     @Override
     public  void showProgressDialog(int resource, boolean cancelable, FragmentManager fm) {
         dismiss();
         if (dialogFragment == null && fm != null ){
             dialogFragment = new ProgressDialogFragment(resource, cancelable);
             dialogFragment.show(fm, "progress_dialog");
         }
     }
 
     @Override
     public  void showProgressDialog(int resource, FragmentManager fm) {
         showProgressDialog(resource, false, fm);
     }
 
     @Override
     public void showErrorDialog(int message, FragmentManager fm) {
         showErrorDialog(message, fm, false);
     }
 
     @Override
     public void showTerminatingErrorDialog(int message, FragmentManager fm) {
         showErrorDialog(message, fm, true);
     }
 
     @Override
     public void showErrorDialog(int message, FragmentManager fm, boolean terminateApp) {
 
         dismiss();
         if (dialogFragment == null && fm != null ){
             dialogFragment = new AlertDialogFragment(message, terminateApp);
             dialogFragment.show(fm, "fatal_error_dialog");
         }
     }
 
 
     @Override
     public void dismiss(){
         if (dialogFragment != null){
             try {
                 dialogFragment.dismiss();
             }catch (Exception ignored){
                 //catch can't commit after onsaveinstance exceptions. Bit of a hack but oh well
                 Log.e(TAG, "Error while attempting to dismiss dialog: " + ignored);
             } finally {
                 dialogFragment = null;
             }
         }
     }
 
     @Override
     public void showOneButtonDialog(final int titleResource, final int messageResource,
                                     final int buttonResourceText,
                                     final DialogInterface.OnClickListener buttonListener,
                                     FragmentManager fm) {
         dismiss();
         if (dialogFragment == null && fm != null ){
             dialogFragment = new OneButtonDialog(applicationContext.getString(titleResource),
                     applicationContext.getString(messageResource),
                     applicationContext.getString(buttonResourceText), buttonListener);
             dialogFragment.show(fm, OKAY);
         }
     }
 
     @Override
     public void showOneButtonDialog(final String titleResource, final String messageResource,
                                     final String buttonResourceText,
                                     final DialogInterface.OnClickListener buttonListener,
                                     FragmentManager fm) {
         dismiss();
         if (dialogFragment == null && fm != null ){
             dialogFragment = new OneButtonDialog(titleResource, messageResource, buttonResourceText, buttonListener);
             dialogFragment.show(fm, OKAY);
 
         }
     }
 
     @Override
     public void showTwoButtonDialog(final int titleResource, final int messageResource,
                                     final int yesResourceText, final int noResourceText,
                                     final DialogInterface.OnClickListener yesListener, final DialogInterface.OnClickListener noListener,
                                     FragmentManager fm) {
         dismiss();
         if (dialogFragment == null && fm != null ){
             dialogFragment = new TwoButtonDialog(applicationContext.getString(titleResource),
                     applicationContext.getString(messageResource), applicationContext.getString(yesResourceText),
                     yesListener, applicationContext.getString(noResourceText), noListener);
             dialogFragment.show(fm, YESNO);
         }
     }
 
     @Override
     public void showTwoButtonDialog(final String title, final String message,
                                     final String yesText, final String noText,
                                     final DialogInterface.OnClickListener yesListener, final DialogInterface.OnClickListener noListener,
                                     FragmentManager fm) {
         dismiss();
         if (dialogFragment == null && fm != null ){
             dialogFragment = new TwoButtonDialog(title,
                     message, yesText,
                     yesListener, noText, noListener);
             dialogFragment.show(fm, YESNO);
         }
     }
 
     @Override
     public void showCustomOneButtonDialog(final int titleResource, final View contentView,
                                           final int yesResourceText,
                                           final DialogInterface.OnClickListener yesListener,
                                           FragmentManager fm) {
         dismiss();
         if (dialogFragment == null && fm != null ){
             dialogFragment = new OneButtonDialog(applicationContext.getString(titleResource), contentView,
                     applicationContext.getString(yesResourceText), yesListener);
             dialogFragment.show(fm, OKAY);
         }
     }
 
     @Override
     public void showCustomTwoButtonDialog(final int titleResource, final View contentView,
                                           final int yesResourceText, final int noResourceText,
                                           final DialogInterface.OnClickListener yesListener, final DialogInterface.OnClickListener noListener,
                                           FragmentManager fm) {
         dismiss();
         if (dialogFragment == null && fm != null ){
             dialogFragment = new TwoButtonDialog(applicationContext.getString(titleResource), contentView,
                     applicationContext.getString(yesResourceText), yesListener,
                     applicationContext.getString(noResourceText), noListener);
             dialogFragment.show(fm, YESNO);
         }
     }
 
 
     /**
      * Shows an alert dialog
      * <p/>
      * Date: 10/08/12
      * Time: 10:37
      *
      * @author: sgdesmet
      */
     public static class AlertDialogFragment extends BaseDialogFragment {
 
         private int messageResource;
 
         private boolean terminateApp;
 
         public AlertDialogFragment(int message) {
             this.messageResource = message;
             terminateApp = false;
         }
 
         public AlertDialogFragment(int message, boolean terminateApp) {
             this.messageResource = message;
             this.terminateApp = terminateApp;
         }
 
         @Override
         public Dialog onCreateDialog(Bundle savedInstanceState) {
 
             setRetainInstance(true);
 
             AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
             builder.setCancelable(true).setMessage(messageResource);
             builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialogInterface, int i) {
                     dismiss();
                     if (terminateApp && getActivity() != null)
                         getActivity().finish();
                 }
             });
             setCancelable(true);
             return builder.create();
         }
 
         @Override
         public void onDestroyView() {
             // workaround for issue http://code.google.com/p/android/issues/detail?id=17423 (dialogfragment gets dismissed
             // on orientation change)
             if (getDialog() != null && getRetainInstance())
                 getDialog().setDismissMessage(null);
             super.onDestroyView();
         }
 
     }
 
     /**
      * Shows a loading message
      * <p/>
      * Date: 10/08/12
      * Time: 10:37
      *
      * @author: sgdesmet
      */
     public static class ProgressDialogFragment extends BaseDialogFragment {
 
         private static final String TITLE = "title";
         private static final String CANCELABLE = "cancelable";
         private int titleResource;
         private boolean cancelable;
 
         public ProgressDialogFragment() {
             titleResource = -1;
             cancelable = true;
         }
 
         public ProgressDialogFragment(int title, boolean cancelable) {
             this.titleResource = title;
             this.cancelable = cancelable;
         }
 
         @Override
         public void onCreate(Bundle savedInstanceState) {
             super.onCreate(savedInstanceState);
             setRetainInstance(true);
             if (savedInstanceState != null){
                 titleResource = savedInstanceState.getInt(TITLE);
                 cancelable = savedInstanceState.getBoolean(CANCELABLE);
             }
         }
 
         @Override
         public void onSaveInstanceState(Bundle outState) {
             super.onSaveInstanceState(outState);    //TODO implement
 
             outState.putInt(TITLE, titleResource);
             outState.putBoolean(CANCELABLE, cancelable);
         }
 
         @Override
         public Dialog onCreateDialog(Bundle savedInstanceState) {
             ProgressDialog dialog = new ProgressDialog(getActivity());
             dialog.setMessage(getString(titleResource, true));
             dialog.setIndeterminate(true);
             dialog.setCancelable(cancelable);
             setCancelable(cancelable);
             return dialog;
         }
 
         @Override
         public void onDestroyView() {
             // workaround for issue http://code.google.com/p/android/issues/detail?id=17423 (dialogfragment gets dismissed
             // on orientation change)
             if (getDialog() != null && getRetainInstance())
                 getDialog().setDismissMessage(null);
             super.onDestroyView();
         }
 
     }
 
    protected static class OneButtonDialog extends BaseDialogFragment{
 
         private String title;
         private String message;
         private String neutralButton;
         private DialogInterface.OnClickListener neutralListener;
         private View customView;
         private boolean cancelable = false;
 
         public OneButtonDialog() {
         }
 
         public OneButtonDialog(String title, String message, String neutralButton, DialogInterface.OnClickListener neutralListener) {
             this.title = title;
             this.message = message;
             this.neutralButton = neutralButton;
             this.neutralListener = neutralListener;
         }
 
         public OneButtonDialog(String title, View customView, String neutralButton, DialogInterface.OnClickListener neutralListener) {
             this.title = title;
             this.customView = customView;
             this.neutralButton = neutralButton;
             this.neutralListener = neutralListener;
         }
 
         @Override
         public void onCreate(Bundle savedInstanceState) {
             super.onCreate(savedInstanceState);
             setRetainInstance(true);
         }
 
         @Override
         public Dialog onCreateDialog(Bundle savedInstanceState) {
 
             if (neutralListener != null){
                 setCancelable(cancelable);
                 AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                 builder.setCancelable(cancelable)
                         .setTitle(title)
                         .setMessage(message)
                         .setNeutralButton(neutralButton, neutralListener);
 
                 if (customView != null)
                     builder.setView(customView);
 
                 return builder.create();
             }
             return super.onCreateDialog(savedInstanceState);
         }
     }
 
    protected static class TwoButtonDialog extends BaseDialogFragment{
 
         private String title;
         private String message;
         private String yesButton;
         private DialogInterface.OnClickListener yesListener;
         private String noButton;
         private DialogInterface.OnClickListener noListener;
         private View customView;
         private boolean cancelable = false;
 
         public TwoButtonDialog() {
         }
 
         public TwoButtonDialog(String title, String message, String yesButton, DialogInterface.OnClickListener yesListener,
                                String noButton, DialogInterface.OnClickListener noListener) {
             this.title = title;
             this.message = message;
             this.yesButton = yesButton;
             this.yesListener = yesListener;
             this.noButton = noButton;
             this.noListener = noListener;
         }
 
         public TwoButtonDialog(String title, View customView, String yesButton, DialogInterface.OnClickListener yesListener,
                                String noButton, DialogInterface.OnClickListener noListener) {
             this.title = title;
             this.customView = customView;
             this.yesButton = yesButton;
             this.yesListener = yesListener;
             this.noButton = noButton;
             this.noListener = noListener;
         }
 
         @Override
         public void onCreate(Bundle savedInstanceState) {
             super.onCreate(savedInstanceState);
             setRetainInstance(true);
         }
 
         @Override
         public Dialog onCreateDialog(Bundle savedInstanceState) {
 
             if (yesListener != null && noListener != null){
                 setCancelable(cancelable);
                 AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                 builder.setCancelable(cancelable)
                         .setTitle(title)
                         .setMessage(message)
                         .setPositiveButton(yesButton, yesListener)
                         .setNegativeButton(noButton, noListener);
 
                if (customView != null)
                     builder.setView(customView);
 
                 return builder.create();
             }
             return super.onCreateDialog(savedInstanceState);
         }
     }
 
     protected static class BaseDialogFragment extends DialogFragment {
 
         @Override
         public void show(FragmentManager manager, String tag) {
             try {
                 //nasty hack for http://code.google.com/p/android/issues/detail?id=23096 :/
                 super.show(manager, tag);
             } catch (IllegalStateException e){
                 Log.w(TAG, "Ignoring IllegalStateException...");
             }
         }
 
         @Override
         public int show(FragmentTransaction transaction, String tag) {
             try {
                 //nasty hack for http://code.google.com/p/android/issues/detail?id=23096 :/
                 return super.show(transaction, tag);
             } catch (IllegalStateException e){
                 Log.w(TAG, "Ignoring IllegalStateException...");
                 return  -1;
             }
         }
     }
 }
