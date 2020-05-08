 package uk.ac.imperial.dws04;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 import org.apache.log4j.Logger;
 
 import uk.ac.imperial.presage2.core.Action;
 import uk.ac.imperial.presage2.core.db.persistent.PersistentEnvironment;
 import uk.ac.imperial.presage2.core.db.persistent.PersistentSimulation;
 import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
 import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
 import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
 import uk.ac.imperial.presage2.core.environment.ServiceDependencies;
 import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
 import uk.ac.imperial.presage2.core.event.EventBus;
 import uk.ac.imperial.presage2.core.event.EventListener;
 import uk.ac.imperial.presage2.core.messaging.Input;
 import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;
 import uk.ac.imperial.presage2.core.simulator.FinalizeEvent;
 import uk.ac.imperial.presage2.util.location.CellMove;
 import uk.ac.imperial.presage2.util.location.LocationService;
 import uk.ac.imperial.presage2.util.location.Move;
 import uk.ac.imperial.presage2.util.location.MoveHandler;
 import uk.ac.imperial.presage2.util.location.area.AreaService;
 import uk.ac.imperial.presage2.util.location.area.EdgeException;
 import uk.ac.imperial.presage2.util.location.area.HasArea;
 
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 
 /**
  * Special move handler to deal with moves in an area representing a multi-lane
  * road. Adds collision detection on top of moves in a Cell area.s
  * 
  * @author Sam Macbeth
  * 
  */
 @ServiceDependencies({ AreaService.class, LocationService.class, SpeedService.class })
 @Singleton
 public class LaneMoveHandler extends MoveHandler {
 
 	private final Logger logger = Logger.getLogger(LaneMoveHandler.class);
	private List<CollisionCheck> checks = new LinkedList<LaneMoveHandler.CollisionCheck>();
 	private int collisions = 0;
 	private PersistentEnvironment persist = null;
 	private RoadEnvironmentService roadEnvironmentService;
 	private SpeedService speedService;
 
 	@Inject
 	public LaneMoveHandler(HasArea environment,
 			EnvironmentServiceProvider serviceProvider,
 			EnvironmentSharedStateAccess sharedState, EventBus eb)
 			throws UnavailableServiceException {
 		super(environment, serviceProvider, sharedState);
 		eb.subscribe(this);
 		this.roadEnvironmentService = serviceProvider.getEnvironmentService(RoadEnvironmentService.class);
 		this.speedService = serviceProvider.getEnvironmentService(SpeedService.class);
 	}
 
 	@Inject(optional = true)
 	void setPersistence(PersistentSimulation sim) {
 		persist = sim.getEnvironment();
 	}
 
 	@EventListener
 	public int checkForCollisions(EndOfTimeCycle e) {
 		int collisionsThisCycle = 0;
 		for (CollisionCheck c : this.checks) {
 			collisionsThisCycle += c.checkForCollision();
 		}
 		this.checks.clear();
 		this.collisions += collisionsThisCycle;
 		if (this.persist != null)
 			this.persist.setProperty("collisions", e.getTime().intValue(),
 					Integer.toString(collisionsThisCycle));
 		return collisionsThisCycle;
 	}
 
 	@EventListener
 	public void onSimulationComplete(FinalizeEvent e) {
 		if (this.persist != null)
 			this.persist.setProperty("totalcollisions",
 					Integer.toString(this.collisions));
 	}
 
 	@Override
 	public boolean canHandle(Action action) {
 		return action instanceof CellMove;
 	}
 
 	@Override
 	public Input handle(Action action, UUID actor)
 			throws ActionHandlingException {
 		CellMove m = (CellMove) action;
 		int prevSpeed = speedService.getAgentSpeed(actor);
 		int maxSpeed = roadEnvironmentService.getMaxSpeed();
 		int maxAccel = roadEnvironmentService.getMaxAccel();
 		int maxDecel = roadEnvironmentService.getMaxDecel();
 		
 
 		// TODO - requires environment services for agents' max speed etc.
 		// check move forward change in magnitude <= actor's max
 		// acceleration/deceleration
 		// check agent only moving in/out of lane0 when off/on ramp is present 
 		
 		// check move direction is positive or 0
 		if (m.getY() < 0) {
 			throw new ActionHandlingException(
 					"Cannot move backwards. Move was: "
 							+ m);
 		}
 		
 		// check move is not too fast
 		if (m.getY() > maxSpeed) {
 			throw new ActionHandlingException(
 					"Cannot move faster than the maximum speed (" + maxSpeed + "). Move was: "
 							+ m);
 		}
 		
 		// check acceleration is not too fast
 		if (m.getY() > prevSpeed) {
 			if ((m.getY() - prevSpeed) > maxAccel) {
 				throw new ActionHandlingException(
 						"Cannot accelerate faster than the maximum acceleration (" + maxAccel + "). Move was: "
 							+ m + " and previous speed was " + prevSpeed);
 			}
 		}
 		
 		// check deceleration is not too fast
 		if (m.getY() < prevSpeed) {
 			if ((prevSpeed-m.getY()) > maxDecel) {
 				throw new ActionHandlingException(
 						"Cannot decelerate faster than the maximum deceleration (" + maxDecel + "). Move was: "
 							+ m + " and previous speed was " + prevSpeed);
 			}
 		}
 
 		// check move sideways magnitude is 0 or 1
 		if (Math.abs(m.getX()) > 1) {
 			throw new ActionHandlingException(
 					"Cannot change greater than one lane at once. Move was: "
 							+ m);
 		}
 		// cannot change lane without forward movement
 		if (Math.abs(m.getX()) > 0 && (int) m.getY() == 0) {
 			throw new ActionHandlingException(
 					"Cannot change lane while stationary");
 		}
 
 		RoadLocation start = (RoadLocation) locationService
 				.getAgentLocation(actor);
 		RoadLocation target = new RoadLocation(start.add(m));
 
 		if (!target.in(environment.getArea())) {
 			try {
 				final Move mNew = environment.getArea().getValidMove(start, m);
 				target = new RoadLocation(start.add(mNew));
 			} catch (EdgeException e) {
 				throw new ActionHandlingException(e);
 			}
 		}
 		this.locationService.setAgentLocation(actor, target);
 		this.speedService.setAgentSpeed(actor, (int) m.getY());
 		checks.add(new CollisionCheck(actor, start, target));
 
 		logger.info(actor + " move: " + m);
 		return null;
 	}
 
 	class CollisionCheck {
 		final RoadLocation startFrom;
 		final RoadLocation finishAt;
 		Set<UUID> collisionCandidates = new HashSet<UUID>();
 		Map<UUID, RoadLocation> candidateLocs = new HashMap<UUID, RoadLocation>();
 		final boolean laneChange;
 		final int areaLength = areaService.getSizeY();
 		final UUID self;
 
 		public CollisionCheck(UUID pov, RoadLocation startFrom,
 				RoadLocation finishAt) {
 			super();
 			this.self = pov;
 			this.startFrom = startFrom;
 			this.finishAt = finishAt;
 
 			// build collision candidate set
 			int startOffset = startFrom.getOffset();
 			int finishOffset = finishAt.getOffset();
 			if (finishOffset < startOffset)
 				finishOffset += areaLength;
 			for (int lane = 0; lane < areaService.getSizeX(); lane++) {
 				collisionCandidates.addAll(getAgentsInLane(lane, startOffset,
 						finishOffset));
 			}
 			collisionCandidates.remove(this.self);
 			laneChange = startFrom.getLane() != finishAt.getLane();
 			for (UUID a : collisionCandidates) {
 				candidateLocs.put(a,
 						(RoadLocation) locationService.getAgentLocation(a));
 			}
 		}
 
 		private Set<UUID> getAgentsInLane(int lane, int from, int to) {
 			Set<UUID> agents = new HashSet<UUID>();
 			for (int y = from; y <= to; y++) {
 				agents.addAll(areaService.getCell(lane, y % areaLength, 0));
 			}
 			return agents;
 		}
 
 		public int checkForCollision() {
 			int collisionsOccured = 0;
 			Set<UUID> agentsOnCurrentCell = areaService.getCell(
 					finishAt.getLane(), finishAt.getOffset(), 0);
 			if (agentsOnCurrentCell.size() > 1) {
 				logger.warn("Collision Occurred: Multiple agents on one cell. Cell: "
 						+ this.finishAt + ", agents: " + agentsOnCurrentCell);
 				collisionsOccured++;
 			}
 			for (UUID a : collisionCandidates) {
 				RoadLocation current = (RoadLocation) locationService
 						.getAgentLocation(a);
 				if (current.getLane() == finishAt.getLane()) {
 					// same lane, if he is behind us then it is a collision
 					int hisOffset = current.getOffset();
 					if (hisOffset < candidateLocs.get(a).getOffset())
 						hisOffset += areaLength;
 					int myOffset = finishAt.getOffset();
 					if (myOffset < startFrom.getOffset())
 						myOffset += areaLength;
 					if (hisOffset < myOffset) {
 						logger.warn("Collision Occured: Agent "
 								+ agentsOnCurrentCell + " went through " + a);
 						collisionsOccured++;
 					}
 				}
 				if (laneChange && current.getLane() == startFrom.getLane()
 						&& finishAt.getLane() == candidateLocs.get(a).getLane()
 						&& current.getOffset() <= finishAt.getOffset()) {
 					logger.warn("Collision Occured: Agent "
 							+ agentsOnCurrentCell + " crossed paths with " + a);
 					collisionsOccured++;
 				}
 			}
 			return collisionsOccured;
 		}
 
 	}
 
 }
