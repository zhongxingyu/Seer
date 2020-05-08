 // rat issue #6 : 9/23/2012
 // Just created this activity
 // rat issue #6 : 9/29/2012
 // Add food screen allow user to store food in efridge
 // rat issue #6 : 9/29/2012
 // Click save button to store food in efridge
// rat issue #7 : 9/29/2012
// Database handling
 
 package com.efridge.activity;
 
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import com.efridge.model.FoodModel;
 
 public class AddFoodActivity extends BaseActivity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) { // TODO
 		
 		setContentView(R.layout.addfood_layout);
 		super.onCreate(savedInstanceState);
 		
 		Button saveFoodBtn = (Button)findViewById(R.id.saveBtn);
 		saveFoodBtn.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 
 				EditText foodName = (EditText)findViewById(R.id.foodNameEdit);
 				EditText foodDescription = (EditText)findViewById(R.id.foodDescriptionEdit);
 				
 				FoodModel foodModel = new FoodModel();
 				foodModel.setFoodName(foodName.getText().toString());
 				foodModel.setFoodDescription(foodDescription.getText().toString());
 				foodModel.setExpiryDate(1);
 				
 				foodModel.saveToDb();
 				setMessage();
				
 			}
 		});
 	}
 	
 	private void setMessage(){
 		
 		EditText foodName = (EditText)findViewById(R.id.foodNameEdit);
 		EditText foodDescription = (EditText)findViewById(R.id.foodDescriptionEdit);
 		TextView msgTextView = (TextView)findViewById(R.id.msgStr);
 		
 		msgTextView.setText(foodName.getText() + " added in eFridge");
 		foodName.setText("");
 		foodDescription.setText("");
 	}
 }
