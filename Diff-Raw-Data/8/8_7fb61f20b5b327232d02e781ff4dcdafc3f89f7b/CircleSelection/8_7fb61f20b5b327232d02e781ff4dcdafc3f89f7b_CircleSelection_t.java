 /*-
  * Copyright 2012 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.dawnsci.plotting.draw2d.swtxy.selection;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.dawnsci.plotting.api.axis.ICoordinateSystem;
 import org.dawnsci.plotting.api.region.IRegion;
 import org.dawnsci.plotting.api.region.IRegionContainer;
 import org.dawnsci.plotting.api.region.ROIEvent;
 import org.dawnsci.plotting.draw2d.swtxy.translate.FigureTranslator;
 import org.dawnsci.plotting.draw2d.swtxy.translate.TranslationEvent;
 import org.dawnsci.plotting.draw2d.swtxy.translate.TranslationListener;
 import org.dawnsci.plotting.draw2d.swtxy.util.Draw2DUtils;
 import org.dawnsci.plotting.draw2d.swtxy.util.PointFunction;
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.Figure;
 import org.eclipse.draw2d.FigureListener;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.Shape;
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.PointList;
 import org.eclipse.draw2d.geometry.PrecisionPoint;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.swt.SWT;
 
 import uk.ac.diamond.scisoft.analysis.roi.CircularROI;
 import uk.ac.diamond.scisoft.analysis.roi.IROI;
 
 public class CircleSelection extends AbstractSelectionRegion {
 
 	DecoratedCircle circle;
 
 	public CircleSelection(String name, ICoordinateSystem coords) {
 		super(name, coords);
 		setRegionColor(ColorConstants.yellow);
 		setAlpha(80);
 		setLineWidth(2);
 	}
 
 	@Override
 	public void setVisible(boolean visible) {
 		if (circle != null)
 			circle.setVisible(visible);
 		getBean().setVisible(visible);
 	}
 
 	@Override
 	public void setMobile(final boolean mobile) {
 		getBean().setMobile(mobile);
 		if (circle != null)
 			circle.setMobile(mobile);
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
 		final int[] pix = coords.getValuePosition(x, y);
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
 
 	@Override
 	public void paintBeforeAdded(Graphics g, PointList clicks, Rectangle parentBounds) {
 		if (clicks.size() <= 1)
 			return;
 
 		g.setLineStyle(SWT.LINE_DOT);
 		g.setLineWidth(2);
 		g.setForegroundColor(getRegionColor());
 		g.setAlpha(getAlpha());
 		Rectangle r = new Rectangle(clicks.getFirstPoint(), clicks.getLastPoint());
 		if (r.width < r.height) {
 			r.width = r.height;
 		} else {
 			r.height = r.width;
 		}
 		double ratio = coords.getAspectRatio();
 		r.height /= ratio;
 		g.drawOval(r);
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
 	protected IROI createROI(boolean recordResult) {
 		final CircularROI croi = new CircularROI();
 		Point p = circle.getCentre();
 		croi.setPoint(coords.getPositionValue(p.x(), p.y()));
 		double r = circle.getRadius();
 		double[] v = coords.getPositionValue((int) (p.preciseX() + r), (int) (p.preciseY() + r));
 		croi.setRadius(v[0] - croi.getPointX()); // NB do not use y as aspect ratio can change(!)
 		croi.setName(getName());
 		if (roi!=null) croi.setPlot(roi.isPlot());
 		// set the Region isActive flag
 		this.setActive(this.isActive());
 		if (recordResult) {
 			roi = croi;
 		}
 		return croi;
 	}
 
 	@Override
 	protected void updateROI(IROI roi) {
 		if (circle == null)
 			return;
 
 		if (roi instanceof CircularROI) {
 			circle.updateFromROI((CircularROI) roi);
 			updateConnectionBounds();
 		}
 	}
 
 	@Override
 	public int getMaximumMousePresses() {
 		return 2;
 	}
 
 	class DecoratedCircle extends Shape implements IRegionContainer, PointFunction {
 		private int tolerance = 2;
 		double radius = 1;
 		double cx, cy;
 		private boolean outlineOnly = true;
 
 		List<IFigure> handles;
 		List<FigureTranslator> fTranslators;
 		private Figure parent;
 		private TranslationListener handleListener;
 		private FigureListener moveListener;
 		private static final int SIDE = 8;
 
 		public DecoratedCircle(Figure parent) {
 			super();
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
 
 		public void setup(PointList corners) {
			Rectangle r = new Rectangle(corners.getFirstPoint(), corners.getLastPoint());
 			if (r.width < r.height) {
 				r.width = r.height;
 			} else {
 				r.height = r.width;
 			}
 			setRadius(0.5*r.width);
 			
 			Point c = r.getCenter();
 			setCentre(c.preciseX(), c.preciseY());
 
 			createHandles(true);
 		}
 
 		private void createHandles(boolean createROI) {
 			// handles
 			for (int i = 0; i < 4; i++) {
 				addHandle(getPoint(i*90));
 			}
 			addCentreHandle();
 
 			// figure move
 			addFigureListener(moveListener);
 			FigureTranslator mover = new FigureTranslator(getXyGraph(), parent, this, handles);
 			mover.setActive(isMobile());
 			mover.addTranslationListener(createRegionNotifier());
 			fTranslators.add(mover);
 
 			if (createROI)
 				createROI(true);
 
 			setRegionObjects(this, handles);
 			Rectangle b = getBounds();
 			if (b != null)
 				setBounds(b);
 		}
 
 		@Override
 		public void setVisible(boolean visible) {
 			super.setVisible(visible);
 			for (IFigure h : handles) {
 				h.setVisible(visible);
 			}
 		}
 
 		public void setMobile(boolean mobile) {
 			for (IFigure h : handles) {
 				h.setVisible(mobile);
 			}
 			for (FigureTranslator f : fTranslators) {
 				f.setActive(mobile);
 			}
 		}
 
 		private Point getPoint(double degrees) {
 			double angle = -Math.toRadians(degrees);
 			double ratio = coords.getAspectRatio();
 			return new PrecisionPoint(radius * Math.cos(angle) + cx, radius * Math.sin(angle) * ratio + cy);
 		}
 
 		@Override
 		public Point calculatePoint(double... parameter) {
 			return getPoint(parameter[0]);
 		}
 
 		private void setCentre(double x, double y) {
 			cx = x;
 			cy = y;
 		}
 
 		private void setRadius(double r) {
 			radius = r;
 		}
 
 		private double getRadius() {
 			return radius;
 		}
 
 		private Point getCentre() {
 			return new PrecisionPoint(cx, cy);
 		}
 
 		@Override
 		protected void fillShape(Graphics graphics) {
 //			graphics.setBackgroundColor(ColorConstants.cyan);
 //			
 //			PointList points = Draw2DUtils.generateCurve(DecoratedCircle.this, 0, 360, 1, 3, Math.toRadians(1));
 //			Draw2DUtils.fillClippedPolygon(graphics, points, getParent().getBounds());
 		}
 		
 		@Override
 		protected void outlineShape(Graphics graphics) {
 			PointList points = Draw2DUtils.generateCurve(DecoratedCircle.this, 0, 360, 1, 3, Math.toRadians(1));
 			Draw2DUtils.drawClippedPolyline(graphics, points, getParent().getBounds(), true);
 
 			if (label != null && isShowLabel()) {
 				graphics.setAlpha(192);
 				graphics.setForegroundColor(labelColour);
 				graphics.setBackgroundColor(ColorConstants.white);
 				graphics.setFont(labelFont);
 				graphics.fillString(label, getPoint(45));
 			}
 		}
 
 		// remove handles from parent and draw directly...
 		private void addHandle(Point p) {
 			RectangularHandle h = new RectangularHandle(coords, getRegionColor(), this, SIDE,
 					p.preciseX(), p.preciseY());
 			h.setVisible(isVisible() && isMobile());
 			parent.add(h);
 			FigureTranslator mover = new FigureTranslator(getXyGraph(), h);
 			mover.setActive(isMobile());
 			mover.addTranslationListener(handleListener);
 			fTranslators.add(mover);
 			h.addFigureListener(moveListener);
 			handles.add(h);
 		}
 
 		private void addCentreHandle() {
 			RectangularHandle h = new RectangularHandle(coords, getRegionColor(), this, SIDE, cx, cy);
 			h.setVisible(isVisible() && isMobile());
 			parent.add(h);
 			FigureTranslator mover = new FigureTranslator(getXyGraph(), h, h, handles);
 			mover.setActive(isMobile());
 			mover.addTranslationListener(createRegionNotifier());
 			fTranslators.add(mover);
 			h.addFigureListener(moveListener);
 			handles.add(h);
 		}
 
 		@Override
 		public String toString() {
 			return "CirSel: cen=" + getCentre() + ", rad=" + getRadius();
 		}
 
 		@Override
 		public void setFill(boolean b) {
 			super.setFill(b);
 			outlineOnly   = !b;
 		}
 
 		@Override
 		public boolean containsPoint(int x, int y) {
 			double ratio = coords.getAspectRatio(); 
 			double r = Math.hypot(x - cx, (y - cy)/ratio);
 
 			if (outlineOnly) {
 				return Math.abs(r - radius) < tolerance; 
 			}
 
 			return r <= radius;
 		}
 
 		/**
 		 * Set tolerance of hit detection of shape
 		 * @param tolerance (number of pixels between point and segment)
 		 */
 		public void setTolerance(int tolerance) {
 			this.tolerance = tolerance;
 		}
 
 		private TranslationListener createHandleNotifier() {
 			return new TranslationListener() {
 				SelectionHandle ha;
 				private boolean major;
 				private Point centre;
 
 				@Override
 				public void onActivate(TranslationEvent evt) {
 					Object src = evt.getSource();
 					if (src instanceof FigureTranslator) {
 						ha = (SelectionHandle) ((FigureTranslator) src).getRedrawFigure();
 						centre = getCentre();
 						int current = handles.indexOf(ha);
 						switch (current) {
 						case 0: case 2:
 							major = true;
 							break;
 						case 1: case 3:
 							major = false;
 							break;
 						default:
 							ha = null;
 							centre = null;
 							break;
 						}
 					}
 				}
 
 				@Override
 				public void translateBefore(TranslationEvent evt) {
 				}
 
 				@Override
 				public void translationAfter(TranslationEvent evt) {
 					Object src = evt.getSource();
 					if (src instanceof FigureTranslator) {
 						if (ha != null) {
 							Point pa = new PrecisionPoint(ha.getSelectionPoint());
 							Dimension d = pa.getDifference(centre);
 							double r = major ? Math.abs(d.preciseWidth()) : Math.abs(d.preciseHeight());
 							setRadius(r);
 							updateHandlePositions();
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
 					ha = null;
 					centre = null;
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
 		public void updateFromROI(CircularROI croi) {
 			final double[] xy = croi.getPointRef();
 			int[] p1 = coords.getValuePosition(xy[0], xy[1]);
 			int[] p2 = coords.getValuePosition(croi.getRadius() + xy[0], croi.getRadius() + xy[1]);
 
 			setRadius(p2[0] - p1[0]);
 
 			setCentre(p1[0], p1[1]);
 
 			updateHandlePositions();
 		}
 
 		private void updateHandlePositions() {
 			final int imax = handles.size() - 1;
 			if (imax > 0) {
 				for (int i = 0; i < imax; i++) {
 					Point np = getPoint(90 * i);
 					SelectionHandle h = (SelectionHandle) handles.get(i);
 					h.setSelectionPoint(np);
 				}
 				SelectionHandle h = (SelectionHandle) handles.get(imax);
 				h.setSelectionPoint(getCentre());
 			} else {
 				createHandles(false);
 			}
 		}
 
 		@Override
 		public IRegion getRegion() {
 			return CircleSelection.this;
 		}
 
 		@Override
 		public void setRegion(IRegion region) {
 		}
 	}
 }
