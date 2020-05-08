 package simulation.builder;
 
 import org.jdom.Element;
 
import common.Vector;

 import environment.ILane;
 import environment.SignWayPoint;
 import environment.SpeedWayPoint;
 
 /**
  * Builder for speed way points
  * 
  * Currently not in use due to missing time
  */
 public class XMLSpeedWayPointBuilder extends XMLWayPointBuilder {
 	/**
 	 * The speed limit of the way point
 	 */
 	int speed;
 
 	/**
 	 * Initialize
 	 * 
 	 * @param e
 	 * @param wBuilder
 	 * @throws InvalidXMLException 
 	 */
 	public XMLSpeedWayPointBuilder(Element e, IXMLWorldBuilder wBuilder) throws InvalidXMLException {
 		super(e, wBuilder);
 	}
 
 	/**
 	 * Create the way point
 	 */
 	@Override
 	public SignWayPoint createWayPoint(ILane lane) {
		return new SpeedWayPoint(lane, this.speed, new Vector(new float[] {1.0f, 1.0f})); // TODO the position?
 	}
 }
