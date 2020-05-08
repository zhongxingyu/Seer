 package com.multimedia.slidepuzzel;
 
 import com.multimedia.slidepuzzel.application.SharedApplication;
 import com.multimedia.slidepuzzel.data.Settings;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 
 public class SettingsActivity extends Activity {
 		private Settings settings;
 		private SharedApplication app;
 	
 		@Override
 		public void onCreate(Bundle savedInstanceState) {
 			super.onCreate(savedInstanceState);
 			
 			setContentView(R.layout.settings);
 			
 			app = (SharedApplication) getApplication();
 			settings = app.dataManager.getSettings();
 			app.diff = settings.getDifficulty();
 			app.size = settings.getSize();
 			app.mode = settings.getMode();
 			
 			Button back = (Button) findViewById(R.id.back3);
 			back.setOnClickListener(new View.OnClickListener() {
 				public void onClick(View view) {
 					finish();
 				}
 			});
 			
 			Button reset = (Button) findViewById(R.id.reset);
 			reset.setOnClickListener(new View.OnClickListener() {
 				public void onClick(View view) {				
 					RadioButton easy = (RadioButton)findViewById(R.id.easy);
 					easy.setChecked(true);
 					app.diff = "EASY";
 					
 					RadioButton three = (RadioButton)findViewById(R.id.three);
 					three.setChecked(true);
 					app.size = 3;
 					
 					RadioButton live = (RadioButton)findViewById(R.id.live);
 					live.setChecked(true);
 					app.mode = 0;
 					
 					settings.setDifficulty(app.diff);
 					settings.setSize(app.size);
 					settings.setMode(app.mode);
 					app.dataManager.updateSettings(settings);
 				}
 			});
 			
 			RadioGroup rg1=(RadioGroup)findViewById(R.id.radioGroup1);
 			if(app.diff.equals("EASY")){
 				rg1.check(R.id.easy);
 			}
 			else if(app.diff.equals("NORMAL")){
 				rg1.check(R.id.normal);
 			}
 			else if(app.diff.equals("HARD")){
 				rg1.check(R.id.hard);
 			}
 			
 			rg1.setOnCheckedChangeListener(new android.widget.RadioGroup.OnCheckedChangeListener(){
 				public void onCheckedChanged(RadioGroup arg0, int arg1) {
 					RadioButton rb = (RadioButton)findViewById(arg1);
 					
 					if(rb.getText().equals("Easy")){
 						app.diff = "EASY";
 						RadioButton easy=(RadioButton)findViewById(R.id.easy);
 						easy.setChecked(true);
 					}
 					else if(rb.getText().equals("Normal")){
 						app.diff = "NORMAL";
 						RadioButton normal=(RadioButton)findViewById(R.id.normal);
 						normal.setChecked(true);
 					}
 					else if(rb.getText().equals("Hard")){
 						app.diff = "HARD";
 						RadioButton hard=(RadioButton)findViewById(R.id.hard);
 						hard.setChecked(true);
 					}
 					settings.setDifficulty(app.diff);
 					app.dataManager.updateSettings(settings);
 				}
 			
 			});
 			
 
 			RadioGroup rg2=(RadioGroup)findViewById(R.id.radioGroup2);
 			if(app.size == 3){
 				rg2.check(R.id.three);
 			}
 			else if(app.size == 4){
 				rg2.check(R.id.four);
 			}
 			
 			rg2.setOnCheckedChangeListener(new android.widget.RadioGroup.OnCheckedChangeListener(){
 				public void onCheckedChanged(RadioGroup arg0, int arg1) {
 					SharedApplication app = (SharedApplication) getApplication();
 					RadioButton rb=(RadioButton)findViewById(arg1);
 					
 					if(rb.getText().equals("4 x 4")){
 						app.size = 4;
 						RadioButton four=(RadioButton)findViewById(R.id.four);
 						four.setChecked(true);
 					}
 					else if(rb.getText().equals("3 x 3")){
 						app.size = 3;
 						RadioButton three=(RadioButton)findViewById(R.id.three);
 						three.setChecked(true);
 					}
 					
 					settings.setSize(app.size);
 					app.dataManager.updateSettings(settings);
 				}
 			});
 
 			RadioGroup rg3=(RadioGroup)findViewById(R.id.radioGroup3);
 			if(app.mode == 0){
 				rg3.check(R.id.live);
 			}
 			else if(app.mode == 1){
 				rg3.check(R.id.image);
 			}
 			
 			rg3.setOnCheckedChangeListener(new android.widget.RadioGroup.OnCheckedChangeListener(){
 				public void onCheckedChanged(RadioGroup arg0, int arg1) {
 					SharedApplication app = (SharedApplication) getApplication();
 					RadioButton rb=(RadioButton)findViewById(arg1);
 					
					if(rb.getText().equals("Live Image")){
 						app.mode = Settings.MODE_LIVE;
 						RadioButton live=(RadioButton)findViewById(R.id.live);
 						live.setChecked(true);
 					}
 					else{
 						app.mode = Settings.MODE_IMAGE;
 						RadioButton image=(RadioButton)findViewById(R.id.image);
 						image.setChecked(true);
 					}
 					
 					settings.setMode(app.mode);
 					app.dataManager.updateSettings(settings);
 				}
 			});
 			
 	   }
 }
 
