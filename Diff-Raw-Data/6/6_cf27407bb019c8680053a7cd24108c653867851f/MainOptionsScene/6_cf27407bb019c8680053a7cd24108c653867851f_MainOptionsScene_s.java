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
 
 import java.text.DecimalFormat;
 
 import org.andengine.entity.Entity;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.scene.background.EntityBackground;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.text.Text;
 import org.andengine.entity.text.TextOptions;
 import org.andengine.util.adt.align.HorizontalAlign;
 
 import com.madgear.ninjatrials.hud.SelectionStripe;
 import com.madgear.ninjatrials.hud.VolumeBar;
 import com.madgear.ninjatrials.managers.ResourceManager;
 import com.madgear.ninjatrials.managers.SFXManager;
 import com.madgear.ninjatrials.managers.SceneManager;
 
 /**
  * Implements the main options scene (sound).
  * @author Madgear Games
  */
 public class MainOptionsScene extends GameScene {
     private final static float WIDTH = ResourceManager.getInstance().cameraWidth;
     private final static float HEIGHT = ResourceManager.getInstance().cameraHeight;
     private SelectionStripe selectionStripe;
     private final String[] menuOptions = {"CONFIGURE CONTROLS","MUSIC VOLUME","SOUNDS VOLUME",
             "MUSIC TEST", "SOUND TEST"};
     private Text musicPercentageText;
     private Text soundPercentageText;
     private VolumeBar musicVolumeBar;
     private VolumeBar soundVolumeBar;
     private final static float VOLUME_INCREMENT_VAL = 0.1f;
     private final static float musicPercentageYPos = 500;
     private final static float soundPercentageYPos = 400;
     private DecimalFormat formatter = new java.text.DecimalFormat("00");
     private boolean testMusicPlaying = false;
 
 
     /**
      * MainOptionsScene constructor.
      * Disabled loading screen.
      */
     public MainOptionsScene() {
         super(0f);
     }
     
     /**
      * This class is not used (loading scene is disabled).
      */
     @Override
     public Scene onLoadingScreenLoadAndShown() {
         return null;
     }
 
     /**
      * This class is not used (loading scene is disabled).
      */
     @Override
     public void onLoadingScreenUnloadAndHidden() {}
 
     @Override
     public void onLoadScene() {
         //TODO: review resources
         ResourceManager.getInstance().loadOptionResources();
         ResourceManager.getInstance().loadSoundsResources();
         ResourceManager.getInstance().loadMusicsResources();
     }
 
     @Override
     public void onShowScene() {
         // Background:
         // Crate the background Pattern Sprite:
         ResourceManager.getInstance().mainOptionsPattern.setTextureSize(WIDTH, HEIGHT);
         Sprite patternSprite = new Sprite(WIDTH/2, HEIGHT/2,
                 ResourceManager.getInstance().mainOptionsPattern,
                 ResourceManager.getInstance().engine.getVertexBufferObjectManager());
         // Add pattern sprite to a new entity:
         Entity backgroundEntity = new Entity();
         backgroundEntity.attachChild(patternSprite);
         // Create a new background from the entity ():
         EntityBackground background = new EntityBackground(0.1f, 0.2f, 0.2f, backgroundEntity);
         setBackground(background);
         
         // Options tittle:
         final Text tittle = new Text(WIDTH/2, HEIGHT - 150,
                 ResourceManager.getInstance().fontXBig, "OPTIONS",
                 new TextOptions(HorizontalAlign.CENTER),
                 ResourceManager.getInstance().engine.getVertexBufferObjectManager());
         attachChild(tittle);
         
         // SelectionStripe:
         selectionStripe = new SelectionStripe(250, HEIGHT / 2 - 150, 
                 SelectionStripe.DISP_VERTICAL, 110f,
                 menuOptions, SelectionStripe.TEXT_ALIGN_LEFT, 0);
         attachChild(selectionStripe);
         
         // Volume Bars:
         musicVolumeBar = new VolumeBar(WIDTH/2 + 600, musicPercentageYPos,
                 SFXManager.getMusicVolume());
         attachChild(musicVolumeBar);
         soundVolumeBar = new VolumeBar(WIDTH/2 + 600, soundPercentageYPos, 
                 SFXManager.getSoundVolume());
         attachChild(soundVolumeBar);
         
         // Volume percentages:
         musicPercentageText = new Text(WIDTH/2 + 200, musicPercentageYPos,
                 ResourceManager.getInstance().fontMedium, "10000",
                 new TextOptions(HorizontalAlign.CENTER),
                 ResourceManager.getInstance().engine.getVertexBufferObjectManager());
         musicPercentageText.setText(String.valueOf(musicVolumeBar.getValuePercent()));
         attachChild(musicPercentageText);
         
         soundPercentageText = new Text(WIDTH/2 + 200, soundPercentageYPos,
                 ResourceManager.getInstance().fontMedium, "10000",
                 new TextOptions(HorizontalAlign.CENTER),
                 ResourceManager.getInstance().engine.getVertexBufferObjectManager());
         soundPercentageText.setText(String.valueOf(soundVolumeBar.getValuePercent()));
         attachChild(soundPercentageText);
     }
 
     @Override
     public void onHideScene() {}
 
     @Override
     public void onUnloadScene() {
         //TODO: review resources
         ResourceManager.getInstance().unloadOptionResources();        
         ResourceManager.getInstance().unloadSoundsResources();
         ResourceManager.getInstance().unloadMusicsResources();
     }
 
     @Override
     public void onPressDpadUp() {
         selectionStripe.movePrevious();
     }
 
     @Override
     public void onPressDpadDown() {
         selectionStripe.moveNext();
     }
     
     @Override
     public void onPressDpadLeft() {
         int optionIndex = selectionStripe.getSelectedIndex();
         switch(optionIndex) {
         // MUSIC VOLUME-
         case 1:
             SFXManager.addMusicVolume(-VOLUME_INCREMENT_VAL);
             musicVolumeBar.setValue(SFXManager.getMusicVolume());
             musicPercentageText.setText(String.valueOf(musicVolumeBar.getValuePercent()));
             updateMusicVolume();
             break;
         // SOUND VOLUME-
         case 2:
             SFXManager.addSoundVolume(-VOLUME_INCREMENT_VAL);
             soundVolumeBar.setValue(SFXManager.getSoundVolume());
             soundPercentageText.setText(String.valueOf(soundVolumeBar.getValuePercent()));
             break;
         }
     }
 
     @Override
     public void onPressDpadRight() {
         int optionIndex = selectionStripe.getSelectedIndex();
         switch(optionIndex) {
         // MUSIC VOLUME+
         case 1:
             SFXManager.addMusicVolume(VOLUME_INCREMENT_VAL);
             musicVolumeBar.setValue(SFXManager.getMusicVolume());
             musicPercentageText.setText(String.valueOf(musicVolumeBar.getValuePercent()));
             updateMusicVolume();
             break;
         // SOUND VOLUME+
         case 2:
             SFXManager.addSoundVolume(VOLUME_INCREMENT_VAL);
             soundVolumeBar.setValue(SFXManager.getSoundVolume());
             soundPercentageText.setText(String.valueOf(soundVolumeBar.getValuePercent()));
             break;
         }
     }
     
 
     private void updateMusicVolume() {
        SFXManager.pauseMusic(ResourceManager.getInstance().intro1);
        SFXManager.resumeMusic(ResourceManager.getInstance().intro1);
     }
     
 
     @Override
     public void onPressButtonO() {
         int optionIndex = selectionStripe.getSelectedIndex();
         switch(optionIndex) {
         // CONFIGURE CONTROLS
         case 0:
             SceneManager.getInstance().showScene(new ControllerOptionsScene());
             break;
         // MUSIC TEST
         case 3:
             // TODO: change demo music??
             if(SFXManager.isPlaying(ResourceManager.getInstance().intro1))
                 SFXManager.pauseMusic(ResourceManager.getInstance().intro1);
             else
                 SFXManager.playMusic(ResourceManager.getInstance().intro1);
             break;
         // SOUND TEST
         case 4:
             SFXManager.playSound(ResourceManager.getInstance().menuActivate);
             break;
         }
     }
 
     @Override
     public void onPressButtonMenu() {
         if (ResourceManager.getInstance().engine != null) {
             SFXManager.pauseMusic(ResourceManager.getInstance().cutMusic);
             SceneManager.getInstance().showScene(new MainMenuScene());
         }
     }
 }
