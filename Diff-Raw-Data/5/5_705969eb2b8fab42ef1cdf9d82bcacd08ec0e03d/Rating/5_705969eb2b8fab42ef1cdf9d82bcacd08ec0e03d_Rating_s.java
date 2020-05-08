 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dit126.group4.group4shop.core;
 
 import java.io.Serializable;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.Table;
 import javax.validation.constraints.*;
 
 /**
  *
  * @author Group4
  */
 @Entity 
 @Table(name="RATING")
 public class Rating implements Serializable {
     
     @Id
<<<<<<< HEAD
     @NotNull
=======
     @Column(name = "ID")
>>>>>>> 387e027cdee0fe1564842ef9a1f3331eae777961
     private Long id;
     @Column(name="USER_ID") private Long user_ID;
     @Column(name="RATING") private String rating;
     
     protected Rating(){}
     
     protected Rating(Long user_ID, String rating){
         this.user_ID = user_ID;
         this.rating = rating; 
     }
     
 }
