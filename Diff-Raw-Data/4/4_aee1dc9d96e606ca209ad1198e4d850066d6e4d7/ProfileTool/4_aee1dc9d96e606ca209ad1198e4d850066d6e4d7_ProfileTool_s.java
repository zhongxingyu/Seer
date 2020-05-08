 package org.dawb.workbench.plotting.tools;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 
 import org.dawb.common.ui.plot.AbstractPlottingSystem;
 import org.dawb.common.ui.plot.PlotType;
 import org.dawb.common.ui.plot.PlottingFactory;
 import org.dawb.common.ui.plot.region.IRegion;
 import org.dawb.common.ui.plot.region.IRegion.RegionType;
 import org.dawb.common.ui.plot.region.IRegionBoundsListener;
 import org.dawb.common.ui.plot.region.IRegionListener;
 import org.dawb.common.ui.plot.region.RegionBounds;
 import org.dawb.common.ui.plot.region.RegionBoundsEvent;
 import org.dawb.common.ui.plot.region.RegionEvent;
 import org.dawb.common.ui.plot.region.RegionUtils;
 import org.dawb.common.ui.plot.tool.AbstractToolPage;
 import org.dawb.common.ui.plot.tool.IToolPageSystem;
 import org.dawb.common.ui.plot.trace.IImageTrace;
 import org.dawb.common.ui.plot.trace.ILineTrace;
 import org.dawb.common.ui.plot.trace.ITrace;
 import org.dawb.common.ui.plot.trace.ITraceListener;
 import org.dawb.common.ui.plot.trace.TraceEvent;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.ui.part.IPageSite;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public abstract class ProfileTool extends AbstractToolPage  implements IRegionBoundsListener {
 
 	private final static Logger logger = LoggerFactory.getLogger(ProfileTool.class);
 	
 	protected AbstractPlottingSystem plotter;
 	private   ITraceListener         traceListener;
 	private   IRegionListener        regionListener;
 	private   Job                    updateProfiles;
 	private   IRegion                currentRegion;
 	private   RegionBounds           currentBounds;
 	private   Map<String,Collection<ITrace>> registeredTraces;
 
 	public ProfileTool() {
 		
 		this.registeredTraces = new HashMap<String,Collection<ITrace>>(7);
 		try {
 			plotter = PlottingFactory.getPlottingSystem();
 			updateProfiles = createProfileJob();
 			
 			this.traceListener = new ITraceListener.Stub() {
 				@Override
 				public void tracesPlotted(TraceEvent evt) {
 					
 					if (!(evt.getSource() instanceof List<?>)) {
 						return;
 					}
 					update(null, null);
 				}
 			};
 			
 			this.regionListener = new IRegionListener.Stub() {			
 				@Override
 				public void regionRemoved(RegionEvent evt) {
 					if (evt.getRegion()!=null) {
 						evt.getRegion().removeRegionBoundsListener(ProfileTool.this);
 						clearTraces(evt.getRegion());
 					}
 				}
 				@Override
 				public void regionAdded(RegionEvent evt) {
 					if (evt.getRegion()!=null) {
 						update(null, null);
 					}
 				}
 				
 				@Override
 				public void regionCreated(RegionEvent evt) {
 					if (evt.getRegion()!=null) {
 						evt.getRegion().addRegionBoundsListener(ProfileTool.this);
 					}
 				}
 			};
 		} catch (Exception e) {
 			logger.error("Cannot get plotting system!", e);
 		}
 	}
 	
 	protected void registerTraces(final IRegion region, final Collection<ITrace> traces) {
 		
 		final String name = region.getName();
 		Collection<ITrace> registered = this.registeredTraces.get(name);
 		if (registered==null) {
 			registered = new HashSet<ITrace>(7);
 			registeredTraces.put(name, registered);
 		}
 		registered.addAll(traces);
 		
 		final ITrace first = traces.iterator().next();
 		if (getRegionType()==RegionType.LINE && first instanceof ILineTrace && region.getName().startsWith("Profile")) {
 			getControl().getDisplay().syncExec(new Runnable() {
 				public void run() {
 					region.setRegionColor(((ILineTrace)first).getTraceColor());
 				}
 			});
 		}
 	}
 	
 	protected void clearTraces(final IRegion region) {
 		final String name = region.getName();
 		Collection<ITrace> registered = this.registeredTraces.get(name);
         if (registered!=null) for (ITrace iTrace : registered) {
 			plotter.removeTrace(iTrace);
 		}
 	}
 	
 	@Override
 	public void createControl(Composite parent) {
 
 
 		final IPageSite site = getSite();
 		
 		plotter.createPlotPart(parent, 
 								getTitle(), 
 								site.getActionBars(), 
 								PlotType.PT1D,
 								this.getPart());		
 
 		createAxes(plotter);
 	}
 
 	@Override
 	public Object getAdapter(Class clazz) {
 		if (clazz == IToolPageSystem.class) {
 			return plotter;
 		} else {
 			return super.getAdapter(clazz);
 		}
 	}
 
 	protected abstract void createAxes(AbstractPlottingSystem plotter);
 	 
 	
 	@Override
 	public ToolPageRole getToolPageRole() {
 		return ToolPageRole.ROLE_2D;
 	}
 
 	@Override
 	public void setFocus() {
 		
 	}
 	
 	public void activate() {
 		super.activate();
 		update(null, null);
 		if (getPlottingSystem()!=null) {
 			getPlottingSystem().addTraceListener(traceListener);
 		}
 		if (getPlottingSystem()!=null) {
 			getPlottingSystem().addRegionListener(regionListener);
 		}		
 		final Collection<IRegion> regions = getPlottingSystem().getRegions();
 		if (regions!=null) for (IRegion iRegion : regions) iRegion.addRegionBoundsListener(this);
 		
 		// Start with a selection of the right type
 		try {
 			getPlottingSystem().createRegion(RegionUtils.getUniqueName("Profile", getPlottingSystem()), getRegionType());
 		} catch (Exception e) {
 			logger.error("Cannot create region for profile tool!");
 		}
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	protected abstract RegionType getRegionType();
 
 	public void deactivate() {
 		super.deactivate();
 		if (getPlottingSystem()!=null) {
 			getPlottingSystem().removeTraceListener(traceListener);
 		}
 		if (getPlottingSystem()!=null) {
 			getPlottingSystem().removeRegionListener(regionListener);
 		}
 		final Collection<IRegion> regions = getPlottingSystem().getRegions();
 		if (regions!=null) for (IRegion iRegion : regions) iRegion.removeRegionBoundsListener(this);
 	}
 	
 	@Override
 	public Control getControl() {
 		if (plotter==null) return null;
 		return plotter.getPlotComposite();
 	}
 	
 	public void dispose() {
 		deactivate();
 		
 		registeredTraces.clear();
 		if (plotter!=null) plotter.dispose();
 		plotter = null;
 		super.dispose();
 	}
 
 	/**
 	 * The user can optionally nominate an x. In this case, we would like to 
 	 * use it for the derviative instead of the indices of the data. Therefore
 	 * there is some checking here to see if there are x values to plot.
 	 * 
 	 * Normally everything will be ILineTraces even if the x is indices.
 	 */
 	private Job createProfileJob() {
 
 		Job job = new Job("Profile update") {
 
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 
 				final Collection<ITrace> traces= getPlottingSystem().getTraces(IImageTrace.class);	
 				IImageTrace image = traces!=null && traces.size()>0 ? (IImageTrace)traces.iterator().next() : null;
 
 				if (image==null) {
 					plotter.clear();
 					return Status.OK_STATUS;
 				}
 
 				// Get the profiles from the line and box regions.
 				if (currentRegion==null) {
 					plotter.clear();
 					registeredTraces.clear();
 					final Collection<IRegion> regions = getPlottingSystem().getRegions();
 					if (regions!=null) {
 						for (IRegion iRegion : regions) {
 							createProfile(image, iRegion, null, false, monitor);
 						}
 					}
 				} else {
 
 					createProfile(image, 
 							      currentRegion, 
 							      currentBounds!=null?currentBounds:currentRegion.getRegionBounds(), 
 							      true, 
 							      monitor);
 					
 				}
 
 				if (monitor.isCanceled()) return Status.CANCEL_STATUS;
                 plotter.repaint();
                                 
 				return Status.OK_STATUS;
 
 			}	
 		};
 		job.setSystem(true);
 		job.setUser(false);
 		job.setPriority(Job.INTERACTIVE);
 
 		return job;
 	}
 
 	/**
 	 * 
 	 * @param image
 	 * @param region
 	 * @param bounds - may be null
 	 * @param monitor
 	 */
 	protected abstract void createProfile(IImageTrace image, IRegion region, final RegionBounds bounds, boolean tryUpdate, IProgressMonitor monitor);
 
 	@Override
 	public void regionBoundsDragged(RegionBoundsEvent evt) {
 		update((IRegion)evt.getSource(), evt.getRegionBounds());
 	}
 
 	@Override
 	public void regionBoundsChanged(RegionBoundsEvent evt) {
 		final IRegion region = (IRegion)evt.getSource();
 		update(region, region.getRegionBounds());
 		
 		try {
 			updateProfiles.join();
 		} catch (InterruptedException e) {
 			logger.error("Update profiles job interrupted!", e);
 		}
 		
         getControl().getDisplay().syncExec(new Runnable() {
         	public void run() {
         		plotter.autoscaleAxes();
         	}
         });
 
 	}
 	
	private void update(IRegion r, RegionBounds rb) {
		if (r.getRegionType()!=getRegionType()) return; // Nothing to do.
 		/**
 		 * TODO FIXME This does not quite work because currentRegion can change
 		 * before the job has finished. Join would not help because not much work
 		 * should be done on regionBoundsDragged(...)
 		 */
 		this.currentRegion = r;
 		this.currentBounds = rb;
 		updateProfiles.schedule();
 	}
 }
