 package org.obudget.client;
 
 import com.google.gwt.dev.jjs.ast.js.JsonObject;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.SuggestBox;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
 
 class Application implements ValueChangeHandler<String> {
 
 	private BudgetLines mChildrenBudgetLines = new BudgetLines();
 	private BudgetLines  mHistoricBudgetLines = new BudgetLines();
 	private ResultGrid mResultsGrid;
 	private PieCharter mPieCharter;
 	private HTML mBreadcrumbs;
 	private TimeLineCharter mTimeLineCharter;
 	private String mCode = null;
 	private Integer mYear = null;
 	private ListBox mYearSelection;
 	private SuggestBox mSearchBox;
 	private Label mSummary1;
 	private Label mSummary2;
 	private HTML mSummary3;
 	private BudgetNews mBudgetNews;
 	
 	public void init() {
 		TotalBudget.getInstance();
 		
 		mResultsGrid = new ResultGrid();
 		mResultsGrid.setWidth("60%");
 
 		mPieCharter = new PieCharter(this);
 		mPieCharter.setWidth("600px");
 		mPieCharter.setHeight("300px");
 		
 		mTimeLineCharter = new TimeLineCharter(this);
 		mTimeLineCharter.setWidth("600px");
 		mTimeLineCharter.setHeight("300px");
 		
 		mBreadcrumbs = new HTML("");
 		mBreadcrumbs.setHeight("20px");
		mBreadcrumbs.setWidth("700px");
 		
 		mYearSelection = new ListBox();
 		mYearSelection.addChangeHandler( new ChangeHandler() {
 			@Override
 			public void onChange(ChangeEvent event) {
 				Integer index = mYearSelection.getSelectedIndex();
 				selectYear( 1992+index );
 			}
 		});
 		
 		mSearchBox = new SuggestBox(new BudgetSuggestionOracle(mYearSelection));
 		mSearchBox.setWidth("300px");
 		mSearchBox.addSelectionHandler( new SelectionHandler<Suggestion>() {
 			@Override
 			public void onSelection(SelectionEvent<Suggestion> event) {
 				final BudgetSuggestion bs = (BudgetSuggestion) event.getSelectedItem();
 				
 				BudgetAPICaller api = new BudgetAPICaller();
 				api.setCode(bs.getCode());
 				api.setParameter("depth", "0");
 				api.setParameter("text", bs.getTitle());
 				
 				api.go( new BudgetAPICallback() {
 					
 					@Override
 					public void onSuccess(JSONArray data) {
 						if ( data.size() > 0 ) {
 							Integer year = (int) data.get(0).isObject().get("year").isNumber().doubleValue();
 							History.newItem(bs.getCode() +","+year );
 						}
 					}
 				});
 				
 			}
 		});
 
 		for ( Integer i = 1992 ; i<=2009 ; i ++ ) {
 			mYearSelection.addItem(i.toString());
 		}
 
 		mSummary1 = new Label();
 		mSummary2 = new Label();
 		mSummary3 = new HTML();
 		
 		mBudgetNews = new BudgetNews();
 				
 		History.addValueChangeHandler( this );
 	}
 
 	public ResultGrid getResultsGrid() {
 		return mResultsGrid;
 	}
 
 	public PieCharter getPieCharter() {
 		return mPieCharter;
 	}
 
 	public void selectYear( Integer year ) {
 		if ( mCode != null ) {
 			selectBudgetCode(mCode, year);
 		}
 	}
 
 	public void selectBudgetCode( String code, Integer year ) {
 		mCode = code;
 		mYear = year;
 		
 		BudgetAPICaller generalInfo = new BudgetAPICaller();
 		
 		// Load single record
 		generalInfo.setCode(code);
 		generalInfo.setParameter("year", year.toString() );
 		generalInfo.setParameter("depth", "0");
 		generalInfo.go( new BudgetAPICallback() {
 			
 			@Override
 			public void onSuccess(JSONArray data) {
 				if ( data.size() < 1 ) {
 					return;
 				}
 				
 				JSONObject firstResult = data.get(0).isObject(); 
 				
 				String title = firstResult.get("title").isString().stringValue();
 				String code = firstResult.get("budget_id").isString().stringValue();
 				mSearchBox.setValue(title);
 
 				mYearSelection.setSelectedIndex( mYear - 1992 );
 
 				mBudgetNews.update("\""+title+"\"");
 				
 				Window.setTitle("תקציב המדינה - "+title+" ("+mYear+")");
 				mSummary1.setText( "לתקציב "+title+" הוקצו בשנת");
 				final Integer revisedSum;
 				final String revisedSumType;
 				if ( (firstResult.get("gross_amount_revised") != null) &&
 					 (firstResult.get("gross_amount_revised").isNumber() != null) ) {
 					revisedSumType = "gross";
 				} else {
 					revisedSumType = "net";
 				}
 				if ( ( firstResult.get(revisedSumType+"_amount_revised") != null ) && 
 					   firstResult.get(revisedSumType+"_amount_revised").isNumber() != null ) {
 					revisedSum = (int) firstResult.get(revisedSumType+"_amount_revised").isNumber().doubleValue();				
 					mSummary2.setText( "סה\"כ "+NumberFormat.getDecimalFormat().format(revisedSum)+",000 \u20aa" + (revisedSumType == "net" ? " (נטו)" : ""));
 				} else {
 					revisedSum = null;
 				}
 				
 				mSummary3.setHTML("");							
 				if ( firstResult.get("parent") != null ) {
 					JSONArray parents = firstResult.get("parent").isArray();	
 					if ( parents.size() > 0 ) {
 						final String parentCode = parents.get(0).isObject().get("budget_id").isString().stringValue();
 						final String parentTitle = parents.get(0).isObject().get("title").isString().stringValue();
 						
 						BudgetAPICaller percent = new BudgetAPICaller();
 						percent.setCode(parentCode);
 						percent.setParameter("year", mYear.toString());
 						percent.setParameter("depth", "0");
 						percent.go( new BudgetAPICallback() {						
 							@Override
 							public void onSuccess(JSONArray data) {
 								if ( (data.get(0).isObject().get(revisedSumType+"amount_revised") != null) && 
 									 (data.get(0).isObject().get(revisedSumType+"amount_revised").isNumber() != null) ) {
 									double percent = revisedSum / data.get(0).isObject().get(revisedSumType+"amount_revised").isNumber().doubleValue();
 									mSummary3.setHTML( "שהם "+NumberFormat.getPercentFormat().format(percent)+" מתקציב <a href='#"+parentCode+","+mYear+"'>"+parentTitle+"</a>");
 								}
 							}
 						});
 					}			
 				
 					String breadcrumbs = "";
 					for ( int i = 0 ; i < parents.size() ; i++ ) {
 						String ptitle = parents.get(i).isObject().get("title").isString().stringValue();
 						String pcode = parents.get(i).isObject().get("budget_id").isString().stringValue();
 						breadcrumbs = "<a href='#"+pcode+","+mYear+"'>"+ptitle+"</a> ("+pcode+") "+"&nbsp;&gt;&nbsp;" + breadcrumbs;
 					}
 					breadcrumbs += "<a href='#"+code+","+mYear+"'>"+title+"</a> ("+code+")";
 					mBreadcrumbs.setHTML(breadcrumbs);			
 				}
 			}
 		});
 
 		// Load all children
 		BudgetAPICaller childrenLines = new BudgetAPICaller();
 		childrenLines.setCode(code);
 		childrenLines.setParameter("year", year.toString());
 		childrenLines.setParameter("depth", "1");
 		childrenLines.go( new BudgetAPICallback() {
 			
 			@Override
 			public void onSuccess(JSONArray data) {
 				mChildrenBudgetLines.parseJson(data);
 				mResultsGrid.handleData(mChildrenBudgetLines);
 				mPieCharter.handleData(mChildrenBudgetLines);				
 			}
 		});
 		
 		// Load same record, over the years
 		BudgetAPICaller historicLines = new BudgetAPICaller();
 		historicLines.setCode(code);
 		historicLines.setParameter("depth", "0");
 		historicLines.go( new BudgetAPICallback() {
 			
 			@Override
 			public void onSuccess(JSONArray data) {
 				mHistoricBudgetLines.parseJson(data);
 				mTimeLineCharter.handleData(mHistoricBudgetLines);				
 			}
 		});
 		
 	}
 
 	public TimeLineCharter getTimeLineCharter() {
 		return mTimeLineCharter;
 	}
 
 	@Override
 	public void onValueChange(ValueChangeEvent<String> event) {
 		String hash = event.getValue();
 		String[] parts = hash.split(",");
 		if ( parts.length == 2 ) {
 			String code = parts[0];
 			Integer year = Integer.decode(parts[1]);
 			selectBudgetCode(code, year);
 		} else {
 			History.newItem("00,2009");
 		}
 	}
 
 	public Widget getYearSelection() {
 		return mYearSelection;
 	}
 
 	public Widget getSearchBox() {
 		return mSearchBox;
 	}
 
 	public Widget getBreadcrumbs() {
 		return mBreadcrumbs;
 	}
 
 	public Widget getSummary1() {
 		return mSummary1;
 	}
 
 	public Widget getSummary2() {
 		return mSummary2;
 	}
 
 	public Widget getSummary3() {
 		return mSummary3;
 	}
 
 	public Widget getBudgetNews() {
 		return mBudgetNews;
 	}
 
 }
