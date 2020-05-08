 package ca.dreamteam.newrecipebook;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.text.Html;
 import android.view.Menu;
 import android.view.View;
 import android.widget.TextView;
 import ca.dreamteam.newrecipebook.Helpers.RecipeSQLite;
 import ca.dreamteam.newrecipebook.Helpers.RecipeSerialization;
 import ca.dreamteam.newrecipebook.Helpers.ElasticSearch.ESClient;
 import ca.dreamteam.newrecipebook.Models.Recipe;
 
 /**
  * Allows the user to view the recipe
  * 
  * @version RecipeBook Project 4
  * @author Conner Bilec, David James, Steve Eckert and Maciej Ogrocki
  * @date Monday 01 April 2013
  */
 
 public class RecipeViewActivity extends Activity {
     /**
      * @var recipe keeps track of the current recipe and serializes it
      */
     private RecipeSQLite recipeCache = new RecipeSQLite(this);
     private RecipeSerialization recipeSerial = RecipeSerialization.getInstance(this);
     private Recipe recipe = null;
 
     @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
     @Override
 
     /**
      * creates the view and serializes the recipe
      * 	
      * @param savedInstanceState
      */
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_recipe_view);
 
         recipe = (Recipe)getIntent().getSerializableExtra("recipe");
 
         this.recipe = (Recipe)getIntent().getSerializableExtra("recipe");
         
         recipe = recipeSerial.readFile(recipe.getId());
 
         ((TextView)findViewById(R.id.recipeName)).setText(recipe.getName());
         ((TextView)findViewById(R.id.recipeAuthor)).setText(recipe.getAuthor());
         ((TextView)findViewById(R.id.instructions)).setText(recipe.getInstructions());
         
         String ingredientsString = "";
         for (String s : recipe.getIngredients())
         {
            ingredientString = ingredientsString.append(s + "\n");
         }
         ((TextView)findViewById(R.id.ingredientsList)).setText(ingredientsString);
 
 
 
 
         //TODO For Maciej: Make sure to set EVERYTHING to uneditable when viewing. We could pass a bool around or something.
     }
     /**
      * Loads options menu
      * 
      * @param menu
      * @return true
      */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_recipe_view, menu);
         return true;
     }
     /**
      * Allows the user to share the recipe to other users using email
      * 
      * @param view
      */
     public void share(View view)
     {
         Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
         emailIntent.setType("plain/text");
         emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "A friend has recommended a recipe for you!");
         emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, Html.fromHtml(recipe.toString()));
         startActivity(Intent.createChooser(emailIntent, "Share a recipe..."));
     }	
 
     /**
      * opens the database when page is resumed 
      */
     @Override
     public void onResume()
     {
         recipeCache.open();
         super.onResume();
     }
     /**
      * Closes the database when the use leaves the page
      */
     @Override
     public void onPause()
     {
         recipeCache.close();
         super.onPause();
     }
 
     /**
      * See name
      * 
      * @param v
      */
     
     public void DeleteRecipe(View v){
         try
         {
             ESClient.getInstance().deleteRecipe(this.recipe);
         }
         catch (Exception e)
         {
             e.printStackTrace();
         }
         recipeSerial.deleteFile(recipe.getId());
         recipeCache.deleteRecipe(recipe);
         super.finish();
     }
 }
