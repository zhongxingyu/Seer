 package com.fisherevans.lrk.states.adventure;
 
 import com.fisherevans.lrk.LRK;
 import com.fisherevans.lrk.Resources;
 import com.fisherevans.lrk.StateLibrary;
 import com.fisherevans.lrk.launcher.Game;
 import com.fisherevans.lrk.managers.DisplayManager;
 import com.fisherevans.lrk.managers.InputManager;
 import com.fisherevans.lrk.states.GFX;
 import com.fisherevans.lrk.states.LRKState;
 import com.fisherevans.lrk.states.adventure.entities.AdventureEntity;
 import com.fisherevans.lrk.states.adventure.entities.Player;
 import com.fisherevans.lrk.states.adventure.entities.Wall;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.FixtureDef;
 import org.jbox2d.dynamics.World;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
 import org.newdawn.slick.tiled.TiledMap;
 
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Fisher evans
  * Date: 5/12/13
  * Time: 5:28 PM
  */
 public class AdventureState extends LRKState
 {
 
     public static final float
        TILE_SIZE = 32f;
 
 
     public static float TILES_WIDE, TILES_HIGH;
 
     private ArrayList<AdventureEntity> _entities, _walls;
     private Player _player, _camera;
     private TiledMap _map;
     private World _world;
     private Image _cursor;
     private boolean _takeMouse = true;
 
     public AdventureState() throws SlickException
     {
         super(StateLibrary.getTempID());
     }
 
     @Override
     public void init() throws SlickException
     {
         resize();
 
         setCursor(Resources.getImage("res/test/images/cursor.png"));
         setGrabMouse(true);
 
         _world = new World(new Vec2(0, 0f), true);
 
         // a list of entities in the world
         _entities = new ArrayList<>();
         _player = new Player(14f, 14f, _world);
         _camera = _player;
 
         _entities.add(_player);
 
         try // load the map
         {
             _map = new TiledMap("res/maps/test.tmx");
         }
         catch(Exception e)
         {
             e.printStackTrace();
             LRK.log("FAILED TO LOAD MAP");
             System.exit(111);
         }
 
         // a list of wall's in the world
         _walls = new ArrayList<AdventureEntity>();
         int layerId = _map.getLayerIndex("collision");
         for(int y = 0;y < _map.getHeight();y++)
         {
             for (int x = 0; x < _map.getWidth(); x++) // loop through each tile in the map
             {
                 int id = _map.getTileId(x, y, layerId); // get the global id
                 if(id > 0) // if the tile isn't empty
                 {
                     FixtureDef def = JBox2DUtils.generateFixture(id); // get the shape based on the id
                     if (def != null) // null means the tile id doesn't have a predefined shape
                     {
                         _walls.add(new Wall(x, y, def, _world)); // but if it does, add the tile shape to the world
                     }
                 }
             }
         }
     }
 
     @Override
     public void enter() throws SlickException
     {
 
     }
 
     @Override
     public void exit() throws SlickException
     {
 
     }
 
     @Override
     public void render(Graphics gfx) throws SlickException
     {
         // get the vector from the center of the screen to the mouse
         Vec2 aimShift = new Vec2(InputManager.getMouseX()-DisplayManager.getRenderWidth()/2f,
                 InputManager.getMouseY()-DisplayManager.getRenderHeight()/2f);
        aimShift.mulLocal(0.3f); // scale it by about a third (for moving the viewport)
         _player.setDegrees((float) Math.toDegrees(Math.atan2(aimShift.y, aimShift.x)));
 
         // when drawing entities, shift each by x and y shift
         // subtract the camera pos and add half the display and subtract the aim shift
         float xShift = (-_camera.getX() + TILES_WIDE/2f)*TILE_SIZE - aimShift.x;
         float yShift = (-_camera.getY() + TILES_HIGH/2f)*TILE_SIZE - aimShift.y;
 
         // the map shift is scaled to the nearest real display pixel
         // stops screen tares
         float mapYShift = GFX.filterDrawPosition(yShift);
         float mapXShift = GFX.filterDrawPosition(xShift);
 
         // what tile to start drawing at
         int startX = (int)(_camera.getX()+aimShift.x/TILE_SIZE) - (int)TILES_WIDE/2;
         int startY = (int)(_camera.getY()+aimShift.y/TILE_SIZE) - (int)TILES_HIGH/2;
 
         drawMapLayer(mapXShift, mapYShift, startX, startY, "background");
 
         for(AdventureEntity ent:_entities) // for each entity
         {
             // draw them with the x and y shifts
             GFX.drawImageCentered(xShift + ent.getX()*TILE_SIZE, yShift + ent.getY()*TILE_SIZE, ent.getImage());
         }
     }
 
     /**
      * Draws a tiled map layer on the primary graphics element
      * @param xShift the sub tile x shift to move the whole layer by
      * @param yShift the sub tile x shift to move the whole layer by
      * @param startX the x index of the tile to begin drawing at
      * @param startY the y index of the tile to begin drawing at
      * @param layers the layers to draw
      */
     private void drawMapLayer(float xShift, float yShift, int startX, int startY, String... layers)
     {
         int[] layerIds = new int[layers.length];
         for(int layerNumber = 0;layerNumber < layers.length;layerNumber++)
         {
             layerIds[layerNumber] = _map.getLayerIndex(layers[layerNumber]);
         }
 
        for(int y = startY;y <= startY+TILES_HIGH+2;y++)
         {
            for(int x = startX;x <= startX+TILES_WIDE+2;x++) // for each tile on the screen
             {
                 try
                 {
                     // draw that tiles image
                     for(Integer layerId:layerIds)
                         GFX.drawImageCentered(x*TILE_SIZE + xShift, y*TILE_SIZE + yShift, _map.getTileImage(x, y, layerId));
                 }
                 catch(Exception e) { }
             }
         }
     }
 
     @Override
     public void update(float delta) throws SlickException
     {
         for(AdventureEntity e: _entities)
         {
             e.update((int)delta); // logic
         }
         _world.step(delta, 5, 5); // physics
     }
 
     @Override
     public void destroy() throws SlickException
     {
     }
 
     @Override
     public void resize()
     {
        TILES_WIDE = (float) Math.floor(DisplayManager.getRenderWidth()/TILE_SIZE);
        TILES_HIGH = (float) Math.floor(DisplayManager.getRenderHeight()/TILE_SIZE);
     }
 
     public void keyPressed(int key, char c)
     {
         if(key == Input.KEY_BACKSLASH) System.exit(0);
     }
 }
