 package swarm.client.states.camera;
 
 
 import java.util.logging.Logger;
 
 import swarm.client.app.AppContext;
 import swarm.client.managers.GridManager;
 import swarm.shared.entities.A_Grid;
 import swarm.shared.statemachine.StateArgs;
 import swarm.shared.statemachine.A_State;
 import swarm.shared.structs.CellAddress;
 import swarm.shared.structs.GridCoordinate;
 import swarm.shared.structs.Point;
 
 public class Action_Camera_SnapToCoordinate extends smA_CameraAction
 {
 	private static final Logger s_logger = Logger.getLogger(Action_Camera_SnapToCoordinate.class.getName());
 	
 	public static interface I_Filter
 	{
 		void adjustTargetPoint(Args args);
 		
 		void setTargetPoint(Args args);
 	}
 	
 	public static class Args extends StateArgs
 	{
 		private final GridCoordinate m_coordinate = new GridCoordinate();
 		private CellAddress m_address;
 		private final Point m_point = new Point();
 		private boolean m_hasTargetPoint;
 		
 		private boolean m_onlyCausedRefresh = false;
 		private boolean m_causeRefresh;
 		
		public boolean historyShouldIgnore = false; // kinda hacky...signal to browser history manager.
		
 		public Args()
 		{
 			init(null);
 		}
 		
 		public void init(GridCoordinate coordinate)
 		{
 			if( coordinate != null )
 			{
 				m_coordinate.copy(coordinate);
 			}
 			else
 			{
 				m_coordinate.set(-1, -1);
 			}
 			
 			m_address = null;
 			
 			m_hasTargetPoint = false;
 			m_causeRefresh = true;
 		}
 		
 		public void init(GridCoordinate coordinate, Point point)
 		{
 			m_coordinate.copy(coordinate);
 			m_point.copy(point);
 			m_address = null;
 			
 			m_hasTargetPoint = true;
 			m_causeRefresh = true;
 		}
 		
 		void init(CellAddress address, GridCoordinate coordinate)
 		{
 			m_address = address;
 			m_coordinate.copy(coordinate);
 			
 			m_hasTargetPoint = false;
 			m_causeRefresh = true;
 		}
 		
 		public GridCoordinate getTargetCoordinate()
 		{
 			return this.m_coordinate;
 		}
 		
 		public Point getTargetPoint()
 		{
 			return this.m_point;
 		}
 		
 		private void clear()
 		{
 			m_address = null;
 			m_hasTargetPoint = false;
 			m_causeRefresh = true;
 		}
 		
 		public void causeRefresh(boolean value)
 		{
 			m_causeRefresh = value;
 		}
 		
 		public boolean onlyCausedRefresh()
 		{
 			return m_onlyCausedRefresh;
 		}
 	}
 	
 	private final GridManager m_gridMngr;
 	private final I_Filter m_filter;
 	
 	Action_Camera_SnapToCoordinate(I_Filter filter_nullable, GridManager gridMngr)
 	{
 		m_gridMngr = gridMngr;
 		m_filter = filter_nullable;
 	}
 	
 	@Override
 	public void prePerform(StateArgs args)
 	{
 		super.prePerform(args);
 		
 		if( m_filter != null )
 		{
 			Args args_cast = (Args) args;
 			
 			if( args_cast.m_hasTargetPoint )
 			{
 				m_filter.adjustTargetPoint(args_cast);
 			}
 			else
 			{
 				m_filter.setTargetPoint(args_cast);
 			}
 		}
 	}
 	
 	@Override
 	public void perform(StateArgs args)
 	{
 		Args args_cast = args.cast();
 		GridCoordinate coordinate = ((Args) args).m_coordinate;
 		Point point = ((Args) args).m_point;
 		StateMachine_Camera machine = this.getState();
 		A_State currentState = machine.getCurrentState();
 		
 		if( currentState instanceof State_ViewingCell )
 		{
 			if( ((State_ViewingCell)currentState).getCell().getCoordinate().isEqualTo(coordinate) )
 			{
 				if( args_cast.m_causeRefresh )
 				{
 					((State_ViewingCell)currentState).refreshCell();
 				}
 				((Args) args).m_onlyCausedRefresh = true;
 				
 				return;
 			}
 		}
 
 		((Args) args).m_onlyCausedRefresh = false;
 		((StateMachine_Camera)this.getState()).snapToCoordinate(((Args) args).m_address, coordinate, point);
 		
 		 ((Args) args).clear();
 	}
 	
 	@Override
 	public boolean isPerformable(StateArgs args)
 	{
 		GridCoordinate coordinate = ((Args) args).m_coordinate;
 		
 		A_Grid grid = m_gridMngr.getGrid();
 		
 		if( !grid.isInBounds(coordinate) )
 		{
 			return false;
 		}
 		
 		if( !grid.isTaken(coordinate) )
 		{
 			return false;
 		}
 		
 		StateMachine_Camera machine = this.getState();
 
 		return true;
 	}
 }
