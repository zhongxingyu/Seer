 package com.nomath4u.breakbrick;
 
 import android.graphics.RectF;
import android.media.AudioManager;
import android.util.Log;
 
 public class ExtraLife {
 	public RectF image;
 	public PhysVector velocity;
 	private static int width = 7;
 	private static int height = 7;
 	private SurfacePanel panel;
 	ExtraLife(int left, int top, SurfacePanel pan)
 	{
 		this.velocity = new PhysVector(2, 270);
 		this.image = new RectF(left, top, left + 7, top + 7);
 		this.panel = pan;
 	}
 	
 public void tick(){
 	image.offset(0, -(float)velocity.speedY());
 	if(image.intersect(panel.mainPaddle.selfimage)){
 		panel.parent.addLife(1);
         panel.parent.rowlives++;
        AudioManager audioManager = (AudioManager) panel.parent.getSystemService(panel.parent.AUDIO_SERVICE);
        float actualVolume = (float) audioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = (float) audioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float volume = actualVolume / maxVolume;
        // Is the sound loaded already?
        if (panel.parent.loaded) {
            panel.parent.pool.play(panel.parent.soundIDb, volume, volume, 1, 0, 1f);
            Log.e("Test", "Played sound");
        }
         if(panel.parent.rowlives >= 5){
             panel.parent.mOutbox.mlife1Achievement = true;
         }
 		/*Remove self*/
 		panel.eLife = null;
 	}
 	
 	if(image.bottom > panel.mainPaddle.screenheight){
 		/*Remove self*/
         panel.parent.rowlives = 0; //Missed one restart
 		panel.eLife = null;
 	}
 	
 }
 }
 	
 
