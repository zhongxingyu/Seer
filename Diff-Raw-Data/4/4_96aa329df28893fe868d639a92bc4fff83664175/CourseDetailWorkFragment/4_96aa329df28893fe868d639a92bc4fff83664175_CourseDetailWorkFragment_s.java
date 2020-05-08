 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarp.courses;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.Fragment;
 import android.util.Base64;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.*;
 import java.util.ArrayList;
 import no.hials.muldvarp.R;
 import no.hials.muldvarp.domain.Course;
 import no.hials.muldvarp.domain.Task;
 import no.hials.muldvarp.domain.Theme;
 import no.hials.muldvarp.utility.DownloadUtilities;
 import no.hials.muldvarp.video.VideoActivity;
 
 /**
  *
  * @author kristoffer
  */
 public class CourseDetailWorkFragment extends Fragment {
     ExpandableListView listview;
     View fragmentView;
     Course c;
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setRetainInstance(true);
     }
     
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         //if(fragmentView == null) {
             fragmentView = inflater.inflate(R.layout.course_work, container, false);
             listview = (ExpandableListView)fragmentView.findViewById(R.id.listview);
         //}
         
         return fragmentView;
     }
 
     @Override
     public void onResume() {
         super.onResume();
         if(c != null) {
             ArrayList<Theme> themes = c.getThemes();
             ArrayList<ArrayList<Task>> allTasks = new ArrayList<ArrayList<Task>>();
             for(Theme theme : themes){
                 ArrayList<Task> tasks = new ArrayList<Task>();
                 for(Task task : theme.getTasks()) {
                     tasks.add(task);
                 }
                 allTasks.add(tasks);
             }
 
             TaskAdapter adapter = new TaskAdapter(fragmentView.getContext(), themes, allTasks);
             listview.setAdapter(adapter);
         }
     }
     
     public void ready(Course course) {
 //        if(c == null) {
 ////            CourseDetailActivity activity = (CourseDetailActivity)CourseDetailWorkFragment.this.getActivity();
 ////            c = activity.getActiveCourse();
 //        }
         this.c = course;
         ArrayList<Theme> themes = c.getThemes();
         ArrayList<ArrayList<Task>> allTasks = new ArrayList<ArrayList<Task>>();
         for(Theme theme : themes){
             ArrayList<Task> tasks = new ArrayList<Task>();
             for(Task task : theme.getTasks()) {
                 tasks.add(task);
             }
             allTasks.add(tasks);
         }
 
         TaskAdapter adapter = new TaskAdapter(fragmentView.getContext(), themes, allTasks);
         listview.setAdapter(adapter);
         
 //        registerForContextMenu(listview);
     }
 //    
 //    @Override
 //    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 //        menu.setHeaderTitle("Sample menu");
 //        menu.add(0, 0, 0, "Test");
 //    }
 //
 //    @Override
 //    public boolean onContextItemSelected(MenuItem item) {
 //        ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
 //
 //        String title = ((TextView) info.targetView).getText().toString();
 //
 //        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
 //        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
 //            int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition); 
 //            int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition); 
 //            Toast.makeText(this.getActivity().getApplicationContext(), title + ": Child " + childPos + " clicked in group " + groupPos,
 //                    Toast.LENGTH_SHORT).show();
 //            return true;
 //        } else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
 //            int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition); 
 //            Toast.makeText(this.getActivity().getApplicationContext(), title + ": Group " + groupPos + " clicked", Toast.LENGTH_SHORT).show();
 //            return true;
 //        }
 //
 //        return false;
 //    }
     
     public class TaskAdapter extends BaseExpandableListAdapter {
 
     private Context context;
     private ArrayList<Theme> themes;
     private ArrayList<ArrayList<Task>> tasks;
     private LayoutInflater inflater;
 
     public TaskAdapter(Context context, 
                         ArrayList<Theme> themes,
 			ArrayList<ArrayList<Task>> tasks ) { 
         this.context = context;
 		this.themes = themes;
         this.tasks = tasks;
         inflater = LayoutInflater.from( context );
     }
 
     public Object getChild(int groupPosition, int childPosition) {
         return tasks.get( groupPosition ).get( childPosition );
     }
 
     public long getChildId(int groupPosition, int childPosition) {
         return (long)( groupPosition*1024+childPosition );  // Max 1024 children per group
     }
 
     public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
         View v = null;
         if( convertView != null )
             v = convertView;
         else
             v = inflater.inflate(R.layout.course_work_child, parent, false);
         final Theme gt = (Theme)getGroup( groupPosition );
         final Task t = (Task)getChild( groupPosition, childPosition );
 		TextView name = (TextView)v.findViewById( R.id.childname );
 		if( name != null ) {
                     name.setText(t.getName());
                     if(t.getDone() == true) {
                         name.setAlpha((float)0.5);
                     } else {
                         name.setAlpha(1);
                     }
                 }		
 		CheckBox cb = (CheckBox)v.findViewById( R.id.checkbox );
                 cb.setChecked(t.getDone());
                 
                 v.setOnClickListener(new OnClickListener() {
 
                 public void onClick(View v) {
                     //Må ha en metode å sortere ut content type
                     //Dette er bare en midlertidig test
                     //TODO: FIX CONTENT TYPE CHECK
                     
                     Intent myIntent = null;
                     
                     if(t.getContent_url() != null) {
                         myIntent = new Intent(v.getContext(), VideoActivity.class);
                         myIntent.putExtra("videoURL", t.getContent_url());
                     } else {
                         Toast.makeText(context, "Empty task", Toast.LENGTH_LONG).show();  
                     }
                    
                    startActivityForResult(myIntent, 0);
                 }
             });
                 
             cb.setOnClickListener(new OnClickListener() {
                 public void onClick(View v) {
                     // Perform action on clicks, depending on whether it's now checked
                     if (((CheckBox) v).isChecked()) {
                         t.setDone(true);
                         new TellServerDone().execute(c.getId().toString(), gt.getId().toString(), t.getId().toString(), "1");
                     } else {
                         t.setDone(false);
                         new TellServerDone().execute(c.getId().toString(), gt.getId().toString(), t.getId().toString(), "0");
                     }
                     notifyDataSetChanged();
                 }
             });
         
         return v;
     }
     
     private class TellServerDone extends AsyncTask<String, Void, String> {
         protected String doInBackground(String... stuff) {
             return DownloadUtilities.getJSONData("http://master.uials.no:8080/muldvarp/resources/course/edit/" + stuff[0] + "/" + stuff[1] + "/"+ stuff[2] + "/" + stuff[3],loadLoginAndMakeHeader()).toString();
         }
         
         @Override
         protected void onPostExecute(String stuff) {            
             Toast.makeText(context, stuff, Toast.LENGTH_LONG).show();
         }
     }
 
     public int getChildrenCount(int groupPosition) {
         return tasks.get( groupPosition ).size();
     }
 
     public Object getGroup(int groupPosition) {
         return themes.get( groupPosition );        
     }
 
     public int getGroupCount() {
         return themes.size();
     }
 
     public long getGroupId(int groupPosition) {
         return (long)( groupPosition*1024 );  // To be consistent with getChildId
     } 
 
     public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
         View v = null;
         if( convertView != null )
             v = convertView;
         else
             v = inflater.inflate(R.layout.course_work_group, parent, false); 
         Theme gt = (Theme)getGroup( groupPosition );
 		TextView themeGroup = (TextView)v.findViewById( R.id.childname );
 		if( gt != null ) {
                     themeGroup.setText( gt.getName() + " " + gt.getCompletion() + "%" );
                         if( gt.getCompletion() == 100) {
                             themeGroup.setAlpha((float)0.5);
                         } else {
                             themeGroup.setAlpha(1);
                         }
                 }           
         return v;
     }
 
     public boolean hasStableIds() {
         return true;
     }
 
     public boolean isChildSelectable(int groupPosition, int childPosition) {
         return true;
     } 
 
     public void onGroupCollapsed (int groupPosition) {} 
     public void onGroupExpanded(int groupPosition) {}
 
 
     }
     
     public String loadLoginAndMakeHeader() {
         SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
 
         String username = settings.getString("username", "");
         String password = settings.getString("password", "");
         
         return "Basic " + Base64.encodeToString((username + ":" + password).getBytes(), Base64.NO_WRAP);
     }
 }
