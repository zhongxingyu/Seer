 /*
  * Copyright © 2011 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.dawb.workbench.plotting.tools;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.dawb.common.ui.plot.IAxis;
 import org.dawb.common.ui.plot.IPlottingSystem;
 import org.dawb.common.ui.plot.PlottingFactory;
 import org.dawb.common.ui.plot.region.IROIListener;
 import org.dawb.common.ui.plot.region.IRegion;
 import org.dawb.common.ui.plot.region.IRegionListener;
 import org.dawb.common.ui.plot.region.ROIEvent;
 import org.dawb.common.ui.plot.region.RegionEvent;
 import org.dawb.common.ui.plot.region.RegionUtils;
 import org.dawb.common.ui.plot.tool.AbstractToolPage;
 import org.dawb.common.ui.plot.tool.IToolPageSystem;
 import org.dawb.common.ui.plot.trace.IImageTrace;
 import org.dawb.common.ui.plot.trace.ILineTrace;
 import org.dawb.common.ui.plot.trace.ITrace;
 import org.dawb.common.ui.plot.trace.ITraceListener;
 import org.dawb.common.ui.plot.trace.TraceEvent;
 import org.dawb.workbench.plotting.Activator;
 import org.dawb.workbench.plotting.tools.MeasurementTool.RegionColorListener;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.MouseEvent;
 import org.eclipse.draw2d.MouseListener;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.window.ToolTip;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.dnd.Clipboard;
 import org.eclipse.swt.dnd.TextTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.ui.IActionBars;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
 
 public class InfoPixelTool extends AbstractToolPage implements IROIListener, IRegionListener, MouseListener  {
 
 	private final static Logger logger = LoggerFactory.getLogger(InfoPixelTool.class);
 	
 	protected IPlottingSystem        plotter;
 	private   ITraceListener         traceListener;
 	private   IRegion                xHair, yHair;
 	private   IAxis                  x1,x2;
 	private   RunningJob             xUpdateJob, yUpdateJob;
 	private   ROIBase           xBounds, yBounds;
 	
 	private Composite     composite;
 	private TableViewer   viewer;
 	private RegionColorListener viewUpdateListener;
 	private Map<String,ROIBase> dragBounds;
 	public double xValues [] = new double[1];	public double yValues [] = new double[1];
 
 		
 	public InfoPixelTool() {
 		dragBounds = new HashMap<String,ROIBase>(7);
 		
 		try {
 			
 			plotter = PlottingFactory.getPlottingSystem();
 			this.traceListener = new ITraceListener.Stub() {
 				@Override
 				public void tracesPlotted(TraceEvent evt) {
 					
 					if (!(evt.getSource() instanceof List<?>)) {
 						return;
 					}
 					
 					if (xUpdateJob!=null) xUpdateJob.scheduleIfNotSuspended();
 					if (yUpdateJob!=null) yUpdateJob.scheduleIfNotSuspended();
 				}
 			};
 						
 		} catch (Exception e) {
 			logger.error("Cannot get plotting system!", e);
 		}
 	}
 	
 	@Override
 	public void createControl(Composite parent) {
 		
 		this.composite = new Composite(parent, SWT.NONE);
 		composite.setLayout(new FillLayout());
 
 		viewer = new TableViewer(composite, SWT.FULL_SELECTION | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
 		createColumns(viewer);
 		viewer.getTable().setLinesVisible(true);
 		viewer.getTable().setHeaderVisible(true);
 		
 		createActions();
 
 		getSite().setSelectionProvider(viewer);
 
 		viewer.setContentProvider(new IStructuredContentProvider() {
 			@Override
 			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 				// TODO Auto-generated method stub
 
 			}
 			@Override
 			public void dispose() {
 				// TODO Auto-generated method stub
 
 			}
 			@Override
 			public Object[] getElements(Object inputElement) {
 				final Collection<IRegion> regions = getPlottingSystem().getRegions();
 				if (regions==null || regions.isEmpty()) return new Object[]{"-"};
 								
 				final List<IRegion> visible = new ArrayList<IRegion>(regions.size()/2);
 				
 				if(regions.size() % 2 == 0){
 					// add the intersection region between the two line regions					
 					for (int i=0; i< regions.size(); i = i +2){
 						// add only one region
 						IRegion pointRegion = (IRegion)(regions.toArray())[0];
 						Rectangle rect = new Rectangle();
 						rect.setX((int) xValues[0]); rect.setY((int) yValues[0]);
						((Rectangle) pointRegion).setBounds(rect);
 						visible.add(pointRegion);
 					}
 				}
 				
 				return visible.toArray(new IRegion[visible.size()]);
 			}
 		});
 
 		viewer.setInput(new Object());
 		
 		
 		//this.viewUpdateListener = new RegionColorListener();
 
 		activate();
 	}
 	
 
 	@Override
 	public Object getAdapter(@SuppressWarnings("rawtypes") Class clazz) {
 		if (clazz == IToolPageSystem.class) {
 			return plotter;
 		} else {
 			return super.getAdapter(clazz);
 		}
 	}
 
 	private void createRegions() {
 		
 		if (getPlottingSystem()==null) return;
 		try {
 			if (xHair==null || getPlottingSystem().getRegion(xHair.getName())==null) {
 				this.xHair = getPlottingSystem().createRegion(RegionUtils.getUniqueName("Y Profile", getPlottingSystem()), IRegion.RegionType.XAXIS_LINE);
 				this.xUpdateJob = addRegion("Updating x cross hair", xHair);
 
 			}
 			
 			if (yHair==null || getPlottingSystem().getRegion(yHair.getName())==null) {
 				this.yHair = getPlottingSystem().createRegion(RegionUtils.getUniqueName("X Profile", getPlottingSystem()), IRegion.RegionType.YAXIS_LINE);
 				this.yUpdateJob = addRegion("Updating x cross hair", yHair);
 			}
 			
 		} catch (Exception ne) {
 			logger.error("Cannot create information box cross-hairs!", ne);
 		}
 	}
 	
 	private RunningJob addRegion(String jobName, IRegion region) {
 		region.setVisible(false);
 		region.setTrackMouse(true);
 		region.setRegionColor(ColorConstants.red);
 		region.setUserRegion(false); // They cannot see preferences or change it!
 		getPlottingSystem().addRegion(region);
 		return new RunningJob(jobName, region);
 	}
 
 	@Override
 	public ToolPageRole getToolPageRole() {
 		return ToolPageRole.ROLE_2D;
 	}
 
 	@Override
 	public void setFocus() {
 		if (viewer!=null && !viewer.getControl().isDisposed()) viewer.getControl().setFocus();
 	}
 	
 	public void activate() {
 		
 		createRegions();
 		if (xHair!=null) {
 			if (!isActive()) xHair.addMouseListener(this);
 			xHair.setVisible(true);
 			xHair.addROIListener(this);
 		}
 		if (yHair!=null) {
 			yHair.setVisible(true);
 			yHair.addROIListener(this);
 		}
 
 		if (getPlottingSystem()!=null) {
 			getPlottingSystem().addTraceListener(traceListener);
 		}
 		
 		// We stop the adding of other regions because this tool does
 		// not like it when other regions are added.
 		setOtherRegionsEnabled(false);
 		
 		super.activate();	
 	}
 	
 	private static final String regionId = "org.dawb.workbench.ui.editors.plotting.swtxy.addRegions";
 	
 	private void setOtherRegionsEnabled(boolean isVisible) {
 
         final IActionBars bars = getPlottingSystem().getActionBars();
         if (bars.getToolBarManager().find(regionId)!=null) {
         	bars.getToolBarManager().find(regionId).setVisible(isVisible);
         	bars.getToolBarManager().update(true);
         }
         if (bars.getMenuManager().find(regionId)!=null) {
         	bars.getMenuManager().find(regionId).setVisible(isVisible);
         	bars.getMenuManager().update(true);
         }
 	}
 
 	public void deactivate() {
 		super.deactivate();
 		setOtherRegionsEnabled(true);
 
 		if (xHair!=null) {
 			xHair.removeMouseListener(this);
 			xHair.setVisible(false);
 			xHair.removeROIListener(this);
 		}
 		if (yHair!=null) {
 			yHair.setVisible(false);
 			yHair.removeROIListener(this);
 		}
 		plotter.clear();
 
 		if (getPlottingSystem()!=null) getPlottingSystem().removeTraceListener(traceListener);
 	}
 	
 	public void dispose() {
 //		if (getPlottingSystem()!=null) {
 //			getPlottingSystem().removeRegionListener(this);
 //		}
 		if (viewUpdateListener!=null) viewer.removeSelectionChangedListener(viewUpdateListener);
 		viewUpdateListener = null;
 
 		if (viewer!=null) viewer.getControl().dispose();
 
 		dragBounds.clear();
 		dragBounds = null;
 
 		super.dispose();
 	}
 	
 	@Override
 	public Control getControl() {
 		return composite;
 	}
 
 
 	/**
 	 * The user can optionally nominate an x. In this case, we would like to 
 	 * use it for the derivative instead of the indices of the data. Therefore
 	 * there is some checking here to see if there are x values to plot.
 	 * 
 	 * Normally everything will be ILineTraces even if the x is indices.
 	 */
 	private class RunningJob extends Job {
 
 		private boolean isJobRunning = false;
 		private IRegion region;
 		private boolean suspend = false;
 		
 		RunningJob(String name, IRegion region) {
 			super(name);
 			this.region = region;
 		}
 
 		@Override
 		protected IStatus run(final IProgressMonitor monitor) {
 
 			try {
 				isJobRunning = true;
 				if (!isActive()) return  Status.CANCEL_STATUS;
 	
 				if (x1==null | x2==null) return Status.OK_STATUS;
 	
 				ROIBase bounds = region==xHair ? xBounds : yBounds;
 				
 				final boolean ok = profile(region, bounds, false, null, monitor);
 
 			    return ok ? Status.OK_STATUS : Status.CANCEL_STATUS;
 			    
 			} finally {
 				isJobRunning = false;
 			}
 		}	
 		
 
 		/**
 		 * Blocks until job has been stopped, does nothing if not running.
 		 */
 		public void stop() {
 			if (isJobRunning) cancel();
 		}
 
 		public void suspend(boolean suspend) {
 			this.suspend  = suspend;
 			cancel();	
 		}
 		
 		public void scheduleIfNotSuspended() {
 			if (suspend) return;
 			super.schedule();
 		}
 	}
 	
 	@Override
 	public void regionAdded(RegionEvent evt) {
 		if (!isActive()) return;
 		if (viewer!=null) viewer.refresh();
 		if (evt.getRegion()!=null) {
 			evt.getRegion().addROIListener(this);
 		}
 	}
 
 	@Override
 	public void regionRemoved(RegionEvent evt) {
 		if (!isActive()) return;
 		if (viewer!=null) viewer.refresh();
 		if (evt.getRegion()!=null) {
 			evt.getRegion().removeROIListener(this);
 		}
 	}
 
 //	@Override
 //	public void ROIBaseDragged(ROIBaseEvent evt) {
 //
 //		if (!isActive()) return;
 //		updateRegion(evt);
 //	}
 //
 //	@Override
 //	public void ROIBaseChanged(ROIBaseEvent evt) {
 //
 //		final IRegion region = (IRegion)evt.getSource();
 //		update(region, region.getROIBase());
 //	}
 	
 //	private void update(IRegion r, ROIBase rb) {
 //		logger.debug("update");
 //				
 //		if (r == xHair) {
 //			xUpdateJob.stop();
 //			this.xBounds = rb;
 //			xUpdateJob.scheduleIfNotSuspended();
 //		}
 //		if (r == yHair) {
 //			yUpdateJob.stop();
 //			this.yBounds = rb;
 //			yUpdateJob.scheduleIfNotSuspended();
 //		}
 //		
 //	}
 
 	@Override
 	public void mousePressed(MouseEvent evt) {
 		
 		if (!isActive()) return;
 		
 		final Collection<IRegion> regions = getPlottingSystem().getRegions();
 		if (regions==null || regions.isEmpty()) logger.debug("no region selected");//return new Object[]{"-"};
 		
 		// add the resulting point region which is the intersection between the 2 line regions
 		IRegion pointRegion = (IRegion)(regions.toArray())[0];
 			
 		viewer.refresh(pointRegion);
 		viewer.add(pointRegion);
 
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent me) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseDoubleClicked(MouseEvent me) {
 		// TODO Auto-generated method stub
 	}
 
 	
 	private boolean profile(final IRegion      region, 
 			                final ROIBase bounds, 
 			                final boolean      snapshot,
 			                final Color        snapShotColor,
 			                final IProgressMonitor monitor) {
 		
 		if (bounds!=null) {
 			
 			if (monitor.isCanceled()) return  false;
 			final Collection<ITrace> traces= getPlottingSystem().getTraces(IImageTrace.class);	
 			IImageTrace image = traces!=null && traces.size()>0 ? (IImageTrace)traces.iterator().next() : null;
 
 			if (image==null) {
 				if (monitor.isCanceled()) return  false;
 				plotter.clear();
 				return true;
 			}
 
 			if (monitor.isCanceled()) return  false;
 			
             		                  
 			ILineTrace trace = (ILineTrace)plotter.getTrace(region.getName());
 			if (trace == null || snapshot) {
 				synchronized (plotter) {  // Only one job at a time can choose axis and create plot.
 					if (region.getName().startsWith("Y Profile")) {
 						plotter.setSelectedXAxis(x1);
 
 					} else {
 						plotter.setSelectedXAxis(x2);
 					}
 					if (monitor.isCanceled()) return  false;
 					logger.debug("adding here row to table");
 					trace = plotter.createLineTrace(region.getName());
 
 				    if (snapShotColor!=null) {
 				    	trace.setTraceColor(snapShotColor);
 				    } else {
 						if (region.getName().startsWith("Y Profile")) {
 							trace.setTraceColor(ColorConstants.blue);
 						} else {
 							trace.setTraceColor(ColorConstants.red);
 						}	
 				    }
 				}
 			}
 
 			final AbstractDataset data = image.getData();
 			AbstractDataset slice=null, sliceIndex=null;
 			if (monitor.isCanceled())return  false;
 			if (region.getName().startsWith("Y Profile")) {
 				int index = (int)Math.round(bounds.getPointX());
 				slice = data.getSlice(new int[]{0,index}, new int[]{data.getShape()[0], index+1}, new int[]{1,1});
 				if (monitor.isCanceled()) return  false;
 				slice = slice.flatten();
 				if (monitor.isCanceled()) return  false;
 				sliceIndex = AbstractDataset.arange(slice.getSize(), AbstractDataset.INT);
 
 			} else {
 				int index = (int)Math.round(bounds.getPointY());
 				slice = data.getSlice(new int[]{index,0}, new int[]{index+1, data.getShape()[1]}, new int[]{1,1});
 				if (monitor.isCanceled()) return  false;
 				slice = slice.flatten();
 				if (monitor.isCanceled()) return  false;
 				sliceIndex = AbstractDataset.arange(slice.getSize(), AbstractDataset.INT);
 			}
 			slice.setName(trace.getName());
 			trace.setData(sliceIndex, slice);
 
 			final ILineTrace finalTrace = trace;
 
 
 			if (monitor.isCanceled()) return  false;
 			getControl().getDisplay().syncExec(new Runnable() {
 				public void run() {
 
 					if (monitor.isCanceled()) return;
 					if (plotter.getTrace(finalTrace.getName())==null) {							
 						plotter.addTrace(finalTrace);
 					}
 
 					if (monitor.isCanceled()) return;
 					plotter.autoscaleAxes();
 					plotter.repaint();
 					if (region.getName().startsWith("Y Profile")) {
 						x1.setRange(0, data.getShape()[0]);
 					} else {
 						x2.setRange(0, data.getShape()[1]);
 					}
 				}
 			});
 		}
 		return true;
 	}
 	
 	private void createActions() {
 
 		final Action copy = new Action("Copy region values to clipboard", Activator.getImageDescriptor("icons/plot-tool-measure-copy.png")) {
 			@Override
 			public void run() {
 				if (!isActive()) return;
 				final IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
 				if (sel!=null && sel.getFirstElement()!=null) {
 					final IRegion region = (IRegion)sel.getFirstElement();
 					if (region==null||region.getROI()==null) return;
 					final ROIBase bounds = region.getROI();
 					if (bounds.getPoint()==null) return;
 
 					final Clipboard cb = new Clipboard(composite.getDisplay());
 					TextTransfer textTransfer = TextTransfer.getInstance();
 					cb.setContents(new Object[]{region.getName()+"  "+bounds}, new Transfer[]{textTransfer});
 				}
 			}
 		};
 		copy.setToolTipText("Copies the region values as text to clipboard which can then be pasted externally.");
 
 		getSite().getActionBars().getToolBarManager().add(copy);
 		getSite().getActionBars().getMenuManager().add(copy);
 
 		final Action delete = new Action("Delete selected region", Activator.getImageDescriptor("icons/plot-tool-measure-delete.png")) {
 			@Override
 			public void run() {
 				if (!isActive()) return;
 				final IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
 				if (sel!=null && sel.getFirstElement()!=null) {
 					final IRegion region = (IRegion)sel.getFirstElement();
 					getPlottingSystem().removeRegion(region);
 				}
 			}
 		};
 		delete.setToolTipText("Delete selected region, if there is one.");
 
 		getSite().getActionBars().getToolBarManager().add(delete);
 		getSite().getActionBars().getMenuManager().add(delete);
 
 		final Separator sep = new Separator(getClass().getName()+".separator1");
 		getSite().getActionBars().getToolBarManager().add(sep);
 		getSite().getActionBars().getMenuManager().add(sep);
 
 		final Action show = new Action("Show all vertex values", Activator.getImageDescriptor("icons/plot-tool-measure-vertices.png")) {
 			@Override
 			public void run() {
 				if (!isActive()) return;
 				final Object[] oa = ((IStructuredContentProvider)viewer.getContentProvider()).getElements(null);
 				for (Object object : oa) {
 					if (object instanceof IRegion) ((IRegion)object).setShowPosition(true);
 				}
 			}
 		};
 		show.setToolTipText("Show vertices in all visible regions");
 
 		getSite().getActionBars().getToolBarManager().add(show);
 		getSite().getActionBars().getMenuManager().add(show);
 
 
 		final Action clear = new Action("Show no vertex values", Activator.getImageDescriptor("icons/plot-tool-measure-clear.png")) {
 			@Override
 			public void run() {
 				if (!isActive()) return;
 				final Object[] oa = ((IStructuredContentProvider)viewer.getContentProvider()).getElements(null);
 				for (Object object : oa) {
 					if (object instanceof IRegion) ((IRegion)object).setShowPosition(false);
 				}
 			}
 		};
 		clear.setToolTipText("Clear all vertices shown in the plotting");
 
 		getSite().getActionBars().getToolBarManager().add(clear);
 		getSite().getActionBars().getMenuManager().add(clear);
 
 		createRightClickMenu();
 	}
 
 	private void createRightClickMenu() {
 		final MenuManager menuManager = new MenuManager();
 		for (IContributionItem item : getSite().getActionBars().getMenuManager().getItems()) menuManager.add(item);
 		viewer.getControl().setMenu(menuManager.createContextMenu(viewer.getControl()));
 	}
 	
 	private void createColumns(final TableViewer viewer) {
 
 		ColumnViewerToolTipSupport.enableFor(viewer,ToolTip.NO_RECREATE);
 
 		TableViewerColumn var   = new TableViewerColumn(viewer, SWT.CENTER, 0);
 		var.getColumn().setText("X position");
 		var.getColumn().setWidth(120);
 		var.setLabelProvider(new InfoPixelLabelProvider(this, 0));
 
 		var   = new TableViewerColumn(viewer, SWT.CENTER, 1);
 		var.getColumn().setText("Y position");
 		var.getColumn().setWidth(100);
 		var.setLabelProvider(new InfoPixelLabelProvider(this, 1));
 
 		var   = new TableViewerColumn(viewer, SWT.CENTER, 2);
 		var.getColumn().setText("Data value");
 		var.getColumn().setWidth(100);
 		var.setLabelProvider(new InfoPixelLabelProvider(this, 2));
 
 		var   = new TableViewerColumn(viewer, SWT.CENTER, 3);
 		var.getColumn().setText("q X (1/\u00c5)");
 		var.getColumn().setWidth(100);
 		var.setLabelProvider(new InfoPixelLabelProvider(this, 3));
 
 		var   = new TableViewerColumn(viewer, SWT.CENTER, 4);
 		var.getColumn().setText("q Y (1/\u00c5)");
 		var.getColumn().setWidth(100);
 		var.setLabelProvider(new InfoPixelLabelProvider(this, 4));
 
 		var   = new TableViewerColumn(viewer, SWT.CENTER, 5);
 		var.getColumn().setText("q Z (1/\u00c5)");
 		var.getColumn().setWidth(100);
 		var.setLabelProvider(new InfoPixelLabelProvider(this, 5));
 
 		var   = new TableViewerColumn(viewer, SWT.CENTER, 6);
 		var.getColumn().setText("2\u03b8 (\u00b0)");
 		var.getColumn().setWidth(80);
 		var.setLabelProvider(new InfoPixelLabelProvider(this, 6));
 
 		var   = new TableViewerColumn(viewer, SWT.CENTER, 7);
 		var.getColumn().setText("Resolution (\u00c5)");
 		var.getColumn().setWidth(120);
 		var.setLabelProvider(new InfoPixelLabelProvider(this, 7));
 
 		var   = new TableViewerColumn(viewer, SWT.CENTER, 8);
 		var.getColumn().setText("Dataset name");
 		var.getColumn().setWidth(120);
 		var.setLabelProvider(new InfoPixelLabelProvider(this, 8));
 		
 	}
 
 	public ROIBase getBounds(IRegion region) {
 		if (dragBounds!=null&&dragBounds.containsKey(region.getName())) return dragBounds.get(region.getName());
 		return region.getROI();
 	}
 	
 //	public double getMax(IRegion region) {
 //
 //		final Collection<ITrace> traces = getPlottingSystem().getTraces();
 //		if (traces!=null&&traces.size()==1&&traces.iterator().next() instanceof IImageTrace) {
 //			final IImageTrace     trace        = (IImageTrace)traces.iterator().next();
 //			final AbstractDataset intersection = ((Object) trace).slice(getBounds(region));
 //			return intersection.max().doubleValue();
 //		} else {
 //			return getBounds(region).getPoint()[1];
 //		}
 //	}
 
 	
 //	private void updateRegion(ROIEvent evt) {
 //
 //		if (viewer!=null) {
 //			IRegion  region = (IRegion)evt.getSource();
 //
 //			if (region.getRegionType().toString().contains("XAXIS_LINE")){
 //				this.xValues[0] = evt.getROI().getPointX();
 //			}
 //			if (region.getRegionType().toString().contains("YAXIS_LINE")){
 //				this.yValues[0] = evt.getROI().getPointY();
 //			}
 //			
 //			ROIBase rb = evt.getROI();
 //			
 //			dragBounds.put(region.getName(), rb);
 //			viewer.refresh(region);
 //		}
 //	}
 
 	@Override
 	public void regionCreated(RegionEvent evt) {
 		// TODO Auto-generated method stub		
 	}
 
 	@Override
 	public void regionsRemoved(RegionEvent evt) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void roiDragged(ROIEvent evt) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void roiChanged(ROIEvent evt) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	
 }
