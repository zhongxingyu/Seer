 package moses.client.com.requests;
 
 import java.util.List;
 
 import moses.client.com.NetworkJSON;
 import moses.client.com.ReqTaskExecutor;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * This class is used for setting filter it provides some basic methods for this
  * 
  * @author Zijad
  * 
  */
 public class RequestSetFilter {
 	public static boolean filterSetOnServer(JSONObject j) throws JSONException {
 		if (j.getString("STATUS").equals("SUCCESS")) {
 			return true;
 		}
 		return false;
 	}
 
 	private JSONObject j;
 
 	ReqTaskExecutor e;
 
 	public RequestSetFilter(ReqTaskExecutor e, String sessionID,
 			String deviceID, String filter) {
 		j = new JSONObject();
 		this.e = e;
 		try {
 			j.put("MESSAGE", "SET_FILTER");
 			j.put("SESSIONID", sessionID);
 			j.put("DEVICEID", deviceID);
			j.put("FILTER", filter);
 		} catch (JSONException ex) {
 			e.handleException(ex);
 		}
 	}
 
 	public void send() {
 		NetworkJSON task = new NetworkJSON();
 		NetworkJSON.APIRequest req;
 		req = task.new APIRequest();
 		req.request = j;
 		req.e = this.e;
 		task.execute(req);
 	}
 }
