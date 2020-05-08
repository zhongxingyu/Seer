 package com.garlicg.sample.cutin;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Rect;
 import android.os.Handler;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.RelativeLayout;
 import android.widget.RelativeLayout.LayoutParams;
 
 import com.garlicg.cutinlib.CutinService;
 
 public class GarlinParade extends CutinService {
 	private ParadeView mView;
 	
 	@Override
 	protected View create() {
 		RelativeLayout layout = (RelativeLayout)LayoutInflater.from(this).inflate(R.layout.garlin_parade, null);
 		mView = new ParadeView(this);
		@SuppressWarnings("deprecation")
		RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
 		layout.addView(mView, 0, p);
 		return layout;
 	}
 
 	@Override
 	protected void start() {
 		mView.setOnParadeEndListener(new OnAnimationEndListener() {
 			@Override
 			public void endAnimation() {
 				finishCutin();
 			}
 		});
 		mView.startParade();
 	}
 
 	@Override
 	protected void destroy() {
 		mView.onDestroy();
 	}
 	
 	
 	private interface OnAnimationEndListener{
 		void endAnimation();
 	}
 	
 	private class ParadeView extends View{
 		private class Garlin{
 			private Rect src;
 			private Rect dest;
 			public Garlin(){
 			}
 		}
 		
 		private Bitmap mBitmap;
 		private Handler mHandler;
 		private Timer mTimer;
 		private OnAnimationEndListener mListener;
 		private long RATE = 1000/40;
 		private float mScreenWidth;
 		private float mDx;
 		private float mFinishDx;
 		private float mScreenHeight;
 		private int mGarlinWidth;
 		private int mGarlinRectX;
 		private int mGarlinRectY;
 		private Garlin[][] mGarlins;
 		private static final int MAX_TATE = 5;
 		private static final int MAX_YOKO = 15;
 		
 		public ParadeView(Context context) {
 			super(context);
 			mBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.garlin);
 			mTimer = new Timer(true);
 			mHandler = new Handler();
 		}
 		
 		
 		@Override
 		protected void onLayout(boolean changed, int left, int top, int right,
 				int bottom) {
 			super.onLayout(changed, left, top, right, bottom);
 			mScreenWidth = right -left;
 			mScreenHeight = bottom - top;
 			
 			mGarlinWidth = (int)(mScreenHeight / MAX_TATE);
 			mGarlinRectX = mGarlinWidth/2;
 			mGarlinRectY = mGarlinWidth;
 		}
 		
 		private void onDestroy(){
 			mTimer.purge();
 			mBitmap.recycle();
 		}
 		
 		@Override
 		protected void onDraw(Canvas canvas) {
 			super.onDraw(canvas);
 			if(mGarlins != null && !mBitmap.isRecycled()){
 				canvas.translate(mDx, 0);
 				int size1 = mGarlins.length;
 				for(int i = 0 ; i < size1 ; i++){
 					int size2 = mGarlins[i].length;
 					for(int s = 0 ; s < size2 ; s++){
 						Garlin garin = mGarlins[i][s];
 						// 描画
 						canvas.save();
 						canvas.rotate((float)(Math.random()*20)-10 , garin.dest.left,garin.dest.top);
 						canvas.drawBitmap(mBitmap, garin.src, garin.dest, null);
 						canvas.restore();
 					}
 				}
 			}
 		}
 		
 		private void startParade(){
 			mGarlins = new Garlin[MAX_TATE][MAX_YOKO];
 			for(int i = 0 ; i < MAX_TATE ; i++){
 				for(int s = 0 ; s< MAX_YOKO ; s++){
 					Garlin garlin = new Garlin();
 					garlin.src = new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
 					garlin.dest = new Rect(
 							mGarlinRectX*s, 
 							mGarlinRectY*i, 
 							mGarlinRectX*s+(int)(mGarlinWidth*1.5), 
 							mGarlinRectY*i+(int)(mGarlinWidth*1.5));
 					mGarlins[i][s] = garlin;
 				}
 			}
 			
 			mDx = -(mScreenWidth + mGarlinRectX * MAX_YOKO);
 			mFinishDx = mScreenWidth + mGarlinRectX * MAX_YOKO;
 			
			// invalidater
 			mTimer.schedule(new TimerTask() {
 				@Override
 				public void run() {
 					mHandler.post(new Runnable() {
 						@Override
 						public void run() {
 							if(mDx > mFinishDx){
 								mTimer.cancel();
 								mListener.endAnimation();
 							}
 							else{
 								invalidate();
 							}
 							mDx += 40;
 						}
 					});
 				}
 			}, 0,RATE);
 		}
 		
 		private void setOnParadeEndListener(OnAnimationEndListener listener){
 			mListener = listener;
 		}
 	}
 
 }
