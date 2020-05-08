 /**
  * See http://www.presage2.info/ for more details on Presage2
  */
 package uk.ac.imperial.dws04;
 
 import java.util.UUID;
 
 import com.google.inject.Inject;
 
 import uk.ac.imperial.presage2.core.environment.EnvironmentService;
 import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
 import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
 import uk.ac.imperial.presage2.core.environment.ServiceDependencies;
 import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
 
 /**
  * An {@link EnvironmentService} to provide information on the speeds of
  * agents.
  * 
  * <h3>Usage</h3>
  * 
  * <p>
  * Add as a global environment service in the environment
  * <p>
  * 
  * @author dws04
  * 
  */
 @ServiceDependencies({ RoadEnvironmentService.class })
 public class SpeedService extends EnvironmentService {
 	
 	EnvironmentServiceProvider serviceProvider;
 	private RoadEnvironmentService roadEnvironmentService;
 
 	@Inject
	protected SpeedService(EnvironmentSharedStateAccess sharedState, EnvironmentServiceProvider serviceProvider) {
 		super(sharedState);
 		this.serviceProvider = serviceProvider;
 	}
 	
 	protected RoadEnvironmentService getRoadEnvironmentService(){
 		if (roadEnvironmentService == null) {
 			try {
 				roadEnvironmentService = serviceProvider.getEnvironmentService(RoadEnvironmentService.class);
 			} catch (UnavailableServiceException e) {
 				throw new RuntimeException(e);
 			}
 		}
 		return roadEnvironmentService;
 	}
 	
 	public int getMaxSpeed() {
 		return (Integer) this.getRoadEnvironmentService().getMaxSpeed();
 	}
 
 	/**
 	 * Get the speed of a given agent specified by it's participant UUID.
 	 * 
 	 * @param participantID
 	 *            {@link UUID} of participant to look up
 	 * @return {@link RoadSpeed} of participants
 	 */
 	public int getAgentSpeed(UUID participantID) {
 		return (Integer) this.sharedState.get("util.speed", participantID);
 	}
 
 	/**
 	 * Update this agent's speed to s.
 	 * 
 	 * 
 	 * @param participantID
 	 * @param s
 	 */
 	public void setAgentSpeed(final UUID participantID, final int s) {
 		this.sharedState.change("util.speed", participantID, s);
 	}
 }
