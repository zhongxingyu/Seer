 package models;
 
import java.util.List;
 import javax.persistence.Entity;
import javax.persistence.OneToMany;
 import play.db.jpa.Model;
 
 @Entity
 public class Item extends Model {
  
     public String name;
     public Float price;
     public Enum type;
     
    @OneToMany
    public List<Menu> listMenus;
     
    public Item(String name, Float price, Enum type, List<Menu> listMenus){
         this.name = name;
        this.listMenus = listMenus;
         this.price = price;
         this.type = type;
     }
 }
