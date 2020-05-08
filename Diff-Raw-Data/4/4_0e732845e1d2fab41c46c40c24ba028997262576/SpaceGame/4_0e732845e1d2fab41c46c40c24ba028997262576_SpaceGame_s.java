 package nitrogene.core;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class SpaceGame extends StateBasedGame{
 
 	private static int SCRwidth;
 	private static int SCRheight;
 	public SpaceGame(String title) {
 		super("No Return");
 		SCRwidth = 1366;
 		SCRheight = 768;
 	}
 
 	public static void main(String[] args) throws SlickException{
 		AppGameContainer app = new AppGameContainer(new SpaceGame("Space Game"));
 		app.setDisplayMode(SCRwidth, SCRheight, false);
		app.setVSync(true);
 		app.start();

		
 	}
 
  
 	@Override
 	public void initStatesList(GameContainer container) throws SlickException {
 		this.addState(new MenuState(SCRwidth,SCRheight)); //1
 		this.addState(new GameState(SCRwidth,SCRheight)); //2
 		this.addState(new OptionState(SCRwidth,SCRheight)); //4
 		this.addState(new HangarState(SCRwidth,SCRheight)); //3
 		this.enterState(1);
 	}
 
 }
