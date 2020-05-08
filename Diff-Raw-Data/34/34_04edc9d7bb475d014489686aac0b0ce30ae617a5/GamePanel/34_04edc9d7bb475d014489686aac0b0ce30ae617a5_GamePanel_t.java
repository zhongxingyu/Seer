 package com.game.mobileblocks;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 import com.game.Units.Block;
 import com.game.Units.WaveyBlock;
 
 public class GamePanel extends SurfaceView implements SurfaceHolder.Callback
 {
 	private static final String TAG = GamePanel.class.getSimpleName();
 	private GameThread thread; 
 	private List<Block> blockList = new ArrayList<Block>();
 	private final int TOTAL_BLOCKS = 10;
	public static int height, width;
 	private long last;
 	private ColorPicker colorPicker = new  ColorPicker();
 	private Player player;
 	private Level level;
 	
 	public GamePanel(Context context) {
 		super(context);
 		// adding the callback (this) to the surface holder to intercept events
 		getHolder().addCallback(this);
 
 		// create blocks and load bitmap
 		// create the game loop thread
 		thread = new GameThread(getHolder(), this);
 
 		// make the GamePanel focusable so it can handle events
 		setFocusable(true);
 	}
 
 
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 		// TODO Auto-generated method stub
 
 	}
 
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) 
 	{
 		// TODO Auto-generated method stub
 		thread.setRunning(true);
 		thread.start();
 	}
 
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		// TODO Auto-generated method stub
 		try {
 			thread.join();
 		} catch (InterruptedException e) {
 			// try again shutting down the thread
 		}
 
 	}
 
 	public void update() {
 
 		synchronized (blockList) {
 			if (System.currentTimeMillis() - last > 1000) {
 				if(blockList.size() < TOTAL_BLOCKS)
 				{
 					genRandomBlocks();
 				}
 				last = System.currentTimeMillis();
 			}
 			for (Block block : blockList)
 			{
 				block.update(2000);
 			}
 		}
 
 	}
 	
 	public void genRandomBlocks() {
 		for (Block block : level.randomBlocks()) {
 			this.blockList.add(block);
 		}
 	}
 	
 	public boolean onTouchEvent(MotionEvent event)
 	{
 		boolean retVal = false;
 		if (event.getAction() == MotionEvent.ACTION_DOWN)
 		{
 			synchronized (blockList) {
 				for (Block block : blockList) {
 					retVal = block.isCollision((int)event.getX(), (int)event.getY());
 					if(retVal)
 					{
 						blockList.remove(block);
 						break;
 					}
 				}
 			}
 		}
 		return false;
 
 	}	
 
 	public void render(Canvas canvas)
 	{
		canvas.drawColor(Color.BLACK);
 		synchronized (blockList) {
 			for (Block block : blockList) {
 				block.draw(canvas);
 			}
 		}
 	}
 
 
 	public void updateSize(int w, int h) {
 		this.height = h;
 		this.width  = w;
 		level = new Level(width);
 	}
 	
 }
