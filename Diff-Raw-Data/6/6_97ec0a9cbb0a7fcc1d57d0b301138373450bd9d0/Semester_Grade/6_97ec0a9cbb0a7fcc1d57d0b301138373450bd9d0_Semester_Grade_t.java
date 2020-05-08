 package layoutClass;
 
 import com.example.grademanager.R;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
 
 public class Semester_Grade extends Activity {
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.semester_grades);
 		
		String text = "Authors: \n" + "Bilal Ait Slimane\n" + "Min Rui Chen\n" + "Charles-Antoine de Salaberry\n" + "Kamil Legault\n" + "Luc Tran\n\n" + "Build version: 1.0.16";
		TextView myText = (TextView) findViewById(R.id.abouttxt);
		myText.setText(text);
 	
 	}
 	
 	public void backSemesterGrades(View view) {
 		Intent intent = new Intent(getApplicationContext(), Semester_Selection.class);
 		startActivity(intent);
 	}
 
 }
