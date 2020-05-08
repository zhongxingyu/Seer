 package swarm.client.view.cell;
 
 import java.util.logging.Logger;
 
 import swarm.client.app.S_Client;
 import swarm.client.entities.Camera;
 import swarm.client.entities.BufferCell;
 import swarm.client.entities.ClientGrid;
 import swarm.client.entities.ClientGrid.Obscured;
 import swarm.client.managers.CellBuffer;
 import swarm.client.managers.CellBufferManager;
 import swarm.client.states.StateMachine_Base;
 import swarm.client.states.camera.Action_Camera_SetViewSize;
 import swarm.client.states.camera.Action_Camera_SnapToPoint;
 import swarm.client.states.camera.Action_ViewingCell_Refresh;
 import swarm.client.states.camera.StateMachine_Camera;
 import swarm.client.states.camera.State_CameraSnapping;
 import swarm.client.states.camera.State_ViewingCell;
 import swarm.client.view.E_ZIndex;
 import swarm.client.view.I_UIElement;
 import swarm.client.view.U_Css;
 import swarm.client.view.ViewContext;
 import swarm.client.view.cell.VisualCell.E_MetaState;
 import swarm.client.view.dialog.Dialog;
 import swarm.shared.debugging.U_Debug;
 import swarm.shared.entities.A_Grid;
 import swarm.shared.entities.U_Grid;
 import swarm.shared.statemachine.StateEvent;
 import swarm.shared.structs.BitArray;
 import swarm.shared.structs.CellAddress;
 import swarm.shared.structs.GridCoordinate;
 import swarm.shared.structs.Point;
 import swarm.shared.utils.U_Bits;
 
 import com.google.gwt.dom.client.Style.Display;
 import com.google.gwt.dom.client.Style.Position;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 import com.google.gwt.user.client.ui.Panel;
 
 /**
  * ...
  * @author 
  */
 public class VisualCellManager implements I_UIElement
 {
 	private static final double NO_SCALING = .99999;
 	private static final Logger s_logger = Logger.getLogger(VisualCellManager.class.getName());
 	
 	private Panel m_container = null;
 	
 	private final Point m_utilPoint1 = new Point();
 	private final Point m_utilPoint2 = new Point();
 	
 	private final Point m_lastBasePoint = new Point();
 	private double m_lastScaling = 0;
 	private double m_lastMetaCellWidth = 0.0;
 	private double m_lastMetaCellHeight = 0.0;
 	
 	private final Dialog m_alertDialog;
 	
 	private final ViewContext m_viewContext;
 	
 	private final VisualCellPool m_cellPool;
 	
 	private final int m_cellDestroyLimitWhileMoving = 1;
 	
 	private CanvasBacking m_backing = null;
 	
 	private final ClientGrid.Obscured m_obscured = new ClientGrid.Obscured();
 	
 	private boolean m_backingDirty = false;
 	
 	public VisualCellManager(ViewContext viewContext, Panel container) 
 	{
 		m_container = container;
 		m_viewContext = viewContext;
 		m_cellPool = new VisualCellPool(viewContext.appContext.cellSandbox, m_container, m_viewContext.spinnerFactory, viewContext);
 		
 		m_viewContext.appContext.cellBufferMngr.getCellPool().setDelegate(m_cellPool);
 
 		m_alertDialog = new Dialog(m_viewContext.clickMngr, 256, 164, new Dialog.I_Delegate()
 		{
 			@Override
 			public void onOkPressed()
 			{
 				BufferCell bufferCell = getCurrentBufferCell();
 				VisualCell visualCell = (VisualCell) bufferCell.getVisualization();
 				visualCell.getBlocker().setContent(null);
 				
 				VisualCellManager.this.m_viewContext.alertMngr.onHandled();
 			}
 		});
 		
 		m_viewContext.alertMngr.setDelegate(new AlertManager.I_Delegate()
 		{
 			@Override
 			public void showAlert(String message)
 			{
 				BufferCell bufferCell = getCurrentBufferCell();
 				
 				if( bufferCell != null )
 				{
 					CellAddress address = bufferCell.getAddress();
 					String title = "Cell says...";
 					
 					if( message.length() > S_Client.MAX_ALERT_CHARACTERS )
 					{
 						message = message.substring(0, S_Client.MAX_ALERT_CHARACTERS);
 						message += "...";
 					}
 					
 					SafeHtml safeHtml = SafeHtmlUtils.fromString(message);
 					m_alertDialog.setTitle(title);
 					m_alertDialog.setBodySafeHtml(safeHtml);
 					VisualCell visualCell = (VisualCell) bufferCell.getVisualization();
 					visualCell.getBlocker().setContent(m_alertDialog);
 				}
 				else
 				{
 					U_Debug.ASSERT(false, "Expected current cell to be set.");
 				}
 			}
 		});
 	}
 	
 	private void initBacking(final ClientGrid grid)
 	{
 		if( m_backing == null )
 		{
 			m_backing = new CanvasBacking(new CanvasBacking.I_Skipper()
 			{
 				@Override public int skip(int m, int n)
 				{
 					CellBufferManager cellManager = m_viewContext.appContext.cellBufferMngr;
 					if( grid.isObscured(m, n, 1, cellManager.getSubCellCount(), m_obscured) )
 					{
 						CellBuffer cellBuffer = cellManager.getDisplayBuffer(U_Bits.calcBitPosition(m_obscured.subCellDimension));
 						BufferCell cell = cellBuffer.getCellAtAbsoluteCoord(m_obscured.m, m_obscured.n);
 						VisualCell visualCell = (VisualCell) cell.getVisualization();
 						E_MetaState state = visualCell.getMetaState();
 						
 //						s_logger.severe(state+"");
 						
 						if( state == VisualCell.E_MetaState.RENDERED )
 						{
 							return m_obscured.offset;
 						}
 					}
 					else
 					{
 						if( grid.isTaken(m, n, 1) )
 						{
 							return 2;
 						}
 					}
 					
 					return 0;
 				}
 			});
 			
 			m_backing.getCanvas().getElement().getStyle().setPosition(Position.ABSOLUTE);
 			m_backing.getCanvas().getElement().getStyle().setLeft(0, Unit.PX);
 			m_backing.getCanvas().getElement().getStyle().setTop(0, Unit.PX);
 			m_backing.getCanvas().getElement().getStyle().setProperty("transformOrigin", "0px 0px 0px");
 			m_backing.getCanvas().addStyleName("sm_canvas_backing");
 			resizeBacking();
 			
 //			E_ZIndex.CELL_BACKING.assignTo(m_backing.getCanvas());
 		}
 						
 //		m_backing.setColor("rgb(255,255,255)");
 		m_backing.setColor("rgb(255,0,0)");
 	}
 	
 	private void resizeBacking()
 	{
 		Camera camera = m_viewContext.appContext.cameraMngr.getCamera();
 		m_backing.onResize((int)Math.ceil(camera.getViewWidth()), (int)Math.ceil(camera.getViewHeight()));
 	}
 	
 	private boolean updateCellTransforms(double timeStep)
 	{
 		return this.updateCellTransforms(timeStep, false);
 	}
 	
 	private boolean updateCellTransforms(double timeStep, boolean isViewStateTransition)
 	{
 		CellBufferManager cellManager = m_viewContext.appContext.cellBufferMngr;
 		ClientGrid grid = m_viewContext.appContext.gridMngr.getGrid(); // TODO: Get grid from somewhere else.
 		Camera camera = m_viewContext.appContext.cameraMngr.getCamera();
 		CellBuffer cellBuffer_highest = cellManager.getHighestDisplayBuffer();
 		
 		int subCellCount_highest = cellManager.getSubCellCount();
 		double distanceRatio = camera.calcDistanceRatio();
 		
 		Point basePoint_highest = m_utilPoint1;
 		cellBuffer_highest.getCoordinate().calcPoint(basePoint_highest, grid.getCellWidth(), grid.getCellHeight(), grid.getCellPadding(), subCellCount_highest);
 		camera.calcScreenPoint(basePoint_highest, basePoint_highest);
 		basePoint_highest.round();
 		m_lastBasePoint.copy(basePoint_highest);
 		
 		if( subCellCount_highest > 1 )
 		{
 			BitArray ownership = grid.getBaseOwnership();
 			
 			if( m_backing == null && ownership != null )
 			{
 				initBacking(grid);
 				
 				m_container.add(m_backing.getCanvas());
 			}
 			
 			if( m_backing != null )
 			{
 				m_backing.getCanvas().setVisible(true);
 			}
 		}
 		else
 		{
 			if( m_backing != null )
 			{
 				m_backing.getCanvas().setVisible(false);
 			}
 		}
 		
 		double positionScaling = U_Grid.calcCellScaling(distanceRatio, subCellCount_highest, grid.getCellPadding(), grid.getCellWidth());
 		double cellWidthPlusPadding = -1;
 		double cellHeightPlusPadding = -1;
 		
 		if( subCellCount_highest == 1 )
 		{
 			cellWidthPlusPadding = (grid.getCellWidth() + grid.getCellPadding()) * positionScaling;
 			cellHeightPlusPadding = (grid.getCellHeight() + grid.getCellPadding()) * positionScaling;
 		}
 		else
 		{
 			//--- DRK > We have to fudge the scaling here so that thick artifact lines don't appear between the meta-cell images at some zoom levels.
 			//---		Basically just rounding things to the nearest pixel so that the browser doesn't just decide on its own how it should look at sub-pixels.
 			cellWidthPlusPadding = (grid.getCellWidth()) * positionScaling;
 			cellWidthPlusPadding = Math.round(cellWidthPlusPadding);
 			
 			cellHeightPlusPadding = grid.getCellHeight() * positionScaling;
 			cellHeightPlusPadding = Math.round(cellHeightPlusPadding);
 			
 			positionScaling = cellWidthPlusPadding / grid.getCellWidth();
 		}
 		
 		int limit = cellManager.getBufferCount();
 		
 		for( int i = 0; i < limit; i++ )
 		{
 			CellBuffer cellBuffer = cellManager.getDisplayBuffer(i);
 			
			if( cellBuffer.getSubCellCount() > 1 && cellBuffer.getCellCount() == 0 )  continue;
 			
 			updateCellTransforms
 			(
 				cellManager, cellBuffer, timeStep, isViewStateTransition,
 				basePoint_highest, cellWidthPlusPadding, cellHeightPlusPadding, positionScaling
 			);
 		}
 		
 		return true;
 	}
 	
 	private boolean updateCellTransforms
 		(
 			CellBufferManager manager, CellBuffer cellBuffer_i, double timeStep, boolean isViewStateTransition,
 			Point basePoint_highest, double cellWidthPlusPadding, double cellHeightPlusPadding, double positionScaling
 		)
 	{
 		int subCellCount_i = cellBuffer_i.getSubCellCount();
 		int bufferSize = cellBuffer_i.getCellCount();
 		
 		CellBufferManager cellManager = m_viewContext.appContext.cellBufferMngr;
 		ClientGrid grid = m_viewContext.appContext.gridMngr.getGrid(); // TODO: Get grid from somewhere else.
 		Camera camera = m_viewContext.appContext.cameraMngr.getCamera();
 		CellBuffer cellBuffer_highest = cellManager.getHighestDisplayBuffer();
 		
 		int subCellCount_highest = cellManager.getSubCellCount();
 		
 		double distanceRatio = camera.calcDistanceRatio();
 		double sizeScaling = U_Grid.calcCellScaling(distanceRatio, subCellCount_i, grid.getCellPadding(), grid.getCellWidth());
 		
 		State_ViewingCell viewingState = m_viewContext.stateContext.getEntered(State_ViewingCell.class);
 		boolean isViewingCell = viewingState != null;
 		BufferCell viewedCell = viewingState != null ? viewingState.getCell() : null;
 		boolean use3dTransforms = m_viewContext.appContext.platformInfo.has3dTransforms();
 		int scrollX = m_viewContext.scrollNavigator.getScrollX();
 		int scrollY = m_viewContext.scrollNavigator.getScrollY();
 		int windowWidth = (int) m_viewContext.scrollNavigator.getWindowWidth();
 		int windowHeight = (int) m_viewContext.scrollNavigator.getWindowHeight();
 	
 		String scaleProperty = sizeScaling < NO_SCALING || sizeScaling > 1-NO_SCALING ? U_Css.createScaleTransform(sizeScaling, use3dTransforms) : null;
 		
 		String transformProperty = m_viewContext.appContext.platformInfo.getTransformProperty();
 		
 		int subCellCount_highest_div = subCellCount_highest / subCellCount_i;
 		double cellWidth_div = cellWidthPlusPadding / ((double)subCellCount_highest_div);
 		double cellHeight_div = cellHeightPlusPadding / ((double)subCellCount_highest_div);
 		int coordMOfHighest = cellBuffer_highest.getCoordinate().getM();
 		int coordNOfHighest = cellBuffer_highest.getCoordinate().getN();
 		coordMOfHighest *= subCellCount_highest_div;
 		coordNOfHighest *= subCellCount_highest_div;
 		
 		if( subCellCount_i == 1 && m_backing != null && m_backing.getCanvas().isVisible() )
 		{
 			int startX_meta = (int) basePoint_highest.getX();
 			int startY_meta = (int) basePoint_highest.getY();
 			GridCoordinate coord = cellBuffer_i.getCoordinate();
 			double scaledCellWidth = cellWidth_div - grid.getCellPadding()*sizeScaling;
 			double scaledCellWidthPlusPadding = cellWidth_div;
 //			double scaledCellWidth = ((double)grid.getCellWidth())*sizeScaling;
 //			double scaledCellWidthPlusPadding = ((double)grid.getCellWidth()+grid.getCellPadding())*sizeScaling;
 			
 			m_backingDirty = false;
 			
 			m_backing.update
 			(
 				startX_meta, startY_meta, coord.getM(), coord.getN(), cellBuffer_i.getWidth(), cellBuffer_i.getHeight(),
 				scaledCellWidth, scaledCellWidthPlusPadding, grid.getWidth(), grid.getBaseOwnership(),
 				cellWidthPlusPadding, subCellCount_highest, coordMOfHighest, coordNOfHighest
 			);
 		}
 		
 		Point basePoint = null;
 		if( subCellCount_i > subCellCount_highest )
 		{
 			basePoint = m_utilPoint2;
 			cellBuffer_highest.getCoordinate().calcPoint(basePoint, grid.getCellWidth(), grid.getCellHeight(), grid.getCellPadding(), subCellCount_i);
 			int sizeMultiplier = subCellCount_i / subCellCount_highest;
 			
 			cellWidthPlusPadding *= sizeMultiplier;
 			cellHeightPlusPadding *= sizeMultiplier;
 		}
 		else
 		{
 			basePoint = basePoint_highest;
 		}
 		
 //		s_logger.severe(subCellCount_buffer + " " + subCellCount_highest + " " +cellWidthPlusPadding + " " + cellWidth_div);
 		
 		//--- DRK > NOTE: ALL DOM-manipulation related to cells should occur within this block.
 		//---		This might be an extraneous optimization in some browsers if they themselves have
 		//---		intelligent DOM-change batching, but we must assume that most browsers are retarded.
 		//--	NOTE: Well, not ALL manipulation is in here, there are a few odds and ends done outside this...but most should be here.
 		m_container.getElement().getStyle().setDisplay(Display.NONE);
 		{			
 			for ( int i = 0; i < bufferSize; i++ )
 			{
 				BufferCell ithBufferCell = cellBuffer_i.getCellAtIndex(i);
 				
 				if( ithBufferCell == null )  continue;
 				
 				VisualCell ithVisualCell = (VisualCell) ithBufferCell.getVisualization();
 				
 				double offsetX, offsetY;
 				
 				if( subCellCount_i <= subCellCount_highest )
 				{
 					int offset_m = ithBufferCell.getCoordinate().getM() - coordMOfHighest;
 					int offset_n = ithBufferCell.getCoordinate().getN() - coordNOfHighest;
 					int offset_m_mod = offset_m % subCellCount_highest_div;
 					int offset_n_mod = offset_n % subCellCount_highest_div;
 					
 					offsetX = (((offset_m-offset_m_mod)/subCellCount_highest_div) * (cellWidthPlusPadding));
 					offsetY = (((offset_n-offset_n_mod)/subCellCount_highest_div) * (cellHeightPlusPadding));
 					offsetX += offset_m_mod * cellWidth_div;
 					offsetY += offset_n_mod * cellHeight_div;
 				}
 				else
 				{
 					int offset_m = ithBufferCell.getCoordinate().getM() - cellBuffer_i.getCoordinate().getM();
 					int offset_n = ithBufferCell.getCoordinate().getN() - cellBuffer_i.getCoordinate().getN();
 					
 					offsetX = cellWidthPlusPadding * offset_m;
 					offsetY = cellHeightPlusPadding * offset_n;
 				}
 				
 				ithVisualCell.update(timeStep);
 				ithVisualCell.validate();
 				
 				if( !isViewingCell || isViewingCell && ithBufferCell != viewedCell )
 				{
 					offsetX += ((double)ithVisualCell.getXOffset())*positionScaling;
 					offsetY += ((double)ithVisualCell.getYOffset())*positionScaling;
 				}
 				else
 				{
 					//--- DRK > At this point in swarm's life, we should only be hitting this block 
 					//---		on window resizes and only resizes. For some reason the scroll has to
 					//---		be included here...it's not apparent why, but resetting the translation
 					//---		removes the window's scroll. Happens in all browsers, so not a "bug",
 					//---		but requires this "workaround" kind of as if it were a bug.
 					offsetX += ((double)ithVisualCell.getStartingXOffset())*positionScaling;
 					offsetY += ((double)ithVisualCell.getStartingYOffset())*positionScaling;
 					
 					//--- DRK > Really not sure why we have to change translation to account for scroll on window resizes.
 					//---		Doesn't make sense, but every browser behaves the same.
 					offsetX += scrollX;
 					offsetY += scrollY;
 				}
 				
 				double translateX = basePoint.getX() + offsetX;
 				double translateY = basePoint.getY() + offsetY;
 				
 				if( isViewingCell )
 				{
 					if( ithBufferCell != viewedCell )
 					{
 						//--- DRK > Need to crop the cell because it has a fixed position and will
 						//---		appear under the scroll bar but still capture the mouse through
 						//---		the scrollbar...happens on all browsers, so I guess not a "bug" per se
 						//---		but still needs this sloppy workaround. IE will in fact show the cell
 						//---		over the scroll bar...so at least it's more honest than other browsers.
 						ithVisualCell.crop((int)translateX, (int)translateY, windowWidth, windowHeight);
 						
 						//--- DRK > In a way we only should need to do this once when target cell becomes focused,
 						//---		but we're doing it for all cells everytime because it's a lightweight operation
 						//---		and passive for if new cells are created on a window resize.
 						ithVisualCell.setScrollMode(E_ScrollMode.SCROLLING_NOT_FOCUSED);
 					}
 					else
 					{
 						ithVisualCell.removeCrop();
 						ithVisualCell.setScrollMode(E_ScrollMode.SCROLLING_FOCUSED);
 					}
 				}
 				else if( !isViewingCell && isViewStateTransition )
 				{
 					//--- DRK > Removing the crop when exiting the viewing cell state.
 					ithVisualCell.removeCrop();
 					ithVisualCell.setScrollMode(E_ScrollMode.NOT_SCROLLING);
 				}
 				
 				String translateProperty = U_Css.createTranslateTransform(translateX, translateY, use3dTransforms);
 				String transform = scaleProperty != null ? translateProperty + " " + scaleProperty : translateProperty;
 				ithVisualCell.getElement().getStyle().setProperty(transformProperty, transform);
 				
 //				s_logger.severe(transform+"");
 				
 				if( ithVisualCell.getParent() == null )
 				{
 					m_container.add(ithVisualCell);
 				}
 			}
 		}
 		m_container.getElement().getStyle().setDisplay(Display.BLOCK);
 		
 		if( subCellCount_i == 1 )
 		{
 			m_lastScaling = sizeScaling;
 			m_lastMetaCellWidth = cellWidthPlusPadding;
 			m_lastMetaCellHeight = cellHeightPlusPadding;
 		}
 		
 		return true;
 	}
 	
 	private void updateCellsIndividually(double timeStep)
 	{
 		CellBufferManager cellManager = m_viewContext.appContext.cellBufferMngr;
 		
 		for( int i = 0; i < cellManager.getBufferCount(); i++ )
 		{
 			CellBuffer cellBuffer = cellManager.getDisplayBuffer(i);
 			
 			updateCellsIndividually(cellBuffer, timeStep);
 		}
 	}
 	
 	private void updateCellsIndividually(CellBuffer cellBuffer, double timeStep)
 	{
 		int bufferSize = cellBuffer.getCellCount();
 		
 		for ( int i = 0; i < bufferSize; i++ )
 		{
 			BufferCell ithBufferCell = cellBuffer.getCellAtIndex(i);
 			
 			if( ithBufferCell == null ) continue;
 			
 			VisualCell ithVisualCell = (VisualCell) ithBufferCell.getVisualization();
 			ithVisualCell.update(timeStep);
 		}
 	}
 	
 	public double getLastScaling()
 	{
 		return m_lastScaling;
 	}
 	
 	public double getLastMetaCellWidth()
 	{
 		return m_lastMetaCellWidth;
 	}
 	
 	public double getLastMetaCellHeight()
 	{
 		return m_lastMetaCellHeight;
 	}
 	
 	public Point getLastBasePoint()
 	{
 		return m_lastBasePoint;
 	}
 	
 	@Override public void onStateEvent(StateEvent event)
 	{
 		switch( event.getType() )
 		{
 			case DID_UPDATE:
 			{
 				if( event.getState().getParent() instanceof StateMachine_Camera )
 				{
 					//boolean flushDestroyQueueIfMoving = (event.getState().getUpdateCount() % 8) == 0;
 					
 					if( event.getState() instanceof State_CameraSnapping )
 					{
 						int i = 0;
 					}
 					
 					if( m_viewContext.appContext.cameraMngr.getAtRestFrameCount() >=2 )
 					{
 						m_cellPool.cleanPool();
 						
 						//--- DRK > This first condition ensures that we're still updating cell positions as they're potentially shrinking
 						//---		after exiting viewing or snapping state. There's probably a more efficient way to determine if they're actually shrinking.
 						//---		This is just a catch-all.
 						if( (event.getState().getPreviousState() == State_ViewingCell.class || event.getState().getPreviousState() == State_CameraSnapping.class) &&
 							event.getState().getTotalTimeInState() <= m_viewContext.config.cellSizeChangeTime_seconds )
 						{
 							this.updateCellTransforms(event.getState().getLastTimeStep());
 						}
 						else if( m_backingDirty )
 						{
 							this.updateCellTransforms(event.getState().getLastTimeStep());
 						}
 						else
 						{
 							this.updateCellsIndividually(event.getState().getLastTimeStep());
 						}
 					}
 					else
 					{
 						this.updateCellTransforms(event.getState().getLastTimeStep());
 					}
 				}
 				
 				break;
 			}
 			
 			case DID_ENTER:
 			{
 				if( event.getState() instanceof State_ViewingCell )
 				{
 					this.updateCellTransforms(0.0, true);
 				}
 				
 				break;
 			}
 			
 			case DID_EXIT:
 			{
 				if( event.getState() instanceof State_ViewingCell )
 				{
 					this.updateCellTransforms(0.0, true);
 				}
 				
 				break;
 			}
 			
 			case DID_PERFORM_ACTION:
 			{
 				if( event.getAction() == Action_Camera_SetViewSize.class )
 				{
 					Action_Camera_SetViewSize.Args args = event.getActionArgs();
 					
 //					if( args.updateBuffer() )
 					{
 						if( m_backing != null )
 						{
 							resizeBacking();
 						}
 						
 						this.updateCellTransforms(0.0);
 					}
 				}
 				else if( event.getAction() == StateMachine_Base.OnGridUpdate.class )
 				{					
 					//--- DRK > In some sense this is a waste of an update since in just a few milliseconds
 					//---		we'll be doing another one anyway. If we don't do it here though, there's some 
 					//---		lagging of the cell visualizations with very rapid resizes of the window.
 					//---		Also, if the camera is still and the itself gets grid updated, visual cells won't appear until
 					//---		we move the camera again.
 					ClientGrid grid = m_viewContext.appContext.gridMngr.getGrid();
 					initBacking(grid);
 					this.updateCellTransforms(0.0);
 				}
 				else if( event.getAction() == Action_Camera_SnapToPoint.class )
 				{
 					Action_Camera_SnapToPoint.Args args = event.getActionArgs();
 					
 					//--- DRK(TODO): Don't really like this null check here...necessary because of legacy
 					//---			 code using null snap args to simply change to floating state.
 					if( args == null || args.isInstant() )
 					{
 						this.updateCellTransforms(0.0);
 					}
 				}
 				else if( event.getAction() == Action_ViewingCell_Refresh.class )
 				{
 					//--- DRK > Used to clear alerts here, but moved it to the actual refresh button handler.
 					//--- 		Probably safe here, but refresh might instantly update the cell's code, before
 					//---		we get a chance to remove the alerts...might be order-dependent strangeness there.
 					//clearAlerts();
 				}
 				
 				break;
 			}
 		}
 	}
 	
 	public void onMetaCellLoaded()
 	{
 		m_backingDirty = true;
 	}
 	
 	private BufferCell getCurrentBufferCell()
 	{
 		State_ViewingCell state = m_viewContext.stateContext.getEntered(State_ViewingCell.class);
 		if( state != null )
 		{
 			return state.getCell();
 		}
 		
 		return null;
 	}
 	
 	public void clearAlerts()
 	{
 		m_viewContext.alertMngr.clear();
 		
 		BufferCell bufferCell = getCurrentBufferCell();
 		
 		if( bufferCell != null )
 		{
 			VisualCell cell = (VisualCell) bufferCell.getVisualization();
 			cell.getBlocker().setContent(null);
 		}
 		else
 		{
 			//--- DRK > Below assert trips badly when exiting view state...state is already
 			//---		exited when we get to here.
 			//smU_Debug.ASSERT(false, "Expected current cell to be set.");
 		}
 	}
 }
