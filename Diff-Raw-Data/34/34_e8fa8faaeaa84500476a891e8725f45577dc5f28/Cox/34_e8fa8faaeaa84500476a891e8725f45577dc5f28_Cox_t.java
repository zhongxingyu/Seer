 package siver.cox;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 
 import repast.simphony.engine.schedule.ScheduledMethod;
 import siver.river.lane.Lane;
 import siver.river.lane.LaneEdge;
 import siver.river.lane.LaneNode;
 import siver.boat.Boat;
 import siver.boat.BoatNavigation;
 import siver.context.SiverContextCreator;
 import siver.cox.actions.*;
 import siver.cox.brains.BasicBrain;
 import siver.cox.brains.CoxBrain;
 
 public class Cox {
 	//The boat the cox is controlling.
 	protected Boat boat;
 	
 	//how fast the boat would like to be travelling
 	private int desired_gear;
 	private double distance_to_cover;
 	
 	private boolean incapcitated;
 	
 	protected Action action;
 	protected BoatNavigation navigator;
 	
 	protected CoxObservations observations;
 	
 	private CoxBrain brain;
 	
 	public Cox() {
 		
 	}
 	
 	public void launch(Boat boat, Lane launchLane, int desGear, double speedMult, double distance_to_cover, Integer launch_schedule_id) throws SecurityException, IllegalArgumentException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
 		launch(BasicBrain.class, boat, launchLane, desGear, speedMult, distance_to_cover, launch_schedule_id);
 	}
 	
 	public void launch(Class brainType, Boat boat, Lane launchLane, int desGear, double speedMult, double distance_to_cover, Integer launch_schedule_id) throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
 		//save reference to boat launched in
 		this.boat = boat;
 		//place the boat at the location of the first node of the lane
 		LaneNode launchNode = launchLane.getStartNode();
 		
 		//and point the boat in the correct direction
 		LaneEdge launchEdge = launchLane.getNextEdge(launchNode, false);
 		boat.launch(this, launchNode.getLocation());
 		
 		navigator = new BoatNavigation(this, boat, launchEdge, false);
 		observations = new CoxObservations(this, boat, navigator);
 		
 		Constructor<CoxBrain> cons = brainType.getConstructor(CoxObservations.class);
 		brain = cons.newInstance(observations);
 		
 		incapcitated = false;
 		
 		this.desired_gear = desGear;
 		this.distance_to_cover = distance_to_cover;

 		
 		boat.launchComplete(launch_schedule_id);
 	}
 	
 	public void land() {
 		getNavigator().getEdge().removeCox(this);
 		boat.land();
 		SiverContextCreator.getContext().remove(this);
 	}
 	
 	//BEHAVIOUR
 	@ScheduledMethod(start = 1, interval = 1, priority=100)
 	public void step() throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
 		if(!incapcitated) {
 			if(outingOver()) {
 				new Land(this).execute();
 				return;
 			}
 			
 			if(action == null) {
 				Constructor<Action> cons = brain.chooseAction().getConstructor(Cox.class);
 				action = cons.newInstance(this);
 			}
 			action.execute();
 		}
 	}
 	
 	
 	
 	/*
 	 * PREDICATES
 	 */	
 	
 	protected boolean outingOver() {
		return (boat.total_distance_covered() >= distance_to_cover) && (navigator.getDestinationNode().equals(navigator.getLane().getStartNode()));
 	}
 	
 
 	
 	/*
 	 * HELPERS
 	 */
 	public Action getAction() {
 		return action;
 	}
 	
 	public void clearAction() {
 		action = null;
 	}
 	
 	public Boat getBoat() {
 		return boat;
 	}
 	
 	public BoatNavigation getNavigator() {
 		return navigator;
 	}
 	
 	public void incapcitate() {
 		boat.deadStop();
 		incapcitated = true;
 	}
 	
 	public void recapcitate() {
 		incapcitated = false;
 	}
 	
 	public boolean isIncapcitated() {
 		return incapcitated;
 	}
 	
 	public int desired_gear() {
 		return desired_gear;
 	}
 	
 	public String brain_type() {
 		return this.brain.getClass().getName();
 	}
 }
