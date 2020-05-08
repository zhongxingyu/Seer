 package herbstJennrichLehmannRitter.engine.controller.impl;
 
 import herbstJennrichLehmannRitter.engine.controller.GameEngineController;
 import herbstJennrichLehmannRitter.engine.controller.WinAndLoseChecker;
 import herbstJennrichLehmannRitter.engine.enums.GameType;
 import herbstJennrichLehmannRitter.engine.exception.GameEngineException;
 import herbstJennrichLehmannRitter.engine.exception.GameEngineException.ENGINE_ERROR;
 import herbstJennrichLehmannRitter.engine.factory.GameCardFactory;
 import herbstJennrichLehmannRitter.engine.factory.PlayerFactory;
 import herbstJennrichLehmannRitter.engine.factory.impl.GameCardFactoryImpl;
 import herbstJennrichLehmannRitter.engine.factory.impl.PlayerFactoryImpl;
 import herbstJennrichLehmannRitter.engine.model.Card;
 import herbstJennrichLehmannRitter.engine.model.Data;
 import herbstJennrichLehmannRitter.engine.model.Player;
 import herbstJennrichLehmannRitter.engine.model.ResourceBuilding;
 import herbstJennrichLehmannRitter.engine.model.action.ComplexCardAction;
 import herbstJennrichLehmannRitter.engine.model.action.ResourceAction;
 import herbstJennrichLehmannRitter.engine.model.impl.DataImpl;
 import herbstJennrichLehmannRitter.engine.utils.MagicUtils;
 
 import java.util.Collection;
 
 /** Description of GameEngineControllerImpl Class
  *  This Class implements the GameEngineController.
  *  It starts the GameCardFactory and sets the default settings for tower, wall, ressource building and level
  *  Furthermore it creates the players, stops the game, discards cards, applies ressources on buildings and fills the
  *  hand deck.
  */
 
 //TODO: Test
 public class GameEngineControllerImpl implements GameEngineController {
 
 	private static boolean once = false;
 	private static final int DEFAULT_TOWER_POINTS = 25;
 	private static final int DEFAULT_WALL_POINTS = 10;
 	private static final int DEFAULT_RSC_BUILDING_LEVEL = 1;
 	private static final int DEFAULT_RSC_BUILDING_STOCK = 15;
 
 	private WinAndLoseChecker winAndLoseChecker;
 	private GameType gameType;
 	
 	private GameCardFactory gameCardFactory = new GameCardFactoryImpl();
 	private PlayerFactory playerFactory = new PlayerFactoryImpl();
 	
 	public GameEngineControllerImpl(GameCardFactory gameCardFactory) {
 		if (once) {
 			System.out.println("WARNING: a second GameEngineController has started. In tests this is okay");
 		}
 		once = true;
 		
 		this.gameCardFactory = gameCardFactory;
 	}
 	
 	@Override
 	protected void finalize() throws Throwable {
 		super.finalize();
 		once = false;
 	}
 
 	@Override
 	public GameCardFactory getGameCardFactory() {
 		return this.gameCardFactory;
 	}
 	
 	@Override
 	public Player createPlayer(String name, Collection<String> cardNames) {
 		
 		Collection<Card> cards = this.gameCardFactory.createCardsFromNames(cardNames);
 		
 		return this.playerFactory.createPlayer(name, cards, DEFAULT_TOWER_POINTS, DEFAULT_WALL_POINTS,
 				DEFAULT_RSC_BUILDING_LEVEL, DEFAULT_RSC_BUILDING_STOCK);
 	}
 
 	@Override
 	public void start(GameType gameType) {
 		this.gameType = gameType;
 		
 		switch (gameType) {
 		case TOWER_BUILDING:
 			this.winAndLoseChecker = new WinAndLoseTowerBuildingChecker();
 			break;
 
 		case COLLECTION_RAGE:
 			this.winAndLoseChecker = new WinAndLoseResourceRageChecker();
 			break;
 			
 		default:
 			throw new GameEngineException(ENGINE_ERROR.UNKNOWN_GAME_TYP);
 		}
 	}
 	
 	@Override
 	public void stop() {
 		this.winAndLoseChecker = null;
 	}
 	
 	private boolean isRunning() {
 		return this.winAndLoseChecker != null;
 	}
 	
 	@Override
 	public Data createDataForPlayer(Player player, Player enemy) {
 		return new DataImpl(player, this.playerFactory.createCopyForEnemy(enemy));
 	}
 	
 	private Player lastPlayerWhoGainedResources = null;
 	@Override
 	public void addResourcesToPlayer(Player player) {
 		if (player == this.lastPlayerWhoGainedResources) {
 			return;
 		}
 		
 		addResourcesToResourceBuilding(player.getMine());
 		addResourcesToResourceBuilding(player.getMagicLab());
 		addResourcesToResourceBuilding(player.getDungeon());
 		
 		this.lastPlayerWhoGainedResources = player;
 	}
 	
 	private void addResourcesToResourceBuilding(ResourceBuilding rb) {
 		rb.addStock(rb.getLevel());
 	}
 
 	@Override
 	public void playCard(Card card, Player player, Player enemyPlayer) {
 		if (!isRunning()) {
 			throw new GameEngineException(ENGINE_ERROR.NOT_RUNNING);
 		}
 		
 		if (!MagicUtils.canPlayerEffortCard(player, card)) {
 			throw new GameEngineException(ENGINE_ERROR.PLAYER_CANT_EFFORT_CARD);
 		}
 		
 		if (!ownsPlayerCard(card, player)) {
 			throw new GameEngineException(ENGINE_ERROR.PLAYER_DONT_OWN_CARD);
 		}
 		
 		applyCostFromCardOnPlayer(card, player);
 		applyResourceAction(card.getOwnResourceAction(), player);
 		applyResourceAction(card.getEnemyResourceAction(), enemyPlayer);
 		applyComplexCardAction(card.getComplexCardAction(), player, enemyPlayer);
 		
 		throwAwayCardAndRefillHandDeckIfNeeded(card, player);
 	}
 	
 	@Override
 	public void discardCard(Card card, Player player) {
 		if (!isRunning()) {
 			throw new GameEngineException(ENGINE_ERROR.NOT_RUNNING);
 		}
 		
 		if (!ownsPlayerCard(card, player)) {
 			throw new GameEngineException(ENGINE_ERROR.PLAYER_DONT_OWN_CARD);
 		}
 		
 		throwAwayCardAndRefillHandDeckIfNeeded(card, player);
 	}
 	
 	public void throwAwayCardAndRefillHandDeckIfNeeded(Card card, Player player) {
 		player.getDeck().discardCard(card);
		
 		if (!card.getCardAction().getPlayCards()) {
 			player.getDeck().pickCards(6);
 		}
 	}
 	
 	private void applyCostFromCardOnPlayer(Card card, Player player) {
 		player.getDungeon().reduceStock(card.getCostMonsters());
 		player.getMagicLab().reduceStock(card.getCostCrystal());
 		player.getMine().reduceStock(card.getCostBrick());
 	}
 	
 	private void applyResourceAction(ResourceAction ra, Player player) {
 
 		player.getMine().addStock(ra.getBrickEffect());
 		player.getMine().addLevel(ra.getMineLvlEffect());
 		
 		player.getDungeon().addStock(ra.getMonsterEffect());
 		player.getDungeon().addLevel(ra.getDungeonLvlEffect());
 		
 		player.getMagicLab().addStock(ra.getCrystalEffect());
 		player.getMagicLab().addLevel(ra.getMagicLabLvlEffect());
 		
 		player.getWall().addPoints(ra.getWallEffect());
 		player.getTower().addPoints(ra.getTowerEffect());
 		
 		player.getTower().applyDamage(player.getWall().applyDamage(ra.getDamage()));
 	}
 	
 	private void applyComplexCardAction(ComplexCardAction cca, Player player, Player enemy) {
 		if (cca == null) {
 			return;
 		}
 		
 		cca.applyActionOnPlayer(player, enemy);
 	}
 	
 	private boolean ownsPlayerCard(Card card, Player player) {
 		return player.getDeck().getAllCards().contains(card);
 	}
 	
 	@Override
 	public boolean hasPlayerWon(Player player, Player enemy) {
 		if (this.winAndLoseChecker.hasPlayerWon(player) || this.winAndLoseChecker.hasPlayerLost(enemy)) {
 			return true;
 		}
 		return false;
 	}
 	
 	@Override
 	public GameType getGameType() {
 		return this.gameType;
 	}
 }
