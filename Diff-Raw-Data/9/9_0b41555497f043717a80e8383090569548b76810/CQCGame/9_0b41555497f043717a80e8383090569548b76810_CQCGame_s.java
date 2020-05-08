 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 
 
 public class CQCGame extends StateBasedGame{
 	
 	public static final int MAINMENU = 0;
 	public static final int GAMEPLAY = 1;
 	
 
 	public CQCGame() {
 		super("CQC");
 		
 		this.addState(new MainMenu(MAINMENU));
 		this.addState(new Gameplay(GAMEPLAY));
 		this.enterState(MAINMENU);
 	}
 	
 	public void main(String [] args) throws SlickException {
 		
 		AppGameContainer app = new AppGameContainer(new CQCGame());
 		
 		app.setDisplayMode(800, 600, false);
 		app.start();
 	}
 
 	@Override
	public void initStatesList(GameContainer arg0) throws SlickException {
		// TODO Auto-generated method stub
 		
	}

 }
