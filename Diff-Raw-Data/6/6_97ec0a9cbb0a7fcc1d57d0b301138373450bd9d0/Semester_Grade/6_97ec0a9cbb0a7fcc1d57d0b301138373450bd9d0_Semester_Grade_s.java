 package layoutClass;
 
 import com.example.grademanager.R;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 
 public class Semester_Grade extends Activity {
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.semester_grades);
 		
 	
 	}
 	
 	public void backSemesterGrades(View view) {
 		Intent intent = new Intent(getApplicationContext(), Semester_Selection.class);
 		startActivity(intent);
 	}
 
 }
