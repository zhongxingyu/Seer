 #set( $symbol_pound = '#' )
 #set( $symbol_dollar = '$' )
 #set( $symbol_escape = '\' )
 package ${package}.core;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 
 public class LoadingScreen extends Screen {
 
 	public static final String LOADING_BORDER_ASSET = "loading-screen/border.png";
 	public static final String LOADING_TEXT_ASSET = "loading-screen/text.png";
 
 	private SpriteBatch batch;
 
 	private Sprite loadingText;
 	private Sprite loadingBorder;
 	private ShapeRenderer loadingBarRenderer;
 
 	private static final float fadeTime = 0.2f;
 	private float fadeTimeLeft = fadeTime;
 
 	private Screen nextScreen;
 
 	public LoadingScreen(final Registry r) {
 		super(r);
 	}
 
 	public Screen setNextScreen(final Screen nextScreen) {
 		this.nextScreen = nextScreen;
 		return this;
 	}
 
 	@Override
 	public void show() {
 		batch = new SpriteBatch();
 		loadingBarRenderer = new ShapeRenderer();
 
 		final boolean assetsAlreadyLoaded = r.assetManager.update();
 		if (assetsAlreadyLoaded) {
 			nextScreen();
 		}
 	}
 
 	@Override
 	public void hide() {
 		batch.dispose();
 		loadingBarRenderer.dispose();

		// Hack around libGDX issue 1640
		final Skin skin = r.assetManager.get(AssetManager.ui);
		final TextButton.TextButtonStyle style = skin.get(TextButton.TextButtonStyle.class);
		style.pressedOffsetX = 3;
		style.pressedOffsetY = -3;
 	}
 
 	@Override
 	public void render(final float delta) {
 		final boolean finishedLoading = r.assetManager.update();
 		final float fadeAlpha = fadeTimeLeft / fadeTime;
 		final Color fadeColour = new Color(fadeAlpha, fadeAlpha, fadeAlpha, 1);
 
 		Gdx.gl.glClearColor(0, 0, 0, 0);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 		tryLoadSprites();
 		drawSprites(fadeColour);
 		drawLoadingBar(fadeColour);
 
 		if (fadeTimeLeft < 0) {
 			nextScreen();
 		}
 		if (finishedLoading) {
 			fadeTimeLeft -= delta;
 		}
 	}
 
 	private void tryLoadSprites() {
 		if (loadingText == null && r.assetManager.isLoaded(LOADING_TEXT_ASSET)) {
 			loadingText = new Sprite(r.assetManager.<Texture>get(LOADING_TEXT_ASSET));
 			loadingText.setPosition(80, 224);
 		}
 		if (loadingBorder == null && r.assetManager.isLoaded(LOADING_BORDER_ASSET)) {
 			loadingBorder = new Sprite(r.assetManager.<Texture>get(LOADING_BORDER_ASSET));
 			loadingBorder.setPosition(144, 80);
 		}
 	}
 
 	private void drawSprites(final Color fadeColour) {
 		batch.begin();
 		if (loadingText != null) {
 			loadingText.setColor(fadeColour);
 			loadingText.draw(batch);
 		}
 		if (loadingBorder != null) {
 			loadingBorder.setColor(fadeColour);
 			loadingBorder.draw(batch);
 		}
 		batch.end();
 	}
 
 	private void drawLoadingBar(final Color fadeColour) {
 		final float progress = r.assetManager.getProgress();
 		final int width = (int) Math.ceil(480 * progress);
 		loadingBarRenderer.begin(ShapeRenderer.ShapeType.Filled);
 		loadingBarRenderer.setColor(fadeColour);
 		loadingBarRenderer.rect(160, 96, width, 32);
 		loadingBarRenderer.end();
 	}
 
 	private void nextScreen() {
 		r.game.setScreen(nextScreen);
 	}
 
 	public static void queueAssets(final AssetManager assetManager) {
 		assetManager.load(LOADING_TEXT_ASSET, Texture.class);
 		assetManager.load(LOADING_BORDER_ASSET, Texture.class);
 	}
 
 }
