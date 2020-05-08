 package fr.frozen.iron.common;
 
 import java.awt.geom.Point2D;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 
 import fr.frozen.iron.common.entities.IronUnit;
 import fr.frozen.iron.util.IronConfig;
 import fr.frozen.iron.util.IronConst;
 import fr.frozen.iron.util.IronGL;
 import fr.frozen.iron.util.PerlinNoise;
 import fr.frozen.util.pathfinding.TileBasedMap;
 
 public class IronMap implements TileBasedMap {
 
 	protected double PROB_EARTH = 0.4;
	protected double PROB_WATER = 0.5;
 	protected double PROB_TREE = 0.3;
 	protected double PROB_ROCK = 0.05;
 	
 	
 	protected Tile[][] tiles;
 	
 	public IronMap() {
 		tiles = new Tile[IronConst.MAP_WIDTH][IronConst.MAP_HEIGHT];
 		
 		for (int i = 0; i < tiles.length; i++) {
 			for (int j = 0; j < tiles[i].length; j++) {
 				tiles[i][j] = new Tile(new Point2D.Float(i, j), this);
 				tiles[i][j].setSubType(findSubType(Tile.tile_names[tiles[i][j].getType()]));
 			}
 		}
 	}
 	
 	
 	private boolean isDeployementZone(int x, int y) {
 		int xstart = (IronConst.MAP_WIDTH - 10) / 2 /*+ (IronConfig.MAP_WIDTH & 1)*/;
 		int xend = IronConst.MAP_WIDTH - (IronConst.MAP_WIDTH - 10) / 2 - 1;
 		boolean inX = x >= xstart && x <= xend;
 		boolean inY = (y >= 0 && y < 4) || (y >= IronConst.MAP_HEIGHT - 4 && y < IronConst.MAP_HEIGHT);
 		return inX && inY;
 	}
 	
 	private void generateElement(int type, double percentage, 
 								double persistence, int octave, 
 								boolean respectDeployement, boolean isObject) {
 		
 		PerlinNoise pn = new PerlinNoise();
 		double [][] vals = new double[tiles.length][tiles[0].length];
 		double min = Double.MAX_VALUE;
 		double max = Double.MIN_VALUE;
 		
 		for (int i = 0; i < tiles.length; i++) {
 			for (int j = 0; j < tiles[i].length; j++) {
 				vals[i][j] =  pn.pNoise(i, j, persistence, octave);
 
 				if (vals[i][j] < min) min = vals[i][j];
 				if (vals[i][j] > max) max = vals[i][j];
 				
 			}
 		}
 
 		for (int i = 0; i < tiles.length; i++) {
 			for (int j = 0; j < tiles[i].length; j++) {
 				vals[i][j] += Math.abs(min);
 			}
 		}
 		max += Math.abs(min);
 
 		double threshold = max * percentage;
 
 		for (int i = 0; i < tiles.length; i++) {
 			for (int j = 0; j < tiles[i].length; j++) {
 				
 				if (vals[i][j] < threshold) {
 					if (respectDeployement && isDeployementZone(i, j)) {
 						continue;
 					}
 					if (isObject) {
 						if (!tiles[i][j].isOccupied()) {
 							tiles[i][j].setObjectOverlap(type);
 							tiles[i][j].setObjectSubType(findSubType(Tile.object_names[type]));
 							tiles[i][j].setOccupied(true);
 						}
 					} else {
 						if (type == Tile.TYPE_WATER) {
 							if (waterAround(i, j, vals, threshold)) {
 								tiles[i][j].setType(type);
 								tiles[i][j].setSubType(findSubType(Tile.tile_names[type]));
 								tiles[i][j].setOccupied(true);
 							}
 						} else {
 							tiles[i][j].setType(type);
 							tiles[i][j].setSubType(findSubType(Tile.tile_names[type]));
 						}
 					}
 				} // if vals[i][j] < threshold
 			} //for j
 		}//for i
 	}
 	
 	private int findSubType(String name) {
 		String nbSubTypes = IronConfig.getIronXMLParser().getAttributeValue("map/"+name, "nbtypes");
 		
 		if (nbSubTypes != null) {
 			int nb = Integer.parseInt(nbSubTypes);
 			if (nb <= 0) return 0;
 			if (nb == 1) return 1;
 			int subType = 1;
 			float chance = 1.0f - 1.0f / nb;
 			//for the moment, chance is the chance of falling on a non default square, the default square being _11
 			
 			String chanceStr = IronConfig.getIronXMLParser().getAttributeValue("map/"+name, "chance");
 			if (chanceStr != null) {
 				chance = Float.parseFloat(chanceStr);
 			}
 			//TODO change this for every subsprite to have its own probability to appear
 			double random = Math.random();
 			if (random <= chance) {
 				int val = (int) (random * 1000);
 				subType += val % (nb - 1);
 				subType ++;
 			}
 			
 			return subType;
 		} 
 		return 0;
 	}
 	
 	
 	private boolean waterAround(int x, int y, double [][] vals, double threshold) {
 		
 		if (x > 0 && vals[x - 1][y] < threshold && !isDeployementZone(x - 1, y)
 			|| x < IronConst.MAP_WIDTH - 1 && vals[x + 1][y] < threshold && !isDeployementZone(x + 1, y)
 			|| y > 0 && vals[x][y - 1] < threshold && !isDeployementZone(x, y - 1)
 			|| y < IronConst.MAP_HEIGHT - 1 && vals[x][y + 1] < threshold && !isDeployementZone(x, y + 1)) {
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public void generateMap() {
 		generateElement(Tile.TYPE_EARTH, PROB_EARTH, 0.1, 1, false, false);
 		generateElement(Tile.TYPE_WATER, PROB_WATER, 0.1, 1, true, false);
 	
 		generateElement(Tile.OBJECT_TREE, PROB_TREE, 0.6, 4, true, true);
 		generateElement(Tile.OBJECT_ROCK, PROB_ROCK, 0.1, 8, true, true);
 		
 		
 		for (int i = 0; i < tiles.length; i++) {
 			for (int j = 0; j < tiles[i].length; j++) {
 				int type = tiles[i][j].getType();
 
 				if (i > 0 && j > 0 && tiles[i-1][j-1].getType() > type) {
 					tiles[i-1][j-1].setTerrainOverlap(tiles[i-1][j-1].getTerrainOverlap() | Tile.SE);
 				}
 
 				if (i > 0 && tiles[i-1][j].getType() > type) {
 					tiles[i-1][j].setTerrainOverlap(tiles[i-1][j].getTerrainOverlap() | Tile.E);
 				}
 
 				if (i > 0 && j < IronConst.MAP_HEIGHT - 1 && tiles[i-1][j+1].getType() > type) {
 					tiles[i-1][j+1].setTerrainOverlap(tiles[i-1][j+1].getTerrainOverlap() | Tile.NE);
 				}
 
 				if (j < IronConst.MAP_HEIGHT - 1 && tiles[i][j+1].getType() > type) {
 					tiles[i][j+1].setTerrainOverlap(tiles[i][j+1].getTerrainOverlap() | Tile.N);
 				}
 
 				if (i < IronConst.MAP_WIDTH - 1 && j < IronConst.MAP_HEIGHT - 1 && tiles[i+1][j+1].getType() > type) {
 					tiles[i+1][j+1].setTerrainOverlap(tiles[i+1][j+1].getTerrainOverlap() | Tile.NW);
 				}
 
 				if (i < IronConst.MAP_WIDTH - 1 && tiles[i+1][j].getType() > type) {
 					tiles[i+1][j].setTerrainOverlap(tiles[i+1][j].getTerrainOverlap() | Tile.W);
 				}
 
 				if (i < IronConst.MAP_WIDTH - 1 && j > 0 && tiles[i+1][j-1].getType() > type) {
 					tiles[i+1][j-1].setTerrainOverlap(tiles[i+1][j-1].getTerrainOverlap() | Tile.SW);
 				}
 
 				if (j > 0 && tiles[i][j-1].getType() > type) {
 					tiles[i][j-1].setTerrainOverlap(tiles[i][j-1].getTerrainOverlap() | Tile.S);
 				}
 			}
 		}
 	}
 	
 	public Tile[][] getTiles() {
 		return tiles;
 	}
 	
 	public Tile getTile(int x, int y) {
 		if (x < 0 || x >= IronConst.MAP_WIDTH || y < 0 || y >= IronConst.MAP_HEIGHT) return null;
 		return tiles[x][y];
 	}
 	
 	public void render(float deltaTime) {
 		for (int i = 0; i < tiles.length; i++) {
 			for (int j = 0; j < tiles[i].length; j++) {
 				tiles[i][j].render();
 				tiles[i][j].renderObject(deltaTime);
 				IronUnit unit = tiles[i][j].getUnitOnTile();
 				if (unit != null) {
 					unit.render(deltaTime);
 				}
 			}
 		}
 	}
 	
 	
 	public void renderTiles(float deltaTime) {
 		for (int i = 0; i < tiles.length; i++) {
 			for (int j = 0; j < tiles[i].length; j++) {
 				tiles[i][j].render();
 			}
 		}
 	}
 	
 	public void renderUnitsTileGfx(float deltaTime) {
 		for (int i = 0; i < tiles.length; i++) {
 			for (int j = 0; j < tiles[i].length; j++) {
 				IronUnit unit = tiles[i][j].getUnitOnTile();
 				if (unit != null) {
 					unit.renderTileGfx(deltaTime);
 				}
 			}
 		}
 	}
 	
 	public void renderUnitsAndObjects(float deltaTime) {
 		for (int i = 0; i < tiles.length; i++) {
 			for (int j = 0; j < tiles[i].length; j++) {
 				tiles[i][j].renderObject(deltaTime);
 				IronUnit unit = tiles[i][j].getUnitOnTile();
 				if (unit != null) {
 					unit.render(deltaTime);
 				}
 			}
 		}
 	}
 	
 	public void renderObjects(float deltaTime) {
 		for (int i = 0; i < tiles.length; i++) {
 			for (int j = 0; j < tiles[i].length; j++) {
 				tiles[i][j].renderObject(deltaTime);
 			}
 		}
 	}
 	
 	public void renderGrid() {
 		if (!IronConfig.isShowGrid()) return;
 		for (int i = 0; i < IronConst.MAP_WIDTH; i++) {
 			for (int j = 0; j < IronConst.MAP_HEIGHT; j++) {
 				IronGL.drawLine(0, j * IronConst.TILE_HEIGHT,
 						IronConst.MAP_WIDTH * IronConst.TILE_WIDTH, j * IronConst.TILE_HEIGHT);
 			}
 			IronGL.drawLine(i * IronConst.TILE_WIDTH, 0,
 				     i * IronConst.TILE_WIDTH, IronConst.MAP_HEIGHT * IronConst.TILE_HEIGHT);
 		}
 	}
 
 	public byte [] serialize() throws IOException {
 		ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
 		
 		byteArray.reset();
 		for (int i = 0; i < tiles.length; i++) {
 			for (int j = 0; j < tiles[i].length; j++) {
 				byteArray.write(tiles[i][j].serialize());
 			}
 		}
 		return byteArray.toByteArray();
 	}
 	
 	public void unserialize(byte [] data) {
 		for (int i = 0; i < data.length / 3; i++) {
 			int x = i / IronConst.MAP_HEIGHT;
 			int y = i % IronConst.MAP_HEIGHT;
 			
 			byte [] tileInfo = {data[i * 3],data[i * 3 + 1],data[i * 3 + 2] };
 			
 			tiles[x][y].unserialize(tileInfo);
 		}
 	}
 	
 	public String toString() {
 		String result = "";
 		for (int j = 0; j < tiles[0].length; j++) {
 			for (int i = 0; i < tiles.length; i++) {
 				result += tiles[i][j];
 			}
 			result += "\n";
 		}
 		return result;
 	}
 	
 	public static void main(String []args) {
 		
 		String nbSubTypes = IronConfig.getIronXMLParser().getAttributeValue("map/grass", "nbtypes");
 		if (nbSubTypes != null) {
 			System.out.println("value found : "+nbSubTypes);
 		} else {
 			System.out.println("nothing found");
 		}
 		/*IronMap map = new IronMap();
 		map.generateMap();*/
 		/*System.out.println("gererated map : \n"+map);
 		try {
 			byte[] data;
 			data = map.serialize();
 			map.unserialize(data);
 			System.out.println("\n\nunserialized : \n"+map);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}*/
 	}
 
 
 	@Override
 	public int getCost(int x, int y) {
 		return getTile(x, y).getCost();
 	}
 
 
 	@Override
 	public int getHeight() {
 		return IronConst.MAP_HEIGHT;
 	}
 
 
 	@Override
 	public int getWidth() {
 		return IronConst.MAP_WIDTH;
 	}
 
 
 	@Override
 	public boolean isBlocked(int x, int y) {
 		return getTile(x, y).isOccupied();
 	}
 }
