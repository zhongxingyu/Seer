 package swarm.client.view.cell;
 
 import java.util.logging.Logger;
 
 import swarm.client.app.ClientAppConfig;
 import swarm.client.app.AppContext;
 import swarm.client.entities.Camera;
 import swarm.client.entities.BufferCell;
 import swarm.client.entities.E_CodeStatus;
 import swarm.client.input.ClickManager;
 import swarm.client.input.I_ClickHandler;
 import swarm.client.managers.CameraManager;
 import swarm.client.navigation.BrowserNavigator;
 import swarm.client.navigation.U_CameraViewport;
 import swarm.client.states.camera.Action_Camera_SnapToPoint;
 import swarm.client.states.camera.Action_Camera_SetViewSize;
 import swarm.client.states.camera.Action_Camera_SnapToAddress;
 import swarm.client.states.camera.Action_Camera_SnapToCoordinate;
 import swarm.client.states.camera.Action_ViewingCell_Refresh;
 import swarm.client.states.camera.StateMachine_Camera;
 import swarm.client.states.camera.State_CameraFloating;
 import swarm.client.states.camera.State_CameraSnapping;
 import swarm.client.states.camera.State_GettingMapping;
 import swarm.client.states.camera.State_ViewingCell;
 import swarm.client.states.code.Action_EditingCode_Preview;
 import swarm.client.states.code.Action_EditingCode_Save;
 import swarm.client.view.E_ZIndex;
 import swarm.client.view.I_UIElement;
 import swarm.client.view.U_Css;
 import swarm.client.view.ViewContext;
 import swarm.client.view.tooltip.E_ToolTipType;
 import swarm.client.view.tooltip.ToolTipConfig;
 import swarm.client.view.tooltip.ToolTipManager;
 import swarm.client.view.widget.SpriteButton;
 import swarm.shared.app.S_CommonApp;
 import swarm.shared.debugging.U_Debug;
 import swarm.shared.entities.A_Grid;
 import swarm.shared.entities.E_CodeType;
 import swarm.shared.statemachine.A_Action;
 import swarm.shared.statemachine.A_State;
 import swarm.shared.statemachine.E_StateTimeType;
 import swarm.shared.statemachine.StateEvent;
 import swarm.shared.structs.GridCoordinate;
 import swarm.shared.structs.Point;
 import swarm.shared.structs.Rect;
 import swarm.shared.structs.Vector;
 import swarm.shared.utils.U_Math;
 
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
 
 public class VisualCellHud extends FlowPanel implements I_UIElement
 {
 	private static final Logger s_logger = Logger.getLogger(VisualCellHud.class.getName());
 	
 	private static final Point s_utilPoint1 = new Point();
 	private static final Point s_utilPoint2 = new Point();
 	private static final Vector s_utilVector = new Vector();
 	
 	private class smHudButton extends SpriteButton
 	{
 		private smHudButton(String spriteId)
 		{
 			super(spriteId);
 			
 			this.addStyleName("sm_hud_button");
 		}
 	}
 	
 	private final HorizontalPanel m_innerContainer = new HorizontalPanel();
 	private final HorizontalPanel m_leftDock = new HorizontalPanel();
 	private final HorizontalPanel m_rightDock = new HorizontalPanel();
 	
 	private final smHudButton m_back		= new smHudButton("back");
 	private final smHudButton m_forward		= new smHudButton("forward");
 	private final smHudButton m_refresh		= new smHudButton("refresh");
 	private final smHudButton m_close		= new smHudButton("close");
 
 	private boolean m_waitingForBeingRefreshableAgain = false;
 	
 	private final ViewContext m_viewContext;
 	
 	private final ClientAppConfig m_appConfig;
 	
 	private final Action_Camera_SnapToPoint.Args m_args_SetCameraTarget = new Action_Camera_SnapToPoint.Args();
 	
 	private final double m_minWidth = 169;// TODO: GHETTO
 	private double m_width = 0;
 	
 	private double m_baseAlpha;
 	private double m_alpha;
 	private final double m_fadeOutTime_seconds;
 	
 	private String m_lastTranslation = "";
 	private final Point m_lastWorldPositionOnDoubleSnap = new Point();
 	private final Point m_lastWorldPosition = new Point();
 	
 	private final Rect m_utilRect = new Rect();
 	
 	private boolean m_performedDoubleSnap;
 	
 	public VisualCellHud(ViewContext viewContext, ClientAppConfig appConfig)
 	{
 		m_viewContext = viewContext;
 		
 		m_appConfig = appConfig;
 		m_fadeOutTime_seconds = m_viewContext.viewConfig.hudFadeOutTime_seconds;
 		
 		m_alpha = m_baseAlpha = 0.0;
 		
 		this.addStyleName("sm_cell_hud");
 		
 		E_ZIndex.CELL_HUD.assignTo(this);
 		
 		this.setVisible(false);
 		
 		m_innerContainer.setWidth("100%");
 		this.getElement().getStyle().setProperty("minWidth", m_minWidth + "px");
 		U_Css.setTransformOrigin(this.getElement(), "0% 0%");
 		
 		m_innerContainer.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
 		m_leftDock.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
 		m_rightDock.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
 		
 		m_leftDock.add(m_back);
 		m_leftDock.add(m_forward);
 		m_leftDock.add(m_refresh);
 		
 		m_rightDock.add(m_close);
 		
 		m_innerContainer.add(m_leftDock);
 		m_innerContainer.add(m_rightDock);
 		
 		m_innerContainer.setCellHorizontalAlignment(m_rightDock, HasHorizontalAlignment.ALIGN_RIGHT);
 		
 		this.add(m_innerContainer);
 		
 		m_viewContext.clickMngr.addClickHandler(m_back, new I_ClickHandler()
 		{
 			@Override
 			public void onClick(int x, int y)
 			{
 				if( !m_back.isEnabled() )  return;
 				
 				m_viewContext.browserNavigator.go(-1);
 			}
 		});
 		
 		m_viewContext.clickMngr.addClickHandler(m_forward, new I_ClickHandler()
 		{
 			@Override
 			public void onClick(int x, int y)
 			{
 				if( !m_forward.isEnabled() )  return;
 				
 				m_viewContext.browserNavigator.go(1);
 			}
 		});
 		
 		m_viewContext.clickMngr.addClickHandler(m_refresh, new I_ClickHandler()
 		{
 			@Override
 			public void onClick(int x, int y)
 			{
 				if( !m_refresh.isEnabled() )  return;
 				
 				VisualCellHud.this.m_viewContext.cellMngr.clearAlerts();
 				
 				m_viewContext.stateContext.performAction(Action_ViewingCell_Refresh.class);
 			}
 		});
 		
 		m_viewContext.clickMngr.addClickHandler(m_close, new I_ClickHandler()
 		{
 			@Override
 			public void onClick(int x, int y)
 			{
 				if( !m_close.isEnabled() )  return;
 
 				s_utilPoint1.copy(VisualCellHud.this.m_viewContext.appContext.cameraMngr.getCamera().getPosition());
 				s_utilPoint1.incZ(m_appConfig.backOffDistance);
 				
 				m_args_SetCameraTarget.init(s_utilPoint1, false, true);
 				m_viewContext.stateContext.performAction(Action_Camera_SnapToPoint.class, m_args_SetCameraTarget);
 			}
 		});
 		
 		ToolTipManager toolTipper = m_viewContext.toolTipMngr;
 		
 		toolTipper.addTip(m_back, new ToolTipConfig(E_ToolTipType.MOUSE_OVER, "Go back."));
 		toolTipper.addTip(m_forward, new ToolTipConfig(E_ToolTipType.MOUSE_OVER, "Go forward."));
 		toolTipper.addTip(m_refresh, new ToolTipConfig(E_ToolTipType.MOUSE_OVER, "Refresh this cell."));
 		toolTipper.addTip(m_close, new ToolTipConfig(E_ToolTipType.MOUSE_OVER, "Back off."));
 	}
 	
 	private void setAlpha(double alpha)
 	{
 		m_alpha = alpha <= 0 ? 0 : alpha;
 		//s_logger.severe(m_alpha + "");
 		this.getElement().getStyle().setOpacity(m_alpha);
 	}
 	
 	private void updateCloseButton()
 	{
 		State_ViewingCell viewingState = m_viewContext.stateContext.getEnteredState(State_ViewingCell.class);
 		State_CameraSnapping snappingState = m_viewContext.stateContext.getEnteredState(State_CameraSnapping.class);
 		
 		if( viewingState != null || snappingState != null && snappingState.getPreviousState() == State_ViewingCell.class )
 		{
 			m_close.setEnabled(true);
 		}
 		else
 		{
 			m_close.setEnabled(false);
 		}
 	}
 	
 	private void updateRefreshButton()
 	{
 		boolean canRefresh = m_viewContext.stateContext.isActionPerformable(Action_ViewingCell_Refresh.class);
 		m_refresh.setEnabled(canRefresh);
 		
 		if( !canRefresh )
 		{
 			m_waitingForBeingRefreshableAgain = true;
 		}
 		else
 		{
 			m_waitingForBeingRefreshableAgain = false;
 		}
 	}
 	
 	private void updatePositionFromScreenPoint(A_Grid grid, Point screenPoint)
 	{
 		Camera camera = m_viewContext.appContext.cameraMngr.getCamera();
 		s_utilPoint1.copy(screenPoint);
 		if( m_viewContext.stateContext.isEntered(State_ViewingCell.class) )
 		{
 			Element scrollElement = this.getParent().getElement();
 			double scrollX = scrollElement.getScrollLeft();
 			s_utilPoint1.incX(-scrollX);
 		}
 		camera.calcWorldPoint(s_utilPoint1, m_lastWorldPosition);
 		
 		boolean has3dTransforms = m_viewContext.appContext.platformInfo.has3dTransforms();
 		m_lastTranslation = U_Css.createTranslateTransform(screenPoint.getX(), screenPoint.getY(), has3dTransforms);
 		
 		double scaling = m_viewContext.cellMngr.getLastScaling();
 		String scaleProperty = U_Css.createScaleTransform(scaling, has3dTransforms);
 		U_Css.setTransform(this.getElement(), m_lastTranslation + " " + scaleProperty);
 	}
 	
 	private void updatePositionFromWorldPoint(A_Grid grid, Point worldPoint)
 	{
 		Camera camera = m_viewContext.appContext.cameraMngr.getCamera();
 		
 		camera.calcScreenPoint(worldPoint, s_utilPoint2);
 		
 		this.updatePositionFromScreenPoint(grid, s_utilPoint2);
 	}
 	
 	private void updatePosition(A_Grid grid, GridCoordinate coord)
 	{		
 		this.calcScreenPositionFromCoord(grid, coord, s_utilPoint2);
 		
 		this.updatePositionFromScreenPoint(grid, s_utilPoint2);
 	}
 	
 	private double calcScrollOffset(A_Grid grid)
 	{
 		Camera camera = m_viewContext.appContext.cameraMngr.getCamera();
 		Element scrollElement = this.getParent().getElement();
 		double scrollX = scrollElement.getScrollLeft();
 		double toReturn = 0;
 		
 		double viewWidth = getViewWidth(camera, grid);
 		double hudWidth = Math.max(m_width, m_minWidth);
 		if( hudWidth > viewWidth && m_viewContext.stateContext.isEntered(State_ViewingCell.class) )
 		{
 			double scrollWidth = scrollElement.getScrollWidth();
 			double clientWidth = scrollElement.getClientWidth();
 			double diff = (hudWidth - viewWidth) +  U_CameraViewport.getViewPadding(grid)/2.0;
 			double scrollRatio = scrollX / (scrollWidth-clientWidth);
 			toReturn -= diff * scrollRatio;
 		}
 		
 		return toReturn;
 	}
 	
 	private void calcScreenPositionFromCoord(A_Grid grid, GridCoordinate coord, Point point_out)
 	{
 		Camera camera = m_viewContext.appContext.cameraMngr.getCamera();
 		
 		coord.calcPoint(s_utilPoint1, grid.getCellWidth(), grid.getCellHeight(), grid.getCellPadding(), 1);
 		camera.calcScreenPoint(s_utilPoint1, point_out);
 
 		Element scrollElement = this.getParent().getElement();
 		double scrollX = scrollElement.getScrollLeft();
 		double scrollY = scrollElement.getScrollTop();
 		
 		double x = (point_out.getX() + scrollX*2) + this.calcScrollOffset(grid);
 		
 		double scaling = m_viewContext.cellMngr.getLastScaling();
 		double y = point_out.getY()-(m_appConfig.cellHudHeight+grid.getCellPadding())*scaling;
 		y -= 1*scaling; // account for margin...sigh
 		y += scrollY * scaling;
 		
 		point_out.set(x, y, 0);
 	}
 	
 	private void updatePositionFromState(State_CameraSnapping state)
 	{
 		A_Grid grid = m_viewContext.appContext.gridMngr.getGrid(); // TODO: Can be more than one grid in the future
 		GridCoordinate coord = state.getTargetCoordinate();
 		
 		this.updatePosition(grid, coord);
 	}
 	
 	private void updatePositionFromState(State_ViewingCell state)
 	{
 		BufferCell cell = ((State_ViewingCell)state).getCell();
 		A_Grid grid = cell.getGrid();
 		GridCoordinate coord = cell.getCoordinate();
 		
 		this.updatePosition(grid, coord);
 	}
 	
 	private void updateHistoryButtons()
 	{
 		State_ViewingCell viewingState = m_viewContext.stateContext.getEnteredState(State_ViewingCell.class);
 		State_CameraSnapping snappingState = m_viewContext.stateContext.getEnteredState(State_CameraSnapping.class);
 		
 		if( viewingState != null || snappingState != null && snappingState.getPreviousState() == State_ViewingCell.class )
 		{
 			m_back.setEnabled(m_viewContext.browserNavigator.hasBack());
 			m_forward.setEnabled(m_viewContext.browserNavigator.hasForward());
 		}
 		else
 		{
 			m_back.setEnabled(false);
 			m_forward.setEnabled(false);
 		}
 	}
 	
 	private double getViewWidth(Camera camera, A_Grid grid)
 	{
 		m_viewContext.scrollNavigator.getScrollableWindow(m_utilRect);
 		double viewPadding = U_CameraViewport.getViewPadding(grid);
 		double viewWidth = m_utilRect.getWidth() - viewPadding*2;
 		
 		return viewWidth;
 	}
 	
 	private void updateSize(A_Grid grid)
 	{
 		Camera camera = m_viewContext.appContext.cameraMngr.getCamera();
 		
 		double viewWidth = getViewWidth(camera, grid);
 		double cellWidth = grid.getCellWidth();
 		double hudWidth = Math.min(viewWidth, cellWidth);
 		
 		m_width = hudWidth - 9; // TODO: GHETTO...takes into account padding or something.
 		//m_width = Math.max(m_width, m_minWidth);
 		
 		//s_logger.severe("hud width: " + m_width);
 		
 		this.getElement().getStyle().setWidth(m_width, Unit.PX);
 	}
 	
 	private void onDoubleSnap()
 	{
 		m_lastWorldPositionOnDoubleSnap.copy(m_lastWorldPosition);
 		m_performedDoubleSnap = true;
 	}
 	
 	@Override
 	public void onStateEvent(StateEvent event)
 	{
 		switch( event.getType() )
 		{
 			case DID_ENTER:
 			{
 				if( event.getState() instanceof State_ViewingCell )
 				{
 					this.updateSize(((State_ViewingCell) event.getState()).getCell().getGrid());
 					this.updatePositionFromState((State_ViewingCell) event.getState());
 					this.updateCloseButton();
 					this.updateHistoryButtons();
 					
 					this.setAlpha(1);
 					m_baseAlpha = m_alpha;
 				}
 				else if( event.getState() instanceof State_CameraSnapping )
 				{
 					this.updateSize(m_viewContext.appContext.gridMngr.getGrid()); // just in case 
 					
 					if( event.getState().getPreviousState() != State_ViewingCell.class )
 					{
 						if( m_alpha <= 0 )
 						{
 							this.updatePositionFromState((State_CameraSnapping)event.getState());
 						}
 						
 						this.setVisible(true);
 						m_baseAlpha = m_alpha;
 						
 					//	s_logger.severe("BASE ALPHA: " + m_baseAlpha);
 					}
 					
 					this.updateCloseButton();
 					this.updateHistoryButtons();
 					
 					//m_performedDoubleSnap = false;
 				}
 				
 				break;
 			}
 			
 			case DID_FOREGROUND:
 			{
 				if( event.getState() instanceof State_ViewingCell )
 				{
 					this.updateHistoryButtons();
 					this.updateRefreshButton();
 				}
 				
 				break;
 			}
 			
 			case DID_UPDATE:
 			{
 				if( event.getState().getParent() instanceof StateMachine_Camera )
 				{
 					if( event.getState() instanceof State_ViewingCell )
 					{
 						State_ViewingCell viewingState = (State_ViewingCell) event.getState();
 						if( m_waitingForBeingRefreshableAgain )
 						{
 							if( viewingState.isForegrounded() )
 							{
 								updateRefreshButton();
 							}
 						}
 					}
 					else if( event.getState() instanceof State_CameraSnapping )
 					{
 						State_CameraSnapping cameraSnapping = m_viewContext.stateContext.getEnteredState(State_CameraSnapping.class);
 						if( cameraSnapping != null )
 						{
 							if( cameraSnapping.getPreviousState() != State_ViewingCell.class )
 							{
 								this.setAlpha(m_baseAlpha + (1-m_baseAlpha) * cameraSnapping.getOverallSnapProgress());
 							}
 					
 							if( !m_performedDoubleSnap && cameraSnapping.getPreviousState() != State_ViewingCell.class)
 							{
 								this.updatePositionFromState((State_CameraSnapping)event.getState());
 							}
 							else
 							{
 								A_Grid grid = m_viewContext.appContext.gridMngr.getGrid();
 								GridCoordinate coord = cameraSnapping.getTargetCoordinate();
 								CameraManager cameraMngr = m_viewContext.appContext.cameraMngr;
 								Camera camera = cameraMngr.getCamera();
 								
 								this.calcScreenPositionFromCoord(grid, coord, s_utilPoint2);
 								camera.calcWorldPoint(s_utilPoint2, s_utilPoint2);
 								s_utilPoint2.calcDifference(m_lastWorldPositionOnDoubleSnap, s_utilVector);
 								double progress = cameraMngr.getWeightedSnapProgress();
 								s_utilVector.scaleByNumber(progress);
 								s_utilPoint2.copy(m_lastWorldPositionOnDoubleSnap);
 								s_utilPoint2.translate(s_utilVector);
 								
 								this.updatePositionFromWorldPoint(grid, s_utilPoint2);
 							}
 						}
 					}
 					else if( event.getState() instanceof State_CameraFloating )
 					{
 						if( m_alpha <= 1 )
 						{
 							if( event.getState().isEntered() )
 							{
 								if( m_alpha > 0 )
 								{
 									double timeMantissa = event.getState().getTimeInState(E_StateTimeType.TOTAL) / m_fadeOutTime_seconds;
 									timeMantissa = U_Math.clamp(timeMantissa, 0, 1);
 									
 									this.setAlpha(m_baseAlpha * (1-timeMantissa));
 									A_Grid grid = m_viewContext.appContext.gridMngr.getGrid();
 									this.updatePositionFromWorldPoint(grid, m_lastWorldPosition);
 									
 									if( m_alpha <= 0 )
 									{
 										this.setVisible(false);
 									}
 								}
 							}
 						}
 					}
 				}
 				
 				break;
 			}
 			
 			case DID_EXIT:
 			{
 				if( event.getState() instanceof State_ViewingCell || event.getState() instanceof State_CameraSnapping )
 				{
 					m_baseAlpha = m_alpha;
 					Camera camera = m_viewContext.appContext.cameraMngr.getCamera();
 					
 					if( event.getState() instanceof State_ViewingCell )
 					{
 						m_waitingForBeingRefreshableAgain = false;
 						
 						this.updateCloseButton();
 						this.updateHistoryButtons();
 						this.updateRefreshButton();
 					}
 				}
 				
 				break;
 			}
 			
 			case DID_PERFORM_ACTION:
 			{
 				if( event.getAction() == Action_Camera_SetViewSize.class )
 				{
 					State_ViewingCell viewingState = event.getContext().getEnteredState(State_ViewingCell.class);
 					State_CameraSnapping snappingState = event.getContext().getEnteredState(State_CameraSnapping.class);
 					
 					if( viewingState != null )
 					{
 						this.updateSize(viewingState.getCell().getGrid());
 						
 						Action_Camera_SetViewSize.Args args = event.getActionArgs();
 						if( args.updateBuffer() )
 						{
 							this.updatePositionFromState(viewingState);
 						}
 					}
 					else if( snappingState != null )
 					{
 						this.updateSize(m_viewContext.appContext.gridMngr.getGrid());
 						
 						this.onDoubleSnap();
 					}
 				}
 				else if( event.getAction() == Action_Camera_SnapToPoint.class )
 				{
 					State_ViewingCell state = event.getContext().getEnteredState(State_ViewingCell.class);
 					
 					if( state != null )
 					{
 						Action_Camera_SnapToPoint.Args args = event.getActionArgs();
 						if( args.isInstant() )
 						{
 							this.updatePositionFromState(state);
 						}
 					}
 				}
 				else if( event.getAction() == Action_Camera_SnapToCoordinate.class )
 				{
 					State_CameraSnapping state = event.getContext().getEnteredState(State_CameraSnapping.class);
 					
 					if( m_alpha <= 0 )
 					{
 						this.updatePositionFromState(state);
 					}
 					
 					//if( state != null && (state.getUpdateCount() > 0 )
 					{
 						//if( !m_lastSnapCoord.isEqualTo(state.getTargetCoordinate()) )
 						{//s_logger.severe("double");
 							this.onDoubleSnap();
 						}
 					}
 				}
 				else if((	event.getAction() == Action_ViewingCell_Refresh.class		||
 							event.getAction() == Action_EditingCode_Save.class			||
 							event.getAction() == Action_EditingCode_Preview.class		||
 				
 							//--- DRK > These two cover the case of if we snap to a cell that we're already visiting.
 							//---		This effectively refreshes the cell in this case.
 							event.getAction() == Action_Camera_SnapToAddress.class		||
 							event.getAction() == Action_Camera_SnapToCoordinate.class	))
 				{
 					updateRefreshButton();
 				}
 				
 				break;
 			}
 		}
 	}
 }
