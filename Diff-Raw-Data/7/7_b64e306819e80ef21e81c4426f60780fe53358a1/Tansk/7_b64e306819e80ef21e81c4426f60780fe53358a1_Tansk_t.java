 package chalmers.TDA367.B17;
 
 import chalmers.TDA367.B17.model.*;
 import org.newdawn.slick.*;
 import java.awt.Point;
 import java.util.*;
 
 public class Tansk extends BasicGame implements MouseListener {
 	World world;
 	ArrayList<Player> players;
 	Player playerOne;
 
 	Image map = null;
 	SpriteSheet tankSprite  = null;
 	SpriteSheet turretSprite = null;
 	Point mouseCoords;
 
 	Input input;
 
 	public Tansk() {
 		super("Tansk!");
 	}
  
 	@Override
 	public void init(GameContainer gc) throws SlickException {
 		gc.setMouseCursor(new Image("data/crosshair.png"), 16, 16);
 		world = new World();
 		playerOne = new Player("Player One");
 		players = new ArrayList<Player>();
 		players.add(playerOne);
 
		turretSprite = new SpriteSheet("data/turret.png", 45, 65);
 		map = new Image("data/map.png");
 		input = gc.getInput();
 		input.addMouseListener(this);
 		mouseCoords = new Point();
 		turretSprite.setCenterOfRotation(22.5f, 22.5f);
 		tankSprite = new SpriteSheet("data/tank.png", 65,85);
 	}
  ;
 	@Override
 	public void update(GameContainer gc, int delta) throws SlickException {
 		if(input.isKeyDown(Input.KEY_W)){
 			playerOne.getTank().accelerate(delta);
 		}else if(input.isKeyDown(Input.KEY_S)){
 			playerOne.getTank().reverse(delta);
 		}else{
 			playerOne.getTank().friction(delta);
 		}
 
 		if(input.isKeyDown(Input.KEY_A) && !input.isKeyDown(Input.KEY_D)){
 			if(input.isKeyDown(Input.KEY_S)){
 				playerOne.getTank().turnRight(delta);
 			} else {
 				playerOne.getTank().turnLeft(delta);
 			}
 		}
 
 		if(input.isKeyDown(Input.KEY_D) && !input.isKeyDown(Input.KEY_A)){
 			if(input.isKeyDown(Input.KEY_S)){
 				playerOne.getTank().turnLeft(delta);
 			} else {
 				playerOne.getTank().turnRight(delta);
 			}
 		}
		if(input.isKeyDown(Input.KEY_TAB)){
			turretSprite = new SpriteSheet("data/quaketurr.png", 45, 65);
			turretSprite.setCenterOfRotation(22.5f, 22.5f);
		}
 		
 		if(input.isKeyDown(Input.KEY_ESCAPE)){
 			gc.exit();
 		}
 		
 		playerOne.getTank().update(delta, mouseCoords);
       
 		tankSprite.setRotation((float) (playerOne.getTank().getDirection().getTheta() + 90));	
         turretSprite.setRotation(playerOne.getTank().getTurret().getRotation());
 	}
 
 	@Override
 	public void render(GameContainer gc, Graphics g) throws SlickException {
 		map.draw();
 		tankSprite.draw(playerOne.getTank().getImagePosition().x, playerOne.getTank().getImagePosition().y);
 		turretSprite.draw(playerOne.getTank().getTurret().getImagePosition().x , playerOne.getTank().getTurret().getImagePosition().y);
 
 		debugRender(g);
 	}
 
 	public void debugRender(Graphics g){
 		g.setColor(Color.black);
 		g.drawString("tankPosX:   " + playerOne.getTank().getPosition().x,  10, 30);
 		g.drawString("tankPosY:   " + playerOne.getTank().getPosition().y,  10, 50);
 		g.drawString("tankAng:    " + playerOne.getTank().getRotation(),	10, 70);
 		g.drawString("tankImgAng: " + (tankSprite.getRotation()),			10, 90);
 
 		g.drawString("turPosX:   " + playerOne.getTank().getTurret().getPosition().x, 300, 30);
 		g.drawString("turPosY:   " + playerOne.getTank().getTurret().getPosition().y, 300, 50);
 		g.drawString("turAng:    " + playerOne.getTank().getTurret().getRotation(),	  300, 70);
 		g.drawString("turImgAng: " + turretSprite.getRotation(),		 			  300, 90);
 
 		g.drawString("mouseX: " + mouseCoords.x, 530, 30);
 		g.drawString("mouseY: " + mouseCoords.y, 530, 50);
 
 		g.drawString("speed: " + Double.toString(playerOne.getTank().getSpeed()), 530, 90);
 /*
 		g.setColor(Color.yellow);
 		g.drawLine(playerOne.getTank().getTurret().getPosition().x, playerOne.getTank().getTurret().getPosition().y, mouseCoords.x, mouseCoords.y);
 		g.setColor(Color.green);
 		g.drawLine(playerOne.getTank().getTurret().getImagePosition().x, playerOne.getTank().getTurret().getImagePosition().y,  mouseCoords.x, mouseCoords.y);
 		g.setColor(Color.red);
 		g.drawLine(playerOne.getTank().getPosition().x, playerOne.getTank().getPosition().y, mouseCoords.x, mouseCoords.y);
 		g.setColor(Color.blue);
 		g.drawLine(playerOne.getTank().getImagePosition().x, playerOne.getTank().getImagePosition().y,  mouseCoords.x, mouseCoords.y);
 */	
 	}
 
 	public void mouseMoved(int oldx, int oldy, int newx, int newy){
 		mouseCoords.setLocation(newx, newy);
 	}
 	
 	public void mouseDragged(int oldx, int oldy, int newx, int newy){
 		mouseCoords.setLocation(newx, newy);
 	}
 
 	public static void main(String[] args) throws SlickException {
 		AppGameContainer app = new AppGameContainer(new Tansk());
 
 		app.setTargetFrameRate(60);
 		app.setMaximumLogicUpdateInterval(500);
 		app.setMinimumLogicUpdateInterval(5);
 		app.setDisplayMode(800, 600, false);
 		
 		app.start();
   }
 }
