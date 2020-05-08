 package com.Redream.TinyConflict.Resources;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
 
 public class Resources {
 	public static TextureAtlas atlas;
 
 	public static boolean[] flipX = new boolean[500];
 	public static boolean[] flipY = new boolean[500];
 
 	public static AtlasRegion[] regions = new AtlasRegion[500];
 
 	public static BitmapFont font = new BitmapFont(file("volter.fnt"),file("volter.png"),false);
 	
 	public static Sound pew = Gdx.audio.newSound(file("pew.wav"));
 	public static Sound blip = Gdx.audio.newSound(file("blip.wav"));
 	public static Sound xplode = Gdx.audio.newSound(file("xplode.wav"));
 	
 	public static FileHandle file(String src) {
 		FileHandle fh;
		fh = Gdx.files.internal("com/Redream/TinyConflict/Resources/" + src);
 		if(!fh.exists())fh = Gdx.files.internal(src);
 		return fh;
 	}
 
 }
