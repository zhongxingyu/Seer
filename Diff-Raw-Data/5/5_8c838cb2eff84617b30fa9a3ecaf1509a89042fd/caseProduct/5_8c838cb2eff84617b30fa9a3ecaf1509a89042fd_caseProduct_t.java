 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package beans;
 
 import com.sun.istack.internal.NotNull;
 import data.Product;
 import data.Storage;
 import exceptions.StorageException;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 import javax.faces.context.FacesContext;
 
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
   @NotNull
   private String description;
   @NotNull
   private String manufacturer;
   private int addProductID;
   private Product selectedProduct;
   private String nameUsed;
 
   public caseProduct() {
     //this.name ="NewTestName";
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
    if ( ! isProductExist(name) ) {
       this.name = name;
     }
   }
   
   public boolean checkNameSet () {
    if (this.name != null && ! this.name.equals("")) {
       return true;
     }
     return false;
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
 }
