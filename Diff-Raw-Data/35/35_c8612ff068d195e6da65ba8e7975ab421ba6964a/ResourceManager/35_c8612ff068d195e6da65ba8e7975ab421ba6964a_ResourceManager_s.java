 /*
  * Copyright (C) 2013 Andrew Tulay
  * @mail: mhyhre@gmail.com
  * 
  * This work is licensed under a Creative Commons 
  * Attribution-NonCommercial-NoDerivs 3.0 Unported License.
  * You may obtain a copy of the License at
  *
  *		http://creativecommons.org/licenses/by-nc-nd/3.0/legalcode
  *
  */
 
 package mhyhre.lightrabbit;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.andengine.audio.sound.Sound;
 import org.andengine.audio.sound.SoundFactory;
 import org.andengine.opengl.font.Font;
 import org.andengine.opengl.font.FontFactory;
 import org.andengine.opengl.texture.ITexture;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.opengl.texture.region.TextureRegionFactory;
 
 import android.graphics.Color;
 import android.util.Log;
 
 /**
  *  .
  */
 public class ResourceManager {
 
 	/**  */
 	Map<String, ITextureRegion> regions;
 	Map<String, BitmapTextureAtlas> atlases;
 	Map<String, Font> fonts;
 	Map<String, Sound> sounds;
 
 	public ResourceManager() {
 		regions = new HashMap<String, ITextureRegion>();
 		atlases = new HashMap<String, BitmapTextureAtlas>();
 		fonts = new HashMap<String, Font>();
 		sounds = new HashMap<String, Sound>();
 	}
 
 	public ITextureRegion getTextureRegion(String key) {
 		if (!regions.containsKey(key))
 			Log.e(MainActivity.DebugID, "ResourceManager::getTextureRegion: invalid key - " + key);
 		return regions.get(key);
 	}
 
 	public BitmapTextureAtlas getTextureAtlas(String key) {
 		if (!atlases.containsKey(key))
 			Log.e(MainActivity.DebugID, "ResourceManager::getTextureAtlas: invalid key - " + key);
 		return atlases.get(key);
 	}
 
 	public Font getFont(String key) {
 		if (!fonts.containsKey(key))
 			Log.e(MainActivity.DebugID, "ResourceManager::getFont: invalid key - " + key);
 		return fonts.get(key);
 	}
 	
 	public Sound getSound(String key) {
 		if (!sounds.containsKey(key))
 			Log.e(MainActivity.DebugID, "ResourceManager::getSound: invalid key - " + key);
 		return sounds.get(key);
 	}
 
 
 	public void loadAtlases() {
 
 		BitmapTextureAtlas atlas;
 		ITextureRegion region;
 
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
 
 		//   
 		atlas = new BitmapTextureAtlas(MainActivity.Me.getTextureManager(), 512, 512, TextureOptions.BILINEAR);
 		BitmapTextureAtlasTextureRegionFactory.createFromAsset(atlas, MainActivity.Me, "User_Interface.png", 0, 0);
 		atlas.load();
 		atlases.put("User_Interface", atlas);
 		
 		region = TextureRegionFactory.extractFromTexture(atlas, 0, 0, 310, 70, false);
 		regions.put("Button1", region);
 		
 		region = TextureRegionFactory.extractFromTexture(atlas, 0, 160, 70, 60, false);
 		regions.put("ButtonVibration", region);
 		
 		region = TextureRegionFactory.extractFromTexture(atlas, 0, 230, 70, 60, false);
 		regions.put("ButtonSound", region);
 
 		Log.i(MainActivity.DebugID, "ResourceManager::loadAtlases: OK");
 	}
 	
 	public void loadSounds() {
 		SoundFactory.setAssetBasePath("sound/");
 
 		addSound("untitled.ogg", "roboClick");
 		addSound("SwitchOn.ogg", "switchOn");
 		addSound("error.ogg", "error");
 		addSound("yes_1.ogg", "yes1");
 		addSound("yes_2.ogg", "yes2");
 	}
 	
 	private void addSound(String filename, String name){
 		Sound snd = null;
 		try {
 			snd = SoundFactory.createSoundFromAsset(MainActivity.Me.getSoundManager(), MainActivity.Me.getApplicationContext(), filename);
 		} catch (IOException e) {
 			Log.e(MainActivity.DebugID, "ResourceManager::addSound: " + e.getMessage());
 			e.printStackTrace();
 		}
 		sounds.put(name, snd);
 	}
 
 	public void loadFonts() {
 
 		FontFactory.setAssetBasePath("font/");
 		Font mFont;
 
 		final ITexture TextureFontPixelWhite = new BitmapTextureAtlas(MainActivity.Me.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
 
 		mFont = FontFactory.createFromAsset(MainActivity.Me.getFontManager(), TextureFontPixelWhite, MainActivity.Me.getAssets(), "Furore.otf", 32, true, Color.WHITE);
 		mFont.load();
 		fonts.put("White Furore", mFont);
 		
 		Log.i(MainActivity.DebugID, "ResourceManager::loadFonts: OK");
 	}
 
 	public void LoadResourcesForPreloader() {
 
 		BitmapTextureAtlas atlas;
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
 
 		Font mFont;
 		FontFactory.setAssetBasePath("font/");
 
 		// press text
 		final ITexture TextureFontFurore = new BitmapTextureAtlas(MainActivity.Me.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
 
 		mFont = FontFactory.createFromAsset(MainActivity.Me.getFontManager(), TextureFontFurore, MainActivity.Me.getAssets(), "Furore.otf", 24, true, Color.BLACK);
 		mFont.load();
 		fonts.put("Furore", mFont);
 
 		// preloader texture;
 		atlas = new BitmapTextureAtlas(MainActivity.Me.getTextureManager(), 512, 512, TextureOptions.BILINEAR);
 		BitmapTextureAtlasTextureRegionFactory.createFromAsset(atlas, MainActivity.Me, "tex_01.png", 0, 0);
 		atlas.load();
 		atlases.put("tex_01", atlas);
 
 	}
 
 }
