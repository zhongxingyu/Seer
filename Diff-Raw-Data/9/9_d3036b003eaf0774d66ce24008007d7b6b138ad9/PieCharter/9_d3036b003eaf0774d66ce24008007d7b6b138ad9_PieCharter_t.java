 package org.obudget.client;
 
 import java.util.LinkedList;
 
 import org.apache.catalina.startup.Embedded;
 
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DecoratedPopupPanel;
 import com.google.gwt.user.client.ui.DecoratedTabPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.LayoutPanel;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.RadioButton;
 import com.google.gwt.user.client.ui.TabBar;
 import com.google.gwt.user.client.ui.TabLayoutPanel;
 import com.google.gwt.user.client.ui.ToggleButton;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.visualization.client.DataTable;
 import com.google.gwt.visualization.client.LegendPosition;
 import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
 import com.google.gwt.visualization.client.visualizations.PieChart;
 import com.google.gwt.visualization.client.visualizations.PieChart.Options;
 
 class PieCharter extends Composite {
 	private TabLayoutPanel mTabPanel;
 	private VerticalPanel mPanel;
 	private Application mApp;
 	private RadioButton mRadio2;
 	private ToggleButton mAllocatedButton;
 	private ToggleButton mRevisedButton;
 	private ToggleButton mUsedButton;
 	private ToggleButton mNetButton;
 	private LinkedList<BudgetLine> mList = null;
 	private boolean mEmbedded;
 
 	public PieCharter( Application app, boolean embedded ) {
 		mApp = app;
 		mTabPanel = new TabLayoutPanel(0,Unit.PX);
 		mTabPanel.setHeight("305px");
 		mTabPanel.setWidth("325px");
 		mTabPanel.setStylePrimaryName("obudget-piechart");	
 		
 		mPanel = new VerticalPanel();
 		mPanel.add(mTabPanel);
 		
 		HorizontalPanel hPanel = new HorizontalPanel();
 		hPanel.setStylePrimaryName("obudget-piechart-type");
 		mNetButton = new ToggleButton("נטו","ברוטו");
 		mNetButton.addClickHandler( new ClickHandler() {			
 			@Override
 			public void onClick(ClickEvent event) {
 				redrawChart();
 			}
 		});
 		mAllocatedButton = new ToggleButton("הקצאה");
 		mAllocatedButton.setDown(true);
 		mRevisedButton = new ToggleButton("הקצאה מעודכנת");
 		mUsedButton = new ToggleButton("שימוש");
 		mAllocatedButton.addClickHandler( new ClickHandler() {	
 			@Override
 			public void onClick(ClickEvent event) {
 				if ( mAllocatedButton.isDown() ) {					
 					mTabPanel.selectTab(0);
 					mRevisedButton.setDown(false);
 					mUsedButton.setDown(false);
 					Application.getInstance().stateChanged();
 				} else {
 					mAllocatedButton.setDown(true);
 				}
 				
 			}
 		});
 		mRevisedButton.addClickHandler( new ClickHandler() {	
 			@Override
 			public void onClick(ClickEvent event) {
 				if ( mRevisedButton.isDown() ) {
 					mTabPanel.selectTab(1);
 					mAllocatedButton.setDown(false);
 					mUsedButton.setDown(false);
 					Application.getInstance().stateChanged();
 				} else {
 					mRevisedButton.setDown(true);
 				}
 				
 			}
 		});
 		mUsedButton.addClickHandler( new ClickHandler() {	
 			@Override
 			public void onClick(ClickEvent event) {
 				if ( mUsedButton.isDown() ) {
 					mTabPanel.selectTab(2);
 					mAllocatedButton.setDown(false);
 					mRevisedButton.setDown(false);
 					Application.getInstance().stateChanged();
 				} else {
 					mUsedButton.setDown(true);
 				}
 				
 			}
 		});
 		
 		hPanel.add( mAllocatedButton );
 		hPanel.add( mRevisedButton );
 		hPanel.add( mUsedButton );
 		hPanel.add( mNetButton );
 		mPanel.add(hPanel);
 
 		mEmbedded = embedded;
 		HTML embedLabel = null;
 		if ( mEmbedded ) {
 			embedLabel = new HTML("מ<a target='_blank' href='http://"+Window.Location.getHost()+"'>אתר התקציב הפתוח</a>");
 			
 		} else {
 			embedLabel = new HTML("<span class='embed-link'>שיבוץ התרשים באתר אחר (embed)<span>");
 			String embedCode = "<iframe scrolling=&quot;no&quot; frameborder=&quot;0&quot; style=&quot;width: 390px; height: 350px&quot; " +
 							   		   "src=&quot;http://" + Window.Location.getHost() + "/embed_pie.html" + Window.Location.getHash() +
 							   		   "&quot;>" +
 							   "</iframe>";
 			
 			final DecoratedPopupPanel simplePopup = new DecoratedPopupPanel(true);
			HTML simplePopupContents = new HTML( "<b>קוד HTML לשיבוץ התרשים באתר אחר:</b><textarea rows='3' cols='40' style='direction: ltr;'>"+embedCode+"</textarea>");
 			simplePopup.setWidget( simplePopupContents );
 			embedLabel.addClickHandler( new ClickHandler() {			
 				@Override
 				public void onClick(ClickEvent event) {
 		            Widget source = (Widget) event.getSource();
 		            int left = source.getAbsoluteLeft() + 150;
 		            int top = source.getAbsoluteTop() - 100;
 		            simplePopup.setPopupPosition(left, top);
 		            simplePopup.show();				
 				}
 			});
 		}
 		mPanel.add( embedLabel );
 		
 		initWidget(mPanel);
 	}
 	
 	public void handleData( LinkedList<BudgetLine> list ) {
 		mList = list;
 		redrawChart();
 	}
 	
 	private void redrawChart() {
 	    mTabPanel.clear();
 
 	    // mList contains header row (which we disregard) and sub items 
 	    if ( mList == null ) { return; }
 
 	    Integer subitemCount = mList.size()-1;
 	    //if ( subitemCount < 1 ) { return; }
 	    	    
 	    Integer mActualRows = Math.min( subitemCount, 10 );
 	    boolean hasExtraRow = subitemCount > mActualRows; 
 
 		Application.getInstance().stateChanged();
 
 		Options	options = Options.create();
 		options.setWidth(340);
 		options.setHeight(305);
 		options.set3D(true);	
 		options.setLegend(LegendPosition.BOTTOM);
 		if ( mEmbedded ) {
 			if ( mList.size() > 0 ) {
 				options.setTitle( mList.getFirst().getCode() + " - " + mList.getFirst().getTitle() );
 			}
 		}
 
 	    DataTable[] data = new DataTable[] { DataTable.create(), DataTable.create(), DataTable.create() };
 	    for ( int t = 0 ; t < 3 ; t ++ ) { 
 		    data[t].addColumn(ColumnType.STRING, "Title");
 		    data[t].addColumn(ColumnType.NUMBER, "Allocated");
 		    if ( hasExtraRow ) {
 		    	data[t].addRows(mActualRows+1);
 		    } else {
 		    	data[t].addRows(mActualRows);		    	
 		    }
 		    
 		    for ( int i = 0 ; i < mActualRows ; i ++ ) {
 			    data[t].setValue(i, 0, mList.get(i+1).getTitle());
 		    }
 		    if ( hasExtraRow ) {
 			    data[t].setValue(mActualRows, 0, "אחרים");
 			    data[t].setValue(mActualRows, 1, 0);
 		    }
 	    }
 	    
 	    Boolean net = !mNetButton.isDown();
 	    for ( int i = 0 ; i < mList.size()-1 ; i ++ ) {
 	    	if ( ( mList.get(i+1).getOriginal( BudgetLine.ALLOCATED, net ) != null ) &&
 	    		 ( mList.get(i+1).getOriginal( BudgetLine.ALLOCATED, net ) > 0 ) ) {
 		    	
 	    		if ( i >= mActualRows && hasExtraRow ) {
 		    		data[0].setValue(mActualRows, 1, 
 		    						 data[0].getValueInt(mActualRows, 1) + 
 		    						 mList.get(i+1).getOriginal( BudgetLine.ALLOCATED, net ));		    		
 		    	} else {
 		    		data[0].setValue(i, 1, mList.get(i+1).getOriginal( BudgetLine.ALLOCATED, net ));
 		    	}
 	    	} else {
 	    		if ( i < mActualRows ) {
 	    			data[0].setValue(i, 1, 0 );
 	    		}
 	    	}
 	    	if ( (mList.get(i+1).getOriginal( BudgetLine.REVISED, net ) != null) && 
 	    		 (mList.get(i+1).getOriginal( BudgetLine.REVISED, net ) > 0) ) {
 		    	if ( i >= mActualRows && hasExtraRow ) {
 		    		data[1].setValue(mActualRows, 1, 
 		    						 data[1].getValueInt(mActualRows, 1) + 
 		    						 mList.get(i+1).getOriginal( BudgetLine.REVISED, net ));		    		
 		    	} else {
 		    		data[1].setValue(i, 1, mList.get(i+1).getOriginal( BudgetLine.REVISED, net ) );
 		    	}
 	    	} else {
 	    		if ( i < mActualRows ) {
 	    			data[1].setValue(i, 1, 0);
 	    		}
 	    	}
 	    	if ( (mList.get(i+1).getOriginal( BudgetLine.USED, net ) != null) &&
 	    		 (mList.get(i+1).getOriginal( BudgetLine.USED, net ) > 0) ) {
 		    	if ( i >= mActualRows && hasExtraRow ) {
 		    		data[2].setValue(mActualRows, 1, data[2].getValueInt(mActualRows, 1) + 
    						 mList.get(i+1).getOriginal( BudgetLine.USED, net ));		    		
 	    		} else {
 	    			data[2].setValue(i, 1, mList.get(i+1).getOriginal( BudgetLine.USED, net ) );
 	    		}
 	    	} else {
 	    		if ( i < mActualRows ) {
 	    			data[2].setValue(i, 1, 0);
 	    		}
 	    	}
 	    }
 
 	    PieChart piechartAllocated = new PieChart( data[0], options );
 	    PieChart piechartRevised = new PieChart( data[1], options );
 	    PieChart piechartUsed = new PieChart( data[2], options );
 
 		mTabPanel.setWidth("325px");   
 	    mTabPanel.add(piechartAllocated,"הקצאה");
 		mTabPanel.add(piechartRevised,"הקצאה מעודכנת");
 		mTabPanel.add(piechartUsed,"שימוש");
 	}
 
 	public void setState(Integer pieChartDataType, Integer pieChartNet) {
 		mNetButton.setDown( pieChartNet == 1 );
 		mAllocatedButton.setDown( pieChartDataType == 0 );
 		mRevisedButton.setDown( pieChartDataType == 1 );
 		mUsedButton.setDown( pieChartDataType == 2 );
 		redrawChart();
 	}
 
 	public String getState() {
 		return (mAllocatedButton.isDown() ? "0," : (mRevisedButton.isDown() ? "1," : "2," )) +
 		   	   (mNetButton.isDown() ? "1" : "0");
 	}
 }
