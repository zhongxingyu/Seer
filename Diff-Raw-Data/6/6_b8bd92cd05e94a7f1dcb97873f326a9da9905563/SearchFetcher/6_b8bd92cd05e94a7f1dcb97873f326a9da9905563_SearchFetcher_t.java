 /**
  * 
  */
 package eu.fbk.dycapo.factories.json;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 import eu.fbk.dycapo.exceptions.DycapoException;
 import eu.fbk.dycapo.models.Search;
 import eu.fbk.dycapo.models.Trip;
 
 /**
  * @author riccardo
  * 
  */
 public abstract class SearchFetcher {
 	private static final String TAG = "SearchFetcher";
 
 	public static final Search fetchSearch(JSONObject jsonobj)
 			throws DycapoException {
 		Search search = new Search();
 		try {
 			if (jsonobj.has(Search.AUTHOR))
 				search.setAuthor(PersonFetcher.fetchPerson(jsonobj
 						.getJSONObject(Search.AUTHOR)));
 
 			if (jsonobj.has(Search.DESTINATION))
 				search.setDestination(LocationFetcher.fetchLocation(jsonobj
 						.getJSONObject(Search.DESTINATION)));
 
 			if (jsonobj.has(Search.ORIGIN))
 				search.setOrigin(LocationFetcher.fetchLocation(jsonobj
 						.getJSONObject(Search.ORIGIN)));
 
 			if (jsonobj.has(Search.TRIPS)) {
				search.setTrips(TripFetcher.extractTrips(jsonobj.getJSONArray(Search.TRIPS)));
 			}
 			if (jsonobj.has(DycapoObjectsFetcher.HREF))
 				search.setHref(jsonobj.getString(DycapoObjectsFetcher.HREF));
 
 		} catch (JSONException e) {
 			Log.e(TAG, e.getMessage());
 			e.printStackTrace();
 		}
 		return search;
 	}
 }
