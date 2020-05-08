 package edu.vanderbilt.cs282.feisele;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 /**
  * An abstract activity which logs the life-cycle call backs.
  * A decorator pattern implemented via inheritance.
  */
 public abstract class LifecycleLoggingFragment extends Fragment {
     static private final String TAG = "Lifecycle Logging Fragment";
 
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
         Log.d(TAG, "onAttach: fragment attached "+activity.toString());
     }
     
     @Override
     public void onDetach() {
         super.onDetach();
         Log.d(TAG, "onDetach: fragment detach");
     }
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.d(TAG, "onCreate: fragment rebuilt");
         if (savedInstanceState == null) {
             Log.d(TAG, "onCreate: fragment created fresh");
         } else {
             Log.d(TAG, "onCreate: fragment restarted");
         }
     }
     
    /**
 * Note the isDynamic parameter which allows this call 
 * whether the fragment is dynamic or static.
 */
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState, boolean isDynamic) {
         Log.d(TAG, "onCreateView: fragment rebuilt");
         if (savedInstanceState == null) {
             Log.d(TAG, "onCreateView: fragment created fresh");
         } else {
             Log.d(TAG, "onCreateView: fragment restarted");
         }
         /**
         * The android documentation suggests that this call should
         * not be made when the fragment is dynamically added
         */
         if (!(isDynamic)) {
              super.onCreateView(inflater, container, savedInstanceState);
         }
         return null;
     }
     
     @Override
     public void onDestroyView() {
         super.onDestroyView();
         Log.d(TAG, "onDestroyView: fragment view destroyed");
     }
 
     @Override
     public void onStart() {
         super.onStart();
         Log.d(TAG, "onStart");
     }
 
     @Override
     public void onResume() {
         super.onResume();
         Log.d(TAG, "onResume");
     }
 
     @Override
     public void onSaveInstanceState(Bundle savedInstanceState) {
         super.onSaveInstanceState(savedInstanceState);
         Log.d(TAG, "onSaveInstanceState");
     }
 
     @Override
     public void onPause() {
         super.onPause();
         Log.d(TAG, "onPause");
     }
 
     @Override
     public void onStop() {
         super.onStop();
         Log.d(TAG, "onStop");
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         Log.d(TAG, "onDestroy");
     }
     
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
         Log.d(TAG, "onActivityCreated: fragment activity created ");
     }
     
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         Log.d(TAG, new StringBuilder("onActivityResult ").
                 append(" request=").append(requestCode).
                 append(" result=").append(resultCode).
                 append(" intent=[").append(data).append("]").
                 toString());
     }
 
 }
