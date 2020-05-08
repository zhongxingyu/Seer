 package game;
 
 /**
  * Server implementation of the GameState, with updating methods
  * 
  * @author Jacob Charles
  */
 import java.util.*;
 
 public class ServerGameState extends GameState {
 
 	//a bunch of positional and directional constants
 	private static final int LEFT = -1;
 	private static final int STOP = 0;
 	private static final int RIGHT = 1;
 	private static final int TOP = -1;
 	private static final int BOTTOM = 1;
 	private static final int NONE = 0;
 
 	/**
 	 * Generic constructor
 	 */
 	public ServerGameState(){
 		super();
 	}
 
 	/**
 	 * Clone constructor from generic GameState
 	 * 
 	 * @param g
 	 * 		GameState to clone from
 	 */
 	public ServerGameState(GameState g){
 		super(g);
 	}
 
 	/**
 	 * Convert ServerGameState to a ClientGameState
 	 * 
 	 * @return ClientGameState version of this object
 	 */
 	public ClientGameState convert() {
 		return new ClientGameState(this);
 	}
 
 	/**
 	 * Update the entire game state
 	 * 
 	 * Apply user input first!
 	 */
 	public void update() {
 		//player logic
 		for (Actor a : getFighters()) {
 			update(a);
 		}
 		//bullet logic (with removal)
 		for(int i = 0; i < getBullets().size(); i++) {
 			update(getBullets().get(i));
 			//remove dead bullets
 			if (getBullets().get(i).isDead()) {
 				getBullets().remove(i);
 				i--;
 			}
 		}
 		//TODO: Other kinds of update logic here.
 
 		//track the number of frames passed
 		incrementFrames();
 	}
 
 	/**
 	 * Apply a participant's controls to their character
 	 * 
 	 * @param p
 	 * 		the participant to update from
 	 */
 	public void readControls(Participant p) {
 		Actor a = p.getPlayer();
 		Controller c = p.getController();
 		readControls(a, c);
 	}
 	
 	/**
 	 * Apply a player's controls to their character
 	 * 
 	 * @param a
 	 * 		the actor associated with the input
 	 * @param c
 	 * 		the controller data object
 	 */
 	//public void readControls(Player p, Controller c) {
 	public void readControls(Actor a, Controller c) {
 		if (a.isDead()) return;
 
 		//running
 		if (c.getLeft() > 0) {
 			run(a, LEFT);
 		}
 		else if (c.getRight() > 0) {
 			run(a, RIGHT);
 		}
 		else {
 			run(a, STOP);
 		}
 
 		//jumping
 		if (c.getJump() == 1 && a.getOnLand() != null) {
 			jump(a);
 		}
 		else if (c.getJump() >= 1 && a.getAirTime() > 0 && a.getAirTime() <= 5) {
 			holdJump(a);
 		}
 
 		//shooting
 		if (c.getFire() == 1) {
 			shoot(a);
 		}
 	}
 
 	/**
 	 * Make an actor jump
 	 * 
 	 * @param a
 	 * 		the actor to make jump
 	 */
 	private void jump (Actor a) {
 		a.setAirTime(1);
 		a.setOnLand(null);
 		a.setVy(-a.getJumpPower());
 	}
 
 	/**
 	 * Extend the height of an actor's jump
 	 * 
 	 * @param a
 	 * 		the actor to extend the jump of
 	 */
 	private void holdJump (Actor a) {
 		if (a.getVy() == 1-a.getJumpPower()) {
 			a.setVy(-a.getJumpPower());
 		}
 	}
 
 	/**
 	 * Make an actor fall
 	 * 
 	 * @param a
 	 * 		the actor to make fall
 	 */
 	private void fall (Actor a) {
 		a.setAirTime(1);
 		a.setOnLand(null);
 		a.setVy(STOP);
 	}
 
 	/**
 	 * Make an actor land on a piece of land
 	 * 
 	 * @param a
 	 * 		the actor to land
 	 * @param l
 	 * 		the land to land on
 	 */
 	private void land (Actor a, Land l) {
 		a.setVy(STOP);
 		a.setAirTime(-1);
 		a.setOnLand(l);
 	}
 
 	/**
 	 * Make an actor run in the specified direction
 	 * 
 	 * @param a
 	 * 		the actor to make run
 	 * @param dir
 	 * 		LEFT, RIGHT, or STOP, the direction to run
 	 */
 	private void run (Actor a, int dir) {
 		//TODO: Air/land differences?
 		if (dir > RIGHT) dir = RIGHT;
 		if (dir < LEFT) dir = LEFT;
 
 		if (dir == STOP) {
 			a.setVx(STOP);
 			return; //don't update anything else
 		}
 
 		a.setVx(dir*a.getRunSpeed());
 		if (dir != 0) {
 			a.setDir(dir);
 		}
 	}
 
 	/**
 	 * Make an actor fire their shot
 	 * 
 	 * @param a
 	 * 		the actor doing the shooting
 	 */
 	private void shoot (Actor a) {
 		//can't fire if you haven't reloaded
 		if (a.getReload() > 0) {
 			return;
 		}
 
 		a.setReload(a.getShotDelay());
 
 		//build a new shot, according to the Actor's specifications
 		Shot s = new Shot();
 		s.setSource(a);
 		s.setVx(a.getShotSpeed()*a.getDir());
 		s.setVy(0);
 		s.setH(a.getShotHei());
 		s.setW(a.getShotWid());
 		s.setLifeTime(a.getShotLife());
 		s.setHCenter(a.getHCenter()+a.getDir()*(a.getW()+a.getShotWid())/2);
 		s.setVCenter(a.getVCenter());
 
 		//add the new bullet to the list of bullets
 		getBullets().add(s);
 	}
 
 	/**
 	 * Kill the specified actor
 	 * 
 	 * @param a
 	 * 		the actor who dies
 	 */
 	private void die (Actor a) {
 		a.setDeadTime(0);
 		a.setDead(true);
 	}
 
 	/**
 	 * Respawn specified actor
 	 * 
 	 * @param a
 	 * 		actor to respawn
 	 */
 	private void respawn (Actor a) {
 		a.setDead(false);
 		a.setHCenter(WIDTH/2);
 		a.setTopEdge(50);
 		a.setOnLand(null);
 		a.setAirTime(1);
 	}
 
 	/**
 	 * update the actor's status
 	 * 
 	 * @param a
 	 * 		the actor to update
 	 */
 	private void update (Actor a) {
 		if (a.getAirTime() > 0) a.setAirTime(a.getAirTime()+1); //time in mid-air
 		if (a.getAirTime() < 0) a.setAirTime(a.getAirTime()-1); //time on the ground
 
 		a.setDeadTime(a.getDeadTime()+1); //respawn timer, potentially spawn armor
 		if (a.getDeadTime() == 50 && a.isDead()) respawn(a);
 
 		if (a.getReload() > 0) a.setReload(a.getReload()-1); //timer between shots
 
 		if (a.getOnLand() != null) a.setVy(a.getOnLand().getVy()); //match platform's vertical speed
 
 		if (!a.isDead()) {
 			move(a); //updates positions and speeds
 		}
 		//TODO: add more as other fields need updating
 	}
 
 	/**
 	 * update a shot's status
 	 * 
 	 * @param s
 	 * 		the shot to update
 	 */
 	private void update (Shot s) {
 		if (s.getLifeTime() <= 0) {
 			s.setDead(true);
 			return;
 		}
 		s.setLifeTime(s.getLifeTime()-1);
 		move(s);
 	}
 
 	/**
 	 * Check if two objects are overlapping
 	 * 
 	 * @param a
 	 * 		the first object
 	 * @param b
 	 * 		the second object
 	 * @return
 	 * 		true if they are overlapped
 	 */
 	private boolean overlap (GameObject a, GameObject b) {
 		//check that the edges are pushed through
 		if (a.getBottomEdge()+a.getVy() >= b.getTopEdge() && a.getTopEdge() <= b.getBottomEdge()) {
 			if (a.getRightEdge() >= b.getLeftEdge() && a.getLeftEdge() <= b.getRightEdge()) {
 				return true;
 			}
 		}
 		//no overlap
 		return false;
 	}
 
 	/**
 	 * Check collisions between two objects horizontally
 	 * 
 	 * @param a
 	 * 		the object to collide
 	 * @param b
 	 * 		the object it collides with
 	 * @return
 	 * 		LEFT if A is to the left of B
 	 * 		RIGHT if A is to the right of B
 	 * 		NONE if there is no collision
 	 */
 	private int hCollide (GameObject a, GameObject b) {
 		//lined up for horizontal collisions
 		if (a.getBottomEdge()+a.getVy() >= b.getTopEdge()+b.getVy() 
 				&& a.getTopEdge()+a.getVy()  <= b.getBottomEdge()+b.getVy()) {
 			//moving right
 			if (a.getRightEdge() < b.getLeftEdge() && a.getRightEdge()+a.getVx() >= b.getLeftEdge()+b.getVx()) {
 				return LEFT;
 			}
 			//moving left
 			else if (a.getLeftEdge() > b.getRightEdge() && a.getLeftEdge()+a.getVx() <= b.getRightEdge()+b.getVx()) {
 				return RIGHT;
 			}
 		}
 
 		//no collisions
 		return 0;
 	}
 
 	/**
 	 * Check collisions between two objects vertically
 	 * 
 	 * @param a
 	 * 		the object to collide
 	 * @param b
 	 * 		the object it collides with
 	 * @return
 	 * 		TOP if A is on top of B
 	 * 		BOTTOM if A is under B
 	 * 		NONE if there is no collision
 	 */
 	private int vCollide (GameObject a, GameObject b) {
 		//lined up for vertical collisions
 		if (a.getRightEdge()+a.getVx() >= b.getLeftEdge()+b.getVx()
 				&& a.getLeftEdge()+a.getVx() <= b.getRightEdge()+b.getVx()) {
 			//falling
 			if (a.getBottomEdge() < b.getTopEdge() && a.getBottomEdge()+a.getVy() >= b.getTopEdge()+b.getVy()) {
 				return TOP;
 			}
 			//rising
 			else if (a.getTopEdge() > b.getBottomEdge() && a.getTopEdge()+a.getVy() <= b.getBottomEdge()+b.getVy()) {
 				return BOTTOM;
 			}
 		}
 
 		//no collisions
 		return 0;
 	}
 
 	/**
 	 * Check for and handle collision between an actor and the stage 
 	 * 
 	 * @param a
 	 * 		the actor to collide
 	 * @param l
 	 * 		the land to check for collisions with
 	 */
 	private void collide (Actor a, Land l) {
 
 		if (a.isDead()) return;
 		
 		//vertical collisions
 		int v = vCollide(a, l);
 		if (v == TOP) {
 			if (l.isPlatform() || l.isSolid()) { //platform and solid tops
 				a.setBottomEdge(l.getTopEdge()-1);
 				land(a, l);
 			}
 		}
 		if (v == BOTTOM) {
 			if (l.isSolid()) { //solid ceilings
 				a.setTopEdge(l.getBottomEdge()+1);
 				a.setVy(STOP);
 			}
 		}
 
 		//horizontal collisions
 		int h = hCollide(a, l);
 		if (h == LEFT) {
 			if (l.isSolid()) { //solid walls
 				a.setRightEdge(l.getLeftEdge()-1);
 				a.setVx(STOP);
 			}
 		}
 		if (h == RIGHT) {
 			if (l.isSolid()) { //solid walls
 				a.setLeftEdge(l.getRightEdge()+1);
 				a.setVx(STOP);
 			}
 		}
 	}
 
 	/**
 	 * Check for actor/actor collisions and head bouncing
 	 * 
 	 * @param a
 	 * 		actor to collide
 	 * @param b
 	 * 		actor to check collisions with
 	 */
 	private void collide(Actor a, Actor b) {
 		//can't hit yourself
 		if (a == b || a.isDead() || b.isDead()) {
 			return;
 		}
 
 		//land on enemy heads
 		if (vCollide(a, b) == TOP) {
 			a.setBottomEdge(b.getTopEdge());
 			die(b);
 			jump(a);
 		}
 
 		//TODO: Make interesting side to side collisions?
 	}
 
 	/**
 	 * Check for collisions between a shot and the terrain
 	 * 
 	 * @param s
 	 * 		shot to collide
 	 * @param l
 	 * 		land to check for collisions with
 	 */
 	private void collide(Shot s, Land l) {
 		//non-solid platforms simply return for now
 		if (!l.isSolid()) {
 			return;
 		}
 
 		//check for any collision
 		if (overlap(s, l) || vCollide(s, l) != NONE || hCollide(s, l) != NONE) {
 			s.setDead(true);
 		}
 	}
 
 	/**
 	 * Check for shot-actor collisions
 	 * 
 	 * @param s
 	 * 		shot to collide
 	 * @param a
 	 * 		actor to check collisions with
 	 */
 	private void collide(Shot s, Actor a) {
 		//don't hit your own source
 		if (s.getSource() == a || a.isDead()) {
 			return;
 		}
 
 		//check for any collision
 		if (vCollide(s, a) != NONE || hCollide(s, a) != NONE) {
 			s.setDead(true);
 			die(a);
 		}
 	}
 
 	/**
 	 * Move the actor according to their current speeds (with gravity)
 	 * 
 	 * @param a
 	 * 		the actor to move
 	 */
 	private void move(Actor a) {
 		//check collisions with the level
 		for (Land l : getLevel()) {
 			collide(a, l);
 		}
 
 		//check for collisions with other actors
 		for (Actor b : getFighters()) {
 			collide(a, b);
 		}
 
 		//move along the ground
 		a.setX(a.getX()+a.getVx());
 
 		//move through the air
 		if (a.getAirTime() > 0) {
 			a.setY(a.getY()+a.getVy());
 			a.setVy(a.getVy()+1); //TODO: determine what exact gravity to use
 			if (a.getVy() > 10) a.setVy(10); //TODO: Make a specific terminal velocity
 		}
 		else {
 			a.setVy(0);
 		}
 
 		//falling off edges
 		Land l = a.getOnLand();
 		if (l != null) {
 			if (a.getRightEdge() < l.getLeftEdge() || a.getLeftEdge() > l.getRightEdge()) {
 				fall(a);
 			}
 		}
 
 		//out-of-bounds wraparound (temp)
 		if (a.getBottomEdge() < 0) a.setTopEdge(GameState.HEIGHT); //off top
 		if (a.getTopEdge() > GameState.HEIGHT) a.setBottomEdge(0); //off bottom
 		if (a.getRightEdge() < 0) a.setLeftEdge(GameState.WIDTH); //off left
 		if (a.getLeftEdge() > GameState.WIDTH) a.setRightEdge(0); //off right
 	}
 
 	/**
 	 * Move the shot according to its speeds
 	 * 
 	 * @param s
 	 * 		the shot to be moved
 	 */
 	private void move(Shot s) {
 		//terrain collisions
 		for (Land l : getLevel()) {
 			collide(s, l);
 		}
 		//target collisions
 		for (Actor a : getFighters()) {
 			collide(s, a);
 		}
 
 		//apply velocity (straight-only)
 		s.setX(s.getX()+s.getVx());
 		s.setY(s.getY()+s.getVy());
 
 		//out of bounds removal
		if (s.getBottomEdge() < 0 || s.getTopEdge() > GameState.HEIGHT
				|| s.getRightEdge() < 0 || s.getLeftEdge() > GameState.WIDTH) {
 			s.setDead(true);
 		}
 	}
 }
