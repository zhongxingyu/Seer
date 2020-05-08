 package me.mattsutter.conditionred;
 
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import me.mattsutter.conditionred.util.LatLng;
 import me.mattsutter.conditionred.util.ProductManager;
 import me.mattsutter.conditionred.util.RenderCommand;
 import android.content.Context;
 import android.graphics.PixelFormat;
 import android.graphics.Point;
 import android.opengl.GLSurfaceView;
 import android.os.Handler;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 
 public class RadarView extends GLSurfaceView {
 	
 	public static final int METERS_PER_NMI = 1852;
 	//public static final float RADAR_MAP_D = 248/60;
 
 	// Possible radii for radial products in meters.
 	public static final int SHORT_DIAM = 64 * METERS_PER_NMI;
 	public static final int DIAMETER = 248 * METERS_PER_NMI;
 	public static final int LONG_DIAM = 496 * METERS_PER_NMI;
 	public static final int VEL_DIAM = 324 * METERS_PER_NMI;
 	public static final int ECHO_TOP_DIAM = 372 * METERS_PER_NMI;
 	
 	private static final int MAX_FRAMES = 15;
 	
 	private final RadarRenderer renderer;
 	private final ConcurrentLinkedQueue<RenderCommand> queue = new ConcurrentLinkedQueue<RenderCommand>();
 	private final ProductManager prod_man;
 	private final GestureDetector gest_detect;
 	
 	private Runnable progOn, progOff;
 	private Handler handler;
 	private boolean progress = false;
 	private Point center = new Point();
 	private LatLng radar_center;
 	private int prod_code = 0;
 	private float radar_width = 0;
 	private String site_id = "";
 	private String prod_url = "";
 	
 	/**
	 * Constructor for RadarView. Used to inflate the {@link View} programmatically. 
 	 * @param context - The app's {@link Context}.
 	 */
 	public RadarView(Context context) {
 		super(context);
 		renderer = new RadarRenderer(queue, MAX_FRAMES);
 		init();
 		gest_detect = new GestureDetector(context, (GestureDetector.OnGestureListener) context);
 		gest_detect.setOnDoubleTapListener((GestureDetector.OnDoubleTapListener) context);
 		prod_man = new ProductManager(context, MAX_FRAMES, true, handler, queue);
 	}
 	
 	/**
	 * Consructor for RadarView.  Called when inflating via {@link #findViewById(int)}.
 	 * @param context - The app's {@link Context}.
 	 * @param attrs - Layout parameters from the XML file.
 	 */
 	public RadarView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		renderer = new RadarRenderer(queue, MAX_FRAMES);
 		init();
 		gest_detect = new GestureDetector(context, (GestureDetector.OnGestureListener) context);
 		gest_detect.setOnDoubleTapListener((GestureDetector.OnDoubleTapListener) context);
 		prod_man = new ProductManager(context, MAX_FRAMES, true, handler, queue);
 	}
 	
 	/** 
 	 * Sets up the {@link GLSurfaceView} stuff.
 	 */
 	public void init(){
 		setEGLConfigChooser(8, 8, 8, 8, 16, 0);
 		setRenderer(renderer);
 //		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
 		setDebugFlags(GLSurfaceView.DEBUG_CHECK_GL_ERROR);
 		getHolder().setFormat(PixelFormat.TRANSLUCENT);
 		setZOrderOnTop(true);
 	}
 	
 	public void onResume(int prod_code, String site_id, String prod_url){
 		prod_man.onResume();
 		final boolean site_has_changed = checkForSiteChange(site_id);
 		final boolean prod_has_changed = checkForProductChange(prod_code, prod_url);
 		if (site_has_changed || prod_has_changed){
 			Log.d("GLOverlay", "Product or site has changed.");
 			prod_man.productChange(prod_url, site_id);
 			prod_man.startAnimation();
 		}
 		
 		onResume();
 	}
 	
 	public void onDestroy(){
 		prod_man.onDestroy();
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent e){
 		if (gest_detect.onTouchEvent(e))
 			return true;
 		return super.onTouchEvent(e);
 	}
 	
 	private boolean checkForProductChange(int prod_code, String prod_url){
 		final boolean prod_has_changed = this.prod_code != prod_code;
 		final boolean url_has_changed = !prod_url.equals(this.prod_url);
 		if (prod_has_changed){
 			queue.add(new ProductChangeCommand(prod_code));
 			this.prod_code = prod_code;
 		}
 		if (url_has_changed)
 			this.prod_url = prod_url;
 		
 		return url_has_changed || prod_has_changed;
 	}
 	
 	private boolean checkForSiteChange(String site){
 		final boolean has_changed = !site_id.equals(site);
 		if (has_changed){
 			queue.add(new MapChangeCommand(prod_code, radar_center, true));
 			site_id = site;
 		}
 		
 		return has_changed;
 	}
 	
 	protected void changeImageAlpha(short alpha){
 		queue.add(new AlphaChangeCommand(alpha));
 	}
 	
 	protected void mapHasChanged(LatLng new_center){
 		radar_center = new_center;
 		queue.add(new MapChangeCommand(prod_code, radar_center, false));
 	}
 
 	protected void setProgRunners(Handler hand, Runnable progOn, Runnable progOff){
 		this.progOn = progOn;
 		this.progOff = progOff;
 		handler = hand;
 	}
 	
 	public void progressOn(){
 		if (progOn != null)
 			handler.post(progOn);
 	}
 	
 	public void progressOff(){
 		if (progOff != null)
 			handler.post(progOff);
 	}
 	
 	protected boolean toggleProgress(){
 		if (progress){
 			progress = false;
 			progressOff();
 		}
 		else{
 			progress = true;
 			progressOn();
 		}
 		
 		return progress;
 	}
 }
