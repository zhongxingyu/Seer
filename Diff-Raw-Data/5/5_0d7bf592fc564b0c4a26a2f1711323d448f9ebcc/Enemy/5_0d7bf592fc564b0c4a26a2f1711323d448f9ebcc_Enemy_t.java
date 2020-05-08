 package spaceinvaders.entity;
 
 /**
  * Enemy Entities<br/>
  * Stores the entity data for all enemies
  * @author Brian Yang
  */
 public class Enemy extends MovableEntity implements Attacker, Defender {
    
     /** Base attack of Enemy */
     private Double attack;
     /** Base defense of Enemy */
     private Double defense;
     /** Weapon name used by Enemy */
     private String weapons;
     /** Weapon used by Enemy */
     private Weapon weapon;
     
     /**
      * Constructs a new enemy entity using a data file
      */    
     public Enemy() {
         // default values - should be ignored by the data file
         super("Panther Ship", 1337, "The most powerful and evil creature you will ever meet in your life.", 10.0, 10.0);
         attack = 9133.7; 
         defense = 9133.7;
         weapons = "Default";
         weapon = new Weapon();
     }
     
     /**
      * Constructs a new enemy entity
      * @param name name of entity
      * @param id index of entity
      * @param description description of entity
      * @param attack base attack of entity
      * @param defense base defense of entity
      * @param weapons name of weapon used by entity
      * @param vx x velocity
      * @param vy y velocity
      */    
     public Enemy(String name, int id, String description, double attack, double defense, String weapons, double vx, double vy) {
         super(name, id, description,vx,vy);
         this.attack = attack;
         this.defense = defense;
         this.weapons = weapons;
         weapon = EntityGroup.getWeapon(weapons);
     }
     
     /**
      * Fire weapon 
      */
    //@Override
     public void fire() {
         if(weapon == null) 
             weapon = EntityGroup.getWeapon(weapons);
         /* Use Bomb Formula 9001 */
         /* The graphics references will be included in the data file later, but for now... */
         weapon.fire(getX(), getY());
     }
     
     /**
      * Accessors for Attack
      * @return attack of entity
      */
     @Override
     public double getAttack() {
         return attack;
     }  
     
     /**
      * Accessors for Defense
      * @return attack of entity
      */
     @Override
     public double getDefense() {
         return defense;
     }  
     
     /**
      * Accessors for Weapon
      * @return weapon used by entity
      */
    //@Override
     public Weapon getWeapon() {
         return weapon;
     }
 }
