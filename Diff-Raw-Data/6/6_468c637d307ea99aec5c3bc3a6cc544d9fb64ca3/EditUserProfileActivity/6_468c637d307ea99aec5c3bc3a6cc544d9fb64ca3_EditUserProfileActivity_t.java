 package com.vorsk.crossfitr;
 
 import com.vorsk.crossfitr.models.AchievementModel;
 import com.vorsk.crossfitr.models.ProfileModel;
 import com.vorsk.crossfitr.models.SQLiteDAO;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class EditUserProfileActivity extends Activity implements OnClickListener 
 {
	private View editBackground;
 	private EditText nameTextField;
 	private EditText weightTextField;
 	private EditText heightTextField;
 	private EditText goalWeightTextField;
 	private InputMethodManager keyControl;
 	ProfileModel model = new ProfileModel(this);
 	AchievementModel achievementModel = new AchievementModel(this);
 	
 	private Typeface font;
 	
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 		keyControl = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 		setContentView(R.layout.userprofile_form);
 		model.open();
 	
 		// Setting typeface
 		font = Typeface.createFromAsset(this.getAssets(),
 				"fonts/Roboto-Thin.ttf");
 		
 		TextView fontSetter = (TextView) findViewById(R.id.edit_user_name);
 		fontSetter.setTypeface(font);
 		
 		fontSetter = (TextView) findViewById(R.id.edit_height);
 		fontSetter.setTypeface(font);
 		
 		fontSetter = (TextView) findViewById(R.id.edit_weight);
 		fontSetter.setTypeface(font);
 		
 		fontSetter = (TextView) findViewById(R.id.edit_goal_weight);
 		fontSetter.setTypeface(font);
		
		editBackground = (View) findViewById(R.id.userprofile_form_bg);
		editBackground.setOnClickListener(this);
		
 
 		// Save button
 		View saveButton = findViewById(R.id.button_userprofile_form_save);
 		saveButton.setOnClickListener(this);
 		fontSetter = (TextView) findViewById(R.id.button_userprofile_form_save);
 		fontSetter.setTypeface(font);
 
 		// Cancel button
 		View cancelButton = findViewById(R.id.button_userprofile_form_cancel);
 		cancelButton.setOnClickListener(this);
 		fontSetter = (TextView) findViewById(R.id.button_userprofile_form_cancel);
 		fontSetter.setTypeface(font);
 		
 		// Name field
         nameTextField = (EditText) findViewById(R.id.user_name_field);
         nameTextField.setOnClickListener(this);
         if(model.getByAttribute("name") != null)
         	nameTextField.setText(model.getByAttribute("name").value);        
 
         // Weight field
         weightTextField = (EditText) findViewById(R.id.user_weight_field);
         weightTextField.setOnClickListener(this);
         if(model.getByAttribute("weight") != null)
         	weightTextField.setText(model.getByAttribute("weight").value);
 
         //Goal Weight field
         goalWeightTextField = (EditText) findViewById(R.id.user_goal_weight_field);
         goalWeightTextField.setOnClickListener(this);
         if(model.getByAttribute("goal_weight") != null)
         	goalWeightTextField.setText(model.getByAttribute("goal_weight").value);
         
         //Height field
         heightTextField = (EditText) findViewById(R.id.user_height_field);
         heightTextField.setOnClickListener(this);
         if(model.getByAttribute("height") != null)
         	heightTextField.setText(model.getByAttribute("height").value);
         
         model.close();
 	}
 
 	public void onClick(View v) 
 	{
 		Context context = getApplicationContext();
 		CharSequence text;
 		int duration;
 		Toast toast;
 		
 		switch(v.getId())
 		{
 		case R.id.button_userprofile_form_save:
 			
 			// Blank Checking Validation
 			if(this.isNotBlank() == false)
 			{
 				text = "Please fill out all fields!";
 				duration = Toast.LENGTH_SHORT;
 				toast = Toast.makeText(context, text, duration);
 				toast.show();
 			}
 			else{
 				model.open();
 				long name_id = model.updateInsert("name", nameTextField.getText().toString());
 				long height_id = model.updateInsert("height", heightTextField.getText().toString());
 				long current_weight_id = model.updateInsert("weight", weightTextField.getText().toString());
 				long goal_weight_id = model.updateInsert("goal_weight", goalWeightTextField.getText().toString());
 				
 				text = achievementModel.getProgress(AchievementModel.TYPE_MISC);
 				duration = Toast.LENGTH_LONG;
 				if(text != null){
 					toast = Toast.makeText(context, text, duration);
 					toast.show();
 				}
 				
 				model.close();
 				finish();
 				Intent u = new Intent(this, UserProfileActivity.class);
 				startActivity(u);
 			}
 			break;
 			
 		case R.id.userprofile_form_bg:
 			hideKeyboard(nameTextField);
 			hideKeyboard(weightTextField);
 			hideKeyboard(heightTextField);
 			hideKeyboard(goalWeightTextField);
 			break;
 
 		case R.id.button_userprofile_form_cancel:
 /*			model.open();
 			if(model.getByAttribute("name") != null &&
 			   model.getByAttribute("weight") != null &&
 			   model.getByAttribute("goal_weight") != null &&
 			   model.getByAttribute("height") != null){  
 				model.close();*/
 				finish();
 				Intent u = new Intent(this, UserProfileActivity.class);
 				startActivity(u);
 /*			}
 			else{
 				text = "Please save your details!";
 				toast = Toast.makeText(context, text, duration);
 				toast.show();
 				model.close();
 			}*/
 				
 		/*case d.background_main:
 			hideKeyboard(weightTextField);
 			hideKeyboard(nameTextField);
 			hideKeyboard(heightTextField);
 			hideKeyboard(goalWeightTextField);
 			break;*/
 		}
 		
 	}
 	
 	// If back is pressed and no information is saved, go directly to the frontpage.
 /*	public void onBackPressed(){
 		model.open();
 		if(model.getByAttribute("name") == null ||
 		   model.getByAttribute("weight") == null ||
 		   model.getByAttribute("goal_weight") == null ||
 		   model.getByAttribute("height") == null){  
 			model.close();
 			Intent u = new Intent(this, CrossFitrActivity.class);
 			startActivity(u);
 		}
 		else{
 			model.close();
 			Intent u = new Intent(this, UserProfileActivity.class);
 			startActivity(u);
 		}
 	}*/
 	
 	//method to hide the keyboard
 	private void hideKeyboard(EditText eBox) 
 	{
 		keyControl.hideSoftInputFromWindow(eBox.getWindowToken(), 0);
 	}
 	
 	private boolean isNotBlank(){
 		if (nameTextField.getText().length() <= 0)
 			return false;
 		else if(heightTextField.getText().length() <= 0)
 			return false;
 		else if(weightTextField.getText().length() <= 0)
 			return false;
 		else if(goalWeightTextField.getText().length() <= 0)
 			return false;
 		else
 			return true;
 	}
 }
 
