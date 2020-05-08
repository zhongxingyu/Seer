 package edu.brown.cs32.goingrogue.gameobjects.actions;
 
 import edu.brown.cs32.goingrogue.gameobjects.creatures.Creature;
 import edu.brown.cs32.goingrogue.util.Util;
 
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Objects;
 
 /**
  *
  * @author Ben Weedon (bweedon)
  */
 public class ArcAttackAction extends Action {
 	
     private Creature _sourceCreature;
     
     public ArcAttackAction(double direction, double distance, double arcLength, int timer, Creature sourceCreature) {
         super(timer, new ArcAttackRange(direction, distance, arcLength, timer));
         
         _type=ActionType.ATTACK;
         _sourceCreature = sourceCreature;
     }
 
     @Override
     public void act(Creature creature) {
         creature.incurDamage(_sourceCreature);
     }
 
     @Override
     public List<ActionAnimation> getActionAnimations() {
         String creatureSpritePath = _sourceCreature.getSpritePath();
         String weaponSpritePath = _sourceCreature.getInventory().getWeapon().getSpritePath();
         
         Point2D.Double creaturePos = _sourceCreature.getPosition();
         double creatureAngle = ((ArcAttackRange) getRange()).getAngle();
         
        double[] weaponPosTemp = new double[]{creaturePos.getX(), creaturePos.getY()};
        double[] weaponPosPolar = Util.rectangularToPolar(weaponPosTemp[0], weaponPosTemp[1]);
         weaponPosPolar[0]+=.5;
        weaponPosTemp = Util.polarToRectangular(weaponPosPolar[0], weaponPosPolar[1]);
         Point2D.Double weaponPos = new Point2D.Double(weaponPosTemp[0], weaponPosTemp[1]);
         
         ActionAnimation creatureAnimation=new ActionAnimation(creatureSpritePath, creaturePos, creatureAngle);
         ActionAnimation weaponAnimation=new ActionAnimation(weaponSpritePath, weaponPos, creatureAngle);
         List<ActionAnimation> list=new ArrayList<>();
         list.add(creatureAnimation);
         list.add(weaponAnimation);
         return list;
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 79 * hash + Objects.hashCode(this._sourceCreature);
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
         final ArcAttackAction other = (ArcAttackAction) obj;
         if (!Objects.equals(this._sourceCreature, other._sourceCreature)) {
             return false;
         }
         return true;
     }
 }
