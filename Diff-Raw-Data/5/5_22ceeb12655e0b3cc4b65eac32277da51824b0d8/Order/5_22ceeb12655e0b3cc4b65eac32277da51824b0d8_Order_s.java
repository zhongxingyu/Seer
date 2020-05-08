 package com.acme.training.domain;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 
 public class Order {
    private String id;
     private String customer;
     private Address deliveryAddress;
     private Address billingAddress;
     private Map<Integer, OrderItem> itemMap = new HashMap<Integer, OrderItem>(); 
     
     @Override
     public String toString() {
         return "Order [id=" + id + ", customer=" + customer + ", deliveryAddress=" + deliveryAddress
                 + "]" + getFormattedItems();
     }
     private String getFormattedItems() {
         StringBuffer ret = new StringBuffer();
         for (OrderItem item : itemMap.values()) {
             ret.append(String.format("%n   %-25s : %3d", item.getFood().getName(), item.getQuantity()));
         }
         return ret.toString();
     }
     public String getId() {
         return id;
     }
     public void setId(String id) {
         this.id = id;
     }
     public String getCustomer() {
         return customer;
     }
     public void setCustomer(String customer) {
         this.customer = customer;
     }
     public Address getDeliveryAddress() {
         return deliveryAddress;
     }
     public void setDeliveryAddress(Address deliveryAddress) {
         this.deliveryAddress = deliveryAddress;
     }
     public Address getBillingAddress() {
         return billingAddress;
     }
     public void setBillingAddress(Address billingAddress) {
         this.billingAddress = billingAddress;
     }
     
     public void addItem(OrderItem item) {
         Food food = item.getFood();
         int quantity = item.getQuantity();
         OrderItem previousItem = itemMap.get(food.getId());
         if (null == previousItem) {
             itemMap.put(food.getId(), item);
         } else {
             previousItem.addQuantity(quantity);
         }         
     }
     public List<OrderItem> getItems() {
         List<OrderItem> ret = new ArrayList(itemMap.values());
         return ret;
     }
 }
