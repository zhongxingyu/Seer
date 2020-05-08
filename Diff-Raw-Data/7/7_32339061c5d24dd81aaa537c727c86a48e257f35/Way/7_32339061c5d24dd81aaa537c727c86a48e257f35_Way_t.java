 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package models;
 
 import java.util.ArrayList;
 import java.util.List;
import javax.persistence.*;
 import org.joda.time.DateTime;
 import play.data.validation.Required;
 import play.db.jpa.Model;
 
 /**
  *
  * @author Antoine
  */
 @Entity
 public class Way extends Model {
     
     @ManyToOne
     @Required
     public City startCity;
     
     @ManyToOne
     @Required
     public City finishCity;
     
     @ManyToOne
     @Required
     public User driver;
     
    @ManyToMany
     public List<User> passengers;
     
     @Required
     public double distance;
     
     @Required
     public DateTime dateHourStart;
     
     @ManyToOne
     @Required
     public Car car;
     
     @Required
     public int placeAvailable;
 
     public Way(City startCity, City finishCity, User driver, double distance, DateTime dateHourStart, Car car, int placeAvailable) {
         this.startCity = startCity;
         this.finishCity = finishCity;
         this.driver = driver;
         this.passengers = new ArrayList<User>();
         this.distance = distance;
         this.dateHourStart = dateHourStart;
         this.car = car;
         this.placeAvailable = placeAvailable;
     }    
 }
