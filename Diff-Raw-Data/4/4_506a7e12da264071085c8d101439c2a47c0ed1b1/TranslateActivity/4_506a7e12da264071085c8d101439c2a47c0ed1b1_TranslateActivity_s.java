 package com.johndaniel.glosar;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.MenuInflater;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.FrameLayout;
 import android.widget.Toast;
 
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 
 
 public class TranslateActivity extends Activity {
 	boolean showingBack;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_translate);
 		
 		if (savedInstanceState == null) {
             getFragmentManager()
                     .beginTransaction()
                     .add(R.id.translate_container, new TranslateFragment1())
                     .commit();
             showingBack = false;
         }
 		
 		FrameLayout container = (FrameLayout) findViewById(R.id.translate_container);
 		container.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				
 				//Snurra
 				flip();
 				
 			}
 		});
 	}
 	@SuppressLint("NewApi")
 	private void flip(){
 		if (showingBack){
 			getFragmentManager().popBackStack();
 			showingBack = false;
 			return;
 		}
 		
 		TranslateFragment2 word2 = new TranslateFragment2();;
 		
 		showingBack = true;
 		
 		getFragmentManager().beginTransaction()
 			.setCustomAnimations(R.anim.card_flip_right_in, R.anim.card_flip_right_out, 
 					R.anim.card_flip_left_in, R.anim.card_flip_left_out)
			.replace(R.id.translate_container, new TranslateFragment2())
 			.addToBackStack(null)
 			.commit();
 	}
 
 	
 	
 }
