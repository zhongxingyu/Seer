 /*
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 
 package org.dawb.workbench.ui.editors;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
 import org.dawb.common.ui.plot.AbstractPlottingSystem;
 import org.dawb.common.ui.plot.IPlottingSystem;
 import org.dawb.common.ui.plot.PlotType;
 import org.dawb.common.ui.plot.PlottingFactory;
 import org.dawb.common.ui.plot.axis.IAxis;
 import org.dawb.common.ui.plot.tool.IToolPageSystem;
 import org.dawb.common.ui.slicing.SliceComponent;
 import org.dawb.common.ui.util.EclipseUtils;
 import org.dawb.common.ui.util.GridUtils;
 import org.dawb.common.ui.views.PlotDataView;
 import org.dawb.common.ui.widgets.ActionBarWrapper;
 import org.dawb.workbench.ui.Activator;
 import org.dawb.workbench.ui.editors.slicing.ExpressionObject;
 import org.dawb.workbench.ui.views.PlotDataPage;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IActionBars2;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.IReusableEditor;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.EditorPart;
 import org.eclipse.ui.part.IPage;
 import org.eclipse.ui.part.Page;
 import org.eclipse.ui.progress.IProgressService;
 import org.eclipse.ui.progress.UIJob;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.io.DataHolder;
 import uk.ac.diamond.scisoft.analysis.io.IMetaData;
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 import uk.ac.gda.monitor.IMonitor;
 
 
 /**
  * An editor which combines a plot with a graph of data sets.
  * 
  * 
  */
 public class PlotDataEditor extends EditorPart implements IReusableEditor, IDatasetEditor {
 	
 	private static Logger logger = LoggerFactory.getLogger(PlotDataEditor.class);
 	
 	// This view is a composite of two other views.
 	private AbstractPlottingSystem      plottingSystem;	
 	private Collection<String>          dataNames;
 	private Map<String,ILazyDataset>    dataCache; // TODO FIXME Do not need dataCache anymore, LoaderFactory does it!
 	private Composite                   tools;
 	private IMetaData                   metaData;
 	private PlotType                    defaultPlotType;
 	private Map<Integer, IAxis>         axisMap;
 
 	public PlotDataEditor(boolean useCaching, final PlotType defaultPlotType) {
 		
 	    this.axisMap = new HashMap<Integer, IAxis>(4);
 		try {
 			this.defaultPlotType= defaultPlotType;
 	        this.plottingSystem = PlottingFactory.createPlottingSystem();
 		} catch (Exception ne) {
 			logger.error("Cannot locate any plotting systems!", ne);
 		}
         if (useCaching) this.dataCache = new HashMap<String,ILazyDataset>(7);
  	}
 
 	@Override
 	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
 		setSite(site);
 		setInput(input);		
 	}
 
 	@Override
 	public Map<String, IDataset> getSelected() {
 		
 		final PlotDataComponent dataSetComponent = getDataSetComponent();
         final Map<String,IDataset> ret = new HashMap<String, IDataset>(3);
 		if (dataSetComponent==null) return ret;
 		
         final List<CheckableObject> selectedNames = dataSetComponent.getSelections();
         for (Object object : selectedNames) {
         	final AbstractDataset set = getDataSet(object, null);
         	if (set==null) continue;
          	ret.put(set.getName(), set);
         }
 		return ret;
 	}
 
 	@Override
 	public boolean isDirty() {
 		return false;
 	}
 	
 
 	public void setToolbarsVisible(boolean isVisible) {
 		GridUtils.setVisible(tools, isVisible);
 		tools.getParent().layout(new Control[]{tools});
 	}
 
 	@Override
 	public void createPartControl(final Composite parent) {
 		
 		final Composite  main       = new Composite(parent, SWT.NONE);
 		final GridLayout gridLayout = new GridLayout(1, false);
 		main.setLayout(gridLayout);
 		GridUtils.removeMargins(main);
 		
 		this.tools = new Composite(main, SWT.RIGHT);
 		tools.setLayout(new GridLayout(2, false));
 		GridUtils.removeMargins(tools);
 		tools.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 		
 		// We use a local toolbar to make it clear to the user the tools
 		// that they can use, also because the toolbar actions are 
 		// hard coded.
 		ToolBarManager toolMan = new ToolBarManager(SWT.FLAT|SWT.RIGHT|SWT.WRAP);
 		final ToolBar          toolBar = toolMan.createControl(tools);
 		toolBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
 
 		ToolBarManager rightMan = new ToolBarManager(SWT.FLAT|SWT.RIGHT|SWT.WRAP);
 		final ToolBar          rightBar = rightMan.createControl(tools);
 		rightBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
 
 		final MenuManager    menuMan = new MenuManager();
 		final IActionBars bars = this.getEditorSite().getActionBars();
 		ActionBarWrapper wrapper = new ActionBarWrapper(toolMan,menuMan,null,(IActionBars2)bars);
 				
 		// NOTE use name of input. This means that although two files of the same
 		// name could be opened, the editor name is clearly visible in the GUI and
 		// is usually short.
 		final String plotName = this.getEditorInput().getName();
 		
 		final Composite plot  = new Composite(main, SWT.NONE);
 		plot.setLayout(new FillLayout());
 		plot.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
         plottingSystem.createPlotPart(plot, plotName, wrapper, defaultPlotType, this);
         
         axisMap.put(1, plottingSystem.getSelectedYAxis());
         for (int i = 2; i <=4; i++) {
         	final IAxis yAxis = plottingSystem.createAxis("Y"+i, true, i==3||i==4?SWT.RIGHT:SWT.LEFT);
         	yAxis.setVisible(false);
         	yAxis.setTitle("Y"+i);
             axisMap.put(i, yAxis);
 		}
         
  		if (rightMan!=null) {
  		    Action menuAction = new Action("", Activator.getImageDescriptor("/icons/DropDown.png")) {
  		        @Override
  		        public void run() {
  	                final Menu   mbar = menuMan.createContextMenu(toolBar);
  	       		    mbar.setVisible(true);
  		        }
  		    };
  		    rightMan.add(menuAction);
  		}
         		
 		
 		// Finally
 		if (toolMan!=null)  toolMan.update(true);
 		if (rightMan!=null) rightMan.update(true);
 	    createData(getEditorInput());	
 	    
 		// We ensure that the view for choosing data sets is visible
 		if (EclipseUtils.getActivePage()!=null) {
 			try {
 				EclipseUtils.getActivePage().showView(PlotDataView.ID, null, IWorkbenchPage.VIEW_ACTIVATE);
 			} catch (PartInitException e) {
 				logger.error("Cannot open "+PlotDataView.ID);
 			}
 		}
 		
 		getEditorSite().setSelectionProvider(plottingSystem.getSelectionProvider());
 
  	}
 	
 	/**
 	 * Call to change plot default by looking at data
 	 */
 	public void createPlotTypeDefaultFromData() {
 	    // If the data is only 2D we tell the PlottingSystem to switch to 2D mode.
 		boolean is1D = false;
 		
 		final PlotDataComponent dataSetComponent = getDataSetComponent();
  		if (dataSetComponent!=null) {
 			for (CheckableObject set : dataSetComponent.getData()) {
 				if (dataSetComponent.getActiveDimensions(set, true)==1)	{
 					is1D=true;
 					break;
 				}
 			}
  		}
 		if (!is1D) {
 			getPlottingSystem().setPlotType(PlotType.IMAGE);
 		}
 
 	}
 
 	
 	private boolean doingUpdate = false;
 	private boolean extractingMetaData;
 	
 	/**
 	 * Must be called in UI thread
 	 * @param selections
 	 * @param useTask
 	 */
 	public void updatePlot(final CheckableObject[]      selections, 
 			               final IPlotUpdateParticipant participant,
 			               final boolean                useTask) {
 		
 		if (doingUpdate) return;
 		
 		if (selections==null || selections.length<1) {
 			if (participant!=null) participant.setSlicerVisible(false);
 		}
 		
 		try {
 			doingUpdate = true;
 			if (selections==null||selections.length<1) {
 				plottingSystem.reset();
 				return;
 			}
 			
 			if (selections.length==1 && participant.getDimensionCount(selections[0])!=1) {
 				
 				participant.setSlicerVisible(true);
 				participant.setSlicerData(selections[0].toString(),
 						EclipseUtils.getFilePath(getEditorInput()), 
 						participant.getMetaData().getDataShapes().get(selections[0].toString()), 
 						plottingSystem);
 
 				return;
 			}
 			
 			participant.setSlicerVisible(false);
 			
 			if (useTask) {
 				IProgressService service =  getEditorSite()!=null 
 				                         ? (IProgressService)getSite().getService(IProgressService.class)
 						                 : (IProgressService)PlatformUI.getWorkbench().getService(IProgressService.class);
 				try {
 					service.run(true, true, new IRunnableWithProgress() {
 						@Override
 						public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 		
 							monitor.beginTask("Updating selected DataSets", 100);
 				
 							createPlot(selections, participant, monitor);
 						}
 	
 	
 					});
 				} catch (Exception e) {
 					logger.error("Cannot create plot required.", e);
 				} 
 			} else {
 				createPlot(selections, participant, new NullProgressMonitor());
 			}
 		} finally {
 			doingUpdate = false;
 		}
 	}
 	
 	private void createPlot(final CheckableObject[] selections, final IPlotUpdateParticipant participant, final IProgressMonitor monitor) {
 		
 
 		if (monitor.isCanceled()) return;
 		plottingSystem.clear();
 		if (participant.getPlotMode()==PlotType.IMAGE) {
 		    plottingSystem.createPlot2D(getDataSet(selections[0], monitor), null, monitor);
 		    
 		} else {
 			List<CheckableObject> sels = new ArrayList<CheckableObject>(Arrays.asList(selections));
 			final AbstractDataset data = getDataSet(sels.get(0), monitor);
 
 			final AbstractDataset x;
 			if (plottingSystem.isXfirst() && sels.size()>1) {
 				x  = data;
 				sels.remove(0);
 			} else {
 				x = null;
 			}
 			
 			if (sels.isEmpty() || (!plottingSystem.isXfirst() && sels.size()==1)) {
 		        plottingSystem.createPlot1D(x, Arrays.asList(data), getEditorInput().getName(), monitor);
 		        return;
 			}
 
             final Map<Integer,List<AbstractDataset>> ys = sels.isEmpty()
                                                        ? null
                                 		               : new HashMap<Integer,List<AbstractDataset>>(4);
 
            // Sort ys by axes (for 2D there is one y)
             if (!sels.isEmpty()) {
         	   for (int i = 1; i <= 4; i++) {
         		   List<AbstractDataset> tmp = getYS(i, sels, monitor);
         		   if (tmp!=null) {
         			   ys.put(i, tmp);
         		   }
         	   }
             }
 
             Display.getDefault().syncExec(new Runnable() {
             	public void run() {
             		createPlotSeparateAxes(x,ys,monitor);
             	}
             });
 
 		}
 		monitor.done();
 	}
 
 	protected void createPlotSeparateAxes(final AbstractDataset                    x,
 			                              final Map<Integer,List<AbstractDataset>> ys,
 			                              final IProgressMonitor                   monitor) {
 		try {
 			for (int i = 1; i <= 4; i++) {
 				final IAxis axis = axisMap.get(i);
 				if (ys.get(i)==null) {
 					axis.setVisible(false);
 					continue;
 				}
 				axis.setVisible(true);
 				plottingSystem.setSelectedYAxis(axis);	
 
 				plottingSystem.createPlot1D(x, ys.get(i), getEditorInput().getName(), monitor);
 			} 
 		} finally {
 			plottingSystem.setSelectedYAxis(axisMap.get(1));
 		}
 	}
 
 	private List<AbstractDataset> getYS(int iyaxis, List<CheckableObject> selections, IProgressMonitor monitor) {
 		
 		List<AbstractDataset> ys = new ArrayList<AbstractDataset>(3);
 		for (CheckableObject co : selections) {
 			
 			if (co.getYaxis()!=iyaxis) continue;
 			ys.add(getDataSet(co, monitor));
 			if (monitor.isCanceled()) return null;
 			monitor.worked(1);
 		}
 		return ys.isEmpty()?null:ys;
 	}
 
 	@Override
 	public AbstractDataset getDataSet(String name, IMonitor monitor) {
 		try {
 			if (dataCache!=null&&dataCache.containsKey(name)) return (AbstractDataset)dataCache.get(name);
 			
 			AbstractDataset set;
 			if (dataCache!=null) {
 				if (dataCache.isEmpty()) {
 					final DataHolder dh = LoaderFactory.getData(EclipseUtils.getFilePath(getEditorInput()), monitor);
 					dataCache.putAll(dh.getMap());
 				}
 				set = (AbstractDataset)dataCache.get(name);
 				
 			} else {
 				set = LoaderFactory.getDataSet(EclipseUtils.getFilePath(getEditorInput()), name, monitor);
 			}
 			try {
 			    set = set.squeeze();
 			} catch (Throwable ignored) {
 				// Leave set assigned as read
 			}
 
 			if (set!=null) set.setName(name);
 			return set;
 			
 		} catch (Exception e) {
 			logger.error("Cannot read "+name);
 			return null;
 		}
 	}
 
 	@Override
 	public AbstractDataset getExpressionSet(String name, final IMonitor monitor) {
 
 		throw new NullPointerException("Expressions are not supported by "+getClass().getName());
 	}
 	
 	@Override
 	public boolean isDataSetName(String name, IMonitor monitor) {
 		if (dataNames==null) return false;
 		return dataNames.contains(name);
 	}
 	
 	@Override	
 	public boolean isExpressionSetName(String name, IMonitor monitor) {
 		throw new NullPointerException("Expressions are not supported by "+getClass().getName());
 	}
 	
 	private AbstractDataset getDataSet(final Object input, final IProgressMonitor monitor) {
 		
 	    Object object = input;	
 		if (input instanceof CheckableObject) {
 			CheckableObject check = (CheckableObject)input;
 			object = check.isExpression() ? check.getExpression() : check.getName();
 		}
 		if (object instanceof ExpressionObject) {
 			try {
 				return ((ExpressionObject)object).getDataSet(monitor);
 			} catch (Exception e) {
 				// valid, user can enter an invalid expression. In this case
 				// it colours red but does not stop them from using the view.
 				return new DoubleDataset();
 			}
 		}
  		return getDataSet((String)object, new ProgressMonitorWrapper(monitor));
 	}
 
 
 	@Override
 	public void setInput(final IEditorInput input) {
 		
 		super.setInput(input);
 		setPartName(input.getName());
 		
 		if (dataCache!=null) dataCache.clear();
 		
 		extractingMetaData = true;
 		// Get the meta data in a thread to avoid things breaking
 		final Job getMeta = new Job("Extract Meta Data") {
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				final String    path = EclipseUtils.getFilePath(getEditorInput());
 				try {
 					PlotDataEditor.this.metaData = LoaderFactory.getMetaData(path, null);
 			        if (metaData==null || metaData.getDataNames()==null) {
 						DataHolder dh = LoaderFactory.getData(path, null);
 						if (dh==null) dh= new DataHolder();
 						PlotDataEditor.this.dataNames = dh.getMap().keySet();
 			        } else {
 			        	PlotDataEditor.this.dataNames = metaData.getDataNames();
 			        }
 				} catch (Exception ne) {
 					logger.error("Cannot generate meta data!", ne);
 					return Status.CANCEL_STATUS;
 				} finally {
 			        extractingMetaData = false;
 				}
 				return Status.OK_STATUS;
 			}
 		};
 		getMeta.setSystem(true);
 		getMeta.schedule();
 
 		createData(input);
 	}
 	
     /**
      * Method used to select all 1D plots in the order they are extracted.
      * If overide then current selected plots will be ignored.
      */
 	public void setAll1DSelected(final boolean overide) {
 		
 		final PlotDataComponent dataSetComponent = getDataSetComponent();
          dataSetComponent.setAll1DSelected(overide);
 	}
 
 	/**
 	 * Reads the data sets from the IEditorInput
 	 */
 	private void createData(final IEditorInput input) {
 
 		final UIJob job = new UIJob("Update data") {
 			
 			@Override
 			public IStatus runInUIThread(IProgressMonitor monitor) {
 				final PlotDataComponent dataSetComponent = getDataSetComponent();
 		 		if (dataSetComponent==null) return Status.CANCEL_STATUS;
 				try {
 					
 					// We wait while extracting meta data
 					int waited = 0;
 					while(extractingMetaData) {
 						Thread.sleep(100);
 						waited+=100;
 						if (waited>=240000) { // 4 mins
 							logger.error("Cannot extract meta data from "+EclipseUtils.getFilePath(getEditorInput()));
 						    return Status.CANCEL_STATUS;
 						}
 					}
 					
 					dataSetComponent.setMetaData(metaData);
 					dataSetComponent.refresh();
 					PlotDataEditor.this.plottingSystem.setRootName(dataSetComponent.getRootName());
 					return Status.OK_STATUS;
 				} catch (Exception ne) {
 					logger.error("Cannot open nexus", ne);
 					return Status.CANCEL_STATUS;
 				}
 			}
 		};
 		job.setUser(false);
 		job.schedule(10);
 	}
 
 	
 
 	@Override
 	public void setFocus() {
 		
 		if (plottingSystem!=null) {
 			plottingSystem.setFocus();
 		}
 		
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				PlotDataComponent pc = getDataSetComponent();
 				if (pc!=null && (pc.getData()==null || pc.getData().isEmpty())) {
 					createData(getEditorInput());
 				}			
 			}
 		});
 	}
 
 	@Override
 	public void doSave(IProgressMonitor monitor) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void doSaveAs() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public boolean isSaveAsAllowed() {
 		return false;
 	}
 
     @Override
     public void dispose() {
 
      	if (plottingSystem!=null) plottingSystem.dispose();
      	super.dispose();
     }
 
 	@Override
 	public PlotDataComponent getDataSetComponent() {
 		
 		final IWorkbenchPage wb =EclipseUtils.getActivePage();
 		if (wb==null) return null;
 		
 		final PlotDataView view = (PlotDataView)wb.findView(PlotDataView.ID);
 		if (view==null) return null;
 		
 		IPage page = view.getCurrentPage();
 		if (!(page instanceof PlotDataPage)) return null;
 		return ((PlotDataPage)page).getDataSetComponent();
 	}
 	public SliceComponent getSliceComponent() {
 		
 		final IWorkbenchPage wb =EclipseUtils.getActivePage();
 		if (wb==null) return null;
 		
 		final PlotDataView view = (PlotDataView)wb.findView(PlotDataView.ID);
 		if (view==null) return null;
 		
 		IPage page = view.getCurrentPage();
 		if (!(page instanceof PlotDataPage)) return null;
 		return ((PlotDataPage)page).getSliceComponent();
 	}
 
 	public IPlottingSystem getPlotWindow() {
 		return plottingSystem;
 	}
 
 	public AbstractPlottingSystem getPlottingSystem() {
 		return this.plottingSystem;
 	}
 	public PlotDataEditor getDataSetEditor() {
 		return this;
 	}
 
     public Object getAdapter(@SuppressWarnings("rawtypes") final Class clazz) {
 		
 		if (clazz == Page.class) {
 			final PlotDataEditor      ed  = getDataSetEditor();
 			return new PlotDataPage(ed);
 		} else if (clazz == IToolPageSystem.class) {
 			return getPlottingSystem();
 		}
 		
 		return super.getAdapter(clazz);
 	}
 
 	@Override
 	public void deleteExpression() {
 		getDataSetComponent().deleteExpression();
 	}
 
 	@Override
 	public void addExpression() {
 		getDataSetComponent().addExpression();
 	}
 
 	public String toString(){
 		if (getEditorInput()!=null) return getEditorInput().getName();
 		return super.toString();
 	}
 
 }
