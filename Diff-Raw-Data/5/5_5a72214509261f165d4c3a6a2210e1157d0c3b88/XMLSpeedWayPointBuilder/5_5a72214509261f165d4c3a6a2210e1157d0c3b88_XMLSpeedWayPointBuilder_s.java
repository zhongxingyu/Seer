 package simulation.builder;
 
 import org.jdom.Element;
 
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
		return new SpeedWayPoint(lane, this.speed, null); // TODO the position?
 	}
 }
