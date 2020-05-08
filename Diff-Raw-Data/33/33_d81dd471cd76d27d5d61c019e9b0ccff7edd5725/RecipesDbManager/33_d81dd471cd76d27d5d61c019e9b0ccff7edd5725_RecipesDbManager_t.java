 package ca.ualberta.cmput301w13t11.FoodBook.model;
 
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
 	protected String recipesTable = "UserRecipes";
	protected String ingredsTable = "RecipeIngredients";
	protected String photosTable = "RecipePhotos";
 	private String getUserRecipesSQL = "SELECT * FROM " + recipesTable;
 
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
