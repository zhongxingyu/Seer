 package com.gris.ege.activity;
 
 import com.gris.ege.R;
 import com.gris.ege.lists.TasksListAdapter;
 
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.ListView;
 import android.app.Activity;
 import android.content.SharedPreferences;
 
 public class ViewTasksActivity extends Activity
 {
     private static final String TAG="ViewTasksActivity";
 
     private ListView mTasksList;
     private TasksListAdapter mTasksAdapter;
 
     private String mSelectedLesson;
 
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.activity_view_tasks);
 
         mTasksList=(ListView)findViewById(R.id.tasksListView);
 
 
 
         SharedPreferences aSettings = getSharedPreferences(MainActivity.PREFS_NAME, 0);
        mSelectedLesson = aSettings.getString("selectedLesson", "");
 
         Log.v(TAG, "View tasks for lesson \""+mSelectedLesson+"\"");
 
 
 
         mTasksAdapter = new TasksListAdapter(this,
                                              R.layout.task_list_item,
                                              null,
                                              new String[] {},
                                              new int[] {},
                                              mSelectedLesson);
 
         mTasksList.setAdapter(mTasksAdapter);
         mTasksAdapter.takeTaskCursor();
     }
 }
