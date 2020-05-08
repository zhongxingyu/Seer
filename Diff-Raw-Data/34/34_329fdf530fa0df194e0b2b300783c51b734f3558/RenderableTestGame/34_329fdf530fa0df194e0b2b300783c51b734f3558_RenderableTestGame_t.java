 package test.RenderableTest;
 
 import java.awt.Color;
 import java.awt.event.KeyEvent;
 import java.util.Random;
 
 import com.Capslock.Engine.game;
 import com.Capslock.Engine.Controls.Keyboard.CKeyEvent;
 import com.Capslock.Engine.Math.CVector2D;
 import com.Capslock.Engine.Physics.Movable;
 import com.Capslock.Engine.Render.Camera;
 import com.Capslock.Engine.Render.RenderTarget;
 import com.Capslock.Engine.Render.Renderable;
 import com.Capslock.Engine.Utility.EngineInstance;
 
 public class RenderableTestGame extends game{
 	
 	Random random = new Random(System.currentTimeMillis());
 	
 	int cameraaccel = 10000;
 	int randren1speed = 600;
 	
 	CVector2D EarthquakeVector;
 	int earthquakefactor = 10;
 	int earthquakespeed = 30; //number of millis between changing the screen position
 
 	long lastquake = 0;
 
 	public RenderableTestGame(EngineInstance e) {
 		super(e);
 	}
 
 	BasicEntity e = new BasicEntity();
 	RenderTarget r = new RenderTarget(this.Engine.getScreen().getWidth(), this.Engine.getScreen().getHeight());
 	Camera c = new Camera(this.Engine.getScreen().getWidth(), this.Engine.getScreen().getHeight());
 	Camera c2 = new Camera(35+50+35, 35+50+35);
 	RandomRenderable randren1 = new RandomRenderable();
 	AxesRenderable a = new AxesRenderable(c);
 	AxesRenderable a2 = new AxesRenderable(c2);
 	
 	EarthquakeZone earthquakezone = new EarthquakeZone();
 	
 	@Override
 	public void init() {
 		Engine.getScreen().setbackgroundcolor(Color.black);
 		e.setPosition(new CVector2D(-250, -250));
 		
 		((Movable) c2).setPosition(new CVector2D(100, 100));
 		
 	}
 
 	@Override
 	public void input() {
 		
 		if(this.Engine.getKeyboard().keyPressed(KeyEvent.VK_UP)){
 			e.setAcceleration(e.getAcceleration().getX(), -cameraaccel);
 		}else if(this.Engine.getKeyboard().keyPressed(KeyEvent.VK_DOWN)){
 			e.setAcceleration(e.getAcceleration().getX(), cameraaccel);
 		}else{
 			e.setAcceleration(e.getAcceleration().getX(), 0);
 		}
 		
 		if(this.Engine.getKeyboard().keyPressed(KeyEvent.VK_LEFT)){
 			e.setAcceleration(-cameraaccel, e.getAcceleration().getY());
 		}else if(this.Engine.getKeyboard().keyPressed(KeyEvent.VK_RIGHT)){
 			e.setAcceleration(cameraaccel, e.getAcceleration().getY());
 		}else{
 			e.setAcceleration(0, e.getAcceleration().getY());
 		}
 		
 		
 		
 		if(this.Engine.getKeyboard().keyPressed(KeyEvent.VK_A)){
 			CVector2D newpos = new CVector2D(randren1.getPosition().getX()-(randren1speed/Engine.getFrameTimer().getRealFramerate()), randren1.getPosition().getY());
 			randren1.setPosition(newpos);
 		}else if(this.Engine.getKeyboard().keyPressed(KeyEvent.VK_D)){
 			CVector2D newpos = new CVector2D(randren1.getPosition().getX()+(randren1speed/Engine.getFrameTimer().getRealFramerate()), randren1.getPosition().getY());
 			randren1.setPosition(newpos);
 		}
 		
 		if(this.Engine.getKeyboard().keyPressed(KeyEvent.VK_W)){
 			CVector2D newpos = new CVector2D(randren1.getPosition().getX(), randren1.getPosition().getY()-(randren1speed/frametimer.getRealFramerate()));
 			randren1.setPosition(newpos);
 		}else if(this.Engine.getKeyboard().keyPressed(KeyEvent.VK_S)){
 			CVector2D newpos = new CVector2D(randren1.getPosition().getX(), randren1.getPosition().getY()+(randren1speed/frametimer.getRealFramerate()));
 			randren1.setPosition(newpos);
 		}
 		
 		CKeyEvent kevent = Engine.getKeyboard().nextKeyEvent();
 		
 		while(kevent!=null){
 			if(kevent.getKeyCode() == KeyEvent.VK_PERIOD && kevent.getEventType() == CKeyEvent.KEYEVENT_PRESSED){
 				randren1.addchild();
 			}else if(kevent.getKeyCode() == KeyEvent.VK_COMMA && kevent.getEventType() == CKeyEvent.KEYEVENT_PRESSED){
 				randren1.removechild();
 			}
 			
 			kevent = Engine.getKeyboard().nextKeyEvent();
 		}
 		
 	}
 
 	@Override
 	public void update() {
 		
 		try{
			//Thread.sleep(20);
 		}catch(Exception e){}
 		
 		e.move(Engine.getFrameTimer().getRealFramerate());
 		
 		c.setViewPosition(new CVector2D(e.getPosition().getX(), e.getPosition().getY()));
 		c2.setViewPosition(randren1.getPosition().subtract(new CVector2D(35, 35)));
 		
 		if(randren1.getCollidableArea().collides(earthquakezone.getCollidableArea())){
 			if(System.currentTimeMillis()>lastquake+earthquakespeed){
 				EarthquakeVector = new CVector2D(random.nextInt()%earthquakefactor, random.nextInt()%earthquakefactor);
 				lastquake = System.currentTimeMillis();
 			}
 		}else{
 			EarthquakeVector = new CVector2D(0, 0);
 		}
 		
 	}
 
 	@Override
 	public void draw() {
 		
 		c.addtoRenderQueue(new Renderable(){
 			public void draw(RenderTarget Target, CVector2D DesiredRenderPosition){
 				Target.getGraphics().setColor(new Color(168, 220, 233));
 				Target.getGraphics().fillRect(0, 0, Target.getImage().getWidth(), Target.getImage().getHeight());
 			}
 		});
 		
 		c.addtoRenderQueue(earthquakezone);
 		
 		c.addtoRenderQueue(randren1);
 		//c.addtoRenderQueue(randren2);
 		c.addtoRenderQueue(a);
 		
 		
 		c2.addtoRenderQueue(new Renderable(){
 			public void draw(RenderTarget Target, CVector2D DesiredRenderPosition){
 				Target.getGraphics().setColor(new Color(168, 220, 233));
 				Target.getGraphics().fillRect(0, 0, Target.getImage().getWidth(), Target.getImage().getHeight());
 			}
 		});
 		
 		c2.addtoRenderQueue(earthquakezone);
 		
 		c2.addtoRenderQueue(randren1);
 		//adding the border to the render queue
 		c2.addtoRenderQueue(a2);
 		c2.addtoRenderQueue(new Renderable(){
 			public void draw(RenderTarget Target, CVector2D DesiredRenderPosition){
 				Target.getGraphics().setColor(Color.BLACK);
 				Target.getGraphics().drawRect(0, 0, Target.getImage().getWidth()-1, Target.getImage().getHeight()-1);
 			}
 		});
 
 		//Engine.getScreen().getsizex()-25-35-50-35, Engine.getScreen().getsizey()-25-35-50-35
 		((Movable) c2).setPosition(new CVector2D(Math.floor(c.getViewPosition().getX())+Engine.getScreen().getsizex()-25-35-50-35, Math.floor(c.getViewPosition().getY())+Engine.getScreen().getsizey()-25-35-50-35));
 		
 		c.addtoRenderQueue((Renderable) c2);
 		
 		r.drawRenderable((Renderable) c,EarthquakeVector);
 		
 		r.getGraphics().drawString("Framerate:  "+Engine.getFrameTimer().getRealFramerate(), 5, 15);
 		r.getGraphics().drawString("Camera velocity:  "+e.getVelocity().getString(), 5, 30);
 		
 		Engine.getScreen().drawRenderTarget(r);	
 		
 		r.clear();
 	}
 
 	@Override
 	public int quit() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 }
