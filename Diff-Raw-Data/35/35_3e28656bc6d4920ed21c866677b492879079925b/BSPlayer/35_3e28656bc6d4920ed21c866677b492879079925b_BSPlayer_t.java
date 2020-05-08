 package edu.smcm.gamedev.butterseal;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.assets.AssetManager;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Animation;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 
 /**
  * Handles player state and movement on-screen.
  * 
  * @author Sean
  *
  */
 public class BSPlayer {
     private static final int FRAME_ROWS = 2;
     private static final int FRAME_COLS = 2;
 
     BSGameState state;
     static SpriteBatch batch;
     static AssetManager assets;
 
     private static class BSAnimation {
         Animation animation;
         Texture spritesheet;
         TextureRegion[] frames;
         float time;
 
         public BSAnimation(BSAsset asset) {
             this.spritesheet = assets.get(asset.assetPath);
             this.setAnimations();
         }
 
         public void setAnimations() {
             TextureRegion[][] tmp =
                 TextureRegion.split(this.spritesheet,
                                     this.spritesheet.getWidth() / FRAME_COLS,
                                     this.spritesheet.getHeight() / FRAME_ROWS);
             this.frames = new TextureRegion[FRAME_COLS * FRAME_ROWS];
             int index = 0;
             for (int i = 0; i < FRAME_ROWS; i++) {
                 for (int j = 0; j < FRAME_COLS; j++) {
                     this.frames[index] = tmp[i][j];
                     index += 1;
                 }
             }
             this.animation = new Animation(0.25f, frames);
             this.time = 0f;
         }
     }
 
     BSAnimation walkUp, walkDown, walkRight, walkLeft, idle;
     TextureRegion currentFrame;
 
     public BSPlayer(float x, float y,
                     BSGameState state) {
         walkUp    = new BSAnimation(BSAsset.PLAYER_WALK_UP);
         walkDown  = new BSAnimation(BSAsset.PLAYER_WALK_DOWN);
         walkRight = new BSAnimation(BSAsset.PLAYER_WALK_RIGHT);
         walkLeft  = new BSAnimation(BSAsset.PLAYER_WALK_LEFT);
         idle      = new BSAnimation(BSAsset.PLAYER_IDLE_STATE);
 
         this.x = x - 16;
         this.y = y - 16;
         this.state = state;
         this.state.facing = BSDirection.NORTH;
         this.state.selectedPower = BSPower.ACTION;
        
        System.out.println(this);
     }
 
     float x, y;
 
     BSTile currentTile;
 
     /**
      * Draws the player on the screen.
      */
     public void draw() {
         if(!state.isMoving) {
             move(BSDirection.IDLE);
         }
        batch.draw(this.currentFrame, x, y, 128, 128);
     }
 
     /**
      * Moves in the given direction.
      * 
      * If this is not the facing direction,
      *   then the player will turn into that direction, updating
      *   the {@link #state} and {@link #facingTile} appropriately.
      * Otherwise, it's a very simple move.
      * 
      * @param direction the direction in which to move
      */
     public void move(BSDirection direction) {
        if(direction != state.facing) {
             System.out.println("Moving " + direction);
        }
         BSAnimation target;
         switch(direction) {
         case NORTH:
             target = walkUp;
            y += BSMap.PIXELS_PER_TILE;
             break;
         case SOUTH:
             target = walkDown;
            y -= BSMap.PIXELS_PER_TILE;
             break;
         case EAST:
             target = walkRight;
            x += BSMap.PIXELS_PER_TILE;
             break;
         case WEST:
             target = walkLeft;
            x -= BSMap.PIXELS_PER_TILE;
             break;
         case IDLE:
         default:
             target = idle;
             break;
         }
         target.time += Gdx.graphics.getDeltaTime();
         this.currentFrame = target.animation.getKeyFrame(target.time, true);
         this.state.facing = direction;
     }
 
     private boolean canMove(BSDirection direction) {
         // If we are already moving,
         //   we should not be able to move again until we finish.
         if(state.isMoving) {
             return false;
         }
         
         state.currentMap.getTileProperties(this);
         return true;
     }
 
     /**
      * 
      * @return a tile describing the one we're facing
      */
     public BSTile getFacingTile() {
         return this.getAdjacentTile(state.facing);
     }
 
     public BSTile getAdjacentTile(BSDirection direction) {
         BSTile adj = new BSTile(state.currentTile);
         switch(direction) {
         case NORTH:
             adj.y += 1;
             break;
         case SOUTH:
             adj.y -= 1;
             break;
         case EAST:
             adj.x -= 1;
             break;
         case WEST:
             adj.x += 1;
             break;
         default:
             break;
         }
         return adj;
     }
 
     public void translate(float x, float y) {
         this.x += x;
         this.y += y;
     }
 
     public void setPower(int i) {
         // TODO May be error-prone
         int l = BSPower.values().length;
         int o = this.state.selectedPower.ordinal();
         int current = o + l;
         int next = (current + i) % l;
         this.setPower(BSPower.values()[next]);
     }
 
     public void setPower(BSPower power) {
         if(this.state.selectedPower != power) {
             this.state.isSelectingPower = true;
             System.out.println("Setting power to " + power);
             this.state.selectedPower = power;
         }
     }
 
     public void usePower() {
         // TODO Auto-generated method stub
         if(!state.isUsingPower) {
             System.out.println("Using power " + this.state.selectedPower);
         }
         this.state.isSelectingPower = false;
         this.state.isUsingPower = false;
     }
 }
 
 // Local Variables:
 // indent-tabs-mode: nil
 // End:
