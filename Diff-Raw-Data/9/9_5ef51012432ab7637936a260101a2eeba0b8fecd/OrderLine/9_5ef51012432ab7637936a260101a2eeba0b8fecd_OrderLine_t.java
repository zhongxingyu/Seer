 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package order;
 
 import java.math.BigDecimal;
 
 /**
  *
  * @author Bill
  */
 public class OrderLine {
     private Item.Item item;
     private Integer quantity;
     
     public OrderLine(Item.Item item, Integer quantity)
     {
         this.item = item;
         this.quantity = quantity;
     }
     
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
     public Integer getQuantity() {
         return quantity;
     }
     public String getItemID() {
         return item.getItemID();
     }
     public String getName() {
         return item.getName();
     }
     public String getDescription() {
         return item.getDescription();
     }
     public String getSpecification() {
         return item.getSpecification();
     }
     public String getPhotoName() {
         return item.getPhotoName();
     }
     public BigDecimal getPrice() {
         return item.getPrice();
     }
     
 }
