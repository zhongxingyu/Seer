 package de.dennowar.view;
 
 import de.dennowar.R;
 import de.dennowar.plain.ActivityRegistry;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.RadioGroup;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 
 public class StartActivity extends Activity {
 	
	private int grad;
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.start);
         ActivityRegistry.register(this);
         
         Button btn = (Button) findViewById(R.id.btn_los);
         RadioGroup rg = (RadioGroup) findViewById(R.id.rg_start);
         
         rg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			
 			@Override
 			public void onCheckedChanged(RadioGroup group, int checkedId) {
 				
 				switch(checkedId){
 					case R.id.rb_jf:
 						grad = 0;
 						break;
 					case R.id.rb_tm:
 						grad = 1;
 						break;
 					case R.id.rb_tf:
 						grad = 2;
 						break;
 					case R.id.rb_gf:
 						grad = 3;
 						break;
 					default:
 						grad = 1;
 						break;
 				}
 			}
 		});
         
         btn.setOnClickListener(new OnClickListener(){
         	@Override
         	public void onClick(View v){
         		Intent i = new Intent(StartActivity.this, FFGameActivity.class);
         		i.putExtra("de.dennowar.view.grad", grad);
         		startActivity(i);
         	}
         });
     }
 }
