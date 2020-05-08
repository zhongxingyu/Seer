 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package beans;
 
import com.sun.istack.internal.NotNull;
 import data.Product;
 import data.Storage;
 import exceptions.StorageException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 import javax.faces.context.FacesContext;
//import javax.validation.constraints.*;
 
 /**
  * (name = "caseProduct1")
  *
  * @author christianlinde
  */
 @ManagedBean
 @RequestScoped
 public class caseProduct {
 
     @NotNull
     private String name;
     @NotNull
     private float price;
     //private String priceString;
     @NotNull
     private String description;
     @NotNull
     private String manufacturer;
     private int addProductID;
     private Product selectedProduct;
     private String nameUsed;
 
     public caseProduct() {
         //this.name ="";
         this.nameUsed = "";
     }
 
     /*
      * Checks if the product exists by name.
      * 
      * @return Returns true if the product exists otherwise false.
      */
     public boolean isProductExist(String name) {
         for (Product p : data.Storage.getInstance().getAllProducts()) {
             if (p.getName().equals(name)) {
                 this.nameUsed = "Name in use";
                 return true;
             }
         }
         return false;
     }
 
     public int addProduct(String name, float price, String description, String manufactorer) throws StorageException {
         return data.Storage.getInstance().addProduct(new data.Product(name, price, description, manufactorer));
     }
 
     public void loadProductByID(int id) {
         this.selectedProduct = data.Storage.getInstance().getProductById(id);
 
         if (selectedProduct != null) {
             this.addProductID = id;
             this.name = selectedProduct.getName();
             this.price = selectedProduct.getPrice();
             this.description = selectedProduct.getDescription();
             this.manufacturer = selectedProduct.getManufacturer();
         }
     }
 
     public String deleteProduct() {
 
         data.Storage.getInstance().deleteProductById(this.addProductID);
 
         if (data.Storage.getInstance().getProductById(addProductID) == null) {
             return "/ViewProduct.jsp";
         } else {
             FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler Product not deleted: " + addProductID, "Product not deleted");
             FacesContext.getCurrentInstance().addMessage("form", msg);
             return null;
         }
     }
 
     public boolean deleteProductByID(int id) {
 
         data.Storage.getInstance().deleteProductById(id);
 
         if (data.Storage.getInstance().getProductById(addProductID) == null) {
             return true;
         } else {
             return false;
         }
     }
 
     public int getAddProductID() {
         return addProductID;
     }
 
     public void setAddProductID(int addProductID) {
         this.addProductID = addProductID;
     }
 
     public String getName() {
         return this.name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String checkNameSet() {
         if (isProductExist(this.name)) {
             return "Product exists";
         } else {
             return "";
         }
     }
 
     public float getPrice() {
         return this.price;
     }
 
     public String getDescription() {
         return this.description;
     }
 
     public String getManufacturer() {
         return this.manufacturer;
     }
 
     public String getPriceString() {
         return String.valueOf(price) + " €"; // priceString
     }
 
     public void setPriceString(String pS) {
         this.price = 0;
         //this.priceString = pS;
         //this.price = Float.parseFloat(pS.split(" ")[0]);
         if (!((String) pS).equals("")) {
             String[] splitString = ((String) pS).split(" ");
             if (splitString[1].equals("€")) {
                 this.price = Float.parseFloat(splitString[0]);
             } else if (splitString[1].equals("$")) {
                 // Umrechnung Doller in Euro
                 this.price = Float.parseFloat(splitString[0]) * 0.75f;
             } else {
                 this.price = Float.parseFloat(splitString[0]);
             }
         }
     }
 
     public void setPrice(float price) {
         this.price = price;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public void setManufacturer(String manufacturer) {
         this.manufacturer = manufacturer;
     }
 
     public String insertNewProduct() {
         try {
             this.addProductID = data.Storage.getInstance().addProduct(new Product(this.name, this.price, this.description, this.manufacturer));
             return "/ViewProduct.jsp";
         } catch (StorageException ex) {
             Logger.getLogger(caseProduct.class.getName()).log(Level.SEVERE, null, ex);
             return null;
         }
     }
 
     public String updateProduct() {
         Product tmpProduct = data.Storage.getInstance().getProductById(this.addProductID);
         if (tmpProduct != null) {
             tmpProduct.setName(this.name);
             tmpProduct.setPrice(this.price);
             tmpProduct.setDescription(this.description);
             tmpProduct.setManufacturer(this.manufacturer);
 
             data.Storage.getInstance().setProduct(tmpProduct);
 
             return "/ViewProduct.jsp"; //action="ViewProduct.jsp" actionListener
         } else {
             FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler ID not found: " + addProductID, "Product not changed");
             FacesContext.getCurrentInstance().addMessage("form", msg);
             return null;
         }
     }
 
     public List<Product> getAllProducts() {
         return data.Storage.getInstance().getAllProducts();
     }
 
     public List<webservice.Product> getAllProductsWS() {
         webservice.WebEntw_Service testWS = new webservice.WebEntw_Service();
         webservice.WebEntw testWP = testWS.getWebEntwPort();
 
 
         return testWP.products();
     }
 }
