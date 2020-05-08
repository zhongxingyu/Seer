 package sps.text;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import sps.bridge.DrawDepths;
 import sps.bridge.Sps;
 import sps.core.Point2;
 import sps.graphics.Renderer;
 
 public class Text {
     public static final float NotTimed = Float.MIN_VALUE;
 
     private Point2 position = new Point2(0, 0);
     private String message;
     private float scale;
     private boolean visible = false;
     private float lifeInSeconds;
     private TextEffect effect;
     private float xVel;
     private float yVel;
     private float dX;
     private float dY;
     private Color _color;
 
     public Text() {
         _color = Color.WHITE;
     }
 
     public void reset(Point2 position, String message, float scale, float lifeInSeconds, TextEffect effect) {
         if (position.equals(Point2.Zero)) {
             visible = false;
             return;
         }
         this.position.copy(position);
         this.message = message;
         this.scale = scale;
         visible = true;
         this.lifeInSeconds = lifeInSeconds;
         this.effect = effect;
         effect.init(this);
     }
 
     public void hide() {
         visible = false;
     }
 
     public void update() {
         if (lifeInSeconds != NotTimed && (position.X != 0 || position.Y != 0)) {
             effect.update(this);
             lifeInSeconds -= Gdx.graphics.getDeltaTime();
             if (lifeInSeconds <= 0) {
                 visible = false;
             }
         }
     }
 
     public void draw() {
         Renderer.get().draw(message, position, _color, scale, DrawDepths.get(Sps.DrawDepths.Default_Text));
     }
 
     public boolean isVisible() {
         return visible;
     }
 
     public void setVel(float x, float y) {
         xVel = x;
         yVel = y;
     }
 
     public void accel() {
         xVel += dX;
         yVel += dY;
         position.reset(position.PosX + xVel, position.PosY + yVel, false);
     }
 
     public void setAccel(float dX, float dY) {
         this.dX = dX;
         this.dY = dY;
     }
 
     public void setColor(Color color) {
         _color = color;
     }
 
     public void setScale(float scale) {
         this.scale = scale;
     }
 
     public String getMessage() {
         return message;
     }
 
     public void setMessage(String message) {
         this.message = message;
     }
 }
