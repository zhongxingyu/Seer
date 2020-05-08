 package edu.berkeley.cs160.smartnature;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.DashPathEffect;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.graphics.drawable.Drawable;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.GestureDetector;
 
 public class EditView extends View implements View.OnClickListener, View.OnLongClickListener, View.OnTouchListener, GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
 
 	EditScreen context;
 	Garden garden;
 	/** plot that is currently pressed */
 	Plot editPlot;
 	/** the entire transformation matrix applied to the canvas */
 	Matrix m = new Matrix();
 	/** translation matrix applied to the canvas */
 	Matrix dragMatrix = new Matrix();
 	/** translation matrix applied to the background */
 	Matrix bgDragMatrix = new Matrix();
 	/** coordinates of shape after transformation */ 
 	float[] shapeMid = new float[2], shapeTop = new float[2];
 	ArrayList<Float> polyPts = new ArrayList<Float>();
 	GestureDetector gestureScanner;
 	Drawable bg;
 	RectF resizeBox = new RectF();
 	Path resizeArrow = new Path();
 	Paint textPaint, whitePaint, boundPaint, arrowPaint, resizePaint, rotatePaint;
 	Paint polyPaint, pointPaint, focPolyPaint, lastPointPaint;
 	float prevX, prevY, x, y;
 	float textSize;
 	float zoomClamp = 1, zoomScale = 1;
 	boolean portraitMode;
 	int tempColor, focPlotColor;
 	/** index of focused point */
 	int focPoint = -1;
 	
 	private final static int IDLE = 0, DRAG_SCREEN = 1, DRAG_SHAPE = 2, ROTATE_SHAPE = 3, RESIZE_SHAPE = 4;
 	private final static int TOUCH_POINT = 5, DRAG_POINT = 6, HOLD_POINT = 7;
 	private int mode;
 	
 	public EditView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		this.context = (EditScreen) context;
 		editPlot = this.context.plot;
 		bg = getResources().getDrawable(R.drawable.tile_dark);	
 		textSize = getResources().getDimension(R.dimen.labelsize_default);
 		initPaint();
 		initMockData();	
 		setOnClickListener(this);
 		setOnLongClickListener(this);
 		setOnTouchListener(this);
 		//GestureOverlayView gestures = (GestureOverlayView) findViewById(R.id.addEntryButton);
 		gestureScanner = new GestureDetector(context, this);
 		//gestureScanner.setOnDoubleTapListener(this);
 	}
 	
 	public void initMockData() {
 		garden = this.context.mockGarden;
 		for (Plot plot : garden.getPlots()) {
 			Paint p = plot.getShape().getPaint();
 			p.setStyle(Paint.Style.STROKE);
 			p.setStrokeCap(Paint.Cap.ROUND);
 			p.setStrokeJoin(Paint.Join.ROUND);
 		}		
 	}
 	
 	public void initPaint() {
 		focPlotColor = getResources().getColor(R.color.focused_plot);
 		
 		whitePaint = new Paint();
 		whitePaint.setColor(Color.WHITE);
 		
 		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FAKE_BOLD_TEXT_FLAG|Paint.DEV_KERN_TEXT_FLAG);
 		textPaint.setTextAlign(Paint.Align.CENTER);
 		textPaint.setTextScaleX(getResources().getDimension(R.dimen.labelxscale_default));
 		textPaint.setTextSize(textSize);
 		
 		resizePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 		int medGray = getResources().getColor(R.color.MEDGRAY);
 		resizePaint.setColor(medGray);
 		resizePaint.setStrokeCap(Paint.Cap.ROUND);
 		resizePaint.setStrokeWidth(getResources().getDimension(R.dimen.strokesize_default));
 		resizePaint.setStyle(Paint.Style.STROKE);
 		
 		polyPaint = new Paint(resizePaint);
 		polyPaint.setStrokeWidth(getResources().getDimension(R.dimen.strokesize_edit));
 		
 		pointPaint = new Paint(resizePaint);
 		pointPaint.setColor(Color.DKGRAY);
 		pointPaint.setStrokeWidth(zoomClamp * getResources().getDimension(R.dimen.point_size));
 		
 		lastPointPaint = new Paint(pointPaint);
 		lastPointPaint.setColor(0xff700000);
 		lastPointPaint.setStyle(Paint.Style.FILL_AND_STROKE);
 		
 		focPolyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 		focPolyPaint.setColor(focPlotColor);
 		focPolyPaint.setStyle(Paint.Style.STROKE);
 		focPolyPaint.setStrokeWidth(getResources().getDimension(R.dimen.strokesize_editactive));//getResources().getDimension(R.dimen.point_size) / 4);
 		//focPolyPaint.setShadowLayer(2, 0, 0, focPlotColor);
 		
 		arrowPaint = new Paint(resizePaint);
 		arrowPaint.setStyle(Paint.Style.FILL_AND_STROKE);
 		
 		rotatePaint = new Paint(resizePaint);
 		rotatePaint.setPathEffect(new DashPathEffect(new float[] {6, 8}, 1));
 		
 		boundPaint = new Paint(resizePaint);
 		boundPaint.setColor(Color.GRAY);
 		boundPaint.setPathEffect(new DashPathEffect(new float[] {8, 8}, 1));
 	}
 	
 	/** called when user clicks "zoom to fit" */
 	public void reset() {
 		zoomClamp = zoomScale = 1;
 		textPaint.setTextScaleX(getResources().getDimension(R.dimen.labelxscale_default));
 		textPaint.setTextSize(textSize);
 		pointPaint.setStrokeWidth(getResources().getDimension(R.dimen.point_size));
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
 		
 		Rect shapeBounds = editPlot.getBounds();
 		shapeMid[0] = shapeBounds.centerX(); shapeMid[1] = shapeBounds.centerY();
 		shapeTop[0] = shapeBounds.left; shapeTop[1] = shapeBounds.top;
 		m.mapPoints(shapeMid);
 		m.mapPoints(shapeTop);
 
 		canvas.save();
 		drawPlots(canvas);
 		canvas.restore();
 		
 		if (context.createPoly) 
 			drawPoly(canvas);
 		else {
 			canvas.save();
 			drawResizeBox(canvas);
 			drawRotate(canvas);
 			canvas.restore();
 		}
 		
 		if (context.showLabels)
 			drawLabels(canvas);
 	}
 	
 	public void drawPlots(Canvas canvas) {
 		canvas.concat(m);
 		for (int i = 0; i < garden.size() - (context.createPoly ? 2 : 1); i++) {
 			Plot p = garden.getPlot(i);
 			canvas.save();
 			Rect shapeBounds = p.getBounds();
 			canvas.rotate(p.getAngle(), shapeBounds.centerX(), shapeBounds.centerY());
 			// check special case
 			if (p == editPlot) {
 				Paint paint = editPlot.getShape().getPaint();
 				Paint oldPaint = new Paint(paint);
 				canvas.drawRect(shapeBounds, boundPaint); //draw rectangular bounds
 				paint.set(whitePaint);
 				editPlot.getShape().draw(canvas);
 				paint.set(oldPaint);
 			}				
 			p.getShape().draw(canvas);
 			canvas.restore();
 		}
 	}
 	
 	public void drawPoly(Canvas canvas) {
 		float[] pts = EditScreen.toFloatArray(polyPts);
 		m.mapPoints(pts);
 		int len = pts.length;
 		canvas.drawLines(pts, polyPaint);
 		if (len >= 6) {
 			canvas.drawLines(pts, 2, len - 2, polyPaint);
 			canvas.drawLine(pts[len - 2], pts[len - 1], pts[0], pts[1], boundPaint);
 		}
 		if (focPoint != -1 && len >= 4) {
 			if (focPoint <= len - 4)
 				canvas.drawLine(pts[focPoint], pts[focPoint+1], pts[focPoint+2], pts[focPoint+3], focPolyPaint);		
			if (focPoint >= 2)
 				canvas.drawLine(pts[focPoint-2], pts[focPoint-1], pts[focPoint], pts[focPoint+1], focPolyPaint);
 		}
 		
 		canvas.drawPoints(pts, pointPaint);
 		lastPointPaint.setStrokeWidth(pointPaint.getStrokeWidth());
 		if (pts.length > 0)
 			canvas.drawPoint(pts[len - 2], pts[len - 1], lastPointPaint);
 		
 		if (focPoint != -1) {
 			float ptRadius = pointPaint.getStrokeWidth() / 2;
 			canvas.drawCircle(pts[focPoint], pts[focPoint + 1], ptRadius + 10, focPolyPaint);
 		}
 	}
 	
 	public void drawResizeBox(Canvas canvas) {
 		Rect shapeBounds = editPlot.getBounds();
 		float boxSize = zoomClamp * getResources().getDimension(R.dimen.resizebox_min);
 		float[] boxCorner = { shapeBounds.right, portraitMode ? shapeBounds.top : shapeBounds.bottom };
 		
 		m.mapPoints(boxCorner);
 			resizeBox.set(boxCorner[0] - boxSize, boxCorner[1] - boxSize, boxCorner[0], boxCorner[1]);
 		canvas.rotate(editPlot.getAngle(), shapeMid[0], shapeMid[1]);
 		canvas.drawRect(resizeBox, whitePaint);
 		canvas.drawRect(resizeBox, resizePaint);
 		
 		Path arrows = new Path();
 		float arrowOffset = 5;
 		float rarrowOffset = boxSize - 5;
 		float pt1 = 5 * zoomClamp * (zoomClamp == 1 ? 1 : 1.5f);
 		float pt2 = pt1 / 2.5f;
 		float pt3 = pt1 - pt2;
 		resizeArrow.reset();
 		resizeArrow.moveTo(arrowOffset, arrowOffset);
 		resizeArrow.rLineTo(pt1, pt2);
 		resizeArrow.rLineTo(-pt3, pt3);
 		resizeArrow.close();
 		resizeArrow.moveTo(rarrowOffset, rarrowOffset);
 		resizeArrow.rLineTo(-pt1, -pt2);
 		resizeArrow.rLineTo(pt3, -pt3);
 		resizeArrow.close();
 		resizeArrow.moveTo(arrowOffset, arrowOffset);
 		resizeArrow.lineTo(rarrowOffset, rarrowOffset);
 		resizeArrow.offset(resizeBox.left, resizeBox.top, arrows);
 		canvas.drawPath(arrows, arrowPaint);
 	}
 	
 	public void drawRotate(Canvas canvas) {
 		float radius = zoomClamp * getResources().getDimension(R.dimen.rotate_radius);
 		float rotateX = shapeMid[0];
 		float rotateY = shapeTop[1] - zoomClamp * getResources().getDimension(R.dimen.rotate_offset);
 		canvas.drawLine(shapeMid[0], shapeMid[1], rotateX, rotateY - radius, rotatePaint);
 		canvas.drawCircle(rotateX, rotateY, radius, whitePaint);
 		canvas.drawCircle(rotateX, rotateY, radius, rotatePaint);
 	}
 	
 	public void drawLabels(Canvas canvas) {
 		for (int i = 0; i < garden.size() - (context.createPoly ? 2 : 1); i++) {
 			Plot p = garden.getPlot(i);
 			float[] labelLoc;
 			RectF bounds = p.getRotateBounds();
 			if (portraitMode)
 				labelLoc = new float[] { bounds.left - 10, bounds.centerY() };
 			else
 				labelLoc =  new float[] { bounds.centerX(), bounds.top - 10 };
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
 		zoomScale *= (float) Math.pow(getResources().getDimension(R.dimen.zoom_scalar), context.zoomPressed);
 		zoomClamp = Math.max(1, zoomScale);
 		textPaint.setTextSize(Math.max(textSize * zoomScale, getResources().getDimension(R.dimen.labelsize_min)));
 		pointPaint.setStrokeWidth(zoomClamp * getResources().getDimension(R.dimen.point_size));
 		invalidate();
 		context.zoomPressed = 0; 
 	}
 	
 	@Override
 	public void onClick(View view) {
 	}
 	
 
 	@Override
 	public boolean onLongClick(View view) {
 		System.out.println("LONG CLICK");
 		if (context.createPoly && mode != DRAG_POINT && focPoint != -1) {
 			/*polyPts.remove(focPoint + 1);
 			polyPts.remove(focPoint);
 			mode = IDLE;
 			focPoint = -1;*/
 			mode = HOLD_POINT;
 			return true;
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public boolean onTouch(View view, MotionEvent event) {
 		gestureScanner.onTouchEvent(event);
 		onTouchEvent(event);
 		context.handleZoom();
 		x = event.getX();
 		y = event.getY();
 		if (context.createPoly)
 			handlePoly(event);
 		else switch (event.getAction()) {
 			case MotionEvent.ACTION_DOWN:
 				handleDown();
 				break;
 			case MotionEvent.ACTION_MOVE:
 				handleMove();
 				break;
 			case MotionEvent.ACTION_UP:
 				handleUp();
 				break;
 		}	
 		prevX = x;
 		prevY = y;
 		invalidate();
 		return true;
 	}
 	
 	float downX, downY;
 	
 	public void handlePoly(MotionEvent event) {
 		if (event.getAction() == MotionEvent.ACTION_DOWN && mode != DRAG_POINT) { // so that created point won't respond to long click
 			System.out.println("ACTION DOWN");
 			downX = x;
 			downY = y;
 			float[] xy = { x, y };
 			Matrix inverse = new Matrix();
 			m.invert(inverse);
 			inverse.mapPoints(xy);
 			
 			float hitRadius = getResources().getDimension(R.dimen.point_size) / 2 + 10;
 			for (int i = 0; i < polyPts.size(); i += 2) {
 				float dx = xy[0] - polyPts.get(i);
 				float dy = xy[1] - polyPts.get(i + 1);
 				
 				if (dx * dx + dy * dy < hitRadius * hitRadius) {
 					mode = TOUCH_POINT;
 					focPoint = i;
 					break;
 				}
 			}
 		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
 			System.out.println("ACTION MOVE");
 			if ((mode == TOUCH_POINT || mode == HOLD_POINT) && (Math.abs(downX - x) > 5 || Math.abs(downY - y) > 5)) // show some leniency
 				mode = DRAG_POINT;
 			if (mode == TOUCH_POINT || mode == DRAG_POINT || mode == HOLD_POINT) { // simply focPoint != 1?
 				float[] xy = { x, y };
 				Matrix inverse = new Matrix();
 				m.invert(inverse);
 				inverse.mapPoints(xy);
 				polyPts.set(focPoint, xy[0]);
 				polyPts.set(focPoint + 1, xy[1]);
 				
 				return;
 			}
 			if (mode != DRAG_SCREEN && (Math.abs(downX - x) > 5 || Math.abs(downY - y) > 5)) // show some leniency
 				mode = DRAG_SCREEN;
 			if (mode == DRAG_SCREEN)
 				handleMove();
 			
 		} else if (event.getAction() == MotionEvent.ACTION_UP) {
 			System.out.println("ACTION UP");
 			if (mode == HOLD_POINT) {
 				polyPts.remove(focPoint + 1);
 				polyPts.remove(focPoint);
 			}
 			mode = IDLE;
 			focPoint = -1;
 		}
 	}
 	
 	public void handleDown() {
 		tempColor = editPlot.getPaint().getColor(); // backup data
 		
 		// check if resize box hit
 		float[] rxy = { x, y };
 		Matrix rot = new Matrix();
 		rot.setRotate(-editPlot.getAngle(), shapeMid[0], shapeMid[1]);
 		rot.mapPoints(rxy); // transformed coordinates with plot's rotation 
 		resizeBox.inset(-5, -5);
 		if (resizeBox.contains(rxy[0], rxy[1])) {
 			mode = RESIZE_SHAPE;
 			// set active resize appearance
 			resizePaint.setColor(focPlotColor);
 			arrowPaint.setColor(focPlotColor);
 			return;
 		}
 		
 		// check if rotate circle hit
 		float rRadius = zoomClamp * getResources().getDimension(R.dimen.rotate_radius) + 5;
 		float rdx = shapeMid[0] - rxy[0];
 		float rdy = shapeTop[1] - zoomClamp * getResources().getDimension(R.dimen.rotate_offset) - rxy[1];
 		if (rdx * rdx + rdy * rdy < rRadius * rRadius) {
 			mode = ROTATE_SHAPE;
 			// set active rotate appearance
 			rotatePaint.setColor(focPlotColor);
 			//rotatePaint.setShadowLayer(4, 0, 0, focPlotColor);
 			return;
 		}
 		
 		// check if shape hit
 		float[] xy = { x, y };
 		Matrix inverse = new Matrix();
 		m.invert(inverse);
 		inverse.mapPoints(xy); // transformed coordinates
 		inverse.postRotate(-editPlot.getAngle(), editPlot.getBounds().centerX(), editPlot.getBounds().centerY());
 		if (editPlot.contains(xy[0], xy[1])) {
 			mode = DRAG_SHAPE;
 			// set active resize appearance
 			editPlot.getPaint().setStrokeWidth(getResources().getDimension(R.dimen.strokesize_editactive));
 			editPlot.getPaint().setColor(focPlotColor);
 			return;
 		}
 		
 		mode = DRAG_SCREEN;	
 	}
 	
 	public void handleMove() {
 		float[] dxy = { prevX, prevY, x, y };
 		
 		if (mode == DRAG_SCREEN) {
 			float dx = dxy[2] - dxy[0], dy = dxy[3] - dxy[1];
 			dragMatrix.postTranslate(dx / zoomScale, dy / zoomScale);
 			bgDragMatrix.postTranslate(dx, dy);
 			return;
 		}
 		
 		Matrix inverse = new Matrix();
 		m.invert(inverse);
 		
 		if (mode == DRAG_SHAPE) {
 			inverse.mapPoints(dxy);
 			editPlot.getBounds().offset((int) (dxy[2] - dxy[0]), (int) (dxy[3] - dxy[1]));
 		}
 		else if (mode == RESIZE_SHAPE) {
 			Rect newBounds = new Rect(editPlot.getBounds());
 			inverse.postRotate(-editPlot.getAngle(), newBounds.centerX(), newBounds.centerY());
 			inverse.mapPoints(dxy);
 			int dx = (int) (dxy[2] - dxy[0]);
 			int dy = (int) (dxy[3] - dxy[1]);
 			newBounds.inset(-dx, portraitMode ? dy : -dy);
 			
 			float minSize = getResources().getDimension(R.dimen.resizebox_min) + 2; 
 			if (newBounds.width() > minSize && newBounds.height() > minSize)
 				editPlot.getShape().setBounds(newBounds);
 		}
 		else if (mode == ROTATE_SHAPE) {
 			float dx = dxy[2] - shapeMid[0];
 			float dy = dxy[3] - shapeMid[1];
 			float angle = -(float)Math.toDegrees(Math.atan(dx/dy));
 			if (dy > 0)
 				angle += 180;
 			else if (dy < 0 && dx < 0)
 				angle += 360;
 			
 			editPlot.setAngle(angle);
 		}
 	}
 	
 	public void handleUp() {
 		mode = IDLE;
 		editPlot.getShape().getPaint().setColor(tempColor);
 		editPlot.getShape().getPaint().setStrokeWidth(getResources().getDimension(R.dimen.strokesize_edit));
 		int medGray = getResources().getColor(R.color.MEDGRAY);
 		arrowPaint.setColor(medGray);
 		resizePaint.setColor(medGray);
 		rotatePaint.setColor(medGray);
 		//rotatePaint.clearShadowLayer();
 	}
 
 	@Override public boolean onDoubleTap(MotionEvent e) {
 		System.out.println("DOUBLE TAP");
 		if (context.createPoly && mode == IDLE) {
 			float[] xy = { e.getX(), e.getY() };
 			Matrix inverse = new Matrix();
 			m.invert(inverse);
 			inverse.mapPoints(xy);
 			polyPts.add(xy[0]);
 			polyPts.add(xy[1]);
 		
 		
 		focPoint = polyPts.size() - 2;
 		mode = DRAG_POINT;
 		}
 		return false;
 	}
 
 	@Override public boolean onDoubleTapEvent(MotionEvent e) { return false; }
 
 	@Override public boolean onSingleTapConfirmed(MotionEvent e) {
 		System.out.println("SINGLE CONFIRMED");
 		return false;
 	}
 
 	@Override public boolean onDown(MotionEvent e) { return false; }
 
 	@Override public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) { return false; }
 
 	@Override public void onLongPress(MotionEvent e) { }
 
 	@Override public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) { return false; }
 
 	@Override public void onShowPress(MotionEvent e) { }
 
 	@Override public boolean onSingleTapUp(MotionEvent e) {
 		System.out.println("SINGLE TAP UP");
 		return false;
 	}
 	
 }
