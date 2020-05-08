 package org.dawb.workbench.plotting.tools.fitting;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 import ncsa.hdf.object.Dataset;
 import ncsa.hdf.object.Datatype;
 import ncsa.hdf.object.h5.H5Datatype;
 
 import org.dawb.common.ui.image.IconUtils;
 import org.dawb.common.ui.menu.CheckableActionGroup;
 import org.dawb.common.ui.menu.MenuAction;
 import org.dawb.common.ui.plot.annotation.AnnotationUtils;
 import org.dawb.common.ui.plot.annotation.IAnnotation;
 import org.dawb.common.ui.plot.region.IRegion;
 import org.dawb.common.ui.plot.region.IRegion.RegionType;
 import org.dawb.common.ui.plot.region.IRegionListener;
 import org.dawb.common.ui.plot.region.RegionUtils;
 import org.dawb.common.ui.plot.tool.IDataReductionToolPage;
 import org.dawb.common.ui.plot.trace.ILineTrace;
 import org.dawb.common.ui.plot.trace.TraceUtils;
 import org.dawb.common.ui.util.EclipseUtils;
 import org.dawb.gda.extensions.loaders.H5Utils;
 import org.dawb.hdf5.IHierarchicalDataFile;
 import org.dawb.hdf5.Nexus;
 import org.dawb.hdf5.nexus.NexusUtils;
 import org.dawb.workbench.plotting.Activator;
 import org.dawb.workbench.plotting.preference.FittingConstants;
 import org.dawb.workbench.plotting.preference.FittingPreferencePage;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.preference.PreferenceDialog;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.window.ToolTip;
 import org.eclipse.swt.SWT;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.dialogs.PreferencesUtil;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
 import uk.ac.diamond.scisoft.analysis.fitting.Generic1DFitter;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.IPeak;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.IdentifiedPeak;
 import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
 import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
 
 public class PeakFittingTool extends AbstractFittingTool implements IRegionListener, IDataReductionToolPage {
 
 	private static final Logger logger = LoggerFactory.getLogger(PeakFittingTool.class);
 	private MenuAction numberPeaks;
 
 	public PeakFittingTool() {
 		super();
 		
 		
 		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
 			
 			@Override
 			public void propertyChange(PropertyChangeEvent event) {
 				if (isActive()) {
 					if (isInterestedProperty(event)) {
 						if (isActive()) fittingJob.fit();
 						
 						if (FittingConstants.PEAK_NUMBER.equals(event.getProperty())) {
 							final int ipeak = Activator.getDefault().getPreferenceStore().getInt(FittingConstants.PEAK_NUMBER);
 							if (numberPeaks!=null) {
 								if (ipeak<11) {
 									numberPeaks.setSelectedAction(ipeak-1);
 									numberPeaks.setCheckedAction(ipeak-1, true);
 								} else {
 									numberPeaks.setSelectedAction(10);
 									numberPeaks.setCheckedAction(10, true);
 								}
 							}
 						}
 					}
 				}
  			}
 
 			private boolean isInterestedProperty(PropertyChangeEvent event) {
 				final String propName = event.getProperty();
 				return FittingConstants.PEAK_NUMBER.equals(propName) ||
 					   FittingConstants.SMOOTHING.equals(propName)   ||
 					   FittingConstants.QUALITY.equals(propName);
 			}
 		});
 		
 
 	}
 
 	@Override
 	protected List<TableViewerColumn> createColumns(final TableViewer viewer) {
 		
 		ColumnViewerToolTipSupport.enableFor(viewer,ToolTip.NO_RECREATE);
 
 		List<TableViewerColumn> ret = new ArrayList<TableViewerColumn>(9);
 		
         TableViewerColumn var   = new TableViewerColumn(viewer, SWT.LEFT, 0);
 		var.getColumn().setText("Trace");
 		var.getColumn().setWidth(80);
 		var.setLabelProvider(new PeakLabelProvider(0));
 		ret.add(var);
 
 		var   = new TableViewerColumn(viewer, SWT.LEFT, 1);
 		var.getColumn().setText("Name");
 		var.getColumn().setWidth(150);
 		var.setLabelProvider(new PeakLabelProvider(1));
 		ret.add(var);
 		
         var   = new TableViewerColumn(viewer, SWT.CENTER, 2);
 		var.getColumn().setText("Position");
 		var.getColumn().setWidth(100);
 		var.setLabelProvider(new PeakLabelProvider(2));
 		ret.add(var);
 		
         var   = new TableViewerColumn(viewer, SWT.CENTER, 3);
 		var.getColumn().setText("Data");
 		var.getColumn().setToolTipText("The nearest data value of the fitted peak.");
 		var.getColumn().setWidth(100);
 		var.setLabelProvider(new PeakLabelProvider(3));
 		ret.add(var);
 		
 		// Data Column not that useful, do not show unless property set.
 		if (!Boolean.getBoolean("org.dawb.workbench.plotting.tools.fitting.tool.data.column.required")) {
 			var.getColumn().setWidth(0);
 			var.getColumn().setResizable(false);
 		}
 		
         var   = new TableViewerColumn(viewer, SWT.CENTER, 4);
 		var.getColumn().setText("Fit");
 		var.getColumn().setToolTipText("The value of the fitted peak.");
 		var.getColumn().setWidth(100);
 		var.setLabelProvider(new PeakLabelProvider(4));
 		ret.add(var);
 
 		var   = new TableViewerColumn(viewer, SWT.CENTER, 5);
 		var.getColumn().setText("FWHM");
 		var.getColumn().setWidth(100);
 		var.setLabelProvider(new PeakLabelProvider(5));
 		ret.add(var);
 		
         var   = new TableViewerColumn(viewer, SWT.CENTER, 6);
 		var.getColumn().setText("Area");
 		var.getColumn().setWidth(100);
 		var.setLabelProvider(new PeakLabelProvider(6));
 		ret.add(var);
 
         var   = new TableViewerColumn(viewer, SWT.CENTER, 7);
 		var.getColumn().setText("Type");
 		var.getColumn().setWidth(100);
 		var.setLabelProvider(new PeakLabelProvider(7));
 		ret.add(var);
 		
         var   = new TableViewerColumn(viewer, SWT.CENTER, 8);
 		var.getColumn().setText("Algorithm");
 		var.getColumn().setWidth(100);
 		var.setLabelProvider(new PeakLabelProvider(8));
 		ret.add(var);
 		
 		return ret;
 
 	}
 	
 	/**
 	 * 
 	 * @param fittedPeaksInfo
 	 * @return
 	 * @throws Exception 
 	 */
 	protected FittedFunctions getFittedFunctions(FittedPeaksInfo fittedPeaksInfo) throws Exception {
 	    return FittingUtils.getFittedPeaks(fittedPeaksInfo);
 	}
 	
 	/**
 	 * Thread safe
 	 * @param peaks
 	 */
 	@Override
 	protected synchronized void createFittedFunctionUI(final FittedFunctions newBean) {
 		
 		if (newBean==null) {
 			fittedFunctions = null;
 			logger.error("Cannot find peaks in the given selection.");
 			return;
 		}
 		composite.getDisplay().syncExec(new Runnable() {
 			
 		    public void run() {
 		    	try {
 		    		
 		    		
 		    		boolean requireFWHMSelections = Activator.getDefault().getPreferenceStore().getBoolean(FittingConstants.SHOW_FWHM_SELECTIONS);
 		    		boolean requirePeakSelections = Activator.getDefault().getPreferenceStore().getBoolean(FittingConstants.SHOW_PEAK_SELECTIONS);
 		    		boolean requireTrace = Activator.getDefault().getPreferenceStore().getBoolean(FittingConstants.SHOW_FITTING_TRACE);
 		    		boolean requireAnnot = Activator.getDefault().getPreferenceStore().getBoolean(FittingConstants.SHOW_ANNOTATION_AT_PEAK);
 
 		    		int ipeak = 1;
 					// Draw the regions
 					for (FittedFunction fp : newBean.getFunctionList()) {
 						
 						if (fp.isSaved()) continue;
 						
 						RectangularROI rb = fp.getRoi();
 						final IRegion area = RegionUtils.replaceCreateRegion(getPlottingSystem(), "Peak Area "+ipeak, RegionType.XAXIS);
 						area.setRegionColor(ColorConstants.orange);
 						area.setROI(rb);
 						area.setMobile(false);
 						getPlottingSystem().addRegion(area);
 						fp.setFwhm(area);
 						if (!requireFWHMSelections) area.setVisible(false);
 												
 						final AbstractDataset[] pair = fp.getPeakFunctions();
 						final ILineTrace trace = TraceUtils.replaceCreateLineTrace(getPlottingSystem(), "Peak "+ipeak);
 						//set user trace false before setting data otherwise the trace sent to events will be a true by default
 						trace.setUserTrace(false);
 						trace.setData(pair[0], pair[1]);
 						trace.setLineWidth(1);
 						trace.setTraceColor(ColorConstants.black);
 						getPlottingSystem().addTrace(trace);
 						fp.setTrace(trace);
 						if (!requireTrace) trace.setVisible(false);
 
 	                   	final IAnnotation ann = AnnotationUtils.replaceCreateAnnotation(getPlottingSystem(), "Peak "+ipeak);
                     	ann.setLocation(fp.getPosition(), fp.getPeakValue());                  	
                     	getPlottingSystem().addAnnotation(ann);                   	
                     	fp.setAnnotation(ann);
                     	if (!requireAnnot) ann.setVisible(false);
                     	
 						final IRegion line = RegionUtils.replaceCreateRegion(getPlottingSystem(), "Peak Line "+ipeak, RegionType.XAXIS_LINE);
 						line.setRegionColor(ColorConstants.black);
 						line.setAlpha(150);
 						line.setLineWidth(1);
 						getPlottingSystem().addRegion(line);
 						line.setROI(new LinearROI(rb.getMidPoint(), rb.getMidPoint()));
 						line.setMobile(false);
 						fp.setCenter(line);
 						if (!requirePeakSelections) line.setVisible(false);
 
 
 					    ++ipeak;
 					}
 				
 					PeakFittingTool.this.fittedFunctions = newBean;
 					viewer.setInput(newBean);
                     viewer.refresh();
                     
                     algorithmMessage.setText(getAlgorithmSummary());
                     algorithmMessage.getParent().layout();
                     //updatePlotServerConnection(newBean);
                     
 		    	} catch (Exception ne) {
 		    		logger.error("Cannot create fitted peaks!", ne);
 		    	}
 		    } 
 		});
 	}
 
 	protected String getAlgorithmSummary() {
 		StringBuilder buf = new StringBuilder("Fit attempted: '");
 		buf.append(FittingUtils.getPeaksRequired());
 		buf.append("' ");
 		buf.append(FittingUtils.getPeakType().getClass().getSimpleName());
 		buf.append("'s using ");
 		buf.append(FittingUtils.getOptimizer().getClass().getSimpleName());
 		buf.append(" with smoothing of '");
 		buf.append(FittingUtils.getSmoothing());
 		buf.append("' (<a>configure smoothing</a>)");
 		return buf.toString();
 	}
 	
 	public DataReductionInfo export(DataReductionSlice slice) throws Exception {
 				
 		final RectangularROI roi = getFitBounds();
 		if (roi==null) return new DataReductionInfo(Status.CANCEL_STATUS, null);
 
 		final double[] p1 = roi.getPointRef();
 		final double[] p2 = roi.getEndPoint();
 
 		AbstractDataset x  = slice.getAxes()!=null && !slice.getAxes().isEmpty()
 				           ? slice.getAxes().get(0)
 				           : IntegerDataset.arange(slice.getData().getSize(), AbstractDataset.INT32);
 
 		AbstractDataset[] a= FittingUtils.xintersection(x,slice.getData(),p1[0],p2[0]);
 		x = a[0]; AbstractDataset y=a[1];
 		
 		// If the IdentifiedPeaks are null, we make them.
 		List<IdentifiedPeak> identifiedPeaks = (List<IdentifiedPeak>)slice.getUserData();
 		if (slice.getUserData()==null) {
 			identifiedPeaks = Generic1DFitter.parseDataDerivative(x, y, FittingUtils.getSmoothing());
 		}
 		DataReductionInfo status = new DataReductionInfo(Status.OK_STATUS, identifiedPeaks);
 
 		try {
 			final FittedPeaksInfo info = new FittedPeaksInfo(x, y, slice.getMonitor());
 			info.setIdentifiedPeaks(identifiedPeaks);
 			
 			FittedFunctions bean = FittingUtils.getFittedPeaks(info);
 			
 			int index = 1;
 			for (FittedFunction fp : bean.getFunctionList()) {
 
 				H5Datatype dType = new H5Datatype(Datatype.CLASS_FLOAT, 64/8, Datatype.NATIVE, Datatype.NATIVE);
 
 				IHierarchicalDataFile file = slice.getFile();
 				final String peakName = "Peak"+index;
 				Dataset s = file.appendDataset(peakName+"_fit",  dType,  new long[]{1},new double[]{fp.getPeakValue()}, slice.getParent());
 				file.setNexusAttribute(s, Nexus.SDS);
 				file.setIntAttribute(s, NexusUtils.SIGNAL, 1);
 				
 				s = file.appendDataset(peakName+"_xposition",  dType,  new long[]{1}, new double[]{fp.getPosition()}, slice.getParent());
 				file.setNexusAttribute(s, Nexus.SDS);
 				file.setIntAttribute(s, NexusUtils.SIGNAL, 1);
 				
 				s = file.appendDataset(peakName+"_fwhm",  dType,  new long[]{1}, new double[]{fp.getFWHM()}, slice.getParent());
 				file.setNexusAttribute(s, Nexus.SDS);
 				file.setIntAttribute(s, NexusUtils.SIGNAL, 1);
 				
 				s = file.appendDataset(peakName+"_area",  dType,  new long[]{1}, new double[]{fp.getArea()}, slice.getParent());
 				file.setNexusAttribute(s, Nexus.SDS);
 				file.setIntAttribute(s, NexusUtils.SIGNAL, 1);
 
 				final AbstractDataset[] pair = fp.getPeakFunctions();
 				AbstractDataset     function = pair[1];
 				s = file.appendDataset(peakName+"_function",  dType,  H5Utils.getLong(function.getShape()), function.getBuffer(), slice.getParent());
 				file.setNexusAttribute(s, Nexus.SDS);
 				file.setIntAttribute(s, NexusUtils.SIGNAL, 1);
 
 
 				++index;
 			}
 
 		} catch (Exception ne) {
 			logger.error("Cannot fit peaks!", ne);
 			return new DataReductionInfo(Status.CANCEL_STATUS, null);
 		}
 		
 		return status;
 	}
 
 	/**
 	 * We use the old actions here for simplicity of configuration.
 	 * 
 	 * TODO consider moving to commands.
 	 */
 	protected void createActions() {
 		
 		final Action createNewSelection = new Action("New fit selection.", IAction.AS_PUSH_BUTTON) {
 			public void run() {
 				createNewFit();
 			}
 		};
 		createNewSelection.setImageDescriptor(Activator.getImageDescriptor("icons/plot-tool-peak-fit.png"));
 		getSite().getActionBars().getToolBarManager().add(createNewSelection);
 		getSite().getActionBars().getToolBarManager().add(new Separator());
 		
 		final Action showAnns = new Action("Show annotations at the peak position.", IAction.AS_CHECK_BOX) {
 			public void run() {
 				final boolean isChecked = isChecked();
 				Activator.getDefault().getPreferenceStore().setValue(FittingConstants.SHOW_ANNOTATION_AT_PEAK, isChecked);
 				if (fittedFunctions!=null) fittedFunctions.setAnnotationsVisible(isChecked);
 			}
 		};
 		showAnns.setImageDescriptor(Activator.getImageDescriptor("icons/plot-tool-peak-fit-showAnnotation.png"));
 		getSite().getActionBars().getToolBarManager().add(showAnns);
 		
 		showAnns.setChecked(Activator.getDefault().getPreferenceStore().getBoolean(FittingConstants.SHOW_ANNOTATION_AT_PEAK));
 
 		final Action showTrace = new Action("Show fitting traces.", IAction.AS_CHECK_BOX) {
 			public void run() {
 				final boolean isChecked = isChecked();
 				Activator.getDefault().getPreferenceStore().setValue(FittingConstants.SHOW_FITTING_TRACE, isChecked);
 				if (fittedFunctions!=null) fittedFunctions.setTracesVisible(isChecked);
 			}
 		};
 		showTrace.setImageDescriptor(Activator.getImageDescriptor("icons/plot-tool-peak-fit-showFittingTrace.png"));
 		getSite().getActionBars().getToolBarManager().add(showTrace);
 
 		showTrace.setChecked(Activator.getDefault().getPreferenceStore().getBoolean(FittingConstants.SHOW_FITTING_TRACE));
 
 		
 		final Action showPeak = new Action("Show peak lines.", IAction.AS_CHECK_BOX) {
 			public void run() {
 				final boolean isChecked = isChecked();
 				Activator.getDefault().getPreferenceStore().setValue(FittingConstants.SHOW_PEAK_SELECTIONS, isChecked);
 				if (fittedFunctions!=null) fittedFunctions.setPeaksVisible(isChecked);
 			}
 		};
 		showPeak.setImageDescriptor(Activator.getImageDescriptor("icons/plot-tool-peak-fit-showPeakLine.png"));
 		getSite().getActionBars().getToolBarManager().add(showPeak);
 		
 		showPeak.setChecked(Activator.getDefault().getPreferenceStore().getBoolean(FittingConstants.SHOW_PEAK_SELECTIONS));
 
 		final Action showFWHM = new Action("Show selection regions for full width, half max.", IAction.AS_CHECK_BOX) {
 			public void run() {
 				final boolean isChecked = isChecked();
 				Activator.getDefault().getPreferenceStore().setValue(FittingConstants.SHOW_FWHM_SELECTIONS, isChecked);
 				if (fittedFunctions!=null) fittedFunctions.setAreasVisible(isChecked);
 			}
 		};
 		showFWHM.setImageDescriptor(Activator.getImageDescriptor("icons/plot-tool-peak-fit-showFWHM.png"));
 		getSite().getActionBars().getToolBarManager().add(showFWHM);
 		
 		showFWHM.setChecked(Activator.getDefault().getPreferenceStore().getBoolean(FittingConstants.SHOW_FWHM_SELECTIONS));
 		
 		final Separator sep = new Separator(getClass().getName()+".separator1");	
 		getSite().getActionBars().getToolBarManager().add(sep);
 		
 		final Action savePeak = new Action("Save peak.", IAction.AS_PUSH_BUTTON) {
 			public void run() {
 				try {
 					fittedFunctions.saveSelectedPeak(getPlottingSystem());
 				} catch (Exception e) {
 					logger.error("Cannot rename saved peak ", e);
 				}
 				viewer.refresh();
 			}
 		};
 		savePeak.setImageDescriptor(Activator.getImageDescriptor("icons/plot-tool-peak-fit-savePeak.png"));
 		getSite().getActionBars().getToolBarManager().add(savePeak);
 		
 		final Separator sep3 = new Separator(getClass().getName()+".separator3");	
 		getSite().getActionBars().getToolBarManager().add(sep3);
 
 		final MenuAction  peakType = new MenuAction("Peak type to fit");
 		peakType.setToolTipText("Peak type to fit");
 		peakType.setImageDescriptor(Activator.getImageDescriptor("icons/plot-tool-peak-fit-peak-type.png"));
 		
 		CheckableActionGroup group = new CheckableActionGroup();
 		
 		Action selectedPeakAction = null;
 		for (final IPeak peak : FittingUtils.getPeakOptions().values()) {
 			
 			final Action action = new Action(peak.getClass().getSimpleName(), IAction.AS_CHECK_BOX) {
 				public void run() {
 					Activator.getDefault().getPreferenceStore().setValue(FittingConstants.PEAK_TYPE, peak.getClass().getName());
 					setChecked(true);
 					if (fittingJob!=null&&isActive()) fittingJob.fit();
 					peakType.setSelectedAction(this);
 				}
 			};
 			peakType.add(action);
 			group.add(action);
 			if (peak.getClass().getName().equals(Activator.getDefault().getPreferenceStore().getString(FittingConstants.PEAK_TYPE))) {
 				selectedPeakAction = action;
 			}
 		}
 		
 		if (selectedPeakAction!=null) {
 			peakType.setSelectedAction(selectedPeakAction);
 			selectedPeakAction.setChecked(true);
 		}
 		getSite().getActionBars().getToolBarManager().add(peakType);
 		getSite().getActionBars().getMenuManager().add(peakType);
 
 		
 		final Separator sep2 = new Separator(getClass().getName()+".separator2");	
 		getSite().getActionBars().getToolBarManager().add(sep2);
 
 		this.tracesMenu = new MenuAction("Traces");
 		tracesMenu.setToolTipText("Choose trace for fit.");
 		tracesMenu.setImageDescriptor(Activator.getImageDescriptor("icons/plot-tool-trace-choice.png"));
 		
 		getSite().getActionBars().getToolBarManager().add(tracesMenu);
 		getSite().getActionBars().getMenuManager().add(tracesMenu);
 				
 		this.numberPeaks = new MenuAction("Number peaks to fit");
 		numberPeaks.setToolTipText("Number peaks to fit");
 				
 		group = new CheckableActionGroup();
 		
 		final int npeak = Activator.getDefault().getPreferenceStore().getDefaultInt(FittingConstants.PEAK_NUMBER_CHOICES);
 		for (int ipeak = 1; ipeak <= npeak; ipeak++) {
 			
 			final int peak = ipeak;
 			final Action action = new Action("Fit "+String.valueOf(ipeak)+" Peaks", IAction.AS_CHECK_BOX) {
 				public void run() {
 					Activator.getDefault().getPreferenceStore().setValue(FittingConstants.PEAK_NUMBER, peak);
 				}
 			};
 			
 			action.setImageDescriptor(IconUtils.createIconDescriptor(String.valueOf(ipeak)));
 			numberPeaks.add(action);
 			group.add(action);
 			action.setChecked(false);
 			action.setToolTipText("Fit "+ipeak+" peak(s)");
 			
 		}
 		final Action preferences = new Action("Preferences...") {
 			public void run() {
 				if (!isActive()) return;
 				PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), FittingPreferencePage.ID, null, null);
 				if (pref != null) pref.open();
 			}
 		};
 		
 		final Action npeaks = new Action("Fit n Peaks", IAction.AS_CHECK_BOX) {
 			public void run() {
 				preferences.run();
 			}
 		};
 		
 		npeaks.setImageDescriptor(IconUtils.createIconDescriptor("n"));
 		numberPeaks.add(npeaks);
 		group.add(npeaks);
 		npeaks.setChecked(false);
 		npeaks.setToolTipText("Fit n peaks");
 
 
 		final int ipeak = Activator.getDefault().getPreferenceStore().getInt(FittingConstants.PEAK_NUMBER);
 		if (ipeak<11) {
 			numberPeaks.setSelectedAction(ipeak-1);
 			numberPeaks.setCheckedAction(ipeak-1, true);
 		} else {
			numberPeaks.setSelectedAction(11);
			numberPeaks.setCheckedAction(11, true);
 		}
 			
 		getSite().getActionBars().getToolBarManager().add(numberPeaks);
 		//getSite().getActionBars().getMenuManager().add(numberPeaks);
 		
 		
 		final Action clear = new Action("Clear all", Activator.getImageDescriptor("icons/plot-tool-peak-fit-clear.png")) {
 			public void run() {
 				clearAll();
 			}
 		};
 		clear.setToolTipText("Clear all regions found in the fitting");
 		
 		getSite().getActionBars().getToolBarManager().add(clear);
 		getSite().getActionBars().getMenuManager().add(clear);
 		
 		final Action delete = new Action("Delete peak selected", Activator.getImageDescriptor("icons/delete.gif")) {
 			public void run() {
 				if (!isActive()) return;
 				if (fittedFunctions!=null) fittedFunctions.deleteSelectedFunction(getPlottingSystem());
 				viewer.refresh();
 			}
 		};
 		delete.setToolTipText("Delete peak selected, if any");
 		
 		getSite().getActionBars().getToolBarManager().add(delete);
 
 
 		getSite().getActionBars().getMenuManager().add(preferences);
 		
 		final Action export = new Action("Export...", IAction.AS_PUSH_BUTTON) {
 			public void run() {
 				try {
 					EclipseUtils.openWizard(FittedPeaksExportWizard.ID, true);
 				} catch (Exception e) {
 					logger.error("Cannot open wizard "+FittedPeaksExportWizard.ID, e);
 				}
 			}
 		};
 
 
 	    final MenuManager menuManager = new MenuManager();
 	    menuManager.add(clear);
 	    menuManager.add(delete);
 	    menuManager.add(savePeak);
 	    menuManager.add(new Separator());
 	    menuManager.add(showAnns);
 	    menuManager.add(showTrace);
 	    menuManager.add(showPeak);
 	    menuManager.add(showFWHM);
 	    menuManager.add(new Separator());
 	    menuManager.add(export);
 		
 	    viewer.getControl().setMenu(menuManager.createContextMenu(viewer.getControl()));
 
 	}
 
 	
 	@Override
 	public Object getAdapter(@SuppressWarnings("rawtypes") Class key) {
 		if (key == IPeak.class) {
 			return fittedFunctions!=null && !fittedFunctions.isEmpty() ? fittedFunctions.getPeakFunctions() : null;
 		}
 		return super.getAdapter(key);
 	}
 	
 	public String exportFittedData(final String path) throws Exception {
 		return exportFittedPeaks(path);
 	}
 
 	/**
 	 * Will export to file and overwrite.
 	 * Will append ".csv" if it is not already there.
 	 * 
 	 * @param path
 	 */
 	String exportFittedPeaks(final String path) throws Exception {
 		
 		File file = new File(path);
 		if (!file.getName().toLowerCase().endsWith(".dat")) file = new File(path+".dat");
 		if (file.exists()) file.delete();
 		
 		final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
 		try {
 			writer.write(FittedFunction.getCVSTitle());
 			writer.newLine();
 			for (FittedFunction peak : getSortedFunctionList()) {
 				writer.write(peak.getTabString());
 				writer.newLine();
 			}
 			
 		} finally {
 			writer.close();
 		}
 		
 		return file.getAbsolutePath();
     }
 
 }
