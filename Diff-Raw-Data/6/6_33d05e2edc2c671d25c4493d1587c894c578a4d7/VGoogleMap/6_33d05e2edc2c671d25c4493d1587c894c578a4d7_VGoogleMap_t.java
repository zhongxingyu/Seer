 package org.vaadin.hezamu.googlemapwidget.widgetset.client.ui;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.RequestException;
 import com.google.gwt.http.client.RequestTimeoutException;
 import com.google.gwt.http.client.Response;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONBoolean;
 import com.google.gwt.json.client.JSONNumber;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONParser;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.json.client.JSONValue;
 import com.google.gwt.maps.client.Copyright;
 import com.google.gwt.maps.client.CopyrightCollection;
 import com.google.gwt.maps.client.InfoWindowContent;
 import com.google.gwt.maps.client.MapType;
 import com.google.gwt.maps.client.MapWidget;
 import com.google.gwt.maps.client.TileLayer;
 import com.google.gwt.maps.client.control.Control;
 import com.google.gwt.maps.client.control.HierarchicalMapTypeControl;
 import com.google.gwt.maps.client.control.LargeMapControl;
 import com.google.gwt.maps.client.control.MapTypeControl;
 import com.google.gwt.maps.client.control.MenuMapTypeControl;
 import com.google.gwt.maps.client.control.OverviewMapControl;
 import com.google.gwt.maps.client.control.ScaleControl;
 import com.google.gwt.maps.client.control.SmallMapControl;
 import com.google.gwt.maps.client.control.SmallZoomControl;
 import com.google.gwt.maps.client.event.MapClickHandler;
 import com.google.gwt.maps.client.event.MapMoveEndHandler;
 import com.google.gwt.maps.client.event.MarkerClickHandler;
 import com.google.gwt.maps.client.geom.LatLng;
 import com.google.gwt.maps.client.geom.LatLngBounds;
 import com.google.gwt.maps.client.geom.MercatorProjection;
 import com.google.gwt.maps.client.geom.Point;
 import com.google.gwt.maps.client.geom.Size;
 import com.google.gwt.maps.client.overlay.Icon;
 import com.google.gwt.maps.client.overlay.Marker;
 import com.google.gwt.maps.client.overlay.MarkerOptions;
 import com.google.gwt.maps.client.overlay.Overlay;
 import com.google.gwt.maps.client.overlay.Polygon;
 import com.google.gwt.maps.client.overlay.PolygonOptions;
 import com.google.gwt.maps.client.overlay.Polyline;
 import com.google.gwt.maps.client.overlay.PolylineOptions;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.DeferredCommand;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Widget;
 import com.vaadin.terminal.gwt.client.ApplicationConnection;
 import com.vaadin.terminal.gwt.client.Paintable;
 import com.vaadin.terminal.gwt.client.UIDL;
 
 /**
  * Client side widget which communicates with the server. Messages from the
  * server are shown as HTML and mouse clicks are sent to the server.
  */
 public class VGoogleMap extends Composite implements Paintable,
 		MapClickHandler, MapMoveEndHandler {
 
 	/** Set the CSS class name to allow styling. */
 	public static final String CLASSNAME = "v-googlemap";
 
 	public static final String CLICK_EVENT_IDENTIFIER = "click";
 
 	/** The client side widget identifier */
 	protected String paintableId;
 
 	/** Reference to the server connection object. */
 	protected ApplicationConnection client;
 
 	private final MapWidget map = new MapWidget();
 
 	private final Map<String, Marker> knownMarkers = new HashMap<String, Marker>();
 
 	private final Map<Integer, Overlay> knownPolygons = new HashMap<Integer, Overlay>();
 
 	private boolean ignoreVariableChanges = true;
 
 	private long markerRequestSentAt;
 
 	private boolean eggActive = false;
 
 	private Marker eggMarker;
 
 	private List<MapControl> controls = new ArrayList<MapControl>();
 
 	public enum MapControl {
 		SmallMapControl, HierarchicalMapTypeControl, LargeMapControl, MapTypeControl, MenuMapTypeControl, OverviewMapControl, ScaleControl, SmallZoomControl
 	}
 
 	class CustomTileLayer extends TileLayer {
 		private final String tileUrl;
 		private final boolean isPng;
 		private final double opacity;
 
 		public CustomTileLayer(CopyrightCollection copyColl, int minZoom,
 				int maxZoom, String tileUrl, boolean isPng, double opacity) {
 			super(copyColl, minZoom, maxZoom);
 
 			this.tileUrl = tileUrl;
 			this.isPng = isPng;
 			this.opacity = opacity;
 		}
 
 		@Override
 		public boolean isPng() {
 			return isPng;
 		}
 
 		@Override
 		public double getOpacity() {
 			return opacity;
 		}
 
 		@Override
 		public String getTileURL(Point tile, int zoomLevel) {
 			String url = tileUrl.replace("{X}", "" + tile.getX()).replace(
 					"{Y}", "" + tile.getY()).replace("{ZOOM}", "" + zoomLevel);
 
 			return url;
 		}
 	}
 
 	/**
 	 * The constructor should first call super() to initialize the component and
 	 * then handle any initialization relevant to Vaadin.
 	 */
 	public VGoogleMap() {
 		initWidget(map); // All Composites need to call initWidget()
 
 		// This method call of the Paintable interface sets the component
 		// style name in DOM tree
 		setStyleName(CLASSNAME);
 
 		map.addMapMoveEndHandler(this);
 
 		map.addMapClickHandler(this);
 	}
 
 	/**
 	 * Called whenever an update is received from the server
 	 */
 	public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
 		// This call should be made first.
 		// It handles sizes, captions, tooltips, etc. automatically.
 		if (client.updateComponent(this, uidl, true)) {
 			return;
 		}
 
 		// Save reference to server connection object to be able to send
 		// user interaction later
 		this.client = client;
 
 		// Save the client side identifier (paintable id) for the widget
 		paintableId = uidl.getId();
 
 		if (uidl.hasAttribute("cached") && uidl.getBooleanAttribute("cached")) {
 			return;
 		}
 
 		addEasterEggIcon();
 
 		eggMarker.setVisible(map.getZoomLevel() >= 13);
 
 		long start = System.currentTimeMillis();
 
 		// Do not send any variable changes while changing the map
 		ignoreVariableChanges = true;
 
 		int newZoom = uidl.getIntVariable("zoom");
 		if (map.getZoomLevel() != newZoom) {
 			map.setZoomLevel(newZoom);
 		}
 
 		LatLng newCenter = LatLng.newInstance(uidl
 				.getDoubleVariable("center_lat"), uidl
 				.getDoubleVariable("center_lng"));
 
 		boolean scrollWheelZoomEnabled = uidl.getBooleanVariable("swze");
 		if (map.isScrollWheelZoomEnabled() != scrollWheelZoomEnabled) {
 			map.setScrollWheelZoomEnabled(scrollWheelZoomEnabled);
 		}
 
 		if (map.getCenter().getLatitude() != newCenter.getLatitude()
 				|| map.getCenter().getLongitude() != newCenter.getLongitude()) {
 			map.setCenter(newCenter);
 		}
 
 		for (MapControl control : MapControl.values()) {
 			if (uidl.hasAttribute(control.name())) {
 				if (!controls.contains(control)) {
 					map.addControl(newControl(control));
 					controls.add(control);
 				}
 			} else if (controls.contains(control)) {
 				map.removeControl(newControl(control));
 				controls.add(control);
 			}
 		}
 
 		if (uidl.hasAttribute("markerRes")) {
 			String markerUrl = client.translateVaadinUri(uidl
 					.getStringAttribute("markerRes"));
 			if (markerUrl != null) {
 				DeferredCommand
 						.addCommand(new MarkerRetrieveCommand(markerUrl));
 			}
 		}
 
 		if (uidl.hasAttribute("marker")) {
			// When adding the markers we get the ID from JSONString.toString()
			// which includes quotation marks around the ID.
			String markerId = "\"" + uidl.getStringAttribute("marker") + "\"";

			Marker marker = knownMarkers.get(markerId);
 
 			for (final Iterator<Object> it = uidl.getChildIterator(); it
 					.hasNext();) {
 				final UIDL u = (UIDL) it.next();
 				if (!u.getTag().equals("tabs")) {
 					continue;
 				}
 
 				if (u.getChildCount() == 0) {
 					log("No contents for info window");
 				} else if (u.getChildCount() == 1) {
 					// Only one component in the info window -> no tabbing
 					UIDL paintableUIDL = u.getChildUIDL(0).getChildUIDL(0);
 					Paintable paintable = client.getPaintable(paintableUIDL);
 
 					map.getInfoWindow().open(marker.getLatLng(),
 							new InfoWindowContent((Widget) paintable));
 
 					// Update components in the info window after adding them to
 					// DOM so that size calculations can succeed
 					paintable.updateFromUIDL(paintableUIDL, client);
 				} else {
 					int tabs = u.getChildCount();
 					// More than one component, show them in info window tabs
 					InfoWindowContent.InfoWindowTab[] infoTabs = new InfoWindowContent.InfoWindowTab[tabs];
 
 					Paintable[] paintables = new Paintable[tabs];
 					UIDL[] uidls = new UIDL[tabs];
 
 					int selectedId = 0;
 					for (int i = 0; i < u.getChildCount(); i++) {
 						UIDL childUIDL = u.getChildUIDL(i);
 						if (selectedId == 0
 								&& childUIDL.getBooleanAttribute("selected")) {
 							selectedId = i;
 						}
 
 						String label = childUIDL.getStringAttribute("label");
 
 						UIDL paintableUIDL = childUIDL.getChildUIDL(0);
 						Paintable paintable = client
 								.getPaintable(paintableUIDL);
 
 						paintables[i] = paintable;
 						uidls[i] = paintableUIDL;
 
 						infoTabs[i] = new InfoWindowContent.InfoWindowTab(
 								label, (Widget) paintable);
 					}
 
 					map.getInfoWindow().open(marker.getLatLng(),
 							new InfoWindowContent(infoTabs, selectedId));
 
 					// Update paintables after adding them to DOM so that
 					// size calculations can succeed
 					for (int i = 0; i < paintables.length; i++) {
 						paintables[i].updateFromUIDL(uidls[i], client);
 					}
 				}
 			}
 		}
 
 		if (uidl.hasAttribute("clearMapTypes")) {
 			for (MapType type : map.getMapTypes()) {
 				map.removeMapType(type);
 			}
 		}
 
 		// Process polygon/polyline overlays and map types
 		for (final Iterator<Object> it = uidl.getChildIterator(); it.hasNext();) {
 			final UIDL u = (UIDL) it.next();
 			if (u.getTag().equals("overlays")) {
 
 				long nodeStart = System.currentTimeMillis();
 
 				for (final Iterator<Object> iter = u.getChildIterator(); iter
 						.hasNext();) {
 					final UIDL polyUIDL = (UIDL) iter.next();
 
 					Overlay poly = null;
 					if (polyUIDL.hasAttribute("fillcolor")) {
 						poly = polygonFromUIDL(polyUIDL);
 					} else {
 						poly = polylineFromUIDL(polyUIDL);
 					}
 
 					if (poly != null) {
 						knownPolygons.put(polyUIDL.getIntAttribute("id"), poly);
 						map.addOverlay(poly);
 					}
 				}
 
 				log("Polygon overlays processed in "
 						+ (System.currentTimeMillis() - nodeStart) + "ms");
 			} else if (u.getTag().equals("mapTypes")) {
 				long nodeStart = System.currentTimeMillis();
 
 				for (final Iterator<Object> iter = u.getChildIterator(); iter
 						.hasNext();) {
 					map.addMapType(mapTypeFromUIDL((UIDL) iter.next()));
 				}
 
 				log("Map types processed in "
 						+ (System.currentTimeMillis() - nodeStart) + "ms");
 			}
 		}
 
 		if (uidl.hasAttribute("closeInfoWindow")) {
 			map.getInfoWindow().close();
 		}
 
 		ignoreVariableChanges = false;
 
 		log("IGoogleMap.updateFromUIDL() took "
 				+ (System.currentTimeMillis() - start) + "ms");
 	}
 
 	private MapType mapTypeFromUIDL(UIDL maptypeUIDL) {
 		int minZoom = maptypeUIDL.getIntAttribute("minZoom");
 		int maxZoom = maptypeUIDL.getIntAttribute("maxZoom");
 		String copyright = maptypeUIDL.getStringAttribute("copyright");
 		String name = maptypeUIDL.getStringAttribute("name");
 		String tileUrl = maptypeUIDL.getStringAttribute("tileUrl");
 		boolean isPng = maptypeUIDL.getBooleanAttribute("isPng");
 		double opacity = maptypeUIDL.getDoubleAttribute("opacity");
 
 		CopyrightCollection myCopyright = new CopyrightCollection();
 
 		myCopyright.addCopyright(new Copyright(1, LatLngBounds.newInstance(
 				LatLng.newInstance(-90, -180), LatLng.newInstance(90, 180)),
 				minZoom, copyright));
 
 		return new MapType(new TileLayer[] { new CustomTileLayer(myCopyright,
 				minZoom, maxZoom, tileUrl, isPng, opacity) },
 				new MercatorProjection(maxZoom - minZoom + 1), name);
 	}
 
 	private Control newControl(MapControl control) {
 		if (control.equals(MapControl.SmallMapControl)) {
 			return new SmallMapControl();
 		}
 		if (control.equals(MapControl.HierarchicalMapTypeControl)) {
 			return new HierarchicalMapTypeControl();
 		}
 		if (control.equals(MapControl.LargeMapControl)) {
 			return new LargeMapControl();
 		}
 		if (control.equals(MapControl.MapTypeControl)) {
 			return new MapTypeControl();
 		}
 		if (control.equals(MapControl.MenuMapTypeControl)) {
 			return new MenuMapTypeControl();
 		}
 		if (control.equals(MapControl.OverviewMapControl)) {
 			return new OverviewMapControl();
 		}
 		if (control.equals(MapControl.ScaleControl)) {
 			return new ScaleControl();
 		}
 		if (control.equals(MapControl.SmallZoomControl)) {
 			return new SmallZoomControl();
 		}
 
 		log("Unknown control: " + control);
 
 		return null;
 	}
 
 	private Polyline polylineFromUIDL(UIDL polyUIDL) {
 		String[] encodedPoints = polyUIDL.getStringAttribute("points").split(
 				" ");
 		LatLng[] points = new LatLng[encodedPoints.length];
 		for (int i = 0; i < encodedPoints.length; i++) {
 			String[] p = encodedPoints[i].split(",");
 			double lat = Double.parseDouble(p[0]);
 			double lng = Double.parseDouble(p[1]);
 			points[i] = LatLng.newInstance(lat, lng);
 		}
 
 		String color = polyUIDL.getStringAttribute("color");
 		int weight = polyUIDL.getIntAttribute("weight");
 		double opacity = polyUIDL.getDoubleAttribute("opacity");
 		boolean clickable = polyUIDL.getBooleanAttribute("clickable");
 
 		return new Polyline(points, color, weight, opacity, PolylineOptions
 				.newInstance(clickable, false));
 	}
 
 	private Polygon polygonFromUIDL(UIDL polyUIDL) {
 		String[] encodedPoints = polyUIDL.getStringAttribute("points").split(
 				" ");
 		LatLng[] points = new LatLng[encodedPoints.length];
 		for (int i = 0; i < encodedPoints.length; i++) {
 			String[] p = encodedPoints[i].split(",");
 			double lat = Double.parseDouble(p[0]);
 			double lng = Double.parseDouble(p[1]);
 			points[i] = LatLng.newInstance(lat, lng);
 		}
 
 		String color = polyUIDL.getStringAttribute("color");
 		int weight = polyUIDL.getIntAttribute("weight");
 		double opacity = polyUIDL.getDoubleAttribute("opacity");
 		String fillColor = polyUIDL.getStringAttribute("fillcolor");
 		double fillOpacity = polyUIDL.getDoubleAttribute("fillopacity");
 		boolean clickable = polyUIDL.getBooleanAttribute("clickable");
 
 		return new Polygon(points, color, weight, opacity, fillColor,
 				fillOpacity, PolygonOptions.newInstance(clickable));
 	}
 
 	private Marker createMarker(JSONNumber jsLat, JSONNumber jsLng,
 			JSONString jsTitle, JSONBoolean jsVisible, JSONString jsIcon,
 			JSONBoolean jsDraggable) {
 
 		Icon icon = null;
 		if (jsIcon != null) {
 			icon = Icon.newInstance(jsIcon.stringValue());
 		}
 
 		MarkerOptions mopts;
 		if (icon != null) {
 			mopts = MarkerOptions.newInstance(icon);
 		} else {
 			mopts = MarkerOptions.newInstance();
 		}
 
 		mopts.setTitle(jsTitle.stringValue());
 		mopts.setDraggable(jsDraggable.booleanValue());
 
 		final double lat = jsLat.doubleValue();
 		final double lng = jsLng.doubleValue();
 
 		if (lat < -90 || lat > 90) {
 			log("Invalid latitude for marker: " + lat);
 			return null;
 		}
 
 		if (lng < -180 || lng > 180) {
 			log("Invalid latitude for marker: " + lat);
 			return null;
 		}
 
 		return new Marker(LatLng.newInstance(lat, lng), mopts);
 	}
 
 	public void onClick(MapClickEvent event) {
 		if (ignoreVariableChanges) {
 			return;
 		}
 
 		if (event.getOverlay() != null) {
 			return;
 		}
 
 		client.updateVariable(paintableId, "click_pos", event.getLatLng()
 				.toString(), true);
 	}
 
 	public void onMoveEnd(MapMoveEndEvent event) {
 		if (ignoreVariableChanges) {
 			return;
 		}
 
 		client.updateVariable(paintableId, "zoom", map.getZoomLevel(), false);
 		client.updateVariable(paintableId, "bounds_ne", map.getBounds()
 				.getNorthEast().toString(), false);
 		client.updateVariable(paintableId, "bounds_sw", map.getBounds()
 				.getSouthWest().toString(), false);
 		client.updateVariable(paintableId, "center",
 				map.getCenter().toString(), true);
 
 		eggMarker.setVisible(map.getZoomLevel() >= 13);
 	}
 
 	protected void markerClicked(String mId) {
 		client.updateVariable(paintableId, "marker", mId, true);
 	}
 
 	private void log(String message) {
 		// Show message in GWT console
 		System.out.println(message);
 
 		// And also in Vaadin debug window
 		ApplicationConnection.getConsole().log(message);
 	}
 
 	private void addEasterEggIcon() {
 		if (eggMarker != null) {
 			return;
 		}
 
 		Icon icon = Icon.newInstance(client
 				.translateVaadinUri("theme://icon/vaadin-logo.png"));
 
 		icon.setIconSize(Size.newInstance(32, 32));
 		icon.setIconAnchor(Point.newInstance(16, 16));
 
 		MarkerOptions mopts = MarkerOptions.newInstance(icon);
 
 		eggMarker = new Marker(LatLng.newInstance(60.4522, 22.3), mopts);
 
 		eggMarker.addMarkerClickHandler(new MarkerClickHandler() {
 			public void onClick(MarkerClickEvent event) {
 				toggleEasterEgg();
 			}
 		});
 
 		map.addOverlay(eggMarker);
 	}
 
 	// Easter egg :) Toggle a rotating 3d cube at IT Mill offices. Too bad
 	// it flickers a lot with the current version of the Maps API...
 	private void toggleEasterEgg() {
 		eggActive = !eggActive;
 		if (!eggActive) {
 			for (Overlay poly : knownPolygons.values()) {
 				map.addOverlay(poly);
 			}
 			return;
 		}
 
 		// Hide regular polygons
 		for (Overlay poly : knownPolygons.values()) {
 			map.removeOverlay(poly);
 		}
 
 		Timer timer = new Timer() {
 			double angle = 0;
 
 			// z = -1
 			double[][] backpoints = new double[][] { { -0.01, 0.01, 0.01 },
 					{ -0.01, 0.01, -0.01 }, { -0.01, -0.01, -0.01 },
 					{ -0.01, -0.01, 0.01 } };
 
 			// z = 1
 			double[][] frontpoints = new double[][] { { 0.01, 0.01, 0.01 },
 					{ 0.01, 0.01, -0.01 }, { 0.01, -0.01, -0.01 },
 					{ 0.01, -0.01, 0.01 } };
 
 			// {z, x, y}
 			int[] axis = new int[] { 0, 2, 1 };
 
 			double[] origo = new double[] {
 					eggMarker.getLatLng().getLongitude(),
 					eggMarker.getLatLng().getLatitude() };
 
 			Polygon[] polys = new Polygon[6];
 
 			String[] colors = new String[] { "#ff0000", "#00ff00", "#0000ff",
 					"#00ffff", "#ff00ff", "#ffff00" };
 
 			int frametime = 75;
 
 			@Override
 			public void run() {
 				long start = System.currentTimeMillis();
 
 				for (Polygon poly : polys) {
 					if (poly != null) {
 						map.removeOverlay(poly);
 					}
 				}
 
 				// Canceled: return and do not reschedule
 				if (!eggActive) {
 					return;
 				}
 
 				angle += 0.05;
 				angle %= Math.PI * 2;
 
 				double[][] pts1 = rotateProjectTranslate(backpoints, angle,
 						axis, origo);
 
 				double[][] pts2 = rotateProjectTranslate(frontpoints, angle,
 						axis, origo);
 
 				// This is slow so do this only once per coordinate
 				LatLng[] pcoords = new LatLng[] {
 						LatLng.newInstance(pts1[0][1], pts1[0][0]),
 						LatLng.newInstance(pts1[1][1], pts1[1][0]),
 						LatLng.newInstance(pts1[2][1], pts1[2][0]),
 						LatLng.newInstance(pts1[3][1], pts1[3][0]),
 
 						LatLng.newInstance(pts2[0][1], pts2[0][0]),
 						LatLng.newInstance(pts2[1][1], pts2[1][0]),
 						LatLng.newInstance(pts2[2][1], pts2[2][0]),
 						LatLng.newInstance(pts2[3][1], pts2[3][0]) };
 
 				// Build polygon vertices
 				LatLng[][] polyps = new LatLng[][] {
 						{ pcoords[0], pcoords[1], pcoords[2], pcoords[3],
 								pcoords[0] },
 						{ pcoords[0], pcoords[4], pcoords[7], pcoords[3],
 								pcoords[0] },
 						{ pcoords[2], pcoords[3], pcoords[7], pcoords[6],
 								pcoords[2] },
 						{ pcoords[0], pcoords[4], pcoords[5], pcoords[1],
 								pcoords[0] },
 						{ pcoords[1], pcoords[5], pcoords[6], pcoords[2],
 								pcoords[1] },
 						{ pcoords[4], pcoords[5], pcoords[6], pcoords[7],
 								pcoords[4] } };
 
 				// Build and add polygons to the map
 				for (int i = 0; i < polys.length; i++) {
 					polys[i] = new Polygon(polyps[i], "#ffffff", 0, 0,
 							colors[i], 0.25);
 					map.addOverlay(polys[i]);
 				}
 
 				// Try to sync frame length
 				int sleeptime = Math.max(1, frametime
 						- (int) (System.currentTimeMillis() - start));
 
 				schedule(sleeptime);
 			}
 		};
 
 		timer.schedule(1);
 	}
 
 	class InfoWindowOpener implements MarkerClickHandler {
 		private String markerId;
 
 		InfoWindowOpener(String markerId) {
 			super();
 			this.markerId = markerId;
 		}
 
 		public void onClick(MarkerClickEvent event) {
 			markerClicked(markerId);
 		}
 	}
 
 	class MarkerRetrieveCommand implements Command {
 		private String markerUrl;
 
 		MarkerRetrieveCommand(String markerUrl) {
 			super();
 			this.markerUrl = markerUrl;
 		}
 
 		public void execute() {
 			RequestBuilder builder = new RequestBuilder(RequestBuilder.GET,
 					markerUrl);
 
 			try {
 				builder.setTimeoutMillis(2000);
 
 				markerRequestSentAt = System.currentTimeMillis();
 
 				builder.sendRequest(null, new RequestCallback() {
 					public void onError(Request request, Throwable e) {
 						if (e instanceof RequestTimeoutException) {
 							log("Timeout fetching marker data: "
 									+ e.getMessage());
 						} else {
 							log("Error fetching marker data: " + e.getMessage());
 						}
 					}
 
 					public void onResponseReceived(Request request,
 							Response response) {
 						String markerJSON = response.getText();
 
 						System.out
 								.println(""
 										+ markerJSON.length()
 										+ " bytes of marker response got in "
 										+ (System.currentTimeMillis() - markerRequestSentAt)
 										+ "ms");
 
 						JSONArray array = null;
 						try {
 							long start = System.currentTimeMillis();
 							JSONValue json = JSONParser.parse(markerJSON);
 							array = json.isArray();
 							log("JSON parsed in "
 									+ (System.currentTimeMillis() - start)
 									+ "ms");
 							if (array == null) {
 								System.out
 										.println("Marker JSON was not an array.");
 								return;
 							}
 
 							handleMarkerJSON(array);
 						} catch (Exception e) {
 							log("Error parsing json: " + e.getMessage());
 						}
 					}
 				});
 			} catch (RequestException e) {
 				log("Failed to send the request: " + e.getMessage());
 			}
 		}
 
 		private void handleMarkerJSON(JSONArray array) {
 			synchronized (knownMarkers) {
 				JSONValue value;
 				long startTime = System.currentTimeMillis();
 				int initSize = knownMarkers.size();
 
 				for (int i = 0; i < array.size(); i++) {
 					JSONObject jsMarker;
 					JSONString jsMID, jsTitle, jsIcon;
 					JSONNumber jsLat, jsLng;
 					JSONBoolean jsVisible, jsHasInfo, jsDraggable;
 
 					if ((jsMarker = array.get(i).isObject()) == null) {
 						continue;
 					}
 
 					// Read marker id
 					if ((value = jsMarker.get("mid")) == null) {
 						continue;
 					}
 					if ((jsMID = value.isString()) == null) {
 						continue;
 					}
 
 					// Skip known markers
 					if (knownMarkers.containsKey(jsMID.toString())) {
 						continue;
 					}
 
 					// Read marker latitude
 					if ((value = jsMarker.get("lat")) == null) {
 						continue;
 					}
 					if ((jsLat = value.isNumber()) == null) {
 						continue;
 					}
 
 					// Read marker longitude
 					if ((value = jsMarker.get("lng")) == null) {
 						continue;
 					}
 					if ((jsLng = value.isNumber()) == null) {
 						continue;
 					}
 
 					// Read marker title
 					if ((value = jsMarker.get("title")) == null) {
 						continue;
 					}
 					if ((jsTitle = value.isString()) == null) {
 						continue;
 					}
 
 					// Read marker visibility
 					if ((value = jsMarker.get("visible")) == null) {
 						continue;
 					}
 					if ((jsVisible = value.isBoolean()) == null) {
 						continue;
 					}
 
 					// Read marker icon
 					if ((value = jsMarker.get("icon")) == null) {
 						jsIcon = null;
 					} else if ((jsIcon = value.isString()) == null) {
 						continue;
 					}
 
 					// Read marker draggability (is that a word? :)
 					if ((value = jsMarker.get("draggable")) == null) {
 						continue;
 					}
 					if ((jsDraggable = value.isBoolean()) == null) {
 						continue;
 					}
 
 					Marker marker = createMarker(jsLat, jsLng, jsTitle,
 							jsVisible, jsIcon, jsDraggable);
 
 					if (marker != null) {
 						map.addOverlay(marker);
 
 						// Read boolean telling if marker has a info window
 						if ((value = jsMarker.get("info")) != null) {
 							if ((jsHasInfo = value.isBoolean()) != null
 									&& jsHasInfo.booleanValue()) {
 								marker
 										.addMarkerClickHandler(new InfoWindowOpener(
 												jsMID.stringValue()));
 							}
 						}
 
 						knownMarkers.put(jsMID.toString(), marker);
 					}
 				}
 
 				int newMarkers = knownMarkers.size() - initSize;
 
 				long dur = System.currentTimeMillis() - startTime;
 
 				if (newMarkers == 0) {
 					log("No new markers added in " + dur + "ms.");
 				} else {
 					log("" + newMarkers + " markers added in " + dur + "ms: "
 							+ dur / newMarkers + "ms per marker");
 				}
 			}
 		}
 	}
 
 	@Override
 	public void setHeight(String height) {
 		super.setHeight(height);
 		map.setHeight(height);
 	}
 
 	@Override
 	public void setWidth(String width) {
 		super.setWidth(width);
 		map.setWidth(width);
 	}
 
 	private static double[][] rotateProjectTranslate(double[][] points,
 			double angle, int[] axis, double[] origo) {
 
 		int camera_z = 100;
 
 		double[][] result = new double[points.length][3];
 
 		double[][] rotated = rotate3Dpoints(points, angle, axis);
 
 		for (int i = 0; i < rotated.length; i++) {
 			double p_x = rotated[i][1] * camera_z / (100 - rotated[i][0])
 					+ origo[0];
 			double p_y = rotated[i][2] * camera_z / (100 - rotated[i][0])
 					+ origo[1];
 
 			result[i] = new double[] { p_x, p_y };
 		}
 
 		return result;
 	}
 
 	// Math adapted from http://maettig.com/code/javascript/3d_dots.html
 	private static double[][] rotate3Dpoints(double[][] points, double angle,
 			int[] axis) {
 
 		double cosx = Math.cos(angle * axis[1]);
 		double sinx = Math.sin(angle * axis[1]);
 		double cosy = axis[1] == axis[2] ? cosx : Math.cos(angle * axis[2]);
 		double cosb = axis[1] == axis[2] ? sinx : Math.sin(angle * axis[2]);
 
 		double[][] rotated = new double[points.length][3];
 		for (int i = 0; i < points.length; i++) {
 			double z = points[i][0];
 			double x = points[i][1];
 			double y = points[i][2];
 
 			double u2 = x * cosx - y * sinx;
 			double v2 = x * sinx + y * cosx;
 			x = u2;
 			y = v2;
 
 			v2 = y * cosy - z * cosb;
 			z = y * cosb + z * cosy;
 
 			x = u2;
 			y = v2;
 
 			rotated[i] = new double[] { z, x, y };
 		}
 
 		return rotated;
 	}
 }
