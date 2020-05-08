 /**
  * Copyright (c) 2012, dector (dector9@gmail.com)
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  *   - Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimer.
  *
  *   - Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
  * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
  * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package ua.org.dector.space_lander.screens;
 
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.ui.*;
 import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
 import ua.org.dector.gcore.common.Settings;
 import ua.org.dector.gcore.game.TableScreen;
 import ua.org.dector.gcore.input.ClickActorListener;
 import ua.org.dector.gcore.managers.MusicManager;
 import ua.org.dector.gcore.managers.SoundManager;
 import ua.org.dector.space_lander.Lander;
 import ua.org.dector.space_lander.constants.Labels;
 
 import static ua.org.dector.space_lander.constants.UISizes.*;
 
 /**
  * @author dector (dector9@gmail.com)
  */
 public class AudioOptionsScreen extends TableScreen<Lander> {
     private boolean sfxEnabled;
     private float sfxVolume;
     private boolean musicEnabled;
     private float musicVolume;
 
     private Label lblSfxVolume;
     private Label lblMusicVolume;
 
     public AudioOptionsScreen(Lander lander) {
         super(lander);
     }
 
     public void show() {
         final SoundManager soundManager = game.getSoundManager();
         final MusicManager musicManager = game.getMusicManager();
 
         final Settings settings = game.getSettings();
             sfxEnabled = settings.isSfxEnabled();
             sfxVolume = settings.getSfxVolume();
             musicEnabled = settings.isMusicEnabled();
             musicVolume = settings.getMusicVolume();
 
         Skin skin = game.getGraphics().getSkin();
         Table table = getTable();
 
 //        final CheckBox chkSfxEnabled = new CheckBox("", skin);
 //        chkSfxEnabled.setChecked(game.getSettings().isSfxEnabled());
 //        chkSfxEnabled.addListener(new ChangeListener() {
 //            public void changed(ChangeEvent changeEvent, Actor actor) {
 //                boolean sfxEnabled = chkSfxEnabled.isChecked();
 //                game.getSoundManager().setEnabled(sfxEnabled);
 //            }
 //        });
 
         Slider sldSfxVolume = new Slider(0, 1, 0.01f, skin);
         sldSfxVolume.setValue(sfxVolume);
         sldSfxVolume.addListener(new ChangeListener() {
             public void changed(ChangeEvent event, Actor actor) {
                 Slider slider = (Slider)actor;
                 sfxVolume = slider.getValue();
                 soundManager.setVolume(sfxVolume);
 
                 updateSfxVolumeLabel();
             }
         });
 
         lblSfxVolume = new Label("", skin);
         updateSfxVolumeLabel();
 
         Slider sldMusicVolume = new Slider(0, 1, 0.01f, skin);
         sldMusicVolume.setValue(musicVolume);
         sldMusicVolume.addListener(new ChangeListener() {
             public void changed(ChangeEvent event, Actor actor) {
                 Slider slider = (Slider)actor;
                 musicVolume = slider.getValue();
                 musicManager.setVolume(musicVolume);
 
                 updateMusicVolumeLabel();
             }
         });
 
         lblMusicVolume = new Label("", skin);
         updateMusicVolumeLabel();
 
         Button btnSave = new TextButton(Labels.OPTIONS$SAVE, skin);
         btnSave.addListener(new ClickActorListener(btnSave) {
             protected void onClick(int button) {
                 if (button == Input.Buttons.LEFT) {
                     settings.setSfxEnabled(sfxEnabled);
                     settings.setSfxVolume(sfxVolume);
                     settings.setMusicEnabled(musicEnabled);
                     settings.setMusicVolume(musicVolume);
 
                     gotoPrevSceen();
                 }
             }
         });
 
         Button btnCancel = new TextButton(Labels.OPTIONS$CANCEL, skin);
         btnCancel.addListener(new ClickActorListener(btnCancel) {
             protected void onClick(int button) {
                 if (button == Input.Buttons.LEFT) {
                     soundManager.setEnabled(settings.isSfxEnabled());
                     soundManager.setVolume(settings.getSfxVolume());
                     musicManager.setEnabled(settings.isMusicEnabled());
                     musicManager.setVolume(settings.getMusicVolume());
 
                     gotoPrevSceen();
                 }
             }
         });
 
         if (Lander.DEV_MODE) table.debug();
 
         table.defaults().spaceBottom(BOTTOM_SPACE);
         table.columnDefaults(3).padLeft(20);
        table.columnDefaults(3).width(30);
         table.add(Labels.OPTIONS$AUDIO).spaceBottom(TITLE_BOTTOM_SPACE).colspan(4);
         table.row();
         table.add(Labels.OPTIONS$SFX_ENABLED);
 //        table.add(chkSfxEnabled).colspan(2).left();
         table.row();
         table.add(Labels.OPTIONS$SFX_VOLUME);
         table.add(sldSfxVolume).colspan(2).fill();
         table.add(lblSfxVolume).colspan(3).uniform().left();
         table.row();
         table.add(Labels.OPTIONS$MUSIC_ENABLED);
         table.row();
         table.add(Labels.OPTIONS$MUSIC_VOLUME);
         table.add(sldMusicVolume).colspan(2).fill();
         table.add(lblMusicVolume).colspan(3).uniform().left();
         table.row();
         table.add(btnSave).colspan(4).size(BUTTONS_WIDTH, BUTTONS_HEIGHT)
                 .fill().uniform();
         table.row();
         table.add(btnCancel).colspan(4).size(BUTTONS_WIDTH, BUTTONS_HEIGHT)
                 .fill().uniform();
 
     }
 
     private void gotoPrevSceen() {
         game.setScreen(new OptionsScreen(game));
     }
 
     private void updateSfxVolumeLabel() {
         String text = String.format("%d%%", (int)(sfxVolume * 100));
         lblSfxVolume.setText(text);
     }
 
     private void updateMusicVolumeLabel() {
         String text = String.format("%d%%", (int) (musicVolume * 100));
         lblMusicVolume.setText(text);
     }
 
     public void render(float delta) {
         super.render(delta);
 
        if (Lander.DEV_MODE) Table.drawDebug(getStage());
     }
 }
