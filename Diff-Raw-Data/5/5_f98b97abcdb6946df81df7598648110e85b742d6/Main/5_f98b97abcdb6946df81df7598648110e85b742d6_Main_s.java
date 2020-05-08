 package com.example.exercise_02;
 
 import android.net.Uri;
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.Toast;
 import android.content.Intent;
 //import android.net.Uri;
 //import android.widget.Toast;
 
 
 public class Main extends Activity {
 	
 	private final String strParam = "PARAM";
 	
 	private final int FROM_ACTIVITY_2 = 2;
 	
 	private final int FROM_ACTIVITY_3 = 3;
 	
 
 	Button bt01, bt02, bt03, btBrowser, btCall;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		setupButtons();
 	}
 
 	public void setupButtons(){
 		
 		bt01 = (Button) this.findViewById(R.id.bt01);
 		bt02 = (Button) this.findViewById(R.id.bt02);
 		bt03 = (Button) this.findViewById(R.id.bt03);
 		btBrowser = (Button) this.findViewById(R.id.btBrowser);
 		btCall = (Button) this.findViewById(R.id.btCall);
 		
 		
 		bt01.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				
 				Intent intent = new Intent(Main.this, Activity_01.class);
 					
 				startActivity(intent);			
 				
 			}
 		});
 		
 		bt02.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				
 				Intent intent = new Intent(Main.this, Activity_02.class);
 				
 				startActivityForResult(intent, FROM_ACTIVITY_2);
 				
 			}
 		});
 		
 		bt03.setOnClickListener( new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 			
 				Intent intent = new Intent(Main.this, Activity_03.class);
 				startActivityForResult(intent, FROM_ACTIVITY_3);
 			}
 		});
 		
 		
 		btBrowser.setOnClickListener( new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Intent browserIntent = 	new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.zoumpis.eu"));
 				startActivity(browserIntent);
 				
 			}
 		});
 		
 		btCall.setOnClickListener( new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				
 				Intent intent = new Intent(Intent.ACTION_CALL);
 				intent.setData(Uri.parse("tel:666-666-666"));
 				startActivity(intent);
 			}
 		});
 			
 		
 	}
 	
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	
 	 @Override
 	    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 
 	        super.onActivityResult(requestCode, resultCode, data);
 
 	        if (resultCode == Activity.RESULT_OK) {
 
 	            if (requestCode == FROM_ACTIVITY_2) {
 
	                Integer value = data.getIntExtra(this.strParam, -1);
 
 	                Toast.makeText(this,
 	                        "From Activity 02:" + String.valueOf(value),
 	                        Toast.LENGTH_SHORT).show();
 
 	            } else if (requestCode == FROM_ACTIVITY_3) {
 
	                Integer value = data.getIntExtra(this.strParam, -1);
 
 	                Toast.makeText(this,
 	                        "From Activity 03:" + String.valueOf(value),
 	                        Toast.LENGTH_SHORT).show();
 	            }
 
 	        }
 	    }
 
 }
