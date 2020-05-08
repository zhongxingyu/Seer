 package edu.berkeley.cs160.smartnature;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Canvas;
import android.graphics.Color;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.graphics.drawable.Drawable;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class GardenView extends View implements View.OnClickListener, View.OnLongClickListener, View.OnTouchListener {
 	
 	GardenScreen context;
 	Garden garden;
 	/** plot that is currently pressed */
 	Plot focusedPlot;
 	/** the entire transformation matrix applied to the canvas */
 	Matrix m = new Matrix();
 	/** translation matrix applied to the canvas */
 	Matrix dragMatrix = new Matrix();
 	/** translation matrix applied to the background */
 	Matrix bgDragMatrix = new Matrix();
 	Drawable bg;
 	Paint textPaint;
 	float x, y, prevX, prevY, downX, downY;
 	float dist, prevDist;
 	float zoomScale = 1;
 	float textSize;
 	boolean portraitMode;
 	int tempColor;
 	
 	private final static int IDLE = 0, TOUCH_SCREEN = 1, DRAG_SCREEN = 2, PINCH_ZOOM = 3;
 	private int mode;
 	
 	public GardenView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		this.context = (GardenScreen) context;
 		textSize = getResources().getDimension(R.dimen.labelsize_default);
 		initPaint();
 		bg = getResources().getDrawable(R.drawable.tile);	
 		initPaint();
 		initMockData();	
 		setOnClickListener(this);
 		setOnTouchListener(this);
 		setOnLongClickListener(this);
 	}
 	
 	public void initMockData() {
 		garden = context.mockGarden;
 		for (Plot plot : garden.getPlots()) {
 			Paint p = plot.getPaint();
 			p.setStrokeWidth(getResources().getDimension(R.dimen.strokesize_default));
 		}
 	}
 	
 	public void initPaint() {
 		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FAKE_BOLD_TEXT_FLAG|Paint.DEV_KERN_TEXT_FLAG);
 		textPaint.setTextAlign(Paint.Align.CENTER);
 		textPaint.setTextScaleX(getResources().getDimension(R.dimen.labelxscale_default));
 		textPaint.setTextSize(textSize);
 	}
 	
 	/** called when user clicks "zoom to fit" */
 	public void reset() {
 		zoomScale = 1;
 		textPaint.setTextScaleX(getResources().getDimension(R.dimen.labelxscale_default));
 		textPaint.setTextSize(textSize);
 		dragMatrix.reset();
 		bgDragMatrix.reset();
 		invalidate();
 	}
 	
 	@Override
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 		int width = getWidth(), height = getHeight();
 		portraitMode = width < height;
 		
 		// draw background grid
 		canvas.save();
 		canvas.concat(bgDragMatrix);
 		bg.setBounds(canvas.getClipBounds());
 		bg.draw(canvas);
 		canvas.restore();
 		
 		m.reset();
 		RectF gardenBounds = context.showFullScreen ? garden.getBounds() : garden.getBounds(portraitMode);
 		m.setRectToRect(gardenBounds, getBounds(), Matrix.ScaleToFit.CENTER);
 		if (portraitMode)
 			m.postRotate(90, width/2f, width/2f);
 		m.postConcat(dragMatrix);
 		m.postScale(zoomScale, zoomScale, width/2f, height/2f);
 		
 		// draw plots
 		canvas.save();
 		canvas.concat(m);
 		for (Plot p: garden.getPlots()) {
 			canvas.save();
 			Rect bounds = p.getBounds();
 			canvas.rotate(p.getAngle(), bounds.centerX(), bounds.centerY());
 			p.getShape().draw(canvas);
 			canvas.restore();
 		}
 		canvas.restore();
 		
 		//draw labels
 		if (context.showLabels)
 			for (Plot p: garden.getPlots()) {
 				RectF rbounds = p.getRotateBounds(); 
 				float[] labelLoc;
 				if (portraitMode)
 					labelLoc = new float[] { rbounds.left - 10, rbounds.centerY() };
 				else
 					labelLoc = new float[] { rbounds.centerX(), rbounds.top - 10 };
 				m.mapPoints(labelLoc);
 				canvas.drawText(p.getName().toUpperCase(), labelLoc[0], labelLoc[1], textPaint);
 			}
 	}
 	
 	public RectF getBounds() {
 		if (portraitMode)
 			return new RectF(getLeft(), getTop(), getBottom(), getRight());
 		else
 			return new RectF(getLeft(), getTop(), getRight(), getBottom());
 	}
 	
 	@Override
 	public void onAnimationEnd() {
 		zoomScale *= Math.pow(getResources().getDimension(R.dimen.zoom_scalar), context.zoomPressed);
 		textPaint.setTextSize(Math.max(textSize * zoomScale, getResources().getDimension(R.dimen.labelsize_min)));
 		invalidate();
 		context.zoomPressed = 0;
 	}
 	
 	@Override
 	public void onClick(View view) {
 		if (focusedPlot != null) {
 			Intent intent = new Intent(context, PlotScreen.class);
 			intent.putExtra("name", focusedPlot.getName());
 			intent.putExtra("garden_id", GardenGnome.gardens.indexOf(garden));
 			intent.putExtra("plot_id", garden.getPlots().indexOf(focusedPlot));
 			context.startActivityForResult(intent, GardenScreen.VIEW_PLOT);
 			//context.handleZoom();
 		}
 	}
 	
 	@Override
 	public boolean onLongClick(View view) {
 		if (focusedPlot != null) {
 			Intent intent = new Intent(context, EditScreen.class);
 			intent.putExtra("name", focusedPlot.getName());
 			intent.putExtra("garden_id", GardenGnome.gardens.indexOf(garden));
 			intent.putExtra("plot_id", garden.indexOf(focusedPlot));
 			intent.putExtra("zoom_scale", zoomScale);
 			float[] values = new float[9], bgvalues = new float[9];
 			dragMatrix.getValues(values);
 			bgDragMatrix.getValues(bgvalues);
 			intent.putExtra("drag_matrix", values);
 			intent.putExtra("bgdrag_matrix", bgvalues);
 			focusedPlot.getPaint().setColor(tempColor);
 			context.startActivityForResult(intent, 0);
 			context.overridePendingTransition(0, 0);
 			return true;
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean onTouch(View view, MotionEvent event) {
 		onTouchEvent(event);
 		context.handleZoom();
 		x = event.getX(); y = event.getY();
 		
 		//System.out.println(event.getPointerCount() + " pointers, " + event.getAction());
 		switch (event.getAction() & MotionEvent.ACTION_MASK) {
 		case MotionEvent.ACTION_DOWN:
 			mode = TOUCH_SCREEN;
 			downX = x; downY = y;
 			focusedPlot = garden.plotAt(x, y, m);
 			if (focusedPlot != null) {
 				// set focused plot appearance
 				tempColor = focusedPlot.getPaint().getColor();
 				focusedPlot.getPaint().setColor(getResources().getColor(R.color.focused_plot));
 				focusedPlot.getPaint().setStrokeWidth(getResources().getDimension(R.dimen.strokesize_active));
 			}
 			break;
 		case MotionEvent.ACTION_POINTER_DOWN:
 			float diffX = event.getX(0) - event.getX(1);
 			float diffY = event.getY(0) - event.getY(1);
 			dist = diffX * diffX + diffY * diffY;
 			if (dist > 10) {
 				mode = PINCH_ZOOM;
 				if (focusedPlot != null) {
 					focusedPlot.getPaint().setColor(tempColor);
 					focusedPlot.getPaint().setStrokeWidth(getResources().getDimension(R.dimen.strokesize_default));
 					focusedPlot = null;
 				}
 			}
 			break;
 		case MotionEvent.ACTION_POINTER_UP:
 			mode = IDLE;
 			break;
 		case MotionEvent.ACTION_MOVE:
 			if (mode == PINCH_ZOOM) {
 				prevDist = dist;
 				diffX = event.getX(0) - event.getX(1);
 				diffY = event.getY(0) - event.getY(1);
 				dist = diffX * diffX + diffY * diffY;
 				//zoomScale *= dist/prevDist;
 				zoomScale = (zoomScale * (dist/prevDist + 1))/2; // less "sensitive"
 				
 				onAnimationEnd();
 			}
 			else if (mode != IDLE) {
 				float dx = x - prevX, dy = y - prevY;
 				dragMatrix.postTranslate(dx / zoomScale, dy / zoomScale);
 				bgDragMatrix.postTranslate(dx, dy);
 				if (mode == TOUCH_SCREEN && (Math.abs(downX - x) > 5 || Math.abs(downY - y) > 5)) // show some leniency
 					mode = DRAG_SCREEN;
 				if (mode == DRAG_SCREEN && focusedPlot != null) {
 					// plot can no longer be clicked so reset appearance
 					focusedPlot.getPaint().setColor(tempColor);
 					focusedPlot.getPaint().setStrokeWidth(getResources().getDimension(R.dimen.strokesize_default));
 					focusedPlot = null; 
 				}
 			}
 			break;
 			
 		case MotionEvent.ACTION_UP:
 			mode = IDLE;
 			if (focusedPlot != null) {
 				// reset clicked plot appearance
 				focusedPlot.getPaint().setColor(tempColor);
 				focusedPlot.getPaint().setStrokeWidth(3);
 			}
 			break;
 		}
 		
 		prevX = x;
 		prevY = y;
 		invalidate();
 		
 		return true;
 	}
 	
 }
