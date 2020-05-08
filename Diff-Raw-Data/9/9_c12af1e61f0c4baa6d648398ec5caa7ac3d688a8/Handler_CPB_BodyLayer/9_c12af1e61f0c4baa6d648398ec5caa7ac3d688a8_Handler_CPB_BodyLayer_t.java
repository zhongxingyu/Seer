 package jig.ironLegends.collision;
 
 import java.util.Iterator;
 
 import jig.engine.physics.Body;
 import jig.engine.physics.BodyLayer;
 import jig.engine.physics.vpe.CollisionHandler;
 import jig.engine.physics.vpe.ConvexPolygon;
 import jig.engine.util.Vector2D;
 import jig.ironLegends.IronLegends;
 import jig.ironLegends.core.ConvexPolyBody;
 import jig.misc.sat.PolygonFactory;
 
 public class Handler_CPB_BodyLayer implements CollisionHandler 
 {
 	protected BodyLayer<Body> m_bodyLayer;
 	protected ConvexPolyBody m_poly;
 	PolygonFactory m_polygonFactory;
 	ISink_CPB_Body m_collisionSink;
 	int m_bodyWidth;
 	int m_bodyHeight;
 	
 	public Handler_CPB_BodyLayer(ConvexPolyBody poly, BodyLayer<Body> bodyLayer
 			, PolygonFactory polygonFactory
 			, int bodyWidth
 			, int bodyHeight
 			, ISink_CPB_Body collisionSink
 			)
 	{
 		m_polygonFactory = polygonFactory;
 		m_bodyLayer = bodyLayer;
 		m_poly 		= poly;
 		m_collisionSink = collisionSink;
 		if (m_collisionSink == null)
 			m_collisionSink = new Sink_CPB_Body_Default();
 		
 		m_bodyWidth = bodyWidth;
 		m_bodyHeight = bodyHeight;
 	}
 
 	@Override
 	public void findAndReconcileCollisions() 
 	{
 		// TODO Auto-generated method stub
 		boolean bCollisionFound = false;
		if (!m_poly.isActive())
			return;
 		
 		do
 		{
 			bCollisionFound = false;
 			ConvexPolygon mPoly = m_poly.getShape();
 			ConvexPolygon p = m_polygonFactory.createRectangle(new Vector2D(0,0), m_bodyWidth, m_bodyHeight);
 			
 			Iterator<Body> iter = m_bodyLayer.iterator();
 			while (iter.hasNext())
 			{
 				Body b = iter.next();
 				if (!b.isActive())
 					continue;
 				
 				p.setPosition(IronLegends.bodyPosToPolyPos(b.getWidth(), b.getHeight(), b.getPosition()));
 
 				Vector2D vCorrection = mPoly.minPenetration(p, false);
 				if (vCorrection != null)
 				{
 					bCollisionFound = true;
 					if (m_collisionSink.onCollision(m_poly, b, vCorrection))
 						break;
 				}
 			}
 			
 		}while (false && bCollisionFound);		
 		
 	}
 
 }
