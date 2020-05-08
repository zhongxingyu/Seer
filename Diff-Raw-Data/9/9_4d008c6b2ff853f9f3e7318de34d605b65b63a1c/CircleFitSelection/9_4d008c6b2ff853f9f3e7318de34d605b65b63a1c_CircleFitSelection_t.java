 package org.dawnsci.plotting.draw2d.swtxy.selection;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.dawb.common.ui.plot.axis.ICoordinateSystem;
 import org.dawb.common.ui.plot.region.IRegion;
 import org.dawb.common.ui.plot.region.IRegionContainer;
 import org.dawb.common.ui.plot.region.ROIEvent;
 import org.dawnsci.plotting.draw2d.swtxy.translate.FigureTranslator;
 import org.dawnsci.plotting.draw2d.swtxy.translate.TranslationEvent;
 import org.dawnsci.plotting.draw2d.swtxy.translate.TranslationListener;
 import org.dawnsci.plotting.draw2d.swtxy.util.AffineTransform;
 import org.dawnsci.plotting.draw2d.swtxy.util.Draw2DUtils;
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.Figure;
 import org.eclipse.draw2d.FigureListener;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.Shape;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.PointList;
 import org.eclipse.draw2d.geometry.PrecisionPoint;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.swt.SWT;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
 import uk.ac.diamond.scisoft.analysis.fitting.CircleFitter;
 import uk.ac.diamond.scisoft.analysis.roi.CircularFitROI;
 import uk.ac.diamond.scisoft.analysis.roi.PointROI;
 import uk.ac.diamond.scisoft.analysis.roi.PolylineROI;
 import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
 
 public class CircleFitSelection extends AbstractSelectionRegion {
 	private final static Logger logger = LoggerFactory.getLogger(CircleFitSelection.class);
 
 	private static final int MIN_POINTS = 3; // minimum number of points to define ellipse
 	DecoratedCircle circle;
 	private CircleFitter fitter;
 
 	public CircleFitSelection(String name, ICoordinateSystem coords) {
 		super(name, coords);
 		setRegionColor(ColorConstants.yellow);
 		setAlpha(80);
 		setLineWidth(2);
 		fitter = new CircleFitter();
 	}
 
 	@Override
 	public void createContents(Figure parent) {
 		circle = new DecoratedCircle(parent);
 		circle.setCursor(Draw2DUtils.getRoiMoveCursor());
 
 		parent.add(circle);
 		sync(getBean());
 		circle.setForegroundColor(getRegionColor());
 		circle.setAlpha(getAlpha());
 		circle.setLineWidth(getLineWidth());
 		updateROI();
 		if (roi == null)
 			createROI(true);
 	}
 
 	@Override
 	public boolean containsPoint(double x, double y) {
 		final int[] pix = coords.getValuePosition(x,y);
 		return circle.containsPoint(pix[0], pix[1]);
 	}
 
 	@Override
 	public RegionType getRegionType() {
 		return RegionType.CIRCLE;
 	}
 
 	@Override
 	protected void updateConnectionBounds() {
 		if (circle != null) {
 			circle.updateFromHandles();
 			Rectangle b = circle.getBounds();
 			if (b != null)
 				circle.setBounds(b);
 		}
 	}
 
 	private void fitPoints(PointList pts, DecoratedCircle circle) {
 		if (pts == null)
 			return;
 
 		final int n = pts.size();
 		final double[] x = new double[n];
 		final double[] y = new double[n];
 		final Point p = new Point();
 		for (int i = 0; i < n; i++) {
 			pts.getPoint(p, i);
 			double[] c = coords.getPositionValue(p.x(), p.y());
 			x[i] = c[0];
 			y[i] = c[1];
 		}
 		AbstractDataset xds = new DoubleDataset(x, n); 
 		AbstractDataset yds = new DoubleDataset(y, n);
 		try {
 			fitter.geometricFit(xds, yds, null);
 			final double[] parameters = fitter.getParameters();
 
 			int[] pnt1 = coords.getValuePosition(parameters[0] + parameters[1], 0);
 			int[] pnt2 = coords.getValuePosition(parameters[1], parameters[2]);
 			
 			circle.setRadius(pnt1[0] - pnt2[0]);
 			circle.setCentre(pnt2[0], pnt2[1]);
 		} catch (IllegalArgumentException e) {
 			logger.info("Can not fit current selection");
 		}
 	}
 
 	private DecoratedCircle tempCircle;
 
 	@Override
 	public void paintBeforeAdded(Graphics g, PointList clicks, Rectangle parentBounds) {
 		g.setLineWidth(2);
 		g.setForegroundColor(getRegionColor());
 		g.setAlpha(getAlpha());
 		g.setLineStyle(Graphics.LINE_DOT);
 		g.drawPolyline(clicks);
 		if (clicks.size() >= MIN_POINTS) {
 			if (tempCircle == null) {
 				tempCircle = new DecoratedCircle();
 				tempCircle.setOutline(true);
 				tempCircle.setFill(false);
 			}
 			fitPoints(clicks, tempCircle);
 			tempCircle.paintFigure(g);
 		}
 	}
 
 	@Override
 	public void setLocalBounds(PointList clicks, Rectangle parentBounds) {
 		if (circle != null) {
 			circle.setup(clicks);
 			setRegionColor(getRegionColor());
 			setOpaque(false);
 			setAlpha(getAlpha());
 			updateConnectionBounds();
 			fireROIChanged(getROI());
 		}
 	}
 
 	@Override
 	protected String getCursorPath() {
 		return "icons/Cursor-circle.png";
 	}
 
 	@Override
 	protected ROIBase createROI(boolean recordResult) {
 		final PointList pl = circle.getPoints();
 		if (pl == null) {
 			return null;
 		}
 
 		final PolylineROI hroi = new PolylineROI();
 		final Point p = new Point();
 		for (int i = 0, imax = pl.size(); i < imax; i++) {
 			pl.getPoint(p, i);
 			hroi.insertPoint(i, coords.getPositionValue(p.x(), p.y()));
 		}
 
 		try {
 			final CircularFitROI croi = new CircularFitROI(hroi);
 			if (recordResult) {
 				roi = croi;
 			}
 			return croi;
 		} catch (IllegalArgumentException e) {
 			// do nothing
 		}
 		return roi;
 	}
 
 	@Override
 	protected void updateROI(ROIBase roi) {
 		if (circle == null)
 			return;
 
 		if (roi instanceof CircularFitROI) {
 			circle.updateFromROI((CircularFitROI) roi);
 			updateConnectionBounds();
 		}
 	}
 
 	@Override
 	public int getMaximumMousePresses() {
 		return 0; // signifies unlimited presses
 	}
 
 	@Override
 	public int getMinimumMousePresses() {
 		return MIN_POINTS;
 	}
 
 	private static PrecisionPoint centre = new PrecisionPoint(0.5, 0.5);
 
 	class DecoratedCircle extends Shape implements IRegionContainer {
 		private AffineTransform affine; // transforms unit square (origin at top-left corner) to transformed rectangle
 		private PointList box; // bounding box of ellipse
 		private boolean outlineOnly = false;
 
 		List<IFigure> handles;
 		List<FigureTranslator> fTranslators;
 		private Figure parent;
 		private TranslationListener handleListener;
 		private FigureListener moveListener;
 		private static final int SIDE = 8;
 
 		public DecoratedCircle() {
 			super();
 			affine = new AffineTransform();
 		}
 
 		public DecoratedCircle(Figure parent) {
 			super();
 			affine = new AffineTransform();
 			handles = new ArrayList<IFigure>();
 			fTranslators = new ArrayList<FigureTranslator>();
 			this.parent = parent;
 			setFill(false);
 			handleListener = createHandleNotifier();
 			moveListener = new FigureListener() {
 				@Override
 				public void figureMoved(IFigure source) {
 					DecoratedCircle.this.parent.repaint();
 				}
 			};
 		}
 
 		/**
 		 * Set centre position
 		 * @param cx
 		 * @param cy
 		 */
 		public void setCentre(double cx, double cy) {
 			Point oc = affine.getTransformed(centre);
 			affine.setTranslation(affine.getTranslationX() + cx - oc.preciseX(), affine.getTranslationY() + cy - oc.preciseY());
 			calcBox();
 		}
 
 		/**
 		 * @return centre of circle
 		 */
 		public Point getCentre() {
 			return affine.getTransformed(centre);
 		}
 
 		/**
 		 * @return radius
 		 */
 		public double getRadius() {
 			return 0.5*affine.getScaleX();
 		}
 
 		/**
 		 * Get point on circle at given angle
 		 * @param degrees (positive for anti-clockwise)
 		 * @return
 		 */
 		public Point getPoint(double degrees) {
 			double angle = -Math.toRadians(degrees);
 			double c = Math.cos(angle);
 			double s = Math.sin(angle);
 			PrecisionPoint p = new PrecisionPoint(0.5*(c+1), 0.5*(s+1));
 			return affine.getTransformed(p);
 		}
 
 		/**
 		 * Set radius
 		 * @param radius
 		 */
 		public void setRadius(double radius) {
 			Point oc = affine.getTransformed(centre);
 			affine.setScale(2*radius);
 			Point nc = affine.getTransformed(centre);
 			affine.setTranslation(affine.getTranslationX() + oc.preciseX() - nc.preciseX(), affine.getTranslationY() + oc.preciseY() - nc.preciseY());
 			calcBox();
 		}
 
 		private void calcBox() {
 			box = affine.getTransformedUnitSquare();
			setBounds(box.getBounds().expand(2, 0));
 		}
 
 		@Override
 		public void setLocation(Point p) {
 			affine.setTranslation(p.preciseX(), p.preciseY());
 			calcBox();
 		}
 
 		@Override
 		public boolean containsPoint(int x, int y) {
 			if (outlineOnly) {
 				double d = affine.getInverseTransformed(new PrecisionPoint(x, y)).getDistance(centre);
 				return Math.abs(d - 0.5) < 2./affine.getScaleX();
 			}
 
 			if (!super.containsPoint(x, y) || !box.polygonContainsPoint(x, y))
 				return false;
 
 			Point p = affine.getInverseTransformed(new PrecisionPoint(x, y));
 			return p.getDistance(centre) <= 0.5;
 		}
 
 		@Override
 		public void setFill(boolean b) {
 			super.setFill(b);
 			outlineOnly  = !b;
 		}
 
 		@Override
 		protected void fillShape(Graphics graphics) {
 	        if (!isShapeFriendlySize()) return;
 
 	        graphics.pushState();
 			graphics.setAdvanced(true);
 			graphics.setAntialias(SWT.ON);
 			graphics.translate((int) affine.getTranslationX(), (int) affine.getTranslationY());
 			graphics.rotate((float) affine.getRotationDegrees());
 			// NB do not use Graphics#scale and unit shape as there are precision problems
 			int d = (int)affine.getScaleX();
 			graphics.fillOval(0, 0, d, d);
 			graphics.popState();
 		}
 
 		@Override
 		protected void outlineShape(Graphics graphics) {
 	        if (!isShapeFriendlySize()) return;
 
 	        graphics.pushState();
 			graphics.setAdvanced(true);
 			graphics.setAntialias(SWT.ON);
 
 			graphics.translate((int) affine.getTranslationX(), (int) affine.getTranslationY());
 			graphics.rotate((float) affine.getRotationDegrees());
 			// NB do not use Graphics#scale and unit shape as there are precision problems
 			int d = (int)affine.getScaleX();
 			graphics.drawOval(0, 0, d, d);
 			graphics.popState();
 		}
 
 		private boolean isShapeFriendlySize() {
 			IFigure p = getParent();
 			if (p == null)
 				return true;
 
 			int r = (int)affine.getScaleX();
 			
 			// If the affine transform is outside the size of the bounds, we
 			// are very likely to be off-screen. 
 			// On linux off screen is bad therefore we do not draw
 			// Fix to http://jira.diamond.ac.uk/browse/DAWNSCI-429
 			Rectangle bnds = p.getBounds().getExpanded(500, 500); // This is a fudge, very elongated do still not show.
 			                                                                // Better than crashes however...
 			if (r>bnds.width && r>bnds.height) return false;
 			return true;
 		}
 
 		@Override
 		public String toString() {
 			return "Radius " + getRadius() + ", centre " + getCentre();
 		}
 
 		public void setup(PointList points) {
 			fitPoints(points, this);
 
 			// handles
 			final Point p = new Point();
 			for (int i = 0, imax = points.size(); i < imax; i++) {
 				points.getPoint(p, i);
 				addHandle(p);
 			}
 			addCentreHandle();
 
 			// figure move (commented out for usability when tweaking handles)
 //			addFigureListener(moveListener);
 //			FigureTranslator mover = new FigureTranslator(getXyGraph(), parent, this, handles);
 //			mover.addTranslationListener(createRegionNotifier());
 
 			createROI(true);
 
 			setRegionObjects(this, handles);
 			Rectangle b = getBounds();
 			if (b != null)
 				setBounds(b);
 		}
 
 		private void addHandle(Point p) {
 			RectangularHandle h = new RectangularHandle(coords, getRegionColor(), this, SIDE,
 					p.preciseX(), p.preciseY());
 			parent.add(h);
 			FigureTranslator mover = new FigureTranslator(getXyGraph(), h);
 			mover.addTranslationListener(handleListener);
 			fTranslators.add(mover);
 			h.addFigureListener(moveListener);
 			handles.add(h);
 		}
 
 		private void addCentreHandle() {
 			Point c = getCentre();
 			RectangularHandle h = new RectangularHandle(coords, getRegionColor(), this, SIDE, c.preciseX(), c.preciseY());
 			parent.add(h);
 			FigureTranslator mover = new FigureTranslator(getXyGraph(), h, h, handles);
 			mover.addTranslationListener(createRegionNotifier());
 			fTranslators.add(mover);
 			h.addFigureListener(moveListener);
 			handles.add(h);
 		}
 
 		private void removeHandle(SelectionHandle h) {
 			parent.remove(h);
 			h.removeFigureListener(moveListener);
 			h.removeMouseListeners();
 		}
 
 		private TranslationListener createHandleNotifier() {
 			return new TranslationListener() {
 				@Override
 				public void onActivate(TranslationEvent evt) {
 				}
 
 				@Override
 				public void translateBefore(TranslationEvent evt) {
 				}
 
 				@Override
 				public void translationAfter(TranslationEvent evt) {
 					Object src = evt.getSource();
 					if (src instanceof FigureTranslator) {
 						fitPoints(getPoints(), DecoratedCircle.this);
 						if (handles.size() > 0) {
 							IFigure f = handles.get(handles.size() - 1);
 							if (f instanceof SelectionHandle) {
 								SelectionHandle h = (SelectionHandle) f;
 								h.setSelectionPoint(getCentre());
 							}
 						}
 						fireROIDragged(createROI(false), ROIEvent.DRAG_TYPE.RESIZE);
 					}
 				}
 
 				@Override
 				public void translationCompleted(TranslationEvent evt) {
 					Object src = evt.getSource();
 					if (src instanceof FigureTranslator) {
 						fireROIChanged(createROI(true));
 						fireROISelection();
 					}
 				}
 			};
 		}
 
 		/**
 		 * @return list of handle points (can be null)
 		 */
 		public PointList getPoints() {
 			int imax = handles.size() - 1;
 			if (imax < 0)
 				return null;
 
 			PointList pts = new PointList(imax);
 			for (int i = 0; i < imax; i++) {
 				IFigure f = handles.get(i);
 				if (f instanceof SelectionHandle) {
 					SelectionHandle h = (SelectionHandle) f;
 					pts.addPoint(h.getSelectionPoint());
 				}
 			}
 			return pts;
 		}
 
 		/**
 		 * Update selection according to centre handle
 		 */
 		private void updateFromHandles() {
 			if (handles.size() > 0) {
 				IFigure f = handles.get(handles.size() - 1);
 				if (f instanceof SelectionHandle) {
 					SelectionHandle h = (SelectionHandle) f;
 					Point p = h.getSelectionPoint();
 					setCentre(p.preciseX(), p.preciseY());
 					double[] parameters = fitter.getParameters();
 					double[] ps   = coords.getPositionValue(p.x(), p.y());
 					parameters[1] = ps[0];
 					parameters[2] = ps[1];
 				}
 			}
 		}
 
 		@Override
 		public Rectangle getBounds() {
 			Rectangle b = super.getBounds();
 			if (handles != null)
 				for (IFigure f : handles) {
 					if (f instanceof SelectionHandle) {
 						SelectionHandle h = (SelectionHandle) f;
 						b.union(h.getBounds());
 					}
 				}
 			return b;
 		}
 
 		/**
 		 * Update according to ROI
 		 * @param sroi
 		 */
 		public void updateFromROI(CircularFitROI croi) {
 			final double[] xy = croi.getPointRef();
 			int[] p1 = coords.getValuePosition(xy[0], xy[1]);
 			double r = croi.getRadius();
 			int[] p2 = coords.getValuePosition(r + xy[0], r + xy[1]);
 
 			setRadius(p2[0] - p1[0]);
 
 			setCentre(p1[0], p1[1]);
 
 			int imax = handles.size() - 1;
 			PolylineROI proi = croi.getPoints();
 
 			if (imax != proi.getNumberOfPoints()) {
 				for (int i = imax; i >= 0; i--) {
 					removeHandle((SelectionHandle) handles.remove(i));
 					fTranslators.remove(i).removeTranslationListeners();
 				}
 				imax = proi.getNumberOfPoints();
 				for (int i = 0; i < imax; i++) {
 					PointROI p = proi.getPoint(i);
 					int[] pos = coords.getValuePosition(p.getPoint());
 					Point np = new Point(pos[0], pos[1]);
 					addHandle(np);
 				}
 				addCentreHandle();
 				setRegionObjects(this, handles);
 			} else {
 				for (int i = 0; i < imax; i++) {
 					PointROI p = proi.getPoint(i);
 					int[] pos = coords.getValuePosition(p.getPoint());
 					Point np = new Point(pos[0], pos[1]);
 					SelectionHandle h = (SelectionHandle) handles.get(i);
 					h.setSelectionPoint(np);
 				}
 				SelectionHandle h = (SelectionHandle) handles.get(imax);
 				h.setSelectionPoint(getCentre());
 			}
 		}
 
 		@Override
 		public IRegion getRegion() {
 			return CircleFitSelection.this;
 		}
 
 		@Override
 		public void setRegion(IRegion region) {
 		}
 	}
 }
