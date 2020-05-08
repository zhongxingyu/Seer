 package org.dawb.workbench.plotting.system.swtxy.selection;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.csstudio.swt.xygraph.figures.Axis;
 import org.dawb.common.ui.plot.region.IRegion;
 import org.dawb.common.ui.plot.region.IRegionContainer;
 import org.dawb.workbench.plotting.system.swtxy.translate.FigureTranslator;
 import org.dawb.workbench.plotting.system.swtxy.translate.TranslationEvent;
 import org.dawb.workbench.plotting.system.swtxy.translate.TranslationListener;
 import org.dawb.workbench.plotting.system.swtxy.util.Draw2DUtils;
 import org.dawb.workbench.plotting.system.swtxy.util.RotatableEllipse;
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.Figure;
 import org.eclipse.draw2d.FigureListener;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.PointList;
 import org.eclipse.draw2d.geometry.Rectangle;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
 import uk.ac.diamond.scisoft.analysis.fitting.EllipseFitter;
 import uk.ac.diamond.scisoft.analysis.roi.EllipticalROI;
 import uk.ac.diamond.scisoft.analysis.roi.PointROI;
 import uk.ac.diamond.scisoft.analysis.roi.PolygonalROI;
 import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
 
 public class EllipseFitSelection extends AbstractSelectionRegion {
 
 	private static final int MIN_POINTS = 5; // minimum number of points to define ellipse
 	DecoratedEllipse ellipse;
 	private EllipseFitter fitter;
 
 	public EllipseFitSelection(String name, Axis xAxis, Axis yAxis) {
 		super(name, xAxis, yAxis);
 		setRegionColor(ColorConstants.red);
 		setAlpha(80);
 		setLineWidth(2);
 		fitter = new EllipseFitter();
 	}
 
 	@Override
 	public void createContents(Figure parent) {
 		ellipse = new DecoratedEllipse(parent);
 		ellipse.setCursor(Draw2DUtils.getRoiMoveCursor());
 
 		parent.add(ellipse);
 		sync(getBean());
 		ellipse.setLineWidth(getLineWidth());
 		updateROI();
 		if (roi == null)
 			createROI(true);
 	}
 	
 	@Override
 	public boolean containsPoint(double x, double y) {
 		final int xpix = xAxis.getValuePosition(x, false);
 		final int ypix = yAxis.getValuePosition(y, false);
 		return ellipse.containsPoint(xpix, ypix);
 	}
 
 	@Override
 	public RegionType getRegionType() {
 		return RegionType.ELLIPSE;
 	}
 
 	@Override
 	protected void updateConnectionBounds() {
 		if (ellipse != null) {
 			ellipse.updateFromHandles();
 			Rectangle b = ellipse.getBounds();
 			if (b != null)
 				ellipse.setBounds(b);
 		}
 	}
 
 	private void fitPoints(PointList pts, RotatableEllipse ellipse) {
 		final int n = pts.size();
 		final double[] x = new double[n];
 		final double[] y = new double[n];
 		final Point p = new Point();
 		for (int i = 0; i < n; i++) {
 			pts.getPoint(p, i);
 			x[i] = xAxis.getPositionValue(p.x(), false);
 			y[i] = yAxis.getPositionValue(p.y(), false);
 		}
 		AbstractDataset xds = new DoubleDataset(x, n); 
 		AbstractDataset yds = new DoubleDataset(y, n);
 		fitter.geometricFit(xds, yds, null);
 		final double[] parameters = fitter.getParameters();
 //		System.err.println("Fit: " + Arrays.toString(parameters));
 		ellipse.setAxes(xAxis.getValuePosition(2*parameters[0] + parameters[3], false) - xAxis.getValuePosition(parameters[3], false),
 				yAxis.getValuePosition(2*parameters[1] + parameters[4], false) - yAxis.getValuePosition(parameters[4], false));
 		ellipse.setCentre(xAxis.getValuePosition(parameters[3], false), yAxis.getValuePosition(parameters[4], false));
 		ellipse.setAngle(Math.toDegrees(parameters[2]));
 	}
 
 	private RotatableEllipse tempEllipse;
 	private PolygonalROI proi; // holds points that ellipse is fitted to
 
 	@Override
 	public void paintBeforeAdded(Graphics g, PointList clicks, Rectangle parentBounds) {
 		g.setLineWidth(2);
 		g.setForegroundColor(getRegionColor());
 		g.setAlpha(getAlpha());
 		g.setLineStyle(Graphics.LINE_DOT);
 		g.drawPolyline(clicks);
 		if (clicks.size() >= MIN_POINTS) {
 			if (tempEllipse == null) {
 				tempEllipse = new RotatableEllipse();
 				tempEllipse.setOutline(true);
 				tempEllipse.setFill(false);
 			}
 			fitPoints(clicks, tempEllipse);
 			tempEllipse.paintFigure(g);
 		}
 	}
 
 	@Override
 	public void setLocalBounds(PointList clicks, Rectangle parentBounds) {
 		if (ellipse != null) {
 			ellipse.setup(clicks);
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
 		final double[] parameters = fitter.getParameters();
 		final EllipticalROI eroi = new EllipticalROI(parameters[0], parameters[1], parameters[2], parameters[3], parameters[4]);
 		final PointList pl = ellipse.getPoints(true);
 		final PolygonalROI hroi = new PolygonalROI();
 		final Point p = new Point();
 		for (int i = 0, imax = pl.size(); i < imax; i++) {
 			pl.getPoint(p, i);
 			hroi.insertPoint(i, xAxis.getPositionValue(p.x(), false), yAxis.getPositionValue(p.y(), false));
 		}
 
 		if (recordResult) {
 			roi = eroi;
 			proi = hroi;
 		}
 		return eroi;
 	}
 
 	@Override
 	protected void updateROI(ROIBase roi) {
 		if (roi instanceof EllipticalROI) {
 			if (ellipse == null)
 				return;
 
 			ellipse.updateFromROI((EllipticalROI) roi, proi);
 
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
 
 	class DecoratedEllipse extends RotatableEllipse implements IRegionContainer {
 		List<IFigure> handles;
 		private Figure parent;
 		private static final int SIDE = 8;
 		public DecoratedEllipse(Figure parent) {
 			super();
 			handles = new ArrayList<IFigure>();
 			this.parent = parent;
 			setFill(false);
 		}
 
 		public void setup(PointList points) {
 			fitPoints(points, this);
 
 			FigureListener listener = new FigureListener() {
 				@Override
 				public void figureMoved(IFigure source) {
 					parent.repaint();
 				}
 			};
 
 			final TranslationListener hListener = createHandleNotifier();
 			// handles
 			FigureTranslator mover;
 			final Point p = new Point();
 			for (int i = 0, imax = points.size(); i < imax; i++) {
 				points.getPoint(p, i);
 //				System.err.println("Pt " + i + ": " + p);
 				RectangularHandle h = new RectangularHandle(xAxis, yAxis, getRegionColor(), this, SIDE,
 						p.preciseX(), p.preciseY());
 				parent.add(h);
 				mover = new FigureTranslator(getXyGraph(), h);
 				mover.addTranslationListener(hListener);
 				h.addFigureListener(listener);
 				handles.add(h);
 			}
 
 			Point c = getCentre();
 			RectangularHandle h = new RectangularHandle(xAxis, yAxis, getRegionColor(), this, SIDE, c.preciseX(), c.preciseY());
 			parent.add(h);
 			mover = new FigureTranslator(getXyGraph(), h, h, handles);
 			mover.addTranslationListener(createRegionNotifier());
 			h.addFigureListener(listener);
 			handles.add(h);
 
			createROI(true);

 			addFigureListener(listener);
 			mover = new FigureTranslator(getXyGraph(), parent, this, handles);
 			mover.addTranslationListener(createRegionNotifier());
 			setRegionObjects(this, handles);
 			Rectangle b = getBounds();
 			if (b != null)
 				setBounds(b);
 		}
 
 		public TranslationListener createHandleNotifier() {
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
 						fitPoints(getPoints(false), DecoratedEllipse.this);
 						EllipticalROI eroi = (EllipticalROI) createROI(false);
 						fireROIDragged(eroi);
 					}
 				}
 
 				@Override
 				public void translationCompleted(TranslationEvent evt) {
 					Object src = evt.getSource();
 					if (src instanceof FigureTranslator) {
 						fitPoints(getPoints(false), DecoratedEllipse.this);
 						EllipticalROI eroi = (EllipticalROI) createROI(true);
 						fireROIDragged(eroi);
 						fireROISelection();
 					}
 				}
 			};
 		}
 
 		/**
 		 * @param includeCentre if true, include centre handle
 		 * @return list of handle points
 		 */
 		public PointList getPoints(boolean includeCentre) {
 			int imax = handles.size();
 			if (!includeCentre)
 				imax--;
 
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
 		public void updateFromROI(EllipticalROI eroi, PolygonalROI proi) {
 			ellipse.setAxes(xAxis.getValuePosition(2*eroi.getSemiAxis(0) + eroi.getPointX(), false) - xAxis.getValuePosition(eroi.getPointX(), false),
 					yAxis.getValuePosition(2*eroi.getSemiAxis(1) + eroi.getPointY(), false) - yAxis.getValuePosition(eroi.getPointY(), false));
 
 			ellipse.setCentre(xAxis.getValuePosition(eroi.getPointX(), false), yAxis.getValuePosition(eroi.getPointY(), false));
 			ellipse.setAngle(eroi.getAngleDegrees());
 
 			final PointList pl = getPoints(true);
 			final int imax = handles.size();
 			if (imax != proi.getSides())
 				return;
 
 			for (int i = 0; i < imax; i++) {
 				PointROI p = proi.getPoint(i);
 				Point np = new Point(xAxis.getValuePosition(p.getPointX(), false), yAxis.getValuePosition(p.getPointY(), false));
 				pl.setPoint(np, i);
 				SelectionHandle h = (SelectionHandle) handles.get(i);
 				h.setSelectionPoint(np);
 			}
 		}
 
 		@Override
 		public IRegion getRegion() {
 			return EllipseFitSelection.this;
 		}
 
 		@Override
 		public void setRegion(IRegion region) {
 		}
 	}
 }
