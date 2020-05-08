 package es.eucm.eadventure.editor.gui.otherpanels;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Graphics;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.swing.ImageIcon;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import es.eucm.eadventure.common.data.chapter.Trajectory;
 import es.eucm.eadventure.common.data.chapter.Trajectory.Node;
 import es.eucm.eadventure.common.data.chapter.Trajectory.Side;
 import es.eucm.eadventure.common.gui.TextConstants;
 import es.eucm.eadventure.editor.control.controllers.AssetsController;
 import es.eucm.eadventure.editor.control.controllers.NormalScenePreviewEditionController;
 import es.eucm.eadventure.editor.control.controllers.DataControl;
 import es.eucm.eadventure.editor.control.controllers.ScenePreviewEditionController;
 import es.eucm.eadventure.editor.control.controllers.scene.ActiveAreaDataControl;
 import es.eucm.eadventure.editor.control.controllers.scene.BarrierDataControl;
 import es.eucm.eadventure.editor.control.controllers.scene.ElementReferenceDataControl;
 import es.eucm.eadventure.editor.control.controllers.scene.ExitDataControl;
 import es.eucm.eadventure.editor.control.controllers.scene.InfluenceAreaDataControl;
 import es.eucm.eadventure.editor.control.controllers.scene.NodeDataControl;
 import es.eucm.eadventure.editor.control.controllers.scene.SceneDataControl;
 import es.eucm.eadventure.editor.gui.otherpanels.imageelements.ImageElement;
 import es.eucm.eadventure.editor.gui.otherpanels.imageelements.ImageElementActiveArea;
 import es.eucm.eadventure.editor.gui.otherpanels.imageelements.ImageElementBarrier;
 import es.eucm.eadventure.editor.gui.otherpanels.imageelements.ImageElementExit;
 import es.eucm.eadventure.editor.gui.otherpanels.imageelements.ImageElementInfluenceArea;
 import es.eucm.eadventure.editor.gui.otherpanels.imageelements.ImageElementNode;
 import es.eucm.eadventure.editor.gui.otherpanels.imageelements.ImageElementPlayer;
 import es.eucm.eadventure.editor.gui.otherpanels.imageelements.ImageElementReference;
 
 /**
  * This panel show the scene in different configurations, allowing objects
  * to be moved around, hidden, etc.
  * 
  * @author Eugenio Marchiori
  *
  */
 public class ScenePreviewEditionPanel extends JPanel {
 
 	/**
 	 * Default serialVersionUID
 	 */
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * Key for a general unidentified category
 	 */
 	public static final int CATEGORY_NONE = 0;
 	
 	/**
 	 * Key for the object reference category
 	 */
 	public static final int CATEGORY_OBJECT = 1;
 	
 	/**
 	 * Key for the character reference category
 	 */
 	public static final int CATEGORY_CHARACTER = 2;
 	
 	/**
 	 * Key for the atrezzo object category
 	 */
 	public static final int CATEGORY_ATREZZO = 3;
 	
 	/**
 	 * Key for the barrier category
 	 */
 	public static final int CATEGORY_BARRIER = 4;
 	
 	/**
 	 * Key for the active area category
 	 */
 	public static final int CATEGORY_ACTIVEAREA = 5;
 	
 	/**
 	 * Key for the player reference category
 	 */
 	public static final int CATEGORY_PLAYER = 6;
 	
 	/**
 	 * Key for the exit category
 	 */
 	public static final int CATEGORY_EXIT = 7;
 	
 	/**
 	 * Key for the node category
 	 */
 	public static final int CATEGORY_NODE = 8;
 
 	public static final int CATEGORY_INFLUENCEAREA = 9;
 
 	/**
 	 * Default margin value 
 	 */
 	private static final int MARGIN = 0;
 
 
 	private static final int LIGHT_BORDER = 1;
 
 	private static final int HARD_BORDER = 2;
 	
 	private static final int RESCALE_BORDER = 3;
 
 	private static final int RESCALE_BORDER_ACTIVE = 4;
 
 	private static final int RESIZE_BORDER = 5;
 
 	private static final int RESIZE_BORDER_ACTIVE = 6;
 
 
 	/**
 	 * Hashmap with a list of imageElement for every Integer representing a category
 	 */
 	private HashMap<Integer, List<ImageElement>> elements;
 	
 	/**
 	 * Hashmap with a Boolean indicating if the category represented by the Integer must be displayed
 	 */
 	private HashMap<Integer, Boolean> displayCategory;
 	
 	/**
 	 * Hashmap with a Boolean indicating if the category represented by the Integer must be displayed
 	 */
 	private HashMap<Integer, Boolean> movableCategory;
 	
 	/**
 	 * The trajectory asociated with the scene
 	 */
 	private Trajectory trajectory;
 	
 	/**
 	 * The background image of the scene
 	 */
 	private Image background;
 	
 	/**
 	 * The size ratio of the panel and backgroud image, used to
 	 * fit the full background in the panel
 	 */
 	private double sizeRatio;
 		
 	/**
 	 * The margin left along the x-axis
 	 */
 	private int marginX;
 	
 	/**
 	 * The margin left along the y-axis
 	 */
 	private int marginY;
 	
 	/**
 	 * The width of the background image
 	 */
 	private int backgroundWidth;
 	
 	/**
 	 * The height of the backgound image
 	 */
 	private int backgroundHeight;
 	
 	/**
 	 * Boolean indicating if the user is rescaling
 	 */
 	private boolean rescale;
 
 	/**
 	 * Boolean indicating if the user is resizeing
 	 */
 	private boolean resize;
 	
 	/**
 	 * The first ImageElement selected for tools that need another one
 	 */
 	private ImageElement firstElement;
 
 	private ElementReferenceSelectionListener elementReferenceSelectionListener;
 	
 	/**
 	 * The ScenePreviewEditionController being currently used
 	 */
 	private ScenePreviewEditionController spec;
 	
 	/**
 	 * Image to be used as a backbuffer
 	 */
 	private BufferedImage backBuffer;
 	
 	/**
 	 * The selected element in the panel
 	 */
 	private ImageElement selectedElement;
 	
 	private ImageElementInfluenceArea influenceArea;
 	
 	private boolean fixedSelectedElement;
 	
 	/**
 	 * Panel where the elements are drawn
 	 */
 	private DrawPanel drawPanel;
 
 	/**
 	 * Panel where the checkboxes are shown
 	 */
 	private JPanel checkBoxPanel;
 	
 	/**
 	 * Boolean indicating if the checkboxes should be shown
 	 */
 	private boolean showCheckBoxes = true;
 		
 	private JPanel textEditionPanel;
 	
 	private JTextField posXTextField;
 	
 	private JTextField posYTextField;
 	
 	private JTextField scaleTextField;
 	
 	private JTextField widthTextField;
 	
 	private JTextField heightTextField;
 	
 	private boolean showTextEdition = false;
 	
 	/**
 	 * Default constructor
 	 */
 	public ScenePreviewEditionPanel() {
 		super();
 		elements = new HashMap<Integer, List<ImageElement>>();
 		displayCategory = new HashMap<Integer, Boolean>();
 		movableCategory = new HashMap<Integer, Boolean>();
 		BorderLayout bl = new BorderLayout();
 		bl.setHgap(10);
 		bl.setVgap(10);
 		setLayout(bl);
 		drawPanel = new DrawPanel();
 		add(drawPanel, BorderLayout.CENTER);
 		recreateCheckBoxPanel();
 		recreateTextEditionPanel();
 		spec = new NormalScenePreviewEditionController(this);
 		changeController(spec);
 	}
 	
 	private void recreateTextEditionPanel() {
 		if (textEditionPanel != null)
 			remove(textEditionPanel);
 		if (showTextEdition && selectedElement != null) {
 			textEditionPanel = createTextEditionPanel();
 			add(textEditionPanel, BorderLayout.NORTH);
 		}
 	}
 	
 	private void recreateCheckBoxPanel() {
 		if (checkBoxPanel != null)
 			remove(checkBoxPanel);
 		if (showCheckBoxes) {
 			checkBoxPanel = createCheckBoxPanel();
 			add(checkBoxPanel, BorderLayout.SOUTH);
 		}
 	}
 	
 	/**
 	 * Private class with the logic for the panel where the
 	 * elements are drawn
 	 */
 	private class DrawPanel extends JPanel {
 		/**
 		 * Default serialVersionUID
 		 */
 		private static final long serialVersionUID = 1L;
 
 		public void repaint() {
 			super.repaint();
 			if (getSize().width > 0 && getSize().height > 0) {
 				calculateSize();
 				backBuffer = new BufferedImage(getSize().width, getSize().height, BufferedImage.TYPE_4BYTE_ABGR);
 				paintBackBuffer();
 				flip();
 			}
 		}
 		
 		public void paint(Graphics g) {
 			super.paint(g);
 			if (backBuffer != null) {
 				g.drawImage(backBuffer, marginX, marginY, marginX + backgroundWidth, marginY + backgroundHeight, marginX, marginY, marginX + backgroundWidth, marginY + backgroundHeight, null);
 			}
 		}
 			
 		/**
 		 * Flip the backbuffer
 		 */
 		public void flip() {
 			this.getGraphics().drawImage(backBuffer, marginX, marginY, marginX + backgroundWidth, marginY + backgroundHeight, marginX, marginY, marginX + backgroundWidth, marginY + backgroundHeight, null);
 		}
 	}
 	
 
 	/**
 	 * Constructor with the path to the background image
 	 * 
 	 * @param imagePath path to the background image
 	 */
 	public ScenePreviewEditionPanel(String imagePath) {
 		this();
 		loadBackground(imagePath);
 	}
 	
 	/**
 	 * Add new element to the panel, in a given category
 	 * 
 	 * @param category the category of the element
 	 * @param element said element
 	 */
 	public void addElement(int category, ElementReferenceDataControl element) {
 		Integer key = new Integer(category);
 		addCategory(category, true, true);
 		List<ImageElement> list = elements.get(key);
 		list.add(new ImageElementReference(element));
 		recreateCheckBoxPanel();
 	}
 	
 	/**
 	 * Add the player to the panel
 	 * 
 	 * @param scene the scene's data control
 	 * @param image the image of the player
 	 */
 	public void addPlayer(SceneDataControl scene, Image image){
 		Integer key = new Integer(CATEGORY_PLAYER);
 		addCategory(key, true, true);
 		List<ImageElement> list = elements.get(key);
 		list.add(new ImageElementPlayer(image, scene));
 		recreateCheckBoxPanel();
 	}
 	
 	/**
 	 * Add a barrier to the panel
 	 * 
 	 * @param barrierDataControl the barrier's data control
 	 */
 	public void addBarrier(BarrierDataControl barrierDataControl) {
 		Integer key = new Integer(CATEGORY_BARRIER);
 		addCategory(key, true, false);
 		List<ImageElement> list = elements.get(key);
 		list.add(new ImageElementBarrier(barrierDataControl));
 		recreateCheckBoxPanel();
 	}
 
 	/**
 	 * Add an active area to the panel
 	 * 
 	 * @param activeAreaDataControl The active area's data control
 	 */
 	public void addActiveArea(ActiveAreaDataControl activeAreaDataControl) {
 		Integer key = new Integer(CATEGORY_ACTIVEAREA);
 		addCategory(key, true, true);
 		List<ImageElement> list = elements.get(key);
 		list.add(new ImageElementActiveArea(activeAreaDataControl));
 		recreateCheckBoxPanel();
 	}
 	
 	/**
 	 * Add an exit to the panel
 	 * 
 	 * @param exitDataControl the exit's data control
 	 */
 	public void addExit(ExitDataControl exitDataControl) {
 		Integer key = new Integer(CATEGORY_EXIT);
 		addCategory(key, true, true);
 		List<ImageElement> list = elements.get(key);
 		list.add(new ImageElementExit(exitDataControl));
 		recreateCheckBoxPanel();
 	}
 
 	/**
 	 * Add a node for a trajectory to the panel
 	 * 
 	 * @param nodeDataControl the node's data control
 	 */
 	public void addNode(NodeDataControl nodeDataControl) {
 		Integer key = new Integer(CATEGORY_NODE);
 		addCategory(key, true, false);
 		List<ImageElement> list = elements.get(key);
 		list.add(new ImageElementNode(nodeDataControl));
 		recreateCheckBoxPanel();
 	}
 
 	
 	public void addInfluenceArea(InfluenceAreaDataControl influenceArea) {
 		Integer key = new Integer(CATEGORY_INFLUENCEAREA);
 		addCategory(key, true, true);
 		List<ImageElement> list = elements.get(key);
 		list.clear();
 		ImageElement temp = null;
 		for (Integer key2 : elements.keySet()) {
 			for (ImageElement imageElement : elements.get(key2)) {	
 				if (imageElement.getElementReferenceDataControl() != null &&
 						imageElement.getElementReferenceDataControl() == influenceArea.getElementReferenceDataControl()) {
 					temp = imageElement;
 				}
 			}
 		}
 		this.influenceArea = new ImageElementInfluenceArea(influenceArea, temp);
 		list.add(this.influenceArea);
 	}
 
 	/**
 	 * Add a category to all the hashmaps
 	 * 
 	 * @param key The key of the category
 	 * @param display Boolean indicating if it should be displayed
 	 * @param movable Boolean indicating if the elmenets are movable
 	 */
 	private void addCategory(Integer key, boolean display, boolean movable) {
 		if (!elements.containsKey(key))
 			elements.put(key, new ArrayList<ImageElement>());
 		if (!displayCategory.containsKey(key))
 			displayCategory.put(key, new Boolean(display));
 		if (!movableCategory.containsKey(key))
 			movableCategory.put(key, new Boolean(movable));		
 	}
 
 
 	/**
 	 * Set a new value to trajectory
 	 * @param trajectory the new value of trajectory
 	 */
 	public void setTrajectory(Trajectory trajectory) {
 		this.trajectory = trajectory;
 	}
 	
 	/**
 	 * Remove an element from a given category
 	 * 
 	 * @param category the category of the element
 	 * @param element the element to remove
 	 */
 	public void removeElement(int category, ElementReferenceDataControl element) {
 		Integer key = new Integer(category);
 		List<ImageElement> list = elements.get(key);
 		int index;
 		if (list!= null){
 			for ( index=0; index < list.size();index++)	
 					if (list.get(index).getElementReferenceDataControl().equals(element))
 						break;
 			if (index>=0){
 			list.remove(index);
 				
 		}
 		}
 		
 		repaint();
 	}
 	
 	/**
 	 * Set a category to be display or hidden
 	 * 
 	 * @param category the category
 	 * @param display boolean indicating if the category should be displayed
 	 */
 	public void setDisplayCategory(int category, boolean display) {
 		Integer key = new Integer(category);
 		displayCategory.put(key, new Boolean(display));	
 		if (!elements.containsKey(key))
 			elements.put(key, new ArrayList<ImageElement>());
 		repaint();
 	}
 	
 	/**
 	 * Set a category to be movable or fixed
 	 * 
 	 * @param category the category
 	 * @param movable boolean indicating if the category should be movable
 	 */
 	public void setMovableCategory(int category, boolean movable) {
 		Integer key = new Integer(category);
 		movableCategory.put(key, new Boolean(movable));
 		if (!elements.containsKey(key))
 			elements.put(key, new ArrayList<ImageElement>());
 	}
 
 	/**
 	 * Set a single movable element, only this object will be moved
 	 * 
 	 * @param element The movable element
 	 */
 	public void setMovableElement(ElementReferenceDataControl element) {
 		selectedElement = new ImageElementReference(element);
 		for (Integer key : movableCategory.keySet()) {
 			movableCategory.put(key, new Boolean(false));
 		}
 		recreateTextEditionPanel();
 	}
 	
 	/**
 	 * Set the background image
 	 * 
 	 * @param background the background image
 	 */
 	public void setBackground(Image background) {
 		this.background = background;
 		repaint();
 	}
 	
 	public void repaint() {
 		super.repaint();
 	}
 	
 	public void paint(Graphics g) {
 		super.paint(g);
 	}
 		
 	/**
 	 * Flip the backbuffer
 	 */
 	public void flip() {
 		drawPanel.flip();
 	}
 	
 	/**
 	 * Paint the components to the backbuffer
 	 */
 	public void paintBackBuffer() {
 		Graphics g = backBuffer.getGraphics();
 		
 		if (background != null) {
 			paintRelativeImage( g, background, background.getWidth(null)/2, background.getHeight(null), 1);
 		} else {
 	    	ImageIcon icon = new ImageIcon("img/icons/noImageFrame.png"); 
 	    	Image image;
 	    	if (icon != null && icon.getImage() != null)
 	    		image = icon.getImage();
 	    	else
 	    		image = new BufferedImage(100,120,BufferedImage.TYPE_3BYTE_BGR);
 			paintRelativeImage( g, image, image.getWidth(null)/2, image.getHeight(null), 1);
 		}
 		
 		if (influenceArea != null)
 			influenceArea.recreateImage();
 		
 		List<ImageElement> elementsToDraw = new ArrayList<ImageElement>();
 				
 		for (Integer key : displayCategory.keySet()) {
 			if (displayCategory.get(key)) {
 				for (ImageElement imageElement : elements.get(key)) {
 					elementsToDraw.add(imageElement);
 				}
 			}
 		}
 		
 		Collections.sort(elementsToDraw);
 		
 		for (ImageElement imageElement : elementsToDraw) {
 			paintRelativeImage( g, imageElement.getImage(), imageElement.getX(), imageElement.getY(), imageElement.getScale());
 		}
 		
 		if (spec.getUnderMouse() != null) {
 			paintBorders(g, spec.getUnderMouse(), LIGHT_BORDER);
 		}
 		if (selectedElement != null) {
 			paintBorders(g, selectedElement, HARD_BORDER);
 			if (selectedElement.canRescale()) {
 				if (rescale)
 					paintBorders(g, selectedElement, RESCALE_BORDER_ACTIVE);
 				else
 					paintBorders(g, selectedElement, RESCALE_BORDER);
 			}
 			if (selectedElement.canResize()) {
 				if (resize)
 					paintBorders(g, selectedElement, RESIZE_BORDER_ACTIVE);
 				else
 					paintBorders(g, selectedElement, RESIZE_BORDER);
 			}
 		}
 		if (influenceArea != null) {
 			if (resize)
 				paintBorders(g, influenceArea, RESIZE_BORDER_ACTIVE);
 			else
 				paintBorders(g, influenceArea, RESIZE_BORDER);
 		}
 		paintTrajectory(g);
 	}
 
 	/**
 	 * Paint borders to an element
 	 * 
 	 * @param g The graphics where to paint
 	 * @param element The element that needs borders
 	 * @param border_type The type of the borders
 	 */
 	private void paintBorders(Graphics g, ImageElement element,
 			int border_type) {
 		
 		int x = (int) ((element.getX() - element.getImage().getWidth(null) *element.getScale() / 2)* sizeRatio) + marginX;
 		int y = (int) ((element.getY() - element.getImage().getHeight(null) * element.getScale()) * sizeRatio) + marginY;
 		int width = (int) (element.getImage().getWidth(null) * element.getScale() * sizeRatio);
 		int height = (int) (element.getImage().getHeight(null) * element.getScale() * sizeRatio);
 		
 		if (border_type == LIGHT_BORDER) {
 			Color color = g.getColor();
 			g.setColor(Color.RED);
 			g.drawRect(x, y, width, height);
 			g.setColor(color);
 			g.drawImage(element.getImage(), x, y, width, height, null);
 		} else if (border_type == HARD_BORDER) {
 			Color color = g.getColor();
 			g.setColor(Color.RED);
 			g.fillRect(x - 4, y - 4, width + 8, 4);
 			g.fillRect(x - 4, y - 4, 4, height + 4);
 			g.fillRect(x + width, y, 4, height + 4);
 			g.fillRect(x - 4, y + height, width + 4, 4);
 			g.setColor(color);
 			g.drawImage(element.getImage(), x, y, width, height, null);
 		} else if (border_type == RESCALE_BORDER) {
 			Color color = g.getColor();
 			g.setColor(Color.GREEN);
 			g.drawRect(x + width - 8, y - 8, 16, 16);
 			g.setColor(color);
 		} else if (border_type == RESCALE_BORDER_ACTIVE) {
 			Color color = g.getColor();
 			g.setColor(Color.BLUE);
 			g.drawRect(x + width - 8, y - 8, 16, 16);
 			g.drawRect(x + width - 7, y - 7, 14, 14);
 			g.setColor(color);
 		} else if (border_type == RESIZE_BORDER) {
 			Color color = g.getColor();
 			g.setColor(Color.GREEN);
 			g.drawRect(x + width -8, y + height - 8, 16, 16);
 			g.setColor(color);
 		} else if (border_type == RESIZE_BORDER_ACTIVE) {
 			Color color = g.getColor();
 			g.setColor(Color.BLUE);
 			g.drawRect(x + width - 8, y + height - 8, 16, 16);
 			g.drawRect(x + width - 7, y + height - 7, 14, 14);
 			g.setColor(color);
 		}
 		
 	}
 
 	/**
 	 * Load the background image form a path
 	 * 
 	 * @param imagePath the path to the background image
 	 */
 	public void loadBackground( String imagePath ) {
 		if( imagePath != null && imagePath.length( ) > 0 )
 			background = AssetsController.getImage( imagePath );
 		calculateSize();
 		repaint();
 	}
 
 	/**
 	 * Calculate the size of the images, depending on the size of the
 	 * panel and the size of the background image
 	 */
 	private synchronized void calculateSize() {
 
 		int backgroundWidth2 = 800;
 		int backgroundHeight2 = 600;
 		if (background != null) {
 			backgroundWidth2 = background.getWidth(null);
 			backgroundHeight2 = background.getHeight(null);
 		}
 		
 		if( background != null && drawPanel.getWidth( ) > 0 && drawPanel.getHeight( ) > 0 ) {
 			double panelRatio = (double) drawPanel.getWidth( ) / (double) drawPanel.getHeight( );
 			double imageRatio = (double) backgroundWidth2 / (double) backgroundHeight2;
 			int width, height;
 			marginX = MARGIN;
 			marginY = MARGIN;
 			
 			if( panelRatio <= imageRatio ) {
 				int panelWidth = drawPanel.getWidth( ) - MARGIN * 2;
 				width = panelWidth;
 				height = (int) (panelWidth / imageRatio);
 			}
 
 			else {
 				int panelHeight = drawPanel.getHeight( ) - MARGIN * 2;
 				width = (int) ( panelHeight * imageRatio );
 				height = panelHeight;
 			}
 			
 			marginX = (drawPanel.getWidth() - width) / 2;
 			marginY = (drawPanel.getHeight() - height) / 2;
 			backgroundWidth = width;
 			backgroundHeight = height;
 
 			sizeRatio = (double) width / (double) backgroundWidth2;
 		}
 	}
 
 	/**
 	 * Paints an rescaled image in the given graphics.
 	 * 
 	 * @param g
 	 *            Graphics to paint
 	 * @param image
 	 *            Image to be painted
 	 * @param x
 	 *            Absolute X position of the center of the image
 	 * @param y
 	 *            Absolute Y position of the bottom of the image
 	 */
 	protected void paintRelativeImage( Graphics g, Image image, int x, int y, double scale) {
 		if( image != null ) {
 			int width = (int) ( image.getWidth( null ) * sizeRatio );
 			int height = (int) ( image.getHeight( null ) * sizeRatio );
 
 			int posX = marginX + (int) (x * sizeRatio - width  * scale / 2);
 			int posY = marginY + (int) (y * sizeRatio - height * scale);
 
 			g.drawImage( image, posX, posY, (int) (width * scale), (int) (height * scale), null );
 		}
 	}
 
 	
 	/**
 	 * Paints a rescaled line form and to the given points
 	 * 
 	 * @param g The graphics where the line is drawn
 	 * @param x1 The absolute x of the start point
 	 * @param y1 The absolute y of the start point
 	 * @param x2 The absolute x of the end point
 	 * @param y2 The absolute y of the end point
 	 */
 	protected void drawRelativeLine( Graphics g, int x1, int y1, int x2, int y2) {
 		int posX1 = marginX + (int) (x1 * sizeRatio);
 		int posY1 = marginY + (int) (y1 * sizeRatio);
 		int posX2 = marginX + (int) (x2 * sizeRatio);
 		int posY2 = marginY + (int) (y2 * sizeRatio);
 		g.drawLine(posX1, posY1, posX2, posY2);
 	}
 
 	/**
 	 * Draw the sides of the trajectory in the graphics component
 	 * 
 	 * @param g the graphics component where to draw
 	 */
 	private void paintTrajectory(Graphics g) {
 		Integer key = new Integer(CATEGORY_NODE);
 		if (trajectory == null || !(displayCategory.get(key) != null ? displayCategory.get(key) : false ))
 			return;
 		
 		for (Side side : trajectory.getSides()) {
 			Node start = trajectory.getNodeForId(side.getIDStart());
 			Node end = trajectory.getNodeForId(side.getIDEnd());
 			drawRelativeLine(g, start.getX(), start.getY(), end.getX(), end.getY());
 		}
 		
 		if (firstElement != null) {
 			int mouseX = (int) getMousePosition().getX();
 			int mouseY = (int) getMousePosition().getY();
 			int x = (int) ((mouseX - marginX) / sizeRatio);
 			int y = (int) ((mouseY - marginY) / sizeRatio);
 			drawRelativeLine(g, firstElement.getX(), firstElement.getY() - 10, x, y);
 		}
 	}
 	
 	/**
 	 * Recreate the image of an element
 	 * 
 	 * @param element The element to be recrated
 	 */
 	public void recreateElement(ElementReferenceDataControl element) {
 		for (Integer key : elements.keySet()) {
 			for (ImageElement imageElement : elements.get(key)) {	
 				if (imageElement.getElementReferenceDataControl() != null &&
 						imageElement.getElementReferenceDataControl() == element) {
 					imageElement.recreateImage();
 				}
 			}
 		}
 		if (selectedElement != null && selectedElement.getElementReferenceDataControl() != null &&
 				selectedElement.getElementReferenceDataControl() == element) {
 			selectedElement.recreateImage();
 		}
 	}
 
 	/**
 	 * Get the movable element at position (x,y)
 	 * 
 	 * @param x the x-axis value
 	 * @param y the y-axis value
 	 * @return The movable ImageElement at (x,y)
 	 */
 	public ImageElement getMovableElement(int x, int y) {
 		if (fixedSelectedElement) {
 			double scale = selectedElement.getScale();
 			int minX = (int) (selectedElement.getX() - selectedElement.getImage().getWidth(null) * scale / 2);
 			int minY = (int) (selectedElement.getY() - selectedElement.getImage().getHeight(null) * scale);
 			int maxX = (int) (minX + selectedElement.getImage().getWidth(null) * scale);
 			int maxY = (int) (minY + selectedElement.getImage().getHeight(null) * scale);
 			if (x > minX && x < maxX && y > minY && y < maxY && !selectedElement.transparentPoint(x - minX, y - minY)) {
 				return selectedElement;
 			}
 		}
 		for (Integer key : movableCategory.keySet()) {
 			if (movableCategory.get(key) && (displayCategory.get(key) != null ? displayCategory.get(key) : false)) {
 				for (ImageElement imageElement : elements.get(key)) {
 					double scale = imageElement.getScale();
 					double width = imageElement.getWidth();
 					double height = imageElement.getHeight();
 					int minX = (int) (imageElement.getX() - width * scale / 2);
 					int minY = (int) (imageElement.getY() - height * scale);
 					int maxX = (int) (minX + width * scale);
 					int maxY = (int) (minY + height * scale);
 					if (x > minX && x < maxX && y > minY && y < maxY && !imageElement.transparentPoint(x - minX, y - minY)) {
 						return imageElement;
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Return the rescaleable element in the position x,y
 	 * 
 	 * @param x Position along the x-axis
 	 * @param y Position along the y-axis
 	 * @return The imageElement found or null
 	 */
 	public ImageElement getRescaleElement(int x, int y) {
 		if (selectedElement == null || !selectedElement.canRescale())
 			return null;
 		int x_image = (int) ((selectedElement.getX() + selectedElement.getImage().getWidth(null) * selectedElement.getScale() / 2));
 		int y_image = (int) ((selectedElement.getY() - selectedElement.getImage().getHeight(null) * selectedElement.getScale()));
 
 		int margin = (int) (8.0f / getSizeRatio());
 		if (x > x_image - margin &&
 				x < x_image + margin &&
 				y > y_image - margin &&
 				y < y_image + margin) {
 			return selectedElement;
 		}
 		return null;
 	}
 
 	/**
 	 * Return the resizable element in the position x,y
 	 * 
 	 * @param x Position along the x-axis
 	 * @param y Position along the y-axis
 	 * @return The imageElement found or null
 	 */
 	public ImageElement getResizeElement(int x, int y) {
 		ImageElement tempElement = selectedElement;
 		if (influenceArea != null)
 			tempElement = influenceArea;
 		else
 			tempElement = selectedElement;
 		if (tempElement == null || !tempElement.canResize())
 			return null;
 		int x_image = (int) ((tempElement.getX() + tempElement.getImage().getWidth(null) * tempElement.getScale() / 2));
 		int y_image = (int) ((tempElement.getY() - tempElement.getImage().getHeight(null) * tempElement.getScale()));
 		int height = (int) ((tempElement.getImage().getHeight(null)* tempElement.getScale()));
 		
 		int margin = (int) (8.0f / getSizeRatio());
 		if (x > x_image - margin &&
 				x < x_image + margin &&
 				y > y_image + height - margin &&
 				y < y_image + height + margin) {
 			return tempElement;
 		}
 		return null;
 	}
 
 	/**
 	 * Changes the current selectedElement created from a ElementReferenceDataControl.
 	 * 
 	 * @param erdc
 	 * 				The new ElementReferenceDataControl
 	 */
 	public void setSelectedElement(ElementReferenceDataControl erdc){
 		this.selectedElement = new ImageElementReference(erdc);
 	}
 	
 	/**
 	 * Changes the current selectedElement created from a ElementReferenceDataControl.
 	 * 
 	 * @param erdc
 	 * 				The new ElementReferenceDataControl
 	 */
 	public void setSelectedElement(DataControl erdc,Image image, SceneDataControl sceneDataControl){
 		if (erdc!=null)
 			this.selectedElement = new ImageElementReference((ElementReferenceDataControl)erdc);
 		else 
 			this.selectedElement = new ImageElementPlayer(image,sceneDataControl);
 	}
 	
 	public void setSelectedElement(ImageElement imageElement) {
 		this.selectedElement = imageElement;
 		if (elementReferenceSelectionListener != null){
 			if (selectedElement != null)
 				elementReferenceSelectionListener.elementReferenceSelected(selectedElement.getLayer());
 			else
 				elementReferenceSelectionListener.elementReferenceSelected(-1);
 		}
 		recreateTextEditionPanel();
 	}
 	
 	public void setSelectedElement(ActiveAreaDataControl activeAreaDataControl) {
 		this.setSelectedElement(new ImageElementActiveArea(activeAreaDataControl));
 	}
 
 	public void setSelectedElement(ExitDataControl exitDataControl) {
 		this.setSelectedElement(new ImageElementExit(exitDataControl));
 	}
 	
 	public void setSelectedElement(BarrierDataControl barrierDataControl) {
 		this.setSelectedElement(new ImageElementBarrier(barrierDataControl));
 	}
 	
 	/**
 	 * Changes the element selection listener that captures the event
 	 * 
 	 * @param elementReferenceSelectionListener
 	 * 				the new element reference selection listener
 	 */
 	public void setElementReferenceSelectionListener(
 			ElementReferenceSelectionListener elementReferenceSelectionListener) {
 		this.elementReferenceSelectionListener = elementReferenceSelectionListener;
 	}
 	
 	/**
 	 * Recreates the images of all elements in a category
 	 * 
 	 * @param category the category of the elements to recreate
 	 */
 	public void recreateElements(int category) {
 		Integer key = new Integer(category);
 		for (ImageElement imageElement : elements.get(key)) {
 			imageElement.recreateImage();
 		}		
 	}
 
 	/**
 	 * Returns the value of sizeRatio
 	 * 
 	 * @return the value of sizeRatio
 	 */
 	public double getSizeRatio() {
 		return sizeRatio;
 	}
 
 	/**
 	 * Returns the value of marginX
 	 * 
 	 * @return the value of marginX
 	 */
 	public int getMarginX() {
 		return marginX;
 	}
 	
 	/**
 	 * Returns the value of marginY
 	 * 
 	 * @return the value of marginY
 	 */
 	public int getMarginY() {
 		return marginY;
 	}
 	
 	/**
 	 * Returns the selected image element
 	 * 
 	 * @return the selected image element
 	 */
 	public ImageElement getSelectedElement() {
 		return selectedElement;
 	}
 	
 	/**
 	 * Returns the value of rescale
 	 * @return the value of rescale
 	 */
 	public boolean isRescale() {
 		return rescale;
 	}
 	
 	/**
 	 * Set the value of rescale
 	 * @param rescale the new value of rescale
 	 */
 	public void setRescale(boolean rescale) {
 		if (rescale) {
 			this.setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
 		} else if (!resize){
 			this.setCursor(Cursor.getDefaultCursor());
 		}
 		this.rescale = rescale;
 	}
 	
 	/**
 	 * Set a new mouse controller for the ScenePreviewEditiorPanel
 	 * 
 	 * @param controller the new controller
 	 */
 	public void changeController(ScenePreviewEditionController controller) {
 		if (spec != null) {
 			drawPanel.removeMouseListener(spec);
 			drawPanel.removeMouseMotionListener(spec);
 		}
 		spec = controller;
 		drawPanel.addMouseListener(controller);
 		drawPanel.addMouseMotionListener(controller);
 	}
 
 	/**
 	 * Remove a element form the hashmaps
 	 * 
 	 * @param category The category of the element
 	 * @param imageElement The element to be removed
 	 */
 	public void removeElement(int category, ImageElement imageElement) {
 		Integer key = new Integer(category);
 		List<ImageElement> list = elements.get(key);
 		list.remove(imageElement);
 	}
 	
 	/**
 	 * Set the value of firstElement
 	 * 
 	 * @param firstElement the new value of firstElement
 	 */
 	public void setFirstElement(ImageElement firstElement) {
 		this.firstElement = firstElement;
 	}
 	
 	/**
 	 * Returns the value of firstElement
 	 * @return the value of firstElement
 	 */
 	public ImageElement getFirstElement() {
 		return firstElement;
 	}
 
 	/**
 	 * Returns the value of resize
 	 * @return the value of resize
 	 */
 	public boolean isResize() {
 		return resize;
 	}
 	
 	/**
 	 * Set a new value to resize
 	 * @param resize the new value of resize
 	 */
 	public void setResize(boolean resize) {
 		if (resize) {
 			this.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
 		} else if (!rescale){
 			this.setCursor(Cursor.getDefaultCursor());
 		}
 		this.resize = resize;
 	}
 	
 	public JPanel createCheckBoxPanel() {
 		JPanel checkBoxPanel = new JPanel();
 		
 		List<JCheckBox> checkBoxList = new ArrayList<JCheckBox>();
 		for (Integer key : displayCategory.keySet()) {
 			JCheckBox newCheckBox = null;
 			if (elements.get(key).size() > 0) {
 				newCheckBox = createCheckBox(key);
 			}
 			if (newCheckBox != null)
 				checkBoxList.add(newCheckBox);
 		}
 		
 		checkBoxPanel.setLayout(new GridLayout(2,0));
 		checkBoxPanel.setAutoscrolls(true);
 		for (JCheckBox checkBox : checkBoxList)
 			checkBoxPanel.add(checkBox);
 		
 		
 		return checkBoxPanel;
 	}
 	
 	private JCheckBox createCheckBox(final Integer category) {
 		String title = null;
 		switch (category.intValue()) {
 		case CATEGORY_ACTIVEAREA:
 			title = TextConstants.getText("SPEP.ShowActiveAreas");
 			break;
 		case CATEGORY_ATREZZO:
 			title = TextConstants.getText("SPEP.ShowAtrezzo");
 			break;
 		case CATEGORY_BARRIER:
 			title = TextConstants.getText("SPEP.ShowBarriers");
 			break;
 		case CATEGORY_CHARACTER:
 			title = TextConstants.getText("SPEP.ShowCharacterReferences");
 			break;
 		case CATEGORY_EXIT:
 			title = TextConstants.getText("SPEP.ShowExits");
 			break;
 		case CATEGORY_NONE:
 			title = TextConstants.getText("SPEP.ShowUncategorized");
 			break;
 		case CATEGORY_OBJECT:
 			title = TextConstants.getText("SPEP.ShowObjectReferences");
 			break;
 		case CATEGORY_PLAYER:
 			title = TextConstants.getText("SPEP.ShowPlayer");
 			break;
 		case CATEGORY_NODE:
 			title = TextConstants.getText("SPEP.ShowTrajectory");
 			break;
 		default:
 		}
 		if (title == null)
 			return null;
 		
 		JCheckBox temp = new JCheckBox(title);
 		temp.setSelected(displayCategory.get(category));
 		temp.addChangeListener(new ChangeListener() {

 			public void stateChanged(ChangeEvent arg0) {
 				boolean isSelected = ((JCheckBox) arg0.getSource()).isSelected();
 				displayCategory.put(category, new Boolean(isSelected));
 				ScenePreviewEditionPanel.this.paintBackBuffer();
 				ScenePreviewEditionPanel.this.flip();
 			}
 		});
 		return temp;
 	}
 	
 	public void updateTextEditionPanel() {
 		if (textEditionPanel != null) {
 			if (posXTextField != null)
 				posXTextField.setText("" + selectedElement.getX());
 			if (posYTextField != null)
 				posYTextField.setText("" + selectedElement.getY());
 			if (scaleTextField != null)
 				scaleTextField.setText("" + selectedElement.getScale());
 			if (widthTextField != null)
 				widthTextField.setText("" + selectedElement.getWidth());
 			if (heightTextField != null)
 				heightTextField.setText("" + selectedElement.getHeight());
 		}
 	}
 	
 	public JPanel createTextEditionPanel() {
 		JPanel textInputPanel = new JPanel();
 		textInputPanel.add(new JLabel(TextConstants.getText("SPEP.XCoordinate")));
 		posXTextField = new JTextField(4);
 		posXTextField.setText("" + selectedElement.getX());
 		posXTextField.addKeyListener(new KeyListener() {
 			public void keyPressed(KeyEvent arg0) {
 			}
 			
 			public void keyReleased(KeyEvent arg0) {
 				int value;
 				try {
 					value = Integer.parseInt(posXTextField.getText());
 				} catch (Exception e) {
 					value = 0;
 				}
 				int y = selectedElement.getY();
 				selectedElement.changePosition(value, y);
 				ScenePreviewEditionPanel.this.paintBackBuffer();
 				ScenePreviewEditionPanel.this.flip();
 			}
 			
 			public void keyTyped(KeyEvent arg0) {
 			}
 		});
 		textInputPanel.add(posXTextField);
 
 		textInputPanel.add(new JLabel("   " + TextConstants.getText("SPEP.YCoordinate")));
 		posYTextField = new JTextField(4);
 		posYTextField.setText("" + selectedElement.getY());
 		posYTextField.addKeyListener(new KeyListener() {
 			public void keyPressed(KeyEvent arg0) {
 			}
 			
 			public void keyReleased(KeyEvent arg0) {
 				int value;
 				try {
 					value = Integer.parseInt(posYTextField.getText());
 				} catch (Exception e) {
 					value = 0;
 				}
 				int x = selectedElement.getX();
 				selectedElement.changePosition(x, value);
 				ScenePreviewEditionPanel.this.paintBackBuffer();
 				ScenePreviewEditionPanel.this.flip();
 			}
 			
 			public void keyTyped(KeyEvent arg0) {
 			}
 		});
 		textInputPanel.add(posYTextField);
 		
 		
 		if (selectedElement.canRescale()) {
 			textInputPanel.add(new JLabel("   " + TextConstants.getText("SPEP.Scale")));
 			scaleTextField = new JTextField(8);
 			scaleTextField.setText("" + selectedElement.getScale());
 			scaleTextField.addKeyListener(new KeyListener() {
 				public void keyPressed(KeyEvent arg0) {
 				}
 				
 				public void keyReleased(KeyEvent arg0) {
 					float value;
 					try {
 						value = Float.parseFloat(scaleTextField.getText());
 					} catch (Exception e) {
 						value = 1.0f;
 					}
 					selectedElement.setScale(value);
 					ScenePreviewEditionPanel.this.paintBackBuffer();
 					ScenePreviewEditionPanel.this.flip();
 				}
 				
 				public void keyTyped(KeyEvent arg0) {
 				}
 			});
 			textInputPanel.add(scaleTextField);
 		} else {
 			scaleTextField = null;
 		}
 
 		if (selectedElement.canResize()) {
 			textInputPanel.add(new JLabel("   " + TextConstants.getText("SPEP.Width")));
 			widthTextField = new JTextField(4);
 			widthTextField.setText("" + selectedElement.getWidth());
 			widthTextField.addKeyListener(new KeyListener() {
 				public void keyPressed(KeyEvent arg0) {
 				}
 				
 				public void keyReleased(KeyEvent arg0) {
 					int value;
 					try {
 						value = Integer.parseInt(widthTextField.getText());
 					} catch (Exception e) {
 						value = 20;
 					}
 					int height = selectedElement.getHeight();
 					selectedElement.changeSize(value, height);
 					ScenePreviewEditionPanel.this.paintBackBuffer();
 					ScenePreviewEditionPanel.this.flip();
 				}
 				
 				public void keyTyped(KeyEvent arg0) {
 				}
 			});
 			textInputPanel.add(widthTextField);
 
 			textInputPanel.add(new JLabel("   " + TextConstants.getText("SPEP.Height")));
 			heightTextField = new JTextField(4);
 			heightTextField.setText("" + selectedElement.getHeight());
 			heightTextField.addKeyListener(new KeyListener() {
 				public void keyPressed(KeyEvent arg0) {
 				}
 				
 				public void keyReleased(KeyEvent arg0) {
 					int value;
 					try {
 						value = Integer.parseInt(heightTextField.getText());
 					} catch (Exception e) {
 						value = 20;
 					}
 					int width = selectedElement.getWidth();
 					selectedElement.changeSize(width, value);
 					ScenePreviewEditionPanel.this.paintBackBuffer();
 					ScenePreviewEditionPanel.this.flip();
 				}
 				
 				public void keyTyped(KeyEvent arg0) {
 				}
 			});
 			textInputPanel.add(heightTextField);
 		} else {
 			widthTextField = null;
 			heightTextField = null;
 		}
 
 		return textInputPanel;
 	}
 
 	public void setFixedSelectedElement(boolean fixedSelectedElement) {
 		this.fixedSelectedElement = fixedSelectedElement;
 	}
 	
 	public boolean getFixedSelectedElement() {
 		return fixedSelectedElement;
 	}
 
 
 	public ImageElement getInfluenceArea() {
 		return influenceArea;
 	}
 	
 	public void setShowCheckBoxes(boolean showCheckBoxes) {
 		this.showCheckBoxes = showCheckBoxes;
 		recreateCheckBoxPanel();
 	}
 	
 	public void setShowTextEdition(boolean textEdition) {
 		this.showTextEdition = textEdition;
 	}
 
 }
