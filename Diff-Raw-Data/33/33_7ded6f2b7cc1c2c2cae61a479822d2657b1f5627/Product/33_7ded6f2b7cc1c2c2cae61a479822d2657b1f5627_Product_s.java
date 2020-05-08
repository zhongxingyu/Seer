 package dit126.group4.group4shop.core;
 import java.io.Serializable;
 import java.util.Objects;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 /**
  *
  * @author Group4
  */
 
 @Entity(name = "Product")
 @Table(name="PRODUCT")
 public class Product implements Serializable {
     
     @Id
     @Column(name="ID", nullable = false) 
     private Long id;
     
     @Column(name="NAME")
     private String name;
     
     @Column(name="PRICE")
     private Double price;
     
     @Column(name="DESCRIPTION")
     private String description;   
     
     @Column(name="CATEGORY")
     private String category;
     
     protected Product(){}
     
     public Product(Long id, String name, Double price, String description){
         this.id = id;
         this.name = name;
         this.price = price;
         category = "Other";
         this.description = description;
     }
     
     public Product(Long id, String name, Double price, String category, String description){
         this.id = id;
         this.name = name;
         this.price = price;
         this.category = category;
         this.description = description;
     }
     
     public Long getId(){
         return id;
     }
     
     public String getName(){
         return name;
     }
     
     public Double getPrice(){
         return price;
     }
     
     public String getDescription(){
         return description;
     }
     
     public String getCategory(){
         return category;
     }
     
     @Override
     public String toString() {
         return "Product{" + "id=" + id + ", name=" + name + ", price=" + price + ", category=" + category + ", description=" + description + '}';
     }
     
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 113 * hash + Objects.hashCode(this.id);
         return hash;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final Product other = (Product) obj;
         if (!Objects.equals(this.id, other.id)) {
             return false;
         }
         return true;
     }
     
 }
