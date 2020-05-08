 package no.mamot.swashbuckler;
 
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 import no.mamot.engine.Camera;
 import no.mamot.engine.Engine;
 import no.mamot.engine.GameProxy;
 import no.mamot.engine.InputHandler;
 import no.mamot.engine.Level;
 import no.mamot.engine.View;
 import no.mamot.engine.ViewImpl;
 import no.mamot.swashbuckler.editor.LevelSaver;
 import no.mamot.swashbuckler.weapon.LightningStrike;
 
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Sound;
 
 public class SwashbucklerEngine implements Engine, InputHandler {
 
 	private GameProxy gameProxy;
 	private Camera camera;
 	private Level level;
 	private View view;
 	private LevelSaver levelLoader = new LevelSaver(null, null);
 	int screenHeight = 768;
 	int screenWidth = 1024;
 	private Physics physics;
 	int nextLevel = 0;
 
 	public SwashbucklerEngine() throws SlickException {
 		view = new ViewImpl(screenWidth, screenHeight);
 		camera = view.getCamera();
 		gameProxy = new GameProxy("Swashbuckler", view, this, this,
 				screenWidth, screenHeight);
 		
 
 	}
 
 	@Override
 	public void updateWorld(int delta) {
 		physics.doPhysics(delta);
 		camera.setCenter(level.getMan().getPosition().getX(), level.getMan().getPosition().getY());
 
 		for (int i = level.getUpdatableList().size() - 1; i >= 0; i--) {
 			level.getUpdatableList().get(i).update(delta);
 		}
 		if (level.getFinished()){
 			
 			try {
 				loadNextLevel();
 			} catch (SlickException e) {				
 				e.printStackTrace();				
 			}
 		}
 	}
 
 	@Override
 	public void start() throws SlickException {
 		gameProxy.start();
 	}
 
 	public static void main(String[] args) throws SlickException {
 		Engine swashbuckler = new SwashbucklerEngine();
 		swashbuckler.start();
 	}
 
 	@Override
 	public void handleInput(Input input, int delta) {
 		// movement
 		if (input.isKeyPressed(Input.KEY_SPACE)) {
 			level.getMan().jump();
 		}
 		if (input.isKeyDown(Input.KEY_W)) {
 			// position.y -= speed / delta;
 		}
 		if (input.isKeyDown(Input.KEY_S)) {
 			// position.y += speed / delta;
 		}
 		if (input.isKeyDown(Input.KEY_A)) {
 			level.getMan().left(delta);
 		}
 		if (input.isKeyDown(Input.KEY_D)) {
 			level.getMan().right(delta);
 		}
 		if (input.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {			
 			attack(input);
 		}
 		if (input.isMousePressed(Input.MOUSE_RIGHT_BUTTON)) {
 			level.getMan().goTo(input.getMouseX() + camera.getTopLeftCorner().x,
 					input.getMouseY() + camera.getTopLeftCorner().y);
 		}
 	}
 
 	
 	private void attack(Input input) {
 		// use command pattern..
 		level.getMan().attack(input.getMouseX() + camera.getTopLeftCorner().x,
 				input.getMouseY() + camera.getTopLeftCorner().y);
 	}
 
 	@Override
 	public void init() throws SlickException {
 		loadNextLevel();
 


 		Sound music = new Sound("data/Music/backgroundSound2.wav");
 		music.loop();
		
		
		setupAttack();
 	}
 
 	private void loadNextLevel() throws SlickException {
 		
 		List <String> levels = getLevels();		
 		if (nextLevel >= levels.size()){
 			System.exit(2);
 		}
 		level = levelLoader.loadLevel(levels.get(nextLevel));		
 		camera.setCenter(level.getMan().getPosition().getX(), level.getMan().getPosition().getY());
 		view.setLevel(level);
 		physics = new Physics(500);
 		physics.setLevel(level);
 		physics.init();
 		nextLevel++;
 		
 	}
 
 	private List<String> getLevels() {
 		Scanner fr;
 		List<String> levels = new ArrayList<String>();
 		try {
 			fr = new Scanner(new File("data/levels/levelsequence.cfg"));
 			while(fr.hasNext()){
 				levels.add(fr.nextLine());
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		return levels;
 
 	}
 
 	private void setupAttack() {
 		LightningStrike strike = new LightningStrike(level.getMan());
 		strike.setPlayer(level.getMan());
 		strike.setPlayerLevel(level);		
 		strike.init();
 		level.getDrawableList().add(strike);
 		level.getGameObjectList().add(strike);
 		level.getUpdatableList().add(strike);
 		level.getMan().setWeapon(strike);
 		physics.addGameObject(strike);
 	}
 
 	public Swashbuckler getMan() {
 		return level.getMan();
 	}
 	
 	
 
 }
