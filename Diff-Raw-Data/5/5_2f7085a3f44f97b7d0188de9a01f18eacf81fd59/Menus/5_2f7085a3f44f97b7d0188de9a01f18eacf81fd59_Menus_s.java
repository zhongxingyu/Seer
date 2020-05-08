 package controllers;
 
 
 import models.Menu;
 import models.MenuDay;
 import models.Recipe;
 import models.User;
 import play.mvc.Before;
 import play.mvc.With;
 import utils.DateUtil;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 
 @With(Secure.class)
 public class Menus extends CRUD {
 
     @Before
     static void setConnectedUser() {
         if (Security.isConnected()) {
             User user = User.find("byEmail", Security.connected()).first();
             if (user != null)
                 renderArgs.put("user", user.fullname);
         }
     }
 
 
     private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 
     public static void planrecipe(Long recipeId, String day) throws ParseException {
         User user = User.find("byEmail", Security.connected()).first();
 
         Menu menu = Menu.findById(user.activeMenu.getId());
 
         Date useDate = dateFormat.parse(day);
         int distance = DateUtil.distance(menu.usedFromDate, useDate);
 
         Recipe recipe = Recipe.findById(recipeId);
 
         menu.addRecipe(recipe, MenuDay.values()[distance]);
 
         menu.save();
     }
 
     public static void unplanrecipe(String day) throws ParseException {
         User user = User.find("byEmail", Security.connected()).first();
 
         Menu menu = Menu.findById(user.activeMenu.getId());
 
         Date useDate = dateFormat.parse(day);
         int distance = DateUtil.distance(menu.usedFromDate, useDate);
 
         menu.deleteRecipeForDay(distance);
     }
 
     public static void dinnerplan(String fromDate) throws ParseException {
         User user = User.find("byEmail", Security.connected()).first();
 
         Menu menu = null;
 
         initMenu(user, fromDate);
 
         if (user != null && user.activeMenu != null) {
             menu = Menu.findById(user.activeMenu.getId());
         }
 
         render(menu);
     }
 
 
     private static void initMenu(User user, String startingDayString) throws ParseException {
         Menu menu = null;
 
         if (startingDayString != null) {
             Date startingDay = dateFormat.parse(startingDayString);
            menu = Menu.find("usedFromDate = ?", startingDay).first();
 
             if (menu == null) menu = new Menu(user, startingDay).save();
         } else {
             if (user.activeMenu != null) {
                 menu = Menu.findById(user.activeMenu.getId());
             }
            if (menu == null || (DateUtil.distance(DateUtil.getStartingDay(), menu.usedFromDate) < 0)) {
                 menu = new Menu(user, DateUtil.getStartingDay()).save();
             }
         }
         user.activeMenu = menu;
         user.save();
     }
 
     public static void delete(Long id)
     {
         Menu.findById(id)._delete();
     }
 
 
 }
