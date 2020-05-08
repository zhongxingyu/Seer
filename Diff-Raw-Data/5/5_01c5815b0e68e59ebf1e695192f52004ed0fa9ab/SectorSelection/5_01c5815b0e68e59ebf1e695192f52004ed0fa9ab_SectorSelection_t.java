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
 import org.dawnsci.plotting.draw2d.swtxy.util.Sector;
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.Figure;
 import org.eclipse.draw2d.FigureListener;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.MouseEvent;
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.PointList;
 import org.eclipse.draw2d.geometry.PrecisionPoint;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.swt.graphics.Color;
 
 import uk.ac.diamond.scisoft.analysis.roi.IROI;
 import uk.ac.diamond.scisoft.analysis.roi.SectorROI;
 import uk.ac.diamond.scisoft.analysis.roi.handler.HandleStatus;
 import uk.ac.diamond.scisoft.analysis.roi.handler.SectorROIHandler;
 
 /**
  * You should not call this concrete class outside of the draw2d 
  * extensions unless absolutely required.
  */
 class SectorSelection extends AbstractSelectionRegion implements ILockableRegion {
 
 	DecoratedSector sector;
 
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
 	public void setVisible(boolean visible) {
 		getBean().setVisible(visible);
 		if (sector != null)
 			sector.setVisible(visible);
 	}
 
 	@Override
 	public void setMobile(boolean mobile) {
 		super.setMobile(mobile);
 		if (sector != null) sector.setMobile(mobile);
 	}
 
 	@Override
 	public void createContents(Figure parent) {
 		sector = new DecoratedSector(parent);
 		sector.setCoordinateSystem(coords);
 		sector.setCursor(Draw2DUtils.getRoiMoveCursor());
 
 		parent.add(sector);
 		sync(getBean());
 		sector.setLineWidth(getLineWidth());
 	}
 
 	@Override
 	public boolean containsPoint(double x, double y) {
 		final int[] pix = coords.getValuePosition(x,y);
 		return sector.containsPoint(pix[0], pix[1]);
 	}
 
 	@Override
 	public RegionType getRegionType() {
 		return RegionType.SECTOR;
 	}
 
 	@Override
 	protected void updateBounds() { // called after a handle translation
 		if (sector != null) {
 			sector.updateFromHandles();
 			Rectangle b = sector.getBounds();
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
 			double as = Math.toDegrees(Math.atan2(h, w));
 			Point out = clicks.getPoint(2);
 			rd = out.getDifference(cen);
 			h = -rd.preciseHeight() / ratio;
 			w = rd.preciseWidth();
 			final double ro = Math.hypot(w, h);
 			double ae = Math.toDegrees(Math.atan2(h, w));
 			double[] a = calcAngles(as, ae);
 //			System.err.printf("Temp: c = %d, %d; r = %.2f, %.2f; a = %.1f, %.1f\n", cen.x(), cen.y(), ri, ro, a[0], a[1]);
 			Sector s = new Sector(cen.preciseX(), cen.preciseY(), ri,  ro, a[0], a[1]);
 			s.setCoordinateSystem(coords);
  		    s.setLineStyle(Graphics.LINE_DOT);
 			s.setLineWidth(getLineWidth());
 			s.setForegroundColor(getRegionColor());
 			s.setBackgroundColor(getRegionColor());
 			s.paintFigure(g);
 		}
 	}
 
 	private Boolean clockwise = null;
 	private double[] calcAngles(double anglea, double angleb) {
 		if (anglea < 0)
 			anglea += 360;
 		if (angleb < 0)
 			angleb += 360;
 		if (clockwise == null) {
 			if (anglea == 0) {
 				clockwise = angleb > 180;
 			} else {
 				clockwise = anglea > angleb;
 			}
 		}
 
 		double l;
 		if (clockwise) {
 			if (anglea < 180) {
 				if (angleb < 180) {
 					l = angleb - anglea;
 					if (l > 0)
 						l -= 360;
 				} else
 					l = angleb - 360 - anglea;
 			} else {
 				if (angleb < 180) {
 					l = angleb - anglea;
 				} else {
 					l = angleb - anglea;
 					if (l > 0)
 						l -= 360;
 				}
 			}
 		} else {
 			if (anglea < 180) {
 				if (angleb < 180) {
 					l = angleb - anglea;
 					if (l < 0)
 						l += 360;
 				} else
 					l = angleb - anglea;
 			} else {
 				if (angleb < 180)
 					l = angleb - anglea + 360;
 				else {
 					l = angleb - anglea;
 					if (l < 0)
 						l += 360;
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
 		final Point c = sector.getCentre();
 		final double[] a = sector.getAnglesDegrees();
 		double offset = coords.getXAxisRotationAngleDegrees();
 		final Point r0 = sector.getPoint(offset, 0);
 		final Point r1 = sector.getPoint(offset, 1);
 		final double[] pc = coords.getPositionValue(c.x(), c.y());
 		final double[] p0 = coords.getPositionValue(r0.x(), r0.y());
 		final double[] p1 = coords.getPositionValue(r1.x(), r1.y());
 
 		final int symmetry = roi != null ? ((SectorROI) roi).getSymmetry() : 0;
 		final boolean combine = roi != null ? ((SectorROI) roi).isCombineSymmetry() : false;
 		offset = 360 - offset;
 		final SectorROI sroi = new SectorROI(pc[0], pc[1], Math.abs(p0[0] - pc[0]), Math.abs(p1[0] - pc[0]),
 				Math.toRadians(offset - a[1]), Math.toRadians(offset - a[0]));
 		sroi.setName(getName());
 		sroi.setSymmetry(symmetry);
 		sroi.setCombineSymmetry(combine);
 		if (roi != null) {
 			sroi.setPlot(roi.isPlot());
 			// set the Region isActive flag
 			this.setActive(roi.isPlot());
 		}
 		if (recordResult) {
 			roi = sroi;
 		}
 
 		return sroi;
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
 
 	class DecoratedSector extends Sector implements IRegionContainer {
 		private List<IFigure> handles;
 		private List<FigureTranslator> fTranslators;
 		private Figure parent;
 		private static final int SIDE = 8;
 		private SectorROIHandler roiHandler;
 		private TranslationListener handleListener;
 		private FigureListener moveListener;
 		private boolean isMobile;
 
 		public DecoratedSector(Figure parent) {
 			super();
 			handles = new ArrayList<IFigure>();
 			fTranslators = new ArrayList<FigureTranslator>();
 			this.parent = parent;
 			roiHandler = new SectorROIHandler((SectorROI) roi);
 			handleListener = createHandleNotifier();
 			moveListener = new FigureListener() {
 				@Override
 				public void figureMoved(IFigure source) {
 					DecoratedSector.this.parent.repaint();
 				}
 			};
 			setBackgroundColor(getRegionColor());
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
 			Point inn = points.getPoint(1);
 			Dimension rd = inn.getDifference(cen);
 			double offset = coords.getXAxisRotationAngleDegrees();
 			double ratio = coords.getAspectRatio();
 			double h, w;
 			offset = 0;
 			h = -rd.preciseHeight() / ratio;
 			w = rd.preciseWidth();
 			double as = offset + Math.toDegrees(Math.atan2(h, w));
 			final double ri = Math.hypot(w, h);
 
 			Point out = points.getPoint(2);
 			rd = out.getDifference(cen);
 			h = -rd.preciseHeight() / ratio;
 			w = rd.preciseWidth();
 
 			final double ro = Math.hypot(w, h);
 			double ae = offset + Math.toDegrees(Math.atan2(h, w));
 			double[] a = calcAngles(as, ae);
 //			System.err.printf("Perm: c = %d, %d; r = %.2f, %.2f; a = %.1f, %.1f\n", cen.x(), cen.y(), ri, ro, a[0], a[1]);
 			setCentre(cen.preciseX(), cen.preciseY());
 			if (ri < ro)
 				setRadii(ri,  ro);
 			else
 				setRadii(ro,  ri);
 			setAnglesDegrees(a[0], a[1]);
 
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
 		public void setVisible(boolean visible) {
 			super.setVisible(visible);
			boolean net = visible && isMobile;
 			for (IFigure h : handles) {
				if (h.isVisible() != net)
					h.setVisible(net);
 			}
 		}
 
 		public void setMobile(boolean mobile) {
 			if (isMobile == mobile)
 				return;
 
 			isMobile = mobile;
 			for (IFigure h : handles) {
 				h.setVisible(mobile);
 			}
 
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
 				private int[] spt;
 
 				@Override
 				public void onActivate(TranslationEvent evt) {
 					Object src = evt.getSource();
 					if (src instanceof FigureTranslator) {
 						final FigureTranslator translator = (FigureTranslator) src;
 						Point start = translator.getStartLocation();
 						double[] c = coords.getPositionValue(start.x(), start.y());
 						spt = new int[]{(int)c[0], (int)c[1]};
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
 						int[] r = new int[]{(int)c[0], (int)c[1]};
 
 						SectorROI croi = (SectorROI)roiHandler.interpretMouseDragging(spt,r);
 
 						intUpdateFromROI(croi);
 						fireROIDragged(croi, roiHandler.getStatus() == HandleStatus.RESIZE ?
 								ROIEvent.DRAG_TYPE.RESIZE : ROIEvent.DRAG_TYPE.TRANSLATE);
 					}
 				}
 
 				@Override
 				public void translationCompleted(TranslationEvent evt) {
 					Object src = evt.getSource();
 					if (src instanceof FigureTranslator) {
 						final FigureTranslator translator = (FigureTranslator) src;
 						Point end = translator.getEndLocation();
 
 						double[] c = coords.getPositionValue(end.x(), end.y());
 						int[] r = new int[]{(int)c[0], (int)c[1]};
 
 						SectorROI croi = (SectorROI)roiHandler.interpretMouseDragging(spt,r);
 
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
 		private void updateFromHandles() {
 			if (handles.size() > 0) {
 				IFigure f = handles.get(roiHandler.size() - 1);
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
 		public void updateFromROI(SectorROI sroi) {
 			roiHandler.setROI(sroi);
 			intUpdateFromROI(sroi);
 		}
 
 		/**
 		 * Update according to ROI
 		 * @param sroi
 		 */
 		private void intUpdateFromROI(SectorROI sroi) {
 			final double x = sroi.getPointX();
 			final double y = sroi.getPointY();
 			final double[] r = sroi.getRadii();
 			final double[] a = sroi.getAnglesDegrees();
 
 			final int[] c  = coords.getValuePosition(x, y);
 			final int[] rd = coords.getValuePosition(x + r[0], y + r[1]);
 			setCentre(c[0], c[1]);
 			double ra = Math.abs(rd[0] - c[0]);
 			double rb = Math.abs(rd[1] - c[1]) / coords.getAspectRatio();
 			if (ra > rb)
 				setRadii(rb, ra);
 			else
 				setRadii(ra, rb);
 
 			double offset = 360 - coords.getXAxisRotationAngleDegrees();
 			a[0] = offset - a[0];
 			a[1] = offset - a[1];
 			setAnglesDegrees(a[1], a[0]);
 			
 			if (sroi.getSymmetry() == SectorROI.NONE) {
 				setDrawSymmetry(false);
 			} else {
 				setDrawSymmetry(true);
 				double[] nang = sroi.getSymmetryAngles();
 				setSymmetryAnglesDegrees(-Math.toDegrees(nang[1]), -Math.toDegrees(nang[0]));
 			}
 
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
