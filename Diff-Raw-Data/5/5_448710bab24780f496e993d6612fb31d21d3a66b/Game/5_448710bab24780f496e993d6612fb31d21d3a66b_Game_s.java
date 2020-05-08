 package se.chalmers.dat255.risk.model;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.ArrayList;
 import java.util.List;
 
 import se.chalmers.dat255.risk.model.TurnAndPhaseManager.Phase;
 import se.chalmers.dat255.risk.model.TurnAndPhaseManager.ResultType;
 
 /**
  * The top game class. Controls flow between our lower classes, such as the
  * battle handler and the WorldMap.
  * 
  */
 
 public class Game implements IGame {
 	private ArrayList<Player> players;
 	private WorldMap worldMap;
 	private EventHandler eventHandler;
 	private TurnAndPhaseManager phaseHandler;
 	private BonusHandler bonusHandler;
 	private BattleHandler battle;
 	private Deck deck;
 	private IProvince oldProvince, secondProvince;
 	private boolean movedTroops = false; // F3
 	private boolean firstProvinceConqueredThisTurn = true;
 	private PropertyChangeSupport pcs;
 	private MissionHandler missionHandler;
 	private GameMode gameMode = GameMode.SECRET_MISSION;
 
 	private String continentsFile;
 	private String neighboursFile;
 	private String missionFile;
 
 	private final int maxAllowedPlayers = 6;
 	private final int minAllowedPlayers = 2;
 	private final int numbersOfWildCards = 6;
 	private final int oneDice = 1;
 	private final int twoDices = 2;
 	private final int threeDices = 3;
 
 	/**
 	 * Creates a new Game.
 	 * 
 	 * @param playersId
 	 *            The ids of the players
 	 */
 	/*public Game() {
 		battle = new BattleHandler();
 		pcs = new PropertyChangeSupport(this);
 	}*/
 
 	public Game(GameMode gameMode) {
 		battle = new BattleHandler();
 		pcs = new PropertyChangeSupport(this);
 		this.gameMode=gameMode;
 	}
 	
 	public void setupGame(String[] playersId, String neighboursFile,
 			String continentsFile, String missionFile) {
 		this.neighboursFile = neighboursFile;
 		this.continentsFile = continentsFile;
 		this.missionFile = missionFile;
 		newGame(playersId);
 	}
 
 	private void newGame(String[] playersId) throws IllegalArgumentException {
 		phaseHandler = new TurnAndPhaseManager();
 		eventHandler = new EventHandler(phaseHandler);
 		int noOfPlayers = playersId.length;
 		if (noOfPlayers > maxAllowedPlayers || noOfPlayers < minAllowedPlayers) {
 			throw new IllegalArgumentException(
 					"The player number must be betwen " + minAllowedPlayers
 							+ " and " + maxAllowedPlayers);
 		}
 		createPlayers(playersId);
 		missionHandler=new MissionHandler(players, missionFile);
 
 		worldMap = new WorldMap(neighboursFile, continentsFile, players);
 		bonusHandler = new BonusHandler(worldMap, players.size());
 		bonusHandler.calcBonusForF0(getActivePlayer().getNrOfProvinces()); // Instancieate
 																			// the
 																			// first
 																			// player's
 																			// bonus
 		setUpDeck();
 	}
 
 	private void setUpDeck() {
 		// SETTING UP DECK
 		ArrayList<String> provinces = new ArrayList<String>();
 		for (IProvince i : worldMap.getProvinces()) {
 			provinces.add(i.getId());
 		}
 		deck = Deck.getInstance();
 		deck.CreateCards(provinces, numbersOfWildCards);
 	}
 
 	private void createPlayers(String[] playersId) {
 		players = new ArrayList<Player>();
 		for (int i = 0; i < playersId.length; i++) {
 			players.add(new Player(i, playersId[i]));
 		}
 	}
 
 	@Override
 	public Player getActivePlayer() {
 		return players.get(phaseHandler.getActivePlayer());
 	}
 
 	/**
 	 * Places one unit in a province
 	 * 
 	 * @param province
 	 *            province to place the unit in
 	 */
 	private void placeBonusUnits(IProvince province) {
 		bonusHandler.placeBonusUnits(1, province);
 	}
 
 	@Override
 	public int getBonusUnitsLeft() {
 		return bonusHandler.getBonus();
 	}
 
 	@Override
 	public Phase getCurrentPhase() {
 		return phaseHandler.getPhase();
 	}
 
 	@Override
 	public ArrayList<Player> getPlayers() {
 		return players;
 	}
 
 	@Override
 	public ArrayList<IProvince> getGameProvinces() {
 		return worldMap.getProvinces();
 	}
 
 	@Override
 	public void handleProvinceEvent(IProvince newProvince) {
 		// TROOP REINFORCMENT PHASE 1, ONLY THE PLACEMENT
 		int bonus = bonusHandler.getBonus();
 		if (getCurrentPhase() == Phase.F1 && bonus > 0) {
 			// PUT A SINGEL UNIT ON THIS PROVINCE IF OWNED
 			if (worldMap.getOwner(newProvince.getId()) == getActivePlayer()) {
 				placeBonusUnits(newProvince);
 			}
 		}
 		// FIGHTING PHASE 2, FIGHT IF TWO PROVINCE CLICKED AND OWNED BY
 		// DIFFERENT PLAYER AND ATTACKING PROVINCE OWNED BY ME
 		else if (getCurrentPhase() == Phase.F2) {
 			handlePhaseF2(newProvince);
 		}// MOVING TROOPS IN PHASE 3
 		else if (getCurrentPhase() == Phase.F3 && !movedTroops) {
 			handlePhaseF3(newProvince);
 		}// Placing troops in build phase
 		else if (getCurrentPhase() == Phase.FBuild) {
 			if (worldMap.getOwner(newProvince.getId()) == getActivePlayer()
 					&& bonus > 0) {
 				placeBonusUnits(newProvince);
 			}
 		}
 	}
 
 	private void handlePhaseF2(IProvince newProvince) {
 		if (myProvince(newProvince.getId()) && newProvince.getUnits() > 1) {
 			if (oldProvince != null) {
 				oldProvince.setActive(false);
 			}
 			oldProvince = newProvince;
 			System.out.println("Moving from: " + oldProvince.getId());
 			oldProvince.setActive(true);
 
 		} else if (oldProvince != null) {
 			// FIGHT IF TWO PROVINCE CLICKED AND OWNED BY DIFFERENT PLAYER
 			// AND ATTACKING PROVINCE OWNED BY ME
 			if (checkProvinceOk(oldProvince, newProvince, false)) {
 				// saving second province to be used later after
 				// nbr of dices has been decided by the user
 				secondProvince = newProvince;
 				secondProvince.setActive(true);
 				pcs.firePropertyChange(ATTACK,
 						oldProvince.getUnits() - 1 >= 3 ? threeDices
 								: oldProvince.getUnits() - 1, secondProvince);
 				// battle(oldClickedProvince, newClickedProvince);
 			} else {
 				flushProvinces();
 			}
 		}
 		if (oldProvince == null) {
 			System.out.println("Moving from: -");
 		}
 	}
 
 	private void handlePhaseF3(IProvince newProvince) {
 		if (myProvince(newProvince.getId()) && oldProvince == null
 				&& newProvince.getUnits() > 1) {
 			oldProvince = newProvince;
 			oldProvince.setActive(true);
 		}
 
 		else if (oldProvince != null) {
 			if (checkProvinceOk(oldProvince, newProvince, true)) {
 				if (oldProvince.getUnits() > 1) {
 
 					secondProvince = newProvince;
 					secondProvince.setActive(true);
 					pcs.firePropertyChange(MOVEMENT, oldProvince.getUnits(), 1);
 				}
 			}
 		}
 	}
 
 	@Override
 	public void flushProvinces() {
 		if (oldProvince != null) {
 			oldProvince.setActive(false);
 		}
 		oldProvince = null;
 		if (secondProvince != null) {
 			secondProvince.setActive(false);
 		}
 		secondProvince = null;
 	}
 
 	// return if current player owns the province
 	private boolean myProvince(String province) {
 		return getActivePlayer() == worldMap.getOwner(province);
 	}
 
 	@Override
 	public void moveToProvince(int nrOfUnits) {
 		if (oldProvince.getUnits() - nrOfUnits > 0) {
 			oldProvince.moveUnits(nrOfUnits, secondProvince);
 			if (getCurrentPhase() == Phase.F3) {
 				movedTroops = true;
 			}
 		}
 		flushProvinces();
 	}
 
 	// checks the value of sameOwner
 	private boolean checkProvinceOk(IProvince from, IProvince to,
 			boolean sameOwner) {
 		if (worldMap.isNeighbours(from.getId(), to.getId())) {
 			if (sameOwner) {
 				return (worldMap.getOwner(from.getId()) == getActivePlayer())
 						&& (worldMap.getOwner(to.getId()) == getActivePlayer());
 			} else {
 				return (worldMap.getOwner(from.getId()) == getActivePlayer())
 						&& (worldMap.getOwner(to.getId()) != getActivePlayer());
 			}
 
 		}
 		return false;
 	}
 
 	@Override
 	public void battle(int nbrOfDice) {
 
 		// if (oldProvince.getUnits() > 1) {
 		attack(nbrOfDice, oldProvince, secondProvince);
 		if (secondProvince.getUnits() == 0) {
 			changeOwner();
 			if (firstProvinceConqueredThisTurn) {
 				getActivePlayer().addCard();
 				firstProvinceConqueredThisTurn = false;
 			}
 			pcs.firePropertyChange(CONQUER, oldProvince.getUnits(), ""
 					+ nbrOfDice);
 		} else if (oldProvince.getUnits() > 1) {
 			pcs.firePropertyChange(
 					AGAIN,
 					oldProvince.getUnits() - 1 >= 3 ? threeDices : oldProvince
 							.getUnits() - 1, 0);
 		} else {
 			flushProvinces();
 		}
 		// }
 	}
 
 	/*
 	 * Change the owner and adds a card if its the first province conquered this
 	 * turn.
 	 */
 	private void changeOwner() {
 		Player lostProvincePlayer = worldMap.getOwner(secondProvince.getId());
 		worldMap.changeOwner(secondProvince.getId(), getActivePlayer());
 
 		checkGameOver(lostProvincePlayer);
 	}
 
 	// playerlose or removeplayer first?
 	private void checkGameOver(Player gameOver) {
 		if (gameOver.getNrOfProvinces() == 0) {
 			int pos = players.indexOf(gameOver);
 			playerLose(gameOver);
<<<<<<< HEAD
 			if(gameMode==GameMode.SECRET_MISSION){
 				missionHandler.playerEliminated(gameOver);
 			}
=======
 			phaseHandler.removePlayer(pos);
>>>>>>> 5b422c6101b9df1f00f1299dfb1cd30c16adba95
 		}
 		if (players.size() == 1) {
 			win(players.get(0));
 		}
 		
 		if(gameMode == GameMode.SECRET_MISSION && missionHandler.winner(getActivePlayer(), worldMap.getPlayersContinents(getActivePlayer()))){
 			win(missionHandler.getWinner());
 		}
 	}
 
 	private void win(Player win) {
 		pcs.firePropertyChange(WIN, 0, win);
 	}
 
 	// also handles defeat of neutral players, 
 
 	private void playerLose(Player gameOver) {
 		gameOver.discard();
 		players.remove(gameOver);
 	}
 
 	private boolean attack(int offensiveDice, IProvince offensive,
 			IProvince defensive) {
 
 		int defensiveDice = defensive.getUnits() == 1 ? oneDice : twoDices;
 
 		int[] result = battle.doBattle(offensiveDice, defensiveDice);
 
 		offensive.removeUnits(result[0]);
 		defensive.removeUnits(result[1]);
 		return true;
 	}
 
 	@Override
 	public void handleCardEvent(ICard card) {
 		if (getCurrentPhase() == Phase.F1) {
 			ArrayList<String> names = eventHandler.handleCardEvent(card,
 
 			getActivePlayer());
 			// HAVE TO FIX BONUSES //
 			if (names != null) {
 				bonusHandler.calcBonusesFromCards(names, getActivePlayer());
 			}
 		}
 	}
 
 	@Override
 	public void surrender(boolean confirm) {
 		if (confirm) {
 			playerLose(getActivePlayer());
 			phaseHandler.surrender(players);
 			if (players.size() == 1) {
 				win(players.get(0));
 				return;
 			}
 			if (getCurrentPhase() == Phase.FBuild) {
 				bonusHandler.calcBonusForF0(getActivePlayer()
 						.getNrOfProvinces());
 			} else {
 				updateValues();
 			}
 			flushProvinces();
 		} else {
 			pcs.firePropertyChange(SURRENDER, true, false);
 		}
 	}
 
 	/*
 	 * Text taken from TurnAndPhase
 	 * 
 	 * Changing phase and then pokes on other methods.
 	 * 
 	 * ComputeBonusForF0 if if a new bonus shall be computed. ChangedPhase if a
 	 * change of phase has taken place. ComputeBonusForF1 if a new turn has
 	 * begun (and not in buildingphase = F0). DoNothing if phase didn't change.
 	 */
 	@Override
 	public void handlePhaseEvent() {
 		int bonus = bonusHandler.getBonus();
 		ResultType result = eventHandler.handlePhaseEvent(getActivePlayer(),
 				bonus, players);
 		if (result == ResultType.ComputeBonusForF0) {
 			bonusHandler.calcBonusForF0(getActivePlayer().getNrOfProvinces());
 		} else if (result == ResultType.ComputeBonusForF1) {
 			updateValues();
 		} else if (result == ResultType.DoNothing) {
 			if (bonus > 0) {
 				pcs.firePropertyChange(UNITS, true, false);
 			} else {
 				pcs.firePropertyChange(CARDS, true, false);
 			}
 		}
 		flushProvinces();// clean temps between turns and phases
 	}
 
 	private void updateValues() {
 		worldMap.updateBonus();
 		bonusHandler.calcBonusUnits(getActivePlayer());
 		firstProvinceConqueredThisTurn = true;
 		movedTroops = false;
 	}
 
 	@Override
 	public int getOwner(String provinceName) {
 		return worldMap.getOwner(provinceName).getId();
 	}
 
 	@Override
 	public void addListener(PropertyChangeListener listener) {
 		pcs.addPropertyChangeListener(listener);
 
 	}
 
 	@Override
 	public void addPlayerListener(List<PropertyChangeListener> list) {
 		for (int i = 0; i < list.size(); i++) {
 			players.get(i).addListener(list.get(i));
 		}
 	}
 }
