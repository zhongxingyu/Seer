 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mountainrangepvp.physics;
 
 import com.badlogic.gdx.math.Vector2;
 import mountainrangepvp.generator.HeightMap;
 import mountainrangepvp.player.Player;
 import mountainrangepvp.player.PlayerManager;
 
 /**
  *
  * @author lachlan
  */
 public class PhysicsSystem {
 
     private static final float GRAVITY = -1000f;
     private static final float DAMPING = 0.01f;
     //
     private final HeightMap heightMap;
     private final PlayerManager playerManager;
 
     public PhysicsSystem(HeightMap heightMap, PlayerManager playerManager) {
         this.heightMap = heightMap;
         this.playerManager = playerManager;
     }
 
     public void update(float dt) {
         for (Player player : playerManager.getPlayers()) {
             updatePlayer(player, dt);
         }
     }
 
     private void updatePlayer(Player player, float dt) {
         Vector2 pos = player.getPosition();
         Vector2 vel = player.getVelocity();
 
         dampenVelocity(vel, dt);
 
         checkWalkUpSlope(vel, pos, dt);
         slideDownSlope(player, pos, vel, dt);
 
         vel.y += GRAVITY * dt;
 
         pos.x += vel.x * dt;
         pos.y += vel.y * dt;
 
         checkGroundIntersection(player, pos, vel);
     }
 
     private void checkWalkUpSlope(Vector2 vel, Vector2 pos, float dt) {
         int base, length;
         if (vel.x < 0) {
             base = (int) (pos.x + vel.x * dt);
             length = (int) -Math.ceil(vel.x * dt) + 1;
 
             int[] block = heightMap.getBlock(base, length);
             for (int i = block.length - 1; i >= 0; i--) {
                 int slope = block[i] - (int) pos.y;
 
                 if (slope > Player.MAX_WALK_SLOPE) {
                     vel.x = 0;
                     break;
                 }
             }
         } else {
             base = (int) pos.x + Player.WIDTH;
             length = (int) Math.ceil(vel.x * dt) + 1;
 
             int[] block = heightMap.getBlock(base, length);
             for (int i = 0; i < block.length; i++) {
                 int slope = block[i] - (int) pos.y;
 
                 if (slope > Player.MAX_WALK_SLOPE) {
                     vel.x = 0;
                     break;
                 }
             }
         }
     }
 
     private void slideDownSlope(Player player, Vector2 pos, Vector2 vel,
             float dt) {
         if (player.isOnGround()) {
             int[] block = heightMap.getBlock((int) pos.x - 1, Player.WIDTH + 3);
             int maxIndex = -1, maxHeight = -1;
 
             for (int i = 1; i < block.length; i++) {
                 int height = block[i];
 
                 if (maxHeight < height) {
                     maxIndex = i;
                     maxHeight = height;
                 }
             }
 
             if (maxIndex == 1) {
                 // Its our left corner
 
                 int slope = (int) pos.y - block[2];
                 if (slope > Player.MIN_SLIDE_SLOPE) {
                     // Slide right
                     vel.x += 500 * dt;
                     vel.y -= 50 * dt;
                 }
            } else if (maxIndex == Player.WIDTH + 1) {
                 // Its the right corner
 
                int slope = (int) pos.y - block[Player.WIDTH];
                 if (slope > Player.MIN_SLIDE_SLOPE) {
                     // Slide left
                     vel.x -= 500 * dt;
                     vel.y -= 50 * dt;
                 }
             }
         }
     }
 
     private void checkGroundIntersection(Player player, Vector2 pos, Vector2 vel) {
         player.setOnGround(false);
 
         int[] block = heightMap.getBlock((int) pos.x, Player.WIDTH);
         for (int i = 0; i < block.length; i++) {
             if (pos.y < block[i]) {
                 pos.y = block[i];
                 vel.y = 0;
 
                 player.setOnGround(true);
             }
         }
     }
 
     private void dampenVelocity(Vector2 vel, float dt) {
         vel.x -= vel.x * DAMPING * dt;
         vel.y -= vel.y * DAMPING * dt;
 
         if (vel.x < 1 && vel.x > -1) {
             vel.x = 0;
         }
     }
 }
