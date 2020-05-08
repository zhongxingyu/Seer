 package eu32k.ludumdare.ld26.level;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 
 import eu32k.ludumdare.ld26.Direction;
 import eu32k.ludumdare.ld26.level.Tile.Rotation;
 import eu32k.ludumdare.ld26.level.Tile.Type;
 import eu32k.ludumdare.ld26.state.GlobalState;
 import eu32k.ludumdare.ld26.state.StateMachine;
 
 public class Level {
 
    private Tile[][] tileMatrix;
 
    private int width;
 
    private int height;
 
    private int dufficulty;
 
    private Random tileRandom;
 
    private List<Tile> tiles;
 
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
             Tile tile = createRandomTile(j, i);
             tiles.add(tile);
             tileMatrix[i][j] = tile;
          }
       }
       placeNeighbors();
       System.out.println(tiles);
    }
 
    private Tile createRandomTile(float x, float y) {
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
                south = tileMatrix[i - 1][j];
                addNeighbor(tile, south, Direction.S);
             }
             if (j > 0) {
                west = tileMatrix[i][j - 1];
                addNeighbor(tile, west, Direction.W);
             }
             if (i < height - 1) {
                north = tileMatrix[i + 1][j];
                addNeighbor(tile, north, Direction.N);
             }
             if (j < width - 1) {
                east = tileMatrix[i][j + 1];
                addNeighbor(tile, east, Direction.E);
             }
             System.out.println(tile.getType());
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
       System.out.println("spawn tile");
       List<Tile> edgeTiles = new ArrayList<Tile>();
       for (Tile tile : tiles) {
          if (tile.getNeighbors().size() < 4) {
             edgeTiles.add(tile);
          }
       }
       int randomTile = tileRandom.nextInt(edgeTiles.size());
       Tile target = edgeTiles.get(randomTile);
       float xRand = target.getX();
       float yRand = target.getY();
       Set<Direction> dirs = target.getNeighbors().keySet();
       List<Direction> allDirs = new ArrayList<Direction>();
       allDirs = Arrays.asList(Direction.values());
       Iterator<Direction> it = allDirs.iterator();
       List<Direction> freeDirs = new ArrayList<Direction>();
       while (it.hasNext()) {
          Direction dir = it.next();
          if (!dirs.contains(dir)) {
             freeDirs.add(dir);
          }
       }
       int dirRand = tileRandom.nextInt(freeDirs.size());
       Direction dir = freeDirs.get(dirRand);
       System.out.println(dir);
       float x, y;
       switch (dir) {
       case S:
          x = xRand;
         y = yRand - target.getSprite().getWidth();
          break;
       case W:
          x = xRand - target.getSprite().getWidth();
          y = yRand;
          break;
       case N:
          x = xRand;
         y = yRand + target.getSprite().getWidth();
          break;
       case E:
       default:
          x = xRand + target.getSprite().getWidth();
          y = yRand;
          break;
       }
       Tile nextTile = createRandomTile(x, y);
       nextTile.getNeighbors().put(Direction.getOpposite(dir), target);
       target.getNeighbors().put(dir, nextTile);
       this.tiles.add(nextTile);
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
       this.tiles.add(nextTile);
       this.nextTile = nextTile;
    }
 
    public void updateNeighbors(Tile spawned, Direction dir) {
       Tile copy = spawned;
       List<Tile> row = new ArrayList<Tile>();
       // List<Tile> rowCopy = new ArrayList<Tile>();
       do {
          row.add(copy);
       } while ((copy = copy.getNeighbors().get(dir)) != null);
       // Collections.copy(rowCopy, row);
       for (Tile toShift : row) {
          Tile newLeft, newRight;
          Direction dirLeft = CCW(dir);
          Direction dirRight = CW(dir);
          Tile currentLeft = toShift.getNeighbors().get(dirLeft);
          Tile currentRight = toShift.getNeighbors().get(dirRight);
          Tile front = toShift.getNeighbors().get(dir);
          
 
          if (front != null) {
             newRight = front.getNeighbors().get(dirRight);
             newLeft = front.getNeighbors().get(dirLeft);
             if (newRight != null) {
                newRight.getNeighbors().put(dirLeft, toShift);
                toShift.getNeighbors().put(dirRight, newRight);
             }
             if (newLeft != null) {
                newLeft.getNeighbors().put(dirRight, toShift);
                toShift.getNeighbors().put(dirLeft, newLeft);
             }
             if(front.getNeighbors().get(dir) == null) {
                toShift.getNeighbors().remove(dir);
             }
          }
 //         if(currentLeft != null) {
 //            currentLeft.getNeighbors().remove(dirRight);
 //         }
 //         if(currentRight != null) {
 //            currentRight.getNeighbors().remove(dirLeft);
 //         }
 
       }
 
    }
 
    public Direction CW(Direction dir) {
       switch (dir) {
       case N:
          return Direction.E;
       case E:
          return Direction.S;
       case S:
          return Direction.W;
       case W:
          return Direction.N;
       default:
          return null;
       }
    }
 
    public Direction CCW(Direction dir) {
       switch (dir) {
       case N:
          return Direction.W;
       case W:
          return Direction.S;
       case S:
          return Direction.E;
       case E:
          return Direction.N;
       default:
          return null;
       }
    }
 }
