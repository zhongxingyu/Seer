 package controllers;
 
 import java.util.List;
 import play.mvc.Controller;
 
 /**
  *
  * @author pierregaste
  */
 public class Car extends Controller {
 
     /**
      * Action par défaut
      */
     public static void index()
     {
         redirect("/car/list");
     }
     
     /**
      * Liste des voitures du compte connecté
      * 
      * @view app/view/car/list.html
      */
     public static void list()
     {
         List<Car> cars =  models.Car.find("byOwner", User.connected()).fetch();
         render(cars);
     }
     
     /**
      * Ajout une voiture pour le compte connecté
      */
     public static void add()
     {        
         render();
     }
     
     public static void add(String name, int cost){               
         new models.Car(name, cost, User.connected()).save();
         
         redirect("/car/list");
     }
     
     /**
      * Edit une voiture pour le compte connecté
      */
     public static void edit()
     {
         render();
     }
     
    public static void edit(Long id, String name, int cost){
         models.Car car = models.Car.findById(id);
         car.name = name;
         car.cost = cost;
         car.save();
         
         redirect("/car/list");
     }
     
     /**
      * Supprime une voiture pour le compte connecté
      */
    public static void delete(Long id)
     {
         models.Car car = models.Car.findById(id);
         car.delete();
         
         redirect("/car/list");
     }
     
     /**
      * Affiche les informations d'une voiture
      */
    public static void details(Long id)
     {
         models.Car car = models.Car.findById(id);
         render(car);
     }
     
 }
