 package com.example.tetris;
 
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.os.Vibrator;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class Tetris_view extends View {
 
 	public Vibrator vibrator;
 	private Rect bounds;
 	private final int rows = 12;
 	private final int columns = 20;
 	private int Score;
 	private Timer timer = null;
 	private MoveTask strafetask;
 	private Figure main_figure;
 	private Figure next_figure;
 	private int new_figure;
 	private int[][] pool;
 	private boolean action=false; 
 	private boolean game_is_over = false;
 	private boolean pause = false;
 	private int block_height;
 	private int block_width;
 	private Paint paint;
 	private Bitmap bitmaps[];
 	private boolean can_vibrate = true;
	private Rect src = new Rect(0,0,24,24);
 	private Rect dst = new Rect();
 	
 	
 	public Tetris_view(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		// TODO Auto-generated constructor stub
 		init();
 		vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
 		}
 
 
 	public void init()
 	{
 		bitmaps = loadBitmaps();
 		timer  = new Timer();
 		MoveTask task = new MoveTask(MoveTask.MOVE_DOWN);
 		pool = new int[rows+8][columns+8];
 		game_is_over = false;
 		new_figure = 0;
 		Score = 0;
 		for (int i=4;i<rows+4;i++)
 			for (int j=4;j<columns+4;j++)
 				pool[i][j] = 0;
 		for (int j=4;j<columns+6;j++)
 			{
 				pool[3][j] = 1;
 				pool[rows+4][j] = 1; 
 			}
 		for (int i=0;i<rows+4;i++)
 				pool[i][3] = 1;
 		next_figure = new Figure();
 		main_figure = new Figure();
 		paint = new Paint();
 		bounds = new Rect();
 		timer.schedule(task, 300, 400);
 	}	
 	private Bitmap[] loadBitmaps()
 	{
 		Bitmap parentbitmap = BitmapFactory.decodeResource(getResources(),R.drawable.block);
 		Bitmap[] bitmaps = new Bitmap[15];
 		for (int i=0;i<3;i++)
 			for (int j=0;j<5;j++)
				bitmaps[i*5+j] = Bitmap.createBitmap(parentbitmap,j*24,i*24,24,24);
 		return bitmaps;
 	}
 	public void onDraw(Canvas canvas)
 	{
 		canvas.getClipBounds(bounds);
 		block_height = bounds.height()/columns;
 		block_width = bounds.width()/(rows+4);
 		paint.setColor(Color.BLACK);
 		canvas.drawColor(Color.rgb(53, 53, 67));
 		for (int i=4;i<rows+4;i++)
 			for (int j=4;j<columns+4;j++)			
 				if (pool[i][j]!=0) 
 				{
 					dst.set((i-4)*block_width+1, bounds.height()-(j-3)*block_height+1, 
 									(i-3)*block_width-1, bounds.height()-(j-4)*block_height-1);
 					canvas.drawBitmap(bitmaps[pool[i][j]-1], src, dst, paint);
 				}
 		for (int i=0;i<4;i++) 
 			{
 					dst.set((main_figure.data[i][0]+main_figure.x-4)*block_width+1, 
 							bounds.height()-(main_figure.data[i][1]-3+main_figure.y)*block_height+1,
 							(main_figure.data[i][0]-3+main_figure.x)*block_width-1, 
 							bounds.height()-(main_figure.data[i][1]-4+main_figure.y)*block_height-1);
 					canvas.drawBitmap(bitmaps[main_figure.colors[i]-1], src, dst, paint);
 			}
 		paint.setColor(Color.GRAY);
 		canvas.drawRect(block_width*rows, 0, bounds.width(), bounds.height(), paint);
 		paint.setColor(Color.BLACK);
 		for (int i=0;i<4;i++)
 				canvas.drawRect((rows+next_figure.data[i][0])*block_width+1, 
 						bounds.height()-(columns+next_figure.data[i][1]-4)*block_height+1, 
 						(rows+1+next_figure.data[i][0])*block_width-1, 
 						bounds.height()-(columns-5+next_figure.data[i][1])*block_height-1, paint);
 		paint.setColor(Color.GREEN);
 		paint.setTextSize(block_height);
 		canvas.drawText(Integer.toString(Score), rows*block_width, 7*block_height, paint);
 		if (game_is_over)	canvas.drawText("Game Over", 10, bounds.height()/2, paint);
 		if (pause)	canvas.drawText("Pause", 10, bounds.height()/2, paint);
 	}
 	public boolean onTouchEvent(MotionEvent me)
 	{
 		if ((!game_is_over)&&(!pause))
 		{
 		if (me.getAction()==MotionEvent.ACTION_DOWN)
 		{
 			action = true;
 			float x = me.getX();
 			float y = me.getY();
 			int height = bounds.height();
 			int width = bounds.width();
 			if (y<3*height/4)
 			{
 				if (x<=width/5)
 				{
 					strafetask = new MoveTask(MoveTask.MOVE_LEFT);
 					timer.schedule(strafetask, 0, 200);
 				}
 				else if (x>=4*width/5)
 				{
 					strafetask = new MoveTask(MoveTask.MOVE_RIGHT);
 					timer.schedule(strafetask, 0, 200);
 				} 
 				else
 				{
 					strafetask = new MoveTask(MoveTask.MOVE_ROTATE);
 					timer.schedule(strafetask, 0, 400);
 				} 
 			}
 			else 
 				{
 					strafetask = new MoveTask(MoveTask.MOVE_DROP);
 					timer.schedule(strafetask, 0);
 				} 
 			action = false;
 		if (can_vibrate)
 			vibrator.vibrate(20);
 		invalidate();
 		}
 		if (me.getAction()==MotionEvent.ACTION_UP)
 		{
 			strafetask.cancel();
 		}
 		}
 		return true;
 	}
 	
 	private class MoveTask extends TimerTask {
 		int move;
 		static final int MOVE_LEFT = 0;
 		static final int MOVE_ROTATE = 1;
 		static final int MOVE_RIGHT = 2;
 		static final int MOVE_DOWN = 3;
 		static final int MOVE_DROP = 4;
 		public MoveTask(int c)
 		{
 			move = c;
 		}
 		@Override
 		public void run() {
 			// TODO Auto-generated method stub
 			switch (move)
 			{
 			case MOVE_LEFT:
 				main_figure.move_left(pool);
 				break;
 			case MOVE_RIGHT:
 				main_figure.move_right(pool);
 				break;
 			case MOVE_ROTATE:
 				main_figure.rotate(pool);
 				break;
 			case MOVE_DROP:
 				main_figure.drop(pool);
 				break;
 			case MOVE_DOWN:
 				{
 				while (action)
 					;
 				if (main_figure.down(pool) == false)
 				{
 					if (new_figure == 0)
 						new_figure = 2;
 					else new_figure--;
 				}
 				if (new_figure == 1) 
 				{
 					main_figure.print(pool);
 					delete();
 					main_figure = new Figure(next_figure);
 					next_figure = new Figure();
 					new_figure = 0;
 				}
 				postInvalidate();
 				}
 			}
 			postInvalidate();
 		}
 	}
 	private void delete()
 	{
 			action = true;
 			int x = 0;
 			boolean combo; 
 			for (int j=4;j<columns+4;j++)
 			{
 				combo = true;
 				for (int i=4;i<rows+4;i++)
 					if (pool[i][j] == 0) combo = false;
 				if (combo)
 				{
 					for (int k=j;k<columns+4;k++)
 						for (int l=4;l<rows+4;l++)
 							pool[l][k] = pool[l][k+1];
 					x++;
 					j--;
 				}
 			}			
 			switch (x)
 			{
 			case 1:Score+=100; break;
 			case 2:Score+=300; break;
 			case 3:Score+=700; break;
 			case 4:Score+=1500; break;
 			}
 			if ((x!=0)&&(can_vibrate))
 				vibrator.vibrate(50);
 			action = false;
 			postInvalidate();
 	}	
 	
 	public void Game_Over()
 	{
 		timer.cancel();
 		game_is_over = true;
 		postInvalidate();
 	}
 	void Pause()
 	{
 		if (!game_is_over)
 		if (!pause)
 		{
 			timer.cancel();
 			pause = true;
 			postInvalidate();
 		}		
 	}
 	void unPause()
 	{
 		if (!game_is_over)
 		if (pause)
 		{
 			timer.cancel();
 			timer = new Timer();
 			MoveTask task = new MoveTask(MoveTask.MOVE_DOWN);
 			timer.schedule(task, 0, 400);
 			pause = false;
 		}
 	}
 	void switchPause()
 	{
 		if (pause) unPause();
 		else Pause();
 	}
 	void switch_vibration()
 	{
 		can_vibrate = !can_vibrate;
 	}
 	private class Figure {
 		private int colors[] = new int[4];
 		private int data[][];
 		private int x,y;
 		public Figure()
 		{
 			x = rows/2+2;
 			y = columns+3;
 			Random r = new Random();
 			int type = (int)r.nextInt(7);
 			switch (type)
 			{
 			case 0: data = new int[][] {{1,0},{1,1},{1,2},{1,3}}; break;
 			case 1: data = new int[][] {{0,3},{0,2},{1,2},{2,2}}; break;
 			case 2: data = new int[][] {{1,1},{2,1},{3,1},{3,2}}; break;
 			case 3: data = new int[][] {{1,1},{1,2},{2,1},{2,2}}; break;
 			case 4: data = new int[][] {{0,1},{1,1},{1,2},{2,2}}; break;
 			case 5: data = new int[][] {{0,1},{1,1},{2,1},{1,2}}; break;
 			case 6: data = new int[][] {{0,2},{1,2},{1,1},{2,1}}; break;
 			}
 			for (int i=0;i<4;i++)
 				colors[i] = r.nextInt(bitmaps.length)+1;
 			if (crossing(pool))
 			{
 				Game_Over();
 			}
 		}
 		public Figure(Figure copy)
 		{
 			data = new int[4][2];
 			for (int i=0;i<4;i++)
 			{
 				data[i][0] = copy.data[i][0];
 				data[i][1] = copy.data[i][1];
 			}
 			x = copy.x;
 			y = copy.y;
 			for (int i=0;i<4;i++)
 				colors[i] = copy.colors[i];
 		}
 		public boolean rotate(int[][] pool)
 		{
 			int buf;
 			Figure figure = new Figure(this);
 			for (int i = 0;i<4;i++)
 			{
 				buf = figure.data[i][0];
 				figure.data[i][0] = figure.data[i][1];
 				figure.data[i][1] = (int) (3-buf);
 			}
 			if (!figure.crossing(pool))
 				for (int i = 0;i<4;i++)
 				{
 					buf = data[i][0];
 					data[i][0] = data[i][1];
 					data[i][1] = (int) (3-buf);
 				}
 			else return false;
 			return true;
 		}
 		public boolean move_left(int[][]pool)
 		{
 			x--;
 			if (crossing(pool))
 				x++;
 			else return true;
 			return false;
 		}
 		public boolean move_right(int[][] pool)
 		{
 			x++;
 			if (crossing(pool))
 				x--;
 			else return true;
 			return false;
 		}
 		public boolean down(int[][] pool)
 		{
 			Figure figure = new Figure(this);
 			figure.y--;
 			if (figure.crossing(pool))
 			{
 				if (!figure.move_left(pool))
 				{
 					if (!figure.move_right(pool))
 					{
 						new_figure = 1;
 						return true;
 					}
 				}
 				return false;
 			}
 			else y--;
 			return true;
 		}
 		public void drop(int[][] pool)
 		{
 			do
 				y--;
 			while (!crossing(pool));
 			y++;
 		}
 		public boolean crossing(int[][] pool)
 		{
 			for (int i=0;i<4;i++)
 					if (pool[data[i][0]+x][data[i][1]+y]!=0) return true;
 			return false;
 		}
 		public void print(int[][] pool)
 		{
 			for (int i=0;i<4;i++)
 				pool[data[i][0]+x][data[i][1]+y]=colors[i];
 		}
 	}
 }
