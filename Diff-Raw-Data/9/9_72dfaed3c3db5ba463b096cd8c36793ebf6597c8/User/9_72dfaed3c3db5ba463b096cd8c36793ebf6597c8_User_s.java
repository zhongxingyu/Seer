 package models;
 
 import org.hibernate.annotations.ManyToAny;
 import play.db.jpa.Model;
 
 import javax.persistence.*;
 import java.util.ArrayList;
 import java.util.List;
 
 @Entity
 @Table(name = "my_user")
 public class User extends Model {
 
     public String email;
     public String password;
     public String fullname;
     public boolean isAdmin;
 
    @ManyToOne
     public Menu activeMenu;
 
 
     @ManyToMany(cascade = CascadeType.ALL)
     public List<Recipe> favorites;
 
     public User(String email, String password, String fullname) {
         this.email = email;
         this.password = password;
         this.fullname = fullname;
         favorites = new ArrayList<Recipe>();
     }
 
     public static User connect(String email, String password) {
         return find("byEmailAndPassword", email, password).first();
     }
 
     public User addFavorite(Recipe recipe)
     {
         if(!favorites.contains(recipe))
         {
             favorites.add(recipe);
         }
         return this;
     }
     public User removeFavorite(Recipe recipe)
     {
         if(favorites.contains(recipe))
         {
             favorites.remove(recipe);
         }
         return this;
     }
 }
