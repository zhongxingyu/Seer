 import java.awt.*;
 import java.awt.event.*;
 
 /********************************************************************************
 ** This is the window used to display the programs output.
 */
 
 public class Display extends Canvas
 {
 	/********************************************************************************
 	** Default constructor.
 	*/
 
 	public Display()
 	{
 		setBackground(Color.white);
 
 		// Add event handler.
 		addComponentListener(new ResizeListener());
 	}
 
 	/********************************************************************************
 	** Get the displays dimensions.
 	*/
 
 	public Dimension getDimensions()
 	{
 		return m_dmBuffer;
 	}
 
 	/********************************************************************************
 	** Clear the display.
 	*/
 
 	public void clear()
 	{
 		m_gImgBuffer.clearRect(0, 0, m_dmBuffer.width, m_dmBuffer.height);
 	}
 
 	/********************************************************************************
 	** Set the line drawing colour.
 	*/
 
 	public void setLineColour(Color clr)
 	{
 		m_gImgBuffer.setColor(clr);
 	}
 
 	/********************************************************************************
 	** Draw a line between the two points.
 	*/
 
 	public void drawLine(int x1, int y1, int x2, int y2)
 	{
 		m_gImgBuffer.drawLine(x1, y1, x2, y2);
 	}
 
 	/********************************************************************************
 	** Draws a polyline.
 	*/
 
 	public void drawPolyline(Point[] aPoints)
 	{
 		int   n = aPoints.length;
 		int[] x = new int[n];
 		int[] y = new int[n];
 
 		// Convert Points array to separate x,y arrays.
 		for (int i = 0; i < aPoints.length; i++)
 		{
 			x[i] = aPoints[i].x;
 			y[i] = aPoints[i].y;
 		}		
 
 		m_gImgBuffer.drawPolyline(x, y, n);
 	}
 
 	/********************************************************************************
 	** Set the painting mode.
 	*/
 
 	public void setPaintMode(boolean bXOR, Color clr)
 	{
 		if (bXOR)
 			m_gImgBuffer.setXORMode(clr);
 		else
 			m_gImgBuffer.setPaintMode();
 	}
 
 	/********************************************************************************
 	** Update the window.
 	*/
 
 	public void update(Graphics g)
 	{
 		paint(g);
 	}
 
 	/********************************************************************************
 	** Paint the window.
 	*/
 
 	public void paint(Graphics g)
 	{
 		// Get the cancas dimensions.
 		Dimension dmSize = getSize();
 
 		// Draw the left-top outer edge.
 		g.setColor(SystemColor.controlDkShadow);
 		g.drawLine(0, 0, dmSize.width-1, 0);
 		g.drawLine(0, 0, 0,              dmSize.height-1);
 
 		// Draw the left-top inner edge.
 		g.setColor(SystemColor.controlShadow);
 		g.drawLine(1, 1, dmSize.width-2, 1);
 		g.drawLine(1, 1, 1,              dmSize.height-2);
 
 		// Draw the right-botton inner edge.
 		g.setColor(SystemColor.control);
 		g.drawLine(2,              dmSize.height-2, dmSize.width-2, dmSize.height-2);
 		g.drawLine(dmSize.width-2, dmSize.height-2, dmSize.width-2, 2);
 
 		// Draw the right-botton outer edge.
 		g.setColor(SystemColor.controlLtHighlight);
 		g.drawLine(1,              dmSize.height-1, dmSize.width-1, dmSize.height-1);
 		g.drawLine(dmSize.width-1, dmSize.height-1, dmSize.width-1, 1);
 
 		// Blit buffer to window.
 		if (m_imgBuffer != null)
 			g.drawImage(m_imgBuffer, 2, 2, this);
 	}
 
 	/********************************************************************************
	** Adapter to handle component events.
 	*/
 
 	public class ResizeListener extends ComponentAdapter
 	{
 		public void componentResized(ComponentEvent eEvent)
 		{
 			Dimension dmSize = getSize();
 
			// Ensure we create a valid buffer.
			if (dmSize.width  < 5)	dmSize.width  = 5;
			if (dmSize.height < 5)	dmSize.height = 5;

 			// Create off-screen buffer to fill canvas inner area.
 			m_dmBuffer   = new Dimension(dmSize.width-4, dmSize.height-4);
 			m_imgBuffer  = createImage(m_dmBuffer.width, m_dmBuffer.height);
 			m_gImgBuffer = m_imgBuffer.getGraphics();
 		}
 	}
 
 	/********************************************************************************
 	** Constants
 	*/
 
 	/********************************************************************************
 	** Members.
 	*/
 
 	private Dimension	m_dmBuffer;		// Off screen bitmap dimensions.
 	private Image		m_imgBuffer;	// Off-screen bitmap.
 	private Graphics	m_gImgBuffer;	// Graphics context for off-screen bitmap.
 }
