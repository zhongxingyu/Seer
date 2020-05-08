 package gg;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.maps.*;
 import com.badlogic.gdx.maps.tiled.*;
 import com.badlogic.gdx.maps.objects.*;
 import com.badlogic.gdx.graphics.*;
 import com.badlogic.gdx.graphics.g2d.*;
 import com.badlogic.gdx.math.*;
 
 import java.lang.Math;
 
 public class Person extends StoryObject {
     public TiledMap onMap = null;
     public final Vector2 position = new Vector2();
     public final Vector2 move = new Vector2();
     
     // TODO: move out
    public static final int LAYER_GAMEPLAY = 4;
    public static final int LAYER_QUESTS = 5;
     
     // Yeah, this is so stupid, but it looks like java enums are unusable here
     public static final int FIRST_TID = 3701;
     public static final int TID_EMPTY = FIRST_TID+0;
     public static final int TID_UNPASS = FIRST_TID+1;
     public static final int TID_ROAD = FIRST_TID+2;
     public static final int TID_ENTER = FIRST_TID+3;
     public static final int TID_OASIS = FIRST_TID+4;
     public static final int TID_SPAWN = FIRST_TID+5;
     public static final int TID_MAPEND = FIRST_TID+6;
     
     public static final int FIRST_TID_QUEST = 5551;
     
     static final float DEFAULT_SPEED = 4;
     static final float ROAD_BOOST = 1.5f;
     
     // gfx
     Texture texture;
     Animation defaultSprite;
     Animation[] spriteRun = new Animation[8];
     Animation[] spriteStand = new Animation[8];
     int direction = 0;
     enum State {
         Stand, Run
     }
     State state = State.Stand;
     float tc;
     
     public Person () {
         texture = new Texture("data/maps/char-1.png");
         TextureRegion[][] regions = TextureRegion.split(texture, 32, 32);
         
         defaultSprite = new Animation(0, regions[0][0]);
         
         for (int i = 0; i < 8; ++i) {
             spriteStand[i] = new Animation(0, regions[i+1][0]);
             
             TextureRegion[] tmp = new TextureRegion[8];
             System.arraycopy(regions[i+1], 1, tmp, 0, 8);
             spriteRun[i] = new Animation(0.11f, tmp);
             spriteRun[i].setPlayMode(Animation.LOOP);
         }
     }
     
     public void moveTo (TiledMap map, Vector2 xy) {
         onMap = map;
         position.set(xy);
     }
     
     public void render (SpriteBatch batch) {
         final Animation[] spriteT;
         if (state == State.Run) {
             spriteT = spriteRun;
         } else {
             spriteT = spriteStand;
         }
         // fix this when all animations are done
         if (direction % 4 == 3) {
             direction = (direction+1)%8;
         }
         direction = direction/2*2;
         final Animation sprite = spriteT[direction];
         
         batch.begin();
         batch.draw(sprite.getKeyFrame(tc), position.x, position.y, 2, 2);
         batch.end();
     }
     
     public void update (float dt) {
         // fur animations
         tc += dt;
         
         if (move.x != 0 || move.y != 0) {
             int angle = ((int) move.angle()/45) * 45;
             float dx = (float) Math.cos(angle*Math.PI/180) * dt * DEFAULT_SPEED;
             float dy = (float) Math.sin(angle*Math.PI/180) * dt * DEFAULT_SPEED;
             
             direction = angle / 45;
             state = State.Run;
             
             // check ground
             TiledMapTile tile;
             tile = getTile(LAYER_GAMEPLAY);
             if (tile != null) {
                     int tid = tile.getId();
                     switch (tid) {
                         case TID_ROAD:
                             dx *= ROAD_BOOST;
                             dy *= ROAD_BOOST;
                             break;
                     }
                 } else {
                     Gdx.app.error("Person.update", "not on map or no feature");
                 }
             
             tile = getTile(LAYER_GAMEPLAY, dx, dy);
             if (tile != null) {
                 int tid = tile.getId();
                 switch (tid) {
                     case TID_UNPASS:
                     case TID_OASIS:
                     case TID_MAPEND:
                         dx = 0;
                         dy = 0;
                         break;
                 }
             }
             
             position.x += dx;
             position.y += dy;
         } else {
             state = State.Stand;
         }
     }
     
     TiledMapTile getTile (int lid) {
         return getTile(lid, 0, 0);
     }
     
     TiledMapTile getTile (int lid, float dx, float dy) {
         if (onMap != null) {
             TiledMapTileLayer layer = (TiledMapTileLayer) onMap.getLayers().get(lid);
             int cx = (int) (getCentre().x+dx);
             int cy = (int) (getCentre().y+dy);
             TiledMapTileLayer.Cell cell = layer.getCell(cx, cy);
             if (cell != null) {
                 return cell.getTile();
             }
         }
         return null;
     }
     
     Vector2 getCentre () {
         return new Vector2().set(position).add(1, 1);
     }
     
     
     public void clicked () {
         clicked(0, 0);
     }
     
     public void clicked (float dx, float dy) {
         TiledMapTile tile = getTile(LAYER_QUESTS, dx, dy);
         if (tile != null) {
             int tid = tile.getId();
             story(tid-FIRST_TID_QUEST);
         }
     }
     
     public void story (int tid) {
         Gdx.app.log("quests", "trigger story "+tid);
         Story.instance().trigger(tid);
     }
 }
