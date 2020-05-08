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
 
 
 package com.madgear.ninjatrials.managers;
 
 import com.madgear.ninjatrials.ResultTrial;
 import com.madgear.ninjatrials.achievements.AchievementSetNinjaTrial;
 import com.madgear.ninjatrials.records.RecordsTableSet;
 
 
 public class GameManager {
 
     //private static GameManager INSTANCE;
 
     // GENERAL:
     // Use DEBUG_MODE = true for show the testing scene.
    public static final boolean DEBUG_MODE = true;
 
     // Use OUYA_CONTROL = true for testing Ouya controls. Use OUYA_CONTROL = false for testing on smartphones
     public static final boolean OUYA_CONTROL = false;
 
     private static final int SCORE_INIT = 0;
     public static final int LIVES_INIT = 3;
 
     public static final int CHAR_RYOKO = 0;
     public static final int CHAR_SHO = 1;
     
     public static final int PLAYER_LEFT = 0;
     public static final int PLAYER_RIGHT = 1;
     
     public static final int DIFF_EASY = 0;
     public static final int DIFF_MEDIUM = 1;
     public static final int DIFF_HARD = 2;
    
     public static final int TRIAL_RUN = 1;
     public static final int TRIAL_CUT = 2;
     public static final int TRIAL_JUMP = 3;
     public static final int TRIAL_SHURIKEN = 4;
     public static final int TRIAL_FINAL = TRIAL_SHURIKEN;
     public static final int TRIAL_START = TRIAL_RUN;
 
     public static final int SEQUENCE_SKIP_DIRECTLY = 0;
     public static final int SEQUENCE_ASK_BEFORE_SKIP = 1;
     public static final int SEQUENCE_CANNOT_SKIP = 2;
 
 
     public static int sequenceBehaviourAtPress = SEQUENCE_SKIP_DIRECTLY;
 
     public static ResultTrial player1result;
     public static ResultTrial player2result;
 
     public static AchievementSetNinjaTrial player1achiev;
     public static AchievementSetNinjaTrial player2achiev;
 
     public static RecordsTableSet recordsTableSet;
     
     private static int score;
     private static int currentTrial;
     private static int lives;
     private static int selectedCharacter;
     private static int selectedDiff;
     private static String gameLanguage;
 
 
 
 
 /*    // Contructor:
     GameManager(){
     }
 
     public static GameManager getInstance(){
         if(INSTANCE == null){
             INSTANCE = new GameManager();
         }
         return INSTANCE;
     }*/
 
     
     // Métodos:
 
     public static void resetGame(){
         score = SCORE_INIT;
         lives = LIVES_INIT;
         currentTrial = TRIAL_START;
         selectedCharacter = CHAR_SHO;
         selectedDiff = DIFF_MEDIUM;
         gameLanguage = "en";
         player1result = new ResultTrial();
         player2result = new ResultTrial();
         player1achiev = new AchievementSetNinjaTrial();
         player2achiev = new AchievementSetNinjaTrial();
         recordsTableSet = new RecordsTableSet();
     }
     
     /**
      * Called when press PLAY in the main Menu Scene.
      */
     public static void newGame() {
         score = SCORE_INIT;
         lives = LIVES_INIT;
         currentTrial = TRIAL_START;
         player1result = new ResultTrial();
         player2result = new ResultTrial();
     }
 
     public static void setSelectedCharacter(int c) {
         selectedCharacter = c;
     }
     
     public static int getSelectedCharacter() {
         return selectedCharacter;
     }
 
     public static void setSelectedDiff(int d) {
         selectedDiff = d;
     }
     
     public static int getSelectedDiff() {
         return selectedDiff;
     }
 
     public static int getCurrentScore(){
         return score;
     }
 
     public static String getGameLanguage() {
         return gameLanguage;
     }
 
     public static void incrementScore(int pIncrementBy){
         score += pIncrementBy;
     }
     
     public static void resetScore() {
         score = 0;
     }
 
     public static int getCurrentTrial() {
         return currentTrial;
     }
     
     public static void setCurrentTrial(int t) {
         currentTrial = t;
     }
     
     public static int nextTrial(int currentTrial) {
        // TODO: fix trial order:
        int nextTrial = TRIAL_RUN;
        switch(currentTrial) {
        case TRIAL_RUN: nextTrial = TRIAL_CUT; break;
        //case TRIAL_CUT: nextTrial = TRIAL_JUMP; break;
        case TRIAL_CUT: nextTrial = TRIAL_SHURIKEN; break;
        case TRIAL_JUMP: nextTrial = TRIAL_SHURIKEN; break;
        case TRIAL_SHURIKEN: nextTrial = TRIAL_RUN; break;
        }
        return nextTrial;
     }
     
     public static int previousTrial(int currentTrial) {
        // TODO: fix trial order:
        int previousTrial = TRIAL_SHURIKEN;
        switch(currentTrial) {
        case TRIAL_SHURIKEN: previousTrial = TRIAL_JUMP; break;
        case TRIAL_JUMP: previousTrial = TRIAL_CUT; break;
        case TRIAL_CUT: previousTrial = TRIAL_RUN; break;
        case TRIAL_RUN: previousTrial = TRIAL_SHURIKEN; break;
        }
        return previousTrial;
     }
 
     public static int getLives() {
         return lives;
     }
 
     public static void setLives(int t) {
         lives = t;
     }
 }
