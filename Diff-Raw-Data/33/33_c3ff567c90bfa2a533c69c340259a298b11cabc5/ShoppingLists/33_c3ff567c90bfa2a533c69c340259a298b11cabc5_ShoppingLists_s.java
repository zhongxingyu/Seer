 package controllers;
 
 
 import models.*;
 import play.Logger;
 import play.mvc.Before;
 import play.mvc.With;
 import utils.DateUtil;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 @With(Secure.class)
 public class ShoppingLists extends CRUD {
 
     @Before
     static void setConnectedUser() {
         if (Security.isConnected()) {
             User user = User.find("byEmail", Security.connected()).first();
             if (user != null)
                 renderArgs.put("user", user.fullname);
         }
     }
 
     public static void show(Long listId) {
        List<ShoppingListIngredient> shoppingList = ShoppingListIngredient.find("shoppingList is ? order by description", ShoppingList.findById(listId)).fetch();
         Menu menu = Menu.find("shoppingList is ?", ShoppingList.findById(listId)).first();
        render(shoppingList, menu);
    }
 
    public static void showCurrent() {
         User user = User.find("byEmail", Security.connected()).first();
 
        Menu menu = Menu.find("usedFromDate = ?", DateUtil.getStartingDay()).first();
         List<ShoppingListIngredient> shoppingListChecked = null;
         List<ShoppingListIngredient> shoppingListUnchecked = null;
         if (menu != null) {
             ShoppingList shoppingListItem = ShoppingList.find("menu = ?", menu).first();
             if (shoppingListItem != null) {
                 List<ShoppingListIngredient> shoppingList = ShoppingListIngredient.find("shoppingList is ? order by description", shoppingListItem).fetch();
                 shoppingListChecked = new ArrayList<ShoppingListIngredient>();
                 shoppingListUnchecked = new ArrayList<ShoppingListIngredient>();
                 for (ShoppingListIngredient ingredient : shoppingList) {
                     if (ingredient.checked) {
                         shoppingListChecked.add(ingredient);
                     } else {
                         shoppingListUnchecked.add(ingredient);
                     }
                 }
             }
         }
         render("ShoppingLists/show.html", shoppingListChecked, shoppingListUnchecked, menu);
     }
 
     public static void list(Long menuId) {
         Menu menu = Menu.findById(menuId);
 
         ShoppingList shoppingList;
 
         if (menu.shoppingList == null) {
             menu.shoppingList = new ShoppingList(menu).save();
             menu.save();
         }
 
         shoppingList = menu.shoppingList;
 
         render(menu, shoppingList);
     }
 
 
     public static void check(Long id) {
         toggleIngredient(id, true);
     }
 
     public static void uncheck(Long id) {
         toggleIngredient(id, false);
     }
 
     private static void toggleIngredient(Long id, boolean toggle) {
         ShoppingListIngredient shoppingListIngredient = ShoppingListIngredient.findById(id);
         shoppingListIngredient.checked = toggle;
         shoppingListIngredient.save();
     }
 
     public static void save(Long id, Long[] includeRecipes) {
         ShoppingList shoppingList = ShoppingList.findById(id);
 
         HashMap<String, Object[]> ingredientMap = new HashMap<String, Object[]>();
 
         for (Long recipeId : includeRecipes) {
             Recipe recipe = Recipe.findById(recipeId);
             String amountString = params.get("recipeAmounts-" + recipeId);
             int amount = 2;
             try {
                 amount = Integer.parseInt(amountString);
             } catch (NumberFormatException e) {
                 Logger.error("Greide ikke å parse recipeAmounts-felt", e);
             }
 
             double multiplier = amount / recipe.serves;
 
             for (Ingredient ingredient : recipe.ingredients) {
                 String key = ingredient.unit.toLowerCase() + ingredient.description.toLowerCase();
                 if (ingredientMap.containsKey(key)) {
                     double currentAmount = (Double) ingredientMap.get(key)[0];
                     ingredientMap.get(key)[0] = Double.parseDouble(ingredient.amount) * multiplier + currentAmount;
                 } else {
                     ingredientMap.put(key, new Object[]{Double.parseDouble(ingredient.amount) * multiplier, ingredient.unit, ingredient.description});
                 }
             }
         }
 
         shoppingList.shoppingListIngredients = new ArrayList<ShoppingListIngredient>();
         shoppingList.save();
 
 
         for (Object[] objects : ingredientMap.values()) {
             shoppingList.addIngredient((Double) objects[0], (String) objects[1], (String) objects[2]);
         }
 
         shoppingList.save();
 
         show(shoppingList.id);
     }
 }
