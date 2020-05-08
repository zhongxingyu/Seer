 package com.benjaminlanders.taptorun.helper;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.audio.Music;
 import com.badlogic.gdx.audio.Sound;
 
 public class SoundManager {
 
 	public static Sound jump, select;
 	public static Music music;
 	public static void init()
 	{
 		jump = Gdx.audio.newSound(Gdx.files.internal("sounds/jump.wav"));
 		select = Gdx.audio.newSound(Gdx.files.internal("sounds/select.wav"));
		music = Gdx.audio.newMusic(Gdx.files.internal("sounds/music.wav"));
 		music.setLooping(true);
 		music.play();
 	}
 	public static void playJump()
 	{
 		jump.play(.5f);
 	}
 	public static void playSelect()
 	{
 		//select.play();
 	}
 
 }
