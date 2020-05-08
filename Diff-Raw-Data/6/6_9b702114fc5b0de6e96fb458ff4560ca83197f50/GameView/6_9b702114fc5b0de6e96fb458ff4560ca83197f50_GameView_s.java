 package ruait.demo.androidanimation;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.view.SurfaceHolder;
 import android.view.SurfaceHolder.Callback;
 import android.view.SurfaceView;
 
 public class GameView extends SurfaceView implements Callback {
 	private Bitmap bm;
 	private SurfaceHolder holder;
 	private GameThread gameThread;
 	private int x = 0;
 
 	public GameView(Context context) {
 		super(context);
 		gameThread = new GameThread(this);
 		holder = getHolder();
 		holder.addCallback(this);
		bm = BitmapFactory.decodeResource(getResources(),
				R.drawable.ic_launcher);
 	}
 
 	@Override
 	public void draw(Canvas canvas) {
 		canvas.drawColor(Color.BLACK);
 		if (x < getWidth() - bm.getWidth()) {
 			x++;
 		}
 		canvas.drawBitmap(bm, 10, 10, null);
 	}
 
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		gameThread.setRunning(true);
 		gameThread.start();
 		// Canvas c = holder.lockCanvas();
 		// draw(c);
 		// holder.unlockCanvasAndPost(c);
 
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 	}
 }
