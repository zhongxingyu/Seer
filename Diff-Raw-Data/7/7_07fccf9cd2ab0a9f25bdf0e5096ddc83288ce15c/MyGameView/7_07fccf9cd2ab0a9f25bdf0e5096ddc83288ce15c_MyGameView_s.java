 package com.slauson.dasher.game;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.TimeUnit;
 
 import com.slauson.dasher.objects.Asteroid;
 import com.slauson.dasher.objects.Drop;
 import com.slauson.dasher.objects.Player;
 import com.slauson.dasher.powerups.ActivePowerup;
 import com.slauson.dasher.powerups.PowerupBumper;
 import com.slauson.dasher.powerups.PowerupDrill;
 import com.slauson.dasher.powerups.PowerupMagnet;
 import com.slauson.dasher.powerups.PowerupSlow;
 import com.slauson.dasher.powerups.PowerupSmall;
 import com.slauson.dasher.powerups.PowerupInvulnerable;
 import com.slauson.dasher.powerups.PowerupBlackHole;
 import com.slauson.dasher.status.Achievements;
 import com.slauson.dasher.status.Configuration;
 import com.slauson.dasher.status.LocalStatistics;
 import com.slauson.dasher.status.Debugging;
 import com.slauson.dasher.status.Statistics;
 import com.slauson.dasher.status.Upgrades;
 import com.slauson.dasher.R;
 
 import android.content.Context;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.util.AttributeSet;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 /**
  * Main game view
  * 
  * Adapted from here: http://android-coding.blogspot.com/2012/01/create-surfaceview-game-step-by-step.html
  * 
  * @author Josh Slauson
  *
  */
 public class MyGameView extends SurfaceView implements SurfaceHolder.Callback {
 
 	/**
 	 * Debugging stuff
 	 */
 	
 	private String debugText = "";
 
 	
 	/**
 	 * Private stuff
 	 */
 	
 	private SurfaceHolder surfaceHolder;
 	
 	// main thread
 	private MyGameThread myGameThread;
	
 	// Canvas stuff
 	private Paint paint;
 	
 	// Stuff on screen
 	private Player player;
 	private ArrayList<Asteroid> asteroids;
 	private LinkedList<Drop> drops;
 
 	// powerups
 	private LinkedList<ActivePowerup> activePowerups;	
 	private int bombCounter;
 	private int quasarCounter;
 	
 	// Initialization flags
 	private boolean surfaceCreated = false;
 	private boolean initialized = false;
 	
 	// for swipe-based dodging
 	private float touchDownY;
 	
 	// for double-tap based dodging
 	private long lastTouchDownTime1;
 	private long lastTouchDownTime2;
 	
 	// for stay in place achievement
 	private long lastMoveTime;
 	
 	private Level level;
 	
 	private MyGameActivity gameActivity = null;
 	private Statistics localStatistics = null;
 	
 	private long pauseTime;
 	
 	
 	/**
 	 * Constants - private
 	 */
 	
 	// powerup sprites
 	private static final int R_POWERUP_DRILL = R.drawable.powerup_drill;
 	private static final int R_POWERUP_MAGNET = R.drawable.powerup_magnet;
 	private static final int R_POWERUP_SLOW = R.drawable.powerup_slow;
 	private static final int R_POWERUP_BLACK_HOLE = R.drawable.powerup_white_hole;
 	private static final int R_POWERUP_BUMPER = R.drawable.powerup_bumper;
 	private static final int R_POWERUP_BOMB = R.drawable.powerup_bomb;
 	private static final int R_POWERUP_SMALL = R.drawable.powerup_ship;
 	private static final int R_POWERUP_INVULNERABLE = R.drawable.powerup_invulnerable;
 	
 	// mode
 	private static final int MODE_PAUSED = 0;
 	private static final int MODE_RUNNING = 1;
 	
 	// drops
 	private static final float DROP_CHANCE = 1f;
 	
 	// powerup stuff
 	private static final int BOMB_COUNTER_MAX = 10;
 	private static final int QUASAR_COUNTER_MAX = 20;
 
 	// paint stuff
 	private static final int PLAYER_PAINT_STROKE_WIDTH = 2;
 	private static final int PLAYER_PAINT_COLOR = Color.WHITE;
 	private static final int ASTEROID_PAINT_STROKE_WIDTH = 1;
 	private static final int ASTEROID_PAINT_COLOR = Color.WHITE;
 	
 	// touch
 	private static final float DASH_TOUCH_FACTOR = 1.5f;
 	private static final float DASH_SWIPE_MIN_DISTANCE = 50;
 	private static final int DASH_DOUBLE_TAP_MIN_DURATION = 500;
 
 	// accelerometer
 	private static final float ACCELEROMETER_DEADZONE = 0.05f;
 	private static final float ACCELEROMETER_MAX = 0.3f;
 
 	/**
 	 * Constants - public
 	 */
 	
 	// powerups
 	public static final int NUM_POWERUPS = 8;
 	public static final int POWERUP_NONE = 0;
 	public static final int POWERUP_SMALL = 1;
 	public static final int POWERUP_SLOW = 2;
 	public static final int POWERUP_INVULNERABLE = 3;
 	public static final int POWERUP_DRILL = 4;
 	public static final int POWERUP_MAGNET = 5;
 	public static final int POWERUP_BLACK_HOLE = 6;
 	public static final int POWERUP_BUMPER = 7;
 	public static final int POWERUP_BOMB = 8;
 	
 	// powerup stuff
 	
 	// direction
 	public static final int DIRECTION_NORMAL = 0;
 	public static final int DIRECTION_REVERSE = 1;
 	
 	// stationary powerups to draw
 	public static final int R_MAGNET = R.drawable.magnet;
 	public static final int R_BLACK_HOLE = R.drawable.black_hole;
 	public static final int R_DRILL = R.drawable.drill;
 	public static final int R_BUMPER_LARGE = R.drawable.bumper_large;
 	public static final int R_BUMPER_LARGE_ALT = R.drawable.bumper_large_alt;
 	public static final int R_BUMPER = R.drawable.bumper;
 	public static final int R_BUMPER_ALT = R.drawable.bumper_alt;
 
 	/**
 	 * Shared stuff
 	 */
 	
 	// canvas
 	public static int canvasWidth, canvasHeight;
 	
 	// powerups
 	public static PowerupSlow powerupSlow;
 	public static PowerupInvulnerable powerupInvulnerability;
 	public static PowerupSmall powerupSmall;
 	
 	// current state
 	public static int gameMode = MODE_RUNNING;
 	public static int direction = DIRECTION_NORMAL;
 	public static float gravity = 1f;
 	
 	// random
 	public static Random random;
 
 	
 	public MyGameView(Context context) {
 		super(context);
 		// TODO Auto-generated constructor stub
 	}
 	
 	public MyGameView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		// TODO Auto-generated constructor stub
 	}
 	
 	public MyGameView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		// TODO Auto-generated constructor stub
 	}
 
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void surfaceCreated(SurfaceHolder holder) {
 		canvasWidth = getWidth();
 		canvasHeight = getHeight();
 		
 		surfaceCreated = true;
 	}
 
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		// TODO Auto-generated method stub
 	}
 	
 	public void MyGameSurfaceView_OnResume() {
 		
 		surfaceHolder = getHolder();
 		getHolder().addCallback(this);
 		
 		// Create and start background Thread
 		myGameThread = new MyGameThread(this);
 		myGameThread.setRunning(true);
 		myGameThread.start();
 		
 	}
 	
 	public void MyGameSurfaceView_OnPause() {
 		// Kill the background thread
 		boolean retry = true;
 		myGameThread.setRunning(false);
 		
 		while (retry) {
 			try {
 				myGameThread.join();
 				retry = false;
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	@Override
 	protected void onDraw(Canvas canvas) {
 		
 		// TODO: come up with a better way of doing this
 		if (!initialized) {
 			init();
 			return;
 		}
 		
 		// clear screen
 		canvas.drawColor(Color.BLACK);
 		
 		// draw active powerups
 		synchronized (activePowerups) {
 			for (ActivePowerup activePowerup : activePowerups) {
 				activePowerup.draw(canvas, paint);
 			}
 		}
 		
 		// draw asteroids
 		paint.setStrokeWidth(ASTEROID_PAINT_STROKE_WIDTH);
 		paint.setColor(ASTEROID_PAINT_COLOR);
 		synchronized (asteroids) {
 		
 			paint.setStrokeWidth(2);
 			paint.setColor(Color.WHITE);
 			paint.setStyle(Style.STROKE);
 
 			for (Asteroid asteroid : asteroids) {
 				asteroid.draw(canvas, paint);
 			}
 		}
 		
 		// draw player
 		paint.setStrokeWidth(PLAYER_PAINT_STROKE_WIDTH);
 		paint.setColor(PLAYER_PAINT_COLOR);
 		player.draw(canvas, paint);
 		
 		// draw drops
 		synchronized (drops) {
 			for (Drop drop : drops) {
 				drop.draw(canvas, paint);
 			}
 		}
 
 		// overlay bomb animation 
 		if (bombCounter > 0) {
 			float factor = Math.abs(1f*BOMB_COUNTER_MAX/2 - bombCounter)/BOMB_COUNTER_MAX*2;
 			
 			paint.setAlpha((int)(255 - 255*factor));
 			paint.setStyle(Style.FILL);
 			canvas.drawRect(0, 0, canvasWidth, canvasHeight, paint);
 
 			paint.setAlpha(255);
 		}
 		
 		// overlay bomb animation 
 		if (quasarCounter > 0) {
 			float factor = Math.abs(1f*QUASAR_COUNTER_MAX/2 - quasarCounter)/QUASAR_COUNTER_MAX*2;
 			
 			paint.setAlpha((int)(255 - 255*factor));
 			paint.setStyle(Style.FILL);
 			canvas.drawRect(0, 0, canvasWidth, canvasHeight, paint);
 
 			paint.setAlpha(255);
 		}
 		
 		// draw debug text
 		long duration = System.currentTimeMillis() - player.getStartTime();
 		
 		if (pauseTime > 0) {
 			duration = pauseTime - player.getStartTime();
 		}
 		
 		String durationText = String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(duration), TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
 		paint.setStrokeWidth(1);
 		paint.setColor(Color.WHITE);
 		canvas.drawText(durationText + "    " + debugText, 0, canvasHeight, paint);
 	}
 	
 	/**
 	 * Update surface view
 	 * @return true on success, false otherwise
 	 */
 	public boolean updateSurfaceView() {
 		
 		// the function run in background thread, not ui thread
 		if (!surfaceCreated) {
 			return false;
 		}
 		
 		Canvas canvas = null;
 		
 		try {
 			canvas = surfaceHolder.lockCanvas();
 			
 			synchronized (surfaceHolder) {
 				updateStates();
 				onDraw(canvas);
 			}
 		} finally {
 			if (canvas != null) {
 				surfaceHolder.unlockCanvasAndPost(canvas);
 			}
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Initialize game view
 	 */
 	private void init() {
 		
 		// initialize stuff
 		if (surfaceCreated && !initialized) {
 			
 			System.out.println("MyGameView init()");
 			
 			level = new Level(Debugging.level, Debugging.levelProgression);
 			
 			paint = new Paint(Paint.ANTI_ALIAS_FLAG);
 			random = new Random();		
 			
 			asteroids = new ArrayList<Asteroid>();
 			drops = new LinkedList<Drop>();
 
 			// powerups
 			activePowerups = new LinkedList<ActivePowerup>();	
 			bombCounter = 0;
 			quasarCounter = 0;
 			
 			// make sure we don't think the first single tap is a double tap
 			lastTouchDownTime1 = 0;
 			lastTouchDownTime2 = -2*DASH_DOUBLE_TAP_MIN_DURATION;
 			
 			lastMoveTime = 0;
 			
 			pauseTime = -1;
 						
 			player = new Player();
 			
 			powerupSlow = new PowerupSlow(Upgrades.slowUpgrade.getLevel());
 			powerupInvulnerability = new PowerupInvulnerable(Upgrades.invulnerabilityUpgrade.getLevel());
 			powerupSmall = new PowerupSmall(Upgrades.smallUpgrade.getLevel());
 
 			
 			float radius, speed;
 			
 			for (int i = 0; i < level.getNumAsteroids(); i++) {
 				
 				radius = level.getAsteroidRadiusFactorMin() + random.nextFloat()*level.getAsteroidRadiusFactorOffset();
 				speed = level.getAsteroidSpeedFactorMin() + random.nextFloat()*level.getAsteroidSpeedFactorOffset();
 				
 				asteroids.add(new Asteroid(radius, speed, level.hasAsteroidHorizontalMovement()));
 			}
 			
 			localStatistics = LocalStatistics.getInstance();
 			
 			initialized = true;
 		}
 	}
 	
 	/**
 	 * Sets activity of this view
 	 * @param gameActivity activity
 	 */
 	public void setActivity(MyGameActivity gameActivity) {
 		this.gameActivity = gameActivity;
 	}
 	
 	/**
 	 * Update asteroids
 	 */
 	public void updateAsteroids() {
 		
 		synchronized (asteroids) {
 		
 			Asteroid temp;
 			
 			// update asteroids
 			for(int i = 0; i < asteroids.size(); i++) {
 				temp = asteroids.get(i);
 				
 				// move asteroids
 				if (powerupSlow.isActive()) {
 					temp.update(0.5f);
 				} else {
 					temp.update();
 				}
 				
 				// reset asteroid off screen (include non-visible, non-intact asteroids here)
 				if (direction == DIRECTION_NORMAL && temp.getY() - temp.getHeight()/2 > canvasHeight) {
 					resetAsteroid(temp);
 				} else if (direction == DIRECTION_REVERSE && temp.getY() + temp.getHeight()/2 < 0) {
 					resetAsteroid(temp);
 				}
 				
 				// reset asteroid that needs reset
 				if (temp.getStatus() == Asteroid.STATUS_NEEDS_RESET) {
 					resetAsteroid(temp);
 				}
 				
 				// don't do anything for asteroids not on screen
 				if (!temp.onScreen()) {
 					continue;
 				}
 				
 				// alter asteroid for each active powerup
 				synchronized (activePowerups) {
 					for (ActivePowerup activePowerup : activePowerups) {
 						activePowerup.alterAsteroid(temp);
 					}
 				}
 				
 				// only check collision with player when asteroid is in normal status
 				if (temp.getStatus() == Asteroid.STATUS_NORMAL) {
 					
 					// check collision with player
 					if (player.getStatus() == Player.STATUS_NORMAL && player.checkAsteroidCollision(temp)) {
 						
 						// player is on top or bottom
 						if (player.inPosition()) {
 							
 							// game over if player isn't invulnerable
 							if (!powerupInvulnerability.isActive() && !Debugging.godMode) {
 								temp.breakup();
 								
 								player.breakup();
 								Timer timer = new Timer();
 								timer.schedule(new TimerTask() {
 									@Override
 									public void run() {
 										// found here: http://stackoverflow.com/questions/5161951/android-only-the-original-thread-that-created-a-view-hierarchy-can-touch-its-vi
 										gameActivity.runOnUiThread(new Runnable() {
 										     public void run() {
 										    	 gameActivity.gameOver();
 										    }
 										});
 									}
 								}, player.getBreakupDuration()-500);
 							} else {
 								// increment pass through counter
 								powerupInvulnerability.passThrough();
 							}
 						}
 						// player is dashing (only destroy asteroids when invulnerability has upgrade)
 						else if (!powerupInvulnerability.isActive() || powerupInvulnerability.isDasher()) {
 
 							temp.breakup();
 
 							// causes drop depending on upgrade
							if (player.getDashNumAffectedAsteroids() == 0 || player.getDashMultipleDrops()) {
 								dropPowerup(temp.getX(), temp.getY());
 							}
 							player.dashAffectedAsteroid(temp);
 							
 							// check small dash destroy achievement
 							if (powerupSmall.isActive()) {
 								Achievements.unlockLocalAchievement(Achievements.localSmallDashDestroy);
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Updates drops
 	 */
 	public void updateDrops() {
 		
 		synchronized (drops) {
 			
 			Drop temp;
 			
 			// update falling powerups
 			for (int i = 0; i < drops.size(); i++) {
 				
 				temp = drops.get(i);
 				
 				if (powerupSlow.isActive() && !powerupSlow.isAffectingDropsAndPowerups()) {
 					temp.update(0.5f);
 				} else {
 					temp.update();
 				}
 				
 				// reset drop off screen
 				if ((direction == DIRECTION_NORMAL && temp.getY() - temp.getHeight()/2 > canvasHeight) || (direction == DIRECTION_REVERSE && temp.getY() + temp.getHeight()/2 < 0)) {
 					drops.remove(temp);
 					i--;
 				}
 				
 				// check collision with player
 				if (player.getStatus() == Player.STATUS_NORMAL && temp.isVisible() && temp.checkBoxCollision(player)) {
 					
 					switch(temp.getType()) {
 					case POWERUP_MAGNET:
 						activePowerups.add(new PowerupMagnet(BitmapFactory.decodeResource(getResources(), R_MAGNET), temp.getX(), temp.getY(), player.getDirection(), Upgrades.magnetUpgrade.getLevel()));
 						localStatistics.usesMagnet++;
 						break;
 					case POWERUP_BLACK_HOLE:
 						activePowerups.add(new PowerupBlackHole(BitmapFactory.decodeResource(getResources(), R_BLACK_HOLE), temp.getX(), temp.getY(), Upgrades.blackHoleUpgrade.getLevel()));
 						localStatistics.usesBlackHole++;
 						break;
 					case POWERUP_DRILL:
 						activePowerups.add(new PowerupDrill(BitmapFactory.decodeResource(getResources(), R_DRILL), temp.getX(), temp.getY(), player.getDirection(), Upgrades.drillUpgrade.getLevel()));
 						localStatistics.usesDrill++;
 						break;
 					case POWERUP_BUMPER:
 						if (Upgrades.bumperUpgrade.getLevel() >= Upgrades.BUMPER_UPGRADE_INCREASED_SIZE) {
 							activePowerups.add(new PowerupBumper(BitmapFactory.decodeResource(getResources(), R_BUMPER_LARGE), BitmapFactory.decodeResource(getResources(), R_BUMPER_LARGE_ALT), temp.getX(), temp.getY(), Upgrades.bumperUpgrade.getLevel()));
 						} else {
 							activePowerups.add(new PowerupBumper(BitmapFactory.decodeResource(getResources(), R_BUMPER), BitmapFactory.decodeResource(getResources(), R_BUMPER_ALT), temp.getX(), temp.getY(), Upgrades.bumperUpgrade.getLevel()));
 						}
 						localStatistics.usesBumper++;
 						break;
 					case POWERUP_INVULNERABLE:
 						powerupInvulnerability.activate();
 						localStatistics.usesInvulnerability++;
 						break;
 					case POWERUP_BOMB:
 						bombCounter = BOMB_COUNTER_MAX;
 						localStatistics.usesBomb++;
 						break;
 					case POWERUP_SMALL:
 						powerupSmall.activate();
 						localStatistics.usesSmall++;
 						break;
 					case POWERUP_SLOW:
 						powerupSlow.activate();
 						localStatistics.usesSlow++;
 						break;
 					}
 					
 					drops.remove(temp);
 					i--;
 					
 					// check dash activate drop achievement
 					if (!player.inPosition()) {
 						Achievements.unlockLocalAchievement(Achievements.localDashActivateDrops);
 					}
 				} else {
 				
 					// alter falling powerup for each active powerup
 					synchronized (activePowerups) {
 						for (ActivePowerup activePowerup : activePowerups) {
 							// TODO: use something better here
 							if (activePowerup instanceof PowerupBumper) {
 								((PowerupBumper)activePowerup).alterSprite(temp);
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Update powerups
 	 */
 	public void updatePowerups() {
 		
 		// update inactive powerups
 		powerupInvulnerability.update();
 		powerupSlow.update();
 		powerupSmall.update();
 		
 		// check achievements
 		if (powerupSlow.isActive()) {
 			powerupSlow.checkAchievements();
 		}
 		if (powerupInvulnerability.isActive()) {
 			powerupInvulnerability.checkAchievements();
 		}
 		
 		synchronized (activePowerups) {
 		
 			ActivePowerup powerup;
 			
 			int blackHoleCount = 0;
 			
 			// update any active global powerups
 			for (int i = 0; i < activePowerups.size(); i++) {
 				
 				powerup = activePowerups.get(i);
 				
 				if (powerupSlow.isActive() && !powerupSlow.isAffectingDropsAndPowerups()) {
 					powerup.update(0.5f);
 				} else {
 					powerup.update(1f);
 				}
 				
 				// black hole specific stuff
 				if (powerup instanceof PowerupBlackHole) {
 					
 					blackHoleCount++;
 					
 					// check if we need to activate the quasar for the black hole
 					if (powerup.isFadingOut() && ((PowerupBlackHole)powerup).hasQuasar()) {
 						quasarCounter = QUASAR_COUNTER_MAX;
 						activateQuasar();
 						((PowerupBlackHole)powerup).activateQuasar();
 					}
 				}
 				
 				// check if any active powerups should be removed
 				if (!activePowerups.get(i).isActive()) {
 										
 					// reset teleporting drill
 					if (powerup instanceof PowerupDrill && ((PowerupDrill)powerup).hasTeleport()) {
 						((PowerupDrill)powerup).teleport();
 					} else {
 						powerup.checkAchievements();
 						activePowerups.remove(i);
 						i--;
 					}
 				} else {
 				
 					// alter drill active powerups for each bumper active powerup
 					for (ActivePowerup activePowerup : activePowerups) {
 						// TODO: use something better here
 						if (activePowerup instanceof PowerupBumper && activePowerups.get(i) instanceof PowerupDrill) {
 							((PowerupBumper)activePowerup).alterDrill((PowerupDrill)activePowerups.get(i));
 						}
 					}
 				}
 			}
 			
 			// check black hole trifecta achievement
 			if (blackHoleCount > Achievements.LOCAL_BLACK_HOLE_TRIFECTA_NUM) {
 				Achievements.unlockLocalAchievement(Achievements.localBlackHoleTrifecta);
 			}
 		}
 	}
 	
 	/**
 	 * Update player
 	 */
 	public void updatePlayer() {
 		player.update();
 		
 		//System.out.println("updatePlayer(): " + direction + " - " + player.getDirection() + ", " + gravity + " - " + player.getGravity());
 		
 		// check player bounds
 		if (player.getX() - player.getWidth()/2 < 0) {
 			player.setX(player.getWidth()/2);
 		}
 		
 		if (player.getX() + player.getWidth()/2 > canvasWidth) {
 			player.setX((canvasWidth - player.getWidth()/2));
 		}
 		
 		// check if player is in transition
 		if (direction != player.getDirection() || Math.abs(gravity - player.getGravity()) > 0.01) {
 			
 			// update gravity
 			gravity = player.getGravity();
 			
 			if (player.inPosition()) {
 				direction = player.getDirection();
 			}
 		}
 		
 		// alter player for each active powerup
 		if (player.getStatus() == Player.STATUS_NORMAL && !powerupInvulnerability.isActive()) {
 			synchronized (activePowerups) {
 				for (ActivePowerup activePowerup : activePowerups) {
 					// TODO: use something better here
 					if (activePowerup instanceof PowerupBumper) {
 						((PowerupBumper)activePowerup).alterPlayer(player);
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Update game state
 	 */
 	public void updateStates() {
 		
 		if (!initialized) {
 			init();
 			return;
 		}
 		
 		if (gameMode == MODE_PAUSED) {
 			return;
 		}
 		
 		// update bomb counter
 		if (bombCounter > 0) {
 			bombCounter--;
 			
 			if (bombCounter == BOMB_COUNTER_MAX/2) {
 				activateBomb();
 			}
 		}
 		
 		// update quasar counter
 		if (quasarCounter > 0) {
 			quasarCounter--;
 		}
 		
 		debugText = "" + player.getSpeed() + " - level " + level.getLevel();
 		
 		// add more asteroids if needed
 		if (level.update()) {
 			// add more asteroids if necessary
 			int numAsteroidsToAdd = level.getNumAsteroids() - asteroids.size();
 			
 			float radius, speed;
 			
 			for (int i = 0; i < numAsteroidsToAdd; i++) {
 				
 				radius = level.getAsteroidRadiusFactorMin() + random.nextFloat()*level.getAsteroidRadiusFactorOffset();
 				speed = level.getAsteroidSpeedFactorMin() + random.nextFloat()*level.getAsteroidSpeedFactorOffset();
 				
 				asteroids.add(new Asteroid(radius, speed, level.hasAsteroidHorizontalMovement()));
 			}
 		}
 
 		// update everything
 		updateAsteroids();
 		updateDrops();
 		updatePowerups();
 		updatePlayer();
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		
 		if (!initialized) {
 			init();
 			return false;
 		}
 		
 		// only move player ship when its in normal or invulnerable status
 		if (player.getStatus() != Player.STATUS_NORMAL && player.getStatus() != Player.STATUS_INVULNERABLE) {
 			return false;
 		}
 		
 		// get event position
 		float x = event.getX();
 		float y = event.getY();
 		
 		int action = event.getAction();
 		
 		switch(action) {
 		case MotionEvent.ACTION_DOWN:
 			
 			// check if pressed on player's position, then dash
 			if (x < player.getX() + player.getWidth()*DASH_TOUCH_FACTOR/2 && x > player.getX() - player.getWidth()*DASH_TOUCH_FACTOR/2 &&
 					y < player.getY() + player.getHeight()*DASH_TOUCH_FACTOR/2 && y > player.getY() - player.getHeight()*DASH_TOUCH_FACTOR/2)
 			{
 				player.dash();
 				break;
 			}
 			touchDownY = y;
 			lastTouchDownTime2 = lastTouchDownTime1;
 			lastTouchDownTime1 = System.currentTimeMillis();
 			
 			// check stay in place achievement
 			if (System.currentTimeMillis() - lastMoveTime > Achievements.LOCAL_OTHER_STAY_IN_PLACE_TIME) {
 				Achievements.unlockLocalAchievement(Achievements.localOtherStayInPlace);
 			}
 			
 			// don't break here so that player still moves
 		case MotionEvent.ACTION_MOVE:
 			
 			// only move horizontally when player is in position
 			if (player.inPosition()) {
 				player.setGoX(x);
 			}
 			
 			break;
 		case MotionEvent.ACTION_UP:
 			
 			// TODO: use touch history here (need to make sure y changed quick enough)
 			// dash based on a swipe motion event
 			if (player.getStatus() == Player.STATUS_NORMAL) {
 				if (direction == DIRECTION_NORMAL && touchDownY - y > DASH_SWIPE_MIN_DISTANCE) {
 					player.dash();
 				} else if (direction == DIRECTION_REVERSE && y - touchDownY > DASH_SWIPE_MIN_DISTANCE) {
 					player.dash();
 				}
 			}
 			
 			// dash based on double tap
 			if (player.getStatus() == Player.STATUS_NORMAL) {
 				if (lastTouchDownTime1 - lastTouchDownTime2 < DASH_DOUBLE_TAP_MIN_DURATION && System.currentTimeMillis() - lastTouchDownTime2 < DASH_DOUBLE_TAP_MIN_DURATION) {
 					player.dash();
 				}
 			}
 			
 			lastMoveTime = System.currentTimeMillis();
 		default:
 			break;
 		}
 		
 		return true;
 	}
 
 	/**
 	 * Handle key down events, this is called from game activity
 	 * @param keyCode
 	 * @param event
 	 */
 	void keyDown(int keyCode, KeyEvent event) {
 		
 		if (!initialized) {
 			init();
 			return;
 		}
 
 		// only move player ship when its in normal or invulnerable status
 		if (player.getStatus() != Player.STATUS_NORMAL && player.getStatus() != Player.STATUS_INVULNERABLE) {
 			return;
 		}
 
 		switch(keyCode) {
 		// left
 		case KeyEvent.KEYCODE_DPAD_LEFT:
 		case KeyEvent.KEYCODE_S:
 			if (Configuration.controlType == Configuration.CONTROL_KEYBOARD) {
 				player.moveLeft();
 				System.out.println("PLAYER MOVE LEFT");
 				
 				// check stay in place achievement
 				if (System.currentTimeMillis() - lastMoveTime > Achievements.LOCAL_OTHER_STAY_IN_PLACE_TIME) {
 					Achievements.unlockLocalAchievement(Achievements.localOtherStayInPlace);
 				}
 				lastMoveTime = System.currentTimeMillis();
 			}
 			break;			
 		// right
 		case KeyEvent.KEYCODE_DPAD_RIGHT:
 		case KeyEvent.KEYCODE_L:
 			if (Configuration.controlType == Configuration.CONTROL_KEYBOARD) {
 				player.moveRight();
 				System.out.println("PLAYER MOVE RIGHT");
 				
 				// check stay in place achievement
 				if (System.currentTimeMillis() - lastMoveTime > Achievements.LOCAL_OTHER_STAY_IN_PLACE_TIME) {
 					Achievements.unlockLocalAchievement(Achievements.localOtherStayInPlace);
 				}
 				lastMoveTime = System.currentTimeMillis();
 			}
 			break;
 		}
 	}
 	
 	/**
 	 * Handle key down events, this is called from game activity
 	 * @param keyCode
 	 * @param event
 	 */
 	void keyUp(int keyCode, KeyEvent event) {
 		
 		if (!initialized) {
 			init();
 			return;
 		}
 
 		// only move player ship when its in normal or invulnerable status
 		if (player.getStatus() != Player.STATUS_NORMAL && player.getStatus() != Player.STATUS_INVULNERABLE) {
 			return;
 		}
 
 		switch(keyCode) {
 		// left/right
 		case KeyEvent.KEYCODE_DPAD_LEFT:
 		case KeyEvent.KEYCODE_DPAD_RIGHT:
 		case KeyEvent.KEYCODE_S:
 		case KeyEvent.KEYCODE_L:
 			if (Configuration.controlType == Configuration.CONTROL_KEYBOARD) {
 				player.moveStop();
 				System.out.println("PLAYER NO MOVE");
 			}
 			break;
 		// dpad center
 		case KeyEvent.KEYCODE_DPAD_CENTER:
 		case KeyEvent.KEYCODE_SPACE:
 			player.dash();
 			break;
 		}
 	}
 	
 	/**
 	 * Handle accelerometer, this is called from game activity
 	 * @param tx x factor
 	 * @param ty y factor
 	 */
 	void updateAccelerometer(float tx, float ty) {
 		
 		if (!initialized) {
 			init();
 			return;
 		}
 		
 		// only move player ship when its in normal or invulnerable status
 		if (player.getStatus() != Player.STATUS_NORMAL && player.getStatus() != Player.STATUS_INVULNERABLE) {
 			return;
 		}
 		
 		float moveX = 0f;
 		
 		if (tx > 0) {
 			player.setDirX(-1);
 		} else {
 			tx = Math.abs(tx);
 			player.setDirX(1);
 		}
 		
 		if (tx < ACCELEROMETER_DEADZONE) {
 			player.setDirX(0);
 			return;
 		}
 
 		if (tx > ACCELEROMETER_MAX) {
 			tx = ACCELEROMETER_MAX;
 		}
 		
 		moveX = ((tx - ACCELEROMETER_DEADZONE)/ACCELEROMETER_MAX)*player.getMaxSpeed();
 		player.setSpeed(moveX);
 		//player.setSpeed(move);
     	//debugText = debugText + tx;
 		
 		// check stay in place achievement
 		if (System.currentTimeMillis() - lastMoveTime > Achievements.LOCAL_OTHER_STAY_IN_PLACE_TIME) {
 			Achievements.unlockLocalAchievement(Achievements.localOtherStayInPlace);
 		}
 		lastMoveTime = System.currentTimeMillis();
 	}
 	
 	/**
 	 * Toggles pause state of game
 	 * @param paused true if game should be paused
 	 */
 	public void togglePause(boolean paused) {
 		if (paused) {
 			gameMode = MODE_PAUSED;
 			
 			pauseTime = System.currentTimeMillis();
 		} else {
 			gameMode = MODE_RUNNING;
 			
 			// update player start time to get accurate time
 			if (pauseTime > 0) {
 				player.addToStartTime(System.currentTimeMillis() - pauseTime);
 			}
 			
 			pauseTime = -1;
 			
 			// reset all update times
 			resetUpdateTimes();
 		}
 	}
 	
 	/**
 	 * May drop a random powerup at the given position
 	 * @param x x coordinate
 	 * @param y y coordinate
 	 * @return powerup type
 	 */
 	private int dropPowerup(float x, float y) {
 		// create powerup
 		int powerup = 1 + random.nextInt(NUM_POWERUPS);
 		
 		// debug mode
 		if (Debugging.dropType != POWERUP_NONE) {
 			powerup = Debugging.dropType;
 		}
 		
 		int r_powerup = 0;
 		
 		switch(powerup) {
 		case POWERUP_SMALL:
 			r_powerup = R_POWERUP_SMALL;
 			break;
 		case POWERUP_SLOW:
 			r_powerup = R_POWERUP_SLOW;
 			break;
 		case POWERUP_INVULNERABLE:
 			r_powerup = R_POWERUP_INVULNERABLE;
 			break;
 		case POWERUP_DRILL:
 			r_powerup = R_POWERUP_DRILL;
 			break;
 		case POWERUP_MAGNET:
 			r_powerup = R_POWERUP_MAGNET;
 			break;
 		case POWERUP_BLACK_HOLE:
 			r_powerup = R_POWERUP_BLACK_HOLE;
 			break;
 		case POWERUP_BUMPER:
 			r_powerup = R_POWERUP_BUMPER;
 			break;
 		case POWERUP_BOMB:
 			r_powerup = R_POWERUP_BOMB;
 			break;
 		}
 		
 		Drop drop= new Drop(BitmapFactory.decodeResource(getResources(), r_powerup), x, y, powerup);
 		
 		drops.add(drop);
 		
 		return powerup;
 	}
 	
 	/**
 	 * Activates bomb powerup, destroying everything on screen
 	 */
 	private void activateBomb() {
 		
 		int numAffectedAsteroids = 0;
 		int numDrops = 0;
 		
 		// destroy all on-screen asteroids
 		for (Asteroid asteroid : asteroids) {
 			asteroid.fadeOut(true);
 			numAffectedAsteroids++;
 			
 			// cause drop if upgraded
 			if (Upgrades.bombUpgrade.getLevel() >= Upgrades.BOMB_UPGRADE_CAUSE_DROP) {
 				if (numDrops == 0 || (numDrops == 1 && Upgrades.bombUpgrade.getLevel() >= Upgrades.BOMB_UPGRADE_CAUSE_DROPS)) {
 					int powerupType = dropPowerup(asteroid.getX(), asteroid.getY());
 					numDrops++;
 					
 					// check bomb drop bomb achievement
 					if (powerupType == POWERUP_BOMB) {
 						Achievements.unlockLocalAchievement(Achievements.localBombDropBomb);
 					}
 				}
 			}
 		}
 		
 		// check for achievements
 		if (numAffectedAsteroids > Achievements.LOCAL_DESTROY_ASTEROIDS_NUM_1 &&
 				!Achievements.localDestroyAsteroidsWithBomb1.getValue())
 		{
 			Achievements.unlockLocalAchievement(Achievements.localDestroyAsteroidsWithBomb1);
 		}
 		
 		if (numAffectedAsteroids > Achievements.LOCAL_DESTROY_ASTEROIDS_NUM_2 &&
 				!Achievements.localDestroyAsteroidsWithBomb2.getValue())
 		{
 			Achievements.unlockLocalAchievement(Achievements.localDestroyAsteroidsWithBomb2);
 		}
 		
 		if (numAffectedAsteroids > Achievements.LOCAL_DESTROY_ASTEROIDS_NUM_3 &&
 				!Achievements.localDestroyAsteroidsWithBomb3.getValue())
 		{
 			Achievements.unlockLocalAchievement(Achievements.localDestroyAsteroidsWithBomb3);
 		}
 
 		
 		// destroy all falling powerups
 		if (Upgrades.bombUpgrade.getLevel() < Upgrades.BOMB_UPGRADE_NO_EFFECT_DROPS) {
 			drops.clear();
 		}
 		
 		// destroy all active powerups
 		if (Upgrades.bombUpgrade.getLevel() < Upgrades.BOMB_UPGRADE_NO_EFFECT_POWERUPS) {
 			activePowerups.clear();
 		}
 	}
 	
 	/**
 	 * Activates quasar powerup, destroying all asteroids on screen
 	 */
 	private void activateQuasar() {
 		// destroy all on-screen asteroids
 		for (Asteroid asteroid : asteroids) {
 			asteroid.fadeOut(false);
 		}
 	}
 	
 	/**
 	 * Resets given asteroid
 	 * @param asteroid asteroid to reset
 	 */
 	private void resetAsteroid(Asteroid asteroid) {
 		float radius = level.getAsteroidRadiusFactorMin() + random.nextFloat()*level.getAsteroidRadiusFactorOffset();
 		float speed = level.getAsteroidSpeedFactorMin() + random.nextFloat()*level.getAsteroidSpeedFactorOffset();
 		
 		asteroid.reset(radius, speed, level.hasAsteroidHorizontalMovement());
 	}
 	
 	/**
 	 * Resets game state
 	 */
 	public void reset() {
 		level.reset();
 		resetAsteroids();
 		resetUpdateTimes();
 		
 		activePowerups.clear();
 		drops.clear();
 	}
 	
 	/**
 	 * Resets all asteroids
 	 */
 	private void resetAsteroids() {
 		int numAsteroids = level.getNumAsteroids();
 		
 		while (asteroids.size() > numAsteroids) {
 			asteroids.remove(asteroids.size()-1);
 		}
 		
 		for (Asteroid asteroid : asteroids) {
 			resetAsteroid(asteroid);
 		}
 	}
 	
 	/**
 	 * Resets update times for player, asteroids, drops, and powerup
 	 */
 	private void resetUpdateTimes() {
 		player.resetUpdateTime();
 		
 		for (Asteroid asteroid : asteroids) {
 			asteroid.resetUpdateTime();
 		}
 		
 		for (Drop drop : drops) {
 			drop.resetUpdateTime();
 		}
 		
 		for (ActivePowerup activePowerup: activePowerups) {
 			activePowerup.resetUpdateTime();
 		}
 	}
 }
