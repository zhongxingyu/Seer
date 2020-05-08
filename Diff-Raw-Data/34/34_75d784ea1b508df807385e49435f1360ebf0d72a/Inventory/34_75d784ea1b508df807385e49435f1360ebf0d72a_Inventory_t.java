 package com.icbat.game.tradesong;
 
 import java.util.ArrayList;
 
 public class Inventory {
 
     int capacity = 15;
     private ArrayList<StackedItem> stacks = new ArrayList<StackedItem>(capacity);
 
     Inventory() {}
 
     public boolean canAdd(Item newItem) {
         for(StackedItem stackedItem : stacks) {
             // If we found a stack of that item
             if (stackedItem.getBaseItem().getItemName().equals(newItem.getItemName())) {
                 // See if we can add to it
                 if (stackedItem.getCount() == stackedItem.getBaseItem().getMaxStack()) {
                     // Note: this needs to be return true; and not return stackedItem.add();
                     //       This is because if it's false, we don't want to quit looking, we want to check the next stack, and so on.
                     return true;
                 }
             }
         }
 
         // We didn't find a stack. Can we make a new one?
         return stacks.size() < capacity;
     }
 
     public boolean add(Item newItem) {
         for(StackedItem stackedItem : stacks) {
             // If we found a stack of that item
             if (stackedItem.getBaseItem().getItemName().equals(newItem.getItemName())) {
                 // See if we can add to it
                 if (stackedItem.add()) {
                     // Note: this needs to be return true; and not return stackedItem.add();
                     //       This is because if it's false, we don't want to quit looking, we want to check the next stack, and so on.
                     return true;
                 }
             }
         }
 
         // We didn't find a stack. Can we make a new one?
         if (stacks.size() >= capacity) {
             // Nope. We're already full.
             return false;
         }
         else {
             // Sure! Add a new one.
             stacks.add(new StackedItem(newItem));
             return true;
         }
 
 
     }
 
     public StackedItem getStack(int i) {
         return stacks.get(i);
     }
 
     public Item getItemType(int i) {
         return stacks.get(i).getBaseItem();
     }
 
     public int size() {
         return stacks.size();
     }
 
     public int capacity() {
         return capacity;
 
     }
 
     public ArrayList<StackedItem> getStacks() {
         return stacks;
     }
 
     public void remove(StackedItem stack) {
         stacks.remove(stack);
     }
 
 }
