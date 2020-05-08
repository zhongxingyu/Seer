 package com.abudko.scheduled.rules;
 
 import com.abudko.reseller.huuto.query.service.item.ItemResponse;
 import com.abudko.reseller.huuto.query.service.item.ItemStatus;
 import com.abudko.reseller.huuto.query.service.item.QueryItemService;
 
 public abstract class AbstractItemValidityRules implements ItemValidityRules {
 
     public abstract QueryItemService getQueryItemService();
     
     public abstract String getIdPrefix();
 
     @Override
     public boolean isValid(String id) {
         if (id == null) {
             return true;
         }
         
         String idPrefix = getIdPrefix();
         
         if (idPrefix != null) {
             return checkOthers(idPrefix, id);
         }
         else {
             return checkHuuto(id);
         }
     }
     
     private boolean checkHuuto(String id) {
         try {
             Long.parseLong(id);
             return getItemStatus("/" + id);
         }
        catch (NumberFormatException e) {
             return true;
         }
     }
     
     private boolean checkOthers(String idPrefix, String id) {
         if (id.substring(0, 2).equalsIgnoreCase(idPrefix)) {
             return getItemStatus(id.substring(2, id.length()));
         }
         return true;
     }
     
     private boolean getItemStatus(String id) {
         ItemResponse itemResponse = getQueryItemService().extractItem(id);
         return ItemStatus.OPENED.equals(itemResponse.getItemStatus());
     }
 }
