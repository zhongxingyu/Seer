 package spaceshooters.entities;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.SpriteSheet;
 
 import spaceshooters.entities.technical.Bullet;
 import spaceshooters.level.Level;
 
 public class EnemyShip extends Entity {
 	
 	private EnumEntityType[] collidables = { EnumEntityType.BULLET, EnumEntityType.ASTEROID };
 	
 	public EnemyShip(Level level, int startX, int startY) throws SlickException {
 		super(level, startX, startY, EnumEntityType.ENEMY_SHIP);
 		this.texture = new SpriteSheet("gfx/sprites.png", 45, 45, 1).getSprite(2, 0);
 		this.width = 36;
 		this.height = 30;
 		this.health = 2;
 		this.velocity = 0.25F;
 	}
 	
 	@Override
 	public void update(GameContainer container, int delta) throws SlickException {
		if ((health <= 0) || (y >= container.getHeight())) {
 			this.setDead();
 		}
 		
 		this.collisionCheck(collidables);
 		
 		y += velocity;
 	}
 	
 	@Override
 	public void onCollide(Entity entity) {
 		if (entity.getEntityType() == EnumEntityType.BULLET) {
 			Bullet b = (Bullet) entity;
 			if (!b.doesMoreDamage()) {
 				this.health -= 1;
 			} else {
 				this.health -= 2;
 			}
			if (this.health <= 0) {
 				this.getLevel().getPlayer().score++;
 			}
 		}
 		
 		if (entity.getEntityType() == EnumEntityType.ASTEROID) {
 			this.setDead();
 		}
 	}
 }
