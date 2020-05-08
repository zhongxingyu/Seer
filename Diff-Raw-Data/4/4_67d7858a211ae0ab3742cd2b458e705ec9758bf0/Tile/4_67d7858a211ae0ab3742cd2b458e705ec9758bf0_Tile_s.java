 public abstract class Tile{
 
 	public static Tile[] tiles = 		new Tile[256];
 	public static final Tile VOID = 	new BasicSolidTile(0, 0, 0, Colors.get(000, -1, -1, -1), 0xFF000000);
 	public static final Tile STONE = 	new BasicSolidTile(1, 1, 0, Colors.get(-1, 222, 233, -1), 0xFF555555);
 	public static final Tile GRASS = 	new BasicTile(2, 2, 0, Colors.get(-1, 131, 141, -1), 0xFF00FF00);
 	public static final Tile WATER = 	new AnimatedTile(3, new int[][] { {0, 5}, {1, 5}, {2, 5}, {1, 5} }, Colors.get(-1, 004, 115, -1), 0xFF0000FF, 1000);
 	public static final Tile WALL_HORIZONTAL =	new BasicSolidTile(4, 0, 1, Colors.get(131, 222, 233, 255), 0xFFFFFFFF);
 	public static final Tile WALL_VERITCAL = 	new BasicSolidTile(5, 1, 1, Colors.get(131, 222, 233, 255), 0xFFFFFF00);
 	public static final Tile DOOR =				new DoorTile(6, 0, 3, Colors.get(050, 505, -1, -1), 0xFFFF00FF, "res/testLevel.png");
 
 	protected byte id;
 	protected boolean solid;
 	protected boolean emitter;
 	private int levelColor;
 
 	public Tile(int id, boolean isSolid, boolean isEmitter, int levelColor){
 		this.id = (byte) id;
 		if(tiles[id] != null)
 			throw new RuntimeException("Duplicate tile id on " + id);
 		
 		this.solid = isSolid;
 		this.emitter = isEmitter;
 		this.levelColor = levelColor;
 
 		tiles[id] = this;
 	}
 
 	public byte getId(){
 		return id;
 	}
 
 	public boolean isSolid(){
 		return solid;
 	}
 
 	public boolean isEmitter(){
 		return emitter;
 	}
 
 	public int getLevelColor(){
 		return levelColor;
 	}
 
 	public abstract void tick();
 
 	public abstract void render(Screen screen, Level level, int x, int y);
 	
 }
