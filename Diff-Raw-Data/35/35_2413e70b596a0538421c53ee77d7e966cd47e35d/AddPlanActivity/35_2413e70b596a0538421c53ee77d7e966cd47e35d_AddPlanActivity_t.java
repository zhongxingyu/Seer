 package com.example.p1;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class AddPlanActivity extends Activity {
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_addplan);
 		Button btnOK = (Button) findViewById(R.id.ok);
 		btnOK.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				EditText text1 = (EditText) findViewById(R.id.editText1);
 				String et1 = text1.getText().toString();
 				Plan p = new Plan();
 				p.name = et1;
 				p.desc = "Description of " + et1;
 				Globals g = Globals.getInstance();
 				g.addPlan(p);
				v.invalidate();
				finish();
 			}
 		});
 	}
 }
