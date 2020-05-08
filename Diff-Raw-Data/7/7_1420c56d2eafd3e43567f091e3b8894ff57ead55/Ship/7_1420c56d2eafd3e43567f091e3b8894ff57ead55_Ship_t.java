 package ship;
 
 import graphics.Model;
 
 import java.util.List;
 
 import ship.shield.Shield;
 import ship.weapon.Weapon;
 
 import actor.Actor;
 
 public abstract class Ship extends Actor {
     private static final long serialVersionUID = -7048308038567858490L;
 
     private static final float DAMAGE_FACTOR = .25f;
 
    protected List<Weapon<? extends actor.Projectile>> weapons;
     protected List<Shield> shields; /* If we want to have different shield generators so front and rear shields are different */
     protected int selectedWeapon, hitPoints;
 
     public Ship(){
         super();
         modelName = Model.Models.FIGHTER;
         selectedWeapon = 0;
        weapons = new java.util.ArrayList<Weapon<? extends actor.Projectile>>();
         shields = new java.util.ArrayList<Shield>();
         hitPoints = 1000;
     }
     
     public Ship(math.Vector3f pos){
         this();
         setPosition(pos);
     }
 
     public void shoot(){
         weapons.get(selectedWeapon).shoot(this);
     }
 
     @Override
     public void handleCollision(Actor other) {
         if(other instanceof actor.Projectile) {
             // Don't collide with our own bullets
             if(other.getParentId().equals(id))
                 return;
             //shield testing code
             actor.Projectile projectile = (actor.Projectile) other;
             takeDamage(projectile.getDamage());
 
         } else if( other instanceof actor.Asteroid || other instanceof Ship){
             float otherKE = other.getMass() * other.getVelocity().magnitude2() * 0.5f;
             takeDamage(otherKE * DAMAGE_FACTOR);
         }
         if(hitPoints < 0)
             die();
     }
 
     public void takeDamage(float amount){
         //TODO When we have multiple shields find which shield to take damage on
         amount = shields.get(0).takeDamage((int)amount);
 
         if(shields.get(0).getStatus() == false){
             hitPoints -= amount;
         }
         System.out.println(shields.get(0).getStrength());
     }
 }
