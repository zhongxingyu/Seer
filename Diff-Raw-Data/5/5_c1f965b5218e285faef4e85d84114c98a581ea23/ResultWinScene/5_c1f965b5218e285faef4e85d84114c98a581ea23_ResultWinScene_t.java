 /*
  * Ninja Trials is an old school style Android Game developed for OUYA & using
  * AndEngine. It features several minigames with simple gameplay.
  * Copyright 2013 Mad Gear Games <madgeargames@gmail.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.madgear.ninjatrials;
 
 import org.andengine.engine.handler.IUpdateHandler;
 import org.andengine.engine.handler.timer.ITimerCallback;
 import org.andengine.engine.handler.timer.TimerHandler;
 import org.andengine.entity.Entity;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.scene.background.SpriteBackground;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.sprite.TiledSprite;
 import org.andengine.entity.text.Text;
 import org.andengine.entity.text.TextOptions;
 import org.andengine.util.adt.align.HorizontalAlign;
 
 import android.util.Log;
 
 import com.madgear.ninjatrials.managers.GameManager;
 import com.madgear.ninjatrials.managers.ResourceManager;
 import com.madgear.ninjatrials.managers.SceneManager;
 import com.madgear.ninjatrials.test.TestingScene;
 import com.madgear.ninjatrials.trials.TrialSceneCut;
 import com.madgear.ninjatrials.trials.TrialSceneJump;
 import com.madgear.ninjatrials.trials.TrialSceneRun;
 import com.madgear.ninjatrials.trials.TrialSceneShuriken;
 
 /**
  * This class shows the trial results, and adds the trial score to the total game score.
  * @author Madgear Games
  *
  */
 public class ResultWinScene extends GameScene {
     public final static int STAMP_THUG = 0;
     public final static int STAMP_NINJA = 1;
     public final static int STAMP_NINJA_MASTER = 2;
     public final static int STAMP_GRAND_MASTER = 3;
     
     private final static float WIDTH = ResourceManager.getInstance().cameraWidth;
     private final static float HEIGHT = ResourceManager.getInstance().cameraHeight;
     private static final int MAX_SCORE_ITEMS = 5;
     private static final float POS_X_LEFT_SCORE = 600f;
     private static final float SCORE_ROW_HEIGHT = HEIGHT - 380;
     protected static final float SCORE_ROW_GAP = 80;
 
     private Text tittleText;
     private String tittle;
     private SpriteBackground bg;
     private Sprite characterSprite;
     private Sprite scroll;
     private TiledSprite drawings;
     private TiledSprite stamp;
     private boolean pressEnabled = true;
     private GrowingScore growingScore;
     private int scoreItemsNumber;
     private int scoreItemArrayIndex;
     private ScoreItem[] scoreItemArray;
     private TimerHandler timerHandler;
     private IUpdateHandler updateHandler;
     private int drawingIndex;
 
     
     public ResultWinScene() {
         super(0f);  // 0 = no loading screen.
     }
 
     @Override
     public Scene onLoadingScreenLoadAndShown() {
         return null;
     }
 
     @Override
     public void onLoadingScreenUnloadAndHidden() {}
 
     @SuppressWarnings("static-access")
     @Override
     public void onLoadScene() {
         ResourceManager.getInstance().loadResultWinResources();
     }
 
     @SuppressWarnings("static-access")
     @Override
     public void onShowScene() {
 
         if (GameManager.DEBUG_MODE == true)
             loadTestData();
 
         // Background:
         bg = new SpriteBackground(new Sprite(WIDTH/2, HEIGHT/2,
                 ResourceManager.getInstance().winBg,
                 ResourceManager.getInstance().engine.getVertexBufferObjectManager()));
         setBackground(bg);
         
         // Scroll:
         scroll = new Sprite(WIDTH/2, HEIGHT/2,
                 ResourceManager.getInstance().winScroll,
                 ResourceManager.getInstance().engine.getVertexBufferObjectManager());
         attachChild(scroll);
 
         // Drawings:
         drawings = new TiledSprite(WIDTH/2, HEIGHT/2,
                 ResourceManager.getInstance().winDrawings,
                 ResourceManager.getInstance().engine.getVertexBufferObjectManager());
         drawings.setVisible(false);
         attachChild(drawings);
         
         // Stamp:
         stamp = new TiledSprite(750, HEIGHT - 850,
                 ResourceManager.getInstance().winStampRanking,
                 ResourceManager.getInstance().engine.getVertexBufferObjectManager());
         stamp.setVisible(false);
         attachChild(stamp);
         
         // Trial Name:
         tittleText = new Text(WIDTH/2, HEIGHT - 200,
                 ResourceManager.getInstance().fontBig, "TRIAL NAME PLACE HOLDER",
                 new TextOptions(HorizontalAlign.CENTER),
                 ResourceManager.getInstance().engine.getVertexBufferObjectManager());
         tittleText.setVisible(false);
         attachChild(tittleText);
 
         // Character:
         if(GameManager.getSelectedCharacter() ==
                 GameManager.CHAR_SHO) {
             characterSprite = new Sprite(300, HEIGHT - 690,
                     ResourceManager.getInstance().winCharSho,
                     ResourceManager.getInstance().engine.getVertexBufferObjectManager());
         }
         else {
             characterSprite = new Sprite(300, HEIGHT - 690,
                     ResourceManager.getInstance().winCharRyoko,
                     ResourceManager.getInstance().engine.getVertexBufferObjectManager());
         }
         attachChild(characterSprite);
         
         // Total Score:
         growingScore = new GrowingScore(POS_X_LEFT_SCORE, HEIGHT - 670, 0);
         attachChild(growingScore);
         
         prepareResults();
     }   
     
     @Override
     public void onHideScene() {}
 
     @SuppressWarnings("static-access")
     @Override
     public void onUnloadScene() {
         ResourceManager.getInstance().unloadResultWinResources();
     }
     
     
     // -------------------------------------------------------
 
     /**
      * Loads the testing data in debug mode.
      */
     private void loadTestData() {
         GameManager.setCurrentTrial(GameManager.TRIAL_CUT);
         GameManager.player1result.cutConcentration = 99;
         GameManager.player1result.cutRound = 3;
     }
 
     /**
      * Prepare the results data before showing them.
      * We must keep control for the drawing in the scroll, trial name, type of stamp and number
      * of rows for score items.
      */
     private void prepareResults() {
         // Score Items:
         // We store a row for each score line in the results (for example time, concentration...).
         scoreItemArray = new ScoreItem[MAX_SCORE_ITEMS];
 
         switch(GameManager.getCurrentTrial()) {
         case GameManager.TRIAL_RUN:
             tittleText.setText("Run Results");
             drawings.setCurrentTileIndex(2);
             stamp.setCurrentTileIndex(TrialSceneRun.getStamp(TrialSceneRun.getScore()));
             scoreItemsNumber = 4;
             scoreItemArray[0] = new ScoreItem("Time",
                     String.valueOf(GameManager.player1result.runTime),
                     TrialSceneRun.getTimeScore());
             scoreItemArray[1] = new ScoreItem("Max Speed Combo",
                     String.valueOf(GameManager.player1result.runMaxSpeedCombo),
                     TrialSceneRun.getMaxSpeedComboScore());
             scoreItemArray[2] = new ScoreItem("Max Speed Combo Total",
                     String.valueOf(GameManager.player1result.runMaxSpeedComboTotal),
                     TrialSceneRun.getMaxSpeedComboTotalScore());
             scoreItemArray[3] = new ScoreItem("Max Speed",
                     String.valueOf(GameManager.player1result.runMaxSpeed),
                     TrialSceneRun.getMaxSpeedScore());
             break;
 
         case GameManager.TRIAL_CUT:
             tittleText.setText("Cut Results");
             drawings.setCurrentTileIndex(3);
             stamp.setCurrentTileIndex(TrialSceneCut.getStamp(TrialSceneCut.getScore()));
             scoreItemsNumber = 2;
             scoreItemArray[0] = new ScoreItem("Rounds",
                     String.valueOf(GameManager.player1result.cutRound),
                     TrialSceneCut.getRoundScore());
             scoreItemArray[1] = new ScoreItem("Concentratation",
                     String.valueOf(GameManager.player1result.cutConcentration),
                     TrialSceneCut.getConcentrationScore());
             break;
             
         case GameManager.TRIAL_JUMP:
             tittleText.setText("Jump Results");
             drawings.setCurrentTileIndex(0);
             stamp.setCurrentTileIndex(TrialSceneJump.getStamp(TrialSceneJump.getScore()));
             scoreItemsNumber = 3;
             scoreItemArray[0] = new ScoreItem("Time",
                     String.valueOf(GameManager.player1result.jumpTime),
                     TrialSceneJump.getTimeScore());
             scoreItemArray[1] = new ScoreItem("Perfect Jump Combo",
                     String.valueOf(GameManager.player1result.jumpPerfectJumpCombo),
                     TrialSceneJump.getPerfectJumpScore());
             scoreItemArray[2] = new ScoreItem("Max Perfect Jump Combo",
                     String.valueOf(GameManager.player1result.jumpMaxPerfectJumpCombo),
                     TrialSceneJump.getMaxPerfectJumpScore());
             break;
             
         case GameManager.TRIAL_SHURIKEN:
             tittleText.setText("Shuriken Results");
             drawings.setCurrentTileIndex(1);
             stamp.setCurrentTileIndex(TrialSceneShuriken.getStamp(TrialSceneShuriken.getScore()));
             scoreItemsNumber = 2;
            scoreItemArray[0] = new ScoreItem("Time",
                     String.valueOf(GameManager.player1result.shurikenTime),
                     TrialSceneShuriken.getTimeScore());
            scoreItemArray[1] = new ScoreItem("Precission",
                     String.valueOf(GameManager.player1result.shurikenPrecission),
                     TrialSceneShuriken.getPrecissionScore());
             break;
         }
     }
 
     /**
      * Show one score item each time, and the growing score.
      * Finally adds the score to the main game score.
      */
     private void showResults() {
         drawings.setVisible(true);
         tittleText.setVisible(true);
         stamp.setVisible(true);
         scoreItemArrayIndex = 0;        
         
         updateHandler = new IUpdateHandler() {
             @Override
             public void onUpdate(float pSecondsElapsed) {
                 if(growingScore.isFinished())
                     if(scoreItemArrayIndex < scoreItemsNumber) {
                         growingScore.addScore(scoreItemArray[scoreItemArrayIndex].addedPoints);
                         addScoreLine(SCORE_ROW_HEIGHT - SCORE_ROW_GAP * scoreItemArrayIndex,
                             scoreItemArray[scoreItemArrayIndex].description,
                             scoreItemArray[scoreItemArrayIndex].value);
                         scoreItemArrayIndex++;                    
                     }
                     else {
                         // No more rows to show.
                         ResultWinScene.this.unregisterUpdateHandler(updateHandler);
                     }
             }
             @Override public void reset() {}
         };
         registerUpdateHandler(updateHandler);
 
         // Add the trial score to the total score:
         GameManager.incrementScore(TrialSceneCut.getScore());
     }
 
     /**
      * If it is the final trial then go to the ending scene. If there are more trials to complete
      * then go to the map scene (and set the current trial to the next one).
      */
     private void endingSequence() {
         if(GameManager.getCurrentTrial() == GameManager.TRIAL_FINAL)
             // TODO:  SceneManager.getInstance().showScene(new EndingScene());
             SceneManager.getInstance().showScene(new TestingScene());
         else
             // TODO:  SceneManager.getInstance().showScene(new MapScene());
             GameManager.setCurrentTrial(GameManager.nextTrial(GameManager.getCurrentTrial()));
             SceneManager.getInstance().showScene(new TestingScene());
     }
 
     /**
      * Add a new score row to the scroll.
      * @param y The Y position of the score line.
      * @param description The score item description, like "Jump Time", "Shuriken Precission", etc.
      * @param value The item value like "34 sec", or "67%".
      */
     private void addScoreLine(float y, String description, String value) {
         Text descriptionText = new Text(WIDTH/2, y,
                 ResourceManager.getInstance().fontSmall,
                 description,
                 new TextOptions(HorizontalAlign.CENTER),
                 ResourceManager.getInstance().engine.getVertexBufferObjectManager());
         attachChild(descriptionText);
         
         Text valueText = new Text(POS_X_LEFT_SCORE, y,
                 ResourceManager.getInstance().fontSmall,
                 value,
                 new TextOptions(HorizontalAlign.CENTER),
                 ResourceManager.getInstance().engine.getVertexBufferObjectManager());
         attachChild(valueText);        
     }
 
 
     // INTERFACE --------------------------------------------------------
 
     /**
      * If the score hasn't finished, then finalize it now, else go to the end sequence.
      */
     @Override
     public void onPressButtonO() {
         if(pressEnabled) {
             if(growingScore.isFinished())
                 endingSequence();
             else
                 growingScore.finalize();
         }
     }
 
 
     // AUXILIARY CLASSES -------------------------------------------------
 
     /**
      * This class represents a score number that can grow along time and shows it in the screen.
      * @author Madgear Games
      */
     private class GrowingScore extends Entity {
         private int startingScore;
         private int currentScore;
         private int finalScore;
         private Text scoreText;
         private final static int POINTS_PER_SECOND = 1000;
         private boolean sumFinished = true;
 
         /**
          * Create the text item and initializes the class fields.
          * @param x The x position in the screen.
          * @param y The y position in the screen.
          * @param s Starting score (normally 0).
          */
         public GrowingScore(float x, float y, int s) {
             startingScore = s;
             currentScore = startingScore;
             
             scoreText = new Text(x, y,
                     ResourceManager.getInstance().fontMedium,
                     "GROWING SCORE PLACE HOLDER",
                     new TextOptions(HorizontalAlign.CENTER),
                     ResourceManager.getInstance().engine.getVertexBufferObjectManager());
             scoreText.setText(Integer.toString(startingScore));
             attachChild(scoreText);
             setIgnoreUpdate(true);
         }
 
         /**
          * Adds points to the score. Tells the class than can be updated.
          * @param points Number of points to add to the score.
          */
         public void addScore(int points) {
             sumFinished = false;
             finalScore = (int)currentScore + points;
             setIgnoreUpdate(false);
         }
 
         /**
          * Tell the class to end the update and set the score to the final value.
          */
         public void finalize() {
             currentScore = finalScore;
             scoreText.setText(Integer.toString(currentScore));
             sumFinished = true;
             setIgnoreUpdate(true);
         }
 
         /**
          * Update the score and show it on the screen while it is growing.
          * If it reach the final value then stops.
          */
         @Override
         protected void onManagedUpdate(final float pSecondsElapsed) {
             if(currentScore < finalScore) {
                 scoreText.setText(Integer.toString(currentScore));
                 currentScore = Math.round(currentScore + pSecondsElapsed * POINTS_PER_SECOND);
             }
             else {
                 finalize();
             }
             super.onManagedUpdate(pSecondsElapsed);
         }
 
         /**
          * Tells us if all the points has been added.
          * @return False if the score is growing yet, else return true.
          */
         public boolean isFinished() {
             return sumFinished;
         }
     }
 
 
     /**
      * Keeps data about a score row. The scene uses an array of them to store the information
      * about trial results.
      * Example: in the cut scene, the score item "concentraton" whould be:
      *  descripton = "Concentration"
      *  value = "89%"
      *  addedPoints = 8455
      *  
      * @author Madgear Games
      */
     private class ScoreItem {
         String description, value;
         int addedPoints;
         
         public ScoreItem(String d, String v, int p) {
             description = d;
             value = v;
             addedPoints = p;
         }
     }
 }
