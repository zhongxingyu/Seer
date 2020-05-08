 package GUI;
 
 import java.awt.*;
 
 import java.util.*;
 import java.awt.event.*;
 
 import javax.swing.*;
 
 import whiteboard.Backend;
 import networking.Networking;
 
 import boardnodes.*;
 /**
  * 
  * @author bverch
  * 
  * 		This is the main Whiteboard Panel. It basically is the class where all of the other aspects
  * of the project come together. The suggest and chat box will be added here, and 
  * 
  * 
  * 
  * 
  */
 public class WhiteboardPanel extends JPanel{
 	/**
 	 * 
 	 */
 	public static int UIDCounter = 0;
 	public static final int STYLED = 1;
 	public static final int SCRIBBLE = 2;
 	public static final int PATH = 3;
 	private int _lastAdded;
 	private BoardPathType _lastPathType;
 	private ArrayList<BoardElt> _elements;
 	private Dimension _panelSize;
 	private boolean _contIns;
 	private Backend _backend;
 	public ViewportDragScrollListener _mouseListener;
 	
 	private Point _addLocation; //the location you should add the next BoardElt to
 
 	private JPopupMenu _rightClickMenu; //the options when a user right-clicks
 
 	public WhiteboardPanel(){
 		super();
 		_lastAdded = 0;
 		_backend = new Backend(this);
 		_backend._mouseListener = _mouseListener;
 		this.setLayout(null);
 		this.setVisible(true);
 		this.setBackground(Color.GRAY);
 		_contIns = true;
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		_rightClickMenu = initPopupMenu();
 	}
 	
 	public Backend getBackend() {
 		return _backend;
 	}
 	
 	public void displayContextMenu(Point display) {
 		_addLocation = display;
 		_rightClickMenu.show(this, display.x, display.y);
 	}
 	
 	//initialize the right-click menu to allow for adding of nodes
 	private JPopupMenu initPopupMenu() {
 		JPopupMenu popup = new JPopupMenu("Context Menu");
 		JMenuItem styledNodeMenuItem = new JMenuItem("Add Styled Node");
 		styledNodeMenuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				newElt(BoardEltType.STYLED, BoardPathType.NORMAL);
 			}
 		});
 		popup.add(styledNodeMenuItem);
 		
 		JMenuItem drawNodeMenuItem = new JMenuItem("Add Scribble Node");
 		drawNodeMenuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				newElt(BoardEltType.SCRIBBLE, BoardPathType.NORMAL);
 			}
 		});
 		popup.add(drawNodeMenuItem);
 		popup.addSeparator();
 		JMenuItem addPathItem = new JMenuItem("Add Path");
 		addPathItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				newElt(BoardEltType.PATH, BoardPathType.NORMAL);
 			}
 		});
 		popup.add(addPathItem);
 		
 		JMenuItem dottedPathItem = new JMenuItem("Add Dotted Path");
 		dottedPathItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				newElt(BoardEltType.PATH, BoardPathType.DOTTED);
 			}
 		});
 		popup.add(dottedPathItem);
 		
 		JMenuItem arrowPathItem = new JMenuItem("Add Arrow");
 		arrowPathItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				newElt(BoardEltType.PATH, BoardPathType.ARROW);
 			}
 		});
 		popup.add(arrowPathItem);
 		
 		JMenuItem dottedArrowPathItem = new JMenuItem("Add Dotted Arrow");
 		dottedArrowPathItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				newElt(BoardEltType.PATH, BoardPathType.DOTTED_ARROW);
 			}
 		});
 		popup.add(dottedArrowPathItem);
 		
 		return popup;
 	}
 	
 	/**
 	 * 
 	 * @param obtrusion		the shape that may or may not extend beyond the bounds of this panel
 	 */
 	public void extendPanel(Rectangle obtrusion){
 		int panelWidth = this.getSize().width;
 		int panelHeight = this.getSize().height;
 		if(obtrusion.x + obtrusion.width > panelWidth){ //extends past the right side
 			Dimension newSize = new Dimension(obtrusion.x + obtrusion.width,panelHeight);
 			this.setPreferredSize(newSize);
 			this.setSize(newSize);
 		}
 		if(obtrusion.y + obtrusion.height > panelHeight){ //extends down past the bottom
 			Dimension newSize = new Dimension(panelWidth, obtrusion.y + obtrusion.height);
 			this.setPreferredSize(newSize);
 			this.setSize(newSize);
 		}
 		/*if(obtrusion.x < this.getBounds().x){
 			//SOME KIND OF TRANSLATION HERE
 			System.out.println(obtrusion.x);
 			System.out.println(this.getBounds().x);
 			panelWidth = panelWidth - obtrusion.x;
 			this.setPreferredSize(new Dimension(getWidth() + 10,getHeight()));
 			this.setBounds(this.getX(),this.getY(),panelWidth,panelHeight);
 			this.repaint();
 		}
 		if(obtrusion.y < this.getBounds().y){
 			//SOME KIND OF TRANSLATION HERE
 			panelHeight = panelHeight - obtrusion.x;
 			this.setPreferredSize(new Dimension(getWidth(),getHeight() + 10));
 			this.setBounds(this.getX(),this.getY(),panelWidth,panelHeight);
 			this.repaint();
 		}*/
 	}
 	public void addNode(Point p){
 		if(_contIns){
 			_addLocation = p;
 //			extendPanel();
 			if(_lastAdded == 0){
 				
 			}
 			else if(_lastAdded == WhiteboardPanel.SCRIBBLE){
 				newElt(BoardEltType.SCRIBBLE, BoardPathType.NORMAL);
 			}
 			else if(_lastAdded == WhiteboardPanel.STYLED){
 				newElt(BoardEltType.STYLED, BoardPathType.NORMAL);
 			} else if (_lastAdded == WhiteboardPanel.PATH) {
 				newElt(BoardEltType.PATH, _lastPathType);
 			}
 		}
 		//repaint();
 	}
 	
 	//boardpathtype only has to be specified when adding a path
 	private void newElt(BoardEltType b, BoardPathType bpt) {
 		//extendPanel(); //taken out when extendPanel changed to accept rect
 		Dimension size;
 		switch(b) {
 		case STYLED:
 			System.out.println("trying to add a styled");
 			StyledNode styledNode = new StyledNode(++WhiteboardPanel.UIDCounter, _backend);
 			_lastAdded = WhiteboardPanel.STYLED;
 			System.out.println("just created a node with UID "+WhiteboardPanel.UIDCounter);
 			size = styledNode.getSize();
 			styledNode.setBounds(_addLocation.x, _addLocation.y, size.width, size.height);
 			add(styledNode,0);
 			_backend.add(styledNode);
 			extendPanel(styledNode.getBounds());
 			break;
 		case SCRIBBLE:
 			System.out.println("trying to add a scribble");
 			ScribbleNode scribbleNode = new ScribbleNode(++WhiteboardPanel.UIDCounter, _backend);
 			_lastAdded = WhiteboardPanel.SCRIBBLE;
 			System.out.println("just created a node with UID "+WhiteboardPanel.UIDCounter);
 			size = scribbleNode.getSize();
 			scribbleNode.setBounds(_addLocation.x, _addLocation.y, size.width, size.height);
 			if (_addLocation.x + size.width > this.getWidth() || _addLocation.y + size.height > this.getHeight()) {
 				System.out.println("Extended past our bounds");
 			}
 			System.out.println("wide " + getWidth() + ", height " + getHeight());
 			add(scribbleNode,0);
 			_backend.add(scribbleNode);
 			extendPanel(scribbleNode.getBounds());
 			break;
 		case PATH:
 			System.out.println("trying to add a path");
 			_lastAdded = WhiteboardPanel.PATH;
 			_lastPathType = bpt;
 			BoardPath bp = new BoardPath(++WhiteboardPanel.UIDCounter, _backend);
 			System.out.println("Just created a path");
 			size = bp.getPreferredSize();
 			System.out.println(size);
 			bp.setPathType(bpt);
 			bp.setSeminal(_addLocation);
 			bp.setTerminal(new Point(_addLocation.x + BoardPath.START_WIDTH, _addLocation.y + BoardPath.START_HEIGHT));
 			//add(bp);
 			_backend.add(bp);
 			extendPanel(bp.getBounds());
 			break;
 		}
 		repaint();
 	}
 	
 	public void undo() {
 		_backend.undo();
 	}
 	
 	public void redo() {
 		_backend.redo();
 	}
 
 /*	public Dimension getPreferredScrollableViewportSize() {
 		return getPreferredSize();
 	}
 	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction){
 		int currentPosition = 0;
 		if(orientation == SwingConstants.HORIZONTAL){
 			currentPosition = visibleRect.x;
 		}
 		else{
 			currentPosition = visibleRect.y;
 		}
 		
 		if(direction < 0){
 			int newPosition = currentPosition - (currentPosition / _increment)*_increment;
 			return(newPosition == 0) ? _increment : newPosition;
 		}
 		else{
 			return ((currentPosition / _increment) + 1) * _increment - currentPosition;
 			
 		}
 	}
 	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction){
 		if (orientation == SwingConstants.HORIZONTAL){
 			return visibleRect.width - _increment;
 		}
 		else{
 			return visibleRect.height - _increment;
 		}
 	}
 	public boolean getScrollableTracksViewportWidth() {
 		return false;
 	}
 	public boolean getScrollableTracksViewportHeight(){
 		return false;
 	}
 	public void setIncrement(int pixels){
 		_increment = pixels;
 	}*/
 	public void updateMember(BoardElt element){
 		for(int i = 0; i<this.getComponentCount();i++){
 			if(element.getUID() == ((BoardElt)this.getComponent(i)).getUID()){
 				remove(this.getComponent(i));
 				add(element,i);
 			}
 		}
 	}
 	protected void paintComponent(Graphics g){
 		super.paintComponent(g);
 		Graphics2D g2 = (Graphics2D)g;
 		for(BoardPath b: _backend.getPaths()) {
 			b.paintComponent(g2);
 		}
 	}
 	public void load(){
 		//_board.load("fileName");
 	}
 	public void setContinuousInsertion(boolean contIns){
 		_contIns = contIns;
 	}
 	public void setListFront(BoardElt element){
 		remove(element);
 		add(element, 0);
		repaint();
 	}
 }
