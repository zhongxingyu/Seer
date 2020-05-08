 package app;
 import inventory.Inventory;
 import inventory.Item;
 
 import java.awt.Graphics2D;
 import java.util.Comparator;
 
 import level.Level;
 import player.Player;
 
 import com.golden.gamedev.GameEngine;
 import com.golden.gamedev.GameObject;
 import com.golden.gamedev.object.PlayField;
 import com.golden.gamedev.object.Sprite;
 
 
 public class RPGame extends GameObject {
    private PlayField field = new PlayField();
     //private Background bg;
     private Player player;
     //private Dialog dialog;
     private Level level;
     private Inventory myInventory;
     String levelFileName = "rsc/savedmaps/level1.json";
     String lower, upper;
     boolean pausedForInventory = false;
     
     public RPGame (GameEngine parent) {
         super(parent);
     }
 
 
     public void initResources () {
     	level = new Level(bsLoader, bsIO, this, levelFileName);
     	field.setComparator(new Comparator() {
 			public int compare(Object o1, Object o2) {
 				// sort based on y-order
 				return (int) (((Sprite) o1).getY()-((Sprite) o2).getY());
 			}
 		} );   	
     	
     }
 
 
    public void render (Graphics2D g) {
    	field.render(g);
    }
 
 
    public void update (long elapsed) {
    	field.update(elapsed);
    }
 
 
     public Player getPlayer () {
         return player;
     }
     
     public void addItems(Item itm){
         myInventory.add(itm);
     }
     
     public PlayField getField(){
     	return field;
     }
 
 
     public void pauseGameForInventory(){
         pausedForInventory = true;
     }
     public void unPauseGameForInventory(){
         pausedForInventory = false;
     }
 
 }
