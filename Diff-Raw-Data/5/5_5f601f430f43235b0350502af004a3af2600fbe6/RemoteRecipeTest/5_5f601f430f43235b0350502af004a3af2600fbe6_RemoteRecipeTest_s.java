 package ca.ualberta.team2recipefinder.test;
 
 import java.io.IOException;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 
 import ca.ualberta.team2recipefinder.controller.RecipeFinderApplication;
 import ca.ualberta.team2recipefinder.model.DuplicateIngredientException;
 import ca.ualberta.team2recipefinder.model.Ingredient;
 import ca.ualberta.team2recipefinder.model.MyKitchen;
 import ca.ualberta.team2recipefinder.model.Recipe;
 import ca.ualberta.team2recipefinder.model.RemoteRecipes;
 import ca.ualberta.team2recipefinder.model.ServerPermissionException;
 
 public class RemoteRecipeTest extends TestCase {
 	private final String path = "file10.sav";
 
 	public void testRemoteRecipes() {		
 		fail("Not yet implemented");
 	}
 
 	public void testCanPublish() {
 		RemoteRecipes rr = new RemoteRecipes(path);				//Test whether you can publish your own recipe
 		Recipe r = new Recipe();
 		assertTrue(rr.canPublish(r));
 
 		Recipe otherUsersRecipe = new Recipe();
 		otherUsersRecipe.setUserId("OtherUser");
 		assertTrue(!rr.canPublish(otherUsersRecipe));		//Test whether you can publish someone elses recipe
 	}
 
 	public void testPublishRecipe() {
 		RemoteRecipes rr = new RemoteRecipes(path);
 		Recipe r = new Recipe();
 
 		r.setAuthor("Test Author");
 		r.setName("Test Name");
 		r.setProcedure("Test Procedure");
 
 		try {
 			rr.publishRecipe(r);
 		}
 		catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch (ServerPermissionException e){
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		Recipe downloaded;
 
 		try {
 			downloaded = rr.download(r.getServerId());
 			assertEquals(downloaded.getRecipeID(), r.getRecipeID());
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void testPostComment() {
 		RemoteRecipes rr = new RemoteRecipes(path);
 		Recipe r = new Recipe();
 
 		r.setAuthor("Test Author");
 		r.setName("Test Name");
 		r.setProcedure("Test Procedure");
 		r.addComment("Test Comment");
 
 		try {
 			rr.publishRecipe(r);
 			rr.postComment(r.getServerId(), r.getComment(0));
 			Recipe downloaded = rr.download(r.getServerId());
 			assertEquals(downloaded.getComment(0), r.getComment(0));
 		}
 		catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch (ServerPermissionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void testSearch() {
 		Recipe r = new Recipe();
 
 		r.setAuthor("Test Author");
 		r.setName("Test Name");
 		r.setProcedure("Test Procedure");
 		r.addComment("Test Comment");
 
 		Recipe r2 = new Recipe();
 
 		r2.setAuthor("Joe Smith");
 		r2.setName("Rice");
 		r2.setProcedure("Boil in Water");
 		r2.addComment("Delicious");
 
 		Recipe r3 = new Recipe();
 
 		r3.setAuthor("John");
 		r3.setName("Waffles");
 		r3.setProcedure("Mix and Bake");
 		r3.addComment(":)");
 
 		RemoteRecipes rr = new RemoteRecipes(path);
 
 		String[] keywords = {"rice"};
 		try {
 			rr.publishRecipe(r);
 			rr.publishRecipe(r2);
 			rr.publishRecipe(r3);
 
 			List<Recipe> recipes = rr.search(keywords);
 
 			assertTrue(recipes.size()>=1);
 			assertEquals(recipes.get(0).getRecipeID(), r2.getRecipeID());
 		}
 		catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch (ServerPermissionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	public void testSearchWithIngredient() {
 		RemoteRecipes rr = new RemoteRecipes(path);
 		Recipe r = new Recipe();
 		String[] keywordName = {"name"};
 		String[] keywordRice = {"rice"};
 		String[] keywordWaffles = {"waffles"};
 		Ingredient ingredient = new Ingredient("Apple", (double) 5, "Whole");
 		Ingredient ingredient2 = new Ingredient("Orange", (double) 3, "Sliced");
 		Ingredient ingredient3 = new Ingredient("Watermelon", (double) 50, "Whole");
 
 		r.setAuthor("Test Author");
 		r.setName("Test Name");
 		r.setProcedure("Test Procedure");
 		r.addComment("Test Comment");
 
 		Recipe r2 = new Recipe();
 		
 		r2.setAuthor("Joe Smith");
 		r2.setName("Rice");
 		r2.setProcedure("Boil in Water");
 		r2.addComment("Delicious");
 
 		Recipe r3 = new Recipe();
 
 		r3.setAuthor("John");
 		r3.setName("Waffles");
 		r3.setProcedure("Mix and Bake");
 		r3.addComment(":)");
 
 		MyKitchen mk = new MyKitchen(path);
 
 		try {
 			r.addIngredient(ingredient);			
 			r.addIngredient(ingredient2);					//Recipe 1 requires 2 ingredients that the My Kitchen has
 			r2.addIngredient(ingredient);					//Recipe 2 requires 1 ingredient that My Kitchen has
 			r3.addIngredient(ingredient3);					//Recipe 3 requires 1 ingredient that My Kitchen does not have
 		}
 		catch (DuplicateIngredientException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 
 		try {
 			mk.add(ingredient);
 			mk.add(ingredient2);							//My Kitchen has two ingredients
 
 			rr.publishRecipe(r);							//Publish Recipes
 			rr.publishRecipe(r2);
 			rr.publishRecipe(r3);
 
 			List<Recipe> recipes1 = rr.searchWithIngredient(keywordName, mk.getIngredients());		//Search should result in Recipe 1 appearing
 			List<Recipe> recipes2 = rr.searchWithIngredient(keywordRice, mk.getIngredients());		//Search should result in Recipe 2 appearing
 			List<Recipe> recipes3 = rr.searchWithIngredient(keywordWaffles, mk.getIngredients());	//Search should result in no recipes
 
 			assertTrue(recipes1.size() == 1);
 			assertTrue(recipes2.size() == 1);
 			assertTrue(recipes3.size() == 0);
 
 			assertEquals(recipes1.get(0).getRecipeID(), r.getRecipeID());
 			assertEquals(recipes2.get(0).getRecipeID(), r2.getRecipeID());
 
 		}
 		catch (DuplicateIngredientException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch (ServerPermissionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
	public void testPostPicture() {
		Recipe r = new Recipe();
		Recipe r2 = new Recipe();
		assertEquals(r.getRecipeID(), r2.getRecipeID());
	}
 
 	public void testDownload() {
 		Recipe r2 = new Recipe();
 
 		r2.setAuthor("Joe Smith");
 		r2.setName("Rice");
 		r2.setProcedure("Boil in Water");
 		r2.addComment("Delicious");
 
 		RemoteRecipes rr = new RemoteRecipes(path);
 		try {
 			rr.publishRecipe(r2);
 		}
 		catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch (ServerPermissionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		Recipe downloaded;
 
 		try {
 			downloaded = rr.download(r2.getServerId());
 			assertEquals(r2.getRecipeID(), downloaded.getRecipeID());
 		}
 		catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
