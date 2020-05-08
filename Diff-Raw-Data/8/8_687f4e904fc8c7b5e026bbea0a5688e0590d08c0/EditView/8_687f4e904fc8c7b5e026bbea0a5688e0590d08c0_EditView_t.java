 package edu.berkeley.cs160.smartnature;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.PorterDuff.Mode;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.graphics.drawable.Drawable;
 import android.graphics.drawable.ShapeDrawable;
 import android.graphics.drawable.shapes.RectShape;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Toast;
 
 public class EditView extends View implements View.OnClickListener, View.OnTouchListener {
 
 	EditScreen context;
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
 	int zoomLevel;
 	float prevX, prevY, downX, downY, x, y, zoomScale = 1;
 	float textSize;
 	boolean portraitMode, dragMode;
 
 	private int status;
 	private final static int START_DRAGGING = 0;
 	private final static int STOP_DRAGGING = 1;
 
 	public EditView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		this.context = (EditScreen) context;
 		textSize = 15.5f * getResources().getDisplayMetrics().scaledDensity;
 		initPaint();
 		bg = getResources().getDrawable(R.drawable.tile);	
 		initPaint();
 		initMockData();	
 		setOnClickListener(this);
 		setOnTouchListener(this);
 	}
 
 	public void initMockData() {
 		garden = this.context.mockGarden;
 		for (Plot plot : garden.getPlots()) {
 			Paint p = plot.getShape().getPaint();
 			p.setStyle(Paint.Style.STROKE);
 			p.setStrokeWidth(3);
 			p.setStrokeCap(Paint.Cap.ROUND);
 			p.setStrokeJoin(Paint.Join.ROUND);
 		}		
 	}
 
 	public void initPaint() {
 		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FAKE_BOLD_TEXT_FLAG|Paint.DEV_KERN_TEXT_FLAG);
 		textPaint.setTextSize(textSize);
 		textPaint.setTextScaleX(1.2f);
 		textPaint.setTextAlign(Paint.Align.CENTER);
 	}
 
 	/** called when user clicks "zoom to fit" */
 	public void reset() {
 		zoomLevel = 0;
 		zoomScale = 1;
 		textPaint.setTextSize(textSize);
 		textPaint.setTextScaleX(1.2f);
 		dragMatrix.reset();
 		bgDragMatrix.reset();
 		invalidate();
 	}
 
 	@Override
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 		int width = getWidth(), height = getHeight();
 		portraitMode = width < height;
 
 		canvas.save();
 		canvas.concat(bgDragMatrix);	
 		bg.setBounds(canvas.getClipBounds());
 		bg.draw(canvas); //canvas.drawRGB(255, 255, 255);
 		canvas.restore();
 
 		m.reset();
 		RectF gardenBounds = context.showFullScreen ? garden.getBounds() : garden.getBounds(portraitMode);
 		m.setRectToRect(gardenBounds, getBounds(), Matrix.ScaleToFit.CENTER);
 		if (portraitMode) {
 			m.postRotate(90);
 			m.postTranslate(width, 0);
 		}
 		m.postConcat(dragMatrix);
 
 		if (zoomLevel != 0) {
 			float zoomShift = (1 - zoomScale) / 2;
 			m.postScale(zoomScale, zoomScale);
 			m.postTranslate(zoomShift * width, zoomShift * height);
 		}
 
 		canvas.save();
 		canvas.concat(m);
 		for (Plot p: garden.getPlots()) {
 			if (p != context.newPlot) {
 				canvas.save();
 				Rect shapeBounds = p.getShape().getBounds();
 				canvas.rotate(p.getAngle(), shapeBounds.centerX(), shapeBounds.centerY());
 				p.getShape().draw(canvas);
 				canvas.restore();
 			}
 		}
 		
 		// "shade" over everything
 		canvas.restore();
 		canvas.drawARGB(100, 0, 0, 0);
 		
 		// draw plot being edited
 		canvas.save();
 		canvas.concat(m);
 		Rect shapeBounds = context.newPlot.getShape().getBounds();
 		canvas.rotate(context.newPlot.getAngle(), shapeBounds.centerX(), shapeBounds.centerY());
 		context.newPlot.getShape().draw(canvas);
 		canvas.restore();
 		
 		if (context.showLabels)
 			for (Plot p: garden.getPlots()) {
 				Rect bounds = p.getShape().getBounds();
 				float[] labelLoc = portraitMode ? new float[] {bounds.left - 10, bounds.centerY()} : new float[] {bounds.centerX(), bounds.top - 10};
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
 		zoomLevel = context.zoomLevel;
 		zoomScale = (float) Math.pow(1.5, zoomLevel);
 		textPaint.setTextSize(Math.max(10, textSize * zoomScale));
 		invalidate();
 	}
 
 	@Override
 	public void onClick(View view) {
 		if (focusedPlot != null)
 			Toast.makeText(context, "clicked " + focusedPlot.getName(), Toast.LENGTH_SHORT).show();
 	}
 
 	@Override
 	public boolean onTouch(View view, MotionEvent event) {
 		context.handleZoom();
 		x = event.getX(); y = event.getY();
 		if (context.getDragPlot()) {
 			switch(event.getAction()) {
 			case(MotionEvent.ACTION_DOWN):
				Matrix inv = new Matrix();
				m.invert(inv);
				float[] xy = { x, y };
				inv.mapPoints(xy);
				if (context.newPlot.contains(xy[0], xy[1])) {
					focusedPlot = context.newPlot;
 					// set focused plot appearance
 					focusedPlot.getShape().getPaint().setColor(0xFF7BB518);
 					focusedPlot.getShape().getPaint().setStrokeWidth(5);
 					status = START_DRAGGING;
 				}
 			break;
 
 			case(MotionEvent.ACTION_UP):
 				status = STOP_DRAGGING;
 			if(focusedPlot != null) {
 				focusedPlot.getShape().getPaint().setColor(Color.BLACK);
 				focusedPlot.getShape().getPaint().setStrokeWidth(3);
 			}
 			break;
 
 			case(MotionEvent.ACTION_MOVE):
 			if(status == START_DRAGGING && focusedPlot != null) {
 				//focusedPlot.getShape().getPaint().setColor(0xFF7BB518);
 				//focusedPlot.getShape().getPaint().setStrokeWidth(5);
 				//if(focusedPlot.getType() == 0) {
 					float[] dxy = {x, y, prevX, prevY};
 					//float[] dxy = {x - prevX, x - prevY};
 					Matrix inverse = new Matrix();
 					m.invert(inverse);
 					inverse.mapPoints(dxy);
 					focusedPlot.getShape().getBounds().offset((int) (- dxy[2] + dxy[0]), (int) (- dxy[3] + dxy[1]));
 					//shape.setBounds((int) (focusedPlot.getShape().getBounds().left + dxy[0]),(int) (focusedPlot.getShape().getBounds().top + dxy[1]), (int) (focusedPlot.getShape().getBounds().right + dxy[0]), (int) (focusedPlot.getShape().getBounds().bottom + dxy[1]));
 					//focusedPlot.setShape(shape);
 				//}
 			}
 			break;
 			}
 
 
 		}
 		else {
 			if(event.getAction() == MotionEvent.ACTION_DOWN) {
 				dragMode = false;
 				downX = x; downY = y;
 				focusedPlot = garden.plotAt(x, y, m);
 				if (focusedPlot != null) {
 					// set focused plot appearance
 					focusedPlot.getShape().getPaint().setColor(0xFF7BB518);
 					focusedPlot.getShape().getPaint().setStrokeWidth(5);
 				}
 
 			}
 			else {
 				float dx = x - prevX, dy = y - prevY;
 				dragMatrix.postTranslate(dx / zoomScale, dy / zoomScale);
 				bgDragMatrix.postTranslate(dx, dy);
 				if (!dragMode)
 					dragMode = Math.abs(downX - x) > 5 || Math.abs(downY - y) > 5; // show some leniency
 					if (dragMode && focusedPlot != null) {
 						// plot can no longer be clicked so reset appearance
 						focusedPlot.getShape().getPaint().setColor(Color.BLACK);
 						focusedPlot.getShape().getPaint().setStrokeWidth(3);
 					}
 			}
 			// onClick for some reason doesn't execute on its own so manually do it
 			if (event.getAction() == MotionEvent.ACTION_UP && !dragMode) {
 				if (focusedPlot != null) {
 					// reset clicked plot appearance
 					focusedPlot.getShape().getPaint().setColor(Color.BLACK);
 					focusedPlot.getShape().getPaint().setStrokeWidth(3);
 				}
 				performClick();
 			}
 		}
 		prevX = x;
 		prevY = y;
 		invalidate();
 		return true;
 	}
 
 
 
 }
