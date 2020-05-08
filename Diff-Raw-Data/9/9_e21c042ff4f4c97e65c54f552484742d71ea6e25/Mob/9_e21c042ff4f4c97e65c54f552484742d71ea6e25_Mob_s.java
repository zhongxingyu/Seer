 package td.entity.mob;
 
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.util.TimerTask;
 
 import td.Game;
 import td.entity.Entity;
 
 public class Mob extends Entity {
 
 	// set up needed variables
 	private final int minHealth = 0;
 	private int maxHealth;
 	private int currHealth;
 	private int armorValue;
 	private int attackDamage;
 	private int inTileID;
 	private float movementSpeed;
 	private float speedMod; // speed modifier - if we want to have a tower that
 							// slows mobs
 	private float fireResist, iceResist, lightningResist, earthResist;
 	private int tilesTraveled;
 	private BufferedImage mobRender;
 	private boolean targetTileChange;
 	private int facingDirection, oldDirection;
 
 	// Constructor
 	public Mob(int mH, int aV, int aD, float mS, float i, float k, float fR,
 			float iR, float lR, float eR) {
 		this.maxHealth = mH;
 		this.armorValue = aV;
 		this.attackDamage = aD;
 		this.movementSpeed = mS;
 		this.x = i;
 		this.y = k;
 		this.fireResist = fR;
 		this.iceResist = iR;
 		this.lightningResist = lR;
 		this.earthResist = eR;
 		this.speedMod = 1; // speed modifier - if we want to have a tower that
 							// slows mobs
 		this.tilesTraveled = -1; // keep track of where the mob is in the path to
 								// the end
 		this.timer.scheduleAtFixedRate(new ScheduleTask(), 1000, 1000/60);
 		this.facingDirection = -1; // always fails first run
 		this.targetTileChange = true;
 		this.inTileID = -1; // always fails for first tick
 	}
 
 	// Set the direction to move
 	public void move1() {
 		if (targetTileChange) {
 			// make sure we're not already at the top of the map
 			if (inTileID % Game.map.getHeight() != Game.map.getHeight() - 1) { 
 				// move up?
 				if (inTileID + 1 == Game.path.path.get(tilesTraveled+1).getID()) {
 					//move2(0, Game.standardMovementSpeed); // here's the important part
 					oldDirection = facingDirection;
 					facingDirection = 0;
 				}
 			} 
 			// make sure we're not already at the right edge of the map
 			if ((int) (Math.floor(inTileID / Game.map.getHeight())) != Game.map.getWidth() - 1) { 
 				// move right?
 				if (inTileID + Game.map.getHeight() == Game.path.path.get(tilesTraveled+1).getID()) {
 					//move2(Game.standardMovementSpeed, 0); // here's the important part
 					oldDirection = facingDirection;
 					facingDirection = 1;
 				}
 			}
 			// make sure we're not on the bottom of the map already
 			if (inTileID % Game.map.getHeight() != 0){
 				// move down?
 				if (inTileID - 1 == Game.path.path.get(tilesTraveled+1).getID()) { 
 					//move2(0, -Game.standardMovementSpeed); // here's the important part
 					oldDirection = facingDirection;
 					facingDirection = 2;
 				}
 			} 
 			// make sure we're not already on the left edge of the map
 			if ((int) (Math.floor(inTileID / Game.map.getHeight())) != 0) { 
 				// move left?
 				if (inTileID - Game.map.getHeight() == Game.path.path.get(tilesTraveled+1).getID()) {
 					//move2(-Game.standardMovementSpeed, 0); // here's the important part
 					oldDirection = facingDirection;
 					facingDirection = 3;
 				}
 			} 
 			targetTileChange = false;
 		}
 		// check if we're in the last tile
 		if (inTileID == Game.map.getEndTileID()) {
 			// Insert here what happens when the mobs reach the end
 		}
 		
 		// take care of actually moving
 		if (facingDirection == 0) { // if facing forward
 			move2(0.0, Game.standardMovementSpeed);
 		} else if (facingDirection == 1) { // if facing right
 			move2(Game.standardMovementSpeed, 0.0);
 		} else if (facingDirection == 2) { // if facing down
 			move2(0.0, -Game.standardMovementSpeed);
 		} else if (facingDirection == 3) { // if facing left
 			move2(-Game.standardMovementSpeed, 0.0);
 		} else { // you've done something wrong
 			move2(0.0, 0.0);
 		}
 		
 	}
 
 	// Move command, will take in standard movement speed of 1/2 tile per second
 	// and modifies it
 	public void move2(double i, double k) {
 		x += ((i * movementSpeed) / speedMod);
 		y += ((k * movementSpeed) / speedMod);
 	}
 
 	public int getInTileID() {
 		return inTileID;
 	}
 
 	// Used for taking damage from a bullet... called by bullet.java
 	// This still needs to take mob armor into account
 	public void takeDamage(int dmg, attackType aType) {
 		int modDmg = 0; // modified damage value
 		if (aType == attackType.FIRE) {
 			modDmg = (int) (dmg * (1 - fireResist));
 		} else if (aType == attackType.ICE) {
 			modDmg = (int) (dmg * (1 - iceResist));
 		} else if (aType == attackType.LIGHTNING) {
 			modDmg = (int) (dmg * (1 - lightningResist));
 		} else if (aType == attackType.EARTH) {
 			modDmg = (int) (dmg * (1 - earthResist));
 		}
 
 		if (modDmg <= 0)
 			modDmg = 1; // make sure we're at least doing 1 damage
 
 		currHealth -= modDmg; // this is the important line
 		if (currHealth <= minHealth) {
 			remove();
 		}
 	}
 
 	// Used for healing a Mob (if we want to have healers)
 	public void heal(int dmg) {
 		if (currHealth + dmg > maxHealth) {
 			currHealth = maxHealth;
 		} else {
 			currHealth += dmg;
 		}
 	}
 
 	// Tick - handles the gears
 	public void tick() {
 
 		//move1();
 		// find the tile ID, for AOE shtuff & Pathfinding
 		int tempx = (int) (x / Game.map.getTile(0, 0).getHeight()) * Game.map.getHeight();
 		int tempy = (int) (y / Game.map.getTile(0, 0).getHeight());
 		int currTile = tempx + tempy;
 		if (inTileID != currTile) {
 			inTileID = currTile;
 			tilesTraveled++;
 			targetTileChange = true;
 		}
 
 	}
 
 	// Returns the buffered image, used by the graphics handler
 	public BufferedImage getRender() {
 		return mobRender;
 	}
 
 	// renders the buffered image of the mob
 	public void render() {
 		mobRender = new BufferedImage(Game.map.getWidthPixels(),
 				Game.map.getHeightPixels(), BufferedImage.TYPE_INT_ARGB);
 		Graphics g = mobRender.createGraphics();
		g.drawImage(this.sprite.getImage(), (int) x,
				(int) (Game.map.getHeightPixels() - y), this.sprite.getWidth()
						* Game.SCALE, this.sprite.getHeight() * Game.SCALE,
				null);
 	}
 	
 	// for movement, since it's handled by a timer
 	class ScheduleTask extends TimerTask {
 		public void run() {
 			move1();
 		}
 	}
 
 }
