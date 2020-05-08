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
 import org.dawnsci.plotting.draw2d.swtxy.util.RotatableRectangle;
 import org.eclipse.draw2d.Figure;
 import org.eclipse.draw2d.FigureListener;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.MouseEvent;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.PointList;
 import org.eclipse.draw2d.geometry.PrecisionPoint;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.swt.SWT;
 
 import uk.ac.diamond.scisoft.analysis.roi.IROI;
 import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
 import uk.ac.diamond.scisoft.analysis.roi.handler.HandleStatus;
 import uk.ac.diamond.scisoft.analysis.roi.handler.RectangularROIHandler;
 
 public class BoxSelection extends AbstractSelectionRegion {
 
 	DecoratedRectangle rect;
 
 	public BoxSelection(String name, ICoordinateSystem coords) {
 		super(name, coords);
 		setRegionColor(RegionType.BOX.getDefaultColor());
 		setAlpha(80);
 		setLineWidth(2);
 	}
 
 	@Override
 	public void setVisible(boolean visible) {
 		getBean().setVisible(visible);
 		if (rect != null)
 			rect.setVisible(visible);
 	}
 
 	@Override
 	public void setMobile(final boolean mobile) {
 		getBean().setMobile(mobile);
 		if (rect != null)
 			rect.setMobile(mobile);
 	}
 
 	@Override
 	public void createContents(Figure parent) {
 		rect = new DecoratedRectangle(parent);
 		rect.setCursor(Draw2DUtils.getRoiMoveCursor());
 
 		parent.add(rect);
 		sync(getBean());
 		setOpaque(false);
 		setAlpha(getAlpha());
 		rect.setForegroundColor(getRegionColor());
 		rect.setAlpha(getAlpha());
 		updateROI();
 		if (roi == null)
 			createROI(true);
 	}
 
 	@Override
 	public boolean containsPoint(double x, double y) {
 		final int[] pix = coords.getValuePosition(x,y);
 		return rect.containsPoint(pix[0], pix[1]);
 	}
 
 	@Override
 	public RegionType getRegionType() {
 		return RegionType.BOX;
 	}
 
 	@Override
 	protected void updateConnectionBounds() {
 		if (rect != null) {
 			Rectangle b = rect.updateFromHandles();
 			rect.setBounds(b);
 		}
 	}
 
 	@Override
 	public void paintBeforeAdded(Graphics g, PointList clicks, Rectangle parentBounds) {
 		g.setLineStyle(SWT.LINE_DOT);
 		final Rectangle bounds = new Rectangle(clicks.getFirstPoint(), clicks.getLastPoint());
 		g.drawRectangle(bounds);
 		g.setBackgroundColor(getRegionColor());
 		g.setAlpha(getAlpha());
 		g.fillRectangle(bounds);
 	}
 
 	@Override
 	public void setLocalBounds(PointList clicks, Rectangle parentBounds) {
 		if (rect != null) {
 			rect.setup(clicks);
 			setRegionColor(getRegionColor());
 			setOpaque(false);
 			setAlpha(getAlpha());
 			createROI(true);
 			fireROIChanged(getROI());
 		}
 	}
 
 	@Override
 	protected String getCursorPath() {
 		return "icons/Cursor-box.png";
 	}
 
 	@Override
 	protected IROI createROI(boolean recordResult) {
 		final PointList pl = rect.getPoints();
 		if (pl.size() < 2)
 			return super.getROI();
 
 		final RectangularROI rroi = new RectangularROI();
 		rroi.setName(getName());
 		rroi.setAngle(Math.toRadians(rect.getAngleDegrees()));
 		Point p;
 		p = pl.getPoint(0);
 		double[] pa = coords.getPositionValue(p.x(), p.y());
 		p = pl.getPoint(1);
 		double[] pb = coords.getPositionValue(p.x(), p.y());
 		p = pl.getPoint(2);
 		double[] pc = coords.getPositionValue(p.x(), p.y());
 		double la = Math.hypot(pa[0] - pb[0], pa[1] - pb[1]);
 		double lb = Math.hypot(pc[0] - pb[0], pc[1] - pb[1]);
 		rroi.setPoint(pa);
 		rroi.setLengths(la,  lb);
 		if (roi != null) {
 			rroi.setPlot(roi.isPlot());
 			// set the Region isActive flag
 			this.setActive(roi.isPlot());
 		}
 		if (recordResult)
 			roi = rroi;
 
 		return rroi;
 	}
 
 	@Override
 	protected void updateROI(IROI roi) {
 		if (roi instanceof RectangularROI && rect != null) {
 			rect.updateFromROI((RectangularROI) roi);
 		}
 	}
 
 	@Override
 	public int getMaximumMousePresses() {
 		return 2;
 	}
 
 	@Override
 	public void dispose() {
 		super.dispose();
 		if (rect != null) {
 			rect.dispose();
 		}
 	}
 
 	protected void drawRectangle(Graphics g) {
 		rect.internalOutline(g);
 	}
 
 	protected void fillRectangle(Graphics g) {
 		rect.internalFill(g);
 	}
 
 	class DecoratedRectangle extends RotatableRectangle implements IRegionContainer {
 		List<IFigure> handles;
 		List<FigureTranslator> fTranslators;
 		private Figure parent;
 		private static final int SIDE = 8;
 		private RectangularROIHandler roiHandler;
 		private TranslationListener handleListener;
 		private FigureListener moveListener;
 
 		public DecoratedRectangle(Figure parent) {
 			super();
 			handles = new ArrayList<IFigure>();
 			fTranslators = new ArrayList<FigureTranslator>();
 			this.parent = parent;
 			roiHandler = new RectangularROIHandler((RectangularROI) roi);
 			handleListener = createHandleNotifier();
 			moveListener = new FigureListener() {
 				@Override
 				public void figureMoved(IFigure source) {
 					DecoratedRectangle.this.parent.repaint();
 				}
 			};
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
 
 		public void setup(PointList points) {
 			final Point pa = points.getFirstPoint();
 			final Point pc = points.getLastPoint();
 
 			setOrigin(Math.min(pa.x(), pc.x()), Math.min(pa.y(), pc.y()));
 			setLengths(Math.abs(pa.x() - pc.x()), Math.abs(pa.y() - pc.y()));
 
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
 					updateConnectionBounds();
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
 						int[] r = new int[] { (int) c[0], (int) c[1] };
 
 						RectangularROI croi = (RectangularROI) roiHandler.interpretMouseDragging(spt, r);
 
 						final SelectionHandle handle = (SelectionHandle) translator.getRedrawFigure();
 						updateFromROI(croi, handle);
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
 						int[] r = new int[] { (int) c[0], (int) c[1] };
 
 						RectangularROI croi = (RectangularROI) roiHandler.interpretMouseDragging(spt, r);
 
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
 
 		@Override
 		public void setVisible(boolean visible) {
 			super.setVisible(visible);
 			boolean hVisible = visible && isMobile();
 			for (IFigure h : handles) {
 				h.setVisible(hVisible);
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
 
 		/**
 		 * Update according to ROI
 		 * @param rroi
 		 */
 		public void updateFromROI(RectangularROI rroi) {
 			roiHandler.setROI(rroi);
 			updateFromROI(rroi, null);
 		}
 
 		/**
 		 * Update according to ROI
 		 * @param rroi
 		 * @param omit selection handle to not move
 		 */
 		private void updateFromROI(RectangularROI rroi, SelectionHandle omit) {
 			int[] pta = coords.getValuePosition(0,0);
 			int[] ptb = coords.getValuePosition(rroi.getLengths());
 
 			double ratio = coords.getAspectRatio();
 			affine.setAspectRatio(ratio);
 			setLengths(Math.abs(pta[0] - ptb[0]), (int) Math.round(Math.abs(pta[1] - ptb[1])/ratio));
 
 			pta  = coords.getValuePosition(rroi.getPointRef());
 			setOrigin(pta[0], pta[1]);
 
 			setAngle(rroi.getAngleDegrees());
 
 			int imax = handles.size();
 			if (imax != roiHandler.size()) {
 				configureHandles();
 			} else {
 				RectangularROIHandler handler = new RectangularROIHandler(rroi);
 				for (int i = 0; i < imax; i++) {
 					double[] hpt = handler.getAnchorPoint(i, SIDE);
 					SelectionHandle handle = (SelectionHandle) handles.get(i);
 					if (handle != omit) {
 						pta  = coords.getValuePosition(hpt);
 						handle.setSelectionPoint(new PrecisionPoint(pta[0], pta[1]));
 					}
 				}
 			}
 		}
 
 		@Override
 		public IRegion getRegion() {
 			return BoxSelection.this;
 		}
 
 		@Override
 		public void setRegion(IRegion region) {
 		}
 
 		private void internalOutline(Graphics g) {
 			super.outlineShape(g);
 		}
 
 		private void internalFill(Graphics g) {
 			super.fillShape(g);
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
