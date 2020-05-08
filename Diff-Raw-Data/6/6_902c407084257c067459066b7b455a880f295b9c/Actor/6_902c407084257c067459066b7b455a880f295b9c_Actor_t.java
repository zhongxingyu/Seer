 package game;
 
 /**
  * Class for the player's characters
  * 
  * @author Jacob Charles
  *
  */
 public class Actor extends GameObject {
 
 	//current data
 	private int at; //air time
 	private int dt; //dead time
 	private int r; //reload
 	private int dir = 1; //direction
 	private int p; //power up
 	private Land ol; //current land underfoot
 	private int s; //score
 	private int l; //lives
 
 	private int m; //selected RoleModel from the Warehouse
 	
 	/**
 	 * Spawn a player character a location
 	 * 
 	 * @param x
 	 * 		starting x
 	 * @param y
 	 * 		starting y
 	 */
 	/*public Actor (int x, int y) {
 		super(x, y, 16, 16);
 		dir = 1;
 		at = 1;
 		ol = null;
 		p = 0;
 		
 		//default stats
 		runSpeed = 3;
 		jumpPower = 10;
 		shotDelay = 16;
 		shotSpeed = 16;
 		shotLife = 20;
 		shotWid = 8;
 		shotHei = 8;
 	}*/
 	
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
		
		dir = 1;
		at = 1;
		ol = null;
		p = 0;
		
 		RoleModel rm = Warehouse.getCharacters()[character];
 		setModel(character);
 		setSkin(rm.getSkin());
 		setW(rm.getW());
 		setH(rm.getH());
 	}
 	
 	/*getters and setters for attributes*/
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
 	public int getPowerup() {
 		return p;
 	}
 	public void setEquip(int powerup) {
 		this.p = powerup;
 	}
 	public Land getOnLand() {
 		return ol;
 	}
 	public void setOnLand(Land onLand) {
 		this.ol = onLand;
 	}
 	public int getScore() {
 		return s;
 	}
 
 	public void setScore(int score) {
 		this.s = score;
 	}
 
 	public int getLives() {
 		return l;
 	}
 
 	public void setLives(int lives) {
 		this.l = lives;
 	}
 
 	/*getters to the RoleModel's properties*/
 	public int getSkin() {
 		return Warehouse.getCharacters()[m].getSkin();
 	}
 	public int getRunSpeed() {
 		return Warehouse.getCharacters()[m].getRunSpeed();
 	}
 	public int getAirSpeed() {
 		return Warehouse.getCharacters()[m].getAirSpeed();
 	}
 	public int getRunMomentum() {
 		return Warehouse.getCharacters()[m].getRunMomentum();
 	}
 	public int getAirMomentum() {
 		return Warehouse.getCharacters()[m].getAirMomentum();
 	}
 	public int getJumpPower() {
 		return Warehouse.getCharacters()[m].getJumpPower();
 	}
 	public int getTermVel() {
 		return Warehouse.getCharacters()[m].getTermVel();
 	}
 	public int getGravNum() {
 		return Warehouse.getCharacters()[m].getGravNum();
 	}
 	public int getGravDen() {
 		return Warehouse.getCharacters()[m].getGravDen();
 	}
 	public int getShotDelay() {
 		return Warehouse.getCharacters()[m].getShotDelay();
 	}
 	public ShotModel getShot() {
 		return Warehouse.getCharacters()[m].getShotType();
 	}
 
 	//getter and setter for basic player type
 	public int getModel() {
 		return m;
 	}
 	public void setModel(int model) {
 		this.m = model;
 	}
 }
