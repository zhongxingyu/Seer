 package se.chalmers.dat255.risk.model;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.ArrayList;
 import java.util.List;
 
 import se.chalmers.dat255.risk.model.TurnAndPhaseManager.Phase;
 
 /**
  * The top game class. Controls flow between our lower classes, such as the
  * battle handler and the WorldMap.
  * 
  */
 
 public class Game implements IGame {
 	private ArrayList<Player> players;
 	private int startingTroopNr;
 	private WorldMap worldMap;
 	private EventHandler clickHandler;
 	private TurnAndPhaseManager phaseHandler;
 	private BonusHandler bonusHandler;
 	private BattleHandler battle;
 	private Deck deck;
 	private IProvince oldProvince, secondProvince;
 	private boolean movedTroops = false; // F3
 	private boolean firstProvinceConqueredThisTurn = true;
 	private PropertyChangeSupport pcs;
 	// CURRENT PHASE
 
 	private String continentsFile;
 	private String neighboursFile;
 
 	/**
 	 * Creates a new Game. lostArmies =
 	 * 
 	 * @param playersId
 	 *            The ids of the players
 	 */
 	public Game(String[] playersId, String neighboursFile, String continentsFile) {
 		battle = new BattleHandler();
 		this.neighboursFile = neighboursFile;
 		this.continentsFile = continentsFile;
 		pcs = new PropertyChangeSupport(this);
 		newGame(playersId);
 	}
 
 	@Override
 	public void newGame(String[] playersId) throws IllegalArgumentException {
 		phaseHandler = new TurnAndPhaseManager();
 		clickHandler = new EventHandler(phaseHandler);
 		bonusHandler = new BonusHandler(worldMap);
 		int noOfPlayers = playersId.length;
 		if (noOfPlayers > 6 || noOfPlayers < 2) {
 			throw new IllegalArgumentException(
 					"The player number must be betwen 2 and 6");
 		}
 
 		createPlayers(playersId);
 		players.get(phaseHandler.getActivePlayer()).setCurrent(true); // Player
 																		// one
 
		bonusHandler.calcStartBonus(players.size());
 
 		// ////////////////// ONLY FOR DEV //////////////////////////
 		// SETTING UP GAMEBOARD RULES AND CREATING PROVINCES
 		worldMap = new WorldMap(neighboursFile, continentsFile, players);
 
 		setUpDeck();
 
 		// refresh(); //BYTS MOT MOTSVARANDE I LIBGDX
 	}
 
 	private void setUpDeck() {
 		// SETTING UP DECK
 		ArrayList<String> provinces = new ArrayList<String>();
 		for (IProvince i : worldMap.getProvinces()) {
 			provinces.add(i.getId());
 		}
 		deck = Deck.getInstance();
 		deck.CreateCards(provinces, 6);// H�rdkodat antal wildcard
 	}
 
 	private void createPlayers(String[] playersId) {
 		players = new ArrayList();
 		for (int i = 0; i < playersId.length; i++) {
 			players.add(new Player(i, playersId[i]));
 		}
 	}
 
 	/*
 	 * private void calcStartBonus() { // INITIALIZING STARTING NUMBER OF TROOPS
 	 * startingTroopNr = 50 - players.size() * 5;
 	 * 
 	 * // /////////////////// ONLY FOR DEV /////////////////////////// // bonus
 	 * = startingTroopNr - getActivePlayer().getNrOfProvinces(); bonus = 3; }
 	 */
 
 	/*
 	 * Method for changing the state of the game to the next state if it should
 	 * be changed.
 	 */
 	/*
 	 * private int changePhase() { return } <<<<<<< HEAD =======
 	 */
 
 	@Override
 	public Player getActivePlayer() {
 		return players.get(phaseHandler.getActivePlayer());
 	}
 
 	@Override
 	public void dealCard() {
 		getActivePlayer().addCard();
 	}
 
 	/*
 	 * @Override public void calcBonusUnits() { int provinces =
 	 * getActivePlayer().getNrOfProvinces(); if (provinces <= 9) { this.bonus =
 	 * 3; } else { this.bonus = provinces / 3; }
 	 * 
 	 * this.bonus += worldMap.getBonus(getActivePlayer());
 	 * 
 	 * }
 	 */
 	/**
 	 * Method for placing the amount of units the player chooses the place on
 	 * the province the player chooses to place them.
 	 * 
 	 * @param units
 	 *            the number of units being placed
 	 */
 	private void placeBonusUnits(int units, IProvince province) {
 		bonusHandler.placeBonusUnits(units, province);
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
 	public void handleProvinceClick(IProvince newProvince) {
 		// TROOP REINFORCMENT PHASE 1, ONLY THE PLACEMENT
 		int bonus = bonusHandler.getBonus();
 		if (getCurrentPhase() == Phase.F1 && bonus > 0) {
 			// PUT A SINGEL UNIT ON THIS PROVINCE IF OWNED
 			if (worldMap.getOwner(newProvince.getId()) == getActivePlayer()) {
 				placeBonusUnits(1, newProvince);
 			}
 		}
 		// FIGHTING PHASE 2, FIGHT IF TWO PROVINCE CLICKED AND OWNED BY
 		// DIFFERENT PLAYER AND ATTACKING PROVINCE OWNED BY ME
 		else if (getCurrentPhase() == Phase.F2) {
 			if (myProvince(newProvince.getId())) {
 				if (oldProvince != null) {
 					oldProvince.setActive(false);
 				}
 				oldProvince = newProvince;
 				System.out.println("Moving from: " + oldProvince.getId());
 				oldProvince.setActive(true);
 
 			}
 
 			else if (oldProvince != null) {
 				// FIGHT IF TWO PROVINCE CLICKED AND OWNED BY DIFFERENT PLAYER
 				// AND ATTACKING PROVINCE OWNED BY ME
 				if (checkProvinceOk(oldProvince, newProvince, false)) {
 					// saving second province to be used later after
 					// nbr of dices has been decided by the user
 					secondProvince = newProvince;
 					secondProvince.setActive(true);
 					pcs.firePropertyChange("Attack", oldProvince,
 							secondProvince);
 					// battle(oldClickedProvince, newClickedProvince);
 				} else {
 					flushTemps();
 				}
 			}
 			if (oldProvince == null) {
 				System.out.println("Moving from: -");
 			}
 
 		}
 
 		// MOVING TROOPS IN PHASE 3
 		else if (getCurrentPhase() == Phase.F3) {
 			if (myProvince(newProvince.getId()) && oldProvince == null) {
 				oldProvince = newProvince;
 				oldProvince.setActive(true);
 			}
 
 			else if (oldProvince != null) {
 				if (checkProvinceOk(oldProvince, newProvince, true)) {
 					secondProvince = newProvince;
 					secondProvince.setActive(true);
 					pcs.firePropertyChange("Movement", oldProvince.getUnits(),
 							1);
 					// MAY
 					// BE
 					// INVALID
 					// INPUT,
 					// THEN
 					// NOTHING
 					// WILL
 					// HAPPEN
 				}
 			}
 		}
 		// Placing troops in build phase
 		else if (getCurrentPhase() == Phase.FBuild) {
 			if (worldMap.getOwner(newProvince.getId()) == getActivePlayer()
 					&& bonus > 0) {
 				placeBonusUnits(1, newProvince);
 				System.out.print("Current player active is player "
 						+ phaseHandler.getActivePlayer() + "\n");
 			}
 		}
 
 	}
 
 	/*
 	 * TODO its bad code to make something null. perhaps we should think of
 	 * another way of storing and flushing our provinces
 	 */
 	private void flushTemps() {
 		if (oldProvince != null) {
 			oldProvince.setActive(false);
 		}
 		oldProvince = null;
 		if (secondProvince != null) {
 			secondProvince.setActive(false);
 		}
 		secondProvince = null;
 		/*
 		 * card1=null; card2=null;
 		 */
 	}
 
 	// return if current player owns the province
 	private boolean myProvince(String province) {
 		return getActivePlayer() == worldMap.getOwner(province);
 	}
 
 	@Override
 	public void moveToProvince(int nrOfUnits) {
 		if (oldProvince.getUnits() - nrOfUnits > 0) {
 			System.out.println("" + oldProvince.getUnits()
 					+ " units moved from " + oldProvince.getId() + " to "
 					+ secondProvince.getId());
 			oldProvince.moveUnits(nrOfUnits, secondProvince);
 		}
 		flushTemps();
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
 
 		if (oldProvince.getUnits() > 1) {
 			attack(nbrOfDice, oldProvince, secondProvince);
 			if (secondProvince.getUnits() == 0) {
 				changeOwner();
 			} else if (oldProvince.getUnits() > 1) {
 				pcs.firePropertyChange("Again?", oldProvince, 1);
 			} else {
 				flushTemps();
 			}
 		}
 	}
 
 	/*
 	 * Change the owner and adds a card if its the first province conquered this
 	 * turn.
 	 */
 	private void changeOwner() {
 		Player lostProvincePlayer = worldMap.getOwner(secondProvince.getId());
 		worldMap.changeOwner(secondProvince.getId(), getActivePlayer());
 		pcs.firePropertyChange("Movement", oldProvince.getUnits(), 1);
 		if (firstProvinceConqueredThisTurn) {
 			getActivePlayer().addCard();
 			firstProvinceConqueredThisTurn = false;
 		}
 		checkGameOver(lostProvincePlayer);
 	}
 
 	private void checkGameOver(Player gameOver) {
 		if (gameOver.getNrOfProvinces() == 0) {
 			playerLoose(gameOver);
 			if (players.size() == 1) {
 
 			}
 		}
 	}
 
 	private void win(Player win) {
 		pcs.firePropertyChange("Win", 0, win);
 	}
 
 	// Remove player from list and changes the turn of the players with "higher"
 	// turn than the player who has lost
 	private void playerLoose(Player gameOver) {
 		players.remove(gameOver.getId());
 		for (int i = gameOver.getId(); i < players.size(); i++) {
 			players.get(i).setTurn(i);
 		}
 	}
 
 	private boolean attack(int offensiveDice, IProvince offensive,
 			IProvince defensive) {
 
 		int defensiveDice = defensive.getUnits() == 1 ? 1 : 2;
 
 		int[] result = battle.doBattle(offensiveDice, defensiveDice);
 
 		offensive.removeUnits(result[0]);
 		defensive.removeUnits(result[1]);
 		return true;
 	}
 
 	@Override
 	public void handleCardEvent(ICard card) {
 		ArrayList<String> names = clickHandler.handleCardEvent(card,
 				getActivePlayer());
 		// HAVE TO FIX BONUSES //
		bonusHandler.calcProvinceBonusesFromCards(names, getActivePlayer());
 	}
 
 	/*
 	 * Text taken from TurnAndPhase
 	 * 
 	 * Changing phase and then pokes on other methods.
 	 * 
 	 * 2 if if a new bonus shall be computed. == 0 for F0 1 if a change of phase
 	 * has taken place. 0 if a new turn has begun. -1 if phase didn't change.
 	 */
 	@Override
 	public void handlePhaseEvent() {
 		int bonus = bonusHandler.getBonus();
 		int result = clickHandler.handlePhaseClick(getActivePlayer(), bonus,
 				players);
 		if (result == 2) {
 			System.out.println("PhaseHandler: New active player "
 					+ phaseHandler.getActivePlayer());
 			System.out.println("Game: New active player "
 					+ getActivePlayer().getId());
 			bonus = startingTroopNr - getActivePlayer().getNrOfProvinces();
 			bonusHandler.calcBonusForF0(getActivePlayer().getNrOfProvinces());
 		} else if (result == 0) {
 			worldMap.updateBonus();
			bonusHandler.calcBonusUnits(getActivePlayer());
 			firstProvinceConqueredThisTurn = true;// didn't see a reset of this
 													// elsewhere
 			// so i added one
 		}
 		flushTemps();// clean temps between turns and phases
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
