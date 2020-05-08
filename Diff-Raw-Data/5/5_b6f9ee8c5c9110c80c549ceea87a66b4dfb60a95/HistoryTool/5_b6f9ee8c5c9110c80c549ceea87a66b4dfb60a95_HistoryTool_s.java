 package org.dawnsci.plotting.tools.history;
 
 import java.io.Serializable;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.dawb.common.services.IExpressionObject;
 import org.dawb.common.ui.plot.tools.HistoryType;
 import org.dawnsci.plotting.api.axis.IAxis;
 import org.dawnsci.plotting.api.trace.ILineTrace;
 import org.dawnsci.plotting.api.trace.ITrace;
 import org.dawnsci.plotting.api.trace.ITraceListener;
 import org.dawnsci.plotting.api.trace.TraceEvent;
 import org.dawnsci.plotting.tools.Activator;
 import org.dawnsci.plotting.tools.history.HistoryBean.AxisType;
 import org.dawnsci.plotting.tools.profile.ProfileType;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.ColumnLabelProvider;
 import org.eclipse.jface.viewers.ColumnViewer;
 import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
 import org.eclipse.jface.viewers.ComboBoxCellEditor;
 import org.eclipse.jface.viewers.EditingSupport;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.TextCellEditor;
 import org.eclipse.jface.viewers.ViewerCell;
 import org.eclipse.jface.window.ToolTip;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
 
 public class HistoryTool extends AbstractHistoryTool implements MouseListener {
 
 	private Logger logger = LoggerFactory.getLogger(HistoryTool.class);
 	
 	/**
 	 * We simply keep the history in a static map of traces.
 	 */
 	private static Map<String, HistoryBean> history;
     static {
     	if (history==null) history = new LinkedHashMap<String, HistoryBean>(17);
     }
     protected Map<String, HistoryBean> getHistoryCache() {
     	return history;
     }
 	@Override
 	public Serializable getToolData() {
 		return (Serializable) history;
 	}		
 	public HistoryTool() {
 		super(true);
 	}
 	
 	@Override
 	public ToolPageRole getToolPageRole() {
 		return ToolPageRole.ROLE_1D;
 	}
 	
 	protected IAction createAddAction() {
 		return new Action("Add currently plotted plot(s) to history", Activator.getImageDescriptor("icons/add.png")) {
 			public void run() {
 				addTraces();
 			}
 		};
 	}
 
 	protected void addTraces() {
 		final Collection<ITrace> traces = getPlottingSystem().getTraces(ILineTrace.class);
 		if (traces==null||traces.isEmpty()) return;
 		
 		// TODO Check if one of our history traces.
 		for (ITrace iTrace : traces) {
 			
 			if (iTrace.getUserObject()==HistoryType.HISTORY_PLOT) continue;
 			if (!iTrace.isUserTrace()) continue;
 			final ILineTrace lineTrace = (ILineTrace)iTrace;
 			final HistoryBean bean = new HistoryBean(this);
 			bean.setXdata((AbstractDataset)lineTrace.getXData());
 			bean.setYdata((AbstractDataset)lineTrace.getYData());
 			bean.setTraceName(iTrace.getName());
 			if (lineTrace.getTraceColor()!=null) {
 				bean.setPlotColour(lineTrace.getTraceColor().getRGB());
 			}
 			if (lineTrace.getUserObject()!=null && lineTrace.getUserObject() instanceof String) {
 				bean.setVariable((String)lineTrace.getUserObject());
 			}
 			
 			bean.setPlotName(getPlottingSystem().getPlotName());
 		    if (isLinkedToolPage()) {
 				// Go back up one so that history of profiles can be done.
 		    	// This is the plotting system for the image, so we take the
 		    	// image name and use that.
 				bean.setPlotName(getLinkedToolPage().getPlottingSystem().getPlotName());
 			}
 		    
 		    // All profiles are treated as unique
 		    if (iTrace.getUserObject()==ProfileType.PROFILE) {
 		    	bean.generateUniqueKey(history.keySet());
 		    }
 		    
 			bean.setSelected(true);
 			history.put(bean.getTraceKey(), bean);
 		}
 		refresh();
 		updatePlots(true);
 	}
 
 	private ITraceListener autoAddTraceListener;
 	private static Action autoAdd;
 	/**
 	 * May be overridden to provide additional actions.
 	 */
 	protected MenuManager createActions(final MenuManager rightClick) {
 		
 		this.autoAddTraceListener = new ITraceListener.Stub() {
 			@Override
 			public void tracesAdded(TraceEvent evt) {
 				addTraces(); // Adds anything it can.
 			}
 		};
 		
 		MenuManager ret = super.createActions(rightClick);
 		
 		if (autoAdd==null) autoAdd = new Action("Automatically add any new plots to history", IAction.AS_CHECK_BOX) {
 			public void run() {
 				toggleAutomaticallyAddTraces();
 			}
 		};
 		getSite().getActionBars().getToolBarManager().add(autoAdd);
 		
 		autoAdd.setImageDescriptor(Activator.getImageDescriptor("icons/autoadd.png"));
 		getSite().getActionBars().getToolBarManager().add(new Separator());
 		getSite().getActionBars().getToolBarManager().add(autoAdd);
 		getSite().getActionBars().getToolBarManager().add(new Separator());
 
 		return ret;
 	}
 	
 	protected void toggleAutomaticallyAddTraces() {
 		if (autoAdd==null)              return;
 		if (autoAddTraceListener==null) return;
 		if (getPlottingSystem()==null)  return;
 		if (autoAdd.isChecked()) {
 			getPlottingSystem().addTraceListener(autoAddTraceListener);
 			addTraces();
 		} else {
 			getPlottingSystem().removeTraceListener(autoAddTraceListener);
 		}		
 	}
 	
 	public void activate() {
 		super.activate();
 		toggleAutomaticallyAddTraces();
 	}
 	
 	public void deactivate() {
 		super.deactivate();
 		if (autoAddTraceListener==null) return;
 		if (getPlottingSystem()==null)  return;
 		getPlottingSystem().removeTraceListener(autoAddTraceListener);
 	}
 
 	protected int createColumns(TableViewer viewer) {
 		
 		ColumnViewerToolTipSupport.enableFor(viewer,ToolTip.NO_RECREATE);
 		viewer.setColumnProperties(new String[] { "Selected", "Name", "Original Plot", "Color" });
 
 		int count = 0;
 		TableViewerColumn var = new TableViewerColumn(viewer, SWT.LEFT, count);
 		var.getColumn().setText("Plot"); // Selected
 		var.getColumn().setWidth(50);
 		var.setLabelProvider(new HistoryLabelProvider());
 
 		var = new TableViewerColumn(viewer, SWT.CENTER, ++count);
 		var.getColumn().setText("Name");
 		var.getColumn().setWidth(140);
 		var.setLabelProvider(new HistoryLabelProvider());
 		var.setEditingSupport(new ExpressionEditingSupport(viewer));
 		
 		var = new TableViewerColumn(viewer, SWT.CENTER,  ++count);
 		var.getColumn().setText("Original Plot");
 		var.getColumn().setWidth(140);
 		var.setLabelProvider(new HistoryLabelProvider());
 
 		var   = new TableViewerColumn(viewer, SWT.LEFT,  ++count);
 		var.getColumn().setText(" ");
 		var.getColumn().setWidth(32);
 		var.setLabelProvider(new AxisLabelProvider());
 		var.setEditingSupport(new AxisEditingSupport(viewer));
 
 		var = new TableViewerColumn(viewer, SWT.CENTER,  ++count);
 		var.getColumn().setText("Shape");
 		var.getColumn().setWidth(80);
 		var.setLabelProvider(new HistoryLabelProvider());
 		
 		return count+1;
 	} 
 	
 	/**
 	 * Moves any axes that are X to Y1
 	 */
 	private void removeXAxis() {
         for (HistoryBean bean : getHistoryCache().values()) {
 			if (AxisType.X==bean.getAxis() && bean.isSelected()) {
 				bean.setAxis(AxisType.Y1);
 			}
 		}
 	}
 
 	protected void updatePlots(boolean force) {
 		
 		if (!isActive()) return;
 		
 		super.updatePlots(force);
 		checkVisibleAxes();
 	}
 	protected HistoryBean toggleSelection() {
 		final HistoryBean bean = super.toggleSelection();
 		if (AxisType.X==bean.getAxis()) {
 			getPlottingSystem().clear();
 			updatePlots(true);
 			return bean;
 		}
 		if (bean!=null) checkVisibleAxes();
 		return bean;
 	}
 
     /**
      * Checks to see if all axes should still be visible.
      */
 	private void checkVisibleAxes() {
 		if (updatingPlotsAlready) return;
 		// We now look for any unused axes and hide them.
 		// Not an ideal thing to do but the 'Data' page and other tools
 		// may be using the other axes so we cannot be sure to remove
 		// any that we are no longer using here.
 		final Collection<IAxis> usedAxes = new HashSet<IAxis>(3);
 		usedAxes.add(getPlottingSystem().getSelectedXAxis());
 		usedAxes.add(getPlottingSystem().getSelectedYAxis());
 		for (ITrace trace : getPlottingSystem().getTraces()) {
 			if (trace instanceof ILineTrace) {
 				ILineTrace ltrace = (ILineTrace)trace;
 				if (!ltrace.isVisible()) continue;
 				usedAxes.add(ltrace.getXAxis());
 				usedAxes.add(ltrace.getYAxis());
 			}
 		}
 		// Finally change the visibility to those actually used
 		final List<IAxis> allAxes = getPlottingSystem().getAxes();
 		for (IAxis iAxis : allAxes) {
 			iAxis.setVisible(usedAxes.contains(iAxis));
 		}
 	}
 	/**
 	 * Pushes history plot to and from the main plot depending on if it is selected.
 	 */
 	protected void updatePlot(HistoryBean bean, boolean force) {
 		
 		if (getPlottingSystem().is2D()) {
 			logger.error("Plotting system is plotting 2D data, history should not be active.");
 			return;
 		}
 		if (updatingAPlotAlready&&!force) return;
 		try {
 			updatingAPlotAlready = true;
 			
 			final boolean isSamePlot = getPlottingSystem().getPlotName()!=null && getPlottingSystem().getPlotName().equals(bean.getPlotName());		
 			if (isSamePlot) {
 				final String message = "Cannot update "+bean.getTraceName()+" from memory to plot in "+bean.getPlotName()+" as it comes from this plot originally!";
 				logger.trace(message);
 			    
 				// User may be interested in this fact.
 				Activator.getPluginLog().log(new Status(IStatus.WARNING, "org.dawnsci.plotting", message));
 				final ITrace trace = getPlottingSystem().getTrace(bean.getTraceName());
 				if (trace!=null) {
 //					bean.setPlotColour(((ILineTrace)trace).getTraceColor().getRGB());
 //					if (viewer!=null) viewer.refresh(bean);
 					return;
 				}
 			}
 			
 			final String traceName = bean.createTraceName();
 				
 			if (!bean.isSelected()) {
 				final ITrace trace = getPlottingSystem().getTrace(traceName);
 				if (trace!=null) getPlottingSystem().removeTrace(trace);
 			} else {
 				
 				if (getPlottingSystem().getTrace(traceName)!=null) {
 					logger.warn("Cannot bring "+traceName+" from memory to plot in "+bean.getPlotName()+" as it already exists there!");
 					return;
 				} else {
 					
 					IAxis selectedYAxis = getPlottingSystem().getSelectedYAxis();
 					try {
 						if (bean.getAxis()!=null) {
 							if (bean.getAxis()==AxisType.Y2) {
 								IAxis y2 = getPlottingSystem().getAxis("Y2");
 								if (y2==null) {
 									y2 = getPlottingSystem().createAxis("Y2", true,SWT.LEFT);
 									y2.setTitle("Y2");
 								}
 								y2.setVisible(true);
 								getPlottingSystem().setSelectedYAxis(y2);
 							} else if (bean.getAxis()==AxisType.X) {
 								return; // This is the X data which we will use.
 							}
 						}
 						final ILineTrace trace = getPlottingSystem().createLineTrace(traceName);
 						trace.setUserObject(HistoryType.HISTORY_PLOT);
 						trace.setData(getXData(bean), bean.getYdata());
 						if (!isColourOk(bean.getPlotColour())) {
 							getPlottingSystem().addTrace(trace);
 							bean.setPlotColour(trace.getTraceColor().getRGB());
 							if (viewer!=null) viewer.refresh(bean);
 						} else {
 							trace.setTraceColor(new Color(null, bean.getPlotColour()));
 							getPlottingSystem().addTrace(trace);
 						}
 					} finally {
 						getPlottingSystem().setSelectedYAxis(selectedYAxis);
 					}
 				}
 			}
 			getPlottingSystem().repaint();
 		} finally {
 			updatingAPlotAlready = false;
 		}
 	}
 
 	/**
 	 * Checks if any bean is set to X, if it is and the X is the right size,
 	 * will return this x-data, otherwise returns the x-data of the bean.
 	 * @param bean
 	 * @return
 	 */
 	private IDataset getXData(HistoryBean bean) {
 		if (bean.getAxis()==AxisType.X) throw new RuntimeException(bean.getTraceName()+" is the X data and should not be plotted as Y anyway!");
 		final IDataset origX = bean.getXdata();
 		for (HistoryBean possibleX : history.values()) {
 			if (possibleX.isSelected() && AxisType.X==possibleX.getAxis()) {
 				final IDataset x = possibleX.getYdata();
 				if (x!=null && x.getSize()==origX.getSize()) return x;
 			}
 		}
 		return origX;
 	}
 	private boolean isColourOk(RGB plotColour) {
 		
 		if (plotColour==null) return false;
 		final Collection<ITrace> lines = getPlottingSystem().getTraces(ILineTrace.class);
 		for (ITrace iTrace : lines) {
 			final ILineTrace lineTrace = (ILineTrace)iTrace;
 			if (lineTrace.getTraceColor()!=null && lineTrace.getTraceColor().getRGB().equals(plotColour)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	private class HistoryLabelProvider extends ColumnLabelProvider {
 		
 		private Image checkedIcon;
 		private Image uncheckedIcon;
 		private Color BLUE,RED;
 		
 		public HistoryLabelProvider() {
 			
 			ImageDescriptor id = Activator.getImageDescriptor("icons/ticked.png");
 			checkedIcon   = id.createImage();
 			id = Activator.getImageDescriptor("icons/unticked.gif");
 			uncheckedIcon =  id.createImage();
 			BLUE = Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
 			RED  = Display.getDefault().getSystemColor(SWT.COLOR_RED);
 		}
 		
 		private int columnIndex;
 		public void update(ViewerCell cell) {
 			columnIndex = cell.getColumnIndex();
 			super.update(cell);
 		}
 		
 		public Image getImage(Object element) {
 			
 			if (!(element instanceof HistoryBean)) return null;
 
 			if (columnIndex==0) {
 				final HistoryBean bean = (HistoryBean)element;
 				return bean.isSelected() ? checkedIcon : uncheckedIcon;
 			}
 			
 			return null;
 		}
 		
 		public String getText(Object element) {
 			
 			if (element instanceof String) return "";
 
 			final HistoryBean bean = (HistoryBean)element;
 			if (columnIndex==1) {
 				final  IExpressionObject o = bean.getExpression();
 				if (o!=null) return o.getExpressionString();
 				return bean.getTraceName();
 			}
 			if (columnIndex==2) {
 				return bean.getPlotName();
 			}
 			if (columnIndex==4) {
 				AbstractDataset data = bean.getYdata();
 				if (data==null) return "-";
 				return Arrays.toString(bean.getYdata().getShape());
 			}
 			return "";
 		}
 		
 		public void dispose() {
 			super.dispose();
 			checkedIcon.dispose();
 			uncheckedIcon.dispose();
 		}
 		
 		private Color getColor(Object element) {
 			if (!(element instanceof HistoryBean)) return null;
 			if (columnIndex==1) { // The name
 				final HistoryBean bean = (HistoryBean)element;
 				if (bean.getPlotColour()==null) return null;
 				return new Color(null, bean.getPlotColour());
 			}
 			return null;
 		}
 
 		/* (non-Javadoc)
 		 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
 		 */
 		public Color getForeground(Object element) {
 			if (columnIndex==1) {
 				final  IExpressionObject o = ((HistoryBean)element).getExpression();
 				if (o!=null) {
 				    return o.isValid(new IMonitor.Stub()) ? BLUE : RED;
 				}
 			}
 			return getColor(element);
 		}
 
 	}
 	
 	private class ExpressionEditingSupport extends EditingSupport {
 
 		public ExpressionEditingSupport(ColumnViewer viewer) {
 			super(viewer);
 		}
 
 		@Override
 		protected CellEditor getCellEditor(Object element) {
 			return new TextCellEditor((Composite)getViewer().getControl());
 		}
 
 		@Override
 		protected boolean canEdit(Object element) {
 			return true;
 		}
 
 		@Override
 		protected Object getValue(Object element) {
 			final HistoryBean bean = ((HistoryBean)element);
 			return bean.getTraceName();
 		}
 
 		@Override
 		protected void setValue(Object element, Object value) {
 			final HistoryBean bean = ((HistoryBean)element);
 			final  IExpressionObject o = bean.getExpression();
             if (o!=null) {
             	o.setExpressionString((String)value);
            	getPlottingSystem().clear();
            	updatePlots(true);
             } else {
 			    ((HistoryBean)element).setTraceName((String)value);
             }
 			viewer.refresh(element);
 		}
 
 	}
 
 	private class AxisLabelProvider extends ColumnLabelProvider {
 		
 		@Override
 		public String getText(Object ob) {
             if (ob==null || !(ob instanceof HistoryBean)) return null;
             return ((HistoryBean)ob).getAxis().name();
 		}
 	}
 	
 	private class AxisEditingSupport extends EditingSupport {
 
 		public AxisEditingSupport(ColumnViewer viewer) {
 			super(viewer);
 		}
 		
 		@Override
 		protected CellEditor getCellEditor(final Object element) {
 			// FIX to http://jira.diamond.ac.uk/browse/DAWNSCI-380 remove axes until they work
 			ComboBoxCellEditor ce = new ComboBoxCellEditor((Composite)getViewer().getControl(), new String[]{"X","Y1","Y2" /**,"Y3","Y4" **/} , SWT.READ_ONLY);
 			final CCombo ccombo = (CCombo)ce.getControl();
 			ccombo.addSelectionListener(new SelectionAdapter() {
 				public void widgetSelected(SelectionEvent e) {
 					setValue(element, ccombo.getItem(ccombo.getSelectionIndex()));
 				}
 			});
 			return ce;
 		}
 
 		@Override
 		protected boolean canEdit(Object ob) {
 			if (ob==null || !(ob instanceof HistoryBean)) return false;
 			return true;
 		}
 
 		@Override
 		protected Object getValue(Object element) {
 			return ((HistoryBean)element).getAxis().getIndex();
 		}
 
 		@Override
 		protected void setValue(Object element, Object value) {
 			viewer.cancelEditing();
 			HistoryBean hb = (HistoryBean)element;
 			if (value instanceof String) {
 				final AxisType type = AxisType.valueOf((String)value);
 				if (type == AxisType.X) removeXAxis(); // Move the other x if there is one to Y1
 
 				hb.setAxis(type);
 				getPlottingSystem().clear();
 				updatePlots(true);
 				viewer.refresh(); // Must be complete refresh, we changed other axes.
 			}
 		}
 		
 	}
 }
