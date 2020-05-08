 package se.chalmers.dat255.risk.model;
 
 import java.util.ArrayList;
 
 import se.chalmers.dat255.risk.model.TurnAndPhaseManager.Phase;
 import se.chalmers.dat255.risk.view.resource.Resource;
 
 /**
  * The top game class. Controls flow between our lower classes, such as the
  * battle handler and the WorldMap.
  * 
  */
 
 public class Game implements IGame {
 	private Player[] players;
 	private int startingTroopNr;
 	// private int currentPhase;
 	private WorldMap worldMap;
 	private ClickHandler clickHandler;
 	private TurnAndPhaseManager phaseHandler;
 	private int bonus;
 	private BattleHandler battle;
 	private Deck deck;
 	private IProvince oldClickedProvince = null;
 	private boolean movedTroops = false; // F3
 	private boolean firstProvinceConqueredThisTurn = true;
 
 	private ICard card1 = null;
 	private ICard card2 = null;
 
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
 		// deck=Deck.getInstance(new ArrayList<String>().add(new
 		// Province("A").getId()), "5");//hårdkodat
 		battle = new BattleHandler();
 		this.neighboursFile = neighboursFile;
 		this.continentsFile = continentsFile;
 		newGame(playersId);
 	}
 
 	/**
 	 * Method for changing the state of the game to the next state if it should
 	 * be changed.
 	 */
 	public int changePhase() {
 		return clickHandler.handlePhaseClick(getActivePlayer(), bonus, players);
 	}
 
 	/**
 	 * OBS OBS OBS OBS Inte alls som den borde va i nul�get. Inmatning av
 	 * antal hindrar fortsatt utveckling
 	 */
 
 	@Override
 	public boolean attack(int offensiveDice, IProvince offensive,
 			IProvince defensive) {
 		// TODO decide number of attackers
 		// check if ok in another method
 		// Counts the number of defending units
 
 		/*
 		 * Hur många tärningar man väljer att slå komer från kontrollern,
 		 * och där borde det vara omöjligt att välja fler än man kan. Så
 		 * kontrollern kommer att säga attack med hur många tärningar spearen
 		 * vill. Eller tycker ni inte det låter rimligt? Linnea
 		 */
 
 		int defensiveDice = defensive.getUnits() == 1 ? 1 : 2;
 
 		int[] result = battle.doBattle(offensiveDice, defensiveDice);
 
 		offensive.removeUnits(result[0]);
 		defensive.removeUnits(result[1]);
 		return true;
 	}
 
 	@Override
 	public Player getActivePlayer() {
 	//	System.out.println("Current turn: " + phaseHandler.getActivePlayer());
 		return players[phaseHandler.getActivePlayer()];
 	}
 
 	@Override
 	public void dealCard() {
 		getActivePlayer().addCard();
 	}
 
 	@Override
 	public void calcBonusUnits() {
 		int provinces = getActivePlayer().getNrOfProvinces();
 		if (provinces <= 9) {
 			this.bonus = 3;
 		} else {
 			this.bonus = provinces / 3;
 		}
 		this.bonus += worldMap.getBonus(getActivePlayer());
 	}
 
 	@Override
 	public void placeBonusUnits(int units, IProvince province) {
 		// Maybe private function??
 		province.addUnits(units);
 		bonus = bonus - units;
 	}
 
 	@Override
 	public int getBonusUnitsLeft() {
 		return bonus;
 	}
 
 	@Override
 	public void newGame(String[] playersId) throws IllegalArgumentException {
 		phaseHandler=new TurnAndPhaseManager();
 		clickHandler=new ClickHandler(phaseHandler);
 
 		
 		int noOfPlayers = playersId.length;
 		if (noOfPlayers > 6 || noOfPlayers < 2) {
 			throw new IllegalArgumentException(
 					"The player number must be betwen 2 and 6");
 		}
 
 		// THE PLAYERS
 		players = new Player[noOfPlayers];
 		for (int i = 0; i < noOfPlayers; i++) {
 			players[i] = new Player(i, playersId[i]);
 		}
 		// SETTING PHASE AND TURN
 	//	currentPhase = Phase.FBuild;
 	//	activePlayer = 0;
 		players[phaseHandler.getActivePlayer()].setCurrent(true); // Player one knows it�s his
 												// turn
 
 		// INITIALIZING STARTING NUMBER OF TROOPS
 		switch (players.length) {
 		case 2:
 			startingTroopNr = 40;
 			break;
 		case 3:
 			startingTroopNr = 35;
 			break;
 		case 4:
 			startingTroopNr = 30;
 			break;
 		case 5:
 			startingTroopNr = 25;
 			break;
 		case 6:
 			startingTroopNr = 20;
 			break;
 		}
 		bonus = startingTroopNr - getActivePlayer().getNrOfProvinces();
 
 		// SETTING UP GAMEBOARD RULES AND CREATING PROVINCES
 		worldMap = new WorldMap(neighboursFile, continentsFile, players);
 
 		// SETTING UP DECK
 		ArrayList<String> provinces = new ArrayList<String>();
 		for (IProvince i : worldMap.getProvinces()) {
 			provinces.add(i.getId());
 		}
 		deck = Deck.getInstance();
 		deck.CreateCards(provinces, 6);// H�rdkodat antal wildcard
 
 		// refresh(); //BYTS MOT MOTSVARANDE I LIBGDX
 	}
 
 	@Override
 	public Phase getCurrentPhase() {
 		// TODO Auto-generated method stub
 		return phaseHandler.getPhase();
 	}
 
 	@Override
 	public Player[] getPlayer() {
 		// TODO Auto-generated method stub
 		return players;
 	}
 
 	@Override
 	public ArrayList<IProvince> getGameProvinces() {
 		// TODO Auto-generated method stub
 		return worldMap.getProvinces();
 	}
 
 	@Override
 	public void handleProvinceClick(IProvince newClickedProvince) {
 		// TODO Auto-generated method stub
 
 		// TROOP REINFORCMENT PHASE 1, ONLY THE PLACEMENT
 		if (getCurrentPhase() == Phase.F1 && bonus > 0) {
 			// PUT A SINGEL UNIT ON THIS PROVINCE IF OWNED
 			if (worldMap.getOwner(newClickedProvince.getId()) == getActivePlayer()) {
 				placeBonusUnits(1, newClickedProvince);
 			}
 		}
 		// FIGHTING PHASE 2
 		else if (getCurrentPhase() == Phase.F2) {
 			if (oldClickedProvince != null) {
 				// FIGHT IF TWO PROVINCE CLICKED AND OWNED BY DIFFERENT PLAYER
 				// AND ATTACKING PROVINCE OWNED BY ME
 				if (checkProvinceOk(oldClickedProvince, newClickedProvince,
 						false)) {
 					battle(oldClickedProvince, newClickedProvince);
 				}
 
 				oldClickedProvince = null;
 			} else {
 				oldClickedProvince = newClickedProvince;
 
 			}
 		}
 		// MOVING TROOPS IN PHASE 3
 		else if (getCurrentPhase() == Phase.F3) {
 			if (oldClickedProvince != null) {
 				if (checkProvinceOk(oldClickedProvince, newClickedProvince,
 						true)) {
 					// DONT FORGET TO ADD POP-UP
 					moveToProvince(1, oldClickedProvince, newClickedProvince);// MAY
 																				// BE
 																				// INVALID
 																				// INPUT,
 																				// THEN
 																				// NOTHING
 																				// WILL
 																				// HAPPEN
 				}
 			} else {
 				oldClickedProvince = newClickedProvince;
 			}
 		}
 		// Placing troops in build phase
 		else if (getCurrentPhase() == Phase.FBuild) {
 			if (worldMap.getOwner(newClickedProvince.getId()) == getActivePlayer()
 					&& bonus > 0) {
 				placeBonusUnits(1, newClickedProvince);
 				System.out.print("Current player active is player " + phaseHandler.getActivePlayer() + "\n");
 			}
 		}
 	}
 
 	private void moveToProvince(int nrOfUnits, IProvince from, IProvince goTo) {
		if (from.getUnits() - nrOfUnits  > 0) {
 			from.moveUnits(nrOfUnits, goTo);
 		}
 	}
 
 	private boolean checkProvinceOk(IProvince from, IProvince to,
 			boolean sameOwner) {
 		if (from != to) {
 			if (worldMap.isNeighbours(from.getId(), to.getId())) {
 				if (sameOwner) {
 					return (worldMap.getOwner(from.getId()) == getActivePlayer())
 							&& (worldMap.getOwner(to.getId()) == getActivePlayer());
 				} else {
 					return (worldMap.getOwner(from.getId()) == getActivePlayer())
 							&& (worldMap.getOwner(to.getId()) != getActivePlayer());
 				}
 			}
 		}
 		return false;
 	}
 
 	private void battle(IProvince from, IProvince to) {
 		// POP-UP for nr of Offensive dice, untill implemented you may only
 		// attack with one
 		int nrOfDices = 1;
 		// if(nrofdice>from.getUnits())
 		if (from.getUnits() > 1) {
 			attack(nrOfDices, from, to);
 			if (to.getUnits() == 0) {
 				worldMap.changeOwner(to.getId(), getActivePlayer());
 				// TODO move attacking units into 'defensive'
				moveToProvince(1, from, to);
 				if (firstProvinceConqueredThisTurn) {
 					getActivePlayer().addCard();
 					firstProvinceConqueredThisTurn = false;
					System.out.println("Du fick ett kort");
 				}
 			}
 		}
 	}
 
 	@Override
 	public void handleCardClick(ICard card) {
 		// TODO Auto-generated method stub
 		if (card2 != null) {
 			getActivePlayer().exchangeCard((Card) card1, (Card) card2,
 					(Card) card);
 			// GIVE BONUS
 			// Check if extra bonus from owned province cards
 			card1 = null;
 			card2 = null;
 		} else {
 			if (card1 == null) {
 				card1 = card;
 			} else {
 				card2 = card;
 			}
 		}
 	}
 
 	@Override
 	public void handlePhaseClick() {
 		// TODO Auto-generated method stub
 		int result = changePhase();
 		if(result == 2){
 			System.out.println("PhaseHandler: New active player " + phaseHandler.getActivePlayer());
 			System.out.println("Game: New active player " + getActivePlayer().getId());
 			bonus = startingTroopNr - getActivePlayer().getNrOfProvinces();
 			
 		}
 		else if(result == 0){
 			calcBonusUnits();
 		}
 	}
 
 	@Override
 	public Phase getPhase() {
 		// TODO Auto-generated method stub
 		return phaseHandler.getPhase();
 
 	}
 
 	@Override
 	public int getOwner(String provinceName) {
 		// TODO Auto-generated method stub
 		return worldMap.getOwner(provinceName).getId();
 	}
 
 }
