 package steve4448.livetextbackground.background;
 
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import steve4448.livetextbackground.R;
 import steve4448.livetextbackground.activity.PreferencesActivity;
 import steve4448.livetextbackground.background.object.ExplosionParticle;
 import steve4448.livetextbackground.background.object.TextObject;
 import android.content.SharedPreferences;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.service.wallpaper.WallpaperService;
 import android.view.SurfaceHolder;
 import android.widget.Toast;
 
 public class LiveTextBackgroundService extends WallpaperService {
 	
 	@Override
 	public Engine onCreateEngine() {
 		return new LiveTextBackgroundEngine();
 	}
 	
 	private class LiveTextBackgroundEngine extends Engine {
 		private Paint paint = new Paint();
 		private CopyOnWriteArrayList<TextObject> textObj = new CopyOnWriteArrayList<TextObject>();
 		private CopyOnWriteArrayList<ExplosionParticle> textExplObj = new CopyOnWriteArrayList<ExplosionParticle>();
 		private Timer logicTimer;
 		private TimerTask logicTimerTask;
 		private final Handler paintHandler = new Handler();
 		private final Runnable paintRunnable = new Runnable() {
 			@Override
 			public void run() {
 				draw();
 			}
 		};
 		private boolean visible = false;
 		
 		private String[] availableStrings;
 		private boolean collisionEnabled;
 		private int desiredFPS;
 		private int textSizeMin, textSizeMax;
		private boolean tried = false;
 		
 		private LiveTextBackgroundEngine() {
 			paint.setAntiAlias(true);
 			paint.setStyle(Paint.Style.STROKE);
 		}
 		
 		@Override
 		public void onCreate(SurfaceHolder holder) {
 			super.onCreate(holder);
 		}
 		
 		@Override
 		public void onDestroy() {
 			super.onDestroy();
 			setupLogicHandler(false);
 		}
 		
 		@Override
 		public void onSurfaceCreated(SurfaceHolder holder) {
 			super.onSurfaceCreated(holder);
 		}
 		
 		@Override
 		public void onSurfaceDestroyed(SurfaceHolder holder) {
 			super.onSurfaceDestroyed(holder);
 			this.visible = false;
 			setupLogicHandler(false);
 		}
 		
 		@Override
 		public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
 			super.onSurfaceChanged(holder, format, width, height);
 			setupLogicHandler(true);
 		}
 		
 		@Override
 		public void onVisibilityChanged(boolean visible) {
 			this.visible = visible;
 			setupLogicHandler(visible);
 		}
 		
 		private boolean loadSettings() {
 			try {
 				SharedPreferences prefs = getSharedPreferences(PreferencesActivity.PREFERENCE_NAME, MODE_PRIVATE);
 				textSizeMin = prefs.getInt("settings_text_size_variance_min", getResources().getInteger(R.integer.label_settings_text_size_default_min));
 				textSizeMax = prefs.getInt("settings_text_size_variance_max", getResources().getInteger(R.integer.label_settings_text_size_default_max));
 				String[] defaultStrings = getResources().getString(R.string.label_settings_text_default).split("\\|");
 				int prefsStrings = prefs.getInt("settings_text", -1);
 				availableStrings = prefsStrings == -1 ? defaultStrings : new String[prefsStrings];
 				if(prefsStrings != -1) {
 					for(int i = 0; i < availableStrings.length; i++) {
 						availableStrings[i] = prefs.getString("settings_text" + i, null);
 					}
 				}
 				
 				if(availableStrings.length == 0) {
 					throw new Exception("Somehow zero available strings at this point..?");
 				}
 				
 				collisionEnabled = prefs.getBoolean("settings_collision", getResources().getBoolean(R.bool.label_settings_collision_default));
 				try {
 					desiredFPS = 1000 / Integer.parseInt(prefs.getString("settings_desired_fps", Integer.toString(getResources().getInteger(R.integer.label_settings_desired_fps_default))));
 				} catch(Exception e) {
 					e.printStackTrace();
 					Toast.makeText(getBaseContext(), R.string.toast_error_parsing_desired_fps, Toast.LENGTH_LONG).show();
 					prefs.edit().putString("settings_desired_fps", Integer.toString(getResources().getInteger(R.integer.label_settings_desired_fps_default))).commit();
 					desiredFPS = 1000 / getResources().getInteger(R.integer.label_settings_desired_fps_default);
 				}
 				return true;
 			} catch(Exception e) {
 				PreferenceManager.setDefaultValues(getBaseContext(), PreferencesActivity.PREFERENCE_NAME, MODE_PRIVATE, R.xml.livetextbackground_settings, true);
 				Toast.makeText(getBaseContext(), R.string.toast_error_loading_preferences, Toast.LENGTH_LONG).show();
				if(!tried) {
					tried = true;
					boolean fixed = loadSettings();
					return (tried = fixed);
				} else
					return false;
 			}
 		}
 		
 		private void setupLogicHandler(boolean on) {
 			if(on) {
 				if(logicTimer != null)
 					setupLogicHandler(false);
 				
 				if(!loadSettings()) {
 					Toast.makeText(getBaseContext(), R.string.toast_fatal_error_loading_preferences, Toast.LENGTH_LONG).show();
 					return;
 				}
 				
 				logicTimer = new Timer();
 				logicTimer.schedule(logicTimerTask = new TimerTask() {
 					@Override
 					public void run() {
 						logic();
 					}
 				}, desiredFPS, desiredFPS);
 				paintHandler.post(paintRunnable);
 			} else {
 				if(logicTimer != null) {
 					if(logicTimerTask != null) {
 						logicTimerTask.cancel();
 						logicTimerTask = null;
 					}
 					logicTimer.cancel();
 					logicTimer.purge();
 					logicTimer = null;
 				}
 				paintHandler.removeCallbacks(paintRunnable);
 			}
 		}
 		
 		private void logic() {
 			// long startTime = System.currentTimeMillis();
 			if((int)(Math.random() * 10) == 1 || textObj.size() < 3) {
 				String text = availableStrings[(int)(Math.random() * availableStrings.length)];
 				int size = (int)(textSizeMin + (Math.random() * (textSizeMax - textSizeMin)));
 				Rect bounds = new Rect();
 				paint.setTextSize(size);
 				paint.getTextBounds(text, 0, text.length(), bounds);
 				textObj.add(new TextObject(text, (float)(Math.random() * getDesiredMinimumWidth()), 0, bounds.width(), bounds.height(), size, Color.argb(155 + (int)(Math.random() * 100), (int)(Math.random() * 255), (int)(Math.random() * 255), (int)(Math.random() * 255))));
 			}
 			for(TextObject t : textObj) {
 				// Text Object Logic
 				if(t.velocityX > 0) {
 					t.velocityX -= 0.01;
 					if(t.velocityX < 0)
 						t.velocityX = 0;
 				}
 				if(t.velocityX < 0) {
 					t.velocityX += 0.01;
 					if(t.velocityX > 0)
 						t.velocityX = 0;
 				}
 				t.x += t.velocityX;
 				t.y += t.velocityY += 0.01;
 				if(t.y > getDesiredMinimumHeight() + (t.height) || t.x > getDesiredMinimumWidth() || t.x < 0 - t.width) {
 					textObj.remove(t);
 					continue;
 				}
 				if(collisionEnabled) {
 					RectF collisionRect = new RectF(t.x, t.y - t.height, t.x + t.width, t.y);
 					for(TextObject t2 : textObj) {
 						if(t2 == t)
 							continue;
 						if(collisionRect.intersect(t2.x, t2.y - t2.height, t2.x + t2.width, t2.y)) {
 							for(int i = 0; i < collisionRect.width() * collisionRect.height(); i++)
 								textExplObj.add(new ExplosionParticle(collisionRect.left + (float)(Math.random() * collisionRect.width()), collisionRect.top + (float)(Math.random() * collisionRect.height()), 2, ((int)(Math.random() * 2) == 0 ? t.color : t2.color)));
 							textObj.remove(t);
 							textObj.remove(t2);
 							break;
 						}
 					}
 				}
 			}
 			for(ExplosionParticle p : textExplObj) {
 				if(p.velocityX > 0) {
 					p.velocityX -= 0.01;
 					if(p.velocityX < 0)
 						p.velocityX = 0;
 				}
 				if(p.velocityX < 0) {
 					p.velocityX += 0.01;
 					if(p.velocityX > 0)
 						p.velocityX = 0;
 				}
 				
 				p.x += p.velocityX;
 				p.y += p.velocityY += 0.01;
 				int leftoverAlpha = Color.alpha(p.color) - (int)(Math.random() * 8);
 				if(leftoverAlpha < 0)
 					leftoverAlpha = 0;
 				p.color = Color.argb(leftoverAlpha, Color.red(p.color), Color.green(p.color), Color.blue(p.color));
 				if(Color.alpha(p.color) <= 0)
 					textExplObj.remove(p);
 			}
 			paintHandler.removeCallbacks(paintRunnable);
 			if(visible)
 				paintHandler.post(paintRunnable);
 			// System.out.println("Finished logic in " + (System.currentTimeMillis() - startTime) + "ms.");
 		}
 		
 		private void draw() {
 			// long startTime = System.currentTimeMillis();
 			final SurfaceHolder holder = getSurfaceHolder();
 			Canvas canvas = null;
 			try {
 				canvas = holder.lockCanvas();
 				if(canvas != null) {
 					canvas.drawColor(Color.DKGRAY);
 					for(ExplosionParticle p : textExplObj) {
 						paint.setColor(p.color);
 						canvas.drawRect(p.x, p.y, p.x + p.size, p.y + p.size, paint);
 					}
 					for(TextObject t : textObj) {
 						paint.setColor(t.color);
 						paint.setTextSize(t.size);
 						canvas.drawText(t.text, t.x, t.y, paint);
 					}
 					paint.setShadowLayer(1, 2, 2, Color.BLACK);
 				}
 			} finally {
 				if(canvas != null)
 					holder.unlockCanvasAndPost(canvas);
 			}
 			// System.out.println("Finished painting in " + (System.currentTimeMillis() - startTime) + "ms."); //Seems to take about 16-20ms on my device (LGP960).
 		}
 	}
 }
