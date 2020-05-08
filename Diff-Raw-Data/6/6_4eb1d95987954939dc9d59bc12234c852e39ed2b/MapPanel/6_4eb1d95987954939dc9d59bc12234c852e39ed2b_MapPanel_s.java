 package nl.sense_os.commonsense.client.visualization.map.components;
 
 import java.util.Date;
 import java.util.Map;
 
 import nl.sense_os.commonsense.client.common.CustomSlider;
 import nl.sense_os.commonsense.client.utility.Log;
 import nl.sense_os.commonsense.shared.sensorvalues.JsonValueModel;
 import nl.sense_os.commonsense.shared.sensorvalues.SensorValueModel;
 
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.data.TreeModel;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.SliderEvent;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.Label;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.Slider;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.extjs.gxt.ui.client.widget.layout.TableLayout;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.maps.client.InfoWindow;
 import com.google.gwt.maps.client.InfoWindowContent;
 import com.google.gwt.maps.client.MapWidget;
 import com.google.gwt.maps.client.control.LargeMapControl;
 import com.google.gwt.maps.client.event.MarkerClickHandler;
 import com.google.gwt.maps.client.event.MarkerDragEndHandler;
import com.google.gwt.maps.client.event.MarkerDragHandler;
import com.google.gwt.maps.client.event.MarkerClickHandler.MarkerClickEvent;
import com.google.gwt.maps.client.event.MarkerDragEndHandler.MarkerDragEndEvent;
import com.google.gwt.maps.client.event.MarkerDragHandler.MarkerDragEvent;
 import com.google.gwt.maps.client.geom.LatLng;
 import com.google.gwt.maps.client.overlay.Marker;
 import com.google.gwt.maps.client.overlay.MarkerOptions;
 import com.google.gwt.maps.client.overlay.Polyline;
 import com.google.gwt.user.client.ui.DockLayoutPanel;
 
 public class MapPanel extends ContentPanel {
 
 	private static final String TAG = "MapPanel";
 	private MapWidget map;
 	private Slider slider;
 	private LayoutContainer sliderContainer;
 	private Label sliderLabel;
 	private DockLayoutPanel dock;	
 	
 	private SensorValueModel[] sensorData;
 	// 24 hs -> 86400 secs
 	private int timeGranularity = 86400;
 	
 	
 	public MapPanel() {
 		// Panel settings. 
 		setLayout(new FitLayout());
 		setHeaderVisible(false);
 		setBodyBorder(false);
 		setScrollMode(Scroll.NONE);
 		setId("viz-map");
 
 		// @@ TODO: change this gwt widget by a gxt conatiner.
 		dock = new DockLayoutPanel(Unit.PX);	
 		
 		createMap();		
 		createSlider();
 		
 		// Add the dock to the panel.
 		this.add(dock);		
 	}
 
 	/**
 	 * Create a google map and add it to the north of the dock.
 	 */
 	private void createMap() {
 		// Create the map.
 		map = new MapWidget();
 		map.setSize("100%", "100%");
 		//map.setDraggable(true);
 
 		// Add some controls for the zoom level
 		map.addControl(new LargeMapControl());
 		map.setScrollWheelZoomEnabled(true);
 
 		// Add the map to the dock.
 		dock.addNorth(map, 500);
 	}
 
 	/**
 	 * Create a slider on the bottom, to filter the points to draw according to
 	 * a time specified with the slider.
 	 */
 	private void createSlider() {
 		final int increment = 22;
 		
 		// Slider added below the map.
 		//slider = new Slider();
 		slider = new CustomSlider(increment);
 		slider.setWidth(215);
 		slider.setHeight(30);
 		slider.setMinValue(0);
 		slider.setMaxValue(214);
 		slider.setIncrement(increment);
 		slider.setMessage("{0} days ago"); 
 		slider.setId("viz-map-slider");
 
 		// Slider label.
 		sliderLabel = new Label();
 		sliderLabel.setWidth(50);
 		
 		// This container adds another background image to the slider.		
 		sliderContainer = new LayoutContainer();
 		sliderContainer.setId("custom-slider");
 		sliderContainer.setLayout(new TableLayout(2));
 		sliderContainer.add(slider);
 		sliderContainer.add(sliderLabel);
 		
 		dock.addSouth(sliderContainer, 30);
 		
 		// Listener to filter the points to draw on the map.
 		slider.addListener(Events.Change, new Listener<SliderEvent>() {
 			@Override
 			public void handleEvent(SliderEvent be) {				
 				int time = be.getNewValue() / increment;
 				
 				Log.d(TAG, "time: " + time);
 				
 				// Update the map according to the selected time.
 				updateMap(time);
 			}
 		});
 	}
 	
 	/**
 	 * This method is called when it used the slider below the map.
 	 * It filters the points to draw a line depending on the time.
 	 * 
 	 * @param time
 	 */
 	private void updateMap(int time) {
 		// Clean the map.
 		map.clearOverlays();
 
 		// Draw the filtered points.
 		if (sensorData.length > 0) {
 			LatLng[] points = new LatLng[sensorData.length];
 
 			long minTimeFilter = 0;
 
 			// The last drawn point's timestamp is used to set the min time filter.
 			if (time != 0) {
 				JsonValueModel v = (JsonValueModel) sensorData[sensorData.length - 1];
 				Date t = v.getTimestamp();
 				
 				// time filter in secs
 				minTimeFilter = t.getTime() / 1000;
 				minTimeFilter -= (timeGranularity * time);				
 			}
 
 			int lastPoint = 0;
 
 			// All the points greater than the minTimeFilter will be drawn.
 			for (int i = 0, j = 0; i < sensorData.length; i++) {
 				JsonValueModel value = (JsonValueModel) sensorData[i];
 				Map<String, Object> fields = value.getFields();
 
 				double latitude = (Double) fields.get("latitude");
 				double longitude = (Double) fields.get("longitude");
 				
 				// timestamp in secs
 				long timestamp = value.getTimestamp().getTime() / 1000;
 				
 				//Log.d(TAG, "minTimeFilter: "+minTimeFilter);
 				//Log.d(TAG, "timestamp: "+timestamp);
 				
 				if (timestamp > minTimeFilter) {
 					lastPoint = j;
 					points[j++] = LatLng.newInstance(latitude, longitude);
 				}
 			}
 
 			// Add the first marker
 			Marker startMarker = new Marker(points[0]);
 			startMarker.setDraggingEnabled(true);
 			map.addOverlay(startMarker);
 
 			// Add the last marker
 			Marker endMarker = new Marker(points[lastPoint]);
 			endMarker.setDraggingEnabled(true);
 			map.addOverlay(endMarker);
 			
 			// Draw a track line
 			Polyline trace = new Polyline(points);
 			map.addOverlay(trace);
 
 			// Center the map
 			map.setCenter(endMarker.getLatLng());
 			map.setZoomLevel(13);
 		}		
 	}
 
 	/**
 	 * Display the markers and draw a trace line on the map.
 	 * 
 	 * @param sensor
 	 * @param data
 	 */
 	public void addData(TreeModel sensor, SensorValueModel[] data) {
 		// Store the sensor data to be used from other methods.
 		sensorData = data;
 
 		// Set the range of values for the slider according to the 
 		// difference between the first marker's time and the last one.
 		JsonValueModel v = (JsonValueModel) data[0];	
 		int min = convertTimestampToNrOfDays(v.getTimestamp().getTime());
 		v = (JsonValueModel) data[data.length - 1];
 		int max = convertTimestampToNrOfDays(v.getTimestamp().getTime());
 		int days = max - min;
 		Log.d(TAG, "days: " + days);		
 		slider.setMaxValue(days);
 
 		// By default the time granularity of the slider is in days.
 		if (days < 31) {
 			// 24 hs -> 3600 seconds
 			timeGranularity = 3600;
 			
 			slider.setMessage("{0} hours ago");
 			slider.setMaxValue(days * 24);
 			
 			sliderLabel.setText("hours");
 			
 		} else {			
 			sliderLabel.setText("days");
 		}
 		
 		// Draw markers and a trace line on the map.
 		if (data.length > 0) {
 			final LatLng[] points = new LatLng[data.length];
 			
 			// Store the points in an array.
 			for (int i = 0; i < data.length; i++) {
 				JsonValueModel value = (JsonValueModel) data[i];
 				Map<String, Object> fields = value.getFields();
 
 				double latitude = (Double) fields.get("latitude");
 				double longitude = (Double) fields.get("longitude");
 
 				points[i] = LatLng.newInstance(latitude, longitude);
 			}
 
 			// Add the first marker
 			MarkerOptions markerOpt = MarkerOptions.newInstance();
 			markerOpt.setDraggable(true);
 			Marker startMarker = new Marker(points[0], markerOpt);
 			map.addOverlay(startMarker);
 
 			// This handler looks for the closest point between the point
 			// where the marker is dropped and the stored points in the array.
 			// Then, it moves the marker to the closest point. 
 			startMarker.addMarkerDragEndHandler(new MarkerDragEndHandler() {
 				private int pointId = 0;
 				
 				@Override
 				public void onDragEnd(MarkerDragEndEvent event) {
 					Marker marker = event.getSender();
 					LatLng currPoint = marker.getLatLng();
 
 					double currDst = points[0].distanceFrom(currPoint);
 					double minDst = currDst;
 					
 					for (int i = 1; i < points.length; i++) {
 						double nextDst = points[i].distanceFrom(currPoint);
 						
 						if (nextDst < minDst) {
 							minDst = nextDst;
 							pointId = i;
 						}
 					}
 					marker.setLatLng(points[pointId]);
 				}
 			});
 
 			startMarker.addMarkerClickHandler(new MarkerClickHandler() {
 				@Override
 				public void onClick(MarkerClickEvent event) {
 					Marker marker = event.getSender();
 					final InfoWindow info = map.getInfoWindow();
 					final InfoWindowContent content = new InfoWindowContent("starting point");
 					info.open(marker, content);
 				}
 			});
 			
 			// Add the last marker
 			//Marker endMarker = new Marker(points[points.length - 1]);
 			//map.addOverlay(endMarker);
 
 			// Draw a trace line through the points.
 			Polyline trace = new Polyline(points);
 			map.addOverlay(trace);
 
 			// Center based on both markers
 			/*
 			 * LatLngBounds bounds = map.getBounds();
 			 * bounds.extend(startMarker.getLatLng());
 			 * bounds.extend(endMarker.getLatLng()); 
 			 * 
 			 * int zoomLevel = map.getBoundsZoomLevel(bounds); 
 			 * Log.d(TAG, "zoom level: " + zoomLevel);
 			 */
 
 			// Adjust the zoom level according to the bounds.
 			/* 
 			 * if (zoomLevel < 9)
 			 * 	map.setZoomLevel(zoomLevel); 
 			 * else
 			 * 	map.setZoomLevel(12);
 			 *  
 			 * map.setCenter(bounds.getCenter(),
 			 * map.getBoundsZoomLevel(bounds));
 			 */
 
 			map.setCenter(startMarker.getLatLng());
 			map.setZoomLevel(13);
 
 		} else {
 			Log.d(TAG, "no data.");
 		}
 	}
 
 	/**
 	 * Should display something to show the user that we are done loading data.
 	 */
 	public void finishLoading() {
 		Log.d(TAG, "Finished loading!");
 	}
 
 	/**
 	 * Convert from a timestamp value to a number of days of the year.
 	 * 
 	 * @param timestamp
 	 * @return
 	 */
 	private static final int convertTimestampToNrOfDays(long timestamp) {
 		return (int) timestamp / 1000 / 60 / 24;
 	}
 }
