 
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
 
 import com.huskysoft.eduki.R;
 import com.huskysoft.eduki.data.Course;
 import com.huskysoft.eduki.data.CourseQuery;
 
 /**
  * @author Cody Thomas Class CoursesListActivity shows a list of all courses,
  *         allowing them to be clicked.
  */
 
 public class CoursesListActivity extends Activity implements TaskComplete {
     /**
      * The list of courses to be displayed, not initialized until data has been
      * loaded
      */
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
 
     /**
      * 
      * @return The current list of courses in use
      */
     public List<Course> getCourseList() {
        return courseList;
     }
     
     @Override
     public void taskComplete(String data) {
         courseList = CourseQuery.parseCourseList(data);
         ArrayAdapter<Course> adapter = new ArrayAdapter<Course>(this,
                 android.R.layout.simple_list_item_1, courseList);
         setContentView(R.layout.activity_courseslist);
         ListView listView = (ListView) findViewById(R.id.courseListView);
         listView.setAdapter(adapter);
         listView.setOnItemClickListener(new OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 CoursesListActivity.this.courseSelected(position);
             }
         });
     }
 
     /**
      * Will use the position parameter and find that course in the list of courses,
      * calling the lessons list activity.
      * @param position the position in the list of the button pressed
      */
     private void courseSelected(int position) {
         Course chosen = courseList.get(position);
         Intent i = new Intent(this, LessonsListActivity.class);
         i.putExtra("course_title", chosen.getTitle());
         i.putExtra("course_id", chosen.getId());
         startActivity(i);
     }
 }
