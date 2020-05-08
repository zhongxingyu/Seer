 package com.cuuuurzel.gollivewallpaper;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class GolSwitcherGrid extends View {
 
 	private GameOfLife gameState;
 	private Paint mPaint;
 	
 	public GolSwitcherGrid( Context context ) {
 		this( context, null );
 	}
 
 	public GolSwitcherGrid( Context context, AttributeSet attrs ) {
 		this( context, attrs, 0 );
 	}
 
 	public GolSwitcherGrid( Context context, AttributeSet attrs, int defStyle ) {
 		super(context, attrs, defStyle);
 		mPaint = new Paint();
 		mPaint.setStyle( Style.FILL_AND_STROKE );
 		mPaint.setColor(0xFFFFFFFF);
 		this.gameState = new GameOfLife( 10, 6 );
 	}
 
 	/**
 	 * Load config from the indicated file.
 	 * File format :
 	 * n of rows, n of columns[, rowN, colM, rowX, colY, ...]
 	 * Where the cells indicated are active.
 	 */
 	public void setup( String path ) {
 		this.gameState.setup( path );
 	}
 	
 	public boolean isAlive( int r, int c ) {
 		return this.gameState.grid[r][c].isAlive;
 	}
 	
 	public int rows() {
 		return this.gameState.grid.length;		
 	}
 	
 	public int cols() {
 		return this.gameState.grid[0].length;		
 	}
 
 	@Override
 	public boolean onTouchEvent( MotionEvent m ) {
 		float d = this.getWidth() / this.cols();
		int r = (int) Math.floor( Math.abs( (m.getY()-1) / d ) );
		int c = (int) Math.floor( Math.abs( (m.getX()-1) / d ) );
 		this.gameState.grid[r][c].isAlive = !this.gameState.grid[r][c].isAlive;
 		this.invalidate();
 		return super.onTouchEvent( m );
 	}
 	
 	@Override
 	public void draw( Canvas cnv ) {			
 		float w = cnv.getWidth();
 		float h = cnv.getHeight();			
 		
 		float d = Math.max( h/gameState.grid.length, 
 				            w/gameState.grid[0].length );
 
 		cnv.drawColor( Color.BLACK );
 		
 		this.drawGrid( d, cnv );
 		
 		for (int r = 0; r < gameState.grid.length; r++) {
 			for (int c = 0; c < gameState.grid[0].length; c++) {
 				if ( gameState.grid[r][c].isAlive ) {
 					cnv.drawRect( d*c, d*r, d*(c+1), d*(r+1), mPaint);
 				}
 			}
 		}
 		cnv.restore();
 		super.draw( cnv );
 	}
 	
 	private void drawGrid( float d, Canvas cnv ) {
 		for (int r=0; r<gameState.grid.length+1; r++) {
 			cnv.drawLine( 0, d*r, d*gameState.grid[0].length, d*r, mPaint );
 		}
 		for (int c=0; c<gameState.grid[0].length+1; c++) {
 			cnv.drawLine( d*c, 0, d*c, d*gameState.grid.length, mPaint );
 		}
 	}
 	
 	public void clear() {
 		this.gameState.clear();		
 	}
 
 	public void setSize(int rows, int cols ) {
 		this.gameState.setSize( rows, cols );		
 	}
 	
 }
