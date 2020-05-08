 package edu.chl.dat076.foodfeed.model.dao;
 
 import edu.chl.dat076.foodfeed.model.entity.Grocery;
 import edu.chl.dat076.foodfeed.model.entity.Recipe;
 import java.util.List;
 
 /**
 * Interface for RecipeDao which prepares for mocking this type in a future
 * test context.
  */
 public interface IRecipeDao extends EntityDao<Recipe, Long> {
 
     void create(Recipe r);
 
     List<Recipe> getByGrocery(Grocery g);
 
     List<Recipe> getByName(String name);
 
     void update(Recipe r);
     
 }
