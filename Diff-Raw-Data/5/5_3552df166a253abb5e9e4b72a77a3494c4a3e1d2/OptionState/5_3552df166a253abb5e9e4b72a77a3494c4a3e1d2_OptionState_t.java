 package com.turbonips.troglodytes.states;
 
 import java.awt.Font;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.UnicodeFont;
 import org.newdawn.slick.font.effects.ColorEffect;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class OptionState extends BaseMenuState {
 	public static final int ID = 2;
 	
 	private UnicodeFont buttonFont;
 	private String windowedSt = "Windowed";
 	private int windowedX, windowedY;
 	private String fullscreenSt = "Fullscreen";
 	private int fullscreenX, fullscreenY;
 	private String backSt = "Back";
 	private int backX, backY;
 	private String musicSt = "Music:";
 	private int musicX, musicY;
 	private String musicOnSt = "On";
 	private int musicOnX, musicOnY;
 	private String musicOffSt = "Off";
 	private int musicOffX, musicOffY;
 	private String soundSt = "Sound:";
 	private int soundX, soundY;
 	private String soundOnSt = "On";
 	private int soundOnX, soundOnY;
 	private String soundOffSt = "Off";
 	private int soundOffX, soundOffY;
 	
 	String titleSt = "Options";
 	int titleX, titleY;
 	private StateBasedGame game;
 	private GameContainer container;
 	enum SelectedState {NONE,
 						BACK,
 						WINDOWED,
 						FULLSCREEN,
 						MUSIC_ON,
 						MUSIC_OFF,
 						SOUND_ON,
 						SOUND_OFF};
 	SelectedState selectedState = SelectedState.NONE;
 
 	private UnicodeFont titleFont;
 
 	@Override
 	public void init(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		super.init(container, game);
 		
 		String fontName = "Palatino";
 		buttonFont = new UnicodeFont (new Font(fontName, Font.BOLD, 25));
 		buttonFont.getEffects().add(new ColorEffect(java.awt.Color.white));
 		titleFont = new UnicodeFont (new Font(fontName, Font.BOLD, 70));
 		titleFont.getEffects().add(new ColorEffect(java.awt.Color.white));
 		
 		int windowedWidth = buttonFont.getWidth(windowedSt);
 		int fullWidth = buttonFont.getWidth(fullscreenSt);
 		int fullHeight = buttonFont.getHeight(fullscreenSt);
 		int backWidth = buttonFont.getWidth(backSt);
 		int musicWidth = buttonFont.getWidth(musicSt);
 		int musicHeight = buttonFont.getHeight(musicSt);
 		int onWidth = buttonFont.getWidth(musicOnSt);
 		int offWidth = buttonFont.getWidth(musicOffSt);
 		int soundWidth = buttonFont.getWidth(soundSt);
 		int soundHeight = buttonFont.getHeight(soundSt);
 		int titleWidth = titleFont.getWidth(titleSt);
 		int titleHeight = titleFont.getHeight(titleSt);
 		
 		titleX = container.getWidth()/2-titleWidth/2;
 		titleY = container.getHeight()/4;
 		windowedX = container.getWidth()/2-(windowedWidth+fullWidth)/2 - 5;
 		windowedY = titleY + titleHeight + 40;
 		fullscreenX = container.getWidth()/2-(windowedWidth+fullWidth)/2 + windowedWidth + 5;
 		fullscreenY = titleY + titleHeight + 40;
 		musicX = container.getWidth()/2-(musicWidth+onWidth+offWidth+20)/2;
 		musicY = fullscreenY + fullHeight+20;
 		musicOnX = musicX + musicWidth + 10;
 		musicOnY = musicY;
 		musicOffX = musicOnX + onWidth + 10;
 		musicOffY = musicY;
 		
 		soundX = container.getWidth()/2-(soundWidth+onWidth+offWidth+20)/2;
 		soundY = musicY + musicHeight + 20;
 		soundOnX = soundX + soundWidth + 10;
 		soundOnY = soundY;
 		soundOffX = soundOnX + onWidth + 10;
 		soundOffY = soundY;
 					
 		backX = container.getWidth()/2-(backWidth/2);
 		backY = soundY + soundHeight + 20;
 		
 		this.game = game;
 		this.container = container;
 	}
 
 	@Override
 	public void render(GameContainer container, StateBasedGame game, Graphics g)
 			throws SlickException {
 		super.render(container, game, g);
 		
 		Color WHITE = new Color(255,255,255);
 		Color ORANGE = new Color(255,127,0);
 		
 		titleFont.drawString(titleX, titleY, titleSt, WHITE);
 		
 		// Draw the windowed button
 		if (selectedState == SelectedState.WINDOWED && container.isFullscreen()) {
 			buttonFont.drawString(windowedX, windowedY, windowedSt, ORANGE);
 		} else {
 			buttonFont.drawString(windowedX, windowedY, windowedSt, WHITE);
 		}
 		if (!container.isFullscreen())
			g.drawRect(windowedX-2, windowedY, buttonFont.getWidth(windowedSt)+4, buttonFont.getHeight(windowedSt)+4);
 		
 		// Draw the fullscreen button
 		if (selectedState == SelectedState.FULLSCREEN && !container.isFullscreen()) {
 			buttonFont.drawString(fullscreenX, fullscreenY, fullscreenSt, ORANGE);
 		} else {
 			buttonFont.drawString(fullscreenX, fullscreenY, fullscreenSt, WHITE);
 		}
 		if (container.isFullscreen())
			g.drawRect(fullscreenX-2, fullscreenY, buttonFont.getWidth(fullscreenSt)+4, buttonFont.getHeight(fullscreenSt)+4);
 		
 		// Draw the music label
 		buttonFont.drawString(musicX, musicY, musicSt, WHITE);
 		
 		// Draw the music on button
 		if (selectedState == SelectedState.MUSIC_ON) {
 			buttonFont.drawString(musicOnX, musicOnY, musicOnSt, ORANGE);
 		} else {
 			buttonFont.drawString(musicOnX, musicOnY, musicOnSt, WHITE);
 		}
 		
 		// Draw the music off button
 		if (selectedState == SelectedState.MUSIC_OFF) {
 			buttonFont.drawString(musicOffX, musicOffY, musicOffSt, ORANGE);
 		} else {
 			buttonFont.drawString(musicOffX, musicOffY, musicOffSt, WHITE);
 		}
 		
 		// Draw the sound label
 		buttonFont.drawString(soundX, soundY, soundSt, WHITE);
 		
 		// Draw the sound on button
 		if (selectedState == SelectedState.SOUND_ON) {
 			buttonFont.drawString(soundOnX, soundOnY, soundOnSt, ORANGE);
 		} else {
 			buttonFont.drawString(soundOnX, soundOnY, soundOnSt, WHITE);
 		}
 		
 		// Draw the sound off button
 		if (selectedState == SelectedState.SOUND_OFF) {
 			buttonFont.drawString(soundOffX, soundOffY, soundOffSt, ORANGE);
 		} else {
 			buttonFont.drawString(soundOffX, soundOffY, soundOffSt, WHITE);
 		}
 		
 		// Draw the back button
 		if (selectedState == SelectedState.BACK) {
 			buttonFont.drawString(backX, backY, backSt, ORANGE);
 		} else {
 			buttonFont.drawString(backX, backY, backSt, WHITE);
 		}
 		
 		buttonFont.loadGlyphs();
 		titleFont.loadGlyphs();
 	}
 
 	@Override
 	public void update(GameContainer container, StateBasedGame game, int delta)
 			throws SlickException {
 	}
 	
 	@Override
 	public void mouseClicked(int button, int x, int y, int clickCount) {
 		// Back clicked
 		if (x > backX && x < backX + buttonFont.getWidth(backSt) &&
 			y > backY && y < backY + buttonFont.getHeight(backSt)) {
 			game.enterState(MenuState.ID);
 		}
 		// Fullscreen clicked
 		if (x > fullscreenX && x < fullscreenX + buttonFont.getWidth(fullscreenSt) &&
 			y > fullscreenY && y < fullscreenY + buttonFont.getHeight(fullscreenSt)) {
 			try { 
 				container.setFullscreen(true);
 			} catch (Exception ex) { }
 		}
 		// Windowed clicked
 		if (x > windowedX && x < windowedX + buttonFont.getWidth(windowedSt) &&
 			y > windowedY && y < windowedY + buttonFont.getHeight(windowedSt)) {
 			try { 
 				container.setFullscreen(false);
 			} catch (Exception ex) { }
 		}
 		selectedState = SelectedState.NONE;
 	}
 	
 	@Override
 	public void mouseMoved(int oldx, int oldy, int newx, int newy) {
 		selectedState = SelectedState.NONE;
 		
 		// Back selected
 		if (newx > backX && newx < backX + buttonFont.getWidth(backSt) &&
 			newy > backY && newy < backY + buttonFont.getHeight(backSt)) {
 			selectedState = SelectedState.BACK;
 		}
 		
 		// Windowed selected
 		if (newx > windowedX && newx < windowedX + buttonFont.getWidth(windowedSt) &&
 			newy > windowedY && newy < windowedY + buttonFont.getHeight(windowedSt)) {
 			selectedState = SelectedState.WINDOWED;
 		}
 		
 		// Fullscreen selected
 		if (newx > fullscreenX && newx < fullscreenX + buttonFont.getWidth(fullscreenSt) &&
 			newy > fullscreenY && newy < fullscreenY + buttonFont.getHeight(fullscreenSt)) {
 			selectedState = SelectedState.FULLSCREEN;
 		}
 		
 		// Music on selected
 		if (newx > musicOnX && newx < musicOnX + buttonFont.getWidth(musicOnSt) &&
 			newy > musicOnY && newy < musicOnY + buttonFont.getHeight(musicOnSt)) {
 			selectedState = SelectedState.MUSIC_ON;
 		}
 		
 		// Music off selected
 		if (newx > musicOffX && newx < musicOffX + buttonFont.getWidth(musicOffSt) &&
 			newy > musicOffY && newy < musicOffY + buttonFont.getHeight(musicOffSt)) {
 			selectedState = SelectedState.MUSIC_OFF;
 		}
 		
 		// Sound on selected
 		if (newx > soundOnX && newx < soundOnX + buttonFont.getWidth(soundOnSt) &&
 			newy > soundOnY && newy < soundOnY + buttonFont.getHeight(soundOnSt)) {
 			selectedState = SelectedState.SOUND_ON;
 		}
 		
 		// Sound off selected
 		if (newx > soundOffX && newx < soundOffX + buttonFont.getWidth(soundOffSt) &&
 			newy > soundOffY && newy < soundOffY + buttonFont.getHeight(soundOffSt)) {
 			selectedState = SelectedState.SOUND_OFF;
 		}
 	}
 
 	@Override
 	public int getID() {
 		return ID;
 	}
 
 }
