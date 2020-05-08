 package controllers;
 
 import java.util.List;
 import models.Item;
 import models.Menu;
 import play.data.validation.Required;
 import play.mvc.Controller;
 
 public class Items extends Controller {
 
     public static void index() {
         List<Item> items = Item.findAll();
         render(items);
     }
     
     public static void create() {
         render();
     }
 
     public static void saveCreate(@Required String name, @Required Float price, Enum type, List<Menu> listMenu) {
         if (validation.hasErrors()) {
             validation.keep();
             params.flash();
             flash.error("Please correct these errors !");
             //index();
         }
         
         Item item = new Item(name, price, type, null);
         item.save();
         index();
     }
     
     public static void edit(Long id) {
         Item item = Item.findById(id);
         render(item);
     }
 
    public static void saveEdit(@Required Long id, @Required String name, @Required Float price, @Required Enum type, @Required Menu menu) {
         Item item = Item.findById(id);
         item.name = name;
         item.price = price;
         item.type = type;
        item.menu = menu;
         validation.valid(item);
         if(validation.hasErrors()) {
             // Message errors to test in views
         } else {
             flash.success("The item has been updated !");
             item.save();
         }
         index();
     }
     
     public static void destroy(Long id) {
         Item item = Item.findById(id);
         item.delete();
         index();
     }
 }
