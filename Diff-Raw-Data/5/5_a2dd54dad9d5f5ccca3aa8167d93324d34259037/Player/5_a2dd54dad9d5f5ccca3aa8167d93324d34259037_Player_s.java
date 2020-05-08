 package edu.brown.cs32.goingrogue.gameobjects.creatures;
 
 import java.awt.geom.Point2D;
 import java.util.List;
 
 import edu.brown.cs32.goingrogue.gameobjects.actions.ArcAttackAction;
 import edu.brown.cs32.goingrogue.gameobjects.actions.MoveAction;
 import edu.brown.cs32.goingrogue.gameobjects.actions.PickupAction;
 import edu.brown.cs32.goingrogue.gameobjects.actions.PickupRange;
 import edu.brown.cs32.goingrogue.gameobjects.creatures.util.CombatUtil;
 import edu.brown.cs32.goingrogue.util.CreatureSize;
 
 /**
  *
  * @author Ben Weedon (bweedon)
  */
 public class Player extends Creature {
 
     public Player(Point2D.Double pos, double direction, String name,
             List<Attribute> attributes, CreatureStats stats, String sprite, CreatureSize size) {
         super(pos, direction, name, attributes, stats, sprite, size);
     }
 
     @Override
     public void incurDamage(Creature attacker) {
         CombatUtil.incurDamage(attacker, this);
     }
 
     @Override
     public boolean isItem() {
         return false;
     }
 
     public InputHandler getHandler() {
         return new InputHandler(this);
     }
 
     public class InputHandler {
     	
     	Player player;
     	
     	InputHandler(Player p) {
     		player=p;
     	}
     	
         public void moveUp() {
            addAction(new MoveAction(Math.PI/2, player));
         }
 
         public void moveRight() {
             addAction(new MoveAction(0, player));
         }
 
         public void moveDown() {
            addAction(new MoveAction(-Math.PI/2, player));
         }
 
         public void moveLeft() {
             addAction(new MoveAction(Math.PI, player));
         }
 
         public void attack() {
             addAction(new ArcAttackAction(getDirection(), getWeaponRange(), getWeaponArcLength(),
                     getWeaponAttackTimer(), Player.this));
         }
 
         public void pickUp() {
             addAction(new PickupAction(0, new PickupRange(Player.this), Player.this));
         }
     }
 }
