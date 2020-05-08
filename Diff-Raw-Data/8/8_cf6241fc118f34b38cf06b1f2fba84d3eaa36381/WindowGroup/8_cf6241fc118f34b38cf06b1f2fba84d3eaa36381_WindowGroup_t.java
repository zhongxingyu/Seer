 package homework3.android;
 
 
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.graphics.Path;
 import android.graphics.Path.Direction;
 import android.graphics.Point;
 import android.graphics.RectF;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.TextView;
 
 public abstract class WindowGroup extends Activity implements Group {
 	final int g_drawIntervalMs = 33;
 	
 	public DrawView m_drawView;
 	public TextView m_debugTextView;	
 	private String debugString;
 	private static final boolean useConsole = false; // false -> use a TextView at the bottom of the window
 	private static final String LOG_TAG = "Homework3.WindowGroup";
 
 	private Path m_clipPath = new Path();
 	
 	private Timer m_drawTimer = new Timer();
 	
 	private Object m_screenDirtyLock = new Object();
 	private boolean m_screenDirty = false;
 
 	@Override
 	public void onCreate(final Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 
 		setContentView(R.layout.test);
 
 		// title is set in AndroidManifest.xml
 		// canvas is setup in onDraw in DrawView
 
 		m_debugTextView = (TextView) findViewById(R.id.debugText);	
 		m_debugTextView.setTextColor(Color.WHITE);
 		m_debugTextView.setTextSize(16);		
 
 		// if useConsole is true, print of log message in LogCat 
 		if(useConsole){
 			m_debugTextView.setText("Messages printed in LogCat");					
 		}
 		// else, initialize debugString, which is the output string for the debug text area
 		else{					
 			debugString = "";
 		}
 
 		println("Starting WindowGroup");
 
 		m_drawView = (DrawView) findViewById(R.id.drawView);		
 		m_drawView.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(final View v){
 				unpause();
 			}
 		});
 
 		// draw the window once the drawView has been set up.
 		// this feels very inelegant and I think there's a better way to do this. I feel like the drawView should actually
 		// be handling the drawing, not the activity
 		final GraphicalObject me = this;
 		Thread t = new Thread(new Runnable() {
 			public void run() {
 				while(!m_drawView.getOnDrawFirstCalled()){
 					try {
 						Thread.sleep(200);
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				setup();
 				addClipRect(new BoundaryRectangle(0,0, m_drawView.getWidth(), m_drawView.getHeight()));
 				redraw(me);
 			}
 		});
 		t.start();
 		
 		// TODO fix this since brad doesn't like calls to redraw in damage
 		// drawloop check if damage has been called. if so, redraws
 		m_drawTimer.scheduleAtFixedRate(new TimerTask(){
 			public void run() {
 				synchronized(m_screenDirtyLock)
 				{
 					if(m_screenDirty) redraw(me);
 					m_screenDirty = false;
 				}
 			}
 		
 		}, 0, g_drawIntervalMs);
 	}
 	
 	
 
 	/*
 	 * Initialize any graphical objects here.
 	 */
 	protected abstract void setup();
 
 
 	// note that clipRect is a BoundaryRectangle, and is therefore the size of the area to be drawn
 	// it is NOT 1 pixel bigger (unlike Android's rectangles)
 	public void addClipRect(final BoundaryRectangle r) {
 			if (savedClipRect != null)
 				savedClipRect.add(r);
 			else if (r==null)
 				savedClipRect = null;
 			else
 				savedClipRect = new BoundaryRectangle(r);	
 	}
 
 	BoundaryRectangle savedClipRect;
 	LinkedList<GraphicalObject> children = new LinkedList<GraphicalObject> ();
 
 
 
 	public void redraw(final GraphicalObject child) {	
 		// if savedClipRect is not null, redraw the canvas with all my children
 		// else, print a message
 		if (savedClipRect != null) {
 
 			for (ListIterator<GraphicalObject> iter = children.listIterator (); iter.hasNext (); ) {
 				GraphicalObject gobj = iter.next ();
 				BoundaryRectangle r = gobj.getBoundingBox ();
 				if (r.intersects (savedClipRect))
 					m_drawView.setGraphicalObject(child, savedClipRect);
 			}
 			m_drawView.redraw();
 			savedClipRect = null;
 		}
 		else println("no clip rectangle");
 	}
 
 
 	//
 	// Group interface
 	//
 	@Override
 	public void addChild (GraphicalObject child) {
 		child.setGroup (this);
 		children.add (child); 
		damage (getBoundingBox());
 	}
 	@Override
 	public void removeChild (GraphicalObject child) {
 		children.remove (child);
		damage (getBoundingBox());
 	}
 	@Override
 	public void bringChildToFront (GraphicalObject child) {
 		children.remove (child);
 		children.add (child);
 	}
 	@Override
 	public void resizeToChildren () {
 	}
 
 	@Override
 	public synchronized void damage (BoundaryRectangle rectangle) {
 		synchronized (m_screenDirtyLock) {
 			addClipRect(rectangle);
 			m_screenDirty = true;
 		}
 		
 		
 	}
 
 	@Override
 	public void draw(final Canvas graphics, final Path clipRect) {
 		graphics.save();
 
 		graphics.clipPath(clipRect);
 
 		// set clip path of this group, which is the same dimensions as the drawView...a bit of a hack...not sure
 		// what Brad expects here.
 		m_clipPath.reset();
 		m_clipPath.addRect(new RectF(0, 0, m_drawView.getWidth(), m_drawView.getHeight()), Direction.CCW);
 
 		// draw the rectangle to redraw
 		for (GraphicalObject child : children) {
 			// draw to the clipshape of the child
 			child.draw(graphics, m_clipPath);
 		}
 		graphics.restore();
 	}
 
 	@Override
 	public BoundaryRectangle getBoundingBox() {
 		// return the bounding box of the whole canvas
 		return new BoundaryRectangle(0, 0, m_drawView.getWidth(), m_drawView.getHeight());
 	}
 
 	@Override
 	public void moveTo (int x, int y) {
 	}
 
 	@Override
 	public Group getGroup () {
 		return null;
 	}
 
 	@Override
 	public void setGroup (Group group) {
 	}
 
 	@Override
 	public List<GraphicalObject> getChildren () {
 		return children;
 	}
 
 	@Override
 	public Point parentToChild (Point pt) {
 		return pt;
 	}
 
 	@Override
 	public Point childToParent (Point pt) {
 		return pt;
 	}
 
 	@Override
 	public void resizeChild(GraphicalObject child) {
 	}
 
 
 	// 
 	// Message output
 	//
 	@Override
 	public boolean contains(final int x, final int y){
 		// check if x, y is within the canvas
 		if(x>=0 && x <= m_drawView.getWidth() && y >= 0 && y <= m_drawView.getHeight()){
 			return true;
 		}
 		else{
 			return false;
 		}
 	}
 
 	public void print(final String s){
 		if(useConsole){			
 			Log.i(LOG_TAG, s);
 		}
 		else{
 			debugString += s;			
 			runOnUiThread(new Runnable() {
 				@Override
 				public void run() {
 					m_debugTextView.setText(debugString);				
 				}
 			});								
 		}
 
 	}
 
 	public void println(final String s){
 		if(useConsole){			
 			Log.i(LOG_TAG, s);
 		}
 		else{
 			debugString += s;
 			debugString += "\n";
 			runOnUiThread(new Runnable() {
 				@Override
 				public void run() {
 					m_debugTextView.setText(debugString);				
 				}
 			});		
 		}
 
 	}
 
 	// 
 	// Sleeping
 	//
 
 	public void sleep (int msec) {
 		try {
 			Thread.sleep (msec);
 		} catch (InterruptedException e) {
 		}
 	}
 
 	//
 	// Waiting for mouse clicks
 	//
 
 	public void pause () {
 		println ("click to continue...");
 		synchronized (this) {
 			try {
 				wait ();
 			} catch (InterruptedException e) {
 			}
 		}
 	}
 
 	public void unpause () {
 		synchronized (this) {
 			notify ();
 		}
 	}
 
 
 	//
 	// Random selections
 	//
 
 	private static java.util.Random r = new java.util.Random();
 
 	public int random(final int n) {
 		return r.nextInt(n);
 	}
 
 
 	public int random(final int[] things) {
 		return things[random(things.length)];
 	}
 
 	public Object random(final Object[] things) {
 		return things[random(things.length)];
 	}
 
 	//
 	// Load image
 	// load an image file from the "assets" part of the project
 	//
 	public Bitmap loadImageFully(final String filename) throws IOException {
 		Bitmap myBitmap = BitmapFactory.decodeStream(this.getAssets().open(filename));
 		if (myBitmap != null) return myBitmap;
 		throw new IOException("cannot load file: " + filename);
 	}
 
 
 
 
 	private Matrix ax;
 	@Override
 	public Matrix getAffineTransform() {
 		return ax;
 	}
 
 	@Override
 	public void setAffineTransform(final Matrix af) {
 		ax = af;
 	}
 
 }
