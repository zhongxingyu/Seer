 package no.ntnu.noahsprogark.bedpresbingo;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 
 public class MainActivity extends Activity {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         findViewById(R.id.newGameButton).setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent i = new Intent(v.getContext(), GameActivity.class);
 				startActivityForResult(i, 0);
 			}
 		});
         findViewById(R.id.adminButton).setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
				Intent i = new Intent(v.getContext(), SettingsActivity.class);
 				startActivityForResult(i, 1);
 				System.out.println("ADMIN");
 			}
 		});
         findViewById(R.id.settingsButton).setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
				Intent i = new Intent(v.getContext(), AdminActivity.class);
 				startActivityForResult(i, 2);
 				System.out.println("SETTINGS");
 			}
 		});
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
 }
