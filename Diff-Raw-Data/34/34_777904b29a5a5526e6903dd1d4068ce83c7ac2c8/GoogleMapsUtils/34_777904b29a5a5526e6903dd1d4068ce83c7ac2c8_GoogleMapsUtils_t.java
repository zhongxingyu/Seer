 package org.jrecruiter.common;
 
 import java.math.BigDecimal;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 
 public class GoogleMapsUtils {
 
     public static final String googleMapsStaticUrl = "http://maps.google.com/staticmap";
     public static final Integer defaultWidth       = Integer.valueOf(400);
     public static final Integer defaultHeight      = Integer.valueOf(300);
 
    public static final BigDecimal defaultMapLatitude  = BigDecimal.valueOf(33.74);
    public static final BigDecimal defaultMapLongitude = BigDecimal.valueOf(-84.38);
    public static final Integer    defaultMapZoomLevel = Integer.valueOf(10);

     public enum GoogleMapsUrlParameterNames {
 
         CENTER("center"),
         ZOOM("zoom"),
         KEY("key"),
         SIZE("size"),
         SENSOR("sensor"),
         MARKERS("markers");
 
         private String parameterNames;
 
         private GoogleMapsUrlParameterNames(final String parameterNames) {
             this.parameterNames = parameterNames;
         }
 
         public String getParameterName() {
             return parameterNames;
         }
     }
 
     public enum GoogleMapsColorNames {
 
         BLACK("black"),
         BROWN("brown"),
         GREEN("green"),
         PURPLE("purple"),
         YELLOW("yellow"),
         BLUE("blue"),
         GRAY("gray"),
         ORANGE("orange"),
         RED("red"),
         WHITE("white");
 
         private String colorName;
 
         private GoogleMapsColorNames(final String colorName) {
             this.colorName = colorName;
         }
 
         public String getColorName() {
             return colorName;
         }
     }
 
     public enum GoogleMapsMarkerSize {
 
         TINY("tiny"),
         MID("mid"),
         SMALL("small");
 
         private String markerSize;
 
         private GoogleMapsMarkerSize(final String markerSize) {
             this.markerSize = markerSize;
         }
 
         public String getMarkerSize() {
             return markerSize;
         }
     }
 
     public static String buildSizeParameterValue(Integer width, Integer height) {
         return width + "x" + height;
     }
 
     public static String buildLatLongParameterValue(BigDecimal latitude, BigDecimal longitude) {
         return String.valueOf(latitude) + "," + String.valueOf(longitude);
     }
 
     public static String buildMarkerParameterValue(BigDecimal latitude, BigDecimal longitude, GoogleMapsMarkerSize size, GoogleMapsColorNames color, Character character) {
         return String.valueOf(latitude) + "," + String.valueOf(longitude) + ","
              + size.markerSize + color.colorName;
     }
 
     public static URL buildGoogleMapsStaticUrl(final BigDecimal latitude,
                                               final BigDecimal longitude,
                                               final Integer zoomLevel,
                                               final String key) {
         final URI uri;
 
         try {
             uri = new URI(GoogleMapsUtils.googleMapsStaticUrl);
         } catch (URISyntaxException e) {
             throw new IllegalStateException("Error creating Google Maps URL.", e);
         }
 
         final UrlEncodedQueryString queryString = UrlEncodedQueryString.parse(uri);
 
         queryString.set(GoogleMapsUrlParameterNames.CENTER.getParameterName(),
                 GoogleMapsUtils.buildLatLongParameterValue(latitude, longitude));
         queryString.set(GoogleMapsUrlParameterNames.ZOOM.getParameterName(), zoomLevel);
         queryString.set(GoogleMapsUrlParameterNames.SIZE.getParameterName(),
                 GoogleMapsUtils.buildSizeParameterValue(GoogleMapsUtils.defaultWidth, GoogleMapsUtils.defaultHeight));
         queryString.set(GoogleMapsUrlParameterNames.KEY.getParameterName(), key);
         queryString.set(GoogleMapsUrlParameterNames.MARKERS.getParameterName(),
                         GoogleMapsUtils.buildMarkerParameterValue(latitude, longitude,
                                                                   GoogleMapsMarkerSize.MID,
                                                                   GoogleMapsColorNames.ORANGE,
                                                                   Character.valueOf('J')));
         queryString.set(GoogleMapsUrlParameterNames.SENSOR.getParameterName(), "false");
 
         try {
             return queryString.apply(uri).toURL();
         } catch (MalformedURLException e) {
             throw new IllegalStateException("Malformed URL.", e);
         }
     }
 
 
 }
