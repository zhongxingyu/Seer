 /* EditRecipeAcivity
  * 
  * Last Edited: March 7, 2013
  * 
  * 
  */
 
 package ca.ualberta.team2recipefinder.views;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 import ca.ualberta.team2recipefinder.R;
 import ca.ualberta.team2recipefinder.R.id;
 import ca.ualberta.team2recipefinder.R.layout;
 import ca.ualberta.team2recipefinder.R.string;
 import ca.ualberta.team2recipefinder.controller.Controller;
 import ca.ualberta.team2recipefinder.controller.RecipeFinderApplication;
 import ca.ualberta.team2recipefinder.controller.SearchResult;
 import ca.ualberta.team2recipefinder.model.DuplicateIngredientException;
 import ca.ualberta.team2recipefinder.model.Ingredient;
 import ca.ualberta.team2recipefinder.model.Recipe;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.Drawable;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * EditRecipeActivity is an android activity for editting a single recipe, 
  * or creating a new one.
  * 
  * @author cmput-301 team 2
  * @see ca.ualberta.team2recipefinder.model.Recipe
  */
 public class EditRecipeActivity extends Activity implements ca.ualberta.team2recipefinder.views.View<Recipe> {
 
 	EditText nameEdit;
 	EditText procedureEdit;
 	Recipe currentRecipe = new Recipe();
 	long recipeID = -1;
 	ListView ingredientList, commentList;
 	int imageIndex = 0;
 	Uri imageFileUri;
 	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
 	
 	private int source = -1;
 	private String serverId = "";
 	
 	Ingredient oldIngredient;
 	
 	Controller c;
 	
 	private final int ADD_INGR_CODE = 0;
 	private final int EDIT_INGR_CODE = 1;
 	
 	private final int ADD_COMMENT_CODE = 2;
 	
 	/**
 	 * Sets up all button listener's for this activity and gets Strings from this
 	 * activity's EditText fields.
 	 * 
 	 * @author cmput-301 team 2
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_edit_recipe);
 		
 		nameEdit = (EditText) findViewById(R.id.edit_name);
 		procedureEdit = (EditText) findViewById(R.id.edit_procedure);
 		ingredientList = (ListView) findViewById(R.id.ingredient_list);
 		commentList = (ListView) findViewById(R.id.comments_list);
 		c = RecipeFinderApplication.getController();
 		
 		
 		if (savedInstanceState == null) {
 			Bundle extras = getIntent().getExtras();
 			if (extras != null) {
 				source = extras.getInt("source");
 				
 				if (source == SearchResult.SOURCE_LOCAL) {
 					recipeID = extras.getLong("recipeID");
 					currentRecipe = c.getRecipe(recipeID);
 				}
 				else {
 					serverId = extras.getString("serverID");
 					
 					AsyncTask<Void, Void, Recipe> task = (new AsyncTask<Void, Void, Recipe>() {
 						@Override
 						protected Recipe doInBackground(Void... arg0) {
 							try {
 								return c.downloadRecipe(serverId);
 							} catch (IOException e) {
 								e.printStackTrace();
 							}
 
 							return null;
 						}
 					}).execute();
 
 					try {
 						currentRecipe = task.get();
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					} catch (ExecutionException e) {
 						e.printStackTrace();
 						Toast.makeText(EditRecipeActivity.this, getString(R.string.no_connection), 
 							   Toast.LENGTH_LONG).show();
 						finish();
 					}
 				}
 			}
 		}
 		
 		// if the recipe is on the server, only allow comments
 		// and photos
 		if (source == SearchResult.SOURCE_REMOTE) {
 			LinearLayout root = (LinearLayout) findViewById(R.id.layout_root);
 			int childcount = root.getChildCount();
 			for (int i=0; i < childcount; i++){
 			      View v = root.getChildAt(i);
 			      v.setVisibility(View.GONE);
 			}
 			
 			LinearLayout photos_layout = (LinearLayout) findViewById(R.id.photos_layout);
 			LinearLayout comments_layout = (LinearLayout) findViewById(R.id.comments_layout);
 			
 			photos_layout.setVisibility(View.VISIBLE);
 			comments_layout.setVisibility(View.VISIBLE);
 		}
 		
 		Button doneButton = (Button) findViewById(R.id.button_done);
 		doneButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				
 				boolean goodEntry = true;
 				
 				if (nameEdit.getText().toString().isEmpty() || procedureEdit.getText().toString().isEmpty()
 						|| currentRecipe.getIngredients().isEmpty()) {
 			
 					Toast.makeText(EditRecipeActivity.this, getString(R.string.missing_fields_edit), 
 							   Toast.LENGTH_LONG).show();
 					goodEntry = false;
 				}
 				
 				if (goodEntry) {
 				
 					String newName = nameEdit.getText().toString();
 					String newProcedure = procedureEdit.getText().toString();
 				
 					currentRecipe.setName(newName);
 					currentRecipe.setProcedure(newProcedure);
 				
 					Controller c = RecipeFinderApplication.getController();
 					if (recipeID == -1) {
 						c.addRecipe(currentRecipe);
 					}
 					else {
 						c.replaceRecipe(currentRecipe, recipeID);
 					}
 					
 					finish();
 				}
 			}	
 		});
 		
 		Button addIngredient = (Button) findViewById(R.id.button_add_ingredient);
 		addIngredient.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				String newName = nameEdit.getText().toString();
 				String newProcedure = procedureEdit.getText().toString();
 			
 				currentRecipe.setName(newName);
 				currentRecipe.setProcedure(newProcedure);
 				
 				Intent intent = new Intent(EditRecipeActivity.this, AddEditIngredientActivity.class);
 				intent.putExtra("mode", "add");
 				startActivityForResult(intent, ADD_INGR_CODE);
 			}
 		});
 		
 		ingredientList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             public void onItemClick(AdapterView parent, View v, int position, long id) {
                 Ingredient ingredient = (Ingredient)ingredientList.getItemAtPosition(position);
                 
                 oldIngredient = ingredient;
                 
 				Intent intent = new Intent(EditRecipeActivity.this, AddEditIngredientActivity.class);
 				intent.putExtra("mode", "edit");
 				intent.putExtra("type", ingredient.getType());
 				intent.putExtra("amount", ingredient.getAmount().toString());
 				intent.putExtra("unit", ingredient.getUnit());
 				startActivityForResult(intent, EDIT_INGR_CODE);
             }
         });
 		
 		Button prevPhoto = (Button) findViewById(R.id.button_edit_prevphoto);
 		prevPhoto.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				if (imageIndex > 0) {
 					imageIndex--;
 				}
 				else {
 					imageIndex = 0;
 				}
 				update(currentRecipe);
 			}
 		});
 		
 		Button nextPhoto = (Button) findViewById(R.id.button_edit_nextphoto);
 		nextPhoto.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				if (imageIndex < (currentRecipe.getAllPhotos().size()-1)) {
 					imageIndex++;
 				}
 				else {
 					imageIndex = currentRecipe.getAllPhotos().size()-1;
 				}
 				update(currentRecipe);
 			}
 		});
 		
 		Button addPhoto = (Button) findViewById(R.id.button_edit_addphoto);
 		addPhoto.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				String newName = nameEdit.getText().toString();
 				String newProcedure = procedureEdit.getText().toString();
 			
 				currentRecipe.setName(newName);
 				currentRecipe.setProcedure(newProcedure);
 				
 				addPhoto();
 			}
 		});
 		
 		Button removePhoto = (Button) findViewById(R.id.button_edit_removephoto);
 		
 		// you can not remove recipes that are on the server
 		if (source == SearchResult.SOURCE_REMOTE) {
 			removePhoto.setEnabled(false);
 		}
 		
 		removePhoto.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				removePhoto();
 			}
 		});
 		
 		Button addComments = (Button) findViewById(R.id.button_add_comments);
 		addComments.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				String newName = nameEdit.getText().toString();
 				String newProcedure = procedureEdit.getText().toString();
 			
 				currentRecipe.setName(newName);
 				currentRecipe.setProcedure(newProcedure);
 				
 				Intent intent = new Intent(EditRecipeActivity.this, AddEditCommentsActivity.class);
 				intent.putExtra("mode", "add");
 				startActivityForResult(intent, ADD_COMMENT_CODE);
 			}
		});		
 		
 		currentRecipe.addView(this);
 		this.update(currentRecipe);
 	}
 	
 	/**
 	 * Removes this View from Model.
 	 */
     @Override
     public void onDestroy() {
         super.onDestroy();
         currentRecipe.removeView(this);
     }
 	
     /**
      * Updates all fields with current information from the recipe object.
      */
 	@Override
 	public void update(Recipe model) {		
 		nameEdit.setText(currentRecipe.getName());
 		procedureEdit.setText(currentRecipe.getProcedure());
 		
 		List<Ingredient> ingredients = currentRecipe.getIngredients();
 		final ArrayAdapter<Ingredient> adapter = new ArrayAdapter<Ingredient>(this, R.layout.list_item, ingredients);
 		ingredientList.setAdapter(adapter);
 		
 		ImageView pictureBox = (ImageView) findViewById(R.id.imageView_editscreen);
 		Bitmap image = currentRecipe.getPhoto(imageIndex);
 		if (image != null) {
 			pictureBox.setImageBitmap(image);
 		}
 		
 		TextView imageInfo = (TextView) findViewById(R.id.photo_number_text);
 		String info;
 		if (currentRecipe.hasPhotos()) {
 			info = (imageIndex+1)+"/"+currentRecipe.getAllPhotos().size();
 		} else {
 			info = "No photos";
 		}
 		imageInfo.setText(info);
 		
 		ListView commentsList = (ListView) findViewById(R.id.comments_list);
 		List<String> comments = currentRecipe.getAllComments();
 		final ArrayAdapter<String> adapter_comments = new ArrayAdapter<String>(this, R.layout.list_item, comments);
 		commentsList.setAdapter(adapter_comments);
 	}
 	
 	/**
 	 * Takes the result from AddEditIngredientActivity and adds it to, or 
 	 * replaces an item from this recipe's ingredient list.
 	 */
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode,
             Intent data) {
         if (requestCode == ADD_INGR_CODE) {
             if (resultCode == RESULT_OK) {
             	// add ingredient
             	Ingredient ingredient = (Ingredient) data.getSerializableExtra("result");
             	
             	try {
             		RecipeFinderApplication.getController().addIngredient(currentRecipe, ingredient);
 				} catch (DuplicateIngredientException e) {
 					Toast.makeText(EditRecipeActivity.this, getString(R.string.have_ingredient_already_recipe), 
 							   Toast.LENGTH_LONG).show();
 				}            	
             }
         }
         else if (requestCode == EDIT_INGR_CODE) {
             if (resultCode == RESULT_OK) {
             	if (data.getStringExtra("deleted") != null) {
             		// delete ingredient
             		RecipeFinderApplication.getController().deleteIngredient(currentRecipe, oldIngredient);
             	}
             	else {
             		// edit ingredient
 	            	Ingredient ingredient = (Ingredient) data.getSerializableExtra("result");
 	            	RecipeFinderApplication.getController().replaceIngredient(currentRecipe, oldIngredient, ingredient);
             	}
             }
         }
 		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
 			if (resultCode == RESULT_OK) {
 				Bitmap image = BitmapFactory.decodeFile(imageFileUri.getPath());
 				
 				// delete temporary file in order to save space
 				File file = new File(imageFileUri.getPath());
 				file.delete();
 				
 				if (source != SearchResult.SOURCE_REMOTE) {
 					currentRecipe.addPhoto(image);
 				}
 				else {
 					AsyncTask<Bitmap, Void, Void> task = (new AsyncTask<Bitmap, Void, Void>() {
 						@Override
 						protected Void doInBackground(Bitmap... arg0) {
 							try {
 								c.postPicture(serverId, arg0[0]);
 								currentRecipe.addPhoto(arg0[0]);
 								Intent intent = new Intent();
 								setResult(RESULT_OK, intent);
 							} catch (IOException e) {
 								e.printStackTrace();
 							}
 
 							return null;
 						}
 					}).execute(image);
 
 					try {
 						task.get();
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					} catch (ExecutionException e) {
 						e.printStackTrace();
 						Toast.makeText(EditRecipeActivity.this, getString(R.string.no_connection), 
 							   Toast.LENGTH_LONG).show();
 					}
 				}
 				update(currentRecipe);
 			} 
 		}
 		if (requestCode == ADD_COMMENT_CODE) {
 			if (resultCode == RESULT_OK) {
 				String comment = data.getStringExtra("result");
 				
 				if (source != SearchResult.SOURCE_REMOTE) {
 					currentRecipe.addComment(comment);
 				}
 				else {
 					AsyncTask<String, Void, Void> task = (new AsyncTask<String, Void, Void>() {
 						@Override
 						protected Void doInBackground(String... arg0) {
 							try {
 								c.postComment(serverId, arg0[0]);
 								currentRecipe.addComment(arg0[0]);
 								Intent intent = new Intent();
 								setResult(RESULT_OK, intent);
 							} catch (IOException e) {
 								e.printStackTrace();
 							}
 
 							return null;
 						}
 					}).execute(comment);
 
 					try {
 						task.get();
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					} catch (ExecutionException e) {
 						e.printStackTrace();
 						Toast.makeText(EditRecipeActivity.this, getString(R.string.no_connection), 
 							   Toast.LENGTH_LONG).show();
 					}
 				}
 				
 				update(currentRecipe);
 			} 
 		}
 	}   
 
 	public void addPhoto() {
 		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 
 		String folder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/tmp";
 		File folderF = new File(folder);
 		if (!folderF.exists()) {
 			folderF.mkdir();
 		}
 
 		String imageFilePath = folder + "/" + String.valueOf(System.currentTimeMillis()) + ".jpg";
 		File imageFile = new File(imageFilePath);
 		imageFileUri = Uri.fromFile(imageFile);
 
 		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);
 		startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
 	}
 	
 	public void removePhoto() {
 		AlertDialog.Builder adb = new AlertDialog.Builder(this);
 		adb.setTitle("Confirm");
 		adb.setMessage("Are you sure you want to delete the currently displayed photo?");
 		adb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				currentRecipe.removePhoto(imageIndex);
 				dialog.cancel();
 				update(currentRecipe);
 			}
 		});
 		adb.setNegativeButton("No", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.cancel();
 			}
 		});
 
 		AlertDialog ad = adb.create();
 		ad.show();
 	}
 }
