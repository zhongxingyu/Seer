 package ca.ualberta.cmput301w13t11.FoodBook.model;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 /**
  * Implements the functionality required to manage the Results database, which
  * stores the results of the latest search query.  Mostly, it will keep track of views
  * which are observing the Results database.
  * <<Singleton>>
  * @author Mark Tupala, Marko Babic
  */
 public class ResultsDbManager extends DbManager {
 	
 	private static ResultsDbManager instance = null;
 	protected String resultsTable = "ResultsRecipes";
 	private String getSQL = "SELECT * FROM " + resultsTable;
 	
 	public ResultsDbManager(Context context)
 	{
 		super(context);
 	}
 
 	/**
 	 * Gets the instance of the IngredientsDbManager -- or creates it, if necessary.
 	 * @param context Context needed to execute database creation statements (if necessary).
 	 * @return The instance of IngredientsDbManager.
 	 */
 	public static ResultsDbManager getInstance(Context context)
 	{
 		// call super to create Db if necessary
 		DbManager.getInstance(context);
 		if (instance == null)
 			instance = new ResultsDbManager(context);
 		return instance;
 	}
 	
 	/**
 	 * @return The instance of the RecipesDbManager.
 	 */
 	public static ResultsDbManager getInstance()
 	{
 		return instance;
 	}
 
 	/**
 	 * store results from server.
 	 * @return should i return boolean for success?
 	 */
 	public void storeRecipes(ArrayList<Recipe> recipes) {
 	    db.delete(resultsTable, null, null);
 	    for (Recipe recipe : recipes) {
 	        insertRecipe(recipe, resultsTable);
 	    }
 	    notifyViews();
 	}
 	
 }
