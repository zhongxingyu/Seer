 package com.macbury.unamed.inventory;
 
 import java.util.ArrayList;
 
 public class InventoryManager extends ArrayList<InventoryItem> {
   private static final long serialVersionUID = 1L;
   public final static int MIN_HOTBAR_INVENTORY_INDEX = 1;
   public final static int MAX_HOTBAR_INVENTORY_INDEX = 10;
   
   public int currentHotBarInventoryIndex = 0;
   
   public InventoryManager() {
     super();
   }
 
   public InventoryItem getCurrentHotBarItem() {
     return this.getItem(currentHotBarInventoryIndex);
   }
   
   public void setInventoryIndex(int index) {
     index = Math.min(index, MAX_HOTBAR_INVENTORY_INDEX);
     index = Math.max(index, MIN_HOTBAR_INVENTORY_INDEX);
     this.currentHotBarInventoryIndex = index-1;
   }
 
   public int getCurrentHotBarIndex() {
     return this.currentHotBarInventoryIndex;
   }
 
   public InventoryItem getItem(int i) {
     try {
       return get(i);
     } catch(IndexOutOfBoundsException e) {
       return null;
     }
   }
   
   public void addItem(InventoryItem item) {
     for (InventoryItem itemBackPack : this) {
      if (item.getKey().equals(itemBackPack.getKey())) {
         itemBackPack.addItem(item.getQuantity());
         return;
       }
     }
     
     add(item);
   }
 }
