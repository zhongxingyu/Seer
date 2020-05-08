 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.io.File;
 
 import javax.swing.BorderFactory;
 import javax.swing.JPanel;
 
 /** manages picture contents
  * adds a picture, provides methods to pick another picture
  * set colored border around picture
  * implements zoom functionality
  * let's you drag the picture around
  * 
  * @author Thomas
  * @version 0.3
  * 
  */
 public class ImageHolder extends JPanel implements MouseWheelListener, MouseMotionListener, MouseListener
 {	
 	ImageHolder()
 	{
 		addMouseWheelListener(this);
 		addMouseMotionListener(this);
 		addMouseListener(this);
 	}
 	
 	// picture directory
 	private File myPictureDirectory = new File ("pictures");
 	
 	// decides which (nth) picture from folder is displayed
 	private int whichPicture = 0;
 	
 	// border color
 	private Color borderColor = new Color(255, 0, 0);
 	
 	// zoom factor
 	private double zoom = 1;
 	
 	// drag offset
 	private int mouseClickedPositionX = 0;
 	private int mouseClickedPositionY = 0;
 	private int xOffset = 0;
 	private int yOffset = 0;
 	
 	/**
 	 * reset dragging offset and zoom etc. (image position back to start)
 	 */
 	public void resetImage()
 	{
 		mouseClickedPositionX = 0;
 		mouseClickedPositionY = 0;
 		xOffset = 0;
 		yOffset = 0;
 		zoom = 1;
 	}
 	
 	// multiple setter and getter methods
 	public int getWhichPicture()
 	{
 		return whichPicture;
 	}
 
 	public void setWhichPicture(int whichPicture)
 	{
 		this.whichPicture = whichPicture;
 	}	
 	
 	public double getZoom()
 	{
 		return zoom;
 	}
 
 	public void setZoom(double zoom)
 	{
 		// shouldn't be too small or too large (between 20% and 400%)
 		this.zoom = Math.min(Math.max(0.2, zoom), 4);
 	}
 	
 	public void setDefaultZoom()
 	{
 		this.zoom = 1;
 	}
 	
 	public Color getBorderColor()
 	{
 		return borderColor;
 	}
 
 	public void setBorderColor(Color borderColor)
 	{
 		this.borderColor = borderColor;
 	}
 	
 	public File getMyPictureDirectory()
 	{
 		return myPictureDirectory;
 	}
 
 	public void setMyPictureDirectory(File myPictureDirectory)
 	{
 		this.myPictureDirectory = myPictureDirectory;
 	}
 	
 	/** paint method
 	 * checks layout, space etc
 	 * draws image
 	 */
 	public void paint(Graphics g)
 	{
 		super.paint(g);
 		
 		// int width and height of window
 		int width = getWidth(); 
 		int height = getHeight();
 		
 		// gets the space that is available
 		int minSpace = Math.min(width, height);
 		int side = (int)Math.round(minSpace*zoom);
 		
 		int widthSide = (int)Math.round(width*zoom);
 		int heightSide = (int)Math.round(height*zoom);
 		
 		// calculates the space that is left
 		int restHeight = (height - heightSide)/2;
 		int restWidth = (width - widthSide)/2;
 		
 		// calls specialPaint
 		//specialPaint(g, side, side, restWidth, restHeight, minSpace);
 		
 		
 		// get nth image in Image folder
 		Image image = getImage(whichPicture);
 		//System.out.println(whichPicture);
 				
 		/*int xMargin = (int)Math.round(xStart * zoom);
 		int yMargin = (int)Math.round(yStart * zoom);*/
 				
 		g.drawImage(image, restWidth+1+xOffset, restHeight+1+yOffset, widthSide-2, heightSide-2, this);
 		//setBorder(BorderFactory.createLineBorder(borderColor));
 		g.setColor(borderColor);
 		g.drawRect(restWidth+1 + xOffset, restHeight+1 + yOffset, widthSide-2, heightSide-2);
 	}	
 	
 	/**
 	 * returns nth (starting at 0) image from image directory
 	 * @param nth
 	 * @return Image 
 	 * @see Image
 	 */
 	public Image getImage(int nth)
 	{
		System.out.println("given nth: "+nth);
 		File[] myFiles = myPictureDirectory.listFiles();
 		
 		// count how many pngs are in directory
 		int directoryLength = 0;
 		for(int i = 0; i < myFiles.length; i++)
 		{
 			if (isPicture(myFiles[i].toString()))
 				directoryLength++;
 		}
		//System.out.println("all: "+myFiles.length+ ", only png: "+directoryLength);
 		
 		if(nth < 0)
			nth = directoryLength - 1 + (nth % (directoryLength-1));
 		
 		// to iterate through files
 		int file = 0;
 		
 		// counts nth files, starting at 1
 		int counter = 0;
 		
 		// finds the nth file with .png as ending in folder
 		// I don't like those while(true) ... break; loops...
 		while(true)
 		{
 			if(file >= directoryLength)
 				file = 0;
 			
 			boolean fileIsPicture = isPicture(myFiles[file].toString()); 
 			if(fileIsPicture && (counter >= nth))
 			{
 				break;
 			}
 			
 			
 			// we're at the nth image but it wasn't a png, let's take the next png we find
 			else if (counter == nth)
 			{
 				file++;
 			}
 			// it was a png, but not the right one yet, let's take the next file and keep counting
 			else if(fileIsPicture)
 			{
 				file++;
 				counter++;
 			}
 			// we're not yet at the nth image and it wasn't a png anyway, let's just take the next one
 			else
 			{
 				file++;
 			}
 		}
 		
 		return Toolkit.getDefaultToolkit().getImage(myFiles[file].toString());
 	}
 
 	/**
 	 * checks if file is picture (currently png) by checking the file ending
 	 * @param fileName
 	 * @return boolean
 	 */
 	private boolean isPicture(String fileName)
 	{
 		String imageName = fileName;
 		String imageEnding = imageName.substring(imageName.length()-4, imageName.length());
 		
 		// right image, we're done
 		if (imageEnding.equals(".png")) //|| imageEnding.equals(".jpg"))
 			// we're done, break, return
 			return true;
 		
 		return false;
 	}
 
 	@Override
 	public void mouseWheelMoved(MouseWheelEvent e)
 	{
 		int notches = e.getWheelRotation();
 	    if (notches < 0) {
 	    	setZoom(getZoom() + 0.1);
 	    	repaint();
 	    } else {
 	    	setZoom(getZoom() - 0.1);
 	    	repaint();
 	    }
 	}
 
 	@Override
 	public void mouseDragged(MouseEvent e)
 	{
 		int mouseXDiff = e.getX() - mouseClickedPositionX;
 		int mouseYDiff = e.getY() - mouseClickedPositionY;
 
 		xOffset += mouseXDiff;
 		yOffset += mouseYDiff;
 
 		repaint();
 
 		mouseClickedPositionX = e.getX();
 		mouseClickedPositionY = e.getY();
 	}
 	
 	@Override
 	public void mouseMoved(MouseEvent arg0)
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent arg0)
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent arg0)
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseExited(MouseEvent arg0)
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e)
 	{
 		mouseClickedPositionX = e.getX();
 		mouseClickedPositionY = e.getY();
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent arg0)
 	{
 		// TODO Auto-generated method stub
 		
 	}
 }
