 package itsmap.f13.stud10731.hand_in3;
 
 import itsmap.f13.stud10731.hand_in3.MenuFragment.OnMenuFragmentClickListener;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Toast;
 
 public class MainActivity extends FragmentActivity implements OnMenuFragmentClickListener {
 	
 	private boolean isDualPane;
 	private final String TAG = "MainActivity.class"; 
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         View frag = findViewById(R.id.content_fragment_container);
         isDualPane = (frag != null && frag.getVisibility() == View.VISIBLE);
         Log.d(TAG,"isDualPane = " + isDualPane);
         
         if (findViewById(R.id.menu_fragment_container) != null) {
         
         	if (savedInstanceState != null) {
                 return;
             }
         	
 	        FragmentManager fragmentManager = getSupportFragmentManager();
 	        FragmentTransaction fragTrans = fragmentManager.beginTransaction();
 	        fragTrans.add(R.id.menu_fragment_container, new MenuFragment());
 	        fragTrans.commit();
         }
         
         
         
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
 	@Override
 	public void onMenuFragmentClick(String item) {
 		Toast.makeText(getBaseContext(), item, Toast.LENGTH_SHORT).show();
 		
 		
 		
 		if(isDualPane) {
 			FragmentManager fragmentManager = getSupportFragmentManager();
 	        FragmentTransaction fragTrans = fragmentManager.beginTransaction();
 	        
 			if (findViewById(R.id.content_fragment_container) != null) {
 				Log.d(TAG,"Content fragment container is null.");
 				ContentFragment contentFragment = new ContentFragment();
 				fragTrans.add(R.id.content_fragment_container, contentFragment).commit();
 				
				if(contentFragment != null && item != null){
					contentFragment.setText(item);
				} else {
					Log.d(TAG,"contentFragment or item is null.");
				}
 			}
 			
 			
 		} else {
 			Intent intent = new Intent(getBaseContext(),ContentActivity.class);
 			intent.putExtra("item", item);
 			startActivity(intent);
 		}
 		
 	}
 }
