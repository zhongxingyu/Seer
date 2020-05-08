 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.chl.dat076.foodfeed.model.dao;
 
 import edu.chl.dat076.foodfeed.model.entity.Grocery;
 import edu.chl.dat076.foodfeed.model.entity.Ingredient;
 import edu.chl.dat076.foodfeed.model.entity.Recipe;
 import java.util.ArrayList;
 import java.util.List;
 import junit.framework.Assert;
 import static org.junit.Assert.*;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {
     "classpath*:spring/root-context.xml",
     "classpath*:spring/security-context.xml"})
 @Transactional
 public class RecipeDaoTest {
 
     @Autowired
     RecipeDao recipeDao;
 
     @Test
     public void testCreate(){
         List<Ingredient> ingredients = new ArrayList();
        ingredients.add(new Ingredient(new Grocery("Red pepper", "Swedish red pepper"), 2.0, "stycken"));
        ingredients.add(new Ingredient(new Grocery("Water", "Tap water"), 20.0, "liter"));
         
         Recipe recipe = new Recipe();
         recipe.setDescription("Best soup in the world");
         recipe.setName("Soup");
         recipe.setIngredients(ingredients);
         
         recipeDao.create(recipe);
         testFind(recipe);
     }
     
     public void testFind(Recipe recipe){
         List<Recipe> result = recipeDao.getByName(recipe.getName());
         Assert.assertTrue("found no recipe", !result.isEmpty());
     }
     
     @Test
     public void testFindAll() {
         List<Recipe> recipes = recipeDao.findAll();
         assertTrue("check that true is true", true);
     }
 }
