 package nitrogene.weapon;
 
 import java.util.ArrayList;
 
 import nitrogene.core.Craft;
 import nitrogene.core.CursorSystem;
 import nitrogene.core.SLaser;
 import nitrogene.core.Zoom;
 import nitrogene.util.TickSystem;
 import nitrogene.world.ArenaMap;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Sound;
 
 public class LaserLauncher {
 	private float desx, desy, x, y, camX, camY;
 	public ArrayList<SLaser> slaserlist = new ArrayList<SLaser>();
 	public int accuracy, timer, maxtime,  damage;
 	private double mangle;
 	private float mmangle, craftX, craftY, size, speed;
 	public boolean isRotating;
 	private int interburst, outerburst, burstnumber;
 	public Craft parent;
 	private ArenaMap map;
 	private Image image;
 	private Image proje;
 	private Sound firesound;
 	
 	@Deprecated
 	public LaserLauncher(Craft w, float xpos, float ypos, Image image, int accuracy, int time, float speed, int damage, float size, Image proj) throws SlickException{
 		parent = w;
 		this.x = xpos;
 		this.y = ypos;
 		desx = 0;
 		desy = 0;
 		this.accuracy = accuracy;
 		mangle = 0;
 		mmangle = 0f;
 		this.maxtime = time;
 		this.image = image;
 		isRotating = false;
 		craftX = 0;
 		craftY = 0;
 		camX = 0;
 		camY = 0;
 		this.speed = speed;
 		this.damage = damage;
 		this.size = size;
 		proje = proj;
 		firesound = new Sound("res/sound/laser1final.ogg");
 	}
 	
 	@Deprecated
 	public LaserLauncher(Craft w, float xpos, float ypos, Image image, int accuracy, int time, float speed, int damage, float size) throws SlickException{
 		parent = w;
 		this.x = xpos;
 		this.y = ypos;
 		desx = 0;
 		desy = 0;
 		this.accuracy = accuracy;
 		mangle = 0;
 		mmangle = 0f;
 		this.maxtime = time;
 		this.image = image;
 		isRotating = false;
 		craftX = 0;
 		craftY = 0;
 		camX = 0;
 		camY = 0;
 		this.speed = speed;
 		this.damage = damage;
 		this.size = size;
 		proje = new Image("res/LaserV2ro.png");
 		firesound = new Sound("res/sound/laser1final.ogg");
 	}
 	public LaserLauncher(Craft w, ArenaMap map, float xpos, float ypos, EnumWeapon stat){
 		parent = w;
 		this.x = xpos;
 		this.y = ypos;
 		this.map = map;
 		desx = 0;
 		desy = 0;
 		this.accuracy = stat.accuracy;
 		mangle = 0;
 		mmangle = 0f;
 		this.interburst = stat.interburst;
 		this.outerburst = stat.outerburst;
 		this.burstnumber = stat.burstnumber;
 		try {
 			this.image = new Image(stat.image);
 			this.proje = new Image(stat.laserimage);
 			this.firesound = new Sound(stat.firesound);
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 		isRotating = false;
 		craftX = 0;
 		craftY = 0;
 		camX = 0;
 		camY = 0;
 		this.speed = stat.speed;
 		this.damage = stat.damage;
 		this.size = stat.size;
 	}
 	
 	public void setTarget(float desx, float desy){
 		this.desx = desx;
 		this.desy = desy;
 		camX = 0;
 		camY = 0;
 	}
 	
 	public void setTarget(float desx, float desy, float camX, float camY){
 		this.desx = desx;
 		this.desy = desy;
 		this.camX = camX;
 		this.camY = camY;
 	}
 	
 	public float getTargetX(){
 		return desx;
 	}
 	
 	public float getTargetY(){
 		return desy;
 	}
 	
 	public void update(float craftX,float craftY){
 		this.craftX = craftX;
 		this.craftY = craftY;
 		//if(this.getTimer().isPauseLocked){
 		//	CursorSystem.changeCursor("redfire");
 		//}else CursorSystem.changeCursor("greenfire");
 	}
 	
 	public void fire() throws SlickException{
 		slaserlist.add(new SLaser(x+craftX,y+craftY, (float) (camX*Zoom.getZoom().inverse)+desx, (float) (camY*Zoom.getZoom().inverse)+desy,
 				accuracy, speed, damage, size, proje, map));
 	}
 	
 	public float getAngle(){
 			double mecX = (desx+(camX*Zoom.getZoom().inverse) - (x+craftX));
 			double mecY = (desy+(camY*Zoom.getZoom().inverse) - (y+craftY));
 			mangle = Math.toDegrees(Math.atan2(mecY,mecX));
 			mmangle = (float) mangle;
 			return mmangle;
 	}
 	
 	public Image getImage(){
 		return image;
 	}
 	
 	public float getX(){
 		return x;
 	}
 	
 	public float getY(){
 		return y;
 	}
 	public WeaponTimer getTimer() {
 		return TickSystem.getTimer(this);
 	}
 	public int getBurstNumber(){
 		return burstnumber;
 	}
 	public int getInterburst(){
 		return interburst;
 	}
 	public int getOuterburst(){
 		return outerburst;
 	}
 }
