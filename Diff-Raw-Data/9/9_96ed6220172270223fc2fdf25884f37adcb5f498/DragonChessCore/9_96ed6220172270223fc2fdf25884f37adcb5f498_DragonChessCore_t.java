 /*
  * Copyright (C) 2014 RolTekk
  * Application: DragonChess
  * Description: core application code
  */
 
 package com.roltekk.game.dragonchess_core;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.audio.Music;
 import com.badlogic.gdx.graphics.Texture;
 import com.roltekk.game.dragonchess_core.debug.Debug;
 import com.roltekk.game.dragonchess_core.enums.Screens;
 import com.roltekk.game.dragonchess_core.screens.GameScreen;
 import com.roltekk.game.dragonchess_core.screens.LogoScreen;
 import com.roltekk.game.dragonchess_core.screens.OptionsScreen;
 import com.roltekk.game.dragonchess_core.screens.TitleScreen;
 
 public class DragonChessCore extends Game {
   private static final String TAG = "DragonChessCore";
   private Music               mBGMusic;
 
   // ApplicationListener overrides ////////////////////////////////////////////
   @Override
   public void create() {
     Gdx.app.setLogLevel(Debug.LOG_LVL);
     Gdx.app.debug(TAG, "create game");
     
     // try to start music
     mBGMusic = Gdx.audio.newMusic(Gdx.files.internal("music/Decline.mp3"));
     mBGMusic.setLooping(true);
    mBGMusic.play();
     
 //    Global.checkGLVersion();
     // results:
     // Android - GL11
     // Desktop - GL11
     // HTML    - GL20
 
     // TODO : get screen metrics and save in global state
     
     // TODO : will have to change where screens get inited so that a loading bar can be shown
     //        (possibly on logo screen)
 
     LogoScreen.getInstance().init(this);
     TitleScreen.getInstance().init(this);
     OptionsScreen.getInstance().init(this);
     GameScreen.getInstance().init(this);
 
     // set first screen
     setScreen(Screens.LOGO);
   }
   
   @Override
   public void dispose() {
     mBGMusic.dispose();
     LogoScreen.getInstance().dispose();
     TitleScreen.getInstance().dispose();
     OptionsScreen.getInstance().dispose();
     GameScreen.getInstance().dispose();
   }
 
   // other functions //////////////////////////////////////////////////////////
   public void setScreen(Screens nextScreen) {
     switch (nextScreen) {
     case LOGO:
       setScreen(LogoScreen.getInstance());
       break;
     case TITLE:
       setScreen(TitleScreen.getInstance());
       break;
     case OPTIONS:
       setScreen(OptionsScreen.getInstance());
       break;
     case GAME:
       setScreen(GameScreen.getInstance());
       break;
     }
   }
 }
