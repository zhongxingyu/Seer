 package map;
 
 import java.util.ArrayList;
 
 import org.jbox2d.collision.shapes.EdgeShape;
 import org.jbox2d.collision.shapes.PolygonShape;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.BodyDef;
 import org.jbox2d.dynamics.BodyType;
 import org.jbox2d.dynamics.Fixture;
 import org.jbox2d.dynamics.FixtureDef;
 import org.jbox2d.dynamics.World;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.tiled.TiledMap;
 
 import util.Corner;
 
 import config.Config;
 import entities.enemies.Enemy;
 import entities.enemies.Ent;
 
 public class Map {
 	TiledMap foreground;
 	World world;
 	private int height;
 	private int width;
 	private int tileHeight;
 	private int tileWidth;
 	private ArrayList<Enemy> enemies;
 	private Vec2 playerLoc;
 	private Vec2 goalLoc;
	
 	public Map(String tmxMap, World world) throws SlickException {
 		foreground = new TiledMap(tmxMap);
 		this.world = world;
 		height = foreground.getHeight();
 		width = foreground.getWidth();
 		tileHeight = foreground.getTileHeight();
 		tileWidth = foreground.getTileWidth();
 		enemies = new ArrayList<Enemy>();
 	}
 	public void parseMapObjects() {
 		parseSpecialObjects();
 		parseWallObjects();
 		parseSlopeObjects();
 		parseEntityObjects();
 		createLine(new Vec2(0, (height+2)*tileHeight/ Config.PIXELS_PER_METER), 
 				new Vec2(width*tileWidth/ Config.PIXELS_PER_METER, (height+2)*tileHeight/ Config.PIXELS_PER_METER));
 	}
 	
 	/**
 	 * Give bodies to things which can be hooked
 	 */
 	private void parseSpecialObjects() {
 		for(int j = 0; j < height; j++) {
 			for(int i = 0; i < width; i++) {
 				int tileId = foreground.getTileId(i, j, 0);
 				String tileType = foreground.getTileProperty(tileId, "hookable", "meh");
 				if (tileType.equals("true")) {
 					createBox(i*tileWidth + tileWidth/2f, j*tileHeight + tileHeight/2f, Config.HOOKABLE, Config.HOOKABLE, false);
 				}
 				tileType = foreground.getTileProperty(tileId, "water", "meh");
 				if (tileType.equals("true")) {
 					createBox(i*tileWidth + tileWidth/2f, j*tileHeight + tileHeight/2f, Config.WATER, 1, true);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Figure out where the player/enemies/etc are
 	 */
 	private void parseEntityObjects() {
 		for(int j = 0; j < height; j++) {
 			for(int i = 0; i < width; i++) {
 				int tileId = foreground.getTileId(i, j, 0);
 				String tileType = foreground.getTileProperty(tileId, "type", "meh");
 				if (tileType.equals("enemy")) {
 					String enemyType = foreground.getTileProperty(tileId, "enemyType", "none");
 					if (enemyType.equals("ent")) {
 						Vec2 center = getPixelCenter(i,j);
 						Enemy ent = new Ent(center.x, center.y);
 						enemies.add(ent);
 					}
 				}
 				if (tileType.equals("player")) {
 					playerLoc = getPixelCenter(i,j);
 				}
 				if (tileType.equals("goal")){
 					goalLoc = getPixelCenter(i,j);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Create a triangle around each slope
 	 * Each slope must have a start and end point, 
 	 * the start point being the one that is the base of the triangle
 	 */
 	private void parseSlopeObjects() {
 		for(int j = 0; j < height; j++) {
 			for(int i = 0; i < width; i++) {
 				int tileId = foreground.getTileId(i, j, 0);
 				String tileType = foreground.getTileProperty(tileId, "type", "meh");
 				// find the first endpoint of all slopes...
 				if(tileType.equals("slope") && foreground.getTileProperty(tileId, "endpoint", "meh").equals("first")) {
 					int slope = Integer.parseInt(foreground.getTileProperty(tileId, "slope", "0"));
 					boolean flipped = Boolean.parseBoolean(foreground.getTileProperty(tileId, "flipped", "false"));
 					int shiftsign = slope < 0 ? -1 : 1;
 					// make sure the tile wasn't already used in a previous slope.
 					int testTile = foreground.getTileId(i+shiftsign, j-1, 0);
 					int testSlope = Integer.parseInt(foreground.getTileProperty(testTile, "slope", "0"));
 					boolean testFlipped = Boolean.parseBoolean(foreground.getTileProperty(testTile, "flipped", "false"));
 					if (!(slope == testSlope && flipped == testFlipped)) {
 						// find the other endpoint, and create triangles
 						SlopeReturn tileData = findSlopeStart(flipped, slope,i, j);
 						Vec2 v1 = new Vec2();
 						Vec2 v2 = new Vec2();
 						if (slope > 0) {
 							v1 = getCorners(i,j)[Corner.TOPRIGHT.ordinal()];
 							v2 = getCorners(tileData.i,tileData.j)[Corner.BOTTOMLEFT.ordinal()];
 						}
 						else {
 							v1 = getCorners(i,j)[Corner.TOPLEFT.ordinal()];
 							v2 = getCorners(tileData.i,tileData.j)[Corner.BOTTOMRIGHT.ordinal()];
 						}
 						createLine(v1,v2);
 						if (flipped) {
 							createLine(v1, new Vec2(v2.x, v1.y));
 							createLine(v2, new Vec2(v2.x, v1.y));
 						}
 						else {
 							createLine(v1, new Vec2(v1.x, v2.y));
 							createLine(v2, new Vec2(v1.x, v2.y));
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	
 	
 	/**
 	 * Parses all wall objects 
 	 * Will create horizontal and vertical lines around all blocks,
 	 * which will allow player to move across them.
 	 */
 	private void parseWallObjects(){
 		// create horizontal lines
 		for(int j = 0; j < height; j++) {
 			boolean running = false;
 			Vec2 startVTop = new Vec2();
 			Vec2 startVBottom = new Vec2();
 			Vec2 endVTop = new Vec2();
 			Vec2 endVBottom = new Vec2();
 			for(int i = 0; i < width; i++) {
 				int tileId = foreground.getTileId(i, j, 0);
 				String tileType = foreground.getTileProperty(tileId, "type", "meh");
 				// create the start of a line
 				if (!running) {
 					if(tileType.equals("wall")) {
 						startVTop.set(i*tileWidth / Config.PIXELS_PER_METER, j*tileHeight / Config.PIXELS_PER_METER);
 						startVBottom.set(i*tileWidth / Config.PIXELS_PER_METER, (j+1)*tileHeight / Config.PIXELS_PER_METER);
 						running = true;
 					}
 				}
 				// end a line if a blank block is reached or end of map
 				if (running) {
 					if(!tileType.equals("wall") || i == width-1) {
 						if(!tileType.equals("wall")) {
 							endVTop.set(i*tileWidth / Config.PIXELS_PER_METER, j*tileHeight / Config.PIXELS_PER_METER);
 							endVBottom.set(i*tileWidth / Config.PIXELS_PER_METER, (j+1)*tileHeight / Config.PIXELS_PER_METER);
 						}
 						// extend line a bit if end of map
 						else if(i == width-1) {
 							endVTop.set((i+1)*tileWidth / Config.PIXELS_PER_METER, j*tileHeight / Config.PIXELS_PER_METER);
 							endVBottom.set((i+1)*tileWidth / Config.PIXELS_PER_METER, (j+1)*tileHeight / Config.PIXELS_PER_METER);
 						}
 						// add lines to map
 						createLine(startVTop, endVTop);
 						createLine(startVBottom, endVBottom);
 						running = false;
 					}
 				}
 			}
 		}
 		// create vertical lines
 		for(int j = 0; j < width; j++) {
 			boolean running = false;
 			Vec2 startVTop = new Vec2();
 			Vec2 startVBottom = new Vec2();
 			Vec2 endVTop = new Vec2();
 			Vec2 endVBottom = new Vec2();
 			for(int i = 0; i < height; i++) {
 				int tileId = foreground.getTileId(j, i, 0);
 				String tileType = foreground.getTileProperty(tileId, "type", "meh");
 				// create the start of a line
 				if (!running) {
 					if(tileType.equals("wall")) {
						startVTop.set(j*tileWidth / Config.PIXELS_PER_METER, i*tileHeight / Config.PIXELS_PER_METER);
						startVBottom.set((j+1)*tileWidth / Config.PIXELS_PER_METER, i*tileHeight / Config.PIXELS_PER_METER);
 						running = true;
 					}
 				}
 				// end a line if a blank block is reached or end of map
 				if (running) {
 					if(!tileType.equals("wall") || i == height-1) {
 						if(!tileType.equals("wall")) {
							endVTop.set(j*tileWidth / Config.PIXELS_PER_METER, i*tileHeight / Config.PIXELS_PER_METER);
							endVBottom.set((j+1)*tileWidth / Config.PIXELS_PER_METER, i*tileHeight / Config.PIXELS_PER_METER);
 						}
 						// extend line a bit if end of map
 						else if(i == height-1) {
							endVTop.set(j*tileWidth / Config.PIXELS_PER_METER, (i+1)*tileHeight / Config.PIXELS_PER_METER);
							endVBottom.set((j+1)*tileWidth / Config.PIXELS_PER_METER, (i+1)*tileHeight / Config.PIXELS_PER_METER);
 						}
 						// add lines to map
 						createLine(startVTop, endVTop);
 						createLine(startVBottom, endVBottom);
 						running = false;
 					}
 				}
 			}
 		}
 	}
 	/**
 	 * Given the first point in a slope, find the second point.
 	 * Will go across multiple slopes if they're the same type.
 	 * @param slope
 	 * @param i
 	 * @param j
 	 * @return
 	 */
 	private SlopeReturn findSlopeStart(boolean flipped, int slope, int i, int j) {
 		// figure out how to find next tile for this slope
 		int shift = 0;
 		int shiftsign = 0;
 		if (slope > 0) {
 			shift = -(slope - 1);
 			shiftsign = -1;
 		}
 		if (slope < 0) {
 			shift = -(slope + 1);
 			shiftsign = 1;
 		}
 		int curi = i;
 		int curj = j;
 		curi += shift;
 		while(true) {
 			// check if there is another line of the same slope connect to this one (make them one line)
 			int testTile = foreground.getTileId(curi+shiftsign, curj+1, 0);
 			int testSlope = Integer.parseInt(foreground.getTileProperty(testTile, "slope", "0"));
 			boolean testFlipped = Boolean.parseBoolean(foreground.getTileProperty(testTile, "flipped", "false"));
 			if(!(testSlope == slope && testFlipped == flipped)) {
 				return new SlopeReturn(curi, curj);
 			}
 			else {
 				curi += shiftsign + shift;
 				curj += 1;
 				
 			}
 		}
 	}
 	
 	private Vec2[] getCorners(int x, int y) {
 		Vec2[] corners = new Vec2[4];
 		corners[Corner.TOPLEFT.ordinal()] = new Vec2((x)*tileWidth / Config.PIXELS_PER_METER,
                 (y)*tileHeight / Config.PIXELS_PER_METER);
 		corners[Corner.TOPRIGHT.ordinal()] = new Vec2((x+1)*tileWidth / Config.PIXELS_PER_METER,
                 (y)*tileHeight / Config.PIXELS_PER_METER);
 		corners[Corner.BOTTOMLEFT.ordinal()] = new Vec2((x)*tileWidth / Config.PIXELS_PER_METER,
                 (y+1f)*tileHeight / Config.PIXELS_PER_METER);
 		corners[Corner.BOTTOMRIGHT.ordinal()] = new Vec2((x+1)*tileWidth / Config.PIXELS_PER_METER,
                 (y+1f)*tileHeight / Config.PIXELS_PER_METER);
 		return corners;
 	}
 	
 	private Vec2 getPixelCenter(int x, int y) {
 		return new Vec2((x+.5f)*tileWidth,
                 (y+.5f)*tileHeight);
 	}
 	
 	private void createLine(Vec2 v1, Vec2 v2) {
 		EdgeShape edge = new EdgeShape();
 		edge.set(v1, v2);
 		BodyDef lineDef = new BodyDef();
 		Body line = world.createBody(lineDef);
 		line.createFixture(edge, Config.DEFAULT_DENSITY);
 	}
 	
 	private void createBox(float x, float y, int category, int collides, boolean isSensor) {
 		BodyDef physicsDef = new BodyDef();
 		physicsDef.type = BodyType.STATIC;
 		physicsDef.fixedRotation = true;
 		physicsDef.position.set(x / Config.PIXELS_PER_METER,
 		                        y / Config.PIXELS_PER_METER);
 		
 		PolygonShape physicsShape = new PolygonShape();
 		physicsShape.setAsBox(tileWidth / 2 / Config.PIXELS_PER_METER,
 		                     tileHeight / 2 / Config.PIXELS_PER_METER);
 		
 		FixtureDef physicsFixtureDef = new FixtureDef();
 		physicsFixtureDef.shape = physicsShape;
 		physicsFixtureDef.density = Config.DEFAULT_DENSITY;
 		physicsFixtureDef.friction = Config.DEFAULT_FRICTION;
 		physicsFixtureDef.filter.categoryBits = category;
 		physicsFixtureDef.filter.maskBits = collides;
 		physicsFixtureDef.isSensor = isSensor;
 		
 		Body physicsBody = world.createBody(physicsDef);
 		Fixture physicsFixture = physicsBody.createFixture(physicsFixtureDef);
 		physicsFixture.setUserData(this);
 	}
 	
 	public ArrayList<Enemy> getEnemies() {
 		return this.enemies;
 	}
 	
 	public Vec2 getPlayerLoc() {
 		return playerLoc;
 	}
 	
 	public Vec2 getGoalLoc() {
 		return goalLoc;
 	}
 	
 	public int getHeight() {
 		return this.height*this.tileHeight;
 	}
 	
 	public int getWidth() {
 		return this.width*this.tileWidth;
 	}
 	
 	public TiledMap getTiledMap() {
 		return foreground;
 	}
 	
 	public void render() {
 		foreground.render(0, 0);
 	}
 }
