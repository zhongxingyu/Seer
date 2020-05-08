 package models;
 
 
 import play.db.jpa.Model;
 
 import javax.persistence.*;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 @Entity
 @Table(name = "my_menu")
 public class Menu extends Model {
 
     @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL)
     public List<RecipeInMenu> recipesInMenu;
 
     @ManyToOne
     public User author;
 
     public Date createdAt;
 
     public Date usedFromDate;
 
    @OneToOne(cascade = CascadeType.ALL)
     public ShoppingList shoppingList;
 
 
     public Menu(User author, Date usedFromDate) {
         this.author = author;
         this.createdAt = new Date();
         this.usedFromDate = usedFromDate;
         this.recipesInMenu = new ArrayList<RecipeInMenu>();
 
     }
 
     public Menu addRecipe(Recipe recipe, MenuDay usedForDay) {
         RecipeInMenu newRecipeInMenu = new RecipeInMenu(this, recipe, usedForDay).save();
         this.recipesInMenu.add(newRecipeInMenu);
         this.save();
         return this;
 
     }
 
     public Recipe getRecipeForDay(int day)
     {
         MenuDay menuDay = MenuDay.values()[day];
         for(RecipeInMenu recipeInMenu:recipesInMenu)
         {
              if(recipeInMenu.usedForDay.compareTo(menuDay) == 0)
              {
                  return recipeInMenu.recipe;
              }
         }
         return null;
     }
 
     public void deleteRecipeForDay(int day)
      {
          MenuDay menuDay = MenuDay.values()[day];
          for(RecipeInMenu recipeInMenu:recipesInMenu)
          {
               if(recipeInMenu.usedForDay.compareTo(menuDay) == 0)
               {
                   recipesInMenu.remove(recipeInMenu);
                   recipeInMenu.delete();
                   save();
                   break;
                   //recipeInMenu.delete();
 
               }
          }
      }
 
 }
