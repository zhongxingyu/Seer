 package com.touchmenotapps.Graph3r.bar;
 
 import java.util.ArrayList;
 
 import com.touchmenotapps.Graph3r.Graph;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.LinearGradient;
 import android.graphics.Paint;
 import android.graphics.Shader;
 import android.graphics.Rect;
 import android.graphics.Paint.Align;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.ScaleGestureDetector;
 import android.view.View;
 
 /**
  * @version Graph3r Alpha 3
  * @author Arindam Nath (strider2023@gmail.com)
  * @Description	The BarGraphView class implements all variation of Bar Graph, like 
  * 	Simple Bar Graph, Stacked Bar Graph and Grouped Bar Graph.
  * TODO Check bug as to why the grid lines are disappearing.
  */
 public class BarGraphView {
 	
 	/**
 	 * 
 	 * @param context
 	 * @param renderer
 	 * @return
 	 */
 	public View getGraphView(Context context, BarGraphRenderer renderer) {
 		return new BarGraph(context, renderer);
 	}
 	
 	public interface BarGraphInterface {
 		public void onBarClickedListener(BarObject data);
 	}
 	
 	private class BarGraph extends View {
 		
 		private final int BAR_HANDLER = 213;
 		
 		private final int X_AXES_LABEL_HANDLER = 9809;
 		
 		private final int Y_AXES_LABEL_HANDLER = 6876;
 
 		private BarGraphRenderer mRenderer;
 		
 		private BarGraphInterface mCallback;
 	
 		private ScaleGestureDetector mScaleDetector;
 	
 		private float mScaleFactor;
 	
 		private BarGraphHelperFunctions mFunctions;
 	
 		private ArrayList<BarObject> barGraphObject = new ArrayList<BarObject>(0);
 	
 		private ArrayList<Double> data = new ArrayList<Double>(0);
 	
 		private ArrayList<int[]> color = new ArrayList<int[]>(0);
 	
 		private ArrayList<String> stackGroupLabels = new ArrayList<String>(0);
 	
 		private int graphWidth;
 	
 		private int graphHeight;
 	
 		private int graphOriginX;
 	
 		private int graphOriginY;
 	
 		private int xAxisLabelRotation;
 	
 		private ArrayList<String> barLabels = new ArrayList<String>(0);
 	
 		private int graphGrouping;
 	
 		private int xAxisLabelDistance;
 	
 		private boolean graphIsScaling = false;
 	
 		private boolean graphIsDragged = false;
 	
 		private Paint stackBarTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 	
 		private Paint barTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 	
 		private Paint xAxesLabelTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 	
 		private Paint xAxesSecondGroupPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 	
 		// These two variables keep track of the X and Y coordinate of the finger
 		// when it first
 		// touches the screen
 		private float startX = 0f;
 		private float startY = 0f;
 	
 		// These two variables keep track of the amount we need to translate the
 		// canvas along the X
 		// and the Y coordinate
 		private float translateX = 0f;
 		private float translateY = 0f;
 	
 		// These two variables keep track of the amount we translated the X and Y
 		// coordinates, the last time we
 		// panned.
 		private float previousTranslateX = 0f;
 		private float previousTranslateY = 0f;
 		
 		/**
 		 * 
 		 * @param context
 		 * @param renderer
 		 */
 		public BarGraph(Context context, BarGraphRenderer renderer) {
 			super(context);
 			mRenderer = renderer;
 			mFunctions = new BarGraphHelperFunctions();
			mCallback = renderer.getBarGraphClickedListener();
 			mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
 			initializeGraphRenderer();
 		}
 		
 		private void initializeGraphRenderer() {
 			/** Set the paint properties **/
 			stackBarTextPaint.setColor(Color.BLACK);
 			stackBarTextPaint.setTextAlign(Align.CENTER);
 			xAxesLabelTextPaint.setColor(Color.BLACK);
 			xAxesLabelTextPaint.setTextAlign(Align.CENTER);
 			barTextPaint.setColor(Color.BLACK);
 			barTextPaint.setTextAlign(Align.CENTER);
 			xAxesSecondGroupPaint.setColor(Color.BLACK);
 			barTextPaint.setTextSize(mRenderer.getXAxisLabelSize());
 			stackBarTextPaint.setTextSize(mRenderer.getXAxisLabelSize());
 			/** Set the default sizes based on mode. **/
 			if (!mRenderer.isRunningOnTablet()) {
 				xAxisLabelDistance = 10;
 				xAxesSecondGroupPaint.setTextSize(10);
 			} else {
 				xAxisLabelDistance = 35;
 				xAxesSecondGroupPaint.setTextSize(15);
 			}
 			/** Set the other graph data **/
 			this.mScaleFactor = mRenderer.getScaleFactor();
 			this.data = mRenderer.getPlotData();
 			this.color = mRenderer.getPlotColor();
 			this.barLabels = mRenderer.getBarLabels();
 			this.graphWidth = mRenderer.getGraphWidth();
 			this.graphHeight = mRenderer.getGraphHeight();
 			this.graphOriginX = mRenderer.getGraphOriginX();
 			this.graphOriginY = mRenderer.getGraphOriginY();
 			this.stackGroupLabels = mRenderer.getStackGroupLabels();
 			this.graphGrouping = mRenderer.getGraphGrouping();
 			/** X axes label properties **/
 			xAxesLabelTextPaint.setTextSize(mRenderer.getXAxisLabelSize());
 			this.xAxisLabelRotation = mRenderer.getXAxisLabelRotation();
 		}
 	
 		@Override
 		public boolean onTouchEvent(MotionEvent event) {
 			if (mRenderer.getGraphIsClickable() && !graphIsScaling
 					&& !graphIsDragged) {
 				int eventX = (int) event.getX();
 				int eventY = (int) event.getY();
 				switch (event.getAction() & MotionEvent.ACTION_MASK) {
 				case MotionEvent.ACTION_UP:
 					Log.i("Bar Graph", "Val X: " + eventX + " Val Y: " + eventY);
 					graphIsDragged = false;
 					for (int counter = 0; counter < barGraphObject.size(); counter++) {
 						if (barGraphObject.get(counter).isInBounds(eventX, eventY)) {
 							mCallback.onBarClickedListener(barGraphObject.get(counter));
 							break;
 						} //else
 							//Log.i("Bar Graph", "Blank Click");
 					}
 					break;
 				}
 				invalidate();
 			}
 	
 			if (mRenderer.getGraphIsPannable() && !graphIsScaling) {
 				switch (event.getAction() & MotionEvent.ACTION_MASK) {
 				case MotionEvent.ACTION_UP:
 					graphIsDragged = false;
 					// All fingers went up, so lets save the value of translateX
 					// and translateY into previousTranslateX and
 					// previousTranslate
 					previousTranslateX = translateX;
 					previousTranslateY = translateY;
 					break;
 	
 				case MotionEvent.ACTION_DOWN:
 					// We assign the current X and Y coordinate of the finger to
 					// startX and startY minus the previously translated
 					// amount for each coordinates This works even when we are
 					// translating the first time because the initial
 					// values for these two variables is zero.
 					startX = event.getX() - previousTranslateX;
 					startY = event.getY() - previousTranslateY;
 					break;
 	
 				case MotionEvent.ACTION_MOVE:
 					if(((event.getY() - startY)/ mScaleFactor) >= 0 
 						&& ((event.getX() - startX)/ mScaleFactor) <= 5
 						&& ((event.getY() - startY)/ mScaleFactor) <= (graphHeight - graphOriginY - (70 * mScaleFactor))
 						&& ((event.getX() - startX)/ mScaleFactor) >= -(graphWidth - graphOriginX - (70 * mScaleFactor))) {
 						translateX = event.getX() - startX;
 						translateY = event.getY() - startY;
 						// We cannot use startX and startY directly because we have
 						// adjusted their values using the previous translation values.
 						// This is why we need to add those values to startX and startY
 						// so that we can get the actual coordinates of the finger.
 						double distance = Math
 								.sqrt(Math.pow(event.getX()
 										- (startX + previousTranslateX), 2)
 										+ Math.pow(event.getY()
 												- (startY + previousTranslateY), 2));
 						if (distance > 0) {
 							graphIsDragged = true;
 							distance *= mScaleFactor;
 						}
 					}
 					break;
 				}
 				invalidate();
 			}
 	
 			if (mRenderer.getGraphIsZoomable()) {
 				mScaleDetector.onTouchEvent(event);
 			}
 	
 			return true;
 		}
 	
 		@Override
 		public void onDraw(Canvas canvas) {
 			super.onDraw(canvas);
 			Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
 			// set the right hand extent of graph
 			int _width = (graphOriginX + graphWidth);
 			if (getWidth() - _width < 10)
 				_width = getWidth() - 20;
 			// set the top extent of graph
 			int _height = 15;
 			if (mRenderer.isRunningOnTablet())
 				_height = 30;
 			int originX = graphOriginX; // set the left hand extent
 			int originY = graphHeight - graphOriginY; // set the bottom extent
 	
 			float padding;
 			if (mRenderer.getStyle() == Graph.STYLE_BAR_NORMAL || mRenderer.getStyle() == Graph.STYLE_BAR_GROUPED)
 				padding = (float) (((float) (_width - originX) / (float) data
 						.size()) * 0.20);
 			else
 				padding = (float) (((float) (_width - originX) / (float) ((float) (data
 						.size()) / (float) graphGrouping)) * 0.20);
 	
 			int maxVal = ((int) (mFunctions.getMaximumSeriesValue(data,
 					graphGrouping, mRenderer.getStyle()) / 10) + 1) * 10;
 			// value is equal to this much height
 			float ratio = (float) ((float) (originY - _height) / (float) maxVal);
 			
 			/** Draw X Axis **/
 			if (mRenderer.getDrawXAxis()) {
 				paint.setColor(mRenderer.getXAxisColor());
 				Rect xAxis = new Rect(originX, originY - 1, _width, originY);
 				canvas.drawRect(xAxis, paint);
 			}
 			/** Draw Y Axis **/
 			if (mRenderer.getDrawYAxis()) {
 				paint.setColor(mRenderer.getYAxisColor());
 				Rect yAxis = new Rect(originX, (int) (_height), originX + 1,
 						originY);
 				canvas.drawRect(yAxis, paint);
 			}
 					
 			/** Draw grid parallel to X-Axis **/
 			if (mRenderer.getDrawGridLines()) {
 				canvas.save();
 				graphInputHandler(canvas, originX, _height, _width, originY, Y_AXES_LABEL_HANDLER);
 				mFunctions.drawXAxisGrid(canvas, 
 				mRenderer.getXAxisColor(), 
 				mRenderer.getYAxisLabelSize(), 
 				originX, originY, _width, maxVal, 
 				ratio, mRenderer.getGraphNumXAxesLabels()); 
 				canvas.restore();
 			}
 					
 			/** Draw Y-axis label **/
 			mFunctions.drawYAxisLabel(canvas, 
 					mRenderer, 
 					originX, originY, _height, getHeight(), 
 					maxVal, 
 					mRenderer.isRunningOnTablet());
 				
 			int tempX = originX + (int) padding;
 			int heightBar;
 			int barLabelsCounter = 0;
 			/** Draw the bar and the labels **/
 			switch (mRenderer.getStyle()) {
 			case Graph.STYLE_BAR_NORMAL:
 				for (int counter = 0; counter < data.size(); counter++) {
 					heightBar = (int) (((originY - (_height)) - (data.get(counter) * ratio)) + (_height));
 					/** Check if to draw a gradient or a single color **/
 					if(color.get(counter % color.size()).length > 1)
 						paint.setShader(new LinearGradient(tempX, heightBar,
 								(int) (tempX + padding * 4), originY, color.get(counter
 										% color.size())[0], color.get(counter
 										% color.size())[1], Shader.TileMode.MIRROR));
 					else if(color.get(counter % color.size()).length == 1)
 						paint.setColor(color.get(counter % color.size())[0]);
 					else
 						throw new  ClassCastException(getClass().getName() + ": Bar color not defined. Feild cannot have null values.");
 					/** Set the zoom and pan handler for the bars **/
 					canvas.save();
 					graphInputHandler(canvas, originX, _height, _width, originY, BAR_HANDLER);
 					/** Draw the bar **/
 					Rect bar = new Rect(tempX, heightBar,
 							(int) (tempX + padding * 4), originY);
 					canvas.drawRect(bar, paint);
 					/** Add it to the items array **/
 					canvas.drawText(String.valueOf(data.get(counter)), tempX + padding * 2,
 							heightBar - 5, barTextPaint);
 					canvas.restore();
 					barGraphObject.add(new BarObject(bar, String.valueOf(data
 							.get(counter))));
 					/** Rotate the text if needed **/
 					canvas.save();
 					graphInputHandler(canvas, originX, _height, _width, originY, X_AXES_LABEL_HANDLER);
 					canvas.rotate(xAxisLabelRotation, tempX + padding * 2,
 							(originY + xAxisLabelDistance));
 					if(barLabels != null && barLabels.size() > 0) 
 						canvas.drawText(barLabels.get(counter), tempX + padding * 2,
 								originY + xAxisLabelDistance, xAxesLabelTextPaint);
 					else 
 						canvas.drawText(String.valueOf(counter), tempX + padding * 2,
 								originY + xAxisLabelDistance, xAxesLabelTextPaint);
 					canvas.restore();
 					tempX = (int) (tempX + padding * 5);
 				}
 				break;
 			case Graph.STYLE_BAR_GROUPED:
 				for (int counter = 0; counter < data.size(); counter++) {
 					if (counter > 0)
 						if (counter % graphGrouping == 0)
 							tempX = (int) (tempX + padding * (graphGrouping + 4));
 						else
 							tempX = (int) (tempX + padding * 4);
 					heightBar = (int) (((originY - (_height)) - (data.get(counter) * ratio)) + (_height));
 					/** Check if to draw a gradient or a single color **/
 					if(color.get((counter % graphGrouping) % color.size()).length > 1)
 						paint.setShader(new LinearGradient(tempX, heightBar,
 								(int) (tempX + padding * 4), originY,
 								color.get((counter % graphGrouping) % color.size())[0],
 								color.get((counter % graphGrouping) % color.size())[1],
 								Shader.TileMode.CLAMP));
 					else if(color.get((counter % graphGrouping) % color.size()).length == 1)
 						paint.setColor(color.get((counter % graphGrouping) % color.size())[0]);
 					else
 						throw new  ClassCastException(getClass().getName() + ": Bar color not defined. Feild cannot have null values.");
 					/** Set the zoom and pan handler for the bars **/
 					canvas.save();
 					graphInputHandler(canvas, originX, _height, _width, originY, BAR_HANDLER);
 					/** Draw the bar **/
 					Rect bar = new Rect(tempX, heightBar,
 							(int) (tempX + padding * 4), originY);
 					canvas.drawRect(bar, paint);
 					/** Add it to the items array **/
 					canvas.drawText(String.valueOf(data.get(counter)), tempX + padding * 2,
 							heightBar - 5, barTextPaint);
 					barGraphObject.add(new BarObject(bar, String.valueOf(data
 							.get(counter))));
 					canvas.restore();
 					// TODO Add item to control x axis text label color
 					if (counter % graphGrouping == 0) {
 						canvas.save();
 						graphInputHandler(canvas, originX, _height, _width, originY, X_AXES_LABEL_HANDLER);
 						canvas.rotate(xAxisLabelRotation, (float) (tempX + 2.5
 								* graphGrouping * padding),
 								(originY + xAxisLabelDistance));
 						if(barLabels != null && barLabels.size() > 0) 
 							canvas.drawText(barLabels.get(barLabelsCounter++),
 									(float) (tempX + 2.5 * graphGrouping * padding),
 									originY + xAxisLabelDistance, xAxesLabelTextPaint);
 						else
 							canvas.drawText(String.valueOf(counter),
 									(float) (tempX + 2.5 * graphGrouping * padding),
 									originY + xAxisLabelDistance, xAxesLabelTextPaint);
 						canvas.restore();
 					}
 				}
 				break;
 			case Graph.STYLE_BAR_STACKED:
 				int temp = 0;
 				for (int counter = 0; counter < data.size(); counter++) {
 					if (counter > 0 && counter % graphGrouping == 0) {
 						tempX = (int) (tempX + padding * 5);
 						temp = 0;
 					}
 					if (counter % graphGrouping == 0)
 						heightBar = (int) ((originY) - (data.get(counter) * ratio));
 					else
 						heightBar = (int) ((temp) - (data.get(counter) * ratio));
 					/** Check if to draw a gradient or a single color **/
 					if(color.get((counter % graphGrouping) % color.size()).length > 1)
 						paint.setShader(new LinearGradient(tempX, heightBar,
 								(int) (tempX + padding * 4), (temp == 0) ? originY
 										: temp, color.get((counter % graphGrouping)
 										% color.size())[0],
 								color.get((counter % graphGrouping) % color.size())[1],
 								Shader.TileMode.MIRROR));
 					else if(color.get((counter % graphGrouping) % color.size()).length == 1)
 						paint.setColor(color.get((counter % graphGrouping) % color.size())[0]);
 					else
 						throw new  ClassCastException(getClass().getName() + ": Bar color not defined. Feild cannot have null values.");
 					/** Set the zoom and pan handler for the bars **/
 					canvas.save();
 					graphInputHandler(canvas, originX, _height, _width, originY, BAR_HANDLER);
 					/** Draw the bar **/
 					Rect bar = new Rect(tempX, heightBar,
 							(int) (tempX + padding * 4), (temp == 0) ? originY
 									: temp);
 					temp = heightBar;
 					canvas.drawRect(bar, paint);
 					/** Add it to the items array **/
 					barGraphObject.add(new BarObject(bar, String.valueOf(data
 							.get(counter))));
 					canvas.drawText(String.valueOf(data.get(counter)), tempX + padding * 2,
 							heightBar + (mRenderer.getXAxisLabelSize() + 2), stackBarTextPaint);
 					canvas.restore();
 					if (counter % graphGrouping == 0) {
 						canvas.save();
 						graphInputHandler(canvas, originX, _height, _width, originY, X_AXES_LABEL_HANDLER);
 						canvas.rotate(xAxisLabelRotation,
 								(float) (tempX + 2 * padding),
 								(originY + xAxisLabelDistance));
 						if(barLabels != null && barLabels.size() > 0) 
 							canvas.drawText(barLabels.get(barLabelsCounter++),
 									(float) (tempX + 2 * padding), originY
 											+ xAxisLabelDistance, xAxesLabelTextPaint);
 						else
 							canvas.drawText(String.valueOf(counter),
 									(float) (tempX + 2 * padding), originY
 											+ xAxisLabelDistance, xAxesLabelTextPaint);
 						canvas.restore();
 					}
 				}
 				break;
 			case Graph.STYLE_BAR_STACK_GROUPED:
 				int currentSecondXLabel = 0;
 				float currentSecondXLabelValue = 0;
 				int stackGroupTemp = 0;
 				for (int counter = 0; counter < data.size(); counter++) {
 					// Setting third grouping factor, X axes labels
 					if (counter > 0 && counter % 8 == 0) {
 						canvas.save();
 						if (currentSecondXLabel == 0)
 							canvas.drawText(
 									stackGroupLabels.get(currentSecondXLabel++),
 									(float) (currentSecondXLabelValue / 1.5), originY
 											+ xAxisLabelDistance + 15,
 											xAxesSecondGroupPaint);
 						else
 							canvas.drawText(
 									stackGroupLabels.get(currentSecondXLabel++),
 									currentSecondXLabelValue, originY + xAxisLabelDistance + 15,
 									xAxesSecondGroupPaint);
 						canvas.restore();
 					}
 					// Drawing the stacks
 					if (counter > 0 && counter % graphGrouping == 0) {
 						tempX = (int) (tempX + padding * 5);
 						stackGroupTemp = 0;
 					}
 					if (counter % graphGrouping == 0)
 						heightBar = (int) ((originY) - (data.get(counter) * ratio));
 					else
 						heightBar = (int) ((stackGroupTemp) - (data.get(counter) * ratio));
 					/** Check if to draw a gradient or a single color **/
 					if(color.get((counter % graphGrouping) % color.size()).length > 1)
 						paint.setShader(new LinearGradient(tempX, heightBar,
 								(int) (tempX + padding * 4),
 								(stackGroupTemp == 0) ? originY : stackGroupTemp,
 								color.get((counter % graphGrouping) % color.size())[0],
 								color.get((counter % graphGrouping) % color.size())[1],
 								Shader.TileMode.MIRROR));
 					else if(color.get((counter % graphGrouping) % color.size()).length == 1)
 						paint.setColor(color.get((counter % graphGrouping) % color.size())[0]);
 					else
 						throw new  ClassCastException(getClass().getName() + ": Bar color not defined. Feild cannot have null values.");
 					/** Set the zoom and pan handler for the bars **/
 					canvas.save();
 					graphInputHandler(canvas, originX, _height, _width, originY, BAR_HANDLER);
 					/** Draw the bar **/
 					Rect bar = new Rect(tempX, heightBar,
 							(int) (tempX + padding * 4),
 							(stackGroupTemp == 0) ? originY : stackGroupTemp);
 					stackGroupTemp = heightBar;
 					canvas.drawRect(bar, paint);
 					/** Add it to the items array **/
 					barGraphObject.add(new BarObject(bar, String.valueOf(data
 							.get(counter))));
 					canvas.drawText(String.valueOf(data.get(counter)), tempX + padding * 2,
 							heightBar + (mRenderer.getXAxisLabelSize()  + 2), stackBarTextPaint);
 					canvas.restore();
 					if (counter % graphGrouping == 0) {
 						canvas.save();
 						graphInputHandler(canvas, originX, _height, _width, originY, X_AXES_LABEL_HANDLER);
 						canvas.rotate(xAxisLabelRotation,
 								(float) (tempX + 2 * padding),
 								(originY + xAxisLabelDistance));
 						if(barLabels != null && barLabels.size() > 0) 
 							canvas.drawText(barLabels.get(barLabelsCounter++),
 									(float) (tempX + 2 * padding), originY
 											+ xAxisLabelDistance, xAxesLabelTextPaint);
 						else
 							canvas.drawText(String.valueOf(counter),
 									(float) (tempX + 2 * padding), originY
 											+ xAxisLabelDistance, xAxesLabelTextPaint);
 						canvas.restore();
 						if (counter > 0 && counter % 6 == 0) {
 							currentSecondXLabelValue = (float) (tempX + 2 * padding);
 						}
 					}
 				}
 				break;
 			}
 		}
 		
 		/**
 		 * 
 		 * @param canvas
 		 * @param originX
 		 * @param _height
 		 * @param _width
 		 * @param originY
 		 * @param HANDLER_CASE
 		 */
 		private void graphInputHandler(Canvas canvas, int originX, int _height, int _width, int originY, int HANDLER_CASE) {
 			switch(HANDLER_CASE) {
 				case BAR_HANDLER:
 					if(mRenderer.getGraphIsZoomable() || mRenderer.getGraphIsPannable())
 						canvas.clipRect(originX, (int) (_height), _width, originY, android.graphics.Region.Op.REPLACE);
 					canvas.scale(mScaleFactor, mScaleFactor, originX, originY);
 					canvas.translate(translateX / mScaleFactor, translateY / mScaleFactor);
 					break;
 				case X_AXES_LABEL_HANDLER:
 					if(mRenderer.getGraphIsZoomable() || mRenderer.getGraphIsPannable())
 						canvas.clipRect(originX, originY, _width, graphHeight, android.graphics.Region.Op.REPLACE);
 					canvas.scale(mScaleFactor, mScaleFactor, originX, originY);
 					canvas.translate(translateX / mScaleFactor, 0);
 					break;
 				case Y_AXES_LABEL_HANDLER:
 					if(mRenderer.getGraphIsZoomable() || mRenderer.getGraphIsPannable())
 						canvas.clipRect(0, (int) (_height), originX, originY, android.graphics.Region.Op.REPLACE);
 					canvas.scale(mScaleFactor, mScaleFactor, originX, originY);
 					canvas.translate(0, translateY / mScaleFactor);
 					break;
 			}
 		}
 	
 		private class ScaleListener extends
 				ScaleGestureDetector.SimpleOnScaleGestureListener {
 	
 			@Override
 			public boolean onScaleBegin(ScaleGestureDetector detector) {
 				graphIsScaling = true;
 				return super.onScaleBegin(detector);
 			}
 	
 			@Override
 			public boolean onScale(ScaleGestureDetector detector) {
 				mScaleFactor *= detector.getScaleFactor();
 				// Don't let the object get too small or too large.
 				mScaleFactor = Math.max(
 						Math.min(mScaleFactor, mRenderer.getMaxZoom()),
 						mRenderer.getMinZoom());
 				invalidate();
 				return true;
 			}
 	
 			@Override
 			public void onScaleEnd(ScaleGestureDetector detector) {
 				graphIsScaling = false;
 				super.onScaleEnd(detector);
 			}
 		}
 	}
 }
