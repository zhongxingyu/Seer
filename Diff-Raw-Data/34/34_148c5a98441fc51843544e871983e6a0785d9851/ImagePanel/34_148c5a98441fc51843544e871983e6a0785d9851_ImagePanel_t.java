 package hci;
 
 import javax.imageio.ImageIO;
 import javax.swing.JPanel;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Hashtable;
 
 import hci.utils.*;
 
 /**
  * Handles image editing panel
  * @author Michal
  *
  */
 public class ImagePanel extends JPanel implements MouseListener {
 	/**
 	 * some java stuff to get rid of warnings
 	 */
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * image to be tagged
 	 */
 	private BufferedImage image = null;
 	
 	/**
 	 * list of current polygon's vertices 
 	 */
 	private ArrayList<Point> currentPolygon = null;
 	
 	/**
 	 * list of polygons
 	 */
 	/* Hash table is now used instead of the arraylist*/
 	//private ArrayList<ArrayList<Point>> polygonsList = null;
 	
 	private Hashtable<String,ArrayList<Point>> polygontable = null;
 	
 	/**
 	 * default constructor, sets up the window properties
 	 */
 	public ImagePanel() {
 		currentPolygon = new ArrayList<Point>();
 		polygontable = new Hashtable<String,ArrayList<Point>>();
 		//polygonsList = new ArrayList<ArrayList<Point>>();
 
 		this.setVisible(true);
 
 		Dimension panelSize = new Dimension(800, 600);
 		this.setSize(panelSize);
 		this.setMinimumSize(panelSize);
 		this.setPreferredSize(panelSize);
 		this.setMaximumSize(panelSize);
 		
 		//addMouseListener(this);
 	}
 	
 	/**
 	 * extended constructor - loads image to be labelled
 	 * @param imageName - path to image
 	 * @throws Exception if error loading the image
 	 */
 	public ImagePanel(String imageName) throws Exception{
 		this();
 		image = ImageIO.read(new File(imageName));
 		if (image.getWidth() > 800 || image.getHeight() > 600) {
 			int newWidth = image.getWidth() > 800 ? 800 : (image.getWidth() * 600)/image.getHeight();
 			int newHeight = image.getHeight() > 600 ? 600 : (image.getHeight() * 800)/image.getWidth();
 			System.out.println("SCALING TO " + newWidth + "x" + newHeight );
 			Image scaledImage = image.getScaledInstance(newWidth, newHeight, Image.SCALE_FAST);
 			image = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
 			image.getGraphics().drawImage(scaledImage, 0, 0, this);
 		}
 	}
     public void createPolygon(){
     	currentPolygon = new ArrayList<Point>();
     }
     
     public ArrayList<Point> get_currentPolygon(){
     	return currentPolygon;
     }
 	/**
 	 * Displays the image
 	 * 
 	 */
 	public void ShowImage() {
 		Graphics g = this.getGraphics();
 		
 		if (image != null) {
 			g.drawImage(
 					image, 0, 0, null);
 		}
 	}
 	
 	@Override
 	public void paint(Graphics g) {
 		super.paint(g);
 		
 		//display image
 		ShowImage();
 		
		//display all the polygons  using information from the hashtable
		Enumeration e = polygontable.keys();
 		while(e.hasMoreElements()){
			displayPolygon((String)e.nextElement(),Color.GREEN);
 		}
 		
 		//display all the completed polygons( this is michals code)
 		//for(ArrayList<Point> polygon : polygonsList) {
 			//drawPolygon(polygon);
 			//finishPolygon(polygon);
 	//	}
 		
 		//display current polygon
 		
 		//drawPolygon(currentPolygon);
 		
 	}
 	
 	//displays the polygon indexed by the key in the hash table
 	public void  displayPolygon(String key, Color color){
 		ArrayList<Point> polygon = (ArrayList<Point>)polygontable.get(key);
 		drawPolygon(polygon,color);
 		finishPolygon(polygon, color);
 	}
 	
 	/**
 	 * displays a polygon without last stroke
 	 * @param polygon to be displayed
 	 */
 	public void drawPolygon(ArrayList<Point> polygon, Color color) {
 		System.out.println("current polygon has been drawn");
 	
 		Graphics2D g = (Graphics2D)this.getGraphics();
 		g.setColor(color);
 		//g.setColor(Color.GREEN);
 		for(int i = 0; i < polygon.size(); i++) {
 			Point currentVertex = polygon.get(i);
 			if (i != 0) {
 				Point prevVertex = polygon.get(i - 1);
 				g.drawLine(prevVertex.getX(), prevVertex.getY(), currentVertex.getX(), currentVertex.getY());
 			}
 			g.fillOval(currentVertex.getX() - 5, currentVertex.getY() - 5, 10, 10);
 		}
 	}
 	
 	/**
 	 * displays last stroke of the polygon (arch between the last and first vertices)
 	 * @param polygon to be finished
 	 */
 	public void finishPolygon(ArrayList<Point> polygon, Color color) {
 		//if there are less than 3 vertices than nothing to be completed
 		if (polygon.size() >= 3) {
 			Point firstVertex = polygon.get(0);
 			Point lastVertex = polygon.get(polygon.size() - 1);
 		
 			Graphics2D g = (Graphics2D)this.getGraphics();
 			g.setColor(color);
 			//g.setColor(Color.GREEN);
 			g.drawLine(firstVertex.getX(), firstVertex.getY(), lastVertex.getX(), lastVertex.getY());
 		}
 	}
 	
 	/**
 	 * moves current polygon to the list of polygons and makes pace for a new one
 	 */
 	public void addNewPolygon(String key) {
 		//finish the current polygon if any
 		if (currentPolygon != null ) {
 			finishPolygon(currentPolygon,Color.GREEN);
 			polygontable.put(key,currentPolygon);
 		}
 		
 		currentPolygon = null;
 	}
 
 	//@Override
 	public void mouseClicked(MouseEvent e) {
 		if (currentPolygon!=null){	
 			
 			int x = e.getX();
 			int y = e.getY();
 		    
 		//check if the cursors with in image area
 				if (x > image.getWidth() || y > image.getHeight()) {
 			//if not do nothing
 					System.out.println("image width is "+ image.getWidth());
 					return;
 				}
 		
 				Graphics2D g = (Graphics2D)this.getGraphics();
 		
 		//if the left button than we will add a vertex to polygon
 				if (e.getButton() == MouseEvent.BUTTON1) {
 				g.setColor(Color.GREEN);
 				if (currentPolygon.size() != 0) {
 					Point lastVertex = currentPolygon.get(currentPolygon.size() - 1);
 					g.drawLine(lastVertex.getX(), lastVertex.getY(), x, y);
 				}
 				g.fillOval(x-5,y-5,10,10);
 			
 				currentPolygon.add(new Point(x,y));
 				System.out.println(x + " " + y);
 			} 
 		}
 	}
 
 	//@Override
 	public void mouseEntered(MouseEvent arg0) {
 	}
 
 	//@Override
 	public void mouseExited(MouseEvent arg0) {
 	}
 
    //	@Override
 	public void mousePressed(MouseEvent arg0) {
 	}
 
 	//@Override
 	public void mouseReleased(MouseEvent arg0) {
 	}
 	
 }
