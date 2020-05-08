 package controller;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.util.ArrayList;
 import java.util.Random;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.Music;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Sound;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.tiled.TiledMap;
 
 import utils.BlockMapUtils;
 import utils.WorldUtils;
 import view.CandyMonsterView;
 import view.InGameView;
 import view.ItemView;
 import view.MoveableBoxView;
 import view.SpikesView;
 import model.Character;
 import model.EndOfLevel;
 import model.FixedPosition;
 import model.Game;
 import model.InGame;
 import model.Item;
 import model.PauseMenu;
 
 public class InGameController extends BasicGameState {
 	private InGame inGame;
 	private InGameView inGameView;
 	private CharacterController characterController;
 	private PlayerController playerController;
 	private WorldController worldController;
 	private StatusBarController statusBarController;
 	private BlockMapController blockMapController;
 	private ArrayList <CandyMonsterController> candyMonsterControllers;
 	private ArrayList <ItemController> itemControllers;
 	private ArrayList <SpikesController> spikesControllers;
 	private ArrayList <MoveableBoxController> moveableBoxControllers;
 	private ArrayList <Item> itemList; //used when checking if pickedUp in update
 	private Item lastHeldItem;
 	private StateBasedGame sbg;
 	private GameController gameController;
 	private GameContainer gameContainer;
 	//should be based on the frame update (delta or something like that)
 	private float timeStep = 1.0f / 60.0f;
 	private int velocityIterations = 6;
 	private int positionIterations = 2;
 	private Sound happySound;
 	private Sound hurtSound;
 	
 	
 	public InGameController(GameController gameController) {
 		this.gameController = gameController;
 		this.playerController = new PlayerController(this);
 		this.inGame = new InGame(playerController.getPlayer());
 	}
 
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame sbg)
 			throws SlickException {
 		this.sbg = sbg;
 		this.statusBarController = new StatusBarController(this);
 		this.happySound = new Sound("music/happy0.wav");
 		this.hurtSound = new Sound("music/aj0.wav");
 	}
 
 	@Override
 	public void enter(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		super.enter(container, game);
 		gameController.getInGameMusic().setVolume(1f);
 		if (!inGame.isPaused()) {
 			if (inGame.isNewGame()) {
 				inGame.resetLevel();
 				playerController.getPlayer().reset();
 				inGame.setNewGame(false);
 			}else {
 				this.inGame.levelUp();
 			}
 			if(this.gameController.getGame().isMusicOn()) {
 				gameController.getInGameMusic().loop();
 			}
 			
 			this.inGame.reset();
 			this.inGame.setGameOver(false);
 			this.candyMonsterControllers = new ArrayList<CandyMonsterController>();
 			this.itemControllers = new ArrayList<ItemController>();
 			this.spikesControllers = new ArrayList<SpikesController>();
 			this.moveableBoxControllers = new ArrayList<MoveableBoxController>();
 
 			int nbrOfVersions = inGame.getNbrOfFiles(this.inGame.getLevel());
 			System.out.println("nbr of versions: " + nbrOfVersions + "of the level: " + this.inGame.getLevel());
 			//Get a new level, randomize between different level versions (i.e. there are many level 1 to randomize from)
 			this.blockMapController = new BlockMapController(this, new TiledMap(BlockMapUtils.getTmxFile(this.inGame.getLevel(), inGame.randomizeVersion(nbrOfVersions))));
 			/*Create candy monster and its items*/
 			for (int i = 0; i < blockMapController.getCandyMonsterMap().getBlockList().size(); i++){
 				this.candyMonsterControllers.add(new CandyMonsterController(this, i)); 
 				this.itemControllers.add(new ItemController(this, i));
 			}
 
 			this.characterController = new CharacterController(this);
 
 			/*Create spikes*/
 			for (int i = 0; i < blockMapController.getSpikesMap().getBlockList().size(); i++){
 				this.spikesControllers.add(new SpikesController(this, i));
 			}
 			for (FixedPosition pos : blockMapController.getBlockMapView().getMoveableBoxMap().getBlockList()) {
 				this.moveableBoxControllers.add(new MoveableBoxController(this, pos));
 			}
 
 			//temporarily store the MoveableBoxViews in a list
 			ArrayList<MoveableBoxView> tmpMoveableBoxViewList = new ArrayList<MoveableBoxView>();
 			for (MoveableBoxController moveableBoxController : moveableBoxControllers) {
 				tmpMoveableBoxViewList.add(moveableBoxController.getMoveableBoxView());
 			}
 			
 			//temporarily store the CandyMonsterViews in a list
 			ArrayList<CandyMonsterView> tmpCandyMonsterViewList = new ArrayList<CandyMonsterView>();
 			for (CandyMonsterController candyMonsterController : candyMonsterControllers) {
 				tmpCandyMonsterViewList.add(candyMonsterController.getCandyMonsterView());
 			}
 			
 			//temporarily store the ItemViews in a list
 			ArrayList<ItemView> tmpItemViewList = new ArrayList<ItemView>();
 			for (ItemController itemController : itemControllers) {
 				tmpItemViewList.add(itemController.getItemView());
 			}
 			
 			//temporarily store the SpikesViews in a list
 			ArrayList<SpikesView> tmpSpikesViewList = new ArrayList<SpikesView>();
 			for (int i = 0; i < spikesControllers.size(); i++) {
 				tmpSpikesViewList.add(spikesControllers.get(i).getSpikesView());
 			}
 			
 			
 			this.worldController = new WorldController(this, characterController.getCharacterView(),
 					tmpMoveableBoxViewList,
 					tmpCandyMonsterViewList,
 					tmpItemViewList,
 					tmpSpikesViewList);
 			inGame.setWorld(worldController.getWorld());
 			this.inGameView = new InGameView(inGame, worldController.getWorldView(), statusBarController.getStatusBarView(), 
 					characterController.getCharacterView(), tmpMoveableBoxViewList, tmpSpikesViewList, this.inGame.getLevel());
 
 			itemList = new ArrayList<Item>();
 			for (ItemController itemController : itemControllers) {
 				itemList.add(itemController.getItem());
 			}
 		} else {
 			this.inGame.setPaused(false);
 		}
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
 			throws SlickException {
 		this.inGameView.render(gc, sbg, g);
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sbg, int delta)
 			throws SlickException {
 		this.gameContainer = gc;
 		//change the time for the game and the character
 		this.inGame.setTime(this.inGame.getTime()-(delta/1000f));
 		this.characterController.getCharacter().setTimeSinceHit(this.characterController.getCharacter().getTimeSinceHit() + delta/1000f);
 		//check if the player is hit by spikes
 		if(this.characterController.getCharacter().isOnSpikes() && this.characterController.getCharacter().getTimeSinceHit() > 1) {
 			this.hurtSound.play(); //plays hurt sound 
 			this.playerController.getPlayer().loseOneLife();
 			this.characterController.getCharacter().setTimeSinceHit(0);
 		}
 		
 		if (this.characterController.getCharacter().getTimeSinceHit() <= 1) {
 			characterController.getCharacterView().animateBlinking();
 		} else {
			characterController.getCharacterView().getAnimation();
 		}
 		//update the timeBar
 		this.statusBarController.getStatusBarView().updateTimeBar(this.inGame.getLevelTime(), this.inGame.getTime());
 		//check if the game is over
 		if (inGame.checkIfGameIsOver(itemControllers.size())) {
 			gameController.getInGameMusic().stop();
 			sbg.enterState(EndOfLevel.STATE_ID);
 		}
 		//check key presses
 		characterController.keyPressedUpdate(gc);
 		//simulate the JBox2D world, timeStep --> delta
 		if(delta > 0) {
 			this.timeStep = (float) delta / 1000f * 4; //4 is for getting a good speed
 		}
 		worldController.getWorldView().getjBox2DWorld().step(timeStep, velocityIterations, positionIterations);
 		
 		characterController.getCharacter().setX(WorldUtils.meter2Pixel(
 				inGameView.getCharacterView().getCharacterBody().getPosition().x) -
 				Character.RADIUS);
 		characterController.getCharacter().setY(WorldUtils.meter2Pixel(
 				inGameView.getCharacterView().getCharacterBody().getPosition().y) -
 				Character.RADIUS);
 		
 		for (int i = 0; i < itemList.size(); i++) {
 			if (itemList.get(i).isPickedUp()) {
 				itemList.get(i).setX((int)characterController.getCharacterView().getSlickShape().getX() + Character.RADIUS);
 				itemList.get(i).setY((int)characterController.getCharacterView().getSlickShape().getY() + Character.RADIUS);
 			}
 		}
 		
 		worldController.updateCharacterSlickShape();
 		worldController.updateItemSlickShapePosition(worldController.getItemViewList(), characterController.getCharacterView());
 		characterController.getCharacter().setX((int)characterController.getCharacterView().getSlickShape().getX());
 		characterController.getCharacter().setY((int)characterController.getCharacterView().getSlickShape().getY());
 		for (int i = 0; i < moveableBoxControllers.size(); i++) {
 			moveableBoxControllers.get(i).getMoveableBox().setX(WorldUtils.meter2Pixel(
 					moveableBoxControllers.get(i).getMoveableBoxView().getBoxBody().getPosition().x));
 			moveableBoxControllers.get(i).getMoveableBox().setY(WorldUtils.meter2Pixel(
 					moveableBoxControllers.get(i).getMoveableBoxView().getBoxBody().getPosition().y));
 		}
 		
 	}
 
 
 	@Override
 	public void keyPressed (int key, char c) {
 		if (key == Input.KEY_DOWN) {
 			if (characterController.findItemToPickUp()!= null && !characterController.getCharacter().isHoldingItem(itemList)) {
 				characterController.getCharacter().pickUpItem(characterController.findItemToPickUp());
 			} else if (characterController.getCharacter().isHoldingItem(itemList) && 
 					characterController.getCharacterView().getCharacterBody().getLinearVelocity().y == 0) {
 				lastHeldItem = characterController.getCharacter().getHeldItem();	
 				characterController.getCharacter().dropDownItem(characterController.getCharacter().getHeldItem());
 				this.itemControllers.get(lastHeldItem.CANDY_NUMBER).uppdateItemShape();
 				if(candyMonsterControllers.get(lastHeldItem.CANDY_NUMBER).isDroppedOnMonster(lastHeldItem) && gameController.getGame().isSoundOn()) {
 					this.happySound.play();
 				}
 			}
 		}
 		if (key == Input.KEY_UP) {
 			characterController.tryToJumpCharacter();
 		}
 		if (key == Input.KEY_ESCAPE){
 			try {
 				inGame.setPaused(true);
 				inGameView.createPauseImage();
 				gameController.getGameView().setPauseImage(inGameView.getPauseImage());
 			} catch (SlickException e) {
 				System.out.println("ERROR: No image could be created");
 				e.printStackTrace();
 			}
 			//Set previous state to the state you where in before entering pause menu
 			PauseMenuController.setPreviousState(InGame.STATE_ID); 
 			
 			gameController.getInGameMusic().setVolume(0.3f);
 			sbg.enterState(PauseMenu.STATE_ID);
 		}
 		
 		if(key == Input.KEY_F) {
 			this.gameController.changeFullscreen(this.gameContainer);
 		}
 	}
 
 	@Override
 	public int getID() {
 		return InGame.STATE_ID;
 	}
 
 	public InGame getInGame() {
 		return inGame;
 	}
 
 
 	public InGameView getInGameView() {
 		return inGameView;
 	}
 
 	public CharacterController getCharacterController() {
 		return characterController;
 	}
 
 	public WorldController getWorldController() {
 		return worldController;
 	}
 
 	public BlockMapController getBlockMapController() {
 		return blockMapController;
 	}
 
 
 	public ArrayList<MoveableBoxController> getMoveableBoxControllers() {
 		return moveableBoxControllers;
 	}
 
 
 	public ArrayList<CandyMonsterController> getCandyMonsterControllers() {
 		return candyMonsterControllers;
 	}
 
 
 	public ArrayList<ItemController> getItemControllers() {
 		return itemControllers;
 	}
 
 
 	public ArrayList<SpikesController> getSpikesControllers() {
 		return spikesControllers;
 	}
 
 
 	public PlayerController getPlayerController() {
 		return playerController;
 	}
 	
 
 	public Sound getHappySound() {
 		return happySound;
 	}
 
 	
 }
