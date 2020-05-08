 package com.livestrong.myplate.activity;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 import com.livestrong.myplate.MyPlateApplication;
 import com.livestrong.myplate.R;
 import com.livestrong.myplate.back.DataHelper;
 import com.livestrong.myplate.back.models.Food;
 import com.livestrong.myplate.back.models.FoodDiaryEntry;
 import com.livestrong.myplate.utilities.ImageLoader;
 import com.livestrong.myplate.utilities.picker.NumberPicker;
 
 public class AddFoodActivity extends LiveStrongActivity {
 	
 	public static String INTENT_FOOD_NAME = "foodName";
 	
 	private Food food;
 	private FoodDiaryEntry diaryEntry;
 	private NumberPicker servingsPicker, servingsFractionPicker;
 	private Button iAteThisButton;
 	private ProgressBar progressBar;
 	
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         if (savedInstanceState != null) {
         	// The activity was destroyed, and is about to be started again. You need to restore your activity state from savedInstanceState.
             // UI elements states are restored automatically by super.onCreate()
         }
 
         // The activity is being created; create views, bind data to lists, etc.
         setContentView(R.layout.activity_add_food);
         
          this.iAteThisButton = (Button) findViewById(R.id.iAteThisButton);
         this.progressBar = (ProgressBar) findViewById(R.id.progressBar);
         
         // Fetch Food from Intent Extras
         Bundle extras = getIntent().getExtras();
         if (extras != null) {
         	this.food = (Food) extras.get(Food.class.getName());
         	// smallFood has the following data members populated: item_title,cals,serving_size,food_id,images.100,images,verification
         	
         	if (this.food != null) {
         		this.diaryEntry = null;
         		if (food.isCustom()) {
 				 	List<Food> foods = new ArrayList<Food>();
 				 	foods.add(food);
 				 	dataReceived(DataHelper.METHOD_GET_FOODS, foods);
 				 } else {
 					 this.iAteThisButton.setVisibility(View.INVISIBLE);
 					 this.progressBar.setVisibility(View.VISIBLE);
 					 DataHelper.getFood(food.getFoodId(), this);
 				 }
         	} else {
         		this.diaryEntry = (FoodDiaryEntry) extras.get(FoodDiaryEntry.class.getName());
        		
         		this.food = this.diaryEntry.getFood();
         		if (this.food.isGeneric()) {
         			this.food.setServingSize("1 serving");
         		}
         		
         		List<Food> foods = new ArrayList<Food>(1);
         		foods.add(this.food);
         		dataReceived(DataHelper.METHOD_GET_FOODS, foods);
         	}
 
         	
         	ImageLoader imageLoader = new ImageLoader(this);
 	    	ImageView imageView = (ImageView)findViewById(R.id.foodImageView);
 	    	// Load food image
 			switch (MyPlateApplication.getWorkingTimeOfDay()) {
 				case BREAKFAST:
 					imageView.setImageResource(R.drawable.icon_breakfast);
 					break;
 				case LUNCH:
 					imageView.setImageResource(R.drawable.icon_lunch);				
 					break;
 				case DINNER:
 					imageView.setImageResource(R.drawable.icon_dinner);
 					break;
 				case SNACKS:
 					imageView.setImageResource(R.drawable.icon_snacks);
 					break;
 			}
 	    	imageLoader.DisplayImage(this.food.getSmallImage(), imageView);
         	
 	    	// Load textViews
 	    	TextView tv;
 	    	if (this.diaryEntry != null) {
 	        	tv = (TextView)findViewById(R.id.foodNameTextView);
 		    	tv.setText(this.diaryEntry.getTitle());
 		    	
 		    	tv = (TextView) findViewById(R.id.foodDescriptionTextView);
 		    	tv.setText(this.diaryEntry.getDescription());
 
 		    	tv = (TextView) findViewById(R.id.caloriesTextView);
 		    	tv.setText(this.diaryEntry.getCals()+"");
 		    	
 		    	
 	    	} else {
 	        	tv = (TextView)findViewById(R.id.foodNameTextView);
 		    	tv.setText(this.food.getTitle());
 		    	
 		    	tv = (TextView) findViewById(R.id.foodDescriptionTextView);
 		    	tv.setText(this.food.getDescription());
 
 		    	tv = (TextView) findViewById(R.id.caloriesTextView);
 		    	tv.setText(this.food.getCals()+"");
 	    	}
 
 	    	tv = (TextView) findViewById(R.id.servingSizeTextView);
 	    	tv.setText(this.food.getServingSize());
 	    	
 	    	tv = (TextView) findViewById(R.id.dateTextView);
 	    	tv.setText(MyPlateApplication.getPrettyWorkingDate());
 	    	
 	    	tv = (TextView) findViewById(R.id.timeOfDayTextView);
 	    	tv.setText(MyPlateApplication.getWorkingTimeOfDayString());
 	    	
 	        this.initializeButtons();       
         }
     }
 	
 	private void initializeButtons(){
         Button deleteButton = (Button) findViewById(R.id.deleteButton);
         Button nutritionFactsButton = (Button) findViewById(R.id.nutritionFactsButton);
 
 		if (this.diaryEntry != null) {
 			iAteThisButton.setText("Update");
         	deleteButton.setVisibility(View.VISIBLE);
     	} else {
     		iAteThisButton.setText("I Ate This");
         	deleteButton.setVisibility(View.GONE);
     	}
 		
 		iAteThisButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				final Dialog dialog = new Dialog(AddFoodActivity.this);
 				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
 				dialog.setContentView(R.layout.dialog_add_food);
 				
 				AddFoodActivity.this.initializePickers(dialog);
 				
 				if (AddFoodActivity.this.diaryEntry != null){
 					setPickers(AddFoodActivity.this.diaryEntry.getServings());	
 				} 
 				
 				Button doneBtn = (Button) dialog.findViewById(R.id.doneButton);
 				doneBtn.setOnClickListener(new View.OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						
 						double pickerServings = getPickerServings();
 						
 						if (AddFoodActivity.this.diaryEntry == null){
 							if (pickerServings > 0.0){
 								FoodDiaryEntry e = new FoodDiaryEntry(
 			    	            		AddFoodActivity.this.food,
 			    	            		null, // TODO mealId
 			    	            		MyPlateApplication.getWorkingTimeOfDay(), 
 			    	            		pickerServings,
 			    	            		MyPlateApplication.getWorkingDateStamp());
 								DataHelper.saveDiaryEntry(e, AddFoodActivity.this);
 							}
 						} else {
 							if (pickerServings == 0.0){
 								DataHelper.deleteDiaryEntry(AddFoodActivity.this.diaryEntry, AddFoodActivity.this);				
 							} else {
 								AddFoodActivity.this.diaryEntry.setServings(pickerServings);
 								DataHelper.saveDiaryEntry(AddFoodActivity.this.diaryEntry, AddFoodActivity.this);
 							}							
 						}   
 						
 						if (pickerServings > 0.0){
 							Intent resultIntent = new Intent();
 							resultIntent.putExtra(AddFoodActivity.INTENT_FOOD_NAME, AddFoodActivity.this.food.getTitle());
 						
 							setResult(Activity.RESULT_OK, resultIntent);
 						}
 						
 	    	            dialog.cancel();
 	    	            finish();							
 					}
 				});
 				
 				Button cancelBtn = (Button) dialog.findViewById(R.id.cancelButton);
 				cancelBtn.setOnClickListener(new View.OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						dialog.cancel();
 					}
 				});
 				
 				dialog.show();
 			}
 		});
 		
     	deleteButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 	            DataHelper.deleteDiaryEntry(AddFoodActivity.this.diaryEntry, AddFoodActivity.this);
 	            finish();
 			}
 		});
 
         nutritionFactsButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(AddFoodActivity.this, NutritionFactsActivity.class);
 				intent.putExtra(AddFoodActivity.this.food.getClass().getName(), AddFoodActivity.this.food);
 				startActivity(intent);
 			}
 		});
 	}
 	
 	private double getPickerServings() {
 		int selectedIndex = this.servingsPicker.getCurrent();
 		Integer servings = (Integer) FoodDiaryEntry.servingsPickerValues.values().toArray()[selectedIndex];
 
 		selectedIndex = this.servingsFractionPicker.getCurrent();
 		Double servingsFraction = (Double) FoodDiaryEntry.servingsFractionPickerValues.values().toArray()[selectedIndex];
 		
 		return servings + servingsFraction;
 	}
 
 	private void setPickers(double servings) {
 		this.servingsPicker.setCurrent(this.diaryEntry.getServingsWholeIndex());
 		this.servingsFractionPicker.setCurrent(this.diaryEntry.getServingsFractionIndex());
 	}
 
 	private void initializePickers(Dialog dialog){
 		// Hook up outlets
 		this.servingsPicker = (NumberPicker) dialog.findViewById(R.id.servingsPicker);
         this.servingsFractionPicker = (NumberPicker) dialog.findViewById(R.id.servingsFractionPicker);
 		
 		String[] servingValues = FoodDiaryEntry.servingsPickerValues.keySet().toArray(new String[FoodDiaryEntry.servingsPickerValues.size()]);
        this.servingsPicker.setRange(1, servingValues.length - 1, servingValues);
         this.servingsPicker.setFocusable(false);
        this.servingsPicker.setCurrent(1);
         
         String[] servingFractionValues = FoodDiaryEntry.servingsFractionPickerValues.keySet().toArray(new String[FoodDiaryEntry.servingsFractionPickerValues.size()]);
         this.servingsFractionPicker.setRange(0, servingFractionValues.length - 1, servingFractionValues);
         this.servingsFractionPicker.setFocusable(false);
 	}
 	
     @Override
     protected void onStart() {
         super.onStart();
         // The activity is about to become visible.
         // -> onResume()
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         // The activity has become visible (it is now "resumed").
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
     	// Called before making the activity vulnerable to destruction; save your activity state in outState.
         // UI elements states are saved automatically by super.onSaveInstanceState()
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         // Another activity is taking focus (this activity is about to be "paused"); commit unsaved changes to persistent data, etc.
         // -> onStop()
     }
 
     @Override
     protected void onStop() {
         super.onStop();
         // The activity is no longer visible (it is now "stopped")
     }
 
     @Override
     protected void onRestart() {
         super.onRestart();
         // The activity was stopped, and is about to be started again. It was not destroyed, so all members are intact.
         // -> onStart()
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         // The activity is about to be destroyed.
         if (isFinishing()) {
         	// Someone called finish()
         } else {
         	// System is temporarily destroying this instance of the activity to save space
         }
     }
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public void dataReceived(Method methodCalled, Object data) {
 		if (data instanceof List) {
 			this.iAteThisButton.setVisibility(View.VISIBLE);
 			this.progressBar.setVisibility(View.INVISIBLE);
 			 
 			this.food = (Food) ((List<Food>) data).get(0);
 
 	    	TextView tv = (TextView)findViewById(R.id.fromCalsTextView);
 	    	tv.setText(this.food.getCalsFromFat()+"");
 	    	tv = (TextView)findViewById(R.id.fatTextView);
 	    	tv.setText(Math.round(this.food.getFat())+"");
 	    	tv = (TextView)findViewById(R.id.carbsTextView);
 	    	tv.setText(Math.round(this.food.getCarbs())+"");
 	    	tv = (TextView)findViewById(R.id.proteinTextView);
 	    	tv.setText(Math.round(this.food.getProtein())+"");
 		} else {
 			DataHelper.getFood(food.getFoodId(), this);
 		}
 	}
 }
