 package airplane.g2.waypoint.pathcalc;
 
 import airplane.g2.util.PlaneUtil;
 import airplane.g2.waypoint.avoidance.*;
 import airplane.g2.waypoint.avoidance.AvoidMethod.PlaneIndex;
 
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.HashMap;
 
 import org.apache.log4j.Logger;
 
 import airplane.g2.waypoint.PlaneCollision;
 import airplane.g2.waypoint.PlanePath;
 import airplane.sim.Plane;
 
 public class SimplePathCalculator extends PathCalculator{
 	private Logger logger = Logger.getLogger(this.getClass());
 
 	//TODO Better method of adding waypoint.
 	@Override
 	public void calculatePaths(HashMap<Plane, PlanePath> waypointHash) {
 		ArrayList<PlanePath> paths = new ArrayList<PlanePath>(waypointHash.values());
 		
 		Boolean thereWasACrash = true;
		for(int x = 0, limit = 10; thereWasACrash && x < limit; x ++) {
 			for(int i = 0, count = paths.size(); i < count; i++) {
 				for(int j = i + 1; j < count; j++) {
 					thereWasACrash = putNewPathsIfPlanesAtIndicesCollide(waypointHash, new ArrayList<PlanePath>(waypointHash.values()), i, j);
 				}
 			}
 		}
 	}
 	
 	protected Boolean putNewPathsIfPlanesAtIndicesCollide(
 			HashMap<Plane, PlanePath> waypointHash,
 			ArrayList<PlanePath> paths,
 			int i, int j) {
 		PlanePath path1 = paths.get(i);
 		PlanePath path2 = paths.get(j);
 		
 		if(pathPlanesAreEqual(path1, path2)) return false;
 		
 		PlaneCollision collision = collidePlanePaths(path1, path2);
 		if(collision == null) return false;
 		
 		putPaths(waypointHash, planePathsDidCollide(collision));
 		return true;
 	}
 	
 	protected void putPaths(HashMap<Plane, PlanePath> waypointHash, PlanePath[] paths) {
 		for(PlanePath path: paths) {
 			putPath(waypointHash, path);
 		}
 	}
 	
 	protected void putPath(HashMap<Plane, PlanePath> waypointHash, PlanePath path) {
 		waypointHash.put(path.getPlane(), path);
 	}
 	
 	protected Boolean pathPlanesAreEqual(PlanePath path1, PlanePath path2) {
 		Plane p1 = path1.getPlane();
 		Plane p2 = path2.getPlane();
 		
 		return PlaneUtil.planesAreEqual(p1, p2);
 	}
 	
 	public int slowestArrivalStep(PlanePath[] paths) {
 		Integer slowest = Integer.MIN_VALUE;
 		for(PlanePath path: paths) {
 			if(path.getArrivalStep() > slowest) {
 				slowest = path.getArrivalStep();
 			}
 		}
 		return slowest;
 	}
 	
 	public PlanePath[] planePathsDidCollide(PlaneCollision collision) {
 		AvoidMethod[] methods = getAvoidMethods();
 		int slowest = Integer.MAX_VALUE;
 		PlanePath[] bestPaths = new PlanePath[]{collision.getPath1(), collision.getPath2()};
 		logger.warn("HEY!" + methods.length);
 		for(AvoidMethod avoid: methods) {
 			PlanePath[] pathsForAvoidMethod = avoid.avoid(
 					collision.getPath1(), collision.getPath2(), collision);
 			
 			//if(planesCollideBeforePreviousCollision(collision, pathsForAvoidMethod)) continue;
 			
 			int stepForAvoidMethod = slowestArrivalStep(pathsForAvoidMethod);
 
 			PlaneCollision newCollision = collidePlanePaths(pathsForAvoidMethod[0], pathsForAvoidMethod[1]);
			logger.error(newCollision);
 			
 			if(newCollision == null && stepForAvoidMethod < slowest) {
 				logger.warn("Got in null");
 				slowest = stepForAvoidMethod;
 				bestPaths = pathsForAvoidMethod;
 			}
 		}
 		return bestPaths;
 	}
 	
 	public Boolean planesCollideBeforePreviousCollision(PlaneCollision collision, PlanePath[] paths) {
 		PlaneCollision newCollision = collidePlanePaths(paths);
 		if(newCollision == null) return false;
 		
 		return collision.getRound() >= newCollision.getRound();
 	}
 	
 	public PlaneCollision collidePlanePaths(PlanePath a, PlanePath b) {
 		return a.getPlaneCollision(b);
 	}
 	
 	public PlaneCollision collidePlanePaths(PlanePath[] paths) {
 		return collidePlanePaths(paths[0], paths[1]);
 	}
 	
 	public Boolean doesCollide(PlanePath a, PlanePath b) {
 		return collidePlanePaths(a, b) != null;
 	}
 	
 	public AvoidMethod[] getAvoidMethods() {
 		return new AvoidMethod[] {
 				new AvoidByMove(PlaneIndex.PLANE_ONE, new Point2D.Double(0, -8)),
 				new AvoidByMove(PlaneIndex.PLANE_ONE, new Point2D.Double(0, 20)),
 				new AvoidByDelay(PlaneIndex.PLANE_ONE, 10),
 				new AvoidByDelay(PlaneIndex.PLANE_ONE, 20),
 				new AvoidByMove(PlaneIndex.PLANE_ONE, new Point2D.Double(10, -10)),
 				new AvoidByMove(PlaneIndex.PLANE_ONE, new Point2D.Double(10, 10)),
 				new AvoidByMove(PlaneIndex.PLANE_ONE, new Point2D.Double(-10, -10)),
 				new AvoidByMove(PlaneIndex.PLANE_ONE, new Point2D.Double(-10, -10))
 		};
 	}
 	
 
 }
