 package com.example.wifilocatordemo;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 //import android.widget.TextView;
 import android.widget.Toast;
 
 public class TestPassAc extends Activity{
 private Button bt1;
 private String test;
 private EditText HintText;
private Button	btChange;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.pass);
 		HintText = (EditText) findViewById(R.id.HintText);
 		bt1 = (Button) findViewById(R.id.bt1);
		btChange = (Button) findViewById(R.id.btChange);
 		bt1.setOnClickListener(new OnClickListener() {
 			
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				test= HintText.getText().toString();
 				if(test.compareTo("abc123") == 0)
 					startActivity(new Intent(TestPassAc.this,WifiActivity.class));
 				else {
 					Toast.makeText(getApplicationContext(), "Incorrect ! Try retype password",
 						Toast.LENGTH_SHORT).show();
 					HintText.setText("");
 				}
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.wifiinfo, menu);
 		return true;
 	}
 	public String getHintText(){
 		return HintText.getHint().toString();
 	}
 }
