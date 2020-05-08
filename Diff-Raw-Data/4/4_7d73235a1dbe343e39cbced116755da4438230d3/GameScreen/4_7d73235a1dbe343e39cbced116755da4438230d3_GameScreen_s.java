 package no.hist.gruppe5.pvu;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
 import com.badlogic.gdx.utils.TimeUtils;
 import no.hist.gruppe5.pvu.book.BookScreen;
 import no.hist.gruppe5.pvu.mainroom.MainScreen;
 
 /**
  * Created with IntelliJ IDEA. User: karl Date: 8/28/13 Time: 9:48 AM
  */
 public abstract class GameScreen implements Screen {
 
     protected PVU game;
     protected SpriteBatch batch;
     protected OrthographicCamera camera;
     private Stage stage;
     private static long lastAction;
     private Skin skinPauseButton;
     private TextureAtlas atlas;
     private boolean running;
     private LabelStyle labelStyle;
     private Label pauseLabel;
 
     private TextButton[] pauseButtons;
     private int selectedButton;
     private TextButtonStyle selectedButtonStyle;
     private TextButtonStyle buttonStyle;
 
     private final long WAIT_KEY_PRESSED = 500l;
 
     public GameScreen(PVU game) {
         this.game = game;
         labelStyle = new Label.LabelStyle(Assets.primaryFont10px, Color.BLACK);
 
         camera = new OrthographicCamera();
         camera.setToOrtho(false, PVU.GAME_WIDTH, PVU.GAME_HEIGHT);
         batch = new SpriteBatch();
 
         lastAction = 0;
         running = true;
         stage = new Stage(PVU.GAME_WIDTH * 2.7f, PVU.GAME_HEIGHT * 2.7f, true);
         atlas = new TextureAtlas("data/menuButtons/menubuttons.pack");
         initPauseLayout();
         Gdx.input.setInputProcessor(stage);
     }
 
     protected abstract void draw(float delta);
 
     protected abstract void update(float delta);
 
     protected abstract void cleanUp();
 
     protected void clearCamera(float r, float g, float b, float a) {
         camera.update();
         camera.apply(Gdx.gl10);
         Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
         Gdx.gl.glClearColor(r, g, b, a);
         batch.setProjectionMatrix(camera.combined);
     }
 
     @Override
     public void resize(int width, int height) {
     }
 
     @Override
     public void render(float delta) {
         if (running) {
             float deltaUpdate = (delta > 0.1f) ? 0.1f : delta;
             update(deltaUpdate);
         }
         draw(delta);
         if (!running) {
             drawPauseMenu();
             checkPauseMenu();
         }
         checkEscapeButton();
         stage.draw();
     }
 
     private void checkEscapeButton() {
         if (TimeUtils.millis() - lastAction > WAIT_KEY_PRESSED && Gdx.input.isKeyPressed(Input.Keys.ANY_KEY)) {
             if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
                if (this instanceof BookScreen) {
                     game.setScreen(PVU.MAIN_SCREEN);
                 } else {
                     pauseButtons[selectedButton].setStyle(buttonStyle);
                     selectedButton = 0;
                     pauseButtons[selectedButton].setStyle(selectedButtonStyle);
                     running = false;
                 }
             }
             lastAction = TimeUtils.millis();
         }
     }
 
     private void checkPauseMenu() {
         if (TimeUtils.millis() - lastAction > WAIT_KEY_PRESSED && Gdx.input.isKeyPressed(Input.Keys.ANY_KEY)) {
             if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
                 running = true;
                 stage.clear();
             } else if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {
                 pauseButtons[selectedButton].setStyle(buttonStyle);
                 selectedButton = (selectedButton == 0) ? 0 : selectedButton - 1;
                 pauseButtons[selectedButton].setStyle(selectedButtonStyle);
             } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
                 pauseButtons[selectedButton].setStyle(buttonStyle);
                 selectedButton = (selectedButton == pauseButtons.length - 1) ? pauseButtons.length - 1 : selectedButton + 1;
                 pauseButtons[selectedButton].setStyle(selectedButtonStyle);
             } else if (Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
                 checkSelectedMenuItem();
             }
             lastAction = TimeUtils.millis();
         }
     }
 
     private void checkSelectedMenuItem() {
         if (selectedButton == Button.RESUME.pos) {
             stage.clear();
             running = true;
         } else if (selectedButton == Button.SOUND.pos) {
             if (Settings.GLOBAL_SOUND) {
                 pauseButtons[selectedButton].setText("Sound off");
                 Settings.setSound(false);
             } else {
                 pauseButtons[selectedButton].setText("Sound on");
                 Settings.setSound(true);
             }
         } else if (selectedButton == Button.BACK.pos) {
             if (this instanceof MainScreen) {
                 stage.clear();
                 running = true;
             } else {
                 game.setScreen(PVU.MAIN_SCREEN);
             }
         } else if (selectedButton == Button.EXIT.pos) {
             System.exit(0);
         }
     }
 
     private void drawPauseMenu() {
         clearCamera(1, 1, 1, 1);
         batch.begin();
         batch.draw(Assets.introMainLogo, PVU.GAME_WIDTH / 3, PVU.GAME_HEIGHT / 2, PVU.GAME_WIDTH / 3, PVU.GAME_HEIGHT / 3);
         batch.end();
         for (TextButton button : pauseButtons) {
             stage.addActor(button);
         }
     }
 
     private void initPauseLayout() {
         buttonStyle = new TextButtonStyle();
         selectedButtonStyle = new TextButtonStyle();
         skinPauseButton = new Skin(atlas);
         buttonStyle.up = skinPauseButton.getDrawable("menubutton.up");
         buttonStyle.down = skinPauseButton.getDrawable("menubutton.down");
         buttonStyle.font = Assets.primaryFont10px;
         selectedButtonStyle.up = skinPauseButton.getDrawable("menubutton.down");
         selectedButtonStyle.down = skinPauseButton.getDrawable("menubutton.up");
         selectedButtonStyle.font = Assets.primaryFont10px;
         pauseLabel = new Label("PAUSE", labelStyle);
         pauseLabel.setFontScale(1.9f);
         pauseLabel.setPosition(PVU.GAME_WIDTH * 1.225f, PVU.GAME_HEIGHT);
 
         String[] pauseButtonText = {"Resume", "Sound on", "Back", "Exit"};
         pauseButtons = new TextButton[pauseButtonText.length];
 
         for (int i = 0; i < pauseButtonText.length; i++) {
             pauseButtons[i] = new TextButton(pauseButtonText[i], buttonStyle);
             pauseButtons[i].setPosition(PVU.GAME_WIDTH * 1.24f, stage.getHeight() / 3 - (i * 20));
         }
     }
 
     @Override
     public void dispose() {
         cleanUp();
         batch.dispose();
     }
 
     private void resumeGame() {
         running = true;
     }
 
     @Override
     public void show() {
     }
 
     @Override
     public void hide() {
     }
 
     @Override
     public void pause() {
     }
 
     @Override
     public void resume() {
     }
 
     /**
      * Method to update soundbutton in (static) main screen room.
      */
     public void updateMainScreenSoundButton() {
         //styleSoundButton.up = (Settings.GLOBAL_SOUND) ? skinSoundButton.getDrawable("sound.up") : skinSoundButton.getDrawable("nosound.up");
     }
 
     public boolean isGamePaused() {
         return !running;
     }
 
     public long getTime() {
         return lastAction;
     }
 
     private enum Button {
 
         RESUME(0), SOUND(1), BACK(2), EXIT(3);
 
         private int pos;
 
         private Button(int c) {
             pos = c;
         }
 
         public int pos() {
             return pos;
         }
     }
 }
