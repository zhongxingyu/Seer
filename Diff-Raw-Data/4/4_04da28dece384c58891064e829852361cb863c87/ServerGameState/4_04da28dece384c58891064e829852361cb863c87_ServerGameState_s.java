 package game;
 
 /**
  * Server implementation of the GameState, with updating methods
  * 
  * @author Jacob Charles
  */
 
 public class ServerGameState extends GameState {
 
 	//a bunch of positional and directional constants
 	private static final int LEFT = -1;
 	private static final int STOP = 0;
 	private static final int RIGHT = 1;
 	private static final int TOP = -1;
 	private static final int BOTTOM = 1;
 	private static final int NONE = 0;
 
 	//non-synchronized game results
 	GameResults res = new GameResults();
 
 	/**
 	 * Generic constructor
 	 */
 	public ServerGameState(){
 		super();
 	}
 
 	/**
 	 * Start game state with specific number of players
 	 * 
 	 * @param players
 	 * 		number of players expected in the game
 	 */
 	public ServerGameState(int players) {
 		super(players);
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
 	 * Get the results of a game
 	 * 
 	 * @return the game results, in a nice package
 	 */
 	public GameResults getResults() {
 		res.setMode(getNextMode()); //nextMode is preserved in case of Sudden Death Match
 		res.setStock(getStock());
 		res.setTime(getTime());
 		res.setWinners(getWinners());
 		return res;
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
 		//land logic
 		for (Land l : getLevel()) {
 			update(l);
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
 		//power-up/item logic (with removal)
 		for(int i = 0; i < getPowerups().size(); i++) {
 			update(getPowerups().get(i));
 			//remove dead items
 			if (getPowerups().get(i).isDead()) {
 				getPowerups().remove(i);
 				i--;
 			}
 		}
 		//power-up/item logic (with removal)
 		for(int i = 0; i < getEffects().size(); i++) {
 			update(getEffects().get(i));
 			//remove dead effects
 			if (getEffects().get(i).isDead()) {
 				getEffects().remove(i);
 				i--;
 			}
 		}
 
 		//spawn powerups
 		if (getFrameNumber() % 350 == 250) spawnPowerup();
 
 		//check for the end of the game
 		checkEnd();
 
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
 	 * 		The controller data for input
 	 */
 	public void readControls(Actor a, Controller c) {
 		//dead take no input, nor those in pipes
 		if (a.isDead() || a.isPipe()) return;
 
 		//running
 		if (c.getLeft() > 0) {
 			run(a, LEFT);
 			a.setLean(true);
 		}
 		else if (c.getRight() > 0) {
 			run(a, RIGHT);
 			a.setLean(true);
 		}
 		else {
 			run(a, STOP);
 			a.setLean(false);
 			a.setSlide(false);
 		}
 
 		//crouching (through tiles)
 		a.setCrouch(c.getDown() > 0);
 
 		//jumping
 		if ((c.getJump() == 1 || c.getUp() == 1) && a.getOnLand() != null) {
 			jump(a);
 		}
 		else if ((c.getJump() == 1 || c.getUp() == 1) && //power-up double jump
 				a.getPowerup() == Item.DJUMP && a.getPowerupVar() > 0) {
 			jump(a);
 			a.setPowerupVar(a.getPowerupVar()-1);
 		}
 		else if (c.getJump() > 0 || c.getUp() > 0) { //holding jump
 			holdJump(a);
 		}
 
 		//shooting
 		/*if (c.getFire() == 1 && !a.isGrab()) {
 			shoot(a);
 			a.setUse(true);
 		}
 		else {
 			a.setUse(false);
 			a.setGrab(false);
 		}*/
 		if (a.isUse() && !a.isGrab()){
 			shoot(a);
 		}
 		if (c.getFire() == 1) {
 			a.setUse(true);
 		}
 		else {
 			a.setUse(false);
 			a.setGrab(false);
 		}
 	}
 
 	@Override
 	public Actor addPlayer(int character) {
 		res.addPlayer();
 		return super.addPlayer(character);
 	}
 
 	@Override
 	public void resumePlayer(Participant p) {
 		super.resumePlayer(p);
 		respawn(p.getPlayer());
 	}
 
 	/**
 	 * Warp from one level to another, once the game is in an appropriate state
 	 * 
 	 * @param i
 	 * 		Map ID of the destination level
 	 */
 	private void warpTo(int i) {
 		if (isReady()) {
 			reSetLevel(i); //move to the new level, resetting everything that needs it
 			setMode(getNextMode()); //change to appropriate game mode
 		}
 	}
 
 	/**
 	 * Update whether the game has ended or not
 	 */
 	private void checkEnd() {
 		//menu has no end
 		if (getMode() == MENU) {
 			setEnd(false);
 		}
 		//there is only one (survivor)
 		if (getMode() == STOCK && getNumberOfPlayers() > 1) {
 			setEnd(getLivingPlayers() < 2);
 		}
 		//single player match (for testing mostly)
 		else if (getMode() == STOCK) {
 			setEnd(getLivingPlayers() <= 0);
 		}
 		//time out
 		else if (getMode() == TIME) {
 			setEnd(getFrameNumber() > getTime());
 		}
 	}
 
 	/**
 	 * @return the number of players still alive
 	 */
 	private int getLivingPlayers() {
 		int n = 0;
 		for (Actor a : getFighters()) {
 			if (a.getLives() > 0) n++;
 		}
 		return n;
 	}
 
 	/**
 	 * Update an actor's frame
 	 * 
 	 * @param a
 	 * 		the actor to update
 	 */
 	private void pose (Actor a) {
 		//TODO: Get numbers for all these frames
 		if (a.getReload() > a.getShotDelay()-15) {
 			a.setFrame(8); //shot frame
 		}
 		else if (a.isSlide()) {
 			a.setFrame(7); //wall stick frame
 		}
 		else if (a.getOnLand() == null && a.getVy() < 0) {
 			a.setFrame(5); //rising frame
 		}
 		else if (a.getOnLand() == null) {
 			a.setFrame(6); //falling frame
 		}
 		else if (Math.abs(a.getVx()) > .1 && a.isLean()) {
 			a.setFrame((getFrameNumber()/11)%4+1); //running frames
 		}
 		else {
 			a.setFrame(0); //idle frame
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
 		if (a.getPowerup() == Item.SPEED) {
 			a.setVy(-a.getJumpPower()*1.65);
 		}
 	}
 
 	/**
 	 * Extend the height of an actor's jump
 	 * 
 	 * @param a
 	 * 		the actor to extend the jump of
 	 */
 	private void holdJump (Actor a) {
 		if (a.getAirTime() > 0 && a.getAirTime() <= a.getJumpHold()
 				&& a.getVy() <= 2*a.getGrav()-a.getJumpPower()) {
 			a.setVy(-a.getJumpPower());
 		}
 		if (a.getPowerup() == Item.SPEED && a.getAirTime() > 0 && a.getAirTime() <= a.getJumpHold()
 				&& a.getVy() <= 3*a.getGrav()-a.getJumpPower()*1.65) {
 			a.setVy(-a.getJumpPower()*1.65);
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
 
 		//recharge double jumps
 		if (a.getPowerup() == Item.DJUMP) {
 			a.setPowerupVar(1);
 		}
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
 		//limit and set direction
 		if (dir > RIGHT) dir = RIGHT;
 		if (dir < LEFT) dir = LEFT;
 		if (dir != 0) {
 			a.setDir(dir);
 		}
 
 		//s-s-s-speed up!
 		if (a.getPowerup() == Item.SPEED) {
 			dir *= 2; //crude, internal method to double move speed
 		}
 
 		//in the air
 		if (a.getAirTime() > 1) {
 			a.setVx(a.getVx()+a.getAirSpeed()*dir);
 		}
 		//on the ground
 		else {
 			a.setVx(a.getVx()+a.getRunSpeed()*dir);
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
 
 		//delay until next shot
 		a.setReload(a.getShotDelay());
 
 		//build a new shot, according to the Actor's specifications
 		Shot s = new Shot(a.getShot(), a);
 
 		//make the shot SUPER (+50% speed, +pierce)
 		if (a.getPowerup() == Item.SSHOT) {
 			s.setVx(s.getVx()*1.5);
			s.setVy(s.getVy()*1.5);
			if (s.isAccel() || s.isGravity()) { //+50% gravity/accel too
 				s.setVar(s.getVar()*3/2);
 			}
 			s.setPierce(true);
 		}
 		//giant/micro shots
 		if (a.getPowerup() == Item.BIG) {
 			float cx = s.getHCenter(), cy = s.getVCenter();
 			s.setW(s.getW()*2);
 			s.setH(s.getH()*2);
 			s.setCenter(cx, cy);
 		}
 		if (a.getPowerup() == Item.MINI) {
 			float cx = s.getHCenter(), cy = s.getVCenter();
 			s.setW(s.getW()/2);
 			s.setH(s.getH()/2);
 			s.setCenter(cx, cy);
 		}
 
 		//add the new bullet to the list of bullets
 		getBullets().add(s);
 	}
 
 	/**
 	 * The specified actor dies
 	 * 
 	 * @param a
 	 * 		the actor who dies
 	 */
 	private void die (Actor a) {
 		a.setPowerup(0);
 		a.setDeadTime(0);
 		a.setDead(true);
 		if (!isGameOver() && getMode() != MENU) {
 			a.loseLife();
 		}
 	}
 
 	/**
 	 * Specified shot dies
 	 * 
 	 * @param s
 	 * 		the shot that dies
 	 */
 	private void die (Shot s) {
 		if (s.isBomb()) {
 			Shot s2 = new Shot(Warehouse.getShots()[Warehouse.EXPLOSION], s);
 			//adjust explosion size
 			if (s.getSource() >= 0 && getPlayer(s.getSource()).getPowerup() == Item.BIG) {
 				s2.setVar(s2.getVar()*2);
 			}
 			else if (s.getSource() >= 0 && getPlayer(s.getSource()).getPowerup() == Item.MINI) {
 				s2.setVar(s2.getVar()/2);
 			}
 			getBullets().add(s2);
 		}
 		s.setLifeTime(0);
 		s.setDead(true);
 	}
 
 	/**
 	 * An actor kills another
 	 * 
 	 * @param a
 	 * 		the killer
 	 * @param b
 	 * 		the kill-ee
 	 */
 	private void kill (Actor a, Actor b) {
 
 		//big mode tanks a hit
 		if (b.getPowerup() == Item.BIG) {
 			b.setPowerup(0); //lose big mode
 			b.setDeadTime(b.getSpawnTime()); //go into hyper armor
 			return; //don't actually die
 		}
 
 		//target dies
 		die(b);
 
 		//killed by a target
 		if (a != null) {
 			//score a point
 			if (!isGameOver() && getMode() != MENU) {
 				a.gainPoint();
 			}
 			//a gets a kill, b gets a death
 			res.addKill(a.getId(), b.getId());
 			res.addDeath(b.getId(), a.getId());
 		}
 		else {
 			//lose a point
 			if (!isGameOver() && getMode() != MENU) {
 				b.losePoint();
 			}
 			//death by level
 			res.addDeath(b.getId(), -1);
 		}
 	}
 
 	/**
 	 * Respawn specified actor
 	 * 
 	 * @param a
 	 * 		actor to respawn
 	 */
 	private void respawn (Actor a) {
 		//don't respawn if you're out of lives in a stock match
 		if (a.getLives() <= 0 && getMode() == STOCK) {
 			return;
 		}
 		a.setDead(false);
 		fall(a);
 		a.setHCenter(getSpawnX(a.getId()));
 		a.setVCenter(getSpawnY(a.getId()));
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
 
 		a.setDeadTime(a.getDeadTime()+1); //respawn timer and spawn invincibility
 		if (a.getDeadTime() == a.getSpawnTime() && a.isDead()) respawn(a); //respawn at the time
 
 		if (a.getReload() > 0) a.setReload(a.getReload()-1); //timer between shots
 		if (a.getReload() % 3 == 2 && a.getPowerup() == Item.SSHOT) { //faster reload for Supershot
 			a.setReload(a.getReload()-1);
 		}
 
 		if (a.getPowerup() == Item.HYPER) { //item countdown
 			a.setPowerupVar(a.getPowerupVar()-1);
 			if (a.getPowerupVar() <= 0) { //time up
 				a.setPowerup(0);
 			}
 		}
 
 		if (!a.isDead()) {
 			move(a); //updates positions and speeds
 		}
 
 		pose(a); //update the player's frame
 	}
 
 	/**
 	 * update a shot's status
 	 * 
 	 * @param s
 	 * 		the shot to update
 	 */
 	private void update (Shot s) {
 		//die off
 		if (s.getLifeTime() <= 0) {
 			s.setDead(true);
 			return;
 		}
 		s.setLifeTime(s.getLifeTime()-1);
 
 		//expand
 		if (s.isGrow()) {
 			s.setW(s.getW()+s.getVar());
 			s.setH(s.getH()+s.getVar());
 			s.setX(s.getX()-s.getVar()/2.0);
 			s.setY(s.getY()-s.getVar()/2.0);
 		}
 
 		//update position
 		move(s);
 	}
 
 	/**
 	 * update an item's status
 	 * 
 	 * @param p
 	 * 		the item to update
 	 */
 	private void update (Item p) {
 		if (p.getLifeTime() <= 0) {
 			p.setDead(true);
 			return;
 		}
 		p.setLifeTime(p.getLifeTime()-1);
 
 		move(p);
 	}
 
 	/**
 	 * update a land's status
 	 * 
 	 * @param l
 	 * 		the land to update
 	 */
 	private void update (Land l) {
 		//bullet tiles shoot bullets
 		if (l.isGun() && getFrameNumber() % Warehouse.getShots()[l.getVar()].getReload() ==
 				Warehouse.getShots()[l.getVar()].getReload()-1) {
 			Shot s2 = new Shot(Warehouse.getShots()[Math.abs(l.getVar())], l);
 			if (l.getVar() < 0) {
 				s2.setVx(-s2.getVx());
 				if (s2.isAccel()) s2.setVar(-s2.getVar());
 			}
 			getBullets().add(s2);
 		}
 	}
 
 	/**
 	 * update an effect's status
 	 * 
 	 * @param e
 	 * 		the effect to update
 	 */
 	private void update (Effect e) {
 		//TODO: Fill out
 	}
 
 	/**
 	 * Spawn a powerup, somewhere
 	 */
 	private void spawnPowerup() {
 		if (getPowerSpawn().size() == 0) return; //no powerups allowed here
 
 		int type = (int)(1+Math.random()*Item.ITEM_NUM);
 		if (getMode() == STOCK && type == Item.HYPER && Math.random() > 0.5) { //extra life chance
 			type++;
 		}
 		int base = (int)(Math.random()*getPowerSpawn().size());
 		double x = getPowerSpawn().get(base).getLeftEdge() + Math.random()*getPowerSpawn().get(base).getW();
 		double y = getPowerSpawn().get(base).getTopEdge() + Math.random()*getPowerSpawn().get(base).getH();
 		spawnPowerup((int)x, (int)y, type);
 	}
 
 	/**
 	 * Spawn a new powerup
 	 * @param x
 	 * 		x position
 	 * @param y
 	 * 		y position
 	 * @param type
 	 * 		primary type
 	 */
 	private void spawnPowerup(int x, int y, int type) {
 		Item p = new Item();
 		//TODO: Spawn properly, constructors, the whole shabang
 		p.setW(14);
 		p.setH(14);
 		p.setLifeTime(550);
 		p.setHCenter(x);
 		p.setVCenter(y);
 		p.setVx(0);
 		p.setVy(0);
 		p.setType(type);
 		p.setSkin(type-1);
 		if (type == Item.DJUMP) p.setSubType(1);
 		if (type == Item.CHANGE) p.setSubType((int)(Math.random()*(Warehouse.CHAR_NUM-1)));
 		if (type == Item.HYPER) p.setSubType(10*50);
 		getPowerups().add(p);
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
 			if (a.getRightEdge() <= b.getLeftEdge() && a.getRightEdge()+a.getVx() > b.getLeftEdge()+b.getVx()) {
 				return LEFT;
 			}
 			//moving left
 			else if (a.getLeftEdge() >= b.getRightEdge() && a.getLeftEdge()+a.getVx() < b.getRightEdge()+b.getVx()) {
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
 			if (a.getBottomEdge() <= b.getTopEdge() && a.getBottomEdge()+a.getVy() > b.getTopEdge()+b.getVy()) {
 				return TOP;
 			}
 			//rising
 			else if (a.getTopEdge() >= b.getBottomEdge() && a.getTopEdge()+a.getVy() < b.getBottomEdge()+b.getVy()) {
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
 
 		//dead men don't interact
 		if (a.isDead()) return;
 		if (l.isHatch() && !isControl()) return;
 		if (l.isNHatch() && isControl()) return;
 
 		//collision values
 		int v = vCollide(a, l);
 		int h = hCollide(a, l);
 		boolean ov = overlap(a, l);
 
 		//slightly hackish "pipes", vertical only
 		if (l.isPipe() && !a.isPipe() && ov) {
 			a.setVx(0);
 			if (a.getVy() < 0) a.setVy(-1);
 			else if (a.getVy() > 0) a.setVy(1);
 			a.setOnLand(l); //tie you to THIS pipe
 			a.setPipe(true);
 		}
 		if (l.isPipe()) return; //temporary!
 
 		//die on touching danger
 		if (l.isDanger() && !a.isArmored() && (v != NONE || h != NONE || ov)) {
 			kill(null, a); //killed by no one
 		}
 
 		//activate warp blocks
 		if (l.isWarp() && isReady() && (v != NONE || h != NONE || ov)) {
 			warpTo(l.getVar()); //go to destination level
 		}
 
 		//character change blocks
 		if (l.isChar() && (v != NONE || h != NONE || ov)) {
 			a.setModel(l.getVar());
 		}
 
 		//option changing blocks
 		if (l.isOption() && (v != NONE || h != NONE || ov)) {
 			//mode changing
 			if (l.getVar() == 0) {
 				//cycle up
 				if (getNextMode() == STOCK) {
 					setNextMode(TIME);
 				}
 				//loop back
 				else {
 					setNextMode(STOCK);
 				}
 			}
 			//option changing
 			if (l.getVar() == 1) {
 				if (getNextMode() == STOCK) {
 					setStock(getStock()+2);
 					if (getStock () > 20) setStock(3);
 					else if (getStock() > 10) setStock(20);
 					else if (getStock() > 5) setStock(10);
 				}
 				else if (getNextMode() == TIME) {
 					setTime(getTime()+2*60*50);
 					if (getTime() > 20*60*50) setTime(1*60*50);
 					else if (getTime() > 10*60*50) setTime(20*60*50);
 					else if (getTime() > 5*60*50) setTime(10*60*50);
 				}
 			}
 		}
 
 		//switch blocks
 		if (l.isSwitch() && (v != NONE || h != NONE || ov)) {
 			setControl(!isControl());
 		}
 
 		//bouncy blocks
 		if (v == TOP && l.isBounce()) {
 			a.setBottomEdge(l.getTopEdge()-.005);
 			a.setVy(-a.getVy()*(10+l.getVar())/10);
 		}
 		//bouncy platforms don't have the other sides
 		if (v == BOTTOM && l.isBounce() && !l.isPlatform()) {
 			a.setTopEdge(l.getBottomEdge()+.005);
 			a.setVy(-a.getVy()*(10+l.getVar())/10);
 		}
 		if (h == LEFT && l.isBounce() && !l.isPlatform()) {
 			a.setRightEdge(l.getLeftEdge()-.005);
 			a.setVx(-a.getVx()*(10+l.getVar())/10);
 		}
 		if (h == RIGHT && l.isBounce() && !l.isPlatform()) {
 			a.setLeftEdge(l.getRightEdge()+.005);
 			a.setVx(-a.getVx()*(10+l.getVar())/10);
 		}
 		if (l.isBounce()) { //bounced off, re-check collisions
 			v = vCollide(a, l);
 			h = hCollide(a, l);
 			ov = overlap(a, l);
 		}
 
 		//solid or platform floors
 		if (v == TOP && (l.isSolid() || (l.isPlatform() && !a.isCrouch())) ) {
 			a.setBottomEdge(l.getTopEdge()-.005);
 			land(a, l);
 		}
 
 		//solid walls and ceilings
 		if (v == BOTTOM && l.isSolid()) {
 			a.setTopEdge(l.getBottomEdge()+.005);
 			a.setVy(STOP);
 		}
 		if (h == LEFT && l.isSolid()) {
 			a.setRightEdge(l.getLeftEdge()-.005);
 			a.setVx(STOP);
 			if (a.isLean() && a.getOnLand() == null && a.getVy() > a.getWallTermVel()) {
 				a.setVy(a.getWallTermVel());
 				a.setSlide(true);
 			}
 		}
 		if (h == RIGHT && l.isSolid()) {
 			a.setLeftEdge(l.getRightEdge()+.005);
 			a.setVx(STOP);
 			if (a.isLean() && a.getOnLand() == null && a.getVy() > a.getWallTermVel()) {
 				a.setVy(a.getWallTermVel());
 				a.setSlide(true);
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
 		//can't hit yourself, a dead guy, or an invincible guy
 		if (a == b || a.isDead() || b.isDead() || b.isArmored()) {
 			return;
 		}
 
 		//supermode kills everything you touch
 		if (a.getPowerup() == Item.HYPER && overlap(a, b)) {
 			kill(a, b);
 			return; //nothing else can happen
 		}
 
 		//land on enemy heads
 		if (vCollide(a, b) == TOP) {
 			a.setBottomEdge(b.getTopEdge());
 			jump(a);
 			kill(a, b);
 		}
 
 		//check horizontal collisions
 		int h = hCollide(a, b);
 		int ap = 4, bp = 4; //default extra push
 
 		//giant mode modifies pushes
 		if (a.getPowerup() == Item.BIG && b.getPowerup() != Item.BIG) {
 			ap = 1;
 			bp = 8;
 		}
 		else if (a.getPowerup() != Item.BIG && b.getPowerup() == Item.BIG) {
 			ap = 8;
 			bp = 1;
 		}
 		//if one side is mini, ignore collisions
 		if (a.getPowerup() == Item.MINI ^ b.getPowerup() == Item.MINI) {
 			return;
 		}
 
 		//bounce away from each other
 		if (h == LEFT) {
 			float cVx = a.getVx();
 			a.setVx(b.getVx()-ap);
 			b.setVx(cVx+bp);
 		}
 		if (h == RIGHT) {
 			float cVx = a.getVx();
 			a.setVx(b.getVx()+ap);
 			b.setVx(cVx-bp);
 		}
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
 		//phasing shots ignore platforms
 		if (s.isPhase()) {
 			return;
 		}
 
 		//out-of-phase platforms ignored
 		if (l.isHatch() && !isControl()) return;
 		if (l.isNHatch() && isControl()) return;
 
 		//flip option/switch blocks
 		if (hCollide(s,l) != NONE || vCollide(s,l) != NONE || overlap(s,l)) {
 			//mode changing
 			if (l.isOption() && l.getVar() == 0) {
 				//cycle up
 				if (getNextMode() == STOCK) {
 					setNextMode(TIME);
 				}
 				//loop back
 				else {
 					setNextMode(STOCK);
 				}
 			}
 			//option changing
 			if (l.isOption() && l.getVar() == 1) {
 				if (getNextMode() == STOCK) {
 					setStock(getStock()+2);
 					if (getStock () > 20) setStock(3);
 					else if (getStock() > 10) setStock(20);
 					else if (getStock() > 5) setStock(10);
 				}
 				else if (getNextMode() == TIME) {
 					setTime(getTime()+2*60*50);
 					if (getTime() > 20*60*50) setTime(1*60*50);
 					else if (getTime() > 10*60*50) setTime(20*60*50);
 					else if (getTime() > 5*60*50) setTime(10*60*50);
 				}
 			}
 			//switch blocks
 			if (l.isSwitch()) {
 				setControl(!isControl());
 			}
 		}
 
 		//bounce off non-damage blocks
 		if (s.isBounce() && l.isSolid() && (!l.isDanger() || !s.isWeak())) {
 			//top and bottom
 			if (vCollide(s,l) != NONE) {
 				s.setVy(-s.getVy());
 			}
 			//sides
 			else if (hCollide(s,l) != NONE) {
 				s.setVx(-s.getVx());
 				if (s.isAccel()) s.setVar(-s.getVar());
 			}
 			//stuck inside
 			else if (overlap(s,l)) {
 				s.setDead(true); 
 			}
 		}
 		//bounce off platform tops
 		if (s.isBounce() && l.isPlatform() && !l.isDanger()) {
 			if (vCollide(s,l) == TOP) {
 				s.setVy(-s.getVy());
 			}
 		}
 		//check for non-bouncing collisions
 		else if (l.isSolid()) {
 			if (overlap(s, l) || vCollide(s, l) != NONE || hCollide(s, l) != NONE) {
 				die(s);
 			}
 		}
 		//fall on platform tops
 		else if (l.isPlatform()) {
 			if (vCollide(s, l) == TOP) {
 				die(s);
 			}
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
 		if (s.getSource() == a.getId() || a.isDead() || a.isArmored()) {
 			return;
 		}
 
 		//check for any collision
 		if (overlap(s, a) || vCollide(s, a) != NONE || hCollide(s, a) != NONE) {
 			if (!s.isPierce()) die(s);
 			kill(getPlayer(s.getSource()), a);
 		}
 	}
 
 	/**
 	 * Check for powerup-level collisions
 	 * @param p
 	 * 		powerup to collide
 	 * @param l
 	 * 		land to check collisions with
 	 */
 	private void collide(Item p, Land l) {
 		//out of phase platforms
 		if (l.isHatch() && !isControl()) return;
 		if (l.isNHatch() && isControl()) return;
 
 		//destroyed by obstacles
 		if (l.isDanger() && overlap(p, l)) {
 			p.setDead(true);
 		}
 
 		//land on top
 		if ((l.isSolid() || l.isPlatform() || l.isBounce()) && vCollide(p, l) == TOP) {
 			p.setBottomEdge(l.getTopEdge()-.005);
 			if (l.isBounce()) p.setVy(-p.getVy());
 			else p.setVy(STOP);
 			//conveyer belt effects
 			if (l.isMove()) {
 				p.setX(p.getX()+l.getVar()/10);
 			}
 		}
 
 		//reverse directions
 		if (l.isSolid() || hCollide(p, l) != NONE) {
 			p.setVx(-p.getVx());
 		}
 	}
 
 	/**
 	 * Check for powerup-actor collisions
 	 * 
 	 * @param p
 	 * 		powerup to collide
 	 * @param a
 	 * 		actor to check collisions with
 	 */
 	private void collide(Item p, Actor a) {
 		//no collection if either is dead
 		if (a.isDead() || p.isDead() || !a.isUse()) return;
 
 		//collect and apply effect
 		if (overlap(a,p)) {
 			p.setDead(true);
 			a.setPowerup(p.getType());
 			a.setPowerupVar(p.getSubType());
 			a.setGrab(true);
 			a.setUse(false);
 		}
 	}
 
 	/**
 	 * Move the actor according to their current speeds (with gravity)
 	 * 
 	 * @param a
 	 * 		the actor to move
 	 */
 	private void move(Actor a) {
 		//assume he ain't wall sliding
 		a.setSlide(false);
 
 		//check for collisions with other actors
 		for (Actor b : getFighters()) {
 			collide(a, b);
 		}
 
 		//check collisions with the level
 		for (Land l : getLevel()) {
 			collide(a, l);
 		}
 
 		//move
 		a.setX(a.getX()+a.getVx());
 		a.setY(a.getY()+a.getVy());
 
 		//pipe physics
 		if (a.isPipe()) {
 			//in a pipe, there is no friction, no gravity, no change, only forward
 			if (a.getOnLand() == null || !overlap(a, a.getOnLand())) {
 				a.setPipe(false);
 				fall(a);
 			}
 		}
 		//apply ground friction (moving ground)
 		else if (a.getOnLand() != null && a.getOnLand().isMove()) {
 			Land l = a.getOnLand();
 			a.setVx((a.getVx()-l.getVar()*.1)*a.getRunSlip()+l.getVar()*.1); //slip + movement
 		}
 		//apply ground friction (normal ground)
 		else if (a.getOnLand() != null) {
 			a.setVx(a.getVx()*a.getRunSlip()); //slide
 		}
 		//apply air frictions
 		else if (a.getAirTime() > 0) {
 
 			//apply air friction and momentum
 			a.setVx(a.getVx()*a.getAirSlip()); //slide
 
 			//gravity
 			if (a.getPowerup() == Item.SPEED) {
 				a.setVy(a.getVy()+a.getGrav()*1.5);
 			}
 			else {
 				a.setVy(a.getVy()+a.getGrav());
 			}
 
 			//apply terminal velocity
 			if (a.getVy() > a.getTermVel()) a.setVy(a.getTermVel());
 			if (a.getPowerup() == Item.SPEED && a.getVy() > a.getTermVel()*1.5) {
 				a.setVy(a.getTermVel()/2);
 			}
 		}
 
 		//falling off edges, ducking through platforms, and hatches opening (also death traps)
 		Land l = a.getOnLand();
 		if (l != null) {
 			if (a.getRightEdge() < l.getLeftEdge() || a.getLeftEdge() > l.getRightEdge()) { //off edge
 				fall(a);
 			}
 			if (a.isCrouch() && l.isPlatform() && !l.isSolid()) { //duck through
 				fall(a);
 			}
 			if (l.isHatch() && !isControl()) { //positive hatch off
 				fall(a);
 			}
 			if (l.isNHatch() && isControl()) { //negative hatch off
 				fall(a);
 			}
 			if (l.isDanger() && !a.isArmored()) { //deadly floors
 				kill(null, a);
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
 		//other bullet collisions (shield-type)
 		if (s.isShield()) {
 			for (int i = 0; i < getBullets().size(); i++) {
 				Shot h = getBullets().get(i);
 				if (s.getSource() != h.getSource() && overlap(s, h)) {
 					if (h.isShield()) die(s); //double-shield counter
 					if (!h.isPierce()) die(h); //piercing lives on
 				}
 			}
 		}
 
 		//apply velocity
 		s.setX(s.getX()+s.getVx());
 		s.setY(s.getY()+s.getVy());
 
 		//apply gravity (uses gravity to decide terminal velocity)
 		if (s.isGravity()) {
 			s.setVy(s.getVy()+s.getVar()/100.0);
 			if (s.getVy() > Math.abs(s.getVar()/10.0)) {
 				s.setVy(Math.abs(s.getVar()/10.0));
 			}
 		}
 
 		//apply acceleration/deceleration, no maximum for now
 		if (s.isAccel()) {
 			s.setVx(s.getVx()+s.getVar()/1000.0);
 		}
 
 		//out of bounds removal
 		if ((s.getBottomEdge() < -s.getH()*2 && s.getVy() < 0)
 				|| (s.getTopEdge() > GameState.HEIGHT+s.getH()*2 && s.getVy() > 0)
 				|| (s.getRightEdge() < -s.getW()*2 && s.getVx() < 0)
 				|| (s.getLeftEdge() > GameState.WIDTH+s.getW()*2 && s.getVx() > 0)) {
 			s.setDead(true);
 		}
 	}
 
 	/**
 	 * Move the item around the level
 	 * 
 	 * @param p
 	 * 		the item to move
 	 */
 	private void move(Item p) {
 		//terrain collisions
 		for (Land l : getLevel()) {
 			collide(p, l);
 		}
 
 		//player collection
 		for (Actor a : getFighters()) {
 			collide(p, a);
 		}
 
 		//apply velocity
 		p.setX(p.getX()+p.getVx());
 		p.setY(p.getY()+p.getVy());
 
 		//fixed gravity
 		p.setVy(p.getVy()+.4);
 		if (p.getVy() > 4) p.setVy(4);
 
 		//out of bounds removal
 		if (p.getTopEdge() > HEIGHT+p.getH()*2
 				|| p.getRightEdge() < -p.getW()*2 || p.getLeftEdge() > WIDTH+p.getW()*2) {
 			p.setDead(true);
 		}
 	}
 	
 	/**
 	 * Move an effect across the screen
 	 * 
 	 * @param e
 	 * 		the effect to move
 	 */
 	private void move(Effect e) {
 		//TODO: Fill out
 	}
 }
