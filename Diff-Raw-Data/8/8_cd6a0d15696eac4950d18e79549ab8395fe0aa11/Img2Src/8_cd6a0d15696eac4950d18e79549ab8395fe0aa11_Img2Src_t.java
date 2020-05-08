 import java.awt.*;
 import java.awt.event.*;
 import java.applet.*;
 import java.net.*;
 import java.awt.image.*;
import gort.applet.*;
import gort.ui.*;
 
 /********************************************************************************
 ** The applet main class.
 */
 
 public class Img2Src extends WebApp
 	implements ActionListener
 {
 	/********************************************************************************
 	** Template method called by the VM to initialise the applet.
 	*/
 
 	public void init()
 	{
 		// Create the applet panel.
 		setLayout(new BorderLayout(5, 5));
 		setBackground(Color.black);
 
 		add(BorderLayout.NORTH, m_pnlParams);
 		add(BorderLayout.CENTER, m_pnlData);
 
 		// Create the child panels.
 		m_pnlButtons.add(m_btnLoad);
 		m_pnlButtons.add(m_btnConvert);
 
 		m_pnlParams.add(BorderLayout.CENTER, m_ebFileName);
 		m_pnlParams.add(BorderLayout.EAST,   m_pnlButtons);
 
 		m_pnlData.add(BorderLayout.CENTER, m_icnImage);
 		m_pnlData.add(BorderLayout.SOUTH,  m_ebBytes);
 
 		// Setup child controls.
 		m_ebFileName.setBackground(Color.white);
 
 		m_ebBytes.setEditable(false);
 		m_ebBytes.setBackground(Color.white);
 
 		m_btnConvert.setEnabled(false);
 
 		// Add event handlers.
 		m_btnLoad.addActionListener(this);
 		m_btnConvert.addActionListener(this);
 	}
 
 	/********************************************************************************
 	** Template method called by the VM to start the applet running.
 	*/
 
 	public void start()
 	{
 	}
 
 	/********************************************************************************
 	** Template method called by the VM to stop the applet running.
 	*/
 
 	public void stop()
 	{
 	}
 
 	/********************************************************************************
 	** Template method called by the VM to terminate the applet.
 	*/
 
 	public void destroy()
 	{
 	}
 
 	/********************************************************************************
 	** Button handler.
 	*/
 
 	public void actionPerformed(ActionEvent eEvent)
 	{
 		try
 		{
 			if (eEvent.getSource() == m_btnLoad)
 				onLoad();
 
 			if (eEvent.getSource() == m_btnConvert)
 				onConvert();
 		}
 		catch (Exception e)
 		{
 			MsgBox.alert(this, APP_NAME, e.toString());
 		}
 	}
 
 	/********************************************************************************
 	** "Load" button event handler.
 	*/
 
 	public void onLoad()
 	{
 		String strFileName = m_ebFileName.getText();
 		URL    urlRootPath = getCodeBase();
 
 		// Validate file name.
 		if (strFileName.length() == 0)
 		{
 			MsgBox.alert(this, APP_NAME, "You must supply a file name.");
 			return;
 		}
 
 		// Reset the UI.
 		m_icnImage.setImage(null);
 		m_btnConvert.setEnabled(false);
 		m_ebBytes.setText("");
 
 		// Load the image.
 		m_imgImage = getImage(urlRootPath, strFileName);
 
 		try
 		{
 			MediaTracker mtTracker = new MediaTracker(this);
 
 			// Wait for image to load.
 			mtTracker.addImage(m_imgImage, 0);
 			mtTracker.waitForAll();
 
 			// Loaded ok?
 			if (mtTracker.isErrorAny())
 			{
 				MsgBox.alert(this, APP_NAME, "Failed to load the image");
 				return;
 			}
 		}
 		catch (InterruptedException e)
 		{ }
 
 		// Display the new image.
 		m_icnImage.setImage(m_imgImage);
 		m_btnConvert.setEnabled(true);
 	}
 
 	/********************************************************************************
 	** "Convert" button event handler.
 	*/
 
 	public void onConvert()
 	{
 		ConvertDlg Dlg = new ConvertDlg(this);
 
 		Dlg.m_rcSrc.x      = 0;
 		Dlg.m_rcSrc.y      = 0;
 		Dlg.m_rcSrc.width  = m_imgImage.getWidth(this);
 		Dlg.m_rcSrc.height = m_imgImage.getHeight(this);
 
 		// Prompt the user for the params.
 		if (Dlg.prompt() == Dlg.OK)
 		{
 			// Clear the existing output.
 			m_ebBytes.setText("");
 
 			// Fetch converion source rectangle.
 			int x = Dlg.m_rcSrc.x;
 			int y = Dlg.m_rcSrc.y;
 			int w = Dlg.m_rcSrc.width;
 			int h = Dlg.m_rcSrc.height;
 
 			// Allocate a buffer for the pixel data.
 			int[] aiPixels = new int[w * h];
 
 			try
 			{
 				// Fetch the pixel data.
 				PixelGrabber oGrabber = new PixelGrabber(m_imgImage, x, y, w, h, aiPixels, 0, w);
 
 				if (!oGrabber.grabPixels())
 				{
 					MsgBox.alert(this, APP_NAME, "Failed to retrive pixel data.");
 					return;
 				}
 			}
 			catch (InterruptedException e)
 			{ }
 
 			// Convert the pixels to hex values.
 			StringBuffer strRow  = new StringBuffer(1024);
 			int			 nPixCnt = Dlg.m_nPerRow;
 
 			// For each row of pixels...
 			for (int i = 0; i < h; i++)
 			{
 				// For each pixel in the row...
 				for (int j = 0; j < w; j++)
 				{
 					// Extract pixel and channels.
 					int nPixel = aiPixels[(i * w) + j];
 //					int nAlpha = (nPixel >> 24) & 0xff;
 //					int nRed   = (nPixel >> 16) & 0xff;
 //					int nGreen = (nPixel >>  8) & 0xff;
 //					int nBlue  = (nPixel      ) & 0xff;
 
 					// Convert to text.
 					String strPixel  = Integer.toHexString(nPixel).toUpperCase();
 					int    nPadChars = 8 - strPixel.length();
 
 					strRow.append("0x");
 
 					// Pad with leading zeros.
 					while (nPadChars-- > 0)
 						strRow.append('0');
 
 					// Append to row.
 					strRow.append(strPixel);
 
 					if (Dlg.m_bCommas)
 						strRow.append(',');
 
 					strRow.append(' ');
 
 					if (--nPixCnt == 0)
 					{
 						// Add row to the output box.
 						strRow.setCharAt(strRow.length()-1, '\n');
 						m_ebBytes.append(strRow.toString());
 
 						strRow.setLength(0);
 						nPixCnt = Dlg.m_nPerRow;
 					}
 				}
 			}
 
 			// Add final row to the output box, if one.
 			if (strRow.length() > 0)
 			{
 				strRow.setCharAt(strRow.length()-1, '\n');
 				m_ebBytes.append(strRow.toString());
 			}
 		}
 	}
 
 	/********************************************************************************
 	** Constants
 	*/
 
 	public static final String APP_NAME = "Img2Src";
 
 	/********************************************************************************
 	** Members.
 	*/
 
 	// Child panels.
 	private Panel		m_pnlParams  = new Panel(new BorderLayout(5, 5));
 	private Panel		m_pnlButtons = new Panel(new GridLayout(1, 0, 5, 5));
 	private Panel		m_pnlData    = new Panel(new BorderLayout(5, 5));
 
 	// Child controls.
 	private TextField	m_ebFileName = new TextField(50);
 	private Button		m_btnLoad    = new Button("Load");
 	private Button		m_btnConvert = new Button("Convert");
 
 	private Icon		m_icnImage   = new Icon();
 	private TextArea	m_ebBytes    = new TextArea(30, 40);
 
 	// Data members.
 	private Image		m_imgImage;
 }
