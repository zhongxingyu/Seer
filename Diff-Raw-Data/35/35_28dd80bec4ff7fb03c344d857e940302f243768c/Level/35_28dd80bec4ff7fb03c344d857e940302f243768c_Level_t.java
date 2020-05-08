 package ua.org.dector.ludumdare.ld24;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Pixmap;
 import com.badlogic.gdx.math.Rectangle;
 
 /**
  * @author dector
  */
 public class Level {
     public static final int NOTHING = 0;
     public static final int BLOCK   = 0x000000ff;
     public static final int SPAWN   = 0x00ff00ff;
 
     int width;
     int height;
     Tile[][] map;
     
     Player player;
 
     public void load(String file) {
         Pixmap p = new Pixmap(Gdx.files.internal(file));
         
         width = p.getWidth();
         height = p.getHeight();
         map = new Tile[width][height];
 
         int t;
         for (int x = 0; x < width; x++) {
             for (int y = 0; y < height; y++) {
                 t = p.getPixel(x, height - y - 1);
 //                System.out.printf("%d:%d %s%n", x, y, Integer.toHexString(t));
                 
                 switch (t) {
                     case BLOCK:
                         map[x][y] = Tile.BLOCK; break;
                     case SPAWN: {
                         player = new Player(Renderer.BLOCK_SIZE * x, Renderer.BLOCK_SIZE * y);
                         map[x][y] = Tile.SPAWN;
                     } break;
                 }
             }
         }
     }
 
     public void update(float dt) {
         player.update(dt);
 
         tryToMovePlayer();
     }
 
     private void tryToMovePlayer() {
        Rectangle pr = new Rectangle((int)player.x / Renderer.BLOCK_SIZE, (int)Math.floor(player.y / Renderer.BLOCK_SIZE), 1, 1);
         Rectangle[] rs;
 
         boolean collided = false;
 
         player.x += player.vx;
         pr.setX((int)player.x / Renderer.BLOCK_SIZE);
         rs = checkCollisions();
         for (int i = 0; i < rs.length; i++) {
             if (pr.overlaps(rs[i])) {
                 if (player.vx < 0)
                    player.x = (rs[i].x + 1) * Renderer.BLOCK_SIZE + 0.01f;
                 else
                    player.x = (rs[i].x - 1) * Renderer.BLOCK_SIZE - 0.01f;
 
                 collided = true;
             }
         }
 
         if (collided) player.vx = 0;
         collided = false;
 
         player.y += player.vy;
         pr.setX((int)player.x / Renderer.BLOCK_SIZE);
        pr.setY((int)Math.floor(player.y / Renderer.BLOCK_SIZE));
         rs = checkCollisions();
         for (int i = 0; i < rs.length; i++) {
             if (pr.overlaps(rs[i])) {
                if (player.vy < 0)
                    player.y = (rs[i].y + 1) * Renderer.BLOCK_SIZE + 0.01f;
                 else
                    player.y = (rs[i].y - 1) * Renderer.BLOCK_SIZE - 0.01f;
 
                 collided = true;
             }
         }
 
         if (collided) player.vy = 0;
     }
 
     private Rectangle[] checkCollisions() {
         int px = (int)player.x / Renderer.BLOCK_SIZE;
         int py = (int)Math.floor(player.y / Renderer.BLOCK_SIZE);
 
         int p1x = px;
         int p1y = py;
         int p2x = px + 1;
         int p2y = py;
         int p3x = px + 1;
         int p3y = py + 1;
         int p4x = px;
         int p4y = py + 1;
 
         Tile tile1 = map[p1x][p1y];
         Tile tile2 = map[p2x][p2y];
         Tile tile3 = map[p3x][p3y];
         Tile tile4 = map[p4x][p4y];
 
         Rectangle[] r = { new Rectangle(), new Rectangle(), new Rectangle(), new Rectangle() };
 
         if (tile1 == Tile.BLOCK)
             r[0].set(p1x, p1y, 1, 1);
         else
             r[0].set(-1, -1, 0, 0);
         if (tile2 == Tile.BLOCK)
             r[1].set(p2x, p2y, 1, 1);
         else
             r[1].set(-1, -1, 0, 0);
         if (tile3 == Tile.BLOCK)
             r[2].set(p3x, p3y, 1, 1);
         else
             r[2].set(-1, -1, 0, 0);
         if (tile4 == Tile.BLOCK)
             r[3].set(p4x, p4y, 1, 1);
         else
             r[3].set(-1, -1, 0, 0);
         
         if (Debug.DEBUG) {
             Debug.tileLeft = tile4;
             Debug.tileRight = tile1;
             Debug.tileTop = tile3;
             Debug.tileBottom = tile2;
         }
         
         return r;
     }
 }
 
