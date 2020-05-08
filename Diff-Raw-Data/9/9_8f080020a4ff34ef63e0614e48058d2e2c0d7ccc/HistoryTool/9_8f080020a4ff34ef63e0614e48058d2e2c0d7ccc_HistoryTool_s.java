 package org.dawb.workbench.plotting.tools.history;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import org.dawb.common.ui.plot.AbstractPlottingSystem;
 import org.dawb.common.ui.plot.trace.ILineTrace;
 import org.dawb.common.ui.plot.trace.ITrace;
 import org.dawb.common.ui.plot.trace.ITraceListener;
 import org.dawb.common.ui.plot.trace.TraceEvent;
 import org.dawb.workbench.plotting.Activator;
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
 import org.eclipse.jface.viewers.EditingSupport;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.TextCellEditor;
 import org.eclipse.jface.viewers.ViewerCell;
 import org.eclipse.jface.window.ToolTip;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
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
 		
 	public HistoryTool() {
 		super();
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
 			final HistoryBean bean = new HistoryBean();
 			bean.setXdata(lineTrace.getXData());
 			bean.setYdata(lineTrace.getYData());
 			bean.setTraceName(iTrace.getName());
 			bean.setPlotColour(lineTrace.getTraceColor().getRGB());
 			bean.setPlotName(getPlottingSystem().getPlotName());
 			history.put(bean.getTraceKey(), bean);
 		}
 		refresh();
 	}
 
 	private ITraceListener autoAddTraceListener;
 	
 	/**
 	 * May be overridden to provide additional actions.
 	 */
 	protected MenuManager createActions(final MenuManager rightClick) {
 		
 		this.autoAddTraceListener = new ITraceListener.Stub() {
 			@Override
 			public void tracesPlotted(TraceEvent evt) {
 				addTraces(); // Adds anything it can.
 			}
 		};
 		final Action autoAdd = new Action("Automatically add any new plots to history", IAction.AS_CHECK_BOX) {
 			public void run() {
 				if (isChecked()) {
 					getPlottingSystem().addTraceListener(autoAddTraceListener);
 					addTraces();
 				} else {
 					getPlottingSystem().removeTraceListener(autoAddTraceListener);
 				}
 			}
 		};
 		getSite().getActionBars().getToolBarManager().add(autoAdd);
 		
 		autoAdd.setImageDescriptor(Activator.getImageDescriptor("icons/autoadd.png"));
 		getSite().getActionBars().getToolBarManager().add(autoAdd);
 		getSite().getActionBars().getToolBarManager().add(new Separator());
 		
 		return super.createActions(rightClick);
 	}
 	protected void createColumns(TableViewer viewer) {
 		
 		ColumnViewerToolTipSupport.enableFor(viewer,ToolTip.NO_RECREATE);
 		viewer.setColumnProperties(new String[] { "Selected", "Name", "Original Plot", "Color" });
 
 		TableViewerColumn var = new TableViewerColumn(viewer, SWT.LEFT, 0);
 		var.getColumn().setText("Plot"); // Selected
 		var.getColumn().setWidth(50);
 		var.setLabelProvider(new HistoryLabelProvider());
 
 		var = new TableViewerColumn(viewer, SWT.CENTER, 1);
 		var.getColumn().setText("Name");
 		var.getColumn().setWidth(200);
 		var.setLabelProvider(new HistoryLabelProvider());
 		var.setEditingSupport(new HistoryEditingSupport(viewer));
 		
 		var = new TableViewerColumn(viewer, SWT.CENTER, 2);
 		var.getColumn().setText("Original Plot");
 		var.getColumn().setWidth(200);
 		var.setLabelProvider(new HistoryLabelProvider());
 
 		var = new TableViewerColumn(viewer, SWT.CENTER, 3);
 		var.getColumn().setText("Shape");
 		var.getColumn().setWidth(80);
 		var.setLabelProvider(new HistoryLabelProvider());
 
 		var = new TableViewerColumn(viewer, SWT.CENTER, 4);
 		var.getColumn().setText("Color");
 		var.getColumn().setWidth(60);
 		var.setLabelProvider(new HistoryLabelProvider());
 		
 	}   
 	
 	/**
 	 * Pushes history plot to and from the main plot depending on if it is selected.
 	 */
 	protected void updatePlot(HistoryBean bean) {
 		
 		if (updatingAPlotAlready) return;
 		try {
 			updatingAPlotAlready = true;
 			
 			final boolean isSamePlot = getPlottingSystem().getPlotName()!=null && getPlottingSystem().getPlotName().equals(bean.getPlotName());		
 			if (isSamePlot) {
 				final String message = "Cannot update "+bean.getTraceName()+" from memory to plot in "+bean.getPlotName()+" as it comes from this plot originally!";
 				logger.trace(message);
 			    
 				// User may be interested in this fact.
 				Activator.getDefault().getLog().log(new Status(IStatus.WARNING, "org.dawb.workbench.plotting", message));
 				final ITrace trace = getPlottingSystem().getTrace(bean.getTraceName());
 				if (trace!=null) {
 					bean.setPlotColour(((ILineTrace)trace).getTraceColor().getRGB());
 					if (viewer!=null) viewer.refresh(bean);
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
 					final ILineTrace trace = getPlottingSystem().createLineTrace(traceName);
 					trace.setUserObject(HistoryType.HISTORY_PLOT);
 					trace.setData(bean.getXdata(), bean.getYdata());
 					getPlottingSystem().addTrace(trace);
 					bean.setPlotColour(trace.getTraceColor().getRGB());
 					if (viewer!=null) viewer.refresh(bean);
 				}
 			}
			((AbstractPlottingSystem)getPlottingSystem()).repaint(false);
 		} finally {
 			updatingAPlotAlready = false;
 		}
 	}
 
 	private class HistoryLabelProvider extends ColumnLabelProvider {
 		
 		private Image checkedIcon;
 		private Image uncheckedIcon;
 		
 		public HistoryLabelProvider() {
 			
 			ImageDescriptor id = Activator.getImageDescriptor("icons/ticked.png");
 			checkedIcon   = id.createImage();
 			id = Activator.getImageDescriptor("icons/unticked.gif");
 			uncheckedIcon =  id.createImage();
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
 			     return bean.getTraceName();
 			}
 			if (columnIndex==2) {
 			     return bean.getPlotName();
 			}
 			if (columnIndex==3) {
 			     return Arrays.toString(bean.getYdata().getShape());
 			}
 			if (columnIndex==4) {
 			     return "-------";
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
 			if (columnIndex==4) {
 				final HistoryBean bean = (HistoryBean)element;
 				return new Color(null, bean.getPlotColour());
 			}
 			return null;
 		}
 
 		/* (non-Javadoc)
 		 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
 		 */
 		public Color getForeground(Object element) {
 			return getColor(element);
 		}
 
 	}
 	
 	private class HistoryEditingSupport extends EditingSupport {
 
 		public HistoryEditingSupport(ColumnViewer viewer) {
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
 			return ((HistoryBean)element).getTraceName();
 		}
 
 		@Override
 		protected void setValue(Object element, Object value) {
 			((HistoryBean)element).setTraceName((String)value);
 			viewer.refresh(element);
 		}
 
 	}
 
 
 
 }
