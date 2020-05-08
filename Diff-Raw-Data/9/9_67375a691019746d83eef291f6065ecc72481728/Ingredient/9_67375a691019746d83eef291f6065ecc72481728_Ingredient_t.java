 package models;
 
 import play.db.jpa.Model;
 
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 @Entity
 @Table(name = "my_ingredient")
 public class Ingredient extends Model {
 
     public Double amount;
     public String description;
     public String unit;
 
     @ManyToOne
     public Recipe recipe;
 
 
     @ManyToOne
     public IngredientType ingredientType;
 
     public Ingredient(Recipe recipe, Double amount, String unit, String description) {
         this.recipe = recipe;
         this.amount = amount;
         this.unit = unit;
         this.description = description;
     }
 
 
     public Double getScaledAmount(Menu menu) {
         return getScaledAmount(null, menu, recipe, this);
 
     }
 
     public Double getScaledAmount(User user) {
        return getScaledAmount(user, null, recipe, this);
     }
 
     public Double getScaledAmount(User user, Menu menu) {
         return getScaledAmount(user, menu, recipe, this);
     }
 
     public static Double getScaledAmount(User user, Menu menu, Recipe recipe, Ingredient ingredient) {
         double scaledServing = getScaledServing(user, menu, recipe);
         double multiplier = scaledServing / recipe.serves;
 
         return ingredient.amount * multiplier;
 
     }
 
     public static Double getScaledServing(User user, Menu menu, Recipe recipe) {
 
        if (recipe != null && recipe.servesUnit != null && (recipe.servesUnit.compareToIgnoreCase("personer") == 0 || recipe.servesUnit.compareToIgnoreCase("porsjoner") == 0)) {
             if (menu != null) {
                 RecipeInMenu recipeInMenu = RecipeInMenu.find("menu = ? and recipe = ?", menu, recipe).first();
                 if (recipeInMenu.amount != null) {
                     return recipeInMenu.amount;
                 }
             }
        }
             if (user != null && user.preferredServings != null) {
                 return user.preferredServings;
             }
         return recipe.serves;
     }
 
 
 }
