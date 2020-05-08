 package org.dawnsci.plotting.tools.profile;
 
 import java.util.Collection;
 import java.util.List;
 
 import org.dawb.common.ui.plot.PlottingFactory;
 import org.dawb.common.ui.util.ColorUtility;
 import org.dawnsci.plotting.Activator;
 import org.dawnsci.plotting.api.IPlottingSystem;
 import org.dawnsci.plotting.api.PlotType;
 import org.dawnsci.plotting.api.axis.IAxis;
 import org.dawnsci.plotting.api.histogram.ImageServiceBean.ImageOrigin;
 import org.dawnsci.plotting.api.region.IROIListener;
 import org.dawnsci.plotting.api.region.IRegion;
 import org.dawnsci.plotting.api.region.IRegion.RegionType;
 import org.dawnsci.plotting.api.region.IRegionListener;
 import org.dawnsci.plotting.api.region.MouseEvent;
 import org.dawnsci.plotting.api.region.MouseListener;
 import org.dawnsci.plotting.api.region.ROIEvent;
 import org.dawnsci.plotting.api.region.RegionEvent;
 import org.dawnsci.plotting.api.region.RegionUtils;
 import org.dawnsci.plotting.api.tool.AbstractToolPage;
 import org.dawnsci.plotting.api.tool.IToolPageSystem;
 import org.dawnsci.plotting.api.trace.IImageTrace;
 import org.dawnsci.plotting.api.trace.ILineTrace;
 import org.dawnsci.plotting.api.trace.ITrace;
 import org.dawnsci.plotting.api.trace.ITraceListener;
 import org.dawnsci.plotting.api.trace.TraceEvent;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.jface.action.Action;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.part.IPageSite;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.roi.IROI;
 
 public class CrossHairProfileTool extends AbstractToolPage implements IROIListener, MouseListener  {
 
 	private final static Logger logger = LoggerFactory.getLogger(CrossHairProfileTool.class);
 	
 	protected IPlottingSystem        profilePlotter;
 	private   ITraceListener         traceListener;
 	private   IRegion                xHair, yHair;
 	private   IAxis                  x1,x2;
 	private   RunningJob             xUpdateJob, yUpdateJob;
 	private   IROI                   xBounds, yBounds;
 	
 	public CrossHairProfileTool() {
 		try {
 			
 			profilePlotter = PlottingFactory.createPlottingSystem();
 			this.traceListener = new ITraceListener.Stub() {
 				@Override
 				public void tracesAdded(TraceEvent evt) {
 					
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
 
 
 		final IPageSite site = getSite();
 		
 		profilePlotter.createPlotPart(parent, 
 								getTitle(), 
 								site.getActionBars(), 
 								PlotType.XY,
 								this.getViewPart());		
 		
 		profilePlotter.getSelectedYAxis().setTitle("Intensity");
 		x1 = profilePlotter.getSelectedXAxis();
 		x1.setTitle("X Slice");
 		
 		x2 = profilePlotter.createAxis("Y Slice", false, SWT.TOP);
 
 		final Action reset = new Action("Clear cross hair profiles", Activator.getImageDescriptor("icons/axis.png")) {
 			public void run() {
 				//profilePlotter.reset();
 				for (ITrace trace : profilePlotter.getTraces(ILineTrace.class)) {
 					profilePlotter.removeTrace(trace);
 				}
 				
 				getPlottingSystem().clearRegions();
 			}
 		};
 		getSite().getActionBars().getToolBarManager().add(reset);
 		getSite().getActionBars().getMenuManager().add(reset);
 		
 		activate();
 	}
 	
 
 	@Override
 	public Object getAdapter(@SuppressWarnings("rawtypes") Class clazz) {
 		if (clazz == IToolPageSystem.class) {
 			return profilePlotter;
 		} else {
 			return super.getAdapter(clazz);
 		}
 	}
 
 	private final static String X_PROFILE_PREFIX = "X Profile";
 	private final static String Y_PROFILE_PREFIX = "Y Profile";
 
 	private void createRegions() {
 		
 		if (getPlottingSystem()==null) return;
 		try {
 			if (xHair==null || getPlottingSystem().getRegion(xHair.getName())==null) {
 				this.xHair = getPlottingSystem().createRegion(RegionUtils.getUniqueName(Y_PROFILE_PREFIX, getPlottingSystem()), IRegion.RegionType.XAXIS_LINE);
 				this.xUpdateJob = addRegion("Updating x cross hair", xHair);
 
 			}
 			
 			if (yHair==null || getPlottingSystem().getRegion(yHair.getName())==null) {
 				this.yHair = getPlottingSystem().createRegion(RegionUtils.getUniqueName(X_PROFILE_PREFIX, getPlottingSystem()), IRegion.RegionType.YAXIS_LINE);
 				this.yUpdateJob = addRegion("Updating x cross hair", yHair);
 			}
 			
 		} catch (Exception ne) {
 			logger.error("Cannot create cross-hairs!", ne);
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
 			getPlottingSystem().setDefaultCursor(IPlottingSystem.CROSS_CURSOR);
 		}
 		
 		// We stop the adding of other regions because this tool does
 		// not like it when other regions are added.
 		setOtherRegionsEnabled(false);
 		
 		super.activate();	
 	}
 	
 	public void deactivate() {
 		super.deactivate();
 		setOtherRegionsEnabled(true);
 
 		if (xHair!=null) {
 			xHair.removeMouseListener(this);
 			xHair.setVisible(false);
 			xHair.removeROIListener(this);
 			getPlottingSystem().removeRegion(xHair);
 			xHair = null;
 		}
 		if (yHair!=null) {
 			yHair.setVisible(false);
 			yHair.removeROIListener(this);
 			getPlottingSystem().removeRegion(yHair);
 			yHair = null;
 			getPlottingSystem().setDefaultCursor(IPlottingSystem.NORMAL_CURSOR);
 		}
 		
 		if (profilePlotter!=null) profilePlotter.clear();
 
 		if (getPlottingSystem()!=null) {
 			getPlottingSystem().removeTraceListener(traceListener);
 			getPlottingSystem().setDefaultCursor(IPlottingSystem.NORMAL_CURSOR);
 		}
 	}
 	
 	private static final String regionId = "org.dawb.workbench.ui.editors.plotting.swtxy.addRegions";
 	
 	private void setOtherRegionsEnabled(boolean isVisible) {
 		if (getPlottingSystem() == null)
 			return;
 
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
 
 	
 	public void dispose() {
 		
 	    deactivate();
 		if (profilePlotter!=null) profilePlotter.dispose();
 		profilePlotter = null;
 		super.dispose();
 	}
 	
 	@Override
 	public Control getControl() {
 		if (profilePlotter==null) return null;
 		return profilePlotter.getPlotComposite();
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
 	
 				IROI bounds = region==xHair ? xBounds : yBounds;
 				
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
 	public void roiDragged(ROIEvent evt) {
 		update((IRegion)evt.getSource(), evt.getROI());
 	}
 
 	@Override
 	public void roiChanged(ROIEvent evt) {
 		final IRegion region = (IRegion)evt.getSource();
 		update(region, region.getROI());
 	}
 	
 	private void update(IRegion r, IROI rb) {
 		if (r == xHair) {
 			xUpdateJob.stop();
 			this.xBounds = rb;
 			xUpdateJob.scheduleIfNotSuspended();
 		}
 		if (r == yHair) {
 			yUpdateJob.stop();
 			this.yBounds = rb;
 			yUpdateJob.scheduleIfNotSuspended();
 		}
 	}
 
 	@Override
 	public void mousePressed(MouseEvent me) {
 		
 		if (!isActive()) return;
 		if (me.getButton()!=1) return;
 		try {
 			xUpdateJob.suspend(true);
 			yUpdateJob.suspend(true);
 	
	        final Color   snapShotColor = RegionUtils.getUniqueColor(xHair.getRegionType(), getPlottingSystem(), ColorUtility.DEFAULT_SWT_COLORS);
 	        final IRegion x = createStaticRegion(Y_PROFILE_PREFIX + " Static", xBounds, snapShotColor, xHair.getRegionType());
 	        profile(x, xBounds, true, snapShotColor, new NullProgressMonitor());
 	        
 	        final IRegion y = createStaticRegion(X_PROFILE_PREFIX + " Static", yBounds, snapShotColor, yHair.getRegionType());
 	        profile(y, yBounds, true, snapShotColor, new NullProgressMonitor());
 
 			//getPlottingSystem().repaint();
 		} catch (Exception ne) {
 			logger.error(ne.getMessage(), ne);
 			
 		} finally {
 			xUpdateJob.suspend(false);
 			yUpdateJob.suspend(false);
 		}
 	}
 
 	private IRegion createStaticRegion(String nameStub, final IROI bounds, final Color snapShotColor, final RegionType regionType) throws Exception {
 		
 
 		final IRegion region = getPlottingSystem().createRegion(RegionUtils.getUniqueName(nameStub, getPlottingSystem()), regionType);
 		region.setRegionColor(snapShotColor);
 		getPlottingSystem().addRegion(region);
 		region.setROI(bounds);
         getPlottingSystem().addRegionListener(new IRegionListener.Stub() {
     		@Override
     		public void regionRemoved(RegionEvent evt) {
     			if (profilePlotter.getTrace(region.getName())!=null) {
     				profilePlotter.removeTrace(profilePlotter.getTrace(region.getName()));
     			}
     		}
         });
         
         region.addROIListener(new IROIListener.Stub() {
         	@Override
     		public void roiDragged(ROIEvent evt) {
         		if (!isActive()) return;
         		profile(region, evt.getROI(), false, snapShotColor, new NullProgressMonitor());
     		}
         });
 		
 		return region;
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent me) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseDoubleClicked(MouseEvent me) {
 		// When clicked adds new plot
 	}
 
 	
 	private boolean profile(final IRegion      region, 
 			                final IROI         bounds, 
 			                final boolean      snapshot,
 			                final Color        snapShotColor,
 			                final IProgressMonitor monitor) {
 		
 		if (bounds!=null) {
 			
 			if (monitor.isCanceled()) return  false;
 			final Collection<ITrace> traces= getPlottingSystem().getTraces(IImageTrace.class);	
 			IImageTrace image = traces!=null && traces.size()>0 ? (IImageTrace)traces.iterator().next() : null;
 
 			if (image==null) {
 				if (monitor.isCanceled()) return  false;
 				profilePlotter.clear();
 				return true;
 			}
 
 			if (monitor.isCanceled()) return  false;
 			final boolean isRegionXAxis = isXAxis(region);
             		                  
 			ILineTrace trace = (ILineTrace)profilePlotter.getTrace(region.getName());
 			if ((trace == null && region.isVisible()) || snapshot) {
 				synchronized (profilePlotter) {  // Only one job at a time can choose axis and create plot.
 					if (isRegionXAxis) {
 						profilePlotter.setSelectedXAxis(x1);
 
 					} else {
 						profilePlotter.setSelectedXAxis(x2);
 					}
 					if (monitor.isCanceled()) return  false;
 					trace = profilePlotter.createLineTrace(region.getName());
 
 				    if (snapShotColor!=null) {
 				    	trace.setTraceColor(snapShotColor);
 				    } else {
 						if (isXAxis(region)) {
 							trace.setTraceColor(ColorConstants.blue);
 						} else {
 							trace.setTraceColor(ColorConstants.red);
 						}	
 				    }
 				}
 			} else if (!region.isVisible() && trace!=null) {
 				final ITrace finalTrace = trace;
 				getControl().getDisplay().syncExec(new Runnable() {
 					public void run() {
 				        profilePlotter.removeTrace(finalTrace);
 				        profilePlotter.repaint();
 					}
 				});
 				return false;
 			}
 
 			final AbstractDataset data = (AbstractDataset)image.getData();
 			AbstractDataset slice=null, sliceIndex=null;
 			if (monitor.isCanceled())return  false;
 			try {
 				int[] shape = data.getShape();
 				if (isRegionXAxis) {
 					int index = (int)Math.round(bounds.getPointX());
 					slice = data.getSlice(new int[]{0,index}, new int[]{shape[0], index+1}, new int[]{1,1});
 				} else {
 					int index = (int)Math.round(bounds.getPointY());
 					slice = data.getSlice(new int[]{index,0}, new int[]{index+1, shape[1]}, new int[]{1,1});
 				}
 			} catch (Throwable ne) {
 				logger.error("Cannot slice using "+bounds, ne);
 				return false;
 			}
 			
 			if (monitor.isCanceled()) return  false;
 			slice = slice.flatten();
 			if (monitor.isCanceled()) return  false;
 			final int size = slice.getSize();
 			sliceIndex = AbstractDataset.arange(size, AbstractDataset.INT);
 			if (trace==null) return false;
 			slice.setName(trace.getName());
 			trace.setData(sliceIndex, slice);
 
 			final ILineTrace finalTrace = trace;
 
 
 			if (monitor.isCanceled()) return  false;
 			getControl().getDisplay().syncExec(new Runnable() {
 				public void run() {
 
 					if (monitor.isCanceled()) return;
 					if (profilePlotter.getTrace(finalTrace.getName())==null) {							
 						profilePlotter.addTrace(finalTrace);
 					}
 
 					if (monitor.isCanceled()) return;
 					if (isRegionXAxis) {
 						x1.setRange(0, size);
 					} else {
 						x2.setRange(0, size);
 					}
 					profilePlotter.autoscaleAxes();
 				}
 			});
 		}
 		return true;
 	}
 
 	private boolean isXAxis(IRegion region) {
 		boolean isXAxis  = region.getName().startsWith(Y_PROFILE_PREFIX); // Y profile is x-axis
 		boolean isYAxis  = region.getName().startsWith(X_PROFILE_PREFIX); // X profile is y-axis
 		ImageOrigin orig = getImageTrace().getImageOrigin();
 		return ( isXAxis && orig==ImageOrigin.TOP_LEFT )   ||
 			   ( isXAxis && orig==ImageOrigin.BOTTOM_RIGHT) ||
 			   ( isYAxis && orig==ImageOrigin.TOP_RIGHT)   ||
 			   ( isYAxis && orig==ImageOrigin.BOTTOM_LEFT);
 	}
 	@Override
 	public void roiSelected(ROIEvent evt) {
 		// TODO Auto-generated method stub
 
 	}
 }
