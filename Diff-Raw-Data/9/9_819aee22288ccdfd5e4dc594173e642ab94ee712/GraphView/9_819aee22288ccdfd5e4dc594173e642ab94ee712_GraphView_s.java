 /*******************************************************************************
  * Nof1 Trials helper, making life easier for clinicians and patients in N of 1 trials.
  * Copyright (C) 2012  WMG, University of Warwick
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You may obtain a copy of the GNU General Public License at  
  * <http://www.gnu.org/licenses/>.
  * 
  * Contributors:
  *     John Lawson - initial API and implementation
  ******************************************************************************/
 package uk.co.jwlawson.nof1.views;
 
 import java.util.ArrayList;
 
 import uk.co.jwlawson.nof1.containers.Label;
 import uk.co.jwlawson.nof1.containers.Line;
 import uk.co.jwlawson.nof1.containers.Vec2;
 import android.content.Context;
 import android.database.Cursor;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Align;
 import android.graphics.Paint.Style;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.View;
 
 /**
  * View to draw and display a graph showing the data over time. x = days, y =
  * patient feedback
  * 
  * @author John Lawson
  * 
  */
 public class GraphView extends View {
 
 	private static final String TAG = "GraphView";
 	private static final boolean DEBUG = false;
 
 	private static final int TICK_SIZE = 5;
 	private static final int TEXT_SIZE = 12;
 
 	private static final int TOP_PAD = 10;
 	private static final int BOTTOM_PAD = 35;
 	private static final int LEFT_PAD = 25;
 	private static final int RIGHT_PAD = 10;
 
 	private static final float RATIO = 0.43f;
 
 	/** List of each data point. */
 	private ArrayList<Vec2> mVecList;
 
 	/** List of lines to draw as x-axis */
 	private ArrayList<Line> mXAxisList;
 
 	/** List of x-axis labels */
 	private ArrayList<Label> mXLabelList;
 
 	/** List of lines to draw as y-axis */
 	private ArrayList<Line> mYAxisList;
 
 	/** List of y-axis labels */
 	private ArrayList<Label> mYLabelList;
 
 	/** Paint to draw the points */
 	private Paint mVecPaint;
 
 	/** Paint to draw axes */
 	private Paint mAxesPaint;
 
 	/** Scale in x direction */
 	private float mScaleX;
 
 	/** Scale in y direction */
 	private float mScaleY;
 
 	/** Largest value of x */
 	private int mMaxX;
 
 	/** Largest value of y */
 	private int mMaxY;
 
 	/** Height of view */
 	private int mHeight;
 
 	/** Width of view */
 	private int mWidth;
 
 	/** Cursor holding data */
 	private Cursor mCursor;
 
 	private float[] floatarr;
 
 	public GraphView(Context context) {
 		this(context, null, 0);
 	}
 
 	public GraphView(Context context, AttributeSet attrs) {
 		this(context, attrs, 0);
 	}
 
 	public GraphView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 
 		mVecList = new ArrayList<Vec2>();
 		mXAxisList = new ArrayList<Line>();
 		mXLabelList = new ArrayList<Label>();
 		mYAxisList = new ArrayList<Line>();
 		mYLabelList = new ArrayList<Label>();
 
 		mVecPaint = new Paint();
 		mVecPaint.setColor(0xFF33B5E5);
 		mVecPaint.setAntiAlias(true);
 		mVecPaint.setStyle(Style.STROKE);
 		mVecPaint.setStrokeWidth(0);
 
 		mAxesPaint = new Paint(mVecPaint);
 		mAxesPaint.setColor(Color.BLACK);
 		mAxesPaint.setTextAlign(Align.CENTER);
 		mAxesPaint.setTextSize(TEXT_SIZE);
 	}
 
 	/** Set the list of points to be drawn. Use either this or setCursor to supply points to graph */
 	public void setVecList(ArrayList<Vec2> vecList) {
 
 		mVecList = vecList;
 
 		floatarr = new float[vecList.size() * 2];
 
 		int i = 0;
 		for (Vec2 vec : vecList) {
 			floatarr[i++] = vec.getX() * mScaleX;
 			floatarr[i++] = mHeight - (vec.getY() * (mHeight - BOTTOM_PAD - TOP_PAD) / mMaxY);
 		}
 	}
 
 	/**
 	 * Set a cursor to read data from. Use either this or setVecList to supply points to the graph
 	 * 
 	 * @param cursor Cursor with x in first column, y in second column
 	 */
 	public void setCursor(Cursor cursor) {
 
 		mCursor = cursor;
 
 		cursor.moveToFirst();
 
 		int i = 0;
 		while (!cursor.isAfterLast()) {
 			floatarr[i++] = (float) cursor.getInt(0) * mScaleX;
 			floatarr[i++] = mHeight - ((float) cursor.getInt(1) * (mHeight - BOTTOM_PAD - TOP_PAD) / mMaxY);
 			cursor.moveToNext();
 		}
 	}
 
 	/** Set largest value on x-axis. Must be initialised before view is drawn */
 	public void setMaxX(int maxX) {
 		mMaxX = maxX;
 
 		mXAxisList.clear();
 		mXLabelList.clear();
 
 		// Add new axis
 		mXAxisList.add(new Line(new Vec2(0, mHeight), new Vec2(mWidth, mHeight)));
 
 		// Add ticks to axis
 		float xtick = (float) mWidth / maxX;
 		for (int i = 1; i <= maxX; i++) {
 			mXAxisList.add(new Line(new Vec2((int) (i * xtick), mHeight), new Vec2((int) (i * xtick), mHeight + TICK_SIZE)));
 		}
 		// Add labels to x-axis
 		if (maxX < 10) {
 			for (int i = 1; i <= maxX; i++) {
 				mXLabelList.add(new Label("" + i, new Vec2((int) (i * xtick), mHeight + TICK_SIZE + TEXT_SIZE)));
 			}
 		} else {
 			for (int i = 5; i <= maxX; i = i + 5) {
 				mXLabelList.add(new Label("" + i, new Vec2((int) (i * xtick), mHeight + TICK_SIZE + TEXT_SIZE)));
 			}
 		}
 		mXLabelList.add(new Label("Days", new Vec2(mWidth / 2, mHeight + TICK_SIZE + 2 * TEXT_SIZE)));
 
 		invalidate();
 	}
 
 	/** Set largest value on y-axis. Must be initialised before view is drawn */
 	public void setMaxY(int maxY) {
 		mMaxY = maxY;
 
 		mYAxisList.clear();
 		mYLabelList.clear();
 
 		// Add axis
 		mYAxisList.add(new Line(new Vec2(0, mHeight), new Vec2(0, TOP_PAD + BOTTOM_PAD)));
 
 		// Add ticks to axis
 		float ytick = (float) (mHeight - TOP_PAD - BOTTOM_PAD) / maxY;
 		for (int i = 1; i <= maxY; i++) {
 			mYAxisList.add(new Line(new Vec2(0, (int) (mHeight - (i * ytick))), new Vec2(-TICK_SIZE, (int) (mHeight - (i * ytick)))));
 		}
 
 		// Add labels to y-axis
 		if (maxY < 10) {
 			for (int i = 1; i <= maxY; i++) {
 				mYLabelList.add(new Label("" + i, new Vec2(-2 * TICK_SIZE, (int) (mHeight - (i * ytick) + TEXT_SIZE / 2))));
 			}
 		} else {
 			for (int i = 5; i <= maxY; i = i + 5) {
 				mYLabelList.add(new Label("" + i, new Vec2(-2 * TICK_SIZE, (int) (mHeight - (i * ytick) + TEXT_SIZE / 2))));
 			}
 		}
 		invalidate();
 	}
 
 	@Override
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 
 		canvas.save();
 		// Move canvas before drawing axes, so when canvas restored
 		// there is room to label axes
 		canvas.translate(LEFT_PAD, -BOTTOM_PAD);
 
 		if (DEBUG) Log.d(TAG, "Drawing x-axis");
 		for (Line line : mXAxisList) {
 			line.draw(canvas, mAxesPaint);
 		}
 
 		if (DEBUG) Log.d(TAG, "Drawing y-axis");
 		for (Line line : mYAxisList) {
 			line.draw(canvas, mAxesPaint);
 		}
 
 		if (DEBUG) Log.d(TAG, "Drawing data");
 		canvas.drawLines(floatarr, mVecPaint);
		canvas.drawLines(floatarr, 2, floatarr.length - 2, mVecPaint);
 
 		if (DEBUG) Log.d(TAG, "Drawing labels");

 		for (Label lab : mXLabelList) {
 			lab.draw(canvas, mAxesPaint);
 		}
 
 		for (Label lab : mYLabelList) {
 			lab.draw(canvas, mAxesPaint);
 		}
 
 		canvas.restore();
 	}
 
 	@Override
 	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 		int width = measureWidth(widthMeasureSpec);
 		int height = measureHeight(heightMeasureSpec, width);
 		setMeasuredDimension(width, height);
 	}
 
 	/**
 	 * Determines the width of this view
 	 * 
 	 * @param measureSpec
 	 *            A measureSpec packed into an int
 	 * @return The width of the view, honoring constraints from measureSpec
 	 */
 	private int measureWidth(int measureSpec) {
 		int result = 0;
 		int specMode = MeasureSpec.getMode(measureSpec);
 		int specSize = MeasureSpec.getSize(measureSpec);
 
 		if (specMode == MeasureSpec.EXACTLY) {
 			// We were told how big to be
 			result = specSize;
 		} else {
 			// Want width as wide as we can
 			result = 2000;
 			if (specMode == MeasureSpec.AT_MOST) {
 				// Respect AT_MOST value if that was what is called for by
 				// measureSpec
 				result = Math.min(result, specSize);
 			}
 		}
 		if (DEBUG) Log.d(TAG, "Measured width = " + result);
 		return result;
 	}
 
 	/**
 	 * Determines the height of this view
 	 * 
 	 * @param measureSpec
 	 *            A measureSpec packed into an int
 	 * @return The height of the view, honoring constraints from measureSpec
 	 */
 	private int measureHeight(int measureSpec, int width) {
 		int result = 0;
 		int specMode = MeasureSpec.getMode(measureSpec);
 		int specSize = MeasureSpec.getSize(measureSpec);
 
 		if (specMode == MeasureSpec.EXACTLY) {
 			// We were told how big to be
 			result = specSize;
 		} else {
 			// Want height to match given RATIO
 			result = (int) ((float) width * RATIO);
 			if (specMode == MeasureSpec.AT_MOST) {
 				// Respect AT_MOST value if that was what is called for by
 				// measureSpec
 				result = Math.min(result, specSize);
 			}
 		}
 		if (DEBUG) Log.d(TAG, "Measured height = " + result);
 		return result;
 	}
 
 	@Override
 	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
 		super.onSizeChanged(w, h, oldw, oldh);
 		/*
 		 * Want to scale the graph to fill the view
 		 * The drawing of the data is initially restrained to pixels, so the
 		 * scale will likely be a large number.
 		 */
 		if (DEBUG) Log.d(TAG, "Layout changed. New height: " + h + " New width: " + w);
 		mHeight = h;
 		mWidth = w - RIGHT_PAD - LEFT_PAD;
 
 		mScaleX = (float) mWidth / mMaxX;
 		mScaleY = (float) h / mMaxY;
 
 		if (mVecList != null) setVecList(mVecList);
 		else if (mCursor != null) setCursor(mCursor);
 
 		setMaxX(mMaxX);
 		setMaxY(mMaxY);
 
 		invalidate();
 	}
 }
