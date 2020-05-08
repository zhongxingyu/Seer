 package balle.world.filter;
 
 import balle.misc.Globals;
 import balle.world.Coord;
 import balle.world.Snapshot;
 import balle.world.objects.Robot;
 
 /**
  * Class for filtering input from world to account for height differences
  * between the detected position and the real position at ground level.
  * 
  * @author s0952880
  */
 public class HeightFilter implements Filter {
 
 	/**
 	 * Point directly below camera.
 	 */
 	private Coord worldCenter;
 
 	/**
 	 * Height of camera above worldCenter.
 	 */
 	private double cameraHeight;
 
 	/**
 	 * Constructor for HeightFilter.
 	 * 
 	 * @param worldCenter
 	 *            Point directly below the camera.
 	 * @param cameraHeight
 	 *            The height of the camera above worldCenter.
 	 */
 	public HeightFilter(Coord worldCenter, double cameraHeight) {
 		this.worldCenter = worldCenter;
 		this.cameraHeight = cameraHeight;
 	}
 
 	@Override
 	public Snapshot filter(Snapshot s) {
 		Robot nBalle = new Robot(filter(s.getBalle().getPosition(),
 				Globals.ROBOT_HEIGHT), s.getBalle().getVelocity(), s.getBalle()
 				.getOrientation());
 
 		Robot nOpp = new Robot(filter(s.getOpponent().getPosition(),
 				Globals.ROBOT_HEIGHT), s.getOpponent().getVelocity(), s
 				.getOpponent().getOrientation());
 
 		return new Snapshot(nOpp, nBalle, s.getBall(), s.getOpponentsGoal(),
 				s.getOwnGoal(), s.getPitch(), s.getTimestamp());
 	}
 
	private Coord filter(Coord in, double height) {
 		if (in == null)
 			return null;
 
 		Coord d = in.sub(worldCenter);
 
		double ratio = height / cameraHeight;
		d.mult(ratio);
		d.add(worldCenter);
 
 		return d;
 	}
 
 }
