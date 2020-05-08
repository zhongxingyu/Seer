 package com.waterlooeventfinder2.client;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.SimplePager;
 import com.google.gwt.user.cellview.client.TextColumn;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.view.client.ListDataProvider;
 import com.google.gwt.view.client.SelectionChangeEvent;
 import com.google.gwt.view.client.SingleSelectionModel;
 import com.waterlooeventfinder2.shared.Event;
 
 public class ClubEventsListContent extends Content{
 
 	public ClubEventsListContent(int userId) {		
		super();
 		if (retrievalService == null) {
 			retrievalService = GWT.create(EventRetrievalService.class);
 		}
 		
 		GetEventByUserId(userId);
 		
 	}
 
 	/**
 	 * Populate events list from database
 	 */
 	private void GetEventByUserId(final int userId) {
 		if (retrievalService == null) {
 			retrievalService = GWT.create(EventRetrievalService.class);
 		}
 
 		// Set up the callback object.
 		AsyncCallback<ArrayList<Event>> callback = new AsyncCallback<ArrayList<Event>>() {
 
 			public void onFailure(Throwable caught) {
 				Window.alert(caught.getMessage());
 			}
 
 			public void onSuccess(ArrayList<Event> results) {
 				CreateCellTable(userId, results);				
 			}
 		};
 
 		retrievalService.GetEventByUserId(userId, callback);
 	}
 
 	/**
 	 * Using a list of events to create a cell table
 	 * @param results arraylist of events
 	 */
 	private void CreateCellTable(final int userId, ArrayList<Event> results) {
         
 
 		CellTable<Event> table = new CellTable<Event>();
 		// To set the size of the table
 		table.setPageSize(10);
 		SimplePager pager = new SimplePager();
 		
 		if (results != null) {
 			// short description column
 			TextColumn<Event> nameColumn = new TextColumn<Event>() {
 				@Override
 				public String getValue(Event object) {
 					return object.Name();
 				}
 			};
 
 			// Starting time column
 			TextColumn<Event> startDateColumn = new TextColumn<Event>() {
 				@Override
 				public String getValue(Event object) {
 					DateTimeFormat dateFormat = DateTimeFormat
 							.getFormat("E, MMMM dd 'at' HH:mm");
 					return dateFormat.format(object.getStarHour());
 				}
 			};
 
 			table.addColumn(nameColumn, "Description");
 			table.addColumn(startDateColumn, "Start Time");
 				
 			ListDataProvider<Event> dataProvider = new ListDataProvider<Event>();
 			dataProvider.addDataDisplay(table);
 			pager.setDisplay(table);
 			
 			List<Event> list =  dataProvider.getList();
 			for (Iterator<Event> i = results.iterator(); i.hasNext(); ) {
 				list.add(i.next());
 			}
 			
 			final SingleSelectionModel<Event> selectionModel = new SingleSelectionModel<Event>();
 			table.setSelectionModel(selectionModel);
 			selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 				public void onSelectionChange(SelectionChangeEvent event) {
 					Event selected = selectionModel.getSelectedObject();
 					if (selected != null) {
 						ContentContainer.getInstance();
 						ContentContainer.setContent(new ClubEventDetailContent(userId, selected.getEventId(), "modifyEvent"));
 					}
 				}
 			});
 			
 			pager.firstPage();
 			panel.add(table);
 			panel.add(pager);
 			
 		} else {
 			panel.add(new Label("No events available. Please check back later."));
 		}
 	}
 	
 	
 	
 
 
 }
 
