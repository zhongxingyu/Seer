 package ca.ualberta.team2recipefinder;
 
 import java.util.List;
 
 import ca.ualberta.team2recipefinder.R;
 import ca.ualberta.team2recipefinder.RecipeFinderApplication;
 import ca.ualberta.team2recipefinder.R.id;
 import ca.ualberta.team2recipefinder.R.layout;
 import ca.ualberta.team2recipefinder.R.menu;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class ViewRecipeActivity extends Activity {
 
 	long recipeID = -1;
 	Recipe currentRecipe = new Recipe();
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_view_recipe);
 		
 		final Controller c = RecipeFinderApplication.getController();
 		boolean isLocal;
 		
 		if (savedInstanceState == null) {
 			Bundle extras = getIntent().getExtras();
 			if (extras != null) {
 				recipeID = extras.getLong("recipeID");
 				currentRecipe = c.getRecipe(recipeID);
 			}
 		}
 		
 		
 		if (currentRecipe.getOnServer()) {
 			isLocal = false;
 		}
 		else {
 			isLocal = true;
 		}
 		Button publishDownloadButton = (Button) findViewById(R.id.publish_download_button);
 		if (isLocal) {
 			publishDownloadButton.setText("Publish");
 		}
 		else {
 			publishDownloadButton.setText("Download");
 		}
 		
 		publishDownloadButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				//if (isLocal) {
 				//	c.publishToServer(currentRecipe);
 				//}
 				//else {
 				//	c.addToLocalList(currentRecipe);
 				//}
 			}		
 		});
 		
 		Button shareButton = (Button) findViewById(R.id.share_button);
 		shareButton.setText("Share");
 		shareButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				/* ADD SHARING METHOD OR ACTIVITY HERE */
 			}
 		});
 		
 		Button editButton = (Button) findViewById(R.id.edit_button);
 		if (isLocal) {
 			editButton.setText("Edit");
 		}
 		else {
 			editButton.setMaxHeight(0);
 		}
 		
 		editButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				Intent intent = new Intent(ViewRecipeActivity.this, EditRecipeActivity.class);
 				intent.putExtra("recipeID", recipeID);
 				startActivity(intent);
 			}
 		});
 		
 		Button deleteButton = (Button) findViewById(R.id.delete_button);
 		if (isLocal) {
 			deleteButton.setText("Delete");
 		}
 		else {
 			deleteButton.setMaxHeight(0);
 		}
 		
 		deleteButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				/* CALL DELETE FROM CONTROLLER, EXIT ACTIVITY */
 				c.deleteRecipe(currentRecipe);				
 				finish();
 			}
 		});
 		
 		Button rightButton = (Button) findViewById(R.id.button5);
 		rightButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				currentRecipe = c.getNextRecipe(recipeID);
 				recipeID = currentRecipe.getRecipeID();
 				refresh();
 			}
 		});
 		
 		Button leftButton = (Button) findViewById(R.id.button6);
 		leftButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				currentRecipe = c.getPreviousRecipe(recipeID);
 				recipeID = currentRecipe.getRecipeID();
 				refresh();
 			}
 		});
 		
 		refresh();
 	}
 	
 	public void refresh() {
 		TextView recipeName = (TextView) findViewById(R.id.recipe_name);
 		recipeName.setText(currentRecipe.getName());
 		
 		TextView procedure = (TextView) findViewById(R.id.procedure_text);
 		String procedureText = currentRecipe.getProcedure();
 		procedure.setText(procedureText);
 		
 		TextView ingredients = (TextView) findViewById(R.id.ingredients_text);
 		List<Ingredient> ingredientTextArray = currentRecipe.getIngredients();
 		
 		String ingredientText = new String();
		String nl = System.getProperty("line.seperator");
 		for (int i = 0; i < ingredientTextArray.size(); i++) {
			ingredientText.concat(ingredientTextArray.get(i).toString()+nl);
 		}
 		ingredients.setText(ingredientText);
 	}
 	
 	@Override
 	public void onStart() {
 		super.onStart();
 		
 		refresh();
 	}
 
 }
