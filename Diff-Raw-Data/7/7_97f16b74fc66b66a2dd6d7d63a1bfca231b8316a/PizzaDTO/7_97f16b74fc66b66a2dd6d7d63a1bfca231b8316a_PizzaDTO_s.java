 package org.jdto.demo.dtos;
 
 import java.io.Serializable;
 import org.jdto.annotation.Source;
 import org.jdto.annotation.Sources;
 import org.jdto.mergers.StringFormatMerger;
 
 /**
  *
  * @author juancavallotti
  */
 public class PizzaDTO implements Serializable {
     
     @Source("id")
     private Long pizzaId;
     
     
     @Sources(value={@Source("pizzaName"), 
         @Source("price")}, merger=StringFormatMerger.class, mergerParam="%s ($ %.2f)")
     private String description;
     
     private double price;
     private int rating;
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public Long getPizzaId() {
         return pizzaId;
     }
 
     public void setPizzaId(Long pizzaId) {
         this.pizzaId = pizzaId;
     }
 
     public int getRating() {
         return rating;
     }
 
     public void setRating(int rating) {
         this.rating = rating;
     }
 
     public double getPrice() {
         return price;
     }
 
     public void setPrice(double price) {
         this.price = price;
     }
     
     
 }
