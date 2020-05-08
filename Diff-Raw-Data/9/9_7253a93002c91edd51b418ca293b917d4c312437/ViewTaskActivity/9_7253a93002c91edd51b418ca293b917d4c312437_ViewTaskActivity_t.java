 package ca.ualberta.cs.completemytask;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.widget.TextView;
 
 public class ViewTaskActivity extends Activity {
 
 	// Position of Task in TaskManager
 	private int position;
 	
 	// View IDs
 	private static final int VIEW_COMMENTS = 3;
 	
 	/**
 	 * Sets up the task view by populating text fields with relevant data and
 	 * adding images and audio to the respective galleries
 	 * 
 	 *   @TODO Add gallery population
 	 */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_view_task);
         
         position = (int) getIntent().getIntExtra("Task Position", -1);
         
         if (position >= 0) {
         	Task task = TaskManager.getInstance().getTaskAt(position);
         	
         	TextView taskName = (TextView) findViewById(R.id.TaskName);
         	taskName.setText(task.getName());
         	
         	TextView taskDescription = (TextView) findViewById(R.id.TaskDescription);
         	taskDescription.setText(task.getDescription());
         	
         	String requires = "";
         	
         	if (task.needsComment()) {
         		requires += "Needs Text\n";
         	}
         	
         	if (task.needsPhoto()) {
         		requires += "Needs Image\n";
         	}
         	
         	if (task.needsAudio()) {
         		requires += "Needs Audio\n";
         	}
         	
         	if (requires.equals("")) {
         		requires = "Nothing";
         	}
         	
         	TextView taskRequirements = (TextView) findViewById(R.id.TaskRequirements);
         	taskRequirements.setText(requires);
         }
     }
     
     /**
      * Called to view the comments for a particular task
      * 
      * @param A view
      */
     public void viewComments(View view){
    	Intent intent = new Intent(this, CommentActivity.class);
     	startActivityForResult(intent, VIEW_COMMENTS);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_view_task, menu);
         return true;
     }
     
     /**
      * Closes the task
      * 
      * @param view
      */
     public void close(View view) {
     	this.finish();
     }
 }
