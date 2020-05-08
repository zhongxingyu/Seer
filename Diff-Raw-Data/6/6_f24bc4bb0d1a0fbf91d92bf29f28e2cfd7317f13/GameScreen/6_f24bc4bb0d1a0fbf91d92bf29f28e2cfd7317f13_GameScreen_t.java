 package org.racenet.racesow;
 
 import java.util.List;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import org.racenet.framework.BitmapFont;
 import org.racenet.framework.Camera2;
 import org.racenet.framework.CameraText;
 import org.racenet.framework.GLGame;
 import org.racenet.framework.GLGraphics;
 import org.racenet.framework.GLTexture;
 import org.racenet.framework.GameObject;
 import org.racenet.framework.SpriteBatcher;
 import org.racenet.framework.TexturedBlock;
 import org.racenet.framework.Vector2;
 import org.racenet.framework.interfaces.Game;
 import org.racenet.framework.interfaces.Screen;
 import org.racenet.framework.interfaces.Input.TouchEvent;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 
 /**
  * Class which represents the racesow game itsself.
  * 
  * @author soh#zolex
  *
  */
 class GameScreen extends Screen {
 		
 	public Player player;
 	CameraText ups, fps, timer;
 	public Map map;
 	
 	Vector2 gravity = new Vector2(0, -30);
 	public Camera2 camera;
 	GLGraphics glGraphics;
 	
 	boolean jumpPressed = false;
 	float jumpPressedTime = 0;
 	boolean shootPressed = false;
 	float shootPressedTime = 0;
 	SpriteBatcher batcher;
 	BitmapFont font;
 	
 	boolean showFPS, showUPS;
 	
 	int fpsInterval = 5;
 	int frames = 10;
 	float sumDelta = 0;
 	public GameState state = GameState.Running;
 	
 	public enum GameState {
 		
 		Running,
 		Paused
 	}
 	
 	public GameScreen(Game game, Camera2 camera, Map map, Player player) {
 			
 		super(game);
 		this.glGraphics = ((GLGame)game).getGLGraphics();
 		this.camera = camera;
 		this.map = map;
 		this.player = player;
 		this.player.setGameScreen(this);
 		
 		GLTexture.APP_FOLDER = "racesow";
 		
 		this.batcher = new SpriteBatcher(this.glGraphics, 96);
 		GLTexture texture = new GLTexture((GLGame)game, "font.png");
 		this.font = new BitmapFont(texture, 0, 0, 17, 30, 50);
 		
 		SharedPreferences prefs = ((Activity)this.game).getSharedPreferences("racesow", Context.MODE_PRIVATE);
 		this.showFPS = prefs.getBoolean("fps", false);
 		this.showUPS = prefs.getBoolean("ups", true);
 		
 		if (this.showFPS) {
 		
 			this.fps = this.createCameraText(this.camera.frustumWidth / 2 - 10, this.camera.frustumHeight / 2 - 3);
 			this.fps.text = "fps";
 			this.camera.addHud(this.fps);
 		}
 		
 		if (this.showUPS) {
 			
 			this.ups = this.createCameraText(this.camera.frustumWidth / 2 - 25, this.camera.frustumHeight / 2 - 3);
 			this.camera.addHud(this.ups);
 		}
 		
 		this.timer = this.createCameraText(this.camera.frustumWidth / 2 - 45, this.camera.frustumHeight / 2 - 3);
 		this.camera.addHud(this.timer);
 		
 		TexturedBlock pause = new TexturedBlock(
 			(GLGame)this.game,
 			"hud/pause.png", GameObject.FUNC_NONE, -1, -1,
 			new Vector2(-this.camera.frustumWidth / 2 + 1 , this.camera.frustumHeight / 2 - 6),
 			new Vector2(-this.camera.frustumWidth / 2 + 6, 0));
 		this.camera.addHud(pause);
 	}
 	
 	/**
 	 * Create a new instance of the CamraText which
 	 * is used as a HudItem on the Camera.
 	 * 
 	 * @param float cameraX
 	 * @param float cameraY
 	 * @return CameraText
 	 */
 	public CameraText createCameraText(float cameraX, float cameraY) {
 		
 		return new CameraText(this.batcher, this.font, this.glGraphics.getGL(), cameraX, cameraY);
 	}
 
 	/**
 	 * Update player, map and camera.
 	 * Called each frame from GLGame.
 	 */
 	public void update(float deltaTime) {
 		
 		List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
 		int len = touchEvents.size();
 		for (int i = 0; i < len; i++) {
 			
 			TouchEvent e = touchEvents.get(i);
 			
 			if (e.type == TouchEvent.TOUCH_DOWN) {
 				
 				// when touching the pause-button
 				if (e.y / (float)game.getScreenHeight() < 0.1f && e.x / (float)game.getScreenWidth() < 0.1f) {
 				
 					if (this.state == GameState.Running) {
 						
 						this.state = GameState.Paused;		
 						
 					} else if (this.state == GameState.Paused) {
 						
 						this.state = GameState.Running;
 						
 					} 
 				}
 				
 				// when touching the jump-area on the screen
 				if (e.x / (float)game.getScreenWidth() > 0.5f) {
 					
 					if (!this.jumpPressed) {
 						
 						this.jumpPressed = true;
 						this.jumpPressedTime = 0;
 					}
 				
 				// when touching the shoot-area on the screen
 				} else {
 					
 					if (!this.shootPressed) {
 						
 						this.shootPressed = true;
 						this.shootPressedTime = 0;
 					}
 				}
 
 			} else if (e.type == TouchEvent.TOUCH_UP) {
 				
 				// this is only for the tutorial
 				if (this.state == GameState.Paused) {
 					
 					this.player.updateTutorial("release");
 				}
 				
 				// when releasing the jump-button
 				if (e.x / (float)game.getScreenWidth() > 0.5f) {
 					
 					this.jumpPressed = false;
 					this.jumpPressedTime = 0;
 					
 				// when releasing the shoot-button
 				} else {
 					
 					this.shootPressed = false;
 					this.shootPressedTime = 0;
 				}
 			}
 		}
 		
 		// execute jump if requested
 		if (this.jumpPressed) {
 			
 			this.player.jump(this.jumpPressedTime);
 			this.jumpPressedTime += deltaTime;
 		}
 		
 		// execute shoot if requested
 		if (this.shootPressed) {
 			
 			this.player.shoot(this.shootPressedTime);
 			this.shootPressedTime += deltaTime;
 		}
 		
 		//  nothing more to do here when paused
 		if (this.state == GameState.Paused) {
 			
			if (map.getCurrentTime() > 0 ) {
			
				this.map.pauseTime += deltaTime;
			}
			
 			return;
 		}
 		
 		// update the player
 		this.player.move(this.gravity, deltaTime, this.jumpPressed);
 		
 		// move the camera upwards if the player goes to high
 		float camY = this.camera.frustumHeight / 2;
 		if (this.player.getPosition().y + 12 > this.camera.frustumHeight) {
 			
 			camY = this.player.getPosition().y - this.camera.frustumHeight / 2 + 12;
 		}
 		
 		this.camera.setPosition(this.player.getPosition().x + 20, camY);		
 		this.map.update(deltaTime);
 
 		if (this.showFPS) {
 			
 			// update HUD for frames per second
 			this.frames--;
 			this.sumDelta += deltaTime;
 			if (frames == 0) {
 			
 				this.fps.text = "fps " + String.valueOf(new Integer((int)(this.fpsInterval / this.sumDelta)));
 				this.frames = fpsInterval;
 				this.sumDelta = 0;
 				
 			}
 		}
 
 		if (this.showUPS) {
 		
 			// update HUD for player-speed
 			this.ups.text = "ups " + String.valueOf(new Integer((int)player.virtualSpeed));
 		}
 		
 		// update hud for time
 		this.timer.text = "t " + String.format("%.4f", map.getCurrentTime());
 	}
 
 	/**
 	 * Draw player map and HUD. Called by GLGame each frame.
 	 */
 	public void present(float deltaTime) {
 		
 		GL10 gl = this.glGraphics.getGL();
 		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		
 		gl.glFrontFace(GL10.GL_CCW);
 		gl.glEnable(GL10.GL_CULL_FACE);
 		gl.glCullFace(GL10.GL_BACK);
 		
 		this.camera.setViewportAndMatrices();
 		
 		gl.glEnable(GL10.GL_TEXTURE_2D);
 		gl.glEnable(GL10.GL_BLEND);
 		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
 		
 		this.map.draw();
 		this.player.draw();
 		
 		synchronized (this.player) {
 		
 			this.camera.drawHud();
 		}
 	}
 
 	/**
 	 * Nothing to do on GLGame pause
 	 */
 	public void pause() {
 
 	}
 
 	/**
 	 * Reload all textures because
 	 * the openGL context was lost
 	 */
 	public void resume() {
 
 		this.camera.reloadTextures();
 		this.map.reloadTextures();
 		this.player.reloadTextures();
 	}
 
 	/**
 	 * Get rid of all textures when leaving the screen
 	 */
 	public void dispose() {
 
 		this.camera.dispose();
 		this.map.dispose();
 		this.player.dispose();
 	}
 }
