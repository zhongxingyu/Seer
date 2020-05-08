 package version1;
 
 /**
  *
  * @author Nathaniel
  */
 public class ElectronicProduct implements Product{
     private Discount discount;
     
     private String productID;
     private String name;
     private String description;
     private double price;
     private String brand;
     private int lifeSpanInYears;
 
     public ElectronicProduct(Discount discount, String productID, String name, String description, double price, String brand, int lifeSpanInYears) {
         setDiscount(discount);
         setProductID(productID);
         setName(name);
         setDescription(description);
         setPrice(price);
         setBrand(brand);
         setLifeSpanInYears(lifeSpanInYears);
     }
 
     public Discount getDiscount() {
         return discount;
     }
 
     public void setDiscount(Discount discount) {
         // need to write some validation
         this.discount = discount;
     }
 
     @Override
     public String getProductID() {
         return productID;
     }
 
     public void setProductID(String productID) {
         // need to write some validation
         this.productID = productID;
     }
 
     @Override
     public String getProductName() {
         return name;
     }
 
     public void setName(String name) {
         // need to write some validation
         this.name = name;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         // need to write some validation
         this.description = description;
     }
 
     @Override
     public double getProductPrice() {
         return price;
     }
 
     public void setPrice(double price) {
         // need to write some validation
         this.price = price;
     }
 
     public String getBrand() {
         return brand;
     }
 
     public void setBrand(String brand) {
         // need to write some validation
         this.brand = brand;
     }
 
     public void setLifeSpanInYears(int lifeSpanInYears) {
         // need to write some validation
         this.lifeSpanInYears = lifeSpanInYears;
     }
 
     @Override
     public double getProductDiscount(double numProducts) {
        return (this.getDiscount().getDiscount(numProducts));
     }
     
 }
