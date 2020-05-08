 package br.com.mibsim.specie;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.List;
 
 import br.com.etyllica.core.graphics.Graphic;
 import br.com.etyllica.core.graphics.SVGColor;
 import br.com.etyllica.layer.AnimatedLayer;
 import br.com.etyllica.layer.ImageLayer;
 import br.com.etyllica.linear.Point2D;
 import br.com.etyllica.linear.PointInt2D;
 import br.com.mibsim.building.basement.Basement;
 import br.com.mibsim.fx.Dialog;
 import br.com.mibsim.planning.PlanningAction;
 import br.com.mibsim.planning.PlanningTask;
 import br.com.tide.action.player.ActionPlayer;
 
 public class Speciemen extends ActionPlayer {
 
 	protected int health = 10000;
 	protected int currentHealth = health;
 	protected int hungryLimiar = health/3;
 	protected int metabolism = 2;
 
 	protected int breathEnergy = 1;
 	protected int walkEnergy = 5;
 	protected int reportEnergy = 10;
 	protected int turnEnergy = 1;
 	protected int sensorRadius = 100;
 
 	protected boolean dead = false;
 	protected boolean hungry = false;
 
 	protected AnimatedLayer layer;
 	protected ImageLayer deadLayer;
 
 	private Dialog dialog = new Dialog();
 
 	protected Basement basement;
 
 	private PlanningTask lastTask;
 
 	protected List<PlanningTask> tasks = new ArrayList<PlanningTask>();
 
 	public Speciemen(int x, int y, int tileW, int tileH, String path, Basement basement) {
 		super(x, y);
 
 		startAngle = 0;
 
 		layer = new AnimatedLayer(x, y, tileW, tileH, path);
 		layer.setAnimateHorizontally(false);
 		layer.setSpeed(100);
 		layer.setFrames(7);
 
 		if(basement != null) {
 			this.basement = basement;
 
 			tasks.add(new PlanningTask(PlanningAction.REPORT, basement.getCenter()));
 		}
 	}
 
 	@Override
 	public void update(long now) {
 		super.update(now);
 
 		if(dead)
 			return;
 
 		act();
 
 		if(isWalking()) {
 			walk(now);
 			loseEnergy(walkEnergy);
 		} else if(isTurning()) {
 			layer.setAngle(angle);
 			loseEnergy(turnEnergy);
 		} else {
 			loseEnergy(breathEnergy);	
 		}
 
 		if(currentHealth <= 0) {
 			die(now);			
 		}
 
 		if(!hungry) {
 			if(isHungry()) {
 				hungry = true;
 				dialog.showHungryDialog();
 			}
 		}
 	}
 
 	private void walk(long now) {
 		layer.animate(now);
 		layer.setCoordinates(x, y);
 
 		dialog.setCoordinates(x, y);
 	}
 
 	private void die(long now) {
 		dead = true;
 
 		deadLayer.centralize(layer);
 	}
 
 	private void loseEnergy(int energy) {
 		currentHealth -= energy*metabolism;
 	}
 
 	private void act() {
 		
 		if(tasks.isEmpty())
 			return;
 
 		PlanningTask currentTask = currentTask();
 		
 		PointInt2D target = currentTask.getTarget();
 
 		if(currentTask != lastTask) {
 			lastTask = currentTask;
 			turnToTarget(target);
 		}
 
 		if(!reachTarget(target)) {
 
 			if(!isWalking()) {
 				walkForward();
 			}
 
 		} else {
 			stopWalk();
 
 			if(!currentTask.isCompleted()) {
 				completeTask(currentTask);
 				tasks.remove(currentTask);
 			}
 		}
 
 	}
 
 	private void completeTask(PlanningTask task) {
 
 		task.setCompleted(true);
 		
 		switch (task.getAction()) {
 
 		case REPORT:
			dialog.showReportDialog();			
 			askDesignation(task);
 			break;
 
 		case EXPLORE:
 			dialog.showExploreDialog();
			reportBasement();
 			break;
 
 		case FEED:
 			break;
 		}
 	}
 	
 	private void askDesignation(PlanningTask report) {
 		if(basement == null)
 			return;
 		
		tasks.add(basement.askForDesignation(report));
 	}
 	
	private void reportBasement() {
 		if(basement == null)
 			return;
 		
		tasks.add(basement.reportToBasement());
 	}
 
 	private boolean isHungry() {
 		return currentHealth<hungryLimiar;
 	}
 
 	private void turnToTarget(PointInt2D target) {
 
 		int cx = layer.getX()+layer.utilWidth()/2;
 		int cy = layer.getY()+layer.utilHeight()/2;
 
 		double angle = Point2D.angle(cx, cy, target.getX(), target.getY());
 
 		this.setStartAngle(angle+90);
 
 		//Compensate sprite rotation
 		layer.setAngle(angle+90);
 
 	}
 
 	private boolean reachTarget(PointInt2D target) {
 
 		int cx = layer.getX()+layer.utilWidth()/2;
 		int cy = layer.getY()+layer.utilHeight()/2;
 
 		double distance = Point2D.distance(cx, cy, target.getX(), target.getY());
 
 		return distance < 10;		
 	}
 
 	private PlanningTask currentTask() {
 		return tasks.get(tasks.size()-1);
 	}
 	
 	public void addTask(PlanningTask task) {
 		tasks.add(task);
 	}
 
 	public Basement getBasement() {
 		return basement;
 	}
 
 	public void setBasement(Basement basement) {
 		this.basement = basement;
 	}
 
 	public void draw(Graphic g, int x, int y) {
 
 		if(!dead) {
 			layer.draw(g, x, y);
 			dialog.draw(g, x, y);
 		} else {
 			deadLayer.draw(g, x, y);
 		}
 	}
 
 	public void drawSensors(Graphic g, int x, int y) {
 		g.setColor(Color.BLACK);
 		g.setAlpha(50);
 		g.fillCircle(layer.getX()+layer.utilWidth()/2+x, layer.getY()+layer.utilHeight()/2+y, sensorRadius);
 		g.resetOpacity();
 	}
 
 	public void drawHealthBar(Graphic g, int x, int y) {
 
 		int border = 1;
 
 		g.setColor(Color.BLACK);
 
 		g.fillRect(layer.getX()+x, layer.getY()+y, layer.utilWidth(), 4*border);
 
 		g.setColor(healthColor());
 
 		int width = layer.utilWidth()*currentHealth/health;
 
 		g.fillRect(layer.getX()+x+border, layer.getY()+y+border, width-2*border, 2*border);
 	}
 
 	private Color healthColor() {
 		if(isHungry())
 			return Color.RED;
 
 		return SVGColor.LIME_GREEN;
 	}
 
 }
