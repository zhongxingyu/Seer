 package sg.edu.nus.ami.wifilocation.api;
 
 import java.lang.reflect.Type;
 import java.util.Vector;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.reflect.TypeToken;
 
 /**
  * Create NUSGeoloc java API for AP based location web service
  * 
  * @author qinfeng
  * 
  */
 public class NUSGeoloc {
 	/**
 	 * A private String member URL directs to the AP based location web service
 	 */
 	private static final String url = "http://172.18.101.125:8080/api/AP";
 
 	/**
 	 * get the location based on the detected Access Point with the strongest
 	 * signal
 	 * 
 	 * @param mac
 	 *            the MAC address of the Access Point
 	 * @return an APLocation which is a location class for a WIFI Access Point
 	 */
 	public Vector<APLocation> getLocationBasedOnAP(Vector<String> mac) {
 		int length = mac.size();
 		Vector<Double> strength = new Vector<Double>(length);
 		for (int i = 0; i < length; i++) {
 			strength.add(new Double(0.0));
 		}
 
 		return getLocationBasedOnAP(mac, strength);
 	}
 
 	/**
 	 * get the location based on the detected Access Point with the strongest
 	 * signal
 	 * 
 	 * @param mac
 	 *            the MAC address of the Access Point
 	 * @param strength
 	 *            a parameter for calculate the accuracy ( To be developed )
 	 * @return an APLocation which is a location class for a WIFI Access Point
 	 */
 
 	public Vector<APLocation> getLocationBasedOnAP(Vector<String> mac,
 			Vector<Double> strength) {
 		Vector<APLocation> apLocation = new Vector<APLocation>();
 
 		try {
 			RestClient client = new RestClient(url);
 			int n = mac.size();
 			client.AddParam("number", String.valueOf(n));
 
 			for (int i = 0; i < n; i++) {
 				client.AddParam("mac" + i, mac.get(i));
 				client.AddParam("strength" + i, String.valueOf(strength.get(i)));
 			}
 			client.AddParam("output", "json");
 			client.Execute(RequestMethod.GET);
 			String response = client.getResponse();
 
			// return an empty vector rather than null
 			if (response == null)
 				return apLocation;
 
 			Gson gson = new GsonBuilder().serializeNulls().create();
			Type T = new TypeToken<Vector<APLocation>>() {
			}.getType();
 			apLocation = gson.fromJson(response, T);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return apLocation;
 
 	}
 
 	public String getUrl() {
 		return url;
 	}
 }
