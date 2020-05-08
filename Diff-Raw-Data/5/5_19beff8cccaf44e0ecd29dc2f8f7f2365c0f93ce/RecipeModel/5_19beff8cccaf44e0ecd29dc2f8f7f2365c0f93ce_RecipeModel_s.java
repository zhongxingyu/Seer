 package ca.ualberta.team2recipefinder;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 
 
 public class RecipeModel
 {
 	private static final String filename = "file.sav";
 	private String path;
 	private ArrayList<Recipe> recipes; 
 	
 	/*
 	 * Constructor
 	 */
 	public RecipeModel(){
 		// gets the folder where we should put the files
 		// created by the application (and appends filename)
 		path = RecipeFinderApplication.getAppContext().getFilesDir() + filename;
 		
 		this.recipes = load();
 	}
 	
 	
 	/*
 	 * Add a new Recipe to the Recipe Array List and then write it to the phone's database
 	 */
 	   public void add(Recipe recipe) {
 		   recipes.add(recipe);
 		   sortRecipes();
 		   writeFile(recipes);
 	}
 	   /*
 	    * Remove the specified recipe
 	    */
 	   public void remove(Recipe recipe){
 		   //Searches for recipes which have equivalent attributes (name, author, procedure) as the specified recipe
 		    for(int i = 0; i<recipes.size(); i++){			   
 			   if(recipe.getRecipeID() == recipes.get(i).getRecipeID()){
 				   recipes.remove(i);
 				   break;
 			   }		   
 		   }
 		   writeFile(recipes);
 	   }
 	   
 	   /*
 	    * Write an arraylist of recipes to the phone's database
 	    */
 	   public void writeFile(ArrayList<Recipe> recipes) {  
 			try {  
 				ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path));
 				out.writeObject(recipes);
 				out.close();  
 			} catch (FileNotFoundException e) {  
 				e.printStackTrace();  
 			} catch (IOException e) {  
 				e.printStackTrace();  
 			}  
 		} 
 	   
 	 /*
 	  * Load an array of recipes from the phone's database 
 	  */	   
 	   private ArrayList<Recipe> load() {  
 		   ArrayList<Recipe> recipes = new ArrayList<Recipe>();
 		   
 		   try {  
 				ObjectInputStream in = new ObjectInputStream(new FileInputStream(path));  
 				recipes = (ArrayList<Recipe>) in.readObject();
 				
 
 			} 
 			catch (IOException e) {  
 				e.printStackTrace();  
 			} catch (ClassNotFoundException e) {  
 				e.printStackTrace();  
 			}
 			
 		   return recipes;
 	   }
 	   
 	   /*
 	    * Searches the phone's recipes for the specific keyword.  It looks at the recipe's name, procedure, author, and ingredients
 	    */
 	   public ArrayList<Recipe> searchRecipe(String[] keywords) {
 		   //Array List of Recipes that match the keyword
 		   ArrayList<Recipe> matchingrecipes = new ArrayList<Recipe>();
 		   
 		   
 		   //Searches the Arrraylist and looks for any recipe name, author, procedure which contains the keywords
 		   for(int k = 0; k<keywords.length; k++){
 		   		for(int i = 0; i<recipes.size(); i++){
 		   			if((recipes.get(i).getName().toLowerCase(Locale.ENGLISH).contains(keywords[k].toLowerCase(Locale.ENGLISH)) || recipes.get(i).getAuthor().toLowerCase(Locale.ENGLISH).contains(keywords[k].toLowerCase(Locale.ENGLISH)) || recipes.get(i).getProcedure().toLowerCase(Locale.ENGLISH).contains(keywords[k]) ) && !matchingrecipes.contains(recipes.get(i)) )
 		   				matchingrecipes.add(recipes.get(i));
 		   			else{
 		   				//Searches the Arraylist and looks for any ingredient that contains the keyword
 		   				// 	for(int n = 0; n<recipes.get(i).getIngredients().length; n++)
 		   					//if((recipes.get(i).getIngredients()[n].toLowerCase(Locale.ENGLISH).contains(keywords[k].toLowerCase(Locale.ENGLISH)) )  && !matchingrecipes.contains(recipes.get(i)))
 		   						//matchingrecipes.add(recipes.get(i));
 		   			}
 		   		}
 		   }
 		   	   
 		   return matchingrecipes;
 		   
 	   }
 	   
 	   /*
 	    * Searches the database for recipes in which the user has every ingredient
 	    */
 	 /*  public ArrayList<Recipe> searchWithIngredient(String[] keywords, boolean searchLocally, boolean searchFromWeb){
 		   boolean ingredientIsInKitchen = true;
 		   IngredientList il = new IngredientList();
 		   ArrayList<Ingredient> kitchenIngredients = new ArrayList<Ingredient>();
 		   
 		   ArrayList<Recipe> matchingRecipes = new ArrayList<Recipe>();
 		   
 		   //Call the local search method to find recipes that match the keywords
 		 //  matchingRecipes = searchRecipe(keywords, searchLocally, searchFromWeb);
 		   ArrayList<Recipe> matchingIngredientRecipes = new ArrayList<Recipe>();
 		   
 		   
 		   kitchenIngredients = il.load();
 		   
 		   
 		   for (int i = 0; i<matchingRecipes.size(); i++){
 			   for (int q = 0; q<matchingRecipes.get(i).getIngredients().length; q++){
 				   if(!ingredientIsInKitchen){
 					   break;
 				   }
 		   			//for (int n = 0; n<kitchenIngredients.size(); n++){
 		   			//	if(!matchingRecipes.get(i).getIngredients()[q].getType().equals(kitchenIngredients.get(n).getType())){
 		   			//	ingredientIsInKitchen = false;		   				
 		   			//	}
 		   			//	else{
 		   			//		ingredientIsInKitchen = true;
 		   			//		break;
 		   			//	}
 		   				
 		   			//}
 
 		   			}
 	   			if(ingredientIsInKitchen){
 	   				matchingIngredientRecipes.add(matchingRecipes.get(i));
 	   				
 		   		}
 		   }
 		   
 		   return matchingIngredientRecipes;
 		   
 		   
 	   } */
 	   /*
 	    * Sorts the recipe in alphabetical order by the recipe's name
 	    */
 	   private void sortRecipes(){
 		   Recipe temp;
 		   
		   for(int n = recipes.size()-1; n > 1; n--)
 		   		for(int i = 0; i<n; i++){
 		   			if(recipes.get(i).getName().compareToIgnoreCase(recipes.get(i+1).getName()) > 0){
 		   				temp = recipes.get(i);
 		   				recipes.set(i, recipes.get(i+1));
 		   				recipes.set(i+1, temp);
 		   			}				   
 		   		}
 	   }
 	   
 	   /*
 	    * Returns a single recipe
 	    */
 	   public Recipe getRecipe(int index) {
 		   return this.recipes.get(index);
 	   }
 	   
 	   /*
 	    * Returns all recipes
 	    */
 	   public List<Recipe> getAllRecipes() {
 		   return this.recipes;
 	   }
 
 	   public Recipe getRecipeById(long id) {
 		   Recipe r = new Recipe();
 		   
 		   for (int i = 0; i < recipes.size(); i++) {
 			   
 			   if (id == recipes.get(i).getRecipeID()) {
 				   r = recipes.get(i);
 			   }
 		   }
 		   
 		   return r;
 	   }
 	   
 	   public void replaceRecipe(Recipe r, long id){
 		   Recipe old = getRecipeById(id);
 		   old.setName(r.getName());
 		   old.setProcedure(r.getProcedure());
 	   }
 		
 	
 	
 	
 }
