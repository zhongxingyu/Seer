 package base;
 
 import java.awt.Canvas;
 
 public abstract class BatGame extends Canvas implements Runnable{
 	
 	Thread loop;
 	private int fps;
	private int renderedFrames = 0;
	private int currentFPS = 0;
	private int frameCount = 0;
	double lastCounterUpdate;
 	
 	public BatGame(int fps){
 		this.fps = fps;
 	}
 	
 	public void init(){
 		loop = new Thread(this);
 		loop.setPriority(Thread.MAX_PRIORITY);
 		loop.start();
 	}
 	
 	@Override
 	public void run() {
 		double dt;
 		double nowTime;
 		double fraction = 1000.0/(double)fps;
		double lastUpdate = System.currentTimeMillis();
 		
 		while(true){
 			nowTime = System.currentTimeMillis(); 
 			dt = nowTime - lastUpdate;
 			while(dt < fraction){
 				try{
 					Thread.sleep((long) (fraction - dt));
 					
 				} catch (Exception e){
 					e.printStackTrace();
 				}
 				dt = System.currentTimeMillis() - lastUpdate;
 			}
 			lastUpdate = System.currentTimeMillis();
			updateFPS(dt);
 			onTick((float)dt);
 		}
 	}
 	
	protected void updateFPS(double dt) {
		frameCount++;
		lastCounterUpdate += dt;
		if(lastCounterUpdate > 1000.0)
		{
			lastCounterUpdate -= 1000.0;
			currentFPS = frameCount;
			frameCount = 0;
		}
	}
	
	protected int getFPS() {
		return currentFPS;
	}
	
 	public abstract void onTick(float dt);
 }
