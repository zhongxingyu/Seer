 package com.yugy.qianban.widget;
 
 import com.yugy.qianban.asisClass.FuncInt;
 import com.yugy.qianban.asisClass.LrcFormat;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Typeface;
 import android.util.AttributeSet;
 import android.view.View;
 
 public class LrcView extends View{
 	
 	private Paint mPaint;   //播放前和播放后歌词
 	private Paint sPaint;   //正在播放歌词
 	private float mX;		//暂存X轴中心
 	private float mY;      //暂存Y轴中心
 	private float currentPlayY;  //高亮部分的当前Y轴位置
 	private static int DY; //每行间隔
 	public int index = -1;
 	private LrcFormat lrcFormat;
 	
 	public LrcView(Context context) {
 		super(context);
 		init();
 	}
 	
 	public LrcView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		init();
 	}
 
 	public LrcView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		init();
 	}
 	
 	private void init(){
		DY = FuncInt.dp(getContext(), 25);
 		
 		mPaint = new Paint();
 		mPaint.setAntiAlias(true);
		mPaint.setTextSize(FuncInt.dp(getContext(), 16));
 		mPaint.setColor(Color.WHITE);
 		mPaint.setTypeface(Typeface.SERIF);
 		mPaint.setAlpha(100);
 		
 		sPaint = new Paint();
 		sPaint.setAntiAlias(true);
		sPaint.setTextSize(FuncInt.dp(getContext(), 16));
 		sPaint.setColor(Color.WHITE);
 		sPaint.setTypeface(Typeface.SERIF);
 		
 		lrcFormat = new LrcFormat();
 		lrcFormat.add(0, "歌词显示");
 	}
 	
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 		Paint mp = mPaint;
 		Paint sp = sPaint; 
 		mp.setTextAlign(Paint.Align.CENTER);
 		if(lrcFormat.getIndex() == -1){
 			return;
 		}
 		sp.setTextAlign(Paint.Align.CENTER);
 		if(index == 0){
 			//先画出正在播放部分
 			canvas.drawText(lrcFormat.getLrc(index), mX, mY, sp);
 			float tempY = mY;
 			//画出本句之前的句子
 			for(int i = index - 1; i >= 0; i --){
 				tempY = tempY - DY;
 				canvas.drawText(lrcFormat.getLrc(i), mX, tempY, mp);
 			}
 			tempY = mY;
 			//画出本句之后的句子
 			for(int i = index + 1; i < lrcFormat.getIndex(); i ++){
 				tempY = tempY + DY;
 				canvas.drawText(lrcFormat.getLrc(i), mX, tempY, mp);
 			}
 			currentPlayY = mY;
 		}
 		else if (index > 0) {
 			canvas.drawText(lrcFormat.getLrc(index), mX, currentPlayY, sp);
 			float tempY = currentPlayY;
 			for(int i = index - 1; i >= 0; i --){
 				tempY = tempY - DY;
 				canvas.drawText(lrcFormat.getLrc(i), mX, tempY, mp);
 			}
 			tempY = currentPlayY;
 			for(int i = index + 1; i < lrcFormat.getIndex(); i ++){
 				tempY = tempY + DY;
 				canvas.drawText(lrcFormat.getLrc(i), mX, tempY, mp);
 			}
 		}
 	}
 	protected void onSizeChanged(int w, int h, int ow, int oh){
 		super.onSizeChanged(w, h, ow, oh);
 		mX = w * 0.5f;
 		mY = h * 0.5f;
 	}
 	
 	public void setLrc(LrcFormat lrc){
 		lrcFormat = lrc;
 		index = 0;
 		invalidate();
 	}
 	
 	public long updateIndexReturnSleeptime(long time){
 		index = lrcFormat.getCurrentIndexFromTime(time);
 		if(index == -1)
 			return -1;
 		else if(index == -2){
 			return lrcFormat.getTime(0);
 		}
 		else
 			return lrcFormat.getSleeptimeFromIndex(index);
 	}
 	
 	public void clear(){   //清空当前歌词
 		lrcFormat.clear();
 		lrcFormat.add(0, "歌词显示");
 		index = 0;
 		invalidate();
 	}
 	
 	public int getIndex(){
 		return lrcFormat.getIndex();
 	}
 }
