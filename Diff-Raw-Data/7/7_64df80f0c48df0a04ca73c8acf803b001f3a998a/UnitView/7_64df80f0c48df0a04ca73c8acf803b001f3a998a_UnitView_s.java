 package com.ahsgaming.valleyofbones.units;
 
 import com.ahsgaming.valleyofbones.VOBGame;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.utils.Array;
 
 /**
  * valley-of-bones
  * (c) 2013 Jami Couch
  * User: jami
  * Date: 1/16/14
  * Time: 8:21 AM
  */
 public class UnitView {
     ProgressBar healthBar;
     Sprite image;
     Sprite overlay;
     Array<Sprite> buildImages;
     int buildImage;
     Array<Sprite> buildOverlayImages;
     int buildOverlayImage;
     float buildAnimTimer = 0.3f;
     Vector2 boardPosition = new Vector2();
     Vector2 lastBoardPosition;
     Array<Vector2> path = new Array<Vector2>();
     Vector2 position = new Vector2();
     Vector2 size = new Vector2();
     Array<Action> actions = new Array<Action>();
     Color color = new Color(1, 1, 1, 1);
     AbstractUnit unit;
 
     public static UnitView createUnitView(AbstractUnit unit) {
         UnitView view = new UnitView();
 
         view.unit = unit;
         view.image = VOBGame.instance.getTextureManager().getSpriteFromAtlas("assets", unit.data.image);
         view.overlay = VOBGame.instance.getTextureManager().getSpriteFromAtlas("assets", unit.data.image + "-overlay");
 
         view.buildImages = VOBGame.instance.getTextureManager().getSpritesFromAtlas("assets", (unit.getOwner() != null ? unit.getOwner().getRace() : "terran") + "-builder");
         view.buildOverlayImages = VOBGame.instance.getTextureManager().getSpritesFromAtlas("assets", (unit.getOwner() != null ? unit.getOwner().getRace() : "terran") + "-builder-overlay");
 
         view.buildImage = 0;
         view.buildOverlayImage = 0;
 
         view.setSize(view.image.getRegionWidth(), view.image.getRegionHeight());
 
         view.healthBar = new ProgressBar();
         view.healthBar.setSize(view.getWidth(), 4f * VOBGame.SCALE);
 
         return view;
     }
 
     public void attackAnim() {
         addAction(Actions.sequence(
                 UnitView.Actions.colorTo(new Color(1, 1, 0.5f, 1), 0.1f),
                 UnitView.Actions.delay(0.2f),
                 UnitView.Actions.colorTo(new Color(1, 1, 1, 1), 0.1f)
         ));
     }
 
     public void damagedAnim() {
         addAction(UnitView.Actions.sequence(
                 UnitView.Actions.colorTo(new Color(1.0f, 0.5f, 0.5f, 1.0f), 0.1f),
                 UnitView.Actions.colorTo(new Color(1.0f, 1.0f, 1.0f, 1.0f), 0.1f),
                 UnitView.Actions.colorTo(new Color(1.0f, 0.5f, 0.5f, 1.0f), 0.1f),
                 UnitView.Actions.colorTo(new Color(1.0f, 1.0f, 1.0f, 1.0f), 0.1f)
         ));
     }
 
     public Sprite getImage() {
         return image;
     }
 
     public Color getColor() {
         return new Color(color);
     }
 
     public void setColor(Color c) {
         color.set(c);
     }
 
     public Vector2 getPosition() {
         return new Vector2(getX(), getY());
     }
 
     public void setPosition(float x, float y) {
         position.set(x, y);
     }
 
     public void setPosition(Vector2 position) {
         this.position.set(position);
     }
 
     public float getX() {
         return position.x;
     }
 
     public void setX(float x) {
         position.set(x, position.y);
     }
 
     public float getY() {
         return position.y;
     }
 
     public void setY(float y) {
         position.set(position.x, y);
     }
 
     public Vector2 getSize() {
         return new Vector2(size);
     }
 
     public void setSize(float width, float height) {
         size.set(width, height);
     }
 
     public void setBounds(float x, float y, float width, float height) {
         setPosition(x, y);
         setSize(width, height);
     }
 
     public float getWidth() {
         return size.x;
     }
 
     public void setWidth(float width) {
         size.set(width, size.y);
     }
 
     public float getHeight() {
         return size.y;
     }
 
     public void setHeight(float height) {
         size.set(size.x, height);
     }
 
     public void setBoardPosition(int x, int y) {
         boardPosition.set(x, y);
     }
 
     public void setBoardPosition(float x, float y) {
         setBoardPosition((int)x, (int)y);
     }
 
     public void setBoardPosition(Vector2 boardPos) {
         setBoardPosition((int)boardPos.x, (int)boardPos.y);
     }
 
     public Vector2 getBoardPosition() {
         return boardPosition;
     }
 
     public void setLastBoardPosition(Vector2 boardPos) {
         if (lastBoardPosition == null) lastBoardPosition = new Vector2(boardPos);
         else lastBoardPosition.set(boardPos);
     }
 
     public Vector2 getLastBoardPosition() {
         return lastBoardPosition;
     }
 
     public Array<Vector2> getPath() {
         return path;
     }
 
     public void clearPath() {
         path.clear();
     }
 
     public void addToPath(Vector2 position) {
        path.add(position);
     }
 
     public void addAction(Action action) {
         actions.add(action);
     }
 
     public void removeAction(Action action) {
         actions.removeValue(action, true);
     }
 
     public boolean hasActions() {
         return actions.size > 0;
     }
 
        public Action getCurAction() {
         return (actions.size > 0 ? actions.first() : null);
     }
 
     public void act(float delta) {
         if (actions.size > 0) {
             actions.first().update(this, delta);
             if (actions.first().isDone())
                 actions.removeIndex(0);
         }
 
         buildAnimTimer -= delta;
         if (buildAnimTimer <= 0) {
             buildAnimTimer += 0.3f;
             buildImage = (buildImage + 1) % buildImages.size;
             buildOverlayImage = (buildOverlayImage + 1) % buildOverlayImages.size;
         }
     }
 
     public void draw(SpriteBatch batch, float offsetX, float offsetY, float parentAlpha, boolean isTurn) {
 
         batch.setColor(color.r, color.g, color.b, color.a * parentAlpha * (unit.getData().isInvisible() ? 0.5f : 1));
 
         batch.draw(unit.data.building ? buildImages.get(buildImage) : image, offsetX + getX(), offsetY + getY(), getWidth() * 0.5f, getHeight() * 0.5f, getWidth(), getHeight(), 1, 1, 0);
 
         if (overlay != null) {
 
             if (unit.owner != null)
                 batch.setColor(color.r * unit.owner.getPlayerColor().r, color.g * unit.owner.getPlayerColor().g, color.b * unit.owner.getPlayerColor().b, color.a * parentAlpha * unit.owner.getPlayerColor().a * (unit.getData().isInvisible() ? 0.5f : 1));
             else
                 batch.setColor(color);
 
             batch.draw(unit.data.building? buildOverlayImages.get(buildOverlayImage) : overlay, offsetX + getX(), offsetY + getY(), getWidth() * 0.5f, getHeight() * 0.5f, getWidth(), getHeight(), 1, 1, 0);
 
         }
 
         if (!hasActions() || !(getCurAction() instanceof MoveAction)) {
 
             if (healthBar != null) {
                 batch.setColor(getColor());
                 healthBar.setCurrent((float)unit.data.getCurHP() / unit.data.getMaxHP());
                 healthBar.draw(batch, offsetX + getX(), offsetY + getY() + 12 * VOBGame.SCALE, parentAlpha);
             }
             //
             if (isTurn) {
                 int x = 0;
                 batch.setColor(getColor());
                 if (unit.data.getMovesLeft() > 0) {
                     TextureRegion tex = VOBGame.instance.getTextureManager().getSpriteFromAtlas("assets", "walking-boot");
 
                     batch.draw(tex, offsetX + getX() + x, offsetY + getY() + healthBar.getHeight() + 12 * VOBGame.SCALE, 0, 0,  tex.getRegionWidth(), tex.getRegionHeight(), 0.5f, 0.5f, 0);
                     x += tex.getRegionWidth() * 0.5f;
                 }
 
                 if (unit.data.getAttacksLeft() > 0) {
                     TextureRegion tex = VOBGame.instance.getTextureManager().getSpriteFromAtlas("assets", (unit.data.ability.equals("mind-control") ? "mind-control" : "rune-sword"));
                     if (!unit.data.ability.equals("mind-control") || unit.data.mindControlUnit == null) {
                         batch.draw(tex, offsetX + getX() + getWidth() - tex.getRegionWidth() * 0.5f, offsetY + getY() + healthBar.getHeight() + 12 * VOBGame.SCALE, 0, 0,  tex.getRegionWidth(), tex.getRegionHeight(), 0.5f, 0.5f, 0);
                         x += tex.getRegionWidth() * 0.5f;
                     }
                 }
             }
         }
     }
 
     public static class Actions {
         public static Action delay(float delay) {
             DelayAction da = new DelayAction();
             da.duration = delay;
             return da;
         }
 
         public static Action moveTo(float x, float y, float duration) {
             MoveAction ma = new MoveAction();
             ma.end = new Vector2(x, y);
             ma.duration = duration;
             return ma;
         }
 
         public static Action colorTo(Color c, float duration) {
             ColorAction ca = new ColorAction();
             ca.end = new Color(c);
             ca.duration = duration;
             return ca;
         }
 
         public static Action sequence(Action... actions) {
             SequenceAction sa = new SequenceAction();
             for (Action a: actions) {
                 sa.actions.add(a);
             }
             return sa;
         }
     }
 
     public static class Action {
         float duration = 0;
         float total = 0;
 
         public void update(UnitView obj, float delta) {
 
         }
 
         public boolean isDone() {
             return (duration <= 0 || total >= duration);
         }
     }
 
     public static class DelayAction extends Action {
         @Override
         public void update(UnitView obj, float delta) {
             total += delta;
         }
     }
 
     public static class MoveAction extends Action {
         Vector2 start, end;
 
         @Override
         public void update(UnitView obj, float delta) {
             if (duration == 0) return;
 
             if (start == null) {
                 start = new Vector2(obj.getPosition());
             }
 
             if (end == null) {
                 end = new Vector2(start);
             }
 
             total += delta;
 
             if (total >= duration) {
                 obj.setPosition(end.x, end.y);
             } else {
                 obj.setPosition(
                         start.x + (end.x - start.x) * total / duration,
                         start.y + (end.y - start.y) * total / duration
                 );
             }
         }
     }
 
     public static class ColorAction extends Action {
         Color start, end;
 
         @Override
         public void update(UnitView obj, float delta) {
             if (duration == 0) return;
 
             if (start == null) {
                 start = new Color(obj.getColor());
             }
 
             if (end == null) {
                 end = new Color(start);
             }
 
             total += delta;
 
             if (total >= duration) {
                 obj.setColor(end);
             } else {
                 float pct = total / duration;
                 obj.setColor(new Color(
                         start.r + (end.r - start.r) * pct,
                         start.g + (end.g - start.g) * pct,
                         start.b + (end.b - start.b) * pct,
                         start.a + (end.a - start.a) * pct
                 ));
             }
         }
     }
 
     public static class SequenceAction extends Action {
         Array<Action> actions = new Array<Action>();
 
         @Override
         public void update(UnitView obj, float delta) {
             if (actions.size == 0) return;
 
             actions.get(0).update(obj, delta);
             if (actions.get(0).isDone()) actions.removeIndex(0);
         }
 
         @Override
         public boolean isDone() {
             return actions.size == 0;
         }
     }
 }
