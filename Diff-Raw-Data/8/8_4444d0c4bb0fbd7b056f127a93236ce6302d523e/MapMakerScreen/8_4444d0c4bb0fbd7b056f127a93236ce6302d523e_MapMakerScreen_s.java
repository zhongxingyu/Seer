 package vooga.towerdefense.gameeditor.gamemaker.editorscreens.mapeditor;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.List;
 import javax.swing.ImageIcon;
 import javax.swing.JPanel;
 import vooga.towerdefense.model.tiles.factories.DefaultTileFactory;
 import vooga.towerdefense.model.tiles.factories.TileFactory;
 
 
 /**
  * This is the screen where the game maker places the tiles in order to make
  * a map. The tiles are placed one at a time on a grid contained in the screen.
  * 
  * @author Leonard K. Ng'eno
  * 
  */
 public class MapMakerScreen extends JPanel {
 
     private static final long serialVersionUID = 1L;
     private static final DefaultTileFactory DEFAULT_TILE_FACTORY = new DefaultTileFactory();
     private static final int MINIMUM_TILE_SIZE = 10;
     private static final String RESOURCE_LOCATION = "/vooga/towerdefense/images/background/";
     private Dimension mySize;
     private Integer myTileSize;
     private MouseAdapter myMouseListener;
     private List<Grid> myGrids;
     private String myMap[][];
     private TileFactory myTileToBuild;
     private String myMapString;
     private java.awt.Image myBackgroundImage;
 
     public MapMakerScreen (Dimension size) {
         setSize(size);
         setPreferredSize(size);
         setBackgroundImage("bg1.jpg");
         mySize = size;
         myTileSize = 50;
         myGrids = new ArrayList<Grid>();
         myTileToBuild = DEFAULT_TILE_FACTORY;
         myMapString = "";
 
         makeListener();
         addMouseListener(myMouseListener);
     }
 
     public void setBackgroundImage (String fileName) {
         myBackgroundImage =
                 new ImageIcon(getClass().getResource(RESOURCE_LOCATION + fileName)).getImage();
     }
 
     @Override
     public void paintComponent (Graphics pen) {
         super.paintComponent(pen);
         pen.drawImage(myBackgroundImage, 0, 0, getWidth(), getHeight(), null);
         setBackground(Color.CYAN);
         paintGridLines(pen);
         paintTilesOnGrid(pen);
     }
 
     private void paintTilesOnGrid (Graphics pen) {
         for (Grid g : myGrids) {
             g.paint((Graphics2D) pen);
         }
        getMapString();
     }
 
     /**
      * This method resets the sizes of the tiles. This also results in the paths the
      * game maker had build being reset.
      * 
      * @param size The size of each tile in pixels
      */
     public void setTileSizes (int size) {
         myTileSize = size;
         myTileToBuild = DEFAULT_TILE_FACTORY;
         repaint();
     }
 
     private void paintGridLines (Graphics pen) {
         if (myTileSize > MINIMUM_TILE_SIZE) {
             myGrids.removeAll(myGrids);
            myMap = new String[mySize.width / myTileSize][mySize.height / myTileSize];
             for (int i = 0; i < mySize.width; i += myTileSize) {
                 for (int j = 0; j < mySize.height; j += myTileSize) {
                     pen.drawLine(i, 0, i, mySize.height);
                     pen.drawLine(0, j, mySize.width, j);
                     Grid rect = new Grid(i, j, myTileSize, myTileSize, DEFAULT_TILE_FACTORY);
                     myGrids.add(rect);
                     myMap[i / myTileSize][j / myTileSize] = DEFAULT_TILE_FACTORY.getTileId() + " ";
                 }
             }
         }
 
     }
 
     private void makeListener () {
         myMouseListener = new MouseAdapter() {
             @Override
             public void mouseClicked (MouseEvent e) {
                 setTileClicked(e.getPoint());
             }
         };
     }
 
     private void setTileClicked (Point point) {
         for (Grid tile : myGrids) {
             if (tile.contains(point)) {
                 tile.setTile(myTileToBuild);
                 myMap[tile.x / myTileSize][tile.y / myTileSize] = myTileToBuild.getTileId() + " ";
                 paintTilesOnGrid(getGraphics());
             }
         }
     }
 
     /**
      * Sets the tile to build to be tile t.
      * 
      * @param t The tile to be fixed on the grid that the game maker clicks on
      */
     public void setTile (TileFactory t) {
         myTileToBuild = t;
     }
 
     /**
      * gets the string representation of the tile id's in the map
      * 
      * @return a string representation of the tile id's
      */
     public String getMapString () {
         myMapString = "";
         for (int j = 0; j < myMap.length; j++) {
             for (String[] element : myMap) {
                myMapString += element[j]; 
             }
         }
         return myMapString;
     }
 
     /**
      * get map width
      * 
      * @return width of the map
      */
     public String getMapWidth () {
         return Integer.toString(getWidth());
     }
 
     /**
      * get map height
      * 
      * @return height of map
      */
     public String getMapHeight () {
         return Integer.toString(getHeight());
     }
 }
