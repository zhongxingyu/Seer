 package boardnodes;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.Stack;
 
 import whiteboard.Backend;
 import whiteboard.BoardActionType;
 import whiteboard.SearchResult;
 
 
 public class BoardPath extends BoardElt {
 	public final static int ARROW_LENGTH = 15;
 	public final static double ARROW_ANGLE = Math.PI/4;
 	public final static int DRAG_RADIUS = 20; //the size of the zone you can click to start dragging
 	public final static int DRAG_SQUARE_RADIUS = (int) (DRAG_RADIUS/1.7); //the size of the marker of the zone you can click to start dragging (needs to be a bit smaller)
 	public final static int SNAP_CIRCLE_RADIUS = 10;
 	final static BasicStroke dashedStroke = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{10.0f}, 0.0f);
 	final static BasicStroke normalStroke = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
 	public final static Color START_COLOR = new Color(Color.GREEN.getRed(),Color.GREEN.getGreen(),Color.GREEN.getBlue(),210);
 	public final static Color END_COLOR = new Color(Color.YELLOW.getRed(),Color.YELLOW.getGreen(),Color.YELLOW.getBlue(),210);
 	public final static Color DELETE_COLOR = Color.RED;
 	public final static int START_WIDTH = 60;
 	public final static int START_HEIGHT = 60;
 
 	public BoardElt _snapSeminal;
 	public BoardElt _snapTerminal;
 	public Point _snapSeminalOffset;//x = 0->right, 1->top, 2->left, 3->bottom;  y=#pixels from 0
 	public Point _snapTerminalOffset;//x = 0->right, 1->top, 2->left, 3->bottom;  y=#pixels from 0
 
 	private boolean _highlighted;
 	private boolean _seminalDrag;
 	private boolean _terminalDrag;
 	private Stack<ActionObject> pastPositions;
 	private Stack<ActionObject> futurePositions;
 	private BoardPathType pathType;
 
 	public Point _seminal; //used for painting, see paint(Graphics)
 	public Point _terminal;
 	private Point _oldSeminal; //used for dragging
 	private Point _oldTerminal;
 
 	/**/
 	public BoardPath(int ID, Backend wb) {
 		super(ID, wb);
 		setBackground(new Color(0,0,0,0));
 		_seminal = new Point(0,0);
 		_terminal = new Point(100, 100);
 		_oldSeminal = _seminal;
 		_oldTerminal = _terminal;
 		pastPositions = new Stack<ActionObject>();
 		futurePositions = new Stack<ActionObject>();
 		type = BoardEltType.PATH;
 		_highlighted = false;
 	}
 
 	//set the start+end point/nodes
 
 	public void setSeminal(Point p) { 
 		_seminal = p;
 	}
 	public void setTerminal(Point p) {
 		_terminal = p;
 	}
 	public void setPathType(BoardPathType b) { pathType = b;}	
 
 	public int getWidth() {
 		return Math.abs(_terminal.x - _seminal.x);
 	}
 
 	public int getHeight() {
 		return Math.abs(_terminal.y - _seminal.y);
 	}
 
 	@Override
 	public Point getLocation() {
 		return new Point(Math.min(_terminal.x, _seminal.x), Math.min(_terminal.y, _seminal.y));
 	}
 
 	public void paintComponent(Graphics graphics) {
 		//super.paintComponent(graphics);
 		Graphics2D g2 = (Graphics2D) graphics;
 		if(_snapSeminal!=null) {
 			_seminal = _snapSeminal.getLocation();
 			int xoff = -1;
 			int yoff = -1;
 			switch(_snapSeminalOffset.x) {
 			case 0:
 				//right
 				xoff = _snapSeminal.getBounds().width;
 				break;
 			case 1:
 				//top
 				yoff = 0;
 				break;
 			case 2:
 				//left
 				xoff = 0;
 				break;
 			case 3:
 				//down
 				yoff = _snapSeminal.getBounds().height;
 
 			}
 			if(xoff == -1)
 				xoff = _snapSeminalOffset.y;
 			if(yoff==-1) 
 				yoff = _snapSeminalOffset.y;
 			
 			if(xoff>_snapSeminal.getBounds().width)
 				xoff=_snapSeminal.getBounds().width;
 			if(yoff>_snapSeminal.getBounds().height)
 				yoff = _snapSeminal.getBounds().height;
 			_seminal.translate(xoff, yoff);
 		}
 		if(_snapTerminal!=null) {
 			_terminal = _snapTerminal.getLocation();
 			int xoff = -1;
 			int yoff = -1;
 			switch(_snapTerminalOffset.x) {
 			case 0:
 				//right
 				xoff = _snapTerminal.getBounds().width;
 				break;
 			case 1:
 				//top
 				yoff = 0;
 				break;
 			case 2:
 				//left
 				xoff = 0;
 				break;
 			case 3:
 				//down
 				yoff = _snapTerminal.getBounds().height;
 
 			}
 			if(xoff == -1)
 				xoff = _snapTerminalOffset.y;
 			if(yoff==-1) 
 				yoff = _snapTerminalOffset.y;
 			if(xoff>_snapTerminal.getBounds().width)
 				xoff=_snapTerminal.getBounds().width;
 			if(yoff>_snapTerminal.getBounds().height)
 				yoff = _snapTerminal.getBounds().height;
 			
 			_terminal.translate(xoff, yoff);
 		}
 
 		g2.setColor(Color.BLACK);
 		if(_snapSeminal!=null) {
 			g2.fillOval(_seminal.x - SNAP_CIRCLE_RADIUS/2, _seminal.y - SNAP_CIRCLE_RADIUS/2, SNAP_CIRCLE_RADIUS, SNAP_CIRCLE_RADIUS);
 			//draw a black circle to show snappiness
 		}
 		if(_snapTerminal!=null) {
 			g2.fillOval(_terminal.x - SNAP_CIRCLE_RADIUS/2, _terminal.y - SNAP_CIRCLE_RADIUS/2, SNAP_CIRCLE_RADIUS, SNAP_CIRCLE_RADIUS);
 			//draw a black circle to show snappiness
 		}
 
 		if(pathType==BoardPathType.NORMAL || pathType==BoardPathType.ARROW) {
 			g2.setStroke(normalStroke);
 		} else {
 			g2.setStroke(dashedStroke);
 		}		
 		g2.drawLine(_seminal.x, _seminal.y, _terminal.x, _terminal.y);
 		g2.setStroke(normalStroke);
 		if(pathType == BoardPathType.ARROW || pathType == BoardPathType.DOTTED_ARROW) {	
 			Point point1 = new Point(_terminal.x, _terminal.y);
 			Point point2 = new Point(_terminal.x, _terminal.y);
 			double angle = Math.atan2(_seminal.y-_terminal.y, _seminal.x-_terminal.x);
 			point1.x+=ARROW_LENGTH*(Math.cos(ARROW_ANGLE+angle));
 			point1.y+=ARROW_LENGTH*(Math.sin(ARROW_ANGLE+angle));
 			point2.x+=ARROW_LENGTH*(Math.cos(angle-ARROW_ANGLE));
 			point2.y+=ARROW_LENGTH*(Math.sin(angle-ARROW_ANGLE));
 			g2.drawLine(_terminal.x, _terminal.y, point1.x, point1.y);
 			g2.drawLine(_terminal.x, _terminal.y, point2.x, point2.y);
 		}
 		if(_highlighted) {			
 			g2.setColor(BoardPath.START_COLOR);
 			//g2.fillRect(_seminal.x-DRAG_SQUARE_RADIUS/2, _seminal.y-DRAG_SQUARE_RADIUS/2, DRAG_SQUARE_RADIUS, DRAG_SQUARE_RADIUS);
 			g2.fillOval(_seminal.x-DRAG_SQUARE_RADIUS/2, _seminal.y-DRAG_SQUARE_RADIUS/2, DRAG_SQUARE_RADIUS, DRAG_SQUARE_RADIUS);
 			g2.setColor(BoardPath.END_COLOR);
 			//g2.fillRect(_terminal.x-DRAG_SQUARE_RADIUS/2, _terminal.y-DRAG_SQUARE_RADIUS/2, DRAG_SQUARE_RADIUS, DRAG_SQUARE_RADIUS);
 			g2.fillOval(_terminal.x-DRAG_SQUARE_RADIUS/2, _terminal.y-DRAG_SQUARE_RADIUS/2, DRAG_SQUARE_RADIUS, DRAG_SQUARE_RADIUS);
 			Point midpt = new Point((_seminal.x + _terminal.x)/2, (_seminal.y + _terminal.y)/2);
 			g2.setColor(BoardPath.DELETE_COLOR);
 			g2.fillRect(midpt.x-DRAG_SQUARE_RADIUS/2, midpt.y-DRAG_SQUARE_RADIUS/2, DRAG_SQUARE_RADIUS, DRAG_SQUARE_RADIUS);
 		}
 	}
 
 	public final Point getSeminal() {
 		return _seminal;
 	}
 	public final Point getTerminal() {
 		return _terminal;
 	}
 
 	public void delete() {
 		backend.remove(this.getUID());
 	}
 	@Override
 	public boolean contains(int x, int y) {
 		return isNearSeminal(x,y)||isNearTerminal(x,y)||isNearDelete(x,y);
 	}
 
 	public boolean isNearSeminal(int x, int y) {
 		return isNearPoint(x, y, _seminal);
 	}
 
 	public boolean isNearTerminal(int x, int y) {
 		return isNearPoint(x, y, _terminal);
 	}
 
 	public boolean isNearDelete(int x, int y) {
 		return isNearPoint(x, y, new Point((_terminal.x+_seminal.x)/2, (_terminal.y+_seminal.y)/2));
 	}
 
 	//helper method for the above methods
 	private boolean isNearPoint(int x, int y, Point p) {
 		return (Math.pow(x-p.x, 2) + Math.pow(y-p.y, 2))<=Math.pow(BoardPath.DRAG_RADIUS,2);
 	}
 	public void setHighlighted(boolean b) {
 		_highlighted = b;
 	}
 
 	public void startSeminalDrag() {
 		_seminalDrag = true;
 		_oldSeminal = (Point) _seminal.clone();
 		_oldTerminal = (Point) _terminal.clone();
 		_snapSeminal = null;
 	}
 
 	public void startTerminalDrag() {
 		_terminalDrag = true;
 		_oldSeminal = (Point) _seminal.clone();
 		_oldTerminal = (Point) _terminal.clone();
 		_snapTerminal = null;
 	}
 
 	public void stopDrag() {
 		if(!_seminal.equals(_oldSeminal) || !_terminal.equals(_oldTerminal)) {
 			//if either of them have changed, it's an event!
			System.out.println("it's a move event!");
 			pastPositions.push(new ActionObject(new Point[]{(Point)_oldSeminal.clone(), (Point)_oldTerminal.clone()}));
 			notifyBackend(BoardActionType.ELT_MOD);
 		}
 		_oldSeminal = (Point)_seminal.clone();
 		_oldTerminal = (Point)_terminal.clone();
 		_seminalDrag = false;
 		_terminalDrag = false;
 	}
 
 	public boolean isSeminalDragging() {
 		return _seminalDrag;
 	}
 
 	public boolean isTerminalDragging() {
 		return _terminalDrag;
 	}
 
 	public void snapTo(BoardElt b) {
 		Point mid = b.getLocation();
 		if(isSeminalDragging()) {
 			_snapSeminal = b;
 			int side = 0;
 			int pixels = 0;
 			if(_seminal.x>=b.getX()+b.getBounds().width){
 				//right
 				side = 0;
 				pixels = _seminal.y - b.getY();
 			} else if(_seminal.y<=b.getY()){
 				//top
 				side = 1;
 				pixels = _seminal.x - b.getX();
 			} else if(_seminal.x<=b.getX()){
 				//left
 				side = 2;
 				pixels = _seminal.y - b.getY();
 			} else if(_seminal.y>=b.getY()+b.getBounds().height){
 				//bottom
 				side = 3;
 				pixels = _seminal.x - b.getX();
 			}
 			_snapSeminalOffset = new Point(side, pixels);
 			//_snapSeminalOffset = new Point(_seminal.x - mid.x, _seminal.y - mid.y);
 		} else if(isTerminalDragging()) {
 			_snapTerminal = b;
 			int side = 0;
 			int pixels = 0;
 			
 			if(_terminal.x>=b.getX()+b.getBounds().width){
 				//right
 				side = 0;
 				pixels = _terminal.y - b.getY();
 			} else if(_terminal.y<=b.getY()){
 				//top
 				side = 1;
 				pixels = _terminal.x - b.getX();
 			} else if(_terminal.x<=b.getX()){
 				//left
 				side = 2;
 				pixels = _terminal.y - b.getY();
 			} else if(_terminal.y>=b.getY()+b.getBounds().height){
 				//bottom
 				side = 3;
 				pixels = _terminal.x - b.getX();
 			}
 			_snapTerminalOffset = new Point(side, pixels);
 			//_snapTerminalOffset = new Point(_terminal.x - mid.x, _terminal.y - mid.y);
 		}
 	}
 
 	public boolean unsnapDrag(BoardElt b) {
 		if(_snapSeminal == b && isSeminalDragging()) {
 			_snapSeminal = null;
 		} else if(_snapTerminal == b && isTerminalDragging()) {
 			_snapTerminal = null;
 		} else
 			return false;
 		return true;
 	}
 	
 	public boolean unsnapFrom(BoardElt b) {
 		if(_snapSeminal == b) {
 			_snapSeminal = null;
 		} else if(_snapTerminal == b) {
 			_snapTerminal = null;
 		} else
 			return false;
 		return true;
 	}
 	@Override
 	public void redo() {
 		//this always means reverting to a previous position, so we just pop it and set 
 		//	our current position to be that one, and then push our current position to the undo stack
 		if(futurePositions.empty()) {
 			return;
 		}
 		ActionObject ao = futurePositions.pop();
 		Point[] newpos = (Point[]) ao.changeInfo;
 		Point[] oldpos = new Point[]{new Point(_seminal), new Point(_terminal)};
 		ao = new ActionObject(oldpos);
 		_seminal = newpos[0];
 		_terminal = newpos[1];
 		pastPositions.push(ao);
 	}
 
 	@Override
 	public void undo() {
 		//this always means reverting to a previous position, so we just pop it and set 
 		//	our current position to be that one, and then push our current position to the redo stack
 		if(pastPositions.empty()) {
 			return;
 		}
 		ActionObject ao = pastPositions.pop();
 		Point[] newpos = (Point[]) ao.changeInfo;
 		Point[] oldpos = new Point[]{new Point(_seminal), new Point(_terminal)};
 		ao = new ActionObject(oldpos);
 		_seminal = newpos[0];
 		_terminal = newpos[1];
 		futurePositions.push(ao);
 	}
 
 	public SerializedBoardElt toSerialized() {
 		SerializedBoardPath toReturn = new SerializedBoardPath();
 		toReturn.UID = UID;
 		toReturn._stroke = pathType;
 		toReturn._snapSeminal = _snapSeminal==null?-1:_snapSeminal.getUID();
 		toReturn._snapTerminal = _snapTerminal==null?-1:_snapTerminal.getUID();
 		toReturn._snapTerminalOffset = _snapTerminalOffset==null?null:(Point) _snapTerminalOffset.clone();
 		toReturn._snapSeminalOffset = _snapSeminalOffset==null?null:(Point) _snapSeminalOffset.clone();
		System.out.println("sending: my seminal is "+_snapSeminal);
 		toReturn._seminal = _seminal;
 		toReturn._terminal = _terminal;
 		toReturn.pastPositions = (Stack<ActionObject>) pastPositions.clone();
 		toReturn.futurePositions = (Stack<ActionObject>) futurePositions.clone();
 		return toReturn;
 	}
 
 	public void ofSerialized(SerializedBoardElt _future) {
 		SerializedBoardPath future = (SerializedBoardPath) _future;
 		UID = _future.getUID();
 		_snapSeminal = future._snapSeminal==-1?null:backend.lookup(future._snapSeminal);
 		_snapTerminal = future._snapTerminal==-1?null:backend.lookup(future._snapTerminal);
		System.out.println("boardpath ofserialized: "+_snapSeminal+"  "+_snapTerminal);
 		_seminal = future._seminal;
 		_terminal = future._terminal;
 		_snapSeminalOffset = future._snapSeminalOffset;
		System.out.println("receiving: my seminal is: "+_snapSeminal);
 		_snapTerminalOffset = future._snapTerminalOffset;
 		//TODO: this is not how we want to do undoing/redoing - copy the whole stack!!!
 		pastPositions = future.pastPositions;
 		futurePositions = future.futurePositions;
 		_oldSeminal = new Point(_seminal);
 		_oldTerminal = new Point(_terminal);
 		setPathType(future._stroke);
 	}
 
 	@Override
 	public void paste(Point pos) {
 		
 	}
 
 	@Override
 	public BoardElt clone() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	@Override
 	public int getUID() {
 		return UID;
 	}
 
 	@Override
 	public void addAction(ActionObject ao) {
 		// TODO Auto-generated method stub		
 	}
 
 	@Override
 	public ArrayList<SearchResult> search(String query) {
 		return new ArrayList<SearchResult>();
 	}
 
 	@Override
 	public void highlightText(int index, int len, boolean isfocus) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void clearHighlight() {
 		// TODO Auto-generated method stub
 
 	}
 }
