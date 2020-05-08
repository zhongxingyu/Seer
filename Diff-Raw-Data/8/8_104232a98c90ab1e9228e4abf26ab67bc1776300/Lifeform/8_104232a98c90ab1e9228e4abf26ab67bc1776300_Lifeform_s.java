 package projectubernahme.lifeforms;
 
 import java.awt.event.KeyEvent;
 import java.awt.geom.Point2D;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import javax.swing.JOptionPane;
 
 import projectubernahme.IngestionThread;
 import projectubernahme.Localizer;
 import projectubernahme.MessageTypes;
 import projectubernahme.Player;
 import projectubernahme.ProjectUbernahme;
 import projectubernahme.TakeoverThread;
 import projectubernahme.Vector2D;
 import projectubernahme.gfx.ConvertedGraphics;
 import projectubernahme.simulator.MainSimulator;
 
 
 /** something with its own mind in the game, can interact with other lifeforms and the world */
 abstract public class Lifeform {	
 	/** the simulator this lifeform is connected to */
 	private MainSimulator sim;
 	
 
 	public Lifeform (MainSimulator sim) {
 		this.setSim(sim);
 		name = new String();
 		id = next_id++;
 		setVelocity(new Vector2D(0.0, 0.0));
 		setSuspicionCases(new CopyOnWriteArrayList<SuspicionCase>());
 	}
 	
 	/*
 	 * the AI
 	 */
 
 	/** this lets the lifeform act, this can be just sitting around or calling for support or attacking another lifeform */
 	public abstract void act(int sleepTime);
 	
 	private CopyOnWriteArrayList<SuspicionCase> suspicionCases;
 	
 	/*
 	 * everything that has to do with movement
 	 * - position
 	 * - velocity
 	 * - rotation
 	 * - distance
 	 */
 
 	private Point2D position;
 
 	public Point2D getPoint2D () {
 		return position;
 	}
 	
 	private Vector2D velocity;
 
 	/** angle of view */
 	private double viewAngle;
 
 	public double getViewAngle() {
 		return viewAngle;
 	}
 
 	public void setViewAngle(double viewAngle) {
 		this.viewAngle = viewAngle;
 	}
 
 	/** movement state variable */
 	private short dvSign;
 
 	/** movement state variable */
 	private short dViewAngleSign;
 
 	/** maximal velocity */
 	private static double vMax = 1.0;
 
 	/** lets the physics work on the lifeform and moves it by its velocities */
 	public void move(int sleepTime) {
 		/* calculate new velocity if this lifeform is controlled */
 		if (isInControlledMode() && !isBusy()) {
 			if (dvSign == 0) {
 				getVelocity().zero();
 			}
 			else {
 				setVelocity(new Vector2D(1.0*dvSign*Math.cos(viewAngle), 1.0*dvSign*Math.sin(viewAngle)));
 			}
 		}
 		
 		getVelocity().lowpassThis(vMax);
 		getVelocity().highpassThis(-vMax);
 	
 		double t = sleepTime/1000.0;
 		neighborsListAge += t;
 		
 		if (isCanMove() || canFly) {
 			Point2D newPosition = getVelocity().multiply(t).add(getPoint2D());
 	
 			/* try to move, check for wall */
 			if (getSim().getEnv().isFreeBetween(getPoint2D(), newPosition)) {
 				setPosition(newPosition);
 			}
 			viewAngle += dViewAngleSign*t;
 		}
 	}
 
 	/** returns the distance to the other lifeform l, currently just the geometric mean of the axes, later it might include some path trough the world */
 	public double distance (Lifeform l) {
 		return getPoint2D().distance(l.getPoint2D());
 	}
 
 	/*
 	 * everything with perception
 	 * - senses
 	 * - neighbors
 	 */
 	
 	private boolean canSee = false;
 	private boolean canSeeIR = false;
 	private boolean canSeeXRay = false;
 	private boolean canHear = false;
 	private boolean canFeel = false;
 	private boolean canSmell = false;
 	private boolean canTaste = false;
 	private boolean canMove = false;
 	private boolean canFly = false;
 	private int rangeOfSight = 0;
 	private double diameter = 0;
 	private double ingestionEff = 0.9;
 	
 	void setIngestionEff(double newEff){
 		this.ingestionEff = newEff;
 	}
 	
 	public double getIngestionEff(){
 		return ingestionEff;
 	}
 
 	void setRangeOfSight(int range){
 		this.rangeOfSight = range;
 	}
 	
 	int getRangeOfSight(){
 		return this.rangeOfSight;
 	}
 	
 	boolean isCanSee() {
 		return canSee;
 	}
 
 	boolean isCanFly() {
 		return canFly;
 	}
 
 	void setCanMove(boolean canMove) {
 		this.canMove = canMove;
 	}
 
 	boolean isCanMove() {
 		return canMove;
 	}
 
 	void setCanSeeIR(boolean canSeeIR) {
 		this.canSeeIR = canSeeIR;
 	}
 
 	boolean isCanSeeIR() {
 		return canSeeIR;
 	}
 
 	void setCanSeeXRay(boolean canSeeXRay) {
 		this.canSeeXRay = canSeeXRay;
 	}
 
 	boolean isCanSeeXRay() {
 		return canSeeXRay;
 	}
 
 	void setCanHear(boolean canHear) {
 		this.canHear = canHear;
 	}
 
 	boolean isCanHear() {
 		return canHear;
 	}
 
 	void setCanFeel(boolean canFeel) {
 		this.canFeel = canFeel;
 	}
 
 	boolean isCanFeel() {
 		return canFeel;
 	}
 
 	void setCanSmell(boolean canSmell) {
 		this.canSmell = canSmell;
 	}
 
 	boolean isCanSmell() {
 		return canSmell;
 	}
 
 	void setCanTaste(boolean canTaste) {
 		this.canTaste = canTaste;
 	}
 
 	boolean isCanTaste() {
 		return canTaste;
 	}
 
 	void setCanSee(boolean canSee) {
 		this.canSee = canSee;
 	}
 
 	void setCanFly(boolean canFly) {
 		this.canFly = canFly;
 	}
 
 	/** decides whether this lifeform can see some other lifeform l */
 	public boolean canSee (Lifeform l) {
 		return (isCanSee() && distance(l) < rangeOfSight+diameter);			
 	}
 	
 	/** list of other lifeforms that this lifeform can see */
 	private ArrayList<Lifeform> neighbors = new ArrayList<Lifeform>();
 
 	/** In order to save power, the list with neighbors is only refreshed when it expires. This tracks the age of the list */
 	private double neighborsListAge = 0.0;
 	
 	public ArrayList<Lifeform> getNeighbors() {
 		if (neighbors == null) {
 			neighbors = new ArrayList<Lifeform>();
 			generateNeighborsList();
 		}
 		/* if the neighbors list is old, generate a new one */
 		if (neighborsListAge > 0.5) {
 			generateNeighborsList();
 			neighborsListAge = 0.0;
 		}
 		return neighbors;
 	}
 
 	/** generates a list with all the lifeforms this one can see */
 	private void generateNeighborsList() {
 		neighbors.clear();
 	
 		/** only traverse if the lifeform can see */
 		if (canSee) {
 			for (Lifeform l : getSim().getLifeforms()) {
 				if (canSee(l) && l != this) {
 					neighbors.add(l);
 				}
 			}
 		}
 	}
 
 	/*
 	 * everything with actions
 	 * - ingestion
 	 * - takeover
 	 */
 
 	/** whether this lifeform is carrying out some sort of action */
 	private boolean busy;
 
 	/** progress with that action, from 0.0 to 1.0 */
 	private double actionProgress;
 
 	/** thread that handles the current action */
 	private Thread actionThread;
 
 	public boolean canTakeover(Lifeform prey) {
 		if (prey == this) {
 			return false;
 		}
 		double random1 = Math.random()/0.5 + 0.5;
 		double random2 = Math.random()/0.5 + 0.5;
 		double sizeFactor = prey.getBiomass()*random1 / this.biomass*random2;
 		double intelligenceFactor = prey.getIntelligence();
 		if (sizeFactor * intelligenceFactor < 5 && canSee(prey)) {
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 
 	/** takes over control of the given lifeform, returns whether that was successful */
 	public boolean takeover(Lifeform lifeform) {
 		if (canTakeover(lifeform) && !isBusy()) {
 			return startTakeover(lifeform);
 		}
 		return false;
 	}
 
 	private boolean startTakeover(Lifeform lifeform) {
 		actionThread = new TakeoverThread(this, getControllingPlayer(), lifeform, getSim());
 		actionThread.start();
 		return true;
 	}
 
 	public boolean canIngest(Lifeform prey) {
 		if (prey == this) {
 			return false;
 		}
 		return canSee(prey);
 	}
 
 	/** ingests the given lifeform */
 	public void ingest(Lifeform whom) {
 		if (whom == null)
 			return;
 
 		if (canIngest(whom) && !isBusy()) {
 			startIngestion(whom);
 		}
 	}
 
 	private void startIngestion(Lifeform whom) {
 		actionThread = new IngestionThread(this, getControllingPlayer(), whom, getSim());
 		actionThread.start();		
 	}
 
 	/** stop whatever that lifeform is doing right now */
 	private void stopAction() {
 		if (actionThread != null && actionThread.isAlive()) {
 			actionThread.interrupt();
 		}
 	}
 	
 	/*
 	 * everything with key handling
 	 * - key pressed
 	 * - key released
 	 */
 
 	/** lets the lifeform handle a key press */
 	public void handleKeyPressed (KeyEvent e) {
 		switch (e.getKeyChar()) {
 		case 'w': dvSign = 1; break;
 		case 's': dvSign = -1; break;
 		case 'a': dViewAngleSign = -3; break;
 		case 'd': dViewAngleSign = 3; break;
 		}
 
 		stopAction();
 	}
 
 	/** lets the lifeform handle a key release */
 	public void handleKeyReleased (KeyEvent e) {
 		switch (e.getKeyChar()) {
 		case 'w':
 		case 's': dvSign = 0; break;
 		case 'a':
 		case 'd': dViewAngleSign = 0; break;
 		case 'p': if (isControlled()) {
 			setInControlledMode(!isInControlledMode());
 			stopAction();
 			break;
 		}
 		}
 	}
 
 	/*
 	 * everything with external control by a player
 	 */
 	
 	/** the player who controls this lifeforms, if null, the computer controls it */
 	private Player controllingPlayer = null;
 	
 	private boolean inControlledMode = false;
 
 	public boolean isControlled() {
 		return getControllingPlayer() != null;
 	}
 
 	public void setControlled(Player p) {
 		this.setControllingPlayer(p);
 		setInControlledMode(true);
 	}
 	
 	/*
 	 * everything with personal properties
 	 * - ID
 	 * - name
 	 * - biomass
 	 *   - diameter
 	 * - graphics
 	 * - intelligence
 	 */
 
 	/** unique id */
 	private int id;
 
 	/** main id counter, determines the next given id */
 	private static int next_id = 0;
 
 	int getID() {
 		return id;
 	}
 
 	/** given name of the object */
 	private String name = new String();
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String value) {
 		name = value;
 	}
 
 	/** the mass of biological stuff the lifeform ingested so far */
 	private double biomass = 0.0;
 
 	public double getBiomass() {
 		return biomass;
 	}
 
 	public void setBiomass(double biomass) {
 		this.biomass = biomass;
 		this.diameter = Math.pow(biomass/1000, 1.0/3.0);
 	}
 
 	/** gives a generic description string of the object */
 	public String toString () {
 		if (name.equals("")) {
 			return getI18nClassName()+(
 					getBiomass() != 0 ? " \t["+ProjectUbernahme.format(getBiomass())+" kg]" : "");
 			
 		}
 		else {
 			return getName()+" \t("+getI18nClassName()+")"+(
 					getBiomass() != 0 ? " \t["+ProjectUbernahme.format(getBiomass())+" kg]" : "");
 		}
 	}
 
 	public double getDiameter() {
 		return diameter;
 	}
 
 	public abstract ConvertedGraphics getConvertedGraphics();
 
 	/** intelligence level, 1.0 would be a genius */
 	private double intelligence;
 	
 	void setIntelligence(double intelligence) {
 		this.intelligence = intelligence;
 	}
 
 	double getIntelligence() {
 		if (getControllingPlayer() != null) {
 			return intelligence*getControllingPlayer().getIntFactor();
 		}
 		else {
 			return intelligence;
 		}
 	}
 
 	void setVelocity(Vector2D velocity) {
 		this.velocity = velocity;
 	}
 
 	public Vector2D getVelocity() {
 		return velocity;
 	}
 
 	public boolean willSuspect(Lifeform l) {
 		// TODO tweak the score system
 		double points = 0.0;
 		
 		/* the smaller the other lifeform the less will it become looked at */
 		points += Math.log(l.getBiomass()/getBiomass());
 		
 		/* the more intelligent the other lifeform is, the less points */
 		points += 10*(intelligence - l.intelligence);
 		
 		if (ProjectUbernahme.getVerboseLevel() >= 5)
 			ProjectUbernahme.log(MessageFormat.format(Localizer.get("{0} has {1} suspicion points against {2}."), new Object[] {toString(), points, l.toString()}), MessageTypes.INFO);
 		
 		return points > 1;
 	}
 	
 	public void rename () {
 		String input = JOptionPane.showInputDialog(Localizer.get("new name")+":");
 		if (input != null) {
 			setName(input);
 		}
 	}
 
 	public abstract String getI18nClassName();
 
 	void setSuspicionCases(CopyOnWriteArrayList<SuspicionCase> suspicionCases) {
 		this.suspicionCases = suspicionCases;
 	}
 
 	public CopyOnWriteArrayList<SuspicionCase> getSuspicionCases() {
 		return suspicionCases;
 	}
 
 	public void setActionProgress(double actionProgress) {
 		this.actionProgress = actionProgress;
 	}
 
 	public double getActionProgress() {
 		return actionProgress;
 	}
 
 	public void setBusy(boolean busy) {
 		this.busy = busy;
 	}
 
 	public boolean isBusy() {
 		return busy;
 	}
 
 	void setControllingPlayer(Player controllingPlayer) {
 		this.controllingPlayer = controllingPlayer;
 	}
 
 	public Player getControllingPlayer() {
 		return controllingPlayer;
 	}
 
 	public void setInControlledMode(boolean inControlledMode) {
 		this.inControlledMode = inControlledMode;
 	}
 
 	public boolean isInControlledMode() {
 		return inControlledMode;
 	}
 
 	void setPosition(Point2D position) {
 		this.position = position;
 	}
 
 	void setSim(MainSimulator sim) {
 		this.sim = sim;
 	}
 
 	MainSimulator getSim() {
 		return sim;
 	}
 }
