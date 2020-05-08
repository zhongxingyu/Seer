 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package plantgame.models;
 
 import java.util.ArrayList;
 import java.util.List;
<<<<<<< HEAD
 import plantgame.database.PersistenceManager;
import plantgame.utils.Fruits;
=======
>>>>>>> 7680a77551b705b43a564d0c448a43b412a4d60a
 
 /**
  *
  * @author aadu
  */
 public class Market {
     
     List<Fruits> mktFruitsList = null;
     PersistenceManager pm = null;
     
     public Market()
     {
         mktFruitsList =new ArrayList<Fruits>();
         pm = PersistenceManager.getInstance();
     } 
     
     
      /*
      * This API will retrive all the fruits owned by
      * a particuler user at a given moment that can
      * be sold on the market. It will pull this from 
      * the DB
      */
     public List<Fruits> getUserFruits(int uid)
     {
         return pm.getUsersFruits(uid);
     }
     
      /*
      * This API will take a Fruit object and calulate
      * its worth using market factors as well as the
      * quality of the Fruit. 
      */
     public double getPriceOfFruit(Fruits fruit)
     {
         double price = 0;
         
         // do calculation
         
         return price;
     }
     
 }
