 package org.dawnsci.plotting.draw2d.swtxy;
 
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
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.PrecisionPoint;
 
 public class RegionCoordinateSystem implements ICoordinateSystem, IAxisListener {
 
 	private IImageTrace imageTrace;
 	private IAxis x,y;
 	private boolean isDisposed=false;
 
 	/**
 	 * 
 	 * @param imageTrace (may be null) 
 	 * @param x
 	 * @param y
 	 */
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
 	public Point getValuePosition(Point value) {
 		if (isDisposed) throw new RuntimeException(getClass().getName()+" is disposed!");
 
 		double[] pos = new double[]{x.getValuePosition(value.preciseX()), y.getValuePosition(value.preciseY())};
 		if (isReversed()) {
 			return new PrecisionPoint(pos[1], pos[0]);
 		}
 		return new PrecisionPoint(pos[0], pos[1]);
 	}
 
	private double aspectRatio = 1.0;

 	private void calcAspectRatio() {
 		aspectRatio = Math.abs(x.getScaling() / y.getScaling());
 	}
 
 	@Override
 	public double getAspectRatio() {
 		return aspectRatio;
 	}
 
 	@Override
 	public double[] getPositionValue(int... position) {
 		if (isDisposed) throw new RuntimeException(getClass().getName()+" is disposed!");
 		
 		double[] value = new double[]{x.getPositionValue(position[0]), y.getPositionValue(position[1])};
 		if (isReversed()) {
 			return new double[]{value[1], value[0]};
 		}
 		return value;
 	}
 
 	@Override
 	public Point getPositionValue(Point position) {
 		if (isDisposed) throw new RuntimeException(getClass().getName()+" is disposed!");
 
 		double[] value = new double[]{x.getPositionValue(position.x), y.getPositionValue(position.y)};
 		if (isReversed()) {
 			return new PrecisionPoint(value[1], value[0]);
 		}
 		return new PrecisionPoint(value[0], value[1]);
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
 		calcAspectRatio();
 		fireCoordinateSystemListeners();
 	}
 
 	@Override
 	public boolean isXReversed() {
 		if (imageTrace==null) return false;
 		return imageTrace.getImageOrigin()!=ImageOrigin.TOP_LEFT &&
 			   imageTrace.getImageOrigin()!=ImageOrigin.BOTTOM_LEFT;
 	}
 
 	@Override
 	public boolean isYReversed() {
 		if (imageTrace==null) return false;
 		return imageTrace.getImageOrigin()!=ImageOrigin.TOP_LEFT &&
 			   imageTrace.getImageOrigin()!=ImageOrigin.TOP_RIGHT;
 	}
 }
