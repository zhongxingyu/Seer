 package edu.brown.cs32.goingrogue.gameobjects.creatures;
 
 import edu.brown.cs32.goingrogue.gameobjects.actions.Action;
 import edu.brown.cs32.goingrogue.gameobjects.creatures.util.CombatUtil;
 import edu.brown.cs32.goingrogue.gameobjects.items.Item;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Objects;
 
 /**
  *
  * @author Ben Weedon (bweedon)
  */
 public abstract class Creature implements Cloneable {
 
     private Point2D.Double _pos;
     private double _direction; // in radians
     private String _name;
     private List<Attribute> _attributes;
     private CreatureStats _stats;
     private String _spritePath;
     private Inventory _inventory;
     private List<Action> _actions;
     private int _level;
 
     public Creature(Point2D.Double pos, double direction, String name,
             List<Attribute> attributes, CreatureStats stats, String spritePath) {
         _pos = pos;
         _direction = direction;
         _name = name;
         _attributes = attributes;
         _stats = stats;
         _spritePath = spritePath;
         _inventory = new Inventory();
         _actions = new ArrayList<>();
         _level = 1;
     }
 
     public List<Attribute> getAttributes() {
         return _attributes;
     }
 
     public Point2D.Double getPosition() {
         return _pos;
     }
 
     public void setPosition(Point2D.Double pos) {
         _pos = pos;
     }
 
     public double getDirection() {
         return _direction;
     }
 
     public void setDirection(double direction) {
         _direction = direction;
     }
 
     public String getSpritePath() {
         return _spritePath;
     }
 
     public void setSpritePath(String spritePath) {
         _spritePath = spritePath;
     }
 
     public String getName() {
         return _name;
     }
 
     public void setName(String name) {
         _name = name;
     }
 
     public Stats getStats() {
         return _stats;
     }
 
     public Inventory getInventory() {
         return _inventory;
     }
 
     public double getSpeed() {
         return _stats.getSpeed();
     }
 
     public void addItem(Item item) {
         _inventory.add(item);
     }
 
     public Creature createNewInstance() throws CloneNotSupportedException {
         return (Creature) super.clone(); // TODO is call to super.clone() OK?
     }
 
     public int getHealth() {
         return _stats.getHealth();
     }
 
     public boolean isDead() {
         return _stats.getHealth() <= 0;
     }
 
     public double getWeaponRange() {
         return _inventory.getWeapon().getStats().getRange();
     }
 
     public double getWeaponArcLength() {
         return _inventory.getWeapon().getStats().getArcLength();
     }
 
     public int getWeaponAttackTimer() {
         return _inventory.getWeapon().getStats().getAttackTimer();
     }
 
     public void incurDamage(Creature attacker) {
         CombatUtil.incurDamage(attacker, this);
     }
 
     public boolean containsAttribute(Attribute attribute) {
         return _attributes.contains(attribute);
     }
 
     public int getLevel() {
         return _level;
     }
 
     public void incrementLevel() {
         ++_level;
     }
 
     public void decrementLevel() {
         --_level;
     }
 
     public List<Action> getActions() {
         try {
             return _actions;
         } finally {
            List<Action> removeActions = new ArrayList<>();
             for (Action action : _actions) {
                 if (action.getTimer() == 0) {
                    removeActions.add(action);
                 }
             }
            for (Action action : removeActions) {
                _actions.remove(action);
            }
         }
     }
 
     public void addAction(Action action) {
         _actions.add(action);
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 23 * hash + Objects.hashCode(this._pos);
         hash = 23 * hash + Objects.hashCode(this._name);
         hash = 23 * hash + Objects.hashCode(this._attributes);
         hash = 23 * hash + Objects.hashCode(this._stats);
         hash = 23 * hash + Objects.hashCode(this._spritePath);
         hash = 23 * hash + Objects.hashCode(this._inventory);
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
         final Creature other = (Creature) obj;
         if (!Objects.equals(this._pos, other._pos)) {
             return false;
         }
         if (!Objects.equals(this._name, other._name)) {
             return false;
         }
         if (!Objects.equals(this._attributes, other._attributes)) {
             return false;
         }
         if (!Objects.equals(this._stats, other._stats)) {
             return false;
         }
         if (!Objects.equals(this._spritePath, other._spritePath)) {
             return false;
         }
         if (!Objects.equals(this._inventory, other._inventory)) {
             return false;
         }
         return true;
     }
 
     public abstract boolean isItem();
 }
