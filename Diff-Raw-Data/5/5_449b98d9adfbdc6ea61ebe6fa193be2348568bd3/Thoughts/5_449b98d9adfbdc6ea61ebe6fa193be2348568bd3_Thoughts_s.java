 package br.ufrj.jfirn.intelligent;
 
 import java.io.Serializable;
 import java.util.ArrayDeque;
 import java.util.Collection;
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import br.ufrj.jfirn.common.Point;
 
 /**
  * This class represents what are an intelligent robot's thoughts and plans.
  * 
  * @author <a href="mailto:ramiro.p.magalhaes@gmail.com">Ramiro Pereira de Magalhães</a>
  *
  */
 public class Thoughts implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * The position I think I'm at. It may be different from the real position
 	 * in case the sensors get noisy data.
 	 */
 	private Point position;
 
 	/**
 	 * The direction and speed I think I'm going.
 	 */
 	private double direction, speed;
 
 	/**
 	 * True when one should fear for his safety.
 	 */
 	private boolean endangered = false;
 
 	/**
 	 * Obstacles statistics data.
 	 */
 	private Map<Integer, MovementStatistics> obstacleStatistics = new HashMap<>();
 
 	/**
 	 * Known collision forecast for this object. Notice that I may have statistics
 	 * about an object movement, but a certain object may not forecast collisions
 	 * with them.
 	 */
 	private Map<Integer, Collision> collisions = new HashMap<>();
 
 	/**
 	 * The target points of interest in the simulation area to where we want to go.
 	 */
 	private Deque<Point> targets = new ArrayDeque<>();
 
 	/**
 	 * Create a zeroed instance of Thoughts. I think I'm the center of
 	 * the world.
 	 */
 	public Thoughts() {
 		this.position = new Point(0, 0);
 		this.direction = 0;
 		this.speed = 0;
 	}
 
 	/**
 	 * Create an instance from an {@link IntelligentRobot} real data.
 	 */
 	public Thoughts(AbstractIntelligentRobot robot, Deque<Point> targets) {
 		this.position = robot.position();
 		this.direction = robot.direction();
 		this.speed = robot.speed();
 
 		this.targets = targets;
 	}
 
 	/**
 	 * If we have any movement statistics about an obstacle with a certain
 	 * id, then we know it.
 	 */
 	public boolean isKnownObstacle(int objectId) {
 		return obstacleStatistics.containsKey(objectId);
 	}
 
 	public MobileObstacleStatistics obstacleStatistics(int objectId) {
 		return obstacleStatistics.get(objectId);
 	}
 
 	public Collision collisionWith(int objectId) {
 		return collisions.get(objectId);
 	}
 
 	public void addObstacleStatistics(int objectId, Point position, double direction, double speed) {
 		if (!isKnownObstacle(objectId)) {
 			this.obstacleStatistics.put(objectId, new MovementStatistics(objectId));
 		}
 		this.obstacleStatistics.get(objectId).addEntry(position, speed, direction);
 	}
 
 	public void putCollision(int objectId, Collision collision) {
 		this.collisions.put(objectId, collision);
 	}
 
	public Collection<? extends MobileObstacleStatistics> allObstacleStatistics() {
		return this.obstacleStatistics.values();
 	}
 
 	public Collection<Collision> allColisions() {
 		return this.collisions.values();
 	}
 
 	public void removeObstacle(int objectId) {
 		if (isKnownObstacle(objectId)) {
 			this.obstacleStatistics.remove(objectId);
 			this.collisions.remove(objectId);
 		}
 	}
 
 	public void removeCollision(int objectId) {
 		this.collisions.remove(objectId);
 	}
 
 	public void retainObstaclesData(List<Integer> objectIds) {
 		obstacleStatistics.keySet().retainAll(objectIds);
 		collisions.keySet().retainAll(objectIds);
 	}
 
 	public Deque<Point> targets() {
 		return targets;
 	}
 
 	public boolean endangered() {
 		return endangered;
 	}
 
 	public void endangered(boolean e) {
 		endangered = e;
 	}
 
 	public Point myPosition() {
 		return position;
 	}
 
 	public double mySpeed() {
 		return speed;
 	}
 
 	public double myDirection() {
 		return direction;
 	}
 
 	public void myPosition(Point position) {
 		this.position = position;
 	}
 
 	public void mySpeed(double speed) {
 		this.speed = speed;
 	}
 
 	public void myDirection(double direction) {
 		this.direction = direction;
 	}
 
 }
