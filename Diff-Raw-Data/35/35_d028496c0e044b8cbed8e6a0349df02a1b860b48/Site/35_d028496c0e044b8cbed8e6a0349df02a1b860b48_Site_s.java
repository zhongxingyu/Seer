 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package admin.src;
 
 import longcat.auction.src.AuctionCatalogue;
 import longcat.auction.src.CustomerCatalogue;
 import longcat.auction.src.SuperSite;
 
 /**
  *
 * @author lissal
  */
 public enum Site {
     INSTANCE;
     
     private final SuperSite site;
     
     private Site(){
        //s = ShopFactory.getShop(true);
        
        //TODO
        //Här ska nån cool metod som hämtar från databasen in.
     }
     
     public AuctionCatalogue getAuctionCatalogue() {
         return site.getAuctionCatalogue();
     }
     
     public CustomerCatalogue getCustomerCatalogue() {
         return site.getCustomerCatalogue();
     }
 }
