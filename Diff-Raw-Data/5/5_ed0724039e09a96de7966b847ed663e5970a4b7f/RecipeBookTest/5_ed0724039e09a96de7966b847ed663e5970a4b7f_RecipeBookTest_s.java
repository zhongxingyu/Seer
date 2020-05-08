 package ca.teamTen.recitopia.test;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Random;
 
import ca.teamTen.recitopia.CloudRecipeBook;
import ca.teamTen.recitopia.Photo;
 import ca.teamTen.recitopia.Recipe;
 import ca.teamTen.recitopia.RecipeBook;
 import junit.framework.TestCase;
 
 /**
  * abstract RecipeBook junit tests
  *
  */
 public abstract class RecipeBookTest extends TestCase
 {
 
 	protected abstract RecipeBook createRecipeBook();
 
 	protected RecipeBook recipeBook;
 
 	/**
 	 * Stores recipes that have been added with addTestData();
 	 */
 	protected ArrayList<Recipe> defaultRecipes;
 
 	/*
 	 * Before each test, create the default recipes
 	 * and recipe book, then populate the book with
 	 * the recipes.
 	 */
 	protected void setUp() throws Exception
 	{
 		super.setUp();
 		defaultRecipes = new ArrayList<Recipe>();
 		generateDefaultRecipes();
 		recipeBook = createRecipeBook();
 		addTestData();
 	}
 
 	protected void tearDown() throws Exception
 	{
 		super.tearDown();
 	}
 
 	/*
 	 * Test that querying by title or title fragments returns
 	 * the expected number of recipes.
 	 */
 	public void testQueryByTitle()
 	{
 		addTestData();
 		assertEquals(recipeBook.query("Spiky Melon").length, 4);
 		assertEquals(recipeBook.query("Soup").length, 1);
 	}
 
 	/*
 	 * For each recipe in our test data, test that it will be returned
 	 * in any queries we make using its data.
 	 */
 	public void testMixedQuery()
 	{
 		addTestData();
 
 		for (Recipe queryBy: defaultRecipes) {
 			// query by recipe name
 			assertTrue(queryResultContains(recipeBook.query(queryBy.getRecipeName()), queryBy));
 
 			//query by recipe name and author
 			assertTrue(queryResultContains(recipeBook.query(
 					queryBy.getRecipeName() + " " + queryBy.getAuthor()
 					), queryBy));
 
 			// query by first ingredient and author
 			assertTrue(queryResultContains(recipeBook.query(
 					queryBy.getIngredients().get(0) + " " + queryBy.getAuthor()
 					), queryBy));
 
 			// query by instructions and first ingredient
 			assertTrue(queryResultContains(recipeBook.query(
 					queryBy.getCookingInstructions() + " " + queryBy.getIngredients().get(0)
 					), queryBy));
 		}
 	}
 
 	/*
 	 * Test that searching is case-insensitive
 	 */
 	public void testCaseInsensitiveQuerying()
 	{
 		addTestData();
 		for (Recipe queryBy: defaultRecipes) {
 			String name = queryBy.getRecipeName();
 
 			// query by lowercase recipe name
 			assertTrue(queryResultContains(recipeBook.query(name.toLowerCase()), queryBy));
 
 			// query by uppercase recipe name
 			assertTrue(queryResultContains(recipeBook.query(name.toUpperCase()), queryBy));
 		}
 	}
 
 	/*
 	 * Test that changing fields other than title and author, and then
 	 * re-adding the recipe modifies the recipe in-place, instead of
 	 * creating a new one.
 	 */
 	public void testUpdatesRecipes()
 	{
 		addTestData();
 		Recipe oldRecipe = defaultRecipes.get(0);
 		String newInstructions = "these are not the same instructions";
 		Recipe modifiedRecipe = new Recipe(oldRecipe.getRecipeName(),
 				oldRecipe.getIngredients(), newInstructions,
 				oldRecipe.getAuthor());
 
 		recipeBook.addRecipe(modifiedRecipe);
 		Recipe results[] = recipeBook.query(modifiedRecipe.getAuthor());
 
 		// the new recipe should be present, but the old one should not
 		assertTrue(queryResultContains(results, modifiedRecipe));
 		assertTrue(!queryResultContains(results, oldRecipe));		
 	}
 
 	
 
 	/*
 	 * Generates a bunch of recipes and adds them to
 	 * defaultRecipes.
 	 */
 	protected void generateDefaultRecipes()
 	{
 		defaultRecipes.add(new Recipe("Spiky Melon Salad",
 				new ArrayList<String>(Arrays.asList("spiky melon", "lettuce", "cucumber")),
 				"Cube the melon, chop the lettuce and cumbers. Mix in bowl and enjoy",
 				"alex@test.com"));
 		defaultRecipes.add(new Recipe("Spiky Melon Soup",
 				new ArrayList<String>(Arrays.asList("spiky melon", "cream", "spices")),
 				"mix, heat and enjoy",
 				"zhexin@test.com"));
 		defaultRecipes.add(new Recipe("Spiky Melon Shake",
 				new ArrayList<String>(Arrays.asList("spiky melon", "cream", "sugar")),
 				"mix, blend and enjoy",
 				"osipovas@test.com"));
 		defaultRecipes.add(new Recipe("Spiky Melon Fries",
 				new ArrayList<String>(Arrays.asList("spiky melon", "salt", "cooking oil")),
 				"chop, fry, eat",
 				"zou@test.com"));
 	}
 
 	/*
 	 * Add the test data from defaultRecipes to the
 	 * recipeBook under test.
 	 */
 	protected void addTestData()
 	{		
 		for (Recipe recipe: defaultRecipes) {
 			recipeBook.addRecipe(recipe);
 		}
 	}
 
 	/*
 	 * Tests whether a query result (an array of Recipes) contains
 	 * a given recipe, based on data-equality, not reference-equality.
 	 */
 	protected boolean queryResultContains(Recipe[] recipes, Recipe recipe) {
 		for (Recipe result: recipes) {
 			if (recipe.equalData(result)) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	
 }
