 package cpsc310.client;
 
 import java.util.Stack;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.JsArray;
 import com.google.gwt.maps.client.InfoWindowContent;
 import com.google.gwt.maps.client.MapOptions;
 import com.google.gwt.maps.client.MapWidget;
 import com.google.gwt.maps.client.InfoWindowContent.InfoWindowTab;
 import com.google.gwt.maps.client.control.LargeMapControl3D;
 import com.google.gwt.maps.client.event.MapClickHandler;
 import com.google.gwt.maps.client.event.MarkerClickHandler;
 import com.google.gwt.maps.client.geocode.Geocoder;
 import com.google.gwt.maps.client.geocode.LatLngCallback;
 import com.google.gwt.maps.client.geocode.LocationCallback;
 import com.google.gwt.maps.client.geocode.Placemark;
 import com.google.gwt.maps.client.geom.LatLng;
 import com.google.gwt.maps.client.geom.Point;
 import com.google.gwt.maps.client.overlay.Icon;
 import com.google.gwt.maps.client.overlay.Marker;
 import com.google.gwt.maps.client.overlay.MarkerOptions;
 import com.google.gwt.maps.client.overlay.PolyEditingOptions;
 import com.google.gwt.maps.client.overlay.PolyStyleOptions;
 import com.google.gwt.maps.client.overlay.Polygon;
 import com.google.gwt.maps.client.streetview.LatLngStreetviewCallback;
 import com.google.gwt.maps.client.streetview.Pov;
 import com.google.gwt.maps.client.streetview.StreetviewClient;
 import com.google.gwt.maps.client.streetview.StreetviewPanoramaOptions;
 import com.google.gwt.maps.client.streetview.StreetviewPanoramaWidget;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.reveregroup.gwt.facebook4gwt.ShareButton;
 
 public class PropertyMap {
 	private MapWidget map;
 	private MapOptions mOptions;
 	private StreetviewPanoramaWidget panorama;
 	private StreetviewClient svClient;
 	// keep a stack of all the markers
 	private Stack<Marker> markers = new Stack<Marker>();
 	private Geocoder geocoder;
 
 	private LoginServiceAsync loginService = GWT.create(LoginService.class);
 
 	// polygon settings
 	private String color = "#FF0000";
 	private double opacity = 0.1;
 	private int weight = 1;
 	private boolean fillFlag = true;
 	private Polygon lastPolygon = null;
 	private boolean specifyingRegion = false;
 
 	/**
 	 * Constructor Instantiates two instances of a map - standard and streetview
 	 * 
 	 * @param location
 	 *            - location you want the map centered on
 	 * 
 	 */
 
 	public PropertyMap(LatLng location) {
 		buildStreetViewMap(location);
 		buildStandardMap(location);
 
 		map.addMapClickHandler(new MapClickHandler() {
 			public void onClick(MapClickEvent e) {
 				LatLng point = e.getLatLng();
 				if (specifyingRegion) {
 					//clear the region first
 					clearMap();
 					clearMarkers();
 					
 					drawSquare(point);
 					// allow only one square to be drawn at a time
 					setSpecifyingRegion(false);
 				}
 			}
 		});
 
 	}
 
 	/**
 	 * Sets up the streetview map centered on provided location
 	 * 
 	 * @param location
 	 *            - the location to center the map on
 	 */
 	public void buildStreetViewMap(LatLng location) {
 		StreetviewPanoramaOptions options = StreetviewPanoramaOptions
 				.newInstance();
 		options.setLatLng(location);
 		svClient = new StreetviewClient();
 		panorama = new StreetviewPanoramaWidget(options);
 		panorama.setSize("100%", "100%");
 	}
 
 	/**
 	 * Sets up the standard map centered on provided location
 	 * 
 	 * @param location
 	 *            - the location to center the map on
 	 */
 	public void buildStandardMap(LatLng location) {
 		mOptions = MapOptions.newInstance();
 		map = new MapWidget();
 		map.setCenter(location, 10);
 		map.setSize("100%", "100%");
 		// Add some controls for the zoom level
 		map.addControl(new LargeMapControl3D());
 	}
 
 	
 	/**
 	 * Changes streetview location given location coordinates
 	 * 
 	 * @param location
 	 *            latitude and longitude
 	 */
 
 	private void refreshStreetView(LatLng location) {
 		svClient.getNearestPanoramaLatLng(location,
 				new LatLngStreetviewCallback() {
 					@Override
 					public void onFailure() {
 						// streetview is not available
 					}
 
 					@Override
 					public void onSuccess(LatLng point) {
 						panorama.setLocationAndPov(point, Pov.newInstance());
 					}
 				});
 	}
 	
 	/**
 	 * Changes streetview location given location coordinates
 	 * 
 	 * @param location
 	 *            latitude and longitude
 	 */
 
 	public void refreshStreetView(String location) {
 		LatLngCallback callback = new LatLngCallback() {
 
 			public void onFailure() {
 				// Window.alert("Location not found");
 			}
 
 			public void onSuccess(LatLng point) {
 				// refresh streetview with this location
 				svClient.getNearestPanoramaLatLng(point,
 						new LatLngStreetviewCallback() {
 							@Override
 							public void onFailure() {
 								// streetview is not available
 							}
 
 							@Override
 							public void onSuccess(LatLng point) {
 								panorama.setLocationAndPov(point, Pov
 										.newInstance());
 							}
 						});
 			}
 		};
 		geocoder = new Geocoder();
 		geocoder.getLatLng(location + " VANCOUVER, BC", callback);
 	}
 
 	/**
 	 * Clears all overlays from the map
 	 */
 	public void clearMap() {
 		map.clearOverlays();
 		if (lastPolygon != null)
 			lastPolygon = null;
 	}
 
 	/**
 	 * Clears all of the markers from the map
 	 */
 	public void clearMarkers() {
 
 		while (!markers.empty()) {
 			map.removeOverlay(markers.pop());
 		}
 	}
 
 	/**
 	 * Returns reference to the standard map
 	 */
 	public MapWidget getMap() {
 		return this.map;
 	}
 
 	/**
 	 * Returns reference to the streetView map
 	 */
 	public StreetviewPanoramaWidget getStreetViewMap() {
 		return this.panorama;
 	}
 
 	
 	/**
 	 * Finds the location and plots it on the map changes the street-view too
 	 * 
 	 * @param location
 	 *            - string representation of the address
 	 * @param placeMarker
 	 *            - true if you want to place a marker on map and update
 	 *            streetview, false otherwise.
 	 * @return returns LatLng point
 	 * 
 	 */
 	public LatLng findLocation(final HouseData house, final boolean placeMarker) {
 		final LatLngWrapper llWrap = new LatLngWrapper();
 
 		LatLngCallback callback = new LatLngCallback() {
 
 			public void onFailure() {
 				Window.alert("Location not found");
 			}
 
 			public void onSuccess(LatLng point) {
 				// add the location onto the map
 				if (placeMarker) {
 					compileMarker(point, house);
 				}
 				llWrap.setResponse(point);
 			}
 		};
 		geocoder = new Geocoder();
 		geocoder.getLatLng(house.getAddress() + " VANCOUVER, BC", callback);
 
 		return llWrap.getLL();
 	}
 
 	/**
 	 * 
 	 * Compiles the marker with the infowindow and adds it to the map
 	 * 
 	 * @param point latlng point
 	 * @param house houseData object
 	 * 
 	 */
 	private void compileMarker(final LatLng point, final HouseData house) {
 		Icon icon = setIcon(house.getIsSelling());
 		MarkerOptions options = MarkerOptions.newInstance();
 		options.setIcon(icon);
 		final Marker marker = new Marker(point, options);
 		map.addOverlay(marker);
 		map.setCenter(point);
 		
 		if (house.getIsSelling()) {
 			AsyncCallback<LoginInfo> userCallback = new AsyncCallback<LoginInfo>() {
 				public void onFailure(Throwable caught) {
 					Window.alert("error trying to get user: propertyMap.java");
 				}
 
 				public void onSuccess(LoginInfo user) {
 					// Assemble the info window
 					final InfoWindowContent content = buildInfoWindow(user, house);
 					map.getInfoWindow().open(marker, content);
 
 					// Click handler for each marker
 					marker.addMarkerClickHandler(new MarkerClickHandler() {
 						public void onClick(MarkerClickEvent event) {
 							try {
 								map.getInfoWindow().open(marker, content);
 								refreshStreetView(point);
 							} catch (Exception e) {
 								Window.alert(e.getMessage());
 							}
 
 						}
 					});
 					markers.push(marker);
 				}
 			};
 			// Window.alert("getting user from db: " + house.getOwner());
 			loginService.getUser(house.getOwner(), userCallback);
 
 		} else // house is not being sold
 		{
 			// Assemble the info window
 			final InfoWindowContent content = buildInfoWindow(null, house);
 			map.getInfoWindow().open(marker, content);
 
 			// Click handler for each marker
 			marker.addMarkerClickHandler(new MarkerClickHandler() {
 				public void onClick(MarkerClickEvent event) {
 					try {
 						map.getInfoWindow().open(marker, content);
 						refreshStreetView(point);
 					} catch (Exception e) {
 						Window.alert(e.getMessage());
 					}
 
 				}
 			});
 			markers.push(marker);
 		}
 		refreshStreetView(point);
 	}
 	
 	/**
 	 * Assembles the info window for a marker given the house point
 	 * 
 	 * @param house
 	 *            HouseData point
 	 * @param realtor
 	 *            LoginInfo - the realtor in charge of the house
 	 * 
 	 * @return InfoWindowContent either with one or two tabs
 	 */
 	private InfoWindowContent buildInfoWindow(LoginInfo realtor, HouseData house) {
 		InfoWindowContent iw;
 		FlowPanel firstTab = getHouseInfoMarkerPanel(house);
 		// Show additional information if the house is being sold
 		if (house.getIsSelling()) {
 			FlowPanel secondTab = getContactInfoMarkerPanel(realtor);
 			iw = getInfoWindowTabs(firstTab, secondTab);
 		} else
 			iw = new InfoWindowContent(firstTab);
 
 		return iw;
 	}
 
 	/**
 	 * if a property is on sale, the icon is set to real estate icon otherwise
 	 * the icon is set to a normal red marker
 	 * 
 	 * 
 	 * @param onSale
 	 * @return Icon
 	 */
 	private Icon setIcon(boolean onSale) {
 		// Set the icon
 		Icon icon;
 		String onSaleIconImgLink = "images/MapStuff/realestate.png";
 		String onSaleIconShadowLink = "images/MapStuff/realestate.shadow.png";
 		String normalIconImgLink = "images/MapStuff/red-dot.png";
 		String normalIconShadowLink = "images/MapStuff/msmarker.shadow.png";
 
 		// marker is a for sale sign if it's on sale, red otherwise
 		if (onSale) {
 			icon = Icon.newInstance(onSaleIconImgLink);
 			icon.setShadowURL(onSaleIconShadowLink);
 		} else {
 			icon = Icon.newInstance(normalIconImgLink);
 			icon.setShadowURL(normalIconShadowLink);
 		}
 		icon.setIconAnchor(Point.newInstance(6, 20));
 		icon.setInfoWindowAnchor(Point.newInstance(5, 1));
 
 		return icon;
 	}
 
 	/**
 	 * 
 	 * Takes in two vertical panels and puts them together
 	 * 
 	 * @param firstTab
 	 *            first tab content
 	 * @param secondTab
 	 *            second tab content
 	 * 
 	 */
 
 	private InfoWindowContent getInfoWindowTabs(FlowPanel firstTab,
 			FlowPanel secondTab) {
 
 		InfoWindowTab tabs[] = new InfoWindowTab[2];
 
 		tabs[0] = new InfoWindowTab("Info", firstTab);
 		tabs[1] = new InfoWindowTab("Contact", secondTab);
 		final InfoWindowContent content = new InfoWindowContent(tabs, 0);
 		return content;
 	}
 
 	/**
 	 * 
 	 * Takes in house data object and adds house info into panel
 	 * 
 	 * @param house
 	 *            the house object
 	 * 
 	 */
 
 	private FlowPanel getHouseInfoMarkerPanel(HouseData house) {
 		FlowPanel markerInfoWindow = new FlowPanel();
 		HTML htmlWidget;
 		// If the house is on sale, provide extra field for sale price and
 		if (house.getIsSelling()) {
 			htmlWidget = new HTML(
 					"<div class = 'wordwrap'><p><b><u>Property Information</u></b></br> "
 							+ "<b>Address: </b>"
 							+ house.getAddress().toLowerCase() + "</br>"
 							+ "<b>Current Land Value: </b>"
 							+ house.getCurrentLandValue() + "</br>"
 							+ "<b>Year built: </b>" + house.getYearBuilt()
 							+ "</br>" + "<b>Selling Price: </b>"
 							+ house.getPrice() + "</br>" + "</p></div>");
 		} else { // show less information if it's not on sale
 			htmlWidget = new HTML(
 					"<div class = 'wordwrap'><p><b><u>Property Information</u></b></br> "
 							+ "<b>Address: </b>"
 							+ house.getAddress().toLowerCase() + "</br>"
 							+ "<b>Current Land Value: </b>"
 							+ house.getCurrentLandValue() + "</br>"
 							+ "<b>Year built: </b>" + house.getYearBuilt()
 							+ "</p></div>");
 		}
 
 		markerInfoWindow.add(htmlWidget);
 
 		String shareURL = generateShareURL(house);
 		ShareButton shareBtn = new ShareButton(shareURL, "");
 		markerInfoWindow.add(shareBtn);
 
 		return markerInfoWindow;
 	}
 
 	 /**
 	 *
 	 * Get Realtor information. Assumes the house is being sold and realtor
 	 * information is available
 	 *
 	 * @param theUser  the realtor in charge of
 	 * the house object
 	 * @return flowPanel containing the marker InfoWindow
 	 *
 	 */
 	private FlowPanel getContactInfoMarkerPanel(LoginInfo theUser) {
 		FlowPanel markerInfoWindow = new FlowPanel();
 		final HTML htmlWidget;
 		LoginInfo user = theUser;
 
 		String email;
 		String realtor;
 		long phoneNumber;
 		String website;
 		String description;
 		String phone;
 		
 		if (user != null) {
 			email = user.getEmailAddress();
 			realtor = user.getNickname();
 			phoneNumber = user.getphoneNumber();
 			website = user.getWebsite();
 			description = user.getDescription();
 			// empty strings if null 
 			if (phoneNumber == 0 || phoneNumber < 10) phone = "";
 			else phone = phoneNumber + "";
 			if (website == null || website.length() < 1) website = "";
 			if (description == null) description = "";
 		} 
 		else {
 			email = "";
 			realtor = "";
 			phone = "";
 			website = "";
 			description = "";
 		}
		htmlWidget = new HTML("<div class = 'wordwrap'><p><b><u>Contact Information</u></b></br> "
 				+ "<b>Realtor: </b>" + realtor + "</br>" + "<b>Email: </b>"
 				+ email + "</br>" + "<b>Phone: </b>" + phone + "</br>"
 				+ "<b>Website: </b>" + website + "</br>" + "<b>About: </b>"
				+ description + "</br>" + "</p></div>");
 
 		markerInfoWindow.add(htmlWidget);
 		markerInfoWindow.setWidth("150px");
 		return markerInfoWindow;
 	}
 
 	/**
 	 * 
 	 * Allows the user to edit the last drawn polygon
 	 * 
 	 */
 	public void editPolygon() {
 		if (lastPolygon == null) {
 			return;
 		}
 		lastPolygon.setEditingEnabled(PolyEditingOptions.newInstance(4));
 	}
 
 	/**
 	 * deletes the specified region on the map
 	 */
 	public void clearSpecifiedRegion() {
 		if (lastPolygon != null) {
 			map.removeOverlay(lastPolygon);
 			lastPolygon = null;
 		}
 	}
 
 	/**
 	 * Allows the user to draw a region on the map
 	 * 
 	 */
 	public void createPolygon() {
 		PolyStyleOptions style = PolyStyleOptions.newInstance(color, weight,
 				opacity);
 
 		final Polygon poly = new Polygon(new LatLng[0], color, weight, opacity,
 				color, fillFlag ? .7 : 0.0);
 		lastPolygon = poly;
 		map.addOverlay(poly);
 		poly.setDrawingEnabled();
 		poly.setStrokeStyle(style);
 	}
 
 	/**
 	 * 
 	 * Algorithm that calculates whether or not the point is in the polygon
 	 * 
 	 * @param point
 	 *            the point to check if it is in the polygon
 	 * @return true if the point is in the polygon, false otherwise
 	 * 
 	 */
 	boolean isPointInPolygon(LatLng point) {
 
 		if (lastPolygon == null) {
 			Window.alert("No region specified");
 			return false;
 		}
 
 		int j = 0;
 		boolean oddNodes = false;
 		double y = point.getLatitude();
 		double x = point.getLongitude();
 		int numVertexes = lastPolygon.getVertexCount();
 
 		for (int i = 0; i < numVertexes; i++) {
 			j++;
 			if (j == numVertexes) {
 				j = 0;
 			}
 			if (((lastPolygon.getVertex(i).getLatitude() < y) && (lastPolygon
 					.getVertex(j).getLatitude() >= y))
 					|| ((lastPolygon.getVertex(j).getLatitude() < y) && (lastPolygon
 							.getVertex(i).getLatitude() >= y))) {
 				if (lastPolygon.getVertex(i).getLongitude()
 						+ (y - lastPolygon.getVertex(i).getLatitude())
 						/ (lastPolygon.getVertex(j).getLatitude() - lastPolygon
 								.getVertex(i).getLatitude())
 						* (lastPolygon.getVertex(j).getLongitude() - lastPolygon
 								.getVertex(i).getLongitude()) < x) {
 					oddNodes = !oddNodes;
 				}
 			}
 		}
 
 		if (oddNodes)
 			Window.alert("point is in the polygon");
 		else
 			Window.alert("point is not in the polygon");
 		return oddNodes;
 	}
 
 	/**
 	 * 
 	 * Draws a rectangle on the map given a corner point
 	 * 
 	 * @param point
 	 *            location of the corner of the rectangle
 	 * 
 	 */
 	public void drawSquare(LatLng point) {
 		PolyStyleOptions style = PolyStyleOptions.newInstance(color, weight,
 				opacity);
 		// the other four points of the triangle
 		LatLng point1 = LatLng.newInstance(point.getLatitude(), point
 				.getLongitude() + 0.1);
 		LatLng point2 = LatLng.newInstance(point.getLatitude() - 0.02, point
 				.getLongitude() + 0.1);
 		LatLng point3 = LatLng.newInstance(point.getLatitude() - 0.02, point
 				.getLongitude());
 
 		LatLng[] polygonPoints = new LatLng[5];
 		polygonPoints[0] = point;
 		polygonPoints[1] = point1;
 		polygonPoints[2] = point2;
 		polygonPoints[3] = point3;
 		polygonPoints[4] = point;
 
 		final Polygon poly = new Polygon(polygonPoints, color, weight, opacity,
 				color, fillFlag ? .7 : 0.0);
 		lastPolygon = poly;
 		map.addOverlay(poly);
 		poly.setStrokeStyle(style);
 		lastPolygon.setEditingEnabled(PolyEditingOptions.newInstance(5));
 
 	}
 
 	/**
 	 * 
 	 * Get the longitude values of the specified polygon
 	 * 
 	 * @pre there is a polygon defined on the map
 	 * 
 	 * @return an array of latitude values, null if no polygon was defined
 	 * 
 	 * 
 	 */
 	public double[] getPolyLat() {
 		if (lastPolygon == null)
 			return null;
 		int numVertexes = lastPolygon.getVertexCount();
 		double[] polyPoints = new double[numVertexes];
 		LatLng point;
 
 		for (int i = 0; i < lastPolygon.getVertexCount(); i++) {
 			point = lastPolygon.getVertex(i);
 			polyPoints[i] = point.getLatitude();
 		}
 
 		return polyPoints;
 	}
 
 	/**
 	 * 
 	 * get the longitude values of the specified polygon
 	 * 
 	 * @pre there is a polygon defined on the map
 	 * @return an array of longitude values
 	 * 
 	 * 
 	 */
 	public double[] getPolyLng() {
 		if (lastPolygon == null)
 			return null;
 		int numVertexes = lastPolygon.getVertexCount();
 		double[] polyPoints = new double[numVertexes];
 		LatLng point;
 
 		for (int i = 0; i < lastPolygon.getVertexCount(); i++) {
 			point = lastPolygon.getVertex(i);
 			polyPoints[i] = point.getLongitude();
 		}
 		return polyPoints;
 	}
 
 	/**
 	 * 
 	 * setter method for map click handler when drawing rectangle
 	 * 
 	 * @param specifyingRegion
 	 *            if the user is specifying a region, it's true
 	 * 
 	 */
 	public void setSpecifyingRegion(boolean specifyingRegion) {
 		this.specifyingRegion = specifyingRegion;
 	}
 
 	/**
 	 * 
 	 * access to the private boolean value specifyingRegion
 	 * 
 	 * @param specifyingRegion
 	 *            if the user is specifying a region, it's true
 	 * 
 	 */
 	public boolean isSpecifyingRegion() {
 		return specifyingRegion;
 	}
 
 	/**
 	 * Method to paratermize the base URL with a given house's civic number and
 	 * street name. Civic number stored in variable cn and street name is stored
 	 * in variable sn.
 	 * 
 	 * @pre house != null;
 	 * @post true;
 	 * @param house
 	 *            - the HouseData object to get the civic number and street name
 	 *            from.
 	 * @return the string containing the base URL with the civic number and
 	 *         street name of the given house as parameters.
 	 */
 	private String generateShareURL(HouseData house) {
 		String baseURL = GWT.getHostPageBaseURL();
 		String civicNumber = String.valueOf(house.getCivicNumber());
 		String streetName = house.getStreetName().toUpperCase();
 
 		// parameterize URL and add house parameters to the URL
 		String shareURL = baseURL + "?cn=" + civicNumber + "&sn=" + streetName;
 
 		return shareURL;
 	}
 
 	/**
 	 * 
 	 * Wrapper classes so we can grab data from async calls
 	 * 
 	 */
 
 	// Wrapper class for latlng
 	class LatLngWrapper {
 		LatLng theLatLng;
 
 		void setResponse(LatLng ll) {
 			this.theLatLng = ll;
 		}
 
 		LatLng getLL() {
 			return theLatLng;
 		}
 	}
 
 
 
 }
