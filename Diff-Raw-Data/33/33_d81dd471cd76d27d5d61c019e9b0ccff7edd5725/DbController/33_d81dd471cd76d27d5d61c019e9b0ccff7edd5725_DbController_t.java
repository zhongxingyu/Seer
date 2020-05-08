 package ca.ualberta.cmput301w13t11.FoodBook.controller;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.util.Log;
 import ca.ualberta.cmput301w13t11.FoodBook.model.DbManager;
 import ca.ualberta.cmput301w13t11.FoodBook.model.FView;
 import ca.ualberta.cmput301w13t11.FoodBook.model.Ingredient;
 import ca.ualberta.cmput301w13t11.FoodBook.model.IngredientsDbManager;
 import ca.ualberta.cmput301w13t11.FoodBook.model.Photo;
 import ca.ualberta.cmput301w13t11.FoodBook.model.Recipe;
 import ca.ualberta.cmput301w13t11.FoodBook.model.RecipesDbManager;
 import ca.ualberta.cmput301w13t11.FoodBook.model.ResultsDbManager;
 
 /**
  * Controller for the DbManager.
  * @author Mark Tupala, Marko Babic
  *
  */
 public class DbController {
         
     // name of tables
     protected String UserRecipes = "UserRecipes";
     protected String ResultsRecipes = "ResultsRecipes";
     protected String UserIngredients = "UserIngredients";
     
     // SQL queries
     private String getUserRecipesSQL = "SELECT * FROM " + UserRecipes;
     
     private String getResultRecipesSQL = "SELECT * FROM " + ResultsRecipes;
     private String getUserIngredientsSQL = "SELECT * FROM " + UserIngredients;
     
     private static DbManager db;
     private static RecipesDbManager recipesManager;
     private static ResultsDbManager resultsManager;
     private static IngredientsDbManager ingredsManager;
      
         
     // singleton pattern implementation
     private static DbController instance = null;
         
     /* 
      * private constructor because we're using the singleton pattern.
      */
     private DbController(Context context) {
     	db = DbManager.getInstance(context);
     	recipesManager = RecipesDbManager.getInstance(context);
     	ingredsManager = IngredientsDbManager.getInstance(context);
    	resultsManager = ResultsDbManager.getInstance(context);
     }
         
     /**
      * Get instance of the singleton DbController.
      * @return The instance of the class.
      */
     public static DbController getInstance(Context context, FView<DbManager> view) {
         if (instance == null) {
                 // db instance doesn't exist, create new one
                 instance = new DbController(context);
         }
         db.addView(view);
         return instance;
     }
 
     /**
      * delete view from model's array of views
      * @return void
      */
     public void deleteView(FView<DbManager> view) {
         db.deleteView(view);
     }
     
     /* *****************************************************************
     USE THESE METHODS FOR ADDING, EDITING, RETRIEVING, AND DELETING
     THE USER'S PERSONAL RECIPES
    ********************************************************************* */
     
     /**
      * @return Returns an ArrayList containing all the Recipes the user has stored on their device.
      */
     public ArrayList<Recipe> getUserRecipes() {
     	return db.getRecipes(getUserRecipesSQL);
     }
     /**
      * @return Returns a Recipe the user has stored on their device, given recipe uri
      */
     public Recipe getUserRecipe(long uri) {
     	String query = new String("SELECT * FROM " + UserRecipes + " WHERE URI = " + uri);
     	return db.getRecipe(query);
     }
 
     /**
      * Adds the given Recipe to the database.
      * @param recipe The recipe to add.
      */
     public void addRecipe(Recipe recipe) {
     	recipesManager.insertRecipe(recipe, "UserRecipes");
     	db.notifyViews();
     }
 
     /**
      * Deletes the given Recipe from the database.
      * @param recipe The recipe to delete.
      */
     public boolean deleteRecipe(Recipe recipe)
     {	
     	
     	//Adjusted this so recipes could be deleted
     	//recipesManager.deleteRecipe(recipe);
     	//db.notifyViews();
     	Log.d("are we here as well", "are we here as well");
     	boolean success = db.removeRecipe(recipe);
     	db.notifyViews();
     	return success;
     	
     }
 
     /**
      * Associates the Ingredient argument correctly with the given Recipe in the database.
      * @param ingredient The ingredient to add to the given Recipe.
      * @param recipe The Recipe to which the ingredient is to be added.
      */
     public void addIngredientToRecipe(Ingredient ingredient, Recipe recipe)
     {
     	db.insertRecipeIngredients(ingredient, recipe.getUri());
     	db.notifyViews();
     }
    
     
     /* *****************************************************************
     USE THESE METHODS FOR RETRIEVING/SAVING/DELETING PHOTOS TO RECIPES
    ********************************************************************* */
     /**
      * Associates the Photo argument correctly with the given Recipe in the database.
      * @param photo The photo to add to the given Recipe.
      * @param recipe The Recipe to which the photo is to be added.
      */
     public void addPhotoToRecipe(Photo photo, long uri)
     {
     	db.insertRecipePhotos(photo, uri);
     	db.notifyViews();
     }
     /**
      * @return Returns an ArrayList of Photos for a given recipe
      */
     public ArrayList<Photo> getRecipePhotos(long uri) {
     	return db.getRecipePhotos(uri);
     }
     
     public boolean deleteRecipePhoto(Photo photo)
     {
     	boolean success = db.removeRecipePhoto(photo);
     	db.notifyViews();
     	return success;
     }
 
     /* *****************************************************************
     USE THESE METHODS FOR RETRIEVING/SAVING RECIPES STORED FROM SEARCH
    ********************************************************************* */
     
     /**
      * @return Returns an ArrayList containing all the Recipes stored from search
      */
     public ArrayList<Recipe> getStoredRecipes() {
    	return resultsManager.getRecipes(getResultRecipesSQL);
     }
     
     /* *****************************************************************
          USE THESE METHODS FOR ADDING, EDITING, RETRIEVING, AND DELETING
          THE USER'S PERSONAL INGREDIENTS
      ********************************************************************* */
 
     /**
      * Adds the given Ingredient to the database.
      * @param ingredient The Ingredient to add.
      */
     public void addIngredient(Ingredient ingredient) {
     	ingredsManager.insert(ingredient);
     	db.notifyViews();
     }
 
     /**
      * @return Returns an ArrayList containing all the Ingredients the user has stored on their device.
      */
     public ArrayList<Ingredient> getUserIngredients() {
     	return ingredsManager.get();
     }
 
     /**
      * Deletes the given Ingredient from the database.
      * @param ingredient The Ingredient to delete.
      */
     public void deleteIngredient(Ingredient ingred) {
     	ingredsManager.delete(ingred);
     	db.notifyViews();
     }
 }
