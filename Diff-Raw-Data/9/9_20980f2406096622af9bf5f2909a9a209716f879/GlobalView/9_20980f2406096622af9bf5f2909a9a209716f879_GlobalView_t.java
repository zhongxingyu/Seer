 package com.cs310.ubc.meetupscheduler.client;
 
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.TabPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.maps.client.InfoWindow;
 import com.google.gwt.maps.client.InfoWindowContent;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.maps.client.MapWidget;
 import com.google.gwt.maps.client.Maps;
 import com.google.gwt.maps.client.event.MarkerClickHandler;
 import com.google.gwt.maps.client.geom.LatLng;
 import com.google.gwt.maps.client.overlay.Marker;
 import com.google.gwt.maps.client.control.LargeMapControl3D;
 import com.google.gwt.maps.client.control.MapTypeControl;
 import com.google.gwt.user.client.Element;
 
 /**
  * view for the parks and events summary page
  * 
  * @author David
  */
 
 public class GlobalView extends Composite implements View{
 
 	private static final int MAP_HEIGHT = 550;
 	private static final int MAP_WIDTH = 700;
 	private static final int EVENT_TABLE_LENGTH = 15;
 
 
 	private HorizontalPanel rootPanel = new HorizontalPanel();
 	private VerticalPanel parkTable = new VerticalPanel();
 	private FlexTable recentEventsTable = new FlexTable();
 	private TabPanel eventTabPanel = new TabPanel();
 	private ListBox parkBox = new ListBox();
 	private ArrayList<HashMap<String, String>> allEvents;
 	private ArrayList<HashMap<String, String>> allParks;
 	SimplePanel viewPanel = new SimplePanel();
 	Element nameSpan = DOM.createSpan();
 
 	public GlobalView() {
 		allEvents = MeetUpScheduler.getEvents();
 		allParks = MeetUpScheduler.getParks();
 
 		viewPanel.getElement().appendChild(nameSpan);
 		initWidget(viewPanel);
 	}
 
 	/**
 	 * Loads the Maps API and returns the global view ui in a panel
 	 */
 	@Override
 	public Widget asWidget() {
 		//Initialize Map, needs a key before this can be deployed
 		Maps.loadMapsApi("", "2", false, new Runnable() {
 			public void run() {
 				buildUi();
 			}
 		});
 		return rootPanel;
 	}
 
 	/**
 	 * Builds the parks and events summary page ui and places it 
 	 * in the rootPanel field
 	 */
 	public void buildUi() {
 		//TODO: pull this out for all classes to use
 		//Map
 		LatLng vancouver = LatLng.newInstance(49.258480, -123.094574);
 		final MapWidget map = new MapWidget(vancouver, 11);
 		map.setPixelSize(MAP_WIDTH, MAP_HEIGHT);
 		map.setScrollWheelZoomEnabled(true);
 		map.addControl(new LargeMapControl3D());
 		map.addControl(new MapTypeControl());
 
 		//Zoom to park button
 		Button parkButton = new Button("Zoom to Park", new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				if(parkBox.getItemCount()>0 && parkBox != null && allParks != null){
 					int boxIndex = parkBox.getSelectedIndex();
 					String selectedPark = parkBox.getValue(boxIndex);
 
 					for(int i=0; i<allParks.size(); i++){
 						if(allParks.get(i).get("name").equals(selectedPark)){
 							String latLong = allParks.get(i).get("google_map_dest");
 
 							int index = latLong.indexOf(",");
 							double lat = Double.parseDouble(latLong.substring(0, index));
 							double lon = Double.parseDouble(latLong.substring(index+1));
 
 							map.setCenter(LatLng.newInstance(lat, lon), 17);
 						}
 					}
 				}
 			}
 		});
 
 		//Parks
 		addParksToListBox(allParks);
 		parkTable.add(parkBox);
 		parkTable.add(parkButton);
 		parkTable.setWidth("500");
 		parkTable.add(map);
 
 		//Recent Events
 		recentEventsTable.setCellPadding(2);
 		recentEventsTable.setCellSpacing(0);
 
 		recentEventsTable.setText(1, 0, "Event Title");
 		recentEventsTable.getCellFormatter().addStyleName(1, 0, "recentEventHeaders");
 
 		recentEventsTable.setText(1, 1, "Event Type");
 		recentEventsTable.getCellFormatter().addStyleName(1, 1, "recentEventHeaders");
 
 		recentEventsTable.setText(1, 2, "Park Name");
 		recentEventsTable.getCellFormatter().addStyleName(1, 2, "recentEventHeaders");
 
 
 
 		//Add Recent Event Table to tabPanel
 		eventTabPanel.getTabBar().getElement().getStyle();
 		eventTabPanel.add(recentEventsTable, "Recent Events");
 		eventTabPanel.add(new HTML("My Events Here"), "My Events");
 		eventTabPanel.add(new HTML("Advisories Here"), "Park Advisories");
 		eventTabPanel.selectTab(0);
 
 		//Add Events in tables and on map
 		addRecentEvents(allEvents);
 		addEventMarkers(allEvents, allParks, map);
 
 		//put ui elements into rootPanel field
 		rootPanel.add(parkTable);
 		rootPanel.add(eventTabPanel);
 	}
 
 	/**
 	 * Adds events to the recent events table. Number of events added is
 	 * specified by the EVENT_TABLE_LENGTH field.
 	 * 
 	 * @param events The events to be added to the recent events table
 	 */
 	private void addRecentEvents(ArrayList<HashMap<String, String>> events){
 		if(events != null && events.size() >0){
 			// If there are less events than our max table length, use the number of events for the length
 			int tableLength = (events.size() < EVENT_TABLE_LENGTH) ? events.size(): EVENT_TABLE_LENGTH;
 
 			for(int i=0; i<tableLength; i++){
 				int row = recentEventsTable.getRowCount();
 				String park_id = events.get(i).get("park_id");
 
 				recentEventsTable.setText(row, 0, events.get(i).get("name"));
 				recentEventsTable.setText(row, 1, events.get(i).get("category"));
 				recentEventsTable.setText(row, 2, events.get(i).get("park_id"));
 
 				/*for(int n=0; n < allParks.size(); n++){
 				if(allParks.get(n).get("park_id").equals(park_id)){
 					eventTable.setText(row, 2, allParks.get(n).get("name"));
 				}
 			}*/
 			}
 		}
 	}
 
 	/**
 	 * Adds parks to the parks ListBox.
 	 * 
 	 * @param parks The parks to be added to the parks ListBox
 	 */
 	private void addParksToListBox(ArrayList<HashMap<String, String>> parks){
 		if(parks != null && parks.size() > 0){
 			ArrayList<String> parkNames = new ArrayList<String>();
 
 			for(int i = 0; i<parks.size(); i++){
 				parkNames.add(parks.get(i).get("name"));
 			}
 
 			Collections.sort(parkNames);
 
 			for(int i = 0; i<parks.size(); i++){
 				parkBox.addItem(parkNames.get(i));
 			}
 		}
 	}
 
 	private void addEventMarkers(ArrayList<HashMap<String, String>> events, ArrayList<HashMap<String, String>> parks, final MapWidget map){
 
 		if(map != null && events != null && events.size() > 0 && parks != null && parks.size() > 0){
 
 			for(int i=0; i<parks.size(); i++){
				StringBuffer parkEvents = new StringBuffer();
 				for(int j=0; j<events.size(); j++){
 					//TODO: change this back to strings when park_id for events are actual park_id's
 					if(parks.get(i).get("name").equals(events.get(j).get("park_id"))){
 						System.out.println("Adding Marker for park name: " + parks.get(i).get("name") + ". For event: " + events.get(j).get("name"));
 
 						parkEvents.append("Event:   " + 
								"<a href=\"/MeetUpScheduler.html?gwt.codesvr=127.0.0.1:9997#CreateEventPlace:Create_Event\">" +
 								events.get(j).get("name") + 
								"</a><br/>");
 
 						String latLong = parks.get(i).get("google_map_dest");
 						int index = latLong.indexOf(",");
 						double lat = Double.parseDouble(latLong.substring(0, index));
 						double lon = Double.parseDouble(latLong.substring(index+1));
 
 						//this is to pass event info into the info window creation
 						final Marker eventMarker = new Marker(LatLng.newInstance(lat, lon));
 						final String eventPasser = new String(parkEvents);
 						final HashMap<String, String> parkInfo = parks.get(i);
 
 						eventMarker.addMarkerClickHandler(new MarkerClickHandler() {
 							public void onClick(MarkerClickEvent event) {
 
 								InfoWindow info = map.getInfoWindow();
 								InfoWindowContent content = new InfoWindowContent(
 										"<font color=\"#4C674C\"><big><b> Events at " + parkInfo.get("name") + ": </b></big></font><br/>"
 										+ new HTML(eventPasser)
 								);
 								info.open(eventMarker, content);
 							}
 						});
 
 						map.addOverlay(eventMarker);
 					}
 				}
 			}
 		}
 	}
 
 
 	@Override
 	public void setName(String name) {
 		nameSpan.setInnerText("Global View, " + name);
 
 	}
 }
 
