 package com.picotech.lightrunnerlibgdx;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Application.ApplicationType;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 
 public class Menu extends Sprite2 {
 	public enum MenuState {
 		MAIN, PAUSE, OPTIONS, INTRODUCTION, HELP, CREDITS, STATISTICS
 	}
 
 	MenuState menuState = MenuState.MAIN;
 
 	// public BitmapFont bf;
 
 	// INTRO variables
 	public float switchTime = 5f;
 	public float fadeBufferTime = 0.75f;
 	public float introTime = 0f;
 	public float introAlpha = 0f;
 
 	// MainMenu
 	public Rectangle skipButton, playButton, helpButton, statisticsButton,
 			creditsButton, quitButton;
 	float buffer;
 	Rectangle grey;
 	public Sprite2 gearButton, playIntroButton;
 
 	// Pause
 	public Rectangle resumeButton, restartButton, backMainButton;
 	public Sprite2 musicPButton, sfxPButton;
 
 	// Options
 	public Sprite2 musicOButton, sfxOButton;
 	public ScrollBar musicVolume, sfxVolume;
 
 	// Instructions
 	public Rectangle BackButton, NextButton;
 	/**
 	 * This is the x-position of the 0th instructions screen.
 	 */
 	public int x0 = 160;
 
 	private float fadingScale;
 	public String[] names = { "Cameron Akker", "Daniel Fang", "Sarang Joshi",
 			"Adarsh Karnati", "Atticus Liu" };
 
 	public Menu() {
 		super(new Vector2(0, 0), GameScreen.width, GameScreen.height);
 		// Initializes the rectangular buttons to be a particular x, y, width,
 		// height
 
 		skipButton = new Rectangle(565 * GameScreen.defS.x,
 				10 * GameScreen.defS.y, 150 * GameScreen.defS.x,
 				75 * GameScreen.defS.y);
 		helpButton = new Rectangle(1050 * GameScreen.defS.x,
 				510 * GameScreen.defS.y, 200 * GameScreen.defS.x,
 				90 * GameScreen.defS.y);
 		statisticsButton = new Rectangle(1050 * GameScreen.defS.x,
 				380 * GameScreen.defS.y, 200 * GameScreen.defS.x,
 				90 * GameScreen.defS.y);
 		creditsButton = new Rectangle(1050 * GameScreen.defS.x,
 				250 * GameScreen.defS.y, 200 * GameScreen.defS.x,
 				90 * GameScreen.defS.y);
 		quitButton = new Rectangle(1050 * GameScreen.defS.x,
 				120 * GameScreen.defS.y, 200 * GameScreen.defS.x,
 				90 * GameScreen.defS.y);
 
 		buffer = GameScreen.width - (creditsButton.x + creditsButton.width);
 		grey = new Rectangle(creditsButton.x - buffer, 0, GameScreen.width
 				- (quitButton.x - buffer), GameScreen.height);
 		gearButton = new Sprite2(grey.x + 90 * GameScreen.defS.x,
 				30 * GameScreen.defS.y, "gear.png");
 		playIntroButton = new Sprite2(0, 0, "playIntro.png");
 
 		// playButton is way bigger, and will eventually be a .png
 		playButton = new Rectangle(0, 0, grey.x, GameScreen.height);
 
 		// Pause
 		resumeButton = new Rectangle(800 * GameScreen.defS.x,
 				460 * GameScreen.defS.y, 400 * GameScreen.defS.x,
 				100 * GameScreen.defS.y);
 		restartButton = new Rectangle(800 * GameScreen.defS.x,
 				310 * GameScreen.defS.y, 400 * GameScreen.defS.x,
 				100 * GameScreen.defS.y);
 		backMainButton = new Rectangle(800 * GameScreen.defS.x,
 				160 * GameScreen.defS.y, 400 * GameScreen.defS.x,
 				100 * GameScreen.defS.y);
 
 		musicPButton = new Sprite2(resumeButton.x + 60 * GameScreen.defS.x,
 				backMainButton.y - 120 * GameScreen.defS.y, "music.png");
 		sfxPButton = new Sprite2(resumeButton.x + resumeButton.width - 140
 				* GameScreen.defS.x,
 				backMainButton.y - 120 * GameScreen.defS.y, "sfx.png");
 
 		// Options
 		musicOButton = new Sprite2(200 * GameScreen.defS.x,
 				410 * GameScreen.defS.y, "music.png");
 		sfxOButton = new Sprite2(200 * GameScreen.defS.x,
 				290 * GameScreen.defS.y, "sfx.png");
 		musicVolume = new ScrollBar(new Vector2(musicOButton.position.x + 160
 				* GameScreen.defS.x, musicOButton.position.y - 10
 				* GameScreen.defS.y), GameScreen.musicVolume,
 				800f * GameScreen.defS.x);
 		sfxVolume = new ScrollBar(new Vector2(sfxOButton.position.x + 160
 				* GameScreen.defS.x, sfxOButton.position.y - 10
 				* GameScreen.defS.y), GameScreen.sfxVolume,
 				800f * GameScreen.defS.x);
 
 		// Instructions
 		BackButton = new Rectangle();
 		NextButton = new Rectangle();
 
 		fadingScale = 0;
 	}
 
 	@Override
 	public void loadContent() {
 		musicPButton.loadContent();
 		sfxPButton.loadContent();
 		musicOButton.loadContent();
 		sfxOButton.loadContent();
 
 		musicVolume.loadContent();
 		sfxVolume.loadContent();
 
 		gearButton.loadContent();
 		playIntroButton.loadContent();
 	}
 
 	/**
 	 * Draws and updates the menu.
 	 */
 	@Override
 	public void draw(SpriteBatch batch) {
 		switch (menuState) {
 		case CREDITS:
 			// white font on black background to make our names pop
 
 			// LightRunner logo here
 			Assets.drawByPixels(batch, Assets.fullScreen, Color.BLACK);
 			Assets.drawByPixels(batch, this.backMainButton, Color.GRAY);
 
 			batch.begin();
 			batch.draw(Assets.titleScreen, Assets.fullScreen.width / 2
 					- (Assets.titleScreen.getWidth() / 2), 440);
 
 			batch.end();
 
 			for (int i = 0; i < names.length; i++) {
 				Assets.textWhite(batch, names[i], 380, 540 - 80 * i);
 			}
 			// Assets.text(batch, "Cameron Akker", 380, 540);
 			// Assets.text(batch, "Daniel Fang", 380, 460);
 			// Assets.text(batch, "Sarang Joshi", 380, 380);
 			// Assets.text(batch, "Adarsh Karnati", 380, 300);
 			// Assets.text(batch, "Atticus Liu", 380, 220);
 
 			Assets.textWhite(batch, "Special thanks to StudentRND", 435, 90);
 			Assets.textWhite(batch, "Main", backMainButton.x
 					+ backMainButton.width / 2 - 30, getMainY(backMainButton));
 
 			// bf.setColor(Color.WHITE);
 			// repositioned names, "Special thanks to StudentRND"
 			// bf.draw(batch, "Cameron Akker", 380, 540);
 			// bf.draw(batch, "Daniel Fang", 380, 460);
 			// bf.draw(batch, "Sarang Joshi", 380, 380);
 			// bf.draw(batch, "Adarsh Karnati", 380, 300);
 			// bf.draw(batch, "Atticus Liu", 380, 220);
 			// bf.draw(batch, "Special thanks to StudentRND", 435, 90);
 			// bf.draw(batch, "Main", backMainButton.x + backMainButton.width /
 			// 2
 			// - 30, getMainY(backMainButton));
 			// batch.end();
 			break;
 		case HELP:
 			if (GameScreen.instructionsScreen < Assets.instructionCuts.length) {
 				Assets.drawByPixels(batch, Assets.fullScreen, Color.BLACK);
 
 				batch.begin();
 				batch.setColor(Color.WHITE);
 
 				// Style 1: Only draws one image at once.
 				/*
 				 * batch.draw(
 				 * Assets.instructionCuts[GameScreen.instructionsScreen], 160,
 				 * 90); //(GameScreen.width -
 				 * Assets.introCuts[GameScreen.instructionsScreen] //
 				 * .getWidth()) / 2, //(GameScreen.height -
 				 * Assets.introCuts[GameScreen.instructionsScreen] //
 				 * .getHeight()) / 2);
 				 */
 
 				// Style 2: Draws the entire instruction "reel"
 				for (int i = 0; i < Assets.instructionCuts.length; i++) {
 					batch.draw(new TextureRegion(Assets.instructionCuts[i]), x0
 							+ GameScreen.defS.x * 1060 * i,
 							GameScreen.defS.y * 90, 0, 0,
 							Assets.instructionCuts[i].getWidth(),
 							Assets.instructionCuts[i].getHeight(),
 							GameScreen.defS.x, GameScreen.defS.y, 0f);
 					// .draw(, , );
 				}
 				batch.draw(new TextureRegion(Assets.play), x0 + 1060
 						* GameScreen.defS.x * Assets.instructionCuts.length,
 						GameScreen.defS.y * 90, 0, 0, Assets.play.getWidth(),
 						Assets.play.getHeight(), GameScreen.defS.x,
 						GameScreen.defS.y, 0f);
 				batch.end();
 			}
 			break;
 		case INTRODUCTION:
 			// two-fold: three plot .png's come here
 			// then put the instruction .png's
 			// will take place as a sequence
 			if (GameScreen.introCut < Assets.introCuts.length) {
 				if (introTime <= fadeBufferTime) {
 					// fading in
 					introAlpha = introTime / fadeBufferTime;
 				} else if (introTime >= switchTime - fadeBufferTime) {
 					// fading out
 					introAlpha = 1
 							- (introTime - (switchTime - fadeBufferTime))
 							/ (fadeBufferTime);
 				} else if (introTime >= fadeBufferTime
 						&& introTime <= switchTime - fadeBufferTime) {
 					introAlpha = 1f;
 				}
 				batch.begin();
 				batch.setColor(Color.WHITE);
 				batch.end();
 				Assets.drawByPixels(batch, Assets.fullScreen, Color.BLACK);
 				batch.begin();
 				batch.setColor(Color.WHITE.r, Color.WHITE.g, Color.WHITE.b,
 						introAlpha);
 
 				// This style scales it to the entire screen.
 				// batch.draw(Assets.introCuts[GameScreen.introCut], 0, 0,
 				// width,
 				// height);
 				// This style draws it in the center.
 				batch.draw(
 						new TextureRegion(Assets.introCuts[GameScreen.introCut]),
 						(GameScreen.width - GameScreen.defS.x
 								* Assets.introCuts[GameScreen.introCut]
 										.getWidth()) / 2,
 						(GameScreen.height - GameScreen.defS.y
 								* Assets.introCuts[GameScreen.introCut]
 										.getHeight()) / 2, 0, 0,
 						Assets.introCuts[GameScreen.introCut].getWidth(),
 						Assets.introCuts[GameScreen.introCut].getHeight(),
 						GameScreen.defS.x, GameScreen.defS.y, 0f);
 				/*
 				 * draw( Assets.introCuts[GameScreen.introCut],
 				 * (GameScreen.width - GameScreen.defS.x *
 				 * Assets.introCuts[GameScreen.introCut] .getWidth()) / 2,
 				 * (GameScreen.height - GameScreen.defS.y *
 				 * Assets.introCuts[GameScreen.introCut] .getHeight()) / 2);
 				 */
 				introTime += Gdx.graphics.getDeltaTime();
 				if (introTime >= switchTime) {
 					// switching screens
 					introTime = 0f;
 					GameScreen.introCut++;
 				}
 				batch.end();
 				// Assets.textWhite(batch, "abcdefghijklmnopqrstuvwxyz",
 				// new Vector2(0, 244));
 
 				Assets.drawByPixels(batch, skipButton, Color.GRAY);
 
 				// skip button
 				Assets.textWhite(batch, "SKIP",
 						Assets.fontPos(skipButton, "SKIP"));
 
 			}
 			break;
 		case MAIN:
 			Assets.drawByPixels(batch, grey, new Color(Color.WHITE.r,
 					Color.WHITE.g, Color.WHITE.b, 0.5f));
 
 			Assets.drawByPixels(batch, playButton, new Color(Color.WHITE.r,
 					Color.WHITE.g, Color.WHITE.b, 0f));
 			Assets.drawByPixels(batch, helpButton, Color.GRAY);
 			Assets.drawByPixels(batch, statisticsButton, Color.GRAY);
 			// Assets.drawByPixels(batch, optionsButton, Color.GRAY);
 			Assets.drawByPixels(batch, creditsButton, Color.GRAY);
 			Assets.drawByPixels(batch, quitButton, Color.GRAY);
 
 			batch.begin();
 			batch.draw(new TextureRegion(Assets.titleScreen), grey.x / 2
 					- (GameScreen.defS.x * Assets.titleScreen.getWidth() / 2),
 					440 * GameScreen.defS.y, 0, 0,
 					Assets.titleScreen.getWidth(),
 					Assets.titleScreen.getHeight(), GameScreen.defS.x,
 					GameScreen.defS.y, 0);
 			// batch.draw(Assets.titleScreen, grey.x / 2 -
 			// (Assets.titleScreen.getWidth() / 2), 440 * GameScreen.defS.y);
 			gearButton.draw(batch);
 			playIntroButton.draw(batch);
 			batch.end();
 
 			// Text
 			// batch.begin();
 			// bf.setColor(Color.WHITE);
 			Assets.setTextScale(2f);
 			Assets.textWhite(batch, "Help", Assets.fontPos(helpButton, "Help"));
 			Assets.textWhite(batch, "Statistics",
 					Assets.fontPos(statisticsButton, "Statistics"));
 			Assets.textWhite(batch, "Credits",
 					Assets.fontPos(creditsButton, "Credits"));
 			Assets.textWhite(batch, "Quit", Assets.fontPos(quitButton, "Quit"));
 			Color c = new Color(1, 1, 1,
 					(float) (0.5f + 0.5f * Math
 							.cos(fadingScale += (GameScreen.dialogBoxActive ? 0
 									: .1))));
 			Assets.setTextScale(4.3f);
 			if (Gdx.app.getType() == ApplicationType.Desktop)
 				Assets.text(batch, "Tap anywhere to play", Assets.fontPos(
 						new Rectangle(0, 0, this.grey.x, GameScreen.height),
 						"Tap anywhere to play"), c);
 			else if (Gdx.app.getType() == ApplicationType.Android)
				Assets.text(batch, "Tap anywhere to play",
						Assets.fontPos(new Rectangle(0, 0, grey.x, grey.height), "Tap anywhere to play"), c);
				//Assets.text(batch, "Tap anywhere to play", new Vector2(0,
					//	GameScreen.height / 2), c);
 			// bf.setScale(2f);
 			// batch.end();
 			break;
 		case OPTIONS:
 			Assets.drawByPixels(batch, Assets.fullScreen, new Color(
 					Color.LIGHT_GRAY.r, Color.LIGHT_GRAY.g, Color.LIGHT_GRAY.b,
 					0.3f));
 
 			// control music and sound (volumes)
 			Assets.drawByPixels(batch, backMainButton, Color.GRAY);
 
 			batch.begin();
 			batch.draw(Assets.titleScreen, 150, 460);
 
 			musicOButton.draw(batch);
 			sfxOButton.draw(batch);
 
 			batch.setColor(Color.WHITE);
 			// bf.draw(batch, "Value:" + musicVolume.value, 400, 400);
 			batch.end();
 			Assets.setTextScale(2f);
 			Assets.textWhite(batch, "Main",
 					Assets.fontPos(this.backMainButton, "Main"));
 
 			Assets.drawByPixels(batch, new Rectangle(musicOButton.bounds.x,
 					musicOButton.bounds.y, musicOButton.bounds.width
 							* GameScreen.defS.x, musicOButton.bounds.height
 							* GameScreen.defS.y), new Color(Color.ORANGE.r,
 					Color.ORANGE.g, Color.ORANGE.b, GameScreen.musicVolume / 2));
 			Assets.drawByPixels(batch, new Rectangle(sfxOButton.bounds.x,
 					sfxOButton.bounds.y, sfxOButton.bounds.width
 					* GameScreen.defS.x, sfxOButton.bounds.height
 					* GameScreen.defS.y), new Color(
 					Color.GREEN.r, Color.GREEN.g, Color.GREEN.b,
 					GameScreen.sfxVolume));
 			// if (World.soundFX)
 			// Assets.drawByPixels(batch, sfxOButton.bounds, new Color(
 			// Color.GREEN.r, Color.GREEN.g, Color.GREEN.b, 0.5f));
 
 			musicVolume.draw(batch);
 			sfxVolume.draw(batch);
 
 			break;
 		case PAUSE:
 			Assets.drawByPixels(batch, Assets.fullScreen, new Color(
 					Color.LIGHT_GRAY.r, Color.LIGHT_GRAY.g, Color.LIGHT_GRAY.b,
 					0.3f));
 			Assets.drawByPixels(batch, resumeButton, Color.GRAY);
 			Assets.drawByPixels(batch, restartButton, Color.GRAY);
 			Assets.drawByPixels(batch, backMainButton, Color.GRAY);
 
 			batch.begin();
 			batch.draw(Assets.titleScreen, 150, 460);
 			musicPButton.draw(batch);
 			sfxPButton.draw(batch);
 			batch.end();
 
 			// Text
 			batch.begin();
 			// bf.setColor(Color.WHITE);
 			batch.end();
 
 			Assets.textWhite(batch, "Resume",
 					Assets.fontPos(resumeButton, "Resume"));
 			Assets.textWhite(batch, "Restart",
 					Assets.fontPos(restartButton, "Restart"));
 			Assets.textWhite(batch, "Main",
 					Assets.fontPos(backMainButton, "Main"));
 			Assets.drawByPixels(batch, musicPButton.bounds, new Color(
 					Color.ORANGE.r, Color.ORANGE.g, Color.ORANGE.b,
 					GameScreen.musicVolume / 2));
 			Assets.drawByPixels(batch, sfxPButton.bounds, new Color(
 					Color.GREEN.r, Color.GREEN.g, Color.GREEN.b,
 					GameScreen.sfxVolume));
 			break;
 		case STATISTICS:
 			Assets.drawByPixels(batch, Assets.fullScreen, new Color(
 					Color.LIGHT_GRAY.r, Color.LIGHT_GRAY.g, Color.LIGHT_GRAY.b,
 					0.7f));
 
 			Assets.drawByPixels(batch, backMainButton, Color.GRAY);
 
 			// display cumulative high score, time played (seconds), total score
 			batch.begin();
 			batch.draw(new TextureRegion(Assets.titleScreen),
 					150 * GameScreen.defS.x, 460 * GameScreen.defS.y, 0, 0,
 					Assets.titleScreen.getWidth() * GameScreen.defS.x,
 					Assets.titleScreen.getHeight() * GameScreen.defS.y,
 					GameScreen.defS.x, GameScreen.defS.y, 0);
 			// batch.draw(Assets.titleScreen, 150, 460);
 			batch.end();
 
 			/*Assets.textWhite(batch, "Main", backMainButton.x
 					+ backMainButton.width / 2 - 30, getMainY(backMainButton));
 */
 			Assets.textWhite(batch, "Main", Assets.fontPos(backMainButton, "Main"));
 			StatLogger2.draw(batch);
 			break;
 		}
 	}
 
 	public void setMusicValue(float newV) {
 		musicVolume.value = newV;
 		GameScreen.musicVolume = musicVolume.value;
 	}
 
 	public void setSFXValue(float newV) {
 		sfxVolume.value = newV;
 		GameScreen.sfxVolume = sfxVolume.value / 2;
 	}
 
 	public float getMainY(Rectangle r) {
 		return r.y + (.65f * r.height);
 	}
 }
