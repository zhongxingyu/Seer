 package gwt.test.client.view;
 
 import gwt.test.client.ChartData;
 import gwt.test.client.ChartDataParams;
 import gwt.test.client.ChartPoint;
 import gwt.test.client.ChartingService;
 import gwt.test.client.ChartingServiceAsync;
 import gwt.test.client.Presenter;
 import gwt.test.client.StockQuote;
 import gwt.test.client.Symbol;
 import gwt.test.client.TimeFrame;
 import gwt.test.client.presenter.StockQuotePresenter;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.jfree.ui.tabbedui.VerticalLayout;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Style;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.HasClickHandlers;
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.DockLayoutPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
 import com.google.gwt.user.client.ui.SuggestBox;
 import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.googlecode.gchart.client.GChart;
 import com.googlecode.gchart.client.GChart.SymbolType;
 
 public class StockQuoteView extends Composite implements StockQuotePresenter.Display {
 	
 	private final List<Symbol> symbols = new ArrayList<Symbol>();
 	private final SuggestBox symbol;
 	private final SuggestBox name;
 	private final ListBox timeframe;	
 	private final MultiWordSuggestOracle nameOracle = new MultiWordSuggestOracle();
 	private final MultiWordSuggestOracle symbolOracle = new MultiWordSuggestOracle();		
 	private ChartData chartData;	
 	private int xChartSize = 1000;
 	private int yChartSize = 700;
 	private final StockQuoteDisplay stockQuoteDisplay;		
 	private final DockLayoutPanel dock = new DockLayoutPanel(Style.Unit.EM);	
 	private GChart centerChart;
 	private final Button showButton;
 	private final Button importButton;
 	private final Map<String,TimeFrame> timeFrameMap = new HashMap<String,TimeFrame>();
 	
 	
 	public StockQuoteView(HandlerManager eventBus) {		
 		super.initWidget(this.dock);
 		
 		final Label nameLabel = new Label("Name:");
 		final Label symbolLabel = new Label("Symbol:");
 		final Label timeFrameLabel = new Label("Timeframe:");				
 		this.symbol = new SuggestBox(symbolOracle);
 		this.name = new SuggestBox(nameOracle);		
 		
 		this.timeFrameMap.put(TimeFrame.All.toString(), TimeFrame.All);
 		this.timeFrameMap.put(TimeFrame.Days14.toString(), TimeFrame.Days14);
 		this.timeFrameMap.put(TimeFrame.Month1.toString(), TimeFrame.Month1);
 		this.timeFrameMap.put(TimeFrame.Months2.toString(), TimeFrame.Months2);
 		this.timeFrameMap.put(TimeFrame.Months6.toString(), TimeFrame.Months6);
 		this.timeFrameMap.put(TimeFrame.Year1.toString(), TimeFrame.Year1);
 		this.timeFrameMap.put(TimeFrame.Years2.toString(), TimeFrame.Years2);
 		this.timeFrameMap.put(TimeFrame.Years5.toString(), TimeFrame.Years5);
 		
 		this.timeframe = new ListBox();
 		timeframe.addItem("All", TimeFrame.All.toString());
 		timeframe.addItem("5 Years", TimeFrame.Years5.toString());
 		timeframe.addItem("2 Years", TimeFrame.Years2.toString());
 		timeframe.addItem("1 Year", TimeFrame.Year1.toString());
 		timeframe.addItem("6 Months", TimeFrame.Months6.toString());
 		timeframe.addItem("2 Months", TimeFrame.Months2.toString());
 		timeframe.addItem("1 Month", TimeFrame.Month1.toString());
 		timeframe.addItem("14 Days", TimeFrame.Days14.toString());
 		
 		showButton = new Button("Show");
 		showButton.getElement().setId(StockQuotePresenter.Display.SHOW_BUTTON_ID);
 		importButton = new Button("Import");
 		importButton.getElement().setId(StockQuotePresenter.Display.IMPORT_BUTTON_ID);
 
 		final HorizontalPanel hPanel1 = new HorizontalPanel();
 		hPanel1.setSpacing(5);
 		hPanel1.add(nameLabel);
 		hPanel1.add(name);
 		hPanel1.add(symbolLabel);
 		hPanel1.add(symbol);
 
 		final HorizontalPanel hPanel2 = new HorizontalPanel();
 		hPanel2.setSpacing(5);
 		hPanel2.add(timeFrameLabel);
 		hPanel2.add(timeframe);
 		hPanel2.add(showButton);
 		hPanel2.add(importButton);
 
 		final VerticalPanel vPanel1 = new VerticalPanel();
 		vPanel1.add(hPanel1);
 		vPanel1.add(hPanel2);				
 
 		this.stockQuoteDisplay = new StockQuoteDisplay();
 		
 		final HorizontalPanel hPanel3 = new HorizontalPanel();
 		hPanel3.setSpacing(5);
 		hPanel3.add(stockQuoteDisplay.getDate());
 
 		final HorizontalPanel hPanel4 = new HorizontalPanel();
 		hPanel4.setSpacing(5);
 		hPanel4.add(stockQuoteDisplay.getDayHigh());
 		hPanel4.add(stockQuoteDisplay.getDayLow());
 		hPanel4.add(stockQuoteDisplay.getClose());
 
 		final HorizontalPanel hPanel5 = new HorizontalPanel();
 		hPanel5.setSpacing(5);
 		hPanel5.add(stockQuoteDisplay.getVolume());
 		hPanel5.add(stockQuoteDisplay.getAdjClose());
 
 		final VerticalPanel vPanel2 = new VerticalPanel();
 		vPanel2.add(hPanel3);
 		vPanel2.add(hPanel4);
 		vPanel2.add(hPanel5);
 
 		// Add text widgets all around
 		this.dock.addNorth(vPanel1, 5);
 		this.dock.addSouth(vPanel2, 5);		
 		
 		this.dock.addEast(new HTML("East"), 5);
 		this.dock.addWest(new HTML("West"), 5);
 		this.centerChart = createGChart();		
 		// scroller.setSize("400px", "100px");
 		this.dock.add(this.centerChart.asWidget());		
 		
 		
 		//add listeners
 		addSuggestBoxListeners();
 		
 		//import symbols for suggestboxes
 		importSymbols();
 		
 		
 	}
 	
 	private GChart createGChart() {
 		GChart chart = new GChart(this.xChartSize, this.yChartSize);
 		chart.setChartTitle("Empty Chart");		
 		chart.addCurve();
 		chart.getCurve().getSymbol().setHeight(0);
 		chart.getCurve().getSymbol().setWidth(0);
 		chart.getCurve().getSymbol().setSymbolType(SymbolType.LINE);
 		for (int i = 0; i < 10; i++) 
 	        chart.getCurve().addPoint(i,i*i);
 		chart.update();
 		return chart;
 	}
 	
 	private void updateChart() {
 		GChart chart = this.centerChart;
 		chart.setChartTitle(this.chartData.getTitle());
 		chart.setChartSize(this.xChartSize, this.yChartSize);
		chart.getXAxis().setTickLabelFormat("=(Date)dd-MM-yyyy");
//		chart.getXAxis().setTickCount(this.xChartSize / 50);
 		chart.clearCurves();
 		chart.addCurve();
 		chart.getCurve().getSymbol().setSymbolType(SymbolType.LINE);
 	    chart.getCurve().getSymbol().setHeight(0);
 	    chart.getCurve().getSymbol().setWidth(0);
 	    int xValues = 1;
 	    for(ChartPoint chartPoint: this.chartData.getPoints()) {
	    	chart.getCurve().addPoint(chartPoint.getTime().getTime(), chartPoint.getValue());
 	    }
 	    chart.getCurve().setLegendLabel(this.chartData.getLegendLabel());
 	    chart.getXAxis().setAxisLabel(this.chartData.getxAxisTitle());
 	    chart.getYAxis().setAxisLabel(this.chartData.getyAxisTitle());
 	    chart.update();
 	}
 	
 	private void addSuggestBoxListeners() {
 		name.addSelectionHandler(new SelectionHandler<Suggestion>() {
 			@Override
 			public void onSelection(SelectionEvent<Suggestion> event) {
 				String myName = event.getSelectedItem().getReplacementString();
 				for(Symbol mySymbol: symbols) {
 					if(myName != null && myName.equalsIgnoreCase(mySymbol.getName())) {
 						symbol.setText(mySymbol.getSymbol());
 						break;
 					}
 				}
 			}
 		});
 
 		symbol.addSelectionHandler(new SelectionHandler<Suggestion>() {
 			@Override
 			public void onSelection(SelectionEvent<Suggestion> event) {
 				String symbolStr = event.getSelectedItem().getReplacementString();
 				for(Symbol mySymbol: symbols) {
 					if(symbolStr != null && symbolStr.equalsIgnoreCase(mySymbol.getSymbol())) {
 						name.setText(mySymbol.getName());
 						break;
 					}
 				}
 			}
 		});
 	}
 	
 	public void showDialogBox(String headerText, String style, String content) {
 		// Create the popup dialog box		
 		final DialogBox dialogBox = new DialogBox();
 		dialogBox.setText(headerText);
 		dialogBox.setAnimationEnabled(true);
 		final Label textToServerLabel = new Label();
 		final Button closeButton = new Button("Close");
 		closeButton.getElement().setId("closeButton");
 		final VerticalPanel dialogVPanel = new VerticalPanel();
 		dialogVPanel.addStyleName(style);
 		dialogVPanel.add(new HTML(content));
 		dialogVPanel.add(textToServerLabel);
 		dialogVPanel.add(closeButton);
 		dialogBox.setWidget(dialogVPanel);
 		dialogBox.center();
 		closeButton.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				dialogBox.hide();
 			}
 		});
 		dialogBox.show();
 	}
 	
 	private void importSymbols() {
 		ChartingServiceAsync chartingService = GWT.create(ChartingService.class);
 		chartingService.getSymbols(new AsyncCallback<List<Symbol>>() {
 			public void onFailure(Throwable caught) {
 				// Show the RPC error message to the user
 				showDialogBox("Remote Procedure Call - Failure", "serverResponseLabelError", SERVER_ERROR);								
 			}
 
 			public void onSuccess(List<Symbol> result) {
 				symbols.clear();
 				symbols.addAll(result);
 				long symbolCount = 0;			
 				for (Symbol mySymbol : result) {
 					if (mySymbol.getName() != null || mySymbol.getName() != null) {
 						nameOracle.add(mySymbol.getName());
 						symbolOracle.add(mySymbol.getSymbol());
 						symbolCount++;
 					}
 				}
 				System.out.println("Symbols added: "+symbolCount);
 			}
 		});
 	}
 	
 	private class StockQuoteDisplay {
 		private final Label date;
 		private final Label dayHigh;
 		private final Label dayLow;
 		private final Label close;
 		private final Label volume;
 		private final Label adjClose;		
 		
 		public StockQuoteDisplay() {
 			StockQuote sq = new StockQuote();
 			sq.setTime(new Date());
 			sq.setAdjLast("-");
 			sq.setCurrency("USD");
 			sq.setDayHigh("-");
 			sq.setDayLow("-");
 			sq.setLast("-");
 			sq.setName("-");
 			sq.setSymbol("-");
 			sq.setVolume("-");
 			
 			Date myDate = sq.getTime() == null ? new Date() : sq.getTime();
 			this.date = new Label("Last Quote: " + myDate.toString());
 			this.dayHigh = new Label("High: " + sq.getDayHigh());
 			this.dayLow = new Label("Low: " + sq.getDayLow());
 			this.close = new Label("Close: " + sq.getLast());
 			this.volume = new Label("Volume: " + sq.getVolume());
 			this.adjClose = new Label("Adj. Close: " + sq.getAdjLast());
 		}
 
 		public void setStockQuote(StockQuote sq) {
 			date.setText("Last Quote: " + sq.getTime().toString());
 			dayHigh.setText("High: " + sq.getDayHigh());
 			dayLow.setText("Low: " + sq.getDayLow());
 			close.setText("Close: " + sq.getLast());
 			volume.setText("Volume: " + sq.getVolume());
 			adjClose.setText("Adj. Close: " + sq.getAdjLast());
 		}
 		
 		public Label getDate() {
 			return date;
 		}
 
 		public Label getDayHigh() {
 			return dayHigh;
 		}
 
 		public Label getDayLow() {
 			return dayLow;
 		}
 
 		public Label getClose() {
 			return close;
 		}
 
 		public Label getVolume() {
 			return volume;
 		}
 
 		public Label getAdjClose() {
 			return adjClose;
 		}
 	}
 	
 	@Override
 	public void setName(String name) {		
 		this.name.setValue(name, true);
 	}
 
 	@Override
 	public void setSymbol(String symbol) {
 		this.symbol.setValue(symbol, true);
 	}
 
 	@Override
 	public void setTimeFrame(int itemNo) {
 		this.timeframe.setItemSelected(itemNo, true);
 	}
 
 	@Override
 	public void setStockQuote(StockQuote stockQuote) {
 		this.stockQuoteDisplay.setStockQuote(stockQuote);		
 	}
 	
 	@Override
 	public void setSymbols(List<Symbol> symbols) {		
 		long symbolCount = 0;			
 		for (Symbol mySymbol : symbols) {
 			if (mySymbol.getName() != null || mySymbol.getName() != null) {
 				this.nameOracle.add(mySymbol.getName());
 				this.symbolOracle.add(mySymbol.getSymbol());
 				symbolCount++;
 			}
 		}
 		System.out.println("Symbols added: "+symbolCount);
 	}
 
 	@Override
 	public void setChartLink(String link) {
 		HTML html = new HTML(link);
 		this.dock.addEast(html.asWidget(), 0);
 	}	
 	
 	@Override
 	public String getSymbol() {
 		return this.symbol.getValue();
 	}
 	
 	@Override
 	public TimeFrame getTimeFrame() {
 		TimeFrame myTimeFrame = this.timeFrameMap.get(this.timeframe.getValue(this.timeframe.getSelectedIndex()));
 		return myTimeFrame;
 	}
 
 	@Override
 	public void setChartData(ChartData chartData) {
 		this.chartData = chartData;
 		updateChart();
 	}
 
 	public ChartDataParams getChartDataParams() {
 		return new ChartDataParams(getSymbol(), getTimeFrame(), this.xChartSize, this.yChartSize);
 	}	
 	
 	@Override
 	public HasClickHandlers getShowButton() {
 		return this.showButton;
 	}
 	
 	@Override
 	public HasClickHandlers getImportButton() {
 		return this.importButton;
 	}
 
 }
