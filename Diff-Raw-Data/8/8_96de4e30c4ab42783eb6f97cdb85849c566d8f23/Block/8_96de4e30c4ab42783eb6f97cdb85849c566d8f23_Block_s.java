 package cz.sparko.Ants;
 
 import org.andengine.entity.modifier.AlphaModifier;
 import org.andengine.entity.modifier.SequenceEntityModifier;
 import org.andengine.entity.sprite.AnimatedSprite;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.region.ITiledTextureRegion;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 import org.andengine.ui.activity.BaseGameActivity;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 public abstract class Block extends AnimatedSprite {
     public static final int SIZE = 64;
     public static final int Z_INDEX = 10;
 
     /*
     TODO: enum ?
      */
     protected static final int UP = 0;
     protected static final int RIGHT = 1;
     protected static final int DOWN = 2;
     protected static final int LEFT = 3;
     protected static final Coordinate[] directions = {new Coordinate(0, -1),  new Coordinate(1, 0), new Coordinate(0, 1), new Coordinate(-1, 0)};
 
     protected Coordinate coordinate;
     protected ArrayList<Integer> sourceWays;
     protected ArrayList<Integer> outWays;
     protected int wayNo = 0;
 
     private boolean active = false;
     private boolean deleted = false;
 
     private static ITiledTextureRegion[] blockTextureRegions;
 
     public Block(Coordinate coordinate, float pX, float pY, ITiledTextureRegion pTiledTextureRegion, VertexBufferObjectManager pVertexBufferObjectManager) {
         super(pX, pY, pTiledTextureRegion, pVertexBufferObjectManager);
         this.coordinate = coordinate;
         this.setPossibleSourceWays();
         this.setOutWays();
         this.setZIndex(Z_INDEX);
     }
 
     public boolean isActive() {
         return this.active;
     }
 
     public void setActive() {
         this.active = true;
     }
 
     public boolean isDeleted() {
         return this.deleted;
     }
 
     public void delete() {
         this.deleted = true;
         this.active = false;
         this.registerEntityModifier(new SequenceEntityModifier(new AlphaModifier(Game.getAnt().getSpeed(), 1f, 1f), new AlphaModifier(Game.getAnt().getSpeed() / 4, 1f, 0f)));
     }
 
     public Coordinate getCoordinate() { return coordinate; }
 
     public static void loadResources(BitmapTextureAtlas mBitmapTextureAtlas, BaseGameActivity gameActivity) {
         blockTextureRegions = new ITiledTextureRegion[4];
         blockTextureRegions[0] = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mBitmapTextureAtlas, gameActivity, "corner.png", 32, 0, 1, 1);
         blockTextureRegions[1] = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mBitmapTextureAtlas, gameActivity, "cross.png", 96, 0, 1, 1);
         blockTextureRegions[2] = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mBitmapTextureAtlas, gameActivity, "line.png", 160, 0, 1, 1);
         blockTextureRegions[3] = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mBitmapTextureAtlas, gameActivity, "start.png", 224, 0, 1, 1);
     }
 
 
     public static Block createRandomBlockFactory(Coordinate coordinate, int posX, int posY, VertexBufferObjectManager vertexBufferObjectManager) throws NotDefinedBlockException{
         Random rnd = new Random();
         Block nBlock;
         switch(rnd.nextInt(3)) {
             case 0:
                 nBlock = new BlockCorner(coordinate, posX, posY, blockTextureRegions[0], vertexBufferObjectManager);
                 break;
             case 1:
                 nBlock = new BlockLine(coordinate, posX, posY, blockTextureRegions[2], vertexBufferObjectManager);
                 break;
             case 2:
                 nBlock = new BlockCross(coordinate, posX, posY, blockTextureRegions[1], vertexBufferObjectManager);
                 break;
             default:
                 throw new NotDefinedBlockException();
         }
         for (int i = 0; i < rnd.nextInt(4); i++) {
             nBlock.rotate();
         }
         return nBlock;
     }
 
     public static Block createStartBlockFactory(Coordinate coordinate, int posX, int posY, VertexBufferObjectManager vertexBufferObjectManager) {
         Block nBlock = new BlockStart(coordinate, posX, posY, blockTextureRegions[3], vertexBufferObjectManager);
         for (int i = 0; i < new Random().nextInt(4); i++) {
             nBlock.rotate();
         }
         return nBlock;
     }
 
     @Override
     public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
         if (pSceneTouchEvent.isActionDown() && !collidesWith(Game.getAnt())) {
             this.rotate();
             return true;
         }
         return false;
     }
 
     public Coordinate getOutCoordinate() {
         return directions[outWays.get(wayNo)];
     }
 
     public boolean canGetInFrom(Coordinate fromCoordinate) {
         if (deleted) return false;
 
         //walking through side walls
         int directionX = fromCoordinate.getX() - coordinate.getX();
         int directionY = fromCoordinate.getY() - coordinate.getY();
 
         if (Math.abs(directionX) > 1 || Math.abs(directionY) > 1) Game.needRefreshField(); //refresh field on walk through wall
 
         if (directionX < -1) directionX = 1;
         if (directionX > 1) directionX = -1;
         if (directionY < -1) directionY = 1;
         if (directionY > 1) directionY = -1;
 
         Coordinate fromDirection = new Coordinate(directionX, directionY);
         int from = -1;
         for (int i = 0; i < directions.length; i++) {
             if (directions[i].equals(fromDirection)) {
                 from = i;
                 break;
             }
         }
         wayNo = sourceWays.indexOf(from);
         if (wayNo > -1)
             return true;
         return false;
     }
 
     public void rotate() {
         this.setRotation(this.getRotation() + 90);
        for (int i = 0; i < sourceWays.size() && i < outWays.size(); i++) {
            sourceWays.set(i, (sourceWays.get(i) + 1)% 4);
             outWays.set(i, (outWays.get(i) + 1) % 4);
         }
     }
 
     public abstract SequenceEntityModifier getMoveHandler(Ant ant);
     public abstract void setPossibleSourceWays();
     public abstract void setOutWays();
 }
