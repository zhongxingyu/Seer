 package de.sgtgtug.hackathon;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
import android.widget.ImageView;
 
 public class HelpME extends Activity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
        ImageView action_btn = (ImageView) findViewById(R.id.action_btn);
         action_btn.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				Intent msgBroker = new Intent(HelpME.this, MessageBroker.class);
 		        msgBroker.putExtra(MessageBroker.SMS, true);
 		        msgBroker.putExtra(MessageBroker.EMAIL, true);
 		        startActivity(msgBroker);
 			}
 		});
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	menu.add(0, Menu.FIRST, Menu.NONE, R.string.settings_title);
     	return super.onCreateOptionsMenu(menu);
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch (item.getItemId()){
     	case Menu.FIRST: 
     		startActivity(new Intent(this, HelpMePreferences.class));
     		break;
     	}
     	return super.onOptionsItemSelected(item);
     }
 }
