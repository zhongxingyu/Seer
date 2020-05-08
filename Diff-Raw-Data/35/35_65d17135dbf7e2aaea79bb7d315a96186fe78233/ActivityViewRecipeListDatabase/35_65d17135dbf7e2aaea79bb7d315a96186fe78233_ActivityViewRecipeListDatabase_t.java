 package com.github.cmput301w13t04.food.view;
 
 import java.util.ArrayList;
 
 import com.github.cmput301w13t04.food.R;
 import com.github.cmput301w13t04.food.controller.Database;
 import com.github.cmput301w13t04.food.controller.RecipeAdapter;
 import com.github.cmput301w13t04.food.model.Ingredient;
 import com.github.cmput301w13t04.food.model.Recipe;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.support.v4.app.NavUtils;
 
 public class ActivityViewRecipeListDatabase extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_view_recipe_list_database);
 		// Show the Up button in the action bar.
 		setupActionBar();
 
 		ArrayList<Recipe> recipes = null;
 
 		// Handle query by Username
		Database database = new Database();
 		String username = getIntent().getStringExtra("USER");
 		if (username != null) {
 			recipes = database.searchRecipeByEmail(username);
 		}
 
 		ArrayList<Ingredient> ingredients = getIntent()
 				.getParcelableArrayListExtra("INGREDIENTS");
 		if(ingredients != null) {
 			recipes = database.searchByIngredients(ingredients);
 		}
 
 		// Populate Recipe List
 		ListView list = (ListView) findViewById(R.id.recipe_list);
 		list.setAdapter(new RecipeAdapter(list.getContext(),
 				R.layout.item_recipe, recipes));
 
 		list.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> arg0, View arg1,
 					int position, long id) {
 				Intent intent = new Intent(ActivityViewRecipeListDatabase.this,
 						ActivityViewRecipe.class);
 				intent.putExtra("RECIPE_ID", id);
 				startActivity(intent);
 			}
 		});
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}.
 	 */
 	private void setupActionBar() {
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 }
