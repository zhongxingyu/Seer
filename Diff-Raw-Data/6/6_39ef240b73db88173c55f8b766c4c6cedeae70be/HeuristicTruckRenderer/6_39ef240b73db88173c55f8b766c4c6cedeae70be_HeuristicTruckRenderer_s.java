 /**
  * 
  */
 package rinde.evo4mas.gendreau06;
 
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
 
 import rinde.sim.core.graph.Point;
 import rinde.sim.core.model.ModelProvider;
 import rinde.sim.core.model.pdp.Parcel;
 import rinde.sim.core.model.pdp.Vehicle;
 import rinde.sim.core.model.road.RoadUser;
 import rinde.sim.ui.renderers.PDPModelRenderer;
 import rinde.sim.ui.renderers.ViewPort;
 
 /**
  * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
  * 
  */
 public class HeuristicTruckRenderer extends PDPModelRenderer {
 
 	protected CoordinationModel coordinationModel;
 
 	protected Color targetLineColor;
 
 	@Override
 	protected void initialize(GC gc) {
 		super.initialize(gc);
		targetLineColor = new Color(gc.getDevice(), new RGB(0, 0, 125));
 	}
 
 	@Override
 	public void renderDynamic(GC gc, ViewPort vp, long time) {
 		super.renderDynamic(gc, vp, time);
 
 		synchronized (pdpModel) {
 			final Map<RoadUser, Point> posMap = roadModel.getObjectsAndPositions();
 			final Set<Parcel> claims = coordinationModel.getClaims();
 			for (final Parcel p : claims) {
 
 				final Point point = posMap.get(p);
 				if (point != null) {
 					final int x = vp.toCoordX(point.x);
 					final int y = vp.toCoordY(point.y);
 
 					final int radius = 8;
 					gc.setLineWidth(3);
 					gc.drawOval(x - radius, y - radius, 2 * radius, 2 * radius);
 					gc.setLineWidth(1);
 				}
 			}
 		}
 
 	}
 
 	@Override
 	protected void drawMore(GC gc, ViewPort vp, long time, Vehicle v, Point p, Map<RoadUser, Point> posMap) {
 
 		if (v instanceof HeuristicTruck) {
 			final int x = vp.toCoordX(p.x);
 			final int y = vp.toCoordY(p.y);
 
 			final HeuristicTruck ht = (HeuristicTruck) v;
 			gc.drawText(ht.stateMachine.getCurrentState().name(), x, y + 20);
 
 			if (ht.currentTarget != null && posMap.containsKey(ht.currentTarget)) {
 				final Point targetPoint = posMap.get(ht.currentTarget);
 				final int tx = vp.toCoordX(targetPoint.x);
 				final int ty = vp.toCoordY(targetPoint.y);
 				gc.setLineWidth(5);
 				gc.setForeground(targetLineColor);
 				gc.drawLine(x, y, tx, ty);
 				gc.setLineWidth(1);
 			}
 
 		}
 	}
 
 	@Override
 	public void registerModelProvider(ModelProvider mp) {
 		super.registerModelProvider(mp);
 		coordinationModel = mp.getModel(CoordinationModel.class);
 	}
 }
