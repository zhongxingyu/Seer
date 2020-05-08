 package ca.ualberta.cmput301w13t11.FoodBook.model;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 /**
  * Singleton class that manages the application's database.
  * @author Mark Tupala
  *
  */
 public class DbManager extends FModel<FView> {
 
 	
     // the actual database 
     protected static SQLiteDatabase db;
     protected static DbOpenHelper dbHelper;
     
     // singleton pattern implementation
     private static DbManager instance = null;
     
     //Used to ensure that multiple connections to the database are not opened and
     //also to ensure that multiple database helpers do not open.
     private static boolean has_executed = false;
     
     // name of database file
     private String dbFileName = "RecipeApplicationDb";
     
    // overwritten by subclasses
    protected String ingredsTable;
    protected String photosTable;
    
 
     /**
      * Protected constructor because we're using the singleton pattern.
      */
     protected DbManager(Context context) {
     	super();
     	// open or create the sqlite db accordingly
     	if (DbManager.has_executed == false) {
     		// Use the has_executed flag to ensure we do not open
     		// multiple connections to the database and we do not
     		// create multiple dbHelper objects.
     		dbHelper = new DbOpenHelper(context, dbFileName);
     		db = dbHelper.getWritableDatabase();
     		DbManager.has_executed = true;
     	}
     }
     
     /**
      * @return True if DbManager constructor has been called before, false if not
      */
     public static boolean getHasExecuted()
     {
     	return has_executed;
     }
     
     /**
      * Get instance of the singleton DbManager.
      * @return The instance of the class.
      */
     public static DbManager getInstance(Context context) {
     	if (instance == null) {
     		// db instance doesn't exist, create new one
     		instance = new DbManager(context);
     	}	
     	return instance;
     }
 
     /**
      * Get instance of the singleton DbManager, when we
      * know that it exists
      * @return The instance of the class.
      */
     public static DbManager getInstance() {	
     	return instance;
     }
     
 	/**
 	 * Inserts a recipe into the table.
 	 * @param recipe The Recipe to be stor2 saves pics to ed in the table.
 	 * @param The name of the table into which the recipe is to be stored.
 	 */
 	public void insertRecipe(Recipe recipe, String tableName) {
 	    ContentValues values = recipe.toContentValues();
 	    db.insert(tableName, null, values);
 	    for (Ingredient ingred : recipe.getIngredients()) {
 	        insertRecipeIngredients(ingred, recipe.getUri());
 	    }
 	    // Pictures get stored one at a time once recipe is in database so we don' need this -Pablo
 	    for (Photo photo : recipe.getPhotos()) {
 	        insertRecipePhotos(photo, recipe.getUri());
 	    }
 	}
     
     /**
      * Inserts the given Ingredient into the database such that it is associated with the
      * recipe identified by recipeURI.
      * @param ingred The ingredient to be 2 saves pics to inserted.
      * @param recipeURI The URI of the Recipe with which to associate the Ingredient.
      */
     public void insertRecipeIngredients(Ingredient ingred, long recipeURI) {
         ContentValues values = ingred.toContentValues();
         values.put("recipeURI", recipeURI);
        db.insert(this.ingredsTable, null, values);
     }
    
     /**
      * Inserts the given Photo into the database such that it is associated with the
      * recipe identified by recipeURI.
      * @param photo The photo to be inserted.
      * @param recipeURI The URI of the Recipe with which to associate the Photo.
      */
     public void insertRecipePhotos(Photo photo, long recipeURI) {
     	ContentValues values = new ContentValues();
     	values.put("recipeURI", recipeURI);
     	values.put("id", photo.getId());
     	values.put("path", photo.getPath());
    	db.insert(this.photosTable, null, values);
     }
 
     /**
      * Returns an ArrayList of all the Recipes stored in the table.
 	 * @return An ArrayList of all the Recipes stored in the table.
 	 */
 	public ArrayList<Recipe> getRecipes(String query) {
 	    Cursor cursor = db.rawQuery(query, null);
 	    return cursorToRecipes(cursor);
 	}
 	// Added this so we can fetch a recipe with its uri. -Pablo
 	/**
 	 * Returns Recipe stored in the table, given recipe's uri
 	 * @return ARecipes stored in the table.
 	 */
 	public Recipe getRecipe(String query) {
 	    Cursor cursor = db.rawQuery(query, null);
 	    return cursorToRecipe(cursor);
 	}
 	//---
     
     /**
      * Given a cursor, convert it to an ArrayList of Recipes.
      * @param cursor The cursor over which we will iterate to get recipes from.
      * @return An ArrayList of Recipes.
      */
     protected ArrayList<Recipe> cursorToRecipes(Cursor cursor) {
         ArrayList<Recipe> recipes = new ArrayList<Recipe>();
         cursor.moveToFirst();
         while (!cursor.isAfterLast()) {
             long uri = cursor.getLong(0);
             User author = new User(cursor.getString(2));
             String title = cursor.getString(1);
             String instructions = cursor.getString(3);
             ArrayList<Ingredient> ingredients = getRecipeIngredients(uri);
             ArrayList<Photo> photos = getRecipePhotos(uri);
             Recipe recipe = new Recipe(uri, author, title, instructions, ingredients, photos);
             recipes.add(recipe);
             cursor.moveToNext();
         }
         return recipes;
     }
     // This is the aux.function to getUserRecipe -Pablo
     /**
     * Given a cursor, convert it to an ArrayList of Recipes.
     * @param cursor The cursor over which we will iterate to get recipes from.
     * @return An ArrayList of Recipes.
     */
    protected Recipe cursorToRecipe(Cursor cursor) {
  
        cursor.moveToFirst();
 
        long uri = cursor.getLong(0);
        User author = new User(cursor.getString(2));
        String title = cursor.getString(1);
        String instructions = cursor.getString(3);
        ArrayList<Ingredient> ingredients = getRecipeIngredients(uri);
        ArrayList<Photo> photos = getRecipePhotos(uri);
        Recipe recipe = new Recipe(uri, author, title, instructions, ingredients, photos);
 
        if (cursor.getCount()!=0) {
     	   //print error message here
        }
        return recipe;
    }
    // --- 
     
     
     /**
      * Gets all the Ingredients associated with the recipe identified by its URI.
      * @param uri The URI of the recipe whose ingredients we are fetching.
      * @return An ArrayList of the Ingredients associated with the recipe.
      */
     protected ArrayList<Ingredient> getRecipeIngredients(long uri) {
     	Cursor cursor = db.rawQuery("Select * From RecipeIngredients Where recipeURI = " + uri, null);
     	return cursorToIngredients(cursor);
     }
     
     /**
      * Gets all the Photos associated with the recipe identified by its URI.
      * @param uri The URI of the recipe whose photos we are fetching.
      * @return An ArrayList of the Photos associated with the recipe.
      */
     // Changed to public so that we can retrieve just photos for galleries -Pablo
     //protected ArrayList<Photo> getRecipePhotos(long uri) {
     public ArrayList<Photo> getRecipePhotos(long uri) {
     	Cursor cursor = db.rawQuery("Select * From RecipePhotos Where recipeURI = " + uri, null);
     	return cursorToPhotos(cursor);
     }
     
     //method to delete photo -Pablo
     public boolean removeRecipePhoto(Photo photo) {
     	//String createStatement = 
     	
     	//String.format("Delete From RecipePhotos Where recipeUri = %S and filename = %S", uri, photo.getName()); 
     	int success = db.delete("RecipePhotos", "id = " + photo.getId(), null); 
     	//Log.d("int", Integer.toString(r));
     	Boolean deleted = false;
     	
     		try{
 		    	File file = new File(photo.getPath());
 		        deleted = file.delete();
     		}
     		catch(Exception e){
     			e.printStackTrace();
     		}  	
     	return (success==1 && deleted==true);
     }
     
     //method to delete ingredients -Pablo
     public boolean removeRecipeIngredients(long uri) {
     	//String createStatement = 
     	
     	int success = db.delete("RecipeIngredients", "recipeURI = " + uri, null); 
     	    	
     	return (success>=1);
     }
     /**
      * Given a cursor, convert it to an ArrayList of Ingredients.
      * @param cursor The cursor over which we will iterate to get ingredients from.
      * @return An ArrayList of Ingredients.
      */
     protected ArrayList<Ingredient> cursorToIngredients(Cursor cursor) {
         ArrayList<Ingredient> ingreds = new ArrayList<Ingredient>();
         cursor.moveToFirst();
         while (!cursor.isAfterLast()) {
             String name = cursor.getString(0);
             String unit = cursor.getString(1);
             float quantity = cursor.getFloat(2);
             Ingredient ingred = new Ingredient(name, unit, quantity);
             ingreds.add(ingred);
             cursor.moveToNext();
         }
         return ingreds;
     }
     
     /**
      * Given a cursor, convert it to an ArrayList of Photos.
      * @param cursor The cursor over which we will iterate to get photos from.
      * @return An ArrayList of Photos.
      */
     protected ArrayList<Photo> cursorToPhotos(Cursor cursor) {
         ArrayList<Photo> photos = new ArrayList<Photo>();
         cursor.moveToFirst();
         while (!cursor.isAfterLast()) {
             String id = cursor.getString(1);
             String path = cursor.getString(2);
             Photo photo = new Photo(id, path);
             //Log.d("DbManager fetched: photo id", id);
             //Log.d("DbManager fetched: photo path", path);
             photos.add(photo);
             cursor.moveToNext();
         }
         return photos;
     }
 
 
     ////added this to delete a recipe -Pablo
     // must include other methods to remove ingredients and other stuff from other tables
 
     public boolean removeRecipe(Recipe recipe) {
     	
     	Long uri = recipe.getUri();
     	
     	int recipes_removed = 0;
     	boolean deleted_pictures = true;
     	boolean deleted_ingreds = true;
     	try{
     		//String s = Long.toString(recipe.getUri());
     		//Log.d("uri in String", s);
     		recipes_removed = db.delete("UserRecipes", "URI = " + recipe.getUri(), null);
     		Log.d("we got past removing recipes", "OK");
     		String s = Integer.toString(recipes_removed);
     		Log.d("recipes", s);
     		
     		ArrayList<Photo> photos = getRecipePhotos(uri); 
     		for (Photo p: photos){
     			if (removeRecipePhoto(p)!=true){
     				deleted_pictures=false;
     			}
     		}
   
     		Log.d("we got past removing photos", "OK");
     		s = new Boolean(deleted_pictures).toString();
     		Log.d("photos", s);
     		//Do the same for ingredients
   			deleted_ingreds = removeRecipeIngredients(uri);
   			
   			Log.d("we got past removing ingredients", "OK");
     		s = new Boolean(deleted_ingreds).toString();
     		Log.d("ingreds", s);
     				    		
     	}catch(Exception e){e.printStackTrace();};
 
     	return (recipes_removed==1 && deleted_pictures==true && deleted_ingreds==true);
     }
 }
 
 
 
     
