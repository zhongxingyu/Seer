 package ua.org.dector.moon_lander;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.Pixmap;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 
 import com.badlogic.gdx.Input.Keys;
 
 import java.awt.*;
 
 import static ua.org.dector.moon_lander.AppConfig.*;
 
 /**
  * @author dector (dector9@gmail.com)
  */
 public class GameScreen implements Screen, InputProcessor {
     private Rocket rocket;
     private Level level;
 
     private TextureRegion rocketTexture;
     private TextureRegion fireTexture;
     private TextureRegion pointerTexture;
     
     private TextureRegion levelTexture;
 
     private boolean collided;
     private boolean landed;
 
     public GameScreen(Rocket rocket, Level level) {
         this.rocket = rocket;
         this.level = level;
 
         Texture graphicsTexture = ResourceLoader.loadTexture(GRAPHICS_FILE);
         rocketTexture = new TextureRegion(
                 graphicsTexture,
                 ROCKET_TEXTURE_WIDTH,
                 ROCKET_TEXTURE_HEIGHT
         );
         fireTexture = new TextureRegion(
                 graphicsTexture,
                 ROCKET_TEXTURE_WIDTH,       // x
                 0,                          // y
                 FIRE_TEXTURE_WIDTH,
                 FIRE_TEXTURE_HEIGHT
         );
         pointerTexture = new TextureRegion(
                 graphicsTexture,
                 ROCKET_TEXTURE_WIDTH + FIRE_TEXTURE_WIDTH,       // x
                 0,                          // y
                 POINTER_TEXTURE_WIDTH,
                 POINTER_TEXTURE_HEIGHT
         );
         
 
         buildLevelTexture();
     }
 
     private void buildLevelTexture() {
         int levelWidth = level.getWidth();
         int levelHeight = level.getHeight();
 
         Pixmap pixmap = new Pixmap(levelWidth, levelHeight, Pixmap.Format.RGBA8888);
 
 //        pixmap.setColor(Color.BLACK);
 //        pixmap.fill();
         pixmap.setColor(Color.WHITE);
 
         int i = 0;
         int[] prevPoint = new int[2];
         int[] currPoint = new int[2];
         int mapLength = level.getMapLength();
 
         prevPoint[0] = level.get(i++);
         prevPoint[1] = level.get(i++);
 
         while (i < mapLength) {
             currPoint[0] = level.get(i++);
             currPoint[1] = level.get(i++);
 
             pixmap.drawLine(
                     prevPoint[0],
                     levelHeight - prevPoint[1],
                     currPoint[0],
                     levelHeight - currPoint[1]
             );
 
             prevPoint[0] = currPoint[0];
             prevPoint[1] = currPoint[1];
         }
 
         // Draw landing platform
         pixmap.fillRectangle(
                 level.getLandingLeftX() + LANDING_PLATFORM_BORDER,
                 levelHeight -
                         (level.getLandingBottomY() + LANDING_PLATFORM_HEIGHT / 2),
                 level.getLandingRightX() - level.getLandingLeftX()
                         - 2 * LANDING_PLATFORM_BORDER,
                 LANDING_PLATFORM_HEIGHT
         );
 
         // Prepare texture
 
         Pixmap texturePixmap = new Pixmap(
                 Utils.toPowerOfTwo(pixmap.getWidth()),
                 Utils.toPowerOfTwo(pixmap.getHeight()),
                 Pixmap.Format.RGBA8888
         );
 
         texturePixmap.drawPixmap(pixmap, 0, 0);
 
         levelTexture = new TextureRegion(
                 new Texture(texturePixmap),
                 pixmap.getWidth(),
                 pixmap.getHeight()
         );
     }
 
     public void render(float delta) {
         if (Gdx.input.isKeyPressed(Keys.ESCAPE)) Gdx.app.exit();
 
         if (! collided) {
             updateCollisions();
             rocket.updateRocket(delta);
         }
 
         Graphics.clear();
 
         Graphics.begin();
         Graphics.draw(levelTexture, 0, 0);
         Graphics.draw(
                 rocketTexture,
                 rocket.getX(),
                 rocket.getY(),
                 ROCKET_WIDTH,
                 ROCKET_HEIGHT,
                 rocket.getDirectionAngle()
         );
         if (rocket.isMoveUp() && ! collided) {
             Graphics.draw(
                     fireTexture,
                     rocket.getX() + FIRE_PADDING,
                     rocket.getY() - FIRE_HEIGHT,
                     FIRE_WIDTH / 2,
                     FIRE_HEIGHT + ROCKET_HEIGHT / 2,
                     FIRE_WIDTH,
                     FIRE_HEIGHT,
                     rocket.getDirectionAngle()
             );
         }
         
         if (rocket.getX() < 0
                 || level.getWidth() < rocket.getX()
                 || level.getHeight() < rocket.getY()) {
             int pointerX = 0;
             int pointerY = 0;
             float pointerAngle = 0;
             
             if (rocket.getX() < 0) {
                 pointerX = 0;
 
                 if (level.getHeight() < rocket.getY()) {
                     pointerY = SCREEN_HEIGHT - POINTER_HEIGHT;
                    pointerAngle = (float)Math.toDegrees(Math.atan(
                            (rocket.getY() - level.getHeight()) / rocket.getX()
                    )) - 180;
                 } else {
                     pointerY = (int)rocket.getY();
                     pointerAngle = 180;
                 }
             } else if (level.getWidth() < rocket.getX()) {
                 pointerX = SCREEN_WIDTH - POINTER_WIDTH;
 
                 if (level.getHeight() < rocket.getY()) {
                     pointerY = SCREEN_HEIGHT - POINTER_HEIGHT;
                    pointerAngle = (float)Math.toDegrees(Math.atan(
                            (rocket.getY() - level.getHeight()) / rocket.getX()
                    ));
                 } else {
                     pointerY = (int)rocket.getY();
                     pointerAngle = 0;
                 }
             } else if (level.getHeight() < rocket.getY()) {
                 pointerX = (int)rocket.getX();
                 pointerY = SCREEN_HEIGHT - POINTER_HEIGHT;
                 pointerAngle = 90;
             }
 
             Graphics.draw(
                     pointerTexture,
                     pointerX,
                     pointerY,
                     POINTER_WIDTH,
                     POINTER_HEIGHT,
                     pointerAngle
             );
         }
 
         Graphics.draw(
                 10, SCREEN_HEIGHT - 10, 15,
                 String.format("X: %d", (int)rocket.getX()),
                 String.format("Y: %d", (int)rocket.getY()),
                 String.format("Vx: %.2f", rocket.getVx()),
                 String.format("Vy: %.2f", rocket.getVy()),
                 String.format("Angle: %.1f", rocket.getDirectionAngle())
         );
         Graphics.end();
     }
 
     private void updateCollisions() {
         int rocketLeftX = (int)rocket.getX();
         int rocketRightX = (int)rocket.getX() + ROCKET_WIDTH;
         int rocketBottomY = (int)rocket.getY();
 
         if (rocketRightX < 0) {
             if (rocketBottomY <= level.get(1))
                 collided = true;
         } else if (level.getWidth() < rocketLeftX) {
             if (rocketBottomY <= level.get(level.getMapLength() - 1))
                 collided = true;
         } else {
             int[] leftPoint = new int[2];
             int[] rightPoint = new int[2];
 
             int pointsCount = level.getPointsCount();
 
             int leftBound = 0;
             int rightBound = pointsCount - 1;
 
             // Find left and right point
 
             while (leftBound + 1 < pointsCount
                     && level.get(2 * (leftBound + 1)) <= rocketLeftX) {
                 leftBound++;
             }
 
             while (0 <= rightBound - 1
                     && rocketRightX <= level.get(2 * (rightBound - 1))) {
                 rightBound--;
             }
 
             for (int i = leftBound; i < rightBound && ! collided; i++) {
                 leftPoint[0] = level.get(2 * i);
                 leftPoint[1] = level.get(2 * i + 1);
 
                 rightPoint[0] = level.get(2 * (i + 1));
                 rightPoint[1] = level.get(2 * (i + 1) + 1);
 
                 // Check intersection
                 collided = new Rectangle(
                         (int)rocket.getX(),
                         (int)rocket.getY(),
                         ROCKET_WIDTH,
                         ROCKET_HEIGHT
                 ).intersectsLine(
                         leftPoint[0],
                         leftPoint[1],
                         rightPoint[0],
                         rightPoint[1]
                 );
             }
         }
         
         if (collided) {
             landed = level.getLandingLeftX() <= rocketLeftX
                     && rocketLeftX <= level.getLandingRightX()
                     && rocket.getVx() <= LANDING_VX_BOUND
                     && rocket.getVy() <= LANDING_VY_BOUND
                     && Math.abs(rocket.getDirectionAngle() - 90) <= LANDING_DIFF_ANGLE;
         }
 
         if (collided) {
             if (landed) {
                 System.out.println("Landed!");
             } else {
                 System.out.println("Crashed!");
             }
         }
     }
 
     public void resize(int width, int height) {
     }
 
     public void show() {
     }
 
     public void hide() {
     }
 
     public void pause() {
     }
 
     public void resume() {
     }
 
     public void dispose() {
     }
 
     public boolean keyDown(int keycode) {
         switch (keycode) {
             case Keys.UP:
                 rocket.moveUp(true);
                 break;
             case Keys.LEFT:
                 rocket.rotateLeft(true);
                 break;
             case Keys.RIGHT:
                 rocket.rotateRight(true);
                 break;
         }
 
         return true;
     }
 
     public boolean keyUp(int keycode) {
         switch (keycode) {
             case Keys.UP:
                 rocket.moveUp(false);
                 break;
             case Keys.LEFT:
                 rocket.rotateLeft(false);
                 break;
             case Keys.RIGHT:
                 rocket.rotateRight(false);
                 break;
         }
 
         return true;
     }
 
     public boolean keyTyped(char character) {
         return false;
     }
 
     public boolean touchDown(int x, int y, int pointer, int button) {
         return false;
     }
 
     public boolean touchUp(int x, int y, int pointer, int button) {
         return false;
     }
 
     public boolean touchDragged(int x, int y, int pointer) {
         return false;
     }
 
     public boolean touchMoved(int x, int y) {
         return false;
     }
 
     public boolean scrolled(int amount) {
         return false;
     }
 }
