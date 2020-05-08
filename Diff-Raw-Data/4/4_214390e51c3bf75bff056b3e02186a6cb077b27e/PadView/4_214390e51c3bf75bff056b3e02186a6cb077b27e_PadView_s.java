 package com.zhideel.tapathon.ui;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.media.MediaPlayer;
 import android.os.Handler;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.View;
 import com.zhideel.tapathon.Config;
 import com.zhideel.tapathon.R;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 public class PadView extends View {
 
     public enum GameLevel {
         EASY, MEDIUM, HARD;
     }
 
     // Usually this can be a field rather than a method variable
     private static Random rand = new Random();
     private Paint mPaint;
     private int[] colors = {getResources().getColor(R.color.tappad_cyan), Color.MAGENTA, getResources().getColor(R.color.tappad_red), getResources().getColor(R.color.tappad_yellow)};
     private boolean isSelected;
     private boolean isPaused = false;
     private Paint textPaint;
     private String currentText;
     private boolean isDividedByTwo;
     public static GameLevel selectedLevel;
     public static int maxNextQuestionDelay;
     private int minDelay, maxDelay;
     private boolean startGame = false;
 
     public PadView(Context context, AttributeSet attrs) {
         super(context, attrs);
         initView();
     }
 
     public static void setLevel(GameLevel level) {
         selectedLevel = level;
     }
 
     public void setPaused(boolean paused) {
         this.isPaused = paused;
         if (isPaused == false) {
             randomPaint();
         }
     }
 
     private void initView() {
         mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
         // set painter color to a color you like
         mPaint.setColor(Color.WHITE);
         mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
 
         textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
         textPaint.setColor(Color.WHITE);
         textPaint.setShadowLayer(5.0f, 5.0f, 5.0f, Color.BLACK);
         textPaint.setTextAlign(Paint.Align.CENTER);
 
         textPaint.setTextSize(Config.getDipfromPixels(50));
 
         if (startGame == true) {
             minDelay = 0;
             maxDelay = 0;
             startGame = false;
         } else {
             if (selectedLevel == GameLevel.EASY) {
                 maxNextQuestionDelay = 15000;
                 minDelay = 4000;
                 maxDelay = 6000;
             } else if (selectedLevel == GameLevel.MEDIUM) {
                 maxNextQuestionDelay = 10000;
                 minDelay = 3500;
                 maxDelay = 4500;
             } else if (selectedLevel == GameLevel.HARD) {
                 maxNextQuestionDelay = 5000;
                 minDelay = 2000;
                 maxDelay = 3500;
             }
         }
         randText();
         randomPaint();
     }
 
     private void randomPaint() {
         new Handler().postDelayed(new Runnable() {
             public void run() {
                 doThePaint();
             }
         }, getRandomDelay());
     }
 
     private void doThePaint()
     {
         if ((!isSelected) && (!isPaused)) {
             textPaint.setColor(colors[randInt(0, 3)]);
         } else if (isSelected) {
             PadView.this.setBackgroundColor(Color.WHITE);
             PadView.this.setAlpha(0.5f);
             textPaint.setColor(getResources().getColor(R.color.tappad_green));
         } else {
             PadView.this.setBackgroundColor(Color.BLACK);
             PadView.this.setAlpha(0.5f);
         }
         invalidate();
         //As long as we are not paused we can keep painting randomly
         if (isPaused == false) {
             randomPaint();
             randText();
         }
     }
 
     private int getRandomDelay() {
         return randInt(1, randInt(1, 3)) * randInt(0, 1) < 0.5 ? minDelay : maxDelay;
     }
 
     public int randInt(int min, int max) {
         int randomNum = rand.nextInt((max - min) + 1) + min;
         return randomNum;
     }
 
     private void randText() {
         if ((!isSelected) && (!isPaused)) {
             int rand = randInt(0, 13);
             if (rand == 10) {
                 currentText = "+";
             } else if (rand == 11) {
                 currentText = "-";
             } else if (rand == 12) {
                 currentText = "X";
             } else if (rand == 13) {
                 currentText = "/";
             } else {
                 currentText = Integer.toString(rand);
             }
         }
     }
 
     @Override
     public boolean onTouchEvent(MotionEvent event) {
         int maskedAction = event.getActionMasked();
 
         switch (maskedAction) {
 
             case MotionEvent.ACTION_DOWN:
             case MotionEvent.ACTION_POINTER_DOWN: {
                 if(!isSelected){
                     MediaPlayer mp = MediaPlayer.create(Config.context, R.raw.tap);
                     mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
 
                         @Override
                         public void onCompletion(MediaPlayer mp) {
                             mp.release();
                         }
 
                     });
                     mp.start();
                     ArrayList<Float> operands = ((GamePadActivity) super.getContext()).getStatsView().getOperands();
                     String operator = ((GamePadActivity) super.getContext()).getStatsView().getOperator();
 
                     try {
                         float number = Float.parseFloat(currentText);
                         if (operands.size() < 2) {
                             ((GamePadActivity) super.getContext()).getStatsView().addOperand(number);
                             this.isSelected = true;
                             if ((operands.size() == 2) && (operator != null)) {
                                 ((GamePadActivity) super.getContext()).getStatsView().doCalc();
                                 ((GamePadActivity) super.getContext()).getStatsView().newQuestion();
                             }
                         }
 
                     } catch (NumberFormatException e) {
                         if (operator == null) {
                             ((GamePadActivity) super.getContext()).getStatsView().setOperator(currentText);
                             this.isSelected = true;
                             if (operands.size() == 2) {
                                 ((GamePadActivity) super.getContext()).getStatsView().doCalc();
                                 ((GamePadActivity) super.getContext()).getStatsView().newQuestion();
                             }
                         }
                     }
                 }
                 else
                 {
 
                     if(!isDividedByTwo && !currentText.equals("X") && !currentText.equals("/") && !currentText.equals("+") && !currentText.equals("-"))
                     {
                         isDividedByTwo = true;
                         currentText = Float.toString((Float.parseFloat(currentText) / 2));
                     }
                 }
                 doThePaint();
                 break;
             }
             case MotionEvent.ACTION_MOVE: { // a pointer was moved
                 break;
             }
             case MotionEvent.ACTION_UP:
             case MotionEvent.ACTION_POINTER_UP:
             case MotionEvent.ACTION_CANCEL: {
                 break;
             }
         }
         invalidate();
 
         return true;
     }
 
     @Override
     protected void onDraw(Canvas canvas) {
         super.onDraw(canvas);
        int xPos =  (int) ((canvas.getWidth() / 2) + textPaint.measureText(currentText)/2);
         int yPos = (int) ((canvas.getHeight() / 2) - ((textPaint.descent() + textPaint.ascent()) / 2));
         canvas.drawText(currentText, xPos, yPos, textPaint);
     }
 
 }
