 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.event.KeyEvent;
 import java.awt.image.BufferedImage;
 
 import com.golden.gamedev.GameEngine;
 import com.golden.gamedev.GameObject;
 import com.golden.gamedev.engine.timer.SystemTimer;
 import com.golden.gamedev.object.Background;
 import com.golden.gamedev.object.CollisionManager;
 import com.golden.gamedev.object.GameFont;
 import com.golden.gamedev.object.PlayField;
 import com.golden.gamedev.object.Sprite;
 import com.golden.gamedev.object.SpriteGroup;
 import com.golden.gamedev.object.background.ColorBackground;
 
 
 public class Multiplayer extends GameObject
 {
 
 	public Multiplayer(GameEngine parent) 
 	{
 		super(parent);
 	}
 	
 	private static Pokemon squirtle;
 	private static Pokemon charmander;
 	private SpriteGroup BULLET_GROUP;
 	private Background background;
 	private SystemTimer myTimer;
 	private SpriteGroup ENEMY_GROUP;
 	private CollisionManager collisionType;
 	private CollisionManager powerType;
 	private CollisionManager playerType;
 	private GameFont f;
 	private long startTime;
 	private SpriteGroup POWERUP_GROUP;
 	private SpriteGroup SQUIRTLE_GROUP;
 	private PlayField field;
 	private SpriteGroup ENEMY_BULLETS;
 	private EnemyGenerator en;
 	private CollisionManager charType;
 	private static String statusMessage;
 	private static String type = "Multiplayer";
 	
 	private BufferedImage fireBall;
 	private BufferedImage Charmander;
 	
 	public static String getType()
 	{
 		return type;
 	}
 	
 	public static String getStatus()
 	{
 		return statusMessage;
 	}
 	
 	public static int getScore()
 	{
 		return squirtle.getScore();
 	}
 
 	public void initResources() 
 	{
 		background = new ColorBackground(Color.BLACK, 1280, 800);
 		myTimer = new SystemTimer();
 		startTime = myTimer.getTime();
 		
 		fireBall = getImage("resources/FireBall.png");
 		Charmander = getImage("resources/Charmander.png");
 		
 		initBullets();
 		initShip();
 		initEnemies();
 		initFont();
 		initCollisions();
 		initPlayField();
 	}
 	
 	public void initPlayField()
 	{
 		field = new PlayField();
 		field.setBackground(background);
 		field.addGroup(SQUIRTLE_GROUP);
 		field.addGroup(ENEMY_GROUP);
 		field.addGroup(BULLET_GROUP);
 		field.addGroup(ENEMY_BULLETS);
 		field.addCollisionGroup(BULLET_GROUP, ENEMY_GROUP, collisionType);
 		field.addCollisionGroup(SQUIRTLE_GROUP, ENEMY_BULLETS, playerType);
 		field.addCollisionGroup(SQUIRTLE_GROUP, ENEMY_GROUP, charType);
 	}
 	
 	public void initShip()
 	{
 		SQUIRTLE_GROUP = new SpriteGroup("Squirtle Group");
 		squirtle = new Squirtle(getImage("resources/Squirtle.png"), BULLET_GROUP);
 		squirtle.setLocation(0, 
                              getHeight() / 2 - squirtle.getHeight() / 2);
 
 		SQUIRTLE_GROUP.add(squirtle);
 	}
 	
 	public void initBullets()
 	{
 		BULLET_GROUP = new SpriteGroup("Bullet Group");
 		ENEMY_BULLETS = new SpriteGroup("Enemy Bullets");
 	}
 	
 	public void initCollisions()
 	{
 		collisionType = new ShotToSpriteCollision();
 		playerType = new SquirtleFireCollision();
 		charType = new SquirtleCharCollision();
 	}
 	
 	public void initEnemies()
 	{
 		ENEMY_GROUP = new SpriteGroup("Enemy Group");
 		charmander = new Enemy(Charmander, fireBall, ENEMY_BULLETS);
 		charmander.setHealth(200);
 		charmander.setFireRate(500);
 		charmander.setLocation(getWidth()  - charmander.getWidth() , 
                 getHeight() / 2 - charmander.getHeight() / 2);
 		ENEMY_GROUP.add(charmander);
 	}
 	
 	public void initFont()
 	{
 		Font font = new Font( "Monospaced", Font.BOLD, 24 );
 		f = fontManager.getFont(font);
 	}
 
 	public void render(Graphics2D pen) 
 	{	
 		field.render(pen);
 		
 		pen.setColor(Color.RED);
 		f.drawString(pen, "Squirtle Health: " + squirtle.getHealth(), GameFont.LEFT, 40, 0, 20);
 		
 		pen.setColor(Color.BLUE);
 		f.drawString(pen, "Charmander Health " + charmander.getHealth() , GameFont.LEFT, 370, 0, 20);
 	}
 
 
 	 public void update(long elapsedTime)
 	 {
 	        field.update(elapsedTime);
 
 	        processKeyPresses(elapsedTime);
 
 	        manageSprites();
 	        
	        System.out.println(SQUIRTLE_GROUP.getActiveSprite() + " " + ENEMY_GROUP.getActiveSprite());
 	        
	        if (SQUIRTLE_GROUP.getActiveSprite() == null && ENEMY_GROUP.getActiveSprite() == null)
 	        {
	        	statusMessage = "Both combatants have perished";
 	        	parent.nextGameID = 3;
 	        	finish();
 	        }
	        else
	        {
	        	 if (SQUIRTLE_GROUP.getActiveSprite() == null)
	 	        {
	 	        	statusMessage = "Charmander has prevailed";
	 	        	parent.nextGameID = 3;
	 	        	finish();
	 	        }
	 	        
	 	        if (ENEMY_GROUP.getActiveSprite() == null)
	 	        {
	 	        	statusMessage = "Squirtle has prevailed";
	 	        	parent.nextGameID = 3;
	 	        	finish();
	 	        }
	        }   
	       
 	    }
 	 
 	 public void manageSprites()
 	 {
 		    removeSprites(BULLET_GROUP);
 		    removeSprites(ENEMY_BULLETS);
 	 }
 	 
 	 public void processKeyPresses(long elapsedTime)
 	 {
 		 if (keyDown(KeyEvent.VK_S)) 
 	        {
 	        	if(squirtle.getY() + squirtle.getHeight() < getHeight() )
 	        		squirtle.move(0, 0.5*elapsedTime);
 	        }
 	        if (keyDown(KeyEvent.VK_W)) 
 	        {
 	        	if(squirtle.getY() > 0 )
 	        		squirtle.move(0, -0.5*elapsedTime);
 	        }
 	        if (keyDown(KeyEvent.VK_D)) 
 	        {
 	        	if(squirtle.getX() + squirtle.getWidth() < getWidth())
 	        		squirtle.move(0.5*elapsedTime, 0);
 	        }
 	        if (keyDown(KeyEvent.VK_A)) 
 	        {
 	        	if(squirtle.getX() > 0)
 	        		squirtle.move(-0.5*elapsedTime, 0);
 	        }
 	        
 	        if (keyDown(KeyEvent.VK_SPACE))
 	        {
 	        	long currentTime = myTimer.getTime();
 	        	if (squirtle.canShoot(currentTime))
 	        	{
 	        	Sprite bullet = new Bullet(getImage("resources/BubbleMult.png"), squirtle);
 
 	        	BULLET_GROUP.add(bullet);
 	        	squirtle.setLastShot(currentTime);
 	        	}
 	        }
 	        if (keyDown(KeyEvent.VK_DOWN)) 
 	        {
 	        	if(charmander.getY() + charmander.getHeight() < getHeight() )
 	        		charmander.move(0, 0.5*elapsedTime);
 	        }
 	        if (keyDown(KeyEvent.VK_UP)) 
 	        {
 	        	if(charmander.getY() > 0 )
 	        		charmander.move(0, -0.5*elapsedTime);
 	        }
 	        if (keyDown(KeyEvent.VK_RIGHT)) 
 	        {
 	        	if(charmander.getX() + charmander.getWidth() < getWidth())
 	        		charmander.move(0.5*elapsedTime, 0);
 	        }
 	        if (keyDown(KeyEvent.VK_LEFT)) 
 	        {
 	        	if(charmander.getX() > 0)
 	        		charmander.move(-0.5*elapsedTime, 0);
 	        }
 	        
 	        if (keyDown(KeyEvent.VK_ENTER))
 	        {
 	        	long currentTime = myTimer.getTime();
 	        	if (charmander.canShoot(currentTime))
 	        	{
 	        	Sprite bullet = new Bullet(fireBall, charmander);
 	        	
 	        	bullet.setSpeed(-.8,0);
 	        	
 	        	ENEMY_BULLETS.add(bullet);
 	        	
 	        	charmander.setLastShot(currentTime);
 	        	}
 	        }
 	 }
 	 
 	 
 	 public void removeSprites(SpriteGroup s)
 	 {
 		 Sprite[] sprites = s.getSprites();
 		 
 		 for (Sprite sp: sprites)
 		 {
 			 if (sp != null && !inBounds(sp))
 				 sp.setActive(false);
 		 }
 	 }
 	 
 	 
 	 
 	 public boolean inBounds(Sprite s)
 	 {
 		 return (s.getX() > 0 && s.getY() > 0 && s.getX() < getWidth() && s.getY() < getHeight());
 	 }
 
 }
