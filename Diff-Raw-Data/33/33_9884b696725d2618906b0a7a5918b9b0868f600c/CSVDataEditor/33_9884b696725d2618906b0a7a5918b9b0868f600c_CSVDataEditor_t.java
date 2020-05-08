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
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import org.dawb.common.ui.util.CSVUtils;
 import org.dawb.common.ui.util.EclipseUtils;
 import org.dawb.common.ui.util.GridUtils;
 import org.dawb.workbench.ui.Activator;
 import org.dawb.workbench.ui.editors.preference.EditorConstants;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.ContributionManager;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.jface.dialogs.IPageChangedListener;
 import org.eclipse.jface.dialogs.PageChangedEvent;
 import org.eclipse.jface.preference.PreferenceDialog;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.viewers.ColumnLabelProvider;
 import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.window.ToolTip;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CLabel;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.IReusableEditor;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.dialogs.PreferencesUtil;
 import org.eclipse.ui.part.EditorPart;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 
 
 /**
  * An editor which shows the current data sets plotted in a table.
  * 
  * This may be needed if the scientist has manipulated the data and would like to view it or export it.
  */
 public class CSVDataEditor extends EditorPart implements IReusableEditor, IPageChangedListener {
 	
 	private static Logger logger = LoggerFactory.getLogger(CSVDataEditor.class);
 	
 	// This view is a composite of two other views.
 	private TableViewer tableViewer;
 	private CLabel      errorLabel;
 	private Composite   main;
 	private Map<String, IDataset> data;
 	private IDatasetEditor  dataProvider;
 	private IPropertyChangeListener propListener;
 	
 	public CSVDataEditor() {
 	    
 	}
 	
 	public void setDataProvider(final IDatasetEditor dataHolder) {
 		this.dataProvider = dataHolder;
 	}
 
 	@Override
 	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
 		setSite(site);
 		setInput(input);
 		setPartName(input.getName());
 	}
 
 	@Override
 	public boolean isDirty() {
 		return false;
 	}
 	
 	@Override
 	public void createPartControl(Composite parent) {
 		
 		this.main       = new Composite(parent, SWT.NONE);
 		final GridLayout gridLayout = new GridLayout(1, false);
 		gridLayout.verticalSpacing = 0;
 		gridLayout.marginWidth = 0;
 		gridLayout.marginHeight = 0;
 		gridLayout.horizontalSpacing = 0;
 		main.setLayout(gridLayout);
 		
 		// We use a local toolbar to make it clear to the user the tools
 		// that they can use, also because the toolbar actions are 
 		// hard coded.
 	    final ToolBarManager   toolMan = new ToolBarManager(SWT.FLAT|SWT.RIGHT);
 	    final ToolBar          toolBar = toolMan.createControl(main);
 	    toolBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
 	    createActions(toolMan);
 	    
 		errorLabel = new CLabel(main, SWT.NONE);
 		errorLabel.setImage(Activator.getImage("icons/error.png"));
 		errorLabel.setText("This editor shows the data plotted. Please plot at least one data set and view the data here.");
 		errorLabel.setForeground(PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_RED));
 		errorLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
 		
 		toolMan.update(false);
 		main.layout();
 		
         this.propListener = new IPropertyChangeListener() {
 			
 			@Override
 			public void propertyChange(PropertyChangeEvent event) {
 				if (event.getProperty().equals(EditorConstants.DATA_FORMAT)) {
 					if (tableViewer!=null) tableViewer.refresh();
 				}
 			}
 		};
 		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(propListener);
  	}
 	
 	private void createActions(final ContributionManager toolMan) {
 		
		final Action refresh = new Action("Refresh", IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				update();
			}
		};
		refresh.setImageDescriptor(Activator.getImageDescriptor("icons/reset.gif"));
		toolMan.add(refresh);
		
		toolMan.add(new Separator(getClass().getName()+"Sep1"));

 		final Action exportCsv = new Action("Export current plotted data to csv file", IAction.AS_PUSH_BUTTON) {
 			@Override
 			public void run() {
 				CSVUtils.createCSV(EclipseUtils.getIFile(getEditorInput()), data, "_plot");
 			}
 		};
 		exportCsv.setImageDescriptor(Activator.getImageDescriptor("icons/page_white_excel.png"));
 		toolMan.add(exportCsv);
 		
 		toolMan.add(new Separator(getClass().getName()+"Sep1"));
 		
 		final Action format = new Action("Preferences...", IAction.AS_PUSH_BUTTON) {
 			@Override
 			public void run() {
 				PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "uk.ac.diamond.scisoft.analysis.rcp.preferencePage", null, null);
 				if (pref != null) pref.open();
 			}
 		};
 		format.setImageDescriptor(Activator.getImageDescriptor("icons/application_view_list.png"));
 		toolMan.add(format);
 	}
 
 	/**
 	 * This method to creates the table columns required to hold the
 	 * 1D data currently in the plotter passed in.
 	 * 
 	 * The first data set is should usually be the x value.
 	 * 
 	 * @param plotter
 	 */
 	private void update() {
 		
 		if (tableViewer!=null) {
			if (!tableViewer.getControl().isDisposed()) {
				GridUtils.setVisible(tableViewer.getTable(), false);
				tableViewer.getTable().dispose();
			}
 		}
 		
 		this.data = dataProvider.getSelected();
 
 		if (data==null || data.isEmpty()) {
 			GridUtils.setVisible(errorLabel, true);
 			main.layout();
 			return;
 		}
 					
 		final Table table = new Table(main, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
 		this.tableViewer  = new TableViewer(table);
 		table.setLayoutData(new GridData(GridData.FILL_BOTH));
 		table.setLinesVisible(true);
 		table.setHeaderVisible(true);
 
 		ColumnViewerToolTipSupport.enableFor(tableViewer, ToolTip.NO_RECREATE);		
 		GridUtils.setVisible(errorLabel, false);
 		main.layout();
 		
 		final boolean is2D = data.size()==1 && data.values().iterator().next().getShape().length==2;
 		int i = 0;
 		for (String name : data.keySet()) {
 
 			final AbstractDataset set = (AbstractDataset)data.get(name);
 
 			final TableViewerColumn col   = new TableViewerColumn(tableViewer, SWT.RIGHT, i);
 			final String colName = set.getName()!=null?set.getName():"";
 			col.getColumn().setText(colName);
 			if (is2D) {
 				col.setLabelProvider(new BufferColumnProvider());
 				col.getColumn().setWidth(3000);
 			} else {
 			    col.setLabelProvider(new ValueColumnProvider(i));
 				col.getColumn().setWidth(150);
 			}
 			++i;
 		}
 		
 		tableViewer.setUseHashlookup(true);
 		tableViewer.setColumnProperties(data.keySet().toArray(new String[data.size()]));
 	    final MenuManager menuManager = new MenuManager();
 	    tableViewer.getControl().setMenu (menuManager.createContextMenu(tableViewer.getControl()));
 	    createActions(menuManager);
 
 		final RowObject[] rowData = getRowData(data.values());
 		tableViewer.setContentProvider(new IStructuredContentProvider() {
 			@Override
 			public void dispose() {}
 			@Override
 			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
 			@Override
 			public Object[] getElements(Object inputElement) {
 				return rowData;
 			}
 		});		
 		tableViewer.setInput(rowData);
 		main.layout();
 	}
 	
 	/**
 	 * The data sets are the columns, we need an array of the rows to work
 	 * best with TableViewer.
 	 * @param sets
 	 * @return an array of RowObjects
 	 */
 	protected RowObject[] getRowData(final Collection<IDataset> sets) {
 		
 		if (sets.size()==1 && sets.iterator().next().getShape().length==2) {
 			
 			final AbstractDataset set  = (AbstractDataset)sets.iterator().next();
 			final int[]          shape = set.getShape();
 			final List<RowObject> rows = new ArrayList<RowObject>(shape[0]);
 			for (int row = 0; row < shape[0]; row++) {
 				final RowObject rowOb = new RowObject(row);
 				rows.add(rowOb);
 				for (int col = 0; col < shape[1]; col++) {
 					rowOb.add(set.getDouble(row, col));
 				}
 			}
 			return rows.toArray(new RowObject[rows.size()]);
 			
 		} else {
 			
 			// Find max
 			int max = Integer.MIN_VALUE;
 			for (IDataset set : sets) max = Math.max(max, set.getSize());
 			
 			final List<RowObject> rows = new ArrayList<RowObject>(max);
 			for (int row = 0; row < max; row++) {
 				final RowObject rowOb = new RowObject(row);
 				rows.add(rowOb);
 				for (IDataset set : sets) {
 					try {
 					    rowOb.add(set.getDouble(row));
 					} catch (Exception ne) {
 						rowOb.add(Double.NaN);
 					}
 				}
 			}
 			
 			return rows.toArray(new RowObject[rows.size()]);
 		}
 	}
 	
 	private class RowObject {
 		private List<Float> rowValues = new ArrayList<Float>(31);
 		private int row;
 		RowObject(int row) {
 			this.row = row;
 		}
 		public void add(float value) {
 			rowValues.add(value);
 		}
 		public void add(double value) {
 			rowValues.add((float)value);
 		}
 		public Float get(int col) {
 			return rowValues.get(col);
 		}
 		public int getRow(){
 			return row;
 		}
 		public String toString() {
 			return rowValues.toString();
 		}
 	}
 	
 	private class ValueColumnProvider extends ColumnLabelProvider {
 		private int columnIndex;
 		ValueColumnProvider(int columnIndex) {
 			this.columnIndex = columnIndex;
 		}
 		@Override
 		public String getText(Object element) {
 			final RowObject row = (RowObject)element;
 			final double    val = row.get(columnIndex);
 			if (Double.isNaN(val)) return "";
 
 			return formatValue(val);
 		}
 		
 		private String formatValue(final double val) {
 			final String formatString = Activator.getDefault().getPreferenceStore().getString(EditorConstants.DATA_FORMAT);
 		    try {
 		    	DecimalFormat format = new DecimalFormat(formatString);
 				return format.format(val);
 		    } catch (Exception ne) {
 		    	logger.debug("Format does not work: "+formatString, ne);
 		    	return String.valueOf(val);
 		    }
 		}
 
 	}
 	
 	private class BufferColumnProvider extends ColumnLabelProvider {
 
 		@Override
 		public String getText(Object element) {
 			final RowObject row = (RowObject)element;
 			return row.toString();
 		}
 	}
 
 	@Override
 	public void setInput(final IEditorInput input) {
 		super.setInput(input);
 		setPartName(input.getName());
 	}
 		
 
 	@Override
 	public void setFocus() {
		try {
			if (tableViewer==null) return;
			if (tableViewer.getControl().isDisposed()) return;
			if (tableViewer!=null) tableViewer.getControl().setFocus();	
		} catch (Throwable ne) {
			return; // Intentionally ignored.
		}
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
 		Activator.getDefault().getPreferenceStore().removePropertyChangeListener(propListener);
 		if (tableViewer!=null) tableViewer.getControl().dispose();
      	if (main!=null)        main.dispose();
      	if (data!=null)        data.clear();
      	dataProvider = null;
     	super.dispose();
     }
 
 	@Override
 	public void pageChanged(PageChangedEvent event) {
 		if (event.getSelectedPage()==this) { // Just selected this page
 			update();
 		}
 	}
 
 }
