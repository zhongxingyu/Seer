 package com.example.ap.verifier;
 

 import com.example.ap.comparison_strings.*;
 
 import android.nfc.Tag;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class HomeActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_home);
 		
 		Button buttonCalc = (Button) findViewById(R.id.calculate);
         buttonCalc.setOnClickListener(new OnClickListener()
         {
      	   public void onClick(View v){
      		   //ssc s = new ssc();
      		   //String comparison = s.get_comparison_string();
      		   //TextView text = (TextView) findViewById(R.id.shortStringValue);
      		   //text.setText(comparison);
      		   }
      	   }
         );
         
         Button buttonWriteTag = (Button) findViewById(R.id.writeTag);
         buttonWriteTag.setOnClickListener(new OnClickListener()
         {
      	   public void onClick(View v){
      		   byte[] file = null;
      		   Tag tag = null;
      		   MifareClassicParser m = new MifareClassicParser();
      		   m.writeMifareClassic(tag, file);
      	   }
         }
         );
         
         Button buttonAbout = (Button) findViewById(R.id.about);
         buttonAbout.setOnClickListener(new OnClickListener()
         {
      	   public void onClick(View v){
      		   Intent i = new Intent(HomeActivity.this, About.class); 
      		   startActivity(i);
      		   }
      	   }
         );
 		
 		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_home, menu);
 		return true;
 	}
 
 }
