 package lti.agent;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import kernel.KernelConstants;
 
 import lti.message.Message;
 import lti.message.Parameter;
 import lti.message.type.BlockadeCleared;
 import lti.message.type.BuildingBurnt;
 import lti.message.type.BuildingEntranceCleared;
 import lti.message.type.Fire;
 import lti.message.type.FireExtinguished;
 import lti.message.type.Help;
 import lti.message.type.TaskDrop;
 import lti.message.type.TaskPickup;
 import lti.message.type.Victim;
 import lti.message.type.VictimDied;
 import lti.message.type.VictimRescued;
 import lti.utils.PairComparator;
 import lti.utils.Search;
 
 import rescuecore2.Constants;
 import rescuecore2.messages.Command;
 import rescuecore2.misc.Pair;
 import rescuecore2.misc.geometry.GeometryTools2D;
 import rescuecore2.misc.geometry.Line2D;
 import rescuecore2.misc.geometry.Point2D;
 import rescuecore2.standard.components.StandardAgent;
 import rescuecore2.standard.entities.AmbulanceTeam;
 import rescuecore2.standard.entities.Blockade;
 import rescuecore2.standard.entities.Building;
 import rescuecore2.standard.entities.Civilian;
 import rescuecore2.standard.entities.FireBrigade;
 import rescuecore2.standard.entities.Human;
 import rescuecore2.standard.entities.PoliceForce;
 import rescuecore2.standard.entities.Road;
 import rescuecore2.standard.entities.StandardEntity;
 import rescuecore2.standard.entities.StandardEntityConstants.Fieryness;
 import rescuecore2.standard.entities.StandardEntityURN;
 import rescuecore2.standard.kernel.comms.ChannelCommunicationModel;
 import rescuecore2.standard.messages.AKSpeak;
 import rescuecore2.worldmodel.ChangeSet;
 import rescuecore2.worldmodel.EntityID;
 
 public abstract class AbstractLTIAgent<E extends StandardEntity> extends
 		StandardAgent<E> {
 	
 	protected static final int RANDOM_WALK_LENGTH = 5;
 
 	private static final String MAX_SIGHT_KEY = "perception.los.max-distance";
 
 	private static final String SPEAK_COMMUNICATION_MODEL = ChannelCommunicationModel.class
 			.getName();
 
 	private static final String PREFIX_CHANNELS = "comms.channels.";
 	
 	private static final String CHANNEL_COUNT = PREFIX_CHANNELS + "count";
 
 	private static final String MAX_CHANNEL_PLATOON = PREFIX_CHANNELS + "max.platoon";
 
 	private static final String MAX_CHANNEL_CENTRE = PREFIX_CHANNELS + "max.centre";
 
 	protected boolean verbose = false;
 	
 	protected Search search;
 
 	protected Set<EntityID> buildingIDs;
 
 	protected Set<EntityID> roadIDs;
 
 	protected Map<EntityID, Set<EntityID>> neighbours;
 
 	// Maximum sight distance
 	protected int maxSight;
 
 	// Communication channel available
 	protected boolean channelComm;
 
 	// Number of channels
 	protected int numChannels;
 
 	// Maximum channels for Platoon
 	protected int maxChannelPlatoon;
 
 	// Maximum channels for Centre
 	protected int maxChannelCentre;
 
 	protected List<Pair<Integer, Integer>> channelList;
 
 	protected EntityID lastPosition;
 
 	protected EntityID currentPosition;
 	
 	protected int lastX;
 
 	protected int lastY;
 
 	protected int currentX;
 
 	protected int currentY;
 	
 	protected int currentTime;
 	
 	protected int internalID;
 
 	protected EntityID target;
 
 	protected Map<EntityID, Set<EntityID>> taskTable;
 
 	// Incêndios já conhecidos
 	protected Set<EntityID> buildingsOnFire;
 
 	// Bloqueios já conhecidos
 	protected Set<EntityID> knownBlockades;
 
 	// Vítimas já conhecidas
 	protected Set<EntityID> knownVictims;
 
 	protected EntityID taskDropped;
 	
 	//Valor de distancia medio observado nos logs com testes 
 	//TODO: Pegar um valor mais condizente, talvez no ambiente do simulador
 	protected int maxDistanceTraveledPerCycle = 5000;
 
 	protected Set<EntityID> buildingEntrancesCleared;
 
 	@Override
 	protected void postConnect() {
 		super.postConnect();
 		lastX = 0;
 		lastY = 0;
 		currentTime = 0;
 		internalID = 0;
 
 		model.indexClass(StandardEntityURN.BUILDING);
 
 		buildingIDs = new HashSet<EntityID>();
 		roadIDs = new HashSet<EntityID>();
 
 		for (StandardEntity next : model) {
 			if (next instanceof Building) {
 				buildingIDs.add(next.getID());
 			}
 			if (next instanceof Road) {
 				roadIDs.add(next.getID());
 			}
 		}
 
 		search = new Search(model);
 
 		neighbours = search.getGraph();
 
 		boolean speakComm = config.getValue(Constants.COMMUNICATION_MODEL_KEY)
 				.equals(SPEAK_COMMUNICATION_MODEL);
 
 		this.numChannels = this.config.getIntValue(CHANNEL_COUNT);
 		if ((speakComm) && (this.numChannels > 1)) {
 			this.channelComm = true;
 		} else {
 			this.channelComm = false;
 		}
 
 		this.maxChannelPlatoon = this.config.getIntValue(MAX_CHANNEL_PLATOON);
 		this.maxChannelCentre = this.config.getIntValue(MAX_CHANNEL_CENTRE);
 
 		channelList = new ArrayList<Pair<Integer, Integer>>();
 
 		maxSight = config.getIntValue(MAX_SIGHT_KEY);
 
 		lastPosition = null;
 
 		currentPosition = location().getID();
 
 		target = null;
 
 		taskTable = new HashMap<EntityID, Set<EntityID>>();
 
 		taskDropped = null;
 
 		buildingsOnFire = new HashSet<EntityID>();
 
 		knownBlockades = new HashSet<EntityID>();
 
 		knownVictims = new HashSet<EntityID>();
 		
 		buildingEntrancesCleared = new HashSet<EntityID>();
 	}
 
 	@Override
 	protected void think(int time, ChangeSet changed, Collection<Command> heard) {
 		recalculaVariaveisCiclo(time);
 
 		// Subscribe to a communication channel
 		if (time == config.getIntValue(KernelConstants.IGNORE_AGENT_COMMANDS_KEY))
 			if (channelComm && maxChannelPlatoon >= 1)
 				chooseAndSubscribeToRadioChannels();
 
 		refreshWorldModel(changed, heard);
 
 		refreshTaskTable(changed);
 	}
 
 	private void recalculaVariaveisCiclo(int time) {
 		lastPosition = currentPosition;
 		currentPosition = location().getID();
 		lastX = currentX;
 		lastY = currentY;
 		currentTime = time;	
 	}
 
 	/**
 	 * Get the list of all possible means of communication
 	 * and choose one or more to communicate
 	 */
 	private void chooseAndSubscribeToRadioChannels() {
 		channelList = new ArrayList<Pair<Integer, Integer>>();
 		List<Pair<Integer, Integer>> auxList = new ArrayList<Pair<Integer, Integer>>();
 		int nAgents = model.getEntitiesOfType(
 				StandardEntityURN.AMBULANCE_TEAM,
 				StandardEntityURN.POLICE_FORCE,
 				StandardEntityURN.FIRE_BRIGADE).size();
 		
 		for (int i = 0; i < numChannels; i++) {
 			if (config.getValue(PREFIX_CHANNELS + i + ".type").equalsIgnoreCase("radio")) {
 				int bandwidth = config.getIntValue(PREFIX_CHANNELS + i + ".bandwidth");
 				int bandwidthPerAgent = (int) (bandwidth / (nAgents * 0.7));
 				auxList.add(new Pair<Integer, Integer>(i, bandwidthPerAgent));
 			} else {
 				int size = config.getIntValue(PREFIX_CHANNELS + i + ".messages.size");
 				channelList.add(new Pair<Integer, Integer>(i, size));
 			}
 		}
 		
 		PairComparator c = new PairComparator(auxList);
 		Collections.sort(auxList, c);
 		for (int i = 0; i < maxChannelPlatoon; i++) {
 			channelList.add(new Pair<Integer, Integer>(auxList.get(i).first(), auxList.get(i).second()));
 			sendSubscribe(currentTime, auxList.get(i).first());
 		}
 		log("Subscribed to channels " + channelList + " from Radio-options: " + auxList);
 	}
 
 	protected List<EntityID> randomWalk() {
 		List<EntityID> result = new ArrayList<EntityID>(RANDOM_WALK_LENGTH);
 		Set<EntityID> seen = new HashSet<EntityID>();
 		EntityID current = ((Human) me()).getPosition();
 		for (int i = 0; i < RANDOM_WALK_LENGTH; ++i) {
 			result.add(current);
 			seen.add(current);
 			List<EntityID> possible = new ArrayList<EntityID>();
 			boolean found = false;
 
 			for (EntityID next : neighbours.get(current)) {
 				if (!(model.getEntity(next) instanceof Building)) {
 					possible.add(next);
 				}
 			}
 
 			Collections.shuffle(possible, random);
 
 			for (EntityID next : possible) {
 				if (seen.contains(next)) {
 					continue;
 				}
 				current = next;
 				found = true;
 				break;
 			}
 			if (!found) {
 				// We reached a dead-end.
 				break;
 			}
 		}
 		return result;
 	}
 
 	protected Set<EntityID> getSafeBuildings(ChangeSet changed) {
 		Set<EntityID> buildings = getVisibleEntitiesOfType(
 				StandardEntityURN.BUILDING, changed);
 		Set<EntityID> result = new HashSet<EntityID>();
 
 		for (EntityID next : buildings) {
 			StandardEntity e = model.getEntity(next);
 			if (!((Building) e).isOnFire()) {
 				result.add(next);
 			}
 		}
 		return result;
 	}
 
 	protected Set<EntityID> getVisibleEntitiesOfType(StandardEntityURN type,
 			ChangeSet changed) {
 		Set<EntityID> visibleEntities = changed.getChangedEntities();
 		Set<EntityID> result = new HashSet<EntityID>();
 
 		for (EntityID next : visibleEntities) {
 			if (model.getEntity(next) != null &&
 					model.getEntity(next).getStandardURN().equals(type)) {
 				result.add(next);
 			}
 		}
 		return result;
 	}
 
 	/*
 	 * Não é mais utilizada devido à comunicação, pois requer parâmetros
 	 * ("apexes") que não são passados por mensagens
 	 */
 	protected int findDistanceTo(Blockade b, int x, int y) {
 		// Logger.debug("Finding distance to " + b + " from " + x + ", " + y);
 		List<Line2D> lines = GeometryTools2D.pointsToLines(
 				GeometryTools2D.vertexArrayToPoints(b.getApexes()), true);
 		double best = Double.MAX_VALUE;
 		Point2D origin = new Point2D(x, y);
 		for (Line2D next : lines) {
 			Point2D closest = GeometryTools2D.getClosestPointOnSegment(next,
 					origin);
 			double d = GeometryTools2D.getDistance(origin, closest);
 			// Logger.debug("Next line: " + next + ", closest point: " + closest
 			// + ", distance: " + d);
 			if (d < best) {
 				best = d;
 				// Logger.debug("New best distance");
 			}
 		}
 		return (int) best;
 	}
 
 	// TODO verificar se essa função é confiável
 	/* Retorna uma lista com todas as estradas que possuem algum bloqueio */
 	protected List<EntityID> getBlockedRoads() {
 		Collection<StandardEntity> e = model
 				.getEntitiesOfType(StandardEntityURN.ROAD);
 		List<EntityID> result = new ArrayList<EntityID>();
 		for (StandardEntity next : e) {
 			Road road = (Road) next;
 			if (road.isBlockadesDefined() && !road.getBlockades().isEmpty()) {
 				result.add(road.getID());
 			}
 		}
 		return result;
 	}
 
 	/* TODO fazer uma boa implementação dela em cada agente */
 	/** Avalia se é melhor abandonar uma tarefa */
 	protected abstract void dropTask(int time, ChangeSet changed);
 
 	/**
 	 * Refresh the task table and the world model of the agent.
 	 * 
 	 * @param changed
 	 *            The agent's ChangeSet for this timestep.
 	 */
 	protected abstract void refreshTaskTable(ChangeSet changed);
 
 	/**
 	 * Atualiza o modelo de mundo do agente de acordo com o que ele enxerga no
 	 * momento e as mensagens por ele recebidas
 	 * 
 	 * Para cada parâmetro da mensagem, verificar qual é o seu tipo
 	 * e, caso à entidade à qual a mensagem se refere não esteja
 	 * visível, atualizar o modelo de mundo de acordo.
 	 */
 	protected void refreshWorldModel(ChangeSet changed,
 			Collection<Command> heard) {
 		removeNonExistingBlockadesFromModel(changed);
 
 		for (Command cmd : heard) {
 			if (cmd instanceof AKSpeak && cmd.getAgentID() != me().getID()) {
 				readMsg(changed, cmd);
 			}
 		}
 	}
 
 	/**
 	 * @param changed
 	 * @param cmd
 	 */
 	private void readMsg(ChangeSet changed, Command cmd) {
 		Message speakMsg;
 		speakMsg = new Message(((AKSpeak) cmd).getContent());
 		//log("Speak from:" + cmd.getAgentID() + " channel:" +
 		//		((AKSpeak)cmd).getChannel()  + " - " + speakMsg.toString());
 		for (Parameter param : speakMsg.getParameters()) {
 			switch (param.getOperation()) {
 			case FIRE:
 				readMsgFire(param, changed);
 				break;
 			case BLOCKADE:
 				readMsgBlockade(param, changed);
 				break;
 			case VICTIM:
 				readMsgVictim(param, changed);
 				break;
 			case TASK_DROP:
 				readMsgTaskDrop(param, cmd);
 				break;
 			case TASK_PICKUP:
 				readMsgTaskPickup(param, cmd);
 				break;
 			case BLOCKADE_CLEARED:
 				readMsgBlockadeCleared(param);
 				break;
 			case VICTIM_DIED:
 				readMsgVictimDied(param, changed);
 				break;
 			case VICTIM_RESCUED:
 				readMsgVictimRescued(param, changed);
 				break;
 			case FIRE_EXTINGUISHED:
 				readMsgFireExtinguished(param, changed);
 				break;
 			case BUILDING_BURNT:
 				readMsgBuildingBurnt(param, changed);
 				break;
 			case HELP_CIVILIAN:
 				readMsgCivilianHeard(param, changed, cmd);
 				break;
 			case BUILDING_ENTRANCE_CLEARED:
 				readMsgBuildingEntranceCleared(param);
 				break;
 			default:
 				log("Message with non-recognized type: " +
 						param.getOperation());
 			}
 		}
 	}
 
 	private void readMsgBuildingEntranceCleared(Parameter param) {
 		if (!(param instanceof BuildingEntranceCleared))
 			return;
 		
 		BuildingEntranceCleared building = (BuildingEntranceCleared) param;
 		EntityID e = new EntityID(building.getBuildingID());
 		if (!buildingEntrancesCleared.contains(e))
 			buildingEntrancesCleared.add(e);
 	}
 
 	private void readMsgCivilianHeard(Parameter param, ChangeSet changed, Command cmd) {
 		if (!(param instanceof Help))
 			return;
 		
 		Help help = (Help) param;
 		EntityID victimID = cmd.getAgentID();
 
 		// For now, only treat as victims civilians who are buried
 		if (changed.getChangedEntities().contains(victimID) ||
 				help.isBuriedness() == 0)
 			return;
 		
 		StandardEntity entity = model.getEntity(victimID);
 		Human human;
 
 		if (entity != null && entity instanceof Human) {
 			switch (entity.getStandardURN()) {
 			case FIRE_BRIGADE:
 				human = (FireBrigade) entity;
 				break;
 			case POLICE_FORCE:
 				human = (PoliceForce) entity;
 				break;
 			case AMBULANCE_TEAM:
 				human = (AmbulanceTeam) entity;
 				break;
 			default:
 				human = (Civilian) entity;
 			}
 		} else {
 			switch (help.getURN()) {
 			case FIRE_BRIGADE:
 				human = new FireBrigade(victimID);
 				break;
 			case POLICE_FORCE:
 				human = new PoliceForce(victimID);
 				break;
 			case AMBULANCE_TEAM:
 				human = new AmbulanceTeam(victimID);
 				break;
 			default:
 				human = new Civilian(victimID);
 			}
 		}
 
 		human.setPosition(currentPosition);
 		human.setHP(1);
 		human.setDamage(help.isDamage());
 		human.setBuriedness(help.isBuriedness());
 
 		if (model.getEntity(victimID) == null) {
 			//model.addEntity(human);
 			// Even though the victim is not in the same place it was heard,
 			// it's the best shot we have
 			//knownVictims.add(victimID);
 			//log("in");
 		}
 	}
 
 	/**
 	 * Avalia se algum prédio foi complemente queimado
 	 * 
 	 * @param param
 	 * @param changed
 	 */
 	private void readMsgBuildingBurnt(Parameter param, ChangeSet changed) {
 		if (!(param instanceof BuildingBurnt))
 			return;
 		
 		BuildingBurnt fire = (BuildingBurnt) param;
 		EntityID buildingID = new EntityID(fire.getBuilding());
 
 		if (changed.getChangedEntities().contains(buildingID))
 			return;
 		
 		StandardEntity entity = model.getEntity(buildingID);
 
 		if (entity != null && entity instanceof Building) {
 			Building building = (Building) entity;
 			building.setFieryness(Fieryness.BURNT_OUT.ordinal());
 			model.addEntity(building);
 			buildingsOnFire.remove(buildingID);
 		}
 	}
 
 	/**
 	 * Avalia se há algum incêndio extinto
 	 * 
 	 * @param param
 	 * @param changed
 	 */
 	private void readMsgFireExtinguished(Parameter param, ChangeSet changed) {
 		if (!(param instanceof FireExtinguished))
 			return;
 		
 		FireExtinguished fire = (FireExtinguished) param;
 		EntityID buildingID = new EntityID(fire.getBuilding());
 
 		if (changed.getChangedEntities().contains(buildingID))
 			return;
 		
 		StandardEntity entity = model.getEntity(buildingID);
 
 		if (entity != null && entity instanceof Building) {
 			Building building = (Building) entity;
 			building.setFieryness(Fieryness.WATER_DAMAGE.ordinal());
 			model.addEntity(building);
 			buildingsOnFire.remove(buildingID);
 		}
 	}
 
 	/**
 	 * Avalia se alguma vítima foi resgatada
 	 * 
 	 * @param param
 	 * @param changed
 	 */
 	private void readMsgVictimRescued(Parameter param, ChangeSet changed) {
 		if (!(param instanceof VictimRescued))
 			return;
 		
 		VictimRescued victim = (VictimRescued) param;
 		EntityID victimID = new EntityID(victim.getVictim());
 
 		if (changed.getChangedEntities().contains(victimID))
 			return;
 		
 		StandardEntity entity = model.getEntity(victimID);
 		Human human;
 
 		if (entity != null && entity instanceof Human) {
 			switch (entity.getStandardURN()) {
 			case FIRE_BRIGADE:
 				human = (FireBrigade) entity;
 				break;
 			case POLICE_FORCE:
 				human = (PoliceForce) entity;
 				break;
 			case AMBULANCE_TEAM:
 				human = (AmbulanceTeam) entity;
 				break;
 			default:
 				human = (Civilian) entity;
 			}
 
 			human.setBuriedness(0);
 
 			model.addEntity(human);
 			knownVictims.remove(victimID);
 		}
 	}
 
 	/**
 	 * Avalia se alguma vítima morreu
 	 * 
 	 * @param param
 	 * @param changed
 	 */
 	private void readMsgVictimDied(Parameter param, ChangeSet changed) {
 		if (!(param instanceof VictimDied))
 			return;
 		
 		VictimDied victim = (VictimDied) param;
 		EntityID victimID = new EntityID(victim.getVictim());
 
 		if (changed.getChangedEntities().contains(victimID))
 			return;
 		
 		StandardEntity entity = model.getEntity(victimID);
 		Human human;
 
 		if (entity != null && entity instanceof Human) {
 			switch (entity.getStandardURN()) {
 			case FIRE_BRIGADE:
 				human = (FireBrigade) entity;
 				break;
 			case POLICE_FORCE:
 				human = (PoliceForce) entity;
 				break;
 			case AMBULANCE_TEAM:
 				human = (AmbulanceTeam) entity;
 				break;
 			default:
 				human = (Civilian) entity;
 			}
 
 			human.setHP(0);
 
 			model.addEntity(human);
 			knownVictims.remove(victimID);
 		}
 	}
 
 	/**
 	 * Avalia se algum bloqueio foi removido
 	 * @param param
 	 */
 	private void readMsgBlockadeCleared(Parameter param) {
 		if (!(param instanceof BlockadeCleared))
 			return;
 		
 		BlockadeCleared blockade = (BlockadeCleared) param;
 		EntityID blockadeID = new EntityID(
 				blockade.getBlockade());
 
 		if (model.getEntity(blockadeID) != null
 				&& model.getEntity(blockadeID) instanceof Blockade) {
 			model.removeEntity(blockadeID);
 			knownBlockades.remove(blockadeID);
 		}
 	}
 
 	/**
 	 * Avalia se algum agente passou a atuar em uma tarefa
 	 * 
 	 * @param param
 	 * @param cmd
 	 */
 	private void readMsgTaskPickup(Parameter param, Command cmd) {
 		if (!(param instanceof TaskPickup))
 			return;
 		
 		TaskPickup task = (TaskPickup) param;
 		EntityID taskID = new EntityID(task.getTask());
 
 		for (Set<EntityID> s : taskTable.values())
 			s.remove(cmd.getAgentID());
 		if (!taskTable.containsKey(taskID))
 			taskTable.put(taskID, new HashSet<EntityID>());
 		taskTable.get(taskID).add(cmd.getAgentID());
 	}
 
 	/**
 	 * Avalia se algum agente abandonou uma tarefa
 	 * Remove o ID do agente em questão do conjunto de agentes
 	 * agindo sobre aquela tarefa
 	 * 
 	 * @param param
 	 * @param cmd
 	 */
 	private void readMsgTaskDrop(Parameter param, Command cmd) {
 		 if (!(param instanceof TaskDrop))
 			 return;
 		 
 		TaskDrop task = (TaskDrop) param;
 		EntityID taskID = new EntityID(task.getTask());
 		
 		if (taskTable.containsKey(taskID))
 			taskTable.get(taskID).remove(cmd.getAgentID());
 	}
 
 	/**
 	 * Avalia se foi identificada uma nova vítima
 	 * @param param
 	 * @param changed
 	 */
 	private void readMsgVictim(Parameter param, ChangeSet changed) {
 		if (!(param instanceof Victim))
 			return;
 		
 		Victim victim = (Victim) param;
 		EntityID victimID = new EntityID(victim.getVictim());
 
 		if (changed.getChangedEntities().contains(victimID))
 			return;
 		
 		StandardEntity entity = model.getEntity(victimID);
 		Human human;
 
 		if (entity != null && entity instanceof Human) {
 			switch (entity.getStandardURN()) {
 			case FIRE_BRIGADE:
 				human = (FireBrigade) entity;
 				break;
 			case POLICE_FORCE:
 				human = (PoliceForce) entity;
 				break;
 			case AMBULANCE_TEAM:
 				human = (AmbulanceTeam) entity;
 				break;
 			default:
 				human = (Civilian) entity;
 			}
 		} else {
 			switch (victim.getURN()) {
 			case FIRE_BRIGADE:
 				human = new FireBrigade(victimID);
 				break;
 			case POLICE_FORCE:
 				human = new PoliceForce(victimID);
 				break;
 			case AMBULANCE_TEAM:
 				human = new AmbulanceTeam(victimID);
 				break;
 			default:
 				human = new Civilian(victimID);
 			}
 		}
 
 		human.setPosition(new EntityID(victim.getPosition()));
 		human.setHP(victim.getHP());
 		human.setDamage(victim.getDamage());
 		human.setBuriedness(victim.getBuriedness());
 
 		model.addEntity(human);
 		knownVictims.add(victimID);
 	}
 
 	/**
 	 * Avalia se foi identificado um novo bloqueio
 	 * 
 	 * @param param
 	 * @param changed
 	 */
 	private void readMsgBlockade(Parameter param, ChangeSet changed) {
 		if(!(param instanceof lti.message.type.Blockade))
 			return;
 		
 		lti.message.type.Blockade blockade = (lti.message.type.Blockade) param;
 		EntityID blockadeID = new EntityID(blockade.getBlockade());
 
 		if (changed.getChangedEntities().contains(blockadeID))
 			return;
 		
 		StandardEntity entity = model.getEntity(blockadeID);
 		Blockade block;
 
 		if (entity != null && entity instanceof Blockade)
 			block = (Blockade) entity;
 		else
 			block = new Blockade(blockadeID);
 		
 		block.setPosition(new EntityID(blockade.getRoad()));
 		block.setX(blockade.getX());
 		block.setY(blockade.getY());
 		block.setRepairCost(blockade.getCost());
 
 		model.addEntity(block);
 		// O bloqueio passa a ser conhecido
 		knownBlockades.add(blockadeID);
 	}
 
 	/**
 	 * Avalia se foi identificado um novo incêndio
 	 * 
 	 * @param param
 	 * @param changed
 	 */
 	private void readMsgFire(Parameter param, ChangeSet changed) {
 		if (!(param instanceof Fire))
 			return;
 		
 		Fire fire = (Fire) param;
 		EntityID buildingID = new EntityID(fire.getBuilding());
 
 		if (changed.getChangedEntities().contains(buildingID))
 			return;
 		
 		StandardEntity entity = model.getEntity(buildingID);
 		Building building;
 
 		if (entity != null && entity instanceof Building)
 			building = (Building) entity;
 		else
 			building = new Building(buildingID);
 
 		building.setFieryness(fire.getIntensity());
 		building.setGroundArea(fire.getGroundArea());
 		building.setFloors(fire.getFloors());
 
 		model.addEntity(building);
 		// Esse incêndio passa a ser conhecido
 		buildingsOnFire.add(buildingID);
 	}
 
 	/**
 	 * Remove blockades that do not exist anymore (blockades the agent
 	 * should see, but does not
 	 * @param changed
 	 */
 	private void removeNonExistingBlockadesFromModel(ChangeSet changed) {
 		Set<StandardEntity> blockades = new HashSet<StandardEntity>(
 				model.getEntitiesOfType(StandardEntityURN.BLOCKADE));
 		Set<EntityID> visibleBlockades =
 				getVisibleEntitiesOfType(StandardEntityURN.BLOCKADE, changed);
 		for (StandardEntity entity : blockades) {
 			Blockade block = (Blockade) entity;
 			EntityID positionBlockade = block.getPosition();
 			EntityID blockadeID = block.getID();
 			if (positionBlockade.equals(currentPosition)
 					&& !visibleBlockades.contains(blockadeID)) {
 				model.removeEntity(block);
 				knownBlockades.remove(blockadeID);
 			}
 		}
 	}
 
 	protected Message composeMessage(ChangeSet changed) {
 		return composeMessage(changed, new Message());
 	}
 	
 	/** Compõe uma mensagem para ser enviada de acordo com o que o agente vê */
 	protected Message composeMessage(ChangeSet changed, Message message) {
 
 		for (EntityID buildingID : getVisibleEntitiesOfType(
 				StandardEntityURN.BUILDING, changed)) {
 			Building building = (Building) model.getEntity(buildingID);
 
 			/*
 			 * Se o prédio está em chamas e este incêndio não é conhecido, deve
 			 * se enviar uma mensagem falando sobre ele
 			 */
 			if (building.isOnFire() && !buildingsOnFire.contains(buildingID)) {
 				Fire fire = new Fire(buildingID.getValue(),
 						building.getGroundArea(), building.getFloors(),
 						building.getFieryness());
 				message.addParameter(fire);
 				// O incêndio passa a ser conhecido
 				buildingsOnFire.add(buildingID);
 
 			}
 			/*
 			 * Se o prédio não está em chamas, mas havia um incêndio nele
 			 * anteriormente, há duas opções: 1) O incêndio comsumiu todo o
 			 * prédio; 2) O incêndio foi extinguido
 			 */
 			else if (!building.isOnFire()
 					&& buildingsOnFire.contains(buildingID)) {
 
 				if (building.getFierynessEnum().equals(Fieryness.BURNT_OUT)) {
 					BuildingBurnt burnt = new BuildingBurnt(
 							buildingID.getValue());
 					message.addParameter(burnt);
 
 				} else if (building.getFierynessEnum().compareTo(
 						Fieryness.INFERNO) > 0) {
 					FireExtinguished extinguished = new FireExtinguished(
 							buildingID.getValue());
 					message.addParameter(extinguished);
 				}
 
 				// Não se sabe mais de nenhum incêndio neste prédio
 				buildingsOnFire.remove(buildingID);
 			}
 		}
 
 		/*
 		 * Se algum bloqueio visível não é conhecido, deve-se enviar uma
 		 * mensagem relatando-o
 		 */
 		for (EntityID blockadeID : getVisibleEntitiesOfType(
 				StandardEntityURN.BLOCKADE, changed)) {
 			Blockade blockade = (Blockade) model.getEntity(blockadeID);
 
 			if (!knownBlockades.contains(blockadeID)) {
 				lti.message.type.Blockade block = new lti.message.type.Blockade(
 						blockadeID.getValue(), blockade.getPosition()
 								.getValue(), blockade.getX(), blockade.getY(),
 						blockade.getRepairCost());
 				message.addParameter(block);
 				knownBlockades.add(blockadeID);
 			}
 		}
 
 		Set<EntityID> toRemove = new HashSet<EntityID>();
 
 		/*
 		 * Se algum bloqueio conhecido não se encontra mais em seu lugar, ele
 		 * foi removido
 		 */
 		for (EntityID blockadeID : knownBlockades) {
 			if (model.getEntity(blockadeID) == null) {
 				BlockadeCleared cleared = new BlockadeCleared(
 						blockadeID.getValue());
 				message.addParameter(cleared);
 				toRemove.add(blockadeID);
 			}
 		}
 
 		knownBlockades.removeAll(toRemove);
 
 		Set<EntityID> victims = new HashSet<EntityID>();
 		StandardEntityURN[] urns = { StandardEntityURN.AMBULANCE_TEAM,
 				StandardEntityURN.FIRE_BRIGADE, StandardEntityURN.CIVILIAN,
 				StandardEntityURN.POLICE_FORCE };
 		/*
 		 * Victims: - AT, FB and PF who are alive and buried; - Civilians who
 		 * are alive and are either buried or inside a building that is not a
 		 * refuge.
 		 */
 		for (int i = 0; i < 4; i++) {
 			Set<EntityID> nonRefugeBuildings = new HashSet<EntityID>(
 					buildingIDs);
 			nonRefugeBuildings.removeAll(getRefuges());
 
 			for (EntityID next : getVisibleEntitiesOfType(urns[i], changed)) {
 				Human human = (Human) model.getEntity(next);
 				if (human.isHPDefined() && human.getHP() != 0) {
 					if (urns[i].equals(StandardEntityURN.CIVILIAN)
 							&& nonRefugeBuildings.contains(human.getPosition())) {
 						victims.add(next);
 
 					} else if (human.isBuriednessDefined()
 							&& human.getBuriedness() != 0) {
 						victims.add(next);
 					}
 				}
 			}
 		}
 
 		/*
 		 * Se alguma vítima visível não é conhecida, deve-se enviar uma mensagem
 		 * relatando-a
 		 */
 		for (EntityID next : victims) {
 			StandardEntity entity;
 			Human human;
 			int urn;
 
 			if (!knownVictims.contains(next)) {
 				entity = model.getEntity(next);
 
 				if (entity != null && entity instanceof Human) {
 					switch (entity.getStandardURN()) {
 					case AMBULANCE_TEAM:
 						human = (AmbulanceTeam) entity;
 						urn = 0;
 						break;
 
 					case FIRE_BRIGADE:
 						human = (FireBrigade) entity;
 						urn = 1;
 						break;
 
 					case POLICE_FORCE:
 						human = (PoliceForce) entity;
 						urn = 2;
 						break;
 
 					default:
 						human = (Civilian) entity;
 						urn = 3;
 					}
 
 					Victim victim = new Victim(next.getValue(), human
 							.getPosition().getValue(), human.getHP(),
 							human.getDamage(), human.getBuriedness(), urn);
 					message.addParameter(victim);
 					knownVictims.add(next);
 				}
 			}
 		}
 
 		toRemove = new HashSet<EntityID>();
 
 		/*
 		 * Se alguma vítima conhecida não está no lugar aonde deveria estar ou
 		 * morreu, deve-se enviar uma mensagem relatando a situação
 		 */
 		for (EntityID victim : knownVictims) {
 			StandardEntity entity = model.getEntity(victim);
 
 			if (entity != null && entity instanceof Human) {
 				Human human = (Human) entity;
 
 				if (human.isHPDefined() && human.getHP() == 0) {
 					VictimDied death = new VictimDied(victim.getValue());
 					message.addParameter(death);
 					toRemove.add(victim);
 				} else if (human.isBuriednessDefined() &&
 						human.getBuriedness() == 0) {
 					VictimRescued rescue = new VictimRescued(victim.getValue());
 					message.addParameter(rescue);
 					toRemove.add(victim);
 				}
 			}
 		}
 
 		knownVictims.removeAll(toRemove);
 
 		/* Se uma tarefa foi abandonada, envia-se uma mensagem */
 		if (taskDropped != null) {
 			TaskDrop drop = new TaskDrop(taskDropped.getValue());
 			message.addParameter(drop);
			taskDropped = null;
 
 			/* Se uma tarefa foi escohida, envia-se uma mensagem relatando-a */
 			if (target != null) {
 				TaskPickup task = new TaskPickup(target.getValue());
 				message.addParameter(task);
 			}
 		}
 		/* Se uma tarefa foi escohida, envia-se uma mensagem relatando-a */
 		if (target != null && currentTime == config.getIntValue(KernelConstants.IGNORE_AGENT_COMMANDS_KEY)) {
 			TaskPickup task = new TaskPickup(target.getValue());
 			message.addParameter(task);
 		}
 
 		return message;
 	}
 
 	protected abstract boolean amIBlocked(int time);
 
 	protected EntityID selectTask() {
 		return new EntityID(0);
 	}
 	
 	public void setVerbose(boolean verbose) {
 		this.verbose = verbose;
 	}
 	
 	protected void log(String s) {
 		if (this.verbose) {
 			String[] type_agent = me().getURN().split(":");
 
 			String msg_erro =
 					"T" + currentTime +
 				" " + type_agent[type_agent.length - 1] + internalID + 
 				" ID" + me().getID() +
 				" Pos:(" + currentX + "," + currentY + ")@" + currentPosition;
 			if (s != "")
 				msg_erro += " - " + s;
 			System.out.println(msg_erro);
 		}
 	}
 }
