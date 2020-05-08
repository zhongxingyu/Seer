 package raisa.simulator;
 
 import java.util.Arrays;
 import java.util.List;
 
 import raisa.domain.WorldModel;
 
 /**
  * Makes scans to several directions around central heading and returns the
  * shortest value.
  * 
  */
 public class SonarDistanceScanner extends IRDistanceScanner {
 	// simulate wide beam by doing several scans and taking the minimum distance
 	private static final List<Float> beamHeadings = Arrays.asList(-5f, -2.5f, 0f, 2.5f, 5f); 
 
 	@Override
 	public float scanDistance(WorldModel worldModel, SimulatorState roverState, float heading) {
 		float min = -1;
		for (float beamHeading : beamHeadings) {
			float distance = super.scanDistance(worldModel, roverState, heading + beamHeading);
 			if(min < 0 || (distance > 0 && distance < min)) {
 				min = distance;
 			}
 		}
 		return min;
 	}
 
 }
