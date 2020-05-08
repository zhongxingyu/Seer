 package beatbots.simulation;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Vector2f;
 
 public strictfp abstract class BeatBot implements Collider, BeatListener, BarListener {
 	
 	private static final float SPEED = 0.02f;
 
 	private BeatMachine beatMachine;
 	private BulletManager bulletManager;
 	
 	private Vector2f startPosition;
 	
 	private boolean isActive;
 	
 	private boolean isMarked;
 	
 	private Vector2f position;
 	
 	private boolean isGoingRight;
 	
 	private int hops;
 	
 	private float desY;
 	
 	public boolean isActive() {
 		
 		return this.isActive;
 	}
 	
 	@Override
 	public Vector2f getPosition() {
 		
 		return this.position;
 	}
 	
 	public BeatBot(BeatMachine beatMachine, BulletManager bulletManager, Vector2f startPosition) {
 		
 		super();
 		
 		this.beatMachine = beatMachine;
 		this.bulletManager = bulletManager;
 		
 		this.isActive = true;
 		
 		this.startPosition = startPosition.copy();
 		
 		this.position = new Vector2f();
 	}
 	
 	public void init(GameContainer gameContainer) throws SlickException {
 		
 		this.beatMachine.addBeatListener(this);
 		this.beatMachine.addBarListener(this);
 		
 		this.position.set(this.startPosition);
 		
 		this.desY = this.position.y;
 		
 		this.isGoingRight = true;
 		
 		this.hops = 0;
 	}
 	
 	public void update(GameContainer gameContainer, int delta) {
 		
 		for (Bullet i : this.bulletManager.getBullets()) {
 			
 			if (i.isActive()) {
 				
 				if (Utils.checkCollision(this, i)) {
 					
 					this.mark(i.getBeat());
 					
 					i.destroy();
 				}
 			}
 		}
 		
 		if (this.position.y < this.desY) {
 			
 			this.position.y += SPEED * delta;
 		}
 		else {
 			
 			if (this.isGoingRight) {
 				
 				this.position.x += SPEED * delta;
 			}
 			else {
 				
 				this.position.x -= SPEED * delta;
 			}
 		}
 	}
 	
 	public void render(GameContainer gameContainer, Graphics graphics) {
 		
 	}
 	
 	public void destroy() {
 		
		if (this.isActive) {
			
			this.isActive = false;
			
			this.onDestroy();
		}
	}
	
	protected void onDestroy() {
		
 	}
 	
 	@Override
 	public void bar() {
 		
 	}
 
 	@Override
 	public void beat() {
 		
 		if (this.isMarked) {
 			
 			this.destroy();
 		}
 		
 		this.hops++;
 		
 		if (this.hops == 5) {
 			
 			this.hops = 0;
 			
 			this.desY = this.position.y + 4f;
 			
 			this.isGoingRight = !this.isGoingRight;
 		}
 	}
 	
 	public void mark(Beat beat) {
 		
 		this.isMarked = true;
 	}
 }
