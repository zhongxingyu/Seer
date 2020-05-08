 package org.dawb.workbench.plotting.tools;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.dawb.common.ui.plot.IPlottingSystem;
 import org.dawb.common.ui.plot.tool.AbstractToolPage;
 import org.dawb.common.ui.plot.tool.IToolPage;
 import org.dawb.common.ui.plot.trace.ILineTrace;
 import org.dawb.common.ui.plot.trace.ITrace;
 import org.dawb.common.ui.plot.trace.ITraceListener;
 import org.dawb.common.ui.plot.trace.TraceEvent;
 import org.dawb.workbench.plotting.tools.history.AbstractHistoryTool;
 import org.dawb.workbench.plotting.util.ColorUtility;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.Maths;
 
 public class DerivativeTool extends AbstractToolPage  {
 	
 	//Class for conveniently passing around a x,y pair of datasets
 	private class AbstractDatasetPair {
 		public AbstractDataset x;
 		public AbstractDataset y;
 		
 		public AbstractDatasetPair(AbstractDataset xIn, AbstractDataset yIn) {
 			x = xIn;
 			y= yIn;
 		}
 	}
 	
 	//Derivative type
 	private enum Derivative {
 		FIRST,SECOND
 	}
 	
 	// Statics
 	private static final int SMOOTHING = 1;
 	
 	//Trace/Dataset pair lists
 	private List<ITrace> eventTraceList= new ArrayList<ITrace>();
 	private List<ITrace> dataTraces = new ArrayList<ITrace>();
 	private ArrayList<AbstractDatasetPair> dervsPair  = new ArrayList<AbstractDatasetPair>();
 	private ArrayList<AbstractDatasetPair> dervs2Pair  = new ArrayList<AbstractDatasetPair>();
 	boolean data = false;
 	boolean deriv = true;
 	boolean deriv2 = false;
 
 	// Logger
 	private final static Logger logger = LoggerFactory.getLogger(DerivativeTool.class);
 
 	// GUI Elements
 	private Composite composite;
 	protected Button dataCheck;
 	protected Button derivCheck;
 	protected Button deriv2Check;
 	private Label infoLabel;
 	private boolean duringSync = false;
 	private boolean duringDispose = false;
 
 	// Listeners
 	private ITraceListener traceListener;
 	private SelectionListener updateChecksSelection;
 
 	// Jobs
 	private Job updatePlotData;
 
 	// Internal Items
 	private boolean isUpdateRunning = false;
 
 	public DerivativeTool() {
 		try {
 			// Set up the listener for new traces
 			this.traceListener = new ITraceListener.Stub() {
 				// Response to traces plotted event, a bit complicated, has to deal with a
 				// few cases
 				@Override
 				public void tracesPlotted(TraceEvent evt) {
 					//First, if the event source is not a list or ITrace ignore event
 					if (!(evt.getSource() instanceof List<?>) && !(evt.getSource() instanceof ITrace)) {
 						return;
 					}
 					
 					//If we are already running, ignore event
 					if (!isUpdateRunning) {
 						
 						//Make a new list for the ITraces in,
 						// deal with lists and single traces
 						List<ITrace> eventSource = new ArrayList<ITrace>();
 						if (evt.getSource() instanceof List<?>)
 							eventSource = (List<ITrace>)evt.getSource();
 						if (evt.getSource() instanceof ITrace) {
 							eventSource.clear();
 							eventSource.add((ITrace)evt.getSource());
 						}
 						
 						if (getPlottingSystem() == null) return;
 						
 						//Cherry pick non history traces and user editable
 						// and remove from the plot
 						// Done here to minimise the "Jump" between the just plotted data
 						// and the derivatives. Would be nicer to have an event before the data
 						// is plotted
 						List<ITrace> processableTraces = new ArrayList<ITrace>();
 						for (ITrace trace: eventSource) {
 							if (trace.isUserTrace() && 
 									trace.getUserObject() != AbstractHistoryTool.HistoryType.HISTORY_PLOT &&
 									trace.getUserObject() != DerivativeTool.class) {
 								
 								processableTraces.add(trace);
 								getPlottingSystem().removeTrace(trace);
 								
 							}
 						}
 							
 						//If there is none, return
 						if (processableTraces.isEmpty()) return;
 						
 						//We can now overwrite the eventTraceList with our new one
 						eventTraceList = processableTraces;
 						
 						updatePlot();
 					}
 				}
 				
 				@Override
 				public void tracesCleared(TraceEvent evt) {
 					eventTraceList.clear();
 				}
 				
 				
 			};
 
 		} catch (Exception e) {
 			logger.error("Cannot get plotting system!", e);
 		}
 		
 		// Set up the listener for the gui elements	
 		updateChecksSelection = new SelectionListener() {
 			
 			@Override
 			public void widgetSelected(SelectionEvent event) {
 				if (!isUpdateRunning) {
 					logger.debug("Update plot called from widget");
 					updatePlot(dataCheck.getSelection(),derivCheck.getSelection(),deriv2Check.getSelection());
 				}
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent event) {
 				widgetSelected(event);
 			}
 		};
 	}
 
 	
 	@Override
 	public void createControl(Composite parent) {
 
 		composite = new Composite(parent, SWT.RESIZE);
 		composite.setLayout(new GridLayout(1, false));	
 
 		infoLabel = new Label(composite, SWT.NONE);
 		infoLabel.setText("Open in a dedicated view to use with other tools.");
 
 		dataCheck = new Button(composite, SWT.CHECK);
 		dataCheck.setText("Display Data");
 		dataCheck.setSelection(data);
 		dataCheck.addSelectionListener(updateChecksSelection);
 		derivCheck = new Button(composite, SWT.CHECK);
 		derivCheck.setSelection(deriv);
 		derivCheck.setText("Display f'(Data)");
 		derivCheck.addSelectionListener(updateChecksSelection);
 		deriv2Check = new Button(composite, SWT.CHECK);
 		deriv2Check.setSelection(deriv2);
 		deriv2Check.setText("Display f''(Data)");
 		deriv2Check.addSelectionListener(updateChecksSelection);
 		
 	}
 
 
 	@Override
 	public ToolPageRole getToolPageRole() {
 		return ToolPageRole.ROLE_1D;
 	}
 
 	@Override
 	public void setFocus() {
 		composite.setFocus();
 	}
 
 	public void activate() {
 		super.activate();
 		
 		// Return if toolpage not set up
 		if (getViewPart() == null) return;
 		if (getPlottingSystem() == null) return;
 		if (isDisposed()) return;
 		getPlottingSystem().addTraceListener(traceListener);
 
 		//This is probably going to lead to the first call to Update Plot
 		//We need to get the user traces from the plot and store them for processing,
 		// and remove them from the plot
 		if (eventTraceList.isEmpty()) {
 			for (ITrace trace : getPlottingSystem().getTraces(ILineTrace.class)) {
 				if (trace.isUserTrace() && trace.getUserObject() != AbstractHistoryTool.HistoryType.HISTORY_PLOT
 						&& trace.getUserObject() != DerivativeTool.class) {
 					eventTraceList.add(trace);
 					getPlottingSystem().removeTrace(trace);
 				}
 			}
 		}
 		
 		if (eventTraceList.isEmpty()) return;
 
 		if (!isUpdateRunning) {
 			Display.getDefault().syncExec(new Runnable() {
 				public void run() {
 					logger.debug("Update plot called from activate");
 					//Run the plot update on the GUI thread so we can check what needs to be plotted
 					updatePlot(dataCheck.getSelection(),derivCheck.getSelection(),deriv2Check.getSelection());					
 				}
 			});
 			
 		}
 
 	}
 
 
 	@Override
 	public void deactivate() {
 		//Check not already deactivated to prevent double calls
 		if (!isActive()) return;
		if (isDisposed()) return;
 		super.deactivate();
 		//If this is being synced to a dedicated view we dont
 		//want to Update the plot
 		if (duringSync) {
 			duringSync = false;
 			return;
 		}
 		
 		if (!isUpdateRunning && !duringDispose) {
 			logger.debug("Update plot called from deactivate");
 			updatePlot(true,false,false);
 		}
 
 	}
 
 	public void sync(IToolPage with) {
 		if (!with.getClass().equals(getClass())) return;
 		//Update dedicated window tool from old tool
 		final DerivativeTool other = (DerivativeTool)with;
 		//To prevent the old tool updating on deactivate
 		other.duringSync = true;
 		this.eventTraceList = other.eventTraceList;
 		this.dataCheck.setSelection(other.dataCheck.getSelection());
 		this.derivCheck.setSelection(other.derivCheck.getSelection());
 		this.deriv2Check.setSelection(other.deriv2Check.getSelection());
 		logger.debug("Update plot called from sync");
 		updatePlot(dataCheck.getSelection(),derivCheck.getSelection(),deriv2Check.getSelection());
 		
 	}
 
 	@Override
 	public Control getControl() {
 		if (composite==null) return null;
 		return composite;
 	}
 
 	@Override
 	public void dispose() {
 		duringDispose = true;
 		deactivate();
 		
 		//Important to call updateDerivates here over updatePlot
 		// If we don't block this thread the plottingSystem become null
 		// before we have finished.
 		updateDerivatives(true,false,false);
 		
 		// clear all lists
 		if (dataTraces!=null) dataTraces.clear();
 		if (eventTraceList!=null) eventTraceList.clear();
 		if (dervsPair!=null) dervsPair.clear();
 		if (dervs2Pair!=null) dervsPair.clear();
 
 		super.dispose();
 	}
 	
 	private synchronized void updatePlot(boolean dataPlot, boolean derivPlot, boolean deriv2Plot) {
 		data = dataPlot;
 		deriv = derivPlot;
 		deriv2 = deriv2Plot;
 		updatePlot();
 	}
 
 	private synchronized void updatePlot() {
 
 		if (updatePlotData==null) {
 			updatePlotData = new Job("Derviative update") {
 
 				@Override
 				protected IStatus run(IProgressMonitor monitor) {
 					try {
 						isUpdateRunning = true;
 						logger.debug("Update Running");
 						
 						//Calculate all derivatives whether required or not
 						// Ignore any non-user traces in the event
 						dataTraces.clear();
 						dervsPair.clear();
 						dervs2Pair.clear();
 						if (eventTraceList.isEmpty()) return Status.OK_STATUS;
 						for (ITrace trace : eventTraceList) {
 								if (!trace.isUserTrace() || trace.getUserObject() == AbstractHistoryTool.HistoryType.HISTORY_PLOT)
 									continue;
 								dataTraces.add(trace);
 								dervsPair.add(processTrace(trace, Derivative.FIRST));
 								dervs2Pair.add(processTrace(trace, Derivative.SECOND));
 						}
 
 						Display.getDefault().syncExec(new Runnable() {
 							public void run() {
 								//Run the plot update on the GUI thread
 								updateDerivatives();						
 							}
 						});
 						if (!isActive()) return Status.CANCEL_STATUS;
 						return Status.OK_STATUS;
 						
 					}finally {
 						logger.debug("Update Finished In Finally");
 						isUpdateRunning = false;
 					}
 				}
 				
 			};
 			
 			updatePlotData.setSystem(true);
 			updatePlotData.setUser(false);
 			updatePlotData.setPriority(Job.INTERACTIVE);
 		}
 		
 		updatePlotData.schedule();
 
 	}
 	
 	private synchronized void updateDerivatives(boolean dataPlot, boolean derivPlot, boolean deriv2Plot) {
 		data = dataPlot;
 		deriv = derivPlot;
 		deriv2 = deriv2Plot;
 		updateDerivatives();
 	}
 	
 	private synchronized void updateDerivatives() {
 		logger.debug("Updating Derivatives");
 		
 		if (getPlottingSystem() == null) return;
 		
 		for (ITrace trace : getPlottingSystem().getTraces(ILineTrace.class)) {
 			if (trace.isUserTrace() && trace.getUserObject() != AbstractHistoryTool.HistoryType.HISTORY_PLOT) {
 				getPlottingSystem().removeTrace(trace);
 			}
 		}
 		
 		//plot all required data, original data from traces
 		// derivative data from AbstractDataset pairs
 		if (data) {
 			for (ITrace trace : dataTraces) {
 				getPlottingSystem().addTrace(trace);
 			}
 		}
 		if (deriv) {
 			int ic = dataTraces.size();
 			for (AbstractDatasetPair dataset : dervsPair) {
 				ILineTrace traceNew = getPlottingSystem().createLineTrace(dataset.y.getName());
 				traceNew.setUserObject(DerivativeTool.class);
 				traceNew.setUserTrace(true);
 				traceNew.setData(dataset.x, dataset.y);
 				traceNew.setTraceColor(ColorUtility.getSwtColour(ic));
 				ic++;
 				getPlottingSystem().addTrace(traceNew);
 			}
 		}
 		if (deriv2) {
 			int ic = dataTraces.size()*2;
 			for (AbstractDatasetPair dataset : dervs2Pair) {
 				ILineTrace traceNew = getPlottingSystem().createLineTrace(dataset.y.getName());
 				traceNew.setUserObject(DerivativeTool.class);
 				traceNew.setUserTrace(true);
 				traceNew.setTraceColor(ColorUtility.getSwtColour(ic));
 				ic++;
 				traceNew.setData(dataset.x, dataset.y);
 				
 				getPlottingSystem().addTrace(traceNew);
 			}
 		}
 		
 
 		//Call repaint so the plotting system obeys button for whether rescale
 		//should happen or not
 		getPlottingSystem().repaint();
 		if (isActive())
 			getPlottingSystem().addTraceListener(traceListener);
 		
 		logger.debug("Update Finished In updateDerivatives");
 		return;
 	}
 	
 	private AbstractDatasetPair processTrace(ITrace trace, Derivative type){
 		
 		// Calculate the derivative from the data in trace,
 		// return as an abstract dataset since we dont want to interact with the plot here
 		// to generate the traces
 		final AbstractDataset traceData = trace.getData();
 		
 		//Get x data if present or if not generate index range
 		final AbstractDataset x = (trace instanceof ILineTrace) 
 				? ((ILineTrace)trace).getXData() 
 						: AbstractDataset.arange(0, traceData.getSize(), 1, AbstractDataset.INT32);
 
 		AbstractDataset derv = null;
 		
 		if (type == Derivative.FIRST)
 			derv = Maths.derivative(x, traceData, SMOOTHING);
 		else if (type==Derivative.SECOND)
 			derv = Maths.derivative(x,Maths.derivative(x, traceData, SMOOTHING), SMOOTHING);
 		
 		return new AbstractDatasetPair(x,derv);
 	}
 }
 	
 	
 
 
