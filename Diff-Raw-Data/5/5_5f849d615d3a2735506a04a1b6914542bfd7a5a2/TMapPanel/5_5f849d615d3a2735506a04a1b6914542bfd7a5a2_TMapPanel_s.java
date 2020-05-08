 package com.bluebarracudas.app;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 
 import sun.java2d.loops.DrawLine;
 
 import com.bluebarracudas.model.TFactory;
 import com.bluebarracudas.model.TRoute;
 import com.bluebarracudas.model.TStation;
 import com.bluebarracudas.model.TStop;
 import com.bluebarracudas.model.TStopData;
 import com.bluebarracudas.model.TTrip;
 
 /** A panel to display our MBTA map */
 public class TMapPanel extends JPanel implements Observer {
 
 	/** The height & width of the trip circle **/
 	public static int tripSize = 6;
 	/** The height & width of the stop square **/
 	public static int stopSize = 10;
 	public static final int WIDTH = 800;
 	public static final int HEIGHT = 800;
 	public static final int PADDING = 100;
 
 	/** The size of our trip indicators */
 	private static final int TRIP_SIZE = 7;
 	/** The size of our station buttons */
 	private static final int STATION_SIZE = 10;
 	/** The size of our T system map lines */
 	private static final int MAP_LINE_SIZE = 5;
 	/** */
 	private static final int ROUTE_LINE_SIZE = 10;
 
 	/** The color of our trip indicators */
 	private static final Color TRIP_COLOR = new Color(100, 190, 140);
 
 	/** Our station buttons */
 	private List<JButton> m_pStationButtons;
 
 	/** Default constructor */
 	public TMapPanel() {
 
 		// Set our preferred size
 		setPreferredSize(new Dimension(WIDTH + PADDING * 2, HEIGHT + PADDING * 2));
 
 		// Set the layout manager to be null so we can put buttons where we want them
 		this.setLayout(null);
 
 		// Get the action handler
 		THandler handler = new THandler();
 
 		m_pStationButtons = new ArrayList<JButton>();
 
 		// Create our station buttons
 		for (TStation pStation : TFactory.getAllStations()) {
 			Point2D pPos = pStation.getPosition();
 
 			// Create and set up a button
 			JButton stopButton = new JButton();
 			stopButton.setBounds((int)pPos.getX(), (int)pPos.getY(), STATION_SIZE, STATION_SIZE);
 			stopButton.setActionCommand(""+pStation.getID());
 			stopButton.addActionListener(handler);
 			stopButton.setVisible(true);
 
 			// Add it to our button list
 			m_pStationButtons.add(stopButton);
 		}
 
 		// Now add the buttons to our panel
 		for(JButton pButton : m_pStationButtons)
 			this.add(pButton);
 		
 		// Get the focus
 		setFocusable(true);
 		requestFocusInWindow();
 	}
 
 	/**
 	 * Render our buffer image
 	 *
 	 * @author Tom Fiset
 	 * Revised by: Liz Brown
 	 */
 	@Override
 	public void paintComponent(Graphics g) {
 
 		Graphics2D g2 = (Graphics2D)g;
 		// Fill the screen with our background color
 		g2.setColor(Color.BLACK);
 		g2.fill(this.getBounds());
 		// Enable anti-aliasing
 		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 		                    RenderingHints.VALUE_ANTIALIAS_ON);
 
 		// Draw the lines between stops
 		renderLines(g2);
 
 		// Draw the trips on the background
 		renderTrips(g2);
 		
 		// Draw our routes
 		renderRoutes(g2);
 	}
 
 	/**
 	 * Draw lines between the stations
 	 *
 	 * @author Liz Brown
 	 * @param g The Graphics2D to render on
 	 */
 	private void renderLines(Graphics2D g2) {
 		renderLine(g2, Color.ORANGE, MAP_LINE_SIZE,
 					   TFactory.getStop(TStopData.OAK_GROVE.getID()), 
 					   TFactory.getStop(TStopData.FOREST_HILLS.getID()));
 		renderLine(g2, Color.BLUE, MAP_LINE_SIZE,
 					   TFactory.getStop(TStopData.WONDERLAND.getID()),
 					   TFactory.getStop(TStopData.BOWDOIN.getID()));
 		renderLine(g2, Color.RED, MAP_LINE_SIZE,
 				       TFactory.getStop(TStopData.ALEWIFE.getID()),
 					   TFactory.getStop(TStopData.ASHMONT_S.getID()));
 		renderLine(g2, Color.RED, MAP_LINE_SIZE,
 				       TFactory.getStop(TStopData.JFK_S_B.getID()),
 					   TFactory.getStop(TStopData.BRAINTREE.getID()));
 	}
 
 	/**
 	 * Renders a line beginning at the coordinates of <param>stop</param>
 	 * and ending at the coordinates of <param>end</param>.
 	 * 
 	 * @author Liz Brown
 	 * @param g2 The Graphics2D to render on
 	 * @param color The color the line should be rendered in
 	 * @param strokeSize The width of the line to be rendered
 	 * @param stop The starting stop for the line
 	 * @param end The last stop to render the line to
 	 */
 	private void renderLine(Graphics2D g2, Color color, int strokeSize, TStop stop, TStop end) {
 		List<TStop> endNext = end.getNextStops();
 		List<TStop> nextStops = stop.getNextStops();
 		TStop nextStop = null;
 		if (nextStops != null)
 			nextStop = nextStops.get(0);
 
 		while (!endNext.contains(nextStop) && nextStop != null) {
 
 			// Draw a line
 			int offset = stopSize / 2;
 			
 			g2.setPaint(color);
 			g2.setStroke(new BasicStroke(strokeSize));
 			g2.drawLine((int)stop.getStation().getPosition().getX() + offset,
 					(int)stop.getStation().getPosition().getY() + offset,
 					(int)nextStop.getStation().getPosition().getX() + offset,
 					(int)nextStop.getStation().getPosition().getY() + offset);
 
 			stop = nextStop;
 
 			List<TStop> newNextStops = stop.getNextStops();
 			nextStop = (!newNextStops.isEmpty()) ? newNextStops.get(0) : null;
 		}
 	}
 
 	/**
 	 * Render all our currently visible routes
 	 */
 	public void renderRoutes(Graphics2D g2) {
 		for(TRoute pRoute : TFactory.getAllRoutes())
 			renderRoute(g2, pRoute);
 	}
 
 	/**
 	 * Renders a route on the map
 	 * NOTE: This cannot handle transfer stations with the current connections
 	 * @author Liz Brown
 	 * @param route
 	 */
 	public void renderRoute(Graphics2D g2, TRoute route) {
 		// Routes should be displayed in a semi-transparent magenta
 		Color color = new Color(1, 0, 1, .5f);
 
 		for (int s = 0; s < route.getStops().size() - 1; s++) {
 			TStop start = route.getStops().get(s);
 			TStop end = route.getStops().get(s+1);
 
 			renderLine(g2, color, ROUTE_LINE_SIZE, start, end);
 		}
 	}
 	
 	
 	/**
 	 * Render all of the trips on the map
 	 *
 	 * @author Liz Brown, Tom Fiset
 	 * @param g The Graphics2D to render on
 	 */
 	private void renderTrips(Graphics2D g2) {
 		// Set our trip color
 		g2.setPaint(TRIP_COLOR);
 
 		// Now draw our trips
 		for (TTrip trip : TFactory.getAllTrips())
 			g2.fill( new Ellipse2D.Double(trip.getPosition().getX(),
 			                              trip.getPosition().getY(),
 			                              TRIP_SIZE, TRIP_SIZE) );
 	}
 
 	@Override
 	public void update(Observable arg0, Object arg1) {
 		System.out.println("UPDATE!");
 		repaint();
 	}
 }
