 package swarm.client.view.cell;
 
 import swarm.client.app.AppContext;
 import swarm.client.entities.BufferCell;
 import swarm.client.entities.Camera;
 import swarm.client.managers.CellBuffer;
 import swarm.client.managers.CellBufferManager;
 import swarm.client.navigation.MouseNavigator;
 import swarm.client.input.Mouse;
 import swarm.client.states.camera.Action_Camera_SetViewSize;
 import swarm.client.states.camera.Action_Camera_SnapToPoint;
 import swarm.client.states.camera.StateMachine_Camera;
 import swarm.client.states.camera.State_ViewingCell;
 import swarm.client.view.E_ZIndex;
 import swarm.client.view.I_UIElement;
 import swarm.client.view.S_UI;
 import swarm.client.view.U_Css;
 import swarm.client.view.ViewConfig;
 import swarm.client.view.ViewContext;
 import swarm.shared.app.S_CommonApp;
 import swarm.shared.debugging.U_Debug;
 import swarm.shared.utils.U_Math;
 import swarm.shared.statemachine.A_Action;
 import swarm.shared.statemachine.StateEvent;
 import swarm.shared.structs.GridCoordinate;
 import swarm.shared.structs.Point;
 
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Panel;
 
 public class VisualCellHighlight extends FlowPanel implements I_UIElement
 {	
 	private final Point m_utilPoint1 = new Point();
 	
 	private double m_lastScaling = -1;
 	
 	private final ViewContext m_viewContext;
 	
 	public VisualCellHighlight(ViewContext viewContext)
 	{
 		m_viewContext = viewContext;
 		
 		this.addStyleName("cell_highlight");
 		
 		this.getElement().setAttribute("ondragstart", "return false;");
 		
 		E_ZIndex.CELL_HIGHLIGHT.assignTo(this);
 		
 		this.setVisible(false);
 	}
 	
 	private void update()
 	{
 		MouseNavigator navManager = m_viewContext.mouseNavigator;
 		boolean isMouseTouchingSnappableCell = navManager.isMouseTouchingSnappableCell();
 		
 		//--- DRK > TODO: Really minor so might never fix, but this is kinda sloppy.
 		//---				There should probably be a "panning" state or something that the highlight listens for instead.
 		Mouse mouse = navManager.getMouse();
 		boolean isPanning = mouse.isMouseDown() && mouse.hasMouseStrayedWhileDown();
 		
 		if( isPanning )
 		{
 			this.setVisible(false);
 			
 			return;
 		}
 		
 		CellBuffer buffer = m_viewContext.appContext.cellBufferMngr.getDisplayBuffer();
 		int subCellDim = buffer.getSubCellCount();
 		
 		Camera camera = m_viewContext.appContext.cameraMngr.getCamera();
 		Point basePoint = null;
 		double highlightScaling = camera.calcDistanceRatio();
 		
 		GridCoordinate mouseCoord = navManager.getMouseGridCoord();
 		BufferCell cell = buffer.getCellAtAbsoluteCoord(mouseCoord);
 		
 		if( cell == null )
 		{
 			this.setVisible(false);
 			
 			return;
 		}
 		
 		basePoint = m_utilPoint1;
 		
 		//--- DRK > For this case, we have to do all kinds of evil witch hackery to ensure that cell highlight lines up
 		//---		visually with individual cells in the meta cell images...this technically creates some disagreement
 		//---		between the highlight and the actual mouse coordinate position for near-cell-boundary cases, but it's zoomed 
 		//---		out enough that it doesn't really matter...you'd really have to look for it to notice a discrepancy.
 		
 		VisualCellManager cellManager = m_viewContext.cellMngr;
 		double lastScaling = cellManager.getLastScaling();
 		Point lastBasePoint = cellManager.getLastBasePoint();
 		
 		int bufferM = buffer.getCoordinate().getM() * subCellDim;
 		int bufferN = buffer.getCoordinate().getN() * subCellDim;
 		int deltaM = mouseCoord.getM() - bufferM;
 		int deltaN = mouseCoord.getN() - bufferN;
 		
 		//TODO: Assuming square cell size.
 		double apparentCellPixels = 0;
 		
 		if( buffer.getSubCellCount() > 1 )
 		{
 			apparentCellPixels = ((cell.getGrid().getCellWidth() / ((double) subCellDim)) * lastScaling);
 		}
 		else
 		{
 			apparentCellPixels = (cell.getGrid().getCellWidth() + cell.getGrid().getCellPadding()) * lastScaling;
 		}
 		
 		double deltaPixelsX = apparentCellPixels * deltaM;
 		double deltaPixelsY = apparentCellPixels * deltaN;
 
 		basePoint.copy(lastBasePoint);
 		basePoint.inc(deltaPixelsX, deltaPixelsY, 0);
 		double y = basePoint.getY();
 		
 		if( m_viewContext.stateContext.isEntered(State_ViewingCell.class) )
 		{
 			Element scrollElement = this.getParent().getParent().getElement();
 			y += scrollElement.getScrollTop();
 		}
 		
		String size = (cell.getGrid().getCellWidth() * highlightScaling) + "px";
		this.setSize(size, size);
 		this.getElement().getStyle().setProperty("top", y + "px");
 		this.getElement().getStyle().setProperty("left", basePoint.getX() + "px");
 		
 		ViewConfig viewConfig = m_viewContext.viewConfig;
 		
 		if( m_lastScaling != highlightScaling )
 		{
 			double scale =  Math.sqrt(highlightScaling);
 			
 			int shadowSize = (int) (((double)viewConfig.cellHighlightMaxSize) * (scale));
 			shadowSize = (shadowSize < viewConfig.cellHighlightMinSize ? viewConfig.cellHighlightMinSize : shadowSize);
 			
 			U_Css.setBoxShadow(this.getElement(), "0 0 "+(shadowSize/2)+"px "+(shadowSize/2)+"px " + m_viewContext.viewConfig.cellHighlightColor);
 		}
 		
 		m_lastScaling = highlightScaling;
 		
 		this.setVisible(true);
 	}
 
 	public void onStateEvent(StateEvent event)
 	{
 		switch(event.getType())
 		{
 			case DID_UPDATE:
 			{
 				if( event.getState().getParent() instanceof StateMachine_Camera )
 				{
 					this.update();
 				}
 				
 				break;
 			}
 			
 			case DID_PERFORM_ACTION:
 			{
 				if( event.getAction() == Action_Camera_SetViewSize.class ||
 					event.getAction() == Action_Camera_SnapToPoint.class )
 				{
 					State_ViewingCell state = event.getContext().getEnteredState(State_ViewingCell.class);
 					
 					if( state != null )
 					{
 						this.update();
 					}
 				}
 				
 				break;
 			}
 		}
 	}
 }
