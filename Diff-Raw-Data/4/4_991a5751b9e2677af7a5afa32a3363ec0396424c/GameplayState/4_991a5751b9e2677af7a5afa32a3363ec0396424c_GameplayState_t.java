 package game;
 
 import map.Cell;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.Music;
 
 import sounds.SoundGroup;
 import utils.MapLoader;
 import entities.players.Player;
 import game.config.Config;
 
 public class GameplayState extends BasicGameState {
 	
 	private final int stateID;
 	private Cell currentCell;
 	private Player player;
 	private Music music;
 	SoundGroup footsteps;
 	  
     GameplayState(int stateID) {
        this.stateID = stateID;
     }
   
     @Override
     public int getID() {
         return stateID;
     }
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame sbg)
 			throws SlickException {
 		//map loading goes here. Needs a better home
 		//method needed to load all maps into their correct index in the array
		MapLoader.setDimensions(2,3);
 		MapLoader.loadMap("data/testmap.tmx", 0, 0);
 		MapLoader.loadMap("data/testmap2.tmx", 1, 0);
 		MapLoader.loadMap("data/testmap3.tmx", 0, 1);
		MapLoader.loadMap("data/testmap4.tmx", 0, 2);
 		//set initial map
 		currentCell = MapLoader.setCurrentCell(0,0);
 		
 		//create player
 		player = new Player(currentCell,new Rectangle(2,2,1,1), 100);
 		
 		//audio
 		music = new Music("data/sounds/theme.ogg", true);
 		music.play(1.0f, 0.05f);
 		footsteps = new SoundGroup("grass"); // choose: grass, gravel
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
 			throws SlickException {  
 		currentCell.render(-Config.getTileSize(),-Config.getTileSize());
 		player.render();
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sbg, int delta)
 			throws SlickException {
 		//update map
 		currentCell = MapLoader.getCurrentCell();
 		
 		//check input
 		Input input = gc.getInput();
 		if (input.isKeyDown(Input.KEY_ESCAPE)){
 			music.release();
 			player.stop_sounds();
 			footsteps.stopSounds();
 			gc.exit();
 		}
 		
 		//update player
 		player.update(input, delta);
 		
 		//update sounds
 		footsteps.playRandom(gc, player);
 	}
 
 	
 }
