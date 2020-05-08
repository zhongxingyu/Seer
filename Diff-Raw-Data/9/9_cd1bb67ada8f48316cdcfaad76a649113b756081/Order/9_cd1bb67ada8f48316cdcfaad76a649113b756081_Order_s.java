 package demo.modernweb.domain;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Order {
 
     private Integer id;
     private DeliveryAddress deliveryAddress;
     private String shipmentMethod;
     private List<OrderItem> items = new ArrayList<OrderItem>();
 
     public Order() {
     }
 
     public Order(Integer id, DeliveryAddress deliveryAddress, String shipmentMethod, List<OrderItem> items) {
         this.id = id;
         this.deliveryAddress = deliveryAddress;
         this.shipmentMethod = shipmentMethod;
         this.items = items;
     }
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     public DeliveryAddress getDeliveryAddress() {
         return deliveryAddress;
     }
 
     public void setDeliveryAddress(DeliveryAddress deliveryAddress) {
         this.deliveryAddress = deliveryAddress;
     }
 
     public String getShipmentMethod() {
         return shipmentMethod;
     }
 
     public void setShipmentMethod(String shipmentMethod) {
         this.shipmentMethod = shipmentMethod;
     }
 
     public List<OrderItem> getItems() {
         return items;
     }
 
     public void setItems(List<OrderItem> items) {
         this.items = items;
     }
 }
