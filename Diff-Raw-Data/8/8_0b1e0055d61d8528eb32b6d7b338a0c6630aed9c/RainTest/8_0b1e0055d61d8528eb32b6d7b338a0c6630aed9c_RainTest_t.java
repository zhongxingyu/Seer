 package utils.particles;
 
 import map.Cell;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 import utils.Position;
 import utils.interval.one.ColourRange;
 import utils.interval.one.FixedValue;
 import utils.interval.one.Interval;
 import utils.interval.two.Interval2D;
 import utils.interval.two.Range2D;
 
 public class RainTest extends ParticleEmitter<BlockedCollidingParticle> implements Runnable {
 	
 	private final Cell cell;
 	private GameContainer gc = null;
 	
 	public static RainTest getRain(Cell c, int layer){
 		RainTest r = new RainTest(c,new RainParticleGenerator(c), new Interval2D(new Interval(1f,c.getWidth()-1f), new FixedValue(0.5f)), new Interval2D(new FixedValue(0f),new Interval(0.12f,0.16f)), layer);
		for(int i = 0;i < 140;i++){
 			r.updateThreadless();
 		}
 		return r;
 	}
 
 	private RainTest(Cell c, ParticleGenerator<? extends BlockedCollidingParticle> pGen,
 			Range2D positionRange, Range2D velocityRange, int layer) {
 		super(pGen, positionRange, velocityRange, layer);
 		this.cell = c;
 	}
 
 	@Override
 	public boolean isEmitting() {
 		return true;
 	}
 
 	@Override
 	protected void generateParticles() {
 		for(int i=(int) cell.getWidth()/2;i>=0;i--){
 			addParticle(getNewParticle());
 		}
 	}
 	
 	public void updateThreadless() {
 		super.update(gc);
 	}
 	
 	private final Thread updateThread = new Thread(this);
 	private boolean running = true;
 	
 	@Override
 	public void update(GameContainer gc) {
 		this.gc = gc;
 		if(!updateThread.isAlive()){
 			updateThread.start();
 		}else{
 			synchronized (updateThread) {
 				updateThread.notify();
 			}
 		}
 	}
 	
 	@Override
 	public void run() {
 		while(running){
 			updateThreadless();
 			synchronized (updateThread) {
 				try {
 					Thread.currentThread().wait();
 				} catch (InterruptedException e) {
 				}
 			}
 		}
 	}
 	
 	@Override
 	protected void finalize() throws Throwable {
 		running = false;
 		synchronized (updateThread) {
 			updateThread.notify();
 		}
 	}
 }
 
 class RainParticleGenerator implements ParticleGenerator<BlockedCollidingParticle> {
 	private static final ColourRange cRange = new ColourRange(0.1f, 0.2f, 0.1f, 0.2f, 0.2f, 0.5f);
 	private static final Interval sizeRange = new Interval(0.005f,0.01f);
 	private static final Image tex;
 	
 	static {
 		Image texture = null;
 		try {
 			texture = new Image("data/images/circle.png");
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 		tex = texture;
 	}
 	
 	private final Cell c;
 	
 	public RainParticleGenerator(Cell cell) {
 		c = cell;
 	}
 	
 	@Override
 	public BlockedCollidingParticle newParticle(Position position, Position velocity) {
 		BlockedCollidingParticle p = new BlockedCollidingParticle(c,tex, position, velocity, cRange.random(), sizeRange.random());
 		p.setFriction(p.getFriction().scaledCopy(0.93f));
 		return p;
 	}
 }
