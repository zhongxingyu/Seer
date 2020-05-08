 package com.barsky.chainreaction1;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 import java.util.Random;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class MyView  extends View {
 	public static Paint paint;
 	Path path;
 	Random rnd = new Random();
 	TouchCircle touchCircle;
 	public int level = 0;
 	public int levelText = 1;
 	public int[] num = {5,10,15,20,25,30,35,40,45,50,55,60};
 	public int[] clearLevel = {1,2,4,6,10,15,18,22,30,37,48,55};
 	public static int clear = 0;
 	public static int touchRadius = 60;
 	boolean startLevel = true;
 	boolean touched, popup, added, canvasBlack = false;
 	ArrayList<Circle> remove = new ArrayList<Circle>();
 	ArrayList<Circle> circles = new ArrayList<Circle>();
 	Iterator<Circle> iterator1, iterator2;
 	Circle prevCircle, newCircle;
 	AlertDialog levelPop = new AlertDialog.Builder(getContext()).create();
 	AlertDialog losePop = new AlertDialog.Builder(getContext()).create();
 	String message;
 	static ScoreBoard scoreBoard = new ScoreBoard();
 	public String score;
 	
 	public MyView(Context context) {
 		super(context);
 		init();
 	}
 	
 	public MyView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		init();
 	}
 	
 	public MyView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		init();
 	}
 	
 	private void init() {
 		paint = new Paint();
 		paint.setStrokeWidth(10);
 		paint.setStyle(Paint.Style.FILL);
 		paint.setTextSize(20);
 	}
 	
 	public void popup() {
 		levelPop.setCanceledOnTouchOutside(false);
 		levelPop.setCancelable(false);
 		levelPop.setButton(-1, "Play", new DialogInterface.OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				// TODO Auto-generated method stub
 				circles.clear();
 				Circle.totalScore.levelBalls = 0;
 				for (int i=0; i<num[level]; i++) {
 					circles.add(new Circle());
 				}
 			}
 		});
 		popup =  true;
 	}
 	
 	public void losePopup() {
 		losePop.setCanceledOnTouchOutside(false);
 		losePop.setCancelable(false);
 		losePop.setButton(-1, "OK", new DialogInterface.OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				// TODO Auto-generated method stub
 				level = 0;
 				startLevel = true;
 				added = false;
 				touched = false;
 				clear = 0;
 				levelText = 1;
 				touchRadius = 60;
				Circle.totalScore.gameScore = 0;
 			}
 		});
 		losePop.setTitle("Too bad, You Lose!");
 		losePop.setMessage("Play Again?\nYour Score: "+Circle.totalScore.gameScore);
 		losePop.show();
 	}
 	public void level() {
 		
 		if(startLevel)
 		{
 			message = "Get "+clearLevel[level]+" out of "+num[level]+" balls!";
 			try {
 				if (!popup) { popup(); }
 				levelPop.setMessage(message);
 				levelPop.setTitle("Level "+levelText);
 				levelPop.show();
 			} catch(NullPointerException e) {
 				//Do nothing
 			}
 			startLevel = false;
 		} else {
 			if(clear >= clearLevel[level] && hasLost()) {
 				startLevel = true;
 				added = false;
 				touched = false;
 				clear = 0;
 				levelText += 1;
 				if(level >= num.length - 1) {
 					level = 0;
 					touchRadius -= 10;
 				} else {
 					level += 1;
 				}
 			}
 		}
 		
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		
 		if(!touched) {
 			if(event.getAction() == MotionEvent.ACTION_DOWN) {
 				touchCircle = new TouchCircle(event.getX(), event.getY());
 				touched = true;
 			}
 			invalidate();
 		}
 		return true;
 	}
 	
 	public void drawCircles(Canvas canvas) {
 		iterator1 = circles.iterator();
 		while(iterator1.hasNext()) {
 			newCircle = iterator1.next();
 			newCircle.drawCircle(canvas);
 			circleDrawable(newCircle);
 		}
 		removeCircles();
 		
 		if(touched && !added) {
 			circles.add(touchCircle);
 			added = true;;
 		}
 	}
 	
 	public void circleDrawable(Circle circle) {
 		if(!circle.drawableCircle()) {
 			remove.add(circle);
 		}
 	}
 	
 	public void removeCircles() {
 		iterator1 = remove.iterator();
 		while(iterator1.hasNext()) {
 			circles.remove(iterator1.next());
 		}
 		remove.clear();
 	}
 	
 	public void checkCollision(Canvas canvas) {
 		if(touched) {
 			iterator1 = circles.iterator();
 			while(iterator1.hasNext()) {
 				prevCircle = iterator1.next();
 				//touchCircle.collision(prevCircle);
 				iterator2 = circles.iterator();
 				while(iterator2.hasNext()) {
 					newCircle = iterator2.next();
 					if(prevCircle != newCircle) {
 						try {
 							newCircle.collision(prevCircle);
 						} catch(IllegalArgumentException e) {
 							continue;
 						} catch(NoSuchElementException e) {
 							continue;
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	public boolean hasLost() {
 		if(touched) {
 			iterator1 = circles.iterator();
 			while (iterator1.hasNext()) {
 				if(iterator1.next().r > Circle.littleRadius) {
 					return false;
 				}
 			} return true;
 		} return false;
 	}
 	
 	public void Lost(Canvas canvas) {
 		if (hasLost() && clear < clearLevel[level]) {
 			canvas.drawColor(Color.BLACK);
 			canvasBlack = true;
 			losePopup();
 		} else if (canvasBlack == true ){ 
 			canvasBlack = false; 
 		}
 	}
 	
 	public void drawBoard(Canvas canvas) {
 		scoreBoard.drawScore(canvas, Circle.totalScore);
 	}
 	@Override
 	public void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 		
 		level();
 		drawCircles(canvas);
 		drawBoard(canvas);
 		checkCollision(canvas);
 		Lost(canvas);
 		
 		invalidate();
 	}
 }
