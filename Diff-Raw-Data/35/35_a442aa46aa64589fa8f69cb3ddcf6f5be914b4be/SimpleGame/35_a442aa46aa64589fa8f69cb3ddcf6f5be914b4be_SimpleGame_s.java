 
 //import org.newdawn.slick.Animation;
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 //import org.newdawn.slick.SpriteSheet;
  
 public class SimpleGame extends BasicGame
 {
 	static final int SCREEN_HEIGHT = 800;
 	static final int SCREEN_WIDTH = 600;
 	
 	Background background = null;
 	Bird bird;
 	Human dude;
 	
     public SimpleGame()
     {
         super("Bird Poop!");
     }
  
     @Override
     public void init(GameContainer gc) 
 			throws SlickException 
 	{
     	background = new Background(new Image("resources/background.png"), new Image("resources/background.png"));
    	Image[] ibird = {new Image("resources/bird/white/01.png"), 
    			new Image("resources/bird/white/02.png"), 
    			new Image("resources/bird/white/03.png"), 
    			new Image("resources/bird/white/04.png"), 
    			new Image("resources/bird/white/05.png")};
     	bird = new Bird(ibird);
    	Image[] images = {new Image("resources/dude/walk/01.png"), 
     			new Image("resources/dude/walk/02.png"), 
     			new Image("resources/dude/walk/03.png"),
     			new Image("resources/dude/walk/04.png"),
     			new Image("resources/dude/walk/05.png"),
     			new Image("resources/dude/walk/06.png"),
     			new Image("resources/dude/walk/07.png")};
    	dude = new Human(images);
     	dude.getAnimation().setAutoUpdate(true);
     	dude.getAnimation().start();
    	
    	bird.getAnimation().setAutoUpdate(true);
    	bird.getAnimation().start();
     }
  
     @Override
     public void update(GameContainer gc, int delta) 
 			throws SlickException     
     {
     	Input input = gc.getInput();
     	background.update(delta);
     	bird.update(input, delta);
     }
     
     @Override
     public void render(GameContainer gc, Graphics g) 
 			throws SlickException 
     {
     	background.draw();
     	bird.draw();
     }
  
     public static void main(String[] args) 
 			throws SlickException
     {    	
          AppGameContainer app = new AppGameContainer(new SimpleGame());
  
          app.setDisplayMode(SCREEN_HEIGHT, SCREEN_WIDTH, false);
          app.start();
     }
 }
