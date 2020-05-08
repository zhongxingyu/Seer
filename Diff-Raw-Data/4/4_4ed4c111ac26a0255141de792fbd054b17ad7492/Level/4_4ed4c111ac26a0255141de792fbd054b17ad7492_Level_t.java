 package eu32k.ludumdare.ld26.level;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import eu32k.ludumdare.ld26.Direction;
 import eu32k.ludumdare.ld26.Tile;
 import eu32k.ludumdare.ld26.Tile.Rotation;
 import eu32k.ludumdare.ld26.Tile.Type;
 import eu32k.ludumdare.ld26.state.GlobalState;
 import eu32k.ludumdare.ld26.state.StateMachine;
 
 public class Level {
 
    enum Edge {
       TOP, RIGHT, BOTTOM, LEFT;
    }
 
    private Tile[][] tileMatrix;
 
    private int width;
 
    private int height;
 
    private int dufficulty;
 
    private Random tileRandom;
 
    private List<Tile> tiles;
 
    public static final int TILE_WIDTH = 27;
 
    private Tile nextTile;
 
    public Level(int width, int height) {
       tileMatrix = new Tile[height][width];
       this.height = height;
       this.width = width;
       GlobalState globalState = StateMachine.instance().getState(GlobalState.class);
       tileRandom = globalState.createNewRandom("tiles");
       tiles = new ArrayList<Tile>();
    }
 
    public void generateRandomTiles() {
       for (int i = 0; i < height; i++) {
          for (int j = 0; j < width; j++) {
             Tile tile = createRandomTile(j * TILE_WIDTH, i * TILE_WIDTH);
             tiles.add(tile);
             tileMatrix[i][j] = tile;
          }
       }
       placeNeighbors();
    }
 
    private Tile createRandomTile(int x, int y) {
       int randType = tileRandom.nextInt(4);
       int randRot = tileRandom.nextInt(4);
       Tile tile = new Tile(x, y, Type.values()[randType], Rotation.values()[randRot]);
       return tile;
    }
 
    private void placeNeighbors() {
       for (int i = 0; i < height; i++) {
          for (int j = 0; j < width; j++) {
             Tile tile = tileMatrix[i][j];
             Tile north, east, south, west;
             if (i > 0) {
                north = tileMatrix[i - 1][j];
                addNeighbor(tile, north, Direction.N);
             }
             if (j > 0) {
                west = tileMatrix[i][j - 1];
                addNeighbor(tile, west, Direction.W);
             }
             if (i < height - 1) {
                south = tileMatrix[i + 1][j];
                addNeighbor(tile, south, Direction.S);
             }
             if (j < width - 1) {
                east = tileMatrix[i][j + 1];
                addNeighbor(tile, east, Direction.E);
             }
          }
       }
    }
 
    private void addNeighbor(Tile target, Tile neighbor, Direction direction) {
       if (neighbor != null) {
          target.getNeighbors().put(direction, neighbor);
       }
    }
 
    // public Tile insertTile(Tile tile, Edge edge, int position) {
    // int x = 0, y = 0;
    // Tile popped;
    // switch (edge) {
    // case TOP:
    // x = position;
    // y = 0;
    // popped = shiftDown(x, y);
    // break;
    // case RIGHT:
    // x = this.width - 1;
    // y = position;
    // popped = shiftLeft(x, y);
    // break;
    // case BOTTOM:
    // x = position;
    // y = this.height - 1;
    // popped = shiftUp(x, y);
    // break;
    // case LEFT:
    // default:
    // x = 0;
    // y = position;
    // popped = shiftRight(x, y);
    // break;
    // }
    // tileMatrix[y][x] = tile;
    // return popped;
    // }
 
    // private Tile shiftDown(int x, int y) {
    // int toPopX = x;
    // int toPopY = height - 1;
    // Tile popped = tileMatrix[toPopY][toPopX];
    // for (int i = height - 1; i > 0; i--) {
    // int toShiftY = i - 1;
    // tileMatrix[i][x] = tileMatrix[toShiftY][x];
    // }
    // return popped;
    // }
    //
    // private Tile shiftLeft(int x, int y) {
    // int toPopX = width - 1;
    // int toPopY = y;
    // Tile popped = tileMatrix[toPopY][toPopX];
    // for (int i = 0; i < width - 1; i++) {
    // int toShiftX = i + 1;
    // tileMatrix[y][i] = tileMatrix[y][toShiftX];
    // }
    // return popped;
    // }
    //
    // private Tile shiftUp(int x, int y) {
    // int toPopX = x;
    // int toPopY = 0;
    // Tile popped = tileMatrix[toPopY][toPopX];
    // for (int i = 0; i < height - 1; i++) {
    // int toShiftY = i + 1;
    // tileMatrix[i][x] = tileMatrix[toShiftY][x];
    // }
    // return popped;
    // }
    //
    // private Tile shiftRight(int x, int y) {
    // int toPopX = 0;
    // int toPopY = y;
    // Tile popped = tileMatrix[toPopY][toPopX];
    // for (int i = width - 1; i > 0; i--) {
    // int toShiftX = i - 1;
    // tileMatrix[y][i] = tileMatrix[y][toShiftX];
    // }
    // return popped;
    // }
 
    public Tile spawnTile() {
       int edgeRand = tileRandom.nextInt(40) % 4;
      int xRand = tileRandom.nextInt(width) ;
      int yRand = tileRandom.nextInt(height) ;
       Edge edge = Edge.values()[edgeRand];
       int x, y;
       switch (edge) {
       case TOP:
          x = xRand * TILE_WIDTH;
          y = (height) * TILE_WIDTH;
          break;
       case RIGHT:
          x = (width) * TILE_WIDTH;
          y = yRand * TILE_WIDTH;
          break;
       case BOTTOM:
          x = xRand * TILE_WIDTH;
          y = TILE_WIDTH * -1;
          break;
       case LEFT:
       default:
          x = TILE_WIDTH * -1;
          y = yRand * TILE_WIDTH;
          break;
       }
       nextTile = createRandomTile(x, y);
       return nextTile;
    }
 
    public void popTile(Tile tile) {
       tiles.remove(tile);
    }
 
    public List<Tile> getTiles() {
       return tiles;
    }
 
    public void setTiles(List<Tile> tiles) {
       this.tiles = tiles;
    }
 
    public int getWidth() {
       return width;
    }
 
    public void setWidth(int width) {
       this.width = width;
    }
 
    public int getHeight() {
       return height;
    }
 
    public void setHeight(int height) {
       this.height = height;
    }
 
    public int getDufficulty() {
       return dufficulty;
    }
 
    public void setDufficulty(int dufficulty) {
       this.dufficulty = dufficulty;
    }
 
    public Tile getNextTile() {
       return nextTile;
    }
 
    public void setNextTile(Tile nextTile) {
       this.nextTile = nextTile;
    }
 
 }
