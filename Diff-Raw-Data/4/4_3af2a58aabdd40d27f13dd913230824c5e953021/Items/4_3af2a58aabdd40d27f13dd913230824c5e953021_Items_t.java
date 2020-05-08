 package ship.ui.inventory;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import media.FontHolder;
 import media.ManagedSpriteSheet;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 
 import ship.View;
 import ship.control.KeyReceiver;
 import ship.control.Keys;
 import ship.ui.Box;
 import ship.ui.inventory.tilecreator.TileCreator;
 import ship.world.vehicle.Vehicle;
 
 public class Items extends Box implements KeyReceiver, Focusable {
     public static final int WIDTH = 8;
 
     public static final int X_OFF = 15;
     public static final int TEXT_X_OFF = 42;
     public static final int Y_OFF = 15;
     public static final int TEXT_Y_OFF = 5;
 
     public static final int ITEM_HEIGHT = 40;
 
     public static final int HIGHLIGHT_W = 300;
 
     private Inventory parent;
 
     private FontHolder         fonts;
     private ManagedSpriteSheet tiles;
     private ManagedSpriteSheet highlight;
 
     private List<TileCreator> items;
     private int selected;
 
     private boolean focus;
 
     public Items(Inventory parent, int x, int y, List<TileCreator> items) throws SlickException {
         super(parent, parent.view().loader(), x, y, WIDTH, (View.window().getHeight() / Box.TH) - 1);
         this.parent = parent;
 
         fonts     = parent.view().fonts();
        tiles     = parent.view().loader().loadManagedSpriteSheet(         "tiles",  Vehicle.TW,  Vehicle.TH);
        highlight = parent.view().loader().loadManagedSpriteSheet("item_highlight", HIGHLIGHT_W, ITEM_HEIGHT);
 
         this.items = new ArrayList<TileCreator>(items);
     }
 
     public void setFocus(boolean val) { focus = val; }
 
     @Override
     public boolean keyPressed(Keys keys, int key, char c) {
         if (focus) {
             if (key == keys.buildUp()) {
                 selected--;
                 updateItems();
                 return true;
 
             } else if (key == keys.buildDown()) {
                 selected++;
                 updateItems();
                 return true;
             }
         }
 
         return false;
     }
 
     public void rebuildItemList(List<TileCreator> itemList, Tag tag) {
         items.clear();
         for (TileCreator tile : itemList)
             if (tile.matches(tag))
                 items.add(tile);
 
         updateItems();
     }
     private void updateItems() {
         selected = selected % items.size();
         if (selected == -1)
             selected = items.size() - 1;
 
         parent.sendSelectedItemAndSubTile();
 
         parent.updateSubMenu();
 
     }
 
     public TileCreator getSelected() {
         if (items.size() > selected)
             return items.get(selected);
         return null;
     }
 
     public void render(GameContainer gc, Graphics g) {
         super.render(gc, g);
 
 
         for (int i = 0; i < items.size(); i++) {
             if (i == selected) {
                 if (focus)
                     highlight.getSpriteSheet().getSprite(0, 0).draw(ix() + getWidth()/2 - HIGHLIGHT_W/2, iy() + i*ITEM_HEIGHT + Y_OFF - 4);
                 else
                     highlight.getSpriteSheet().getSprite(1, 0).draw(ix() + getWidth()/2 - HIGHLIGHT_W/2, iy() + i*ITEM_HEIGHT + Y_OFF - 4);
                 fonts.invSelected().drawString(ix() +X_OFF +TEXT_X_OFF, iy() +i*ITEM_HEIGHT +Y_OFF +TEXT_Y_OFF, items.get(i).getName());
             } else
                 fonts.inv().drawString(ix() +X_OFF +TEXT_X_OFF, iy() +i*ITEM_HEIGHT +Y_OFF +TEXT_Y_OFF, items.get(i).getName());
 
             tiles
               .getSpriteSheet()
               .getSprite(items.get(i).getIcon() % tiles.getSpriteSheet().getHorizontalCount(),
                          items.get(i).getIcon() / tiles.getSpriteSheet().getHorizontalCount())
                 .draw(ix() + X_OFF, iy() + i*ITEM_HEIGHT + Y_OFF);
         }
     }
 
     @Override
     public boolean keyReleased(Keys keys, int key, char c) {
         return false;
     }
 
 }
