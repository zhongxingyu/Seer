 /******************************************************
  * Copyright (C) 2012 Felix Wiemuth                   *
  * Licensed under the GNU GENERAL PUBLIC LICENSE      *
  * See LICENSE or http://www.gnu.org/licenses/gpl.txt *
  ******************************************************/
 
 package shoptool;
 
 /**
  *
  * @author Felix Wiemuth
  */
 public class PriceListItem implements PriceListItemInterface {
     private String id; //minecraft block id
     private String name; //minecraft block name
     //prices in relation to base value 10
     private int priceSell;
     private int priceBuy;
     private int stockUpdateTime; //time in seconds the current stock is changed by one item towards 'normalStock'
     private int normalStock; //amount of items the store tries to have
     private int maxStock; //the maximum amount of items the store can keep
 //    private String code;
 //    private static String codeDefault;
     
     @Override
     public String id() {
         return id;
     }
     
     @Override
     public String name() {
         return name;
     }
     
     @Override
     public int priceSell() {
         return priceSell;
     }
 
     @Override
     public int priceBuy() {
         return priceBuy;
     }
 
     @Override
     public int normalStock() {
        return normalStock();
     }
 
     @Override
     public int maxStock() {
        return maxStock();
     }
 
     @Override
     public int stockUpdateTime() {
        return stockUpdateTime();
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public void setMaxStock(int maxStock) {
         this.maxStock = maxStock;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public void setNormalStock(int normalStock) {
         this.normalStock = normalStock;
     }
 
     public void setPriceBuy(int priceBuy) {
         this.priceBuy = priceBuy;
     }
 
     public void setPriceSell(int priceSell) {
         this.priceSell = priceSell;
     }
 
     public void setStockUpdateTime(int stockUpdateTime) {
         this.stockUpdateTime = stockUpdateTime;
     }
     
     
 }
