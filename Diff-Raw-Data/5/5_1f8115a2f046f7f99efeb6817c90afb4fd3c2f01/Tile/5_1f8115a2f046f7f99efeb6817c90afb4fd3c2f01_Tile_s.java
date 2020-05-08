 package fr.frozen.iron.common;
 
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.List;
 
 import fr.frozen.game.ISprite;
 import fr.frozen.game.ISpriteManager;
 import fr.frozen.game.SpriteManagerImpl;
 import fr.frozen.iron.common.entities.IronUnit;
 import fr.frozen.iron.util.IronConst;
 
 public class Tile {
 	
 	public static final int TYPE_GRASS = 0;
 	public static final int TYPE_EARTH = 1;
 	public static final int TYPE_WATER = 2;
 	
 	public static final int NB_DISPLAY_LEVELS = 3;
 	public static final int DISPLAY_TOP = 2;
 	public static final int DISPLAY_MIDDLE = 1;
 	public static final int DISPLAY_BOTTOM = 0;
 	
 	public static final int OBJECT_NOTHING = 0;
 	public static final int OBJECT_TREE = 1;
 	public static final int OBJECT_ROCK = 2;
 	
 	public static final String [] tile_names = {"grass","earth","water"};
 	public static final String [] object_names = { "nothing","tree","rock"};
 	
 	public static int NW =  128;
 	public static int N =  64;
 	public static int NE =  32;
 	public static int E =  16;
 	public static int SE =  8;
 	public static int S =  4;
 	public static int SW =  2;
 	public static int W =  1;
 	
 	//serialization : [[4 bits type][4bits subtype]]-[[4 bits objectoverlap][3 bits objectSubType][1bit occupied]]-[8 bits terrainoverlap]
 	//overlap is natural object overlapping tile
 	
 	protected IronMap map;
 	protected ISprite sprite;
 	protected ISprite overlapSprite;
 	protected List<ISprite> []overlaySprites;
 	
 	protected int type = 0;//coded on 4 bits
 	protected int subType = 0;//16 values possible coded on 4 bits
 	protected int terrainOverlap = 0;//1 byte
 	protected int objectOverlap = 0;//16values possible, coded on 4 bits
 	protected int objectSubType = 0;
 	
 	protected Point2D pos;
 	
 	protected boolean occupied = false;//coded on 1 bit
 	protected IronUnit unitOnTile = null;
 	
 	protected int moveCost = IronConst.MOVE_COST_DEFAULT;
 	
 	public Tile (Point2D pos, IronMap map) {
 		this.pos = pos;
 		this.map = map;
 		this.type = TYPE_GRASS;
 	}
 
 	public boolean canShootOver() {
 		if (getType() == TYPE_WATER) return true;
 		return !isOccupied();
 	}
 	
 	public int getType() {
 		return type;
 	}
 	
 	public int getCost() {
 		return moveCost;
 	}
 
 	public void setCost(int val) {
 		moveCost = val;
 	}
 	
 	public void setType(int type) {
 		this.type = type;
 	}
 	
 	public int getTerrainOverlap() {
 		return terrainOverlap;
 	}
 
 	public void setTerrainOverlap(int type) {
 		this.terrainOverlap = type;
 	}
 
 	public int getObjectOverlap() {
 		return objectOverlap;
 	}
 
 	public void setObjectOverlap(int overlap) {
 		this.objectOverlap = overlap;
 	}
 	
 	public void setSubType(int val) {
 		subType = val;
 	}
 	
 	public int getSubType() {
 		return subType;
 	}
 	
 	public Point2D getPos() {
 		return pos;
 	}
 
 	public void setPos(Point2D pos) {
 		this.pos = pos;
 	}
 
 	public boolean isOccupied() {
 		return occupied | unitOnTile != null;
 	}
 
 	public void setOccupied(boolean occupied) {
 		this.occupied = occupied;
 	}
 
 	public IronUnit getUnitOnTile() {
 		return unitOnTile;
 	}
 
 	public void setUnitOnTile(IronUnit unitOnTile) {
 		this.unitOnTile = unitOnTile;
 	}
 
 	public void setObjectSubType(int val) {
 		objectSubType = val;
 	}
 	
 	public int getObjectSubType() {
 		return objectSubType;
 	}
 	
 	
 	//serialization : [[4 bits type][4bits subtype]]-[[4 bits objectoverlap][3 bits objectSubType][1bit occupied]]-[8 bits terrainoverlap]
 	//overlap is natural object overlapping tile
 	public byte[] serialize() {
 		//TODO : maybe check if values are respected, ie type < 16 & overlap < 8
 		byte []data = new byte[3];
 		data[0] = (byte)(type & 0xf);
 		data[0] <<= 4;
 		data[0] |= (subType & 0xf);
 		
 		data[1] = (byte)(objectOverlap & 0xf);
 		data[1] <<= 3;
 		data[1] |= (0x7 & objectSubType);
 		data[1] <<= 1;
 		data[1] |= occupied ? 1 : 0;
 		
 		data[2] = (byte)(terrainOverlap & 0xff);
 		return data;
 	}
 	
 	public void unserialize(byte []data) {
 		
 		subType = data[0] & 0xf;
 		data[0] >>= 4;
 		type = data[0] & 0xf;
 		
 		occupied = (data[1] & 0x1) == 1;
 		data[1] >>= 1;
 		objectSubType = (data[1] & 0x7);
 		data[1] >>= 3;
 		objectOverlap = (byte) (data[1] & 0xf);
 		
 		//subType = (byte) (data[1] & 0xf);
 		
 		terrainOverlap = data[2] & 0xff;
 	}
 	
 	public String toString() {
 		return "["+type+ ", "+subType+"]["+objectOverlap+", "+objectSubType+", "+occupied+"]["+terrainOverlap+"] ";
 	}
 
 	public void render() {
 		if (sprite != null) {
 			sprite.draw((float)pos.getX() * IronConst.TILE_WIDTH, (float)pos.getY() * IronConst.TILE_HEIGHT);
 		}
 		
 		if (overlaySprites != null) {
 			for (int i = 0; i < NB_DISPLAY_LEVELS; i++) {
 				for (ISprite s : overlaySprites[i]) {
 					s.draw((float)pos.getX() * IronConst.TILE_WIDTH, (float)pos.getY() * IronConst.TILE_HEIGHT);
 				}
 			}
 		}
 	}
 	
 	
 	public void renderObject(float deltaTime) {
 		if (overlapSprite != null) {
 			overlapSprite.draw((float)pos.getX() * IronConst.TILE_WIDTH, (float)pos.getY() * IronConst.TILE_HEIGHT);
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void findSprites() {
 		ISpriteManager spriteManager = SpriteManagerImpl.getInstance();
 
 		if (subType > 0) {
 			sprite = spriteManager.getSprite(tile_names[type]+"_"+subType);
 		} else {
 			sprite = spriteManager.getSprite(tile_names[type]);
 		}
 		
 		//sprite = spriteManager.getSprite(tile_names[type]);
 		
 		if (objectOverlap != OBJECT_NOTHING) {
 			
 			if (objectSubType > 0) {
 				overlapSprite = spriteManager.getSprite(object_names[objectOverlap]+"_"+objectSubType);
 			} else {
 				overlapSprite = spriteManager.getSprite(object_names[objectOverlap]);
 			}
 		}
 		
 		boolean []bools = new boolean[8];
 		int tmp = terrainOverlap;
 		boolean overlay = false;
 		for (int i = 0; i < 8; i++) {
 			bools[7 - i] = (tmp & 1) == 1;
 			overlay |= bools[7 - i];
 			tmp >>= 1;
 		}
 		
 		if (!overlay) return;
 		
 		overlaySprites = new List[NB_DISPLAY_LEVELS];
 		for (int i = 0; i < NB_DISPLAY_LEVELS; i++) {
 			overlaySprites[i] = new ArrayList<ISprite>();
 		}
 		ISprite spriteToAdd;
 		
 		//order of bits is : NW, N, NE, E, SE, S, SW, W
 		//following is ref image + mirror x + mirror y
 		Object [][] coords = {{"NW",false,false}, //NW
 							  {"N",false,false}, //N
 							  {"NW", true, false},//NE
 							  {"W",true, false},//E
 							  {"NW", true, true},//SE
 							  {"N",false, true},//S
 							  {"NW",false, true},//SW
 							  {"W", false, false}};//W
 		
 		for (int i = 0; i < 8; i++) {
 			if (bools[i]) {
 				String type = tile_names[type(i)];
 				
 				if (getType() == TYPE_WATER) {
 					spriteToAdd = ISpriteManager.getInstance().getSprite("water_rincle_"+coords[i][0]);
 					spriteToAdd.setMirrorX((Boolean)coords[i][1]);
 					spriteToAdd.setMirrorY((Boolean)coords[i][2]);
 					overlaySprites[DISPLAY_BOTTOM].add(spriteToAdd);
 				}
 				
 				spriteToAdd = ISpriteManager.getInstance().getSprite(type+"_overlay_"+coords[i][0]);
 				spriteToAdd.setMirrorX((Boolean)coords[i][1]);
 				spriteToAdd.setMirrorY((Boolean)coords[i][2]);
 				
 				if (type(i) == TYPE_GRASS) {
 					overlaySprites[DISPLAY_TOP].add(spriteToAdd);
 				} else if (type(i) == TYPE_EARTH) {
 					overlaySprites[DISPLAY_MIDDLE].add(spriteToAdd);	
 				} else {
 					System.out.println("FUUUUUUU");
 				}
 				
 				if (i % 2 == 1 && bools[(8 + i - 2) % 8] && type(i) == type((8 + i - 2) % 8)) {
 					boolean mirX = false;
 					boolean mirY = false;
 					if (i == 3 || i == 5) {
 						mirX = true;
 					}
 					if (i == 5 || i == 7) {
 						mirY = true;
 					}
 					
 					if (getType() == TYPE_WATER) {
 						spriteToAdd = ISpriteManager.getInstance().getSprite("water_rincle_corner");
 						spriteToAdd.setMirrorX(mirX);
 						spriteToAdd.setMirrorY(mirY);
 						overlaySprites[DISPLAY_BOTTOM].add(spriteToAdd);
 					}
 					
 					spriteToAdd = ISpriteManager.getInstance().getSprite(type+"_overlay_corner");
 					spriteToAdd.setMirrorX(mirX);
 					spriteToAdd.setMirrorY(mirY);
 					
 					if (type(i) == TYPE_GRASS) {
 						overlaySprites[DISPLAY_TOP].add(spriteToAdd);
 					} else if (type(i) == TYPE_EARTH) {
 						overlaySprites[DISPLAY_MIDDLE].add(spriteToAdd);	
 					}
 				}
 			}
 		}
 	}
 	
 	private int type(int i) {
 		int x = (int)pos.getX();
 		int y = (int)pos.getY();
 		
 		if (i == 0 || i >= 6) {
 			x--;
 		}
 		if (i >= 2 && i <= 4) {
 			x++;
 		}
 		
 		if (i >= 0 && i <= 2) {
 			y--;
 		}
 		if (i >= 4 && i <= 6) {
 			y++;
 		}
 		
 		return map.getTile(x, y).getType();
 	}
 	
 	public void findSubType() {
 	}
 	
 	public static void main(String []args) {
 		Tile tile = new Tile(new Point2D.Float(0,0), null);
 		tile.setType(1);
 		tile.setSubType(2);
 		tile.setObjectOverlap(3);
 		tile.setObjectSubType(4);
 		tile.setOccupied(true);
 		tile.setTerrainOverlap(10);
 		
 		System.out.println("before : "+tile);
 		Tile tile2 = new Tile(new Point2D.Float(0,0), null);
 		tile2.unserialize(tile.serialize());
 		System.out.println("after : "+tile2);
 	}
 }
