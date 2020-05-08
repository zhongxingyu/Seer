 package balle.strategy.pathFinding;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import balle.strategy.curve.Interpolator;
 import balle.strategy.pathFinding.path.Path;
 import balle.strategy.pathFinding.path.ReversePath;
 import balle.world.Coord;
 import balle.world.Orientation;
 import balle.world.Snapshot;
 import balle.world.objects.Robot;
 
 public class ForwardAndReversePathFinder extends SimplePathFinder {
 
 	public ForwardAndReversePathFinder(Interpolator i) {
 		super(i);
 	}
 
 	@Override
 	public List<Path> getPaths(Snapshot s, Coord end, Orientation endAngle)
 			throws ValidPathNotFoundException {
 		Robot robot = s.getBalle();
 
 		List<Path> list;
 
 		// full forwards
 		try {
 			list = super.getPaths(s, robot.getPosition(),
 					robot.getOrientation(), end, endAngle);
 		} catch (ValidPathNotFoundException e) {
 			list = new ArrayList<Path>();
 		}
 
 		// full backwards
 		try {
 			List<Path> sList = super.getPaths(s, robot.getPosition(), robot
 					.getOrientation().getOpposite(), end, endAngle);
 
 			for (Path each : sList)
				list.add(new ReversePath(each));
 
 		} catch (ValidPathNotFoundException e) {
 			if (list.size() == 0)
 				throw new ValidPathNotFoundException(
 						"Neither backward or forward path found.");
 		}
 
 		return list;
 	}
 
 }
