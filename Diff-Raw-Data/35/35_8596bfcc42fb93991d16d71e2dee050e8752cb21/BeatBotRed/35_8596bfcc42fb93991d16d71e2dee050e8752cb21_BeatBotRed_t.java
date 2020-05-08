 package beatbots.simulation;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Vector2f;
 
 public strictfp class BeatBotRed extends BeatBot {
 	
 	private BeatTokenManager beatTokenManager;
 	
 	private Animation animation;
 	
 	private Color color;
 	
 	@Override
 	public float getRadius() {
 		
 		return this.animation.getWidth() / 2;
 	}
 
 	public BeatBotRed(BeatMachine beatMachine, BulletManager bulletManager, Vector2f startPosition, BeatTokenManager beatTokenManager) {
 		
 		super(beatMachine, bulletManager, startPosition);
 		
 		this.beatTokenManager = beatTokenManager;
 	}
 	
 	@Override
 	public strictfp void init(GameContainer gameContainer) throws SlickException {
 		
 		super.init(gameContainer);
 		
 		this.animation = new Animation(new Image[] { new Image("assets/BeatBot2Frame1.png"), new Image("assets/BeatBot2Frame2.png") }, 200, true);
 		
 		this.color = Utils.getBeatColor(Beat.Red);
 	}
 	
 	@Override
 	public strictfp void render(GameContainer gameContainer, Graphics graphics) {
 		
 		super.render(gameContainer, graphics);
 		
 		graphics.drawAnimation(
 				this.animation, 
 				this.getPosition().getX() - this.animation.getWidth() / 2, 
 				this.getPosition().getY() - this.animation.getHeight() / 2, 
 				this.color);
 	}
 	
 	@Override
	protected strictfp void onDestroy() {
 		
		super.onDestroy();
 		
 		this.beatTokenManager.drop(this.getPosition(), Beat.Red);
 	}
 }
