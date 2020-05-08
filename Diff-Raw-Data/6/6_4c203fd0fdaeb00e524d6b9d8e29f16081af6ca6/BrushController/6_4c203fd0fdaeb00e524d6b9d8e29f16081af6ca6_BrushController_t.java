 package net.coobird.paint.brush;
 
 import java.awt.AlphaComposite;
 import java.awt.Composite;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import net.coobird.paint.image.ImageLayer;
 
/*
 * FIXME Threading issues?
 * synchronize methods?
 * look into fixing the problems with threading, if exists.
 */

 /**
  * The {@code BrushController} handles the drawing of brushes onto an image.
  * @author coobird
  *
  */
 public class BrushController
 {
 	/**
 	 * The {@code BrushAction} class represents an event to process.
 	 * Events such as drawing and releasing a brush is represented as an
 	 * instance of {@code BrushAction}, and is processed by the 
 	 * {@code BrushController}.
 	 * @author coobird
 	 *
 	 */
 	static class BrushAction
 	{
 		private final Brush b;
 		private final ImageLayer il;
 		private final int x;
 		private final int y;
 		private final State state;
 
 		private enum State
 		{
 			DRAW,
 			RELEASE;
 		}
 		
 		private BrushAction(ImageLayer il, Brush b, int x, int y)
 		{
 			this.b = b;
 			this.il = il;
 			this.x = x;
 			this.y = y;
 			this.state = State.DRAW;
 		}
 		
 		private BrushAction()
 		{
 			this.b = null;
 			this.il = null;
 			this.x = UNDEFINED;
 			this.y = UNDEFINED;
 			this.state = State.RELEASE;
 		}
 		
 		private int getX()
 		{
 			return this.x;
 			
 		}
 		private int getY()
 		{
 			return this.y;
 			
 		}
 		private Brush getBrush()
 		{
 			return this.b;
 			
 		}
 		private ImageLayer getLayer()
 		{
 			return this.il;
 			
 		}
 		private State getState()
 		{
 			return this.state;
 			
 		}
 	}
 
 	private static final int UNDEFINED = Integer.MIN_VALUE;
 	private static final double UNDEFINED_THETA = Double.NaN;
 	
 	private Queue<BrushAction> actionQueue;
 	private double theta;
 	private double lastTheta = UNDEFINED_THETA;
 	private int lastX = UNDEFINED;
 	private int lastY = UNDEFINED;
 	private Brush lastBrush = null;
 	private boolean rotatable = true;
 	private long lastTime = System.currentTimeMillis();
 
 	private DrawingThread drawingThread = new DrawingThread();
 	private ExecutorService executor = Executors.newSingleThreadExecutor();
 	
 	
 	/**
 	 * Constructs an instance of the {@code BrushController}.
 	 */
 	public BrushController()
 	{
 		actionQueue = new LinkedList<BrushAction>();
 		theta = 0;
 	}
 	
 	/**
 	 * Draws the brush between last draw point and current draw point.
 	 * @param action		The {@code BrushAction} containing the draw action.
 	 */
 	private void interpolatedDraw(BrushAction action)
 	{
 		// Constant used to determine the distance moved between one brush draw.
 		// By detault, the distance is 1/8 the size of the brush.
 		final int STEP_DIVISION = 8;
 		
 		Graphics2D g = action.getLayer().getGraphics();
 		setCompositeForBrush(g, action.getBrush());
 		BufferedImage brushImage = action.getBrush().getImage();
 
 		double distX = lastX - action.getX();
 		double distY = lastY - action.getY();
 	
 		double dist = Math.sqrt(Math.pow(distX, 2) + Math.pow(distY, 2));
 		
 		double steps = dist / (double)(brushImage.getWidth() / (double)STEP_DIVISION);
 		
 		double incX = -distX / steps;
 		double incY = -distY / steps;
 		
 		double x = lastX - (brushImage.getWidth() / 2);
 		double y = lastY - (brushImage.getHeight() / 2);
 		
 		int intSteps = (int)Math.round(steps);
 		
 		// true -> unsmooth transition of brush angle
 		// false -> smooth transition of brush angle --> buggy
 		if (false)
 		{
 			BufferedImage rotatedBrushImage = new BufferedImage(
 					brushImage.getWidth(),
 					brushImage.getHeight(),
 					BufferedImage.TYPE_INT_ARGB
 			);
 
 			Graphics2D brushg = rotatedBrushImage.createGraphics();
 			if (rotatable)
 			{
 				brushg.rotate(
 						theta,
 						rotatedBrushImage.getWidth() / 2.0d,
 						rotatedBrushImage.getHeight() / 2.0d
 				);
 			}
 	
 			brushg.drawImage(brushImage, 0, 0, null);
 			
 			for (int i = 0; i < intSteps; i++)
 			{
 				x += incX;
 				y += incY;
 				
 				g.drawImage(rotatedBrushImage, (int)x, (int)y, null);
 			}
 			brushg.dispose();
 		}
 		else
 		{
 			double t = lastTheta;
 			double incTheta = lastTheta - theta;
 			
 			for (int i = 0; i < intSteps; i++)
 			{
 				x += incX;
 				y += incY;
 				t += incTheta;
 
 				BufferedImage rotatedBrushImage = new BufferedImage(
 						brushImage.getWidth(),
 						brushImage.getHeight(),
 						BufferedImage.TYPE_INT_ARGB
 				);
 				
 				Graphics2D brushg = rotatedBrushImage.createGraphics();
 				
 				if (rotatable)
 				{
 					brushg.rotate(
 							t,
 							rotatedBrushImage.getWidth() / 2,
 							rotatedBrushImage.getHeight() / 2
 					);
 				}
 		
 				brushg.drawImage(brushImage, 0, 0, null);
 				
 				g.drawImage(rotatedBrushImage, (int)x, (int)y, null);
 				brushg.dispose();
 			}
 		}
 	}
 
 	/*
 	 * Note: Exception happened here, may be threading-related.
 	 *  
 	 * Exception in thread "pool-1-thread-1" java.util.NoSuchElementException
 	at java.util.LinkedList.remove(Unknown Source)
 	at java.util.LinkedList.removeFirst(Unknown Source)
 	at java.util.LinkedList.remove(Unknown Source)
 	at net.coobird.paint.brush.BrushController.processBrush(BrushController.java:217)
 	at net.coobird.paint.brush.BrushController.access$1(BrushController.java:215)
 	at net.coobird.paint.brush.BrushController$DrawingThread.run(BrushController.java:397)
 	at java.util.concurrent.ThreadPoolExecutor$Worker.runTask(Unknown Source)processing
 
 	at java.util.concurrent.ThreadPoolExecutor$Worker.run(Unknown Source)
 	at java.lang.Thread.run(Unknown Source)
 	 */
 	
 	/**
 	 * Processes a {@code BrushAction} in the queue.
 	 */
 	private void processBrush()
 	{
 		BrushAction action = actionQueue.remove();
 		
 		if (
 				lastBrush != action.getBrush() ||
 				action.getState() == BrushAction.State.RELEASE
 		)
 		{
 			System.out.println("last brush released.");
 			clearTheta();
 		}
 		
 		if (action.getState() == BrushAction.State.RELEASE)
 		{
 			lastX = UNDEFINED;
 			lastY = UNDEFINED;
 			return;
 		}
 		
 
 		Graphics2D g = action.getLayer().getGraphics();
 		
 		// TODO Setting Composite for g should be fine.
 		// if so, remove one setComposite... from interpolateDraw and here.
 		// uncomment below:
 		// setCompositeForBrush(g, action.getBrush());
 
 		BufferedImage brushImage = action.getBrush().getImage();
 		
 		// Determine the brush painting mean to use.
 		// If brush is already being dragged across, then use interpolatedDraw.
 		// If not, handle the "single-click" painting of brush.
 		if (lastX != UNDEFINED)
 		{
 			if (rotatable)
 			{
 				calcTheta(action.getX(), action.getY());
 			}
 			
 			interpolatedDraw(action);
 		}
 		else
 		{
 			int x = action.getX() - (brushImage.getWidth() / 2);
 			int y = action.getY() - (brushImage.getHeight() / 2);
 
 			setCompositeForBrush(g, action.getBrush());
 			g.drawImage(brushImage, x, y, null);
 		}
 		
 		lastX = action.x;
 		lastY = action.y;
 		
 		lastBrush = action.getBrush();
 	}
 	
 	/**
 	 * Sets the {@link Composite} of the given {@link Graphics2D} object. Used
 	 * for setting the composite mode of the {@code Graphics2D} object to allow
 	 * switching modes between a regular brush and an eraser.
 	 * @param g				The {@code Graphics2D} object to change the
 	 * 						composite mode of.
 	 * @param b				The {@code Brush} object from which the composite
 	 * 						mode to set to should be retrieved.
 	 */
 	private void setCompositeForBrush(Graphics2D g, Brush b)
 	{
 		Composite layerComposite = AlphaComposite.getInstance(
 				b.getMode().getComposite().getRule(),
 				b.getAlpha()
 		);
 		
 		g.setComposite(layerComposite);
 	}
 	
 	/**
 	 * Draws the on the specified {@link ImageLayer} object, using the specified
 	 * {@link Brush} object at the location {@code x, y}.
 	 * @param il			The {@code ImageLayer} object to draw on.
 	 * @param b				The {@code Brush} object to draw with.
 	 * @param x				The {@code x} coordinate of the {@code ImageLayer}
 	 * 						to draw the brush at.
 	 * @param y				The {@code y} coordinate of the {@code ImageLayer}
 	 * 						to draw the brush at.
 	 */
 	public void drawBrush(ImageLayer il, Brush b, int x, int y)
 	{
 		/*
 		 * Account for offset of the ImageLayer
 		 * wont work at zoom != 1
 		 */
 //		x = x - il.getX();
 //		y = y - il.getY();
 		
 		actionQueue.add(new BrushAction(il, b, x, y));
 		System.out.println("add brushevent");
 		long timePast = System.currentTimeMillis() - lastTime;
 		
 		if (!drawingThread.running && timePast > 100)
 		{
 			System.out.println("start thread");
 
 			executor.execute(drawingThread);
 			
 			lastTime = System.currentTimeMillis();
 		}
 	}
 	
 	public void releaseBrush()
 	{
 		actionQueue.add(new BrushAction());
 	}
 	
 	/**
 	 * Calculate the angle to rotate the brush.
 	 * @param x			The x coordinate of the brush.
 	 * @param y			The y coordinate of the brush.
 	 */
 	private void calcTheta(int x, int y)
 	{
 		if (lastX == UNDEFINED && lastY == UNDEFINED)
 		{
 			lastX = x;
 			lastY = y;
 		}
 		lastTheta = theta;
 		
 		int diffX = lastX - x;
 		int diffY = lastY - y;
 		
 		theta = Math.atan2(diffX, diffY);
 	}
 	
 	/**
 	 * Clears the stored location and angle of the last brush action.
 	 */
 	private void clearTheta()
 	{
 		lastX = UNDEFINED;
 		lastY = UNDEFINED;
 		lastTheta = UNDEFINED_THETA;
 	}
 	
 	/**
 	 * Sets the state for allowing the brush to rotate depending on the movement
 	 * of the brush.
 	 * @param b				Whether or not to allow rotation of the brush.
 	 */
 	public void setMovable(boolean b)
 	{
 		this.rotatable = b;
 	}
 	
 	/**
 	 * Returns whether or not brush rotation is currently enabled.
 	 * @return				Returns {@code true} if brush rotation is allowed,
 	 * 						{@code false} otherwise.
 	 */
 	public boolean getMovable()
 	{
 		return this.rotatable;
 	}
 
 	/**
 	 * Thread for processing the {@link BrushAction} queue.
 	 * @author coobird
 	 *
 	 */
 	class DrawingThread extends Thread
 	{
 		boolean running = false;
 		
 		/**
 		 * Processes the {@link BrushAction} queue.
 		 */
 		public void run()
 		{
 			running = true;
 			while(!actionQueue.isEmpty())
 			{
 				System.out.println("processing");
 				processBrush();
 			}
 			running = false;
 		}
 	}
 }
