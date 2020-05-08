 package controllers;
 import java.util.List;
 import models.Item;
 import models.Menu;
 import models.Restaurant;
 import play.data.validation.Required;
 import play.mvc.Controller;
 
 public class Menus extends Controller {
     
     public static void index() {
         List<Menu> menus = Menu.findAll();
         render(menus);
     }
     
     public static void show(Long id){
         Menu menu = Menu.findById(id);
         List<Item> items = Item.find("byMenu_id", id).fetch();
         render(menu,items);
     }
     
     public static void create(@Required String name, List<Item> items) {
         if (validation.hasErrors()) {
             validation.keep();
             params.flash();
             flash.error("Please correct these errors !");
             index();
         }
         Menu menu = new Menu(name, items);
         menu.save();
         index();
     }
     
     public static void edit(Long id){
         Menu menu = Menu.findById(id);
         render(menu);
     }
 
    public static void saveEdit(@Required Long id, @Required String name) {
         Menu menu = Menu.findById(id);
         menu.name = name;
         validation.valid(menu);
         if(validation.hasErrors()) {
             // Message errors to test in views
         } else {
             menu.save();
         }
         show(id);
     }
 
     public static void editListItem(Long id){
         Menu menu = Menu.findById(id);
         List<Item> items = Item.findAll();
         render(menu, items);
     }
 
     public static void saveEditListItem(@Required Long id, @Required List<Item> listItems) {
         Menu menu = Menu.findById(id);
         menu.listItems = listItems;
         validation.valid(menu);
         if(validation.hasErrors()) {
             // Message errors to test in views
         } else {
             menu.save();
         }
         show(id);
     }
     
     public static void destroy(Long id) {
         List<Restaurant> restaurants = Restaurant.find("byCurrentMenu_id", id).fetch();
 
         for(int i=0; i<restaurants.size(); i++){
             restaurants.get(i).currentMenu = null;
             validation.valid(restaurants.get(i));
             if(validation.hasErrors()) {
                 // Message errors to test in views
             } else {
                 restaurants.get(i).save();
             }
         }
 
         Menu menu = Menu.findById(id);
         menu.delete();
         index();
     }
 }
