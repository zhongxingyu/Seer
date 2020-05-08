 package com.siu.android.demos.activity;
 
 import android.os.Bundle;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Window;
 import com.siu.android.demos.R;
 import com.siu.android.demos.adapter.DemoAdapter;
 import com.siu.android.demos.model.DemoModel;
 import com.siu.android.demos.task.DemoTask;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class DemoActivity extends SherlockActivity {
 
     private ListView listView;
     private ArrayAdapter<DemoModel> listAdapter;
 
     private List<DemoModel> demoModels;
     private DemoTask demoTask;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         // request indeterminate progress before setContentView
         requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 
         // progress is shown by default, so disable it
         setSupportProgressBarIndeterminateVisibility(false);
 
         setContentView(R.layout.main);
         listView = (ListView) findViewById(R.id.list);
 
         demoTask = (DemoTask) getLastNonConfigurationInstance();
 
         if (null == demoTask) {
             demoModels = new ArrayList<DemoModel>();
         } else {
             demoModels = (List<DemoModel>) savedInstanceState.get("list");
             demoTask.setActivity(this);
         }
 
         initList();
     }
 
     @Override
     protected void onStart() {
         super.onStart();
 
         if (demoModels.isEmpty() && null == demoTask) {
             startDemoTask();
         }
     }
 
     private void initList() {
         listAdapter = new DemoAdapter(this, demoModels);
         listView.setAdapter(listAdapter);
     }
 
 
     /* Rotation handling */
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putSerializable("list", (ArrayList<DemoModel>) demoModels);
     }
 
     @Override
     public Object getLastNonConfigurationInstance() {
 
         if (null == demoTask) {
             return null;
         }
 
         // if demo task is running
         // remove old activity that will be destroyed
         demoTask.setActivity(null);
 
         // and return the demotask as a saved object during rotation
         return demoTask;
     }
 
 
     /* Demo task */
 
     private void startDemoTask() {
         stopDemoTaskIfRunning();
 
         setSupportProgressBarIndeterminateVisibility(true);
         demoTask = new DemoTask(this);
         demoTask.execute();
     }
 
     private void stopDemoTaskIfRunning() {
         if (null == demoTask) {
             return;
         }
 
         if (!demoTask.isCancelled()) {
             demoTask.cancel(true);
         }
 
         setSupportProgressBarIndeterminateVisibility(false);
         demoTask = null;
     }
 
     public void onDemoTaskFinished(List<DemoModel> demoModelLoaded) {
         stopDemoTaskIfRunning();
 
        if (null == demoModelLoaded || demoModelLoaded.isEmpty()) {
            if (demoModels.isEmpty()) {
                Toast.makeText(this, "Impossible de récupérer les informations du flux", Toast.LENGTH_LONG).show();
            }
             return;
         }
 
         // clear previous content from list
         demoModels.clear();
 
         // and add the new loaded content
         demoModels.addAll(demoModelLoaded);
 
         // notifiy list adapter that content changed
         listAdapter.notifyDataSetChanged();
     }
 
     public void onDemoTaskProgress(List<DemoModel> demoModelsLoaded) {
         if (null == demoModelsLoaded || demoModelsLoaded.isEmpty()) {
             return;
         }
 
         // clear previous content from list
         demoModels.clear();
 
         // and add the new loaded content
         demoModels.addAll(demoModelsLoaded);
 
         // notifiy list adapter that content changed
         listAdapter.notifyDataSetChanged();
     }
 }
