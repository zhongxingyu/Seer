 package org.dawb.workbench.plotting.system.swtxy;
 
 import java.util.Collection;
 import java.util.HashSet;
 
 import org.dawb.common.services.ImageServiceBean.ImageOrigin;
 import org.dawb.common.ui.plot.axis.AxisEvent;
 import org.dawb.common.ui.plot.axis.CoordinateSystemEvent;
 import org.dawb.common.ui.plot.axis.IAxis;
 import org.dawb.common.ui.plot.axis.IAxisListener;
 import org.dawb.common.ui.plot.axis.ICoordinateSystem;
 import org.dawb.common.ui.plot.axis.ICoordinateSystemListener;
 import org.dawb.common.ui.plot.trace.IImageTrace;
 
 public class RegionCoordinateSystem implements ICoordinateSystem, IAxisListener {
 
 	private IImageTrace imageTrace;
 	private IAxis x,y;
 	private boolean isDisposed=false;
 
 	public RegionCoordinateSystem(IImageTrace imageTrace, IAxis x, IAxis y) {
 		this.imageTrace = imageTrace;
 		this.x          = x;
 		this.y          = y;
 	}
 	
 	public IAxis getX() {
 		return x;
 	}
 
 	public IAxis getY() {
 		return y;
 	}
 
 	public void dispose() {
 		isDisposed = true;
 		if (x!=null) x.removeAxisListener(this);
 		if (this.coordinateListeners!=null) {
 			coordinateListeners.clear();
 			coordinateListeners = null;
 		}
 	}
 
 	@Override
 	public int[] getValuePosition(double... value) {
 		if (isDisposed) throw new RuntimeException(getClass().getName()+" is disposed!");
 		if (isReversed()) {
 			return new int[]{x.getValuePosition(value[1]), y.getValuePosition(value[0])};
 		} else {
 			return new int[]{x.getValuePosition(value[0]), y.getValuePosition(value[1])};
 		}
 	}
 
 	@Override
 	public double[] getPositionValue(int... position) {
 		if (isDisposed) throw new RuntimeException(getClass().getName()+" is disposed!");
 		if (isReversed()) {
			return new double[]{x.getPositionValue(position[1]), y.getPositionValue(position[0])};
 		} else {
			return new double[]{x.getPositionValue(position[0]), y.getPositionValue(position[1])};
 		}
 	}
 
 	private Collection<ICoordinateSystemListener> coordinateListeners;
 	@Override
 	public void addCoordinateSystemListener(ICoordinateSystemListener l) {
 		if (isDisposed) throw new RuntimeException(getClass().getName()+" is disposed!");
 		if (coordinateListeners==null) {
 			coordinateListeners = new HashSet<ICoordinateSystemListener>();
 			x.addAxisListener(this);
 		}
 		coordinateListeners.add(l);
 	}
 
 	@Override
 	public void removeCoordinateSystemListener(ICoordinateSystemListener l) {
 		if (isDisposed) throw new RuntimeException(getClass().getName()+" is disposed!");
 		if (coordinateListeners==null) return;
 		coordinateListeners.remove(l);
 		if (coordinateListeners.isEmpty()) {
 			x.removeAxisListener(this);
 			coordinateListeners = null;
 		}
 	}
 
 
 	private void fireCoordinateSystemListeners() {
 		if (coordinateListeners==null) return;
 		final CoordinateSystemEvent evt = new CoordinateSystemEvent(this);
 		for (ICoordinateSystemListener l : coordinateListeners) {
 			l.coordinatesChanged(evt);
 		}
 	}
 	
 	private boolean isReversed() {
 		if (imageTrace==null) return false;
 		if (imageTrace.getImageOrigin()==ImageOrigin.TOP_LEFT || 
 			imageTrace.getImageOrigin()==ImageOrigin.BOTTOM_RIGHT) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 
 	@Override
 	public void rangeChanged(AxisEvent evt) {
 		fireCoordinateSystemListeners();
 	}
 
 	@Override
 	public void revalidated(AxisEvent evt) {
 		fireCoordinateSystemListeners();
 	}
 }
