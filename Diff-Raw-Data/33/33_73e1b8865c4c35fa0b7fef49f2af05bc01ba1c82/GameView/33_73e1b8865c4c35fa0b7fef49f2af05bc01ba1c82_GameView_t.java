 package org.waldk33n3r.android.gnosis.games.frog;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Random;
 
 import org.waldk33n3r.android.gnosis.R;
import org.waldk33n3r.android.gnosis.games.Question;
import org.waldk33n3r.android.gnosis.games.QuestionDatabaseHandler;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Align;
 import android.graphics.RectF;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class GameView extends View {
 
 	private Paint paint;
 
 	private Bitmap lilly;
 
 	private ArrayList<LillyPad> lillies;
 
 	private Random rand;
 
 	private String[] strings;
 	
	QuestionDatabaseHandler db;
	
 	public GameView(Context context) {
 		super(context);
 		paint = new Paint();
 		lilly = BitmapFactory.decodeResource(getResources(), R.drawable.lilypad);
 		rand = new Random();
		db = new QuestionDatabaseHandler(getContext());
		Log.e("Blargh",""+db.getQuestionsCount());
 		lillies = new ArrayList<LillyPad>();
 		init();
 		Timer timer = new Timer(100, true, new Executable() {
 
 			@Override
 			public void exec(Object... obj) {
 				((View) obj[0]).post(new Runnable() {
 					@Override
 					public void run() {
 						invalidate();
 					}
 				});
 			}
 
 		}, this);
 		timer.start();
 	}
 
 	private void init() {
 		strings = new String[]{"Randomly generated","sizes (weighted)","and directions.","","Click a lilly pad to","make it disappear.","","Click the place again to","make it reappear"};
 		int dir1 = rand.nextInt(2);
 		int dir2 = rand.nextInt(2);
 		int dir3 = rand.nextInt(2);
 		int dir4 = rand.nextInt(2);
 		for (int i = 0; i < rand.nextInt(40) + 20; i++) {
 			int size = rand.nextInt(3) + 1;
 			size = size == 3 ? 200 : 100;
 			float x = (rand.nextInt(10) + 1) * 50;
 			float y = rand.nextInt(4) * 200 + 20;
 			boolean intersects = false;
 			for (Iterator<LillyPad> it = lillies.iterator(); it.hasNext();) {
 				LillyPad lilly = it.next();
 				RectF newRect = new RectF(x, y, x + size, y + size);
 				intersects = RectF.intersects(lilly.getRectF(), newRect);
 				if (intersects)
 					break;
 			}
 			if (!intersects) {
 				int dir;
 				int row = (int) ((y - 20) / 200);
 				if (row == 1)
 					dir = dir1;
 				else if (row == 2)
 					dir = dir2;
 				else if (row == 3)
 					dir = dir3;
 				else
 					dir = dir4;
 				lillies.add(new LillyPad(dir/* rand.nextInt(2) */, x, y, size, size, lilly));
 			}
 		}
 	}
 
 	@Override
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 
 		paint.setColor(Color.CYAN);
 		canvas.drawPaint(paint);
 
 		paint.setColor(Color.RED);
 		
 		for (int i = 0; i <= canvas.getHeight(); i += 10) {
 			canvas.drawLine(i, 0, i, canvas.getHeight(), paint);
 			canvas.drawLine(0, i, canvas.getWidth(), i, paint);
 		}
 
 		for (Iterator<LillyPad> it = lillies.iterator(); it.hasNext();) {
 			LillyPad lilly = it.next();
 			lilly.move(this, 5);
 			lilly.draw(canvas);
 		}
 		paint.setColor(Color.BLACK);
 		paint.setTextSize(36);
 		paint.setTextAlign(Align.CENTER);
		//for (int i = 0; i < strings.length; i++)
			//canvas.drawText(strings[i], canvas.getWidth() / 2, canvas.getHeight() / 4 + 30 * i, paint);
 
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		if (event.getAction() == MotionEvent.ACTION_DOWN) {
 			Log.w("Touch Event", String.format("Touched at %s, %s", event.getX(), event.getY()));
 			for (Iterator<LillyPad> it = this.lillies.iterator(); it.hasNext();) {
 				LillyPad lilly = it.next();
 				if (new RectF(lilly.getRectF()).contains(event.getX(), event.getY())) {
 					Log.w("Touch Event", "Lilly pad touched");
 					// lilly.setCoord(event.getX(), event.getY());
 					lilly.setVisible(!lilly.isVisible());
 					this.invalidate();
 				}
 			}
 		}
 		return true;
 	}
 
 	interface Executable {
 		public void exec(Object... obj);
 	}
 
 	class Timer extends Thread {
 		Executable func;
 		int wait;
 		boolean repeat;
 		Object[] params;
 
 		public Timer(int wait, boolean repeat, Executable func, Object... params) {
 			this.wait = wait;
 			this.repeat = repeat;
 			this.func = func;
 			this.params = params;
 		}
 
 		@Override
 		public void run() {
 			do {
 				try {
 					super.sleep(this.wait);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				func.exec(params);
 			} while (repeat);
 		}
 	}
 
 }
