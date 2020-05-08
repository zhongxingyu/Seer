 /*
  * (c) Fachhochschule Potsdam
  */
 package org.fritzing.fritzing.diagram.edit.parts;
 
 import java.util.Iterator;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.eclipse.draw2d.ConnectionAnchor;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.PositionConstants;
 import org.eclipse.draw2d.RectangleFigure;
 import org.eclipse.draw2d.StackLayout;
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.ecore.impl.EReferenceImpl;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.EditPolicy;
 import org.eclipse.gef.Request;
 import org.eclipse.gef.commands.Command;
 import org.eclipse.gef.editpolicies.LayoutEditPolicy;
 import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
 import org.eclipse.gef.editpolicies.ResizableEditPolicy;
 import org.eclipse.gef.handles.HandleBounds;
 import org.eclipse.gef.requests.CreateRequest;
 import org.eclipse.gef.requests.DropRequest;
 import org.eclipse.gef.requests.ReconnectRequest;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.BorderedBorderItemEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.IBorderItemEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.BorderItemSelectionEditPolicy;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.GraphicalNodeEditPolicy;
 import org.eclipse.gmf.runtime.diagram.ui.figures.BorderItemLocator;
 import org.eclipse.gmf.runtime.diagram.ui.requests.CreateUnspecifiedTypeConnectionRequest;
 import org.eclipse.gmf.runtime.gef.ui.figures.DefaultSizeNodeFigure;
 import org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure;
 import org.eclipse.gmf.runtime.gef.ui.figures.SlidableAnchor;
 import org.eclipse.gmf.runtime.notation.NotationPackage;
 import org.eclipse.gmf.runtime.notation.View;
 import org.eclipse.gmf.runtime.notation.impl.NodeImpl;
 import org.eclipse.swt.graphics.Color;
 import org.fritzing.fritzing.Terminal;
 import org.fritzing.fritzing.diagram.edit.policies.NonDeleteComponentEditPolicy;
 import org.fritzing.fritzing.diagram.edit.policies.Terminal2ItemSemanticEditPolicy;
 import org.fritzing.fritzing.diagram.part.FritzingVisualIDRegistry;
 
 /**
  * @generated
  */
 public class Terminal2EditPart extends BorderedBorderItemEditPart {
 
 	/**
 	 * @generated
 	 */
 	public static final int VISUAL_ID = 3001;
 
 	/**
 	 * @generated
 	 */
 	protected IFigure contentPane;
 
 	/**
 	 * @generated NOT
 	 */
 	public static final int standardPlateMeasure = 10;
 
 	/**
 	 * @generated NOT
 	 */
 	public static final int standardTerminalMeasure = 5;
 	
 	public static final int standardFeedbackInset = -1;
 
 
 	/**
 	 * @generated
 	 */
 	protected IFigure primaryShape;
 
 	protected ConnectionAnchor legConnectionAnchor;
 
 	/**
 	 * @generated
 	 */
 	public Terminal2EditPart(View view) {
 		super(view);
 	}
 
 	public Point getLegTargetPosition() {
 		if (this.getParent() instanceof PartEditPart) {
 			return ((PartEditPart) this.getParent()).getLegTargetPosition(this);
 		}
 
 		return null;
 	}
 	
 	protected void handleNotificationEvent(Notification notification) {
 		Object feature = notification.getFeature();
 		if (feature instanceof EReferenceImpl) {
 			String name = ((EReferenceImpl) feature).getName();
 			if (name.equals("targetEdges") || name.equals("sourceEdges")) {
 				// clean up "connected" highlighting				
 				this.getParent().refresh();
 				getMainFigure().invalidate();								
 			}
 		}
 		super.handleNotificationEvent(notification);
 	}
 
 	
 	public EditPart getTargetEditPart(Request request) {
 		return super.getTargetEditPart(request);
 	}
 
 	public void displayTargetFeedback(boolean display) {
 		((TerminalDefaultSizeNodeFigure) this.getMainFigure())
 				.displayFeedback(display);
 	}
 	
 	public Rectangle getAnchorBox() {
 		return ((TerminalDefaultSizeNodeFigure) this.getMainFigure()).getAnchorBox();
 		
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	protected void createDefaultEditPolicies() {
 
 		super.createDefaultEditPolicies();
 		installEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE,
 				getPrimaryDragEditPolicy());
 		installEditPolicy(EditPolicyRoles.SEMANTIC_ROLE,
 				new Terminal2ItemSemanticEditPolicy());
 		installEditPolicy(EditPolicy.LAYOUT_ROLE, createLayoutEditPolicy());
 
 		// don't want delete
 		installEditPolicy(EditPolicy.COMPONENT_ROLE,
 				new NonDeleteComponentEditPolicy());
 
 		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE,
 				new Terminal2GraphicalNodeEditPolicy());
 
 		// make it non-selectable? (doesn't seem to work)
 		//removeEditPolicy(EditPolicyRoles.CONNECTION_HANDLES_ROLE);
 		//removeEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE);
 
 		// POPUP_BAR and CONNECTOR_HANDLES are disabled in preferences
 
 		// XXX need an SCR to runtime to have another abstract superclass that would let children add reasonable editpolicies
 		// removeEditPolicy(org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles.CONNECTION_HANDLES_ROLE);
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	public void setTextColor(Color color) {
 		for (int i = 0; i < this.getChildren().size(); i++) {
 			if (this.getChildren().get(i) instanceof TerminalName2EditPart) {
 				((TerminalName2EditPart) this.getChildren().get(i))
 						.setFontColorEx(color);
 				break;
 			}
 		}
 
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	protected LayoutEditPolicy createLayoutEditPolicy() {
 		LayoutEditPolicy lep = new LayoutEditPolicy() {
 
 			protected EditPolicy createChildEditPolicy(EditPart child) {
 				if (child instanceof IBorderItemEditPart) {
 					BorderItemSelectionEditPolicy bisep = new BorderItemSelectionEditPolicy();
 					bisep.setDragAllowed(false); // disable move
 					return bisep;
 				}
 				EditPolicy result = child
 						.getEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE);
 				if (result == null) {
 					result = new NonResizableEditPolicy();
 				}
 				return result;
 			}
 
 			protected Command getMoveChildrenCommand(Request request) {
 				return null;
 			}
 
 			protected Command getCreateCommand(CreateRequest request) {
 				return null;
 			}
 		};
 		return lep;
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	protected IFigure createNodeShape() {
 		TerminalFigure figure = new TerminalFigure(this);
 		return primaryShape = figure;
 	}
 
 	/**
 	 * @generated
 	 */
 	public TerminalFigure getPrimaryShape() {
 		return (TerminalFigure) primaryShape;
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	protected void addBorderItem(IFigure borderItemContainer,
 			IBorderItemEditPart borderItemEditPart) {
 		if (borderItemEditPart instanceof TerminalName2EditPart) {
 			BorderItemLocator locator = new BorderItemLocator(getMainFigure(),
 					PositionConstants.SOUTH_WEST);
 			locator.setBorderItemOffset(new Dimension(0, 0));
 			borderItemContainer.add(borderItemEditPart.getFigure(), locator);
 		} else {
 			super.addBorderItem(borderItemContainer, borderItemEditPart);
 		}
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	protected NodeFigure createNodePlate() {
 		DefaultSizeNodeFigure result = new TerminalDefaultSizeNodeFigure(this, 
 				getMapMode().DPtoLP(standardPlateMeasure), getMapMode().DPtoLP(
 						standardPlateMeasure));
 
 		//FIXME: workaround for #154536
 		result.getBounds().setSize(result.getPreferredSize());
 		return result;
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	public EditPolicy getPrimaryDragEditPolicy() {
 
 		EditPolicy result = super.getPrimaryDragEditPolicy();
 		if (result instanceof ResizableEditPolicy) {
 			ResizableEditPolicy ep = (ResizableEditPolicy) result;
 			ep.setResizeDirections(PositionConstants.NONE);
 		}
 		if (result instanceof NonResizableEditPolicy) {
 			// don't allow terminals to be moved
 			((NonResizableEditPolicy) result).setDragAllowed(false);
 		}
 		return result;
 
 	}
 
 	/**
 	 * Creates figure for this edit part.
 	 * 
 	 * Body of this method does not depend on settings in generation model
 	 * so you may safely remove <i>generated</i> tag and modify it.
 	 * 
 	 * @generated
 	 */
 	protected NodeFigure createMainFigure() {
 		NodeFigure figure = createNodePlate();
 		figure.setLayoutManager(new StackLayout());
 		IFigure shape = createNodeShape();
 		figure.add(shape);
 		contentPane = setupContentPane(shape);
 		return figure;
 	}
 
 	/**
 	 * Default implementation treats passed figure as content pane.
 	 * Respects layout one may have set for generated figure.
 	 * @param nodeShape instance of generated figure class
 	 * @generated
 	 */
 	protected IFigure setupContentPane(IFigure nodeShape) {
 		return nodeShape; // use nodeShape itself as contentPane
 	}
 
 	/**
 	 * @generated
 	 */
 	public IFigure getContentPane() {
 		if (contentPane != null) {
 			return contentPane;
 		}
 		return super.getContentPane();
 	}
 
 	/**
 	 * @generated
 	 */
 	public EditPart getPrimaryChildEditPart() {
 		return getChildBySemanticHint(FritzingVisualIDRegistry
 				.getType(TerminalName2EditPart.VISUAL_ID));
 	}
 
 	void setLegConnectionAnchor(ConnectionAnchor ca) {
 		legConnectionAnchor = ca;
 	}
 
 	ConnectionAnchor getLegConnectionAnchor() {
 		return legConnectionAnchor;
 	}
 
 	public boolean hasLeg() {
 		if (!this.hasConnections()) return false;
 		for (Iterator it = this.getSourceConnections().iterator(); it.hasNext();) {
 			Object o = it.next();
 			if (o instanceof LegEditPart)
 				return true;
 		}
 
 		return false;
 	}
 
 	public boolean isFemale() {
 		if (this.getParent() instanceof PartEditPart) {
 			return ((PartEditPart) this.getParent()).isTerminalFemale(this);
 
 		}
 
 		return false;
 	}
 	
 	public boolean hasConnections() {
 		return (this.getTargetConnections().size() > 0 || this.getSourceConnections().size() > 0);
 	}
 
 	public class Terminal2GraphicalNodeEditPolicy extends
 			GraphicalNodeEditPolicy {
 		
 		
 		public EditPart getTargetEditPart(Request request) {
 			if (REQ_CONNECTION_START.equals(request.getType())
 				|| REQ_CONNECTION_END.equals(request.getType())
 				|| REQ_RECONNECT_SOURCE.equals(request.getType())
 				|| REQ_RECONNECT_TARGET.equals(request.getType())
 				|| REQ_SELECTION.equals(request.getType())
 				|| REQ_MOVE.equals(request.getType())
 
 				) {
 				
 				EditPart editPart = this.getHost();
 				if (editPart instanceof Terminal2EditPart) {
 					if (((Terminal2EditPart) editPart).hasLeg()) 
 					{
 						// don't allow connections to a terminal that has a leg
 						return null;
 					}
 				}			
 
 				return getHost();
 			}
 			return null;
 		}
 		
 		protected void showTargetConnectionFeedback(DropRequest request) {
 			xTargetConnectionFeedback(request, true);
 		}
 
 		protected void eraseTargetConnectionFeedback(DropRequest request) {
 			xTargetConnectionFeedback(request, false);
 		}
 
 		protected void xTargetConnectionFeedback(DropRequest request,
 				boolean display) {
 			if (request instanceof ReconnectRequest) {
 				EditPart target = ((ReconnectRequest) request).getTarget();
 				if (target instanceof Terminal2EditPart) {
 					((Terminal2EditPart) target).displayTargetFeedback(display);
 				}
 			} else if (request instanceof CreateUnspecifiedTypeConnectionRequest) {
 				EditPart target = ((CreateUnspecifiedTypeConnectionRequest) request)
 						.getTargetEditPart();
 				if (target instanceof Terminal2EditPart) {
 					((Terminal2EditPart) target).displayTargetFeedback(display);
 				}
 			}
 		}
 	}
 
 	public class TerminalDefaultSizeNodeFigure extends DefaultSizeNodeFigure {
 		protected boolean displayFeedbackFlag;
 		TerminalAnchor lastAnchor;
 		
 		protected int standardFeedbackInsetConverted = getMapMode().DPtoLP(standardFeedbackInset);
 
 		Terminal2EditPart terminalPart;
 		
 		public TerminalDefaultSizeNodeFigure(Terminal2EditPart terminalPart, Dimension defSize) {
 			super(defSize);
 			displayFeedbackFlag = false;
 			this.terminalPart = terminalPart;
 		}
 
 		public TerminalDefaultSizeNodeFigure(Terminal2EditPart terminalPart, int width, int height) {
 			super(width, height);
 			this.terminalPart = terminalPart;
 		}
 		
 		public Rectangle getAnchorBox() {
 			if (lastAnchor == null) return null;
 			
 			return lastAnchor.getBox();
 		}
 
 		protected ConnectionAnchor createDefaultAnchor() {
 			lastAnchor = new TerminalAnchor(this);
 			return lastAnchor;
 		}
 
 		public ConnectionAnchor getTargetConnectionAnchorAt(Point p) {
 			return createDefaultAnchor();
 		}
 
 		public void displayFeedback(boolean display) {
 			displayFeedbackFlag = display;
 			repaint();
 		}
 
 		protected void paintFigure(Graphics g) {
 			// show its state
 			Rectangle tempRect = new Rectangle(getBounds());
 			tempRect.expand(standardFeedbackInsetConverted, standardFeedbackInsetConverted);
 			if (displayFeedbackFlag) { 
 				// hover state
 				g.pushState();
 				g.setBackgroundColor(THIS_FEED);
 				g.setAlpha(200);
 				g.fillRectangle(tempRect);
 				g.popState();
 			} else if (!terminalPart.hasLeg() && terminalPart.hasConnections()) {
 				// connected state
 				g.pushState();
 				g.setBackgroundColor(THIS_CONNECTED);
 				g.setAlpha(150);
 				g.fillRectangle(tempRect);
 				g.popState();
 			}
 			super.paintFigure(g);
 		}
 
 	}
 
 	public class TerminalAnchor extends SlidableAnchor {
 		public TerminalAnchor(IFigure f) {
 			super(f);
 		}
 
 		protected Rectangle getBox() {
 			Rectangle rBox = getOwner().getBounds().getCopy();
 			if (getOwner() instanceof HandleBounds)
 				rBox = ((HandleBounds) getOwner()).getHandleBounds().getCopy();
 
 			getOwner().translateToAbsolute(rBox);
 
 			rBox.x += rBox.width / 2;
 			rBox.y += rBox.height / 2;
 			rBox.width = 0;
 			rBox.height = 0;
 
 			return rBox;
 		}
 
 	}
 
 	/**
 	 * @generated
 	 */
 	public class TerminalFigure extends RectangleFigure {
 
 		/**
 		 * @generated NOT
 		 */
 		public TerminalFigure() {
 			// shouldn't be used!
 		}
 
 		protected int standardTerminalConverted;
 		protected Terminal2EditPart terminalPart;
 
 		/**
 		 * @generated NOT
 		 */
 		public TerminalFigure(Terminal2EditPart terminalPart) {
 			this.terminalPart = terminalPart;
 			standardTerminalConverted = getMapMode().DPtoLP(
 					standardTerminalMeasure);
 			this.setOutline(false);
 			this.setForegroundColor(THIS_FORE);
 			this.setBackgroundColor(THIS_BACK);
 			this.setPreferredSize(new Dimension(standardTerminalConverted,
 					standardTerminalConverted));
 		}
 
 		public void paint(Graphics graphics) {
 			if (terminalPart.isFemale()) {				
 				this.setVisible(false);
 				return;
 			}
 
 			super.paint(graphics);
 		}
 
 		public void paintFigure(Graphics graphics) {
 			if (!terminalPart.hasLeg()) {
 				super.paintFigure(graphics);
 			}
 		}
 
 		/**
 		 * @generated NOT
 		 */
 		protected void fillShape(Graphics graphics) {
 			Rectangle r = getBounds();
 			int d = (r.width - standardTerminalConverted) / 2;
 			r.x += d;
 			r.y += d;
 			r.setSize(standardTerminalConverted, standardTerminalConverted);
 			graphics.fillRectangle(r);
 		}
 
 		/**
 		 * @generated
 		 */
 		private boolean myUseLocalCoordinates = false;
 
 		/**
 		 * @generated
 		 */
 		protected boolean useLocalCoordinates() {
 			return myUseLocalCoordinates;
 		}
 
 		/**
 		 * @generated
 		 */
 		protected void setUseLocalCoordinates(boolean useLocalCoordinates) {
 			myUseLocalCoordinates = useLocalCoordinates;
 		}
 
 	}
 
 	/**
 	 * @generated
 	 */
 	static final Color THIS_FORE = new Color(null, 80, 80, 80);
 
 	/**
 	 * @generated
 	 */
 	static final Color THIS_BACK = new Color(null, 0, 0, 0);
 
 	static final Color THIS_FEED = new Color(null, 0, 158, 79);
 
 	static final Color THIS_CONNECTED = new Color(null, 100, 162, 132);
 
 }
