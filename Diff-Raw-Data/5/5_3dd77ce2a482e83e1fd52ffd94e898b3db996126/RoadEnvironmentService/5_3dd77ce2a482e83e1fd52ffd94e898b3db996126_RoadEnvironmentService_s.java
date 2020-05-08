 /**
  * See http://www.presage2.info/ for more details on Presage2
  */
 package uk.ac.imperial.dws04.Presage2Experiments;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.UUID;
 
 import org.apache.log4j.Logger;
 
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import com.google.inject.name.Named;
 
 import uk.ac.imperial.dws04.utils.MathsUtils.MathsUtils;
 import uk.ac.imperial.presage2.core.environment.EnvironmentService;
 import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
 import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
 import uk.ac.imperial.presage2.core.environment.ServiceDependencies;
 import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
 
 /**
  * Class to hold global shared state variables for the RoadSimulation, 
  * and to handle junction-related tasks
  * 
  * @author dws04
  *
  */
 @Singleton
 @ServiceDependencies( { RoadLocationService.class, SpeedService.class } )
 public class RoadEnvironmentService extends EnvironmentService {
 
 	private final Logger logger = Logger.getLogger(RoadEnvironmentService.class);
 	EnvironmentServiceProvider serviceProvider;
 	private final ArrayList<Integer> junctionLocations;
 	
 	@Inject
 	protected RoadEnvironmentService(EnvironmentSharedStateAccess sharedState,
 			EnvironmentServiceProvider serviceProvider, @Named("params.length") int length,
 			@Named("params.junctionCount") int junctionCount, @Named("params.lanes") int lanes,
 			@Named("params.maxSpeed") int maxSpeed, @Named("params.maxAccel") int maxAccel, @Named("params.maxDecel") int maxDecel){
 		super(sharedState);
 		junctionLocations = createJunctionList(length, junctionCount);
 		sharedState.createGlobal("junctionLocations", junctionLocations);
 		sharedState.createGlobal("maxSpeed", maxSpeed);
 		sharedState.createGlobal("maxAccel", maxAccel);
 		sharedState.createGlobal("maxDecel", maxDecel);
 		sharedState.createGlobal("length", length);
 		sharedState.createGlobal("lanes", lanes);
 		this.serviceProvider = serviceProvider;
 	}
 	
 	RoadLocationService getLocationService() {
 		try {
 			return this.serviceProvider.getEnvironmentService(RoadLocationService.class);
 		} catch (UnavailableServiceException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	SpeedService getSpeedService() {
 		try {
 			return this.serviceProvider.getEnvironmentService(SpeedService.class);
 		} catch (UnavailableServiceException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Creates list of locations at which to place evenly-spaced junctions. (length*(0->jcCount/jCount+1) 
 	 * @param length
 	 * @param junctionCount
 	 * @return
 	 */
 	private final ArrayList<Integer> createJunctionList(int length, int junctionCount) {
 		ArrayList<Integer> result = new ArrayList<Integer>();
 		if (junctionCount!=0){
 			// length*(i/(junctionCount+1) -> (length/(junctionCount+1))*i to avoid Integer rounding
 			for (int i = 0; i<junctionCount; i++) {
 				//Integer temp = length*(i/(junctionCount+1));
 				result.add((i*length)/junctionCount);
 				//result.add((length/(junctionCount+1))*i);
 				logger.trace("Added a junction at " + (i*length)/junctionCount);
 			}
 		}
 		return result;
 	}
 
 	public int getMaxSpeed() {
 		return (Integer) this.sharedState.getGlobal("maxSpeed");
 	}
 
 	public int getLength() {
 		return (Integer) this.sharedState.getGlobal("length");
 	}
 
 	public int getMaxAccel() {
 		return (Integer) this.sharedState.getGlobal("maxAccel");
 	}
 
 	public int getMaxDecel() {
 		return (Integer) this.sharedState.getGlobal("maxDecel");
 	}
 	
 	public int getLanes() {
 		return (Integer) this.sharedState.getGlobal("lanes");
 	}
 
 	/**
 	 * @return the junctionLocations
 	 */
 	public ArrayList<Integer> getJunctionLocations() {
 		return junctionLocations;
 	}
 	
	// TODO make this wrap ?
 	public boolean isJunctionOffset(int offset){
		return getJunctionLocations().contains(offset);
 	}
 	
 	public Integer getNextInsertionJunction(){
 		ArrayList<Integer> junctions = getJunctionLocations();
 		Collections.shuffle(junctions);
 		Iterator<Integer> it = junctions.iterator();
 		boolean notAdded = true;
 		Integer result = null;
 		// go through the junctions in a random order
 		while (it.hasNext() && notAdded) {
 			int startOffset = it.next();
 			int offset;
 			UUID targetUUID = null;
 			logger.debug("Starting check for agents backwards from " + startOffset + " to a distance of " + this.getLocationService().getPerceptionRange());
 			for (int i = 0; i<this.getLocationService().getPerceptionRange(); i++) {
 				// look back from the insertion point to see if you find an agent
 				logger.trace("offset is " + startOffset + " - " + i + " % " + getLength() + " = " + (startOffset-i) + " % " + getLength() + " = " + ((startOffset-i)%getLength()) + " / corrected:" + ((((startOffset-i)%getLength())+getLength())%getLength()));
 				// to do negative modulo... ffs
 				offset = MathsUtils.mod((startOffset-i),getLength());
 				targetUUID = this.getLocationService().getLocationContents(0, offset);
 				logger.debug("Checked location [0,"+offset+"] and found " + targetUUID);
 				if (targetUUID!=null) {
 					// found someone
 					break;
 				}
 			}
 			if (targetUUID==null) {
 				logger.debug("No agents detected within max stopping distance");
 				result = startOffset;
 				notAdded = false;
 			}
 			else if (this.getLocationService().getAgentLocation(targetUUID).getOffset() + this.getSpeedService().getStoppingDistance(targetUUID) < startOffset) {
 				logger.debug("Agent " + targetUUID + " was detected at location " + this.getLocationService().getAgentLocation(targetUUID) + " but should stop in time.");
 				result = startOffset;
 				notAdded = false;
 			}
 			else {
 				logger.debug("Agent " + targetUUID + " was detected at location " + this.getLocationService().getAgentLocation(targetUUID) + " but is unable to stop in time.");
 			}
 		}
 		return result;
 	}
 	
 }
