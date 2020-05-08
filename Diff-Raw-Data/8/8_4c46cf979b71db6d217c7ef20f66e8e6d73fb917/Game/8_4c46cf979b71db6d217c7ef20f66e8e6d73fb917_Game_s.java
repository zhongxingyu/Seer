 package lh.koneke.games.TerraWatt;
 
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.LWJGLException;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.newdawn.slick.Color;
 
 import lh.koneke.Guts.AtomController;
 import lh.koneke.Guts.Time;
 
 #define scrw 800
 #define scrh 640
 
 public class Game {
 	public static void main(String[] args) {
 		Game g = new Game();
 		g.run();
 	}
 	
 	public void run() {
 		init();
 		while(!Display.isCloseRequested()) {
 			update();
 			draw();
 		}
 		quit();
 	}
 	
 	public void init() {
 		Graphics.setDisplay(scrw, scrh);
 		Graphics.setupOpenGL();
 		AtomController.setup();
 		AtomController.getType("Gamepad").get(0).report();
 		
 		entities = new ArrayList<Entity>();
 		ninventory = new Inventory();
 		world = new World(25, 20);
 
 		Entity player = new Entity();
 		player.setPosition(new Vector2(12*tilesize,14*tilesize+16));
 		player.setSize(new Vector2(tilesize,tilesize));
 		player.setOffset(new Vector2(-tilesize/2,-tilesize/2));
 		player.setColor(new Color(1f,0f,0f,1f));
 		entities.add(player);
 
 		Item i = new Item();
 		i.setPosition(new Vector2(9*tilesize,14*tilesize+16));
 		i.setSize(new Vector2(16,16));
 		i.setOffset(new Vector2(-8,-8));
 		i.setColor(new Color(0f,0f,1f,1f));
 		entities.add(i);
 
 		i = new Item();
 		i.setPosition(new Vector2(8*tilesize,14*tilesize+16));
 		i.setSize(new Vector2(16,16));
 		i.setOffset(new Vector2(-8,-8));
 		i.setColor(new Color(1f,0f,1f,1f));
 		entities.add(i);
 
 		i = new Item();
 		i.setPosition(new Vector2(7*tilesize,14*tilesize+16));
 		i.setSize(new Vector2(16,16));
 		i.setOffset(new Vector2(-8,-8));
 		i.setColor(new Color(1f,0f,1f,1f));
 		entities.add(i);
 
 		for(int x = 0; x < world.getWidth(); x++) {
 			for(int y = 0; y < world.getHeight(); y++) {
 				if(y < 15) {
 					world.set(x,y,null);
 				} else {
 					Block b = new Block();
 					b.setColor(randomColor());
 					world.set(x,y,b);
 				}
 			}
 		}
 		world.set(10,14,new Block(randomColor()));
 	}
 
 	public void quit() { }
 
 	public Color randomColor() {
 		Color c = new Color(
 			random.nextFloat(),
 			random.nextFloat(),
 			random.nextFloat(),
 			1);
 		return c;
 	}
 	
 	List<Entity> entities;
 	World world;
 
 	final int mapWidth = 25;
 	final int mapHeight = 20;
 	final int tilesize = 32;
 
 	AtomController controller;
 	Inventory ninventory;
 
 	Random random = new Random();
 	boolean redraw = true;
 
 	Vector2 playerSpeed = new Vector2(0,0);
 	Vector2 playerAcc = new Vector2(0,0);
 	boolean canjump = true;
 
 	int dev_fpstimer;
 	int dev_fpsupdates;
 
 	public void update() {
 		Time.update();
 		dev_fpstimer += Time.dt;
 
 		AtomController.updateAll();
 
 		AtomController gamepad = 
 			AtomController.getType("Gamepad").get(0);
 		float x = gamepad.getValue("x");
 		x = (float)Math.floor(x * 10f)/10f;
 
 		Entity player = entities.get(0);
 		Vector2 playerPos = player.getPosition();
 		if(Math.abs(x) > 0.3) {
 			Vector2 newpos = playerPos.plus(
 				new Vector2(
 				0.25f*Time.dt*x
 				,0));
 			Vector2 gridpos =
 				getGridPosition(newpos.plus(new Vector2(
 					player.getSize().times
 						(x > 0 ? 0.5f : -0.5f).x(),0)
 				));
 			Block b = world.get((int)gridpos.x(), (int)gridpos.y());
 			if(b == null) {
 				player.getPosition().setx(newpos.x());
 			}
 		}
 
 		if(gamepad.getValue("A") == 1.0f && gamepad.getLast("A") != 1.0f) {
 			if(gamepad.getValue("rz") != 1.0f) {
 				List<Entity> copy = new ArrayList<Entity>(entities);
 				for(Entity e : entities) {
 					if(e != player &&
 						Vector2.distance(
 							e.getPosition(), player.getPosition()) < tilesize) {
 						copy.remove(e);
 						Item i = new Item();
 						i.setSize(e.getSize().clone());
 						i.setOffset(e.getOffset().clone());
 						i.setColor(e.getColor());
 						ninventory.addItem(i);
 						break;
 					}
 				}
 				entities = new ArrayList<Entity>(copy);
 			} else {
 				if(!ninventory.empty()) {
 					Item i = ninventory.drop();
 					i.setPosition(player.getPosition().clone());
 					entities.add(i);
 				}
 			}
 		}
 
 		if(gamepad.getValue("X") == 1.0f && gamepad.getLast("X") != 1.0f) {
 			float angle = gamepad.getValue("pov")*4;
 			if(angle % 1 == 0 && angle > 0) {
 				angle = angle * (float)Math.PI/2f+(float)Math.PI;
 				float ix = player.getPosition().x()+(float)Math.cos(angle)*tilesize;
 				float iy = player.getPosition().y()+(float)Math.sin(angle)*tilesize;
 				Vector2 gridpos = getGridPosition(new Vector2(ix,iy));
 				Block b = world.get((int)gridpos.x(), (int)gridpos.y());
 				if(gamepad.getValue("rz") == 1.0f) {
 					if(b != null) {
 						Item i = new Item();
 						i.setSize(new Vector2(16,16)); //TODO: constant this or summing
 						i.setPosition(gridpos.times(tilesize).plus(
 							new Vector2(tilesize/2f, tilesize/2f)));
 						i.setOffset(new Vector2(-8,-8)); //TODO: this one as well
 						i.color = b.getColor();
 						entities.add(i);
 						world.set((int)gridpos.x(), (int)gridpos.y(), null);
 					}
 				} else {
 					if(b == null) {
						Item i = ninventory.drop();
						b = new Block(i.getColor());
						world.set((int)gridpos.x(), (int)gridpos.y(), b);
 					}
 				}
 			}
 		}
 
 		#define pjump -384f
 		#define gravity 14000f
 		if(gamepad.getValue("B") == 1.0f && gamepad.getLast("B") != 1.0f && canjump) {
 			playerSpeed = new Vector2(0, pjump);
 			canjump = false; }
 		Vector2 gridPositionBeneathA = getGridPosition(
 			player.getPosition().
 				plus(player.getOffset().plus(new Vector2(32,0)).plus(player.getSize())));
 		Vector2 gridPositionBeneathB = getGridPosition(
 			player.getPosition().
 				plus(player.getOffset().plus(new Vector2(0,0)).plus(player.getSize())));
 		Vector2 gridBenA = getGridPosition(
 			player.getPosition().plus(new Vector2(-12,16)));
 		Vector2 gridBenB = getGridPosition(
 			player.getPosition().plus(new Vector2(12,16)));
 		Block beneath1 = world.get(
 			(int)gridBenA.x(), (int)gridBenA.y());
 		Block beneath2 = world.get(
 			(int)gridBenB.x(), (int)gridBenB.y());
 		if(beneath1 == null && beneath2 == null) {
 			//playerAcc.sety(playerAcc.y()+2000f*Time.dt/1000f);
 			playerAcc.sety(playerAcc.y()+gravity*Time.dt/1000f);
 		} else if (playerAcc.y() > 0)  {
 			playerAcc.sety(0);
 			playerSpeed.sety(0);
 			player.getPosition().sety(
 				gridPositionBeneathA.plus(new Vector2(0,-1)).times(tilesize)
 				.plus(player.getOffset().plus(player.getSize())).y());
 			canjump = true;
 		}
 		playerSpeed.sety(playerSpeed.y()+playerAcc.y()*Time.dt/1000f);
 		playerPos.sety(playerPos.y()+playerSpeed.y()*Time.dt/1000f);
 
 		//if(gamepad.getValue("pov") == 0.5f && gamepad.getLast("pov") != 0.5f) {
 		if(gamepad.getValue("z") > 0.5f && gamepad.getLast("z") < 0.5f) {
 			ninventory.prev(); }
 		//if(gamepad.getValue("pov") == 1.0f && gamepad.getLast("pov") != 1.0f) {
 		if(gamepad.getValue("Left Thumb") == 1.0f &&
 			gamepad.getLast("Left Thumb") != 1.0f) {
 			ninventory.next(); }
 
 		AtomController.postUpdateAll();
 
 		if(dev_fpstimer > 1000f/60f) {
 			dev_fpstimer = 0;
 			redraw = true;
 		}
 	}
 
 	public Vector2 getGridPosition(Vector2 screenPosition) {
 		float x = screenPosition.x();
 		x -= x % tilesize;
 		x = x/tilesize;
 		float y = screenPosition.y();
 		y -= y % tilesize;
 		y = y/tilesize;
 		return new Vector2(x,y);
 	}
 	
 	public void draw() {
 		//TODO: Clean me!
 		if(redraw) {
 			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
 			for(int x = 0; x < mapWidth; x++) {
 				for(int y = 0; y < mapHeight; y++) {
 					if(world.get(x,y) != null) {
 						world.get(x,y).getColor().bind();
 						float x1 = 1+x*tilesize;
 						float x2 = -1+(x+1)*tilesize;
 						float y1 = 1+y*tilesize;
 						float y2 = -1+(y+1)*tilesize;
 						Graphics.begin(Graphics.Quads);
 							Graphics.point(x1, y1);
 							Graphics.point(x2, y1);
 							Graphics.point(x2, y2);
 							Graphics.point(x1, y2);
 						Graphics.end();
 					}
 				}
 			}
 			for(Entity e : entities) {
 				e.draw(e.getPosition());
 			}
 			Entity player = entities.get(0);
 			Color.white.bind();
 			Graphics.begin(Graphics.Quads);
 				Graphics.point(
 					player.getPosition().x-2, player.getPosition().y-2);
 				Graphics.point(
 					player.getPosition().x+2, player.getPosition().y-2);
 				Graphics.point(
 					player.getPosition().x+2, player.getPosition().y+2);
 				Graphics.point(
 					player.getPosition().x-2, player.getPosition().y+2);
 			Graphics.end();
 
 			
 			AtomController gamepad = 
 				AtomController.getType("Gamepad").get(0);
 			float angle = gamepad.getValue("pov")*4;
 			if(angle % 1 == 0 && angle > 0) {
 				angle = angle * (float)Math.PI/2f+(float)Math.PI;
 				float x = player.getPosition().x()+(float)Math.cos(angle)*tilesize;
 				float y = player.getPosition().y()+(float)Math.sin(angle)*tilesize;
 				x = x - x % tilesize;
 				y = y - y % tilesize;
 				Color.red.bind();
 				Graphics.begin(Graphics.Quads);
 					Graphics.point(x,y);
 					Graphics.point(x+tilesize,y);
 					Graphics.point(x+tilesize,y+tilesize);
 					Graphics.point(x,y+tilesize);
 				Graphics.end();
 			}
 
 			ninventory.draw();
 
 			redraw = false;
 		}
 		Display.update();
 	}
 }
