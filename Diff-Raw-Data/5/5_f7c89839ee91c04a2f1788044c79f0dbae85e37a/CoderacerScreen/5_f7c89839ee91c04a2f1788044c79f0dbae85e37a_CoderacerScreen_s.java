 package no.hist.gruppe5.pvu.coderacer;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
 import com.badlogic.gdx.scenes.scene2d.utils.Align;
 import com.badlogic.gdx.utils.Timer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import no.hist.gruppe5.pvu.Assets;
 import no.hist.gruppe5.pvu.GameScreen;
 import no.hist.gruppe5.pvu.PVU;
 import no.hist.gruppe5.pvu.ScoreHandler;
 
 /**
  * Created with IntelliJ IDEA. User: karl Date: 8/28/13 Time: 10:49 AM
  */
 public class CoderacerScreen extends GameScreen {
 
     private Label finishedCode;
     private Label codeOutput;
     private Label grade;
     private Label time;
     private Code code = new Code();
     private Stage stage;
     private int remainingTime = 30;
     private int score = 0;
     private boolean start;
     private boolean pause;
     private Timer.Task task = new Timer.Task() {
 
         @Override
         public void run() {
             remainingTime--;
         }
     };
 
     public CoderacerScreen(PVU game) {
         super(game);
         
         pause = false;
         start = false;
 
         stage = new Stage(PVU.SCREEN_WIDTH, PVU.SCREEN_HEIGHT, true, batch);
 
         Group outputGroup = new Group();
         Group inputGroup = new Group();
 
         LabelStyle outputStyle = new LabelStyle(Assets.primaryFont10px, Color.BLACK);
         codeOutput = new Label("Du har 1 minutt på å skrive så mye av koden som mulig.\nTrykk space for å begynne.", outputStyle);
         codeOutput.setFontScale(3f);
         codeOutput.setFillParent(true);
         codeOutput.setWrap(true);
        codeOutput.setAlignment(Align.top);
 
         LabelStyle finishedStyle = new LabelStyle(Assets.primaryFont10px, Color.RED);
         finishedCode = new Label("", finishedStyle);
         finishedCode.setFontScale(3f);
         finishedCode.setFillParent(true);
         finishedCode.setWrap(true);
         finishedCode.setAlignment(Align.bottom);
 
         grade = new Label("", finishedStyle);
         grade.setFontScale(2.5f);
 
         time = new Label("" + remainingTime, finishedStyle);
         time.setFontScale(3f);
 
         outputGroup.addActor(codeOutput);
         outputGroup.setWidth(120);
         outputGroup.setHeight(40);
        outputGroup.setPosition(PVU.SCREEN_WIDTH / 2 - outputGroup.getWidth() / 2, 450);
 
         inputGroup.addActor(finishedCode);
         inputGroup.setWidth(120);
         inputGroup.setHeight(40);
         inputGroup.setPosition(PVU.SCREEN_WIDTH / 2 - inputGroup.getWidth() / 2, 190);
 
         stage.addActor(grade);
         stage.addActor(outputGroup);
         stage.addActor(inputGroup);
         stage.addActor(time);
 
         time.setPosition(PVU.SCREEN_WIDTH * 0.9f, PVU.SCREEN_HEIGHT * 0.1f);
 
         Gdx.input.setInputProcessor(new inputListener());
     }
 
     @Override
     protected void draw(float delta) {
         clearCamera(1, 1, 1, 1); // Important
         if (start) {
             stage.getSpriteBatch().begin();
             batch.draw(Assets.msPcBackground, 0, 0);
             stage.getSpriteBatch().end();
             stage.draw();
         } else {
             stage.getSpriteBatch().begin();
             batch.draw(Assets.msPcBackground, 0, 0);
             stage.getSpriteBatch().end();
             stage.draw();
             if (Gdx.input.isKeyPressed(Keys.SPACE)) {
                 codeOutput.setText(code.getCode());
                 start = true;
                 Timer.schedule(task, 1f, 1f);
             }
         }
     }
 
     @Override
     protected void update(float delta) {
         if (code.isFinished() || remainingTime <= 0) {
             codeOutput.setVisible(false);
             finishedCode.setVisible(false);
             grade.setText("Din score ble " + score + "\nTrykk space for å avslutte.");
             ScoreHandler.updateScore(ScoreHandler.CODE, code.getGrade(score));
             grade.setPosition(PVU.SCREEN_WIDTH / 2f - grade.getPrefWidth() / 2f, PVU.SCREEN_HEIGHT * 0.6f);
             if (Gdx.input.isKeyPressed(Keys.SPACE)) {
                 game.setScreen(PVU.MAIN_SCREEN);
             }
         } else {
             time.setText(remainingTime + "");
         }
 
         if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
             pause = true;
             game.setScreen(PVU.MAIN_SCREEN);
         }
     }
 
     @Override
     protected void cleanUp() {
     }
 
     private class inputListener implements InputProcessor {
 
         @Override
         public boolean keyDown(int keycode) {
             return false;
         }
 
         @Override
         public boolean keyUp(int keycode) {
             return false;
         }
 
         @Override
         public boolean keyTyped(char character) {
             if (!code.isFinished()) {
                 if (character > 31) {
                     if (code.equals(character)) {
                         score++;
                         if (!code.isFinished()) {
                             updateOutput();
                         }
                     }
                 }
                 return true;
             }
             return false;
         }
 
         @Override
         public boolean touchDown(int x, int y, int pointer, int button) {
             return false;
         }
 
         @Override
         public boolean touchUp(int x, int y, int pointer, int button) {
             return false;
         }
 
         @Override
         public boolean touchDragged(int x, int y, int pointer) {
             return false;
         }
 
         @Override
         public boolean scrolled(int amount) {
             return false;
         }
 
         @Override
         public boolean mouseMoved(int screenX, int screenY) {
             return false;
         }
     }
 
     private void updateOutput() {
         codeOutput.setText(code.getLeft());
         finishedCode.setText(code.getCorrect());
     }
     
     
 }
