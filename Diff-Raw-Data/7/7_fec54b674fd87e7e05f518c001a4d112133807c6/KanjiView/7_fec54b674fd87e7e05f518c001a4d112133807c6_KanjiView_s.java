 package ch.shibastudio.kanjinotepad.drawing;
 
 import java.util.ArrayList;
 
 import ch.shibastudio.kanjinotepad.R;
 import ch.shibastudio.kanjinotepad.drawing.Point;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.DashPathEffect;
 import android.graphics.Paint;
 import android.graphics.Bitmap.Config;
 import android.graphics.Paint.Cap;
 import android.graphics.Paint.Join;
 import android.graphics.Path;
 import android.graphics.Rect;
 import android.graphics.Paint.Style;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class KanjiView extends View{
 	
 	private class Line{
 		public ArrayList<Point> points;
 		
 		public Line(){
 			points = new ArrayList<Point>();
 		}
 		
 		public void addPoint(Point p){
 			points.add(p);
 		}
 	}
 	
 	private final int GUIDE_BORDER = 10;
 	private final float MIN_DISTANCE = 1f;
 	private final int SMOOTHING = 8;
 	private final float STROKE_WIDTH = 10f;
 	
 	// -- Members --------------------------------------------------------
 	// Flags & states
 	public boolean showGrid = true;
 	public boolean showShadow = true;
 	public boolean showBorder = true;
 	private boolean mDrawing = false;
 	
 	// Paint
 	private Paint mPainter = new Paint();
 	private int mPenColor;
 	private float mPenWidth;
 	private Bitmap mCache;
 	
 	// Dimensions
 	private int mWidthBorder;
 	private int mHeightBorder;
 	private int mGuideWidth;
 	private int mGuideHeight;
 	
 	// Others
 	private Canvas mCanvas = null;
 	private ArrayList<Line> mLines = new ArrayList<Line>();
 	private ArrayList<Path> mPreviousPath = new ArrayList<Path>();
 	private Line mCrntLine;
 	private Path mCrntPath;
 	private Point mPreviousPoint;
 	
 	// -- Methods --------------------------------------------------------
 	public KanjiView(Context c){
 		super(c);
 		init();
 	}
 	
 	public KanjiView(Context c, AttributeSet attrs){
 		super(c, attrs);
 		init();
 	}
 	
 	private void init(){
 		mPenColor = Color.BLACK;
 		mPenWidth = 12f;
 		mPainter.setDither(true);
 		mPainter.setColor(mPenColor);
 		mPainter.setStyle(Paint.Style.STROKE);
 		mPainter.setStrokeJoin(Paint.Join.ROUND);
 		mPainter.setStrokeCap(Paint.Cap.ROUND);
 		mPainter.setStrokeWidth(mPenWidth);
 	}
 	
 	private int guideWidth(){
 		int w = getWidth();
 		int h = getHeight();
 		
 		if(w < h){
 			mWidthBorder = GUIDE_BORDER;
 			mHeightBorder = (h-w)/2;
 			return w - 2*mWidthBorder;
 		}else{
 			mHeightBorder = GUIDE_BORDER;
 			mWidthBorder = (w-h)/2;
 			return h - 2*mHeightBorder;
 		}
 	}
 	
 	private void initPainting(){
 		mGuideWidth = guideWidth();
 		mGuideHeight = mGuideWidth;
 		setupPainter();
 		refreshCache();
 	}
 	
 	private void setupPainter(){
 		mPainter.setColor(Color.BLACK);
 		mPainter.setAntiAlias(true);
 		mPainter.setStrokeCap(Cap.ROUND);
 		mPainter.setStrokeJoin(Join.ROUND);
 		mPainter.setStrokeWidth(STROKE_WIDTH);
 	}
 	
 	private void drawBackground(Canvas canvas){
 		mPainter.setColor(getResources().getColor(R.color.drawingpaper));
 		mPainter.setStrokeWidth(3f);
 		mPainter.setPathEffect(null);
 		mPainter.setStyle(Style.FILL);
 		Rect guide = new Rect(mWidthBorder, mHeightBorder, mWidthBorder + mGuideWidth, mHeightBorder + mGuideHeight);
 		canvas.drawRect(guide, mPainter);
 		mPainter.setStyle(Style.STROKE);
 	}
 	
 	private void drawGrid(Canvas canvas){
 		mPainter.setColor(Color.LTGRAY);
 		mPainter.setStrokeWidth(3f);
 		mPainter.setPathEffect(null);
 		canvas.drawLine(mWidthBorder +mGuideWidth/2, mHeightBorder, mWidthBorder +mGuideWidth/2, mHeightBorder + mGuideHeight, mPainter);
 		canvas.drawLine(mWidthBorder, mHeightBorder + mGuideHeight/2, mWidthBorder + mGuideWidth, mHeightBorder + mGuideHeight/2, mPainter);
 		mPainter.setStrokeWidth(1f);
 		mPainter.setPathEffect(new DashPathEffect(new float[]{3, 3}, 0));
 		canvas.drawLine(mWidthBorder, mHeightBorder + mGuideHeight/4, mWidthBorder + mGuideWidth, mHeightBorder + mGuideHeight/4, mPainter);
 		canvas.drawLine(mWidthBorder, mHeightBorder + 3*mGuideHeight/4, mWidthBorder + mGuideWidth, mHeightBorder + 3*mGuideHeight/4, mPainter);
 		canvas.drawLine(mWidthBorder + mGuideWidth/4, mHeightBorder, mWidthBorder + mGuideWidth/4, mHeightBorder + mGuideHeight, mPainter);
 		canvas.drawLine(mWidthBorder + 3*mGuideWidth/4, mHeightBorder, mWidthBorder + 3*mGuideWidth/4, mHeightBorder + mGuideHeight, mPainter);
 	}
 	
 	private void drawBorder(Canvas canvas){
 		mPainter.setColor(Color.BLACK);
 		mPainter.setStrokeWidth(3f);
 		mPainter.setPathEffect(null);
 		Rect guide = new Rect(mWidthBorder, mHeightBorder, mWidthBorder + mGuideWidth, mHeightBorder + mGuideHeight);
 		canvas.drawRect(guide, mPainter);
 	}
 
 	private boolean isInGuide(float x, float y){
 		int guideHeight = guideWidth();
		if(x >= mWidthBorder && x <= mWidthBorder + guideWidth() && y >= mHeightBorder && y <= mHeightBorder + guideHeight){
 			return true;
 		}
 		return false;
 	}
 	
 	public void clearCanvas(){
 		mLines.clear();
 		mPreviousPath.clear();
 		refreshCache();
 		invalidate();
 	}
 	
 	public void verifyDrawing(){
 		// TODO: Implement me! Use the NDK with Zinnia + Tomoe
 	}
 	
 	private void generateCurrentPath(){
 		if(null != mCrntLine){
 			mCrntPath = new Path();
 			Point c0 = new Point();
 			Point c1 = new Point();
 			if(mCrntLine.points.size() > 1){
 		        for(int i = mCrntLine.points.size() - 2; i < mCrntLine.points.size(); i++){
 		            if(i >= 0){
 		                if(i == 0){
 		                    mCrntLine.points.get(i).dx = ((mCrntLine.points.get(i + 1).x - mCrntLine.points.get(i).x) / SMOOTHING);
 		                    mCrntLine.points.get(i).dy = ((mCrntLine.points.get(i + 1).y - mCrntLine.points.get(i).y) / SMOOTHING);
 		                }
 		                else if(i == mCrntLine.points.size() - 1){
 		                    mCrntLine.points.get(i).dx = ((mCrntLine.points.get(i).x - mCrntLine.points.get(i - 1).x) / SMOOTHING);
 		                    mCrntLine.points.get(i).dy = ((mCrntLine.points.get(i).y - mCrntLine.points.get(i - 1).y) / SMOOTHING);
 		                }
 		                else{
 		                    mCrntLine.points.get(i).dx = ((mCrntLine.points.get(i + 1).x - mCrntLine.points.get(i - 1).x) / SMOOTHING);
 		                    mCrntLine.points.get(i).dy = ((mCrntLine.points.get(i + 1).y - mCrntLine.points.get(i - 1).y) / SMOOTHING);
 		                }
 		            }
 		        }
 		    }
 
 		    boolean first = true;
 		    for(int i = 0; i < mCrntLine.points.size(); i++){
 		        Point point = mCrntLine.points.get(i);
 		        if(first){
 		            first = false;
 		            mCrntPath.moveTo(point.x, point.y);
 		        }
 		        else{
 		        	Point prev = mCrntLine.points.get(i - 1);
 		        	c0.x = prev.x + prev.dx;
 		        	c0.y = prev.y + prev.dy;
 		        	c1.x = point.x - point.dx;
 		        	c1.y = point.y - point.dy;
 		            
 		            mCrntPath.cubicTo(c0.x, c0.y, c1.x, c1.y, point.x, point.y);
 		        }
 		    }
 		}
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		Point p = new Point(event.getX(), event.getY());
 		switch(event.getAction()){
 		case MotionEvent.ACTION_DOWN:
 			if(isInGuide(p.x, p.y)){
 				mCrntLine = new Line();
 				mCrntLine.addPoint(p);
 				generateCurrentPath();
 				mPreviousPoint = p;
 				mDrawing = true;
 			}
 			break;
 				
 		case MotionEvent.ACTION_MOVE:
 			if(mDrawing && isInGuide(p.x, p.y)){
 				if(Math.abs(mPreviousPoint.x - p.x) >= MIN_DISTANCE && Math.abs(mPreviousPoint.y - p.y) >= MIN_DISTANCE){
 					mCrntLine.addPoint(p);		
 					mPreviousPoint = p;
 				}
 				generateCurrentPath();
 			}
 			break;
 			
 		case MotionEvent.ACTION_UP:
 			if(mDrawing && isInGuide(p.x, p.y)){
 				generateCurrentPath();
 				mCrntPath.lineTo(p.x, p.y);
 				mCrntLine.addPoint(p);
 				mPreviousPoint = p;
 				mPreviousPath.add(mCrntPath);
 				refreshCache();
 				mCrntLine = null;
 				mCrntPath = null;
 				mDrawing = false;
 			}
 			break;
 			
 		default:
 			break;	
 		}
 		// Refresh the view
 		invalidate();
 		return true;
 	}
 	
 	private void refreshCache(){
 		mCache = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
 		Canvas cacheCanvas = new Canvas(mCache);
 		
 		drawBackground(cacheCanvas);
 		drawGrid(cacheCanvas);
 		drawBorder(cacheCanvas);
 		setupPainter();
 		
 		for(int i=0; i<mPreviousPath.size(); i++){
 			cacheCanvas.drawPath(mPreviousPath.get(i), mPainter);
 		}
 	}
 	
 	@Override
 	protected void onDraw(Canvas c){
 		
 		setupPainter();
 		
 		if(null == mCanvas){
 			mCanvas = c;
 			initPainting();
 		}
 		
 		if(null != mCache){
 			c.drawBitmap(mCache, 0, 0, mPainter);
 		}
 		
 		if(null != mCrntPath){
 			c.drawPath(mCrntPath, mPainter);
 		}
 	}
 }
