 package com.github.cmput301w13t04.food.view;
 
 import java.io.File;
 
 import com.github.cmput301w13t04.food.R;
 import com.github.cmput301w13t04.food.model.Ingredient;
 import com.github.cmput301w13t04.food.model.Photo;
 
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.app.Activity;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 /**
  * Gets input from the user and saves it to the cache.
  * 
  * @author W13T04
  * 
  */
 public class ActivityManageIngredient extends Activity {
 
 	public static final int RESULT_DELETE = 10;
 
 	private static final int TAKE_PHOTO = 1;
 	private Photo p;
 
 	private int position;
 	private Ingredient ingredient;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_manage_ingredient);
 		// Get Ingredient ID
 		position = getIntent().getIntExtra("POSITION", -1);
 
 		if (position != -1) {
 			// Modify Existing Ingredient
 			ingredient = getIntent().getParcelableExtra("INGREDIENT");
 
 			if (ingredient.getPhoto() != null) {
 				ImageView photoView = (ImageView) findViewById(R.id.ingredient_image);
 				photoView.setImageURI(Uri
 						.parse(ingredient.getPhoto().getPath()));
 			}
 
 			EditText quantity = (EditText) findViewById(R.id.add_quantity);
 			quantity.setText(String.valueOf(ingredient.getQuantity()));
 
 			EditText name = (EditText) findViewById(R.id.add_name);
 			name.setText(ingredient.getName());
 
 			EditText description = (EditText) findViewById(R.id.add_description);
 			description.setText(ingredient.getDescription());
 		} else {
 			// New Ingredient
 			ingredient = new Ingredient();
 		}
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.add_ingredient, menu);
 		return true;
 	}
 
 	public void removeIngredient(View view) {
 		Intent result = new Intent();
 		result.putExtra("POSITION", position);
 		setResult(RESULT_DELETE, result);
 
 		Toast.makeText(view.getContext(), "Ingredient Removed!",
 				Toast.LENGTH_SHORT).show();
 
 		finish();
 	}
 
 	/**
 	 * Update the ingredient based on any new input from the user
 	 */
 	public void updateIngredient(View view) {
 		// Get Name
 		EditText name = (EditText) findViewById(R.id.add_name);
 		String nameIngredient = name.getText().toString();
 		if (nameIngredient.isEmpty()) {
 			Toast.makeText(view.getContext(), "Missing Ingredient Name!",
 					Toast.LENGTH_SHORT).show();
 			return;
 		}
 		ingredient.setName(nameIngredient);
 
 		// Get Quantity
 		EditText quantity = (EditText) findViewById(R.id.add_quantity);
 		String quantityIngredient = quantity.getText().toString();
 
 		if (quantityIngredient.isEmpty()) {
 			Toast.makeText(view.getContext(), "Missing Ingredient Quantity!",
 					Toast.LENGTH_SHORT).show();
 			return;
 		}
 		ingredient.setQuantity(quantityIngredient);
 
 		// Get Description
 		EditText description = (EditText) findViewById(R.id.add_description);
 		String descriptionIngredient = description.getText().toString();
 		ingredient.setDescription(descriptionIngredient);
 
 		// Set Picture
 		if (p != null) {
 			if (ingredient.getPhoto() != null) {
 
 				Photo pOld = ingredient.getPhoto();
 				File file = new File(pOld.getAbsolutePath());
 
 				if (file.exists())
 					file.delete();
 
 				// clear device's cache
 				sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
 						Uri.parse("file://"
 								+ Environment.getExternalStorageDirectory())));
 			}
 			ingredient.setPhoto(p);
 		}
 
 		Intent result = new Intent();
 		result.putExtra("INGREDIENT", ingredient);
 		result.putExtra("POSITION", position);
 		setResult(Activity.RESULT_OK, result);
 
 		Toast.makeText(view.getContext(), "Added " + nameIngredient + "!",
 				Toast.LENGTH_SHORT).show();
 
 		finish();
 	}
 
 	/**
 	 * The button listener for starting the photo-taking intent
 	 */
 	public void takePhoto(View view) {
 		Intent intent = new Intent(this, ActivityTakePhoto.class);
 		startActivityForResult(intent, TAKE_PHOTO);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == TAKE_PHOTO) {
 			if (resultCode == RESULT_OK) {
 				String path = data.getStringExtra("path");
 				String desc = data.getStringExtra("desc");
 				// get User instead of null
 				p = new Photo(path, desc, null);
 
 				ImageView photoView = (ImageView) findViewById(R.id.ingredient_image);
 				if (photoView == null)
 					Log.d("Path", "NULL");
 
 				if (p != null) {
 					Log.d("Photo_URI", p.getPath().toString());
 					photoView.setImageURI(Uri.parse(p.getPath()));
 				}
 			}
 		}
 	}
 }
