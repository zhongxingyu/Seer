 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package models;
 
 import java.awt.Point;
 import java.util.List;
 import javax.persistence.Entity;
 import play.db.jpa.Model;
 
 /**
  *
  * @author Antoine
  */
 @Entity
 public class City extends Model{
     
     public String name;
     public Integer codeINSEE;
     public Double latitude;
     public Double longitude;
     public Point pxPosition;    
 
     public City(String name, Integer codeINSEE, Double latitude, Double longitude) {
         this.name = name;
         this.codeINSEE = codeINSEE;
         this.latitude = latitude;
         this.longitude = longitude;
         this.pxPosition = new Point();
    }
     
     public List<Road> roads(){                 
        
        return Road.find("byFirstcityOrSecondcity", this).fetch();
     }
 }
