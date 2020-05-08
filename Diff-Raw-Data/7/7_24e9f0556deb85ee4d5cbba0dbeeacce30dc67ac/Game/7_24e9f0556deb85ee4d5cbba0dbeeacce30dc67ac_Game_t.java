 import static org.lwjgl.opengl.GL11.*;
  
 import org.lwjgl.opengl.*;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.*;
  
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
  
 import org.newdawn.slick.opengl.Texture;
 import org.newdawn.slick.opengl.TextureLoader;
 
 import de.matthiasmann.twl.utils.PNGDecoder;
  
 public class Game{
 	private static ArrayList<Entity> entities = new ArrayList<Entity>();
 	private static ArrayList<Entity> removeList = new ArrayList<Entity>();
     public Map map;
     private MyTank player;
     static private int camera_x,camera_y,camera_w,camera_h;
     static final int WORLD_W,WORLD_H;
     private float gunRotation = 0;
     float bodyAng = 0;
     private int maxSpeed = 20,speed = 2;
     private boolean keyControlRelease;
     private static long timerTicksPerSecond = Sys.getTimerResolution();
     private long lastLoopTime = getTime();
     private long lastFpsTime;
     private int fps;
     public static SoundManager soundManager;
     public static int SOUND_SHOT;
     public static int SOUND_HIT;
     
     public float initBulletX,initBulletY;
     
 	private EnemyTank[] enemyTank;
 	public static int numEnemy;
     
     static{
     	//initial world size
         WORLD_W = 1920;
         WORLD_H = 1200;
     }
    
     public Game() throws IOException, LWJGLException{
     	//initialed value of camera
     	camera_x = 0;
         camera_y = 0;
         camera_w = 640;
         camera_h = 480;
         
         Display.setIcon(new ByteBuffer[] {
 		        loadIcon(getClass().getResource("game.png"))  // "bin/gameIcon.png" size 32x32
 		    });
         //Mouse.setGrabbed(true);
 
         //initial display
         Display.setDisplayMode(new DisplayMode(camera_w, camera_h));
         Display.create();
         
         entities.clear();
         soundManager = new SoundManager();
 		soundManager.initialize(8);
 		SOUND_SHOT   = soundManager.addSound("shot.wav");
 		SOUND_HIT    = soundManager.addSound("hit.wav");
         
         // set game
         map = new Map();
         player = new MyTank(this,100);
         player.setPositionToMap(2,3);
         player.setGun(player.gunType.MINIGUN);
         Brick[] brick = new Brick[11];
         for(int i = 0;i < brick.length;i++){
         	brick[i] = new Brick(this,30);
         	brick[i].setPositionToMap(i+3, 3);
         	entities.add(brick[i]);
         }
         entities.add(player);
 		enemyTank = new EnemyTank[5];
 		for (int i = 0; i < enemyTank.length; i++) {
			enemyTank[i] = new EnemyTank(this,50);
 			enemyTank[i].setPositionToMap(i+5, 4);
 			enemyTank[i].setBodyAngle(39);
 			entities.add(enemyTank[i]);
 		}
 		numEnemy = enemyTank.length;
                
         //initialization opengl code
         glMatrixMode(GL_PROJECTION);
         glLoadIdentity();
         //glOrtho(0 ,640 ,480 ,0 ,-1 , 1);
         glOrtho(camera_x ,640+camera_x ,480+camera_y ,camera_y ,-1 , 1);
         glMatrixMode(GL_MODELVIEW);
         glEnable(GL_TEXTURE_2D);
         
         glEnable(GL_BLEND);
         glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
 
         while(!Display.isCloseRequested()){
         	//clear screen
             glClear(GL_COLOR_BUFFER_BIT);
             
             long delta = getTime() - lastLoopTime;
     		lastLoopTime = getTime();
     		lastFpsTime += delta;
     		fps++;
 
     		// update our FPS
     		if (lastFpsTime >= 1000) {
     			Display.setTitle("Awesome Tank (FPS: " + fps + ")");
     			lastFpsTime = 0;
     			fps = 0;
     		}
     		
     		if(numEnemy == -2){
     			map.createMap(2);
     			entities.clear();
     			numEnemy = 5;
     		}
     		initBulletX = (float)(player.x-Math.cos(0.0174532925*gunRotation)*player.width/1.5);
     		initBulletY = (float)(player.y-Math.sin(0.0174532925*gunRotation)*player.height/1.5);
            
             /*sky.bind();
             glBegin(GL_QUADS);
             	glTexCoord2f(0,0);
             	glVertex2f(0 ,0);//upper left
             	glTexCoord2f(1,0);
             	glVertex2f(WORLD_W ,0);//upper right
             	glTexCoord2f(1,1);
             	glVertex2f(WORLD_W ,WORLD_H);//bottom right
             	glTexCoord2f(0,1);
             	glVertex2f(0 ,WORLD_H);//bottom left
             glEnd();*/
             
     		input();
     		
             if(!soundManager.isPlayingSound()){
             	for ( Entity entity : entities ) {
                 	if(entity instanceof MyTank)
                 		player.move(delta, bodyAng);
                 	else
                 		entity.move(delta);
                 	if(entity instanceof Bullet){
     					if(map.blocked((int)entity.x/map.TILE_SIZE, (int)entity.y/map.TILE_SIZE)){
     						((Bullet) entity).reinitialize(initBulletX,initBulletY ,((Bullet)entity).getMoveSpeed(), ((Bullet)entity).getMoveSpeed());
     						removeList.add(entity);
     					}
     				}
     			}
             }
             
             // check collisions and move
     		for (int p = 0; p < entities.size(); p++) {
     			for (int s = p + 1; s < entities.size(); s++) {
     				Entity me = entities.get(p);
     				Entity him = entities.get(s);
     				if (me.collidesWith(him)) {
     					me.collidedWith(him);
     					him.collidedWith(me);
     					/*if(him.getClass() == MyTank.class){
     						System.out.println("\ny axis");
     						((MyTank)him).moveBack();
     					}*/
     				}
     			}
     		}
             
             //set camera
             setCamera();
             glMatrixMode(GL_PROJECTION);
             glLoadIdentity();
             glOrtho(camera_x ,640+camera_x ,480+camera_y ,camera_y ,-1 , 1);
             
     		entities.removeAll(removeList);
     		removeList.clear();
     		
             //draw
             map.draw();
             for ( Entity entity : entities ) {
             	if(entity instanceof MyTank)
             		player.draw();
             	else
             		entity.draw();
 			}
             
             Display.update();
             Display.sync(60);
         }
     }
    
     private void input() {
     	boolean KEY_W = Keyboard.isKeyDown(Keyboard.KEY_W);
     	boolean KEY_S = Keyboard.isKeyDown(Keyboard.KEY_S);
     	boolean KEY_D = Keyboard.isKeyDown(Keyboard.KEY_D);
     	boolean KEY_A = Keyboard.isKeyDown(Keyboard.KEY_A);
     	boolean KEY_1 = Keyboard.isKeyDown(Keyboard.KEY_1);
     	boolean KEY_2 = Keyboard.isKeyDown(Keyboard.KEY_2);
     	boolean KEY_3 = Keyboard.isKeyDown(Keyboard.KEY_3);
     	boolean KEY_4 = Keyboard.isKeyDown(Keyboard.KEY_4);
     	boolean KEY_5 = Keyboard.isKeyDown(Keyboard.KEY_5);
     	boolean KEY_6 = Keyboard.isKeyDown(Keyboard.KEY_6);
     	
         if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)){
         	soundManager.destroy();
         	Display.destroy();
             System.exit(0);
         }
         
         /*if(keyControlRelease) {
         	//Display.setTitle("ss");
         	if(speed > 0)
         		speed -= 5;
         }*/
         
         /*while (Keyboard.next()) {
             if (Keyboard.getEventKeyState()) {
                 if (Keyboard.getEventKey() == Keyboard.KEY_W) {
                 	//########################################
                 	if(speed < maxSpeed)
                     	speed += 5;
                     box.move(0, -speed);
                     keyControlRelease = false; 
                 	Display.setTitle("w pres");
                 	player.move(0, -5);
                 }
                 if (Keyboard.getEventKey() == Keyboard.KEY_S) {
                 	keyControlRelease = false;
                 	player.move(0, 5);
                 }
                 if (Keyboard.getEventKey() == Keyboard.KEY_D) {
                 	keyControlRelease = false;
                 	player.move(5, 0);
                 }
                 if (Keyboard.getEventKey() == Keyboard.KEY_A) {
                 	keyControlRelease = false;
                 	player.move(-5, 0);
                 }
             }
             else {
                 if (Keyboard.getEventKey() == Keyboard.KEY_W) {
                 	Display.setTitle("w release");
                 	keyControlRelease = true;
                 }
                 if (Keyboard.getEventKey() == Keyboard.KEY_S) {
                 	keyControlRelease = true;
                 }
                 if (Keyboard.getEventKey() == Keyboard.KEY_D) {
                 	keyControlRelease = true;
                 }
                 if (Keyboard.getEventKey() == Keyboard.KEY_A) {
                 	keyControlRelease = true;
                 }
             }
         }*/
         
         player.setDX(0);
         player.setDY(0);
         if(KEY_W && !KEY_S){
         	bodyAng = 90;
         	if(KEY_A)
         		bodyAng = 45;
         	if(KEY_D)
         		bodyAng = 135;
         	player.setDY(-speed);
         }
         if(KEY_S && !KEY_W){
         	bodyAng = 270;
         	if(KEY_A)
         		bodyAng = 315;
         	if(KEY_D)
         		bodyAng = 225;
         	player.setDY(speed);
         }
         if(KEY_D && !KEY_A){
         	bodyAng = 180;
         	if(KEY_W)
         		bodyAng = 135;
         	if(KEY_S)
         		bodyAng = 225;
         	player.setDX(speed);
         }
         if(KEY_A && !KEY_D){
         	bodyAng = 0;
         	if(KEY_W)
         		bodyAng = 45;
         	if(KEY_S)
         		bodyAng = 315;
         	player.setDX(-speed);
         }
         if(KEY_1)
         	player.setGun(player.gunType.MINIGUN);
         if(KEY_2)
         	player.setGun(player.gunType.SHOTGUN);
         if(KEY_3)
         	player.setGun(player.gunType.RICOCHET);
         if(KEY_4)
         	player.setGun(player.gunType.CANNON);
         if(KEY_5)
         	player.setGun(player.gunType.ROCKET);
         if(KEY_6)
         	player.setGun(player.gunType.LASER);
         
         
         if(!soundManager.isPlayingSound()){
         	if(Mouse.isButtonDown(0)){
             	player.Fire(initBulletX,initBulletY,gunRotation);
             }
         }
 	}
     
     private void setCamera(){
     	// mouse position (0,0) at center screen
     	float mouseX = (Mouse.getX() - camera_w/2)*0.5f;
     	float mouseY = (Mouse.getY() - camera_h/2)*0.5f;
     	
     	String str = String.valueOf(gunRotation);
     	
     	// Mouse.getX(),Mouse.getY() position (0,0) at bottom left
     	//Display.setTitle(str);
     	//gunRotation = 57.2957795f*(float)Math.atan2(camera_h/2 - Mouse.getY(),Mouse.getX() - camera_w/2);
     	gunRotation = (float) (180/Math.PI*(float)Math.atan2(camera_h/2 - Mouse.getY(),Mouse.getX() - camera_w/2));
     	gunRotation += 180;
     	player.setGunAngle(gunRotation);
 
    	camera_x = (int)mouseX + (int)(player.get_centerX()-player.halfSize - camera_w/2);
    	camera_y = -(int)mouseY + (int)(player.get_centerY()-player.halfSize - camera_h/2);
     }
     
     public void addEntity(Entity entity) {
     	entities.add(entity);
 	}
     
     public static void removeEntity(Entity entity) {
 		removeList.add(entity);
 	}
     
     public static long getTime() {
 		return (Sys.getTime() * 1000) / timerTicksPerSecond;
 	}
 
 	private ByteBuffer loadIcon(URL url) throws IOException {
         InputStream is = url.openStream();
         try {
             PNGDecoder decoder = new PNGDecoder(is);
             ByteBuffer bb = ByteBuffer.allocateDirect(decoder.getWidth()*decoder.getHeight()*4);
             decoder.decode(bb, decoder.getWidth()*4, PNGDecoder.Format.RGBA);
             bb.flip();
             return bb;
         } finally {
             is.close();
         }
     }
    
     public static void main(String[] args) throws IOException, LWJGLException {
             new Game();
     }
 }
