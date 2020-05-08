 package com.room;
 
 import java.util.StringTokenizer;
 
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.RectF;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.MotionEvent;
 
 import com.room.R;
 import com.room.media.MSoundManager;
 import com.room.scene.SLayout;
 import com.room.scene.SLayoutLoader;
 import com.room.scene.SSceneActivity;
 import com.room.utils.UBitmapUtil;
 
 public class Options extends SSceneActivity {
 	//
 	// UI specific
 	public static final int NUM_OPTIONS = 3;
 	public static final int REL_KEY_SIZE = 8;
 	private static final String TAG = "Options";
 	private RectF[] checkboxArea = new RectF[NUM_OPTIONS];
 	private RectF[] barArea = new RectF[NUM_OPTIONS];
 	private Bitmap checkImage;
 	private Bitmap keyImage;
 	private float checkboxWidth, checkboxHeight;
 	private float barWidth;
 	private float keyWidth, keyHeight;
 
 	//
 	// Movement specific
 	private int touchedBar;
 	private int touchedKey;
 	private float prevX;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setLayout(SLayoutLoader.getInstance().options);
 		showInventory(false);
 		for (int i = 0; i < NUM_OPTIONS; ++i) {
 			checkboxArea[i] = getBoxPixelCoords("checkbox_" + i);
 			barArea[i] = getBoxPixelCoords("bar_" + i);
 		}
 		checkboxWidth = checkboxArea[0].right - checkboxArea[0].left;
 		checkboxHeight = checkboxArea[0].bottom - checkboxArea[0].top;
 		barWidth = barArea[0].right - barArea[0].left;
 		keyWidth = (barArea[0].right - barArea[0].left) / REL_KEY_SIZE;
 		keyHeight = barArea[0].bottom - barArea[0].top;
 		checkImage = UBitmapUtil.loadScaledBitmap(R.drawable.options_check,
 				(int) checkboxWidth, (int) checkboxHeight,false);
 		keyImage = UBitmapUtil.loadScaledBitmap(R.drawable.options_key,
 				(int) keyWidth, (int) keyHeight,false);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		setBackgroundImage(R.drawable.options);
 	}
 
 	@Override
 	public void onDraw(Canvas canvas, Paint paint) {
 		super.onDraw(canvas, paint);
 		if (OptionManager.isMusicEnabled()) {
 			canvas.drawBitmap(checkImage, checkboxArea[0].left,
 					checkboxArea[0].top, paint);
 			canvas.drawBitmap(checkImage, checkboxArea[1].left,
 					checkboxArea[1].top, paint);
 		}
 		if (OptionManager.isSoundEnabled()) {
 			canvas.drawBitmap(checkImage, checkboxArea[0].left,
 					checkboxArea[0].top, paint);
 			canvas.drawBitmap(checkImage, checkboxArea[2].left,
 					checkboxArea[2].top, paint);
 		}
 		for (int i = 0; i < NUM_OPTIONS; ++i)
 			canvas.drawBitmap(keyImage, getLeftKeyPosn(i), barArea[i].top, paint);
 	}
 
 	@Override
 	public void onBoxDown(SLayout.Box box, MotionEvent event) {
 		StringTokenizer st = new StringTokenizer(box.name, "_");
 		String clicked = st.nextToken();
 		if (clicked.equals("checkbox")) {
 			MSoundManager.getInstance().playSoundEffect(R.raw.tick);
 			int check = Integer.parseInt(st.nextToken());
 			boolean curState;
 			switch (check) {
 			case 0: // master
 				curState = OptionManager.isMusicEnabled();
 				OptionManager.setMusic(!curState);
 				OptionManager.setSound(!curState);
 				if (!OptionManager.isMusicEnabled())
 					MSoundManager.getInstance().stopMusic();
 				else
 					MSoundManager.getInstance().playMusic(R.raw.music_menu);
 				break;
 			case 1: // music
 				curState = OptionManager.isMusicEnabled();
 				OptionManager.setMusic(!curState);
 				if (!OptionManager.isMusicEnabled())
 					MSoundManager.getInstance().stopMusic();
 				else
 					MSoundManager.getInstance().playMusic(R.raw.music_menu);
 				break;
 			case 2: // sound effect
 				curState = OptionManager.isSoundEnabled();
 				OptionManager.setSound(!curState);
 			}
 		} else if (clicked.equals("bar")) {
 			touchedBar = Integer.parseInt(st.nextToken());
 			prevX = event.getRawX();
			touchedKey = touchedBar;
			if (!isTouchingKey(touchedBar, prevX)){
 				float newVolume = (prevX - barArea[0].left) / barWidth;
 				setVolume(touchedBar, newVolume);
 				if (touchedKey != 2)
 					MSoundManager.getInstance().updateMusicVolume();
 			}
 		}
 		repaint();
 	}
 
 	@Override
 	public void onBoxMove(SLayout.Box box, MotionEvent event) {
 		StringTokenizer st = new StringTokenizer(box.name, "_");
 
 		if (!st.nextToken().equals("bar"))
 			return;
 		if (touchedKey == -1)
 			return;
 
 		float newVolume = Math.max(Math.min((event.getRawX() - barArea[0].left) / barWidth, 1.0f),0.0f);
 		setVolume(touchedKey, newVolume);
 		if (touchedKey != 2)
 			MSoundManager.getInstance().updateMusicVolume();
 		repaint();
 	}
 
 	@Override
 	public void onBoxRelease(SLayout.Box box, MotionEvent event) {
 		if (touchedKey != -1) {
 			MSoundManager.getInstance().playSoundEffect(R.raw.tick);
 			touchedKey = -1;
 		}
 	}
 
 	public float getLeftKeyPosn(int key) {
 		if (key == 0)
 			return OptionManager.getMasterVolume() * (barWidth - keyWidth)
 					+ barArea[0].left;
 		if (key == 1)
 			return OptionManager.getMusicVolume() * (barWidth - keyWidth)
 					+ barArea[0].left;
 		if (key == 2)
 			return OptionManager.getSoundVolume() * (barWidth - keyWidth)
 					+ barArea[0].left;
 		Log.e(TAG, "Error in getLeftKeyPosn");
 		return 0;
 	}
 
 	public float getRightKeyPosn(int key) {
 		return getLeftKeyPosn(key) + keyWidth;
 	}
 
 	public boolean isTouchingKey(int key, float xPosn) {
 		return xPosn >= getLeftKeyPosn(key) && xPosn <= getRightKeyPosn(key);
 	}
 
 	public void setVolume(int key, float volume) {
 		switch (key) {
 		case 0:
 			OptionManager.setMasterVolume(volume);
 			break;
 		case 1:
 			OptionManager.setMusicVolume(volume);
 			break;
 		case 2:
 			OptionManager.setSoundVolume(volume);
 		}
 	}
 
 	public float getVolume(int key) {
 		switch (key) {
 		case 0:
 			return OptionManager.getMasterVolume();
 		case 1:
 			return OptionManager.getMusicVolume();
 		case 2:
 			return OptionManager.getSoundVolume();
 		}
 		return 0;
 	}
 }
