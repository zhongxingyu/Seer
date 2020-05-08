 package models;
 
 import javax.persistence.Entity;
import javax.persistence.ManyToOne;
 import play.db.jpa.Model;
 
 @Entity
 public class Item extends Model {
  
     public String name;
     public Float price;
     public Enum type;
     
    @ManyToOne
    public Menu menu;
     
    public Item(String name, Float price, Enum type, Menu menu){
         this.name = name;
        this.menu = menu;
         this.price = price;
         this.type = type;
     }
 }
