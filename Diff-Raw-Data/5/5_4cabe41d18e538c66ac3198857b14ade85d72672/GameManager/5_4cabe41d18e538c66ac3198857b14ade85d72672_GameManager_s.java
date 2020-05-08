 package riskyspace;
 
 import java.net.InetAddress;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import riskyspace.logic.Battle;
 import riskyspace.logic.FleetMove;
 import riskyspace.logic.Path;
 import riskyspace.logic.data.BattleStats;
 import riskyspace.model.Colony;
 import riskyspace.model.Fleet;
 import riskyspace.model.Planet;
 import riskyspace.model.Player;
 import riskyspace.model.Position;
 import riskyspace.model.Resource;
 import riskyspace.model.ShipType;
 import riskyspace.model.Territory;
 import riskyspace.model.World;
 import riskyspace.services.Event;
 import riskyspace.services.EventBus;
 import riskyspace.services.EventText;
 
 public enum GameManager {
 	INSTANCE;
 	
 	private Player[] players = {Player.BLUE, Player.RED, Player.GREEN, Player.PINK};
 	
 	private Player currentPlayer = null;
 	private boolean initiated = false;
 	private World world = null;
 	private int turn;
 	
 	private List<Player> activePlayers = new ArrayList<Player>();
 	private Map<Player, PlayerInfo> playerInfo = new HashMap<Player, PlayerInfo>();
 	private Map<Player, Selection> selections = new HashMap<Player, Selection>();
 	
 	private class Selection {
 		private Position lastFleetSelectPos = null;
 		private int fleetSelectionIndex = 0;
 		private Set<Fleet> selectedFleets = new HashSet<Fleet>();
 		private Map<Fleet, Path> fleetPaths = new HashMap<Fleet, Path>();
 		private Colony selectedColony = null;
 		private Planet selectedPlanet = null;
 	}
 	
 	/**
 	 * Used for Network Game
 	 * @param world
 	 */
 	public void init(World world) {
 		this.world = world;
 		turn = 1;
 		initiated = true;
 	}
 	
 	/**
 	 * Used for Hot-Seat Game
 	 * @param world
 	 * @param numberOfPlayers
 	 */
 	public void init(World world, int numberOfPlayers) {
 		init(world);
 		initPlayers(numberOfPlayers);
 	}
 	
 	public void start() {
 		changePlayer();
 		world.updatePlayerStats(getCurrentPlayer());
 	}
 	
 	public void initPlayers(int numberOfPlayers) {
 		for (int i = 0; i < numberOfPlayers; i++) {
 			activePlayers.add(players[i]);
 		}
 		for (Player player : activePlayers) {
 			playerInfo.put(player, new PlayerInfo());
 			selections.put(player, new Selection());
 		}
 	}
 	
 	public Player addPlayer(InetAddress ip){
 		Player player = players[activePlayers.size()];
 		activePlayers.add(player);
 		playerInfo.put(player, new PlayerInfo());
 		playerInfo.get(player).setIP(ip);
 		selections.put(player, new Selection());
 		return player;
 	}
 
 	public Player getCurrentPlayer() {
 		return currentPlayer;
 	}
 	
 	public List<Player> getActivePlayers() {
 		return new LinkedList<Player>(activePlayers);
 	}
 	
 	public int getTurn() {
 		return turn;
 	}
 	
 	public PlayerInfo getInfo(Player player) {
 		return playerInfo.get(player);
 	}
 	
 	private void changePlayer() {
 		currentPlayer = activePlayers.get(((activePlayers.indexOf(currentPlayer) + 1) % activePlayers.size()));
 		Event event = new Event(Event.EventTag.ACTIVE_PLAYER_CHANGED, currentPlayer);
 		EventBus.SERVER.publish(event);
 		world.giveIncome(currentPlayer);
 		world.updatePlayerStats(currentPlayer);
 		world.processBuildQueue(getCurrentPlayer());
 		world.updatePlayerStats(currentPlayer);
 		world.resetShips();
 		if (currentPlayer == players[0]) {
 			turn++;
 		}
 	}
 
 	public void handleEvent(Event evt, Player player) {
 		System.out.println("manager: " + evt + " player: " + player + " | cur = " + currentPlayer);
 		if (!initiated) {
 			return;
 		}
 		/*
 		 * View triggered Events
 		 */
 		if (!FleetMove.isMoving()) {
 			if(evt.getObjectValue() instanceof Position){
 				Position pos = (Position) evt.getObjectValue();
 				if (evt.getTag() == Event.EventTag.SET_PATH && player == getCurrentPlayer()) {
 					setPath(pos);
 				} else if (evt.getTag() == Event.EventTag.COLONIZER_SELECTED) {
 					setColonizerSelected(pos, player);
 				} else if (evt.getTag() == Event.EventTag.ADD_FLEET_SELECTION) {
 					addFleetSelection(pos, player);
 				} else if (evt.getTag() == Event.EventTag.NEW_FLEET_SELECTION) {
 					newFleetSelection(pos, player);
 				}
 			} else if (evt.getTag() == Event.EventTag.NEXT_TURN && player == getCurrentPlayer()) {
 				changePlayer();
 			} else if (evt.getTag() == Event.EventTag.COLONIZE_PLANET && player == getCurrentPlayer()) {
 				colonizePlanet(evt);
 			} else if (evt.getTag() == Event.EventTag.NEW_FLEET_SELECTION) {
 				newFleetsSelection(evt, player);
 			} else if (evt.getTag() == Event.EventTag.MOVE && player == getCurrentPlayer()) {
 				performMoves();
 			} else if (evt.getTag() == Event.EventTag.QUEUE_SHIP && player == getCurrentPlayer()) {
 				queueShip((ShipType) evt.getObjectValue());
 			}
 		} else if (FleetMove.isMoving() && evt.getTag() == Event.EventTag.MOVE && player == getCurrentPlayer()) {
 			FleetMove.interrupt();
 		}
 		if(evt.getTag() == Event.EventTag.PLANET_SELECTED) {
 			planetSelected((Position) evt.getObjectValue(), player);
 		} 
 		if (evt.getTag() == Event.EventTag.DESELECT) {
 			resetVariables(player);
 		} 
 		if (evt.getTag() == Event.EventTag.SHIP_SELFDESTCRUCT) {
 			shipDestruct();
 		}
 		/*
 		 * Model or Controller triggered Events
 		 */
 		if (evt.getTag() == Event.EventTag.MOVES_COMPLETE) {
 			movesComplete();
 		}
 		if (evt.getTag() == Event.EventTag.INCOME_CHANGED) {
 			incomeChanged((Player) evt.getObjectValue());
 		}
 	}
 	
 	private void incomeChanged(Player affectedPlayer) {
 		int metalIncome = 10;
 		int gasIncome = 0;
 		for (Position pos : world.getContentPositions()) {
 			Territory terr = world.getTerritory(pos);
 			if (terr.hasColony()) {
 				if (terr.getColony().getOwner() == affectedPlayer) {
 					if (terr.getPlanet().getType() == Resource.METAL) {
 						metalIncome += terr.getColony().getIncome();
 					} else if (terr.getPlanet().getType() == Resource.GAS) {
 						gasIncome += terr.getColony().getIncome();
 					}
 				}
 			}
 		}
 		world.setIncome(affectedPlayer, Resource.METAL, metalIncome);
 		world.setIncome(affectedPlayer, Resource.GAS, gasIncome);
 	}
 	
 	private void planetSelected(Position pos, Player player) {
 		resetVariables(player);
 		Territory selectedTerritory = world.getTerritory(pos);
 		if (selectedTerritory.hasPlanet()) {
 			if (selectedTerritory.hasColony() && player == selectedTerritory.getColony().getOwner()) {
 				selections.get(player).selectedColony = selectedTerritory.getColony();
 				Event evt = new Event(Event.EventTag.SELECTION, selectedTerritory.getColony());
 				EventBus.SERVER.publish(evt);
 			} else {
 				selections.get(player).selectedPlanet = selectedTerritory.getPlanet();
 				Event evt = new Event(Event.EventTag.SELECTION, selectedTerritory.getPlanet());
 				EventBus.SERVER.publish(evt);
 				if (selectedTerritory.hasColonizer() && player == selectedTerritory.controlledBy() && !selectedTerritory.hasConflict()) {
 					evt = new Event(Event.EventTag.COLONIZER_PRESENT, null);
 					EventBus.SERVER.publish(evt);
 				}
 			}
 		}
 	}
 	
 	private void movesComplete() {
 		for (Position pos : world.getContentPositions()) {
 			Territory terr = world.getTerritory(pos);
 			if (terr.hasConflict()) {
 				BattleStats battleStats = Battle.doBattle(terr);
 				for (Fleet f : battleStats.getDestroyedFleets()) {
 					for(Player player : battleStats.getParticipants()) {
 						selections.get(player).fleetPaths.remove(f);
 					}
 				}
 				if (battleStats.isColonyDestroyed()) {
 					/*
 					 * Remove BuildQueue
 					 */
 					world.removeBuildQueue(battleStats.getLoser(), pos);
 				}
 				EventText et = new EventText(battleStats.getWinnerString(), pos);
 				Event event = new Event(Event.EventTag.EVENT_TEXT, et);
 //				EventBus.INSTANCE.publish(event);  TODO: Ignore evtText atm
 			}
 		}
 	}
 	
 	private void shipDestruct() {
 		/*
 		 * TODO: Removes ships, but does so in an uncontrolled manner and needs to be redone somehow.
 		 * 		 It's in other words unfinished...
 		 */
 //		if (world.getTerritory(lastFleetSelectPos).hasFleet() && fleetSelectionIndex >=0) {
 //			selectedFleets.remove(world.getTerritory(lastFleetSelectPos).getFleet(fleetSelectionIndex));
 //			world.getTerritory(lastFleetSelectPos).removeFleet(world.getTerritory(lastFleetSelectPos).getFleet(fleetSelectionIndex));
 //			System.out.println(lastFleetSelectPos);
 //			System.out.println(world.getTerritory(lastFleetSelectPos).getFleets().size());
 //			if (!world.getTerritory(lastFleetSelectPos).hasFleet()) {
 //				resetVariables();
 //			} else {
 //				fleetSelectionIndex = Math.max(fleetSelectionIndex - 1, 0);
 //				Collections.unmodifiableSet(selectedFleets) Make it show fleet menu!
 //			}
 //		} TODO: Fix later, low prio
 	}
 	
 	public Position[][] getPaths(Player player) {
 		Position[][] tmp = new Position[selections.get(player).fleetPaths.size()][];
 		int i = 0;
 		for (Fleet fleet : selections.get(player).fleetPaths.keySet()) {
 			if (fleet.getOwner() == player) {
 				tmp[i] = selections.get(player).fleetPaths.get(fleet).getPositions();
 				i++;
 			}
 		}
 		Position[][] paths = new Position[i][];
 		for (int j = 0; j < paths.length; j++) {
 			paths[j] = tmp[j];
 		}
 		return paths;
 	}
 
 	private void performMoves() {
 		selections.get(getCurrentPlayer()).fleetSelectionIndex = 0;
 		FleetMove.move(world, selections.get(getCurrentPlayer()).fleetPaths, GameManager.INSTANCE.getCurrentPlayer());
 	}
 
 	private void setPath(Position target) {
 		for (Fleet fleet : selections.get(getCurrentPlayer()).selectedFleets) {
 			if (GameManager.INSTANCE.getCurrentPlayer() == fleet.getOwner()) {
 				selections.get(getCurrentPlayer()).fleetPaths.get(fleet).setTarget(target);
 			}
 		}
 		Event evt = new Event(Event.EventTag.UPDATE_SPRITEDATA, null);
 		EventBus.SERVER.publish(evt);
 	}
 	
 	private void colonizePlanet(Event evt) {
 		/*
 		 * TODO REMAKE WITH POS
 		 */
 		if (evt.getObjectValue() instanceof Territory) {
 			Territory ter = (Territory) evt.getObjectValue();
 			if (ter.hasFleet() && ter.hasPlanet() && !ter.hasColony()) {
 				for (Fleet fleet : ter.getFleets()) {
 					if (fleet.hasColonizer()) {
 						/*
 						 * Remove fleet is empty?
 						 */
 						fleet.useColonizer();
 						ter.getPlanet().buildColony(fleet.getOwner());
 						ter.removeFleet(fleet);
 						world.updatePlayerStats(getCurrentPlayer());
 						break; // Stop looping through fleets.
 					}
 				}
 //				selections.get(player).selectedColony = ter.getColony();
 				// Make it show colony
 			}
 		}
 	}
 	
 	private void setColonizerSelected(Position pos, Player player) {
 		resetVariables(player);
 		selections.get(player).fleetSelectionIndex = 0;
 		selections.get(player).lastFleetSelectPos = null;
 		if (world.getTerritory(pos).hasColonizer()) {
 			for (Fleet fleet : world.getTerritory(pos).getFleets()) {
 				if (fleet.hasColonizer()) {
 					selections.get(player).selectedFleets.add(fleet);
 					selections.get(player).fleetPaths.put(fleet, new Path(pos));
 					break;
 				}
 			}
 			Event evt = new Event(Event.EventTag.SELECTION, new Fleet(selections.get(player).selectedFleets));
 			EventBus.SERVER.publish(evt);
 		}
 		
 	}
 	
 	private void addFleetSelection(Position pos, Player player) {
 		selections.get(player).selectedColony = null;
 		
 		if (selections.get(player).lastFleetSelectPos == null || !selections.get(player).lastFleetSelectPos.equals(pos)) {
 			selections.get(player).lastFleetSelectPos = pos;
 			selections.get(player).fleetSelectionIndex = 0;
 		}
 		if (world.getTerritory(pos).hasFleet() && (world.getTerritory(pos).controlledBy() == GameManager.INSTANCE.getCurrentPlayer())) {
 			/*
 			 * Check that there are other ships than colonizers
 			 */
 			if (world.getTerritory(pos).getFleets().size() != world.getTerritory(pos).shipCount(ShipType.COLONIZER)) {
 				Fleet fleet;
 				do {
 					fleet = world.getTerritory(pos).getFleet(selections.get(player).fleetSelectionIndex);
 					selections.get(player).fleetSelectionIndex = (selections.get(player).fleetSelectionIndex + 1) % world.getTerritory(pos).getFleets().size();
 				} while(fleet.hasColonizer());
 				addSelectedFleet(fleet, pos);
 				Event evt = new Event(Event.EventTag.SELECTION, new Fleet(selections.get(player).selectedFleets));
 				EventBus.SERVER.publish(evt);
 			}
 		}
 	}
 
 	private void newFleetSelection(Position pos, Player player) {
 		resetVariables(player); // Reset all selections as we make a new selection
 		if (selections.get(player).lastFleetSelectPos == null || !selections.get(player).lastFleetSelectPos.equals(pos)) {
 			selections.get(player).lastFleetSelectPos = pos;
 			selections.get(player).fleetSelectionIndex = 0;
 		}
 		if (world.getTerritory(pos).hasFleet() && (world.getTerritory(pos).controlledBy() == GameManager.INSTANCE.getCurrentPlayer())) {
 			if (world.getTerritory(pos).getFleets().size() != world.getTerritory(pos).shipCount(ShipType.COLONIZER)) {
 				Fleet fleet;
 				do {
 					fleet = world.getTerritory(pos).getFleet(selections.get(player).fleetSelectionIndex);
 					selections.get(player).fleetSelectionIndex = (selections.get(player).fleetSelectionIndex + 1) % world.getTerritory(pos).getFleets().size();
 				} while(fleet.hasColonizer());
 				addSelectedFleet(fleet, pos);
 			}
 		}
 		if (!selections.get(player).selectedFleets.isEmpty()) {
 			Event evt = new Event(Event.EventTag.SELECTION, new Fleet(selections.get(player).selectedFleets));
 			EventBus.SERVER.publish(evt);
 		}
 	}
 
 	private void newFleetsSelection(Event evt, Player player) {
 		resetVariables(player); // Reset all selections as we make a new selection
 		if (evt.getObjectValue() instanceof List) {
 			List<?> positions = (List<?>) evt.getObjectValue();
 			for (int i = 0; i < positions.size(); i++) {
 				if (positions.get(i) instanceof Position) {
 					Position pos = (Position) positions.get(i);
 					if (world.getTerritory(pos).hasFleet() && (world.getTerritory(pos).controlledBy() == getCurrentPlayer())) {
 						for (Fleet fleet : world.getTerritory(pos).getFleets()) {
 							if (!fleet.hasColonizer()) {
 								addSelectedFleet(fleet, pos);
 							}
 						}
 					}
 				}
 			}
 		}
 		if (!selections.get(player).selectedFleets.isEmpty()) {
 			Event event = new Event(Event.EventTag.SELECTION, new Fleet(selections.get(player).selectedFleets));
 			EventBus.SERVER.publish(event);
 		}
 	}
 	private void addSelectedFleet(Fleet fleet, Position pos){
 		selections.get(fleet.getOwner()).selectedFleets.add(fleet);
 		if (!selections.get(fleet.getOwner()).fleetPaths.containsKey(fleet)) {
 			selections.get(fleet.getOwner()).fleetPaths.put(fleet, new Path(pos));
 		}
 	}
 
 	private void queueShip(ShipType shipType) {
 		for (Position pos : world.getContentPositions()) {
 			if (world.getTerritory(pos).hasColony()) {
 				if (world.getTerritory(pos).getColony() == selections.get(getCurrentPlayer()).selectedColony) {
 					if (world.canAfford(getCurrentPlayer(), shipType)) {
 						world.purchase(getCurrentPlayer(), shipType);
 						world.addToBuildQueue(shipType, getCurrentPlayer(), pos);
 					}
 				}
 			}
 		}
 	}
 
 	private void resetVariables(Player player) {
 		selections.get(player).selectedFleets.clear();
 		selections.get(player).selectedColony = null;
 	}
 }
