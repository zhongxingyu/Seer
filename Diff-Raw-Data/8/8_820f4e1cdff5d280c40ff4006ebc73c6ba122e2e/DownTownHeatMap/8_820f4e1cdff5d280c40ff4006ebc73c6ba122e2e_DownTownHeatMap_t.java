 package ai.ilikeplaces.logic.Listeners.widgets;
 
 import ai.ilikeplaces.doc.License;
 import ai.ilikeplaces.entities.PrivateEvent;
 import ai.ilikeplaces.logic.crud.DB;
 import ai.ilikeplaces.logic.hotspots.Hotspot;
 import ai.ilikeplaces.logic.hotspots.HotspotAnalyzer;
 import ai.ilikeplaces.logic.hotspots.Rawspot;
 import ai.ilikeplaces.logic.modules.Modules;
 import ai.ilikeplaces.logic.validators.unit.BoundingBox;
 import ai.ilikeplaces.logic.validators.unit.GeoCoord;
 import ai.ilikeplaces.logic.validators.unit.HumanId;
 import ai.ilikeplaces.logic.validators.unit.Info;
 import ai.ilikeplaces.servlets.Controller;
 import ai.ilikeplaces.util.*;
 import com.google.gdata.data.geo.impl.W3CPoint;
 import net.sf.oval.Validator;
 import org.itsnat.core.ItsNatDocument;
 import org.itsnat.core.ItsNatServletRequest;
 import org.itsnat.core.event.NodePropertyTransport;
 import org.itsnat.core.html.ItsNatHTMLDocument;
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.w3c.dom.Element;
 import org.w3c.dom.events.Event;
 import org.w3c.dom.events.EventListener;
 import org.w3c.dom.events.EventTarget;
 import org.w3c.dom.html.HTMLDocument;
 import twitter4j.org.json.JSONException;
 
 import java.util.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: <a href="http://www.ilikeplaces.com"> http://www.ilikeplaces.com </a>
  * Date: Sep 10, 2010
  * Time: 5:07:59 PM
  */
 
 @License(content = "This code is licensed under GNU AFFERO GENERAL PUBLIC LICENSE Version 3")
 public class DownTownHeatMap extends AbstractWidgetListener {
 
     private static final String WOEIDUPDATE_TOKEN = "WOEIDUPDATE_TOKEN";
     private static final String OPEN_BRACKET = "(";
     private static final String CLOSE_BRACKET = ")";
     private static final String SINGLE_QUOTE = "'";
     private static final String CLOSE_BRACE = "}";
     private static final String SEMI_COLON = ";";
     private static final String OPEN_BRACE = "{";
     private static final String DownTownHeatMapWOEIDUpdate =
             "\nDownTownHeatMapWOEIDUpdate = function" + OPEN_BRACKET + "lat,lng" + CLOSE_BRACKET + OPEN_BRACE + "document.getElementById(" + SINGLE_QUOTE + WOEIDUPDATE_TOKEN + SINGLE_QUOTE + CLOSE_BRACKET + ".value = " + SINGLE_QUOTE + "' + lat + ',' + lng" + SEMI_COLON + " document.getElementById" + OPEN_BRACKET + SINGLE_QUOTE + WOEIDUPDATE_TOKEN + SINGLE_QUOTE + CLOSE_BRACKET + ".focus" + OPEN_BRACKET + CLOSE_BRACKET + SEMI_COLON + " return document.getElementById(" + SINGLE_QUOTE + WOEIDUPDATE_TOKEN + SINGLE_QUOTE + CLOSE_BRACKET + SEMI_COLON + CLOSE_BRACE + "\n";
 
     private static final String BBUPDATE_TOKEN = "BBUPDATE_TOKEN";
     private static final String DownTownHeatMapBBUpdate =
             "\nDownTownHeatMapBBUpdate = function" + OPEN_BRACKET + "swlat,swlng,nelat,nelng" + CLOSE_BRACKET + OPEN_BRACE + "document.getElementById(" + SINGLE_QUOTE + BBUPDATE_TOKEN + SINGLE_QUOTE + CLOSE_BRACKET + ".value = " + SINGLE_QUOTE + "' + swlat + ',' + swlng + ',' + nelat + ',' + nelng" + SEMI_COLON + " document.getElementById" + OPEN_BRACKET + SINGLE_QUOTE + BBUPDATE_TOKEN + SINGLE_QUOTE + CLOSE_BRACKET + ".focus" + OPEN_BRACKET + CLOSE_BRACKET + SEMI_COLON + " return document.getElementById(" + SINGLE_QUOTE + BBUPDATE_TOKEN + SINGLE_QUOTE + CLOSE_BRACKET + SEMI_COLON + CLOSE_BRACE + "\n";
     private static final String COMMA = ",";
     private static final String X1 = CLOSE_BRACKET + ", ";
     private static final String COLON = ":";
     private static final String TITLE = "title" + COLON + SINGLE_QUOTE;
     private static final String MAP_MAP = "map" + COLON + " map, ";
     private static final String ICON_GET_COLORED_MARKER_WITH_INTENSITY = "icon" + COLON + " getColoredMarkerWithIntensity" + OPEN_BRACKET;
     private static final String X2 = CLOSE_BRACKET + "  " + CLOSE_BRACE + "))" + SEMI_COLON;
 
     private static final com.google.places.api.impl.ClientFactory GOOGLE_API_CLIENT_FACTORY = Modules.getModules().getGooglePlacesAPIFactory();
     private static final String LOCATION_JSON_OBJ_KEY = "location";
     private static final String LOCATION = LOCATION_JSON_OBJ_KEY;
     private static final String RADIUS = "radius";
     private static final String PLACE_TYPES = "types";
     private static final String COMMON_PLACE_TYPES = "airport%7Camusement_park%7Caquarium%7Cart_gallery%7Catm%7Cbakery%7Cbank%7Cbar%7Cbeauty_salon%7Cbook_store%7Cbowling_alley%7Cbus_station%7Ccafe%7Ccampground%7Ccar_rental%7Ccasino%7Cchurch%7Ccity_hall%7Cclothing_store%7Cembassy%7Cfire_station%7Cflorist%7Cfood%7Cgas_station%7Cgym%7Chair_care%7Chindu_temple%7Cjewelry_store%7Clibrary%7Clodging%7Cmeal_delivery%7Cmeal_takeaway%7Cmosque%7Cmovie_rental%7Cmovie_theater%7Cmuseum%7Cnight_club%7Cpark%7Cparking%7Cpet_store%7Cplace_of_worship%7Cpolice%7Crestaurant%7Crv_park%7Cschool%7Cshoe_store%7Cshopping_mall%7Cspa%7Cstadium%7Csubway_station%7Csynagogue%7Ctaxi_stand%7Ctrain_station%7Ctravel_agency%7Cuniversity%7Czoo";
     private static final String SENSOR = "sensor";
     private static final String TRUE = "true";
     private static final String LIST_OF_HOT_SPOTS_UNSHIFT_NEW_GOOGLE_MAPS_MARKER = "listOfHotSpots.unshift" + OPEN_BRACKET + "new google.maps.Marker(" + OPEN_BRACE + " ";
     private static final String POSITION_NEW_GOOGLE_MAPS_LAT_LNG = "position" + COLON + " new google.maps.LatLng" + OPEN_BRACKET;
     private static final String GOOGLE_MAPS_EVENT_ADD_LISTENER = "google.maps.event.addListener";
     private static final String GOOGLE_MAPS_EVENT_ADD_LISTENER_LIST_OF_HOT_SPOTS_0_CLICK_FUNCTION = GOOGLE_MAPS_EVENT_ADD_LISTENER + OPEN_BRACKET + "listOfHotSpots[0], " + SINGLE_QUOTE + "click', function(" + CLOSE_BRACKET + " " + OPEN_BRACE + "\n";
     private static final String X3 = SINGLE_QUOTE + ", ";
     private static final String X4 = CLOSE_BRACE + CLOSE_BRACKET + SEMI_COLON;
     private static final String GOOGLE_PLACES_JSON_ENDPOINT = "https" + COLON + "//maps.googleapis.com/maps/api/place/search/json";
     private static final String FAILED_TO_FETCH_GOOGLE_PLACES_DATA = "Failed to fetch Google Places Data";
     private static final String RESULTS = "results";
     private static final String GEOMETRY = "geometry";
     private static final String LAT = "lat";
     private static final String LNG = "lng";
     private static final String NAME = "name";
     private static final double APPROX_LENGTH_OF_ONE_LAT = 111325;
     private static final String SQUOTE = SINGLE_QUOTE;
     private static final String URL_SPACE = "%20";
     private static final String SPACE = " ";
     private static final String NOTIFY_USER_POSITIVE = "setNotificationFetchPositive" + OPEN_BRACKET + CLOSE_BRACKET + SEMI_COLON;
     private static final String NOTIFY_USER_NEGATIVE = "setNotificationFetchNegative" + OPEN_BRACKET + CLOSE_BRACKET + SEMI_COLON;
     private static final String FUNCTION = "function";
     private static final String HOT_SPOT_CLICKED = "hotSpotClicked";
     private static final String LATITUDE = "latitude";
     private static final String LONGITUDE = "longitude";
     private static final String URL = "url";
     private static final String LIST_OF_HOT_SPOTS_0 = "listOfHotSpots[0]";
     private static final String CLICK = "click";
     private static final String MOUSEOVER = "mouseover";
     private static final String SET_NOTIFICATION = "setNotification";
     private static final String HOT_SPOT_MOUSE_ENTER = "hotSpotMouseEnter";
     private static final String COMMON_NAME = "commonName";
     private static final String PROCESS_MARKER = "processMarker";
 
 
     private Element elementToUpdateWithWOEID;
     private HumanId humanId;
 
     /**
      * @param request__                request__
      * @param appendToElement__        appendToElement__
      * @param elementToUpdateWithWOEID elementToUpdateWithWOEID
      * @param humanId__                humanId__
      */
     public DownTownHeatMap(final ItsNatServletRequest request__, final Element appendToElement__, final Element elementToUpdateWithWOEID, final String humanId__) {
         super(request__, Controller.Page.DownTownHeatMap, appendToElement__, elementToUpdateWithWOEID, humanId__);
     }
 
     @Override
     protected void init(final Object... initArgs) {
 
         elementToUpdateWithWOEID = (Element) initArgs[0];
         humanId = new HumanId((String) initArgs[1]);
 
         itsNatDocument_.addCodeToSend(
                 DownTownHeatMapWOEIDUpdate.replace(
                         WOEIDUPDATE_TOKEN,
                         $$(Controller.Page.DownTownHeatMapWOEID).getAttribute(MarkupTag.GENERIC.id()))
         );
 
         itsNatDocument_.addCodeToSend(
                 DownTownHeatMapBBUpdate.replace(
                         BBUPDATE_TOKEN,
                         $$(Controller.Page.DownTownHeatMapBB).getAttribute(MarkupTag.GENERIC.id()))
         );
     }
 
     /**
      * Use ItsNatHTMLDocument variable stored in the AbstractListener class
      * Do not call this method anywhere, just implement it, as it will be
      * automatically called by the constructor
      *
      * @param itsNatHTMLDocument_
      * @param hTMLDocument_
      */
     @Override
     protected void registerEventListeners(final ItsNatHTMLDocument itsNatHTMLDocument_, final HTMLDocument hTMLDocument_) {
 
         itsNatHTMLDocument_.addEventListener((EventTarget) $$(Controller.Page.DownTownHeatMapWOEID), EventType.BLUR.toString(), new EventListener() {
 
             final Validator v = new Validator();
             RefObj<String> woeid;
 
             @Override
             public void handleEvent(final Event evt_) {
                 woeid = new Info(((Element) evt_.getCurrentTarget()).getAttribute(MarkupTag.TEXTAREA.value()));
 
                 if (woeid.validate(v) == 0) {
                     elementToUpdateWithWOEID.setAttribute(MarkupTag.INPUT.value(), woeid.getObjectAsValid());
                     //
                     // clear($$(PrivateLocationCreateCNotice));
                 } else {
 //                    $$(PrivateLocationCreateCNotice).setTextContent(woeid.getViolationAsString());
                 }
             }
         }, false, new NodePropertyTransport(MarkupTag.TEXTAREA.value()));
 
 
         itsNatHTMLDocument_.addEventListener((EventTarget) $$(Controller.Page.DownTownHeatMapBB), EventType.BLUR.toString(), new EventListener() {
 
             final Validator v = new Validator();
             final GeoCoord geoCoordSW = new GeoCoord();
             final GeoCoord geoCoordNE = new GeoCoord();
             final ItsNatDocument ind = itsNatDocument_;
             final HumanId myhumanId = humanId;
 
             @Override
             public void handleEvent(final Event evt_) {
 
                 final GeoCoord[] geoCoord = GeoCoord.getGeoCoordsByBounds(((Element) evt_.getCurrentTarget()).getAttribute(MarkupTag.TEXTAREA.value()));
 
                 if (geoCoord[0].validate(v) == 0 && geoCoord[1].validate(v) == 0) {
                     elementToUpdateWithWOEID.setAttribute(MarkupTag.INPUT.value(), geoCoord[0].toString() + COMMA + geoCoord[1]);
 
                     /////////////////////////////
                     final Double latitudeLB = geoCoord[0].getObjectAsValid().getLatitude();
                     final Double latitudeTR = geoCoord[1].getObjectAsValid().getLatitude();
                     final Double longitudeLB = geoCoord[0].getObjectAsValid().getLongitude();
                     final Double longitudeTR = geoCoord[1].getObjectAsValid().getLongitude();
 
                     final Set<Rawspot> rs = new HashSet<Rawspot>() {
                         /*StaticBlockInsideSetToAddElementsEasily*/ {
 
                             UCILikePlacesPlaces:
                             {
                                 final List<PrivateEvent> privateEvents__ = DB.getHumanCrudPrivateEventLocal(true).doRPrivateEventsByBoundsAsSystem(
                                         latitudeLB,
                                         latitudeTR,
                                         longitudeLB,
                                         longitudeTR).returnValue();
 
                                 for (final PrivateEvent privateEvent : privateEvents__) {
                                     add(
                                             new Rawspot(
                                                     new W3CPoint(privateEvent.getPrivateLocation().getPrivateLocationLatitude(), privateEvent.getPrivateLocation().getPrivateLocationLongitude()),
                                                     privateEvent.getPrivateLocation().getPrivateLocationName()));
                                 }
                             }
 
                             UCGooglePlaces:
                             {
 
                                 try {
                                     final double latitudeAverage = (latitudeLB +
                                             latitudeTR) / 2;
                                     final double longitudeAverage = (longitudeLB +
                                             longitudeTR) / 2;
 
                                     final JSONObject placesJsonObject = getGooglePlaces(
                                             latitudeAverage,
                                             longitudeAverage,
                                             (long) (Math.abs(latitudeAverage - latitudeLB) * APPROX_LENGTH_OF_ONE_LAT), COMMON_PLACE_TYPES);
 
                                     final JSONArray placesJsonArray = placesJsonObject.getJSONArray(RESULTS);
 
                                     for (int i = 0; i < placesJsonArray.length(); i++) {
                                         final JSONObject lngLat = placesJsonArray.getJSONObject(i).getJSONObject(GEOMETRY).getJSONObject(LOCATION_JSON_OBJ_KEY);
                                         add(
                                                 new Rawspot(
                                                         new W3CPoint(Double.parseDouble(lngLat.getString(LAT)), Double.parseDouble(lngLat.getString(LNG))),
                                                         placesJsonArray.getJSONObject(i).getString(NAME).toLowerCase()));
                                     }
 
                                 } catch (final Exception e) {
                                     Loggers.ERROR.error(FAILED_TO_FETCH_GOOGLE_PLACES_DATA, e);
                                 }
                             }
                         }
                     };
 
                     final BoundingBox bb = new BoundingBox().setObj(
                             latitudeLB,
                             longitudeLB,
                             latitudeTR,
                             longitudeTR);
 
                     final HotspotAnalyzer hsa = new HotspotAnalyzer(rs, (BoundingBox) bb.validateThrowAndGetThis());
 
 //                    final Map<Integer, Map<Integer, Hotspot>> hotspots = hsa.getHotspots();
                     final Hotspot[][] hotspots = hsa.getHotspots();
 
                     boolean foundAnySpots = false;
 
 
                     for (final Hotspot[] hotspotspitch : hotspots) {
                         for (final Hotspot yaw : hotspotspitch) {
                             final W3CPoint coords = yaw.getCoordinates();
 
                             if (yaw.getCoordinates() != null) {
 
                                 foundAnySpots = true;
 
                                 final Double latitude = coords.getLatitude();
                                 final Double longitude = coords.getLongitude();
 
                                 final String commonName = yaw.getCommonName();
 
                                 final long hits = yaw.getHits();
 
                                 generateMarker(latitude, longitude, commonName, hits);
 
                                 generateMarkerEvents(coords, commonName, hits);
 
                                 $$sendJSStmt(PROCESS_MARKER + OPEN_BRACKET +
                                         (OPEN_BRACE +
                                                 (COMMON_NAME + COLON + SINGLE_QUOTE + commonName + SINGLE_QUOTE) + COMMA +
                                                 (LATITUDE + COLON + coords.getLatitude()) + COMMA +
                                                (LONGITUDE + COLON + coords.getLongitude()) + COMMA +
                                                 (URL + COLON + SINGLE_QUOTE + new Parameter(Controller.Page.Organize.getURL()).append(Controller.Page.DocOrganizeCategory, 143, true).append(WOEIDGrabber.WOEHINT, coords.getLatitude() + COMMA + coords.getLongitude()).get() + SINGLE_QUOTE) +
                                                 CLOSE_BRACE) +
                                         CLOSE_BRACKET
                                 );
                             }
                         }
                     }
 
 
 //                    for (int i = 0; i < hotspots.size(); i++) {
 //                        for (int j = 0; j < hotspots.get(i).size(); j++) {
 //                            final W3CPoint coords = hotspots.get(i).get(j).getCoordinates();
 //
 //                            if (coords != null) {
 //                                final Double latitudeLB = coords.getLatitude();
 //                                final Double longitude = coords.getLongitude();
 //
 //                                final String commonName = hotspots.get(i).get(j).getCommonName();
 //
 //                                final long hits = hotspots.get(i).get(j).getHits();
 //
 //                                generateMarker(latitudeLB, longitude, commonName, hits);
 //
 //                                generateMarkerEvents(coords,commonName,hits);
 //
 //                            }
 //                        }
 //                    }
 
                     if (foundAnySpots) {
                         $$sendJSStmt(NOTIFY_USER_POSITIVE);
                     } else {
                         $$sendJSStmt(NOTIFY_USER_NEGATIVE);
                     }
 
 
                 } else {
 
                 }
             }
 
             @Override
             public void finalize() throws Throwable {
                 Loggers.finalized(this.getClass().getName());
                 super.finalize();
             }
         }, false, new NodePropertyTransport(MarkupTag.TEXTAREA.value()));
 
 
     }
 
     private void generateMarker(Double latitude, Double longitude, String commonName, long hits) {
         itsNatDocument_.addCodeToSend(LIST_OF_HOT_SPOTS_UNSHIFT_NEW_GOOGLE_MAPS_MARKER +
                 POSITION_NEW_GOOGLE_MAPS_LAT_LNG
                 + latitude
                 + COMMA
                 + longitude
                 + X1 +
                 TITLE + commonName + X3 +
                 MAP_MAP +
                 ICON_GET_COLORED_MARKER_WITH_INTENSITY + hits + COMMA + SQUOTE + commonName.replace(SPACE, URL_SPACE) + SQUOTE + X2);
     }
 
     private void generateMarkerEvents(final W3CPoint coords, String commonName, long hits) {
 
         $$sendJSStmt(generateGoogleMarkerEvent(
                 LIST_OF_HOT_SPOTS_0,
                 CLICK,
                 (HOT_SPOT_CLICKED + OPEN_BRACKET +
                         (OPEN_BRACE +
                                 (LATITUDE + COLON + coords.getLatitude()) + COMMA +
                                (LONGITUDE + COLON + coords.getLongitude()) + COMMA +
                                 (URL + COLON + SINGLE_QUOTE + new Parameter(Controller.Page.Organize.getURL()).append(Controller.Page.DocOrganizeCategory, 143, true).append(WOEIDGrabber.WOEHINT, coords.getLatitude() + COMMA + coords.getLongitude()).get() + SINGLE_QUOTE) +
                                 CLOSE_BRACE) + CLOSE_BRACKET + SEMI_COLON)
         ));
         $$sendJSStmt(generateGoogleMarkerEvent(
                 LIST_OF_HOT_SPOTS_0,
                 MOUSEOVER,
                 (HOT_SPOT_MOUSE_ENTER + OPEN_BRACKET +
                         (OPEN_BRACE +
                                 (COMMON_NAME + COLON + SINGLE_QUOTE + commonName + SINGLE_QUOTE) + COMMA +
                                 (LATITUDE + COLON + coords.getLatitude()) + COMMA +
                                (LONGITUDE + COLON + coords.getLongitude()) +
                                 CLOSE_BRACE) +
                         CLOSE_BRACKET +
                         SEMI_COLON)
         ));
 
     }
 
 
     public static JSONObject getGooglePlaces(final double latitude, final double longitude, final long radiusInMeters, final String placeTypes) throws JSONException {
         final Map<String, String> params = new HashMap<String, String>();
         params.put(LOCATION, latitude + "," + longitude);
         params.put(RADIUS, Double.toString(radiusInMeters));
         params.put(PLACE_TYPES, placeTypes);
         params.put(SENSOR, TRUE);
         return GOOGLE_API_CLIENT_FACTORY.getInstance(GOOGLE_PLACES_JSON_ENDPOINT).get("", params);
     }
 
     private String generateGoogleMarkerEvent(final String elementToWhichToAssociateEvent, final String eventType, final String javascriptCallbackFunctionContent) {
         return GOOGLE_MAPS_EVENT_ADD_LISTENER + OPEN_BRACKET + elementToWhichToAssociateEvent + COMMA + SINGLE_QUOTE + eventType + SINGLE_QUOTE + COMMA + FUNCTION + OPEN_BRACKET + CLOSE_BRACKET + OPEN_BRACE +
                 javascriptCallbackFunctionContent +
                 CLOSE_BRACE + CLOSE_BRACKET + SEMI_COLON;
 
     }
 }
 
 //                            /*
 //                            //Below is the working javascript code put here for reference. Do not delete.
 //                            listOfHotSpots.unshift(
 //                                new google.maps.Marker({
 //                                      position: myLatlng,
 //                                      map: map,
 //                                      title:"2",
 //                                      icon:"http://chart.apis.google.com/chart?chst=d_simple_text_icon_below&chld=Point%20this%20Marker|14|000|glyphish_map-marker|16|bb77ee|892e40"
 //                                })
 //                            );
 //                            */
