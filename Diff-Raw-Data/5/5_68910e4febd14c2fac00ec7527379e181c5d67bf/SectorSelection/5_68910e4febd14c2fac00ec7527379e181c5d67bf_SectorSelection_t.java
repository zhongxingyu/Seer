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
 import org.dawnsci.plotting.api.region.ILockableRegion;
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
 import org.eclipse.draw2d.MouseEvent;
 import org.eclipse.draw2d.Shape;
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.PointList;
 import org.eclipse.draw2d.geometry.PrecisionPoint;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 
 import uk.ac.diamond.scisoft.analysis.roi.IROI;
 import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
 import uk.ac.diamond.scisoft.analysis.roi.SectorROI;
 import uk.ac.diamond.scisoft.analysis.roi.handler.HandleStatus;
 import uk.ac.diamond.scisoft.analysis.roi.handler.SectorROIHandler;
 
 /**
  * You should not call this concrete class outside of the draw2d 
  * extensions unless absolutely required.
  */
 class SectorSelection extends AbstractSelectionRegion implements ILockableRegion {
 
 	Sector sector;
 
 	SectorSelection(String name, ICoordinateSystem coords) {
 		super(name, coords);
 		setRegionColor(ColorConstants.red);
 		setAlpha(80);
 		setLineWidth(2);
 	}
 	
 	public void setRegionColor(Color regionColor) {
 
 		super.setRegionColor(regionColor);
 		if (sector!=null) {
 			sector.setForegroundColor(regionColor);
 			sector.setBackgroundColor(regionColor);
 		}
 	}
 
 	@Override
 	public void setMobile(boolean mobile) {
 		super.setMobile(mobile);
 		if (sector != null)
 			sector.setMobile(mobile);
 	}
 
 	@Override
 	public void createContents(Figure parent) {
 		sector = new Sector(parent, coords);
 		sector.setCursor(Draw2DUtils.getRoiMoveCursor());
 
 		parent.add(sector);
 		sync(getBean());
 		sector.setLineWidth(getLineWidth());
 	}
 
 	@Override
 	public boolean containsPoint(int x, int y) {
 		return sector.containsPoint(x, y);
 	}
 
 	@Override
 	public RegionType getRegionType() {
 		return RegionType.SECTOR;
 	}
 
 	@Override
 	protected void updateBounds() { // called after a handle translation
 		if (sector != null) {
 			Rectangle b = sector.updateFromHandles();
 			if (b != null)
 				sector.setBounds(b);
 		}
 	}
 
 	@Override
 	public void paintBeforeAdded(Graphics g, PointList clicks, Rectangle parentBounds) {
 		if (clicks.size() < 2)
 			return;
 
 		g.setLineStyle(Graphics.LINE_DOT);
 		g.setForegroundColor(getRegionColor());
 		g.setAlpha(getAlpha());
 
 		final Point cen = clicks.getFirstPoint();
 		Point inn = clicks.getPoint(1);
 		Dimension rd = inn.getDifference(cen);
 		double ratio = coords.getAspectRatio();
 		double h = -rd.preciseHeight() / ratio;
 		double w = rd.preciseWidth();
 		final double ri = Math.hypot(w, h);
 		if (clicks.size() == 2) {
 			g.setLineWidth(getLineWidth());
 			g.drawOval((int) Math.round(cen.preciseX() - ri), (int) Math.round(cen.preciseY() - ri * ratio),
 					(int) Math.round(2*ri), (int) Math.round(2*ri*ratio));
 		} else {
 			Sector s = new Sector(null, coords);
 			s.setup(clicks);
  		    s.setLineStyle(Graphics.LINE_DOT);
 			s.setLineWidth(getLineWidth());
 			s.setForegroundColor(getRegionColor());
 			s.setBackgroundColor(getRegionColor());
 			s.paintFigure(g);
 		}
 	}
 
 	private Boolean clockwise = null;
 	private final static double ONE_PI = Math.PI;
 	private final static double TWO_PI = 2.0 * Math.PI;
 	private double[] calcAngles(double anglea, double angleb) {
 		if (anglea < 0)
 			anglea += TWO_PI;
 		if (angleb < 0)
 			angleb += TWO_PI;
 		if (clockwise == null) {
 			if (anglea == 0) {
 				clockwise = angleb > ONE_PI;
 			} else {
 				clockwise = anglea > angleb;
 			}
 		}
 
 		double l;
 		if (clockwise) {
 			if (anglea < ONE_PI) {
 				if (angleb < ONE_PI) {
 					l = angleb - anglea;
 					if (l > 0)
 						l -= TWO_PI;
 				} else
 					l = angleb - TWO_PI - anglea;
 			} else {
 				if (angleb < ONE_PI) {
 					l = angleb - anglea;
 				} else {
 					l = angleb - anglea;
 					if (l > 0)
 						l -= TWO_PI;
 				}
 			}
 		} else {
 			if (anglea < ONE_PI) {
 				if (angleb < ONE_PI) {
 					l = angleb - anglea;
 					if (l < 0)
 						l += TWO_PI;
 				} else
 					l = angleb - anglea;
 			} else {
 				if (angleb < ONE_PI)
 					l = angleb - anglea + TWO_PI;
 				else {
 					l = angleb - anglea;
 					if (l < 0)
 						l += TWO_PI;
 				}
 			}
 		}
 
 		return l < 0 ? new double[] {anglea + l, anglea} : new double[] {anglea, anglea + l};
 	}
 
 	@Override
 	public void initialize(PointList clicks) {
 		if (sector != null) {
 			sector.setup(clicks);
 			fireROIChanged(getROI());
 		}
 	}
 
 	@Override
 	protected String getCursorPath() {
 		return "icons/Cursor-sector.png";
 	}
 
 	@Override
 	protected IROI createROI(boolean recordResult) {
 		if (recordResult) {
 			roi = sector.croi;
 		}
 		return sector.croi;
 	}
 
 	@Override
 	protected void updateRegion() {
 		if (sector != null && roi instanceof SectorROI) {
 			sector.updateFromROI((SectorROI) roi);
 			sync(getBean());
 		}
 	}
 
 	@Override
 	public int getMaximumMousePresses() {
 		return 3;
 	}
 
 	@Override
 	public void dispose() {
 		super.dispose();
 		if (sector != null) {
 			sector.dispose();
 		}
 	}
 
 	private boolean isCentreMovable=true;
 
 	class Sector extends Shape implements IRegionContainer, PointFunction {
 		private List<IFigure> handles;
 		private List<FigureTranslator> fTranslators;
 		private Figure parent;
 		private static final int SIDE = 8;
 		private SectorROIHandler roiHandler;
 		private TranslationListener handleListener;
 		private FigureListener moveListener;
 		private boolean isMobile;
 		private Rectangle bnds;
 		private boolean dirty = true;
 		private ICoordinateSystem cs;
 		private SectorROI croi;
 		private SectorROI troi = null; // temporary ROI used in dragging
 		private PointFunction innerFunction;
 		private PointFunction outerFunction;
 
 		public Sector(Figure parent, ICoordinateSystem system) {
 			super();
 			this.parent = parent;
 			cs = system;
 			handles = new ArrayList<IFigure>();
 			fTranslators = new ArrayList<FigureTranslator>();
 			roiHandler = new SectorROIHandler((SectorROI) roi);
 			handleListener = createHandleNotifier();
 			moveListener = new FigureListener() {
 				@Override
 				public void figureMoved(IFigure source) {
 					Sector.this.parent.repaint();
 				}
 			};
 			setBackgroundColor(getRegionColor());
 
 			innerFunction = new PointFunction.Stub() {
 				@Override
 				public Point calculatePoint(double... parameter) {
 					return Sector.this.getPoint(parameter[0], 0);
 				}
 			};
 
 			outerFunction = new PointFunction.Stub() {
 				@Override
 				public Point calculatePoint(double... parameter) {
 					return Sector.this.getPoint(parameter[0], 1);
 				}
 			};
 		}
 
 		public SectorROI getROI() {
 			return troi != null ? troi : croi;
 		}
 
 		public void setCentre(Point nc) {
 			double[] pt = cs.getPositionValue(nc.x(), nc.y());
 			double[] pc = croi.getPointRef();
 			pt[0] -= pc[0];
 			pt[1] -= pc[1];
 			croi.addPoint(pt);
 			dirty = true;
 			calcBox(croi, true);
 		}
 
 		public void dispose() {
 			for (IFigure f : handles) {
 				((SelectionHandle) f).removeMouseListeners();
 			}
 			for (FigureTranslator t : fTranslators) {
 				t.removeTranslationListeners();
 			}
 			removeFigureListener(moveListener);
 		}
 
 		/**
 		 * Set up sector according to clicks
 		 * @param points
 		 */
 		public void setup(PointList points) {
 			final Point cen = points.getFirstPoint();
 			double[] pc = cs.getPositionValue(cen.x(), cen.y());
 
 			Point inn = points.getPoint(1);
 			double[] pa = cs.getPositionValue(inn.x(), inn.y());
 			pa[0] -= pc[0];
 			pa[1] -= pc[1];
 			double as = Math.atan2(pa[1], pa[0]);
 			final double ri = Math.hypot(pa[0], pa[1]);
 
 			Point out = points.getPoint(2);
 			double[] pb = cs.getPositionValue(out.x(), out.y());
 			pb[0] -= pc[0];
 			pb[1] -= pc[1];
 
 			double ae = Math.atan2(pb[1], pb[0]);
 			final double ro = Math.hypot(pb[0], pb[1]);
 			double[] a = calcAngles(as, ae);
 			croi = new SectorROI(pc[0], pc[1], ri, ro, a[0], a[1]);
 
 			if (parent == null) { // for last click rendering
 				return;
 			}
			roiHandler.setROI((SectorROI)createROI(true));
 			configureHandles();
 		}
 
 		/**
 		 * Get point on ellipse at given angle
 		 * @param angle (positive for anti-clockwise)
 		 * @return
 		 */
 		public Point getPoint(double angle, int i) {
 			SectorROI sroi = getROI();
 			if (sroi == null) {
 				return null;
 			}
 			double r = sroi.getRadius(i);
 			double[] c = sroi.getPointRef();
 			int[] pt = cs.getValuePosition(c[0] + r * Math.cos(angle), c[1] + r * Math.sin(angle));
 			return new Point(pt[0], pt[1]);
 		}
 
 		@Override
 		public Point calculatePoint(double... parameter) {
 			return null;
 		}
 
 		@Override
 		public double[] calculateXIntersectionParameters(int x) {
 			return null;
 		}
 
 		@Override
 		public double[] calculateYIntersectionParameters(int y) {
 			return null;
 		}
 
 		@Override
 		public void setCoordinateSystem(ICoordinateSystem system) {
 		}
 
 		@Override
 		public boolean containsPoint(int x, int y) {
 			if (croi == null)
 				return super.containsPoint(x, y);
 			return croi.containsPoint(cs.getPositionValue(x, y));
 		}
 
 		public void setMobile(boolean mobile) {
 			if (isMobile == mobile)
 				return;
 			isMobile = mobile;
 		
 			for (FigureTranslator f : fTranslators) {
 				f.setActive(mobile);
 			}
 			
 			if (mobile) {
 				setOpaque(true);
 				setCursor(Draw2DUtils.getRoiMoveCursor());
 				addFigureListener(moveListener);
 			} else {
 				setOpaque(false);
 				setCursor(null);
 				removeFigureListener(moveListener);
 			}
 			parent.revalidate();
 		}
 
 		@Override
 		public void setVisible(boolean visible) {
 			super.setVisible(visible);
 			for (IFigure h : handles) {
 				if (isMobile && visible && !h.isVisible())
 					h.setVisible(true);
 			}
 		}
 
 		private void configureHandles() {
 			boolean mobile = isMobile();
 			boolean visible = isVisible() && mobile;
 			// handles
 			FigureTranslator mover;
 			final int imax = roiHandler.size();
 			for (int i = 0; i < imax; i++) {
 				double[] hpt = roiHandler.getAnchorPoint(i, SIDE);
 				roiHandler.set(i, i);
 				
 				int[] p = coords.getValuePosition(hpt);
 				RectangularHandle h = new RectangularHandle(coords, getRegionColor(), this, SIDE, p[0], p[1]);
 				h.setVisible(visible);
 				parent.add(h);
 				mover = new FigureTranslator(getXyGraph(), h);
 				mover.setActive(mobile);
 				mover.addTranslationListener(handleListener);
 				fTranslators.add(mover);
 				h.addFigureListener(moveListener);
 				handles.add(h);
 			}
 
 			addFigureListener(moveListener);
 			mover = new FigureTranslator(getXyGraph(), parent, this, handles) {
 				public void mouseDragged(MouseEvent event) {
 					if (!isCentreMovable) return;
 					super.mouseDragged(event);
 				}
 			};
 			mover.setActive(mobile);
 			mover.addTranslationListener(createRegionNotifier());
 			fTranslators.add(mover);
 			setRegionObjects(this, handles);
 			Rectangle b = getBounds();
 			if (b != null)
 				setBounds(b);
 		}
 
 		@Override
 		protected void fillShape(Graphics graphics) {
 			graphics.pushState();
 			graphics.setAdvanced(true);
 			graphics.setAntialias(SWT.ON);
 
 			SectorROI sroi = getROI();
 			fillSector(graphics, sroi.getAngles());
 
 			if (sroi.getSymmetry() != SectorROI.NONE)
 				fillSector(graphics, sroi.getSymmetryAngles());
 
 			graphics.popState();
 		}
 
 		private static final double DELTA = Math.PI/180;
 
 		private void fillSector(Graphics graphics, double[] ang) {
 			PointList points = Draw2DUtils.generateCurve(innerFunction, ang[0], ang[1], DELTA);
 			PointList oPoints = Draw2DUtils.generateCurve(outerFunction, ang[0], ang[1], DELTA);
 			oPoints.reverse();
 			points.addAll(oPoints);
 			graphics.fillPolygon(points);
 		}
 
 		@Override
 		protected void outlineShape(Graphics graphics) {
 			graphics.pushState();
 			graphics.setAdvanced(true);
 			graphics.setAntialias(SWT.ON);
 
 			double[] ang = getROI().getAngles();
 			PointList points = Draw2DUtils.generateCurve(innerFunction, ang[0], ang[1], DELTA);
 			PointList oPoints = Draw2DUtils.generateCurve(outerFunction, ang[0], ang[1], DELTA);
 			oPoints.reverse();
 			points.addAll(oPoints);
 			Rectangle bnd = new Rectangle();
 			graphics.getClip(bnd);
 			Draw2DUtils.drawClippedPolyline(graphics, points, bnd, true);
 
 			graphics.popState();
 		}
 
 		private TranslationListener createRegionNotifier() {
 			return new TranslationListener() {
 				@Override
 				public void translateBefore(TranslationEvent evt) {
 				}
 
 				@Override
 				public void translationAfter(TranslationEvent evt) {
 					updateBounds();
 					fireROIDragged(createROI(false), ROIEvent.DRAG_TYPE.TRANSLATE);
 				}
 
 				@Override
 				public void translationCompleted(TranslationEvent evt) {
 					fireROIChanged(createROI(true));
					roiHandler.setROI((SectorROI)roi);
 					fireROISelection();
 				}
 
 				@Override
 				public void onActivate(TranslationEvent evt) {
 				}
 			};
 		}
 
 		private TranslationListener createHandleNotifier() {
 			return new TranslationListener() {
 				private double[] spt;
 
 				@Override
 				public void onActivate(TranslationEvent evt) {
 					Object src = evt.getSource();
 					if (src instanceof FigureTranslator) {
 						final FigureTranslator translator = (FigureTranslator) src;
 						Point start = translator.getStartLocation();
 						spt = coords.getPositionValue(start.x(), start.y());
 						final IFigure handle = translator.getRedrawFigure();
 						final int h = handles.indexOf(handle);
 						HandleStatus status = HandleStatus.RESIZE;
 						if (h == (roiHandler.size()-1)) {
 							status = HandleStatus.CMOVE;
 						} else if (h == 4) {
 							status = HandleStatus.RMOVE;
 						}
 						roiHandler.configureDragging(h, status);
 					}
 				}
 
 				@Override
 				public void translateBefore(TranslationEvent evt) {
 				}
 
 				@Override
 				public void translationAfter(TranslationEvent evt) {
 					Object src = evt.getSource();
 					if (src instanceof FigureTranslator) {
 						final FigureTranslator translator = (FigureTranslator) src;
 						Point end = translator.getEndLocation();
 						
 						if (end==null) return;
 						double[] c = coords.getPositionValue(end.x(), end.y());
 
 						troi = (SectorROI)roiHandler.interpretMouseDragging(spt, c);
 
 						intUpdateFromROI(troi);
 						fireROIDragged(troi, roiHandler.getStatus() == HandleStatus.RESIZE ?
 								ROIEvent.DRAG_TYPE.RESIZE : ROIEvent.DRAG_TYPE.TRANSLATE);
 					}
 				}
 
 				@Override
 				public void translationCompleted(TranslationEvent evt) {
 					Object src = evt.getSource();
 					if (src instanceof FigureTranslator) {
 						troi = null;
 						final FigureTranslator translator = (FigureTranslator) src;
 						Point end = translator.getEndLocation();
 
 						double[] c = coords.getPositionValue(end.x(), end.y());
 
 						SectorROI croi = (SectorROI)roiHandler.interpretMouseDragging(spt, c);
 
 						updateFromROI(croi);
 						roiHandler.unconfigureDragging();
 						roi = croi;
 
 						fireROIChanged(croi);
 						fireROISelection();
 					}
 				}
 			};
 		}
 
 		/**
 		 * Update selection according to centre handle
 		 */
 		private Rectangle updateFromHandles() {
 			if (handles.size() > 0) {
 				IFigure f = handles.get(roiHandler.size() - 1);
 				if (f instanceof SelectionHandle) {
 					SelectionHandle h = (SelectionHandle) f;
 					setCentre(h.getSelectionPoint());
 				}
 			}
 			return getBounds();
 		}
 
 		@Override
 		public Rectangle getBounds() {
 			if (getROI() != null && dirty)
 				calcBox(getROI(), false);
 			dirty = false;
 			Rectangle b = bnds == null ? super.getBounds() : new Rectangle(bnds);
 			if (handles != null)
 				for (IFigure f : handles) {
 					if (f instanceof SelectionHandle) {
 						SelectionHandle h = (SelectionHandle) f;
 						b.union(h.getBounds());
 					}
 				}
 			return b;
 		}
 
 		private void calcBox(SectorROI proi, boolean redraw) {
 			RectangularROI rroi = proi.getBounds();
 			int[] bp = cs.getValuePosition(rroi.getPointRef());
 			int[] ep = cs.getValuePosition(rroi.getEndPoint());
 			bnds = new Rectangle(new Point(bp[0], bp[1]), new Point(ep[0], ep[1]));
 			ep = cs.getValuePosition(rroi.getPoint(0, 1));
 			bnds.union(new Point(ep[0], ep[1]));
 			ep = cs.getValuePosition(rroi.getPoint(1, 0));
 			bnds.union(new Point(ep[0], ep[1]));
 			if (redraw) {
 				setBounds(bnds);
 			}
 			dirty = false;
 		}
 
 		/**
 		 * Update according to ROI
 		 * @param sroi
 		 */
 		public void updateFromROI(SectorROI sroi) {
 			croi = sroi;
 			roiHandler.setROI(sroi);
 			intUpdateFromROI(sroi);
 		}
 
 		/**
 		 * Update according to ROI
 		 * @param sroi
 		 */
 		private void intUpdateFromROI(SectorROI sroi) {
 			int imax = handles.size();
 			if (imax != roiHandler.size()) {
 				configureHandles();
 			} else {
 				SectorROIHandler handler = new SectorROIHandler(sroi);
 				for (int i = 0; i < imax; i++) {
 					double[] hpt = handler.getAnchorPoint(i, SIDE);
 					SelectionHandle handle = (SelectionHandle) handles.get(i);
 					int[] pnt  = coords.getValuePosition(hpt);
 					handle.setSelectionPoint(new PrecisionPoint(pnt[0], pnt[1]));
 				}
 			}
 			dirty = true;
 			calcBox(sroi, true);
 		}
 
 		@Override
 		public IRegion getRegion() {
 			return SectorSelection.this;
 		}
 
 		@Override
 		public void setRegion(IRegion region) {
 		}
 
 		public List<FigureTranslator> getHandleTranslators() {
 			return fTranslators;
 		}
 		public List<IFigure> getHandles() {
 			return handles;
 		}
 	}
 
 	@Override
 	public boolean isCentreMovable() {
 		return isCentreMovable;
 	}
 
 	@Override
 	public void setCentreMovable(boolean isCentreMovable) {
 		this.isCentreMovable = isCentreMovable;
 		if (isCentreMovable) {
 			sector.setCursor(Draw2DUtils.getRoiMoveCursor());
 			sector.getHandleTranslators().get(sector.getHandleTranslators().size()-1).setActive(true);
 			sector.getHandles().get(sector.getHandles().size()-1).setVisible(true);
 		} else {
 			sector.setCursor(null);			
 			sector.getHandleTranslators().get(sector.getHandleTranslators().size()-1).setActive(false);
 			sector.getHandles().get(sector.getHandles().size()-1).setVisible(false);
 		}
 	}
 
 	@Override
 	public boolean isOuterMovable() {
 		throw new RuntimeException("Cannot call isOuterMovable on "+getClass().getName());
 	}
 
 	@Override
 	public void setOuterMovable(boolean isOuterMovable) {
 		throw new RuntimeException("Cannot call setOuterMovable on "+getClass().getName());
 	}
 }
