 package states;
 
 import input.Controls;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import main.MainGame;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.util.FontUtils;
 
 import config.Config;
 import entities.BearSprite;
 import entities.Salmon;
 
 public class TitleState extends BasicGameState {
 	
 	private Image name;
 	private Salmon salmon;
 	private BearSprite bear;
 	private float jigglin = 0;
 	private List<Image> backgrounds = new ArrayList<Image>();
 
 	private float screenChange = 0;
 	private final float CHANGE_TIME = 1800;
 	private final float TRANS_TIME = 500;
 	private int section = 0;
	private int nextSection;
 	private int oldSection = 0;
 	private static int load = 0;
 
 	public TitleState() {
 		  
         try {
 			salmon = new Salmon(800, 500);
 			salmon.setDrawWidth(130);
 			salmon.setDrawHeight(130);
 			bear = new BearSprite(550, 500, 150, 150);
 			
 			backgrounds.add(new Image(Config.BACKGROUND_PATH + "forest3.png"));
 			backgrounds.add(new Image(Config.BACKGROUND_PATH + "mountainLake3.png"));
 			backgrounds.add(new Image(Config.BACKGROUND_PATH + "desert4.png"));
 			backgrounds.add(new Image(Config.BACKGROUND_PATH + "canyon2.png"));
 			backgrounds.add(new Image(Config.BACKGROUND_PATH + "hell2.png"));
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
         
 	}
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame game)
 			throws SlickException {
 		name = new Image("assets/images/ui/title screen/title.png");
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame game, Graphics graphics)
 			throws SlickException {
 		if (load == 0) {
 			graphics.drawString("Loading...", Config.RESOLUTION_WIDTH/2 - 30, Config.RESOLUTION_HEIGHT/2 - 20);
 			load += 1;
 			return;
 		}
 		
 		
 		Image background = backgrounds.get(section);
 
 		
 		background.drawCentered(Config.RESOLUTION_WIDTH/2, Config.RESOLUTION_HEIGHT/2);
 		
 		Image fadeOut = backgrounds.get(oldSection);
 		if (screenChange < TRANS_TIME) {
 			
 
 			fadeOut.setAlpha(1.0f - (screenChange/TRANS_TIME));
 			fadeOut.drawCentered(Config.RESOLUTION_WIDTH/2, Config.RESOLUTION_HEIGHT/2);
 		} else {
 			fadeOut.setAlpha(1.0f);
 		}
 		
 		name.drawCentered(Config.RESOLUTION_WIDTH / 2, Config.RESOLUTION_HEIGHT / 2);
 		
 		FontUtils.drawCenter(Config.MENU_FONT, "Press any key to continue", 0, Config.RESOLUTION_HEIGHT * 3 / 4, Config.RESOLUTION_WIDTH);
 		String credits = "By: Elizabeth Findley, Daniel Heins, Tiffany Huang, Adrian Mullings, Mike Salvato";
 		FontUtils.drawCenter(Config.PLAIN_FONT, credits, 0, Config.RESOLUTION_HEIGHT -Config.PLAIN_FONT.getHeight(credits), Config.RESOLUTION_WIDTH);
 		bear.render(graphics);
 		salmon.render(graphics);
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame game, int delta)
 			throws SlickException {
 		if (load == 0){
 			return;
 		}
 		if (load == 1){
 			Config.loadFonts();
 			Config.initMusic();
 			Config.playMusic(Config.titleMusic);
 			MainGame.initStatesAftesLoad(gc, game);
 			load++;
 		}
 		Controls.update(gc);
 		
 		if (Controls.moveKeyPressed()) {
			game.enterState(1);
 		}
 		
 		rotateTitle();
 		jigglin += delta/100f;
 		
 		salmon.update(gc, delta);
 		bear.update(gc, delta);
 		
 		screenChange += delta;
 		if (screenChange > CHANGE_TIME) {
 			screenChange = screenChange % CHANGE_TIME;
 			oldSection  = section;
 			section = (section + 1) % backgrounds.size();
 		}
 
 	}
 
 	public void rotateTitle() {
 		name.setRotation(12f *  (float) Math.cos(jigglin));
 	}
 	
 	@Override
 	public int getID() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 }
