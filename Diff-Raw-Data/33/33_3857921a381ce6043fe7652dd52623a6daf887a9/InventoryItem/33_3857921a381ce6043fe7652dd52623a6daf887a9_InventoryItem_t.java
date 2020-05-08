 package com.ticktockdevelopment.simpleeventstore.Domain;
 
 
 import com.ticktockdevelopment.simpleeventstore.Messaging.Events.Event;
 import com.ticktockdevelopment.simpleeventstore.Messaging.Events.InventoryItemCreated;
 import com.ticktockdevelopment.simpleeventstore.Messaging.Events.InventoryItemDeactivated;
 
 /**
  * Created with IntelliJ IDEA.
  * User: tchaplin
  * Date: 1/21/13
  * Time: 10:49 PM
  * To change this template use File | Settings | File Templates.
  */
 public class InventoryItem extends AggregateRoot {
 
     private boolean  activated;
 
     public InventoryItem() {
     }
 
     public InventoryItem(int id,String name) {
         ApplyChange(new InventoryItemCreated(id,name));
     }
 
     private void Apply(InventoryItemCreated event) {
         Id = event.Id;
         activated = true;
     }
 
     private void Apply(InventoryItemDeactivated event) {
         Id = event.Id;
         activated = false;
     }
 
     @Override
     protected void Apply(Event event) {
         if (event instanceof InventoryItemCreated)
         {
             Apply((InventoryItemCreated)event);
         }
         else if (event instanceof InventoryItemDeactivated)
         {
             Apply((InventoryItemDeactivated)event);
         }
         else
         {
             throw new IllegalArgumentException("UnknownEvent Type:"+event);
         }
     }
 
     public boolean isActivated() {
         return activated;
     }
 
     public void Deactivate()
     {
        if(!isActivated())
             return;//TODO: Add error
 
         ApplyChange(new InventoryItemDeactivated(Id));
     }
 }
