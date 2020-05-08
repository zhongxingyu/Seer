 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package pkgOnlineShop;
 
 import java.util.ArrayList;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.SessionScoped;
 
 /**
  *
  * @author matthias
  */
 
 public class ProductsController {
     
     private ArrayList<Produkt> products;
     private ArrayList<Produkt> randomProducts;
     private Produkt selectedProduct;
     private int amount = 1;
     private String userNotLoggedIn;
     
     private Database db;
     
     private MainController mainController;
     
     public ProductsController() {
         
     }
 
     public String addToCart() {
     		String retVal = "";
         try {
             if(db.isProductAvailable(selectedProduct, amount)) {
              db.addToCart(amount, selectedProduct.getId(), mainController.getUser().getId());
         		retVal = "cart";
             }
             else {
                mainController.setMessage("Produkt in dieser Anzanhl nicht mehr vorhanden");
             }
         }
         catch(Exception e) {
             System.out.println(e);
         }
         return retVal;
     }
     
     public String getUserNotLoggedIn() {
     		String retVal = "true";
     		
     		if(mainController.getUser() != null) {
     			retVal = "false";
     		}
     		    			
     		return retVal;
     }
     
     public void setUserNotLoggedIn(String v) {
     		this.userNotLoggedIn = v;
     }
     
     public Database getDb() {
         return db;
     }
 
     public void setDb(Database db) {
         this.db = db;
     }
 
     public ArrayList<Produkt> getProducts() throws Exception {
         products = db.getProductsForCategory(mainController.getSelectedKategorie());
         return products;
     }
 
     public void setProducts(ArrayList<Produkt> products) {
         this.products = products;
     }
 
     public MainController getMainController() {
         return mainController;
     }
 
     public void setMainController(MainController mainController) {
         this.mainController = mainController;
     }
 
     public Produkt getSelectedProduct() {
         return selectedProduct;
     }
 
     public void setSelectedProduct(Produkt selectedProduct) {
         this.selectedProduct = selectedProduct;
         System.out.println("selectedProduct: "+selectedProduct.toString());
     }
 
     public int getAmount() {
         return amount;
     }
 
     public void setAmount(int amount) {
         this.amount = amount;
     }
 
 	public ArrayList<Produkt> getRandomProducts() throws Exception {
 		randomProducts = db.getRandomProducts();
 		System.out.println("randomProducts: "+randomProducts.size());
 		return randomProducts;
 	}
 
 	public void setRandomProducts(ArrayList<Produkt> randomProducts) {
 		this.randomProducts = randomProducts;
 	}
     
     
 }
