 package ca.ualberta.cmput301w13t11.FoodBook.model;
 
 import java.io.File;
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.util.Log;
 
 /**
  * Implements the functionality required to manage the table of Recipes for the application.
  * @author Marko Babic, Marko Tupala
  *
  *------------------------------------------------------------------------------------------------------------
  * NOTE: REFACTORTING IS NOT COMPLETE AS OF THIS ITERATION.  IN THE FUTURE, MORE FUNCTIONALITY FROM DbManager
  * WILL APPEAR IN THIS CLASS.
  *------------------------------------------------------------------------------------------------------------
  */
 public class RecipesDbManager extends DbManager {
 	
 	private static RecipesDbManager instance = null;
	public String recipesTable = "UserRecipes";
	public String ingredsTable = "RecipeIngredients";
	public String photosTable = "RecipePhotos";
	public String getSQL = "SELECT * FROM " + recipesTable;
 
 	public RecipesDbManager(Context context)
 	{
 		super(context);
 	}
 	
 	/**
 	 * 
 	 * @param context Context needed to execute database creation statements (if necessary).
 	 * @return The instance of RecipesDbManager.
 	 */
 	public static RecipesDbManager getInstance(Context context)
 	{
 		// call super to create Db if necessary
 		DbManager.getInstance(context);
 		if (instance == null)
 			instance = new RecipesDbManager(context);
 		return instance;
 	}
 	
 	/**
 	 * 
 	 * @return The instance of the RecipesDbManager.
 	 */
 	public static RecipesDbManager getInstance()
 	{
 		return instance;
 	}
 
 	/**
 	 * Deletes the given Recipe from the database.
 	 * @param recipe The Recipe to be deleted.
 	 */
     
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
 	
 	//something's going here! Im bypassing this in dbController -pablo
 	/*
 	public void deleteRecipe(Recipe recipe) {
 		try{
 		//String s = Long.toString(recipe.getUri());
 		//Log.d("uri in String", s);
 	    int r = db.delete("UserRecipes", "URI = " + recipe.getUri(), null);
 	    //s = Integer.toString(r);
 	    //Log.d("results", s);
 		}catch(Exception e){e.printStackTrace();};
 	}
 	*/
 }
