 package hu.miracle.workers;
 
 import java.awt.Dimension;
 import java.awt.Point;
import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 public class Scene {
 
 	private static final String className = "Scene";
 
 	// Members
 	private Dimension dimension;
 	private List<Ant> ants;
 	private List<Storage> storages;
 	private List<Obstacle> obstacles;
 	private List<Creature> creatures;
 	private Map<Point, Effect> effects; // Ez elvileg nem lista lesz, de meg nem
 										// tudom megmondani hogy mi
	
	public Scene() {
		this.ants = new ArrayList<Ant>();
		this.storages = new ArrayList<Storage>();
		this.obstacles = new ArrayList<Obstacle>();
		this.creatures = new ArrayList<Creature>();
	}
 
 	// Protected methods
 	// Eltavolitja a szemetet a palyarol (lejart cuccok)
 	protected void clearDebris() {
 		System.out.println(className + " clearDebris");
 	}
 
 	// Public interface
 	public Dimension getDimension() {
 		System.out.println(className + " getDimension");
 		return dimension;
 	}
 
 	public List<Ant> getAnts() {
 		System.out.println(className + " getAnts");
 		return ants;
 	}
 
 	public List<Storage> getStorages() {
 		System.out.println(className + " getStorages");
 		return storages;
 	}
 
 	public List<Obstacle> getObstacles() {
 		System.out.println(className + " getObstacles");
 		return obstacles;
 	}
 
 	public List<Creature> getCreatures() {
 		System.out.println(className + "getCreatures");
 		return creatures;
 	}
 
 	public Map<Point, Effect> discoverEffects(Point point) {
 		System.out.println(className + " discoverEffects");
 		return effects;
 	}
 
 	// Egy pont kornyeki akadalyokat adja vissza
 	// es a mergezeshez kell
 	public List<Obstacle> discoverObstacles(Point point) {
 		System.out.println(className + " discoverObstacles");
 		return obstacles;
 	}
 
 	// uj effekteket tarol el, szagnyom letetelehez szukseges
 	public void placeEffect(Point point, Effect effect) {
 		System.out.println(className + " placeEffect");
 	}
 
 	// pont korzeteben eltunteti az effekteket, sprayhez
 	// szukseges
 	public void clearEffects(Point point) {
 		System.out.println(className + " clearEffect");
 	}
 
 	// uj akadalyt tarol el, sprayhez szukseges
 	public void placeObstacle(Obstacle obstacle) {
 		System.out.println(className + " placeObstacle");
 	}
 
 	// Ez itt csak placeholder, fogalmam sincs hogyan taroljuk a palyakat es
 	// milyen parameterekre lesz szukseg
 	public void buildScene(String settings) {
 		System.out.println(className + " buildScene");
 	}
 
 	public void delegateTick() {
 		System.out.println(className + " delegateTick");
 
 		for (Ant ant : ants) {
 			ant.handleTick();
 		}
 
 		for (Storage storage : storages) {
 			storage.handleTick();
 		}
 
 		for (Obstacle obstacle : obstacles) {
 			obstacle.handleTick();
 		}
 
 		for (Creature creature : creatures) {
 			creature.handleTick();
 		}
 	}
 }
