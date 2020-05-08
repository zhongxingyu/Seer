 package com.ticktockdevelopment.simpleeventstore.Messaging.CommandHandlers;
 
 import com.ticktockdevelopment.simpleeventstore.Domain.InventoryItem;
 import com.ticktockdevelopment.simpleeventstore.Infrastructure.IHandler;
 import com.ticktockdevelopment.simpleeventstore.Infrastructure.IRepository;
 import com.ticktockdevelopment.simpleeventstore.Messaging.Commands.DeactivateInventoryItem;
import com.ticktockdevelopment.simpleeventstore.Messaging.Events.InventoryItemDeactivated;
 
 public class DeactivateInventoryItemCommandHandler implements IHandler<DeactivateInventoryItem>
 {
     private IRepository repository;
 
     public DeactivateInventoryItemCommandHandler(IRepository repository) {
             this.repository = repository;
     }
 
     @Override
     public <Y> boolean CanHandle(Y type) {
        return type instanceof InventoryItemDeactivated;
     }
 
    public void Handle(DeactivateInventoryItem message)
    {
         InventoryItem inventoryItem = repository.GetById(InventoryItem.class,message.InventoryItemId);
         inventoryItem.Deactivate();
         repository.Save(inventoryItem,message.OriginalVersion);
     }
 }
