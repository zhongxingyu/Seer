 package com.ahsgaming.valleyofbones.map;
 
 import com.ahsgaming.valleyofbones.Player;
 import com.ahsgaming.valleyofbones.VOBGame;
 import com.ahsgaming.valleyofbones.units.Unit;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.utils.Array;
 
 /**
  * valley-of-bones
  * (c) 2013 Jami Couch
  * User: jami
  * Date: 1/18/14
  * Time: 2:49 PM
  */
 public class MapView {
 
     public static final int NORMAL = 0;
     public static final int HIGHLIGHT = 1;
     public static final int DIMMED = 2;
     public static final int FOG = 3;
 
     public static final Color[] HEX_COLOR = new Color[] {
             new Color(0.8f, 0.8f, 0.8f, 1),
             new Color(1, 1, 1, 1),
             new Color(0.6f, 0.6f, 0.6f, 1),
             new Color(0.4f, 0.4f, 0.4f, 1)
     };
 
     Array<Sprite> tileSprites, tileDepthSprites;
     int[] hexStatus;
     boolean[] hexHighlight;
     boolean[] hexDimmed;
     Player player;
     MapData mapData;
 
     long modified;
     boolean mapDirty = true;
 
     public boolean isMapDirty() {
         return mapDirty;
     }
 
     public void setMapDirty(boolean mapDirty) {
         this.mapDirty = mapDirty;
     }
 
     public void draw(SpriteBatch batch, float x, float y, float alpha, Array<Unit> units, HexMap map) {
         Color save = batch.getColor();
         TextureRegion depth = VOBGame.instance.getTextureManager().getSpriteFromAtlas("assets", "metal-hex-depth");
 
         batch.begin();
         // draw un-fogged hexes
         float curX = x;
         float curY = y + mapData.tileSize.y * 0.75f * (mapData.bounds.y - 1);
         int prev = -1;
         boolean odd = false;
 
         for (int j = (int)mapData.bounds.y - 1; j >= 0; j--) {
             curX = x + (odd ? mapData.tileSize.x * 0.5f : 0);
             odd = !odd;
             for (int i = 0; i < mapData.bounds.x; i++) {
                 boolean drewTile = false;
                 if (hexStatus[i + j * (int)mapData.bounds.x] != FOG) {
                     if (prev < 0 || prev != hexStatus[i + j * (int)mapData.bounds.x]) {
                         prev = hexStatus[i + j * (int)mapData.bounds.x];
                         batch.setColor(HEX_COLOR[prev]);
                     }
 
                     for (TileLayer l: mapData.tileLayers) {
                         int gid = l.data.get(i + j * (int)mapData.bounds.x);
                         if (gid != 0) {
                             batch.draw(tileSprites.get(gid - 1), curX, curY);
                             drewTile = true;
                             depth = tileDepthSprites.get(gid - 1);
                         }
                     }
 
                 } else {
                     for (TileLayer l: mapData.tileLayers) {
                         if (l.data.get(i + j * (int)mapData.bounds.x) != 0) {
                             drewTile = true;
                             depth = tileDepthSprites.get(l.data.get(i + j * (int)mapData.bounds.x) - 1);
                         }
                     }
                 }
 
                 if (drewTile) {
                     if (prev < 0 || prev != hexStatus[i + j * (int)mapData.bounds.x]) {
                         prev = hexStatus[i + j * (int)mapData.bounds.x];
                         batch.setColor(HEX_COLOR[prev]);
                     }
                     batch.draw(depth, curX, curY + mapData.tileSize.y * 0.25f - depth.getRegionHeight() + 1 * VOBGame.SCALE);
                 }
                 curX += mapData.tileSize.x;
             }
             curY -= mapData.tileSize.y * 0.75;
         }
 
         batch.end();
 
         ShapeRenderer sr = new ShapeRenderer();
         sr.setProjectionMatrix(batch.getProjectionMatrix());
         sr.begin(ShapeRenderer.ShapeType.Filled);
         for (Unit unit: units) {
             if (unit.getData().getMoveSpeed() > 0 && unit.getOwner() != null) {
                 sr.setColor(unit.getOwner().getPlayerColor());
                 if (unit.getView().getLastBoardPosition() != null) {
                     if (unit.getOwner() == player ||
                             (hexStatus[(int)(unit.getView().getBoardPosition().x + unit.getView().getBoardPosition().y * mapData.bounds.x)] != FOG
                                     || (unit.getView().getLastBoardPosition() != null && hexStatus[(int)(unit.getView().getLastBoardPosition().x + unit.getView().getLastBoardPosition().y * mapData.bounds.x)] != FOG && unit.getView().hasActions() && !unit.getData().isBuilding()))
                                     && (!unit.getData().isInvisible() || map.detectorCanSee(player, units, unit.getView().getBoardPosition()))) {
 
                         Vector2 lastPos = map.boardToMapCoords(unit.getView().getLastBoardPosition().x, unit.getView().getLastBoardPosition().y).add(32 * VOBGame.SCALE, 32 * VOBGame.SCALE).add(x, y);
                         Vector2 thisPos = map.boardToMapCoords(unit.getView().getBoardPosition().x, unit.getView().getBoardPosition().y).add(32 * VOBGame.SCALE, 32 * VOBGame.SCALE).add(x, y);
 
                         sr.rectLine(lastPos, thisPos, 3 * VOBGame.SCALE);
                         sr.circle(lastPos.x, lastPos.y, 5 * VOBGame.SCALE);
                         sr.circle(thisPos.x, thisPos.y, 5 * VOBGame.SCALE);
 
                     }
                 }
             }
         }
         sr.end();
 
         batch.begin();
 
         // draw enemy units
         for (Unit u: units) {
             if (player != u.getOwner()){
                 if ((hexStatus[(int)(u.getView().getBoardPosition().x + u.getView().getBoardPosition().y * mapData.bounds.x)] != FOG
                         || (u.getView().getLastBoardPosition() != null && hexStatus[(int)(u.getView().getLastBoardPosition().x + u.getView().getLastBoardPosition().y * mapData.bounds.x)] != FOG && u.getView().hasActions() && !u.getData().isBuilding()))
                         && (!u.getData().isInvisible() || map.detectorCanSee(player, units, u.getView().getBoardPosition())))
                     u.getView().draw(batch, x, y, alpha, false);
             }
         }
 
         // draw fogged hexes
         curX = x;
         curY = y + mapData.tileSize.y * 0.75f * (mapData.bounds.y - 1);
         prev = -1;
         odd = false;
 
         for (int j = (int)mapData.bounds.y - 1; j >= 0; j--) {
             curX = x + (odd ? mapData.tileSize.x * 0.5f : 0);
             odd = !odd;
             for (int i = 0; i < mapData.bounds.x; i++) {
                 if (hexStatus[i + j * (int)mapData.bounds.x] == FOG) {
                     if (prev < 0 || prev != hexStatus[i + j * (int)mapData.bounds.x]) {
                         prev = hexStatus[i + j * (int)mapData.bounds.x];
                         batch.setColor(HEX_COLOR[prev]);
                     }
 
                     for (TileLayer l: mapData.tileLayers) {
                         int gid = l.data.get(i + j * (int)mapData.bounds.x);//  getTileData(i, j);
                         if (gid != 0) {
                             batch.draw(tileSprites.get(gid - 1), curX, curY);
                         }
                     }
                 }
                 curX += mapData.tileSize.x;
             }
             curY -= mapData.tileSize.y * 0.75;
         }
 
         // draw friendly units
         for (Unit u: units) {
             if (player == u.getOwner())
                 u.getView().draw(batch, x, y, alpha, true);
         }
         batch.end();
 
         batch.setColor(save);
     }
 
     public void clearHighlight() {
         for (int i = 0; i < hexHighlight.length; i++)
             hexHighlight[i] = false;
     }
 
     public void clearDim() {
         for (int i = 0; i < hexDimmed.length; i++)
             hexDimmed[i] = false;
     }
 
     public void clearHighlightAndDim() {
         clearDim();
         clearHighlight();
     }
 
     public void dimIfHighlighted(Array<Vector2> positions) {
         clearDim();
         for (Vector2 p: positions) {
             int ind = (int)p.y * (int)mapData.bounds.x + (int)p.x;
             if (hexHighlight[ind]) {
                 hexDimmed[ind] = true;
                 hexStatus[ind] = DIMMED;
             }
         }
     }
 
     public boolean isBoardPositionVisible(Vector2 pos) {
         return isBoardPositionVisible((int)pos.x, (int)pos.y);
     }
 
     public boolean isBoardPositionVisible(int x, int y) {
        return (x >= 0 && y >= 0 && y * mapData.bounds.x + x < hexStatus.length && hexStatus[y * (int)mapData.bounds.x + x] != FOG);
     }
 
     public static MapView createMapView(MapData data, Player player) {
 
         MapView mapView = new MapView();
 
         mapView.hexStatus = new int[(int)(data.bounds.x * data.bounds.y)];
         mapView.hexHighlight = new boolean[(int)(data.bounds.x * data.bounds.y)];
         mapView.hexDimmed = new boolean[(int)(data.bounds.x * data.bounds.y)];
 
         mapView.player = player;
         mapView.mapData = data;
 
         mapView.tileSprites = new Array<Sprite>();
         mapView.tileDepthSprites = new Array<Sprite>();
         for (String tile: data.tiles) {
             mapView.tileSprites.add(VOBGame.instance.getTextureManager().getSpriteFromAtlas("assets", tile));
             mapView.tileDepthSprites.add(VOBGame.instance.getTextureManager().getSpriteFromAtlas("assets", tile + "-depth"));
         }
 
         return mapView;
     }
 }
