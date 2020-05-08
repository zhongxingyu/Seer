 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
  
 public class SimpleGame extends BasicGame
 {	
 	private Background background = null;
 	private Bird bird;
 	private Enemy enemy;
 	private EntityManager entityManager;
 	private EntityQueueHandler entityQueueHandler;
 	private Score score;
 	
     public SimpleGame()
     {
         super("Bird Poop!");
     }
  
     @Override
     public void init(GameContainer gc) 
 			throws SlickException 
 	{
     	score = new Score();
     	background = new Background(new Image(GLOBAL.BACKGROUND), new Image(GLOBAL.BACKGROUND));
     	
     	Image[] ibird = {
     			new Image(GLOBAL.BIRD_WHITE_1, GLOBAL.chromakey), 
     			new Image(GLOBAL.BIRD_WHITE_2, GLOBAL.chromakey), 
     			new Image(GLOBAL.BIRD_WHITE_3, GLOBAL.chromakey), 
     			new Image(GLOBAL.BIRD_WHITE_4, GLOBAL.chromakey), 
     			new Image(GLOBAL.BIRD_WHITE_5, GLOBAL.chromakey)
     			};
     	bird = new Bird(ibird);
     	
     	Image[] iEnemy = {
     		new Image(GLOBAL.BIRD_BROWN_1, GLOBAL.chromakey), 
 			new Image(GLOBAL.BIRD_BROWN_2, GLOBAL.chromakey), 
 			new Image(GLOBAL.BIRD_BROWN_3, GLOBAL.chromakey), 
 			new Image(GLOBAL.BIRD_BROWN_4, GLOBAL.chromakey), 
 			new Image(GLOBAL.BIRD_BROWN_5, GLOBAL.chromakey)
     		};
     	enemy = new Enemy(iEnemy);
     	
     	entityManager = new EntityManager(bird);
     	entityManager.addEntity(Human.getRandomlyPlacedHuman());
    	entityManager.addEntity(Enemy.getRandomlyPlacedEnemy());
     	entityQueueHandler = new EntityQueueHandler(entityManager);
     }
  
     @Override
     public void update(GameContainer gc, int delta) 
 			throws SlickException     
     {
     	Input input = gc.getInput();
     	
     	if(input.isKeyDown(Input.KEY_H)){
     		entityManager.addEntity(Human.getRandomlyPlacedHuman());
     	}
     	
     	background.update(delta);
     	//bird.update(input, delta);
     	entityQueueHandler.update();
     	entityManager.update(input, delta);
     	score.update();
     }
     
     @Override
     public void render(GameContainer gc, Graphics g) 
 			throws SlickException 
     {
     	background.draw();
     	//bird.draw();
     	entityManager.draw();
     	
     	gc.getGraphics().drawString(score.getScorePrintable(), GLOBAL.SCREEN_WIDTH - 75, 0);
     }
  
     /**
      * Entry point
      * @param args
      * @throws SlickException
      */
     public static void main(String[] args) 
 			throws SlickException
     {    	
          AppGameContainer app = new AppGameContainer(new SimpleGame());
  
          app.setDisplayMode(GLOBAL.SCREEN_WIDTH, GLOBAL.SCREEN_HEIGHT, false);
          app.setTargetFrameRate(60);
          app.start();
     }
 }
