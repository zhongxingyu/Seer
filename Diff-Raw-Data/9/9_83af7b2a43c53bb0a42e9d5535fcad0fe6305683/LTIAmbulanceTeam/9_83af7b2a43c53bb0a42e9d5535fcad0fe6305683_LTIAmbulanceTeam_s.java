 package lti.agent.ambulance;
 
 /*
  * TODO
  * - desenvolver método para encontrar rotas alternativas em
  *   caso de bloqueios.
  */
 
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.EnumSet;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 import java.util.TreeSet;
 
 import lti.agent.AbstractLTIAgent;
 import lti.message.Message;
 import lti.utils.EntityIDComparator;
 import rescuecore2.messages.Command;
 import rescuecore2.misc.Pair;
 import rescuecore2.standard.entities.AmbulanceTeam;
 import rescuecore2.standard.entities.Building;
 import rescuecore2.standard.entities.Civilian;
 import rescuecore2.standard.entities.Human;
 import rescuecore2.standard.entities.Refuge;
 import rescuecore2.standard.entities.Road;
 import rescuecore2.standard.entities.StandardEntity;
 import rescuecore2.standard.entities.StandardEntityURN;
 import rescuecore2.worldmodel.ChangeSet;
 import rescuecore2.worldmodel.EntityID;
 import area.Sector;
 import area.Sectorization;
 
 public class LTIAmbulanceTeam extends AbstractLTIAgent<AmbulanceTeam> {
 
 	private Set<EntityID> buildingsToCheck;
 
 	private List<EntityID> refuges;
 
 	private static enum State {
 		LOADING_CIVILIAN, PATROLLING, TAKING_ALTERNATE_ROUTE, MOVING_TO_UNBLOCK,
 		MOVING_TO_TARGET, MOVING_TO_REFUGE, RANDOM_WALKING, RETURNING_TO_SECTOR,
 		RESCUEING, DEAD, BURIED
 	};
 
 	private State state;
 
 	private Set<EntityID> safeBuildings;
 	
 	private List<EntityID> ambulanceTeamsList;
 	
 	private Sectorization sectorization;
 	
 	private Sector sector;
 	
 	private List<EntityID> path;
 
 	@Override
 	protected void postConnect() {
 		super.postConnect();
 		
 		inicializaVariaveis();
 
 		changeState(State.RANDOM_WALKING);
 	}
 
 	private void inicializaVariaveis() {
 		currentX = me().getX();
 		currentY = me().getY();
 
 		Set<EntityID> ambulanceTeams = new TreeSet<EntityID>(
 				new EntityIDComparator());
 
 		for (StandardEntity e : model
 				.getEntitiesOfType(StandardEntityURN.AMBULANCE_TEAM)) {
 			ambulanceTeams.add(e.getID());
 		}
 
 		ambulanceTeamsList = new ArrayList<EntityID>(ambulanceTeams);
 		
 		internalID = ambulanceTeamsList.indexOf(me().getID()) + 1;
 		
 		refuges = new ArrayList<EntityID>();
 		Collection<Refuge> ref = getRefuges();
 
 		for (Refuge next : ref) {
 			refuges.add(next.getID());
 		}
 		
 		safeBuildings = new HashSet<EntityID>();
 			
 		sectorize();
 		
 		buildingsToCheck = new HashSet<EntityID>();
 		for(EntityID buildingID : buildingIDs)
 			if(sector.getLocations().keySet().contains(buildingID))
 				buildingsToCheck.add(buildingID);
 		
 		buildingsToCheck.removeAll(refuges);
 	}
 
 	@Override
 	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
 		return EnumSet.of(StandardEntityURN.AMBULANCE_TEAM);
 	}
 
 	@Override
 	protected void think(int time, ChangeSet changed, Collection<Command> heard) {
 		super.think(time, changed, heard);
 		recalculaVariaveisCiclo();
 
 		if (me().getHP() == 0) {
 			changeState(State.DEAD);
 			return;
 		}
 
 		if (me().getBuriedness() != 0) {
 			sendMessageAboutPerceptions(changed);
 			changeState(State.BURIED);
 			return;
 		}
 		
 		// Will check the building for victims
 		if (buildingIDs.contains(currentPosition) && emptyBuilding(changed)) {
 			buildingsToCheck.remove(currentPosition);
 			log("Checked one more building, now empty: " + currentPosition);
 		}
 		
 		// If I'm blocked it's probably because there's an obstructing blockade
 		if (amIBlocked(time)) {
 			if (movingToUnblock())
 				return;
 		}
 		
 		sendMessageAboutPerceptions(changed);
 		
 		evaluateTaskDroppingAndSelection(changed);
 		
 		// Work on the task, if you have one
 		if (target != null) {
 			// Am I carrying a civilian?
 			if (state.equals(State.LOADING_CIVILIAN) || state.equals(State.MOVING_TO_REFUGE)) {
 				// Am I at a refuge?
 				if (refuges.contains(currentPosition)) {
 					sendUnload(time);
 					log("I'm at a refuge, so unloading");
 					return;
 				}
 				// No? I need to get to one, then.
 				path = search.breadthFirstSearch(currentPosition, refuges);
 				if (path == null)
 					path = randomWalk();
 				changeState(State.MOVING_TO_REFUGE);
 
 				if (path != null && path.size() > 0) {
 					sendMove(time, path);
 					log("Path calculated and sent move: " + path);
 					return;
 				}
 			} else {
 				Human victim = (Human) model.getEntity(target);
 
 				if (victim.getPosition().equals(currentPosition)) {
 					if (victim.isBuriednessDefined() &&
 							victim.getBuriedness() != 0) {
 						sendRescue(time, target);
 						changeState(State.RESCUEING);
 						log("Rescueing " + victim + " buriedness: " + victim.getBuriedness());
 					} else if (victim instanceof Civilian) {
 						sendLoad(time, target);
 						changeState(State.LOADING_CIVILIAN);
 						log("Loading civilian " + victim);
 					}
 					return;
 				} else {
 					path = search.breadthFirstSearch(currentPosition, victim.getPosition());
 					if (path != null && path.size() > 0) {
 						changeState(State.MOVING_TO_TARGET);
 						sendMove(time, path);
 						log("Path calculated and sent move: " + path);
 						return;
 					}
 				}
 			}
 		}
 
 		// Move around the map
 		// Nothing to do here. Moving on.
 		safeBuildings = getSafeBuildings(changed);
 
 		getMoreSafeBuildings();
 		
 		// Using an aux list of buildings to check
 		Set<EntityID> auxBuildingsToCheck = new HashSet<EntityID>(buildingsToCheck);
 		
 		// We remove all the buildings that aren't safe to enter
 		auxBuildingsToCheck.retainAll(safeBuildings);
 		
 		// We then try to go to the closest building not yet checked
 		path = search.breadthFirstSearch(me().getPosition(), auxBuildingsToCheck);
 		
 		// If we find a path, we set it as the next location
 		if(path != null && path.size() > 0){
 			sendMove(time, path);
 			changeState(State.PATROLLING);
 			return;
 		}
 
 		path = randomWalk();
 		if(path != null && path.size() > 0) {
 			sendMove(time, path);
 			log("Path calculated and sent move: " + path);
 		}
 	}
 
 	private boolean movingToUnblock() {
 		if (target != null && state.equals(State.MOVING_TO_TARGET)) {
 			// Find another task
 			taskDropped = target;
 			target = null;
 			log("Dropped task: " + taskDropped);
 			return false;
 		}
 		
 		if (path != null && path.size() > 0 &&
 				model.getEntity(path.get(0)) instanceof Road) {
 			Rectangle2D rect = ((Road) model.getEntity(path.get(0)))
 					.getShape().getBounds2D();
 			Random rdn = new Random();
 			int x = (int) (rect.getMinX() + rdn.nextDouble()
 					* (rect.getMaxX() - rect.getMinX()));
 			int y = (int) (rect.getMinY() + rdn.nextDouble()
 					* (rect.getMaxY() - rect.getMinY()));
 	
 			if (rect.contains(x, y) && currentTime % 3 == 0) {
 				EntityID e = path.get(0);
 				path = new ArrayList<EntityID>();
 				path.add(e);
 				sendMove(currentTime, path, x, y);
 				changeState(State.MOVING_TO_UNBLOCK);
 				log("Found path: " + path + " and sent move to dest: " + x
 						+ "," + y);
 				return true;
 			}
 		}
 		path = randomWalk();
 		if (path != null && path.size() > 0) {
 			sendMove(currentTime, path);
 			log("Path calculated to unblock and sent move: " + path);
 			return true;
 		}
 		return false;
 	}
 	
 	private void sendMessageAboutPerceptions(ChangeSet changed) {
 		// Send a message about all the perceptions
 		Message msg = composeMessage(changed);
 
 		if (this.channelComm) {
 			if (!msg.getParameters().isEmpty() && !channelList.isEmpty()) {
 				for (Pair<Integer,Integer> channel : channelList) {
 					sendSpeak(currentTime, channel.first(),
 							msg.getMessage(channel.second().intValue()));
 				}
 			}
 		}
 	}
 
 	private void evaluateTaskDroppingAndSelection(ChangeSet changed) {
 		// Evaluate task dropping
 		if (target != null) {
 			dropTask(currentTime, changed);
 			if (target == null)
 				log("Dropped task: " + taskDropped);
 		}
 
 		/*
 		 * Choose a victim to rescue from the task table, in the following
 		 * priority order: ambulances, fire brigades, civilians and police
 		 * forces
 		 */
 		if (target == null) {
 			target = selectTask();
 			if (target != null)
 				log("Selected task: " + model.getEntity(target));
 		}
 	}
 		
 	protected List<EntityID> randomWalk(){
 		List<EntityID> result = new ArrayList<EntityID>();
 		EntityID current = currentPosition;
 		
 		if (!sector.getLocations().keySet().contains(currentPosition)) {
 			List<EntityID> local = new ArrayList<EntityID>(sector
 					.getLocations().keySet());
 			changeState(State.RETURNING_TO_SECTOR);
 			return search.breadthFirstSearch(currentPosition, local);
 		}
 
 		for (int i = 0; i < RANDOM_WALK_LENGTH; ++i) {
 			result.add(current);
 			List<EntityID> possible = new ArrayList<EntityID>();
 
 			for (EntityID next : sector.getNeighbours(current))
 				if (model.getEntity(next) instanceof Road)
 					possible.add(next);
 
 			Collections.shuffle(possible, new Random(me().getID().getValue()
 					+ currentTime));
 			boolean found = false;
 
 			for (EntityID next : possible) {
 				if (!result.contains(next)) {
 					current = next;
 					found = true;
 					break;
 				}
 			}
 			if (!found)
 				break; // We reached a dead-end.
 		}
 
 		result.remove(0); // Remove actual position from path
 		changeState(State.RANDOM_WALKING);
 		return result;
 	}
 
 	private void recalculaVariaveisCiclo() {
 		currentX = me().getX();
 		currentY = me().getY();
 	}
 
 	/**
 	 * Check if this ambulance is carrying a civilian.
 	 * 
 	 * @return true if the ambulance is carrying a civilian, false otherwise.
 	 */
 	private boolean targetOnBoard() {
 		if (target != null && model.getEntity(target) instanceof Human) {	
 			Human t = (Human) model.getEntity(target);
 			if (t.isPositionDefined())
 				return t.getPosition().equals(getID());
 		}
 
 		log("Position of the human target not defined. Can't say if it's on board");
 		return false;
 	}
 
 	@Override
 	protected boolean amIBlocked(int time) {
 		return lastPosition.equals(currentPosition) &&
 				isMovingState() &&
 				time > 3;
 	}
 
 	/**
 	 * Choose a vicitm from the Task Table to rescue according to the following
 	 * pritority: ambulances, fire brigades, civilians and police forces.
 	 * 
 	 * @return The victim's ID.
 	 */
 	@Override
 	protected EntityID selectTask() {
 		Human victim = pickVictim();
 
 		if (victim != null) {
 			for (Set<EntityID> agents : taskTable.values()) {
 				if (agents != null)
 					agents.remove(me().getID());
 			}
 
 			if (!taskTable.containsKey(victim.getID()))
 				taskTable.put(victim.getID(), new HashSet<EntityID>());
 			taskTable.get(victim.getID()).add(me().getID());
 			return victim.getID();
 		}
 
 		return null;
 	}
 		
 	private Human pickVictim() {
 		int totalDistance = 0;
 		double savePriority = 0, newSavePriority;
 		Human result = null;
 
 		for (EntityID next : taskTable.keySet()) {
 			if (!next.equals(taskDropped)) {
 				Human victim = (Human) model.getEntity(next);
 				int distanceFromAT = model.getDistance(getID(),
 						victim.getPosition());
 				
 				// For now, it ignores the distance to the refuge
 				int distanceToRefuge = 0;
 				
 				totalDistance = distanceToRefuge + distanceFromAT;
 				newSavePriority = getVictimSavePriority(victim, totalDistance);
 				if(newSavePriority > savePriority){
 					result = victim;
 					savePriority = newSavePriority;
 				}
 			}
 		}
 		log("FINAL_SCORE: " + result + " -> " + savePriority);
 
 		return result;
 	}
 	
 	private boolean evaluateSavingConditions(Human victim, int totalDistance){
 		
 		//Se a vitima nao estiver em um refugio
 		if(!(victim.isPositionDefined() && refuges.contains(victim.getPosition()))){
 			//se a vitima ainda nao estiver sendo salva
 			if(!isVictimBeingRescued(victim)){
 				//Se ainda e possivel salvar a vitima
 				if (isSavable(victim, totalDistance)) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 		
 	private Boolean isVictimBeingRescued(Human victim){		
 		return  taskTable.keySet().contains(victim.getID()) &&
 				!taskTable.get(victim.getID()).isEmpty();		
 	}
 
 	private boolean isSavable(Human victim, int totalDistance) {
 		//Calcula quantos ciclos a vitima tem de vida
 		int remainingCycles = 0;
 		if (victim.isDamageDefined() && victim.getDamage() != 0)
 			remainingCycles = victim.getHP() / victim.getDamage();
 		else
 			return true;
 
 		// Calcula quantos ciclos precisa para salvar a vitima
 		// TODO: considerar tempo de rescue e load
 		int necessaryCycles = 0;
 
 		if (maxDistanceTraveledPerCycle != 0)
 			necessaryCycles = totalDistance / maxDistanceTraveledPerCycle;
 
 		if (necessaryCycles > remainingCycles)
 			return false;
 
 		return true;
 	}
 
 	private double getVictimSavePriority(Human victim, int totalDistance) {
 		
 		double savability = 5 - (totalDistance - 10000) / 20000;
 		int b = 0;
 		if(victim.isBuriednessDefined())
 			b = victim.getBuriedness();
 		boolean isPossibleToSave = evaluateSavingConditions(victim, totalDistance);
 		boolean isSamePosition = false;
 		if(victim.isPositionDefined())
 			isSamePosition = victim.getPosition().equals(currentPosition);
 		
 		savability += (200 - b) / 40.0;
 		savability += isPossibleToSave ? 2 : 0;
 		savability += isSamePosition ? 5 : 0;
 		
 		log("SCORE: " + victim + " -> " + savability + " (d:" + totalDistance + ", b:" + b + ", ip:" + isPossibleToSave);
 		return savability;
 	}
 
 	@Override
 	protected void refreshTaskTable(ChangeSet changed) {
 		Set<EntityID> victims = new HashSet<EntityID>();
 		List<StandardEntityURN> urns = new ArrayList<StandardEntityURN>();
 		
 		urns.add(StandardEntityURN.AMBULANCE_TEAM);
 		urns.add(StandardEntityURN.FIRE_BRIGADE);
 		urns.add(StandardEntityURN.CIVILIAN);
 		urns.add(StandardEntityURN.POLICE_FORCE);
 		
 		/*
 		 * Victims: - AT, FB and PF who are alive and buried; - Civilians who
 		 * are alive and are either buried or inside a building that is not a
 		 * refuge.
 		 */
 		for (StandardEntityURN entUrn : urns) {
 			Set<EntityID> nonRefugeBuildings = new HashSet<EntityID>(
 					buildingIDs);
 			nonRefugeBuildings.removeAll(refuges);
 
 			for (StandardEntity next : model.getEntitiesOfType(entUrn)) {
 				Human h = (Human) next;
 				if (h.isHPDefined() && h.getHP() != 0) {
 					if (entUrn.equals(StandardEntityURN.CIVILIAN)
 							&& nonRefugeBuildings.contains(h.getPosition())) {
 						victims.add(h.getID());
 					} else if (h.isBuriednessDefined() &&
 							h.getBuriedness() != 0 &&
 							!h.getID().equals(getID())) {
 						victims.add(next.getID());
 					}
 				}
 			}
 		}
 
 		Set<EntityID> toRemove = new HashSet<EntityID>();
 
 		/*
 		 * Remove victims who are not found in the position they were thought to
 		 * be; they have already been rescued.
 		 */
 		for (EntityID next : victims) {
 			Human exVictim = (Human) model.getEntity(next);
 			if (exVictim.getPosition().equals(currentPosition) &&
 					!changed.getChangedEntities().contains(next)) {
 				exVictim.undefineBuriedness();
 				exVictim.undefinePosition();
 				toRemove.add(next);
 			}
 		}
 
 		victims.removeAll(toRemove);
 		
 		//Include as victims the target loaded
 		if (targetOnBoard())
 			victims.add(target);
 		
 		taskTable.keySet().retainAll(victims);
 		victims.removeAll(taskTable.keySet());
 
 		for (EntityID next : victims) {
 			taskTable.put(next, new HashSet<EntityID>());
 		}
 	}
 
 	/**
 	 * Check the building to see if there are victims inside it.
 	 * 
 	 * @param changed
 	 *            The ChangeSet of the ambulance.
 	 * @return false, if there is still a victim inside the building, otherwise
 	 *         returns true.
 	 */
 	private boolean emptyBuilding(ChangeSet changed) {
 		Set<EntityID> visible = changed.getChangedEntities();
 		visible.retainAll(taskTable.keySet());
 
 		for (EntityID next : visible) {
 			Human human = (Human) model.getEntity(next);
 			if (human.getPosition().equals(currentPosition)) {
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	private void getMoreSafeBuildings() {
 		Set<EntityID> safe = new HashSet<EntityID>();
 
 		for (StandardEntity next : model
 				.getEntitiesOfType(StandardEntityURN.BUILDING)) {
 			if (!((Building) next).isOnFire()) {
 				if (!((Building) next).isBrokennessDefined())
 					safe.add(next.getID());
 			}
 		}
 
 		safe.removeAll(refuges);
 		safeBuildings.retainAll(safe);
 		safe.removeAll(safeBuildings);
 		safeBuildings.addAll(safe);
 	}
 
 	@Override
 	protected void dropTask(int time, ChangeSet changed) {
 		if (!targetOnBoard() && !taskTable.keySet().contains(target)) {
 			taskDropped = target;
 			target = null;
 		}		
 	}
 	
 	private boolean isMovingState() {
 		List<State> ss = new ArrayList<State>();
 		ss.add(State.PATROLLING);
 		ss.add(State.TAKING_ALTERNATE_ROUTE);
 		ss.add(State.MOVING_TO_TARGET);
 		ss.add(State.MOVING_TO_REFUGE);
 		ss.add(State.RANDOM_WALKING);
 		ss.add(State.RETURNING_TO_SECTOR);
 		ss.add(State.MOVING_TO_UNBLOCK);
 
 		return ss.contains(state);
 	}
 	
 	private void changeState(State state) {
 		this.state = state;
 		log("Changed state to: " + this.state);
 	}
 	
 	private void sectorize(){
 		sectorization = new Sectorization(model, neighbours,
 				ambulanceTeamsList.size(), verbose);
 		
 		sector = sectorization.getSector(internalID);
 		
 		log("Defined sector: " + sector);
 	}
 	
 }
