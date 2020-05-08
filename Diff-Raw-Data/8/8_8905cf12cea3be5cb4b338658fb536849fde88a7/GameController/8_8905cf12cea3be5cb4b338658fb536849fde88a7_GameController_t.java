 package se.chalmers.tda367.std.core;
 
 import de.lessvoid.nifty.input.NiftyInputEvent;
 import se.chalmers.tda367.std.core.events.WaveStartedEvent;
 import se.chalmers.tda367.std.core.factories.GameBoardFactory;
 import se.chalmers.tda367.std.core.factories.WaveFactory;
 import se.chalmers.tda367.std.core.tiles.IBuildableTile;
 import se.chalmers.tda367.std.core.tiles.TerrainTile;
 import se.chalmers.tda367.std.core.tiles.towers.ITower;
 import se.chalmers.tda367.std.utilities.BoardPosition;
 import se.chalmers.tda367.std.utilities.EventBus;
 import se.chalmers.tda367.std.utilities.Position;
 
 
 
 /**
  * The class that contains the game logic and controls the game.
  * @author Johan Andersson
  * @modified Johan Gustafsson
  * @date Mar 22, 2012
  */
 public class GameController {
 	
 	private Player player;
 	private GameBoard board;
 	private BuildController buildControl;
 	private WaveController waveControl;
 	private Properties prop = Properties.INSTANCE;
 	private int tileScale;
 	
 	private final GameBoardFactory boardFactory;
 	
 	private int releasedWaves = 0;
 	private int level;
 	
 	
 	/** Constructor for the GameController, requires a player and a board to work.
 	 * 
 	 * @param player - player playing the game.
 	 * @param board - board to play the game on.
 	 */
 	public GameController(Player player){
 		this.player = player;
 		this.level = 1;
 		tileScale = prop.getTileScale();
 		boardFactory = new GameBoardFactory();
 		
 		init();
 	}
 	
 	private void init(){
 		this.board = boardFactory.create(level);
 		buildControl = new BuildController(board, player);
 		waveControl = new WaveController(board, player);
 	}
 	
 	/**
 	 * Update the game to the next state.
 	 * @param delta - the amount of time in milliseconds from the previous update.
 	 */
 	public void updateGameState(final int delta) {
 		waveControl.updateWaveRelated(delta);
 	}
 	
 	/** 
 	 * Starts the next wave of enemies.
 	 */
 	public void nextWave(){
 		Wave wave = new WaveFactory().create(++releasedWaves);
 		waveControl.startWave(wave);
 		EventBus.INSTANCE.post(new WaveStartedEvent(releasedWaves));
 	}
 	
 	/** Builds a tower on the board.
 	 * 
 	 * @param tower - Tower to be built.
 	 * @param pos - Position to build upon.
 	 * @return - True if tower was build otherwise false
 	 */
 	public boolean buildTower(ITower tower, BoardPosition pos){
 		return buildControl.buildTower(tower, pos);
 	}
 	
 	/** Sells a tower if possible.
 	 * 
 	 * @param tower - Tower to be sold.
 	 * @param pos - Position on which the tower is built.
 	 * @return - True if tower is sold.
 	 */
 	public boolean sellTower(ITower tower, BoardPosition pos){
 		return buildControl.sellTower(tower, pos);
 	}
 	
 	/** Upgrades a tower if possible.
 	 * 
 	 * @param tower - Tower to be upgraded.
 	 * @return - True if tower was upgraded.
 	 */
 	public boolean upgradeTower(ITower tower){
 		return buildControl.upgradeTower(tower);
 	}
 
 	/** Tells if a player can afford to upgrade a tower.
 	 * 
 	 * @param tower - Tower considered to upgrade.
 	 * @return - True if player can afford upgrade.
 	 */
 	public boolean playerCanAffordUpgrade(ITower tower) {
 		return buildControl.playerCanAffordUpgrade(tower);
 	}
 	
 	/** Tells if a spot is buildable or not.
 	 * 
 	 * @param pos - Position to test buildability on.
 	 * @return - True if position is buildable on board.
 	 */
 	public boolean isBuildableSpot(BoardPosition pos) {
 		return buildControl.isBuildableSpot(pos);
 	}
 	
 	/** Tells if a player can afford a tower.
 	 * 
 	 * @param tower - Tower to test affordability on.
 	 * @return - True if player can afford upgrade.
 	 */
 	public boolean playerCanAffordTower(ITower tower) {
 		return buildControl.playerCanAffordTower(tower);
 	}
 	
 	/**
 	 *  Returns how many waves that have been released
 	 * 
 	 * @return number of waves released
 	 */
 	public int getWavesReleased() {
 		return releasedWaves;
 	}
 	
 	/**
 	 * Returns the current level.
 	 * @return the current level.
 	 */
 	public int getLevel() {
 		return this.level;
 	}
 
 	/**
 	 * @return the game board
 	 */
 	public GameBoard getGameBoard() {
 		return board;
 	}
 	
 	/**
 	 * Causes the player to move depending on the {@code MovementEnum} provided.
 	 * @param direction Enum to use for calculation.
 	 * @param delta time in milliseconds since last update.
 	 */
 	public void moveChar(MovementEnum direction, int delta) {
 		Position playerPos = player.getCharacter().getPos();
 		if(isAbleToWalkTo(direction.newPosition(playerPos, delta))) {
 			playerPos.copyFromPosition(direction.newPosition(playerPos, delta));
 		}
 	}
 	
 	/**
 	 * Method to check if a given position is a buildable or terrain tile.
 	 * This determines if player character can move to this position or not.
 	 * @param p position to check.
 	 * @return true if it's a {@code IBuildableTile} or {@code TerrainTile}.
 	 */
 	public boolean isAbleToWalkTo(Position p) {
 		//Calculate on which tile the position given is on.
 		int x = (int)(p.getX()/tileScale);
 		int y = (int)(p.getY()/tileScale);
		if(board.posOnBoard(x, y)) {
			if(board.getTileAt(x, y) instanceof IBuildableTile
					|| board.getTileAt(x, y) instanceof TerrainTile) {
				return true;
			}
 		}
 		return false;
 	}
 }
