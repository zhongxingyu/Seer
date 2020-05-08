 package edu.smcm.gamedev.butterseal;
 
 import java.util.Map;
 
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.maps.MapLayer;
 import com.badlogic.gdx.maps.tiled.TiledMap;
 import com.badlogic.gdx.maps.tiled.TiledMapTile;
 import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
 import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
 import com.badlogic.gdx.maps.tiled.TmxMapLoader;
 import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
 
 public enum BSMap {
     ICE_CAVE_ENTRY(BSAsset.ICE_CAVE_ENTRY, "ice-cave-entry", new BSGameStateActor() {
         @Override
         public void act(BSGameState state) { }
         @Override
         public void update(BSGameState state) { }
         @Override
         public void reset(BSGameState state) { }
         @Override
         public void load(BSGameState state) {
         }
     }),
     ICE_CAVE(BSAsset.ICE_CAVE, "ice-cave", new BSGameStateActor() {
         @Override
         public void act(BSGameState state) {
             BSMap m = state.currentMap;
             TiledMapTileLayer dark = m.getLayer("dark");
             TiledMapTileLayer light = m.getLayer("light");
            TiledMapTileLayer wall = m.getLayer("wall");
             TiledMapTile invis = BSTile.getTileForProperty(m, "invisible", "true");
 
             if(state.currentTile.hasProperty(m, light, "beacon", "on")) {
                 // Clear the surrounding tiles
                 for (int i = -1; i <= 1; i++) {
                     for (int j = -1; j <= 1; j++) {
                         BSTile lookingAt = new BSTile(state.currentTile);
                         lookingAt.transpose(i, j);
                         if(lookingAt.isContainedIn(dark)) {
                             Cell point = lookingAt.getCell(dark);
                             if(!point.getTile().equals(invis)) {
                                 if(ButterSeal.DEBUG > 2) {
                                     System.out.printf("Clearing tile %d,%d%n", lookingAt.x, lookingAt.y);
                                 }
                                 point.setTile(invis);
                             }
                         }
                     }
                 }
                 // Clear the rank and file
                 for(BSDirection d : BSDirection.values()) {
                     if(ButterSeal.DEBUG > 2) {
                         System.out.println("Clearing " + d);
                     }
                     BSTile point = new BSTile(state.currentTile);
                     while(point.isContainedIn(dark) &&
                         !point.hasProperty(m, wall, "wall", "true")) {
                         point.getCell(dark).setTile(invis);
                         point.transpose(d.dx, d.dy);
                     }
                     if(point.isContainedIn(dark)) {
                         Cell i = point.getCell(dark);
                         i.setTile(invis);
                     }
                 }
             }
         }
         @Override
         public void update(BSGameState state) {
             BSMap m = state.currentMap;
             TiledMapTileLayer light = m.getLayer("light");
             TiledMapTile beacon_on = BSTile.getTileForProperty(m, "beacon", "on");
 
             // update beacon state
             if (state.isUsingPower && state.selectedPower == BSPower.FIRE) {
                 if (state.currentTile.hasProperty(m, light, "beacon", "off")) {
                     state.currentTile.setProperty(light, "beacon", "on");
                     if(ButterSeal.DEBUG > 2) {
                         System.out.println("Lighting beacon.");
                     }
                     state.currentTile.getCell(light).setTile(beacon_on);
                 }
                 state.isUsingPower = false;
             }
             if (state.currentTile.hasProperty(m, m.playerLevel, "objective", "true")) {
                 if (state.isUsingPower && state.selectedPower == BSPower.ACTION) {
                     if(!m.objectiveReached) {
                         m.objectiveReached = true;
                         state.setMusic(BSAsset.SECOND_MUSIC);
                         state.world.addRoute(BSMap.ICE_CAVE, BSMap.ICE_CAVE_EXIT, null);
 
                         if(!state.available_powers.contains(BSPower.LIGHT)) {
                             state.available_powers.add(BSPower.LIGHT);
                         }
                     }
                 }
             }
         }
         @Override
         public void reset(BSGameState state) { }
         @Override
         public void load(BSGameState state) {
             if(!state.available_powers.contains(BSPower.FIRE)) {
                 state.available_powers.add(BSPower.FIRE);
             }
             BSMap m = state.nextMap;
             TiledMapTileLayer dark = m.getLayer("dark");
             TiledMapTileLayer light = m.getLayer("light");
             TiledMapTile invis = BSTile.getTileForProperty(m, "invisible", "true");
 
             for(int row = 0; row < m.playerLevel.getHeight(); row++) {
                 for(int col = 0; col < m.playerLevel.getWidth(); col++) {
                     BSTile curr = new BSTile(row, col);
 
                     if(curr.hasProperty(m, light, "light", "torch")) {
                         if(ButterSeal.DEBUG > 3) {
                             System.out.println("found torch");
                         }
                         for (int i = -1; i <= 1; i++) {
                             for (int j = -1; j <= 1; j++) {
                                 Cell point = new BSTile(row + i, col + j).getCell(dark);
                                 if(!point.getTile().equals(invis)) {
                                     if(ButterSeal.DEBUG > 2) {
                                         System.out.printf("Clearing tile %d,%d%n", row + i, col + j);
                                     }
                                     point.setTile(invis);
                                 }
                             }
                         }
                     }
                 }
             }
         }
     }),
     ICE_CAVE_EXIT(BSAsset.ICE_CAVE_EXIT, "ice-cave-exit", new BSGameStateActor() {
         @Override
         public void act(BSGameState state) { }
         @Override
         public void update(BSGameState state) { }
         @Override
         public void reset(BSGameState state) { }
         @Override
         public void load(BSGameState state) { }
     }),
     HOUSE(BSAsset.HOUSE, "house", new BSGameStateActor() {
         @Override
         public void act(BSGameState state) { }
         @Override
         public void update(BSGameState state) { }
         @Override
         public void reset(BSGameState state) { }
         @Override
         public void load(BSGameState state) { }
     }),
     MAZE (BSAsset.MAZE, "maze", new BSGameStateActor() {
         @Override
         public void act(BSGameState state) { }
         @Override
         public void update(BSGameState state) { }
         @Override
         public void reset(BSGameState state) { }
         @Override
         public void load(BSGameState state) { }
     });
 
     static final float PIXELS_PER_TILE = 64;
 
     TiledMap map;
     OrthogonalTiledMapRenderer renderer;
     BSAsset asset;
     String key;
     BSGameStateActor update;
     TiledMapTileLayer playerLevel;
     boolean objectiveReached;
 
     BSMap(BSAsset asset, String key, BSGameStateActor update) {
         this.map = new TmxMapLoader().load(asset.assetPath);
         this.renderer = new OrthogonalTiledMapRenderer(this.map, 1f/BSMap.PIXELS_PER_TILE);
         this.asset = asset;
         this.key = key;
         this.update = update;
         this.playerLevel = this.getLayer("player");
         this.SetNullsToInvisible();
     }
 
     void draw(OrthographicCamera camera) {
         this.renderer.setView(camera);
         this.renderer.render();
     }
 
     BSTile getPlayer(String key) {
         BSTile ret = null;
         int h = playerLevel.getHeight();
         int w = playerLevel.getWidth();
         for(int row = 0; row < h; row++) {
             for(int col = 0; col < w; col++) {
                 ret = new BSTile(row, col);
                 Map<String, String> prop = ret.getProperties(this).get("player");
                 if (prop != null && prop.containsKey("player") && prop.get("player").equals(key)) {
                     return ret;
                 }
             }
         }
         return ret;
     }
 
     public static BSMap getByKey(String key) {
         for (BSMap m : BSMap.values()) {
             if (m.key.equals(key)) {
                 return m;
             }
         }
         return null;
     }
 
     public void usePower(BSGameState state) {
         update.update(state);
         update.act(state);
     }
 
     public TiledMapTileLayer getLayer(String name) {
         return (TiledMapTileLayer) this.map.getLayers().get(name);
     }
 
     public void reset(BSGameState state) {
         this.update.reset(state);
     }
 
     public void load(BSGameState state) {
         this.update.load(state);
     }
 
     private void SetNullsToInvisible() {
         TiledMapTile invis = BSTile.getTileForProperty(this, "invisible", "true");
         Cell c = new Cell();
         c.setTile(invis);
 
         for(MapLayer layer : this.map.getLayers()) {
             TiledMapTileLayer l = (TiledMapTileLayer) layer;
             int h = l.getHeight();
             int w = l.getWidth();
             for(int row = 0; row < w; row++) {
                 for(int col = 0; col < h; col++) {
                     if(l.getCell(row, col) == null) {
                         l.setCell(row, col, c);
                     }
                 }
             }
         }
     }
 }
 
 // Local Variables:
 // indent-tabs-mode: nil
 // End:
