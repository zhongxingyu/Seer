 package mickey.com.Dust;
 
 import mickey.com.Map.Area;
 import mickey.com.Sprite.AnimatedUnit;
 import mickey.com.Sprite.Sprite;
 import mickey.com.Sprite.VirtualAnimatedEnemy;
 import mickey.com.Sprite.VirtualSpaceAnimatedSprite;
 import mickey.com.Sprite.VirtualSpaceSprite;
 
 import java.io.File;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.tiled.TiledMap;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.particles.ParticleSystem;
 import org.newdawn.slick.particles.ConfigurableEmitter;
 import org.newdawn.slick.particles.ParticleIO;
 
 import ethan.com.Camera.Camera;
 
 public class GamePlayBetterCamera extends BasicGameState{
 
 	int stateID = -1;
 	public static AnimatedUnit player;
 	
 	ParticleSystem part;
 	ParticleSystem part2;
 	Rectangle viewPort = new Rectangle(0,0,800,600);
 	Area testArea;
 	Camera camera;
 
 	public GamePlayBetterCamera(int stateID) {
 		this.stateID = stateID;
 	}
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame sb)
 			throws SlickException {
 		// TODO Auto-generated method stub
 		initPlayer(gc, sb);
 		camera = new Camera(player.getLocation());
 		testArea = new Area(new TiledMap("data/testArea2.tmx"));
		camera.setMinBoundingBox(0, 0);
		camera.setMaxBoundingBox(testArea.getMap().getWidth()*testArea.getMap().getTileWidth()-gc.getWidth(), testArea.getMap().getHeight()*testArea.getMap().getTileHeight()-gc.getHeight());
 	}
 
 	public void initPlayer(GameContainer gc, StateBasedGame sb)
 			throws SlickException {
 		// TODO Auto-generated method stub
 		player = new AnimatedUnit(new Image("images/characterSpriteSheet.png"), 0);
 		player.setSpeed(0.2f);
 		//player.setX(gc.getWidth()/2 - 32);
 		//player.setY(gc.getHeight()/2 - 32);
 		
 		//enemy = new VirtualAnimatedEnemy(new Image("images/characterSpriteSheet.png"), 0, gc.getWidth()/2 - 32, gc.getHeight()/2 - 32);
 	   //enemy.setSpeed(0.2f);
 
 		Image temp = new Image("images/particle.png", false);
 		part = new ParticleSystem(temp, 1500);
 
 		temp = new Image("images/particle2.png", false);
 		part2 = new ParticleSystem(temp, 1500);
 
 		try{
 			File xmlFile = new File("data/test_emitter.xml");
 			ConfigurableEmitter emitter = ParticleIO.loadEmitter(xmlFile);
 			emitter.setPosition(20,600);
 			part.addEmitter(emitter);
 
 			xmlFile = new File("data/test_emitter2.xml");
 			ConfigurableEmitter emitter2 = ParticleIO.loadEmitter(xmlFile);
 			emitter2.setPosition(500,700);
 			part2.addEmitter(emitter2);
 		}
 		catch (Exception e) {
 			System.out.println("Exception: " + e.getMessage());
 			e.printStackTrace();
 			System.exit(0);
 		}
 
 		part.setBlendingMode(ParticleSystem.BLEND_ADDITIVE);
 		part2.setBlendingMode(ParticleSystem.BLEND_COMBINE);
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame sb, Graphics g)
 			throws SlickException {
 		camera.drawCamera(g);
 		testArea.draw(g);
 		//g.translate(-viewPort.getX(), -viewPort.getY());//Written by  Ethan
 		g.setColor(new Color(44,155,26));
 		//g.fillRect(0,0, gc.getWidth(), gc.getHeight());
 		g.drawString("Gameplay better camera", 300, 10);
 		player.draw(g);
 		//enemy.draw(g);
 		part.render();
 		part2.render();
 		System.out.println(player.getX() +"                        "+player.getY());
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sb, int delta)
 			throws SlickException {
 		Input input = gc.getInput();
 
 		part.update(delta);
 		part2.update(delta);
 		//enemy.act(testArea, delta, gc);
 		if(input.isKeyDown(Input.KEY_S) && input.isKeyDown(Input.KEY_D))
 		{
 			player.moveDownRight(delta);
 			player.update(delta);
 		}
 		else if(input.isKeyDown(Input.KEY_S) && input.isKeyDown(Input.KEY_A))
 		{
 			player.moveDownLeft(delta);
 			player.update(delta);
 		}
 		else if(input.isKeyDown(Input.KEY_W) && input.isKeyDown(Input.KEY_D))
 		{
 			player.moveUpRight(delta);
 			player.update(delta);
 		}
 		else if(input.isKeyDown(Input.KEY_W) && input.isKeyDown(Input.KEY_A))
 		{
 			player.moveUpLeft(delta);
 			player.update(delta);
 		}
 		else if(input.isKeyDown(Input.KEY_S))
 		{
 			//viewPort.setY((float) (viewPort.getY()+(0.18*delta)));//written by Ethan
 			//player.moveDown(delta);
 			//player.moveForward(testArea, gc, sb, delta);
 			player.moveDown(delta);
 			player.update(delta);
 		}
 		else if(input.isKeyDown(Input.KEY_D))
 		{
 			//viewPort.setX((float) (viewPort.getX()+(0.18*delta)));//written by Ethan
 			player.moveRight(delta);
 			player.update(delta);
 		}	
 		else if(input.isKeyDown(Input.KEY_A))
 		{
 			//viewPort.setX((float) (viewPort.getX()-(0.18*delta)));//written by Ethan
 			//player.moveLeft(delta);
 			player.moveLeft(delta);
 			player.update(delta);
 		}
 		else if(input.isKeyDown(Input.KEY_W))
 		{
 			//viewPort.setY((float) (viewPort.getY()-(0.18*delta)));//written by Ethan
 			//player.moveUp(delta);
 			player.moveUp(delta);
 			player.update(delta);
 		}
 		
 		camera.CameraMove(player.getLocation());
 
 
 		//exit the game
 		if(input.isKeyPressed(Input.KEY_ESCAPE))
 		{
 			System.out.println("Game exited due to escape key.");
 			System.exit(0);
 		}
 	}
 
 	@Override
 	public int getID() {
 		// TODO Auto-generated method stub
 		return stateID;
 	}
 
 }
