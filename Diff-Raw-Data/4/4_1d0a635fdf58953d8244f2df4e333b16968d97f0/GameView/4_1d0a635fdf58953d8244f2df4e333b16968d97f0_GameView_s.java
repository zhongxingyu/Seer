 package org.ivan.simple.game;
 
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 import org.ivan.simple.ImageProvider;
 import org.ivan.simple.PandaApplication;
 import org.ivan.simple.UserControlType;
 import org.ivan.simple.bitmaputils.ColorBackground;
 import org.ivan.simple.bitmaputils.PandaBackground;
 import org.ivan.simple.bitmaputils.TextureAtlasParser;
 import org.ivan.simple.game.hero.Hero;
 import org.ivan.simple.game.level.LevelCell;
 import org.ivan.simple.game.level.LevelModel;
 import org.ivan.simple.game.level.LevelView;
 import org.ivan.simple.game.monster.Monster;
 import org.ivan.simple.game.monster.MonsterFactory;
 import org.ivan.simple.game.motion.MotionType;
 import org.ivan.simple.game.tutorial.GuideAnimation;
 import org.xmlpull.v1.XmlPullParserException;
 
 import java.io.IOException;
 
 public class GameView extends SurfaceView {
 	
 	private static int GRID_STEP;
 	
 	private static int JUMP_SPEED;
 	private static int ANIMATION_JUMP_SPEED;
 	
 	private static int LEFT_BOUND;
 	private static int RIGHT_BOUND;
 	private static int TOP_BOUND;
 	private static int BOTTOM_BOUND;
 
 	private Hero hero;
 	private Monster monster;
 	protected LevelView level;
     GuideAnimation guideAnimation = new GuideAnimation();
 	
 	private GameControl control;
 	
 	protected Bitmap background;
 //    private PandaBackground bgr;
 
     private Paint backgroundPaint;
 
 	protected LevelCell prevCell;
 	
 	private int levId = 0;
 	
 	protected boolean finished = false;
     private boolean monsterLose = false;
     private GameActivity gameContext;
     private boolean initialized = false;
 
     public GameView(GameActivity context) {
 		super(context);
         this.gameContext = context;
 		init();
         control = new GameControl(this);
     }
 
 	public GameControl getControl() {
 		return control;
 	}
 	
 	private void init() {
         backgroundPaint = new Paint();
         backgroundPaint.setAntiAlias(true);
         getHolder().addCallback(new SurfaceHolder.Callback() {
 			
 			public void surfaceDestroyed(SurfaceHolder holder) {
 				System.out.println("surfaceDestroyed");
 				// turn motion to initial stage (stage == 0)
 				//level.model.getMotion().startMotion();
                 control.stopManager();
                 //ImageProvider.removeFromCatch(backgroundId);
 //                background.recycle();
 //                background = null;
 			}
 			
 			public void surfaceCreated(SurfaceHolder holder) {
 				System.out.println("surfaceCreated");
                 if(!initialized) {
                     initialized = true;
                     initSurface();
                 }
 				if(control.getGameLoopThread() == null) {
 					control.startManager();
 				}
 				control.getGameLoopThread().doDraw(false);
 			}
 			
 			public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
 				System.out.println("surfaceChanged");
 				control.getGameLoopThread().doDraw(false);
 			}
 		});
 
 	}
 	
 	protected void initSurface() {
 //        try {
 //            bgr = new TextureAtlasParser().createTextureAtlasBackground(gameContext, backgroundId);
 //        } catch (IOException e) {
 //            bgr = new ColorBackground();
 //        } catch (XmlPullParserException e) {
 //            bgr = new ColorBackground();
 //        }
         background = imageProvider().getBackground(getBackgroundId(levId), getWidth(), getHeight());
 
         GRID_STEP = imageProvider().getGridStep();
 //		System.out.println("GRID_STEP = " + GRID_STEP);
 
         LevelModel model =
                 new LevelModel(levId, PandaApplication.getPandaApplication().getLevelParser());
 		TOP_BOUND = (getHeight() - GRID_STEP * model.getRows()) / 2 + GRID_STEP / 2;
 		// TODO check this bound carefully!
 		LEFT_BOUND = (getWidth() - GRID_STEP * model.getCols()) / 2 + GRID_STEP / 2;
 		JUMP_SPEED = GRID_STEP;
 		ANIMATION_JUMP_SPEED = JUMP_SPEED / 8;
 
         BOTTOM_BOUND = TOP_BOUND + model.getRows() * GRID_STEP;
         RIGHT_BOUND = LEFT_BOUND + model.getCols() * GRID_STEP;
 
         level = new LevelView(model, GRID_STEP, LEFT_BOUND, TOP_BOUND);
 
 		prevCell = level.model.getHeroCell();
 
 		hero = new Hero(level.model.hero);
 		monster = MonsterFactory.createMonster(level.model.monster, GRID_STEP);
 		
 		hero.x = LEFT_BOUND + level.model.hero.getX() * GRID_STEP;
 		hero.y = TOP_BOUND + level.model.hero.getY() * GRID_STEP;
 		
 		if(level.model.monster != null) {
 			monster.xCoordinate = LEFT_BOUND + level.model.monster.getCol() * GRID_STEP;
 			monster.yCoordinate = TOP_BOUND + level.model.monster.getRow() * GRID_STEP;
 		}
 
         guideAnimation = new GuideAnimation();
 	}
 
     private ImageProvider imageProvider() {
         return PandaApplication.getPandaApplication().getImageProvider();
     }
 
     /**
 	 * Draw hero, level and etc.
 	 */
 	@Override
 	protected void onDraw(Canvas canvas) {
 		onDraw(canvas, false);
 	}
 	
 	protected void onDraw(Canvas canvas, boolean update) {
 		canvas.drawColor(0xFFB6D76E);
 //        bgr.draw(canvas);
         canvas.drawBitmap(background, 0, 0, backgroundPaint);
 		level.onDraw(canvas, update);
 		hero.onDraw(canvas, update);
 		monster.onDraw(canvas, update);
         guideAnimation.onDraw(canvas, update);
 //		level.drawGrid(canvas);
 		drawFPS(canvas);
 		drawScore(canvas);
         drawMemoryUsage(canvas);
 		if(level.model.isLost()) {
 			drawLose(canvas);
 		} else if(isReadyToPlayWinAnimation()) {
 			drawWin(canvas);
             control.playWinSound();
 		}
 	}
 
     private void drawOnCenterCoordinates(Bitmap bitmap, int x, int y, Paint paint, Canvas canvas) {
         canvas.drawBitmap(bitmap, x - bitmap.getWidth() / 2, y - bitmap.getHeight() / 2, paint);
     }
 	
 	private void drawFPS(Canvas canvas) {
 		Paint paint = new Paint(); 
 		paint.setStyle(Paint.Style.FILL); 
 		paint.setTextSize(25);
 		paint.setColor(Color.BLUE);
 		canvas.drawText("FPS: " + GameManager.getFPS(), 5, 25, paint);
 	}
 	
 	private void drawScore(Canvas canvas) {
 		Paint paint = new Paint(); 
 		paint.setStyle(Paint.Style.FILL); 
 		paint.setTextSize(25);
 		paint.setColor(Color.MAGENTA);
 		canvas.drawText("Score: " + getScore(), 100, 25, paint);
 	}
 
     private void drawMemoryUsage(Canvas canvas) {
         Paint paint = new Paint();
         paint.setStyle(Paint.Style.FILL);
         paint.setTextSize(25);
         paint.setColor(Color.BLACK);
         canvas.drawText(
                 String.format(
                         "used: %d KiB free: %d KiB",
                         Runtime.getRuntime().totalMemory() / 1024,
                         Runtime.getRuntime().freeMemory() / 1024
                 ), 300, 25, paint
         );
     }
 	
 	private void drawWin(Canvas canvas) {
         drawStamp(canvas, "COMPLETE", Color.RED);
 	}
 
     private void drawLose(Canvas canvas) {
 		drawStamp(canvas, "GAME OVER", Color.BLACK);
 	}
 
     private void drawStamp(Canvas canvas, String complete, int color) {
         Paint paint = new Paint();
         paint.setTextSize(80);
         Rect textRect = new Rect();
         paint.getTextBounds(complete, 0, complete.length(), textRect);
         canvas.rotate(-35, getWidth() / 2, getHeight() / 2);
         paint.setStyle(Paint.Style.FILL);
         paint.setColor(color);
         canvas.drawText(complete, getWidth() / 2 - textRect.exactCenterX(), getHeight() / 2 - textRect.exactCenterY(), paint);
         canvas.restore();
     }
 
 	/**
 	 * Checks if game is ready to switch hero animation and/or motion
 	 * @return
 	 */
 	protected boolean readyForUpdate(UserControlType controlType) {
		// if the level is complete or lost the game should be not updatable on this level 
		if(level.model.isLost()) return false;
 		if(isReadyToPlayWinAnimation()) return false;
 		
 		boolean inControlState = hero.isInControlState();
         boolean interruptStayCase = checkInterruptStay(controlType);
 		/*
 		 * Hero is in control state usually when motion animation has ended
 		 * If hero animation is in starting state game model should not be updated
 		 * (after starting animation main animation will be played)
 		 * If hero animation is in finishing state game model should not be updated
 		 * (after finishing animation next motion animation will begin)   
 		 */
 		boolean stateReady = inControlState || interruptStayCase;
 		// change behavior only if hero is in ready for update state AND is on grid point
 		return stateReady;
 	}
 
     private boolean checkInterruptStay(UserControlType controlType) {
         return hero.getRealMotion().getType() == MotionType.STAY &&
                 (controlType == UserControlType.LEFT ||
                 controlType == UserControlType.RIGHT ||
                 controlType == UserControlType.UP);
     }
 
     /**
 	 * Switch hero animation and motion
 	 */
 	protected void updateGame(UserControlType controlType) {
 	    if(hero.isFinishing()) {
             // try to end pre/post motion if it exists
             continueModel();
         } else {
             // get new motion type only if it was not obtained yet
             // (obtained yet means that pre- or post- motion was just ended)
 			updateModel(controlType);
             control.playSound();
 		}
         prevCell.updateCell(hero.model.currentMotion, hero.model.finishingMotion);
 	}
 	
 	/**
 	 * Use user control to obtain next motion type, move hero in model (to next level cell),
 	 * switch hero motion animation and cell platforms reaction to this animation  
 	 */
 	private void updateModel(UserControlType controlType) {
 		// Used to remember pressed control (action down performed and no other actions after)
 //		UserControlType controlType = control.getUserControl();
         // Store cell before update in purpose to play cell animation (like floor movement while jump)
 		prevCell = level.model.getHeroCell();
 		// calculate new motion depending on current motion, hero cell and user control
 		level.model.updateGame(controlType);
 		// switch hero animation
 		hero.finishPrevMotion(prevCell);
 		// play cell reaction to new motion
 		if(!hero.isFinishing()) {
 			hero.switchToCurrentMotion();
 		}
 	}
 
     /**
 	 * Switch to next animation after pre/post- animation finished
 	 * @return true if pre or post animation ended, otherwise - false 
 	 */
 	private void continueModel() {
         // when motion at last switches we need to play cell animation
         if(hero.isFinishingMotionEnded()) {
             hero.switchToCurrentMotion();
         }
 	}
 	
 	/**
 	 * Move hero sprite on the screen
 	 */
 	protected void updateHeroScreenPosition() {
 		if(level.model.isLost()) {
 			finished = !moveLose();
 		} else if(isReadyToPlayWinAnimation()) {
 			finished = !hero.playWinAnimation();
 		} else {
             regularMove();
 		}
 	}
 
     private void regularMove() {
         if(hero.getRealMotion().getType().isHorizontalTP() ||
                 hero.getRealMotion().getType() == MotionType.TP) {
             hero.x = LEFT_BOUND + level.model.hero.getX() * GRID_STEP;
             hero.y = TOP_BOUND + level.model.hero.getY() * GRID_STEP;
         }
         int xSpeed = hero.getRealMotion().getXSpeed() * ANIMATION_JUMP_SPEED;
         int ySpeed = hero.getRealMotion().getYSpeed() * ANIMATION_JUMP_SPEED;
 
         hero.x += xSpeed;
         hero.y += ySpeed;
     }
 
     private int loseDelay = 3;
 
     /**
 	 * Random rotating movement if hero was spiked
 	 * @return
 	 */
 	private boolean moveLose() {
         if(monsterLose) {
             return doMonsterLose();
         } else if(level.model.outOfBounds()) {
             return doFallLose();
         } else {
             return doSpikeLose();
         }
 	}
 
     private boolean doSpikeLose() {
         if (loseDelay > 0) {
             loseDelay--;
             return true;
         } else {
             control.playDetonateSound();
             return hero.playDetonateAnimation();
         }
     }
 
     private boolean doFallLose() {
         regularMove();
         return !outOfAnimationBounds();
     }
 
     private boolean doMonsterLose() {
         control.playDetonateSound();
         return hero.playDetonateAnimation();
     }
 
     private boolean outOfAnimationBounds() {
         return hero.x <= LEFT_BOUND - GRID_STEP || hero.x >= RIGHT_BOUND + GRID_STEP ||
                hero.y <= TOP_BOUND - GRID_STEP || hero.y >= BOTTOM_BOUND + GRID_STEP;
     }
 
     private void moveLoseRandom() {
         double rand = Math.random();
         if(rand < 0.33) {
             hero.x += JUMP_SPEED;
         } else if(rand < 0.66) {
             hero.x -= JUMP_SPEED;
         }
         rand = Math.random();
         if(rand < 0.33) {
             hero.y += JUMP_SPEED;
         } else if(rand < 0.66) {
             hero.y -= JUMP_SPEED;
         }
     }
 	
 	public boolean isComplete() {
 		return level.model.isComplete();
 	}
 
     private boolean isReadyToPlayWinAnimation() {
         return isComplete() && !hero.isFinishing();
     }
 	
 	public int getScore() {
 		return level.model.getScore();
 	}
 	
 	protected void setLevId(int levId) {
 		this.levId = levId;
 	}
 
     private String getBackgroundId(int levId) {
         switch(levId) {
             case 1: return "background/background_l_1.jpg";
             case 2: return "background/background_l_2.jpg";
             case 3: return "background/background_l_3.jpg";
             case 4: return "background/background_l_4.jpg";
             default:return "background/background_l_4.jpg";
         }
     }
 	
 	protected int getLevId() {
 		return levId;
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		if(control.scanControl(event)) {
 			return true;
 		}
 		return super.onTouchEvent(event);
 	}
 	
 	public void updateMonster() {
 		if(isGridCoordinates(monster.xCoordinate, monster.yCoordinate)) {
 			level.model.nextDirection();
 		}
 		monster.moveInCurrentDirection();
 	}
 	
 	private boolean isGridCoordinates(int xCoordinate, int yCoordinate) {
 		return (xCoordinate - LEFT_BOUND) % GRID_STEP == 0 && 
 			   (yCoordinate - TOP_BOUND) % GRID_STEP == 0;
 	}
 	
 	protected void checkMonsterCollision() {
         if(monster == null) return;
         int heroShrink = (int) (GRID_STEP * 0.10);
         int monsterShrink = heroShrink;
         Rect heroRect = shrinkRect(hero.x, hero.y, GRID_STEP, GRID_STEP, heroShrink);
         Rect monsterRect = shrinkRect(
                 monster.xCoordinate, monster.yCoordinate, GRID_STEP, GRID_STEP, monsterShrink);
         if(heroRect.intersect(monsterRect)) {
             level.model.setLost(true);
             monsterLose = true;
         }
 	}
 
     private Rect shrinkRect(int x, int y, int w, int h, int shrink) {
         return new Rect(
                 x + shrink,
                 y + shrink,
                 x + w - 2 * shrink,
                 y + h - 2 * shrink);
     }
 
     public void updatePositions() {
         updateHeroScreenPosition();
         updateMonster();
         checkMonsterCollision();
     }
 
     public GameActivity getGameContext() {
         return gameContext;
     }
 
     protected Hero getHero() {
         return hero;
     }
 }
