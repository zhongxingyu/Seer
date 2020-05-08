 /*-
  * Copyright 2013 Diamond Light Source Ltd.
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
 import org.eclipse.draw2d.Figure;
 import org.eclipse.draw2d.FigureListener;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.MouseEvent;
 import org.eclipse.draw2d.Shape;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.PointList;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.swt.SWT;
 
 import uk.ac.diamond.scisoft.analysis.roi.IROI;
 import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
 import uk.ac.diamond.scisoft.analysis.roi.handler.HandleStatus;
 import uk.ac.diamond.scisoft.analysis.roi.handler.RectangularROIHandler;
 
 class BoxSelection extends AbstractSelectionRegion {
 
 	Box box;
 
 	BoxSelection(String name, ICoordinateSystem coords) {
 		super(name, coords);
 		setRegionColor(RegionType.BOX.getDefaultColor());
 		setAlpha(80);
 		setLineWidth(2);
 	}
 
 	@Override
 	public void setMobile(boolean mobile) {
 		super.setMobile(mobile);
 		if (box != null)
 			box.setMobile(mobile);
 	}
 
 	@Override
 	public void createContents(Figure parent) {
 		box = new Box(parent, coords);
 		box.setCursor(Draw2DUtils.getRoiMoveCursor());
 
 		parent.add(box);
 		sync(getBean());
 		setOpaque(false);
 	}
 
 	@Override
 	public boolean containsPoint(int x, int y) {
 		return box.containsPoint(x, y);
 	}
 
 	@Override
 	public RegionType getRegionType() {
 		return RegionType.BOX;
 	}
 
 	@Override
 	protected void updateBounds() {
 		if (box != null) {
 			Rectangle b = box.updateFromHandles();
 			if (b != null)
 				box.setBounds(b);
 		}
 	}
 
 	@Override
 	public void paintBeforeAdded(Graphics g, PointList clicks, Rectangle parentBounds) {
 		if (clicks.size() <= 1)
 			return;
 
 		g.setLineStyle(SWT.LINE_DOT);
 		final Rectangle bounds = new Rectangle(clicks.getFirstPoint(), clicks.getLastPoint());
 		g.drawRectangle(bounds);
 		g.setBackgroundColor(getRegionColor());
 		g.setAlpha(getAlpha());
 		g.fillRectangle(bounds);
 	}
 
 	@Override
 	public void initialize(PointList clicks) {
 		if (box != null) {
 			box.setup(clicks);
 			fireROIChanged(getROI());
 		}
 	}
 
 	@Override
 	protected String getCursorPath() {
 		return "icons/Cursor-box.png";
 	}
 
 	@Override
 	protected IROI createROI(boolean recordResult) {
 		if (recordResult) {
 			roi = box.croi;
 		}
 		return box.croi;
 	}
 
 	@Override
 	protected void updateRegion() {
 		if (box != null && roi instanceof RectangularROI) {
 			box.updateFromROI((RectangularROI) roi);
 			sync(getBean());
 		}
 	}
 
 	@Override
 	public int getMaximumMousePresses() {
 		return 2;
 	}
 
 	@Override
 	public void dispose() {
 		super.dispose();
 		if (box != null) {
 			box.dispose();
 		}
 	}
 
 	protected void drawRectangle(Graphics g) {
 		box.internalOutline(g);
 	}
 
 	protected void fillRectangle(Graphics g) {
 		box.internalFill(g);
 	}
 
 	@Override
 	public IROI getROI() {
 		return box != null ? box.getROI() : super.getROI();
 	}
 
 	class Box extends Shape implements IRegionContainer {
 		List<IFigure> handles;
 		List<FigureTranslator> fTranslators;
 		private Figure parent;
 		private static final int SIDE = 8;
 		private RectangularROIHandler roiHandler;
 		private TranslationListener handleListener;
 		private FigureListener moveListener;
 		private boolean isMobile;
 		private Rectangle bnds;
 		private boolean dirty = true;
 		private ICoordinateSystem cs;
 		private RectangularROI croi;
 		private RectangularROI troi = null; // temporary ROI used in dragging
 
 		public Box(Figure parent, ICoordinateSystem system) {
 			super();
 			this.parent = parent;
 			cs = system;
 			handles = new ArrayList<IFigure>();
 			fTranslators = new ArrayList<FigureTranslator>();
 			roiHandler = new RectangularROIHandler((RectangularROI) roi);
 			handleListener = createHandleNotifier();
 			moveListener = new FigureListener() {
 				@Override
 				public void figureMoved(IFigure source) {
 					Box.this.parent.repaint();
 				}
 			};
 		}
 
 		public void setCentre(Point nc) {
 			double[] pt = cs.getPositionValue(nc.x(), nc.y());
 			double[] pc = croi.getMidPoint();
 			pt[0] -= pc[0];
 			pt[1] -= pc[1];
 			croi.addPoint(pt);
 			dirty = true;
 			calcBox(croi, true);
 		}
 
 		@Override
 		public boolean containsPoint(int x, int y) {
 			if (croi == null)
 				return super.containsPoint(x, y);
 			return croi.containsPoint(cs.getPositionValue(x, y));
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
 
 		public RectangularROI getROI() {
 			return troi != null ? troi : croi;
 		}
 		
 		public void setup(PointList corners) {
 			final Point pa = corners.getFirstPoint();
 			final Point pc = corners.getLastPoint();
 
 			double[] a = cs.getPositionValue(pa.x(), pa.y());
 			double[] c = cs.getPositionValue(pc.x(), pc.y());
 			double ox = Math.min(a[0], c[0]);
 			double oy = Math.min(a[1], c[1]);
 			double lx = Math.abs(a[0] - c[0]);
 			double ly = Math.abs(a[1] - c[1]);
 			double angle = 0;
 			if (lx < ly) {
 				angle = 0.5*Math.PI;
 				ox += lx;
 				double t = lx;
 				lx = ly;
 				ly = t;
 			}
 			croi = new RectangularROI(ox, oy, lx, ly, angle);
 
 			roiHandler.setROI(createROI(true));
 			configureHandles();
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
 					roiHandler.setROI(roi);
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
 					troi = null;
 					Object src = evt.getSource();
 					if (src instanceof FigureTranslator) {
 						final FigureTranslator translator = (FigureTranslator) src;
 						Point start = translator.getStartLocation();
 						spt = coords.getPositionValue(start.x(), start.y());
 						final IFigure handle = translator.getRedrawFigure();
 						final int h = handles.indexOf(handle);
 						HandleStatus status = h == 4 ? HandleStatus.RMOVE : HandleStatus.RESIZE;
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
 						troi = (RectangularROI) roiHandler.interpretMouseDragging(spt, c);
 
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
 						RectangularROI croi = (RectangularROI) roiHandler.interpretMouseDragging(spt, c);
 
 						updateFromROI(croi);
 						roiHandler.unconfigureDragging();
 						roi = croi;
 
 						fireROIChanged(croi);
 						fireROISelection();
 					}
 				}
 			};
 		}
 
 		private Rectangle updateFromHandles() {
 			if (handles.size() > 0) {
 				IFigure f = handles.get(4); // centre point
 				if (f instanceof SelectionHandle) {
 					SelectionHandle h = (SelectionHandle) f;
 					setCentre(h.getSelectionPoint());
 				}
 			}
 			return getBounds();
 		}
 
 		protected PointList generatePointList() {
 			PointList pl = new PointList(4);
 			int[] pt;
 			RectangularROI proi = getROI();
 			pt = cs.getValuePosition(proi.getPointRef());
 			pl.addPoint(pt[0], pt[1]);
 			pt = cs.getValuePosition(proi.getPoint(1, 0));
 			pl.addPoint(pt[0], pt[1]);
 			pt = cs.getValuePosition(proi.getPoint(1, 1));
 			pl.addPoint(pt[0], pt[1]);
 			pt = cs.getValuePosition(proi.getPoint(0, 1));
 			pl.addPoint(pt[0], pt[1]);
 
 			return pl;
 		}
 
 		private void internalFill(Graphics graphics) {
 			graphics.pushState();
 			graphics.setAdvanced(true);
 			graphics.setAntialias(SWT.ON);
 			graphics.fillPolygon(generatePointList());
 			graphics.popState();
 		}
 
 		private void internalOutline(Graphics graphics) {
 			graphics.pushState();
 			graphics.setAdvanced(true);
 			graphics.setAntialias(SWT.ON);
 
 			Rectangle bnds = parent.getBounds();
 			Draw2DUtils.drawClippedPolyline(graphics, generatePointList(), bnds, true);
 			graphics.popState();
 		}
 
 		private void calcBox(RectangularROI proi, boolean redraw) {
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
 
 		@Override
 		public void setVisible(boolean visible) {
 			super.setVisible(visible);
 			for (IFigure h : handles) {
 				if (isMobile && visible && !h.isVisible())
 					h.setVisible(true);
 			}
 		}
 
 		public void setMobile(boolean mobile) {
 			if (mobile == isMobile)
 				return;
 			isMobile = mobile;
 
 			for (IFigure h : handles) {
 				if (h.isVisible() != mobile)
 					h.setVisible(mobile);
 			}
 			for (FigureTranslator f : fTranslators) {
 				f.setActive(mobile);
 			}
 		}
 
 		@Override
 		public String toString() {
 			if (croi == null)
 				return "BoxSel: undefined";
 
 			int[] pt = cs.getValuePosition(croi.getPointRef());
 			Point start = new Point(pt[0], pt[1]);
 			int[] pta = cs.getValuePosition(0, 0);
 			int[] ptb = cs.getValuePosition(croi.getLengths());
 
 			return "BoxSel: start=" + start + ", major=" + (ptb[0] - pta[0]) + ", minor=" + (ptb[1] - pta[1]) + ", ang=" + croi.getAngleDegrees();
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
 
 		@Override
 		public Rectangle getBounds() {
			if (getROI() != null && dirty)
				calcBox(getROI(), false);
 			dirty = false;
 			Rectangle b = bnds == null ? super.getBounds() : new Rectangle(bnds);
 			if (handles != null) {
 				for (IFigure f : handles) {
 					if (f instanceof SelectionHandle) {
 						SelectionHandle h = (SelectionHandle) f;
 						b.union(h.getBounds());
 					}
 				}
 			}
 			return b;
 		}
 
 		/**
 		 * Update according to ROI
 		 * @param rroi
 		 */
 		public void updateFromROI(RectangularROI rroi) {
 			croi = rroi;
 			roiHandler.setROI(rroi);
 			intUpdateFromROI(rroi);
 		}
 
 		/**
 		 * Update according to ROI
 		 * @param rroi
 		 */
 		private void intUpdateFromROI(RectangularROI rroi) {
 			int imax = handles.size();
 			if (imax != roiHandler.size()) {
 				configureHandles();
 			} else {
 				RectangularROIHandler handler = new RectangularROIHandler(rroi);
 				for (int i = 0; i < imax; i++) {
 					double[] hpt = handler.getAnchorPoint(i, SIDE);
 					SelectionHandle handle = (SelectionHandle) handles.get(i);
 					int[] pta = coords.getValuePosition(hpt);
 					handle.setSelectionPoint(new Point(pta[0], pta[1]));
 				}
 			}
 			dirty = true;
 			calcBox(rroi, true);
 		}
 
 		@Override
 		public IRegion getRegion() {
 			return BoxSelection.this;
 		}
 
 		@Override
 		public void setRegion(IRegion region) {
 		}
 
 		@Override
 		protected void outlineShape(Graphics g) {
 			drawRectangle(g);
 		}
 
 		@Override
 		protected void fillShape(Graphics g) {
 			fillRectangle(g);
 		}
 	}
 }
