 package com.example.tetris;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.GestureDetector;
 import android.view.GestureDetector.SimpleOnGestureListener;
 import android.view.MotionEvent;
 import android.widget.Toast;
 
 import com.example.ccweibo.R;
 
 /**
  * View is in charge of listening gesture and initialize View
  * 
  * @author flamearrow
  * 
  */
 public class TetrisActivity extends Activity {
 	private GestureDetector _gesDect;
 	private TetrisView _tetrisView;
 	private TetrisActivity _act = this;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_tetris);
 		_tetrisView = (TetrisView) findViewById(R.id.tetrisActivity);
 		// can also let TetrisActivity implements OnGestureListener, but that
 		// will leave some blank methods
 		_gesDect = new GestureDetector(this, new SimpleOnGestureListener() {
 			@Override
 			public boolean onDoubleTap(MotionEvent e) {
 				Toast.makeText(_act, "doubleTap!!", Toast.LENGTH_SHORT).show();
 				_tetrisView.rotate();
 				return true;
 			}
 
 			@Override
 			public boolean onSingleTapConfirmed(MotionEvent e) {
 				Toast.makeText(_act, "singleTap!!", Toast.LENGTH_SHORT).show();
 				_tetrisView.moveLR(e);
 				return super.onSingleTapConfirmed(e);
 			}
 		});
 	}
 
 	// called when this method is sent background, should either pause the
 	// thread or release resources
 	@Override
 	protected void onPause() {
 		super.onPause();
 		_tetrisView.pause();
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		_tetrisView.releaseResources();
 	}
 
 	// now support this: move left/right when single table left/right part of
 	// the screen
 	// rotate clockwise when double tap
 	// drop when swipe down
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		// Toast can be used to make a little quick text message box
 		// Toast.makeText(this, "mlgb don't touch me!!", Toast.LENGTH_SHORT)
 		// .show();
 		// explicitly call onTouchEvent - it's a bit weird
 		return _gesDect.onTouchEvent(event);
 	}
 
 }
