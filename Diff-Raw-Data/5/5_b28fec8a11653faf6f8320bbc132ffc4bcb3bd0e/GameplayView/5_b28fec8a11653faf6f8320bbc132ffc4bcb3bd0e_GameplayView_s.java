  package tetrix.view;
 
 import java.awt.Font;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.UnicodeFont;
 import org.newdawn.slick.font.effects.ColorEffect;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.state.transition.EmptyTransition;
 import org.newdawn.slick.state.transition.FadeInTransition;
 import org.newdawn.slick.state.transition.FadeOutTransition;
 
 import tetrix.core.BlockBox;
 import tetrix.core.Bullet;
 import tetrix.core.Cannon;
 import tetrix.core.CollisionHandler;
 import tetrix.core.FileReader;
 import tetrix.core.HighScore;
 import tetrix.core.Player;
 import tetrix.core.Position;
 import tetrix.core.tetrominos.Square;
 import tetrix.core.tetrominos.Tetromino;
 import tetrix.sound.GameMusic;
 import tetrix.sound.SoundEffects;
 import tetrix.util.Util;
 import tetrix.view.StateHandler.States;
 import tetrix.view.theme.ThemeHandler;
 
 /**
  * Class responsible for updating and rendering of the gameplay view.
  * 
  * @author Magnus Huttu, Linus Karlsson
  *
  */
 public class GameplayView extends BasicGameState {
 
 	private int stateID;
 
 	private Image background;
 	private Image cannonImage;
 	private Image iBlock;
 	private Image jBlock;
 	private Image lBlock;
 	private Image oBlock;
 	private Image tBlock;
 	private Image sBlock;
 	private Image zBlock;
 	private Image lockedBlock;
 	private Image screenCapture;
 	
 	private Cannon cannon;
 	private Player player;
 	private Bullet bullet; 
 	private BlockBox blockBox;
 	private CollisionHandler ch;
 
 	private List<Bullet> bulletList;
 	private List<Image> blocks;
 
 	private UnicodeFont scoreDisplay;
 	private boolean isPaused;
 	private long timerInterval;
 	private int levelUpInterval;
 
 	public GameplayView(int stateID) {
 		this.stateID = stateID;
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public void init(GameContainer gc, StateBasedGame sbg)
 			throws SlickException {
 		background = ThemeHandler.get(ThemeHandler.GAME_BACKGROUND_IMG);
 		screenCapture = new Image(Util.WINDOW_WIDTH, Util.WINDOW_HEIGHT);
 		cannonImage = new Image(50,50);
 
 		iBlock = ThemeHandler.getBlockOrCannon(ThemeHandler.PURPLE_BLOCK_IMG);
 		jBlock = ThemeHandler.getBlockOrCannon(ThemeHandler.BLUE_BLOCK_IMG);
 		lBlock = ThemeHandler.getBlockOrCannon(ThemeHandler.ORANGE_BLOCK_IMG);
 		oBlock = ThemeHandler.getBlockOrCannon(ThemeHandler.YELLOW_BLOCK_IMG);
 		tBlock = ThemeHandler.getBlockOrCannon(ThemeHandler.GREEN_BLOCK_IMG);
 		sBlock = ThemeHandler.getBlockOrCannon(ThemeHandler.RED_BLOCK_IMG);
 		zBlock = ThemeHandler.getBlockOrCannon(ThemeHandler.TURQUOISE_BLOCK_IMG);
 		lockedBlock = ThemeHandler.getBlockOrCannon(ThemeHandler.LOCKED_BLOCK_IMG);
 
 		cannon = new Cannon();
 		bulletList = new ArrayList<Bullet>();
 		blocks = new ArrayList<Image>();
 		try {
 			player = new Player(FileReader.getPlayerName().toString());
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		blockBox = new BlockBox(player);
 		ch = new CollisionHandler(blockBox);
 		timerInterval = 2000;
 		
 		Font font = new Font("Verdana", Font.PLAIN,55);
 		scoreDisplay = new UnicodeFont(font , 15, true, false);
 		scoreDisplay.addAsciiGlyphs();
 		scoreDisplay.addGlyphs(400, 600);
 		scoreDisplay.getEffects().add(new ColorEffect(java.awt.Color.YELLOW));
 		try {
 			scoreDisplay.loadGlyphs();
 		} catch (SlickException e1) {
 			e1.printStackTrace();
 		}
 		
 		levelUpInterval = 100;
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
 			throws SlickException {
 		background.draw(0,0);
 		cannonImage.setRotation(cannon.getRotation());
 		cannonImage.draw(cannon.getX(), cannon.getY());
 		scoreDisplay.drawString(5, 0, player.getScore() + "");
 
 		if(blockBox.isInUse()){
 
 			blockBox.update();
 			putImage();
 
 			int i = 0;
 			Position[][] p = blockBox.getPos();
 			int length = blockBox.getTetroList().size();
 			for(int j = 0; j < length; j++){
 				Position[] pe = p[j];
 				for(int h = 0; h < pe.length; h++){
 					blocks.get(i).draw(pe[h].getX(), pe[h].getY());
 					i++;
 				}
 			}
 		}
 
 		g.setColor(Color.black);
 		for(int i = 0; i < bulletList.size(); i++){
 			g.fillRect(((Bullet) bulletList.get(i)).getX(), ((Bullet) bulletList.get(i)).getY(), 5, 5);
 		}
 
 		if(isPaused) {
 			g.copyArea(screenCapture, 0, 0);
 		}
 	}
 
 	public void enter(GameContainer gc, StateBasedGame sbg) {
 		isPaused = false;
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sbg, int delta)
 			throws SlickException {
 		Input input = gc.getInput();
 		checkInput(input, sbg);
		
		player.increaseScore();
 
 		int size = bulletList.size();
 		for(int i = 0; i < size; i++){
 			if(!ch.checkCollision(bulletList.get(i))){
 				bulletList.get(i).update();
 			} else{
 				bulletList.remove(i);
 				size--;
 			}
 		}
 		
 		if(timerInterval >= 500 && player.getScore() !=  0) {
 			if(player.getScore() % levelUpInterval == 0) {
 				increaseSpeed(200);
				levelUpInterval =  levelUpInterval+100;
 				SoundEffects.instance().speedUpPlay();
 			}
 		}
 		
 		if(blockBox.gameOver()) {
 			isPaused = true;
 			try {
 				HighScore.instance().setHighScore(player.getName(), player.getScore());
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			sbg.enterState(States.GAMEOVERVIEW.getID(), new FadeOutTransition(), new FadeInTransition());	
 		}
 	}
 
 	public void checkInput(Input input, StateBasedGame sbg) throws SlickException {
 		int updateSpeed = 300/Util.FPS;
 
 		if(input.isKeyDown(Input.KEY_RIGHT)) {
 			cannon.move(updateSpeed);
 		}
 
 		if(input.isKeyDown(Input.KEY_LEFT)) {
 			cannon.move(-updateSpeed);
 		}
 
 		if(input.isKeyDown(Input.KEY_D)) {
 			cannon.move(updateSpeed);
 		}
 
 		if(input.isKeyDown(Input.KEY_A)) {
 			cannon.move(-updateSpeed);
 		}
 
 		if(input.isKeyPressed(Input.KEY_SPACE)) {
 			SoundEffects.instance().shot();
 			bullet = new Bullet(cannon.getPosition(), cannon.getValue());
 			bulletList.add(bullet);
 		}
 
 		if(input.isKeyPressed(Input.KEY_ENTER) || input.isKeyPressed(Input.KEY_ESCAPE)) {
 			isPaused = true;
 			try {
 				GameMusic.instance().gameMusicPause();
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			}
 			sbg.enterState(States.PAUSEDGAMEVIEW.getID(), new EmptyTransition(), new FadeInTransition());
 		}
 	}
 
 	/**
 	 * Repeatedly create a new block at a given speed
 	 */
 	public void startTimer(){
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					if(!isPaused){
 						blockBox.newBlock((int)(Math.random()*7+0.5));
 					}
 					Thread.sleep(timerInterval);
 					startTimer();
 				} catch (SlickException e) {
 					e.printStackTrace();
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}).start();
 	}
 	
 	public void setCannonImage(Image image) {
 		this.cannonImage = image;
 	}
 
 	public void putImage() throws SlickException{
 		Image block = null;
 		blocks.clear();
 		for(Tetromino t : blockBox.getTetroList()){
 			for(Square s : t.getSquares()){
 				if(!s.isMoving()){
 					block = lockedBlock;
 				} else{
 
 					if(t.toString().equals("I")){
 						block = iBlock;
 					}else if(t.toString().equals("J")){
 						block = jBlock;
 					}else if(t.toString().equals("L")){
 						block = lBlock;
 					}else if(t.toString().equals("O")){
 						block = oBlock;
 					}else if(t.toString().equals("T")){
 						block = tBlock;
 					}else if(t.toString().equals("S")){
 						block = sBlock;
 					}else if(t.toString().equals("Z")){
 						block = zBlock;
 					}
 				}
 
 				if(!s.destroyed()){
 					blocks.add(block);
 				}
 			}
 		}
 	}
 
 
 	public Image getPausedScreen() {
 		return screenCapture;
 	}
 
 	public void pause() {
 		isPaused = true;
 	}
 
 	/**
 	 * Resets the values
 	 */
 	public void newGame() {
 		player.resetScore();
 		timerInterval = 2000;
 		blockBox.clearBoard();
 		blocks.clear();
 		blockBox.backToGame();
 		bulletList.clear();
 		cannon.reset();
 	}
 
 	public void increaseSpeed(int value) {
 		timerInterval -= value; 
 	}
 
 	public void setLevel(int i){
 		blockBox.setLevel(i);
 	}
 
 	@Override
 	public int getID() {
 		return stateID;
 	}
 }
