 package game;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Stack;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 
 import orig.Creature;
 import orig.DungeonMap;
 import orig.Item;
 import util.General.GridPoint;
 import util.ImageUtil;
 
 public class OnScreenChar implements Serializable {
 	static final int TICKLIMIT = 180;
 	int tickCounter = 0;
 	transient Image looks;
 	transient Image looksFlipped;
 	String imgName;
 	int xPos;
 	int yPos;
 	// the location the monster intends to move to
 	int speed;
 	double ratioMovement;
 	double stackedMovement;
 	public Creature baseCreature;
 	final int tileSize = 32;
 	boolean isPlayer;
 	Stack<GridPoint> pathPlan;
 	
 	public OnScreenChar(int X, int Y, Creature c) {
 		this(c.getRName(), X, Y, c);
 	}
 	
 	public OnScreenChar(String imgName, int X, int Y, Creature c){
 		xPos = X;
 		yPos = Y;
 		baseCreature = c;
 		this.imgName = imgName;
 		speed = 1; //baseCreature.get(bStats.SPEED, sVal.LEVEL)
 		ratioMovement = speed;
 		stackedMovement = 0;
 		isPlayer = false;
 	}
 	
 	public OnScreenChar(int X, int Y, Creature c, boolean b){
 		this(initializeSprite(c), X, Y, c);
 	}
 	
 	public void draw(int mapX, int mapY, Graphics g){
 		// draw sprite image
 		if (looks == null) {
 			looks = ImageUtil.getImage(this.imgName);
 			looksFlipped = looks.getFlippedCopy(true, false);
 		}
 		if (tickCounter < TICKLIMIT)
 			looks.draw((xPos-mapX)*tileSize, (yPos-mapY)*tileSize);
 		else
 			looksFlipped.draw((xPos-mapX)*tileSize, (yPos-mapY)*tileSize);
 		
 		++tickCounter;
 		if (tickCounter == TICKLIMIT*2)
 			tickCounter = 0;
 		
 		// if player, put the name
 		if (isPlayer()) {
 			g.drawString(baseCreature.getName(), (xPos-mapX-1)*tileSize, (yPos-mapY-1)*tileSize);
 			g.setColor(Color.black);
 		}
 	}
 	
 	public void initializeRatio(int playerSpeed){
 		ratioMovement = ((double)speed)/((double)playerSpeed);
 	}
 	
 	//useless
 	public void step(int x, int y, DungeonMap dm){
 		if(xPos>x){
 			move(-1, 0, dm);
 		}
 		else if(xPos<x){
 			move(1, 0, dm);
 		}
 		if(yPos>y){
 			move(0, -1, dm);
 		}
 		else if(yPos<y){
 			move(0, 1, dm);
 		}
 	}
 	
 	/**
 	 * This method tells the OnScreenChar to ask the dmap for information about
 	 * the area around itself, particularly if there is treasure or a player
 	 * SMART:
 	 * If a player is detected, set the current 'target loc' to be the player's sq
 	 * ask the dmap to send it on the best path
 	 * SMART:
 	 * If a player *was* detected, and we haven't reached the last known loc,
 	 * keep moving toward the last known loc.
 	 * GREEDY:
 	 * If nothing about the player is known, was there treasure? Move toward treasure
 	 * DEFAULT:
 	 * Nothing at all? Random.
 	 * @param radius The area around the monster which it can see
 	 * @param dm The current map
 	 */
 	public void move(int radius, DungeonMap dm) {
 		// find targets of interest:
 		ArrayList<ArrayList<GridPoint>> targets = dm.detectArea(radius, this);
 		ArrayList<GridPoint> enemies = targets.get(0);
 		ArrayList<GridPoint> treasure = targets.get(1);
 		int totalTargets = enemies.size() + treasure.size();
 		// if nothing interesting around
 		if (totalTargets == 0) {
 			System.out.printf("TotalTargets = 0!\n");
 			if (pathPlan != null && !pathPlan.isEmpty()) {
 				System.out.printf("\tWe have a plan!\n");
 				// we had a plan, so we'll follow it cause nothing else to do
 				// assuming that the plan has no invalid moves
 				GridPoint next = pathPlan.pop();
 				System.out.printf("\tAttackmove from (%d, %d) to %s\n", xPos, yPos, next);
 				dm.attackMove(xPos, yPos, next.getX(), next.getY(), true);
 			} else {
 				System.out.printf("\tWe have no plan...\n");
 				// no plan, or nowhere else to go according to plan
 				// random move
 				// assuming for now that creature can move any direction
 				boolean notFound = true;
 				while (notFound) {
 					int dx = (int)(Math.random() * 3) - 1;
 					int dy = (int)(Math.random() * 3) - 1;
 					if (dm.isPassable(dx+xPos, dy+yPos)) {
 						dm.attackMove(xPos, yPos, dx+xPos, dy+yPos, true);
 						notFound = false;
 					}
 				}
 			}
 		} else {
 			if (enemies.size() != 0) {
 				// there is an enemy that we can see.
 				// charge!
 				GridPoint enemy = enemies.get(0);
 				System.out.printf("There is an enemy at %s\n", enemy);
 				dm.moveOSC(xPos, yPos, enemy.getX(), enemy.getY());
 				// there should always be a move, if there is a pathPlan
 				// if no pathPlan, bad data was passed into moveOSC call
 				if (pathPlan != null) {
 					enemy = pathPlan.pop();
 					System.out.printf("\tWe formulated a plan, to move to %s\n", enemy);
 					dm.attackMove(xPos, yPos, enemy.getX(), enemy.getY(), true);
 				}
 			} else if (treasure.size() != 0) {
 				// there is a treasure we can see.
 				// walk over there, you love treasure
 				GridPoint item = treasure.get(0);
 				dm.moveOSC(xPos, yPos, item.getX(), item.getY());
 				if (pathPlan != null) {
 					item = pathPlan.pop();
 					dm.attackMove(xPos, yPos, item.getX(), item.getY(), true);
 				}
 			}
 		}
 		// lastly, after moving, see if it can detect any enemies and come up with a plan.
 		targets = dm.detectArea(radius, this);
 		enemies = targets.get(0);
 		System.out.printf("After moving, try to detect enemies");
 		if (enemies.size() != 0) {
 			System.out.printf("\tAftermove Enemy detected!\n");
 			// there is an enemy, so make a plan
 			GridPoint enemy = enemies.get(0);
 			dm.moveOSC(xPos, yPos, enemy.getX(), enemy.getY());
 		} // else do nothing
 	}
 	
 	public void move(int left, int up, DungeonMap dm){
 		if(left>1||left<-1||up>1||up<-1) return;
 		dm.removeOnScreenChar(xPos, yPos);
 		stackedMovement = stackedMovement+ratioMovement;
 		int movement = (int)stackedMovement;
 		if(movement>0){
 			stackedMovement = stackedMovement-movement;
 			if(dm.isPassable(xPos+left*movement, yPos+up*movement)){
 				this.xPos += left*movement;
 				this.yPos += up*movement;
 			}
 			else{
 				for(int i=left*movement; i!=0; i-=left){
 					if(dm.isPassable(xPos+i,yPos)){
 						this.xPos+=i;
 						break;
 					}
 				}
 				for(int i=up*movement; i!=0; i-=up){
 					if(dm.isPassable(xPos, yPos+i)){
 						this.yPos+=i;
 						break;
 					}
 				}
 			}
 		}
 		dm.putOnScreenChar(xPos, yPos, this, true);
 	}
 	
 	/**
 	 * ONLY DUNGEONMAP SHOULD USE THIS
 	 * This is used when the player steps on a door/stairs, to re-adjust their position
 	 * @param newX
 	 * @param newY
 	 */
 	public void setPosition(int newX, int newY) {
 		this.xPos = newX;
 		this.yPos = newY;
 	}
 	
 	public boolean canMove(int left, int up, DungeonMap dm){
 		boolean b = dm.isPassable(xPos+left, yPos+up);
 		if(b){
			move(left, up, dm);
 		}
 		return b;
 	}
 	
 	public void pickup(Item i){
 		baseCreature.pickup(i);
 	}
 	
 	public ArrayList<Item> getInventory(){
 		return baseCreature.getInventory();
 	}
 	
 	public String getEquipped(int i){
 		return baseCreature.getEquipped(i);
 	}
 	
 	public int getNumArms(){
 		return baseCreature.getNumArms();
 	}
 	
 	public void equip(Item i){
 		baseCreature.equip(i);
 	}
 	
 	public void drop(Item i){
 		baseCreature.drop(i);
 	}
 
 	public boolean isPlayer() {
 		return isPlayer;
 	}
 	
 	public void setAsPlayer() {
 		isPlayer = true;
 	}
 
 	public static String initializeSprite(Creature c){
 		String str="";
 		str+=c.getNumArms()<=4 ? c.getNumArms() : 6;
 		str+=c.getNumLegs()<=4 ? c.getNumLegs() : 6; 
 		str+=c.getRaceKey()%10;
 		return str;
 	}
 	
 	public int getX() {
 		return this.xPos;
 	}
 	
 	public int getY() {
 		return this.yPos;
 	}
 
 	public void setPathPlan(Stack<GridPoint> path) {
 		this.pathPlan = path;
 	}
 	
 	public boolean detects(OnScreenChar osc) {
 		return this.baseCreature.detects(this.xPos,this.yPos,osc.getX(),osc.getY(),osc.baseCreature);
 	}
 	
 	//attack, move, etc, will call the creature's attach stuff, I'll probably also do the same abstraction for inventory at some point...
 }
