 package yaz.game.states;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Sound;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class MainMenu extends BasicGameState {
 	
 	protected int MAINMENU = 0;
 	
 	Image MMBackground_Final, MM_Text_YAZ, MM_Text_Play, MM_Text_Options, MM_Text_LoadGame, MM_Text_Credits, MM_Text_Quit, MM_TextOverlay_YAZ, MM_TextOverlay_Play, MM_TextOverlay_Options, MM_TextOverlay_LoadGame, MM_TextOverlay_Credits, MM_TextOverlay_Quit = null;
 	Sound MM_yaz;
 	
 	@SuppressWarnings("unused")
 	private int MouseX, MouseY, Frame; 
 	
 	boolean IsInYAZ = false;
 	boolean IsInPlay = false;
 	boolean IsInLoad = false;
 	boolean IsInOptions = false;
 	boolean IsInCredits = false;
 	boolean IsInQuit = false;
 
 	public MainMenu(int mainmenustate) {
 		this.MAINMENU = mainmenustate;
 	}
 	
 	@Override
 	public int getID() {
 		return this.MAINMENU;
 	}
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame stg) throws SlickException {
 		MMBackground_Final = new Image("res/MMBackground_Final.png");
 		MM_Text_YAZ = new Image("res/MM_Text_YAZ.png");
 		MM_Text_Play = new Image("res/MM_Text_Play.png");
 		MM_Text_Options = new Image("res/MM_Text_Options.png");
 		MM_Text_LoadGame = new Image("res/MM_Text_LoadGame.png");
 		MM_Text_Credits = new Image("res/MM_Text_Credits.png");
 		MM_Text_Quit = new Image("res/MM_Text_Quit.png");
 		MM_TextOverlay_YAZ = new Image("res/MM_TextOverlay_YAZ.png");
 		MM_TextOverlay_Play = new Image("res/MM_TextOverlay_Play.png");
 		MM_TextOverlay_Options = new Image("res/MM_TextOverlay_Options.png");
 		MM_TextOverlay_LoadGame = new Image("res/MM_TextOverlay_LoadGame.png");
 		MM_TextOverlay_Credits = new Image("res/MM_TextOverlay_Credits.png");
 		MM_TextOverlay_Quit = new Image("res/MM_TextOverlay_Quit.png");
 		MM_yaz = new Sound("res/MM_yaz.ogg");
 		MM_yaz.play();
 		gc.setTargetFrameRate(60);
 		gc.setVSync(true);
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame stg, Graphics g) throws SlickException {
 		MMBackground_Final.draw(0, 0);
 		g.drawString("A: "+ this.IsInYAZ, 600, 100);
 		g.drawString("B: "+ this.IsInPlay, 600, 120);
 		g.drawString("C: "+ this.IsInLoad, 600, 140);
 		g.drawString("D: "+ this.IsInOptions, 600, 160);
 		g.drawString("E: "+ this.IsInCredits, 600, 180);
 		g.drawString("F: "+ this.IsInQuit, 600, 200);
 		if(IsInYAZ)
 			MM_TextOverlay_YAZ.draw(8, 10);
 		else
 			MM_Text_YAZ.draw(8, 10);
 		
 		
 		if(IsInPlay)
 			MM_TextOverlay_Play.draw(120, 120);
 		else
 			MM_Text_Play.draw(120, 120);
 		
 		
 		if(IsInLoad)
 			MM_TextOverlay_LoadGame.draw(120, 200);
 		else
 			MM_Text_LoadGame.draw(120, 200);
 		
 		
 		if(IsInOptions)
 			MM_TextOverlay_Options.draw(120, 280);
 		else
 			MM_Text_Options.draw(120, 280);
 		
 		
 		if(IsInCredits)
 			MM_TextOverlay_Credits.draw(120, 360);
 		else
 			MM_Text_Credits.draw(120, 360);
 		
 		if(IsInQuit)
 			MM_TextOverlay_Quit.draw(120, 440);
 		else
 			MM_Text_Quit.draw(120, 440);
 	
 		
 	}
 
 	/*
 	 * @param delta = frames
 	 * Ugly I know, but it works efficiently :)
 	 */
 	@Override
 	public void update(GameContainer gc, StateBasedGame stg, int delta) throws SlickException {
 		Input i = gc.getInput();
 		MouseX = i.getMouseX();
 		MouseY = i.getMouseY();
 		if(i.isKeyPressed(Input.KEY_ENTER)) {
 			stg.enterState(0);
 		}
 		
 		if((MouseX >= 8 && MouseX <= 8 + MM_Text_YAZ.getWidth()) && (MouseY >= 10 && MouseY <= 10 + MM_Text_YAZ.getHeight())) {
 			IsInYAZ = true;
 		}else{
 			IsInYAZ = false;
 		}
 		
 		if((MouseX >= 120 && MouseX <= 120 + MM_Text_Play.getWidth()) && (MouseY >= 120 && MouseY <= 120 + MM_Text_Play.getHeight())) {
 			IsInPlay = true;
 		}else{
 			IsInPlay = false;
 		}
 		
 		if((MouseX >= 120 && MouseX <= 120 + MM_Text_LoadGame.getWidth()) && (MouseY >= 200 && MouseY <= 200 + MM_Text_LoadGame.getHeight())) {
 			IsInLoad = true;
 		}else{
 			IsInLoad = false;
 		}
 		
 		if((MouseX >= 120 && MouseX <= 120 + MM_Text_Options.getWidth()) && (MouseY >= 280 && MouseY <= 280 + MM_Text_Options.getHeight())) {
 			IsInOptions = true;
 		}else{
 			IsInOptions = false;
 		}
 		
 		if((MouseX >= 120 && MouseX <= 120 + MM_Text_Credits.getWidth()) && (MouseY >= 360 && MouseY <= 360 + MM_Text_Credits.getHeight())) {
 			IsInCredits = true;
 		}else{
 			IsInCredits = false;
 		}
 		
 		if((MouseX >= 120 && MouseX <= 120 + MM_Text_Quit.getWidth()) && (MouseY >= 440 && MouseY <= 440 + MM_Text_Quit.getHeight())) {
 			IsInQuit = true;
 		}else{
 			IsInQuit = false;
 		}
 		
 		if(i.isMousePressed(Input.MOUSE_LEFT_BUTTON)){
 			
 			if(IsInYAZ && MM_yaz.playing()){
 				MM_yaz.stop();
			}else if(IsInYAZ && !MM_yaz.playing()){
 				MM_yaz.loop();
 			}
 			
 			if(IsInQuit){
 				gc.exit();
 			}
 		
 		}
 	}
 }
