 package org.gdc.gdcalaga;
 import java.util.Random;
 
 import org.gdc.gdcalaga.Upgrade.UpgradeType;
 import org.gdc.gdcalaga.audio.AudioAsset;
 import org.gdc.gdcalaga.audio.AudioManager;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Vector2f;
 
 
 public class Enemy extends Entity
 {
     private static final int MAX_FIRE = 1500;
     private static final int SIZE_WIDTH = 41;
     private static final int SIZE_HEIGHT = 32;
     
     protected Vector2f velocity;
     protected float health;
     protected int pointValue;
     Image ship;
     private Explosion exp;
     private boolean exploding, pathing;
     
     private Path path;
     private PathNode node;
     private Vector2f startPos, pathVelocity;
     private float fireRate;
     private float fireRateDT;
     private Random rand;
     
     private EnemyGroup group;
     private boolean grouped;
     public Vector2f relPos;
 
     protected AudioManager audioManager;
     
     public Enemy(EntityManager manager, Vector2f position)
     {   
         super(manager);
         pos.set(position);
         size = new Vector2f(SIZE_WIDTH, SIZE_HEIGHT);
         velocity = new Vector2f(0, 100);
         health = 3;
         alliance = Alliance.ENEMY;
         pointValue = 10;
         shape = new RectShape(pos, size);
         
         pathing = false;
         grouped = false;
         relPos = new Vector2f(0, 0);
         
         startPos = new Vector2f(0, 0);
         pathVelocity = new Vector2f(0, 0);
         
         rand = new Random(System.currentTimeMillis());
         fireRate = rand.nextInt(MAX_FIRE);
         fireRateDT = 0;
 
         try {
             ship = new Image("Pics/Enemy.png");
         } catch (SlickException e) {
             e.printStackTrace();
         }
         
         audioManager = AudioManager.getAudioManager();
     }
     
     public void setPath(Path newPath)
     {
         path = newPath;
     	
     	//Spawn at the first node in the path
     	PathNode spawnPos = path.next();
     	pos.set(spawnPos.goalPos);
     	
     	//Then start moving toward the next node
     	if(path.hasNext())
     	{
     	    node = path.next();
     	    startPos.set(pos);
     	    pathing = true;
     	}
     }
     
     public void setGroup(EnemyGroup g, Vector2f relativePosition)
     {
         group = g;
     	grouped = true;
     	relPos.set(relativePosition);
     	pos.x = relPos.x + group.pos.x;
     	pos.y = relPos.y + group.pos.y;
 
     	g.addEnemy(this);
     }
     
     
     public void update(float delta)
     {
         if(exploding)
         {
             exp.update(delta);
             if(exp.IsDead())
             	Destroy();
         }
         
         else
         {
             fireRateDT += delta;
         	
             if (fireRateDT > fireRate)
             {
                 fire();
                 fireRateDT = 0;
                 fireRate = rand.nextInt(MAX_FIRE);
             }
             move(delta);
         }
         
         
         RectShape rect = (RectShape)shape;
         rect.pos.set(this.pos);
         
        if(pos.x < -800 || pos.x > 3000 || pos.y < -800 || pos.y >3000){
         	Destroy();
         }
     }
     
     
     
     public void draw(Graphics g)
     {
         int drawX = (int)(pos.x - SIZE_WIDTH / 2);
         int drawY = (int)(pos.y - SIZE_HEIGHT / 2);
         float scale = SIZE_WIDTH / ship.getWidth();
         if(!exploding){
             ship.draw(drawX, drawY, scale, Color.white);
         } else {
             exp.render(g);
         }
     }
 
     public void Collide(Entity other)
     {
         if(other instanceof Bullet && ((Bullet)other).getAlliance()!=alliance)
     	{
     	    Hurt(((Bullet)other).getDamage());
     	}
         
     }
     
     public void fire()
     {
         Vector2f bulletPosition = new Vector2f((pos.x - size.x / 2) + 3, pos.y);
         Bullet newBullet = new Bullet(entities, bulletPosition, 1, alliance);
         newBullet.setSpeed(-250, 0);
         audioManager.playSFX(AudioAsset.SFX_FIRE2);
     }
     
     
     public void move(float delta)
     {
         if(!pathing)
         {
             if(grouped)
             {
                 pos.x = group.pos.x + relPos.x;
                 pos.y = group.pos.y + relPos.y;
             }
             else
             {
                 pos.x += velocity.x * delta / 1000;
                 pos.y += velocity.y * delta / 1000;
 
                 if(pos.y < 0)
                 {
                     pos.y = 0;
                     velocity.y *= -1;
                 }
 	            
                 else if(pos.y > 720)
                 {
                     pos.y = 720;
                     velocity.y *= -1;
                 }
             }
         }
         else
         {
             pathVelocity.x = (node.goalPos.x - startPos.x) / node.speed;
             pathVelocity.y = (node.goalPos.y - startPos.y) / node.speed;
             pos.x += pathVelocity.x * delta / 1000;
             pos.y += pathVelocity.y * delta / 1000;
     		
             if (pathVelocity.x * (node.goalPos.x - pos.x) < 0 
              || pathVelocity.y * (node.goalPos.y - pos.y) < 0)
             {
                 pos.set(node.goalPos);
                 startPos.set(pos);
     			
                 if (path.hasNext())
                 {
                     node = path.next();
                     if (grouped && !path.hasNext())
                     {
                         node.goalPos.x = group.pos.x + relPos.x;
                         node.goalPos.y = group.getYAfterTime(node.speed) + relPos.y;
                     }
                 }
                 else
                 {
                     pathing = false;
                 }
             }
         }
     }
     
     private void Explode()
     {
         shape.type = Shape.ShapeType.Null;
 
         exp = new Explosion(pos, 41, 8, size);
         exp.SetImage(ship);
         
         exploding = true;    
         
         if(grouped) group.removeEnemy(this);
         Player.increaseTotalPoints(pointValue);
     }
     
     
     public void Hurt(float dmg)
     {
         health -= dmg;
         if(health <= 0) 
         {
             Explode();
             tryDropUpgrade();
         }
     }
     
     /*
      * Generates a random number to see if it is a ripe time to drop
      * an upgrade
      */
     private void tryDropUpgrade()
     {
         final double decimalChance = 0.05; // TODO Make static final
         if (Math.random() < decimalChance)
         {
             //generate a random upgrade
             int numUpgrades = UpgradeType.getUpgradeCount();            
             Upgrade.UpgradeType type = UpgradeType.getIndexedUpgrade((int) (numUpgrades * Math.random()) + 1); // Add 1 because the indices start at 1
             new Upgrade(entities, this.pos, type);
         }
     }
 }
