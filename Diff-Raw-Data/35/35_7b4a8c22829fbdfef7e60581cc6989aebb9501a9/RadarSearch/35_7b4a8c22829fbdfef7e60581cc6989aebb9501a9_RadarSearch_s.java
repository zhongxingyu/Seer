 package io.github.axxiss.places.request;
 
 import io.github.axxiss.places.callback.PlacesCallback;
 import io.github.axxiss.places.enums.Params;
 import io.github.axxiss.places.enums.Request;
 
 /**
  * Created by alexis on 25/10/13.
  */
 public class RadarSearch extends BaseSearch {
 
     /**
      * The Google Places API Radar Search Service allows you to search for up to 200 Places at once,
      * but with less detail than is typically returned from a Text Search or Nearby Search request.
      * With Radar Search, you can create applications that help users identify specific areas of
      * interest within a geographic area. The search response will include up to 200 Places,
      * identified only by their geographic coordinates and Place reference. You can send a Place
      * Details request for more information about any of the Places in the response.
      *
      * @param lat    The latitude around which to retrieve Place information.
      * @param lng    The longitude around which to retrieve Place information.
      * @param radius Defines the distance (in meters) within which to return Place results. The
     *               maximum allowed radius is 50 000 meters.
      * @return
      */
     public RadarSearch(final double lat, final double lng, int radius) {
         super(Request.RadarSearch);
         params.put(Params.Location, PlaceParams.buildLocation(lat, lng));
         params.put(Params.Radius, radius);
     }
 
     public RadarSearch setKeyword(String value) {
         params.put(Params.Keyword, value);
         return this;
     }
 
     public RadarSearch setName(String value) {
         params.put(Params.Name, value);
         return this;
     }
 
     @Override
     public void sendRequest(PlacesCallback callback) {
         PlacesClient.sendRequest(Request.RadarSearch, params, callback);
     }
 }
