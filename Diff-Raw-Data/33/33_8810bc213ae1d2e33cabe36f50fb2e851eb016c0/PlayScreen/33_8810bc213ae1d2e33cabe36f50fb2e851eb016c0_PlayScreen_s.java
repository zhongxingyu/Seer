 package com.shval.jnpgame;
 
 import static com.shval.jnpgame.Globals.*;
 
 import java.util.ArrayList;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.utils.TimeUtils;
 import com.sun.xml.internal.ws.api.pipe.NextAction;
 
 public class PlayScreen implements Screen, InputProcessor {
 
 	private static final String TAG = PlayScreen.class.getSimpleName();
 	
 	private Background background;
 	private Board board; // this is our world now
 	private JnpGame game;
 	private Sound buttonSound;
 	private float soundVolume; // in [0,1]
 	private ArrayList<Button> buttons;
 	private ArrayList<JNPLabel> labels;
 	//private int boardWidth;
 	private int boardHeight;
 	SpriteBatch spriteBatch;
 	OrthographicCamera camera;
 	private boolean isPrevRevertable;
 	static float worldWidth = 1000;
 	static float worldHeight = 1000;
 	TextureRegion frameCursor;
 	TextureRegion passedLevelStar;
 	float passedLevelStarGX;
 	float passedLevelStarGY;
 	
 	// secret sequences
 	final int SECRET_LENGTH = 3;
 	int secretInd;
 	int secretSequence;
 	
 	//
 	private float delta;
 	static private final float PERIOD = 0.0015f; //TODO: how to do it right ?
 	
 	// UI
 	private final int UI_FACTOR = 8; // higher is more sensitive
 	int uiThreshold;	
 	private int cellWidth;
 	private int cellHeight;
 
 	// panel state
 	int xDown;
 	int yDown;
 	boolean down;
 
 	private long timeDown;
 	
 	public PlayScreen(BoardConfig config, JnpGame game) {
 		this.game = game;
 		// this.level = level;
 		board = new Board(config, this);
 		background = config.getBackground();
 		buttonSound = config.getSound(SOUND_BUTTON);
 		soundVolume = config.getSoundVolume();
 		
 		Gdx.app.debug(TAG, "cellwidth is:" + cellWidth);
 		initButtons(config);
 		initLabels(config);
 
 		this.isPrevRevertable = false;
 		
         
 		
 		// ex BoardView
 		
 		//camera = new OrthographicCamera(10, 7);
 
 
 		camera = new OrthographicCamera(worldWidth, worldHeight);
 		camera.position.set(worldWidth / 2, worldHeight / 2, 0);
 		camera.update();
 
 		spriteBatch = new SpriteBatch();
 		spriteBatch.setProjectionMatrix(camera.combined);
 		//spriteBatch.setTransformMatrix(camera.);
 		
 		this.frameCursor = new TextureRegion( Assets.getFrameCursorTextrue(), 7, 7, 47, 47);
 		if (config.isCurrentLevelCompleted())
 			this.passedLevelStar = new TextureRegion( Assets.getStarTexture());
 		// the action begins (here, and not in Screen's constructor!)
         board.start();
 	}
 
 	private void initButtons(BoardConfig config) {
 		buttons = new ArrayList<Button>();
 		Button butt;
 		float fineOffset = 0.2f;
 		
 		// reset button
 		butt = new Button(board.getCols() - 4, 0 - fineOffset, 3, Button.BLACK_BG_BLUE_FRAME, Button.ICON_NONE, "Reset", new String("btnReset"));
 		buttons.add(butt);
 		
 		// next button
 		if (!config.lastLevel()) {
 			butt = new Button(board.getCols() - 1, board.getRows() - 1 + fineOffset, 1, Button.BLACK_BG_BLUE_FRAME, Button.ICON_ARROW_RIGHT, null, new String("btnNext"));
 			buttons.add(butt);
 		}		
 		
 		// prev button
 		if (!config.firstLevel()) {
 			butt = new Button(0, board.getRows() - 1 + fineOffset, 1, Button.BLACK_BG_BLUE_FRAME, Button.ICON_ARROW_LEFT, null, new String("btnPrevious"));
 			buttons.add(butt);
 		}
 		
 		// revert button
 		butt = new Button(board.getCols() - 6, 0 - fineOffset, 1, Button.BLACK_BG_BLUE_FRAME, Button.ICON_NONE, null, new String("btnRevert"));
 		butt.setIsEnabled(false);
 		buttons.add(butt);
 
 	}
 
 	
 	private void initLabels(BoardConfig config) {
 		labels = new ArrayList<JNPLabel>();
 		 int level = config.getLevel();
 		 int bonus = config.whichBonusLevel(level);
 		 String text;
 		 if (bonus > 0) {
 			 text = "Bonus Level ";
 			 while (bonus-- > 0)
 				 text = text + "I";
 		 }
 		 else
 			 text = "Level " + level;
 		 
 		 if (bonus == 0)
 			 text += " (last)";
 		JNPLabel label = new JNPLabel(text, board.getCols() / 2 - text.length() / 6 , board.getRows() - 1, false);
 		labels.add(label);
 		float x = board.getCols() / 2 - text.length() / 6 - 0.9f;
 		float y = board.getRows() - 0.75f;
 		
 		passedLevelStarGX =  (x * (float) worldWidth / (float) board.getCols());
 		passedLevelStarGY = (y * (float) worldHeight / (float) board.getRows());
 	}
 	
 	
 	@Override
 	public void render(float dt) {
 
 		//long startTime = TimeUtils.nanoTime();
 		//float t = Gdx.app.getGraphics().getDeltaTime();
 		delta += dt;
 		if (delta < PERIOD)
 			return;
 			
 		//Gdx.app.debug(TAG, "delta = " + delta + " ~ " + 1 / delta + " FPS");
 		// background (render & update)
 		spriteBatch.begin();
 		background.render(delta, spriteBatch);
 		spriteBatch.end();
 		
 		
 		
 		// board
 		
 		board.update(delta);
 		Gdx.gl.glEnable(GL10.GL_TEXTURE_2D);
 		
 		if (Board.renderMode == 2 /* static rendering */)
 			spriteBatch.begin();
 		
 		board.render(delta, spriteBatch);
 
 		if (Board.renderMode == 2 /* static rendering */)
 			spriteBatch.end();
 
 		//Gdx.gl.glDisable(GL10.GL_TEXTURE_2D);
 		
 		
 		//Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		updateRevertIcon();
 		spriteBatch.begin();
 		// buttons
 		for (Button button: buttons) {
 			button.render(spriteBatch);	
 		}
 		
 		
 		// completed star
 		if (passedLevelStar != null) {
 			spriteBatch.draw(passedLevelStar, passedLevelStarGX, passedLevelStarGY, 0.7f * worldWidth / board.getCols(), 0.6f * worldHeight / board.getRows());
 		}
 		
 			
 		// labels
 		for (JNPLabel label: labels) {
 			label.render(spriteBatch);
 		}
 		
		renderCursorFrame(spriteBatch);
		
 		spriteBatch.end();
 		
 		while (delta >= PERIOD)
 			delta -= PERIOD; //TODO: handle missed render calls?
 		
 	}
 
 	private void updateRevertIcon() {
 		boolean isRevertable = board.isRevertable();
 		if (isRevertable && !isPrevRevertable) {// just turned revertable
 			Button button = getButtonById("btnRevert");
 			button.setIconType(Button.ICON_ARROW_CIRC);
 			button.setIsEnabled(true);
 		}
 		if (!isRevertable && isPrevRevertable) {// just turned un-revertable
 			Button button = getButtonById("btnRevert");
 			button.setIconType(Button.ICON_NONE);
 			button.setIsEnabled(false);
 		} 
 		isPrevRevertable = isRevertable;
 	}
 	
 	Button getButtonById(String id) {
 		for (Button b: buttons) {
 			if (id.equals(b.getId()))
 				return b;
 		}
 		return null;
 	}
 	
 	@Override
 	public void resize(int width, int height) {
 		// TODO Auto-generated method stub
 		Gdx.app.debug(TAG, "Resizing screen to " + width + " x " + height);
 		//this.boardWidth = width; we need only height to flip vertical orientation
 		this.boardHeight = height;
 		cellWidth = width / board.getCols();
 		cellHeight = height / board.getRows();
 		uiThreshold = cellWidth/UI_FACTOR;
 		
 		background.setResolution((int) worldWidth, (int) worldHeight);
 		board.setResolution(worldWidth, worldHeight);
 		
 		for (Button button: buttons) {
 			button.setResolution((int) worldWidth / board.getCols(),
 							     (int) worldHeight / board.getRows());	
 		}
 		for (JNPLabel label: labels) {
 			label.setResolution((int) worldWidth / board.getCols(),
 				     (int) worldHeight / board.getRows());	
 
 		}
 		
 	}
 
 	@Override
 	// this is called when the main game makes this screen active
 	public void show() {
 		 Gdx.input.setInputProcessor(this);
 	}
 
 	@Override
 	// this is called when the main game makes another screen active
 	public void hide() {
 		Gdx.input.setInputProcessor(null);
 	}
 
 	@Override
 	public void pause() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void resume() {
 		// TODO Auto-generated method stub
 
 	}
 	
 	// TODO: does someone calls this function??
 	@Override
 	public void dispose() {
 		// TODO: where do we disspose of all game txtures
 		// created in asets?
 		// TODO: why memory increases as we go through levels?!! 
 		
 		board.dispose();
 		spriteBatch.dispose();
 		
 		// TODO: anything else ?
 		Gdx.input.setInputProcessor(null);
 	}
 
 	// * InputProcessor methods ***************************//
 	@Override
 	public boolean keyDown(int keycode) {
 		// TODO Auto-generated method stub
 		Gdx.app.debug(TAG, "Action keydown spotted. keycode = " + keycode);
 		if (keycode == Keys.X) {
 			// switch between rendering modes
 			if (PHYSICS_SUPPORTED) {
 				Board.renderMode += 1;
 				if (Board.renderMode == 3)
 					Board.renderMode = 0;
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public boolean keyUp(int keycode) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean keyTyped(char character) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
 		Gdx.app.debug(TAG, "Action down spotted. coords: x = " + screenX + " y = " + screenY);
 		xDown = screenX;
 		
 		// here (0, 0) is in the upper left, thats nasty
 		yDown = boardHeight - screenY;
 		 
 		float x = (float) xDown/ (float) cellWidth;
 		float y = (float) yDown/ (float) cellHeight;
 		
 		Gdx.app.debug(TAG, "Action down spotted. boardHeight: " + boardHeight + ". x = " + x + " y = " + y);
 
 		// ask buttons
 		String type = null;
 		for (Button butt: buttons) {
 			if (butt.isPressed(x, y)) {
 				type = butt.getId();
 				break;
 			}
 		}
 
 		
 		// secret buttons
 		{
 			if (xDown < cellWidth) { 
 				// record secret button sequence
 				 secretSequence = (10 * secretSequence) + (int) yDown / cellHeight;
 				 secretInd++;
 				 
 				if (secretInd == SECRET_LENGTH) {
 					Gdx.app.error(TAG, "Secret sequence pressed: sequence: " + secretSequence);
 					switch(secretSequence) {
 					case 123:
 						Gdx.app.error(TAG, "flipping");
 						game.flipLevel();
 						break;
 					case 000:
 						keyDown(Keys.X);
 						break;
 					default:
 					}
 					
 					if (secretSequence <= 40 && secretSequence > 0)
 						game.goToLevel(secretSequence);
 					secretInd = 0;
 					secretSequence = 0;
 				}
 			}
 			else {
 				secretInd = 0;
 				secretSequence = 0;
 			}
 		}
 				
 		
 		if (type == null) { // no button pressed
 			down = true;
 			if (timeDown == 0)
 				timeDown = TimeUtils.millis();
 			return true;
 		}
 		
 		
 		// button pressed - who?
 		boolean pressed = true;
 		if (type.equals(new String("btnReset"))) // TODO: implement buttons with cb like humans
 			pressed = board.start();	
 		if (type.equals(new String("btnRevert")))
 			pressed = board.revert();
 		if (type.equals(new String("btnPrevious")))
 			game.previousLevel();
 		if (type.equals(new String("btnNext")))
 			game.nextLevel();
 
 		if(pressed)
 			buttonSound.play(soundVolume);
 
 		return true;
 	}
 
 	@Override
 	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
 		down = false;
 		timeDown = 0;
 		return true;
 	}
 
 	@Override
 	public boolean touchDragged(int screenX, int screenY, int pointer) {
 		int x = screenX;
 		// here (0, 0) is in the upper left, thats nasty
 		int y = boardHeight - screenY;
 		int dir;
 		int uiThreshold = cellWidth/UI_FACTOR;
 		Gdx.app.debug(TAG, "Action move spotted. coords: x = " + x + " y = " + y);
 		
 		// apply thresholds for good UI
 		if (x > xDown + uiThreshold)
 			dir = RIGHT;
 		else if (x < xDown - uiThreshold)
 			dir = LEFT;
 		else
 			return true;
 
 		Gdx.app.debug(TAG, "Attempting to slide cell (" + xDown/cellWidth + ", " + yDown/cellHeight + ")" + "in direction " + dir);
 		if (board.attemptSlide(dir, xDown/cellWidth, yDown/cellHeight))
 			timeDown = TimeUtils.millis();
 		xDown = x;
 		yDown = y;
 		return true;
 	}
 
 	@Override
 	public boolean mouseMoved(int screenX, int screenY) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean scrolled(int amount) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public void win() {
 		String text = "Excellent !!!";
 		// make it large by setting
 		JNPLabel label = new JNPLabel(text, board.getCols() / 2 - text.length() / 6 , board.getRows() - 3, true);
 		label.setResolution((int) worldWidth / board.getCols(),
 			     (int) worldHeight / board.getRows());	
 		
 		labels.add(label);
 		
 		
 		Button button = getButtonById("btnReset");
 		buttons.remove(button);
 
 		button = getButtonById("btnRevert");
 		buttons.remove(button);
 
 		// next button
 		{
 			float fineOffset = 0.2f;
 			button = new Button(board.getCols() - 4, 0 - fineOffset, 3, Button.BLACK_BG_BLUE_FRAME, Button.ICON_NONE, "Next", new String("btnNext"));
 			button.setResolution((int) worldWidth / board.getCols(),
 				     (int) worldHeight / board.getRows());	
 
 			buttons.add(button);
 		}		
 		
 		// completed star
 		this.passedLevelStar = new TextureRegion( Assets.getStarTexture());
 		this.game.config.setCurrentLevelAsComplete();
 
 	}
 
 	void renderCursorFrame(SpriteBatch batch) {
 		
 		if (down == false)
 			return;
 
 		int delta = (int) ( TimeUtils.millis() - timeDown ) ;
		float a = (float) delta / ((float) 1000 * 2f);
 		//Gdx.app.debug(TAG, "a = " + a + " delta = " + delta + ", time " + (float) TimeUtils.millis() + ", timeDown " + timeDown);
 		int x = xDown / cellWidth;
 		int y = yDown / cellHeight;
 		
 		float gCellWidth = worldWidth / board.getCols();
 		float gCellHeight = worldHeight / board.getRows();
 		
		final int K = 2; 
 		for (int i = -K; i <= K; ++i) {
 			for (int j = -K; j <= K; ++j) {
				float d = (float) ( i * i + j * j ) / (float) (K * K + K * K);
 				int xx = x + i;
 				int yy = y + j;
				float alpha = Math.min(Math.max(0f, a - d), 1f);
 				batch.setColor(1, 1, 1, alpha);
 				batch.draw(frameCursor, xx * gCellWidth, yy * gCellHeight, gCellWidth, gCellHeight);
 			}
 		}
 			
 		batch.setColor(1, 1, 1, 1);
 		
 	}
 }
