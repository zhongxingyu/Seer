 package swarm.client.managers;
 
 import java.util.logging.Logger;
 
 import swarm.client.app.AppContext;
 import swarm.client.entities.Camera;
 import swarm.shared.app.S_CommonApp;
 import swarm.shared.debugging.U_Debug;
 import swarm.shared.entities.A_Grid;
 import swarm.shared.structs.Point;
 import swarm.shared.structs.Tolerance;
 import swarm.shared.structs.Vector;
 
 public class CameraManager
 {
 	private static final double SNAP_TOLERANCE = .00001;
 	private static final Logger s_logger = Logger.getLogger(CameraManager.class.getName());
 	
 	private final Point m_cameraOrigin = new Point();
 	private final Point m_targetPosition = new Point();
 	private final Vector m_diffVector = new Vector();
 	private final Vector m_utilVector = new Vector();
 	private final Point m_utilPoint1 = new Point();
 	
 	private double m_startY = 0;
 	private double m_lengthToTravel = 0;
 	private double m_xProgress = 0;
 	private double m_exponent = 0.0;
 	private double m_snapTime = 0.0;
 	private double m_minSnapTime = 0;
 	private double m_snapTimeRange = 0;
 	private double m_weightedProgress = 0;
 	
 	private int m_cameraAtRestFrameCount = 1;
 	
 	private final Camera m_camera;
 	private final GridManager m_gridMngr;
 	
 	public CameraManager(GridManager gridMngr, Camera camera, double minSnapTime, double snapTimeRange)
 	{
 		m_gridMngr = gridMngr;
 		m_minSnapTime = minSnapTime;
 		m_snapTimeRange = snapTimeRange;
 		
 		m_camera = camera;
 	}
 	
 	public Camera getCamera()
 	{
 		return m_camera;
 	}
 	
 	public Point getTargetPosition()
 	{
 		return m_targetPosition;
 	}
 	
 	private double calcY(double x)
 	{
 		double y = Math.pow(x*(1/m_snapTime), m_exponent);
 		
 		return y;
 	}
 	
 	public double getSnapProgress()
 	{
 		if( m_snapTime > 0 ) // just being safe
 		{
 			return 1 - (m_xProgress / m_snapTime);
 		}
 		
 		return 0;
 	}
 	
 	public double getWeightedSnapProgress()
 	{
 		return m_weightedProgress;
 	}
 	
 	public void update(double timeStep)
 	{
 		if ( m_xProgress == 0 )
 		{
 			m_cameraAtRestFrameCount++;
 			m_snapTime = 0;
 			
 			return;
 		}//s_logger.severe(m_camera.getPosition() + "");
 
 		m_xProgress -= timeStep;
 		
 		if ( m_xProgress <= 0 )
 		{
 			this.setCameraPosition(m_targetPosition, true);
 			
 			return;
 		}
 		
 		double progressRatio = calcY(m_xProgress);
 		progressRatio = 1 - progressRatio / m_startY;
 		m_weightedProgress = progressRatio;
 		
 		m_utilVector.copy(m_diffVector);
 		m_utilVector.scaleByNumber(progressRatio);
 		
 		m_camera.getPosition().copy(m_cameraOrigin);
 		m_camera.getPosition().add(m_utilVector);
 		
 		m_camera.update();
 	}
 	
 	private void constrainZ(Point point_out)
 	{
 		double maxZ = m_camera.calcMaxZ();
 		
 		//--- DRK < Constrain Z position.
 		if( point_out.getZ() > maxZ )
 		{
 			point_out.setZ(maxZ);
 		}
 		else if( point_out.getZ() < 0 )
 		{
 			point_out.setZ(0);
 		}
 	}
 	
 	public void shiftCamera(double deltaX, double deltaY)
 	{
 		m_camera.getPosition().inc(deltaX, deltaY, 0);
 		m_targetPosition.inc(deltaX, deltaY, 0);
 		m_cameraOrigin.inc(deltaX, deltaY, 0);
 	}
 	
 	public void setTargetPosition(Point point, boolean instant, boolean resetSnapTime)
 	{
 		A_Grid grid = m_gridMngr.getGrid();
 		
 		Point oldTargetPosition = m_utilPoint1;
 		oldTargetPosition.copy(m_targetPosition);
 		
 		//--- DRK > This is here so that given target points can selectively choose which
 		//---		components they want to update. For example a zoom action might set x
 		//---		and y to NaN values because it only wants to update z.
 		for( int i = 0; i < 3; i++ )
 		{
 			double sourceComponent = point.getComponent(i);
 			if( !Double.isNaN(sourceComponent) )
 			{
 				m_targetPosition.setComponent(i, sourceComponent);
 			}
 		}
 		
 		this.constrainZ(m_targetPosition);
 		
 		if( instant )
 		{
 			m_cameraAtRestFrameCount = 2;
 			
 			oldTargetPosition.calcDifference(m_targetPosition, m_utilVector);
 			if( m_utilVector.calcLengthSquared() < SNAP_TOLERANCE ) // kinda hacky
 			{
 				this.setCameraPosition(m_targetPosition, false); // just make sure target exactly matches camera position
 				
 				return; // let the camera continue along its trajectory, instead of recalculating it.
 				// NOTE: DRK > Don't know what above comment means now...maybe just a copy/paste from another location?
 			}
 			
 			this.setCameraPosition(m_targetPosition, false);
 			
 			return;
 		}
 		
 		m_cameraOrigin.copy(m_camera.getPosition());
 		m_targetPosition.calcDifference(m_cameraOrigin, m_diffVector);
 		
 		double lengthSquared = m_diffVector.calcLengthSquared();
 		
 		if ( lengthSquared < SNAP_TOLERANCE)
 		{
 			this.setCameraPosition(m_targetPosition, false); // just make sure target exactly matches camera position
 			
 			return;
 		}
 
 		m_lengthToTravel = Math.sqrt(lengthSquared);
 		
 		//TODO: Leaving this constant here cause it's prolly not a final solution anyway.
 		//		Obviously should be moved out someplace that's a little more configurable.
 		double maxDistance = 1024 * Math.max(grid.getCellWidth(), grid.getCellHeight());
 		
 		double distanceRatio = m_lengthToTravel / maxDistance;
 		
 		if( distanceRatio < 1 )
 		{
 			distanceRatio = Math.pow(distanceRatio, 1.0/3.0);
 		}
 		
 		if( !instant )
 		{
 			if( resetSnapTime )
 			{
 				double timeToTravel = m_minSnapTime + distanceRatio * m_snapTimeRange;
 				
 				m_snapTime = timeToTravel;
 			}
 		}
 		
 		final double MIN_EXPONENT = 3;
 		final double MAX_EXPONENT = 5;
 		final double EXPONENT_RANGE = MAX_EXPONENT - MIN_EXPONENT;
 		
 		m_exponent = MIN_EXPONENT + distanceRatio * EXPONENT_RANGE;
 
 		m_startY = calcY(m_snapTime);
 		
 		m_xProgress = m_snapTime;
 		m_weightedProgress = 0;
 		m_cameraAtRestFrameCount = 0;
 	}
 	
 	//--- DRK > NOTE: Must make sure to manually update the cell buffer manager if necessary after this call.
 	public void setCameraPosition(Point point, boolean enforceZConstraints)
 	{
 		m_targetPosition.copy(point);
 		
 		if( enforceZConstraints )
 		{
 			constrainZ(m_targetPosition);
 		}
 		
 		m_camera.getPosition().copy(m_targetPosition);
 		
 		m_camera.update();
 		
 		m_cameraOrigin.copy(m_targetPosition);
 		
 		m_xProgress = 0;
 		m_weightedProgress = 1;
 	}
 	
 	public boolean didCameraJustComeToRest()
 	{
 		return m_cameraAtRestFrameCount == 1;
 	}
 	
 	public int getAtRestFrameCount()
 	{
 		return m_cameraAtRestFrameCount;
 	}
 	
 	public boolean isCameraAtRest()
 	{
 		if( m_cameraAtRestFrameCount >= 1 )
 		{
 			U_Debug.ASSERT(m_targetPosition.isEqualTo(m_camera.getPosition(), Tolerance.EXACT), "isCameraAtRest1");
 			
 			return true;
 		}
 
 		return false;
 	}
 }
