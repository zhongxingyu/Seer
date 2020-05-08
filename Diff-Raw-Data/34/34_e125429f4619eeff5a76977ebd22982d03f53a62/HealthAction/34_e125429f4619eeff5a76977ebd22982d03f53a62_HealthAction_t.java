 package se.chalmers.dryleafsoftware.androidrally.libgdx.actions;
 
 import java.util.List;
 
import se.chalmers.dryleafsoftware.androidrally.libgdx.AnimatedImage;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.gameboard.MapBuilder;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.gameboard.RobotView;
 
 /**
  * This action changes its robot's life and damage values.
  * 
  * @author
  * 
  */
 public class HealthAction extends GameAction {
 
 	private final int damage, lives;
 
 	public static final int DECREASE_ONE = -2;
 	public static final int INCREASE_ONE = -3;
	private final AnimationAction hitAnim;
 
 	/**
 	 * Creates a new instance which will handle the robot with the specified ID.
 	 * 
 	 * @param robotID
 	 *            The ID of the robot to handle.
 	 * @param damage
 	 *            The damage to set to the robot. Supports UNCHANGED.
 	 * @param lives
 	 *            The number of lives to set to the robot. Supports UNCHANGED.
 	 */
	public HealthAction(int robotID, int damage, int lives, AnimatedImage animation) {
 		super(robotID, 0);
 		this.damage = damage;
 		this.lives = lives;
		this.hitAnim = new AnimationAction(robotID, damage, animation);
 	}
 
 	@Override
 	public void action(List<RobotView> robots, MapBuilder map) {
 		start();
 		// Everything is being done in cleanUp!
 	}
 
 	@Override
 	public void cleanUp(List<RobotView> robots, MapBuilder map) {
 		RobotView robot = robots.get(getRobotID());
		int oldDamage = robot.getDamage();
		int newDamage = oldDamage;
 		if (damage != UNCHANGED) {
 			if (damage == INCREASE_ONE) {
 				robot.setDamage(robot.getDamage() + 1);
 			} else if (damage == DECREASE_ONE) {
 				robot.setDamage(robot.getDamage() - 1);
				newDamage = robot.getDamage();
 			} else {
 				robots.get(getRobotID()).setDamage(damage);
				newDamage = robot.getDamage();
			}
			if(oldDamage < newDamage) {
				hitAnim.setDuration(getDuration());
				hitAnim.action(robots, map);
 			}
 		}
 		if (lives != UNCHANGED) {
 			if (lives == INCREASE_ONE) {
 				robot.setLives(robot.getLives() + 1);
 			} else if (lives == DECREASE_ONE) {
 				robot.setLives(robot.getLives() - 1);
 			} else {
 				robot.setLives(lives);
 			}
 		}
 		if (robots.get(getRobotID()).getLives() == 0) {
 			robots.get(getRobotID()).setDead(true);
 		}
 	}
 }
