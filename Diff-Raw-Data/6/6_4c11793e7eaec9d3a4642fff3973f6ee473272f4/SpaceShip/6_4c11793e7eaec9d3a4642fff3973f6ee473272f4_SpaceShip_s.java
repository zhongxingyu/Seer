 package ch.kanti_wohlen.asteroidminer.entities;
 
 import ch.kanti_wohlen.asteroidminer.Player;
 import ch.kanti_wohlen.asteroidminer.TaskScheduler;
 import ch.kanti_wohlen.asteroidminer.Textures;
 import ch.kanti_wohlen.asteroidminer.audio.SoundPlayer;
 import ch.kanti_wohlen.asteroidminer.audio.SoundPlayer.SoundEffect;
 import ch.kanti_wohlen.asteroidminer.entities.bars.HealthBar;
 import ch.kanti_wohlen.asteroidminer.entities.bars.ShieldBar;
 
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.PolygonShape;
 import com.badlogic.gdx.physics.box2d.World;
 
 public class SpaceShip extends Entity implements Damageable {
 
 	public static final int MAX_HEALTH = 100;
 	public static final int MAX_SHIELD = 100;
 	public static final float DEFAULT_FIRING_DELAY = 0.3f;
 	public static final float DEFAULT_SPEED = 1f;
 
 	private final Player player;
 	private final HealthBar healthBar;
 	private final ShieldBar shieldBar;
 	private final Rectangle boundingBox;
 
 	private int health;
 	private int shield;
 	private int laserDamage;
 	private float firingDelay;
 	private float speed;
 	private boolean canShoot;
 
 	public SpaceShip(World world, Player owningPlayer) {
 		super(world, createBodyDef(), createFixture());
 		player = owningPlayer;
 		healthBar = new HealthBar(MAX_HEALTH);
 		shieldBar = new ShieldBar(MAX_SHIELD);
 
 		health = MAX_HEALTH;
 		shield = 0;
 		firingDelay = DEFAULT_FIRING_DELAY;
 		speed = DEFAULT_SPEED;
 		laserDamage = Laser.DEFAULT_DAMAGE;
 		canShoot = true;
 
 		final float width = Textures.SPACESHIP.getWidth() * PIXEL_TO_BOX2D;
 		final float height = Textures.SPACESHIP.getHeight() * PIXEL_TO_BOX2D;
 		boundingBox = new Rectangle(0f, 0f, width, height);
 	}
 
 	@Override
 	public void render(SpriteBatch batch) {
 		Sprite s = Textures.SPACESHIP;
 		positionSprite(s);
 		s.draw(batch);
 
 		final Vector2 barLoc = new Vector2(s.getX() - s.getWidth() * 0.05f, s.getY() + s.getHeight() * 1.15f);
 		if (shield > 0) barLoc.y += Textures.HEALTH_HIGH.getRegionHeight() / 2f;
 
 		healthBar.render(batch, health, barLoc);
 		if (shield > 0) {
 			shieldBar.render(batch, shield, barLoc.sub(0f, Textures.HEALTH_HIGH.getRegionHeight()), healthBar.getAlpha());
 		}
 	}
 
 	@Override
 	public boolean isRemoved() {
 		return false;
 	}
 
 	@Override
 	public Rectangle getBoundingBox() {
 		final Rectangle rect = new Rectangle(boundingBox);
 		rect.setCenter(body.getPosition());
 		return rect;
 	}
 
 	@Override
 	public EntityType getType() {
 		return EntityType.SPACESHIP;
 	}
 
 	public Player getPlayer() {
 		return player;
 	}
 
 	public int getHealth() {
 		return health;
 	}
 
 	public void setHealth(int newHealth) {
 		if (newHealth != health) {
 			health = MathUtils.clamp(newHealth, 0, MAX_HEALTH);
 			healthBar.resetAlpha();
 		}
 	}
 
 	public void heal(int healingAmoung) {
 		setHealth(health + healingAmoung);
 	}
 
 	public int getShield() {
 		return shield;
 	}
 
 	public void setShield(int newShield) {
 		if (shield != newShield) {
 			shield = MathUtils.clamp(newShield, 0, MAX_SHIELD);
 			healthBar.resetAlpha();
 		}
 	}
 
 	public float getFiringDelay() {
 		return firingDelay;
 	}
 
	public void setFiringDelay(float delay) {
		if (firingDelay == 0.3f) {
			firingDelay = delay;
 		}
 	}
 
 	public float getSpeed() {
 		return speed;
 	}
 
 	public void setSpeed(float newSpeed) {
 		speed = newSpeed;
 	}
 
 	public int getLaserDamage() {
 		return laserDamage;
 	}
 
 	public void setLaserDamage(int newDamage) {
 		laserDamage = newDamage;
 	}
 
 	public void damage(int damageAmount) {
 		if (shield >= damageAmount) {
 			setShield(shield - damageAmount);
 		} else {
 			final int hullDamage = damageAmount - shield;
 			if (shield != 0) setShield(0);
 			setHealth(health - hullDamage);
 		}
 	}
 
 	public void kill() {
 		setHealth(0);
 	}
 
 	public void fireLaser() {
 		if (canShoot) {
 			canShoot = false;
 			new Laser(body.getWorld(), this, laserDamage);
 			TaskScheduler.INSTANCE.runTaskLater(new Runnable() {
 
 				@Override
 				public void run() {
 					canShoot = true;
 				}
 			}, firingDelay);
 			SoundPlayer.playSound(SoundEffect.LASER_SHOOT, 0.05f);
 		}
 	}
 
 	private static BodyDef createBodyDef() {
 		BodyDef bd = new BodyDef();
 		bd.allowSleep = false;
 		bd.type = BodyType.DynamicBody;
 		bd.angularDamping = 10f;
 		bd.linearDamping = 2.5f;
 		bd.position.set(2f, 2f);
 		bd.gravityScale = 5f;
 
 		return bd;
 	}
 
 	private static FixtureDef createFixture() {
 		final FixtureDef fixture = new FixtureDef();
 		fixture.density = 1f;
 		final PolygonShape ps = new PolygonShape();
 		ps.setAsBox(Textures.SPACESHIP.getWidth() / 2f * PIXEL_TO_BOX2D,
 				Textures.SPACESHIP.getHeight() / 2f * PIXEL_TO_BOX2D);
 		fixture.shape = ps;
 		fixture.filter.categoryBits = 2;
 		return fixture;
 	}
 
 }
