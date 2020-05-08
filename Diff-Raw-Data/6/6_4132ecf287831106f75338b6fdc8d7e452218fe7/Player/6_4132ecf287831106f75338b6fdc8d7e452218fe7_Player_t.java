 package org.gdc.gdcalaga;
 import org.gdc.gdcalaga.Upgrade.UpgradeType;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Vector2f;
 
 /*
  * Upgrades planned for the player:
  * Droppable: Health, shields, fire rate, more bullets, damage
  * Buyable: Speed 
  * 
  * TODO
  * Store the upgrades in an easily retrievable, searchable, and identifiable manner
  *  We want to have levels of upgrades, so this should be considered
  * Create a shop at the end of a wave/stage (or something like that)
  *  for the player to buy upgrades.
  * Create functionality for the player to be able to activate shields/invuln mode
  * Create functionality for the player to have more guns
  */
 
 public class Player extends Entity
 {
     private static final int SIZE_WIDTH = 49;
     private static final int SIZE_HEIGHT = 29;
 	
     private static int totalPoints = 0; //the score is static in case we give players multiple lives in the future
     Image ship;
     
     //Upgradable Player attributes
     private static float health;
     private static float shields;
     private static float fireRate; //fireRate in bullets per second
     private static float damage;
     protected Vector2f velocity;
     
     //these values are intertwined with fireRate to create a standard fire rate
     private static int ticksPerBullet;
     private int ticksSinceLastBullet;
     
     public Player(EntityManager manager, Vector2f position)
     {
         super(manager);
 
         pos.set(position);
         size = new Vector2f(SIZE_WIDTH, SIZE_HEIGHT);
         velocity = new Vector2f(220, 220);
         damage=1;
         alliance = Alliance.FRIENDLY;
         
         health = 10;
         shields = 0;
        fireRate = 20;   //bullets per second
         ticksPerBullet = (int)(1000 / fireRate);   //milliseconds in a second / fireRate
                                                    //since delta time is in milliseconds
         ticksSinceLastBullet = ticksPerBullet;  //so we can shoot right off the bat
         
         shape = new RectShape(pos, size);
         
         try {
             ship= new Image("Pics/Player.png");
         } catch (SlickException e) {
             e.printStackTrace();
         }
     }
     
     
     public void update(float delta)
     {
     	RectShape rect = (RectShape)shape;
         rect.pos.set(this.pos);
     }
     
     public void draw(Graphics g)
     {
         int drawX = (int)(pos.x - size.x / 2);
         int drawY = (int)(pos.y - size.y / 2);
         float scale = size.x / ship.getWidth();
         ship.draw(drawX, drawY, scale, Color.white);
     }
     
     
     public void moveUp(float delta)
     {
         pos.y -= velocity.y * delta / 1000;
         pos.y = Math.max(size.y / 2, pos.y);
     }
     
     public void moveDown(float delta)
     {    
         pos.y += velocity.y * delta / 1000;
         pos.y = Math.min(720 - size.y / 2, pos.y);
     }
     
     public void moveLeft(float delta)
     {
         pos.x -= velocity.x * delta / 1000;
         pos.x = Math.max(0 + size.x / 2, pos.x);
     }
     
     public void moveRight(float delta)
     {
         pos.x += velocity.x * delta / 1000;
         pos.x = Math.min(1280 - size.x / 2, pos.x);
     }
     
     public boolean fire(float delta)
     {
         //TODO add more firing positions and more bullets depending on the upgrades
         ticksSinceLastBullet += delta;
         if (ticksSinceLastBullet >= ticksPerBullet)
         {
             Vector2f position = new Vector2f(pos.x + size.x / 2, pos.y);
             Bullet newBullet = new Bullet(entities, position, (int)(damage), alliance);
             newBullet.setSpeed(500, 0);
             
             ticksSinceLastBullet = 0;
             return true;
         }
         else 
         {
             return false;
         }
             
     }
     
     public void Collide(Entity other)
     {
     	if(other instanceof Bullet && ((Bullet)other).getAlliance() != alliance)
     	{
     	    Hurt(((Bullet)other).getDamage());
     	}
     }
     
     public void Hurt(float dmg){
         health-=dmg;
     }
     
     public float getHealth()
     {
         return health;
     }
     
     public Entity.Alliance getAlliance()
     {
         return alliance;
     }
     
     public static int getTotalPoints()
     {
     	return totalPoints;
     }
     
     public static void increaseTotalPoints(int pointValue)
     {
     	totalPoints += pointValue;
     }
     //my idea here is that ship upgrades will cost points to buy, like in most games.
     public static void decreaseTotalPoints(int pointValue)
     {
     	if( (totalPoints - pointValue) >= 0)
     	{
     	    totalPoints -= pointValue;
     	}
     	else
     	    System.out.println("Can't spend any more points!"); //TODO Fix error message
     }
     
     public static void upgrade(Upgrade.UpgradeType upgrade)
     {
         //TODO Change the health, hp/armor, fire rate, amount of guns, speed
         //according to the upgrade passed in
     	switch (upgrade)
         {
         case HEALTH:
             health+=5;
             
             break;
         case SHIELD:
             shields+=3;
             break;
         case FIRE_RATE:
            fireRate +=1;
            ticksPerBullet = (int)(1000 / fireRate); //A little bit weird that we have to do these two things...
             break; 
         case NUM_GUNS:
             
             break;
         case DAMAGE:
             damage++;
             break;
         case INVALID_UPGRADE:
             
             break;
         default:
             
             break;  
         }
     } 
 }
