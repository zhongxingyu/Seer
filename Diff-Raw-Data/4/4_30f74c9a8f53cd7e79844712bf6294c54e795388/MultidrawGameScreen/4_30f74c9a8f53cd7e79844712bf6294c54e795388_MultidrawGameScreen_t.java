 package org.i52jianr.multidraw.screens;
 
 import java.util.ArrayList;
 
 import org.i52jianr.multidraw.Brush;
 import org.i52jianr.multidraw.BrushButtonDescriptor;
 import org.i52jianr.multidraw.Brushes;
 import org.i52jianr.multidraw.DrawingArea;
 import org.i52jianr.multidraw.Multidraw;
 import org.i52jianr.multidraw.multiplayer.callbacks.EndGameHandler;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Pixmap;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.Texture.TextureFilter;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Button;
 import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.Slider;
 import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
 import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
 import com.badlogic.gdx.utils.Scaling;
 
 public class MultidrawGameScreen extends MultidrawBaseGameScreen {
 
 	private final int OFFSET_X = 31;
 	private final int OFFSET_Y = 16;
 	
 	private int red_x = 25;
 	private int red_y = 80;
 	
 	private int green_x = 25;
 	private int green_y = 50;
 	
 	private int blue_x = 25;
 	private int blue_y = 20;
 	
 	private float scaleX = 1.0f;
 	private float scaleY = 1.0f;
 	
 	/* UI Stuff */
 	private Slider red_slider;
 	private Slider blue_slider;
 	private Slider green_slider;
 	private Pixmap colorPreview;
 	private Texture colorPreviewTexture;
 	
 	private ArrayList<BrushButtonDescriptor> brushButtonsDesc;
 	private ArrayList<Button> brushButtons;
 	private int brushIndex;
 	
 	private Button activeBrushButton;
 	
 	private boolean online;
 	private boolean justTouched;
 	
 	public MultidrawGameScreen(Multidraw game) {
 		this(game, "Whatever you want");
 		online = false;
 	}
 	
 	public MultidrawGameScreen(Multidraw game, String word) {
 		super(game, word);
 		
 		this.game.nat.gameStarted(new EndGameHandler() {
 			
 			@Override
 			public void onGameEnd(String why) {
 				MultidrawGameScreen.this.game.setMenuScreen(why);
 			}
 		});
 		
 		online = true;
 		brushButtonsDesc = new ArrayList<BrushButtonDescriptor>();
 		brushButtons = new ArrayList<Button>();
 		
 		brushButtonsDesc.add(new BrushButtonDescriptor(Brushes.pixel, red_x, red_y + 80));
 		brushButtonsDesc.add(new BrushButtonDescriptor(Brushes.round, red_x + 45, red_y + 80));
 		brushButtonsDesc.add(new BrushButtonDescriptor(Brushes.round7, red_x + 90, red_y + 80));
 		brushButtonsDesc.add(new BrushButtonDescriptor(Brushes.round11, red_x + 135, red_y + 80));
 		brushButtonsDesc.add(new BrushButtonDescriptor(Brushes.crossSmooth, red_x + 180, red_y + 80));
 		brushButtonsDesc.add(new BrushButtonDescriptor(Brushes.smooth5, red_x + 225, red_y + 80));
 		brushButtonsDesc.add(new BrushButtonDescriptor(Brushes.smooth7, red_x, red_y + 35));
 		brushButtonsDesc.add(new BrushButtonDescriptor(Brushes.smooth11, red_x + 45, red_y + 35));
 		brushButtonsDesc.add(new BrushButtonDescriptor(Brushes.square3, red_x + 90, red_y + 35));
 		brushButtonsDesc.add(new BrushButtonDescriptor(Brushes.square5, red_x + 135, red_y + 35));
 		brushButtonsDesc.add(new BrushButtonDescriptor(Brushes.square11, red_x + 180, red_y + 35));
 		brushButtonsDesc.add(new BrushButtonDescriptor(Brushes.square15, red_x + 225, red_y + 35));
 	}
 	
 	@Override
 	public void render(float delta) {
 		Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		
 		// We could use event handlers in case of performance drop 
 		setSelectedColor();
 		handleInput();
 		
 		Texture texture = new Texture(drawingArea.getPixmap());
 		texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
 		texture.bind();
 		
 		Texture colortext = new Texture(colorPreview);
 		colortext.bind();
 		
 		cam.update();
 		batch.setProjectionMatrix(cam.combined);
 		batch.begin();
 		batch.draw(texture, OFFSET_X, OFFSET_Y);
 		batch.draw(colortext, 180, 400, 48, 48);
 		batch.end();
 		
 		stage.act(Gdx.graphics.getDeltaTime());
 		stage.draw();
 		
 		texture.dispose();
 		colortext.dispose();
 	}
 
 	@Override
 	public void show() {
 		cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
 		cam.setToOrtho(true, ORIGINAL_WIDTH, ORIGINAL_HEIGHT);																																					
 		drawingArea = new DrawingArea(canvasSize);
 		batch = new SpriteBatch();
 		stage = new Stage(ORIGINAL_WIDTH, ORIGINAL_HEIGHT, true, batch);
 		
 		Gdx.input.setInputProcessor(stage);
 		
 		setupUI(manager.get("data/uiskin.json", Skin.class));
 	
 		Gdx.input.setInputProcessor(stage);
 		// Weird alpha if not called wtf
 		drawingArea.clearArea();
 	}
 
 	@Override
 	public void hide() {
 	}
 
 	private void handleInput() {
 		if (Gdx.input.isTouched()) {
 			int touchx = Gdx.input.getX();
 			int touchy = Gdx.input.getY();
 			
 			touchx = Math.round(touchx * scaleX);
 			touchy = Math.round(touchy * scaleY);
 			
 			touchx -= OFFSET_X;
 			touchy -= OFFSET_Y;
 			
 			drawingArea.normDraw(touchx, touchy);
 			
 			if (online) {
 				int r = 255;
 				int g = 255;
 				int b = 255;
 				
 				if (!drawingArea.isEraseMode()) {
 					r = (int)red_slider.getValue();
 					g = (int)green_slider.getValue();
 					b = (int)blue_slider.getValue();
 				}
 				
 				touchx = (touchx * 256) / 256;
 				touchy = ((256 - touchy) * 256) / 256;
 				
 				this.game.nat.draw(	touchx, 
 									touchy, 
 									r,
 									g,
 									b,
 									brushIndex);
 			}
 			
 			justTouched = true;
 		} else if (justTouched) {
 			 drawingArea.removeLast();
 			 justTouched = false;
 			 if (online) {
 				 this.game.nat.draw(	-1, 
 										-1, 
 										-1, 
 										-1, 
 										-1, 
 										-1);
 			}
 		}
 	}
 
 	@Override
 	public void resize(int width, int height) {
         scaleX = ((float)ORIGINAL_WIDTH) / width;
         scaleY = ((float)ORIGINAL_HEIGHT) / height;
 	}
 	
 	@Override
 	public void pause() {
 	}
 
 	@Override
 	public void resume() {
 		
 		for(Button button : brushButtons) {
 			stage.removeActor(button);
 		}
 		
 		brushButtons.clear();
 		
 		for(BrushButtonDescriptor desc : brushButtonsDesc) {
 			Button tmp = brushButtonFactory(desc.getBrush(), manager.get("data/uiskin.json", Skin.class), desc.getX(), desc.getY(), brushButtonsDesc.indexOf(desc));
 			brushButtons.add(tmp);
 			stage.addActor(tmp);
 		}
 	}
 
 	@Override
 	public void dispose() {
 		drawingArea.dispose();
 		batch.dispose();
 	}
 
 	protected void setupUI(final Skin skin) {
 		Label red_label = new Label(skin);
 		red_label.setText("R");
 		Label green_label = new Label(skin);
 		green_label.setText("G");
 		Label blue_label = new Label(skin);
 		blue_label.setText("B");
 		
 		for(BrushButtonDescriptor desc : brushButtonsDesc) {
 			Button tmp = brushButtonFactory(desc.getBrush(), skin, desc.getX(), desc.getY(), brushButtonsDesc.indexOf(desc));
 			brushButtons.add(tmp);
 			stage.addActor(tmp);
 		}
 		
 		// Setting up initial state, not my best work but hey
		brushIndex = 2;
		activeBrushButton = brushButtons.get(2);
 		activeBrushButton.setStyle(skin.getStyle("checked", ButtonStyle.class));
 
 		red_slider = new Slider(0, 255, 1, skin.getStyle(SliderStyle.class), "slider");
 		green_slider = new Slider(0, 255, 1, skin.getStyle(SliderStyle.class), "slider");
 		blue_slider = new Slider(0, 255, 1, skin.getStyle(SliderStyle.class), "slider");
 		
 		stage.addActor(red_slider);
 		stage.addActor(green_slider);
 		stage.addActor(blue_slider);
 		
 		stage.addActor(red_label);
 		stage.addActor(green_label);
 		stage.addActor(blue_label);
 		
 		red_slider.y = red_y;
 		red_slider.x = red_x;
 		red_label.x = red_x - 15;
 		red_label.y = red_y + 6;
 		
 		green_slider.y = green_y;
 		green_slider.x = green_x;
 		green_label.x = green_x - 15;
 		green_label.y = green_y + 6;
 		
 		blue_slider.y = blue_y;
 		blue_slider.x = blue_x;
 		blue_label.x = blue_x - 15;
 		blue_label.y = blue_y + 6;
 		
 		// Random Initial values
 		red_slider.setValue(192);
 		green_slider.setValue(168);
 		blue_slider.setValue(172);
 		
 		// Maybe it's not necessary to use RGBA?
 		colorPreview = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
 		colorPreviewTexture = new Texture(colorPreview);
 		colorPreviewTexture.bind();
 
 		Label drawThis = new Label(manager.get("data/uiskin.json", Skin.class));
 		drawThis.setText("Draw..." + word);
 		drawThis.x = (ORIGINAL_WIDTH / 2) - (drawThis.getTextBounds().width / 2);
 		drawThis.y = ORIGINAL_HEIGHT - OFFSET_Y + 5;
 		
 		stage.addActor(drawThis);
 		
 		Label currentColor = new Label("Color", manager.get("data/uiskin.json", Skin.class));
 		currentColor.y = red_slider.y;
 		currentColor.x = 180;
 		
 		stage.addActor(currentColor);
 		
 		// Brush, eraser, clear buttons
 		final Button brush_button = new Button(new Image(manager.get("draw-brush.png", Texture.class), Scaling.fill), manager.get("data/uiskin.json", Skin.class));
 		final Button erase_button = new Button(new Image(manager.get("draw-eraser-2.png", Texture.class), Scaling.fill), manager.get("data/uiskin.json", Skin.class));
 		Button clear_button = new Button(new Image(manager.get("edit-clear-2.png", Texture.class), Scaling.fill), manager.get("data/uiskin.json", Skin.class));
 		Button menu_button = new Button(new Image(manager.get("format-list-unordered.png", Texture.class), Scaling.fill), manager.get("data/uiskin.json", Skin.class));
 		
 		brush_button.setClickListener(new ClickListener() {
 			
 			@Override
 			public void click(Actor actor, float x, float y) {
 				drawingArea.setDrawMode();
 				// Toggle the state (same on the other button's method)
 				erase_button.setStyle(skin.getStyle("unchecked", ButtonStyle.class));
 				brush_button.setStyle(skin.getStyle("checked", ButtonStyle.class));
 			}
 		});
 		
 		erase_button.setClickListener(new ClickListener() {
 			
 			@Override
 			public void click(Actor actor, float x, float y) {
 				drawingArea.setEraseMode();
 				brush_button.setStyle(skin.getStyle("unchecked", ButtonStyle.class));
 				erase_button.setStyle(skin.getStyle("checked", ButtonStyle.class));
 			}
 		});
 		
 		clear_button.setClickListener(new ClickListener() {
 			
 			@Override
 			public void click(Actor actor, float x, float y) {
 				drawingArea.clearArea();
 			}
 		});
 		
 		menu_button.setClickListener(new ClickListener() {
 			
 			@Override
 			public void click(Actor actor, float x, float y) {
 				if (online) {
 					game.nat.endGame("Server closed the game");
 				}
 				game.setMenuScreen();
 			}
 		});
 		
 		// Initial style state
 		brush_button.click(0, 0);
 		
 		Table buttonTable = new Table(manager.get("data/uiskin.json", Skin.class));
 		buttonTable.row();
 		buttonTable.add(brush_button).pad(5);
 		buttonTable.add(erase_button).pad(5);
 		buttonTable.row();
 		buttonTable.add(clear_button).pad(5);
 		buttonTable.add(menu_button).pad(5);
 
 		buttonTable.x = ORIGINAL_WIDTH - (clear_button.width * 2);
 		buttonTable.y = red_slider.y - 5;
 		
 		stage.addActor(buttonTable);
 	}
 
 	// Overloading is cool
 	protected void setSelectedColor() {
 		setSelectedColor(new Color(red_slider.getValue() / 255.0f, green_slider.getValue()  / 255.0f, blue_slider.getValue() / 255.0f, 1.0f));
 	}
 	
 	protected void setSelectedColor(Color color) {
 		colorPreview.setColor(color);
 		colorPreview.fill();
 		drawingArea.setColor(color);
 	}
 	
 	/**
 	 * Auxiliary function that generates a Button from a Brush
 	 * @author Román Jiménez
 	 * @param brush {@link Brush} to generate {@link Button} from
 	 * @param skin Skinfile to use
 	 * @param x Position in horizontal axis
 	 * @param y Position in vertical axis
 	 * @return {@link Button} representation of the {@link Brush}
 	 */
 	private Button brushButtonFactory(final Brush brush, final Skin skin, int x, int y, final int index) {
 		Texture text = new Texture(brush.getPixmap());
 		text.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
 		text.bind();
 		Image image = new Image(text, Scaling.fill);
 		final Button button = new Button(image, skin.getStyle(ButtonStyle.class));
 		button.setClickListener(new ClickListener() {
 			@Override
 			public void click(Actor actor, float x, float y) {
 				drawingArea.setBrush(brush);
 				if (!activeBrushButton.equals(button)) {
 					button.setStyle(skin.getStyle("checked", ButtonStyle.class));
 					activeBrushButton.setStyle(skin.getStyle("unchecked", ButtonStyle.class));
 					activeBrushButton = button;
 					brushIndex = index;
 				}
 			}
 		});
 		
 		button.x = x;
 		button.y = y;
 		
 		return button;
 	}
 
 }
