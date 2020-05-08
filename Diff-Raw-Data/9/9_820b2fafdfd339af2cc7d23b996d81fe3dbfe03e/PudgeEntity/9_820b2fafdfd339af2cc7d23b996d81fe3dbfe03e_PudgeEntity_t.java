 package pudgewars.entities;
 
 import java.awt.BasicStroke;
 import java.awt.Image;
 import java.awt.Shape;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Ellipse2D;
 
 import pudgewars.Game;
 import pudgewars.Window;
 import pudgewars.components.Stats;
 import pudgewars.entities.hooks.HookEntity;
 import pudgewars.entities.hooks.HookType;
 import pudgewars.entities.hooks.NormalHookEntity;
 import pudgewars.entities.hooks.PullHookEntity;
 import pudgewars.input.MouseButton;
 import pudgewars.interfaces.BBOwner;
 import pudgewars.level.Tile;
 import pudgewars.util.Animation;
 import pudgewars.util.ImageHandler;
 import pudgewars.util.Time;
 import pudgewars.util.Vector2;
 
 public class PudgeEntity extends Entity implements LightSource {
 	public final static int CLICK_SIZE = 8;
 	public final static int MAX_LIFE = 20;
 
 	public final static double COLLISION_WIDTH = 1;
 	public final static double COLLISION_HEIGHT = 1;
 
 	public Stats stats;
 
 	// Whether or not you can control this Pudge
 	public boolean controllable = false;
 
 	public boolean isHooking;
 	public boolean canMove;
 	public boolean canTileCollide;
 	public NormalHookEntity attachedHook;
 
 	// Target Position
 	protected Image clicker;
 	protected Vector2 target;
 	protected double targetRotation;
 
 	// Rendering
 	protected Image img;
 	protected Animation ani;
 	protected Image fullLife;
 	protected Image emptyLife;
 
 	public PudgeEntity(Vector2 position, Team team) {
 		super(position, new Vector2(COLLISION_WIDTH, COLLISION_HEIGHT));
 
 		this.team = team;
 
 		canTileCollide = true;
 		canMove = true;
 
 		stats = new Stats(this);
 		stats.restoreDefaults();
 
 		rigidbody.physicsSlide = true;
 
 		ani = Animation.makeAnimation("pudge3", 8, 16, 16, 0.05);
 		ani.startAnimation();
 		img = ImageHandler.get().getImage("pudge3");
 
 		clicker = ImageHandler.get().getImage("selector");
 		target = null;
 
 		fullLife = ImageHandler.get().getImage("life_full");
 		emptyLife = ImageHandler.get().getImage("life_empty");
 	}
 	
 	public void update(){}
 	
 	public void update(int i) {
		if(Game.positions.get(i) != null && !controllable) target = Game.positions.get(i);
 		if (rigidbody.isMoving()) ani.update();
 
 		// Stats
 		if (Game.keyInput.buyMode.wasPressed()) {
 			stats.isOpen ^= true; // Cool way to NOT
 		}
 
 		if (!canMove) {
 			target = null;
 		}
 
 		// Controls
 		if (controllable && canMove) {
 			// Change Cursor
 			if (Game.keyInput.specialHook.isDown) Game.cursor.setCursor("Special");
 			else Game.cursor.setCursor("Default");
 
 			Vector2 rightClick = Game.mouseInput.getMouseClicked(MouseButton.RIGHT);
 			if (rightClick != null) {
 				target = Game.s.screenToWorldPoint(rightClick);
 			}
 			Vector2 leftClick = Game.mouseInput.getMouseClicked(MouseButton.LEFT);
 			if (leftClick != null) {
 				// Activate Hook
 				if (Game.keyInput.specialHook.isDown) this.setHook(Game.s.screenToWorldPoint(leftClick), HookType.PULL);
 				else setHook(Game.s.screenToWorldPoint(leftClick), HookType.NORMAL);
 			}
 
 			// Rotate the Clicker
 			if (target != null) targetRotation += -0.1;
 		}
 
 		if (target != null) {
 			transform.rotateTowards(target, 0.1);
 
 			double dist = transform.position.distance(target);
 			if (dist < rigidbody.velocity.magnitude() * Time.getTickInterval()) {
 				setVerticalMovement(0);
 				setHorizontalMovement(0);
 				target = null;
 			} else {
 				rigidbody.setDirection(target);
 			}
 		}
 		
 		if(controllable){
 			if(target == null) Game.client.sendMessage("null");
 			else Game.client.sendMessage(target.x + " " + target.y);
 		}
 		rigidbody.updateVelocity();
 
 		// Un-comment this to have some fun!
 		isHooking = false;
 	}
 
 	final static int LIFESTROKE_DEPTH = 3;
 	final static BasicStroke LIFESTROKE = new BasicStroke(LIFESTROKE_DEPTH);
 
 	final static int ARC_STARTANGLE = 135;
 	final static int FULL_LIFE_ARC = 180;
 
 	public void render() {
 		// Draw Pudge
 		Game.s.g.drawImage(ani.getImage(), transform.getAffineTransformation(), null);
 
 		/*
 		 * LIFE DRAWING
 		 */
 
 		// Dimension Definitions!
 		Vector2 v = Game.s.worldToScreenPoint(transform.position);
 		v.y -= Game.TILE_SIZE / 2;
 		int lifebarWidth = fullLife.getWidth(null);
 		int lifebarHeight = fullLife.getHeight(null);
 		int lifebarActual = (int) (fullLife.getWidth(null) * stats.lifePercentage());
 
 		Game.s.g.drawImage(emptyLife, (int) v.x - lifebarWidth / 2, (int) v.y - lifebarHeight / 2, (int) v.x + lifebarWidth / 2, (int) v.y + lifebarHeight / 2, //
 				0, 0, lifebarWidth, lifebarHeight, null);
 		Game.s.g.drawImage(fullLife, (int) v.x - lifebarWidth / 2, (int) v.y - lifebarHeight / 2, (int) v.x - lifebarWidth / 2 + lifebarActual, (int) v.y + lifebarHeight / 2, //
 				0, 0, lifebarActual, lifebarHeight, null);
 	}
 
 	public void onGUI() {
 		if (target != null) {
 			Vector2 targetLocation = Game.s.worldToScreenPoint(target);
 			AffineTransform a = new AffineTransform();
 			a.translate((int) (targetLocation.x - CLICK_SIZE / 2), (int) (targetLocation.y - CLICK_SIZE / 2));
 			a.rotate(targetRotation, CLICK_SIZE / 2, CLICK_SIZE / 2);
 			Game.s.g.drawImage(clicker, a, null);
 		}
 
 		stats.onGUI();
 	}
 
 	public void setHook(Vector2 click, int hookType) {
 		if (!isHooking) {
 			Entity e = null;
 
 			switch (hookType) {
 				case HookType.NORMAL:
 					e = new NormalHookEntity(this, click);
 					break;
 				case HookType.PULL:
 					e = new PullHookEntity(this, click);
 					break;
 			}
 			Game.entities.entities.add(e);
 			isHooking = true;
 		}
 	}
 
 	/*
 	 * Collisions
 	 */
 	public boolean shouldBlock(BBOwner b) {
 		if (b instanceof HookEntity) return true;
 		if (b instanceof PudgeEntity) return true;
 		if (canTileCollide) {
 			if (b instanceof Tile) {
 				if (((Tile) b).isPudgeSolid()) return true;
 			}
 		}
 		return false;
 	}
 
 	public void collides(Entity e, double vx, double vy) {
 		if (e instanceof PudgeEntity) {
 			PudgeEntity p = (PudgeEntity) e;
 			if (p.attachedHook != null) {
 				if (p.attachedHook.owner == this) {
 					p.attachedHook.detachPudge();
 					p.rigidbody.velocity = Vector2.ZERO.clone();
 				}
 			}
 		}
 	}
 
 	public void kill() {
 		super.kill();
 		System.out.println("Pudge was Killed");
 	}
 
 	public Shape getLightShape() {
 		Vector2 v = Game.s.worldToScreenPoint(transform.position);
 		v.scale(1.0 / Window.LIGHTMAP_MULT);
 		double r = (4 * Game.TILE_SIZE) / Window.LIGHTMAP_MULT;
 		Shape circle = new Ellipse2D.Double(v.x - r, v.y - r, r * 2, r * 2);
 		return circle;
 	}
 }
