 package org.fr.ykatchou.paillardes;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 public class PaillardeMenu extends Activity {
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		Chanson.dbhelp = new DatabaseHelper(this);
 
 		// / BINDINGS
 		// //////////////////////////////////////////////////
 
 		Button b = (Button) findViewById(R.id.btn_list);
 		b.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent i = new Intent(v.getContext(), PaillardeList.class);
 				Bundle b = new Bundle();
 				b.putString("filter", "");
 				i.putExtra("data", b);
 				startActivity(i);
 			}
 		});
 
 		b = (Button) findViewById(R.id.btn_about);
 		b.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent i = new Intent(v.getContext(), About.class);
 				startActivity(i);
 			}
 		});
 
 		b = (Button) findViewById(R.id.btn_quit);
 		b.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				finish();
 			}
 		});
 	}
 }
