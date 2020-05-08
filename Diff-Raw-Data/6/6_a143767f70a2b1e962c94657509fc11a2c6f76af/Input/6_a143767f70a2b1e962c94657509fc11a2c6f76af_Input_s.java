 package net.fourbytes.shadow;
 
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.utils.Array;
 import com.badlogic.gdx.utils.ObjectIntMap;
 import com.badlogic.gdx.utils.ObjectMap;
 import net.fourbytes.shadow.entities.Cursor;
 
 public class Input {
 	
 	public static class TouchPoint {
 		public static enum TouchMode {
 			KeyInput, 
 			Cursor;
 		}
 		public TouchMode touchmode;
 		public int id = -1;
 		public int button = -1;
 		public Vector2 pos = new Vector2(-1f, -1f);
 		public TouchPoint(int x, int y, int id, int button, TouchMode touchmode) {
 			this.id = id;
 			this.button = button;
 			this.pos.set(x, y);
 			this.touchmode = touchmode;
 			if (touchmode == TouchMode.Cursor && Shadow.level != null && Shadow.level.player != null && Input.isAndroid && !Input.isOuya) {
 				this.button = -1; //Special button id for Android
 				Shadow.level.fillLayer(0);
 				Cursor c = new Cursor(new Vector2(0f, 0f), Shadow.level.layers.get(0), id);
 				c.pos.set(c.calcPos(pos));
 				Shadow.level.cursors.add(c);
 			}
 		}
 	}
 
 	public static interface KeyListener {
 		public void keyDown(Key key);
 		public void keyUp(Key key);
 	}
 
 	static OrthographicCamera cam;
 	
 	public static boolean isAndroid = false;
 	public static boolean isOuya = false;
 	public static boolean isInMenu = false;
 	
 	public static class Key {
 		public static final class Triggerer {
 			public static final int KEYBOARD = 0;
 			public static final int SCREEN = 0;//Screen simulates keyboard presses and as for now shares same ID.
 			public static final int CONTROLLER_BUTTON = 1;
 			public static final int CONTROLLER_AXIS = 2;
 		}
 
 		public String name;
 		public int[] keyid;
 		Rectangle drawrec;
 		Rectangle origrec;
 		Rectangle rec;
 		int pointer = -1;
 		int triggerer = 0;
 		
 		public boolean wasDown = false;
 		public boolean isDown = false;
 		public boolean nextState = false;
 		
 		public Key(String name, int[] keyid) {
 			this.name = name;
 			this.keyid = keyid;
			this.origrec = new Rectangle();
			this.rec = new Rectangle();
			this.drawrec = new Rectangle();
 			all.add(this);
 		}
 		
 		public void down() {
 			//System.out.println("N: "+name+"; M: D; X: "+rec.x+"; Y:"+rec.y+"; W: "+rec.width+"; H: "+rec.height);
 			tmpkeylisteners.clear();
 			tmpkeylisteners.addAll(keylisteners);
 			for (KeyListener l : tmpkeylisteners) {
 				if (l instanceof Level && ((Level)l) != Shadow.level) {
 					continue;
 				}
 				if (l instanceof GameObject && ((GameObject)l).layer.level != Shadow.level) {
 					continue;
 				}
 				l.keyDown(this);
 			}
 		}
 		
 		public void up() {
 			//System.out.println("N: "+name+"; M: U; X: "+rec.x+"; Y:"+rec.y+"; W: "+rec.width+"; H: "+rec.height);
 			tmpkeylisteners.clear();
 			tmpkeylisteners.addAll(keylisteners);
 			for (KeyListener l : tmpkeylisteners) {
 				if (l instanceof Level && ((Level)l) != Shadow.level) {
 					continue;
 				}
 				if (l instanceof GameObject && ((GameObject)l).layer.level != Shadow.level) {
 					continue;
 				}
 				l.keyUp(this);
 			}
 		}
 		
 		public void tick() {
 			wasDown = isDown;
 			isDown = nextState;
 
 			rec.width = Shadow.dispw/Shadow.touchw;
 			rec.x = origrec.x*Shadow.dispw/Shadow.touchw;
 			rec.height = Shadow.disph/Shadow.touchh;
 			rec.y = origrec.y*Shadow.disph/Shadow.touchh;
 
 			if (wasPressed()) up();
 			if (wasReleased()) down();
 		}
 		
 		public void render() {
 			if (isDown) {
 				Shadow.spriteBatch.setColor(1f, 0.5f, 0.5f, 0.575f);
 			} else {
 				Shadow.spriteBatch.setColor(1f, 1f, 1f, 0.575f);
 			}
 			Shadow.spriteBatch.draw(Images.getTextureRegion("white"), drawrec.x, drawrec.y, 1f, 1f);
 		}
 		
 		public boolean wasPressed() {
 			return wasDown && !isDown;
 		}
 		
 		public boolean wasReleased() {
 			return !wasDown && isDown;
 		}
 
 		public void setRect(float x, float y, float width, float height, boolean draw) {
 			origrec.set(x, y, width, height);
 			if (draw) {
 				drawrec.set(x, y, width, height);
 			}
 		}
 	}
 	
 	public static Array<Key> all = new Array<Key>();
 	
 	public static Key up = new Key("Up", new int[] {Keys.UP, Keys.W});
 	public static Key jump = new Key("Jump", new int[] {Keys.UP, Keys.W});
 	public static Key down = new Key("Down", new int[] {Keys.DOWN, Keys.S});
 	public static Key left = new Key("Left", new int[] {Keys.LEFT, Keys.A});
 	public static Key right = new Key("Right", new int[] {Keys.RIGHT, Keys.D});
 	
 	public static Key pause = new Key("Pause", new int[] {Keys.ESCAPE});
 	public static Key enter = new Key("Confirm", new int[] {Keys.ENTER});
 	
 	public static Key screenshot = new Key("Screenshot", new int[] {Keys.F12});
 	public static Key record = new Key("Record", new int[] {Keys.F11});
 	
 	public static Key androidBack = new Key("Back", new int[] {Keys.BACK});
 	public static Key androidMenu = new Key("Menu", new int[] {Keys.MENU});
 	
 	public static ObjectMap<Integer, TouchPoint> touches = new ObjectMap<Integer, TouchPoint>();
 	
 	public static Array<KeyListener> keylisteners = new Array<KeyListener>();
 	static Array<KeyListener> tmpkeylisteners = new Array<KeyListener>();
 	
 	public static void setUp() {
 		for (Key k : all) {
 			if (k.rec.x < 0) {
 				k.rec.x = -k.rec.x;
 			}
 			if (k.rec.y < 0) {
 				k.rec.y = -k.rec.y;
 			}
 		}
 		
 		resize();
 	}
 	
 	public static void resize() {
 		if (cam == null) {
 			cam = new OrthographicCamera(Shadow.touchw, -Shadow.touchh);
 		} else {
 			cam.viewportWidth = Shadow.touchw;
 			cam.viewportHeight = -Shadow.touchh;
 		}
 		cam.position.set(Shadow.touchw/2, Shadow.touchh/2, 0);
 		cam.update();
 		
 		jump.setRect(Shadow.touchw-2, Shadow.touchh-3, 1, 1, true);
 		up.setRect(Shadow.touchw-2, Shadow.touchh-3, 1, 1, false);
 		down.setRect(Shadow.touchw-2, Shadow.touchh-2, 1, 1, true);
 		left.setRect(1, Shadow.touchh-2, 1, 1, true);
 		right.setRect(3, Shadow.touchh-2, 1, 1, true);
 	}
 	
 	public static void tick() {
 		for (Key k :all) {
 			k.tick();
 		}
 		
 		tmpkeylisteners.clear();
 		tmpkeylisteners.addAll(keylisteners);
 		
 		for (KeyListener kl : tmpkeylisteners) {
 			if (kl == null) {
 				keylisteners.removeValue(kl, true);
 				continue;
 			}
 			
 			check(kl);
 		}
 	}
 	
 	public static ObjectIntMap<KeyListener> timemap = new ObjectIntMap<KeyListener>();
 	
 	public static void check(KeyListener kl) {
 		if (kl instanceof Level) {
 			Level l = (Level) kl;
 			Level sl = Shadow.level;
 			
 			if (!(sl instanceof MenuLevel)) {
 				if (l instanceof MenuLevel) {
 					MenuLevel ml = (MenuLevel) l;
 					if (ml != sl) {
 						remove(kl);
 					}
 				} else {
 					if (sl instanceof MenuLevel) {
 						MenuLevel ml = (MenuLevel) sl;
 						if (l != ml.bglevel) {
 							remove(kl);
 						}
 					} else {
 						remove(kl);
 					}
 				}
 			}
 		}
 		if (kl instanceof GameObject) {
 			GameObject go = (GameObject) kl;
 			Level clevel = Shadow.level;
 			if (clevel instanceof MenuLevel) {
 				clevel = ((MenuLevel)clevel).bglevel;
 			}
 			if (go instanceof Entity) {
 				Entity e = (Entity) go;
 				if (e.layer == null || !e.layer.entities.contains(e, true) || clevel != e.layer.level) {
 					remove(kl);
 				}
 			}
 			if (go instanceof Block) {
 				Block b = (Block) go;
 				if (b.layer == null || !b.layer.blocks.contains(b, true) || clevel != b.layer.level) {
 					remove(kl);
 				}
 			}
 		}
 	}
 	
 	private static void remove(KeyListener kl) {
 		if (!timemap.containsKey(kl)) {
 			timemap.put(kl, 0);
 		} else if (timemap.get(kl, 0) >= 4) {
 			keylisteners.removeValue(kl, true);
 		} else {
 			timemap.put(kl, timemap.get(kl, 0)+1);
 		}
 	}
 	
 	public static void render() {
 		if (isAndroid && !isOuya && !isInMenu) {
 			for (Key k : all) {
 				k.render();
 			}
 		}
 	}
 	
 }
