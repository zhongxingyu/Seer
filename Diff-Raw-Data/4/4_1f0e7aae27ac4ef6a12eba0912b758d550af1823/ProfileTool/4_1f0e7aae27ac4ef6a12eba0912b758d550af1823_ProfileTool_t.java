 package org.dawb.workbench.plotting.tools.profile;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 
 import org.dawb.common.ui.plot.AbstractPlottingSystem;
 import org.dawb.common.ui.plot.PlotType;
 import org.dawb.common.ui.plot.PlottingFactory;
 import org.dawb.common.ui.plot.region.IROIListener;
 import org.dawb.common.ui.plot.region.IRegion;
 import org.dawb.common.ui.plot.region.IRegion.RegionType;
 import org.dawb.common.ui.plot.region.IRegionListener;
 import org.dawb.common.ui.plot.region.ROIEvent;
 import org.dawb.common.ui.plot.region.RegionEvent;
 import org.dawb.common.ui.plot.region.RegionUtils;
 import org.dawb.common.ui.plot.tool.AbstractToolPage;
 import org.dawb.common.ui.plot.tool.IDataReductionToolPage;
 import org.dawb.common.ui.plot.tool.IToolPage;
 import org.dawb.common.ui.plot.tool.IToolPageSystem;
 import org.dawb.common.ui.plot.tool.IToolPage.ToolPageRole;
 import org.dawb.common.ui.plot.trace.IImageTrace;
 import org.dawb.common.ui.plot.trace.ILineTrace;
 import org.dawb.common.ui.plot.trace.IPaletteListener;
 import org.dawb.common.ui.plot.trace.ITrace;
 import org.dawb.common.ui.plot.trace.ITraceListener;
 import org.dawb.common.ui.plot.trace.PaletteEvent;
 import org.dawb.common.ui.plot.trace.TraceEvent;
 import org.dawb.common.ui.util.EclipseUtils;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.part.IPageSite;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.io.IMetaData;
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
 
 public abstract class ProfileTool extends AbstractToolPage  implements IROIListener, IDataReductionToolPage {
 
 	private final static Logger logger = LoggerFactory.getLogger(ProfileTool.class);
 	
 	protected AbstractPlottingSystem profilePlottingSystem;
 	private   ITraceListener         traceListener;
 	private   IRegionListener        regionListener;
 	private   IPaletteListener       paletteListener;
 	private   ProfileJob             updateProfiles;
 	private   Map<String,Collection<ITrace>> registeredTraces;
 
 	public ProfileTool() {
 		
 		this.registeredTraces = new HashMap<String,Collection<ITrace>>(7);
 		try {
 			profilePlottingSystem = PlottingFactory.createPlottingSystem();
 			updateProfiles = new ProfileJob();
 			
 			this.paletteListener = new IPaletteListener.Stub() {
 				@Override
 				public void maskChanged(PaletteEvent evt) {
 					update(null, null, false);
 				}
 				@Override
 				public void imageOriginChanged(PaletteEvent evt) {
 					update(null, null, false);
 				}
 			};
 			
 			this.traceListener = new ITraceListener.Stub() {
 				@Override
 				public void tracesAdded(TraceEvent evt) {
 					
 					if (!(evt.getSource() instanceof List<?>)) {
 						return;
 					}
 					if (getImageTrace()!=null) getImageTrace().addPaletteListener(paletteListener);
 					ProfileTool.this.update(null, null, false);
 				}
 				@Override
 				protected void update(TraceEvent evt) {
 					ProfileTool.this.update(null, null, false);
 				}
 
 			};
 			
 			this.regionListener = new IRegionListener.Stub() {			
 				@Override
 				public void regionRemoved(RegionEvent evt) {
 					if (evt.getRegion()!=null) {
 						evt.getRegion().removeROIListener(ProfileTool.this);
 						clearTraces(evt.getRegion());
 					}
 				}
 				@Override
 				public void regionAdded(RegionEvent evt) {
 					if (evt.getRegion()!=null) {
 						ProfileTool.this.update(null, null, false);
 					}
 				}
 				
 				@Override
 				public void regionCreated(RegionEvent evt) {
 					if (evt.getRegion()!=null) {
 						evt.getRegion().addROIListener(ProfileTool.this);
 					}
 				}
 				
 				protected void update(RegionEvent evt) {
 					ProfileTool.this.update(null, null, false);
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
 		
 		// Used to set the line on the image to the same color as the plot for line profiles only.
 		if (!traces.isEmpty()) {
 			final ITrace first = traces.iterator().next();
 			if (isRegionTypeSupported(RegionType.LINE) && first instanceof ILineTrace && region.getName().startsWith(getRegionName())) {
 				getControl().getDisplay().syncExec(new Runnable() {
 					public void run() {
 						region.setRegionColor(((ILineTrace)first).getTraceColor());
 					}
 				});
 			}
 		}
 	}
 	
 	protected void clearTraces(final IRegion region) {
 		final String name = region.getName();
 		Collection<ITrace> registered = this.registeredTraces.get(name);
         if (registered!=null) for (ITrace iTrace : registered) {
 			profilePlottingSystem.removeTrace(iTrace);
 		}
 	}
 
 	@Override
 	public void createControl(Composite parent) {
 		final IPageSite site = getSite();
 		createControl(parent, site!=null?site.getActionBars():null);
 	}
 
 	public void createControl(Composite parent, IActionBars actionbars) {
 		
 		final Action reselect = new Action("Create new profile.", getImageDescriptor()) {
 			public void run() {
 				createNewRegion();
 			}
 		};
 		if (actionbars != null){
			actionbars.getToolBarManager().add(new Separator("org.dawb.workbench.plotting.tools.profile.newProfileGroup"));
			actionbars.getToolBarManager().insertAfter("org.dawb.workbench.plotting.tools.profile.newProfileGroup", reselect);
 		}
 
 		profilePlottingSystem.createPlotPart(parent, 
 											 getTitle(), 
 											 actionbars, 
 											 PlotType.XY,
 											 this.getViewPart());				
 		
 		
 		configurePlottingSystem(profilePlottingSystem);
 		
 		// Unused actions removed for tool
 		profilePlottingSystem.getPlotActionSystem().remove("org.dawb.workbench.plotting.rescale");
 		profilePlottingSystem.getPlotActionSystem().remove("org.dawb.workbench.plotting.plotIndex");
 		profilePlottingSystem.getPlotActionSystem().remove("org.dawb.workbench.plotting.plotX");
 
 		profilePlottingSystem.setXfirst(true);
 	}
 
 	@Override
 	public Object getAdapter(@SuppressWarnings("rawtypes") Class clazz) {
 		if (clazz == IToolPageSystem.class) {
 			return profilePlottingSystem;
 		} else {
 			return super.getAdapter(clazz);
 		}
 	}
 
 	protected abstract void configurePlottingSystem(AbstractPlottingSystem plotter);
 	 
 	
 	@Override
 	public ToolPageRole getToolPageRole() {
 		return ToolPageRole.ROLE_2D;
 	}
 
 	@Override
 	public void setFocus() {
 		
 	}
 	
 	public void activate() {
 		super.activate();
 		update(null, null, false);
 		if (getPlottingSystem()!=null) {
 			getPlottingSystem().addTraceListener(traceListener);
 		}
 		if (getPlottingSystem()!=null) {
 			getPlottingSystem().addRegionListener(regionListener);
 		}	
 		
 		if (getPlottingSystem()!=null) {
 			final Collection<IRegion> regions = getPlottingSystem().getRegions();
 			if (regions!=null) for (IRegion iRegion : regions) iRegion.addROIListener(this);
 		}
 		
 		// We try to listen to the image mask changing and reprofile if it does.
 		if (getPlottingSystem()!=null) {
 			if (getImageTrace()!=null) getImageTrace().addPaletteListener(paletteListener);
 		}
 		
 		createNewRegion();
 	}
 	
 	private void createNewRegion() {
 		// Start with a selection of the right type
 		try {
 			getPlottingSystem().createRegion(RegionUtils.getUniqueName(getRegionName(), getPlottingSystem()), getCreateRegionType());
 		} catch (Exception e) {
 			logger.error("Cannot create region for profile tool!");
 		}
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	protected abstract boolean isRegionTypeSupported(RegionType type);
 	
 	/**
 	 * 
 	 */
     protected abstract RegionType getCreateRegionType();
     
 	public void deactivate() {
 		super.deactivate();
 		if (getPlottingSystem()!=null) {
 			getPlottingSystem().removeTraceListener(traceListener);
 		}
 		if (getPlottingSystem()!=null) {
 			getPlottingSystem().removeRegionListener(regionListener);
 		}
 		if (getPlottingSystem()!=null) {
 			final Collection<IRegion> regions = getPlottingSystem().getRegions();
 			if (regions!=null) for (IRegion iRegion : regions) iRegion.removeROIListener(this);
 		}
 		if (getPlottingSystem()!=null) {
 			if (getImageTrace()!=null) getImageTrace().removePaletteListener(paletteListener);
 		}
 
 	}
 	
 	@Override
 	public Control getControl() {
 		if (profilePlottingSystem==null) return null;
 		return profilePlottingSystem.getPlotComposite();
 	}
 	
 	public void dispose() {
 		deactivate();
 		
 		registeredTraces.clear();
 		if (profilePlottingSystem!=null) profilePlottingSystem.dispose();
 		profilePlottingSystem = null;
 		super.dispose();
 	}
 
 	/**
 	 * 
 	 * @param image
 	 * @param region
 	 * @param roi - may be null
 	 * @param monitor
 	 */
 	protected abstract void createProfile(IImageTrace image, 
 			                              IRegion region, 
 			                              ROIBase roi, 
 			                              boolean tryUpdate, 
 			                              boolean isDrag,
 			                              IProgressMonitor monitor);
 
 	@Override
 	public void roiDragged(ROIEvent evt) {
 		update((IRegion)evt.getSource(), evt.getROI(), true);
 	}
 
 	@Override
 	public void roiChanged(ROIEvent evt) {
 		final IRegion region = (IRegion)evt.getSource();
 		update(region, region.getROI(), false);
 		
 		
         getControl().getDisplay().asyncExec(new Runnable() {
         	public void run() {
         		profilePlottingSystem.autoscaleAxes();
         	}
         });
 
 	}
 	
 	protected synchronized void update(IRegion r, ROIBase rb, boolean isDrag) {
 	
 		if (r!=null && !isRegionTypeSupported(r.getRegionType())) return; // Nothing to do.
          
 		updateProfiles.profile(r, rb, isDrag);
 	}
 	
 	protected String getRegionName() {
 		return "Profile";
 	}
 	
 	private final class ProfileJob extends Job {
 		
 		private   IRegion                currentRegion;
 		private   ROIBase                currentROI;
 		private   boolean                isDrag;
 
 		ProfileJob() {
 			super(getRegionName()+" update");
 			setSystem(true);
 			setUser(false);
 			setPriority(Job.INTERACTIVE);
 		}
 
 		public void profile(IRegion r, ROIBase rb, boolean isDrag) {
 
 	        // This in principle is not needed and appears to make no difference wether in or out.
 		    // However Irakli has advised that it is needed in some circumstances.
 			// This causes the defect reported here however: http://jira.diamond.ac.uk/browse/DAWNSCI-214
 			// therefore we are currently not using the extra cancelling.
 	        //for (Job job : Job.getJobManager().find(null))
 	        //    if (job.getClass()==getClass() && job.getState() != Job.RUNNING)
 	        //	    job.cancel();
 
 			this.currentRegion = r;
 			this.currentROI    = rb;
 			this.isDrag        = isDrag;
 	        
           	schedule();		
 		}
 
 		@Override
 		protected IStatus run(IProgressMonitor monitor) {
 
 			if (!isActive()) return Status.CANCEL_STATUS;
 
 			final Collection<ITrace> traces= getPlottingSystem().getTraces(IImageTrace.class);	
 			IImageTrace image = traces!=null && traces.size()>0 ? (IImageTrace)traces.iterator().next() : null;
 
 			if (monitor.isCanceled()) return  Status.CANCEL_STATUS;
 			if (image==null) {
 				profilePlottingSystem.clear();
 				return Status.OK_STATUS;
 			}
 
 			// Get the profiles from the line and box regions.
 			if (currentRegion==null) {
 				profilePlottingSystem.clear();
 				registeredTraces.clear();
 				final Collection<IRegion> regions = getPlottingSystem().getRegions();
 				if (regions!=null) {
 					for (IRegion iRegion : regions) {
 						if (monitor.isCanceled()) return  Status.CANCEL_STATUS;
 						createProfile(image, iRegion, null, false, isDrag, monitor);
 					}
 				}
 			} else {
 
 				if (monitor.isCanceled()) return  Status.CANCEL_STATUS;
 				createProfile(image, 
 						currentRegion, 
 						currentROI!=null?currentROI:currentRegion.getROI(), 
 								true, 
 								isDrag,
 								monitor);
 
 			}
 
 			if (monitor.isCanceled()) return Status.CANCEL_STATUS;
 			profilePlottingSystem.repaint();
 
 			return Status.OK_STATUS;
 
 		}	
 		
 		
 	}
 	
 	/**
 	 * Tries to get the meta from the editor part or uses the one in AbtractDataset of the image
 	 * @return IMetaData, may be null
 	 */
 	protected IMetaData getMetaData() {
 		
 		if (getPart() instanceof IEditorPart) {
 			IEditorPart editor = (IEditorPart)getPart();
 	    	try {
 				return LoaderFactory.getMetaData(EclipseUtils.getFilePath(editor.getEditorInput()), null);
 			} catch (Exception e) {
 				logger.error("Cannot get meta data for "+EclipseUtils.getFilePath(editor.getEditorInput()), e);
 			}
 		}
 		
 		return getImageTrace().getData().getMetadata();
 	}
 	
 	/**
 	 * Used to tell if tool can be used with multiple slice 'Data Reduction' tool.
 	 */
 	public boolean isProfileTool() {
 		return true;
 	}
 }
