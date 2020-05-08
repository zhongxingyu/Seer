 package prototype;
 
 
 
 import java.awt.Rectangle;
 import java.awt.event.KeyEvent;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 public class Player {
 	
 	private Rectangle bounding;
 	private float f_playposx;
 	private float f_playposy;
 	private short kartenPositionX;	//enstpricht dem Feld auf der Map. Zur berprfung welche Felder auf Kollision geprft werden
 	private short kartenPositionY;
 	private float speedX;
 	private float speedY;
 	private float speedGainRate=2000;
 	private float speedReductionRate=1000;
 	private float maximumSpeed=300;
 	private int worldsize_x;
 	private int worldsize_y;
 	private Map map;
 	private BufferedImage bimg;
 	private boolean isAlive = true;
	private boolean bCheck = true;  // Aktiviert Kollisionsabfrage
 	
 	
 	public Player(int x, int y, int worldsize_x, int worldsize_y, Map map){
 		try {
 			bimg = ImageIO.read(getClass().getClassLoader().getResourceAsStream("gfx/Rossi.png"));
 		} catch (IOException e) {e.printStackTrace();}
 		bounding = new Rectangle(x+10,y+10,bimg.getWidth()-20,bimg.getHeight()-20);
 		f_playposx = x;
 		f_playposy = y;
 		this.worldsize_x=worldsize_x;
 		this.worldsize_y=worldsize_y;
 		this.map=map;
 	}
 	
 	public void update(float frametime){
 		
 		if(!isAlive)return;
 		
 		if(Keyboard.isKeyDown(KeyEvent.VK_W))speedY -= speedGainRate*frametime;
 		if(Keyboard.isKeyDown(KeyEvent.VK_S))speedY += speedGainRate*frametime;
 		if(Keyboard.isKeyDown(KeyEvent.VK_A))speedX -= speedGainRate*frametime;
 		if(Keyboard.isKeyDown(KeyEvent.VK_D))speedX += speedGainRate*frametime;
 		
 		f_playposy+=speedY*frametime;
 		f_playposx+=speedX*frametime;
 		
 		if(Math.abs(speedY)<speedReductionRate/100&&!Keyboard.isKeyDown(KeyEvent.VK_W)&&!Keyboard.isKeyDown(KeyEvent.VK_S)){speedY=0;}else	//Speed geht mit der Zeit wieder runter und unter 1 geht er direkt auf 0
 		if(speedY>maximumSpeed){speedY=maximumSpeed;}else
 		if(speedY<-maximumSpeed){speedY=-maximumSpeed;}else
 		if(speedY>speedReductionRate/1000){speedY-=speedReductionRate*frametime;}else
 		if(speedY<speedReductionRate/1000){speedY+=speedReductionRate*frametime;}
 		if(Math.abs(speedX)<speedReductionRate/100&&!Keyboard.isKeyDown(KeyEvent.VK_A)&&!Keyboard.isKeyDown(KeyEvent.VK_D)){speedX=0;}else
 		if(speedX>maximumSpeed){speedX=maximumSpeed;}else
 		if(speedX<-maximumSpeed){speedX=-maximumSpeed;}else
 		if(speedX>speedReductionRate/1000){speedX-=speedReductionRate*frametime;}else
 		if(speedX<speedReductionRate/1000){speedX+=speedReductionRate*frametime;}
 		
 		
 		if(f_playposx<0){f_playposx=0;speedX=-speedX;}else
 		if(f_playposy<0){f_playposy=0;speedY=-speedY;}else
 		if(f_playposx>worldsize_x - bounding.width){f_playposx=worldsize_x - bounding.width;speedX=-speedX;}else
 		if(f_playposy>worldsize_y - bounding.height){f_playposy=worldsize_y - bounding.height;speedY=-speedY;}
 		
 		
 		
 		kartenPositionX=(short)(f_playposx/Tile.getFeldGre());
 		kartenPositionY=(short)(f_playposy/Tile.getFeldGre());
 		bounding.x = ((int) f_playposx)-10;	//Aufgrund der Natur des Bilds machen diese einrckungen Sinn
 		bounding.y = ((int) f_playposy)-10;
 		
 		System.out.println(kartenPositionX);
 		System.out.println(kartenPositionY);
 		
 		//Schalter Kollision
 		
 		
 	
 		if(bCheck){
 		
 		//WAndprfung
 		for(int tx = kartenPositionX - 1; tx < kartenPositionX + 1; tx++){
 			if(tx<0)tx=0;
 			if(tx>31)break;
 			for(int ty = kartenPositionY - 1; ty< kartenPositionY + 1; ty++){
 				if(ty<0)ty=0;
 				if(ty>17)break;
				if(map.getTile(tx, ty).getWalkOver()&&bounding.intersects(map.getTile(tx, ty).getBounding())){
 					
 					// TODO Durch bessere Methode ersetzen.
 					
 					f_playposx = 500;
 					f_playposy = 500;					
 					
 				}
 			}
 		}
 		
 		// Fallen berprfung
 		for(int tx = kartenPositionX - 1; tx < kartenPositionX + 1; tx++){
 			if(tx<0)tx=0;
 			if(tx>31)break;
 			for(int ty = kartenPositionY - 1; ty< kartenPositionY + 1; ty++){
 				if(ty<0)ty=0;
 				if(ty>17)break;
 				if(map.getTile(tx, ty).getKillYou()&&bounding.intersects(map.getTile(tx, ty).getBounding())){
 					
 					//ggf. Ersetzen
 					
 					isAlive = false;					
 					
 				}
 			}
 		}
 		}
 	}
 	
 	public Rectangle getBounding(){
 		return bounding;
 	}
 	
 	public BufferedImage getBimg(){
 		return bimg;
 	}
 	
 	public void respawn(){
 		f_playposx = 500;
 		f_playposy = 500;	
 		isAlive = true;
 	}
 	
 	public void bCheckOn(){
 		bCheck = true;
 	}
 	
 	public void bCheckOff(){
 		bCheck = false;
 	}
 	
 
 }
