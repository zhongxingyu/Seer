 package ch.sfdr.fractals.fractals;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 
 import ch.sfdr.fractals.gui.component.ColorMap;
 import ch.sfdr.fractals.gui.component.ImageDisplay;
 import ch.sfdr.fractals.math.ComplexNumber;
 import ch.sfdr.fractals.math.LineClipping;
 import ch.sfdr.fractals.math.Scaler;
 
 /**
  * A fractal based on escape time algorithm
  * @author S.Freihofer
  * @author D.Ritz
  */
 public class ComplexEscapeFractal
 {
 	private ImageDisplay display;
 	private Scaler scaler;
 	private StepFractalFunction function;
 	private ColorMap colorMap;
 	private Color setColor = Color.BLACK;
 
 	private Thread thread;
 
 	private double boundarySqr;
 
 	private StatisticsObserver statObserver;
 	private long stepCount;
 	private long drawTime;
 
 	private ArrayList<Orbit> orbitList = new ArrayList<Orbit>();
 
 	/**
 	 * Creates the ComplexEscapeFractal
 	 * @param display the display area to draw on
 	 * @param scaler the scaler to convert between screen and fractal coordinates
 	 * @param function the function to use
 	 */
 	public ComplexEscapeFractal(ImageDisplay display, Scaler scaler,
 			StepFractalFunction function, ColorMap colorMap)
 	{
 		this.display = display;
 		this.scaler = scaler;
 		this.colorMap = colorMap;
 		setFractalFunction(function);
 	}
 
 	/**
 	 * Sets the fractal function and initializes the scaler
 	 * @param function the function to use
 	 */
 	public void setFractalFunction(StepFractalFunction function)
 	{
 		this.function = function;
 		boundarySqr = function.getBoundarySqr();
 		scaler.init(
 			function.getLowerBounds().getReal(),
 			function.getLowerBounds().getImaginary(),
 			function.getUpperBounds().getReal(),
 			function.getUpperBounds().getImaginary());
 	}
 
 	/**
 	 * Returns the currently used fractal function
 	 * @return the function
 	 */
 	public StepFractalFunction getFunction()
 	{
 		return function;
 	}
 
 	/**
 	 * Sets the specific ColorMap
 	 * @param colorMap the colorMap to set
 	 */
 	public void setColorMap(ColorMap colorMap)
 	{
 		this.colorMap = colorMap;
 	}
 
 	/**
 	 * Sets the set color
 	 * @param setColor the setColor to set
 	 */
 	public void setSetColor(Color setColor)
 	{
 		this.setColor = setColor;
 	}
 
 	/**
 	 * Sets the StatisticsObserver
 	 * @param statObserver the statObserver to set
 	 */
 	public void setStatObserver(StatisticsObserver statObserver)
 	{
 		this.statObserver = statObserver;
 	}
 
 	/**
 	 * Gets the StatisticsObserver
 	 * @return the statObserver
 	 */
 	public StatisticsObserver getStatObserver()
 	{
 		return statObserver;
 	}
 
 	/**
 	 * Draws the fractal with a max number of iterations per point
 	 * @param maxIterations the max number of iterations
 	 * @throws InterruptedException
 	 */
 	public synchronized void drawFractal(final int maxIterations)
 	{
 		// if there's a previous thread: interrupt and wait for it
 		if (thread != null) {
 			thread.interrupt();
 			try {
 				thread.join();
 			} catch (InterruptedException e) {
 			}
 		}
 
 		// create and start the new thread
 		thread = new Thread() {
 			@Override
 			public void run()
 			{
 				long start = System.currentTimeMillis();
 
 				doDrawFractal(maxIterations);
 
 				drawTime = System.currentTimeMillis() - start;
 
 				if (statObserver != null)
 					statObserver.statisticsDataAvailable();
 			}
 		};
 		thread.start();
 	}
 
 	/**
 	 * Draws the orbit for the given coordinates and step delay
 	 * @param x the display coordinate in x direction
 	 * @param y the display coordinate in y direction
 	 * @param maxIter the max number of iterations
 	 * @param stepDelay the delay in milliseconds
 	 */
 	public void drawOrbit(int x, int y, int maxIterations, Color color, long stepDelay)
 	{
 		drawOrbit(new ComplexNumber(scaler.scaleX(x), scaler.scaleY(y)),
 			maxIterations, color, stepDelay);
 	}
 
 	/**
 	 * Draws the orbit for the given coordinates and step delay
 	 * @param start the start coordinate as complex number
 	 * @param maxIter the max number of iterations
 	 * @param stepDelay the delay in milliseconds
 	 */
 	public void drawOrbit(final ComplexNumber start, final int maxIterations,
 			final Color color, final long stepDelay)
 	{
 		orbitList.add(new Orbit(start, color, maxIterations));
 
 		Thread thread = new Thread() {
 			@Override
 			public void run()
 			{
 				BufferedImage img = display.createImage();
 				Graphics2D g = img.createGraphics();
 
 				doDrawOrbit(img, g, start, maxIterations, color, stepDelay, true);
 			}
 		};
 		thread.start();
 	}
 
 	private void doDrawFractal(int maxIterations)
 	{
 		stepCount = 0;
 
 		int width = display.getImageWidth();
 		int height = display.getImageHeight();
 
 		BufferedImage img = display.createImage();
 		Graphics2D g = img.createGraphics();
 
 		// the only two, reusable complex numbers
 		ComplexNumber z0 = new ComplexNumber(0, 0);
 		ComplexNumber z = new ComplexNumber(0, 0);
 
 		/*
 		 * main loop for all pixels
 		 *
 		 * loop hierarchy:
 		 * - from coarse to fine
 		 * -- rows
 		 * --- pixels
 		 */
 		for (int step = 16, oldStep = 32; step > 0; oldStep = step, step /= 2) {
 			for (int y = 0; y < height; y += step) {
 				int start = 0;
 				int inc = step;
 				// skip pixel already drawn in last iteration
 				if (step < 16 && y % oldStep == 0) {
 					start = step;
 					inc = oldStep;
 				}
 
 				if (Thread.interrupted())
 					return;
 
 				double fractalY = scaler.scaleY(y);
 
 				// inner loop: pixels
 				for (int x = start; x < width; x += inc) {
 					double fractalX = scaler.scaleX(x);
 
 					// set the z0 and the variable z to the current values
 					z0.set(fractalX, fractalY);
 					z.set(fractalX, fractalY);
 
 					// get iteration count using the step() function
 					int count = 0;
 					while (z.absSqr() < boundarySqr && count++ < maxIterations)
 						function.step(z0, z);
 
 					stepCount += count;
 
 					// map to color and draw pixel
 					Color color = getColor(count, maxIterations);
 					g.setColor(color);
 			        g.fillRect(x, y, step, step);
 				}
 			}
 			display.updateImage(img, 0);
 		}

		synchronized (this) {
			thread = null;
		}
 	}
 
 	private void doDrawOrbit(BufferedImage img, Graphics2D g,
 			ComplexNumber orbitStart, int maxIterations,
 			Color orbitColor, long orbitDelay, boolean updateImage)
 	{
 		ComplexNumber z0 = orbitStart;
 		ComplexNumber z = z0.clone();
 
 		LineClipping lc = new LineClipping();
 
 		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 			RenderingHints.VALUE_ANTIALIAS_ON);
 
 		g.setColor(orbitColor);
 
 		int width = display.getImageWidth();
 		int height = display.getImageHeight();
 
 		double lastX = scaler.unscaleX(z0.getReal());
 		double lastY = scaler.unscaleY(z0.getImaginary());
 
 		// draw a little circle indicating the starting point
 		if (LineClipping.isPointInside(lastX, lastY, 0, 0, width, height)) {
 			g.setStroke(new BasicStroke(1.5f));
 			g.drawOval((int) Math.round(lastX) - 5, (int) Math.round(lastY) - 5,
 				10, 10);
 		}
 
 		// a thinner stroke for the lines
 		g.setStroke(new BasicStroke(0.4f));
 
 		int count = 0;
 		while (z.absSqr() < boundarySqr && count++ < maxIterations) {
 			function.step(z0, z);
 
 			double x = scaler.unscaleX(z.getReal());
 			double y = scaler.unscaleY(z.getImaginary());
 
 			if (lc.clipLineToRectangle(lastX, lastY, x, y, 0, 0, width, height)) {
 				g.drawLine(lc.getClipX1(), lc.getClipY1(),
 					lc.getClipX2(), lc.getClipY2());
 
 				// only show the first 30 steps "animated", the rest in one go
 				if (count < 30 && orbitDelay > 0) {
 					display.updateImage(img, 1);
 					sleep(orbitDelay);
 				}
 			}
 
 			lastX = x;
 			lastY = y;
 
 		}
 		if (updateImage)
 			display.updateImage(img, 1);
 	}
 
 	private static void sleep(long delay)
 	{
 		try {
 			Thread.sleep(delay);
 		} catch (InterruptedException e) {
 		}
 	}
 
 	private Color getColor(int count, int maxIterations)
 	{
 		if (count >= maxIterations)
 			return setColor;
 		return colorMap.getColor(count);
 	}
 
 	/**
 	 * Gets the count of all steps
 	 * @return the stepCount
 	 */
 	public long getStepCount()
 	{
 		return stepCount;
 	}
 
 	/**
 	 * Gets the time to draw in milliseconds
 	 * @return the drawTime
 	 */
 	public long getDrawTime()
 	{
 		return drawTime;
 	}
 
 	private static class Orbit
 	{
 		private ComplexNumber start;
 		private Color color;
 		private int itarations;
 
 		public Orbit(ComplexNumber start, Color color, int iterations)
 		{
 			this.start = start;
 			this.color = color;
 			this.itarations = iterations;
 		}
 	}
 
 	/**
 	 * Used to draw the saved orbits again, after a zoom
 	 */
 	public void redrawAllOrbits()
 	{
 		BufferedImage img = display.createImage();
 		Graphics2D g = img.createGraphics();
 
 		for (Orbit o : orbitList) {
 			doDrawOrbit(img, g, o.start, o.itarations, o.color, 0, false);
 		}
 		display.updateImage(img, 1);
 	}
 
 	/**
 	 * Removes all saved orbits from the list
 	 */
 	public void clearOrbits()
 	{
 		orbitList.clear();
 		display.clearLayer(1);
 	}
 }
