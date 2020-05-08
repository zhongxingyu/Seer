 package org.amanzi.awe.afp.wizards;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.amanzi.awe.afp.filters.AfpTRXFilter;
 import org.amanzi.awe.afp.models.AfpModel;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeItem;
 import org.neo4j.graphdb.Node;
 
 public class AfpWizardPage extends WizardPage implements SelectionListener {
 	
 	public static final String ASSIGN = "assign";
 	public static final String CLEAR = "clear";
 	public static final String LOAD = "load";
 	
 	private Label filterInfoLabel;
 	private Group trxFilterGroup;
 	private Label siteFilterInfoLabel;
 	private Group siteTrxFilterGroup;
 	protected AfpModel model;
 	private TableViewer viewer;
 	private AfpTRXFilter filter;
 	private FilterListener listener;
 	protected Button assignButton;
 	
     protected HashMap<String,Set<Object>> uniqueSitePropertyValues = new HashMap<String,Set<Object>>();
     protected HashMap<String,Set<Object>> uniqueSectorPropertyValues = new HashMap<String,Set<Object>>();
     protected HashMap<String,Set<Object>> uniqueTrxPropertyValues = new HashMap<String,Set<Object>>();
 
 
 	protected AfpWizardPage(String pageName) {
 		super(pageName);
 		for(String p: AfpModel.sitePropertiesName) {
 			uniqueSitePropertyValues.put(p, new HashSet<Object>());
 		}
 		for(String p: AfpModel.sectorPropertiesName) {
 			uniqueSectorPropertyValues.put(p, new HashSet<Object>());
 		}
 		
 		for(String p: AfpModel.trxPropertiesName) {
 			uniqueTrxPropertyValues.put(p, new HashSet<Object>());
 		}
 
 	}
 	
 	protected AfpWizardPage(String pageName, AfpModel model) {
 		super(pageName);
 		this.model = model;
 		for(String p: AfpModel.sitePropertiesName) {
 			uniqueSitePropertyValues.put(p, new HashSet<Object>());
 		}
 		for(String p: AfpModel.sectorPropertiesName) {
 			uniqueSectorPropertyValues.put(p, new HashSet<Object>());
 		}
 		
 		for(String p: AfpModel.trxPropertiesName) {
 			uniqueTrxPropertyValues.put(p, new HashSet<Object>());
 		}
 	}
 
 	@Override
 	public void createControl(Composite parent) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void refreshPage() {
 		if (this instanceof AfpSeparationRulesPage){
 			updateSectorFilterLabel(model.getTotalSectors(), model.getTotalSectors());
 			updateSiteFilterLabel(model.getTotalSites(), model.getTotalSites());
 		}
 		else if(this instanceof AfpSYHoppingMALsPage){
 //			updateTRXFilterLabel(0, model.getTotalRemainingMalTRX());
 		}
 		else{
 			updateTRXFilterLabel(model.getTotalTRX(),model.getTotalRemainingTRX());
 		}
 		
 	}
 	
 	public void updateTRXFilterLabel(int selected, int total){
 		filterInfoLabel.setText(String.format("Filter Status: %d Trxs selected out of %d", selected, total));
 		trxFilterGroup.layout();
 	}
 	
 	public void updateSectorFilterLabel(int selected, int total){
 		filterInfoLabel.setText(String.format("Filter Status: %d sectors selected out of %d", selected, total));
 		trxFilterGroup.layout();
 	}
 	
 	public void updateSiteFilterLabel(int selected, int total){
 		siteFilterInfoLabel.setText(String.format("Filter Status: %d sites selected out of %d", selected, total));
 		siteTrxFilterGroup.layout();
 	}
 	
 	protected Table addTRXFilterGroup(Group main, String[] headers, int emptyrows, boolean isSite, FilterListener listener, String[] noListenerHeaders){
 		final Shell parentShell = main.getShell();
 		this.listener = listener;
 		Arrays.sort(noListenerHeaders);
 		
 		parentShell.addMouseListener(new MouseListener(){
 
 			@Override
 			public void mouseDoubleClick(MouseEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void mouseDown(MouseEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void mouseUp(MouseEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 		});
 		
 		/** Create TRXs Filters Group */
     	Group trxFilterGroup = new Group(main, SWT.NONE);
     	trxFilterGroup.setLayout(new GridLayout(4, false));
     	trxFilterGroup.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false,1 ,2));
     	trxFilterGroup.setText("TRXs Filter");
     	
     	Label filterInfoLabel = new Label(trxFilterGroup, SWT.LEFT);
     	
     	if (isSite){
     		this.siteTrxFilterGroup = trxFilterGroup;
     		this.siteFilterInfoLabel = filterInfoLabel;
     	}
     	else {
     		this.trxFilterGroup = trxFilterGroup;
     		this.filterInfoLabel = filterInfoLabel;
     	}
     		
     	
     	Button loadButton = new Button(trxFilterGroup, SWT.RIGHT);
     	loadButton.setLayoutData(new GridData(GridData.END, GridData.BEGINNING, true, false, 1 , 1));
     	loadButton.setText("Load");
     	loadButton.setData(LOAD);
     	loadButton.setEnabled(false);
     	loadButton.addSelectionListener(this);
     	
     	Button clearButton = new Button(trxFilterGroup, SWT.RIGHT);
     	clearButton.setLayoutData(new GridData(GridData.END, GridData.BEGINNING, false, false, 1 , 1));
     	clearButton.setText("Clear");
     	clearButton.setData(CLEAR);
     	clearButton.addSelectionListener(this);
     	
     	assignButton = new Button(trxFilterGroup, SWT.RIGHT);
     	assignButton.setLayoutData(new GridData(GridData.END, GridData.BEGINNING, false, false, 1 , 1));
     	assignButton.setText("Assign");
     	assignButton.setData(ASSIGN);
     	assignButton.addSelectionListener(this);
     	
     	viewer = new TableViewer(trxFilterGroup, SWT.H_SCROLL | SWT.V_SCROLL);
     	Table filterTable = viewer.getTable();
     	filterTable.setHeaderVisible(true);
     	filterTable.setLinesVisible(true);
 //    	filter = new AfpTRXFilter();
 //    	viewer.addFilter(filter);
     	for (String item : headers) {
     		TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
   	      	TableColumn column = viewerColumn.getColumn();
   	      	column.setText(item);
   	      	column.setData(item);
   	      	column.setResizable(true);
   	      	if (Arrays.binarySearch(noListenerHeaders, item) < 0)
   	      		column.addListener(SWT.Selection, new ColumnFilterListener(parentShell));
   	    }
     	
     	
     	
 
 //		Table filterTable = new Table(trxFilterGroup, SWT.VIRTUAL | SWT.MULTI);
 //		filterTable.setHeaderVisible(true);
 		GridData tableGrid = new GridData(GridData.FILL, GridData.CENTER, true, true, 4 ,1);
 		filterTable.setLayoutData(tableGrid);
 		
 //	    for (String item : headers) {
 //	      TableColumn column = new TableColumn(filterTable, SWT.NONE);
 //	      column.setText(item);
 //	    }
 	    for (int i=0;i<emptyrows;i++) {
 	    	TableItem item = new TableItem(filterTable, SWT.NONE);
 	    	for (int j = 0; j < headers.length; j++){
     			item.setText(j, "");
 	    	}
 	    }
 	    for (int i = 0; i < headers.length; i++) {
 	    	filterTable.getColumn(i).pack();
 	    }
 	    return filterTable;
 
 	}
 
 	@Override
 	public void widgetDefaultSelected(SelectionEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void widgetSelected(SelectionEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	/**
 	 * 
 	 * @param colName
 	 * @return unique values for the column
 	 */
 	protected Object[] getColumnUniqueValues(String colName){
 		return null;
 	}
 	
 	class ColumnFilterListener implements Listener{
 		
 		Shell parentShell;
 		public ColumnFilterListener(final Shell subShell) {
 			super();
 			this.parentShell = subShell;
 		}
 		
 		@Override
 		public void handleEvent(Event event) {
 			
 			final ArrayList<String> selectedValues = new ArrayList<String>();
			final Shell subShell = new Shell(parentShell,SWT.PRIMARY_MODAL|SWT.RESIZE|SWT.DIALOG_TRIM);
 			subShell.setLayout(new GridLayout(2, false));
 			Point location = subShell.getDisplay().getCursorLocation();
 			
			subShell.setLocation(location.x,location.y);
 			
 			/*subShell.addMouseListener(new MouseAdapter(){
 
 				@Override
 				public void mouseDown(MouseEvent e) {
 					// TODO Auto-generated method stub
 					System.out.println("Yeah, I can listen it");
 					super.mouseDown(e);
 					System.out.println("Yeah, I can listen it");
 				}
 
 				@Override
 				public void mouseUp(MouseEvent e) {
 					// TODO Auto-generated method stub
 					System.out.println("Yeah, I can listen it");
 					super.mouseUp(e);
 					System.out.println("Yeah, I can listen it");
 				}
 				
 			}
 					
 					new MouseListener(){
 
 				@Override
 				public void mouseDoubleClick(MouseEvent e) {
 					// TODO Auto-generated method stub
 					
 				}
 
 				@Override
 				public void mouseDown(MouseEvent e) {
 					Point location = subShell.getDisplay().getCursorLocation();
 					Rectangle bounds = subShell.getBounds();
 					if (!(bounds.contains(location)))
 						subShell.dispose();
 				}
 
 				@Override
 				public void mouseUp(MouseEvent e) {
 					Point location = subShell.getDisplay().getCursorLocation();
 					Rectangle bounds = subShell.getBounds();
 					if (!(bounds.contains(location)))
 						subShell.dispose();
 					
 				}
 				
 			});*/
 //			subShell.setLayoutData(gridData);
 //			subShell.setSize(100, 200);
 //			subShell.setBounds(50, 50, 100, 200);
 			//subShell.setLocation(300, 200);
 //			subShell.setText("Filter");
 			
 			final String col = (String)event.widget.getData();
 			
 			Group filterGroup = new Group(subShell, SWT.NONE | SWT.SCROLL_PAGE);
 			filterGroup.setLayout(new GridLayout(2, false));
 			filterGroup.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false,2 ,1));
 			
 			Object[] values = getColumnUniqueValues(col);
 			
 			if(values == null) {
 				subShell.dispose();
 				return;
 			}
 			if(values.length == 0) {
 				subShell.dispose();
 				return;
 			}
 		    final Tree tree = new Tree(filterGroup, SWT.CHECK | SWT.BORDER|SWT.V_SCROLL);
 		    GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, false, 2, 1);
 		    gridData.heightHint = 200;
 		    tree.setLayoutData(gridData);
 		    
 	    	for (Object value : values){
 	    		TreeItem item = new TreeItem(tree, 0);
 		        item.setText(value.toString());
 	    	}
             Button applyButton = new Button(filterGroup, SWT.PUSH);
             applyButton.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, true, 1, 1));
             applyButton.setText("Apply");
             applyButton.addSelectionListener(new SelectionAdapter() {
 
                 @Override
                 public void widgetSelected(SelectionEvent e) {
 
                     for (TreeItem item : tree.getItems()) {
                         if (item.getChecked()) {
                             selectedValues.add(item.getText());
                         }
                     }
                     listener.onFilterSelected(col, selectedValues);
                     // filter.setEqualityText("900");
                     // viewer.refresh(true);
                     subShell.dispose();
                 }
 
             });
 	    	Button cacelButton = new Button(filterGroup, SWT.PUSH);
 	    	cacelButton.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true, 1, 1));
 	    	cacelButton.setText("Cancel");
 	    	cacelButton.addSelectionListener(new SelectionAdapter(){
 				
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					subShell.dispose();
 				}
 				
 			});
 
 
 			
 			
 			subShell.pack();
 			subShell.open();
 			
 		}//end handle event
 		
 	}
 	
 	protected void addSiteUniqueProperties(Node node) {
 		// add to unique properties
 		for(String p:AfpModel.sitePropertiesName ) {
 			Object oVal = node.getProperty(p,null);
 
 			if(oVal != null) {
 				Set<Object> s =  uniqueSitePropertyValues.get(p);
 				if(s != null) {
 					s.add(oVal);
 				}
 			}				
 		}
 
 	}
 	protected void addSectorUniqueProperties(Node node) {
 		// add to unique properties
 		for(String p:AfpModel.sectorPropertiesName ) {
 			Object oVal = node.getProperty(p,null);
 
 			if(oVal != null) {
 				Set<Object> s = uniqueSectorPropertyValues.get(p);
 				if(s != null) {
 					s.add(oVal);
 				}
 			}				
 		}
 	}
 	
 	protected void addTrxUniqueProperties(Node node) {
 		// add to unique properties
 		for(String p:AfpModel.trxPropertiesName ) {
 			Object oVal = node.getProperty(p,null);
 
 			if(oVal != null) {
 				Set<Object> s= uniqueTrxPropertyValues.get(p);
 				if(s != null) {
 					s.add(oVal);
 				}
 			}				
 		}
 	}
 	
 	public Object[] getSiteUniqueValuesForProperty(String prop) {
 		Set<Object> s = this.uniqueSitePropertyValues.get(prop);
 		
 		if(s!= null) {
 			if(s.size() >0) {
 				return s.toArray(new Object[0]);
 			}
 		}
 		return null;
 	}
 	public Object[] getSectorUniqueValuesForProperty(String prop) {
 		Set<Object> s = this.uniqueSectorPropertyValues.get(prop);
 		
 		if(s!= null) {
 			if(s.size() >0) {
 				return s.toArray(new Object[0]);
 			}
 		}
 		return null;
 	}
 	public Object[] getTrxUniqueValuesForProperty(String prop) {
 		Set<Object> s = this.uniqueTrxPropertyValues.get(prop);
 		
 		if(s!= null) {
 			if(s.size() >0) {
 				return s.toArray(new Object[0]);
 			}
 		}
 		return null;
 	}
 
 	protected void clearAllUniqueValuesForProperty() {
 		for(Set s: this.uniqueSectorPropertyValues.values()) {
 			s.clear();
 		}
 		for(Set s: this.uniqueSitePropertyValues.values()) {
 			s.clear();
 		}
 		for(Set s: this.uniqueTrxPropertyValues.values()) {
 			s.clear();
 		}
 	}
 }
