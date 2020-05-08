 package com.game.rania.model.element;
 
 import java.util.Vector;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.game.rania.RaniaGame;
 import com.game.rania.controller.Controllers;
 import com.game.rania.controller.command.RemoveObjectCommand;
 import com.game.rania.model.Indexes;
 import com.game.rania.model.animator.Animator;
 
 public class Object
 {
   public static Shader currentShader  = null;
 
   public boolean       keysObject     = false;
   public boolean       touchObject    = false;
   public boolean       scrollObject   = false;
   public boolean       allTouchObject = false;
 
   public boolean       visible        = true;
   public float         lifeTime       = Float.MAX_VALUE;
   public Vector2       position       = new Vector2(0.0f, 0.0f);
   public FloatLink     angle          = new FloatLink(0.0f);
   public Vector2       scale          = new Vector2(1.0f, 1.0f);
   public Color         color          = new Color(1.0f, 1.0f, 1.0f, 1.0f);
   public int           zIndex         = Indexes.object;
 
   public Shader        shader         = null;
 
   public RegionID      regionID       = RegionID.NONE;
   public TextureRegion region         = null;
 
   public Object()
   {
     this(0, 0, 0, 1, 1);
   }
 
   public Object(float posX, float posY)
   {
     this(posX, posY, 0, 1, 1);
   }
 
   public Object(float posX, float posY, float rotAngle)
   {
     this(posX, posY, rotAngle, 1, 1);
   }
 
   public Object(float posX, float posY, float rotAngle, float scaleX, float scaleY)
   {
     position.set(posX, posY);
     angle.value = rotAngle;
     scale.set(scaleX, scaleY);
   }
 
   public Object(RegionID id, float posX, float posY)
   {
     this(id, posX, posY, 0, 1, 1);
   }
 
   public Object(RegionID id, float posX, float posY, float rotAngle)
   {
     this(id, posX, posY, rotAngle, 1, 1);
   }
 
   public Object(RegionID id, float posX, float posY, float rotAngle, float scaleX, float scaleY)
   {
     regionID = id;
     region = RaniaGame.mView.getTextureRegion(id);
     position.set(posX, posY);
     angle.value = rotAngle;
     scale.set(scaleX, scaleY);
   }
 
   public void reloadTexture()
   {
     region = RaniaGame.mView.getTextureRegion(regionID);
   }
 
   public float getMaxSize()
   {
     return Math.max(region.getRegionWidth() * scale.x, region.getRegionHeight() * scale.y);
   }
 
   public float getWidth()
   {
     return region.getRegionWidth() * scale.x;
   }
 
   public float getHeight()
   {
     return region.getRegionHeight() * scale.y;
   }
 
   public float getLeft()
   {
     return -region.getRegionWidth() * scale.x * 0.5f;
   }
 
   public float getRight()
   {
     return region.getRegionWidth() * scale.x * 0.5f;
   }
 
   public float getBottom()
   {
     return -region.getRegionHeight() * scale.y * 0.5f;
   }
 
   public float getTop()
   {
     return region.getRegionHeight() * scale.y * 0.5f;
   }
 
   public enum Align
   {
     LEFT,
     RIGHT,
     CENTER,
     TOP,
     BOTTOM
   }
 
   protected Vector2 offset = new Vector2(0, 0);
   protected Align   hAlign = Align.CENTER, vAlign = Align.CENTER;
 
   public void setAlign(Align horzAlign, Align vertAlign)
   {
     hAlign = horzAlign;
     vAlign = vertAlign;
   }
 
   public Align getHorzAlign()
   {
     return hAlign;
   }
 
   public Align getVertAlign()
   {
     return vAlign;
   }
 
   public Vector2 getOffset()
   {
     return offset;
   }
 
   protected void calcOffset(float width, float height)
   {
     switch (hAlign)
     {
       case LEFT:
         offset.x = 0.0f;
         break;
       case RIGHT:
         offset.x = -width;
         break;
       default:
         offset.x = -width * 0.5f;
         break;
     }
 
     switch (vAlign)
     {
       case TOP:
         offset.y = 0.0f;
         break;
       case BOTTOM:
         offset.y = -height;
         break;
       default:
         offset.y = -height * 0.5f;
         break;
     }
   }
 
   public boolean intersectObject(float x, float y)
   {
     if (region == null)
       return false;
     float width = getWidth();
     float height = getHeight();
    calcOffset(region.getRegionWidth(), region.getRegionHeight());
    Rectangle rect = new Rectangle(position.x + offset.x,
                                   position.y + offset.y,
                                    width,
                                    height);
     Vector2 point = new Vector2(x, y);
     point.sub(position);
     point.rotate(angle.value);
     point.add(position);
     return rect.contains(point.x, point.y);
   }
 
   // keyboard
   public boolean keyDown(int keycode)
   {
     return false;
   }
 
   public boolean keyUp(int keycode)
   {
     return false;
   }
 
   public boolean keyTyped(char character)
   {
     return true;
   }
 
   // touch
   public boolean touchDown(float x, float y)
   {
     return false;
   }
 
   public boolean touchDragged(float x, float y)
   {
     return false;
   }
 
   public boolean touchUp(float x, float y)
   {
     return false;
   }
 
   public boolean scrolled(int amount)
   {
     return false;
   }
 
   protected float            timeObject      = 0.0f;
   protected Vector<Animator> animators       = new Vector<Animator>();
   protected Vector<Animator> removeAnimators = new Vector<Animator>();
 
   public void addAnimator(Animator animator)
   {
     if (timeObject > animator.getEndTime() || animators.contains(animator))
       return;
 
     animators.add(animator);
   }
 
   // update and draw
   public boolean update(float deltaTime)
   {
     timeObject += deltaTime;
     if (!checkLife())
       return false;
 
     if (animators.isEmpty())
       return true;
 
     for (Animator animator : animators)
     {
       if (animator.update(timeObject))
         removeAnimators.add(animator);
     }
     animators.removeAll(removeAnimators);
     removeAnimators.clear();
     return true;
   }
 
   protected boolean checkLife()
   {
     if (timeObject > lifeTime)
     {
       removeObject();
       return false;
     }
     return true;
   }
 
   public void removeObject()
   {
     Controllers.commandController.addCommand(new RemoveObjectCommand(this));
   }
 
   public boolean setShader(SpriteBatch sprite)
   {
     if (currentShader == shader)
       return false;
     sprite.setShader(shader);
     currentShader = shader;
     return true;
   }
 
   public boolean draw(SpriteBatch sprite, ShapeRenderer shape)
   {
     if (!visible)
       return false;
     sprite.setColor(color);
     return drawRegion(sprite, region);
   }
 
   public boolean draw(SpriteBatch sprite, Vector2 position, float angle, Vector2 scale, Color color)
   {
     if (!visible)
       return false;
     sprite.setColor(color);
     return drawRegion(sprite, region, position, angle, scale);
   }
 
   protected boolean drawRegion(SpriteBatch sprite, TextureRegion textureRegion)
   {
     return drawRegion(sprite, textureRegion, position.x, position.y, angle.value, scale.x, scale.y);
   }
 
   protected boolean drawRegion(SpriteBatch sprite, TextureRegion textureRegion, Vector2 position, float angle, Vector2 scale)
   {
     return drawRegion(sprite, textureRegion, position.x, position.y, angle, scale.x, scale.y);
   }
 
   protected boolean drawRegion(SpriteBatch sprite, TextureRegion textureRegion, float x, float y, float angle, float scaleX, float scaleY)
   {
     if (textureRegion == null)
       return false;
 
     calcOffset(textureRegion.getRegionWidth(), textureRegion.getRegionHeight());
     sprite.draw(textureRegion,
                 x + offset.x,
                 y + offset.y,
                 -offset.x,
                 -offset.y,
                 textureRegion.getRegionWidth(),
                 textureRegion.getRegionHeight(),
                 scaleX,
                 scaleY,
                 angle);
 
     return true;
   }
 }
