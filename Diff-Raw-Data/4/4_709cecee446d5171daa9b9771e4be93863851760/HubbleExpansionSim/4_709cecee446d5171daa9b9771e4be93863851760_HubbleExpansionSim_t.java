 package edu.utk.phys.astro;
 
 import java.util.Random;
 
 import org.apache.commons.math.ode.DerivativeException;
 import org.apache.commons.math.ode.IntegratorException;
 
 import edu.utk.phys.astro.hubble.HarmonicODE;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.drawable.ShapeDrawable;
 import android.graphics.drawable.shapes.OvalShape;
 import android.os.Handler;
 import android.os.Message;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class HubbleExpansionSim extends View {
 
 	static final int GALAXY_COLOR = Color.argb(255, 66, 66, 66);
 	static final int UNIVERSE_COLOR = Color.argb(255, 0, 0, 0);
 	static final float REAL_GALAXY_RADIUS = 0.02f; // Inches
 
 	// Class constants defining state of the thread
 	static final int DONE = 0;
 	static final int RUNNING = 1;
 	static final long delay = 20; // Milliseconds of delay in the update loop
 	int numberOfGalaxies;
 	int galaxyRadius = 5;
 	float expansionConstant = 1.1f;
 	double evolutionTime = 14e9;
 	float centerX = 0, centerY = 0, width = 0, height = 0;
 	double realWidth, realHeight; // Inches
 	float X0 = 0, Y0 = 0;
 	private float X[]; // Current X position of galaxy (pixels )
 	private float Y[]; // Current Y position of galaxy (pixels)
 	private float expandedX[];
 	private float expandedY[];
 	private ShapeDrawable galaxy; // Galaxy symbol
 	private Paint paint;
 	private Random rnd = new Random();
 	private AnimationThread animThread; // The animation thread
 	private HarmonicODE ode;
 	private HarmonicODE odes[];
 	double xdpi;
 	double ydpi;
 	float touchX;
 	float touchY;
 	boolean isDragging;
 	boolean isExpanded = false;
 	boolean drawLines = true;
 
 	final Handler handler = new Handler() {
 		public void handleMessage(Message msg) {
 			// When we receive a message from the animation thread indicating
 			// one
 			// time through the animation loop, invalidate the main UI view to
 			// force a redraw.
 			invalidate();
 		}
 	};
 
 	/*
 	 * Inner class that performs animation calculations on a second thread.
 	 * Implement the thread by subclassing Thread and overriding its run()
 	 * method. Also provide a setState(state) method to stop the thread
 	 * gracefully.
 	 */
 
 	private class AnimationThread extends Thread {
 
 		Handler mHandler;
 		int mState;
 		int steps = 100;
 		int currentStep = 0;
 		boolean done = false;
 
 		// Constructor with an argument that specifies Handler on main thread
 		// to which messages will be sent by this thread.
 
 		AnimationThread(Handler h) {
 			mHandler = h;
 
 			expandedX = new float[X.length];
 			expandedY = new float[Y.length];
 		}
 
 		/*
 		 * Override the run() method that will be invoked automatically when the
 		 * Thread starts. Do the work required to update the animation on this
 		 * thread but send a message to the Handler on the main UI thread to
 		 * actually change the visual representation by calling invalidate() on
 		 * the View.
 		 */
 
 		@Override
 		public void run() {
 			mState = RUNNING;
 			isExpanded = true;
 			// for(int galaxyId = 0; galaxyId < numberOfGalaxies; galaxyId++) {
 			// HarmonicODE thisOde = odes[galaxyId];
 			// try {
 			// thisOde.compute(new double[]{1.0,Y[galaxyId]}, this.t0, this.tf);
 			// } catch (DerivativeException e) {
 			// e.printStackTrace();
 			// } catch (IntegratorException e) {
 			// e.printStackTrace();
 			// }
 			// }
 
 			for (int step = 0; step < steps & mState == RUNNING; step++) {
 				currentStep = step;
 				float percentage = (float) currentStep / (float) steps;
 				newXY(percentage);
 				// The method Thread.sleep throws an InterruptedException if
 				// Thread.interrupt()
 				// were to be issued while thread is sleeping; the exception
 				// must be caught.
 				try {
 					// Control speed of update (but precision of delay not
 					// guaranteed)
 					Thread.sleep(delay);
 				} catch (InterruptedException e) {
 					Log.e("ERROR", "Thread was Interrupted");
 				}
 
 				/*
 				 * Send message to Handler on UI thread so that it can update
 				 * the screen display. We could also send a data bundle with the
 				 * message (see the ProgressBarExample) but there is no need in
 				 * this case since the coordinates to be plotted (X and Y) are
 				 * defined in the enclosing class and thus can be changed
 				 * directly from this inner class.
 				 */
 
 				Message msg = mHandler.obtainMessage();
 				mHandler.sendMessage(msg);
 			}
 			
 			done = true;
 		}
 
 		/*
 		 * Method to set the new X/Y coordinate based upon the galaxy's
 		 * trajectory to its final (expanded) destination.
 		 */
 
 		
 
 		// Set current state of thread (use state=AnimationThread.DONE to stop
 		// thread)
 		public void setState(int state) {
 			mState = state;
 		}
 	}
 
 	public HubbleExpansionSim(Context context) {
 		super(context);
 		init();
 
 	}
 	
 	public HubbleExpansionSim(Context context, AttributeSet attrs) {
 		super(context,attrs);
 		init();
 	}
 	
 	public void init() {
 		// Define the galaxy as circular shape
 		galaxy = new ShapeDrawable(new OvalShape());
 		galaxy.getPaint().setColor(Color.WHITE);
 		galaxy.setBounds(0, 0, 2 * galaxyRadius, 2 * galaxyRadius);
 
 		// Set up the Paint object that will control format of screen draws
 		paint = new Paint();
 		paint.setAntiAlias(true);
 		paint.setTextSize(14);
 		paint.setStrokeWidth(1);
 	}
 	
 	private void newXY(float percentage) {
 		if(!isExpanded) {
 			return;
 		}
 		for (int galaxyId = 0; galaxyId < numberOfGalaxies; galaxyId++) {
 			float dx = X[galaxyId] - X0;
 			float dy = Y[galaxyId] - Y0;
 			float newx = dx*expansionConstant + X0;
 			float newy = dy*expansionConstant + Y0;
 			float xend = X[galaxyId] + (newx - X[galaxyId])*percentage;
 			float yend = Y[galaxyId] + (newy - Y[galaxyId])*percentage;
 			expandedX[galaxyId] = xend;
 			expandedY[galaxyId] = yend;
 		}
 
 	}
 
 	// Stop the thread loop
 	public void stopLooper() {
		if(animThread!=null) {
			animThread.setState(DONE);
		}
 	}
 
 	// Start the thread loop
 	public void expand() {
 		animThread = new AnimationThread(handler);
 		animThread.start();
 	}
 
 	public void setNumberOfGalaxies(int nGalaxies) {
 		isExpanded = false;
 		numberOfGalaxies = nGalaxies;
 		X = new float[numberOfGalaxies];
 		Y = new float[numberOfGalaxies];
 		for (int i = 0; i < numberOfGalaxies; i++) {
 			X[i] = (rnd.nextFloat()) * width;
 			Y[i] = (rnd.nextFloat()) * height;
 		}
 	}
 	
 	public void newUniverse() {
 		setNumberOfGalaxies(numberOfGalaxies);
 		invalidate();
 	}
 
 	/*
 	 * The View display size is only available after a certain stage of the
 	 * layout. Before then the width and height are by default set to zero. The
 	 * onSizeChanged method of View is called when the size is changed and its
 	 * arguments give the new and old dimensions. Thus this can be used to get
 	 * the sizes of the View after it has been laid out (or if the layout
 	 * changes, as in a switch from portrait to landscape mode, for example).
 	 */
 
 	@Override
 	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
 		// Coordinates for center of screen
 		centerX = w / 2;
 		centerY = h / 2;
 		X0 = centerX;
 		Y0 = centerY;
 
 		width = w;
 		height = h;
 
 		// Calculate the size of the galaxies according to the physical area of
 		// the screen
 		double density = getContext().getResources().getDisplayMetrics().density;
 		xdpi = getContext().getResources().getDisplayMetrics().xdpi;
 		ydpi = getContext().getResources().getDisplayMetrics().ydpi;
 		// size in inches is xdpi*w and ydpi*h
 		realWidth = (float) xdpi * w;
 		realHeight = (float) ydpi * h;
 		double averageDpi = (xdpi + ydpi) / 2.0;
 
 		// We want the galaxy size to be this size
 		int nPix = (int) Math.round((float) REAL_GALAXY_RADIUS * averageDpi);
 		galaxyRadius = nPix;
 		// galaxyRadius = 3;
 		galaxy.setBounds(0, 0, 2 * galaxyRadius, 2 * galaxyRadius);
 
 		Log.i("INFO", "Height is " + h + " width is " + w);
 		Log.i("INFO", "Density is " + density);
 		Log.i("INFO", "Npix is " + nPix);
 
 		setNumberOfGalaxies(50);
 	}
 
 	/*
 	 * This method will be called each time the screen is redrawn. The draw is
 	 * on the Canvas object, with formatting controlled by the Paint object.
 	 * When to redraw is under Android control, but we can request a redraw
 	 * using the method invalidate() inherited from the View superclass.
 	 */
 
 	@Override
 	public void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 		drawBackground(paint, canvas);
 		drawCrossOnCenter(paint, canvas);
 
 		for (int i = 0; i < numberOfGalaxies; i++) {
 
 			// Draw the expanded galaxies, if available
 			if (isExpanded) {
 				canvas.save();
 				canvas.translate((float) expandedX[i], (float) expandedY[i]);
 				galaxy.getPaint().setColor(Color.RED);
 				galaxy.draw(canvas);
 				canvas.restore();
 			}
 
 			// Draw the original positions in white
 			canvas.save();
 			galaxy.getPaint().setColor(Color.WHITE);
 			canvas.translate((float) X[i], (float) Y[i]);
 			galaxy.draw(canvas);
 			canvas.restore();
 			
 			
 
 		}
 		
 		if(isExpanded) {
 			
 			if(animThread.done & drawLines){
 				canvas.save();
 				drawLines(paint,canvas);
 				canvas.restore();
 			}
 		}
 	}
 
 	// Called by onDraw to draw the background
 	private void drawBackground(Paint paint, Canvas canvas) {
 		paint.setColor(Color.YELLOW);
 		paint.setStyle(Paint.Style.FILL);
 		// canvas.drawCircle(centerX + X0, centerY + Y0, sunRadius, paint);
 		paint.setStyle(Paint.Style.STROKE);
 	}
 
 	public void setCenter(float X0, float Y0) {
 		this.X0 = X0;
 		this.Y0 = Y0;
 	}
 
 	public void translateCenter(double dx, double dy) {
 		this.X0 += dx;
 		this.Y0 += dy;
 	}
 
 	public void setExpansionFactor(float f) {
 		this.expansionConstant = f;
 	}
 
 	public void drawCrossOnCenter(Paint paint, Canvas canvas) {
 		paint.setColor(Color.WHITE);
 		paint.setStyle(Paint.Style.STROKE);
 		float x0 = (float) (X0 - 10.0f);
 		float y0 = (float) (Y0 - 10.0f);
 		float x1 = (float) (X0 + 10.0f);
 		float y1 = (float) (Y0 + 10.0f);
 		canvas.drawLine(x0, (y1 + y0) / 2.0f, x1, (y1 + y0) / 2.0f, paint);
 		canvas.drawLine((x1 + x0) / 2.0f, y0, (x1 + x0) / 2.0f, y1, paint);
 	}
 	
 	public void drawLines(Paint paint, Canvas canvas) {
 		paint.setColor(Color.GRAY);
 		for(int i = 0; i< numberOfGalaxies; i++) {
 			canvas.drawLine(X[i] + galaxyRadius, Y[i]+galaxyRadius, expandedX[i]+galaxyRadius, expandedY[i]+galaxyRadius, paint);
 		}
 	}
 
 	/*
 	 * Process MotionEvents corresponding to screen touches and drags.
 	 * MotionEvent reports movement (mouse, pen, finger, trackball) events. The
 	 * MotionEvent method getAction() returns the kind of action being performed
 	 * as an integer constant of the MotionEvent class, with possible values
 	 * ACTION_DOWN, ACTION_MOVE, ACTION_UP, and ACTION_CANCEL. Thus we can
 	 * switch on the returned integer to determine the kind of event and the
 	 * appropriate action.
 	 */
 
 	@Override
 	public boolean onKeyLongPress(int keyCode, KeyEvent ev) {
 
 		setNumberOfGalaxies(50);
 		return true;
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent ev) {
 
 		Log.i("Touch", "Touched!");
 		final int action = ev.getAction();
 
 		switch (action) {
 
 		// MotionEvent class constant signifying a finger-down event
 
 		case MotionEvent.ACTION_DOWN: {
 
 			isDragging = false;
 
 			// Get coordinates of touch event
 			final float x = ev.getX();
 			final float y = ev.getY();
 
 			touchX = x;
 			touchY = y;
 
 			break;
 		}
 
 			// MotionEvent class constant signifying a finger-drag event
 
 		case MotionEvent.ACTION_MOVE: {
 
 			// Only process if touch selected a symbol
 			isDragging = true;
 			final float x = ev.getX();
 			final float y = ev.getY();
 
 			// Calculate the distance moved
 			final float dx = x - touchX;
 			final float dy = y - touchY;
 
 			// Move the object selected
 			translateCenter(dx, dy);
 
 			// Remember this touch position for the next move event of this
 			// object
 			touchX = x;
 			touchY = y;
 			
 			// Calculate new X/Y positions
 			newXY(1.0f);
 
 			// Request a redraw
 			invalidate();
 
 		}
 			break;
 		// MotionEvent class constant signifying a finger-up event
 
 		case MotionEvent.ACTION_UP: {
 			if (!isDragging) {
 				Log.i("Result", "Clicked!");
 
 				//setNumberOfGalaxies(50);
 				expand();
 			}
 			isDragging = false;
 			invalidate(); // Request redraw
 		}
 			break;
 
 		}
 
 		return true;
 
 	}
 	
 	float getExpansionConstant() {
 		return this.expansionConstant;
 	}
 
 }
