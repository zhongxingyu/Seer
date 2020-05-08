 package com.huskysoft.eduki;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 import java.util.List;
 
 import com.example.eduki.R;
 import com.huskysoft.eduki.data.Course;
 import com.huskysoft.eduki.data.CourseQuery;
 
 public class CoursesListActivity extends Activity implements TaskComplete {
     
     private List<Course> courseList;
     
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         CourseQuery.getAllCourses(this);
         setContentView(R.layout.loading_screen);
     }
     
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
     public void taskComplete(String data) {
         setContentView(R.layout.activity_courseslist);
        courseList = CourseQuery.parseCourseList(data);
         ArrayAdapter<Course> adapter = new ArrayAdapter<Course>(this, 
                 android.R.layout.simple_list_item_1, courseList);
         ListView listView = (ListView) findViewById(R.id.courseListView);
         listView.setAdapter(adapter);
         listView.setOnItemClickListener(new OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 CoursesListActivity.this.courseSelected(position);
             }
         });
     }
     
     private void courseSelected(int position) {
         Course chosen = courseList.get(position);
         Intent i = new Intent(this, LessonsListActivity.class);
        i.putExtra("course_title", chosen.getTitle());
        i.putExtra("course_id", chosen.getId());
         startActivity(i);
     }
 }
