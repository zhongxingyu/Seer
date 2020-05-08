 
 package com.wartricks.systems;
 
 import com.artemis.systems.VoidEntitySystem;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.utils.Array;
 import com.wartricks.boards.GameMap;
 import com.wartricks.utils.MapTools;
 import com.wartricks.utils.PlatformUtils;
 
 public class MapRenderSystem extends VoidEntitySystem {
     private SpriteBatch batch;
 
     private TextureAtlas atlas;
 
     private Array<AtlasRegion> textures;
 
     private OrthographicCamera camera;
 
     private GameMap gameMap;
 
     public MapRenderSystem(OrthographicCamera camera, GameMap gameMap) {
         this.camera = camera;
         this.gameMap = gameMap;
     }
 
     @Override
     protected void initialize() {
         batch = new SpriteBatch();
         // Load the map tiles into an Array
         atlas = new TextureAtlas(Gdx.files.internal(PlatformUtils
                 .getPath("resources/textures/maptiles.atlas")), Gdx.files.internal(PlatformUtils
                 .getPath("resources/textures/")));
         textures = atlas.findRegions(MapTools.name);
     }
 
     @Override
     protected boolean checkProcessing() {
         return true;
     }
 
     @Override
     protected void processSystem() {
         TextureRegion reg;
         int x, y;
         // Get bottom left and top right coordinates of camera viewport and convert
         // into grid coordinates for the map
         // Get bottom left and top right coordinates of camera viewport and convert
         // into grid coordinates for the map
         int x0 = MathUtils.floor(camera.frustum.planePoints[0].x / MapTools.col_multiple) - 1;
         int y0 = MathUtils.floor(camera.frustum.planePoints[0].y / MapTools.row_multiple) - 1;
         int x1 = MathUtils.floor(camera.frustum.planePoints[2].x / MapTools.col_multiple) + 2;
         int y1 = MathUtils.floor(camera.frustum.planePoints[2].y / MapTools.row_multiple) + 1;
         // Restrict the grid coordinates to realistic values
         if ((x0 % 2) == 1) {
             x0 -= 1;
         }
         if (x0 < 0) {
             x0 = 0;
         }
         if (x1 > gameMap.width) {
             x1 = gameMap.width;
         }
         if (y0 < 0) {
             y0 = 0;
         }
         if (y1 > gameMap.height) {
             y1 = gameMap.height;
         }
         // Loop over everything in the window to draw. Draw 2 columns at once
         for (int row = y0; row < y1; row++) {
             for (int col = x0; col < (x1 - 1); col += 2) {
                 x = col * MapTools.col_multiple;
                 y = row * MapTools.row_multiple;
                 reg = textures.get(gameMap.map[col][row]);
                 batch.draw(reg, x, y, 0, 0, reg.getRegionWidth(), reg.getRegionHeight(), 1, 1, 0);
                 x += MapTools.col_multiple;
                 y += MapTools.row_multiple / 2;
                 reg = textures.get(gameMap.map[col + 1][row]);
                 batch.draw(reg, x, y, 0, 0, reg.getRegionWidth(), reg.getRegionHeight(), 1, 1, 0);
             }
            if ((x1 >= gameMap.width) && ((gameMap.width % 2) == 1)) {
                 final int col = gameMap.width - 1;
                 x = col * MapTools.col_multiple;
                 y = row * MapTools.row_multiple;
                 reg = textures.get(gameMap.map[col][row]);
                 batch.draw(reg, x, y, 0, 0, reg.getRegionWidth(), reg.getRegionHeight(), 1, 1, 0);
             }
         }
         // This line can draw a small image of the whole map
         // batch.draw(gameMap.texture,0,0);
     }
 
     @Override
     protected void begin() {
         batch.setProjectionMatrix(camera.combined);
         batch.begin();
     }
 
     @Override
     protected void end() {
         batch.end();
     }
 }
