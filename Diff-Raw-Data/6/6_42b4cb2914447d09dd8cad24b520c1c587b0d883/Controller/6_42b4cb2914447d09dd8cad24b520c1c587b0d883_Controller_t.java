 package vooga.towerdefense.controller;
 
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import vooga.towerdefense.controller.modes.BuildMode;
 import vooga.towerdefense.controller.modes.ControlMode;
 import vooga.towerdefense.controller.modes.SelectMode;
 import vooga.towerdefense.gameElements.GameElement;
 import vooga.towerdefense.model.GameMap;
 import vooga.towerdefense.model.GameModel;
 import vooga.towerdefense.model.Tile;
 import vooga.towerdefense.util.Pixmap;
 import vooga.towerdefense.view.TDView;
 
 
 /**
  * As part of a MVC framework, the Controller controls how the view interacts
  * with the model.
  * 
  * @author Jimmy Longley
  * @author Erick Gonzalez
  * @author Angelica Schwartz
  */
 public class Controller {
     private GameModel myModel;
     private TDView myView;
     private ControlMode myControlMode;
 
     //TODO: controller constructor should take waves & map in order to initialize GameModel?
     public Controller () {
         myModel = new GameModel(this, null, new GameMap(null, 800, 600, null), null);
         myView = new TDView(this);
         myControlMode = new SelectMode();
     }
 
     /**
      * handles a click to the map appropriately depending
      * on the mode.
      * 
      * @param p is the location of the click
      */
     public void handleMapMouseDrag (Point p) {
         myControlMode.handleMapMouseDrag(p, this);
     }
     
     /**
      * handles a mouse drag on the map appropriately depending
      * on the mode.
      * 
      * @param p is the location of the mouse
      */
     public void handleMapClick (Point p) {
         myControlMode.handleMapClick(p, this);
     }
     
     /**
      * changes the mode to BuildMode and gets the item the user
      *          wants to build from the Shop.
      * @param itemName is the name of the item the user wants to
      *          buy
      */
     public void handleShopClickOnItem(String itemName) {
         GameElement itemToBuy = myModel.getShop().getShopItem(itemName);
         BuildMode myNewMode = new BuildMode();
         myNewMode.setItemToBuild(itemToBuy);
         myControlMode = myNewMode;
     }
     
     /**
      * displays information about the GameElement on the tile.
      * @param p is the point that was clicked.
      */
     public void displayElementInformation(Point p) {
         Tile tile = myModel.getTile(p);
         if (tile.containsElement()) {
             myView.getTowerInfoScreen().displayInformation(tile.getElement().getAttributes().toString());
            //TODO: implement tower upgrades section
//            if (tile.getElement() instanceof Tower) {
//                myView.getTowerInfoScreen().showUpgradeInformation(tile.getElement());
//            }
         }
     }
 
 //    //testing method to check if displaying the correct info
 //    public void displayTileCoordinates (Point p) {
 //        Tile t = myModel.getTile(p);
 //        Point center = t.getCenter();
 //        System.out.println(center);
 //        myView.getTowerInfoScreen().displayInformation(center.toString());
 //    }
     
     /**
      * places the new item onto the map & changes the mode
      *          back to SelectMode.
      * @param item
      * @param p
      */
     public void fixItemOnMap (GameElement item, Point p) {
         Tile myTile = myModel.getTile(p);
         myTile.setTower(item);
         myModel.getMap().addToMap(item);
         displayMap();
         myControlMode = new SelectMode();
     }
     
     public void displayMap() {
         myView.getMapScreen().update();
     }
     
     public void update(double elapsedTime) {
         myModel.update(elapsedTime);
     }
     
     public void paintMap(Graphics pen) {
         myModel.paintMap((Graphics2D) pen);
     }
     
     /**
      * paints the ghost image of the item on the MapScreen.
      * @param p is the mouselocation
      * @param itemImage is the image
      */
     public void paintGhostImage (Point p, Pixmap itemImage) {
        displayMap();
         myView.getMapScreen().paintGhostImage(p, itemImage);
     }
 
 }
