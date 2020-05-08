 package de.georgwiese.functionInspector;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 
 import de.georgwiese.calculationFunktions.CalcFkts;
 import de.georgwiese.calculationFunktions.Function;
 import de.georgwiese.calculationFunktions.Point;
 import de.georgwiese.calculationFunktions.PointMaker;
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.CornerPathEffect;
 import android.graphics.DashPathEffect;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.PathEffect;
 import android.graphics.Picture;
 import android.graphics.Paint.Align;
 import android.graphics.Paint.Style;
 import android.graphics.Path.FillType;
 import android.os.Handler;
 import android.os.Looper;
 import android.os.Message;
 import android.util.Log;
 import android.view.Gravity;
 import android.widget.LinearLayout;
 
 public class FunctionView extends LinearLayout {
 	protected ArrayList<Function> fkts;
 	protected double[] mParameter= new double[3];
 	protected Paint paint;
 	private ArrayList<Path> paths;
 	protected double zoomFactorX, zoomFactorY;
 	protected Boolean bZoom, bZoomDyn, bZoomXY;
 	protected double lastZoomX, lastZoomY;
 	protected double totalZoomX, totalZoomY;
 	protected double middleX,middleY;
 	protected double minX,maxX;
 	private float offsetX,offsetY;
 	private float totalOffsetX,totalOffsetY;
 	private float lastOriginX,lastOriginY;
 	protected Boolean redraw;
 	protected float borderTop, borderBottom;
 
 	public static final int COLORSCHEMA_DARK=1;
 	public static final int COLORSCHEMA_LIGHT=2;
 	protected int colorSchema=COLORSCHEMA_DARK;
 	protected int COLOR_BACKGROUND=Color.WHITE;
 	protected int COLOR_AXES=Color.BLACK;
 	protected int COLOR_LINES=Color.parseColor("#222222");
 	protected int[] COLORS_GRAPHS={Color.RED, Color.GREEN, Color.CYAN};
 	protected int COLOR_ACTIVE_POINT=Color.YELLOW;
 	protected int COLOR_INTERSECTION=Color.GRAY;
 	protected final double MAX_ZOOM=25.0;
 	protected final double MIN_ZOOM=0.059;
 	protected final int QUALITY_LOW=5;
 	protected final int QUALITY_MEDIUM=1;
 	protected final int QUALITY_HIGH=2;
 	protected final int QUALITY_PREVIEW=5;
 	protected int quality;
 	protected DecimalFormat df1,df2;
 	double stepsX, stepsY;
 	
 	//dynamics
 	protected float mLastTouchX, mLastTouchY, mFirstTouchX, mFirstTouchY;
 	protected long mLastTime;
 	protected double velocityX, velocityY, velStoreX, velStoreY;
 	private final double FRICTION_FACTOR=0.85;
 	protected Thread dynamics;
 	protected Thread zoomThread;
 	protected Boolean init, doDyn;
 	protected final double MAX_VELOCITY=100;
 	protected long timeLastZoomStop;
 	
 	//zoom
 	double oldZoomX,oldZoomY;
 	boolean zoomInit=false;
 	boolean zoomIn;
 	protected double zoomToX, zoomToY;
 	protected double lastMiddleX, lastMiddleY;
 	
 	//points
 	protected ArrayList<ArrayList<Point>> roots;
 	protected ArrayList<ArrayList<Point>> extrema;
 	protected ArrayList<ArrayList<Point>> inflections;
 	protected ArrayList<Point> intersections;
 	protected ArrayList<ArrayList<Double>> discontinuities;
 	protected boolean disRoots, disExtrema, disInflections, disIntersections, disDiscon;
 	
 	//modes
 	public static final int MODE_PAN=0;
 	public static final int MODE_TRACE=1;
 	public static final int MODE_SLOPE=2;
 	protected int mode;
 	protected Double currentX;
 	protected Point activePoint;
 	protected FunctionScrollView pointDisplay;
 	
 	//Drawing
 	RedrawThread redrawThread;
 	boolean redrawThreadStarted;
 	protected Object lockDrawing = new Object();
 
 	protected final int FACTOR_ONE=0;
 	protected final int FACTOR_PI=1;
 	protected final int FACTOR_E=2;
 	protected final int FACTOR_DEG=3;
 	protected int factorX = FACTOR_ONE;
 	protected int factorY = FACTOR_ONE;
 	
 	Context mContext;
 	//zoom=1.0 --> 30px=1 length unit
 	
 	public FunctionView(Context context){
 		super(context);
 		mContext=context;
 		fkts= new ArrayList<Function>();
 		paths = new ArrayList<Path>();
 		zoomFactorX=1.0;
 		zoomFactorY=1.0;
 		lastZoomX=1.0;
 		totalZoomX=1.0;
 		lastZoomY=1.0;
 		totalZoomY=1.0;
 		bZoom=false;
 		bZoomDyn=false;
 		bZoomXY=true;
 		middleX=0.0;
 		middleY=0.0;
 		paint = new Paint();
 		paint.setAntiAlias(true);
 		paint.setStrokeWidth(2);
 		paint.setStyle(Style.STROKE);
 		roots= new ArrayList<ArrayList<Point>>();
 		inflections= new ArrayList<ArrayList<Point>>();
 		extrema= new ArrayList<ArrayList<Point>>();
 		intersections= new ArrayList<Point>();
 		discontinuities = new ArrayList<ArrayList<Double>>();
 		offsetX=0;
 		offsetY=0;
 		totalOffsetX=0;
 		totalOffsetY=0;
 		redraw=true;
 		quality=QUALITY_MEDIUM;
 		borderBottom=0;
 		borderTop=0;
 		disExtrema=false; disInflections=false; disIntersections=false; disRoots=false;
 		df1 = new DecimalFormat("0.0##");
 		df2 = new DecimalFormat("0.00");
 		
 		mode = MODE_PAN;
 		currentX=null;
 		
 		setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
 		setOrientation(VERTICAL);
 		setGravity(Gravity.RIGHT);
 
 		Handler h = new Handler(){
 			@Override
 			public void handleMessage(Message msg) {
 				super.handleMessage(msg);
 				//Log.d("Developer", "HANDLE!");
 				invalidate();
 			}
 		};
 		redrawThread = new RedrawThread(h);
 		redrawThreadStarted=false;
 		
 		//Dynamics
 		dynamics = new Thread(){
 			@Override
 			public void run() {
 				if(doDyn){
 					velocityX*=FRICTION_FACTOR;
 					velocityY*=FRICTION_FACTOR;
 					//set Max velocity
 					if (Math.abs(velocityX)>MAX_VELOCITY)
 						velocityX=velocityX>0?MAX_VELOCITY:-MAX_VELOCITY;
 					if (Math.abs(velocityY)>MAX_VELOCITY)
 						velocityY=velocityY>0?MAX_VELOCITY:-MAX_VELOCITY;
 					//set Min velocity
 					if (Math.abs(velocityX)<1.0)
 						velocityX=0;
 					if (Math.abs(velocityY)<1.0)
 						velocityY=0;
 					if (velocityX==0&velocityY==0&(pxToUnitX(0)<minX | pxToUnitX(getWidth())>maxX))
 						redraw=true;
 					
 					middleX-=getDeltaUnit((float)velocityX,zoomFactorX);
 					middleY-=getDeltaUnit((float)velocityY,zoomFactorY);
 					invalidate();
 					postDelayed(this, 50);
 				}
 			}
 		};
 		velocityX=0;
 		velocityY=0;
 		init=false;
 		doDyn=true;
 		timeLastZoomStop=0;
 		
 		zoomThread = new Thread(){
 			double factor=1.1;
 
 			@Override
 			public void run() {
 				super.run();
 				if(bZoomDyn){
 					if(zoomIn){
 						if (zoomFactorX<oldZoomX*2){
 							zoomFactorX*=factor;
 							zoomFactorY*=factor;
 						}
 						else{
 							redraw=true;
 							bZoomDyn=false;
 						}
 					}
 					else{
 						if (zoomFactorX>oldZoomX/2){
 							zoomFactorX/=factor;
 							zoomFactorY/=factor;
 						}
 						else{
 							redraw=true;
 							bZoomDyn=false;
 						}	
 					}
 					
 					invalidate();
 				}
 				postDelayed(this, 50);
 			}
 		};
 
 		stepsX=getSteps(zoomFactorX, factorX);
 		stepsY=getSteps(zoomFactorY, factorY);
 	}
 	/**
 	 * Updates x and y scaling Factors.
 	 */
 	public void updateFactors(){
 		SharedPreferences prefs = mContext.getSharedPreferences("prefs", Activity.MODE_PRIVATE);
 		factorX=prefs.getInt("prefs_factor_x", 0);
 		factorY=prefs.getInt("prefs_factor_y", 0);
 		
 	}
 	public void zoomIn(){
 		if(!zoomInit){
 			zoomThread.run();
 			zoomInit=true;
 		}
 		lastMiddleX=middleX;
 		lastMiddleY=middleY;
 		oldZoomX=zoomFactorX;
 		oldZoomY=zoomFactorY;
 		bZoomDyn=true;
 		zoomIn=true;
 	}
 	public void setBorderTop(float p){
 		borderTop=p;
 	}
 	public void setBorderBottom(float p){
 		borderBottom=p;
 	}
 	public void zoomOut(){
 		if(!zoomInit){
 			zoomThread.run();
 			zoomInit=true;
 		}
 		lastMiddleX=middleX;
 		lastMiddleY=middleY;
 		oldZoomX=zoomFactorX;
 		oldZoomY=zoomFactorY;
 		zoomIn=false;
 		bZoomDyn=true;
 	}
 	public void addFkt(String f){
 		if (CalcFkts.check(f)){
 			fkts.add(new Function(CalcFkts.formatFktString(f)));
 			redraw=true;
 			invalidate();
 		}
 	}
 	protected double pxToUnitX(float px){
 		return (px-getWidth()/2)/30/zoomFactorX+middleX;
 	}
 	protected double pxToUnitX(float px, double zoom, double midd){
 		return (px-getWidth()/2)/30/zoom+midd;
 	}
 	protected float unitToPxX(double unit){
 		return Math.round((unit-middleX)*30*zoomFactorX+getWidth()/2);
 	}
 	protected float unitToPxX(double unit, double zoom, double midd){
 		return Math.round((unit-midd)*30*zoom+getWidth()/2);
 	}
 	protected double pxToUnitY(float px){
 		return middleY-(px-getHeight()/2)/30/zoomFactorY;
 	}
 	protected double pxToUnitY(float px, double zoom, double midd){
 		return midd-(px-getHeight()/2)/30/zoom;
 	}
 	protected float unitToPxY(double unit){
 		return Math.round(getHeight()/2-(unit-middleY)*30*zoomFactorY);
 	}
 	protected float unitToPxY(double unit, double zoom, double midd){
 		return Math.round(getHeight()/2-(unit-midd)*30*zoom);
 	}
 	public double getDeltaUnit(float deltaPixel, double z){
 		return deltaPixel/30/z;
 	}
 	private double getSteps(double z, int factor){
 		int exponent=0;
 		double steps = getDeltaUnit(25,z);
 		if (factor==FACTOR_PI)
 			steps/=Math.PI;
 		else if (factor==FACTOR_E)
 			steps/=Math.E;
 		else if (factor==FACTOR_DEG)
 			steps=steps/Math.PI*180;
 		while (steps<1 | steps>=10){
 			if (steps<1){
 				steps *= 10;
 				--exponent;
 			}
 			if (steps >=10){
 				steps /= 10;
 				++exponent;
 			}
 		}
 		if (steps>5)
 			steps=10;
 		else if (steps>2)
 			steps=5;
 		else if (steps>1)
 			steps=2;
 		else
 			steps=1;
 		double result = steps*Math.pow(10, exponent);
 		if (factor==FACTOR_PI)
 			return result*Math.PI;
 		else if (factor==FACTOR_E)
 			return result*Math.E;
 		else if (factor==FACTOR_DEG)
 			return result*Math.PI/180;
 		else
 			return result;
 	}/*
 	private void setColors (int bg, int axes, int lines, int g1, int g2, int g3, int ap){
 		COLOR_BACKGROUND=bg;
 		COLOR_AXES=axes;
 		COLOR_LINES=lines;
 		COLORS_GRAPHS[0]=g1;
 		COLORS_GRAPHS[1]=g2;
 		COLORS_GRAPHS[2]=g3;
 		COLOR_ACTIVE_POINT=ap;
 	}*/
 	public void setColorSchema(int schema){
 		switch (schema){
 		case COLORSCHEMA_LIGHT:
 			COLOR_BACKGROUND=Color.WHITE;
 			COLOR_AXES=Color.BLACK;
 			COLOR_LINES=Color.parseColor("#DDDDDD");
 			COLOR_ACTIVE_POINT=Color.parseColor("#9F6105");
 			COLORS_GRAPHS = new int[7];
 			COLORS_GRAPHS[0] = Color.RED;
 			COLORS_GRAPHS[1] = Color.parseColor("#00780A");
 			COLORS_GRAPHS[2] = Color.BLUE;
 			COLORS_GRAPHS[3] = Color.parseColor("#FFC400");
 			COLORS_GRAPHS[4] = Color.parseColor("#FF00DD");
 			COLORS_GRAPHS[5] = Color.CYAN;
 			COLORS_GRAPHS[6] = Color.YELLOW;
 			//setColors(Color.WHITE, Color.BLACK, Color.parseColor("#DDDDDD"), Color.RED, Color.parseColor("#00780A"), Color.BLUE, Color.parseColor("#9F6105"));
 			break;
 		case COLORSCHEMA_DARK:
 			COLOR_BACKGROUND=Color.BLACK;
 			COLOR_AXES=Color.WHITE;
 			COLOR_LINES=Color.parseColor("#222222");
 			COLOR_ACTIVE_POINT=Color.YELLOW;
 			COLORS_GRAPHS = new int[7];
 			COLORS_GRAPHS[0] = Color.RED;
 			COLORS_GRAPHS[1] = Color.GREEN;
 			COLORS_GRAPHS[2] = Color.CYAN;
 			COLORS_GRAPHS[3] = Color.parseColor("#FFC400");
 			COLORS_GRAPHS[4] = Color.parseColor("#FF00DD");
 			COLORS_GRAPHS[5] = Color.BLUE;
 			COLORS_GRAPHS[6] = Color.YELLOW;
 			//COLORS_GRAPHS[7] = Color.CYAN;
 			//COLORS_GRAPHS[8] = Color.CYAN;
 			//COLORS_GRAPHS[9] = Color.CYAN;
 			//setColors(Color.BLACK, Color.WHITE, Color.parseColor("#222222"), Color.RED, Color.GREEN, Color.CYAN, Color.YELLOW);
 			break;
 		}
 		colorSchema=schema;
 	}
 	@Override
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 		
 		if (!redrawThreadStarted){
 			redrawThreadStarted=true;
 			redrawThread.start();
 		}
 		if(redraw){
 			stepsX=getSteps(zoomFactorX, factorX);
 			stepsY=getSteps(zoomFactorY, factorY);
 		}
 		if(bZoom | bZoomDyn){
 			middleX=zoomToX-((zoomToX-lastMiddleX)*oldZoomX/totalZoomX);
 			middleY=zoomToY-((zoomToY-lastMiddleY)*oldZoomY/totalZoomY);
 		}
 		
 		canvas.drawColor(COLOR_BACKGROUND);
 		paint.setTextAlign(Align.CENTER);
 		paint.setTextSize(15);
 		paint.setStyle(Style.FILL_AND_STROKE);
 		double rightBorder=new Double(pxToUnitX(getWidth())/stepsX).intValue()+1;
 		for (double i=new Double(pxToUnitX(0)/stepsX).intValue()-1; i<=rightBorder;i++){
 			paint.setColor(COLOR_LINES);
 			canvas.drawLine(unitToPxX(i*stepsX), 0, unitToPxX(i*stepsX), getHeight(), paint);
 			paint.setColor(COLOR_AXES);
 			canvas.drawLine(unitToPxX(i*stepsX), unitToPxY(0), unitToPxX(i*stepsX), unitToPxY(0)+5, paint);
 			paint.setStrokeWidth(1);
 			if (i!=0 & i%2==0){
 				String text;
 				if (factorX==FACTOR_PI)
 					text=df1.format(i*stepsX/Math.PI)+"\u03C0";
 				else if (factorX==FACTOR_DEG)
 					text=df1.format(i*stepsX/Math.PI*180)+"\u00B0";
 				else if (factorX==FACTOR_E)
 					text=df1.format(i*stepsX/Math.E)+"e";
 				else
 					text=df1.format(i*stepsX);
 				if (unitToPxY(0)<=getHeight()-30-borderBottom & unitToPxY(0)>=borderTop)
 					canvas.drawText(text,unitToPxX(i*stepsX), unitToPxY(0)+20, paint);
 				else if (unitToPxY(0)>getHeight()-30-borderBottom)
 					canvas.drawText(text,unitToPxX(i*stepsX), getHeight()-10-borderBottom, paint);
 				else if (unitToPxY(0)<borderTop)
 					canvas.drawText(text,unitToPxX(i*stepsX), 20+borderTop, paint);
 			}
 			paint.setStrokeWidth(2);
 		}
 		paint.setTextAlign(Align.RIGHT);
 		double bottomBorder=new Double(pxToUnitY(getHeight())/stepsY).intValue()-1;
 		for (int i=new Double(pxToUnitY(0)/stepsY).intValue()+1; i>=bottomBorder; i--){
 			paint.setColor(COLOR_LINES);
 			canvas.drawLine(0, unitToPxY(i*stepsY), getWidth(), unitToPxY(i*stepsY), paint);
 			paint.setColor(COLOR_AXES);
 			canvas.drawLine(unitToPxX(0), unitToPxY(i*stepsY), unitToPxX(0)-3, unitToPxY(i*stepsY), paint);
 			paint.setStrokeWidth(1);
 			if (i!=0 & i%2==0){
 				String text;
 				if (factorY==FACTOR_PI)
 					text=df1.format(i*stepsY/Math.PI)+"\u03C0";
 				else if (factorY==FACTOR_DEG)
 					text=df1.format(i*stepsY/Math.PI*180)+"\u00B0";
 				else if (factorY==FACTOR_E)
 					text=df1.format(i*stepsY/Math.E)+"e";
 				else
 					text=df1.format(i*stepsY);
 				if (unitToPxX(0)<=getWidth() & unitToPxX(0)>=45)
 					canvas.drawText(text, unitToPxX(0)-10, unitToPxY(i*stepsY)+5, paint);
 				if (unitToPxX(0)>getWidth())
 					canvas.drawText(text, getWidth()-10, unitToPxY(i*stepsY)+5, paint);
 				if (unitToPxX(0)<45){
 					paint.setTextAlign(Align.LEFT);
 					canvas.drawText(text, 5, unitToPxY(i*stepsY)+5, paint);
 				}
 			}
 			paint.setStrokeWidth(2);
 		}
 		canvas.drawLine(unitToPxX(0), 0, unitToPxX(0), getHeight(), paint);
 		canvas.drawLine(0, unitToPxY(0), getWidth(), unitToPxY(0), paint);
 		paint.setStyle(Style.STROKE);
 		
 		synchronized (lockDrawing){
 			offsetX=unitToPxX(0)-lastOriginX-totalOffsetX;
 			offsetY=unitToPxY(0)-lastOriginY-totalOffsetY;
 			totalOffsetX+=offsetX;
 			totalOffsetY+=offsetY;
 			for (Path p:paths)
 				p.offset(offsetX, offsetY);
 			
 			//if (bZoom|bZoomDyn){	
 				Matrix matrix = new Matrix();
 				lastZoomX=zoomFactorX/totalZoomX;
 				totalZoomX*=lastZoomX;
 				lastZoomY=zoomFactorY/totalZoomY;
 				totalZoomY*=lastZoomY;
 				matrix.setScale((float)(lastZoomX),(float)(lastZoomY),unitToPxX(0),unitToPxY(0));
 				for (Path p:paths)
 					p.transform(matrix);
 				stepsX=getSteps(zoomFactorX, factorX);
 				stepsY=getSteps(zoomFactorY, factorY);
 			//}}
 		for (int i=0; i<paths.size();i++){
 			paint.setColor(COLORS_GRAPHS[i%COLORS_GRAPHS.length]);
 			if (disRoots | disExtrema | disInflections |disDiscon){
 				paint.setStyle(Style.FILL_AND_STROKE);
 				if (disRoots)
 					if (i<roots.size())
 						for (Point p:roots.get(i))
 							canvas.drawCircle(unitToPxX(p.getX()), unitToPxY(p.getY()), 5, paint);
 				if (disExtrema)
 					if (i<extrema.size())
 						for (Point p:extrema.get(i))
 							canvas.drawCircle(unitToPxX(p.getX()), unitToPxY(p.getY()), 5, paint);
 				if (disInflections)
 					if (i<inflections.size())
 						for (Point p:inflections.get(i))
 							canvas.drawCircle(unitToPxX(p.getX()), unitToPxY(p.getY()), 5, paint);
 				paint.setStyle(Style.STROKE);
 				if (disDiscon){
 					if (i<discontinuities.size()){
 						for (Double d:discontinuities.get(i)){
 							if (unitToPxX(d)>-5 && unitToPxX(d)<getWidth()+5){
 								Path p = new Path();
 								p.moveTo(unitToPxX(d), -30 + unitToPxY(0)%30);
 								p.lineTo(unitToPxX(d), getHeight());
 								float[] intervals = {20,10};
 								paint.setStrokeWidth(4);
 								paint.setPathEffect(new DashPathEffect(intervals, 0));
 								canvas.drawPath(p, paint);
 								paint.setStrokeWidth(2);
 								paint.setPathEffect(null);	
 							}
 						}}}
 				paint.setStyle(Style.STROKE);
 			}
 			canvas.drawPath(paths.get(i), paint);
 		}
 		if (disIntersections){
 			paint.setColor(COLOR_INTERSECTION);
 			paint.setStyle(Style.FILL_AND_STROKE);
 			for (Point p:intersections)
 				canvas.drawCircle(unitToPxX(p.getX()), unitToPxY(p.getY()), 5, paint);
 			paint.setStyle(Style.STROKE);
 		}
 		}
 		paint.setColor(COLOR_AXES);
 		paint.setStrokeWidth(2);
 	}
 	class RedrawThread extends Thread{
 		Handler mHandler;
 		public RedrawThread(Handler h){
 			super();
 			mHandler=h;
 		}
 		public void setHandler(Handler h){
 			mHandler=h;
 		}
 		@Override
 		public void run() {
 			while (true){
 				if(redraw && !(bZoom | bZoomDyn)){//|bZoom|bZoomDyn){
 					
 					//synchronized (lockDrawing) {
 					redraw=false;
 					float _lastOriginX=unitToPxX(0);
 					float _lastOriginY=unitToPxY(0);
 					double _totalZoomX=zoomFactorX;
 					double _totalZoomY=zoomFactorY;
 					
 					double _zoomFactorX=zoomFactorX;
 					double _zoomFactorY=zoomFactorY;
 					double _middleX = middleX;
 					double _middleY = middleY;
 					ArrayList<Function> _fkts=new ArrayList<Function>(fkts);
 					minX=pxToUnitX(-50,_totalZoomX,_middleX);
 					maxX=pxToUnitX(getWidth()+50,_zoomFactorX,_middleX);
 					
 					ArrayList<Path> helperPaths = new ArrayList<Path>();
 					ArrayList<ArrayList<Point>> hRoots=new ArrayList<ArrayList<Point>>();
 					ArrayList<ArrayList<Point>> hExtrema=new ArrayList<ArrayList<Point>>();
 					ArrayList<ArrayList<Point>> hInflections=new ArrayList<ArrayList<Point>>();
 					ArrayList<ArrayList<Double>> hDiscon = new ArrayList<ArrayList<Double>>();
 					boolean disRoots2=disRoots;
 					boolean disInflections2=disInflections;
 					boolean disExtrema2=disExtrema;
 					
 					
 					for (int j=0; j<_fkts.size(); j++){
 						Function f = _fkts.get(j);
 						Path p = new Path();
 							boolean first = true;
 						if (f==null){
 							hRoots.add(new ArrayList<Point>());
 							hExtrema.add(new ArrayList<Point>());
 							hInflections.add(new ArrayList<Point>());
 							hDiscon.add(new ArrayList<Double>());
 						}
 						else{
 							f.setA(mParameter[0]);
 							f.setB(mParameter[1]);
 							f.setC(mParameter[2]);
 							hDiscon.add(PointMaker.getDiscontinuities(f, quality==QUALITY_PREVIEW?pxToUnitX(0,_zoomFactorX,_middleX):pxToUnitX(-getWidth(),_zoomFactorX,_middleX), quality==QUALITY_PREVIEW?pxToUnitX(getWidth(),_zoomFactorX,_middleX):pxToUnitX(2*getWidth(),_zoomFactorX,_middleX), getDeltaUnit(15,_zoomFactorX)));
 							if (disExtrema2 | disRoots2)
 								hExtrema.add(PointMaker.getExtrema(f, hDiscon.get(j), quality==QUALITY_PREVIEW?pxToUnitX(0,_zoomFactorX,_middleX):pxToUnitX(-getWidth(),_zoomFactorX,_middleX), quality==QUALITY_PREVIEW?pxToUnitX(getWidth(),_zoomFactorX,_middleX):pxToUnitX(2*getWidth(),_zoomFactorX,_middleX), getDeltaUnit(15,_zoomFactorX)));
 							if (disRoots2)
 								hRoots.add(PointMaker.getRoots(f, hExtrema.get(j), hDiscon.get(j), quality==QUALITY_PREVIEW?pxToUnitX(0,_zoomFactorX,_middleX):pxToUnitX(-getWidth(),_zoomFactorX,_middleX), quality==QUALITY_PREVIEW?pxToUnitX(getWidth(),_zoomFactorX,_middleX):pxToUnitX(2*getWidth(),_zoomFactorX,_middleX), getDeltaUnit(15,_zoomFactorX)));
 							if (disInflections2)
 								hInflections.add(PointMaker.getInflectionPoints(f, hDiscon.get(j), quality==QUALITY_PREVIEW?pxToUnitX(0,_zoomFactorX,_middleX):pxToUnitX(-getWidth(),_zoomFactorX,_middleX), quality==QUALITY_PREVIEW?pxToUnitX(getWidth(),_zoomFactorX,_middleX):pxToUnitX(2*getWidth(),_zoomFactorX,_middleX), getDeltaUnit(15,_zoomFactorX)));
 							
 							int inIndex=0;
 							ArrayList<Double> discons = hDiscon.get(_fkts.indexOf(f));
 							for (float x=-getWidth(); x<2*getWidth(); x+=quality){
 								if (quality==QUALITY_PREVIEW && x<-2*QUALITY_PREVIEW)
 									x=-2*QUALITY_PREVIEW;
 								double y = f.calculate(pxToUnitX(x,_zoomFactorX,_middleX));
 								if (inIndex<discons.size() && pxToUnitX(x,_zoomFactorX,_middleX)>=discons.get(inIndex)){
 									first=true;
 									inIndex++;
 								}
 								if (!Double.isNaN(y)){
 									if (!(quality==QUALITY_PREVIEW & x>getWidth())){
 										if ((x>=-50 & x<=getWidth()+50) | x % 5==0){
 											if (first)
												p.moveTo(x, unitToPxY(y,_zoomFactorY,_middleY));
 											first=false;
											p.lineTo(x, unitToPxY(y,_zoomFactorY,_middleY));
 										}
 									}
 								}
 								else
 									first=true;
 							}
 						}
 						helperPaths.add(p);
 					}
 					ArrayList<Point> hIntersections = new ArrayList<Point>();
 					if (disIntersections)
 						for (int i = 0; i<_fkts.size()-1; i++)
 							for (int j=i+1; j<_fkts.size(); j++)
 								if (_fkts.get(i)!=null && _fkts.get(j)!=null && !_fkts.get(i).equals(_fkts.get(j)))
 									hIntersections.addAll(PointMaker.getIntersections(_fkts.get(i), _fkts.get(j), quality==QUALITY_PREVIEW?pxToUnitX(0,_zoomFactorX,_middleX):pxToUnitX(-getWidth(),_zoomFactorX,_middleX), quality==QUALITY_PREVIEW?pxToUnitX(getWidth(),_zoomFactorX,_middleX):pxToUnitX(2*getWidth(),_zoomFactorX,_middleX), getDeltaUnit(15,_zoomFactorX)));
 					
 					synchronized (lockDrawing){
 						offsetX=0;
 						offsetY=0;
 						totalOffsetX=0;
 						totalOffsetY=0;
 						lastOriginX=_lastOriginX;
 						lastOriginY=_lastOriginY;
 						lastZoomX=1.0;
 						lastZoomY=1.0;
 						totalZoomX=_totalZoomX;
 						totalZoomY=_totalZoomY;
 						
 						paths = new ArrayList<Path>(helperPaths);
 						roots = new ArrayList<ArrayList<Point>>(hRoots);
 						extrema = new ArrayList<ArrayList<Point>>(hExtrema);
 						inflections = new ArrayList<ArrayList<Point>>(hInflections);
 						intersections = new ArrayList<Point>(hIntersections);
 						discontinuities = new ArrayList<ArrayList<Double>>(hDiscon);
 					}
 					//Looper.prepare();
 					if (mHandler!=null)
 						mHandler.sendEmptyMessage(0);
 					//Looper.loop();
 				}
 				if (quality!=QUALITY_PREVIEW)
 					try{sleep(500);}catch(Exception e){}
 			}
 		};
 		
 	}
 }
