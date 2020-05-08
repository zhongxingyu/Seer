 package com.redcarddev.kickshot.views;
 
 import com.redcarddev.kickshot.R;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class Board extends View {
 
 	Bitmap chip = null;
 	
 	//two dice in case of doubles
 	Dice dice1 = null;
 	Dice dice2 = null;
 	Bitmap diceImage = null;
 	
 	
 	private Canvas canvas = null;
 	
 	protected int chipXPos = 0;
 	protected int chipYPos = 0;
 	
 	protected int dice1XPos = 0;
 	protected int dice1YPos = 0;
 	protected int dice2XPos = 0;
 	protected int dice2YPos = 0;
 	
 	protected int dice1HomePosition = 0;
 	protected int dice2HomePosition = 0;
 	
 	int initSet = 0;
 
 	public Board(Context context, AttributeSet attrs) {
 	    super(context, attrs);
 	    
 	    this.dice1 = new Dice(context);
 	    this.dice2 = new Dice(context);
 	}
 	
 	public boolean onTouchEvent(MotionEvent event) {
 		return super.onTouchEvent(event);
 	}
 	
 	public void changeChip(int playerTurn) {
 		
 		int chipDrawable;
 		
 		if (playerTurn == 1) {//home chip
 			chipDrawable = R.drawable.ballchiphome;
 		} else {//away chip
 			chipDrawable = R.drawable.ballchipaway;
 		}
 		
 		Resources res = getContext().getResources();
 		this.chip = BitmapFactory.decodeResource(res, chipDrawable);
 		
 		invalidate();
 	}
 	
 	public void changeDice(int dice1Face, int dice2Face) {
 		this.dice1.setDiceFace(dice1Face);
 		this.dice2.setDiceFace(dice2Face);
 		invalidate();
 	}
 	
 	public void positionDiceHome() {
 		
 		//this.diceYPos = this.diceHomePosition;
		this.dice1YPos = 150;
		this.dice2YPos = 250;
 		invalidate();
 		
 	}
 	
 	public void positionDiceAway() {
 		this.dice1YPos = 150;
 		this.dice2YPos = 250;
 		invalidate();
 	}
 	
 	public void towardsAway(int steps) {
 		this.moveChip(steps);
 	}
 	
 	public void towardsHome(int steps) {
 		this.moveChip(-1 * steps);
 	}
 	
 	private void moveChip(int number) {
 		//this is just barely too much
 		this.chipYPos = this.chipYPos - (40 * number);
 		if(chipYPos < 120){
 			chipYPos = 120;
 			// put shot on goal function here
 			// Shoot(int player)
 		}
 		else if(chipYPos > 850){
 			chipYPos = 850;
 			// put shot on goal function here
 			// Shoot(int player)
 		}
 		invalidate();
 	}
 	
 	private void init() {
 		
 		this.dice1HomePosition = this.canvas.getHeight() - 150;
 		this.dice2HomePosition = this.canvas.getHeight() - 250;
 		
 		changeChip(1);
 		positionDiceHome();
 		
 		
 		this.chipXPos = (this.canvas.getWidth() - this.chip.getWidth()) / 2;
 		this.chipYPos = (this.canvas.getHeight() - this.chip.getHeight()) / 2;
 		
 		this.initSet = 1;
 		
 	}
 
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 		
 		this.canvas = canvas;
 		
 		if (this.initSet == 0) {
 			this.init();
 		}
 		
 		canvas.drawBitmap(this.dice1.getCurrent(), this.dice1XPos, this.dice1YPos, null);
 		canvas.drawBitmap(this.dice2.getCurrent(), this.dice2XPos, this.dice2YPos, null);
 		
 		canvas.drawBitmap(this.chip, this.chipXPos, this.chipYPos, null);
 		
 	}
 
 }
