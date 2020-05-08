 package hci;
 
 import javax.imageio.ImageIO;
 
 import javax.swing.JPanel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Polygon;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.util.ArrayList;
 import javax.swing.JLabel;
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
 	BufferedImage image = null;
 	
 	JPanel labelPanel = null;
 	
 	/**
 	 * list of current polygon's vertices 
 	 */
 	ArrayList<Point> currentPolygon = null;
 	
 	/**
 	 * list of polygons
 	 */
 	ArrayList<ArrayList<Point>> polygonsList = null;
 	
 	/**
 	 * List of Polygons, done properly.
 	 */
 	ArrayList<Polygon> actualPolygonList = null;
 	
 	/**
 	 * UI components
 	 */
 	ArrayList<String> labelList = null;
 	JList labelsBox = null;
 	
 	/**
 	 * The polygon currently selected by the user
 	 */
 	int selectedPolygon = -1;
 	
 	/**
 	 * default constructor, sets up the window properties
 	 */
 	public ImagePanel() {
 		currentPolygon = new ArrayList<Point>();
 		polygonsList = new ArrayList<ArrayList<Point>>();
 		actualPolygonList = new ArrayList<Polygon>();
 		labelList = new ArrayList<String>();
 		labelPanel = new JPanel();
 		labelPanel.setOpaque(true);
 		labelPanel.setLayout(new BorderLayout());
 		JLabel title = new JLabel("Labels:             ");
 		labelPanel.add(title,BorderLayout.NORTH);
 		
 		labelsBox = new JList(labelList.toArray());
 		labelsBox.addListSelectionListener(new ListSelectionListener(){
 			@Override
 			public void valueChanged(ListSelectionEvent e){
 				selectedPolygon = labelsBox.getSelectedIndex();
 				refresh();
 			}
 		});
         
 		labelPanel.add(labelsBox, BorderLayout.CENTER);
 		//buttonBox.setBorder(new EmptyBorder(50, 0, 0, 0) );
 
 		
 		this.setVisible(true);
 
 		Dimension panelSize = new Dimension(800, 600);
 		this.setSize(panelSize);
 		this.setMinimumSize(panelSize);
 		this.setPreferredSize(panelSize);
 		this.setMaximumSize(panelSize);
 		
 		addMouseListener(this);
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
 	
 	public void deleteLabel(){
 		if (labelList.size() > 0 && selectedPolygon >=0){
 			labelList.remove(selectedPolygon);
 			actualPolygonList.remove(selectedPolygon);
 			polygonsList.remove(selectedPolygon);
 			drawLabels();
 			refresh();
 		}
 	}
 	
 	public void editLabel(){
 		int position = labelsBox.getSelectedIndex();
		if (position >= 0){
			addLabel(position);
		}
 	}
 	
 	
 	public void drawLabels() {
 		labelsBox.setListData(labelList.toArray());
 	}
 	
 	/**
 	 * Displays the image
 	 */
 	public void ShowImage() {
 		Graphics g = this.getGraphics();
 		g.clearRect(0, 0, 800, 600);
 		if (image != null) {
 			g.drawImage(
 					image, 0, 0, null);
 		}
 	}
 
 	public void refresh(){
 		paint(this.getGraphics());
 	}
 	
 	@Override
 	public void paint(Graphics g) {
 		super.paint(g);
 		
 		//display image
 		ShowImage();
 		actualPolygonList.clear();
 		//display all the completed polygons
 		for(ArrayList<Point> polygon : polygonsList) {
 			drawPolygon(polygon);
 			finishPolygon(polygon);
 		}
 		
 		//display current polygon
 		drawPolygon(currentPolygon);
 	}
 	
 	/**
 	 * displays a polygon without last stroke
 	 * @param polygon to be displayed
 	 */
 	public void drawPolygon(ArrayList<Point> polygon) {
 		Graphics2D g = (Graphics2D)this.getGraphics();
 		g.setColor(Color.GREEN);
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
 	public void finishPolygon(ArrayList<Point> polygon) {
 		//if there are less than 3 vertices than nothing to be completed
 		if (polygon.size() >= 3) {
 			Point firstVertex = polygon.get(0);
 			Point lastVertex = polygon.get(polygon.size() - 1);
 			
 			Graphics2D g = (Graphics2D)this.getGraphics();
 			g.setColor(Color.GREEN);
 			g.drawLine(firstVertex.getX(), firstVertex.getY(), lastVertex.getX(), lastVertex.getY());
 			
 			Polygon fillShape = new Polygon();
 			for (int i = 0; i < polygon.size(); i++){
 				Point vertex = polygon.get(i);
 				fillShape.addPoint(vertex.getX(), vertex.getY());
 			}
 			if (actualPolygonList.size() == selectedPolygon){
 				g.setColor(new Color(100,100,255,127));
 			} else{
 				g.setColor(new Color(0,255,0,127));
 			}
 			actualPolygonList.add(fillShape);
 			g.fill(fillShape);
 		}
 	}
 	
 	public void addLabel(int index) {
 		String label = null;
 		if ((index < labelList.size()) && (index >= 0)){
 			label = JOptionPane.showInputDialog("Edit label",labelList.get(index));
 			if (label == null){
 				return;
 			}
 			if (label.length() == 0){
 				addLabel(index);
 			} else {
 				labelList.set(index, label);
 			}
 		} else {
 			label = JOptionPane.showInputDialog("Please enter a label");
 			if ((label == null) || (label.length() == 0)){
 				addLabel(-1);
 			}
 			labelList.add(label);
 		}
 		drawLabels();
 	}
 	
 	/**
 	 * moves current polygon to the list of polygons and makes pace for a new one
 	 */
 	public void addNewPolygon() {
 		//finish the current polygon if any
 		if (currentPolygon != null ) {
 			finishPolygon(currentPolygon);
 			polygonsList.add(currentPolygon);
 		}
 		
 		currentPolygon = new ArrayList<Point>();
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		int x = e.getX();
 		int y = e.getY();
 		
 		//check if the cursor's within image area
 		if (x > image.getWidth() || y > image.getHeight()) {
 			//if not do nothing
 			return;
 		}
 		
 		Graphics2D g = (Graphics2D)this.getGraphics();
 		
 		//if the left button than we will add a vertex to polygon
 		if (e.getButton() == MouseEvent.BUTTON1) {
 			g.setColor(Color.GREEN);
 			if (currentPolygon.size() != 0) {
 				Point firstVertex = currentPolygon.get(0);
 				if (closeTo(x, firstVertex.getX(), 5) && closeTo(y, firstVertex.getY(), 5)){
 					addNewPolygon();
 					addLabel(-1);
 					return;
 				}	
 				Point lastVertex = currentPolygon.get(currentPolygon.size() - 1);
 				g.drawLine(lastVertex.getX(), lastVertex.getY(), x, y);
 			}
 			g.fillOval(x-5,y-5,10,10);
 			
 			currentPolygon.add(new Point(x,y));
 			System.out.println(x + " " + y);
 		} 
 		// a right-click in a polygon adds a label
 		if (e.getButton() == MouseEvent.BUTTON3){
 			for (int i = (actualPolygonList.size() -1); i >= 0; i--){
 				if (actualPolygonList.get(i).contains(x, y)){
 					//selectedPolygon = i;
 					labelsBox.setSelectedIndex(i);
 					addLabel(i);
 					break;
 				}
 			}
 			// right-click while drawing to remove lines
 			if (currentPolygon.size() > 0){
 				currentPolygon.remove(currentPolygon.size() - 1);
 				refresh();
 			}
 		}
 	}
 
 	// return true if a is within range of b
 	public boolean closeTo(int a, int b,  int range){
 		if (a <= (b + range) && a >= (b - range)){
 			return true;
 		}else{
 			return false;	
 		}
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent arg0) {
 	}
 
 	@Override
 	public void mouseExited(MouseEvent arg0) {
 	}
 
 	@Override
 	public void mousePressed(MouseEvent arg0) {
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent arg0) {
 	}
 }
