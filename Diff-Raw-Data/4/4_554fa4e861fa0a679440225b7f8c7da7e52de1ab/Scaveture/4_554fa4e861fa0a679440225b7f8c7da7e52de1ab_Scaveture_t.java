 package com.scaveture.client;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import com.google.gwt.core.client.Callback;
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.logical.shared.CloseEvent;
 import com.google.gwt.event.logical.shared.CloseHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.geolocation.client.Geolocation;
 import com.google.gwt.geolocation.client.Geolocation.PositionOptions;
 import com.google.gwt.geolocation.client.Position;
 import com.google.gwt.geolocation.client.Position.Coordinates;
 import com.google.gwt.geolocation.client.PositionError;
 import com.google.gwt.maps.client.InfoWindowContent;
 import com.google.gwt.maps.client.MapOptions;
 import com.google.gwt.maps.client.MapType;
 import com.google.gwt.maps.client.MapWidget;
 import com.google.gwt.maps.client.control.SmallMapControl;
 import com.google.gwt.maps.client.event.MarkerClickHandler;
 import com.google.gwt.maps.client.geom.LatLng;
 import com.google.gwt.maps.client.overlay.Marker;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.scaveture.client.search.RadixTree;
 import com.scaveture.client.search.RadixTreeImpl;
 import com.scaveture.client.util.Random;
 import com.scaveture.client.util.StringTokenizer;
 import com.scaveture.shared.Hunt;
 import com.scaveture.shared.QueryParameters;
 import com.scaveture.shared.Submission;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class Scaveture implements EntryPoint {
 	private final static LatLng SEATTLE = LatLng.newInstance(47.611, -122.334);
 	private final HuntServiceAsync huntService = GWT.create(HuntService.class);
 	private Geolocation geoloc = null;
 	private MapWidget mapWidget;
 	private HorizontalPanel menuPanel;
 	private final Label errorLabel = new Label("");
 	private Anchor huntLink = new Anchor("new hunt");
 	private final TextBox filterText = new TextBox();
 	private final Button filterButton = new Button("filter");
 	private final PositionOptions posOptions = new PositionOptions();
 	private NewHuntDialog newHuntDialog = new NewHuntDialog();
 	private boolean isMobileClient = false;
 	private Map<Long, Hunt> mappedHunts;
 	private Map<String, MarkerContent> mappedMarkers;
 	private RadixTree<List<MarkerContent>> markerIndex = new RadixTreeImpl<List<MarkerContent>>();
 
 	/**
 	 * This is the entry point method.
 	 */
 	public void onModuleLoad() {
     	posOptions.setHighAccuracyEnabled(false);
     	posOptions.setTimeout(60000);
     	//posOptions.setMaximumAge(120000);
 		mappedHunts = new HashMap<Long, Hunt>();
 		mappedMarkers = new HashMap<String, MarkerContent>();
 		ImageUploadDialog.exportJavaScriptMethod();
 	    errorLabel.addStyleDependentName("Error");
 	    errorLabel.addStyleDependentName("Big");
 	    String mobileClient = Window.Location.getParameter("mc");
 	    if(mobileClient != null && mobileClient.trim() == "1") {
 	    	isMobileClient = true;
 	    }
     	setUpHistoryListeners();
     	setupLayout(SEATTLE);
 		setupFilterBox();
 		setupHuntLink();
     	
 	    geoloc = Geolocation.getIfSupported();
 	    if(geoloc != null) {
 	    	geoloc.getCurrentPosition(
 	    		new Callback<Position, PositionError>() {
 		            public void onFailure(PositionError error) {
 		            	errorLabel.setText("Obtaining position FAILED! Code: " + error.getCode());
 		            	mapWidget.setCenter(SEATTLE);
 		            }
 
 		            public void onSuccess(Position position) {
 		            	Coordinates c = position.getCoordinates();
 		            	LatLng here = LatLng.newInstance(c.getLatitude(), c.getLongitude());
 		            	errorLabel.setText("");
 		            	mapWidget.setCenter(here);
 		            	
 		            	huntService.findHuntsNear(
 		            			mapWidget.getBounds().getNorthEast().getLatitude(), 
 		            			mapWidget.getBounds().getNorthEast().getLongitude(), 
 		            			mapWidget.getBounds().getSouthWest().getLongitude(), 
 		            			mapWidget.getBounds().getSouthWest().getLatitude(), 
 		            			new AsyncCallback<List<Hunt>>() {
 									@Override
 									public void onFailure(Throwable caught) {
 										errorLabel.setText("Exception: " + caught.getMessage());
 									}
 
 									@Override
 									public void onSuccess(List<Hunt> result) {
 										for(Hunt h : result) {
 											addMarker(new HuntMarkerContent(h, isMobileClient), false);
 											mappedHunts.put(h.getId(), h);
 										}
 									}} 
 		            			);
 		            }
 	            }, 
 	            posOptions
 	        );
 	    }
 	    else {
 	    	errorLabel.setText("geoloc NOT supported");
         	mapWidget.setCenter(SEATTLE);
 	    }
 	}
 	
 	private void setUpHistoryListeners() {
 		History.addValueChangeHandler(new ValueChangeHandler<String>(){
 			@Override
 			public void onValueChange(ValueChangeEvent<String> event) {
 				QueryParameters parms = new QueryParameters(event.getValue());
 				String path = parms.getString("path");
 				long hid = parms.getLong("hid");
 				String open = parms.getString("open");
 				
 				if(path != null) {
 					ImageUploadDialog.open(parms.getString("id"), path, parms.getString("lat"), parms.getString("long"));
 				}
 				else if(hid != -1 && mappedHunts.containsKey(hid)) {
 					displaySubmissions(mappedHunts.get(hid));
 				}
 				else if(open != null) {
 					MarkerContent content = mappedMarkers.get(open);
 					if(content == null) {
 						content = createMarkerContent(open);
 						addMarker(content, true);
 					}
 					else {
 						mapWidget.getInfoWindow().open(content.getMarker(), new InfoWindowContent(content.toHtml()));
 					}
 				}
 			}
 		});
 	}
 
 	//Set up the map widget with an initial center and menu bar at bottom
 	private void setupLayout(LatLng point) {
 	    menuPanel = new HorizontalPanel();
 	    menuPanel.setWidth("100%");
 	    menuPanel.add(errorLabel);
 	    RootPanel.get().add(menuPanel);
 	    
 		final MapOptions options = MapOptions.newInstance();
 		List<MapType> mapTypes = new LinkedList<MapType>();
 		mapTypes.add(MapType.getNormalMap());
 	    options.setMapTypes(mapTypes);
 	    mapWidget = new MapWidget(point, 14, options);
 	    mapWidget.setDraggable(true);
 	    mapWidget.addControl(new SmallMapControl());
 	    
 	    final int menuHeight = menuPanel.getOffsetHeight();
 	    final int mapHeight = Window.getClientHeight() - menuHeight;
 	    mapWidget.setSize("100%", mapHeight + "px");
 
 	    RootPanel.get().add(mapWidget);
 	}
 	
 	private void setAllMarkersVisibility(Collection<MarkerContent> markers, boolean isVisible) {
 		for(MarkerContent content : markers) {
 			content.getMarker().setVisible(isVisible);
 		}
 	}
 	
 	private void setupFilterBox() {
 		filterText.addStyleDependentName("Left");
 		filterButton.addStyleDependentName("Left");
 	    menuPanel.add(filterText);
 	    menuPanel.add(filterButton);
 	    filterButton.addClickHandler(new ClickHandler(){
 			@Override
 			public void onClick(ClickEvent event) {
 				StringTokenizer tok = new StringTokenizer(filterText.getText());
 				if(tok.hasMoreTokens()) {
 					setAllMarkersVisibility(mappedMarkers.values(), false);
 					do {
 						String next = tok.nextToken();
 						ArrayList<List<MarkerContent>> results = markerIndex.searchPrefix(next, 10);
 						for(List<MarkerContent> list : results) {
 							setAllMarkersVisibility(list, true);
 						}
 					} while(tok.hasMoreTokens());
 				}
 				else {
 					setAllMarkersVisibility(mappedMarkers.values(), true);
 				}
 			}
 		});
 	}
 	
 	private void setupHuntLink() {
 	    huntLink.addStyleDependentName("Right");
 	    huntLink.addStyleDependentName("Big");
 	    menuPanel.add(huntLink);
 	    huntLink.addClickHandler(new ClickHandler(){
 			@Override
 			public void onClick(ClickEvent event) {
 				newHuntDialog.clear();
 				newHuntDialog.show();
 				newHuntDialog.center();
 			}
 		});
 	    
 	    newHuntDialog.addCloseHandler(new CloseHandler<PopupPanel>(){
 			@Override
 			public void onClose(CloseEvent<PopupPanel> event) {
 				if(newHuntDialog.isSaved()) {
 					if(newHuntDialog.getDescription().length() > 0) {
 					    if(geoloc != null) {
 					    	geoloc.getCurrentPosition(
 					    		new Callback<Position, PositionError>() {
 						            public void onFailure(PositionError error) {
 						            	errorLabel.setText("Obtaining position FAILED! Code: " + error.getCode());
 						            }
 
 						            public void onSuccess(Position position) {
 						            	Coordinates c = position.getCoordinates();
 						            	final Hunt h = new Hunt();
 						            	h.setLatitude(Random.fuzz(c.getLatitude(), 0.001D));
 						            	h.setLongitude(Random.fuzz(c.getLongitude(), 0.001D));
 						            	h.setDescription(newHuntDialog.getDescription());
 						            	
 						            	huntService.saveHunt(h, new AsyncCallback<Hunt>(){
 											@Override
 											public void onFailure(Throwable caught) {
 												errorLabel.setText("Exception: " + caught.getMessage());
 											}
 
 											@Override
 											public void onSuccess(Hunt saved) {
 												errorLabel.setText("Successfully saved new hunt!");
 												addMarker(new HuntMarkerContent(saved, isMobileClient), false);
 												mappedHunts.put(saved.getId(), saved);
 											}
 										});
 						            	
 						            	LatLng there = LatLng.newInstance(c.getLatitude(), c.getLongitude());
 						            	errorLabel.setText("");
 						            	mapWidget.setCenter(there);
 						            }
 					            }, 
 					            posOptions
 					        );
 					    }
 					    else {
 					    	errorLabel.setText("geoloc NOT supported");
 					    }
 					}
 					else {
 						errorLabel.setText("Enter some text to describe what you're looking for.");
 					}
 				}
 			}
 		});
 	}
 	
 	private MarkerContent createMarkerContent(String token) {
 		MarkerContent content = null;
 		if(token != null) {
 			int idxHid = token.indexOf("hid");
 			if(idxHid > -1) {
 				long hid = -1;
 				int idxSid = token.indexOf("sid");
 				if(idxSid > -1) {
 					long sid = -1;
 					String strHid = token.substring(idxHid + "hid".length(), idxSid).trim();
 					String strSid = token.substring(idxSid + "sid".length()).trim();
 					try {
 						hid = Long.parseLong(strHid);
 						sid = Long.parseLong(strSid);
 						if(mappedHunts.containsKey(hid)) {
 							Hunt hunt = mappedHunts.get(hid);
 							for(Submission s : hunt.getSubmissions()) {
 								if(s.getId() == sid) {
 									content = new SubmissionMarkerContent(s);
 								}
 							}
 						}
 					}
 					catch(NumberFormatException ex) {
 						// eat if for now
 					}
 				}
 				else {
 					String strHid = token.substring(idxHid + "hid".length()).trim();
 					try {
 						hid = Long.parseLong(strHid);
 						if(mappedHunts.containsKey(hid)) {
 							Hunt hunt = mappedHunts.get(hid);
 							content = new HuntMarkerContent(hunt, isMobileClient);
 						}
 					}
 					catch(NumberFormatException ex) {
 						// eat if for now
 					}
 				}
 			}
 		}
 		return content;
 	}
 	
 	private void indexMarker(MarkerContent content) {
 		StringTokenizer izer = new StringTokenizer(content.getText());
 		List<MarkerContent> list;
 		String token;
 		
 		while(izer.hasMoreTokens()) {
 			token = izer.nextToken();
 			list = markerIndex.find(token);
 			
 			if(list == null) {
 				list = new LinkedList<MarkerContent>();
 				markerIndex.insert(token, list);
 			}
 			
 			if(!list.contains(content)) {
 				list.add(content);
 			}
 		}
 	}
 	
 	private void addMarker(final MarkerContent content, boolean shouldOpen) {
 		if(content != null) {
 			String key = content.uniqueKey();
 			if(!mappedMarkers.containsKey(key)) {
 			    final Marker marker = new Marker(content.getCoordinates());
 			    marker.setImage(content.getIconUrl());
 			    content.setMarker(marker);
 			    marker.addMarkerClickHandler(new MarkerClickHandler(){
 					@Override
 					public void onClick(MarkerClickEvent event) {
 						mapWidget.getInfoWindow().open(content.getMarker(), new InfoWindowContent(content.toHtml()));
 					}
 				});
 				mappedMarkers.put(key, content);
 				// index the marker the first time we map it
 				indexMarker(content);
				mapWidget.addOverlay(marker);
 			}
 			
 			if(shouldOpen) {
 				mapWidget.getInfoWindow().open(content.getMarker(), new InfoWindowContent(content.toHtml()));
 			}
 		}
 	}
 	
 	private void displaySubmissions(Hunt h) {
 		SubmissionsDialog dlg = new SubmissionsDialog(h);
 		dlg.show();
 		dlg.center();
 	}
 
 }
