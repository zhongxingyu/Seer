 package com.aciertoteam.osm.model;
 
 import org.apache.commons.lang.StringUtils;
 
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * @author Bogdan Nechyporenko
  */
 public abstract class StaticOsmRequest implements OsmRequest {
 
     private static final String STATIC_MAP = "http://open.mapquestapi.com/staticmap/v4/getmap";
     private final ImageType imageType = ImageType.PNG;
     private final Size size;
     private final int zoom;
     private final Coordinate center;
     private final List<Coordinate> markers = new ArrayList<Coordinate>();
 
     protected StaticOsmRequest(Size size, int zoom, Coordinate center) {
         this.size = size;
         this.zoom = zoom;
         this.center = center;
     }
 
     public Size getSize() {
         return size;
     }
 
     public int getZoom() {
         return zoom;
     }
 
     public Coordinate getCenter() {
         return center;
     }
 
     public List<Coordinate> getMarkers() {
         return Collections.unmodifiableList(markers);
     }
 
     @SuppressWarnings("unchecked")
     public <T extends OsmRequest> T addMarker(Coordinate coordinate) {
        if (coordinate != null) {
            markers.add(coordinate);
        }
         return (T) this;
     }
 
     @Override
     public String getUrl(String appKey) {
         return MessageFormat.format("{0}?key={1}&size={2}&zoom={3}&center={4}&imageType={5}&scalebar=false{6}",
                 STATIC_MAP, appKey, size, zoom, center, imageType, getMarkersToString());
     }
 
     private String getMarkersToString() {
         return markers.isEmpty() ? "" : "&pois=" + StringUtils.join(markers, "|");
     }
 }
