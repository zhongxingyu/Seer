 package items.projectiles;
 
 import map.Cell;
 import map.Tile;
 import map.tileproperties.TileProperty;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Shape;
 import org.newdawn.slick.geom.Transform;
 
 import utils.MapLoader;
 import utils.Position;
 import entities.DestructibleEntity;
 import entities.MovingEntity;
 import entities.StaticEntity;
 import entities.VeryAbstractEntity;
 import game.config.Config;
 
 public class Projectile extends VeryAbstractEntity {
 	
 	private static final int PROJECTILE_DEFAULT_LAYER = -500;
 	
 	private static final float NORMAL_SPEED = 0.5f;
 	private static final float MAX_SPEED = 0.5f;
 	
 	private Animation moving;
 	private Animation sprite;
 	private final Position dxdy;
 	private final int damage;
 	private double angle;
 	private Shape hitbox;
 	
 	public Projectile(Position centre, int damage, double angle, float speed){
 		Image[] movementForward = null;
 		float width = 0f,height = 0f;
 		try {
 			Image projImage = new Image("data/images/projectile.png");
 			width  = (float) projImage.getWidth()  / Config.getTileSize();
 			height = (float) projImage.getHeight() / Config.getTileSize();
 			projImage.rotate((float)(angle * 180/Math.PI));
 			movementForward = new Image[]{projImage};
 		} catch (SlickException e) {
 			//do shit all
 		}
 		moving = new Animation(movementForward, 200, false);
 		sprite=moving;
 		
 		this.angle = angle;
 		this.damage = damage;
 		this.hitbox = new Rectangle(centre.getX()-width/2, centre.getY()-height/2, width, height).transform(Transform.createRotateTransform((float) angle,centre.getX(),centre.getY()));
 		this.dxdy = new Position((float)Math.cos(angle),(float)Math.sin(angle));
 		this.dxdy.scale(Math.min(NORMAL_SPEED * speed, MAX_SPEED));
 		
 	}
 	
 	public Projectile(float x, float y, int damage, double angle, float speed){
 		this(new Position(x,y),damage,angle,speed);
 	}
 	
 	@Override
 	public Projectile clone() {
 		return this; //TODO
 	}
 
 	@Override
 	public void render(GameContainer gc, Graphics g) {
 		sprite.draw((int)((hitbox.getCenterX()-1f)*Config.getTileSize()-sprite.getWidth()/2), (int)((hitbox.getCenterY()-1)*Config.getTileSize()-sprite.getHeight()/2));
 		/*For debugging purposes.*/
 		g.setColor(Color.green); 
 		g.draw(hitbox.transform(Transform.createTranslateTransform(-1, -1)).transform(Transform.createScaleTransform(Config.getTileSize(), Config.getTileSize())));
 		//*/
 	}
 
 	@Override
 	public int getLayer() {
 		return PROJECTILE_DEFAULT_LAYER;
 	}
 
 	@Override
 	public float getdX() {
 		return dxdy.getX();
 	}
 
 	@Override
 	public float getdY() {
 		return dxdy.getY();
 	}
 
 	@Override
 	public int takeDamage(int normalDamage) {
 		MapLoader.getCurrentCell().removeMovingEntity(this);
 		return 0;
 	}
 
 	@Override
 	public int getDamage() {
 		// TODO Auto-generated method stub
 		return damage;
 	}
 
 	@Override
 	public int getNormalDamage() {
 		// TODO Auto-generated method stub
 		return damage;
 	}
 
 	@Override
 	public int getHealth() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public int getMaxHealth() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public void frameMove() {
 		hitbox = hitbox.transform(Transform.createTranslateTransform(dxdy.getX(), dxdy.getY()));
 		sprite.update(Config.DELTA);
 		{
 			int cX = (int) getCentreX(), cY = Math.max(1, (int) getCentreY());
 			Tile cT = MapLoader.getCurrentCell().getTile(cX, cY);
 			// gravity
 			dxdy.translate(0, 0.1f*cT.lookup(TileProperty.GRAVITY).getFloat()); 
 			dxdy.translate(-getdX()*0.02f*cT.lookup(TileProperty.FRICTIONX).getFloat(), -getdY()*0.02f*cT.lookup(TileProperty.FRICTIONY).getFloat());
 		}
 		// update angle and hitbox
 		double lastangle = angle;
 		angle = Math.atan2(dxdy.getY(), dxdy.getX());
 		hitbox = hitbox.transform(Transform.createRotateTransform((float) (angle - lastangle),hitbox.getCenterX(),hitbox.getCenterY()));
 		
 		// update animation (copied and pasted from above)
 		// there's definitely a better way of doing this...but not at 1am... - G //TODO
 		Image[] movementForward = null;
 		try {
 			Image projImage = new Image("data/images/projectile.png");
 			projImage.rotate((float)(angle * 180/Math.PI));
 			movementForward = new Image[]{projImage};
 		} catch (SlickException e) {
 			//do shit all
 		}
 		moving = new Animation(movementForward, 200, false);
 		sprite=moving;
 	}
 
 	@Override
 	public boolean isOnGround() {
 		return false;
 	}
 
 	@Override
 	public void jump() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void accelerate(float ddx, float ddy) {
 		dxdy.translate(ddx, ddy);
 	}
 
 	@Override
 	public void setVelocity(float dx, float dy) {
 		dxdy.set(dx, dy);
 	}
 
 	@Override
 	public void update(GameContainer gc) {
 		frameMove();
 		int x = (int) getCentreX(), y = Math.max(1, (int) getCentreY());
 		Cell cell = MapLoader.getCurrentCell();
 		if(x < 0 || x >= cell.getWidth() - 1 || y >= cell.getHeight() - 1 || 
 				cell.getTile(x, y).lookup(TileProperty.BLOCKED).getBoolean()) {
 			MapLoader.getCurrentCell().removeMovingEntity(this);
 		}
 	}
 
 	@Override
 	public void stop_sounds() {
 	}
 
 	@Override
 	public void collide(MovingEntity e) {
 		if(e != MapLoader.getCurrentCell().getPlayer()){
 			e.takeDamage(damage);
 			MapLoader.getCurrentCell().removeMovingEntity(this);
 		}
 	}
 
 	@Override
 	public void collide(StaticEntity<?> e) {
 		if(e.isSolid()){
 			MapLoader.getCurrentCell().removeMovingEntity(this);
 		}
 	}
 
 	@Override
 	public Shape getHitbox() {
 		return hitbox;
 	}
 
 	@Override
 	public void collide(DestructibleEntity d) {
 		MapLoader.getCurrentCell().removeMovingEntity(this);
 	}
 
 }
