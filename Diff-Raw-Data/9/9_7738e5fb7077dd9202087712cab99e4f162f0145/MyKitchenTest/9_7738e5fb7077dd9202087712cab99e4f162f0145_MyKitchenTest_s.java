 package ca.ualberta.team2recipefinder.test;
 
 import static org.junit.Assert.*;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import ca.ualberta.team2recipefinder.DuplicateIngredientException;
 import ca.ualberta.team2recipefinder.Ingredient;
 import ca.ualberta.team2recipefinder.MyKitchen;
 import ca.ualberta.team2recipefinder.Recipe;
 
 public class MyKitchenTest {
 
 	MyKitchen model;
 	
 	@Before
 	public void setUp() {
 		model = new MyKitchen();
 		
 		//ensure model is empty
 		
 		model.getIngredients().removeAll(model.getIngredients());
 	}
 	
 	@Test
 	public void testAdd() {
 		Ingredient i = new Ingredient("Milk", 250d, "mL");
 		
 		try {
 			model.add(i);
 		} catch (DuplicateIngredientException e) {
 			e.printStackTrace();
 		}
 		
 		// make sure there is one ingredient in the collection
 		assertEquals(model.getIngredients().size(), 1);
 		
 		// make sure it is the ingredient we just added
 		assertEquals(model.searchIngredient(new String[] { "Milk" }).size(), 1);
 		
 		// make sure it is the ingredient we just added
 		assertEquals(model.getIngredient("Milk").getType(), i.getType());
 		
 		// make sure it is the ingredient we just added
 		assertEquals(model.getIngredients().get(0), i);
 	}
 	
 	@Test
 	public void testAddingDuplivate() {
 		Ingredient i = new Ingredient("Milk", 250d, "mL");
 		Ingredient ii = new Ingredient("Milk", 250d, "mL");
 		boolean exceptionWasCaught = false;
 		
 		try {
 			model.add(i);
 		} catch (DuplicateIngredientException e) {
 			e.printStackTrace();
 		}
 		
 		try {
 			model.add(ii);
 		} catch (DuplicateIngredientException e) {
 			exceptionWasCaught = true;
 		}
 		
 		assertTrue(exceptionWasCaught);
 	}
 	
 	@Test
 	public void testRemove() {
 		Ingredient i1 = new Ingredient("Milk", 250d, "mL");
 		Ingredient i2 = new Ingredient("Sugar", 100d, "g");
 		
 		try {
 			model.add(i1);
 			model.add(i2);
 		} catch (DuplicateIngredientException e) {
 			e.printStackTrace();
 		}
 		
 		model.remove(i1);
 		
 		// make sure only one ingredient in collection
 		assertEquals(model.getIngredients().size(), 1);
 		
 		// make sure that ingredient in list is "sugar"
 		assertEquals(model.searchIngredient(new String[] { "Sugar" }).size(), 1);
 		
 		// make sure the ingredient in the list matches i2
 		assertEquals(model.getIngredient("Sugar"), i2);		
 	}
 	
 	@Test
 	public void testGetIngredient() {
 		Ingredient i1 = new Ingredient("Milk", 250d, "mL");
 		
 		try {
 			model.add(i1);
 		} catch (DuplicateIngredientException e) {
 			e.printStackTrace();
 		}
 		
 		Ingredient rval = model.getIngredient("Milk");
 		
 		// ensure the returned ingredient is the same one we added
 		assertEquals(rval, i1);
 	}
 	
 	@Test
 	public void testReplace() {
 		Ingredient i1 = new Ingredient("Milk", 250d, "mL");
 		Ingredient i2 = new Ingredient("Milk", 1d, "L");
 		
 		try {
 			model.add(i1);
 		} catch (DuplicateIngredientException e) {
 			e.printStackTrace();
 		}
 		
 		model.replaceIngredient(i1, i2);
 		
 		// make sure there is one ingredient in the collection
 		assertEquals(model.getIngredients().size(), 1);
 		
 		// make sure it is the ingredient we just replaced
 		assertEquals(model.searchIngredient(new String[] { "Milk" }).size(), 1);
 		
 		// make sure it is the ingredient we just replaced
 		assertEquals(model.getIngredient("Milk"), i2);
 		
 		// make sure it is the ingredient we just added
 		assertEquals(model.getIngredients().get(0), i2);
 		
 		// ensure milk has amount of 1 L, not 250 mL
 		assertEquals(model.getIngredient("Milk").getAmount(), i2.getAmount());
 	}
 	
 	@Test
 	public void testSearch() {
 		Ingredient i1 = new Ingredient("Sugar", 300d, "g");
 		Ingredient i2 = new Ingredient("Salt", 300d, "g");
 		Ingredient i3 = new Ingredient("Soy Bean", 250d, "g");
 		Ingredient i4 = new Ingredient("Cocoa", 250d, "g");
 		Ingredient i5 = new Ingredient("Carrot", 3d, "unit");
 		
 		try {
 			model.add(i1);
 			model.add(i2);
 			model.add(i3);
 			model.add(i4);
 			model.add(i5);
 		} catch (DuplicateIngredientException e) {
 			e.printStackTrace();
 		}
 		
 		List<Ingredient> S_Ingredients = model.searchIngredient(new String[] { "S" });
 		List<Ingredient> O_Ingredients = model.searchIngredient(new String[] { "O" });
 		
 		// if you search for "S", there must be three results
 		assertEquals(S_Ingredients.size(), 3);
 		
 		// and they should be ingredients 1, 2 and 3
 		List<Ingredient> expectedResults = new LinkedList<Ingredient>();
 		expectedResults.add(i1);
 		expectedResults.add(i2);
 		expectedResults.add(i3);
 		checkExpectedResults(S_Ingredients, expectedResults);
 		
 		// if you search for "O", there must be three results
 		assertEquals(O_Ingredients.size(), 3);
 		
 		// and they should be ingredients 3, 4 and 5
 		expectedResults.add(i3);
 		expectedResults.add(i4);
 		expectedResults.add(i5);
 		checkExpectedResults(O_Ingredients, expectedResults);
 	}
 	
 	@Test
 	public void getAllIngredients() {
 		Ingredient i1 = new Ingredient("Sugar", 300d, "g");
 		Ingredient i2 = new Ingredient("Salt", 300d, "g");
 		Ingredient i3 = new Ingredient("Soy Bean", 250d, "g");
		Ingredient i4 = new Ingredient("Cocoa", 250d, "g");
 		Ingredient i5 = new Ingredient("Carrot", 3d, "unit");
 		
 		try {
 			model.add(i1);
 			model.add(i2);
 			model.add(i3);
 			model.add(i4);
 			model.add(i5);
 		} catch (DuplicateIngredientException e) {
 			e.printStackTrace();
 		}
 		
 		List<Ingredient> all = model.getIngredients();
 		
		// ensure returned list contains all ingredients in  alphabetical order
		assertEquals(all.get(0), i5);
		assertEquals(all.get(1), i4);
 		assertEquals(all.get(2), i2);
 		assertEquals(all.get(3), i3);
 		assertEquals(all.get(4), i1);
 		
 		// ensure size is 5
 		assertEquals(all.size(), 5);
 	}
 	
 	private void checkExpectedResults(List<Ingredient> results, List<Ingredient> expectedResults) {
 		for (Ingredient i : results) {
 			assertTrue(expectedResults.contains(i));
 			expectedResults.remove(i);
 		}
 	}
 
 }
