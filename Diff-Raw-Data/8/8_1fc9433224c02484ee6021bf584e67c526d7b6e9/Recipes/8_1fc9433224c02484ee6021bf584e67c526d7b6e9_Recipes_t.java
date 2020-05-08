 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import java.util.*;
 
 import models.*;
 
 public class Recipes extends Controller {
 
     public static void listMine(){
         String username = Security.connected();
         Application.listUserRecipes(username);
     }
 
     public static void show(Long id) {
         Recipe recipe = Recipe.findById(id);
         if (recipe == null){
             notFound();
         }
         render(recipe);
     }
 
     public static void create(){
         render();
     }
 
     public static void processCreate(String title, String[] ingredients, String[] quantities, String description, String instructions){
         Recipe recipe = new Recipe();
         List<RecipeIngredient> recipeIngredients = new ArrayList<RecipeIngredient>();
 
         recipe.title = title;
         recipe.date = new Date();
         recipe.description = description;
         recipe.instructions = instructions;
         
         for(int i = 0; i < ingredients.length; i++) {
             try {
                 List<Ingredient> foundIngredients = Ingredient.find("byName", ingredients[i]).fetch();
                 if (foundIngredients.size() == 0) {
                     // if no ingredients was found, add it in db then add it to
                     // the recipe
                     Ingredient ing = new Ingredient();
                     ing.name = ingredients[i];
                     ing.periods = null;
                     ing.save();
                     RecipeIngredient recipeIngredient = new RecipeIngredient();
                     recipeIngredient.ingredient = ing;
                    recipeIngredient.quantity = quantities[i];
                     recipeIngredient.save();
                     recipe.ingredients.add(recipeIngredient);
                 } else {
                     // else, directly build and save the RecipeIngredient
                     RecipeIngredient recipeIngredient = new RecipeIngredient();
                     recipeIngredient.ingredient = foundIngredients.get(0);
                    recipeIngredient.quantity = quantities[i];
                     recipeIngredient.save();
                     recipe.ingredients.add(recipeIngredient);
                 }
             } catch (Exception e) {
                Logger.error(e.getMessage());
                 redirect("errors/500.html");
             }
 
         }
         recipe.save();
     }
 }
