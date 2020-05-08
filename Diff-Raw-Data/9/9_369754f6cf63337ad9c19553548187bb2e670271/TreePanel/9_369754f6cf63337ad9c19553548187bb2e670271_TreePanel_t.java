 package edu.rpi.phil.legup.newgui;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Polygon;
 import java.awt.Rectangle;
 import java.awt.Shape;
 import java.awt.RenderingHints;
 import java.awt.Stroke;
 import java.awt.event.MouseEvent;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Line2D;
 import java.util.ArrayList;
 import java.util.Vector;
 
 import javax.swing.ImageIcon;
 
 import edu.rpi.phil.legup.BoardState;
 import edu.rpi.phil.legup.CaseRule;
 import edu.rpi.phil.legup.Contradiction;
 import edu.rpi.phil.legup.Justification;
 import edu.rpi.phil.legup.Legup;
 import edu.rpi.phil.legup.PuzzleRule;
 import edu.rpi.phil.legup.Selection;
 
 /**
  * The TreePanel is where the tree view is stored
  *
  */
 public class TreePanel extends ZoomablePanel implements TransitionChangeListener, TreeSelectionListener
 {
 	private static final long serialVersionUID = 3124502814357135662L;
 	public static final Color nodeColor = new Color(255,255,155);
 	public static final int NODE_RADIUS = 10;
 	private static final int SMALL_NODE_RADIUS = 7;
 	private static final int COLLAPSED_DRAW_DELTA_Y = 10;
 
 	private ArrayList <Rectangle> currentStateBoxes = new ArrayList <Rectangle>();
 	private Point selectionOffset = null;
 	private Point lastMovePoint = null;
 
 	private static final float floater[] = new float[] {(float)(5.0), (float)(10.0)}; // dashed setup
 	private static final float floater2[] = new float[] {(float)(2.0), (float)(3.0)}; // dotted setup
 	private static final Stroke dashed =  new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,10
 		,floater ,0);
 	private static final Stroke dotted =  new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,10
 			,floater2 ,0);
 	private static final Stroke medium = new BasicStroke(2);
 	private static final Stroke thin = new BasicStroke(1);
 
 	//Path for node images
 	//Currently only classic and smiley options exist
 	private static final String NodeImgs = "images/tree/smiley/";
 
 	private static final Image images[] =
 	{
 		null,
 		null,
 		null,
 		new ImageIcon(NodeImgs + "cont_good.png").getImage(),
 		new ImageIcon(NodeImgs + "cont_bad.png").getImage(),
 		null
 	};;
 
 	private static final Image leadsToContradtionImage =
 		new ImageIcon(NodeImgs + "leads_to_cont.png").getImage();
 
 	private static final Image leadsToSolutionImage =
 		new ImageIcon(NodeImgs + "leads_to_soln.png").getImage();
 
 	public TreePanel()
 	{
 		BoardState.addTransitionChangeListener(this);
 		Legup.getInstance().getSelections().addTreeSelectionListener(this);
 
 		setDefaultPosition(-60,-80);
 		setPreferredSize(new Dimension(640,160));
 	}
 
 	/**
 	 * Get the most distant (grand) child of this state that is still collapsed
 	 * @param s the state we're finding child of
 	 * @return the furthest down state that is still collapsed from this one
 	 */
 	private BoardState getLastCollapsed(BoardState s)
 	{
 		Vector <BoardState> children = s.getTransitionsFrom();
 		BoardState rv = s;
 
 		if (children.size() == 1)
 		{
 			BoardState child = children.get(0);
 
 			if (child.isCollapsed())
 			{
 				rv = getLastCollapsed(child);
 			}
 		}
 
 		return rv;
 	}
 
 	protected void draw(Graphics2D g)
 	{
 		currentStateBoxes.clear();
 		BoardState state = Legup.getInstance().getInitialBoardState();
 		if( state != null ){
 			drawTree(g,state);
 			drawCurrentStateBoxes(g);
 			if (mouseOver != null) drawMouseOver(g);
 		}
 	}
 
 	/**
 	 * Recursively renders the tree below <code>state</code>.
 	 * Passing in the root node will effectively draw the entire tree.
 	 * @param g the Graphics to draw on
 	 * @param state the state we're drawing
 	 */
 	private void drawTree(Graphics g, BoardState state)
 	{
 		Graphics2D g2D = (Graphics2D)g;
 		ArrayList <Selection> sel = Legup.getInstance().getSelections().getCurrentSelection();
 		boolean isCollapsed = state.isCollapsed();
 
 		boolean flag = LEGUP_Gui.profFlag(LEGUP_Gui.IMD_FEEDBACK);
 		Vector <BoardState> transitionsFrom = null;
 		Point draw;
 		if(mouseOver != null)
 		if((mouseOver.getState().getJustification() != null)||(mouseOver.getState().getCaseRuleJustification() != null))
 		{
 			draw = mousePoint;//(Point)mouseOver.getState().getLocation().clone();
 			if((mouseOver.getState().justificationText != null)&&(mouseOver.getState().getColor() != TreePanel.nodeColor))
 			{
 				g.setColor(Color.black);
 				g2D.drawString(mouseOver.getState().justificationText,draw.x,draw.y-40);
				//g2D.drawString("Status:"+mouseOver.getState().getStatus(),draw.x,draw.y-50);
 				//System.out.println(mouseOver.getState().justificationText);
 			}
 			g.setColor(Color.gray);
 			g2D.drawRect(draw.x+30,draw.y-30,100,100);
 		}
 		g.setColor(Color.black);
 		draw = (Point)state.getLocation().clone();
 		if (!isCollapsed)
 			transitionsFrom = state.getTransitionsFrom();
 		else
 		{
 			transitionsFrom = getLastCollapsed(state).getTransitionsFrom();
 			draw.y += COLLAPSED_DRAW_DELTA_Y;
 		}
 
 		for (int c = 0; c < transitionsFrom.size(); ++c)
 		{
 			BoardState b = transitionsFrom.get(c);
 			Point childPoint = (Point)b.getLocation().clone();
 
 			if (transitionsFrom.size() == 1)
 			{
 				int status = (flag ? b.getStatus() : b.getDelayStatus());
 				if (status == BoardState.STATUS_RULE_CORRECT || status ==  BoardState.STATUS_CONTRADICTION_CORRECT)
 				{
 					g.setColor(flag ? Color.green : new Color(0x80ff80));
 					g2D.setStroke(medium);
 				}
 				else if (status == BoardState.STATUS_RULE_INCORRECT || status ==  BoardState.STATUS_CONTRADICTION_INCORRECT)
 				{
 					g.setColor(flag ? Color.red : new Color(0xff8080));
 					g2D.setStroke(medium);
 				}
 				else
 					g.setColor(flag ? Color.black : Color.gray);
 
 				if (b.isCollapsed())
 					childPoint.y -= COLLAPSED_DRAW_DELTA_Y;
 
 				drawTransition(new Line2D.Float(draw.x, draw.y, childPoint.x-NODE_RADIUS, childPoint.y), g, state, b.isCollapsed());
 				//System.out.format("%d, %d,   %d, %d\n", childPoint.x, childPoint.y, state.getLocation().x, state.getLocation().y);
 				g2D.setStroke(thin);
 			}
 			else
 				/*
 				 * We might need to do a dotted transition type thing because green implies justified,
 				 * while a case rule is not justified until all but one child lead to a contradiction
 				 */
 			{
 				if (state.getCaseSplitJustification() == null)
 					g.setColor(flag ? Color.black : Color.gray);
 				else if (state.isJustifiedCaseSplit() != null) // invalid split
 					g.setColor(flag ? Color.red : new Color(0xff8080));
 				else
 					g.setColor(flag ? Color.green : new Color(0x80ff80));
 
 				// set the stroke depending on whether it leads to a contradiction or is the last state
 				if (state.getCaseSplitJustification() == null)
 					g2D.setStroke(thin);
 				else if (b.leadsToContradiction())
 				{
 					g2D.setStroke(medium);
 				}
 				else
 				{
 					// maybe all the other ones are contradictions (proof by contradiction)
 					boolean allOthersLeadToContradiction = true;
 
 					for (int index = 0; index < transitionsFrom.size(); ++index)
 					{
 						if (c == index) // skip ourselves
 							continue;
 
 						BoardState sibling = transitionsFrom.get(index);
 
 						if (!sibling.leadsToContradiction())
 						{
 							allOthersLeadToContradiction = false;
 							break;
 						}
 					}
 
 					if (allOthersLeadToContradiction)
 						g2D.setStroke(medium);
 					else
 						g2D.setStroke(dotted);
 				}
 
 				if (b.isCollapsed())
 					childPoint.y -= COLLAPSED_DRAW_DELTA_Y;
 				
 				drawTransition(new Line2D.Float(draw.x, draw.y, childPoint.x-NODE_RADIUS, childPoint.y), g, state, b.isCollapsed());
 				
 				g2D.setStroke(thin);
 			}
 			
 			//**********************Source of node issue*************************//
 			//if (b.getTransitionsFrom().size() > 0)
 				drawTree(g, b);
 				//drawTree(g, b.getTransitionsFrom().get(0));
 		}
 
 		Selection theSelection = new Selection(state,false);
 
 		if (sel.contains(theSelection))
 		{ // handle updating the selection information
 			int deltaY = 0;
 			int yRad = 36;
 
 			if (isCollapsed)
 			{
 				deltaY = -2 * COLLAPSED_DRAW_DELTA_Y; // times 2 because draw.y is already adjusted
 				yRad += 2 * COLLAPSED_DRAW_DELTA_Y;
 			}
 
 			//currentStateBoxes.add(new Rectangle(draw.x - 18, draw.y - 18 + deltaY,36,yRad));
 		}
 
 		if (!isCollapsed)
 		{
 			drawNode(g,draw.x, draw.y,state);
 		}
 		else
 			drawCollapsedNode(g,draw.x,draw.y);
 		// to prevent the drawing of contradictions from taking over the CPU
 		try {
 			Thread.sleep(1);
 		} catch (Exception e) {
 			System.err.println("zzz...");
 		}
 	}
 	
 	/**
 	 * Draw the current transition (will make it blue if it's part of the selection)
 	 * @param trans the line of the transition we're drawing, starting at the source
 	 * @param g the graphics to use
 	 * @param parent the parent board state of the transition we're drawing
 	 * @param collapsedChild is the child we're connecting to a collapsed state
 	 */
 	private void drawTransition(Line2D.Float trans, Graphics g,
 			BoardState parent, boolean collapsedChild)
 	{
 		Graphics2D g2d = (Graphics2D)g;
 		ArrayList <Selection> sel = Legup.getInstance().getSelections().getCurrentSelection();
 		Selection theSelection = new Selection(parent,true);
 		int nodeRadius = collapsedChild ? SMALL_NODE_RADIUS : NODE_RADIUS;
 		
 		g2d.setStroke(medium);
 		g.setColor(((sel.contains(theSelection)) ? Color.blue : Color.gray));
 		
 		g2d.draw(trans);
 
 		// we also want to draw the arrowhead
 		final int ARROW_SIZE = 8;
 
 		// find the tip of the arrow, the point NODE_RADIUS away from the destination endpoint
 		double theta = Math.atan2(trans.y2 - trans.y1, trans.x2 - trans.x1);
 
 		double nx = nodeRadius * Math.cos(theta);
 		double ny = nodeRadius * Math.sin(theta);
 
 		int px = (int)Math.round(trans.x2);
 		int py = (int)Math.round(trans.y2);
 
 		Polygon arrowhead = new Polygon();
 		arrowhead.addPoint(px, py);
 
 		nx = (ARROW_SIZE) * Math.cos(theta);
 		ny = (ARROW_SIZE) * Math.sin(theta);
 
 		px = (int)Math.round(trans.x2 - nx);
 		py = (int)Math.round(trans.y2 - ny);
 		// px and py are now the "base" of the arrowhead
 
 		theta += Math.PI / 2.0;
 		double dx = (ARROW_SIZE / 2) * Math.cos(theta);
 		double dy = (ARROW_SIZE / 2) * Math.sin(theta);
 
 		arrowhead.addPoint((int)Math.round(px + dx), (int)Math.round(py + dy));
 
 		theta -= Math.PI;
 		dx = (ARROW_SIZE / 2) * Math.cos(theta);
 		dy = (ARROW_SIZE / 2) * Math.sin(theta);
 
 		arrowhead.addPoint((int)Math.round(px + dx), (int)Math.round(py + dy));
 
 		g2d.fill(arrowhead);
 	}
 
 	/**
 	 * Draw a node at a given location
 	 * @param g the graphics to draw it with
 	 * @param x the x location of the center of the node
 	 * @param y the y location of the center of the node
 	 * @param state the state to draw
 	 */
 	private void drawNode( Graphics g, int x, int y, BoardState state ){
 		final int diam = NODE_RADIUS + NODE_RADIUS;
 		Graphics2D g2D = (Graphics2D)g;
 		g2D.setStroke(thin);
 		Polygon triangle = new Polygon();
 		for(double c1 = 0;c1 < 360;c1+=120)
 		{
 			triangle.addPoint((int)(x+1.5*NODE_RADIUS*Math.cos(Math.toRadians(c1))),(int)(y+1.5*NODE_RADIUS*Math.sin(Math.toRadians(c1))));
 		}
 		Selection theSelection = new Selection(state,false);
 		ArrayList <Selection> sel = Legup.getInstance().getSelections().getCurrentSelection();
 		//g.setColor(((!state.isModifiable()) ? nodeColor : Color.green));
 		g.setColor(state.getColor());
 		if(!state.isModifiable())
 		{
 			g.fillOval( x - NODE_RADIUS, y - NODE_RADIUS, diam, diam );
 			g.setColor((sel.contains(theSelection)? Color.blue : Color.black));
 			g2D.setStroke((sel.contains(theSelection)? medium : thin));
 			g.drawOval( x - NODE_RADIUS, y - NODE_RADIUS, diam, diam );
 		}
 		else
 		{
 			if(!(state.getJustification() instanceof Contradiction))
 			{
 				g2D.fill(triangle);
 				g.setColor((sel.contains(theSelection)? Color.blue : Color.black));
 				g2D.setStroke((sel.contains(theSelection)? medium : thin));
 				g.drawPolygon(triangle);
 			}
 			else
 			{
 				g2D.fillRect(x-NODE_RADIUS,y-NODE_RADIUS,NODE_RADIUS*2,NODE_RADIUS*2);
 				g.setColor((sel.contains(theSelection)? Color.blue : Color.black));
 				g2D.setStroke((sel.contains(theSelection)? medium : thin));
 				g2D.drawRect(x-NODE_RADIUS,y-NODE_RADIUS,NODE_RADIUS*2,NODE_RADIUS*2);
 				g.setColor(Color.red);
 				g2D.drawLine(x-NODE_RADIUS,y-NODE_RADIUS,x+NODE_RADIUS,y+NODE_RADIUS);
 				g2D.drawLine(x+NODE_RADIUS,y-NODE_RADIUS,x-NODE_RADIUS,y+NODE_RADIUS);
 				g.setColor((sel.contains(theSelection)? Color.blue : Color.black));
 			}
 		}
 		boolean flag = LEGUP_Gui.profFlag(LEGUP_Gui.IMD_FEEDBACK);
 
 		// extra drawing instructions
 		/*int status = state.getStatus();
 		Image i = images[status];
 
 		if (i != null)
 		{
 			g.drawImage(i,x-i.getWidth(null)/2,y-i.getHeight(null)/2,null);
 		}*/
 	}
 
 	/**
 	 * Draw a collapsed node at the current location
 	 * @param g the Graphics to draw with
 	 * @param x the x location to draw it on
 	 * @param y the y location to draw it on
 	 */
 	private void drawCollapsedNode(Graphics g,int x, int y)
 	{
 		final int rad = SMALL_NODE_RADIUS;
 		final int diam = 2 * rad;
 		final int deltaY = -COLLAPSED_DRAW_DELTA_Y;
 
 		Graphics2D g2D = (Graphics2D)g;
 		g2D.setStroke(thin);
 		g2D.setColor(Color.black);
 		g2D.drawLine(x,y+2*deltaY,x,y);
 
 		for (int c = 0; c < 3; ++c)
 		{
 			g.setColor(nodeColor);
 			g.fillOval(x - rad,y - rad + c * deltaY,diam,diam);
 			g.setColor(Color.black);
 			g.drawOval(x - rad,y - rad + c * deltaY,diam,diam);
 		}
 	}
 
 	/**
 	 * Draw the current state boxes (the cached selection)
 	 * @param g the graphics to use to draw
 	 */
 	private void drawCurrentStateBoxes(Graphics g)
 	{
 		if (currentStateBoxes != null)
 		{
 			Graphics2D g2d = (Graphics2D)g;
 
 			g.setColor(Color.blue);
 			g2d.setStroke(dashed);
 
 			for (int x = 0; x < currentStateBoxes.size(); ++x)
 				g2d.draw(currentStateBoxes.get(x));
 		}
 	}
 
 	private void drawMouseOver(Graphics2D g)
 	{
 		BoardState B = mouseOver.getState();
 		//J contains both basic rules and contradictions
 		Justification J = (Justification)B.getJustification();
 		if (J != null)
 		{
 			g.drawImage(J.getImageIcon().getImage(), mousePoint.x+30, mousePoint.y-30, null);
 		}
 		CaseRule CR = B.getCaseSplitJustification();
 		if (CR != null)
 		{
 			g.drawImage(CR.getImageIcon().getImage(), mousePoint.x+30, mousePoint.y-30, null);
 			return;
 		}
 	}
 
 	/**
 	 * Merge the two or more selected states
 	 * TODO: add elegant error handling
 	 */
 	public void mergeStates()
 	{
 		ArrayList <Selection> selected = Legup.getInstance().getSelections().getCurrentSelection();
 
 		if (selected.size() > 1)
 		{
 			boolean allStates = true;
 
 			for (int x = 0; x < selected.size(); ++x)
 			{
 				if (selected.get(x).isTransition())
 				{
 					allStates = false;
 					break;
 				}
 				else if (selected.get(x).getState().isModifiable())
 				{
 					allStates = false;
 					break;
 				}
 			}
 
 			if (allStates)
 			{
 				ArrayList <BoardState> parents = new ArrayList <BoardState>();
 
 				for (int x = 0; x < selected.size(); ++x)
 					parents.add(selected.get(x).getState());
 
 				BoardState.merge(parents, false);
 			}
 			else
 				System.out.println("not all states");
 		}
 		else
 			System.out.println("< 2 selected");
 	}
 
 	/**
 	 * Inserts a child between the current state and the next state
 	 * @return the new child we created
 	 */
 	/*
 	 * Makes a new node from the changes made to the board state
 	 * The transition is the justification made for the new node
 	 * The node currently selected remains unchanged
 	 * The new node becomes selected
 	 */
 	public BoardState addChildAtCurrentState(Object justification)
 	{
 		// Code taken from revision 250 to add child
 		/*Selection s = Legup.getInstance().getSelections().getFirstSelection();
 		BoardState rv = null;
 
 		if (s.isState())
 		{
 			BoardState state = s.getState();
 
 			rv = state.addTransitionFrom();
 		}
 		Legup.getInstance().getSelections().setSelection(new Selection(rv, false));
 
 		return rv;*/
 		//this was what was in the rightclick before the menu - Avi
 		Selection selection = Legup.getInstance().getSelections().getFirstSelection();
 		BoardState cur = selection.getState();
 		if((cur.getChangedCells().size() > 0)||(cur.extraDataChanged()))
 		{
 			if (cur.isModifiable() && selection.isState())
 			{
 				//cur.setModifiableState(false);
 				//cur.finalize_cells();
 				Legup.getInstance().getSelections().setSelection(new Selection(cur.endTransition(), false));
 			}
 		}
 		return cur;
 		/*
 		Selection s = Legup.getInstance().getSelections().getFirstSelection();
 		//BoardState firstState = null;
 		BoardState nextState = new BoardState(s.getState());
 		BoardState originalState = s.getState();
 		originalState.revertToOriginalState();
 		
 		
 		// firstState selects the state before the transition (if any)
 		if (currentState.isModifiable()) {
 			firstState = currentState.getTransitionsTo().get(0);
 		} else {
 			firstState = currentState;
 		}
 		
 		nextState.addTransitionFrom(originalState,((PuzzleRule)justification));
 		nextState.setOffset(new Point(0, (int)(4.5*TreePanel.NODE_RADIUS)));
 		originalState.setOffset(new Point(0, 0));
 		Legup.getInstance().getSelections().setSelection(new Selection(nextState, false));
 		return nextState;
 		
 		
 		// create the middle two states
 		BoardState midState = firstState.copy();
 		midState.setModifiableState(true);
 		BoardState lastState = midState.endTransition();
 		
 		// reposition any related states in the tree
 		BoardState.reparentChildren(firstState, lastState);
 		firstState.addTransitionFrom(midState, null);
 		
 		Legup.getInstance().getSelections().setSelection(new Selection(midState, false));
 		midState.setOffset(new Point(0, (int)(4.5*TreePanel.NODE_RADIUS)));
 		lastState.setOffset(new Point(0, 0));
 		return midState;
 		*/
 	}
 	
 	/**
 	 * Collapse / expand the view at the current state
 	 *
 	 */
 	public void collapseCurrentState()
 	{
 		Selection s = Legup.getInstance().getSelections().getFirstSelection();
 
 		BoardState state = s.getState();
 
 		state.toggleCollapse();
 		
 		// TODO kueblc
 		repaint();
 	}
 
 	/**
 	 * Delete the current state and associated transition then fix the children
 	 */
 	public void delCurrentState()
 	{
 		Selection s = Legup.getInstance().getSelections().getFirstSelection();
 		BoardState currentState = s.getState();
 		
 		// make sure we don't delete the initial board state
 		if (currentState.getTransitionsTo().size() == 0)
 			return;
 		
 		// choose the previous state and move the children from after state
 		BoardState parentState = null;
 		BoardState childState = null;
 		
 		if (currentState.isModifiable()) {
 			parentState = currentState.getSingleParentState();
 			childState = currentState.endTransition();
 			parentState.getTransitionsFrom().remove(currentState);
 			currentState.getTransitionsTo().remove(parentState);
 		} else {
 			parentState = currentState.getSingleParentState().getSingleParentState();
 			childState = currentState;
 			parentState.getTransitionsFrom().remove(currentState.getSingleParentState());
 			currentState.getSingleParentState().getTransitionsTo().remove(parentState);
 		}
 		
 		BoardState.reparentChildren(childState, parentState);
 		
 		// delete the current state
 		if (currentState.isModifiable()) {
 			BoardState.deleteState(currentState);
 		} else {
 			BoardState.deleteState(currentState.getSingleParentState());
 		}
 		
 		Legup.getInstance().getSelections().setSelection(new Selection(parentState, false));
 	}
 
 	/**
 	 * Delete the child and child's subtree starting at the current state
 	 */
 	public void delChildAtCurrentState()
 	{
 		if(!Legup.getInstance().getGui().imdFeedbackFlag)BoardState.removeColorsFromTransitions();
 		Selection s = Legup.getInstance().getSelections().getFirstSelection();
 		BoardState state = s.getState();
 		
 		
 		if (s.isState())
 		{ // state
 		
 			// make sure we don't delete the initial board state
 			Vector<BoardState> parentStates = state.getTransitionsTo();
 			if (parentStates.size() == 0)
 				return;
 				
 			// use to select the previous state
 			BoardState parent = parentStates.get(0);
 			
 			BoardState.deleteState(state);
 			
 			Legup.getInstance().getSelections().setSelection(new Selection(parent, false));
 			
 		}
 		else
 		{ //  transition, delete all the things we're trasitioning from
 			
 			// select current state
 			Legup.getInstance().getSelections().setSelection(new Selection(state, false));
 
 			// delete children states
 			Vector <BoardState> children = state.getTransitionsFrom();
 
 			while (children.size() > 0)
 			{
 				BoardState child = children.get(0);
 
 				BoardState.deleteState(child);
 
 				children.remove(0);
 			}
 
 		}
 
 		
 	}
 
 	/**
 	 * Get the boardstate / transition at a point in the tree
 	 * @param state the state to check now (starts at root)
 	 * @param where the point where the user clicked
 	 * @return the node or transition the user selected, or null if he or she missed
 	 */
 	private Selection getSelectionAtPoint(BoardState state, Point where)
 	{
 		if(state == null)return null;
 		Selection rv = null;
 		Point loc = state.getLocation();
 		boolean isCollapsed = state.isCollapsed();
 		final int radius = isCollapsed ? (2 * NODE_RADIUS) : NODE_RADIUS;
 
 		Point draw = new Point(loc.x - radius, loc.y - radius);
 		// distance from a transition which is considered clicking on it, squared
 		final int MAX_CLICK_DISTANCE_SQ = 5*5;
 		Shape myBounds;
 		//System.out.println("getSelectionAtPoint called for (" + where.x + "," + where.y + ") on node at point (" + state.getLocation().x + "," + state.getLocation().y + ")");
 		if(state.isModifiable())
 		{
 			/*draw.x += 128;
 			int[] points_x = new int[3];
 			int[] points_y = new int[3];
 			for(int c1 = 0;c1 < 3;c1+=1)
 			{
 				points_x[c1] = (int)(draw.x+radius*Math.cos(Math.toRadians(c1*120)));
 				points_y[c1] = (int)(draw.y+radius*Math.sin(Math.toRadians(c1*120)));
 			}
 			myBounds = new Polygon(points_x,points_y,3);*/
 			draw.x -= radius/2;
 			draw.y -= radius/2;
 			myBounds = new Ellipse2D.Float(draw.x,draw.y,(int)(3*radius),(int)(3*radius));
 		}
 		else
 		{
 			myBounds = new Ellipse2D.Float(draw.x,draw.y,2 * radius,2 * radius);
 		}
 		
 		boolean stateSelected = myBounds.contains(where);
 
 		if (stateSelected && isCollapsed)
 		{
 			Vector <BoardState> parents = state.getTransitionsTo();
 
 			if (parents.size() == 1 && parents.get(0).isCollapsed())
 				stateSelected = false; // can't select a collapsed state
 		}
 
 		if (stateSelected)
 		{
 			rv = new Selection(state,false);
 		}
 		else
 		{
 			for(BoardState b : state.getTransitionsFrom())
 			{
 				Selection s = getSelectionAtPoint(b,where);
 				if(s != null)rv = s;
 			}
 			//the whole chunk of code below was deliberately skipping the transitions, which
 			//is no longer desireable
 			/*Vector<BoardState> transitionsFrom = state.getTransitionsFrom();
 			
 			for (int c = 0; c < transitionsFrom.size(); ++c)
 			{
 				BoardState b = transitionsFrom.get(c);
 				Point childCenter = b.getLocation();
 				
 				Line2D.Float transitionLine = new Line2D.Float(childCenter,loc);
 
 				if (transitionLine.ptSegDistSq(where) < MAX_CLICK_DISTANCE_SQ)
 				{
 					rv = new Selection(b, false);
 				}
 				
 				Selection s = null;
 				// note that we may select a state after we've found select transition,
 				// which is desired
 				if (b.getTransitionsFrom().size() > 0)
 					s = getSelectionAtPoint(b.getTransitionsFrom().get(0), where);
 
 				if (s != null)
 					rv = s;
 
 			}*/
 			// note that we may select a state after we've found select transition,
 			// rv = new Selection(state,true);
 			// transitionLine.ptSegDistSq(where) < MAX_CLICK_DISTANCE_SQ)
 		}
 
 		return rv;
 	}
 
 	/**
 	 * Toggle a state in a selection (something was ctrl + clicked)
 	 * @param state the state to check now (starts at root)
 	 * @param bounds the bounds of the state and all it's children
 	 * @param where the point where the user ctrl + clicked
 	 */
 	private void toggleSelection(BoardState state, Point where)
 	{
 		Selection s = getSelectionAtPoint(state, where);
 
 		Legup.getInstance().getSelections().toggleSelection(s);
 	}
 
 	/**
 	 * Select a new state or transition that the user clicked on
 	 * @param state the state we're at
 	 * @param bounds the bounds of the state and all it's children
 	 * @param where the point where the user clicked
 	 * @return the new Selection
 	 */
 	private Selection newSelection(BoardState state, Point where)
 	{
 		Selection s = getSelectionAtPoint(state, where);
 
 		Legup.getInstance().getSelections().setSelection(s);
 
 		return s;
 	}
 
 	protected void mousePressedAt( Point p, MouseEvent e ){
 		// left click
 		if( e.getButton() == MouseEvent.BUTTON1 ){
 			
 			lastMovePoint = new Point(p);
 			
 			// add to selection
 			if ( e.isControlDown() )
 				toggleSelection( Legup.getInstance().getInitialBoardState(), p );
 			// make a new selection
 			else
 				newSelection( Legup.getInstance().getInitialBoardState(), p );
 		// right click
 		} else if( e.getButton() == MouseEvent.BUTTON3 ){
 			// create a new child node and select it
 			//Selection s = new Selection( addChildAtCurrentState(), false );
 			//Legup.getInstance().getSelections().setSelection( s );
 		}
 		// TODO focus
 		//grabFocus();
 	}
 
 	protected void mouseReleasedAt(Point p, MouseEvent e)
 	{
 		selectionOffset = null;
 		lastMovePoint = null;
 	}
 
 	protected void mouseExitedAt(Point realPoint, MouseEvent e)
 	{
 		selectionOffset = null;
 		mouseOver = null;
 
 		repaint();
 
 		Legup.getInstance().refresh();
 	}
 
 	private static Selection mouseOver;
 	private Point mousePoint;
 	protected void mouseMovedAt(Point p, MouseEvent e)
 	{
 		Selection prev = mouseOver;
 		mouseOver = getSelectionAtPoint(Legup.getInstance().getInitialBoardState(), p);
 		mousePoint = p;
 
 		if( prev != null || mouseOver != null )
 			repaint();
 		if( prev != null ^ mouseOver != null )
 			Legup.getInstance().refresh();
 		if( prev != null && mouseOver != null )
 			if( !prev.equals(mouseOver) )
 				Legup.getInstance().refresh();
 	}
 	
 	protected void mouseDraggedAt(Point p, MouseEvent e) {
 		if (lastMovePoint == null)
 			lastMovePoint = new Point(p);
 			
 		moveX += p.x - lastMovePoint.x;
 		moveY += p.y - lastMovePoint.y;
 		
 		repaint();
 	}
 
 	public static Selection getMouseOver(){
 		return mouseOver;
 	}
 
 	public void transitionChanged(){
 		repaint();
 	}
 
 	public void treeSelectionChanged(ArrayList <Selection> newSelection)
 	{
 
 	}
 }
