 /*******************************************************
  * Author: Mark Johnson
  * Date: 10/07/12
  *
  * Compilation: javac ProductNonExempt.java
  * Execution: not applicable
  *
  * Defines a type for exempt products.
  ******************************************************/
 
 package salestax;
 
 public class ProductExempt extends Product {
     private static final double basicSalesTax = 0.0;
     private static final double importDuty = 5.0;
     private boolean imported;
     private String name;
     private double unitPrice;
     private int quantity;
 
     public ProductExempt(int quantity, boolean imported, String name, double unitPrice) {
         if (quantity < 1) throw new java.lang.IllegalArgumentException();
         if (name.isEmpty()) throw new java.lang.IllegalArgumentException();
         if (unitPrice < 0) throw new java.lang.IllegalArgumentException();
         this.quantity = quantity;
         this.imported = imported;
         this.name = name;
         this.unitPrice = unitPrice;
     }
 
     public String name() {
         return this.name;
     }
 
     public double price() {
        return this.price + this.tax();
     }
 
     public int quantity() {
         return this.quantity;
     }
 
     public boolean isImported() {
         return false;
     }
 
     public double tax() {
         return 0.0;
     }
 }
