 package com.chalmers.speedtype.view;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.BitmapFactory.Options;
 import android.hardware.Sensor;
 import android.util.AttributeSet;
 import com.chalmers.speedtype.R;
 import com.chalmers.speedtype.model.BalanceModel;
 import com.chalmers.speedtype.model.GameModel;
 import com.chalmers.speedtype.model.Word;
 import com.chalmers.speedtype.util.Util;
 
 public class BalanceView extends GameView {
 
 	private BalanceModel model;
 	private static final float ballDiameter = 0.006f;
 
 	private Bitmap ballImage;
 	private Bitmap boardImage;
 	private float xOrigin;
 	private float yOrigin;
 	
 	public BalanceView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 	}
 
 	public BalanceView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 	}
 
 	public BalanceView(Context context) {
 		super(context);
 	}
 
 	@Override
 	protected void onFinishInflate() {
 		super.onFinishInflate();
 
 		// Scaling of the ball
 		Bitmap ball = BitmapFactory.decodeResource(getResources(),
 				R.drawable.ball);
 		final int dstWidth = (int) (ballDiameter * Util.getMetersToPixelsX());
 		final int dstHeight = (int) (ballDiameter * Util.getMetersToPixelsY());
 		ballImage = Bitmap.createScaledBitmap(ball, dstWidth, dstHeight, true);
 
 		Options opts = new Options();
 		opts.inDither = true;
 		opts.inPreferredConfig = Bitmap.Config.RGB_565;
 		
 	}
 
 	public void setModel(GameModel model) {
 		super.setModel(model);
 		this.model = (BalanceModel) model;
 	}
 
 	@Override
 	protected void onDraw(Canvas canvas) {
 
 
 
 		final long now = model.getSensorTimeStamp() + (System.nanoTime() - model.getCpuTimeStamp());
 		final float sx = model.getSensorX();
 		
 		model.updateParticle(sx, now);
 		
 		final float xc = xOrigin;
 		final float yc = yOrigin;
 		final float xs = Util.getMetersToPixelsX();
 		final float ys = Util.getMetersToPixelsY();
 		final Bitmap bitmap = ballImage;
 		final float x = xc + model.getParticle().getPosX() * xs;
 		final float y = yc - model.getParticle().getPosY() * ys;
 		
 		canvas.drawBitmap(bitmap, x, y, null);
 		
 		Word activeWord = model.getActiveWord();
 		Word nextWord = model.getNextWord();
 		int CurrentCharPos = model.getCurrentCharPos();
 		
 		canvas.drawLine(0, getDisplayHeightFromPercentage(18), displayWidth-1, getDisplayHeightFromPercentage(18), bluePaint);
 		
 		drawScore(canvas);
 		drawNextWord(canvas, nextWord);
 		drawCompletedChars(canvas, activeWord, CurrentCharPos);
 		drawIncompletedChars(canvas, activeWord, CurrentCharPos);
 		drawActiveChar(canvas, activeWord, CurrentCharPos);
 	}
 
 	private void drawNextWord(Canvas canvas, Word nextWord) {
 		grayPaint.setTextSize(60);
 		grayPaint.setTypeface(mensch);
 		float x = getDisplayWidthFromPercentage(50) - grayPaint.measureText(nextWord.toString())
 				/ 2;
 		float y = grayPaint.getTextSize() + getDisplayHeightFromPercentage(20);
 		canvas.drawText(nextWord + "", x, y, grayPaint);
 	}
 
 	private void drawScore(Canvas canvas) {
 		whitePaint.setTextSize(40);
 		whitePaint.setTypeface(mensch);
 		int score = model.getScore();
 		float x = (displayWidth - whitePaint.measureText(score + "") - getDisplayWidthFromPercentage(2));
 		float y = whitePaint.getTextSize() + getDisplayHeightFromPercentage(2);
 		canvas.drawText(model.getScore() + "", x, y, whitePaint);
 
 	}
 
 	private void drawCompletedChars(Canvas canvas, Word activeWord,
 			int currentCharPos) {
 
		bluePaint.setTextSize(100);
		bluePaint.setTypeface(mensch);
 		float x = getDisplayWidthFromPercentage(50)
				- bluePaint.measureText(activeWord.toString()) / 2;
		float y = bluePaint.getTextSize() + getDisplayHeightFromPercentage(35);
 
 		canvas.drawText(activeWord.substring(0, currentCharPos), x, y,
				bluePaint);
 	}
 	
 	private void drawActiveChar(Canvas canvas, Word activeWord, int currentCharPos){
 		whitePaint.setTextSize(100);
 		whitePaint.setTypeface(mensch);
 		redPaint.setTextSize(100);
 		redPaint.setTypeface(mensch);
 		Paint activePaint = whitePaint;
 		
 		float x = getDisplayWidthFromPercentage(50) - whitePaint.measureText(activeWord.toString()) / 2 + whitePaint.measureText(activeWord.substring(0, currentCharPos));
		float y = bluePaint.getTextSize() + getDisplayHeightFromPercentage(35);
 		
 		if(model.correctInputReport() != true){
 			activePaint = redPaint;
 		}
 		
 		canvas.drawText(
 				activeWord.substring(currentCharPos, (currentCharPos + 1)), x,
 				y, activePaint);
 	}
 	
 	private void drawIncompletedChars(Canvas canvas, Word activeWord,
 			int currentCharPos) {
 
 		whitePaint.setTextSize(100);
 		whitePaint.setTypeface(mensch);
 
 		float x = getDisplayWidthFromPercentage(50)
 				- whitePaint.measureText(activeWord.toString())
 				/ 2
 				+ whitePaint.measureText(activeWord
 						.substring(0, currentCharPos + 1));
		float y = bluePaint.getTextSize() + getDisplayHeightFromPercentage(35);
 
 		canvas.drawText(
 				activeWord.substring(currentCharPos + 1, activeWord.length()), x,
 				y, whitePaint);
 	}
 
 	public void onAccuracyChanged(Sensor arg0, int arg1) {
 	}
 
 	@Override
 	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
 		// Calculate the middle of the screen
 		super.onSizeChanged(w, h, oldw, oldh);
 		xOrigin = (w - ballImage.getWidth()) * 0.5f;
 		yOrigin = (h - ballImage.getHeight()) * 0.5f;
 		model.setHorizontalBound(((w / Util.getMetersToPixelsX() - ballDiameter) * 0.5f));
 		model.setVerticalBound(((h / Util.getMetersToPixelsY() - ballDiameter) * 0.5f));
 	}
 }
