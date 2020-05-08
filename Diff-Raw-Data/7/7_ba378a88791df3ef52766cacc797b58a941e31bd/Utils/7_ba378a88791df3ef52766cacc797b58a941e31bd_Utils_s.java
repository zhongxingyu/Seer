 package party.mood.meter;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.Bundle;
 
 public class Utils {
   public static final String SITE_ROOT = "http://partymoodmeter.herokuapp.com";
   
   public static Bundle convertJSONObjectToBundle(JSONObject obj) throws JSONException {
     Bundle result = new Bundle();
     JSONArray names = obj.names();
     for(int i = 0; i < names.length(); i++) {
       String name = names.getString(i);
       Object value = obj.get(name);
       if(value instanceof String)
         result.putString(name, (String)value);
       else if(value instanceof Integer)
         result.putInt(name, (Integer)value);
       else if(value instanceof Boolean)
         result.putBoolean(name, (Boolean)value);
       else {
         /* ignore */
       }
     }
     return result;
   }
   
   // http://en.wikipedia.org/wiki/Numerical_differentiation
   // http://upload.wikimedia.org/wikipedia/en/math/6/d/c/6dc717fa5e96fdad5d91afa7957190e4.png
   public static double differentiateFive(double h, double[] points) {
     double numer = -points[4] + 8*points[3] - 8*points[1] + points[0],
           denom = 12*h,
           const_ = (Math.pow(h, 4)/30.0) * Math.pow(points[2], 5);
    return (numer/denom) + const_;
   }
 }
 
