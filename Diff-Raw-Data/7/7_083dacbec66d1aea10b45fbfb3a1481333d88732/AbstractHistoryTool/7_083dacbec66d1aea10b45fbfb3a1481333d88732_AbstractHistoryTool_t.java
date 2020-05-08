 package org.dawnsci.plotting.tools.history;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.dawb.common.gpu.Operator;
 import org.dawb.common.services.IExpressionObjectService;
 import org.dawb.common.services.IVariableManager;
 import org.dawb.common.services.ServiceManager;
 import org.dawb.common.ui.util.EclipseUtils;
 import org.dawb.common.ui.wizard.persistence.PersistenceExportWizard;
 import org.dawnsci.plotting.api.tool.AbstractToolPage;
 import org.dawnsci.plotting.api.trace.ITraceListener;
 import org.dawnsci.plotting.api.trace.TraceEvent;
 import org.dawnsci.plotting.tools.Activator;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.ColumnViewer;
 import org.eclipse.jface.viewers.EditingSupport;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.TextCellEditor;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.wizard.IWizard;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.graphics.Region;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.commands.ICommandService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
 import uk.ac.diamond.scisoft.analysis.utils.OSUtils;
 
 public abstract class AbstractHistoryTool extends AbstractToolPage implements MouseListener, KeyListener, IVariableManager {
 
 	protected final static Logger logger = LoggerFactory.getLogger(AbstractHistoryTool.class);
 	
 	protected Composite      composite;
     protected TableViewer    viewer;
     protected ITraceListener traceListener;
 	
 	public AbstractHistoryTool(boolean requireCreateListener) {
 		if (requireCreateListener) this.traceListener = new ITraceListener.Stub() {
 			
 			@Override
 			public void tracesAdded(TraceEvent evt) {
 				updatePlots(false);
 			}
 			
 			@Override
 			public void tracesRemoved(TraceEvent evet) {
 				updatePlots(false);
 			}
 		};
 	}
 
 	/**
 	 * Implement to return the cache of HistoryBeans. Normally this is a 
 	 * static map kept by the sub-classes.
 	 * 
 	 * @return
 	 */
     protected abstract Map<String, HistoryBean> getHistoryCache();
 
 	/**
 	 * Call to process history beans and update the plot with the
 	 * compare data
 	 */
 	protected abstract void updatePlot(HistoryBean bean, boolean force);
 	
 
 	/**
 	 * Called to return an IAction for adding a history bean to the cache.
 	 */
 	protected abstract IAction createAddAction();
 	
 	/**
 	 * Called to create the table columns for interacting with the history
 	 * @return the number of columns added (meaning that it's equal to the next column index to add as 0-based loops).
 	 */
 	protected abstract int createColumns(TableViewer viewer);
 
 	
 	/**
 	 * 
 	 */
 	protected boolean updatingAPlotAlready = false;
 	protected boolean updatingPlotsAlready = false;
     
 	protected void updatePlots(boolean force) {
 		
 		if (updatingPlotsAlready) return;
 		try {
 			updatingPlotsAlready = true;
 			// Loop over history and reprocess it all.
 			for (String key : getHistoryCache().keySet()) {
 				updatePlot(getHistoryCache().get(key), force);
 			}
 		} finally {
 			updatingPlotsAlready = false;
 		}
 	}
 	
 	@Override
 	public void createControl(Composite parent) {
 		
 		this.composite = new Composite(parent, SWT.NONE);
 		composite.setLayout(new FillLayout());
 
 		viewer = new TableViewer(composite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
 		int length = createColumns(viewer);
 		
 		// Add variable name column
 		TableViewerColumn var = new TableViewerColumn(viewer, SWT.LEFT, length);
 		var.getColumn().setText("Variable Name");
 		var.getColumn().setWidth(80);
 		var.setLabelProvider(new VariableNameLabelProvider());
 		var.setEditingSupport(new VariableNameEditingSupport(viewer));
 		setColumnVisible(length, -1 , false);
 		
 		viewer.getTable().setLinesVisible(true);
 		viewer.getTable().setHeaderVisible(true);
 		
 		createActions(new MenuManager());
 				
 		getSite().setSelectionProvider(viewer);
 		
 		viewer.setContentProvider(new IStructuredContentProvider() {			
 			@Override
 			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 			}			
 			@Override
 			public void dispose() {
 			}		
 			@Override
 			public Object[] getElements(Object inputElement) {
 				return getHistoryCache().values().toArray(new HistoryBean[getHistoryCache().size()]);
 			}
 		});
 		viewer.setInput(new Object());
 
 		viewer.getTable().addMouseListener(this);
 		viewer.getTable().addKeyListener(this);
 		
 		
 		// Allow the colours to be drawn nicely.
 		final Table table = viewer.getTable();
 		if (OSUtils.isWindowsOS()) table.addListener(SWT.EraseItem, new Listener() {
 			public void handleEvent(Event event) {
 				if ((event.detail & SWT.SELECTED) != 0) {
 					GC gc = event.gc;
 					Rectangle area = table.getClientArea();
 					/*
 					 * If you wish to paint the selection beyond the end of last column,
 					 * you must change the clipping region.
 					 */
 					int columnCount = table.getColumnCount();
 					if (event.index == columnCount - 1 || columnCount == 0) {
 						int width = area.x + area.width - event.x;
 						if (width > 0) {
 							Region region = new Region();
 							gc.getClipping(region);
 							region.add(event.x, event.y, width, event.height);
 							gc.setClipping(region);
 							region.dispose();
 						}
 					}
 					gc.setAdvanced(true);
 					if (gc.getAdvanced()) gc.setAlpha(50);
 					Rectangle rect = event.getBounds();
 					Color foreground = gc.getForeground();
 					Color background = gc.getBackground();
 					gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_LIST_SELECTION));
 					gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
 					gc.fillGradientRectangle(0, rect.y, 500, rect.height, false);
 
 					// restore colors for subsequent drawing
 					gc.setForeground(foreground);
 					gc.setBackground(background);
 					event.detail &= ~SWT.SELECTED;
 					return;
 				}
 			}
 		});
 
 	}
 	
 	private class VariableNameEditingSupport extends EditingSupport {
 
 		private IExpressionObjectService service;
 
 		public VariableNameEditingSupport(ColumnViewer viewer) {
 			super(viewer);
 			try {
 				this.service = (IExpressionObjectService)ServiceManager.getService(IExpressionObjectService.class);
 			} catch (Exception e) {
 				this.service = null; // allowed to be null.
 			}
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
 			return ((HistoryBean)element).getVariable();
 		}
 
 		@Override
 		protected void setValue(Object element, Object value) {
 			try {
 				final HistoryBean bean = (HistoryBean)element;
 	    		if (bean.getVariable()!=null && bean.getVariable().equals(value)) return;
 	    		if (bean.getVariable()!=null && value!=null && bean.getVariable().equals(((String)value).trim())) return;
 	            String variableName = service.validate(bean.getVariableManager(), (String)value);
 
 				clearExpressionCache(variableName, bean.getVariable());
 				bean.setVariable(variableName);
 				
 			} catch (Exception e) {
 				final String message = "The name '"+value+"' is not valid.";
 				final Status status  = new Status(Status.WARNING, "org.dawb.workbench.ui", message, e);
 				ErrorDialog.openError(Display.getDefault().getActiveShell(), "Cannot rename expression", message, status);
 			    return;
 			}
 			getViewer().refresh();
 
 		}
 
 	}
 	public void clearExpressionCache(String... variableNames) {
 		for (HistoryBean bean : getHistoryCache().values()) {
 			if (bean.isExpression() && (variableNames==null || variableNames.length<1 || bean.getExpression().containsVariable(variableNames)))  {
 				bean.getExpression().clear();
 			}
 		}
 	}
 	
 	public void keyPressed(KeyEvent e) {
 		if (e.character==SWT.DEL) {
 			deleteSelectedBean();
 		}
 	}
 	
 	public void keyReleased(KeyEvent e) {
 		
 	}
 
 
 	protected void setColumnVisible(final int col, final int width, boolean isVis) {
 		if (this.viewer==null || this.viewer.getControl().isDisposed()) return;
 		viewer.getTable().getColumn(col).setWidth(isVis?width:0);
 		viewer.getTable().getColumn(col).setResizable(isVis?true:false);
 	}
 	
 	/**
 	 * May be overridden to provide additional actions.
 	 */
 	protected MenuManager createActions(final MenuManager rightClick) {
 		
 		final IAction addPlot = createAddAction();
 		getSite().getActionBars().getToolBarManager().add(addPlot);
 		rightClick.add(addPlot);
 
 		final Action exportHist = new Action("Export history", Activator.getImageDescriptor("icons/mask-export-wiz.png")) {
 			public void run() {
 				try {
 					IWizard wiz = EclipseUtils.openWizard(PersistenceExportWizard.ID, false);
 					WizardDialog wd = new  WizardDialog(Display.getCurrent().getActiveShell(), wiz);
 					wd.setTitle(wiz.getWindowTitle());
 					wd.open();
 				} catch (Exception e) {
 					logger.error("Problem opening import!", e);
 				}
 			}			
 		};
 		getSite().getActionBars().getToolBarManager().add(exportHist);
 				
 		
 		final Action deletePlot = new Action("Delete selected", Activator.getImageDescriptor("icons/delete.gif")) {
 			public void run() {
 				deleteSelectedBean();
 			}
 		};
 		getSite().getActionBars().getToolBarManager().add(deletePlot);
 		rightClick.add(deletePlot);
 		
 		final Action clearPlots = new Action("Clear history", Activator.getImageDescriptor("icons/plot-tool-history-clear.png")) {
 			public void run() {
 				for (HistoryBean bean : getHistoryCache().values()) {
 					if (!bean.isModifiable()) continue;
 					bean.setSelected(false);
 				}
 				updatePlots(false);
 				clearCache();
 			    refresh();
 			}
 		};
 		getSite().getActionBars().getToolBarManager().add(clearPlots);
 		rightClick.add(clearPlots);
 
 		rightClick.add(new Separator());
 		final Action checkAll = new Action("Select all") {
 			public void run() {
 				setAllChecked(true);
 			}
 		};
 		rightClick.add(checkAll);
 		final Action checkNone = new Action("Select none") {
 			public void run() {
 				setAllChecked(false);
 			}
 		};
 		rightClick.add(checkNone);
 		getSite().getActionBars().getToolBarManager().add(new Separator());
 		rightClick.add(new Separator());
 
 		final Action editAction = new Action("Rename selected", IAction.AS_PUSH_BUTTON) {
 			public void run() {
 				viewer.editElement(getSelectedPlot(), 1);
 			}
 		};
 		editAction.setImageDescriptor(Activator.getImageDescriptor("icons/rename_plot.png"));
 		getSite().getActionBars().getToolBarManager().add(editAction);
 		rightClick.add(editAction);
 		
 		getSite().getActionBars().getToolBarManager().add(new Separator());
 		final Action addExpression = new Action("Add expression") {
 			public void run() {
 				final ICommandService cs = (ICommandService)PlatformUI.getWorkbench().getService(ICommandService.class);
 				try {
 					cs.getCommand("org.dawb.workbench.editors.addExpression").executeWithChecks(new ExecutionEvent());
 				} catch (Exception e) {
 					logger.error("Cannot run action", e);
 				} 
 				
 			}
 		};
 		addExpression.setImageDescriptor(Activator.getImageDescriptor("icons/add_expression.png"));
 		addExpression.setToolTipText("Adds an expression which can be plotted. Must be function of other data sets.");
 		getSite().getActionBars().getToolBarManager().add(addExpression);
 		rightClick.add(addExpression);
 		
 		final Action deleteExpression = new Action("Delete expression") {
 			public void run() {
 				final ICommandService cs = (ICommandService)PlatformUI.getWorkbench().getService(ICommandService.class);
 				try {
 				    cs.getCommand("org.dawb.workbench.editors.deleteExpression").executeWithChecks(new ExecutionEvent());
 				} catch (Exception e) {
 					logger.error("Cannot run action", e);
 				} 
 			}
 		};
 		deleteExpression.setImageDescriptor(Activator.getImageDescriptor("icons/delete_expression.png"));
 		deleteExpression.setToolTipText("Deletes an expression.");
 		getSite().getActionBars().getToolBarManager().add(deleteExpression);
 		rightClick.add(deleteExpression);
 
 		
 		final Menu menu = rightClick.createContextMenu(viewer.getControl());
 		viewer.getControl().setMenu(menu);
 		
 		return rightClick;
 	}
 
 	protected void deleteSelectedBean() {
 		final HistoryBean bean = getSelectedPlot();
 		if (bean==null) return;
 		if (!bean.isModifiable()) return;
 		bean.setSelected(false);
 		updatePlot(bean, false);
 		getHistoryCache().remove(bean.getTraceKey());
 	    refresh();
 	}
 
 	protected void setAllChecked(boolean checked) {
 		for (HistoryBean bean : getHistoryCache().values()) {
 			if (!bean.isModifiable()) continue;
 			bean.setSelected(checked);
 		}
 		updatePlots(false);
 	    refresh();		
 	}
 
 	protected void clearCache() {
 		final Iterator<String> it = getHistoryCache().keySet().iterator();
 		while (it.hasNext()) {
 			String key = it.next();
 			if (!getHistoryCache().get(key).isModifiable()) continue;
 			it.remove();
 		}
 	}
 
 	@Override
 	public Control getControl() {
 		return this.composite;
 	}
 
 	@Override
 	public void setFocus() {
         if (viewer!=null && !viewer.getControl().isDisposed()) viewer.getControl().setFocus();
 	}
 	
 	
 	protected void refresh() {
 		if (viewer==null || viewer.getControl().isDisposed()) return;
 		viewer.refresh();
 	}
 	
 	@Override
 	public void activate() {
 		
         updatePlots(false);
         
         if (getPlottingSystem()!=null) {
         	getPlottingSystem().addTraceListener(this.traceListener);
         }
         super.activate();
         refresh();
 	}
 	
 	@Override
 	public void deactivate() {
         if (getPlottingSystem()!=null) {
         	getPlottingSystem().removeTraceListener(this.traceListener);
         }
 		super.deactivate();
 	}
 
 	@Override
 	public void dispose() {
 	    if (viewer!=null&&!viewer.getControl().isDisposed()) {
 	    	viewer.getControl().removeMouseListener(this);
 	    	viewer.getControl().removeKeyListener(this);
 	    }
 	    deactivate();
 		super.dispose();
 	}
 
 
 	@Override
 	public void mouseDoubleClick(MouseEvent e) {
 		
 	}
 
 	@Override
 	public void mouseDown(MouseEvent e) {
 		if (e.button==1) {
 			
 			// Toggle if first column clicked.
 			Point     pnt  = new Point(e.x, e.y);
 			TableItem item = viewer.getTable().getItem(pnt);
 	        if (item == null) return;
             Rectangle rect = item.getBounds(0);
             if (rect.contains(pnt)) {
   				toggleSelection();
 	        } 	          
 		}
 	}
 
 	@Override
 	public void mouseUp(MouseEvent e) {
 		
 	}
 
 	protected HistoryBean toggleSelection() {
 		final HistoryBean bean = getSelectedPlot();
 		if (bean!=null) {
  			viewer.cancelEditing();
 			bean.setSelected(!bean.isSelected());
 			viewer.update(bean, null);
 			updatePlot(bean, true);
 			return bean;
 		}
 		return null;
 	}
 	
 	protected HistoryBean getSelectedPlot() {
 		final StructuredSelection sel = (StructuredSelection)viewer.getSelection();
 		if (sel.getFirstElement()!=null && sel.getFirstElement() instanceof HistoryBean) {
 			return (HistoryBean)sel.getFirstElement();
 		}
 		return null;
 	}
 	
 
 	@Override
 	public boolean isVariableName(String name, IMonitor monitor) {
 		final Map<String, HistoryBean> history = getHistoryCache();
 		for (HistoryBean bean : history.values()) {
 			if (bean.getVariable().equals(name)) return true;
  		}
 		return false;
 	}
 
 	@Override
 	public AbstractDataset getVariableValue(String name, IMonitor monitor) {
 		final Map<String, HistoryBean> history = getHistoryCache();
 		for (HistoryBean bean : history.values()) {
 			if (bean.getVariable().equals(name)) return bean.getData();
  		}
 		return null;
 	}	
 	
 	/**
 	 * TODO Make history properly lazy? At the moment we assume 
 	 * that the history can kept hard references to fully loaded 
 	 * data.
 	 */
 	@Override
 	public ILazyDataset getLazyValue(String name, IMonitor monitor) {
 		final Map<String, HistoryBean> history = getHistoryCache();
 		for (HistoryBean bean : history.values()) {
 			if (bean.getVariable().equals(name)) return bean.getData();
  		}
 		return null;
 	}	
 
 	@Override
 	public boolean isDataName(String name, IMonitor monitor) {
 		final Map<String, HistoryBean> history = getHistoryCache();
 		return history.containsKey(name);
 	}	
 
 	@Override
 	public ILazyDataset getDataValue(String name, IMonitor monitor) {
 		final Map<String, HistoryBean> history = getHistoryCache();
 		for (String key : history.keySet()) {
 			if (key.equals(name)) return history.get(key).getData();
  		}
 		return null;
 	}	
 
 	@Override
 	public void deleteExpression() {
 		
 		final HistoryBean bean = getSelectedPlot();
 		if (bean==null) return;
 		
 		final Map<String, HistoryBean> history = getHistoryCache();
 		history.remove(bean.getTraceKey());
 		clearExpressionCache(bean.getVariable());
 		
 		viewer.refresh();
 	}
 
 
 	@Override
 	public void addExpression() {
 		
 		final HistoryBean bean = new HistoryBean(this);
 		bean.setExpression(true);
 		bean.setPlotName("Expression");
 		bean.setOperator(Operator.ADD);
 		
 		final Map<String, HistoryBean> history = getHistoryCache();
 		final String traceKey = bean.getTraceKey();
 		System.out.println(traceKey);
 		history.put(traceKey, bean);
 		
 		viewer.refresh();
 		setColumnVisible(viewer.getTable().getColumnCount()-1, 120, true);
 
 		viewer.editElement(bean, 1);
 	}
 
 	@Override
 	public void saveExpressions() {
 
 	}
 
 	@Override
 	public List<String> getVariableNames() {
 		Map<String,HistoryBean> data = getHistoryCache();
 		List<String> ret = new ArrayList<String>(data.size());
 		for (String key : data.keySet()) {
 			ret.add(data.get(key).getVariable());
 		}
 		return ret;
 	}
 	
 	@Override
 	public List<String> getDataNames() {
 		Map<String,HistoryBean> data = getHistoryCache();
 		List<String> ret = new ArrayList<String>(data.size());
 		for (String key : data.keySet()) {
 			ret.add(data.get(key).getTraceName());
 		}
 		return ret;
 	}
 
 }
