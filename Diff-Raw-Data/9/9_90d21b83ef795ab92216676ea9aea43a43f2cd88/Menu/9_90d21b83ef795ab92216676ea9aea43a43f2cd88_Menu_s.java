 package View;
 
 import java.awt.GraphicsDevice;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 
 
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 
 import org.lwjgl.input.Mouse;
 import org.newdawn.slick.*;
 import org.newdawn.slick.openal.Audio;
 import org.newdawn.slick.openal.AudioLoader;
 import org.newdawn.slick.state.*;
 import org.newdawn.slick.tiled.TiledMap;
 import org.newdawn.slick.util.ResourceLoader;
 
 import Control.GlobalClassSelector;
 
 
 
 
 
 public class Menu extends BasicGameState implements ActionListener{
 
 	private String mouse = "No input yet";
 
 	/** The wav sound effect */
 	private Audio wavEffect;
 	
 	private boolean startMusic = true;
 	
 	Image bg;
 
 	Image backgroundImage;
 	Image singleplayerButton;
 	Image multiplayerButton;
 	Image exitButton;
 	Image titleText;
 	
 	public Menu (int state){
 		
 	}
 	
 	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException{
 		
 		backgroundImage = new Image("res/miscImages/bg.png");
 		singleplayerButton = new Image("res/buttons/singleplayer.png");
 		multiplayerButton = new Image("res/buttons/multiplayer.png");
 		exitButton = new Image("res/buttons/exitButton.png");
 		titleText = new Image("res/miscImages/title.png");
 		
 		try {
 			wavEffect = AudioLoader.getAudio("WAV", ResourceLoader.getResourceAsStream("res/sounds/bg-music.wav"));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException{
 		gc.setFullscreen(false);
 		g.setColor(Color.black);
 		g.drawImage(backgroundImage, 0, 0);
 
 		if(startMusic){
 			wavEffect.playAsSoundEffect(1.0f, 1.0f, true);
 			startMusic = false;
 		}
 		g.drawString(mouse, 500, 20);
 		
 		g.drawImage(titleText, 380, 100);
 		g.drawImage(singleplayerButton, 500, 300);
 		g.drawImage(multiplayerButton, 500, 400);
 		g.drawImage(exitButton, 500, 500);
 	}
 
 	public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException{
 		
 		int xPos = Mouse.getX();
 		int yPos = 720 - Mouse.getY();
 		
 		mouse = "Mouse position: (" + xPos + "," + yPos + ")";
 		
 		Input input = gc.getInput();  
 		// Escape key quits the game
         if(input.isKeyDown(Input.KEY_ESCAPE)) gc.exit();
         
 		
 		if((500<xPos && xPos<750) && (300<yPos && yPos<354)){
 			singleplayerButton = new Image("res/buttons/singleplayer_pressed.png");
 			if(input.isMousePressed(0)){ // 0 = leftclick, 1 = rightclick
 				GlobalClassSelector.getController().setSingleOrMulti(false);
				sbg.enterState(5);
 			}
 		} else if((500<xPos && xPos<750) && (400<yPos && yPos<454)){
 
 			multiplayerButton = new Image("res/buttons/multiplayer_pressed.png");
 			if(input.isMousePressed(0)){ // 0 = leftclick, 1 = rightclick
 				GlobalClassSelector.getController().setSingleOrMulti(true);
 				
 				// Try to connect to server.
 				GlobalClassSelector.getController().getSocketClient().getPlayer().setConnected(true);
 				GlobalClassSelector.getController().getSocketClient().findConnection();
 				
 				long oldTime = System.currentTimeMillis();
 				long timeDiff = 0;
 				
 				// Wait ca 3 seconds
 				while(timeDiff < 3000) {
 					timeDiff = System.currentTimeMillis() - oldTime;
 					if(GlobalClassSelector.getController().getSocketClient().getPlayer().isConnected()) {
 						break;
 					}
 				}
 				
 				// Enter Multiplayer state if and only if SocketClient successfully connecter to the server.
 				if(GlobalClassSelector.getController().getSocketClient().getPlayer().isConnected()) {
 					sbg.enterState(3);
 				} else {
 					
 				}
 			}
 		} else if((500<xPos && xPos<750) && (500<yPos && yPos<597)){
 			exitButton = new Image("res/buttons/exitButton.png");
 			if(input.isMousePressed(0)){ // 0 = leftclick, 1 = rightclick
 				System.exit(0);
 			}
 		}else{
 			singleplayerButton = new Image("res/buttons/singleplayer.png");
 			multiplayerButton = new Image("res/buttons/multiplayer.png");
 		}
 	}
 	
 	public int getID(){
 		return 0;
 	}
 	
 	public int getWidth(Image image){
 		return image.getWidth();
 	}
 	
 	public int getHeight(Image image){
 		return image.getHeight();
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		
 	}
 }
