 package com.scholastic.sbam.client.uiobjects.uireports;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.SortedSet;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import com.extjs.gxt.charts.client.Chart;
 import com.extjs.gxt.charts.client.model.BarDataProvider;
 import com.extjs.gxt.charts.client.model.ChartModel;
 import com.extjs.gxt.charts.client.model.Legend;
 import com.extjs.gxt.charts.client.model.Legend.Position;
 import com.extjs.gxt.charts.client.model.PieDataProvider;
 import com.extjs.gxt.charts.client.model.ScaleProvider;
 import com.extjs.gxt.charts.client.model.Text;
 import com.extjs.gxt.charts.client.model.axis.XAxis;
 import com.extjs.gxt.charts.client.model.charts.BarChart;
 import com.extjs.gxt.charts.client.model.charts.BarChart.BarStyle;
 import com.extjs.gxt.charts.client.model.charts.ChartConfig;
 import com.extjs.gxt.charts.client.model.charts.CylinderBarChart.Bar;
 import com.extjs.gxt.charts.client.model.charts.PieChart.Slice;
 import com.extjs.gxt.charts.client.model.charts.PieChart;
 import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
 import com.extjs.gxt.ui.client.data.BaseFilterPagingLoadConfig;
 import com.extjs.gxt.ui.client.data.BaseModelData;
 import com.extjs.gxt.ui.client.data.BasePagingLoader;
 import com.extjs.gxt.ui.client.data.BeanModel;
 import com.extjs.gxt.ui.client.data.BeanModelReader;
 import com.extjs.gxt.ui.client.data.ModelData;
 import com.extjs.gxt.ui.client.data.PagingLoadConfig;
 import com.extjs.gxt.ui.client.data.PagingLoadResult;
 import com.extjs.gxt.ui.client.data.PagingLoader;
 import com.extjs.gxt.ui.client.data.RpcProxy;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.ComponentEvent;
 import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
 import com.extjs.gxt.ui.client.event.SelectionChangedListener;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.Html;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.button.ButtonBar;
 import com.extjs.gxt.ui.client.widget.button.ToggleButton;
 import com.extjs.gxt.ui.client.widget.button.ToolButton;
 import com.extjs.gxt.ui.client.widget.grid.Grid;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.extjs.gxt.ui.client.widget.layout.MarginData;
 import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
 import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
 import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.scholastic.sbam.client.services.SnapshotParameterSetGetService;
 import com.scholastic.sbam.client.services.SnapshotParameterSetGetServiceAsync;
 import com.scholastic.sbam.client.services.SnapshotTakeService;
 import com.scholastic.sbam.client.services.SnapshotTakeServiceAsync;
 import com.scholastic.sbam.client.services.SnapshotTermDataListService;
 import com.scholastic.sbam.client.services.SnapshotTermDataListServiceAsync;
 import com.scholastic.sbam.client.uiobjects.fields.EnhancedComboBox;
 import com.scholastic.sbam.client.uiobjects.foundation.FieldFactory;
 import com.scholastic.sbam.client.util.IconSupplier;
 import com.scholastic.sbam.client.util.UiConstants;
 import com.scholastic.sbam.shared.exceptions.ServiceNotReadyException;
 import com.scholastic.sbam.shared.objects.SnapshotParameterSetInstance;
 import com.scholastic.sbam.shared.objects.SnapshotParameterValueObject;
 import com.scholastic.sbam.shared.objects.SnapshotTermDataInstance;
 import com.scholastic.sbam.shared.objects.SnapshotInstance;
 import com.scholastic.sbam.shared.objects.SynchronizedPagingLoadResult;
 import com.scholastic.sbam.shared.reporting.SnapshotParameterNames;
 
 public class TermReportChartCard extends SnapshotCardBase {
 	
 	public static final int		PIE_CHART		=	0;
 	public static final int		BAR_CHART		=	1;
 	
 	public static final String chartTitleStyle	= "font-size: 20px; font-family: Verdana; text-align: center; color: saddlebrown;";
 	public static final String [] chartColors	=	{ "#ff0000", "#00aa00", "#0000ff", "#ff9900", "#ff00ff" };
 	
 	
 	protected int														chartType				=	PIE_CHART;
 	protected SnapshotParameterSetInstance								snapshotParameterSet	=	null;
 	
 	protected long														searchSyncId;
 	protected PagingLoader<PagingLoadResult<SnapshotTermDataInstance>>	termDataLoader;
 
 	protected ContentPanel								contentPanel	=	 getNewContentPanel();
 	
 	protected Chart										chart;
 	
 	protected boolean									localStore;
 	protected ListStore<BeanModel>						store;
 	protected Grid<BeanModel>							sourceGrid;
 	protected ListStore<BeanModel>						sourceStore;
 	
 	protected HashMap<String, String>					groupLabelMap		=	getGroupLabelMap();
 	protected HashMap<String, String>					propertyHeaderMap	=	getPropertyHeaderMap();
 	
 	protected String									valueLabel			=	"Dollar Values";
 	protected String									groupLabel			=	"Product";
 	
 	protected String									groupCol			=	"productCode";
 	protected String									labelCol			=	"product.description";
 	protected String									valueCol			=	"dollarValue";
 	protected String									textCol				=	"product.description";
 	
 	protected ToggleButton								pieButton;
 	protected ToggleButton								barButton;
 	protected EnhancedComboBox<ModelData>				valueCombo;
 	protected EnhancedComboBox<ModelData>				groupCombo;
 	
 	protected final SnapshotTakeServiceAsync 			takeSnapshotService					= GWT.create(SnapshotTakeService.class);
 	protected final SnapshotTermDataListServiceAsync 	snapshotTermDataListService 		= GWT.create(SnapshotTermDataListService.class);
 	protected final SnapshotParameterSetGetServiceAsync	snapshotParameterSetGetService		= GWT.create(SnapshotParameterSetGetService.class);
 	
 	public TermReportChartCard() {
 		super();
 		this.headingToolTip = "Use this panel to view a terms based chart.";
 	}
 
 	@Override
 	public void addPanelContent() {
 //		contentPanel = new ContentPanel();
 		contentPanel.setLayout(new FitLayout());
 //		contentPanel.setHeading("Snapshot Terms Chart View");
 		IconSupplier.setIcon(contentPanel, IconSupplier.getPieChartIconName());
 		
 		if (store == null) 
 			store = getNewStore();
 		
 		if (chart == null)
 			createChart();
 		
 		contentPanel.getHeader().addTool(getParametersTool());
 		
 		contentPanel.setTopComponent(getButtonsBar());
 	    
 	    contentPanel.add(chart, new MarginData(20)); 
 		
 		add(contentPanel);
 		
 		if (snapshot != null  && localStore)
 			store.getLoader().load();
 	}
 	
 	public ToolButton getParametersTool() {
 		ToolButton parametersTool = new ToolButton("x-tool-help") {
 			@Override
 			protected void onClick(ComponentEvent ce) {
 				if (contentPanel.getHeader().getToolTip() != null)
 					contentPanel.getHeader().getToolTip().show();
 			}
 		};
 		parametersTool.enable();
 		return parametersTool; 
 	}
 	
 	public void createChart() {
 		chart = new Chart("ExtGWT/chart/open-flash-chart.swf");
 		
 		setChartModel();
 	}
 	
 	public void setChartModel () {	
 		switch (chartType) {
 			case	BAR_CHART:	setNewBarChartModel();
 								break;
 			case	PIE_CHART:	setNewPieChartModel();
 								break;
 			default:			setNewPieChartModel();
 		}
 	}
 	
 	public void setNewBarChartModel() {
 		      
 		ChartModel model = new ChartModel(valueLabel + " by " + groupLabel, chartTitleStyle);  
 	    model.setBackgroundColour("#fefaee");  
 	    model.setLegend(new Legend(Position.TOP, true));  
 	    model.setScaleProvider(ScaleProvider.ROUNDED_NEAREST_SCALE_PROVIDER);  
 	  
 //	    BarChart bar = new BarChart(BarStyle.GLASS);  
 //	    bar.setColour("#ff0000");
 //	    bar.setAnimateOnShow(true);
 //	    BarDataProvider barProvider = new BarDataProvider("dollarValue", "productCode");  
 //	    barProvider.bind(gridStore);  
 //	    bar.setDataProvider(barProvider);
 //	    model.addChartConfig(bar);
 	    
 	    BarChart bar = new BarChart(BarStyle.GLASS);
 //	    bar.setColours(chartColors);
 	    BarDataProvider barProvider = new BarDataProvider(valueCol, groupCol) {
 	    	  @Override
 	    	  public void populateData(ChartConfig config) {
 	    	    BarChart chart = (BarChart) config;
 	    	    chart.getValues().clear();
 	    	    
 	    	    TreeMap<String, Number> valueMap	= new TreeMap<String, Number>();
 	    	    TreeMap<String, String> textMap		= new TreeMap<String, String>();
 
 	    	    XAxis xAxis = null;
 	    	    if (labelProperty != null) { // || labelProvider != null) {
 	    	      xAxis = chart.getModel().getXAxis();
 	    	      if (xAxis == null) {
 	    	        xAxis = new XAxis();
 	    	        chart.getModel().setXAxis(xAxis);
 	    	      }
 	    	      xAxis.getLabels().getLabels().clear();
 	    	    }
 
 	    	    for (ModelData m : store.getModels()) {
 		    	     Number n = (valueCol == null || valueCol.equals("count")) ? 1 : getValue(m);
 	    	      String label = getLabel(m);
 	    	      String text  = getText(m);
 	    	      if (text == null || text.length() == 0)
 	    	    	  text = label;
 	    	      textMap.put(label, text);
 	    	      if (valueMap.containsKey(label))
 	    	    	  valueMap.put(label, new Double(valueMap.get(label).doubleValue() + n.doubleValue()));
 	    	      else
 	    	    	  valueMap.put(label, n);
 	    	    }
 
 	    	    int color = 0;
 	    	    List<Bar> bars = new ArrayList<Bar>();
 	    	    for (String label : valueMap.keySet()) {
 	    	    	Bar oneBar = new Bar(valueMap.get(label), 0, chartColors [color], chartColors [color]);
 	    	    	bars.add(oneBar);
 	    	    	color = (color + 1) % chartColors.length;
 
 	    	        minYValue = Math.min(minYValue, valueMap.get(label).doubleValue());
 	    	        maxYValue = Math.max(maxYValue, valueMap.get(label).doubleValue());
 	    	        
 					if (xAxis != null) {
						xAxis.addLabels(textMap.get(label));
 					}
 	    	    	
 //	    	        chart.addBars(oneBar);
 	    	    }
 	    	    chart.addBars(bars.toArray(new Bar [] {}));
 	    	  }
 	    };
 	    barProvider.setTextProperty(labelCol);
 	    barProvider.bind(store);
 	    bar.setDataProvider(barProvider);
 	    model.addChartConfig(bar);
 	      
 	    chart.setChartModel(model);
 		
 	}
 	
 	public void setNewPieChartModel() {
 		      
 		ChartModel model = new ChartModel(valueLabel + " by " + groupLabel, chartTitleStyle);  
 	    model.setBackgroundColour("#fefaee");  
 	    model.setLegend(new Legend(Position.TOP, true));  
 	    model.setScaleProvider(ScaleProvider.ROUNDED_NEAREST_SCALE_PROVIDER);  
 	  
 //	    BarChart bar = new BarChart(BarStyle.GLASS);  
 //	    bar.setColour("#ff0000");
 //	    bar.setAnimateOnShow(true);
 //	    BarDataProvider barProvider = new BarDataProvider("dollarValue", "productCode");  
 //	    barProvider.bind(gridStore);  
 //	    bar.setDataProvider(barProvider);
 //	    model.addChartConfig(bar);
 	    
 	    PieChart pie = new PieChart();
 	    pie.setColours(chartColors);
 	    PieDataProvider pieProvider = new PieDataProvider(valueCol, groupCol) {
 	    	  @Override
 	    	  public void populateData(ChartConfig config) {
 	    	    PieChart chart = (PieChart) config;
 	    	    chart.getValues().clear();
 	    	    
 	    	    TreeMap<String, Number> valueMap	= new TreeMap<String, Number>();
 	    	    TreeMap<String, String> textMap		= new TreeMap<String, String>();
 
 	    	    for (ModelData m : store.getModels()) {
 	    	      Number n = (valueCol == null || valueCol.equals("count")) ? 1 : getValue(m);
 	    	      String label = getLabel(m);
 	    	      String text  = getText(m);
 	    	      if (text == null || text.length() == 0)
 	    	    	  text = label;
 	    	      textMap.put(label, text);
 	    	      if (valueMap.containsKey(label))
 	    	    	  valueMap.put(label, new Double(valueMap.get(label).doubleValue() + n.doubleValue()));
 	    	      else
 	    	    	  valueMap.put(label, n);
 	    	    }
 
 	    	    for (String label : valueMap.keySet()) {
 	    	        chart.addSlices(new Slice(valueMap.get(label), label, textMap.get(label)));
 	    	    }
 	    	  }
 	    };
 	    pieProvider.setTextProperty(labelCol);
 	    pieProvider.bind(store);
 	    pie.setDataProvider(pieProvider);
 	    model.addChartConfig(pie);
 	      
 	    chart.setChartModel(model);
 		
 	}
 	
 	public ListStore<ModelData> getValueComboStore() {
 		ListStore<ModelData> valueStore = new ListStore<ModelData>();
 		
 		ModelData model;
 		
 		model = new BaseModelData();
 		model.set("value", "dollarValue");
 		model.set("description", "Dollar Values");
 		valueStore.add(model);
 		
 		model = new BaseModelData();
 		model.set("value", "count");
 		model.set("description", "Services");
 		valueStore.add(model);
 		
 		return valueStore;
 	}
 	
 	public ListStore<ModelData> getGroupComboStore(Grid<BeanModel> sourceGrid) {
 		
 		if (sourceGrid.getColumnModel().getColumnCount() > 0) {
 			ListStore<ModelData> groupStore = new ListStore<ModelData>();
 			ModelData model;
 			for (int i = 0; i < sourceGrid.getColumnModel().getColumnCount(); i++) {
 				model = new BaseModelData();
 				model.set("value", sourceGrid.getColumnModel().getColumn(i).getId());
 				model.set("description", sourceGrid.getColumnModel().getColumn(i).getHeader());
 				groupStore.add(model);
 			}
 			return groupStore;
 		} else
 			return getGroupComboStore();
 		
 	}
 	
 	public ListStore<ModelData> getGroupComboStore(ListStore<BeanModel> sourceStore) {
 
 		if (sourceGrid != null && groupCombo != null)
 			return groupCombo.getStore();
 		
 		if (sourceStore.getCount() > 0) {
 
 			ListStore<ModelData> groupStore = new ListStore<ModelData>();
 			ModelData model;
 			
 			SortedSet<String> properties = new TreeSet<String>();
 			for (String property : sourceStore.getAt(0).getPropertyNames())
 				properties.add(property);
 			
 			for (String property : properties) {
 				model = new BaseModelData();
 				model.set("value", property);
 				model.set("description", getPropertyHeader(property));
 				groupStore.add(model);
 			}
 			
 			return groupStore;
 			
 		} else
 			return getGroupComboStore();
 		
 	}
 	
 	public ListStore<ModelData> getGroupComboStore() {
 		
 		if (sourceGrid != null)
 			return getGroupComboStore(sourceGrid);
 		if (sourceStore != null)
 			return getGroupComboStore(sourceStore);
 		
 		ListStore<ModelData> groupStore = new ListStore<ModelData>();
 		
 		ModelData model;
 		
 		model = new BaseModelData();
 		model.set("value", "productCode");
 		model.set("description", "Product");
 		groupStore.add(model);
 		
 		model = new BaseModelData();
 		model.set("value", "serviceCode");
 		model.set("description", "Service");
 		groupStore.add(model);
 
 		
 		model = new BaseModelData();
 		model.set("value", "institution.institutionName");
 		model.set("description", "Institution");
 		groupStore.add(model);
 		
 		model = new BaseModelData();
 		model.set("value", "institution.state");
 		model.set("description", "State");
 		groupStore.add(model);
 		
 		return groupStore;
 	}
 	
 	public HashMap<String, String> getGroupLabelMap() {
 		HashMap<String, String> groupLabelMap = new HashMap<String, String>();
 		
 		groupLabelMap.put("productCode", "product.description");
 		groupLabelMap.put("ucn", "institution.institutionName");
 		
 		return groupLabelMap;
 	}
 	
 	public String getPropertyHeader(String property) {
 		if (propertyHeaderMap.containsKey(property))
 			return propertyHeaderMap.get(property);
 		return property;
 	}
 	
 	public HashMap<String, String> getPropertyHeaderMap() {
 		HashMap<String, String> propertyHeaderNap = new HashMap<String, String>();
 		
 		propertyHeaderNap.put("productCode", "Product");
 		propertyHeaderNap.put("institution.institutionName", "Institution");
 		
 		return propertyHeaderNap;
 	}
 	
 	/**
 	 * Set up and get a button bar with buttons to expand or collapse the tree.
 	 * @return
 	 */
 	public ToolBar getButtonsBar() {
 		ButtonBar toolbar = new ButtonBar();
 		toolbar.setAlignment(HorizontalAlignment.CENTER);
 		
 		pieButton = new ToggleButton("Pie Chart");
 		pieButton.setToggleGroup("chartType");
 		IconSupplier.forceIcon(pieButton, IconSupplier.getPieChartIconName());
 		pieButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
 			   
 			@Override
 			public void componentSelected(ButtonEvent ce) {
 				chartType = PIE_CHART;
 				redoChart();
 			}  
 		 
 		});
 		
 		barButton = new ToggleButton("Bar Graph");
 		barButton.setToggleGroup("chartType");
 		IconSupplier.forceIcon(barButton, IconSupplier.getBarChartIconName());
 		barButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
 			   
 			@Override
 			public void componentSelected(ButtonEvent ce) {
 				chartType = BAR_CHART;
 				redoChart();
 			}  
 		 
 		});
 		
 		if (chartType == BAR_CHART)
 			barButton.toggle(true);
 		else
 			pieButton.toggle(true);
 		
 		valueCombo = FieldFactory.getModelDataComboField("valueBox", "", 100, "Select the value to chart.", getValueComboStore(), "value", "description");
 		valueCombo.enable();
 		setValueCombo();
 		valueCombo.addSelectionChangedListener(new SelectionChangedListener<ModelData>() {
 
 			@Override
 			public void selectionChanged(SelectionChangedEvent<ModelData> se) {
 				if (se.getSelectedItem() != null && se.getSelectedItem().get("value") != null) {
 					valueCol = se.getSelectedItem().get("value").toString();
 					valueLabel = se.getSelectedItem().get("description").toString();
 					redoChart();
 				}
 			}
 
 		});
 		
 		groupCombo = FieldFactory.getModelDataComboField("valueBox", "", 100, "Select the value to chart.", getGroupComboStore(), "value", "description");
 		groupCombo.enable();
 		setGroupCombo();
 		groupCombo.addSelectionChangedListener(new SelectionChangedListener<ModelData>() {
 
 			@Override
 			public void selectionChanged(SelectionChangedEvent<ModelData> se) {
 				if (se.getSelectedItem() != null && se.getSelectedItem().get("value") != null) {
 					groupCol = se.getSelectedItem().get("value").toString();
 					groupLabel = se.getSelectedItem().get("description").toString();
 					labelCol = groupCol;
 					textCol = groupCol;
 					redoChart();
 				}
 			}
 
 		});
 		
 		toolbar.add(pieButton);
 		toolbar.add(barButton);
 		toolbar.add(new SeparatorToolItem());
 		toolbar.add(new Html("Chart"));
 		toolbar.add(valueCombo);
 		toolbar.add(new Html("By"));
 		toolbar.add(groupCombo);
 		
 		return toolbar;
 	}
 	
 	public ContentPanel getNewContentPanel() {
 		ContentPanel contentPanel = new ContentPanel();
 		contentPanel.setLayout(new FitLayout());
 		contentPanel.setHeading(getPanelTitle());
 		IconSupplier.setIcon(contentPanel, IconSupplier.getReportIconName());
 		return contentPanel;
 	}
 	
 	public ListStore<BeanModel> getNewStore() {
 		termDataLoader = getTermDataLoader();
 
 		localStore = true;
 		ListStore<BeanModel> store = new ListStore<BeanModel>(termDataLoader);
 		
 		return store;
 	}
 	
 	@Override
 	public ContentPanel getContentPanel() {
 		return contentPanel;
 	}
 	
 	@Override
 	public void setSnapshot(SnapshotInstance snapshot) {
 		super.setSnapshot(snapshot);
 		loadSnapshotParameters();
 		if (snapshot != null) {
 			if (snapshot.getSnapshotTaken() == null) {
 				takeSnapshot();
 			} else {
 				if (store != null && localStore)
 					store.getLoader().load();
 			}
 		}
 	}
 	
 	protected void takeSnapshot() {
 		if (snapshot.getSnapshotTaken() != null)
 			return;
 		
 		contentPanel.mask("Please wait while the snapshot is compiled.");
 		
 		takeSnapshotService.takeSnapshot(snapshot.getSnapshotId(),
 				new AsyncCallback<SnapshotInstance>() {
 					public void onFailure(Throwable caught) {
 						// Show the RPC error message to the user
 						if (caught instanceof IllegalArgumentException)
 							MessageBox.alert("Alert", caught.getMessage(), null);
 						else {
 							MessageBox.alert("Alert", "Take snapshot failed unexpectedly.", null);
 							System.out.println(caught.getClass().getName());
 							System.out.println(caught.getMessage());
 						}
 						contentPanel.unmask();
 					}
 
 					public void onSuccess(SnapshotInstance result) {
 						snapshot.setSnapshotTaken(result.getSnapshotTaken());
 						snapshot.setSnapshotRows(result.getSnapshotRows());
 						snapshot.setExcelFilename(result.getExcelFilename());
 						if (parentCardPanel != null) {
 							parentCardPanel.reflectSnapshotChanges(snapshot);
 						}
 						contentPanel.unmask();
 						if (store != null)
 							store.getLoader().load();
 					}
 				});
 	}
 	
 	/**
 	 * Construct and return a loader to handle returning a list of institutions.
 	 * @return
 	 */
 	protected PagingLoader<PagingLoadResult<SnapshotTermDataInstance>> getTermDataLoader() {
 		// proxy and reader  
 		RpcProxy<PagingLoadResult<SnapshotTermDataInstance>> proxy = new RpcProxy<PagingLoadResult<SnapshotTermDataInstance>>() {  
 			@Override  
 			public void load(Object loadConfig, final AsyncCallback<PagingLoadResult<SnapshotTermDataInstance>> callback) {
 		    	
 				// This could be as simple as calling userListService.getUsers and passing the callback
 				// Instead, here the callback is overridden so that it can catch errors and alert the users.  Then the original callback is told of the failure.
 				// On success, the original callback is just passed the onSuccess message, and the response (the list).
 				
 				AsyncCallback<SynchronizedPagingLoadResult<SnapshotTermDataInstance>> myCallback = new AsyncCallback<SynchronizedPagingLoadResult<SnapshotTermDataInstance>>() {
 					public void onFailure(Throwable caught) {
 						// Show the RPC error message to the user
 						if (caught instanceof IllegalArgumentException)
 							MessageBox.alert("Alert", caught.getMessage(), null);
 						else if (caught instanceof ServiceNotReadyException)
 								MessageBox.alert("Alert", "The " + caught.getMessage() + " is not available at this time.  Please try again in a few minutes.", null);
 						else {
 							System.out.println(caught.getClass().getName());
 							System.out.println(caught.getMessage());
 							MessageBox.alert("Alert", "Snapshot data load failed unexpectedly.", null);
 						}
 						callback.onFailure(caught);
 					}
 
 					public void onSuccess(SynchronizedPagingLoadResult<SnapshotTermDataInstance> syncResult) {
 						if (syncResult.getSyncId() != searchSyncId)
 							return;
 						
 						PagingLoadResult<SnapshotTermDataInstance> result = syncResult.getResult();
 
 						callback.onSuccess(result);
 
 						contentPanel.unmask();
 					}
 				};
 				
 				if (contentPanel.isMasked()) contentPanel.unmask();	//	To put our own mask message up
 				if (!contentPanel.isMasked()) contentPanel.mask("Loading snapshot data... Please wait...");		// Required because GXT forgets to do this when a remote sort is initiated through the columns
 				searchSyncId = System.currentTimeMillis();
 				invokeSnapshotTermDataListService((PagingLoadConfig) loadConfig, snapshot.getSnapshotId(), searchSyncId, myCallback);
 		    }  
 		};
 		BeanModelReader reader = new BeanModelReader();
 		
 		// loader and store  
 		BasePagingLoader<PagingLoadResult<SnapshotTermDataInstance>> loader = new BasePagingLoader<PagingLoadResult<SnapshotTermDataInstance>>(proxy, reader) {
 			@Override
 			  protected Object newLoadConfig() {
 				return new BaseFilterPagingLoadConfig();
 			}
 		};
 		loader.setReuseLoadConfig(false);
 		return loader;
 	}
 	
 	public void invokeSnapshotTermDataListService(PagingLoadConfig loadConfig, int snapshotId, long searchSyncId, AsyncCallback<SynchronizedPagingLoadResult<SnapshotTermDataInstance>> myCallback) {
 		snapshotTermDataListService.getSnapshotTermData((PagingLoadConfig) loadConfig, snapshotId, searchSyncId, myCallback);
 	}
 
 	protected void loadSnapshotParameters() {
 		setSnapshotParameterSet(null);
 		snapshotParameterSetGetService.getSnapshotParameterSet(snapshot.getSnapshotId(), null,	//	null for all sources
 				new AsyncCallback<SnapshotParameterSetInstance>() {
 					public void onFailure(Throwable caught) {
 						// Show the RPC error message to the user
 						if (caught instanceof IllegalArgumentException)
 							MessageBox.alert("Alert", caught.getMessage(), null);
 						else {
 							MessageBox.alert("Alert", "Snapshot parameter load failed unexpectedly.", null);
 							System.out.println(caught.getClass().getName());
 							System.out.println(caught.getMessage());
 						}
 					}
 
 					public void onSuccess(SnapshotParameterSetInstance snapshotParameterSet) {
 						setSnapshotParameterSet(snapshotParameterSet);
 					}
 			});
 	}
 	
 	protected void setSnapshotParameterSet(SnapshotParameterSetInstance snapshotParameterSet) {
 		this.snapshotParameterSet = snapshotParameterSet;
 		formatToolTip();
 	}
 	
 	/**
 	 * Format the contents of the tooltip.
 	 * 
 	 * Note that this function must wait for the states to load (if they are needed) before continuing.  This is done with a timer.
 	 */
 	protected void formatToolTip() {
 		if (snapshotParameterSet != null && snapshotParameterSet.getValues().containsKey(SnapshotParameterNames.INSTITUTION_STATE) && !UiConstants.areInstitutionStatesLoaded()) {
 			UiConstants.loadInstitutionStates();
 			Timer timer = new Timer() {
 				@Override
 				public void run() {
 					if (UiConstants.areInstitutionStatesLoaded()) {
 						this.cancel();
 						formatToolTipNoWait();
 					}
 				}
 			};
 			timer.scheduleRepeating(200);
 		} else {
 			formatToolTipNoWait();
 		}
 	}
 		
 	protected void formatToolTipNoWait() {
 		StringBuffer sb = new StringBuffer();
 		
 		buildParameterBuffer(sb);
 
 		if (getContentPanel().getHeader().getToolTip() == null
 		||  getContentPanel().getHeader().getToolTip().getToolTipConfig() == null) {
 			ToolTipConfig toolTip = new ToolTipConfig(sb.toString());
 			toolTip.setCloseable(true);
 			toolTip.setShowDelay(60000);
 			getContentPanel().getHeader().setToolTip(toolTip);
 		} else {
 			getContentPanel().getHeader().setToolTip(sb.toString());
 		}
 	}
 	
 	protected void buildParameterBuffer(StringBuffer sb) {
 		if (snapshot == null) {
 			sb.append("No snapshot.");
 			sb.append("<br />");
 			sb.append("<br />");
 		} else {
 			sb.append("Snapshot <span class=\"sbam-report-title\">#");
 			sb.append(snapshot.getSnapshotId());
 			sb.append(" : ");
 			sb.append(snapshot.getSnapshotName());
 			sb.append("</span>");
 			sb.append("<br />");
 			sb.append("<br />");
 		}
 		if (snapshotParameterSet == null) {
 			sb.append("No parameters.");
 		} else {
 			sb.append("<table class=\"sbam-report-parms\">");
 			
 			appendParameters(sb);
 			
 			sb.append("</table>");
 		}
 	}
 	
 	protected void appendParameters(StringBuffer sb) {
 			for (String parameterName : snapshotParameterSet.getValues().keySet()) {
 				List<SnapshotParameterValueObject> values = snapshotParameterSet.getValues(parameterName);
 				if (values != null && values.size() > 0) {
 					sb.append("<tr><td class=\"sbam-report-parm\">");
 					sb.append(SnapshotParameterNames.getLabel(parameterName));
 					sb.append("</td><td class=\"sbam-report-value\">");
 					int count = 0;
 					for (SnapshotParameterValueObject value : values) {
 						if (count > 0)
 							sb.append(", ");
 						sb.append(getTranslatedValue(parameterName, value.toString()));
 						count++;
 					}
 				}
 				sb.append("</td></tr>");
 			}
 	}
 	
 	protected String getTranslatedValue(String name, String value) {
 		if (name.equals(SnapshotParameterNames.UCN_TYPE)) {
 			return snapshot.getUcnTypeDescription();
 		} else if (name.equals(SnapshotParameterNames.PRODUCT_SERVICE_TYPE)) {
 			return snapshot.getProductServiceDescription();
 		} else if (name.equals(SnapshotParameterNames.TERM_TYPES)) {
 			return getCodeDescription(value, UiConstants.getTermTypes());
 		} else if (name.equals(SnapshotParameterNames.INSTITUTION_STATE)) {
 			return getCodeDescription(value, UiConstants.getInstitutionStates());
 		} else if (name.equals(SnapshotParameterNames.PRODUCT_CODE)) {
 			return getCodeDescription(value, UiConstants.getProducts());
 		} else if (name.equals(SnapshotParameterNames.PROD_COMM_CODES)
 				|| name.equals(SnapshotParameterNames.AGREEMENT_COMM_CODES)
 				|| name.equals(SnapshotParameterNames.TERM_COMM_CODES)) {
 			return getCodeDescription(value, UiConstants.getCommissionTypes());
 		}
 		return value;
 	}
 	
 	protected String getCodeDescription(String value, ListStore<BeanModel> store) {
 		BeanModel model = store.findModel(value);
 		if (model != null && model.get("description") != null)
 			return model.get("description").toString();
 		return value;
 	}
 	
 	@Override
 	public String getPanelTitle() {
 		return "Snapshot Chart View";
 	}
 	
 	@Override
 	public boolean okayToReturn() {
 		if (store != null && localStore) {
 			store.removeAll();
 		}
 		return true;
 	}
 	
 	public void redoChart() {
 		if (chart == null) {
 			createChart();
 			return;
 		}
 		
 		setChartModel();
 		
 		chart.refresh();
 		chart.repaint();
 		
 //		for (ChartConfig chartConfig : chart.getChartModel().getChartConfigs()) {
 //			if (chartConfig instanceof PieChart) {
 //				redoChart( (PieChart) chartConfig);
 //			}
 //		}
 //		
 //		chart.refresh();
 	}
 	
 	public void redoChart(PieChart pieChart) {		
 		pieChart.getModel().setTitle(new Text(valueLabel + " by " + groupLabel, chartTitleStyle));
 		PieDataProvider provider = (PieDataProvider) pieChart.getDataProvider();
 		provider.setLabelProperty(groupCol);
 		provider.setValueProperty(valueCol);
 		provider.setTextProperty(labelCol);
 		provider.bind(store);
 		provider.populateData(pieChart);
 	}
 
 	public ListStore<BeanModel> getStore() {
 		return store;
 	}
 	
 	public void setChart(Grid<BeanModel> grid) {
 		if (grid.getStore().getSortField() != null && grid.getStore().getSortField().length() > 0) {
 			setChart(grid, grid.getStore().getSortField(), "dollarValue");
 		} else {
 			setChart(grid, "product.description", "dollarValue");
 		}
 	}
 	
 	public void setChart(Grid<BeanModel> grid, String groupCol, String valueCol) {
 		setGroupCol(groupCol, grid);
 		setTextCol(groupCol);
 		setValueCol(valueCol, grid);
 		setStore(grid.getStore());
 		
 		redoChart();
 	}
 
 	public void setStore(ListStore<BeanModel> store) {
 		localStore = false;
 		this.store = store;
 		setSourceStore(store);
 	}
 
 	public String getValueLabel() {
 		return valueLabel;
 	}
 
 	public void setValueLabel(String valueLabel) {
 		this.valueLabel = valueLabel;
 	}
 
 	public String getGroupLabel() {
 		return groupLabel;
 	}
 
 	public void setGroupLabel(String groupLabel) {
 		this.groupLabel = groupLabel;
 	}
 
 	public String getGroupCol() {
 		return groupCol;
 	}
 
 	public void setSourceStore(ListStore<BeanModel> store) {
 		sourceStore = store;
 		if (groupCombo != null) {
 			groupCombo.setStore(getGroupComboStore(store));
 			setGroupCombo();
 		}
 	}
 
 	public void setSourceGrid(Grid<BeanModel> grid) {
 		if(sourceGrid == null || sourceGrid != grid) {
 			sourceGrid = grid;
 			if (groupCombo != null)
 				groupCombo.setStore(getGroupComboStore(sourceGrid));
 			setGroupCombo();
 		}
 	}
 	
 	public void setGroupCol(String groupCol) {
 		this.groupCol = groupCol;
 		setLabelCol();
 		setGroupCombo();
 	}
 
 	public void setGroupCol(String groupCol, Grid<BeanModel> grid) {
 		setSourceGrid(grid);
 		
 		this.groupCol = groupCol;
 		if (grid.getColumnModel().getColumnById(groupCol) != null)
 			groupLabel = grid.getColumnModel().getColumnById(groupCol).getHeader();
 		else
 			groupLabel = groupCol;
 		setLabelCol();
 		setGroupCombo();
 	}
 	
 	public void setGroupCombo() {
 		if (groupCombo == null) {
 			return;
 		}
 		if (groupCol == null) {
 			groupCombo.select(null);
 			return;
 		}
 		for (int i = 0; i < groupCombo.getStore().getCount(); i++) {
 			if (groupCol.equals(groupCombo.getStore().getAt(i).get("value").toString())) {
 				groupCombo.select(i);
 				return;
 			}
 		}
 		
 		groupCombo.select(null);
 	}
 
 	public String getLabelCol() {
 		return labelCol;
 	}
 
 	public void setLabelCol(String labelCol) {
 		this.labelCol = labelCol;
 	}
 	
 	public void setLabelCol() {
 		if (groupCol == null)
 			setLabelCol("");
 		else if (groupLabelMap.containsKey(groupCol))
 			setLabelCol(groupLabelMap.get(groupCol));
 		else
 			setLabelCol(groupCol);
 	}
 
 	public String getValueCol() {
 		return valueCol;
 	}
 
 	public void setValueCol(String valueCol) {
 		this.valueCol = valueCol;
 		setValueCombo();
 	}
 
 	public void setValueCol(String valueCol, Grid<BeanModel> grid) {
 		setSourceGrid(grid);
 		
 		this.valueCol = valueCol;
 		if (grid.getColumnModel().getColumnById(valueCol) != null)
 			valueLabel = grid.getColumnModel().getColumnById(valueCol).getHeader();
 		else
 			valueLabel = valueCol;
 		setValueCombo();
 	}
 	
 	public void setValueCombo() {
 		if (valueCombo == null)
 			return;
 		if (valueCol == null) {
 			valueCombo.select(null);
 		}
 		for (int i = 0; i < valueCombo.getStore().getCount(); i++) {
 			if (valueCol.equals(valueCombo.getStore().getAt(i).get("value").toString())) {
 				valueCombo.select(i);
 				return;
 			}
 		}
 		valueCombo.select(null);
 	}
 
 	public String getTextCol() {
 		return textCol;
 	}
 
 	public void setTextCol(String textCol) {
 		this.textCol = textCol;
 	}
 
 	public int getChartType() {
 		return chartType;
 	}
 
 	public void setChartType(int chartType) {
 		this.chartType = chartType;
 		if (chartType == PIE_CHART && pieButton != null)
 			pieButton.toggle(true);
 		if (chartType == BAR_CHART && barButton != null)
 			barButton.toggle(true);
 		if (chart != null)
 			redoChart();			//	 Only do this now if we already have a chart
 	}
 	
 }
