 package com.bluebarracudas.app;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Insets;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.util.Collection;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 
 import com.bluebarracudas.model.TFactory;
 import com.bluebarracudas.model.TStation;
 import com.bluebarracudas.model.TStop;
 import com.bluebarracudas.model.TStopData;
 import com.bluebarracudas.model.TTrip;
 
 /** A panel to display our MBTA map */
 public class TMapPanel extends JPanel implements Runnable {
 
 	/** Our buffered image */
 	private Image bufferImage;
 	/** A reference to our buffer image's graphics object */
 	private Graphics2D imageGraphics;
 
 	/** The height & width of the trip circle **/
 	public static int tripSize = 6;
 	/** The height & width of the stop square **/
 	public static int stopSize = 10;
 	public static final int WIDTH = 800;
 	public static final int HEIGHT = 600;
 	public static final int PADDING = 100;
 
 	/* Our frame rate calculation variables */
 	private long m_nTimeLastFrameTime = 0;
 	private int  m_nFrameRate = 0;
 
 	/* Our FPS display variables */
 	private final boolean m_bShowFPS  = true;
 	private final Color   m_pFPSColor = Color.RED;
 	private final Font    m_pFPSFont  = new Font("San Serif", Font.PLAIN | Font.BOLD, 20);
 
 	/** Default constructor */
 	public TMapPanel() {
 
 		// Set our preferred size
 		setPreferredSize(new Dimension(WIDTH + PADDING * 2, HEIGHT + PADDING * 2));
 
 		// Set the layout manager to be null so we can put buttons where we want them
 		this.setLayout(null);
 		
 		// Get the focus
 		setFocusable(true);
 		requestFocusInWindow();
 
 		// Start our main loop
 		new Thread(this).start();
 	}
 
 	@Override
 	/** Our main loop */
 	public void run() {
 		// Set our thread priority
 		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
 		while (true) {
 
 			// Try to calculate our frame rate
 			try {
 				float fDeltaTime = (float)(System.currentTimeMillis() - m_nTimeLastFrameTime);
 				m_nTimeLastFrameTime = System.currentTimeMillis();
 				m_nFrameRate = (int)(1/(fDeltaTime/1000));
 			}
 			catch(Exception e) { }
 
 			// Make sure we're valid before trying to display ourself
 			if (isValid()) {
 				render();
 				display();
 			}
 
 			// Go to sleep for a bit
 			try {
 				Thread.sleep(5);
 			} catch (InterruptedException ex) {
 			}
 
 			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
 		}
 	}
 
 	/**
 	 * Render our buffer image
 	 *
 	 * @author Tom Fiset
 	 * Revised by: Liz Brown
 	 */
 	private void render() {
 		// First set everything up...
 
 		// If we don't have a buffer image, create one
 		if (bufferImage == null) {
 			bufferImage = createImage(getSize().width, getSize().height);
 
 			// If we couldn't create a buffer image, let the world know.
 			if (bufferImage == null) {
 				System.out.println("Failed to create buffer Image...");
 				return;
 			}
 
 			// Otherwise associate it with our graphics object.
 			imageGraphics = (Graphics2D) bufferImage.getGraphics();
 
 			imageGraphics.setBackground(Color.black);
 			imageGraphics.setClip( this.getBounds() );
 		}
 
 		// Do actual rendering here...
 
 		// Fill the screen with our background color
 		imageGraphics.setColor(imageGraphics.getBackground());
 		imageGraphics.fill(imageGraphics.getClip());
 
 		// Draw the stations on the background
 		renderStations();
 
 		// Draw the lines between stops
 		renderLines();
 
 		// Draw the trips on the background
 		renderTrips();
 
 		// Display our FPS
 		if(m_bShowFPS) {
 			imageGraphics.setColor(m_pFPSColor);
 			imageGraphics.setFont(m_pFPSFont);
 			imageGraphics.drawString(Integer.toString(m_nFrameRate), 50, 50);
 		}
 	}
 
 	/**
 	 * Render buttons all of the stops
 	 *
 	 * @author Liz Brown, Tom Fiset
 	 * @param g The Graphics2D to render on
 	 */
 	private void renderStations() {
 
 		// Get the action handler
 		THandler handler = new THandler();
 		
 		// Place a button on each station
 		for (TStation pStation : TFactory.getAllStations()) {
 			Point2D pPos = pStation.getPosition();
 
 			Insets insets = this.getInsets();
 			JButton stopButton = new JButton();
 			stopButton.setBounds(insets.left + (int)pPos.getX(), insets.right + (int)pPos.getY(), 10, 10);
 			//stopButton.setBounds((int)pPos.getX(), (int)pPos.getY(), 10, 10);
 			stopButton.setActionCommand(""+pStation.getID());
 			stopButton.addActionListener(handler);
 			stopButton.setVisible(true);
 			this.add(stopButton);
 		}
 	}
 
 	/**
 	 * Draw lines between the stations Note: This is a very rough proof of
 	 * concept.
 	 *
 	 * @author Liz Brown
 	 * @param g The Graphics2D to render on
 	 */
 	private void renderLines() {
 		// TODO Make this more efficient
 		// TODO Figure out the best way to handle Ashmont -> JFK & transfer
 		// stations
 		
		renderLine(TFactory.getStop(TStopData.OAK_GROVE_S.getID()));
		renderLine(TFactory.getStop(TStopData.WONDERLAND_S.getID()));
		renderLine(TFactory.getStop(TStopData.ALEWIFE_S.getID()));
 		renderLine(TFactory.getStop(TStopData.JFK_S_B.getID()));
 	}
 	
 	private void renderLine(TStop stop) {
 		List<TStop> nextStops = stop.getNextStops();
 		TStop nextStop = null;
 		if (nextStops != null)
 			nextStop = nextStops.get(0);
 
 		while (nextStop != null) {
 			drawLine(stop, nextStop);
 			stop = nextStop;
 			
 			List<TStop> newNextStops = stop.getNextStops();
 			nextStop = (!newNextStops.isEmpty()) ? newNextStops.get(0) : null;
 		}
 	}
 	
 	private void drawLine(TStop stop, TStop nextStop) {
 		int offset = stopSize / 2;
 		imageGraphics.setPaint(Color.white);
 		imageGraphics.drawLine((int)stop.getStation().getPosition().getX() + offset,
 				(int)stop.getStation().getPosition().getY() + offset,
 				(int)nextStop.getStation().getPosition().getX() + offset,
 				(int)nextStop.getStation().getPosition().getY() + offset);
 	}
 
 	/**
 	 * Render all of the trips on the map
 	 *
 	 * @author Liz Brown
 	 * @param g The Graphics2D to render on
 	 */
 	private void renderTrips() {
 		Color tripColor = new Color(0, 255, 255, 200);
 
 		Collection<TTrip> trips = TFactory.getAllTrips();
 		for (TTrip trip : trips) {
 			Point2D position = trip.getPosition();
 
 			imageGraphics.setPaint(tripColor);
 			imageGraphics.fill(new Rectangle2D.Double(position.getX(), position.getY(), tripSize,
 					tripSize));
 		}
 	}
 
 	/** Draw our buffer image on the screen */
 	private void display() {
 		// A reference to our screen graphics object
 		Graphics screenGraphics;
 
 		// Try to set up the reference...
 		try {
 			// Get the reference
 			screenGraphics = this.getGraphics();
 
 			// And use it to draw our buffer image
 			if ((screenGraphics != null) && (bufferImage != null))
 				screenGraphics.drawImage(bufferImage, 0, 0, null);
 
 			// Clean up
 			screenGraphics.dispose();
 		} catch (Exception e) {
 			System.out.println("Exception in display()! " + e);
 		}
 	}
 }
