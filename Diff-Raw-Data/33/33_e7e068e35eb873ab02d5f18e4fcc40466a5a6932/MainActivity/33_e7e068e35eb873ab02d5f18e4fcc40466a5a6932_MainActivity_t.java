 package com.example.wheresmystuff;
 
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
 import android.util.Log;
import android.view.Gravity;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
 
 public class MainActivity extends Activity implements OnClickListener{
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		View register = findViewById(R.id.btn_register);
 		register.setOnClickListener(this);
		View login = findViewById(R.id.btnLogin);
		login.setOnClickListener(this);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 		
 		int button = v.getId();
 		if(button == R.id.btn_register){
 			Intent i = new Intent(this, Register.class);
 			startActivity(i);
			Log.d("MainActivity", "RegisterButton Clicked");
		}else if(button == R.id.btnLogin){
			Log.d("MainActivity", "LOGINBUTTON Clicked");
			TextView textView = new TextView(this);
	        textView.setBackgroundColor(Color.DKGRAY);
	        textView.setTextColor(Color.WHITE);
	        textView.setTextSize(30);
			Typeface typeface = Typeface.create("serif", Typeface.BOLD);
	        textView.setTypeface(typeface);
	        textView.setGravity(Gravity.CENTER);
	        textView.setText("User not found or Invalid Password");
	        
	      
			 Toast t = Toast.makeText(this, null, Toast.LENGTH_LONG);
			 t.setGravity(Gravity.FILL_HORIZONTAL|Gravity.CENTER_HORIZONTAL, 0, 0);
			 t.setView(textView);
			 t.show();
 		}
 		
 	}
 
 }
