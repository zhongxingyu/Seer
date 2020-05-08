 package com.stuffsystem.rest;
 
 public interface InventoryDAL
 {
     /// Get the number of items in the database.
     public long getItemCount();
 
     /// Remove all documents from the items collection.
     public long removeAllItems();
 
     /// Reset items collection for test - clears Items and adds some test data.
     public long resetItemsCollectionForTest(String dataSetName);
 
    /// Post item
    public String postItem(String jsonItem);

 }
 
