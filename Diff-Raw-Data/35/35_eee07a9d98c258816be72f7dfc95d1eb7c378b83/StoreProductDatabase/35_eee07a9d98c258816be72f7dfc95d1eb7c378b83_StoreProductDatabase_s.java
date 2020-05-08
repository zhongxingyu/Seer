 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package version1;
 
 /**
  *
  * @author Nathaniel
  */
 public class StoreProductDatabase implements Database{
     private final int ARRAY_START = 0;
     private Product[] products;
     
     public StoreProductDatabase(){
         products = new Product[0];
        Product product = new ClothingProduct(new FlatRateDiscount(.1), "ABC123", "Blue Levi", "Blue jeans", 12.98, "Levi", ClothingSize.MEDIUM);
         addProduct(product);
         product = new ElectronicProduct(new FlatRateDiscount(.2), "RAD456", "Rad Blaster", "Red lasor gun", 34.95, "Alkaid", 2);
         addProduct(product);
         product = new ElectronicProduct(new FlatRateDiscount(.2), "NDS1", "Nintendo DS FAT", "Collapsable Portable Game System", 149.99, "Nintendo", 2);
         addProduct(product);
         product = new ClothingProduct(new FlatRateDiscount(.1), "SHO83", "Green shorts", "Green shorts with four pockets in front and two in back", 10.99, "Juniper", ClothingSize.EXTRA_LARGE);
         addProduct(product);
     }
 
     public void addProduct(Product product){
         Product[] temp = new Product[products.length + 1];
         System.arraycopy(products, ARRAY_START, temp, ARRAY_START, products.length);
         temp[temp.length-1] = product;
         products = temp;
     }
     
     public Product retrieveProduct(String productID){
         int productIndex = -1;
         
         for(int i = 0; i < products.length; i++){
             if(products[i].getProductID().equals(productID)){
                 productIndex = i;
             }
         }
         
         return products[productIndex];
     }
 }
