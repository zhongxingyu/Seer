 package edu.brown.cs32.goingrogue.gameobjects.items;
 
 import edu.brown.cs32.goingrogue.gameobjects.creatures.Attribute;
 import edu.brown.cs32.goingrogue.gameobjects.creatures.Creature;
 import edu.brown.cs32.goingrogue.gameobjects.creatures.CreatureStats;
 import edu.brown.cs32.goingrogue.util.CreatureSize;
 import java.awt.geom.Point2D;
 import java.util.List;
 import java.util.Objects;
 
 /**
  *
  * @author Ben Weedon (bweedon)
  */
 public class GridItem extends Creature {
 
     private boolean _pickedUp = false;
     private ItemStats _stats;
 
     public GridItem(Point2D.Double pos, String name, List<Attribute> attributes,
             ItemStats stats, String spritePath, CreatureSize size) {
         super(pos, 0.0, name, attributes, new CreatureStats(stats), spritePath, size);
         _stats = stats;
     }
 
     @Override
     public void incurDamage(Creature attacker) {
         // do nothing
     }
 
     @Override
     public int getHealth() {
         if (_pickedUp) {
             return 0;
         } else {
             return 1;
         }
     }
 
     @Override
     public boolean isItem() {
         return true;
     }
 
     public ItemStats getItemStats() {
         return _stats;
     }
 
     public void pickUp() {
         _pickedUp = true;
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 67 * hash + (this._pickedUp ? 1 : 0);
         hash = 67 * hash + Objects.hashCode(this._stats);
         return hash;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final GridItem other = (GridItem) obj;
         if (this._pickedUp != other._pickedUp) {
             return false;
         }
         if (!Objects.equals(this._stats, other._stats)) {
             return false;
         }
         return true;
     }
 }
