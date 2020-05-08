 package se.samuelandersson.clicky.game;
 
 import java.text.DecimalFormat;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Buttons;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.InputListener;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.badlogic.gdx.utils.Array;
 import com.badlogic.gdx.utils.Disposable;
 
 import aurelienribon.tweenengine.BaseTween;
 import aurelienribon.tweenengine.Tween;
 import aurelienribon.tweenengine.TweenAccessor;
 import aurelienribon.tweenengine.TweenCallback;
 import aurelienribon.tweenengine.TweenManager;
 import se.samuelandersson.clicky.Clicky;
 import se.samuelandersson.clicky.manager.SoundManager.GameSound;
 import se.samuelandersson.clicky.util.MathHelper;
 
 public class ClickyGame extends InputListener implements Disposable,
         TweenAccessor<Label>
 {
     private final Clicky app;
     private final Stage stage;
     static public final int TWEEN_LABEL = 0;
 
     private TweenManager tweenManager;
 
     private boolean paused;
     private boolean started;
     private boolean gameOver;
 
     private BallManager ballManager;
 
     private int score = 0;
     private int clicks = 0;
     private int hits = 0;
     int misses = 0;
     int missedClicks = 0;
     private Rules rules;
 
     private float timer;
 
     private Array<GameListener> gameListeners;
     private ClickListener listener;
 
     public ClickyGame(Clicky app, Stage stage)
     {
         this.app = app;
         this.stage = stage;
         gameListeners = new Array<GameListener>();
         ballManager = new BallManager(this);
         tweenManager = new TweenManager();
         rules = new Rules();
         Tween.registerAccessor(Ball.class, ballManager);
         Tween.registerAccessor(Label.class, this);
     }
 
     public void initialize()
     {
         reset();
 
         getStage().addListener(this);
         listener = new ClickListener(Buttons.LEFT)
         {
             @Override
             public void clicked(InputEvent event, float x, float y)
             {
                 if (!started || paused || gameOver)
                     return;
                 clicks++;
                 if (event.getTarget() instanceof Ball) {
                     Ball b = (Ball) event.getTarget();
                     ballManager.clicked(b);
                     hits++;
                     rules.currentBallDelay -= rules.ballDelayDec;
                     app.getSoundManager().play(GameSound.SWOOP);
                     addScore(b, new Vector2(x, y));
                 } else {
                     missedClicks++;
                     misses++;
                     addClickedLabel(x, y, "Miss!", Color.RED);
                     if (misses > 10) // FIXME magic number
                         gameOver();
                 }
             }
 
             @Override
             public boolean isOver(Actor actor, float x, float y)
             {
                 if (actor instanceof Group) {
                     return true;
                 }
                 return super.isOver(actor, x, y);
             }
         };
         getStage().addCaptureListener(listener);
     }
 
     public void reset()
     {
         timer = 0;
         score = 0;
         clicks = 0;
         hits = 0;
         misses = 0;
         missedClicks = 0;
         rules.reset();
         started = false;
         gameOver = false;
         for (BaseTween<?> t : tweenManager.getObjects()) {
             if (t.getUserData() instanceof Actor) {
                 ((Actor) t.getUserData()).remove();
             }
         }
         tweenManager.killAll();
     }
 
     @Override
     public void dispose()
     {
         getStage().removeListener(this);
         gameListeners.clear();
         tweenManager.killAll();
     }
 
     public void start()
     {
         started = true;
         fireGameStarted();
     }
 
     public void update(float delta)
     {
         tweenManager.update(delta);
         if (!started) {
             return;
         }
 
         timer += delta;
         while (timer > rules.currentBallDelay) {
             ballManager.addRandomBall(rules.ballLifeTime);
             timer -= rules.currentBallDelay;
         }
     }
 
     public void render(SpriteBatch batch)
     {
         if (Clicky.DEBUGDRAW) {
             drawDebug(batch);
         }
     }
 
     private void addScore(Ball target, Vector2 mousePos)
     {
         float timeAlive = target.getTimeAlive();
         float radius = target.getWidth() / 2;
         Vector2 bPos = new Vector2(target.getX() + radius, target.getY()
                 + radius);
         float distanceToMid = bPos.dst(mousePos);
         distanceToMid = MathHelper.convert(distanceToMid, 0, radius, 0.5f, 1);
 
         float base = (100 - MathHelper.clamp(radius, 0.1f, 99.9f));
         int toAdd = (int) (base / timeAlive / distanceToMid);
         score += toAdd;
 
         addClickedLabel(target.getX() + target.getWidth() / 2, target.getY()
                 + target.getHeight(), "" + toAdd, Color.GREEN);
     }
 
     void addClickedLabel(float x, float y, String text)
     {
         addClickedLabel(x, y, text, Color.WHITE);
     }
 
     void addClickedLabel(float x, float y, String text, Color color)
     {
         final Label l = new Label(text, getApp().getSkin());
         l.setColor(color);
         l.setPosition(x - l.getWidth() / 2, y);
         getStage().addActor(l);
         TweenCallback cb = new TweenCallback()
         {
             @Override
             public void onEvent(int type, BaseTween<?> source)
             {
                 if (type == COMPLETE)
                     l.remove();
             }
         };
 
         Tween.to(l, TWEEN_LABEL, 1).target(l.getY() + 30, 0).setCallback(cb)
                 .setUserData(l).start(getTweenManager());
     }
 
     public void gameOver()
     {
         ballManager.removeAll();
         gameOver = true;
         started = false;
        if (Highscores.qualifiesForHighscore(getFinalScore()) >= 0) {
            fireNewHighscore(getFinalScore());
         }
         fireGameOver();
     }
 
     @Override
     public boolean keyUp(InputEvent e, int key)
     {
         if (key == Keys.P) {
             paused = !paused;
             return true;
         }
         return false;
     }
 
     public void drawDebug(SpriteBatch batch)
     {
         // build string
         StringBuilder builder = new StringBuilder();
 
         builder.append("FPS: " + Gdx.graphics.getFramesPerSecond() + "\n");
 
         /* Get camera position */
         DecimalFormat f = new DecimalFormat("00000");
         float camX = getStage().getCamera().position.x;
         float camY = getStage().getCamera().position.y;
         builder.append("Camera: " + f.format(camX) + ", " + f.format(camY)
                 + "\n");
 
         builder.append("Clicks: " + clicks + "\n");
         builder.append("Hits: " + hits + "\n");
         builder.append("Misses: " + misses + "\n");
         builder.append("Missed Clicks: " + missedClicks + "\n");
 
         // draw it
         batch.begin();
         app.getDebugFont().drawMultiLine(batch, builder.toString(), 5,
                 Gdx.graphics.getHeight() - 5);
         batch.end();
     }
 
     /* *** GET-/SETTERS *** */
 
     public TweenManager getTweenManager()
     {
         return tweenManager;
     }
 
     public Clicky getApp()
     {
         return app;
     }
 
     public boolean isPaused()
     {
         return paused;
     }
 
     public boolean isStarted()
     {
         return started;
     }
 
     public void setPaused(boolean pause)
     {
         paused = pause;
     }
 
     public final Stage getStage()
     {
         return stage;
     }
 
     public int getClicks()
     {
         return clicks;
     }
 
     public int getHits()
     {
         return hits;
     }
 
     public int getMisses()
     {
         return misses;
     }
 
     public int getMissedClicks()
     {
         return missedClicks;
     }
 
    public float getScore()
     {
         return score;
     }
 
    public int getFinalScore()
    {
        return (int) (score * hits / (float) clicks);
    }

     public boolean isGameOver()
     {
         return gameOver;
     }
 
     public void addListener(GameListener listener)
     {
         gameListeners.add(listener);
     }
 
     public void removeListener(GameListener listener)
     {
         gameListeners.removeValue(listener, true);
     }
 
     public void removeAllListeners()
     {
         gameListeners.clear();
     }
 
     protected void fireGameStarted()
     {
         for (GameListener l : gameListeners) {
             l.gameStarted();
         }
     }
 
     protected void fireGameOver()
     {
         for (GameListener l : gameListeners) {
             l.gameOver();
         }
     }
 
     protected void fireNewHighscore(int score)
     {
         for (GameListener l : gameListeners) {
             l.newHighscore(score);
         }
     }
 
     @Override
     public int getValues(Label target, int tweenType, float[] returnValues)
     {
         if (tweenType == TWEEN_LABEL) {
             returnValues[0] = target.getY();
             returnValues[1] = target.getColor().a;
             return 2;
         }
         return 0;
     }
 
     @Override
     public void setValues(Label target, int tweenType, float[] newValues)
     {
         if (tweenType == TWEEN_LABEL) {
             target.setY(newValues[0]);
             target.getColor().a = newValues[1];
         }
     }
 
 }
