 package model.units;
 
 import model.attack.AttackDistance;
 import model.attack.AttackDistanceArcher;
 
 /**
  * @author Aurel
  * 
  */
 public class ArcherDragon extends Unit {
 
     public ArcherDragon() {
 	super(new AttackDistance(4), 200, 55, 80, 4, 15, 10, 0, Weapon.BOW,
 		"Archer");
 
     }
 
     @Override
     public void activatePower() {
 	pow = true;
	setIAttack(new AttackDistanceArcher(3));
     }
 
 }
