 package game;
 
 /**
  * Class for the player's characters
  * 
  * @author Jacob Charles
  *
  */
 public class Actor extends GameObject {
 
 	private static final int CROUCH = 1;
 	private static final int LEAN = 2;
 	private static final int SLIDE = 4;
 	private static final int USE = 8;
 	private static final int GRAB = 16;
 	private static final int PIPE = 32;
 	private static final int MAX = 64-1;
 
 	//current data
 	private int id; //used in place of references
 	private int at = 1; //air time
 	private int dt; //dead time
 	private int r; //reload
 	private int dir = 1; //direction
 	private int cr = 0; //crouch and other unique stuff
 	private int p = 0; //power up
 	private int pv = 0; //power up variable (extra data)
 	private int lid = -1, lm = -1; //id and map id of the current land
 	private int s = 0; //score
 	private int l; //lives
 
 	private int m; //selected RoleModel from the Warehouse
 
 	/**
 	 * Spawn a player with a given archetype and location
 	 * 
 	 * @param x
 	 *		start x
 	 * @param y
 	 * 		start y
 	 * @param character
 	 * 		which player the character is using
 	 */
 	public Actor (int x, int y, int character) {
 		super(x, y);
 
 		//bind to their RoleModel
 		setModel(character);
 
 		//initialize some basic values
 		dt = getSpawnTime();
 	}
 
 	/**
 	 * @return true if the actor is under spawn armor
 	 */
 	public boolean isArmored() {
 		if (getPowerup() == Item.HYPER) return true; //hypermode armor
 		return (!isDead() && dt < getSpawnTime()+getSpawnInv());
 	}
 
 	@Override
 	public int getSkin() {
 		if (p == Item.CHANGE) {
 			return Warehouse.getCharacters()[pv].getSkin();			
 		}
 		else return super.getSkin();
 	}
 
 	/*getters and setters for attributes*/
 	public int getId() {
 		return id;
 	}
 	public void setId(int id) {
 		this.id = id;
 	}
 	public int getAirTime() {
 		return at;
 	}
 	public void setAirTime(int airTime) {
 		this.at = airTime;
 	}
 	public int getDeadTime() {
 		return dt;
 	}
 	public void setDeadTime(int deadTime) {
 		this.dt = deadTime;
 	}
 	public int getReload() {
 		return r;
 	}
 	public void setReload(int reload) {
 		this.r = reload;
 	}
 	public int getDir() {
 		return dir;
 	}
 	public void setDir(int dir) {
 		this.dir = dir;
 	}
 	public boolean isCrouch() {
 		return (cr&CROUCH) != 0;
 	}
 	public void setCrouch(boolean b) {
 		if (b) cr |= CROUCH;
 		else cr &= MAX-CROUCH;
 	}
 	public boolean isLean() {
 		return (cr&LEAN) != 0;
 	}
 	public void setLean(boolean b) {
 		if (b) cr |= LEAN;
 		else cr &= MAX-LEAN;
 	}
 	public boolean isSlide() {
 		return (cr&SLIDE) != 0;
 	}
 	public void setSlide(boolean b) {
 		if (b) cr |= SLIDE;
 		else cr &= MAX-SLIDE;
 	}
 	public boolean isUse() {
 		return (cr&USE) != 0;
 	}
 	public void setUse(boolean b) {
 		if (b) cr |= USE;
 		else cr &= MAX-USE;
 	}
 	public boolean isGrab() {
 		return (cr&GRAB) != 0;
 	}
 	public void setGrab(boolean b) {
 		if (b) cr |= GRAB;
 		else cr &= MAX-GRAB;
 	}
 	public boolean isPipe() {
 		return (cr&PIPE) != 0;
 	}
 	public void setPipe(boolean b) {
 		if (b) cr |= PIPE;
 		else cr &= MAX-PIPE;
 	}
 	public int getPowerup() {
 		return p;
 	}
 	public void setPowerup(int powerup) {
 		//grab a 1up (doesn't replace other powerups)
 		if (powerup == Item.LIFE) {
 			powerup = 0;
 			gainLife();
 			return;
 		}
 		//exit big mode (reset size)
 		if (this.p == Item.BIG && powerup != Item.BIG) {
 			float cx = getHCenter(), cy = getVCenter();
 			setW(Warehouse.getCharacters()[m].getW());
 			setH(Warehouse.getCharacters()[m].getH());
 			setCenter(cx, cy);
 			setOnLand(null); //off the ground
 			setAirTime(1);
 		}
 		//exit mini mode (reset size)
 		if (this.p == Item.MINI && powerup != Item.MINI) {
			setY(getY()-getH()-2);
 			setX(getX()-getW());
 			setW(Warehouse.getCharacters()[m].getW());
 			setH(Warehouse.getCharacters()[m].getH());
 			setOnLand(null); //off the ground
 			setAirTime(1);
 		}
 		//enter big mode (get huge)
 		if (this.p != Item.BIG && powerup == Item.BIG) {
			setY(getY()-getH()-2);
 			setX(getX()-getW()/2);
 			setW(getW()*2);
 			setH(getH()*2);
 		}
 		//enter mini mode (get tiny)
 		if (this.p != Item.MINI && powerup == Item.MINI) {
 			float cx = getHCenter(), cy = getVCenter();
 			setW(getW()/2);
 			setH(getH()/2);
 			setCenter(cx, cy);
 			setOnLand(null); //off the ground
 			setAirTime(1);
 		}
 		//don't morph into yourself
 		if (powerup == Item.CHANGE && pv == m) {
 			pv++;
 		}
 		this.p = powerup;
 	}
 	public int getPowerupVar() {
 		return pv;
 	}
 	public void setPowerupVar(int powerupVar) {
 		//don't morph into yourself
 		if (p == Item.CHANGE && powerupVar == m) {
 			powerupVar++;
 		}
 		this.pv = powerupVar;
 	}
 	public Land getOnLand() {
 		if (lm == -1 || lid == -1) {
 			return null;
 		}
 		return Warehouse.getMaps()[lm].getPieces().get(lid);
 	}
 	public void setOnLand(Land onLand) {
 		if (onLand == null) {
 			lm = -1;
 			lid = -1;
 		}
 		else {
 			lm = onLand.getMap();
 			lid = onLand.getId();
 		}
 	}
 	public int getScore() {
 		return s;
 	}
 	public void setScore(int score) {
 		this.s = score;
 	}
 	public void gainPoint() {
 		this.s++;
 	}
 	public void losePoint() {
 		this.s--;
 	}
 	public int getLives() {
 		return l;
 	}
 	public void setLives(int lives) {
 		this.l = lives;
 	}
 	public void loseLife() {
 		this.l--;
 	}
 	public void gainLife() {
 		this.l++;
 	}
 
 	/*getters to the RoleModel's properties*/
 	public float getRunSpeed() {
 		if (getOnLand() != null && getOnLand().isSlip()) { //slippery floor
 			return getRoleModel().getRunSpeed()/5;
 		}
 		return getRoleModel().getRunSpeed();
 	}
 	public float getAirSpeed() {
 		return getRoleModel().getAirSpeed();
 	}
 	public float getRunSlip() {
 		if (getOnLand() != null && getOnLand().isSlip()) { //slippery floor
 			return 1-getRoleModel().getRunFrict()/5;
 		}
 		return getRoleModel().getRunSlip();
 	}
 	public float getRunFrict() {
 		if (getOnLand() != null && getOnLand().isSlip()) { //slippery floor
 			return getRoleModel().getRunFrict()/5;
 		}
 		return getRoleModel().getRunFrict();
 	}
 	public float getAirSlip() {
 		return getRoleModel().getAirSlip();
 	}
 	public float getAirFrict() {
 		return getRoleModel().getAirFrict();
 	}
 	public float getMaxSpeed() {
 		return getRoleModel().getMaxSpeed();
 	}
 	public float getJumpPower() {
 		return getRoleModel().getJumpPower();
 	}
 	public int getJumpHold() {
 		return getRoleModel().getJumpHold();
 	}
 	public float getTermVel() {
 		if (isCrouch()) return getRoleModel().getTermVel()*getSink();
 		return getRoleModel().getTermVel();
 	}
 	public float getWallTermVel() {
 		return getRoleModel().getWallTermVel();
 	}
 	public float getGrav() {
 		if (isCrouch()) return getRoleModel().getGrav()*getSink();
 		return getRoleModel().getGrav();
 	}
 	public float getSink() {
 		return getRoleModel().getSink();
 	}
 	public int getSpawnTime() {
 		return getRoleModel().getSpawnTime();
 	}
 	public int getSpawnInv() {
 		return getRoleModel().getSpawnInv();
 	}
 	public int getShotDelay() {
 		return getRoleModel().getShotDelay();
 	}
 	public ShotModel getShot() {
 		return getRoleModel().getShotType();
 	}
 
 	//getter and setter for basic player type
 	public int getModel() {
 		return m;
 	}
 	//also sets derived fields
 	public void setModel(int model) {
 		this.m = model;
 		RoleModel rm = Warehouse.getCharacters()[model];
 		setSkin(rm.getSkin());
 		setW(rm.getW());
 		setH(rm.getH());
 	}
 	/**
 	 * Get the RoleModel object referenced by m (model). 
 	 * @return the actual RoleModel from the warehouse
 	 */
 	private RoleModel getRoleModel() {
 		//Change powerup intercepts your model
 		if (p == Item.CHANGE) {
 			return Warehouse.getCharacters()[pv];			
 		}
 		return Warehouse.getCharacters()[m];
 	}
 }
