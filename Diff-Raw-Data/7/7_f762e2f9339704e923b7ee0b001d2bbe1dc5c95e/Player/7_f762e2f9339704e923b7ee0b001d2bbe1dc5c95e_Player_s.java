 package dk.cphbusiness.models;
 
 import java.util.Collections;
 import java.util.List;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Lists;
 
 public class Player {
     private Dungeon currentDungeon;
     private List<Item> items;
 
     public Player(Dungeon currentDungeon) {
         this.items = Lists.newArrayList();
 
         setCurrentDungeon(currentDungeon);
     }
 
     public Dungeon getCurrentDungeon() {
         return currentDungeon;
     }
 
     public void setCurrentDungeon(Dungeon currentDungeon) {
         Preconditions.checkNotNull(currentDungeon,
                 "Current dungeon may not be null");
 
         this.currentDungeon = currentDungeon;
     }
 
     public List<Item> getItems() {
         return items;
     }
 
     public void setItems(List<Item> items) {
         Preconditions.checkNotNull(items, "Items list may not be null");
 
         this.items = items;
     }
 
     public void addItem(Item item) {
        Preconditions.checkNotNull(currentDungeon, "Item may not be null");

        this.items.add(item);
     }
     
     public void addItems(Item... items) {
        Preconditions.checkNotNull(currentDungeon, "Items may not be null");
         
         Collections.addAll(this.items, items);
     }
     
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result
                 + ((currentDungeon == null) ? 0 : currentDungeon.hashCode());
         result = prime * result + ((items == null) ? 0 : items.hashCode());
         return result;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         Player other = (Player) obj;
         if (currentDungeon == null) {
             if (other.currentDungeon != null) {
                 return false;
             }
         } else if (!currentDungeon.equals(other.currentDungeon)) {
             return false;
         }
         if (items == null) {
             if (other.items != null) {
                 return false;
             }
         } else if (!items.equals(other.items)) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "Player [currentDungeon=" + currentDungeon + ", items=" + items
                 + "]";
     }
 }
