 package com.example.wheresmystuff;
 
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 
 public class MainActivity extends Activity implements OnClickListener{
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		View register = findViewById(R.id.btn_register);
 		register.setOnClickListener(this);
		
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
			Log.d("MainActivity", "RegisterButton Clicked");
 			startActivity(i);
			
			
 		}
 		
 	}
 
 }
