 package civ;
 
 import java.util.ArrayList;
 import java.util.TreeSet;
 import java.util.Random;
 
 import static civ.State.UnitState.UnitSelected;
 
 public class GameMap{
     private static final State state = State.getInstance();
     private Tile currentTile = null;
     private Tile[][] tiles;
     private static GameMap map;
     private GameMapView gmv;
     private int height = 15, width = 15;
 
     private int[][] offsets = {
         {-1,-1},
         {0,-1},
         {1,0},
         {1,1},
         {0,1},
         {-1,0}
     };
 
 
     public GameMap(GameMapView gmv){
         super();
         map = this;
 
         // Parsing the map
         parseMap("Put server map data here");
         exploreMap();
         this.gmv = gmv;
         // Put all TileViews on the GameMapView
         for(int j=getHeight()-1; j>=0; --j){
             for(int i=getWidth()-1; i>=0; --i){
                 gmv.add(tiles[i][j].getView());
             }
         }
     }
 
     /**
      * Temporary solution to be able to reach the map from anywhere.
      * Assumes GameMapView initialized the object before this is called.
      */
     public static GameMap getInstance(){
         return map;
     }
 
     public void exploreMap(){
         for(Tile[] temp : tiles){
             for(Tile tile : temp){
                 if(tile.hasUnit() && tile.getUnit().isAlly()){ 
                     for(Tile t : getNeighbours(tile, tile.getUnit().getType().getVision())){
                         t.setExplored(true);
                     }
                 }
                 tile.getView().repaint();
             }
         }
     }
 
     /**
      * Get a specific tile from the array.
      */
     public Tile getTile(int x, int y){
         if(x >= getWidth() || y >= getHeight() || x < 0 || y < 0){
             return null;
         } 
         return tiles[x][y];
     }
 
     /**
      * Get a specific tile from the array at the 
      * coordinates relative to GameMapView.
      */
     public Tile getTileAt(int x, int y){
         /*
            kolumn =((pixelx - offsetX) / bredd)
            rad = ((pixely - offsetY) / (höjd/2))
            x = (kolumn + rad) / 2
            y = (rad - kolumn) / 2
            */
         for(int j=getHeight()-1; j>=0; --j){
             for(int i=getWidth()-1; i>=0; --i){
                 if(tiles[i][j].getView().contains(x, y)){
                     return tiles[i][j];
                 }
             }
         }
         return null;
         /*
            double kolumn = ((x - 120 * width) / 120.0 );
            double rad = (y / 70.0);
            int absX = (int)Math.floor((kolumn + rad) / 2);
            int absY = (int)Math.floor((rad - kolumn) / 2);
            System.out.println(x +","+ y +" = "+ absX +","+ absY);
            return getTile(absX, absY);
            */
     }
 
     /**
      * Get the width of the map.
      * Width is in the X direction.
      */
     public int getWidth(){
         return width;
     }
 
     /**
      * Get the height of the map.
      * Height is in the Y direction.
      */
     public int getHeight(){
         return height;
     }
 
     /**
      * Get all neighbours to a tile in an ArrayList(Not finished yet!)
      * @param tile Neighbours to this tile will be returned
      * @param range Neighbours in this range will be returned.
      */
     public ArrayList<Tile> getNeighbours(Tile tile, int range){
         return getNeighbours(tile, range, false);
     }
 
     public ArrayList<Tile> getNeighbours(Tile tile, int range, boolean considerTerrain){
         ArrayList<Tile> acc = new ArrayList<Tile>();
         getNeighbours(tile, range, acc, considerTerrain);
         acc.remove(tile);
         return acc;
     }
 
     private ArrayList<Tile> getNeighbours(Tile tile, boolean terrain){
         ArrayList<Tile> result = new ArrayList<Tile>();
         for(int[] off : offsets){
             // Add all surrounding tiles
             Tile t = getTile(tile.getX() - off[0], tile.getY() - off[1]);
             if(t != null)
                 if(terrain && state.getUnitState() == UnitSelected){
                     if(t.getTerrain().isTraversible(state.getSelectedUnit())){
                         result.add(t);
                     }
                 }
                 else{
                     result.add(t);
                 }
         }
         return result;
     }
 
     private ArrayList<Tile> getNeighbours(Tile tile, int range, ArrayList<Tile> acc, boolean terrain){
         if(range < 1){
             return new ArrayList<Tile>();
         }
         int x = tile.getX();
         int y = tile.getY();
 
         ArrayList<Tile> neighbours = getNeighbours(tile, terrain);
         for(Tile t1 : neighbours){
             // Add all surrounding tiles
             if(!acc.contains(t1))
                 acc.add(t1);
         }
         if(range > 1){
             // Recurse through all surrounding tiles with one less range cost
             for(Tile t2 : neighbours){
                 getNeighbours(t2, range - 1, acc, terrain);
             }
         }
 
         return acc;            
     }
 
     public int getDistance(Tile a, Tile b){
         return 1; 
     }
     /*
        public void scale(double grade){
        double size;
        for(int j=height-1; j>=0; --j){
        for(int i=width-1; i>=0; --i){
        if(tiles[i][j].getView().getWidth()*grade > 200){
        size = 200;
        }
        else if(tiles[i][j].getView().tWidth()*grade < 50){
        size = 50;
        }
        else{
        size = tiles[i][j].getView().getWidth()*grade;
        }
        tiles[i][j].getView().resize(150);
        }
        }
        }
 
     /**
      * Sets all the tiles in the map.
      *
      * @param terrain Indata from the server interface.
      */
     public void parseMap(String terrain){
         TerrainType[] tt = TerrainType.values();
         Random r = new Random();
         Tile[][] result = new Tile[width][];
         for(int k=0; k<width; k++){
             result[k] = new Tile[height];
         }
         for(int j=height-1; j>=0; --j){
             for(int i=width-1; i>=0; --i){
                 int rand = Math.abs(r.nextInt())%tt.length;
                 Tile temp = new Tile(tt[rand], i, j);
                 result[i][j] = temp;
             }
         }
         tiles = result;
        tiles[1][1].setUnit(new PhysicalUnit(PhysicalUnitType.Musketeer, Player.getInstance("Andy")));
     }
 }
