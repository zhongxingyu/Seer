 package com.estudio.cheke.game.gstb;
 /*
  * Created by Estudio Cheke, creative purpose.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  * The music was taked from 
  * http://www.musicloops.com/classical/info_c.php?page=optional_page
  */
 
 import android.content.Context;
 import android.media.MediaPlayer;
 import android.os.PowerManager;
 
 public class SoundManager{
 
 	static MediaPlayer mMediaPlayer= new MediaPlayer();
 	private static Context mContext;
 	static private SoundManager _instance;
 	public static int music=0; 
 
 	private SoundManager(){}
 	static synchronized public SoundManager getInstance(){
 		if (_instance == null) 
 			_instance = new SoundManager();
 		return _instance;
 	}
 	public static  void initSounds(Context theContext){
 		mContext = theContext;
 		getInstance();
 	}
 	public static void playSound(int song, boolean loop) {
		if(mMediaPlayer!=null){
 			stop();
 		}
 		music=song;
 		switch(song){
 		case 0:
 			mMediaPlayer = MediaPlayer.create(mContext, R.raw.pachbel0);
 			mMediaPlayer.setLooping(loop);
 			mMediaPlayer.start();
 			break;
 		case 1:
 			mMediaPlayer = MediaPlayer.create(mContext, R.raw.bach_14);
 			mMediaPlayer.setLooping(loop);
 			mMediaPlayer.start();
 			break;
 		case 2:
 			mMediaPlayer = MediaPlayer.create(mContext, R.raw.bach_4);
 			mMediaPlayer.setLooping(loop);
 			mMediaPlayer.start();
 			break;
 		case 3:
 			mMediaPlayer = MediaPlayer.create(mContext, R.raw.bach_13p);
 			mMediaPlayer.setLooping(loop);
 			mMediaPlayer.start();
 			break;
 		case 4:
 			mMediaPlayer = MediaPlayer.create(mContext, R.raw.pachebel);
 			mMediaPlayer.setLooping(loop);
 			mMediaPlayer.start();
 			break;
 		}
 		mMediaPlayer.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK);//intentar que no se apague la musica sola
 		if(mMediaPlayer!=null){
 			float volumen=100;
 			mMediaPlayer.setVolume(volumen, volumen);
 		}
 	}
 	public static void setVolume(float volumen){
 		if(mMediaPlayer != null){
 			mMediaPlayer.setVolume(volumen, volumen);
 		}
 	}
 	public static void stop() {
 		if (mMediaPlayer != null) {
 			mMediaPlayer.stop();
 		}
 	}
 	public static void pause() {
 		if (mMediaPlayer != null) {
 			mMediaPlayer.pause();
 		}
 	}
 	public static void resume() {
		if (mMediaPlayer != null) {
 			mMediaPlayer.start();
 		}
 	}
 	public static boolean songPlaying(){
 		boolean play=false;
 		if (mMediaPlayer != null) {
 			if (mMediaPlayer.isPlaying()) {
 				play=true;
 			}
 		}
 		return play;
 	}
 	public static void cleanup() {
 		if (mMediaPlayer != null) {
 			if (mMediaPlayer.isPlaying()) {
 				mMediaPlayer.stop();
 			}
 			mMediaPlayer.release();
 			mMediaPlayer = null;
 		}
 	}
 }
